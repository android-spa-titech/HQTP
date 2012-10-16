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

import static org.hamcrest.core.IsEqual.equalTo;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(HQTPTestRunner.class)
public class TimelineRecurringUpdaterTest extends RoboGuiceTest {
    @Inject
    TimelineRecurringUpdater updater;
    @Inject
    HQTPProxy proxy;

    private User user;

    @Test
    public void updaterShouldNotifyObserver() throws Exception {
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post(31, "body", new Date(), 1234, user));
        when(proxy.getTimeline(47)).thenReturn(posts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(47);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);

        updater.stop();
        verify(proxy).getTimeline(47);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(posts));
    }

    @Test
    public void updaterShouldNotifyRecurrently() throws Exception {
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post(31, "body", new Date(), 1234, user));
        when(proxy.getTimeline(47)).thenReturn(posts).thenReturn(posts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(47);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(1000);

        updater.stop();
        assert (observer.times > 1);
    }

    @Test
    public void unregisteredObserverShouldNotNotified() throws Exception {
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post(31, "body", new Date(), 1234, user));
        when(proxy.getTimeline(47)).thenReturn(posts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(47);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);

        updater.unregisterTimelineObserver(observer);
        Thread.sleep(1000);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(posts));
    }

    @Test
    public void shouldNotNotifyAfterStopped() throws Exception {
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post(31, "body", new Date(), 1234, user));
        when(proxy.getTimeline(47)).thenReturn(posts);

        TestObserver observer = new TestObserver();

        updater.setLectureId(47);
        updater.registerTimelineObserver(observer);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(posts));

        updater.stop();
        Thread.sleep(1000);
        assertThat(observer.times, equalTo(1));
        assertThat(observer.posts, equalTo(posts));
    }

    @Test
    public void updaterShouldCallProxy() throws Exception {
        List<Post> posts = new ArrayList<Post>();
        posts.add(new Post(31, "body", new Date(), 1234, user));
        when(proxy.getTimeline(47)).thenReturn(posts);

        updater.setLectureId(47);
        updater.startRecurringUpdateTimeline();
        Thread.sleep(100);

        updater.stop();
        verify(proxy).getTimeline(47);
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
        this.user = new User(
                1,
                "test_user",
                "https://twimg0-a.akamaihd.net/profile_images/2222585882/android_onsen_normal.png"
                );
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
