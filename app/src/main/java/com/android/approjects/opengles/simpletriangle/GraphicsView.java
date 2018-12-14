package com.android.approjects.opengles.simpletriangle;

import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

public class GraphicsView extends GLSurfaceView {

    protected int redSize = 8;
    protected int greenSize = 8;
    protected int blueSize = 8 ;
    protected int alphaSize = 8;
    protected int depthSize = 16;
    protected int sampleSize = 4;
    protected int stencilSize = 0;
    protected int[] value = new int [1];

    public GraphicsView(Context context) {
        super(context);

        setEGLContextFactory(new EGLContextFactory() {
            @Override
            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {

                final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
                int[] attrib_list = {
                        EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
                };
                EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);

                return context;
            }

            @Override
            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                egl.eglDestroyContext(display, context);
            }
        });

        setEGLConfigChooser(new EGLConfigChooser() {
            @Override
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
                final int EGL_OPENGL_ES2_BIT = 4;
                int[] configAttributes = {
                        EGL10.EGL_RED_SIZE, redSize,
                        EGL10.EGL_GREEN_SIZE, greenSize,
                        EGL10.EGL_BLUE_SIZE, blueSize,
                        EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_SAMPLES, sampleSize,
                        EGL10.EGL_DEPTH_SIZE, depthSize,
                        EGL10.EGL_STENCIL_SIZE, stencilSize,
                        EGL10.EGL_NONE
                };

                int[] numConfigs = new int[1];
                egl.eglChooseConfig(display, configAttributes, null, 0, numConfigs);
                int numConfig = numConfigs[0];

                EGLConfig[] configs = new EGLConfig[numConfig];
                egl.eglChooseConfig(display, configAttributes, configs, numConfig, numConfigs);

                return selectConfig(egl, display, configs);
            }

            public EGLConfig selectConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs)
            {
                for(EGLConfig config : configs)
                {
                    int d = getConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                    int s = getConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                    int r = getConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE,0);
                    int g = getConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                    int b = getConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                    int a = getConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

                    if (r == redSize && g == greenSize && b == blueSize && a == alphaSize && d >= depthSize && s >= stencilSize)
                        return config;
                }

                return null;
            }

            private int getConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config,
                                        int attribute, int defaultValue)
            {
                if (egl.eglGetConfigAttrib(display, config, attribute, value))
                    return value[0];

                return defaultValue;
            }

        });

        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                SimpleTriangleLibrary.init(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                SimpleTriangleLibrary.step();
            }
        });
    }
}
