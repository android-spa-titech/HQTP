package org.hqtp.android;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

public class TimelineActivity extends RoboActivity {

    @InjectView(R.id.listPost)
    ListView timelineListView;
    @InjectView(R.id.buttonUpdate)
    Button updateButton;
    @Inject
    HQTPProxy proxy;

    public static final String LECTURE_ID = "LECTURE_ID";

    private int lectureId;
    private TimelineAdapter adapter;

    private ScheduledFuture<?> scheduled_future;
    private final long timeline_update_period = 500;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline);

        lectureId = getIntent().getIntExtra(LECTURE_ID, -1);
        assert lectureId != -1;

        adapter = new TimelineAdapter(this, R.layout.post_item);
        timelineListView.setAdapter(adapter);

        // リストの要素をクリックされたときの挙動
        timelineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                Post p = (Post) listView.getItemAtPosition(position);
                // TODO(draftcode): Should show something. おそらくは投稿者の名前をタイトルに表示すべき
                showAlert("投稿", p.getBody());
            }
        });

        updateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                loadTimeline();
            }
        });

        startRecurringUpdateTimeline();
    }

    @Override
    protected void onStop() {
        scheduled_future.cancel(true);
        super.onStop();
    }

    private void loadTimeline() {
        GetTimeline gt = new GetTimeline();
        gt.execute();
    }

    private void updateTimelineAdaptor(List<Post> posts) {
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

    private void startRecurringUpdateTimeline() {
        // recurring update
        // TODO: 定期的なタイムライン更新を行うかどうかをテストケースで弄れるようにすべき。
        ScheduledExecutorService timeline_thread = Executors.newSingleThreadScheduledExecutor();
        scheduled_future = timeline_thread.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Post> posts = proxy.getTimeline(lectureId);
                    if (!scheduled_future.isCancelled()) {
                        updateTimelineAdaptor(posts);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, timeline_update_period, TimeUnit.MILLISECONDS);
    }

    private class GetTimeline extends RoboAsyncTask<List<Post>> {

        @Override
        public List<Post> call() throws Exception {
            return proxy.getTimeline(lectureId);
        }

        @Override
        protected void onSuccess(List<Post> posts) {

            if (posts == null) {
                showAlert("GetTimeline", "投稿がありません。");
            } else {
                updateTimelineAdaptor(posts);
            }
        }

        @Override
        protected void onException(Exception e) {
            showAlert("GetTimeline", "サーバーとの通信に失敗しました。");
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

            // 文字数が多いと全文をそのまま表示するとよくないかも
            bodyView.setText(post.getBody());

            return convertView;
        }
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
    }
}
