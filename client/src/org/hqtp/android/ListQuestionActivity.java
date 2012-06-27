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
    ListView questionListView;
    @InjectView(R.id.buttonUpdate)
    Button updateButton;

    @Inject
    HQTPProxy proxy;

    private QuestionAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        adapter = new QuestionAdapter(this, R.layout.question_item);
        questionListView.setAdapter(adapter);

        // リストの要素をクリックされたときの挙動
        questionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                Question q = (Question) listView.getItemAtPosition(position);
                showAlert(q.getTitle(), q.getBody());
            }
        });

        updateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                loadQuestion();
            }
        });

        loadQuestion();
    }

    // 質問をリストに読み込み
    private void loadQuestion() {
        GetQuestion gq = new GetQuestion();
        gq.execute();
    }

    private class GetQuestion extends RoboAsyncTask<List<Question>> {

        @Override
        public List<Question> call() throws Exception {
            return proxy.getQuestions();
        }

        @Override
        protected void onSuccess(List<Question> questions) {

            if (questions == null) {
                showAlert("GetQuestion", "質問がありません。");
            } else {
                adapter.clear();
                // Robolectric does not implement addAll,
                // so we use add method and a loop instead for the time being.
                // https://github.com/pivotal/robolectric/issues/281
                for (Question q : questions) {
                    adapter.add(q);
                }
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onException(Exception e) {
            showAlert("GetQuestion", "サーバーとの通信に失敗しました。");
        }
    }

    private class QuestionAdapter extends ArrayAdapter<Question> {

        private int resourceId;

        public QuestionAdapter(Context context, int resource) {
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

            Question question = (Question) getItem(position);

            TextView titleView = (TextView) convertView.findViewById(R.id.question_title);
            TextView bodyView = (TextView) convertView.findViewById(R.id.question_body);

            titleView.setText(question.getTitle());
            // 文字数が多いと全文をそのまま表示するとよくないかも
            bodyView.setText(question.getBody());

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
