package com.android.approjects.opengles.firstnative;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

public class FirstNativeActivity extends Activity {

    private static String LOGTAG = "FirstNative";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOGTAG, "On Create Method Calling Native Library");

        FirstNativeLibrary.init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
