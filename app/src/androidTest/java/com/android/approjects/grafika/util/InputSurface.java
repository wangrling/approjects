package com.android.approjects.grafika.util;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

/**
 * EGLDisplay
 * EGLContext
 * EGLSurface
 */

public class InputSurface implements InputSurfaceInterface {

    private static final String TAG = "InputSurface";

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private EGLConfig[] mConfigs = new EGLConfig[1];

    private Surface mSurface;
    private int mWidth;
    private int mHeight;

    /**
     * Creates an InputSurface from a Surface.
     */
    public InputSurface(Surface surface) {
        if (surface == null) {
            throw new NullPointerException();
        }
        mSurface = surface;

        eglSetup();
    }

    /**
     * Prepares EGL. We want a GLES 2.0 context and a surface that supports recording.
     */
    private void eglSetup() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }

        // Configure EGL for recordable and OpenGL ES 2.0.  We want enough RGB bits
        // to minimize artifacts from possible YUV conversion.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGLExt.EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };

        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, mConfigs, 0, mConfigs.length,
                numConfigs, 0)) {
            throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
        }

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mConfigs[0], EGL14.EGL_NO_CONTEXT,
                attrib_list, 0);
        checkEglError("eglCreateContext");
        if (mEGLContext == null) {
            throw new RuntimeException("null context");
        }

        // Create a window surface, and attach it to the Surface we received.
        createEGLSurface();

        mWidth = getWidth();
        mHeight = getHeight();

    }

    private void createEGLSurface() {
        //EGLConfig[] configs = new EGLConfig[1];
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mConfigs[0], mSurface,
                surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");
        if (mEGLSurface == null) {
            throw new RuntimeException("surface was null");
        }
    }

    /**
     * Checks for EGL errors.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    @Override
    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    @Override
    public boolean swapBuffers() {
        return EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    @Override
    public void setPresentationTime(long nescs) {

    }

    @Override
    public void configure(MediaCodec codec) {

    }

    @Override
    public void configure(NdkMediaCodec codec) {

    }

    @Override
    public void updateSize(int width, int height) {

    }

    @Override
    public void release() {

    }

    /**
     * Queries the surface's width.
     */
    public int getWidth() {
        int[] value = new int[1];
        EGL14.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL14.EGL_WIDTH, value, 0);
        return value[0];
    }

    /**
     * Queries the surface's height.
     */
    public int getHeight() {
        int[] value = new int[1];
        EGL14.eglQuerySurface(mEGLDisplay, mEGLSurface, EGL14.EGL_HEIGHT, value, 0);
        return value[0];
    }

}
