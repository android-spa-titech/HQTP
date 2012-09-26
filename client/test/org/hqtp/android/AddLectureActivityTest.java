package org.hqtp.android;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@RunWith(AddLectureActivityTestRunner.class)
public class AddLectureActivityTest {

    @Inject
    Injector injector;
    @Inject
    HQTPProxy proxy;

    @Test
    public void activityShouldHaveComponents() {
        AddLectureActivity activity = new AddLectureActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        assertNotNull(activity.findViewById(R.id.lecture_code_text));
        assertNotNull(activity.findViewById(R.id.lecture_name_text));
        assertNotNull(activity.findViewById(R.id.lecture_add_button));
        assertNotNull(activity.findViewById(R.id.lecture_add_cancel_button));
    }

    @Test
    public void pressingTheCancelButtonShouldNotCallProxy() throws Exception {
        AddLectureActivity activity = new AddLectureActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView lectureCodeText = (TextView) activity.findViewById(R.id.lecture_code_text);
        TextView lectureNameText = (TextView) activity.findViewById(R.id.lecture_name_text);
        Button cancelButton = (Button) activity.findViewById(R.id.lecture_add_cancel_button);

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
        AddLectureActivity activity = new AddLectureActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView lectureCodeText = (TextView) activity.findViewById(R.id.lecture_code_text);
        TextView lectureNameText = (TextView) activity.findViewById(R.id.lecture_name_text);

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
        AddLectureActivity activity = new AddLectureActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView lectureCodeText = (TextView) activity.findViewById(R.id.lecture_code_text);
        TextView lectureNameText = (TextView) activity.findViewById(R.id.lecture_name_text);
        Button addButton = (Button) activity.findViewById(R.id.lecture_add_button);

        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        addButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy).addLecture("lectureCode", "lectureName");
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        // TODO: Uncomment if HQTPProxyImple.addLecture() is implemented.
        // assertNotNull(startedIntent);
        // ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);
        // assertThat(shadowIntent.getComponent().getClassName(),
        //         equalTo(TimelineActivity.class.getName()));
    }

    @Test
    public void failingPostShouldShowAlert() throws Exception {
        AddLectureActivity activity = new AddLectureActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView lectureCodeText = (TextView) activity.findViewById(R.id.lecture_code_text);
        TextView lectureNameText = (TextView) activity.findViewById(R.id.lecture_name_text);
        Button addButton = (Button) activity.findViewById(R.id.lecture_add_button);

        when(proxy.addLecture("lectureCode", "lectureName")).thenThrow(
                new HQTPAPIException("Cannot post"));
        lectureCodeText.setText("lectureCode");
        lectureNameText.setText("lectureName");
        addButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        verify(proxy).addLecture("lectureCode", "lectureName");
        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        // TODO: Uncomment if error handling code is implemented.
        // assertNotNull(alert);
    }

}
