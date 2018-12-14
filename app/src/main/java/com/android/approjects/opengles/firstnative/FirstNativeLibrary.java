package com.android.approjects.opengles.firstnative;

public class FirstNativeLibrary {

    static {
        System.loadLibrary("opengles");
    }

    public static native void init();
}
