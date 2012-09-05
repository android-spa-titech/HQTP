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
    public void loadingActivityShouldAccessGetTimeline() throws Exception {
        int lectureId = 1;
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post());//TODO: Postの詳細
        when(proxy.getTimeline(lectureId)).thenReturn(posts);

        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        verify(proxy).getTimeline(lectureId);
        ListView listView = (ListView) activity.findViewById(R.id.listQuestion);
        assertThat(listView.getCount(), equalTo(1));
    }

    @Test
    public void repeatedlyLoadingActivityShouldHaveValidPosts() throws Exception {
        int lectureId = 1;
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post());//TODO: Postの詳細
        when(proxy.getTimeline(lectureId)).thenReturn(posts);

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
    public void updateButtonShoudUpdateTimeline() throws Exception {
        int lectureId = 1;
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post());//TODO: Postの詳細
        when(proxy.getTimeline(lectureId)).thenReturn(posts);

        ListQuestionActivity activity = new ListQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        ListView listView = (ListView) activity.findViewById(R.id.listQuestion);
        assertThat(listView.getCount(), equalTo(1));

        List<Post> posts2 = new ArrayList<Post>();
        posts2.add(new Post());
        posts2.add(new Post());
        when(proxy.getTimeline(lectureId)).thenReturn(posts2);

        Button button = (Button) activity.findViewById(R.id.buttonUpdate);
        button.performClick();

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        Thread.sleep(100);

        assertThat(listView.getCount(), equalTo(2));
    }

    @Test
    public void activityShouldShowAlertWhenFailed() throws Exception {
        int lectureId = 1;
        when(proxy.getTimeline(lectureId)).thenThrow(new HQTPAPIException("Cannot get timeline"));

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
    public void activityShouldShowAlertWhenNoPosts() throws Exception {
        int lectureId = 1;
        when(proxy.getTimeline(lectureId)).thenReturn(null);

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
