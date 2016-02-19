package com.ls.sip.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ls.sip.Config;
import com.ls.sip.ImagePickerActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int INTENT_REQUEST_GET_IMAGES = 13;

    ArrayList<Uri> image_uris = new ArrayList<>();
    private ViewGroup mSelectedImagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSelectedImagesContainer = (ViewGroup) findViewById(R.id.selected_photos_container);
        View getImages = findViewById(R.id.get_images);
        getImages.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                getImages();
            }
        });


        View getImages2 = findViewById(R.id.get_images2);
        getImages2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Config config = new Config();
                config.setCameraHeight(R.dimen.app_camera_height);
                config.setSelectedBottomHeight(R.dimen.bottom_height);
                config.setFlashOn(true);


                getImages();
            }
        });

    }

    private void getImages() {

        Intent intent = new Intent(this, ImagePickerActivity.class);

//        if (image_uris != null) {
//            intent.putParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS, image_uris);
//        }


        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);

    }


    @Override
    protected void onActivityResult(int requestCode, int resuleCode, Intent intent) {
        super.onActivityResult(requestCode, resuleCode, intent);


        if (resuleCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_REQUEST_GET_IMAGES) {
//                image_uris = intent.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);

                if (image_uris != null) {
                    showMedia();
                }
            }
        }
    }


    private void showMedia() {
        // Remove all views before
        // adding the new ones.
        mSelectedImagesContainer.removeAllViews();
        if (image_uris.size() >= 1) {
            mSelectedImagesContainer.setVisibility(View.VISIBLE);
        }

        int wdpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        int htpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());


        for (Uri uri : image_uris) {

            View imageHolder = LayoutInflater.from(this).inflate(R.layout.image_item, null);
            ImageView thumbnail = (ImageView) imageHolder.findViewById(R.id.media_image);

            Glide.with(this)
                    .load(uri.toString())
                    .fitCenter()
                    .into(thumbnail);

            mSelectedImagesContainer.addView(imageHolder);

            thumbnail.setLayoutParams(new FrameLayout.LayoutParams(wdpx, htpx));
        }

    }
}
