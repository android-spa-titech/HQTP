package org.hqtp.android;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.FloatMath;

import com.google.inject.Inject;

public class ImageConverter {
    private ContentResolver contentResolver;

    @Inject
    public ImageConverter(ContentResolver resolver) {
        this.contentResolver = resolver;
    }

    private static int calculateInSampleSize(int imageWidth, int imageHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (imageWidth > reqWidth || imageHeight > reqHeight) {
            if (imageWidth > imageHeight) {
                inSampleSize = (int) FloatMath.floor((float) imageWidth / (float) reqWidth);
            } else {
                inSampleSize = (int) FloatMath.floor((float) imageHeight / (float) reqHeight);
            }
        }

        return inSampleSize;
    }

    private Bitmap resizeImage(Bitmap image, int width, int height) {
        if (image == null || width <= 0 || height <= 0) {
            return null;
        }
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        float scale = Math.min(Math.min((float) width / imageWidth, (float) height / imageHeight), 1);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true);
    }

    public Bitmap loadImageWithinSize(Uri bitmapUri, int width, int height) throws FileNotFoundException {
        if (Math.min(width, height) <= 0) {
            return BitmapFactory.decodeStream(contentResolver.openInputStream(bitmapUri));
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(contentResolver.openInputStream(bitmapUri), null, options);

        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, width, height);

        options.inJustDecodeBounds = false;
        return resizeImage(BitmapFactory.decodeStream(contentResolver.openInputStream(bitmapUri), null, options),
                width, height);
    }

    public byte[] compressImageWithinSize(Uri bitmapUri, Bitmap.CompressFormat format, int quality, int width,
            int height) throws FileNotFoundException {
        Bitmap bitmap = loadImageWithinSize(bitmapUri, width, height);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (!bitmap.compress(format, quality, bos))
            return null;
        return bos.toByteArray();
    }
}
