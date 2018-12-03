package com.android.approjects.universalmusicplayer.utils;

public class LogHelper {

    private final static String TAG = "UniversalMusicPlayer";

    public static void d(String message) {

        android.util.Log.d(TAG, message);
    }

    public static void e(String message) {

        android.util.Log.e(TAG, message);
    }
}
