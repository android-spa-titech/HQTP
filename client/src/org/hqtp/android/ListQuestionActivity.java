package org.hqtp.android;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

public class ListQuestionActivity extends RoboActivity {

    @InjectView(R.id.listQuestion)
    ListView questionListView;
    
    @InjectView(R.id.question_title)
    EditText titleEditText;
    @InjectView(R.id.question_body)
    EditText bodyEditText;
    @Inject
    HQTPProxy proxy;

    static List<Question> questionList = new ArrayList<Question>();
    static QuestionAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        adapter = new QuestionAdapter( this, R.layout.list, questionList);
        questionListView.setAdapter(adapter);

        // クリックされたとき、選択されたときのコールバックリスナーを登録

        GetQuestion gq = new GetQuestion();
        try {
            gq.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
        // 読み込みをキャンセルしたい場合は何かしないといけない

    }
    

 
    private class GetQuestion extends RoboAsyncTask<List<Question>> {
        @Override
        public List<Question> call() throws Exception {
            return proxy.getQuestions();
        }

        @Override
        protected void onSuccess(List<Question> questions) {
            StringBuilder sb = new StringBuilder();
            if (questions == null) {
                sb.append("質問がありません");
            } else {
                for (Question q : questions) {
                    sb.append(q.getTitle());
                    sb.append("\n");
                    sb.append(q.getBody());
                    sb.append("\n");
                    questionList.add( q );
                }
            }
            Log.d("info", sb.toString());       
            // リストに反映させる
        }
        // onExceptionとかしたほうがいいのか
    }
    

   // BaseAdapterを継承するのがよい？
   private class QuestionAdapter extends ArrayAdapter<Question> {

        private int resourceId;
        private LayoutInflater inflater;

        public QuestionAdapter(Context context, int resource, List<Question> questions) {
            super(context, resource, questions);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); // ここ
            resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Question question = (Question)getItem(position);
            
            // メモリを無駄に使わないようにらしい
            if (convertView == null) {
                convertView = inflater.inflate(resourceId, null);
            }

            TextView titleview = (TextView)convertView.titleEditText;
            TextView bodyview  = (TextView)convertView.bodyEditText;
            titleview.setText( question.getTitle() );
            bodyview.setText( question.getBody() );
            
            // ボタンを付けるときはここでSetOnClickListenerを設定
            
            return convertView;
        }

    }
}
