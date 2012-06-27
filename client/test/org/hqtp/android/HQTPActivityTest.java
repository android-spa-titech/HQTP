package org.hqtp.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(HQTPActivityTestRunner.class)
public class HQTPActivityTest {

    @Inject
    Injector injector;

    @Test
    public void activityShouldHaveTwitterButtons() {
        HQTPActivity activity = new HQTPActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        assertNotNull(activity.findViewById(R.id.post_button));
        assertNotNull(activity.findViewById(R.id.getallpost_button));
    }

    @Test
    public void pressingThePostButtonShouldCallActivity() throws Exception {
        HQTPActivity activity = new HQTPActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        activity.findViewById(R.id.post_button).performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(PostQuestionActivity.class.getName()));
    }

    @Test
    public void pressingTheAllPostButtonShouldCallActivity() throws Exception {
        HQTPActivity activity = new HQTPActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        activity.findViewById(R.id.getallpost_button).performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(ListQuestionActivity.class.getName()));
    }
}
