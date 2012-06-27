package org.hqtp.android;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
import android.content.Intent;
import android.widget.Button;

import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

@RunWith(HQTPActivityTestRunner.class)
public class HQTPActivityTest {

    @Inject
    HQTPActivity activity;
    @Inject
    HQTPProxy proxy;
    @InjectView(R.id.getallpost_button)
    Button getallpost_button;
    @InjectView(R.id.post_button)
    Button post_button;

    @Before
    public void setUp() throws Exception {
        activity.onCreate(null);
    }

    @Test
    public void pressingThePostButtonShouldCallActivity() throws Exception {
        post_button.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(PostQuestionActivity.class.getName()));
    }
}
