package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.inject.Inject;

public class AddLectureActivity extends RoboActivity implements
        OnClickListener {

    @InjectView(R.id.lecture_code_text)
    EditText lectureCodeText;
    @InjectView(R.id.lecture_name_text)
    EditText lectureNameText;
    @InjectView(R.id.lecture_add_button)
    Button addButton;
    @InjectView(R.id.lecture_add_cancel_button)
    Button cancelButton;

    @Inject
    HQTPProxy proxy;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lecture);

        addButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.lecture_add_button:
            PostAddLecture addlecture = new PostAddLecture();
            addlecture.execute();
            break;
        case R.id.lecture_add_cancel_button:
            finish();
            break;
        default:
            break;
        }
    }

    private class PostAddLecture extends RoboAsyncTask<Void> {
        @Override
        public Void call() throws Exception {
            String lectureCode = lectureCodeText.getText().toString();
            String lectureName = lectureNameText.getText().toString();
            // APIでリクエスト
            return null;
        }

        @Override
        protected void onSuccess(Void t) throws Exception {
            // 成功したら講義TLへ遷移
            // すでに存在する講義を入力していたらアラートを提示
        }

        @Override
        protected void onException(Exception e) {
            // 通信エラー
        }
    }
}
