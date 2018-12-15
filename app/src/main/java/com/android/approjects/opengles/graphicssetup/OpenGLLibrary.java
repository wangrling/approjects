package com.android.approjects.opengles.graphicssetup;

public class OpenGLLibrary {

    static {
        System.loadLibrary("opengles");
    }

    public static native void init(int width, int height);

    public static native void step();
}
