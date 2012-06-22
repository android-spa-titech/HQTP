package org.hqtp.android;


import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import roboguice.activity.RoboActivity;

import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

public class ListQuestionActivity extends RoboActivity {

    @InjectView(R.id.listQuestion)
    ListView questionListView;

    @Inject
    HQTPProxy proxy;

    static List<Question> questionList = new ArrayList<Question>();
    static QuestionAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        adapter = new QuestionAdapter( this, R.layout.question_item, questionList);
        questionListView.setAdapter(adapter);
 
        // リストの要素をクリックされたときの挙動
        questionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick( AdapterView<?> parent, View view, int position, long id ){
               ListView listView = (ListView)parent;
               Question q = (Question)listView.getItemAtPosition(position);
               showAlert(q.getTitle(), q.getBody());
           }
        });

        GetQuestion gq = new GetQuestion();
        try {
            gq.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class GetQuestion extends RoboAsyncTask<List<Question>> {

        @Override
        public List<Question> call() throws Exception {
            return proxy.getQuestions();
        }

        @Override
        protected void onSuccess(List<Question> questions) {

            if (questions == null) {
                showAlert( "GetQuestion", "質問がありません。" );
            } else {
                for (Question q : questions) {
                    questionList.add( q );
                }
            }
            // スクロールを確認するため追加
            for( int i = 0 ; i < 20 ; i++ )
                questionList.add( new Question( "タイトル"+i, "本文"+i, "作者"+i ) );

            adapter.notifyDataSetChanged();
        }
    }

    private class QuestionAdapter extends ArrayAdapter<Question> {
             
        private int resourceId;

        public QuestionAdapter(Context context, int resource, List<Question> questions) {
            super(context, resource, questions);
            resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // メモリを無駄に使わないように
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resourceId, null);
            }
            
            Question question = (Question)getItem(position);

            TextView titleview = (TextView)convertView.findViewById(R.id.question_title);
            TextView bodyview  = (TextView)convertView.findViewById(R.id.question_body);

            titleview.setText( question.getTitle() );
            // 文字数が多いと全文をそのまま表示するとよくないかも
            bodyview.setText( question.getBody() );

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
