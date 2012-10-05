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
import android.widget.Button;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;

@RunWith(HQTPTestRunner.class)
public class HQTPActivityTest extends RoboGuiceTest {
    @Inject
    HQTPActivity activity;
    @InjectView(R.id.getallpost_button)
    Button getAllPostButton;

    @Test
    public void pressingTheAllPostButtonShouldCallActivity() throws Exception {
        activity.onCreate(null);
        getAllPostButton.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
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
            bind(HQTPActivity.class).toInstance(activity);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new HQTPActivity();
        setUpRoboGuice(new TestModule(), activity);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
