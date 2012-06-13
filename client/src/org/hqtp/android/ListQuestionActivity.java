package org.hqtp.android;

import java.util.List;

import com.google.inject.Inject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import roboguice.activity.RoboActivity;
import roboguice.util.RoboAsyncTask;

public class ListQuestionActivity extends RoboActivity {

    // グリッド読み込み
    @Inject HQTPProxy proxy;
        
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        GetQuestion gq = new GetQuestion();
        try {
            gq.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
        // 読み込みキャンセルしたい場合は何かしないといけない
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
                }
            }
            Log.d("info", sb.toString());       
            // リストに反映させる
        }
        // onExceptionとかしたほうがいいのか
    }
    
    
   private class QuestionAdapter extends ArrayAdapter<Question> {

        private int resourceId;

        public QuestionAdapter(Context context, int resource, List<Question> questions) {
            super(context, resource, questions);
            resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Question question = (Question)getItem(position);
            
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); // ここ
                convertView = inflater.inflate(resourceId, null);
            }

            TextView titleview = (TextView)convertView.findViewById(R.id.a); // ここ
            TextView bodyview  = (TextView)conveetView.findViewById(R.id.b); // ここ
            titleview.setText( question.getTitle() );
            bodyview.setText( question.getBody() );
            
            return view;
        }

    }
    
}
