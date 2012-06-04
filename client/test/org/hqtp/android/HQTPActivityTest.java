package org.hqtp.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.inject.InjectView;
import android.widget.Button;

import com.google.inject.Inject;

@RunWith(HQTPActivityTestRunner.class)
public class HQTPActivityTest {

    @Inject HQTPActivity activity;
    @Inject HQTPProxy proxy;
    @InjectView(R.id.getallpost_button) Button getallpost_button;

    @Test
    public void pressingTheButtonShouldAccessGetQuestion() throws Exception {
    }

}