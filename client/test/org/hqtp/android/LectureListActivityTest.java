package org.hqtp.android;

import java.util.ArrayList;
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
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class LectureListActivityTest extends RoboGuiceTest {
    @Inject
    HQTPProxy proxy;
    @Inject
    LectureListActivity activity;
    @InjectView(R.id.lecture_list)
    ListView lectureList;
    @InjectView(R.id.add_lecture)
    Button addLectureButton;
    @InjectView(R.id.refresh)
    Button refreshButton;

    private ShadowActivity shadowActivity;

    @Test
    public void clickItemShouldStartActivity() throws Exception {
        List<Lecture> lectures = new ArrayList<Lecture>();
        lectures.add(new Lecture(1, "Test lecture", "Test lecture code"));
        when(proxy.getLectures()).thenReturn(lectures);

        activity.onCreate(null);
        Thread.sleep(100);
        assertThat(lectureList.getAdapter().getCount(), equalTo(1));
        lectureList.performItemClick(lectureList.getAdapter().getView(0, null, null),
                0, 0);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(TimelineActivity.class.getName()));
        assertThat(shadowIntent.getIntExtra(TimelineActivity.LECTURE_ID, -1),
                equalTo(1));
        assertThat(shadowIntent.getStringExtra(TimelineActivity.LECTURE_NAME),
                equalTo("Test lecture"));
    }

    @Test
    public void clickAddLectureButtonShouldStartActivity() throws Exception {
        activity.onCreate(null);
        addLectureButton.performClick();

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(AddLectureActivity.class.getName()));
    }

    @Test
    public void clickRefreshButtonShouldRefreshList() throws Exception {
        List<Lecture> lectures = new ArrayList<Lecture>();
        when(proxy.getLectures()).thenReturn(lectures);
        activity.onCreate(null);
        Thread.sleep(100);

        assertThat(lectureList.getAdapter().getCount(), equalTo(0));

        lectures.add(new Lecture(1, "Test lecture", "Test lecture code"));
        when(proxy.getLectures()).thenReturn(lectures);
        refreshButton.performClick();
        Thread.sleep(100);

        assertThat(lectureList.getAdapter().getCount(), equalTo(1));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
            bind(LectureListActivity.class).toInstance(activity);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new LectureListActivity();
        setUpRoboGuice(new TestModule(), activity);

        shadowActivity = shadowOf(activity);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
