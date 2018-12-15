package com.android.approjects.opengles.simplecube;

import android.os.Bundle;

import com.android.approjects.opengles.graphicssetup.GraphicsSetupActivity;

import androidx.annotation.Nullable;

public class SimpleCubeActivity extends GraphicsSetupActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        graphicsView = new GraphicsView(getApplication());
        super.onCreate(savedInstanceState);
    }
}
