package com.tcl.statisticsdk.util;

import android.content.Context;
import android.content.pm.PackageManager;


public class PermissionUtil {

    public static final int REQUEST_CODE_ASK_READ_PHONE_PERMISSION = 999;

    public static boolean hanPermission(Context context, String permission) {
        boolean hasPermission = context.checkCallingOrSelfPermission(permission) != PackageManager. PERMISSION_DENIED;
        LogUtils.D("permission " + permission + ":" + hasPermission);
        return hasPermission;
    }
}