package org.hqtp.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class HQTPActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        View authentication_button = this.findViewById(R.id.authentication_button);
        authentication_button.setOnClickListener(this);
        View getallpost_button = this.findViewById(R.id.getallpost_button);
        getallpost_button.setOnClickListener(this);
        View post_button = this.findViewById(R.id.post_button);
        post_button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.authentication_button:
            showAlert("authentication", "authentication_button is clicked");
            // TODO: implement
            break;
        case R.id.getallpost_button:
            showAlert("getallpost", "getallpost_button is clicked");
            // TODO: implement
            break;
        case R.id.post_button:
            showAlert("post", "post_button is clicked");
            // TODO: implement
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