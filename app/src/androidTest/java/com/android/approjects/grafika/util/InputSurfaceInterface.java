package com.android.approjects.grafika.util;

import android.media.MediaCodec;

/**
 * This interface exposes the minimum set of {@link InputSurface} APIs used in {@link EncodeDecodeTest}.
 */
interface InputSurfaceInterface {

    void makeCurrent();

    boolean swapBuffers();

    void setPresentationTime(long nescs);

    void configure(MediaCodec codec);

    void configure(NdkMediaCodec codec);

    void updateSize(int width, int height);

    void release();
}
