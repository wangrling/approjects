package com.android.approjects.opengles;

import android.content.Context;
import android.os.Bundle;

import com.android.approjects.opengles.setup.ES3SetupActivity;
import com.android.approjects.opengles.setup.ES3SetupView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.Nullable;

public class ProjectedLightsActivity extends ES3SetupActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applicationContext = getApplicationContext();
        assetsDirectory = applicationContext.getFilesDir().getPath() + "/";

        extractAsset("mail.bmp");
        extractAsset("projected_lights_shader.vert");
        extractAsset("projected_lights_shader.frag");

        es3SetupView = new ProjectedLightsView(this);
        super.onCreate(savedInstanceState);
    }

    private class ProjectedLightsView extends ES3SetupView {
        public ProjectedLightsView(Context context) {
            super(context);

            setRenderer(new Renderer() {
                @Override
                public void onSurfaceCreated(GL10 gl, EGLConfig config) {

                }

                @Override
                public void onSurfaceChanged(GL10 gl, int width, int height) {
                    init(width, height);
                }

                @Override
                public void onDrawFrame(GL10 gl) {
                    step();
                }
            });
        }

        @Override
        public void uninit() {
            destroy();
        }
    }

    public native static void init(int width, int height);
    public native static void step();
    public native static void destroy();
}
