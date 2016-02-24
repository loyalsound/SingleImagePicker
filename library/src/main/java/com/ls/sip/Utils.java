package com.ls.sip;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Thanh
 * @since 2/26/16
 */
public class Utils {

    public static Uri getPhotoPath() {
        return Uri.fromFile(new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator
                        + "Pictures"
                        + File.separator
                        + "PHOTO_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date())
                        + ".jpg"));
    }

    public static Uri getCachePhotoPath(Context context) throws IOException {
        File[] cacheDirs = ContextCompat.getExternalCacheDirs(context);
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return Uri.fromFile(File.createTempFile("cache", ".cache", cacheDir));
    }

}
