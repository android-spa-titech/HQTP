package org.hqtp.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.Inject;
import com.google.inject.Injector;

import static org.hamcrest.core.IsEqual.equalTo;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(TimelineActivityTestRunner.class)
public class TimelineActivityTest {
    public static int LECTURE_ID = 47;

    @Inject
    HQTPProxy proxy;
    @Inject
    TimelineActivity activity;
    @InjectView(R.id.listPost)
    ListView listView;
    @InjectView(R.id.buttonUpdate)
    Button updateButton;

    @After
    public void tearDown() {
        activity.onStop();
    }

    @Test
    public void loadingActivityShouldAccessGetTimeline() throws Exception {
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post(31, "body", new Date(), 1234));
        when(proxy.getTimeline(LECTURE_ID)).thenReturn(posts);

        activity.onCreate(null);
        Thread.sleep(100);

        verify(proxy).getTimeline(LECTURE_ID);
        assertThat(listView.getCount(), equalTo(1));
    }

    @Test
    public void repeatedlyLoadingActivityShouldHaveValidPosts() throws Exception {
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post(31, "body", new Date(), 1234));
        when(proxy.getTimeline(LECTURE_ID)).thenReturn(posts);

        activity.onCreate(null);
        Thread.sleep(100);
        activity.onBackPressed();
        Thread.sleep(100);
        activity.onCreate(null);
        Thread.sleep(100);

        assertThat(listView.getCount(), equalTo(1));
    }

    @Test
    public void updateButtonShoudUpdatePosts() throws Exception {
        Post post1 = new Post(31, "body", new Date(), 1234);
        Post post2 = new Post(32, "body", new Date(), 2222);

        List<Post> posts1 = new ArrayList<Post>();
        posts1.add(post1);
        List<Post> posts2 = new ArrayList<Post>();
        posts2.add(post1);
        posts2.add(post2);

        when(proxy.getTimeline(LECTURE_ID)).thenReturn(posts1);
        activity.onCreate(null);
        Thread.sleep(100);
        assertThat(listView.getCount(), equalTo(1));

        when(proxy.getTimeline(LECTURE_ID)).thenReturn(posts2);
        updateButton.performClick();
        Thread.sleep(100);

        assertThat(listView.getCount(), equalTo(2));
    }

    @Test
    public void activityShouldUpdateTimelineRepeatedly() throws Exception {
        Post post1 = new Post(31, "body", new Date(), 1234);
        List<Post> posts1 = new ArrayList<Post>();
        posts1.add(post1);

        when(proxy.getTimeline(LECTURE_ID)).thenReturn(posts1);
        // BUG: 定期的にタイムラインを更新する操作に関して上記のモックが動いてない？proxy.getTimelineを呼ぶ度に例外が発生している
        activity.onCreate(null);
        // TODO: 定期的に実行するタイミングを指定する(例：1秒後から0.5秒ごとに実行)
        Thread.sleep(3000);
        verify(proxy, atLeast(3)).getTimeline(LECTURE_ID);
    }
}
