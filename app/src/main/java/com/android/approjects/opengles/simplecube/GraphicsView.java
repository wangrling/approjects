package com.android.approjects.opengles.simplecube;

import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GraphicsView extends com.android.approjects.opengles.graphicssetup.GraphicsView {



    public GraphicsView(Context context) {
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

    public static native void init(int width, int height);

    public static native void step();
}
