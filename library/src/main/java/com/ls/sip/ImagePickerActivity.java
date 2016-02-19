/*
 * Copyright (c) 2016. Ted Park. All Rights Reserved
 */

package com.ls.sip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;

public class ImagePickerActivity extends AppCompatActivity implements CameraHostProvider, OnPageChangeListener {

    /**
     * Returns the parcelled image uris in the intent with this extra.
     */
    public static final String EXTRA_IMAGE_URI = "image_uri";

    public static final String EXTRA_CONFIGS = "configs";

    public static CwacCameraFragment.MyCameraHost mMyCameraHost;

    protected Uri mSelectedImage;

    protected Config mConfig;

    protected Toolbar mToolbar;
    protected TextView mTitleTextView;
    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;

    protected PickerPagerAdapter mAdapter;

    @Override
    public CameraHost getCameraHost() {
        return mMyCameraHost;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFromSavedInstanceState(savedInstanceState);

        if (mConfig == null) {
            mConfig = new Config();
        }

        setContentView(R.layout.picker_activity_main_pp);

        initView();

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

    private void setupFromSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mSelectedImage = savedInstanceState.getParcelable(EXTRA_IMAGE_URI);
            mConfig = savedInstanceState.getParcelable(EXTRA_CONFIGS);
        } else {
            mSelectedImage = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
            mConfig = getIntent().getParcelableExtra(EXTRA_CONFIGS);
        }

        // TODO: Rebind data if needed
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EXTRA_IMAGE_URI, mSelectedImage);
        outState.putParcelable(EXTRA_CONFIGS, mConfig);
    }

    private void setupTabs() {
        mAdapter = new PickerPagerAdapter(this, getSupportFragmentManager());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTitleTextView.setText(mAdapter.getPageTitle(0));

        // TODO: Remove style from config
        if (mConfig != null) {

            if (mConfig.getTabBackgroundColor() > 0) {
                mTabLayout.setBackgroundColor(mConfig.getTabBackgroundColor());
            }

            if (mConfig.getTabSelectionIndicatorColor() > 0) {
                mTabLayout.setSelectedTabIndicatorColor(mConfig.getTabSelectionIndicatorColor());
            }
        }
    }

    public GalleryFragment getGalleryFragment() {
        if (mAdapter == null || mAdapter.getCount() < 2) {
            return null;
        }

        return (GalleryFragment) mAdapter.getItem(1);
    }

//    public boolean addImage(final Uri uri) {
//
//        if (mSelectedImages.size() == mConfig.getSelectionLimit()) {
//            String text = String.format(getResources().getString(R.string.max_count_msg), mConfig.getSelectionLimit());
//            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (mSelectedImages.add(uri)) {
//            View rootView = LayoutInflater.from(this).inflate(R.layout.picker_list_item_selected_thumbnail, null);
//            ImageView thumbnail = (ImageView) rootView.findViewById(R.id.selected_photo);
//            ImageView iv_close = (ImageView) rootView.findViewById(R.id.iv_close);
//            iv_close.setImageResource(mConfig.getSelectedCloseImage());
//
//
//            rootView.setTag(uri);
//
//
//            //  mImageFetcher.loadImage(image.mUri, thumbnail);
//            mSelectedImagesContainer.addView(rootView, 0);
//
//            int selected_bottom_size = (int) getResources().getDimension(mConfig.getSelectedBottomHeight());
//
//            //noinspection SuspiciousNameCombination
//            Glide.with(getApplicationContext())
//                    .load(uri.toString())
//                    .override(selected_bottom_size, selected_bottom_size)
//                    .dontAnimate()
//                    .centerCrop()
//                    .error(R.drawable.sip_no_image)
//                    .into(thumbnail);
//
//            iv_close.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    removeImage(uri);
//
//                }
//            });
//
//
//            if (mSelectedImages.size() >= 1) {
//                mSelectedImagesContainer.setVisibility(View.VISIBLE);
//                mSelectedImageEmptyMessage.setVisibility(View.GONE);
//            }
//            return true;
//        }
//
//
//        return false;
//    }
//
//    public boolean removeImage(Uri uri) {
//
//
//        boolean result = mSelectedImages.remove(uri);
//
//
//        if (result) {
//
//            if (GalleryFragment.mGalleryAdapter != null) {
//                GalleryFragment.mGalleryAdapter.notifyDataSetChanged();
//            }
//
//            for (int i = 0; i < mSelectedImagesContainer.getChildCount(); i++) {
//                View childView = mSelectedImagesContainer.getChildAt(i);
//                if (childView.getTag().equals(uri)) {
//                    mSelectedImagesContainer.removeViewAt(i);
//                    break;
//                }
//            }
//
//            if (mSelectedImages.size() == 0) {
//                mSelectedImagesContainer.setVisibility(View.GONE);
//                mSelectedImageEmptyMessage.setVisibility(View.VISIBLE);
//            }
//
//
//        }
//        return result;
//    }
//
//    public boolean containsImage(Uri uri) {
//        return mSelectedImages.contains(uri);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_confirm, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_done) {
            updatePicture();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updatePicture() {
        // TODO: Fix message
        if (mSelectedImage == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_IMAGE_URI, mSelectedImage);
        setResult(Activity.RESULT_OK, intent);
        finish();
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

    private class PickerPagerAdapter extends FragmentPagerAdapter {

        private String[] tabTitles;

        public PickerPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            tabTitles = context.getResources().getStringArray(R.array.tab_titles);
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
            switch (position) {
                case 0:
                    return new GalleryFragment();
                case 1:
                    return CwacCameraFragment.createInstance(mConfig);
            }

            return null;

        }

    }

}