#What is SingleImagePicker?

SingleImagePicker is image selector library for android that allow you select single image.<br />
Do not waste your time for write image select function code. You can take a picture or select image from gallery.<br />

Also you can customize color, drawable, etc for your application.

I use cwac-cam2 for take a picture.

## Demo
![Screenshot](images/image1.png) 

![Screenshot](images/image2.png)     
  
![Screenshot](images/image3.png)     

##Setup

#####Gradle
```javascript
repositories {
    mavenCentral()
    maven { url "https://s3.amazonaws.com/repo.commonsware.com" }
}

dependencies {
      compile 'com.loyalsound:android-single-image-picker:0.0.2'
}
```

#####Permission
Add permission for Camera, External Storage.

```javascript
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

#####Activity
Declare Activity in your `AndroidManifest.xml`

```javascript
<activity
        android:name="com.ls.sip.ImagePickerActivity"
        android:screenOrientation="portrait"
        android:theme="@style/Sip.Theme" />
        
<activity
        android:name="com.ls.sip.ImageCropActivity"
        android:screenOrientation="portrait"
        android:theme="@style/Sip.Theme" />
```

If you want to customize theme, just simply extend original theme like this.
ImagePickerActivity use toolbar without actionbar
```javascript
<style name="MySipTheme" parent="Sip.Theme">
    
    <item name="sipBackgroundColor">@color/sip_background_color</item>
    <item name="sipTitleTextColor">@color/sip_title_text_color</item>
    <item name="sipTabTextColor">@color/sip_tab_text_color</item>
    <item name="sipTabSelectedTextColor">@color/sip_tab_selected_text_color</item>
    <item name="sipButtonTextColor">@color/sip_button_text_color</item>
    <item name="sipToolbarIconColor">@color/sip_toolbar_icon_color</item>

    <item name="sipTabStyle">@style/Sip.Theme.TabStyle</item>
    <item name="sipTitleTextStyle">@style/Sip.Theme.TitleTextStyle</item>

    <item name="sipCropViewStyle">@style/Sip.Theme.CropViewStyle</item>
    <item name="sipCropBackground">@drawable/sip_cropview_bg_repeat</item>

</style>
```

Customize style of crop screen
```javascript
<style name="MySipTheme.CropViewStyle" parent="Sip.Theme.CropViewStyle">
    <item name="sipDimmedColor">@color/sip_crop_dimmed_color</item>
    <item name="sipOvalDimmedLayer">false</item>
    <item name="sipShowCropFrame">true</item>
    <item name="sipCropFrameColor">@color/sip_crop_frame_color</item>
    <item name="sipCropFrameStrokeWidth">@dimen/sip_crop_frame_stroke_width</item>
    <item name="sipShowCropGrid">true</item>
    <item name="sipCropGridRowCount">4</item>
    <item name="sipCropGridColumnCount">4</item>
    <item name="sipCropGridColor">@color/sip_crop_grid_color</item>
    <item name="sipCropGridStrokeWidth">@dimen/sip_crop_grid_stroke_width</item>
</style>
```

##How to use

#####1. Start Activity
Add your request code for `startActivityForResult()` and start `ImagePickerActivity`

```javascript
private static final int INTENT_REQUEST_GET_IMAGES = 13;

private void chooseImage() {

    Intent i = new ImagePickerActivity.IntentBuilder(MainActivity.this)
                .crop()
                    .useSourceImageAspectRatio()
                    .saveIntermediateFile()
                    .and()
                .build();
    
    startActivityForResult(i, INTENT_REQUEST_GET_IMAGES);

}
```


#####2. Receive Activity
If you finish image select, you will recieve image path array (Uri type)
```javascript
@Override
protected void onActivityResult(int requestCode, int resuleCode, Intent intent) {
    super.onActivityResult(requestCode, resuleCode, intent);

    if (requestCode == INTENT_REQUEST_GET_IMAGES && resuleCode == Activity.RESULT_OK ) {

        Uri selectedImageUri = intent.getData();

        //do something
    }
}
```

## Contributions
* Thanh Nguyen [@9you](https://github.com/9you)

## Changes log
* 0.0.2: Add cropping image feature. Change `statusBarColor` and `navigationBarColor`

## Thanks
* [CommonsGuy](https://github.com/commonsguy) for [Cam2](https://github.com/commonsguy/cwac-cam2) - A taking picture library
* [Yalantis](https://github.com/Yalantis) for [UCrop](https://github.com/Yalantis/uCrop) - An image cropping library
* [bumptech](https://github.com/bumptech) for [Glide](https://github.com/bumptech/glide) - An image loading and caching library
* [Ted Park](https://github.com/ParkSangGwon/TedPicker) - Original project

## License 
```
Copyright 2016 LoyalSound Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```