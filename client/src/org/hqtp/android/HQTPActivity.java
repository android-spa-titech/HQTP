package org.hqtp.android;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HQTPActivity extends RoboActivity implements OnClickListener {

    @InjectView(R.id.getallpost_button)
    Button getallpost_button;
    @InjectView(R.id.post_button)
    Button post_button;

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
        case R.id.getallpost_button: {
            Intent intent = new Intent(this, TimelineActivity.class);
            // TODO(draftcode): Should put a valid lecture id.
            intent.putExtra(TimelineActivity.LECTURE_ID, 0);
            startActivity(intent);
            break;
        }
        case R.id.post_button: {
            Intent intent = new Intent(this, PostQuestionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            break;
        }
        default:
            break;
        }
    }

}
