package com.android.approjects.universalmusicplayer;

import android.content.Context;

/**
 * 允许其它的程序访问，比如Auto, Wear等。
 */
public class PackageValidator {

    // always return true.
    public static boolean isCallerAllowed(Context context, String callingPackage,
                                          int callingUid) {
        return true;
    }
}
