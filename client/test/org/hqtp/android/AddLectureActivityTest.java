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
import com.xtremelabs.robolectric.shadows.ShadowIntent;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.*;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class AddLectureActivityTest extends RoboGuiceTest {
    @Inject
    AddLectureActivity activity;
    @Inject
    APIClient proxy;
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
        assertThat(activity.isFinishing(), is(true));
        ShadowActivity shadowActivity = shadowOf(activity);
        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
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
        assertThat(activity.isFinishing(), is(true));
        ShadowActivity shadowActivity = shadowOf(activity);
        assertThat(shadowActivity.getNextStartedActivity(), nullValue());
    }

    @Test
    public void pressingTheAddButtonShouldCallProxy() throws Exception {
        activity.onCreate(null);
        when(proxy.addLecture("lectureCode", "lectureName")).thenReturn(
                new Lecture(123, "lectureName", "lectureCode"));

        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        addButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy).addLecture("lectureCode", "lectureName");
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(TimelineActivity.class.getName()));
        assertThat(shadowIntent.getIntExtra(TimelineActivity.LECTURE_ID, 0),
                equalTo(123));
        assertThat(shadowIntent.getStringExtra(TimelineActivity.LECTURE_NAME),
                equalTo("lectureName"));
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
        assertThat(alert, notNullValue());
    }

    @Test
    public void alreadyCreatedLecture() throws Exception {
        activity.onCreate(null);

        when(proxy.addLecture("lectureCode", "lectureName")).thenThrow(
                new LectureAlreadyCreatedException(
                        new Lecture(123, "lectureName", "lectureCode"),
                        "Lecture exists"));
        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        addButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy).addLecture("lectureCode", "lectureName");
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(alert, notNullValue());
        alert.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(),
                equalTo(TimelineActivity.class.getName()));
        assertThat(shadowIntent.getIntExtra(TimelineActivity.LECTURE_ID, 0),
                equalTo(123));
        assertThat(shadowIntent.getStringExtra(TimelineActivity.LECTURE_NAME),
                equalTo("lectureName"));
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(APIClient.class).toInstance(mock(APIClient.class));
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
