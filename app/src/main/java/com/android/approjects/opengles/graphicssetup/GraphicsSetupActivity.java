package com.android.approjects.opengles.graphicssetup;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

public class GraphicsSetupActivity extends Activity {

    static {
        System.loadLibrary("opengles");
    }

    public static String LOGTAG = "OpenGL";

    protected GraphicsView graphicsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOGTAG, "Creating New Tutorial View");
        // graphicsView = new GraphicsView(getApplication());
        setContentView(graphicsView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        graphicsView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        graphicsView.onResume();
    }
}
