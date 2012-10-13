package org.hqtp.android;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import roboguice.inject.ContextSingleton;
import roboguice.util.SafeAsyncTask;

import com.google.inject.Inject;
import com.google.inject.name.Named;

@ContextSingleton
public class TimelineRecurringUpdaterImpl implements TimelineRecurringUpdater {
    @Inject
    HQTPProxy proxy;
    @Inject
    @Named("TimelineUpdatePeriod")
    Long timeline_update_period;

    private int lecture_id;
    private RecurringTaskManager manager;
    private Set<TimelineObserver> observers = new HashSet<TimelineObserver>();

    @Override
    public void registerTimelineObserver(TimelineObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterTimelineObserver(TimelineObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void setLectureId(int lecture_id) {
        this.lecture_id = lecture_id;
    }

    @Override
    public synchronized void startRecurringUpdateTimeline() {
        if (manager == null) {
            manager = new RecurringTaskManager();
            manager.start();
        }
    }

    @Override
    public synchronized void stop() {
        if (manager != null) {
            manager.stop();
            manager = null;
        }
    }

    // RecurringTaskManager is used to avoid unexpected results related to multi-threading.
    // This class is happened to be restart. If you use Executor framework's
    // recurring execution, you might call cancel method in the stop method.
    // But it is difficult to handle InterruptedException with you notifying
    // results to observer in UI thread. This class will use SafeAsyncTask
    // to avoid this complicated exception handling.
    private class RecurringTaskManager {
        private ScheduledExecutorService wait_thread = Executors.newSingleThreadScheduledExecutor();
        private AtomicBoolean stopped = new AtomicBoolean(false);
        private RecurringTask task;

        public void start() {
            task = new RecurringTask();
            task.execute();
        }

        public void stop() {
            stopped.set(true);
            wait_thread.shutdown();
        }

        private class RecurringTask extends SafeAsyncTask<List<Post>> {
            @Override
            public List<Post> call() throws Exception {
                return proxy.getTimeline(lecture_id);
            }

            @Override
            protected void onSuccess(List<Post> posts) throws Exception {
                if (!stopped.get()) {
                    for (TimelineObserver observer : observers) {
                        observer.onUpdate(posts);
                    }
                }
            }

            @Override
            protected void onFinally() {
                if (!stopped.get()) {
                    wait_thread.schedule(new Runnable() {
                        @Override
                        public void run() {
                            // It is probable that this task is stopped during delayed time.
                            if (!stopped.get()) {
                                task = new RecurringTask();
                                task.execute();
                            }
                        }
                    }, timeline_update_period, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
