package org.hqtp.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class LoginActivity extends RoboActivity implements OnClickListener {
    @InjectView(R.id.twitter_login)
    Button loginButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.twitter_login){
            startActivity(new Intent(this, HQTPActivity.class));
        }
    }

}
