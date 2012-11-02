package org.hqtp.android;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import roboguice.RoboGuice;
import roboguice.util.SafeAsyncTask;

import com.google.inject.Inject;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileView extends LinearLayout {
    private final int UPDATE_INTERVAL = 5;// seconds
    private ImageView icon_view;
    private TextView username_view;
    private TextView total_point_view;
    private ScheduledExecutorService executor;
    private RecurringTask task;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    @Inject
    ImageLoader loader;
    @Inject
    Activity activity;
    @Inject
    APIClient proxy;

    public ProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.profileview, this);
        initView();
        updateView(null);
        RoboGuice.getInjector(context).injectMembers(this);
    }

    private void initView()
    {
        icon_view = (ImageView) findViewById(R.id.userIcon);
        username_view = (TextView) findViewById(R.id.userName);
        total_point_view = (TextView) findViewById(R.id.totalPoint);
    }

    private void updateView(User user)
    {
        if (user != null) {
            username_view.setText(user.getName());
            total_point_view.setText("? pt");// TODO: show user's point
            icon_view.setTag(user.getIconURL());
            loader.displayImage(icon_view, this.activity);
        } else {
            username_view.setText("--");
            total_point_view.setText("-- pt");
            icon_view.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }

    public synchronized void startRecurringUpdate()
    {
        stopped.set(false);
        executor = Executors.newSingleThreadScheduledExecutor();
        task = new RecurringTask();
        task.execute();
    }

    // When activity's onPause/onStop is called, this method should be called.
    public synchronized void stop()
    {
        if (executor != null) {
            stopped.set(true);
            executor.shutdown();
        }
    }

    private class RecurringTask extends SafeAsyncTask<User> {
        @Override
        public User call() throws Exception {
            int id = proxy.getUserId();
            Log.d("user_id",id+"");
            return null;// TODO: call proxy.getUser(id);
        }

        @Override
        protected void onSuccess(User user) throws Exception {
            updateView(user);
        }

        @Override
        protected void onFinally() throws RuntimeException {
            if (!stopped.get()) {
                ProfileView.this.executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!stopped.get()) {
                            task = new RecurringTask();
                            task.execute();
                        }
                    }
                }, UPDATE_INTERVAL, TimeUnit.SECONDS);
            }
        }
    }
}
