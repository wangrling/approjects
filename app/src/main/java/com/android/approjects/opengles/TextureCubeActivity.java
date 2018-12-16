package com.android.approjects.opengles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.android.approjects.opengles.setup.ES2SetupActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.Nullable;

public class TextureCubeActivity extends ES2SetupActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        graphicsView = new TextureCubeView(getApplication());

        super.onCreate(savedInstanceState);
    }


    private class TextureCubeView extends com.android.approjects.opengles.setup.ES2SetupView {
        public TextureCubeView(Context context) {
            super(context);

            setRenderer(new GLSurfaceView.Renderer() {
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
    }

    public static native void init(int width, int height);

    public static native void step();
}
