package org.hqtp.android;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.widget.ListView;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xtremelabs.robolectric.Robolectric;

@RunWith(ListQuestionActivityTestRunner.class)
public class ListQuestionActivityTest {
    @Inject
    Injector injector;
    @Inject
    HQTPProxy proxy;

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
}
