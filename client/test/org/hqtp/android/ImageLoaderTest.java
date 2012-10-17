package org.hqtp.android;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.hqtp.android.util.HQTPTestRunner;
import org.hqtp.android.util.RoboGuiceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import roboguice.activity.RoboActivity;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.Robolectric;

import static org.mockito.Mockito.*;

@RunWith(HQTPTestRunner.class)
public class ImageLoaderTest extends RoboGuiceTest {
    @Inject
    ImageLoader loader;

    private Activity activity;

    @Test
    public void loaderShouldDisplayImage() throws Exception {
        // prepare http response
        byte[] buffer = new byte[1];
        InputStream is = new ByteArrayInputStream(buffer);
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        response.setEntity(new InputStreamEntity(is, 1));
        Robolectric.addPendingHttpResponse(response);
        Bitmap bmp = BitmapFactory.decodeStream(is);

        ImageView image_view = mock(ImageView.class);
        when(image_view.getTag()).thenReturn("http://example.com/test.png");

        loader.displayImage(image_view, activity);
        Thread.sleep(1000);

        verify(image_view).setImageBitmap(bmp);
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ImageLoader.class).to(ImageLoaderImpl.class);
            bind(Activity.class).toInstance(activity);
        }
    }

    @Before
    public void setUp() {
        activity = new RoboActivity() {
        };
        setUpRoboGuice(new TestModule());
    }

    @After
    public void tearDown() {
        tearDownRoboGuice();
    }
}
