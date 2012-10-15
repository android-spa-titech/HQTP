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
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowToast;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class PostTimelineActivityTest extends RoboGuiceTest {
    private static final int TEST_LECTURE_ID = 47;
    private static final int TEST_PREV_VIRTUAL_TS = 1000;
    private static final int TEST_NEXT_VIRTUAL_TS = 2000;
    private static final String TEST_POST_BODY = "Test body";

    @Inject
    HQTPProxy proxy;
    @InjectView(R.id.postBody)
    TextView postBody;
    @InjectView(R.id.postButton)
    Button postButton;

    private PostTimelineActivity activity;
    private ShadowActivity shadowActivity;

    @Test
    public void activityShouldPostTimeline() throws Exception {
        activity.onCreate(null);
        postBody.setText(TEST_POST_BODY);
        postButton.performClick();
        Thread.sleep(100);

        verify(proxy).postTimeline(
                TEST_POST_BODY, TEST_LECTURE_ID,
                TEST_PREV_VIRTUAL_TS, TEST_NEXT_VIRTUAL_TS);
        assertThat(shadowActivity.getResultCode(), equalTo(Activity.RESULT_OK));
        assertThat(shadowActivity.isFinishing(), equalTo(true));
    }

    @Test
    public void activityShouldSaveState() throws Exception {
        activity.onCreate(null);
        postBody.setText(TEST_POST_BODY);
        Bundle outState = new Bundle();
        activity.onSaveInstanceState(outState);

        assertThat(outState.getString(PostTimelineActivity.SAVED_POST_BODY),
                equalTo(TEST_POST_BODY));
    }

    @Test
    public void activityShouldRestoreState() throws Exception {
        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putString(PostTimelineActivity.SAVED_POST_BODY, TEST_POST_BODY);
        activity.onCreate(savedInstanceState);

        assertThat(postBody.getText().toString(), equalTo(TEST_POST_BODY));
    }

    @Test
    public void activityShouldReturnCancelResult() throws Exception {
        activity.onCreate(null);
        activity.onBackPressed();

        assertThat(shadowActivity.getResultCode(), equalTo(Activity.RESULT_CANCELED));
        assertThat(shadowActivity.isFinishing(), equalTo(true));
    }

    @Test
    public void activityShouldWarnIfNoBody() throws Exception {
        activity.onCreate(null);
        postButton.performClick();
        Thread.sleep(100);

        assertThat(ShadowToast.getLatestToast(), notNullValue());
    }

    @Test
    public void activityShouldWarnIfFailed() throws Exception {
        when(proxy.postTimeline(
                TEST_POST_BODY, TEST_LECTURE_ID,
                TEST_PREV_VIRTUAL_TS, TEST_NEXT_VIRTUAL_TS))
            .thenThrow(new HQTPAPIException("Cannot post"));

        activity.onCreate(null);
        postBody.setText(TEST_POST_BODY);
        postButton.performClick();
        Thread.sleep(100);

        assertThat(ShadowToast.getLatestToast(), notNullValue());
        assertThat(shadowActivity.isFinishing(), equalTo(false));
        assertThat(postBody.getText().toString(), equalTo(TEST_POST_BODY));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
        }
    }

    @Before
    public void setUp() {
        activity = new PostTimelineActivity();
        Intent intent = new Intent();
        intent.putExtra(PostTimelineActivity.LECTURE_ID, PostTimelineActivityTest.TEST_LECTURE_ID);
        intent.putExtra(PostTimelineActivity.PREV_VIRTUAL_TS, PostTimelineActivityTest.TEST_PREV_VIRTUAL_TS);
        intent.putExtra(PostTimelineActivity.NEXT_VIRTUAL_TS, PostTimelineActivityTest.TEST_NEXT_VIRTUAL_TS);

        activity.setIntent(intent);
        shadowActivity = Robolectric.shadowOf(activity);
        setUpRoboGuice(new TestModule(), activity);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
