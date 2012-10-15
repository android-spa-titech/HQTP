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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class TimelineRecurringUpdaterTest extends RoboGuiceTest {
    public static int LECTURE_ID = 47;

    @Inject
    TimelineRecurringUpdater updater;
    @Inject
    HQTPProxy proxy;

    private Post testPost;
    private User testUser;
    private Lecture testLecture;
    private List<Post> testPosts;

    @Before
    public void setUpFixture() throws Exception {
        testUser = new User(1, "testUser", "http://example.com/icon.png");
        testLecture = new Lecture(2, "testLecture", "testLectureCode");
        testPost = new Post(3, "body", new Date(), 1000, testUser, testLecture);
        testPosts = new ArrayList<Post>();
        testPosts.add(testPost);
    }

    @Test
    public void updaterShouldNotifyObserver() throws Exception {
        when(proxy.getTimeline(LECTURE_ID)).thenReturn(testPosts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(LECTURE_ID);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);

        updater.stop();
        verify(proxy).getTimeline(LECTURE_ID);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(testPosts));
    }

    @Test
    public void updaterShouldNotifyRecurrently() throws Exception {
        when(proxy.getTimeline(LECTURE_ID))
            .thenReturn(testPosts).thenReturn(testPosts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(LECTURE_ID);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(1000);

        updater.stop();
        assertThat(observer.times > 1, is(true));
    }

    @Test
    public void unregisteredObserverShouldNotNotified() throws Exception {
        when(proxy.getTimeline(LECTURE_ID)).thenReturn(testPosts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(LECTURE_ID);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);

        updater.unregisterTimelineObserver(observer);
        Thread.sleep(1000);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(testPosts));
    }

    @Test
    public void shouldNotNotifyAfterStopped() throws Exception {
        when(proxy.getTimeline(LECTURE_ID)).thenReturn(testPosts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(LECTURE_ID);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(testPosts));

        updater.stop();
        Thread.sleep(1000);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(testPosts));
    }

    @Test
    public void updaterShouldCallProxy() throws Exception {
        when(proxy.getTimeline(LECTURE_ID)).thenReturn(testPosts);

        updater.setLectureId(LECTURE_ID);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);

        updater.stop();
        verify(proxy).getTimeline(LECTURE_ID);
    }

    @Test
    public void updaterShouldNotCallProxyWhenNoLectureId() throws Exception {
        updater.startRecurringUpdateTimeline();
        Thread.sleep(1000);

        verify(proxy, never()).getTimeline(-1);
    }

    private class TestObserver implements TimelineObserver {
        public List<Post> posts = null;
        public int times = 0;

        @Override
        public void onUpdate(List<Post> posts) {
            this.posts = posts;
            times++;
        }
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
            bind(TimelineRecurringUpdater.class).to(TimelineRecurringUpdaterImpl.class);
            bind(Long.class).annotatedWith(Names.named("TimelineUpdatePeriod")).toInstance(
                    Long.valueOf(500));
        }
    }

    @Before
    public void setUp() {
        setUpRoboGuice(new TestModule());
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
