package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.content.DialogInterface;
import android.content.Intent;
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
    @Inject
    Alerter alerter;

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
                alerter.alert("AddLectureActivity", "授業名を入力してください。");
            }
            else if (lectureCode.isEmpty()) {
                alerter.alert("AddLectureActivity", "授業コードを入力してください。");
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
        public PostAddLecture() {
            super(AddLectureActivity.this);
        }

        @Override
        public Lecture call() throws Exception {
            return proxy.addLecture(
                    lectureCodeText.getText().toString(),
                    lectureNameText.getText().toString());
        }

        @Override
        protected void onSuccess(Lecture lecture) throws Exception {
            Intent intent = new Intent(AddLectureActivity.this, TimelineActivity.class);
            intent.putExtra(TimelineActivity.LECTURE_ID, lecture.getId());
            intent.putExtra(TimelineActivity.LECTURE_NAME, lecture.getName());
            startActivity(intent);
        }

        @Override
        protected void onException(Exception e) {
            if (e instanceof LectureAlreadyCreatedException) {
                final Lecture lecture = ((LectureAlreadyCreatedException) e).lecture;
                alerter.alert("AddLectureActivity", "既に授業が追加されていました",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(AddLectureActivity.this, TimelineActivity.class);
                                intent.putExtra(TimelineActivity.LECTURE_ID, lecture.getId());
                                intent.putExtra(TimelineActivity.LECTURE_NAME, lecture.getName());
                                startActivity(intent);
                            }
                        });

            } else {
                alerter.alert("AddLectureActivity", "通信エラーです");
            }
        }
    }
}
