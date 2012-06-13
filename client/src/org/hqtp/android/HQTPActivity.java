package org.hqtp.android;

import java.util.List;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.inject.Inject;

public class HQTPActivity extends RoboActivity implements OnClickListener {

    @InjectView(R.id.authentication_button) Button authentication_button;
    @InjectView(R.id.getallpost_button)     Button getallpost_button;
    @InjectView(R.id.post_button)           Button post_button;
    @Inject HQTPProxy proxy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        authentication_button.setOnClickListener(this);
        getallpost_button.setOnClickListener(this);
        post_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.authentication_button:
            showAlert("authentication", "authentication_button is clicked");
            // TODO: アクセストークンを取得
            // TODO: proxy.authenticate(access_token_key,access_token_secret);
            // TODO: 戻り値に応じてダイアログ表示
            break;
        case R.id.getallpost_button:
            GetPostTask gp_task = new GetPostTask();
            gp_task.execute();
            break;
        case R.id.post_button:
            startActivity(new Intent(this, PostQuestionActivity.class));
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

    private class GetPostTask extends RoboAsyncTask<List<Question>> {
        @Override
        public List<Question> call() throws Exception {
            return proxy.getQuestions();
        }

        @Override
        protected void onSuccess(List<Question> questions) {
            StringBuilder sb = new StringBuilder();
            if (questions == null) {
                sb.append("質問がありません＞＜");
            } else {
                for (Question q : questions) {
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
