package com.gradientinsight.gallery01.util;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;

import com.gradientinsight.gallery01.R;

import java.io.File;

public class Util {

    public static int dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static String getBucketPhotosCount(Context c, String album_name) {
        int counter = 0;
        Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = c.getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + album_name + "\"", null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String file = cursor.getString(0);
                if (new File(file).exists())
                    counter = counter + 1;
            }
            cursor.close();
        }
        return counter + c.getString(R.string.media);
    }

    public static String getTotalPhotosCount(Context context) {
        int counter = 0;
        Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uriExternal,
                projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String file = cursor.getString(0);
                if (new File(file).exists())
                    counter = counter + 1;
            }
            cursor.close();
        }
        return counter + context.getString(R.string.media);
    }

    public static boolean validFileSize(File file) {
        long fileSizeInBytes = file.length();
        return (int) fileSizeInBytes / 1024 > 1;
    }
}
