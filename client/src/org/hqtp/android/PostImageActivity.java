package org.hqtp.android;

import java.io.FileNotFoundException;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.google.inject.Inject;

public class PostImageActivity extends RoboActivity implements OnClickListener {
    public static final String LECTURE_ID = "LECTURE_ID";
    public static final String PREV_VIRTUAL_TS = "PREV_VIRTUAL_TS";
    public static final String NEXT_VIRTUAL_TS = "NEXT_VIRTUAL_TS";

    public static final String SAVED_IMAGE_URI = "SAVED_IMAGE_URI";

    public static final int INTENT_CAMERA_REQUESTCODE = 100;
    public static final int INTENT_GALLERY_REQUESTCODE = 200;

    public static final int MAX_WIDTH = 1024;
    public static final int MAX_HEIGHT = 768;

    private static final Bitmap.CompressFormat FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int QUALITY = 50;

    @Inject
    APIClient proxy;
    @Inject
    Alerter alerter;
    @Inject
    ImageConverter imageConverter;
    @InjectView(R.id.postImageButton)
    Button postButton;
    @InjectView(R.id.selectFromCameraButton)
    Button cameraButton;
    @InjectView(R.id.selectFromGalleryButton)
    Button galleryButton;
    @InjectView(R.id.postImageView)
    ImageView imageView;
    @InjectView(R.id.profileView)
    ProfileView profileView;

    @InjectExtra(LECTURE_ID)
    int lectureId;
    @InjectExtra(PREV_VIRTUAL_TS)
    long prevVirtualTimestamp;
    @InjectExtra(NEXT_VIRTUAL_TS)
    long nextVirtualTimestamp;

    Uri imageUri;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_image);
        assert ((prevVirtualTimestamp != -1 && nextVirtualTimestamp != -1) || (prevVirtualTimestamp == -1 && nextVirtualTimestamp == -1));

        if (savedInstanceState != null) {
            String imageUriString = savedInstanceState.getString(SAVED_IMAGE_URI);
            if (imageUriString != null) {
                imageUri = Uri.parse(imageUriString);
                int width = MAX_WIDTH, height = MAX_HEIGHT;
                // Here, imageView's size is happened to be zero. Use default size to avoid OutOfMemory.
                if (imageView.getWidth() != 0 && imageView.getHeight() != 0) {
                    width = imageView.getWidth();
                    height = imageView.getHeight();
                }
                try {
                    imageView.setImageBitmap(imageConverter.loadImageWithinSize(imageUri, width, height));
                } catch (FileNotFoundException e) {
                    alerter.alert("HQTP", "画像のロード時にエラーが発生しました");
                    imageUri = null;
                }
            }
        }

        postButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.postImageButton) {
            if (imageUri == null) {
                alerter.toastShort("画像を選択してください");
            } else {
                new PostImageTask(imageUri,
                        lectureId,
                        prevVirtualTimestamp,
                        nextVirtualTimestamp).execute();
            }
        } else if (v.getId() == R.id.selectFromCameraButton) {
            String imageFileName = System.currentTimeMillis() + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, imageFileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, INTENT_CAMERA_REQUESTCODE);
        } else if (v.getId() == R.id.selectFromGalleryButton) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, INTENT_GALLERY_REQUESTCODE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_GALLERY_REQUESTCODE) {
                imageUri = data.getData();
            }

            imageView.setImageBitmap(null);

            try {
                imageView.setImageBitmap(imageConverter.loadImageWithinSize(imageUri, imageView.getWidth(),
                        imageView.getHeight()));
            } catch (FileNotFoundException e) {
                alerter.alert("HQTP", "画像のロード時にエラーが発生しました");
                imageUri = null;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        profileView.startRecurringUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        profileView.stop();
    }

    @Override
    public void onBackPressed() {
        // Do not call super. Calling it leads to finish this activity.
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            super.onSaveInstanceState(outState);
            outState.putString(SAVED_IMAGE_URI, imageUri.toString());
        }
    }

    /**
     * Used to post a text to the timeline.
     * <p>
     * This class will disable the form and the button. If failed, this class shows alert and re-enables them. If
     * succeed, this class set the activity result and finish it.
     * </p>
     */
    private class PostImageTask extends RoboAsyncTask<Void> {
        private final Uri imageUri;
        private final int lectureId;
        private final long prevVirtualTimestamp;
        private final long nextVirtualTimestamp;

        public PostImageTask(Uri imageUri, int lectureId, long prevVirtualTimestamp, long nextVirtualTimestamp) {
            super(PostImageActivity.this);
            this.imageUri = imageUri;
            this.lectureId = lectureId;
            this.prevVirtualTimestamp = prevVirtualTimestamp;
            this.nextVirtualTimestamp = nextVirtualTimestamp;
        }

        @Override
        protected void onPreExecute() throws Exception {
            PostImageActivity.this.cameraButton.setEnabled(false);
            PostImageActivity.this.galleryButton.setEnabled(false);
            PostImageActivity.this.postButton.setEnabled(false);
        }

        @Override
        public Void call() throws Exception {
            byte[] bytes = imageConverter.compressImageWithinSize(imageUri, FORMAT, QUALITY, MAX_WIDTH, MAX_HEIGHT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            proxy.postTimeline(bitmap, lectureId, prevVirtualTimestamp, nextVirtualTimestamp);
            return null;
        }

        @Override
        protected void onSuccess(Void t) throws Exception {
            PostImageActivity.this.setResult(RESULT_OK);
            PostImageActivity.this.finish();
        }

        @Override
        protected void onException(Exception e) throws RuntimeException {
            e.printStackTrace();
            alerter.toastShort("投稿に失敗しました");
            PostImageActivity.this.cameraButton.setEnabled(true);
            PostImageActivity.this.galleryButton.setEnabled(true);
            PostImageActivity.this.postButton.setEnabled(true);
        }
    }
}
