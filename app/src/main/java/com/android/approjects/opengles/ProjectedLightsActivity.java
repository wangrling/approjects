package com.android.approjects.opengles;

import android.content.Context;
import android.os.Bundle;

import com.android.approjects.opengles.graphicssetup.GraphicsSetupActivity;
import com.android.approjects.opengles.graphicssetup.GraphicsView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.Nullable;

public class ProjectedLightsActivity extends GraphicsSetupActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        graphicsView = new ProjectedLightsView(this);

        super.onCreate(savedInstanceState);
    }

    private class ProjectedLightsView extends GraphicsView {
        public ProjectedLightsView(Context context) {
            super(context);

            setRenderer(new Renderer() {
                @Override
                public void onSurfaceCreated(GL10 gl, EGLConfig config) {

                }

                @Override
                public void onSurfaceChanged(GL10 gl, int width, int height) {

                }

                @Override
                public void onDrawFrame(GL10 gl) {

                }
            });
        }
    }
}
