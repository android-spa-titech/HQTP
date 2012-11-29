package org.hqtp.android;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import roboguice.inject.ContextSingleton;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

@ContextSingleton
public class ImageLoaderImpl implements ImageLoader {
    private LruCache<String, Bitmap> image_cache;
    private final int placeholder = android.R.drawable.ic_menu_close_clear_cancel;
    // ImageViewとURL文字列の対応を管理することでImageViewを使い回しているときの画像を一意に決める。
    // 参考： http://lablog.lanche.jp/archives/220
    private Map<ImageView, String> view2url;
    private ExecutorService loading_service;
    private int memory_cache_size = 4 * 1024 * 1024;// 4MB

    public ImageLoaderImpl() {
        memory_cache_size = (int) Runtime.getRuntime().maxMemory() / 8;
        Log.d("memorycache", memory_cache_size + "BYTE");
        image_cache = new LruCache<String, Bitmap>(memory_cache_size) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        view2url = new ConcurrentHashMap<ImageView, String>();
        loading_service = Executors.newSingleThreadExecutor();
    }

    public void displayImage(ImageView image_view, Activity activity) {
        String image_url = (String) image_view.getTag();
        view2url.put(image_view, image_url);
        Bitmap bmp = image_cache.get(image_url);
        if (bmp != null && !bmp.isRecycled()) {
            image_view.setImageBitmap(bmp);
            return;
        } else {
            image_view.setImageResource(placeholder);
            queueJob(image_view, image_url, activity);
        }
    }

    public void clearCache() {
        image_cache.evictAll();
    }

    public void shutdown() {
        // via
        // http://gurimmer.lolipop.jp/daihakken/2012/01/27/javaexecutorservice%E3%81%AE%E6%AD%A3%E3%81%97%E3%81%84%E7%B5%82%E4%BA%86shutdown%E3%81%AE%E4%BB%95%E6%96%B9/
        loading_service.shutdown();
        try {
            if (!loading_service.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                loading_service.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            loading_service.shutdownNow();
        }
        clearCache();
    }

    private void queueJob(ImageView image_view, String image_url, Activity activity) {
        loading_service.submit(new LoadingHandler(image_view, image_url, activity));
    }

    private class LoadingHandler implements Runnable {
        private ImageView image_view;
        private String image_url;
        private Activity activity;

        public LoadingHandler(ImageView image_view, String image_url, Activity activity) {
            this.image_view = image_view;
            this.image_url = image_url;
            this.activity = activity;
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
            activity.runOnUiThread(new DisplayHandler(image_view, image_url, bmp));
        }
    }

    private Bitmap loadBitmap(String image_url) {
        // Usually we use java.net.URL to get Input Stream of response, but mockito cannot mock it.
        // Instead, we use HttpClient to get InputStream of response...
        DefaultHttpClient client = new DefaultHttpClient();
        try {
            return client.execute(new HttpGet(image_url), new ResponseHandler<Bitmap>() {
                @Override
                public Bitmap handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    HttpEntity entity = response.getEntity();
                    Bitmap bmp = BitmapFactory.decodeStream(entity.getContent());
                    entity.consumeContent();
                    return bmp;
                }
            });
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
}
