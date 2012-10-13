package org.hqtp.android;

import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(HQTPTestRunner.class)
public class AddLectureActivityTest extends RoboGuiceTest {
    @Inject
    AddLectureActivity activity;
    @Inject
    HQTPProxy proxy;
    @InjectView(R.id.lecture_code_text)
    TextView lectureCodeText;
    @InjectView(R.id.lecture_name_text)
    TextView lectureNameText;
    @InjectView(R.id.lecture_add_button)
    Button addButton;
    @InjectView(R.id.lecture_add_cancel_button)
    Button cancelButton;

    @Test
    public void pressingTheCancelButtonShouldNotCallProxy() throws Exception {
        activity.onCreate(null);

        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        cancelButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy, never()).addLecture("lectureCode", "lectureName");
        assertTrue(activity.isFinishing());
        ShadowActivity shadowActivity = shadowOf(activity);
        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void pressingTheBackButtonShouldNotCallProxy() throws Exception {
        activity.onCreate(null);

        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        activity.onBackPressed();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy, never()).addLecture("lectureCode", "lectureName");
        assertTrue(activity.isFinishing());
        ShadowActivity shadowActivity = shadowOf(activity);
        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void pressingTheAddButtonShouldCallProxy() throws Exception {
        activity.onCreate(null);

        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        addButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy).addLecture("lectureCode", "lectureName");
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        // TODO: Uncomment when HQTPProxyImple.addLecture() is implemented.
        // assertNotNull(startedIntent);
        // ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);
        // assertThat(shadowIntent.getComponent().getClassName(),
        //         equalTo(TimelineActivity.class.getName()));
    }

    @Test
    public void failingPostShouldShowAlert() throws Exception {
        activity.onCreate(null);

        when(proxy.addLecture("lectureCode", "lectureName")).thenThrow(
                new HQTPAPIException("Cannot post"));
        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        addButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy).addLecture("lectureCode", "lectureName");
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        // TODO: Uncomment when error handling code is implemented.
        // assertNotNull(alert);
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HQTPProxy.class).toInstance(mock(HQTPProxy.class));
            bind(AddLectureActivity.class).toInstance(activity);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new AddLectureActivity();
        setUpRoboGuice(new TestModule(), activity);
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
