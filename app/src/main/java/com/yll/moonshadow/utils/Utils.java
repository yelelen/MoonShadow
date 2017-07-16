package com.yll.moonshadow.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

/**
 * Created by yelelen on 7/14/2017.
 */

public class Utils {
    public static boolean checkPermission(Activity activity, final String permissions, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activity.checkSelfPermission(permissions) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{permissions}, requestCode);
            return false;
        }
        return true;
    }

    public static String formatDuration(long duration){
        StringBuilder builder = new StringBuilder();
        int seconds = (int)(duration / 1000);
        int s = seconds % 60;
        int minutes = (seconds -s) / 60;
        int m = minutes % 60;
        int h = (minutes - m) / 60;

        if (h < 10) {
            builder.append("0");
        }
        builder.append(h).append(":");
        if (m < 10)
            builder.append("0");
        builder.append(m).append(":");
        if (s < 10)
            builder.append("0");
        builder.append(s);

        return builder.toString();
    }

    public static boolean isWebUri(Uri uri){
        if (uri != null) {
            String path = uri.toString().toLowerCase();
            if (path.startsWith("http") || path.startsWith("rtsp") || path.startsWith("mms")) {
                return true;
            }
        }

        return false;
    }

}
