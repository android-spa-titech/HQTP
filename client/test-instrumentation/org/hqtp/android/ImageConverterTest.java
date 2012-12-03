package org.hqtp.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class ImageConverterTest extends AndroidTestCase {
    private ImageConverter converter;

    @Override
    protected void setUp() throws Exception {
        converter = new ImageConverter(mContext.getContentResolver());
    }

    @SmallTest
    public void testLoadedImageShouldResizedWithinSize() throws Exception {
        Uri uri = convertBitmapToUri(Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888));
        Bitmap bitmap = converter.loadImageWithinSize(uri, 100, 100);
        assertTrue(bitmap.getWidth() == 100);
        assertTrue(bitmap.getHeight() <= 100);
    }

    @SmallTest
    public void testLoadedImageShouldNotResizedWhenImageIsSmall() throws Exception {
        Uri uri = convertBitmapToUri(Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888));
        Bitmap bitmap = converter.loadImageWithinSize(uri, 200, 300);
        assertEquals(200, bitmap.getWidth());
        assertEquals(300, bitmap.getHeight());
    }

    @SmallTest
    public void testLoadOriginalSizeImageIfSizeIsLessThanZero() throws Exception {
        Uri uri = convertBitmapToUri(Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888));
        Bitmap bitmap = converter.loadImageWithinSize(uri, 0, 0);
        assertEquals(200, bitmap.getWidth());
        assertEquals(300, bitmap.getHeight());
    }

    @SmallTest
    public void testCompressedImageShouldBeJPEG() throws Exception {
        Uri uri = convertBitmapToUri(Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888));
        byte[] bytes = converter.compressImageWithinSize(uri, Bitmap.CompressFormat.JPEG, 50, 200, 300);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        assertEquals("image/jpeg", options.outMimeType);
    }

    private Uri convertBitmapToUri(Bitmap bitmap) throws IOException {
        File tempFile = File.createTempFile("test", ".jpg", mContext.getCacheDir());
        bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(tempFile));
        return Uri.fromFile(tempFile);
    }
}
