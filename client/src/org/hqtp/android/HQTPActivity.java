package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.inject.Inject;

public class HQTPActivity extends RoboActivity implements OnClickListener {

    @InjectView(R.id.getallpost_button)
    Button getallpost_button;
    @InjectView(R.id.post_button)
    Button post_button;
    @Inject
    HQTPProxy proxy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getallpost_button.setOnClickListener(this);
        post_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.getallpost_button:
            startActivity(new Intent(this, ListQuestionActivity.class));
            break;
        case R.id.post_button:
            startActivity(new Intent(this, PostQuestionActivity.class));
            break;
        default:
            break;
        }
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
    }

}
