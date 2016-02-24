package com.commonsware.cwac.cam2;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;


/**
 * @author Thanh
 * @since 2/26/16
 */
public class ImagePickerIntentBuilder extends AbstractCameraActivity.IntentBuilder<AbstractCameraActivity.IntentBuilder> {

    public ImagePickerIntentBuilder(Context ctxt, Class clazz) {
        super(ctxt, clazz);
    }

    @Override
    Intent buildChooserBaseIntent() {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }
}
