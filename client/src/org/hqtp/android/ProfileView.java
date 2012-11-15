package org.hqtp.android;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import roboguice.RoboGuice;
import roboguice.util.SafeAsyncTask;

import com.google.inject.Inject;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileView extends LinearLayout {
    private final int UPDATE_INTERVAL = 5;// seconds
    private final String PREFERENCES_NAME = "PROFILE_VIEW_PREF";
    private final String SAVED_SINCE_ID = "SAVED_ACHIEVEMENT_SINCE_ID";
    private ImageView icon_view;
    private TextView username_view;
    private TextView total_point_view;
    private ScheduledExecutorService executor;
    private RecurringTask task;
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private int since_id = 0;
    private AtomicBoolean already_get_user_info = new AtomicBoolean(false);

    @Inject
    ImageLoader loader;
    @Inject
    Activity activity;
    @Inject
    APIClient proxy;
    @Inject
    Alerter alerter;

    public ProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.profileview, this);
        initView();
        updateUserInfo(null);
        RoboGuice.getInjector(context).injectMembers(this);
    }

    private void initView()
    {
        icon_view = (ImageView) findViewById(R.id.userIcon);
        username_view = (TextView) findViewById(R.id.userName);
        total_point_view = (TextView) findViewById(R.id.totalPoint);
    }

    private void updateUserInfo(User user)
    {
        if (user != null) {
            username_view.setText(user.getName());
            updateTotalPoint(user.getTotalPoint());
            icon_view.setTag(user.getIconURL());
            loader.displayImage(icon_view, this.activity);
        } else {
            username_view.setText("--");
            updateTotalPoint(0);
            icon_view.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }

    private void updateTotalPoint(int total_point)
    {
        total_point_view.setText(total_point + " pt");
    }

    private class LoadUserInfoTask extends SafeAsyncTask<User> {
        @Override
        public User call() throws Exception {
            int user_id = proxy.getUserId();
            return proxy.getUser(user_id);
        }

        @Override
        protected void onSuccess(User user) throws Exception {
            updateUserInfo(user);
            already_get_user_info.set(true);
        }
    }

    public synchronized void startRecurringUpdate()
    {
        new LoadUserInfoTask().execute();
        since_id = getPreferences().getInt(SAVED_SINCE_ID, 0);
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
        loader.clearCache();
    }

    private SharedPreferences getPreferences() {
        return activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private class RecurringTask extends SafeAsyncTask<AchievementResponse> {
        @Override
        public AchievementResponse call() throws Exception {
            int user_id = proxy.getUserId();
            return proxy.getAchievements(user_id, since_id);
        }

        @Override
        protected void onSuccess(AchievementResponse achievement_response) throws Exception {
            if (already_get_user_info.get()) {
                updateTotalPoint(achievement_response.getTotalPoint());
            }
            List<Achievement> achievements = achievement_response.getAchievements();
            int size = achievements.size();
            if (size > 0) {

                since_id = achievements.get(size - 1).getId();
                SharedPreferences pref = getPreferences();
                // To avoid duplicate notification, check if since_id is newer.
                boolean already_shown = false;
                synchronized (pref) {
                    int prev_since_id = pref.getInt(SAVED_SINCE_ID, 0);
                    if (prev_since_id < since_id) {
                        pref.edit().putInt(SAVED_SINCE_ID, since_id).commit();
                    } else {
                        already_shown = true;
                    }
                }
                if (!already_shown) {
                    for (Achievement achieve : achievements) {
                        alerter.toastShort(Achievement.getDescription(achieve) + "！ +" + achieve.getPoint());
                    }
                }
            }
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
