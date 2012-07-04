package org.hqtp.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PostQuestionActivityTestRunner.class)
public class PostQuestionActivityTest {
    @Inject
    Injector injector;
    @Inject
    HQTPProxy proxy;

    @Test
    public void activityShouldHaveComponents() {
        PostQuestionActivity activity = new PostQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        assertNotNull(activity.findViewById(R.id.title_text));
        assertNotNull(activity.findViewById(R.id.body_text));
        assertNotNull(activity.findViewById(R.id.post_button));
        assertNotNull(activity.findViewById(R.id.cancel_button));
    }

    @Test
    public void pressingThePostButtonShouldCallProxy() throws Exception {
        PostQuestionActivity activity = new PostQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView titleText = (TextView) activity.findViewById(R.id.title_text);
        TextView bodyText = (TextView) activity.findViewById(R.id.body_text);
        Button postButton = (Button) activity.findViewById(R.id.post_button);

        titleText.setText("sample title");
        bodyText.setText("sample body");
        postButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy).postQuestion("sample title", "sample body");
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(ListQuestionActivity.class.getName()));
    }

    @Test
    public void pressingTheCancelButtonShouldNotCallProxy() throws Exception {
        PostQuestionActivity activity = new PostQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView titleText = (TextView) activity.findViewById(R.id.title_text);
        TextView bodyText = (TextView) activity.findViewById(R.id.body_text);
        Button cancelButton = (Button) activity.findViewById(R.id.cancel_button);

        titleText.setText("sample title");
        bodyText.setText("sample body");
        cancelButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy, never()).postQuestion("sample title", "sample body");
        assertTrue(activity.isFinishing());
        ShadowActivity shadowActivity = shadowOf(activity);
        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void failingPostShouldShowAlert() throws Exception {
        PostQuestionActivity activity = new PostQuestionActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView titleText = (TextView) activity.findViewById(R.id.title_text);
        TextView bodyText = (TextView) activity.findViewById(R.id.body_text);
        Button postButton = (Button) activity.findViewById(R.id.post_button);

        when(proxy.postQuestion("sample title", "sample body")).thenThrow(
                new HQTPAPIException("Cannot post"));
        titleText.setText("sample title");
        bodyText.setText("sample body");
        postButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(alert);
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNull(startedIntent);
        assertThat(titleText.getText().toString(), equalTo("sample title"));
        assertThat(bodyText.getText().toString(), equalTo("sample body"));
        assertFalse(activity.isFinishing());
    }
}
