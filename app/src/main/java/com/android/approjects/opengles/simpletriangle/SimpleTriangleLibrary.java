package com.android.approjects.opengles.simpletriangle;

public class SimpleTriangleLibrary {
    static {
        System.loadLibrary("opengles");
    }

    public static native void init(int width, int height);
    public static native void step();
}
