package org.hqtp.android;

import java.util.List;

import com.google.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PostQuestionActivity extends RoboActivity implements
        OnClickListener {

    @InjectView(R.id.post_button)
    Button post_button;
    @InjectView(R.id.cancel_buttton)
    Button cancel_button;
    @Inject
    HQTPProxy proxy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);
        post_button.setOnClickListener(this);
        cancel_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.post_button:
            PostQuestion pq = new PostQuestion();
            pq.execute();
            break;
        case R.id.cancel_buttton:
            finish();
            break;
        default:
            break;
        }
    }

    private class PostQuestion extends RoboAsyncTask<List<Question>> {
        @Override
        public List<Question> call() throws Exception {
            String title = ((EditText) findViewById(R.id.title_text)).getText().toString();
            String body = ((EditText) findViewById(R.id.body_text)).getText().toString();
            proxy.postQuestion(title, body);
            return null;
        }
        @Override
        protected void onSuccess(List<Question> t) throws Exception {
            super.onSuccess(t);
            finish();
        }

    }
}
