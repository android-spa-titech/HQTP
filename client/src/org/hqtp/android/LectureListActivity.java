package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LectureListActivity extends RoboActivity {

    @InjectView(R.id.lecture_list)
    ListView lectureListView;
    private LectureListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecture_list);
        adapter = new LectureListAdapter(this, R.layout.lecture_item);
        adapter.add(new Lecture(1, "テスト講義", "AAAAA"));

        lectureListView.setAdapter(adapter);
        // TODO 講義リストを更新するためのタスク
        // リストの要素をクリックされたときの挙動
        lectureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                Lecture lecture = (Lecture) listView.getItemAtPosition(position);

                Intent intent = new Intent(LectureListActivity.this, TimelineActivity.class);
                intent.putExtra(TimelineActivity.LECTURE_ID, lecture.getId());
                startActivity(intent);
            }
        });
    }

    private class LectureListAdapter extends ArrayAdapter<Lecture> {

        private int resourceId;

        public LectureListAdapter(Context context, int resource) {
            super(context, resource);
            resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // メモリを無駄に使わないように
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resourceId, null);
            }
            Lecture lecture = getItem(position);
            TextView textView = (TextView) convertView.findViewById(R.id.lectureName);
            textView.setText(lecture.getName());
            return convertView;
        }
    }
}
