package org.hqtp.android;

import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(HQTPTestRunner.class)
public class ImageLoaderTest extends RoboGuiceTest {
    @Inject
    HQTPActivity activity;
    @Inject
    ImageLoader loader;

    private final String IMAGE_URL = "https://twimg0-a.akamaihd.net/profile_images/2222585882/android_onsen_normal.png";

    @Test
    public void loaderShouldDisplayImage() throws Exception {
        ImageView image_view = mock(ImageView.class);
        when(image_view.getTag()).thenReturn(IMAGE_URL);
        Bitmap bmp = mock(Bitmap.class);

        // TODO: BitmapFactory.decodeStream()がbmpを返すようにする
        loader.displayImage(image_view, activity);
        Thread.sleep(1000);

        verify(image_view).setImageBitmap(bmp);
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ImageLoader.class).to(ImageLoaderImpl.class);
            bind(HQTPActivity.class).toInstance(activity);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new HQTPActivity();
        setUpRoboGuice(new TestModule());
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
