package org.hqtp.android;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
    @InjectView(R.id.post_button)
    Button postButton;
    @InjectView(R.id.getallpost_button)
    Button getAllPostButton;

    @Test
    public void pressingThePostButtonShouldCallActivity() throws Exception {
        activity.onCreate(null);
        postButton.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(PostQuestionActivity.class.getName()));
    }

    @Test
    public void pressingTheAllPostButtonShouldCallActivity() throws Exception {
        activity.onCreate(null);
        getAllPostButton.performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(ListQuestionActivity.class.getName()));
    }
}
