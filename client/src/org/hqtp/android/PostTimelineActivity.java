package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

/**
 * Post a text to the timeline.
 * <p>
 * This activity requires a lecture id and a previous virtual timestamp and a next virtual timestamp. If you do not want
 * to specify virtual timestamp, set them to -1.
 * </p>
 * <p>
 * This activity will return RESULT_OK if the text posting succeeds. If the back-button was pressed, this activity will
 * return RECULT_CANCEL. When the posting failed, this activity remains.
 * </p>
 * 
 * @author draftcode
 */
public class PostTimelineActivity extends RoboActivity {
    public static final String LECTURE_ID = "LECTURE_ID";
    public static final String PREV_VIRTUAL_TS = "PREV_VIRTUAL_TS";
    public static final String NEXT_VIRTUAL_TS = "NEXT_VIRTUAL_TS";

    public static final String SAVED_POST_BODY = "SAVED_POST_BODY";

    @Inject
    APIClient proxy;
    @Inject
    Alerter alerter;
    @InjectView(R.id.postBody)
    TextView postBody;
    @InjectView(R.id.postButton)
    Button postButton;
    @InjectView(R.id.profileView)
    ProfileView profileView;
    @InjectExtra(LECTURE_ID)
    int lectureId;
    @InjectExtra(PREV_VIRTUAL_TS)
    long prevVirtualTimestamp;
    @InjectExtra(NEXT_VIRTUAL_TS)
    long nextVirtualTimestamp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_timeline);

        assert ((prevVirtualTimestamp != -1 && nextVirtualTimestamp != -1) || (prevVirtualTimestamp == -1 && nextVirtualTimestamp == -1));

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postBody.getText().length() == 0) {
                    alerter.toastShort("本文を入力してください");
                } else {
                    new PostTask(postBody.getText().toString(),
                            lectureId,
                            prevVirtualTimestamp,
                            nextVirtualTimestamp).execute();
                }
            }
        });

        if (savedInstanceState != null) {
            postBody.setText(savedInstanceState.getString(SAVED_POST_BODY));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        profileView.startRecurringUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        profileView.stop();
    }

    @Override
    public void onBackPressed() {
        // Do not call super. Calling it leads to finish this activity.
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_POST_BODY, postBody.getText().toString());
    }

    /**
     * Used to post a text to the timeline.
     * <p>
     * This class will disable the form and the button. If failed, this class shows alert and re-enables them. If
     * succeed, this class set the activity result and finish it.
     * </p>
     */
    private class PostTask extends RoboAsyncTask<Void> {
        private final String body;
        private final int lectureId;
        private final long prevVirtualTimestamp;
        private final long nextVirtualTimestamp;

        public PostTask(String body, int lectureId, long prevVirtualTimestamp, long nextVirtualTimestamp) {
            super(PostTimelineActivity.this);
            this.body = body;
            this.lectureId = lectureId;
            this.prevVirtualTimestamp = prevVirtualTimestamp;
            this.nextVirtualTimestamp = nextVirtualTimestamp;
        }

        @Override
        protected void onPreExecute() throws Exception {
            PostTimelineActivity.this.postBody.setEnabled(false);
            PostTimelineActivity.this.postButton.setEnabled(false);
        }

        @Override
        public Void call() throws Exception {
            proxy.postTimeline(body, lectureId, prevVirtualTimestamp, nextVirtualTimestamp);
            return null;
        }

        @Override
        protected void onSuccess(Void t) throws Exception {
            PostTimelineActivity.this.setResult(RESULT_OK);
            PostTimelineActivity.this.finish();
        }

        @Override
        protected void onException(Exception e) throws RuntimeException {
            e.printStackTrace();
            alerter.toastShort("投稿に失敗しました");
            PostTimelineActivity.this.postBody.setEnabled(true);
            PostTimelineActivity.this.postButton.setEnabled(true);
        }
    }
}
