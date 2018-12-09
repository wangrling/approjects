package com.android.approjects.cameraraw;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class CameraRawActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, CameraRawFragment.newInstance())
                    .commit();
        }
    }
}
