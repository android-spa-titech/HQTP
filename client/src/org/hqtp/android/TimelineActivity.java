package org.hqtp.android;

import java.util.List;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

public class TimelineActivity extends RoboActivity implements TimelineObserver {

    @InjectView(R.id.listPost)
    ListView timelineListView;
    @Inject
    TimelineRecurringUpdater updater;
    @Inject
    Alerter alerter;

    @Inject
    ImageLoader image_loader;

    public static final String LECTURE_ID = "LECTURE_ID";

    private int lectureId;
    private TimelineAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline);

        lectureId = getIntent().getIntExtra(LECTURE_ID, -1);
        assert lectureId != -1;

        updater.setLectureId(lectureId);
        updater.registerTimelineObserver(this);

        adapter = new TimelineAdapter(this, R.layout.post_item);
        timelineListView.setAdapter(adapter);

        // リストの要素をクリックされたときの挙動
        timelineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                Post p = (Post) listView.getItemAtPosition(position);
                // TODO(draftcode): Should show something. おそらくは投稿者の名前をタイトルに表示すべき
                alerter.alert("投稿", p.getBody());
            }
        });

        updater.startRecurringUpdateTimeline();
    }

    @Override
    protected void onStop() {
        updater.unregisterTimelineObserver(this);
        updater.stop();
        image_loader.shutdown();
        super.onStop();
    }

    @Override
    public void onUpdate(List<Post> posts) {
        runOnUiThread(new TimelineUpdateTask(posts));
    }

    private class TimelineUpdateTask implements Runnable {
        private List<Post> posts;

        TimelineUpdateTask(List<Post> posts) {
            super();
            this.posts = posts;
        }

        @Override
        public void run() {
            // Robolectric does not implement addAll,
            // so we use add method and a loop instead for the time being.
            // https://github.com/pivotal/robolectric/issues/281
            adapter.clear();
            if (posts != null) {
                for (Post p : posts) {
                    adapter.add(p);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class TimelineAdapter extends ArrayAdapter<Post> {

        private int resourceId;

        public TimelineAdapter(Context context, int resource) {
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

            Post post = (Post) getItem(position);
            TextView bodyView = (TextView) convertView.findViewById(R.id.postContent);

            ImageView image_view = (ImageView) convertView.findViewById(R.id.icon);
            image_view.setTag(post.getUser().getIconURL());
            image_loader.displayImage(image_view, TimelineActivity.this);

            // 文字数が多いと全文をそのまま表示するとよくないかも
            bodyView.setText(post.getBody());

            return convertView;
        }
    }
}
