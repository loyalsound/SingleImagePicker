package com.ls.sip;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
    public static final String EXTRA_FLASH_MODES =
            "cwac_cam2_flash_modes";

    /**
     * True if we should allow the user to change the flash mode
     * on the fly (if the camera supports it), false otherwise.
     * Defaults to false.
     */
    public static final String EXTRA_ALLOW_SWITCH_FLASH_MODE =
            "cwac_cam2_allow_switch_flash_mode";

    /**
     * Extra name for indicating what facing rule for the
     * camera you wish to use. The value should be a
     * CameraSelectionCriteria.Facing instance.
     */
    public static final String EXTRA_FACING = "cwac_cam2_facing";

    /**
     * Extra name for indicating that the requested facing
     * must be an exact match, without gracefully degrading to
     * whatever camera happens to be available. If set to true,
     * requests to take a picture, for which the desired camera
     * is not available, will be cancelled. Defaults to false.
     */
    public static final String EXTRA_FACING_EXACT_MATCH =
            "cwac_cam2_facing_exact_match";

    /**
     * Extra name for indicating whether extra diagnostic
     * information should be reported, particularly for errors.
     * Default is false.
     */
    public static final String EXTRA_DEBUG_ENABLED = "cwac_cam2_debug";

    /**
     * Extra name for indicating if MediaStore should be updated
     * to reflect a newly-taken picture. Only relevant if
     * a file:// Uri is used. Default to false.
     */
    public static final String EXTRA_UPDATE_MEDIA_STORE =
            "cwac_cam2_update_media_store";

    /**
     * If set to true, forces the use of the ClassicCameraEngine
     * on Android 5.0+ devices. Has no net effect on Android 4.x
     * devices. Defaults to false.
     */
    public static final String EXTRA_FORCE_CLASSIC = "cwac_cam2_force_classic";

    /**
     * If set to true, horizontally flips or mirrors the preview.
     * Does not change the picture or video output. Used mostly for FFC,
     * though will be honored for any camera. Defaults to false.
     */
    public static final String EXTRA_MIRROR_PREVIEW = "cwac_cam2_mirror_preview";

    /**
     * Extra name for focus mode to apply. Value should be one of the
     * AbstractCameraActivity.FocusMode enum values. Default is CONTINUOUS.
     * If the desired focus mode is not available, the device default
     * focus mode is used.
     */
    public static final String EXTRA_FOCUS_MODE = "cwac_cam2_focus_mode";

    public static final String EXTRA_REQUIRE_CROP = "sip_require_crop";

    public static final String EXTRA_NEED_THUMBNAIL = "sip_need_thumbnail";

    public static final String[] PERMS = new String[]{"android.permission.CAMERA"};

    protected static final String TAG_CAMERA = ImagePickerActivity.class.getCanonicalName();

    private static final int REQUEST_PERMS = 13401;

    protected Toolbar mToolbar;
    protected TextView mTitleTextView;
    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;

    protected PickerPagerAdapter mAdapter;
    protected Uri mOutputUri;
    protected boolean mAlreadyHasRequiredPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.validateEnvironment(this);

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.sip_ic_close_white_24dp);
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
            completeRequest(getOutputUri());
        } else {
            finish();
        }
    }

    public void completeRequest(final Uri selectedUri) {
//        boolean needsThumbnail = getIntent().getBooleanExtra(EXTRA_NEED_THUMBNAIL, false);
//        if (needsThumbnail) {
//            final Intent result = new Intent();
//
//            result.putExtra("data", imageContext.buildResultThumbnail());
//
//            findViewById(android.R.id.content).post(new Runnable() {
//                @Override
//                public void run() {
//                    setResult(RESULT_OK, result);
////                        removeFragments();
//                }
//            });
//        }
//        else {
            findViewById(android.R.id.content).post(new Runnable() {
                @Override
                public void run() {
                    setResult(RESULT_OK, new Intent().setData(selectedUri));
                    finish();
//                        removeFragments();
                }
            });
//        }
    }

    protected Uri getOutputUri() {
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
                        Log.e(TAG_CAMERA, "Cannot create cache file to store photo/video", e);
                        output = com.ls.sip.Utils.getPhotoPath();
                    }
                }
            }
            mOutputUri = output;
        }

        return mOutputUri;
    }

    private void setupTabs() {
        mAdapter = new PickerPagerAdapter(this, getSupportFragmentManager());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTitleTextView.setText(mAdapter.getPageTitle(0));
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.sip_menu_confirm, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
//        else if (id == R.id.sip_action_done) {
//            updatePicture();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

//    private void updatePicture() {
//        // TODO: Fix message
//        if (mOutputUri == null) {
//            return;
//        }
//
//        Intent intent = new Intent();
//        intent.putExtra(EXTRA_IMAGE_URI, mOutputUri);
//        setResult(Activity.RESULT_OK, intent);
//        finish();
//    }

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
                    CameraFragment cameraFragment = CameraFragment.newPictureInstance(getOutputUri(), getIntent().getBooleanExtra(EXTRA_UPDATE_MEDIA_STORE, false));
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

        public IntentBuilder debugSavePreviewFrame() {
            //result.putExtra(EXTRA_DEBUG_SAVE_PREVIEW_FRAME, true);

            return(this);
        }
    }

}