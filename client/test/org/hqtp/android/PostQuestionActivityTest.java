package org.hqtp.android;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

@RunWith(PostQuestionActivityTestRunner.class)
public class PostQuestionActivityTest {

    @Inject PostQuestionActivity            activity;
    @Inject HQTPProxy                       proxy;
    @InjectView(R.id.title_text) TextView   title_text;
    @InjectView(R.id.body_text) TextView    body_text;
    @InjectView(R.id.post_button) Button    post_button;
    @InjectView(R.id.cancel_buttton) Button cancel_button;

    @Before
    public void setUp() throws Exception {
        activity.onCreate(null);
    }

    @Test
    public void pressingThePostButtonShouldCallProxy() throws Exception {
        title_text.setText("sample title");
        body_text.setText("sample body");
        post_button.performClick();

        verify(proxy).postQuestion("sample title", "sample body");
    }

    @Test
    public void pressingTheCancelButtonShouldNotCallProxy() throws Exception {
        title_text.setText("sample title");
        body_text.setText("sample body");
        cancel_button.performClick();

        verify(proxy, never()).postQuestion("sample title", "sample body");
    }
}