package org.hqtp.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.Intent;
import android.widget.ListView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import static org.mockito.Mockito.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(HQTPTestRunner.class)
public class TimelineActivityTest extends RoboGuiceTest {
    public static int LECTURE_ID = 47;

    @Inject
    TimelineActivity activity;
    @InjectView(R.id.listPost)
    ListView listView;
    @Inject
    TimelineRecurringUpdater updater;

    @Test
    public void loadingActivityShouldStartAndRegisterItself() throws Exception {
        activity.onCreate(null);

        verify(updater).setLectureId(LECTURE_ID);
        verify(updater).registerTimelineObserver(activity);
        verify(updater).startRecurringUpdateTimeline();
        assertThat(listView.getCount(), equalTo(0));

        activity.onStop();
        verify(updater).unregisterTimelineObserver(activity);
        verify(updater).stop();
    }

    @Test
    public void activityShouldUpdateTimelineRepeatedly() throws Exception {
        User user = new User(1, "testUser", "http://example.com/icon.png");
        Lecture lecture = new Lecture(2, "testLecture", "testLectureCode");
        Post post1 = new Post(31, "body", new Date(), 1234, user, lecture);
        List<Post> posts = new ArrayList<Post>();
        posts.add(post1);

        activity.onCreate(null);
        assertThat(listView.getCount(), equalTo(0));
        activity.onUpdate(posts);
        assertThat(listView.getCount(), equalTo(1));
        activity.onUpdate(posts);
        assertThat(listView.getCount(), equalTo(1));

        Post post2 = new Post(32, "body2", new Date(), 1233, user, lecture);
        posts.add(post2);
        activity.onUpdate(posts);
        assertThat(listView.getCount(), equalTo(2));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TimelineRecurringUpdater.class).toInstance(
                    mock(TimelineRecurringUpdater.class));
            bind(TimelineActivity.class).toInstance(activity);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new TimelineActivity();
        Intent intent = new Intent();
        intent.putExtra(
                TimelineActivity.LECTURE_ID,
                TimelineActivityTest.LECTURE_ID);
        activity.setIntent(intent);
        setUpRoboGuice(new TestModule(), activity);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
