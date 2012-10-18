package org.hqtp.android;

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

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.instanceOf;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class TimelineActivityTest extends RoboGuiceTest {
    private static final int LECTURE_ID = 47;
    private static final String LECTURE_NAME = "lecture name";

    @InjectView(R.id.listPost)
    ListView listView;
    @Inject
    TimelineAdapter adapter;
    @Inject
    TimelineRecurringUpdater updater;

    private TimelineActivity activity;

    @Test
    public void loadingActivityShouldStartAndRegisterAdapter() throws Exception {
        activity.onCreate(null);
        activity.onStart();

        verify(updater).setLectureId(LECTURE_ID);
        verify(adapter).setLectureId(LECTURE_ID);
        verify(updater).registerTimelineObserver(adapter);
        verify(updater).startRecurringUpdateTimeline();
        assertThat(listView.getOnItemLongClickListener(),
                instanceOf(TimelineAdapter.ItemLongClickListener.class));

        activity.onPause();
        verify(updater).unregisterTimelineObserver(adapter);
        verify(updater).stop();
    }

    @Test
    public void activityShouldDelegateResult() throws Exception {
        activity.onCreate(null);
        activity.onActivityResult(0, Activity.RESULT_OK, null);

        verify(adapter).onActivityResult(0, Activity.RESULT_OK, null);
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
            bind(TimelineRecurringUpdater.class).toInstance(
                    mock(TimelineRecurringUpdater.class));
            bind(TimelineAdapter.class).toInstance(mock(TimelineAdapter.class));
            bind(ImageLoader.class).toInstance(mock(ImageLoader.class));
            bind(TimelineActivity.class).toInstance(activity);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new TimelineActivity();
        Intent intent = new Intent();
        intent.putExtra(TimelineActivity.LECTURE_ID, TimelineActivityTest.LECTURE_ID);
        intent.putExtra(TimelineActivity.LECTURE_NAME, TimelineActivityTest.LECTURE_NAME);
        activity.setIntent(intent);
        setUpRoboGuice(new TestModule(), activity);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
