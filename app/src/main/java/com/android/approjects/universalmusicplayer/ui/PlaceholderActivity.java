package com.android.approjects.universalmusicplayer.ui;

import android.os.Bundle;

import com.android.approjects.R;

import androidx.annotation.Nullable;

/**
 * Placeholder activity for features that are not implemented in this sample, but
 * are in the navigation drawer.
 */

public class PlaceholderActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_placeholder);

        initializeToolbar();
    }
}
