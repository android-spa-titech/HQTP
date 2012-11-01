package org.hqtp.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileView extends LinearLayout {
    private ImageView icon_view;
    private TextView username_view;
    private TextView total_point_view;

    public ProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.profileview, this);
        initView();
    }

    private void initView()
    {
        icon_view = (ImageView) findViewById(R.id.userIcon);
        username_view = (TextView) findViewById(R.id.userName);
        total_point_view = (TextView) findViewById(R.id.totalPoint);
    }

    public void setUserName(String name)
    {
        username_view.setText(name);
    }

    public ImageView getIconView()
    {
        return icon_view;
    }

    public void setTotalPoint(int point)
    {
        total_point_view.setText(point + "pt");
    }
}
