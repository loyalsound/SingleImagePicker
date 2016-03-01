package com.ls.sip;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.BitmapLoadUtils;
import com.yalantis.ucrop.view.CropImageView;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;

import java.io.OutputStream;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
public class ImageCropActivity extends AppCompatActivity {

    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    private static final String TAG = "ImageCropActivity";

    private int mToolbarIconColor;

    private UCropView mUCropView;
    private GestureCropImageView mGestureCropImageView;

    private Uri mOutputUri;

    private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
    private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int[] toolbarIconColorAttr = new int[] { R.attr.sipToolbarIconColor };
        TypedArray a = obtainStyledAttributes(toolbarIconColorAttr);
        mToolbarIconColor = a.getColor(0, ContextCompat.getColor(this, android.R.color.white));
        a.recycle();

        setContentView(R.layout.sip_activity_image_crop);

        setupViews();
        setImageData();

        mGestureCropImageView.setScaleEnabled(true);
        mGestureCropImageView.setRotateEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.sip_menu_confirm, menu);

        // Change the next menu icon color to match the rest of the UI colors
        MenuItem done = menu.findItem(R.id.sip_action_done);

        Drawable defaultIcon = done.getIcon();
        if (defaultIcon != null) {
            defaultIcon.mutate();
            defaultIcon.setColorFilter(mToolbarIconColor, PorterDuff.Mode.SRC_ATOP);
            done.setIcon(defaultIcon);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sip_action_done) {
            cropAndSaveImage();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGestureCropImageView != null) {
            mGestureCropImageView.cancelAllAnimations();
        }
    }

    /**
     * This method extracts all data from the incoming intent and setups views properly.
     */
    private void setImageData() {
        final Intent intent = getIntent();

        Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_INPUT_URI);
        mOutputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
        processOptions(intent);

        if (inputUri != null && mOutputUri != null) {
            try {
                mGestureCropImageView.setImageUri(inputUri);
            } catch (Exception e) {
                setResultException(e);
                finish();
            }
        } else {
            setResultException(new NullPointerException(getString(R.string.ucrop_error_input_data_is_absent)));
            finish();
        }

        if (intent.getBooleanExtra(UCrop.EXTRA_ASPECT_RATIO_SET, false)) {
            float aspectRatioX = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_X, 0.0f);
            float aspectRatioY = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_Y, 0.0f);

            if (aspectRatioX > 0 && aspectRatioY > 0) {
                mGestureCropImageView.setTargetAspectRatio(aspectRatioX / aspectRatioY);
            } else {
                mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
            }
        }

        if (intent.getBooleanExtra(UCrop.EXTRA_MAX_SIZE_SET, false)) {
            int maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0);
            int maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0);

            if (maxSizeX > 0 && maxSizeY > 0) {
                mGestureCropImageView.setMaxResultImageSizeX(maxSizeX);
                mGestureCropImageView.setMaxResultImageSizeY(maxSizeY);
            } else {
                Log.w(TAG, "EXTRA_MAX_SIZE_X and EXTRA_MAX_SIZE_Y must be greater than 0");
            }
        }
    }

    /**
     * This method extracts {@link com.yalantis.ucrop.UCrop.Options #optionsBundle} from incoming intent
     * and setups Activity, {@link OverlayView} and {@link CropImageView} properly.
     */
    private void processOptions(@NonNull Intent intent) {
        Bundle optionsBundle = intent.getBundleExtra(UCrop.EXTRA_OPTIONS);
        if (optionsBundle != null) {
            // Bitmap compression options
            String compressionFormatName = optionsBundle.getString(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME);
            Bitmap.CompressFormat compressFormat = null;
            if (!TextUtils.isEmpty(compressionFormatName)) {
                compressFormat = Bitmap.CompressFormat.valueOf(compressionFormatName);
            }
            mCompressFormat = (compressFormat == null) ? DEFAULT_COMPRESS_FORMAT : compressFormat;

            mCompressQuality = optionsBundle.getInt(UCrop.Options.EXTRA_COMPRESSION_QUALITY, ImageCropActivity.DEFAULT_COMPRESS_QUALITY);

            // Crop image view options
            mGestureCropImageView.setMaxBitmapSize(optionsBundle.getInt(UCrop.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE));
            mGestureCropImageView.setMaxScaleMultiplier(optionsBundle.getFloat(UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER));
            mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(optionsBundle.getInt(UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION));
        }
    }

    private void setupViews() {
        setupAppBar();
        initiateRootViews();
    }

    /**
     * Configures and styles both status bar and toolbar.
     */
    private void setupAppBar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ((TextView) toolbar.findViewById(R.id.title_text_view)).setText(R.string.sip_crop_image);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            Drawable upIndicator = ContextCompat.getDrawable(this, R.drawable.sip_ic_arrow_back_white_24dp);
            upIndicator.setColorFilter(mToolbarIconColor, PorterDuff.Mode.SRC_ATOP);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(upIndicator);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initiateRootViews() {
        mUCropView = (UCropView) findViewById(R.id.ucrop);
        mGestureCropImageView = mUCropView.getCropImageView();

        mGestureCropImageView.setTransformImageListener(mImageListener);
    }

    private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {

        }

        @Override
        public void onScale(float currentScale) {

        }

        @Override
        public void onLoadComplete() {
            View ucropView = findViewById(R.id.ucrop);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ucrop_fade_in);
            fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mUCropView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            ucropView.startAnimation(fadeInAnimation);
        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
            setResultException(e);
            finish();
        }

    };

    private void resetRotation() {
        mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void rotateByAngle(int angle) {
        mGestureCropImageView.postRotate(angle);
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void cropAndSaveImage() {
        OutputStream outputStream = null;
        try {
            final Bitmap croppedBitmap = mGestureCropImageView.cropImage();
            if (croppedBitmap != null) {
                outputStream = getContentResolver().openOutputStream(mOutputUri);
                croppedBitmap.compress(mCompressFormat, mCompressQuality, outputStream);
                croppedBitmap.recycle();

                setResultUri(mOutputUri, mGestureCropImageView.getTargetAspectRatio());
                finish();
            } else {
                setResultException(new NullPointerException("CropImageView.cropImage() returned null."));
            }
        } catch (Exception e) {
            setResultException(e);
            finish();
        } finally {
            BitmapLoadUtils.close(outputStream);
        }
    }

    private void setResultUri(Uri uri, float resultAspectRatio) {
        setResult(RESULT_OK, new Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio));
    }

    private void setResultException(Throwable throwable) {
        setResult(UCrop.RESULT_ERROR, new Intent().putExtra(UCrop.EXTRA_ERROR, throwable));
    }

}