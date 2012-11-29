package org.hqtp.android;

import java.io.File;

import android.app.Activity;
import android.widget.ImageView;

public interface ImageLoader {
    public void initializeDiskCache(File directory);

    public void displayImage(ImageView image_view, Activity activity);

    public void clearCache();

    public void shutdown();
}
