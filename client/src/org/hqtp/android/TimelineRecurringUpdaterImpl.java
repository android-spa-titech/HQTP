package org.hqtp.android;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import roboguice.inject.ContextSingleton;

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
    private ScheduledFuture<?> scheduled_future;
    private Set<TimelineObserver> observers = new HashSet<TimelineObserver>();

    @Override
    public void stop() {
        scheduled_future.cancel(true);
    }

    @Override
    public void registerTimelineObserver(TimelineObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterTimelineObserver(TimelineObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void startRecurringUpdateTimeline() {
        ScheduledExecutorService timeline_thread = Executors.newSingleThreadScheduledExecutor();
        scheduled_future = timeline_thread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    if (lecture_id != -1) {
                        List<Post> posts = proxy.getTimeline(lecture_id);
                        if (!scheduled_future.isCancelled()) {
                            notify_update(posts);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, timeline_update_period, TimeUnit.MILLISECONDS);
    }

    private void notify_update(List<Post> posts) {
        for (TimelineObserver observer : observers) {
            observer.onUpdate(posts);
        }
    }

    @Override
    public void setLectureId(int lecture_id) {
        this.lecture_id = lecture_id;
    }
}
