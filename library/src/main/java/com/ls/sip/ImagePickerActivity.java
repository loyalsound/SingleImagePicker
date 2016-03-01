package com.ls.sip;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;

import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraSelectionCriteria;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.FocusMode;
import com.commonsware.cwac.cam2.ImagePickerIntentBuilder;
import com.commonsware.cwac.cam2.PublicCameraController;
import com.commonsware.cwac.cam2.util.Utils;
import com.yalantis.ucrop.UCrop;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class ImagePickerActivity extends AppCompatActivity implements OnPageChangeListener {

    /**
     * List<FlashMode> indicating the desired flash modes,
     * or null for always taking the default. These are
     * considered in priority-first order (i.e., we will use
     * the first FlashMode if the device supports it, otherwise
     * we will use the second FlashMode, ...). If there is no
     * match, whatever the default device behavior is will be
     * used.
     */
    public static final String EXTRA_FLASH_MODES = "com_ls_sip_flash_modes";

    /**
     * True if we should allow the user to change the flash mode
     * on the fly (if the camera supports it), false otherwise.
     * Defaults to false.
     */
    public static final String EXTRA_ALLOW_SWITCH_FLASH_MODE = "com_ls_sip_allow_switch_flash_mode";

    /**
     * Extra name for indicating what facing rule for the
     * camera you wish to use. The value should be a
     * CameraSelectionCriteria.Facing instance.
     */
    public static final String EXTRA_FACING = "com_ls_sip_facing";

    /**
     * Extra name for indicating that the requested facing
     * must be an exact match, without gracefully degrading to
     * whatever camera happens to be available. If set to true,
     * requests to take a picture, for which the desired camera
     * is not available, will be cancelled. Defaults to false.
     */
    public static final String EXTRA_FACING_EXACT_MATCH = "com_ls_sip_facing_exact_match";

    /**
     * Extra name for indicating whether extra diagnostic
     * information should be reported, particularly for errors.
     * Default is false.
     */
    public static final String EXTRA_DEBUG_ENABLED = "com_ls_sip_debug";

    /**
     * Extra name for indicating if MediaStore should be updated
     * to reflect a newly-taken picture. Only relevant if
     * a file:// Uri is used. Default to false.
     */
    public static final String EXTRA_UPDATE_MEDIA_STORE = "com_ls_sip_update_media_store";

    /**
     * If set to true, forces the use of the ClassicCameraEngine
     * on Android 5.0+ devices. Has no net effect on Android 4.x
     * devices. Defaults to false.
     */
    public static final String EXTRA_FORCE_CLASSIC = "com_ls_sip_force_classic";

    /**
     * If set to true, horizontally flips or mirrors the preview.
     * Does not change the picture or video output. Used mostly for FFC,
     * though will be honored for any camera. Defaults to false.
     */
    public static final String EXTRA_MIRROR_PREVIEW = "com_ls_sip_mirror_preview";

    /**
     * Extra name for focus mode to apply. Value should be one of the
     * AbstractCameraActivity.FocusMode enum values. Default is CONTINUOUS.
     * If the desired focus mode is not available, the device default
     * focus mode is used.
     */
    public static final String EXTRA_FOCUS_MODE = "com_ls_sip_focus_mode";

    public static final String EXTRA_SAVE_INTERMEDIATE_FILE = "com_ls_sip_save_intermediate_file";
    public static final String EXTRA_REQUIRE_CROP = "com_ls_sip_require_crop";

    public static final String EXTRA_ASPECT_RATIO_SET = "com_ls_sip_aspect_ratio_set";
    public static final String EXTRA_ASPECT_RATIO_X = "com_ls_sip_aspect_ratio_x";
    public static final String EXTRA_ASPECT_RATIO_Y = "com_ls_sip_aspect_ratio_y";

    public static final String EXTRA_MAX_SIZE_SET = "com_ls_sip_max_size_set";
    public static final String EXTRA_MAX_SIZE_X = "com_ls_sip_max_size_x";
    public static final String EXTRA_MAX_SIZE_Y = "com_ls_sip_max_size_y";
    public static final String EXTRA_USE_SOURCE_IMAGE_ASPECT_RATIO = "com_ls_sip_use_source_image_ratio";

    public static final String[] PERMS = new String[]{"android.permission.CAMERA"};

    protected static final String TAG = ImagePickerActivity.class.getCanonicalName();

    private static final int REQUEST_PERMS = 13401;
    private static final int REQUEST_CROP = 13402;

    protected Toolbar mToolbar;
    protected TextView mTitleTextView;
    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;

    protected PickerPagerAdapter mAdapter;
    protected Uri mOutputUri;
    protected Uri mIntermediateUri;
    protected boolean mAlreadyHasRequiredPermissions;

    private int mToolbarIconColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.validateEnvironment(this);

        int[] toolbarIconColorAttr = new int[] { R.attr.sipToolbarIconColor };
        TypedArray a = obtainStyledAttributes(toolbarIconColorAttr);
        mToolbarIconColor = a.getColor(0, ContextCompat.getColor(this, android.R.color.white));
        a.recycle();

        setContentView(R.layout.sip_activity_image_picker);

        initView();

        if (useRuntimePermissions()) {
            mAlreadyHasRequiredPermissions = false;
            String[] needRequestPermissions = netPermissions();
            if (needRequestPermissions.length == 0) {
                mAlreadyHasRequiredPermissions = true;
            } else {
                requestPermissions(needRequestPermissions, REQUEST_PERMS);
            }
        } else {
            mAlreadyHasRequiredPermissions = true;
        }

        setupTabs();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs_layout);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            Drawable upIndicator = ContextCompat.getDrawable(this, R.drawable.sip_ic_close_white_24dp);
            upIndicator.setColorFilter(mToolbarIconColor, PorterDuff.Mode.SRC_ATOP);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(upIndicator);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String[] perms = netPermissions();

        if (perms.length == 0) {
            initCamera(mAdapter.getCameraFragment());
        } else {
            // TODO: Allow use to pick image
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            mAdapter.getCameraFragment().performCameraAction();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraController.NoSuchCameraEvent event) {
        // TODO: Allow use to pick image
        finish();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraController.ControllerDestroyedEvent event) {
        // TODO: Allow use to pick image
        finish();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraEngine.CameraTwoGenericEvent event) {
        // TODO: Allow use to pick image
        finish();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraEngine.PictureTakenEvent event) {
        if (event.exception == null) {
            completeRequest(getIntermediateUri());
        } else {
            finish();
        }
    }

    public void onEventMainThread(GalleryFragment.PictureSelectedEvent event) {
        completeRequest(event.getUri());
    }

    private void completeRequest(final Uri uri) {
        if (getIntent().getBooleanExtra(EXTRA_REQUIRE_CROP, false)) {
            openCrop(uri);
        } else {
            setResultAndFinish(uri);
        }
    }

    private void setResultAndFinish(final Uri uri) {
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK, new Intent().setData(uri));
                finish();
            }
        });
    }

    private void openCrop(final Uri selectedUri) {
        Intent intent = buildCropIntent(selectedUri, getOutputUri());
        startActivityForResult(intent, REQUEST_CROP);
    }

    private Intent buildCropIntent(Uri source, Uri destination) {
        UCrop uCrop = UCrop.of(source, destination);
        if (getIntent().getBooleanExtra(EXTRA_ASPECT_RATIO_SET, false)) {
            uCrop.withAspectRatio(
                    getIntent().getFloatExtra(EXTRA_ASPECT_RATIO_X, 0),
                    getIntent().getFloatExtra(EXTRA_ASPECT_RATIO_Y, 0));
        }

        if (getIntent().getBooleanExtra(EXTRA_USE_SOURCE_IMAGE_ASPECT_RATIO, false)) {
            uCrop.useSourceImageAspectRatio();
        }

        if (getIntent().getBooleanExtra(EXTRA_MAX_SIZE_SET, false)) {
            uCrop.withMaxResultSize(
                    getIntent().getIntExtra(EXTRA_MAX_SIZE_X, 0),
                    getIntent().getIntExtra(EXTRA_MAX_SIZE_Y, 0));
        }
        return uCrop
                .getIntent(ImagePickerActivity.this)
                .setClass(ImagePickerActivity.this, ImageCropActivity.class);
    }

    private Uri getOutputUri() {
        if (mOutputUri == null) {
            Uri output = getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT);

            if (output == null) {
                boolean updateMediaStore = getIntent().getBooleanExtra(EXTRA_UPDATE_MEDIA_STORE, false);
                if (updateMediaStore) {
                    output = com.ls.sip.Utils.getPhotoPath();
                } else {
                    try {
                        output = com.ls.sip.Utils.getCachePhotoPath(this);
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot create cache file to store photo/video", e);
                        output = com.ls.sip.Utils.getPhotoPath();
                    }
                }
            }
            mOutputUri = output;
        }

        return mOutputUri;
    }

    /**
     * Return {@link Uri} used to store picture after taken.
     * If crop is not enabled, this method will return same result as {@link #getOutputUri()}.
     * If required to save intermediate file, we should store image into a public directory.
     */
    private Uri getIntermediateUri() {
        if (!getIntent().getBooleanExtra(EXTRA_REQUIRE_CROP, false)) {
            return getOutputUri();
        }
        if (mIntermediateUri == null) {
            if (getIntent().getBooleanExtra(EXTRA_SAVE_INTERMEDIATE_FILE, false)) {
                mIntermediateUri = com.ls.sip.Utils.getPhotoPath();
            } else {
                try {
                    mIntermediateUri = com.ls.sip.Utils.getCachePhotoPath(this);
                } catch (IOException e) {
                    Log.e(TAG, "Cannot create cache file to store photo/video", e);
                    mIntermediateUri = com.ls.sip.Utils.getPhotoPath();
                }
            }
        }
        return mIntermediateUri;
    }

    private void setupTabs() {
        mAdapter = new PickerPagerAdapter(this, getSupportFragmentManager());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTitleTextView.setText(mAdapter.getPageTitle(0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);

            // Update mediastore if required
            if (getIntent().getBooleanExtra(EXTRA_UPDATE_MEDIA_STORE, false) && resultUri != null) {
                new Thread() {
                    @Override
                    public void run() {
                        SystemClock.sleep(2000);
                        MediaScannerConnection.scanFile(
                                ImagePickerActivity.this,
                                new String[]{ resultUri.getPath() },
                                null,
                                null);
                    }
                }.start();
            }
            setResultAndFinish(resultUri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Log.e(TAG, "Fail to crop image", cropError);
            finish();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mTitleTextView.setText(mAdapter.getPageTitle(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return !useRuntimePermissions() || checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean useRuntimePermissions() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    private String[] netPermissions() {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : PERMS) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return (result.toArray(new String[result.size()]));
    }

    protected void initCamera(CameraFragment cameraFragment) {
        FocusMode focusMode = (FocusMode) getIntent().getSerializableExtra(EXTRA_FOCUS_MODE);
        boolean allowChangeFlashMode = getIntent().getBooleanExtra(EXTRA_ALLOW_SWITCH_FLASH_MODE, false);

        PublicCameraController ctrl = new PublicCameraController(focusMode, allowChangeFlashMode, false);

        cameraFragment.setController(ctrl);
        cameraFragment.setMirrorPreview(getIntent().getBooleanExtra(EXTRA_MIRROR_PREVIEW, false));

        Facing facing = (Facing) getIntent().getSerializableExtra(EXTRA_FACING);

        if (facing == null) {
            facing = Facing.BACK;
        }

        boolean match = getIntent().getBooleanExtra(EXTRA_FACING_EXACT_MATCH, false);

        CameraSelectionCriteria criteria =
                new CameraSelectionCriteria.Builder()
                        .facing(facing)
                        .facingExactMatch(match)
                        .build();

        boolean forceClassic = getIntent().getBooleanExtra(EXTRA_FORCE_CLASSIC, false);

        if ("samsung".equals(Build.MANUFACTURER) &&
                ("ha3gub".equals(Build.PRODUCT) || "k3gxx".equals(Build.PRODUCT))) {
            forceClassic = true;
        }

        ctrl.setEngine(CameraEngine.buildInstance(this, forceClassic), criteria);
        ctrl.getEngine().setDebug(getIntent().getBooleanExtra(EXTRA_DEBUG_ENABLED, false));

        this.configEngine(ctrl.getEngine());
    }

    protected void configEngine(CameraEngine engine) {
        Method[] methods = CameraEngine.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("setPreferredFlashModes")) {
                method.setAccessible(true);
                List<FlashMode> flashModes = new ArrayList<FlashMode>();
                flashModes.add(FlashMode.AUTO);
                try {
                    method.invoke(engine, flashModes);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    // TODO: Do something here
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    // TODO: Do something here
                }
                break;
            }
        }
    }

    private class PickerPagerAdapter extends FragmentPagerAdapter {

        private String[] tabTitles;
        private Fragment[] fragments = new Fragment[2];

        public PickerPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            tabTitles = context.getResources().getStringArray(R.array.sip_tab_titles);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (fragments[position] == null) {
                fragments[position] = createFragment(position);
            }

            return fragments[position];
        }

        private Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new GalleryFragment();
                case 1:
                    // Force update media store if required to save intermediate file
                    boolean updateMediaStore =
                            (getIntent().getBooleanExtra(EXTRA_UPDATE_MEDIA_STORE, false)
                                && !getIntent().getBooleanExtra(EXTRA_REQUIRE_CROP, false))
                            || (getIntent().getBooleanExtra(EXTRA_SAVE_INTERMEDIATE_FILE, false)
                                && getIntent().getBooleanExtra(EXTRA_REQUIRE_CROP, false));

                    CameraFragment cameraFragment = CameraFragment.newPictureInstance(getIntermediateUri(), updateMediaStore);
                    if (mAlreadyHasRequiredPermissions) {
                        initCamera(cameraFragment);
                    }
                    return cameraFragment;
            }
            return null;
        }

        private CameraFragment getCameraFragment() {
            return (CameraFragment) getItem(1);
        }

    }

    /**
     * Class to build an Intent used to start the ImagePickerActivity.
     */
    public static class IntentBuilder extends ImagePickerIntentBuilder {

        private CropOptions cropOptions = new CropOptions();

        /**
         * Standard constructor. May throw a runtime exception
         * if the environment is not set up properly (see
         * validateEnvironment() on Utils).
         *
         * @param ctxt any Context will do
         */
        public IntentBuilder(Context ctxt) {
            super(ctxt, ImagePickerActivity.class);
        }

        public CropOptions crop() {
            result.putExtra(EXTRA_REQUIRE_CROP, true);
            return cropOptions;
        }
        
        public class CropOptions {

            /**
             * Set an aspect ratio for crop bounds.
             *
             * @param x aspect ratio X
             * @param y aspect ratio Y
             */
            public CropOptions withAspectRatio(float x, float y) {
                result.putExtra(EXTRA_ASPECT_RATIO_SET, true);
                result.putExtra(EXTRA_ASPECT_RATIO_X, x);
                result.putExtra(EXTRA_ASPECT_RATIO_Y, y);
                return this;
            }

            /**
             * Set an aspect ratio for crop bounds that is evaluated from source image width and height.
             */
            public CropOptions useSourceImageAspectRatio() {
                result.putExtra(EXTRA_USE_SOURCE_IMAGE_ASPECT_RATIO, true);
                result.removeExtra(EXTRA_ASPECT_RATIO_SET);
                result.removeExtra(EXTRA_ASPECT_RATIO_X);
                result.removeExtra(EXTRA_ASPECT_RATIO_Y);
                return this;
            }

            /**
             * Set maximum size for result cropped image.
             *
             * @param width  max cropped image width
             * @param height max cropped image height
             */
            public CropOptions withMaxResultSize(@IntRange(from = 100) int width, @IntRange(from = 100) int height) {
                result.putExtra(EXTRA_MAX_SIZE_SET, true);
                result.putExtra(EXTRA_MAX_SIZE_X, width);
                result.putExtra(EXTRA_MAX_SIZE_Y, height);
                return this;
            }

            public CropOptions saveIntermediateFile() {
                result.putExtra(EXTRA_SAVE_INTERMEDIATE_FILE, true);
                return this;
            }

            public IntentBuilder and() {
                return IntentBuilder.this;
            }
        }
    }

}