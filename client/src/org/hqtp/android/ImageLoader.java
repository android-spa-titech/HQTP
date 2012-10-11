package org.hqtp.android;

import android.app.Activity;
import android.widget.ImageView;

public interface ImageLoader {
    public void displayImage(ImageView image_view, Activity activity);

    public void clearCache();

    // TODO: 現在積んでるタスクのキャンセルも用意すべき？
}
