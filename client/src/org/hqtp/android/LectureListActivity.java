package org.hqtp.android;

import java.util.List;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

public class LectureListActivity extends RoboActivity {
    @Inject
    APIClient proxy;
    @Inject
    Alerter alerter;
    @Inject
    LectureListAdapter adapter;
    @InjectView(R.id.lecture_list)
    ListView lectureListView;
    @InjectView(R.id.add_lecture)
    Button addLectureButton;
    @InjectView(R.id.refresh)
    Button refreshButton;
    @InjectView(R.id.profileView)
    ProfileView profileView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecture_list);

        lectureListView.setAdapter(adapter);
        lectureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                Lecture lecture = (Lecture) listView.getItemAtPosition(position);

                Intent intent = new Intent(LectureListActivity.this, TimelineActivity.class);
                intent.putExtra(TimelineActivity.LECTURE_ID, lecture.getId());
                intent.putExtra(TimelineActivity.LECTURE_NAME, lecture.getName());
                startActivity(intent);
            }
        });

        addLectureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LectureListActivity.this, AddLectureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                new RefreshTask().execute();
            }
        });

        new RefreshTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        profileView.startRecurringUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        profileView.stop();
    }

    private static class LectureListAdapter extends ArrayAdapter<Lecture> {
        private static final int resourceId = R.layout.lecture_item;
        @Inject
        LayoutInflater inflater;

        @Inject
        public LectureListAdapter(Context context) {
            super(context, resourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(resourceId, null);
            }
            Lecture lecture = getItem(position);
            TextView textView = (TextView) convertView.findViewById(R.id.lectureName);
            textView.setText(lecture.getName());
            return convertView;
        }
    }

    private class RefreshTask extends RoboAsyncTask<List<Lecture>> {
        public RefreshTask() {
            super(LectureListActivity.this);
        }

        @Override
        public List<Lecture> call() throws Exception {
            return proxy.getLectures();
        }

        @Override
        protected void onSuccess(List<Lecture> lectures) throws Exception {
            super.onSuccess(lectures);
            adapter.clear();
            if (lectures != null) {
                adapter.addAll(lectures);
            }
        }

        @Override
        protected void onException(Exception e) throws RuntimeException {
            super.onException(e);
            alerter.toastShort("講義一覧の取得に失敗しました");
        }

        @Override
        protected void onFinally() {
            refreshButton.setEnabled(true);
        }
    }
}
