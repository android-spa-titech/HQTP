package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
            String lectureName = lectureNameText.getText().toString();
            String lectureCode = lectureCodeText.getText().toString();
            if (lectureName.isEmpty()) {
                showAlert("AddLectureActivity", "授業名を入力してください。");
            }
            else if (lectureCode.isEmpty()) {
                showAlert("AddLectureActivity", "授業コードを入力してください。");
            } else {
                PostAddLecture addlecture = new PostAddLecture();
                addlecture.execute();
            }
            break;
        case R.id.lecture_add_cancel_button:
            finish();
            break;
        default:
            break;
        }
    }

    private class PostAddLecture extends RoboAsyncTask<Lecture> {

        @Override
        public Lecture call() throws Exception {

            Lecture lecture = new Lecture(
                    0,
                    lectureNameText.getText().toString(),
                    lectureCodeText.getText().toString()
                    );

            // TODO:APIリクエスト
            // return proxy.addLecture( lecture );
            return lecture;
        }

        @Override
        protected void onSuccess(Lecture lecture) throws Exception {
            // TODO:成功したら講義コードをつけて講義TLアクティビティへ遷移
            /*
             * Intent intent = new Intent( addLectureActivity.this, ****.class );
             * intent.putExtra("lectureCode", lectureCode);
             * startActivity( intent );
             */
        }

        @Override
        protected void onException(Exception e) {
            // TODO:すでに存在する講義を入力していたらアラートを提示
            // TODO:通信エラー処理
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
