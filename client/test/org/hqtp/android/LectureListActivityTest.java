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
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;

@RunWith(HQTPTestRunner.class)
public class LectureListActivityTest extends RoboGuiceTest {
    @Inject
    LectureListActivity activity;
    @InjectView(R.id.lecture_list)
    ListView lectureList;

    ShadowActivity shadowActivity;

    @Test
    public void clickItemShouldStartActivity() throws Exception {
        activity.onCreate(null);
        lectureList.performItemClick(lectureList.getAdapter().getView(0, null, null),
                0, 0);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(TimelineActivity.class.getName()));
        assertThat(shadowIntent.getIntExtra(TimelineActivity.LECTURE_ID, -1),
                equalTo(1));
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
