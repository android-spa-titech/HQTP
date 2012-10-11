package org.hqtp.android;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

//TODO: singletonにすべき?
public class ImageLoaderImpl implements ImageLoader {
    private ImageCache image_cache;
    private final int placeholder = android.R.drawable.ic_menu_close_clear_cancel;
    // ImageViewとURL文字列の対応を管理することでImageViewを使い回しているときの画像を一意に決める。
    // 参考： http://lablog.lanche.jp/archives/220
    private Map<ImageView, String> view2url;
    private ExecutorService loading_service;

    public ImageLoaderImpl() {
        image_cache = new ImageCache();
        view2url = new ConcurrentHashMap<ImageView, String>();
        loading_service = Executors.newSingleThreadExecutor();
    }

    public void displayImage(ImageView image_view, String image_url) {
        view2url.put(image_view, image_url);
        Bitmap bmp = image_cache.get(image_url);
        if (bmp != null && !bmp.isRecycled()) {
            image_view.setImageBitmap(bmp);
            return;
        } else {
            image_view.setImageResource(placeholder);
            queueJob(image_view, image_url);
        }
    }

    public void clearCache() {
        image_cache.clear();
    }

    private void queueJob(ImageView image_view, String image_url) {
        loading_service.submit(new LodingHandler(image_view, image_url));
    }

    private class LodingHandler implements Runnable {
        private ImageView image_view;
        private String image_url;

        public LodingHandler(ImageView image_view, String image_url) {
            this.image_view = image_view;
            this.image_url = image_url;
        }

        @Override
        public void run() {
            Bitmap bmp = loadBitmap(image_url);
            Log.d("LoadingHandler", (bmp != null ? "succeed" : "failed") + " to load url=" + image_url);
            image_cache.put(image_url, bmp);
            if (isReused(image_view, image_url)) {
                return;
            }
            // ロードした画像をUIに表示
            Activity parent = (Activity) image_view.getContext();
            parent.runOnUiThread(new DisplayHandler(image_view, image_url, bmp));
        }
    }

    private Bitmap loadBitmap(String image_url) {
        try {
            URL url = new URL(image_url);
            return BitmapFactory.decodeStream(url.openStream());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isReused(ImageView image_view, String image_url) {
        String current_url = view2url.get(image_view);
        return !image_url.equals(current_url);
    }

    private class DisplayHandler implements Runnable {
        private ImageView image_view;
        private String image_url;
        private Bitmap bmp;

        public DisplayHandler(ImageView image_view, String image_url, Bitmap bmp) {
            this.image_view = image_view;
            this.image_url = image_url;
            this.bmp = bmp;
        }

        @Override
        public void run() {
            if (isReused(image_view, image_url)) {
                return;
            }
            if (bmp != null) {
                image_view.setImageBitmap(bmp);
            } else {
                Log.d("DisplayHandler", "fail to load : " + image_url);
                image_view.setImageResource(placeholder);
            }
        }
    }

    private class ImageCache {
        private Map<String, Bitmap> cache;

        public ImageCache() {
            cache = new ConcurrentHashMap<String, Bitmap>();
        }

        public Bitmap get(String image_url) {
            if (cache.containsKey(image_url)) {
                Log.d("ImageCache", "hit! :" + image_url);
                return cache.get(image_url);
            } else {
                Log.d("ImageCache", "miss! :" + image_url);
                return null;
            }
        }

        public void put(String image_url, Bitmap bmp) {
            cache.put(image_url, bmp);
        }

        public void clear() {
            cache.clear();
        }
    }
}
