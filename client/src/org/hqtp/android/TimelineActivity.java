package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

public class TimelineActivity extends RoboActivity {
    @InjectView(R.id.listPost)
    ListView timelineListView;
    @InjectView(R.id.lectureName)
    TextView lectureNameTextView;// TODO: id変えたい・・・
    @InjectView(R.id.profileView)
    ProfileView profileView;
    @Inject
    TimelineRecurringUpdater updater;
    @Inject
    TimelineAdapter adapter;
    @Inject
    ImageLoader imageLoader;

    public static final String LECTURE_ID = "LECTURE_ID";
    public static final String LECTURE_NAME = "LECTURE_NAME";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline);

        String lectureName = getIntent().getStringExtra(LECTURE_NAME);
        assert lectureName != null;
        lectureNameTextView.setText(lectureName);

        int lectureId = getIntent().getIntExtra(LECTURE_ID, -1);
        assert lectureId != -1;
        updater.setLectureId(lectureId);
        adapter.setLectureId(lectureId);

        timelineListView.setAdapter(adapter);
        timelineListView.setOnItemLongClickListener(
            adapter.new ItemLongClickListener());
    }

    @Override
    protected void onStart() {
        Log.d("HQTP", "onStart");
        super.onStart();
        updater.registerTimelineObserver(adapter);
        updater.startRecurringUpdateTimeline();
        profileView.startRecurringUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updater.stop();
        updater.unregisterTimelineObserver(adapter);
        profileView.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        imageLoader.shutdown();
        super.onDestroy();
    }
}
