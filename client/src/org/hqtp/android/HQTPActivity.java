package org.hqtp.android;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class HQTPActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        View authentication_button = this.findViewById(R.id.authentication_button);
        authentication_button.setOnClickListener(this);
        View getallpost_button = this.findViewById(R.id.getallpost_button);
        getallpost_button.setOnClickListener(this);
        View post_button = this.findViewById(R.id.post_button);
        post_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.authentication_button:
            showAlert("authentication", "authentication_button is clicked");
            // TODO: アクセストークンを取得
            // TODO: HQTPProxy.getInstance().setAccessToken(access_token).authenticate();
            // TODO: 戻り値に応じてダイアログ表示
            break;
        case R.id.getallpost_button:
            GetPostTask gp_task = new GetPostTask();
            gp_task.execute();
            break;
        case R.id.post_button:
            showAlert("post", "post_button is clicked");
            // TODO: 投稿UI表示
            // TODO: HQTPProxy.getInstance().postQuestion(title,body)
            break;
        default:
            break;
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
    
    private class GetPostTask extends AsyncTask<Void,Void,Void> {
        private List<Question> questions;
        @Override
        protected Void doInBackground(Void... params) {
            this.questions = HQTPProxy.getInstance().getQuestions();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            StringBuilder sb = new StringBuilder();
            if (this.questions == null) {
                sb.append("質問がありません＞＜");
            } else {
                for (Question q : this.questions) {
                    sb.append(q.getTitle());
                    sb.append("\n");
                    sb.append(q.getBody());
                    sb.append("\n");
                }
            }
            showAlert("質問一覧", sb.toString());
        }
    }
}