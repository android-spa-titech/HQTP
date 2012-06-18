package org.hqtp.android;

import java.util.List;

import com.google.inject.Inject;

import android.os.Bundle;
import roboguice.activity.RoboActivity;
import roboguice.util.RoboAsyncTask;

public class ListQuestionActivity extends RoboActivity {
    @Inject
    HQTPProxy proxy;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        (new GetPostTask()).execute();
    }

    private class GetPostTask extends RoboAsyncTask<List<Question>> {

        @Override
        public List<Question> call() throws Exception {
            return proxy.getQuestions();
        }

        @Override
        protected void onSuccess(List<Question> questions) {

            if (questions == null) {
                //TODO
            } else {
                for (Question q : questions) {
                //TODO adapter
                }
            }
        }
    }
}
