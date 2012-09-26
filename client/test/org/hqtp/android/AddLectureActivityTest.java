package org.hqtp.android;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowIntent;

@RunWith(AddLectureActivityTestRunner.class)
public class AddLectureActivityTest {

    @Inject
    Injector injector;
    @Inject
    HQTPProxy proxy;

    @Test
    public void activityShouldHaveComponents() {
        PostQuestionActivity activity = new PostQuestionActivity();
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

        // TODO: APIを実装したらコメントはずして引数を設定
        // verify(proxy, never()).addLecture();
        assertTrue(activity.isFinishing());
        ShadowActivity shadowActivity = shadowOf(activity);
        assertNull(shadowActivity.getNextStartedActivity());
    }

    @Test
    public void pressingThePostButtonShouldCallProxy() throws Exception {
        AddLectureActivity activity = new AddLectureActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView lectureCodeText = (TextView) activity.findViewById(R.id.lecture_code_text);
        TextView lectureNameText = (TextView) activity.findViewById(R.id.lecture_name_text);
        Button addButton = (Button) activity.findViewById(R.id.lecture_add_button);

        lectureCodeText.setText("sample title");
        lectureNameText.setText("sample body");
        addButton.performClick();
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

        // TODO: APIを実装したらコメントはずして引数を設定
        // verify(proxy).addLecture();
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
        ShadowIntent shadowIntent = Robolectric.shadowOf(startedIntent);
        // TODO: 授業タイムライン画面のアクティビティ名前が決まったらコメントをはずす
        /*
         * assertThat(shadowIntent.getComponent().getClassName(),
         * equalTo(****.class.getName()));
         */
    }

    @Test
    public void failingPostShouldShowAlert() throws Exception {
        AddLectureActivity activity = new AddLectureActivity();
        injector.injectMembers(activity);
        activity.onCreate(null);

        TextView lectureCodeText = (TextView) activity.findViewById(R.id.lecture_code_text);
        TextView lectureNameText = (TextView) activity.findViewById(R.id.lecture_name_text);
        Button addButton = (Button) activity.findViewById(R.id.lecture_add_button);

        /*
        when(proxy.postQuestion("sample title", "sample body")).thenThrow(
                new HQTPAPIException("Cannot post"));
        titleText.setText("sample title");
        bodyText.setText("sample body");
        postButton.performClick();
        */
        Robolectric.runBackgroundTasks();
        Thread.sleep(100);

    }
    
}
