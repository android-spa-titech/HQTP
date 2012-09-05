package org.hqtp.android;

import java.util.List;

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

public class ListQuestionActivity extends RoboActivity {

    @InjectView(R.id.listQuestion)
    ListView timelineListView;
    @InjectView(R.id.buttonUpdate)
    Button updateButton;

    @Inject
    HQTPProxy proxy;

    private TimelineAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        adapter = new TimelineAdapter(this, R.layout.question_item);
        timelineListView.setAdapter(adapter);

        // リストの要素をクリックされたときの挙動
        timelineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                Post p = (Post) listView.getItemAtPosition(position);
                showAlert("投稿", p.getBody()); //TODO: おそらくは投稿者の名前をタイトルに表示すべき
            }
        });

        updateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                loadTimeline();
            }
        });

        loadTimeline();
    }

    // 質問をリストに読み込み
    private void loadTimeline() {
        GetTimeline gt = new GetTimeline();//TODO: 講義IDを指定する
        gt.execute();
    }

    private class GetTimeline extends RoboAsyncTask<List<Post>> {

        @Override
        public List<Post> call() throws Exception {
            return proxy.getTimeline(1);//TODO: 講義IDを指定する
        }

        @Override
        protected void onSuccess(List<Post> posts) {

            if (posts == null) {
                showAlert("GetTimeline", "質問がありません。");
            } else {
                adapter.clear();
                // Robolectric dopostes not implement addAll,
                // so we use add method and a loop instead for the time being.
                // https://github.com/pivotal/robolectric/issues/281
                for (Post p : posts) {
                    adapter.add(p);
                }
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onException(Exception e) {
            e.printStackTrace();
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
            //TODO: post-item.xmlに合わせる

            TextView titleView = (TextView) convertView.findViewById(R.id.question_title);
            TextView bodyView = (TextView) convertView.findViewById(R.id.question_body);

            titleView.setText("title");//TODO: 件名のかわりに何か表示
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
