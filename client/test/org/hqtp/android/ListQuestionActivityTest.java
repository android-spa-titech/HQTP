package org.hqtp.android;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;

import static org.hamcrest.core.IsEqual.equalTo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ListQuestionActivityTestRunner.class)
public class ListQuestionActivityTest {
    @Inject
    Injector injector;
    @Inject
    HQTPProxy proxy;

    @Test
    public void activityShouldHaveComponents() throws Exception {
        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        assertNotNull(activity.findViewById(R.id.listQuestion));
        assertNotNull(activity.findViewById(R.id.buttonUpdate));
    }

    @Test
    public void loadingActivityShouldAccessGetQuestion() throws Exception {
        List<Question> questions = new ArrayList<Question>();
        questions.add(new Question("title", "body", "author"));
        when(proxy.getQuestions()).thenReturn(questions);

        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        verify(proxy).getQuestions();
        ListView listView = (ListView) activity.findViewById(R.id.listQuestion);
        assertThat(listView.getCount(), equalTo(1));
    }

    @Test
    public void repeatedlyLoadingActivityShouldHaveValidQuestions() throws Exception {
        List<Question> questions = new ArrayList<Question>();
        questions.add(new Question("title", "body", "author"));
        when(proxy.getQuestions()).thenReturn(questions);

        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        activity.onBackPressed();
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        activity.onCreate(null);
        Thread.sleep(100);

        ListView listView = (ListView) activity.findViewById(R.id.listQuestion);
        assertThat(listView.getCount(), equalTo(1));
    }

    @Test
    public void updateButtonShoudUpdateQuestions() throws Exception {
        List<Question> questions = new ArrayList<Question>();
        questions.add(new Question("title", "body", "author"));
        when(proxy.getQuestions()).thenReturn(questions);

        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        ListView listView = (ListView) activity.findViewById(R.id.listQuestion);
        assertThat(listView.getCount(), equalTo(1));

        List<Question> questions2 = new ArrayList<Question>();
        questions2.add(new Question("title", "body", "author"));
        questions2.add(new Question("title2", "body2", "author2"));
        when(proxy.getQuestions()).thenReturn(questions2);

        Button button = (Button) activity.findViewById(R.id.buttonUpdate);
        button.performClick();

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        assertThat(listView.getCount(), equalTo(2));
    }

    @Test
    public void activityShouldShowAlertWhenFailed() throws Exception {
        when(proxy.getQuestions()).thenThrow(new HQTPAPIException("Cannot get questions"));

        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        ListView listView = (ListView) activity.findViewById(R.id.listQuestion);
        assertThat(listView.getCount(), equalTo(0));

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alert);
    }

    @Test
    public void activityShouldShowAlertWhenNoQuestions() throws Exception {
        when(proxy.getQuestions()).thenReturn(null);

        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        ListView listView = (ListView) activity.findViewById(R.id.listQuestion);
        assertThat(listView.getCount(), equalTo(0));

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alert);
    }
}
