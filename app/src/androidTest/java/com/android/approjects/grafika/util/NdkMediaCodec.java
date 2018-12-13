package com.android.approjects.grafika.util;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;

import java.nio.ByteBuffer;

public class NdkMediaCodec implements MediaCodecWrapper {



    @Override
    public void release() {

    }

    @Override
    public void configure(MediaFormat format, int flags) {

    }

    @Override
    public void setInputSurface(InputSurfaceInterface inputSurface) {

    }

    @Override
    public InputSurfaceInterface createInputSurface() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public int dequeueOutputBuffer(MediaCodec.BufferInfo info, long timeoutUs) {
        return 0;
    }

    @Override
    public void releaseOutputBuffer(int index, boolean render) {

    }

    @Override
    public void signalEndOfInputStream() {

    }

    @Override
    public String getOutputFormatString() {
        return null;
    }

    @Override
    public ByteBuffer getOutputBuffer(int index) {
        return null;
    }

    @Override
    public ByteBuffer[] getOutputBuffers() {
        return new ByteBuffer[0];
    }

    @Override
    public ByteBuffer getInputBuffer(int index) {
        return null;
    }

    @Override
    public ByteBuffer[] getInputBuffers() {
        return new ByteBuffer[0];
    }

    @Override
    public void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) {

    }

    @Override
    public int dequeueInputBuffer(long timeoutUs) {
        return 0;
    }

    @Override
    public void setParameters(Bundle params) {

    }

    @Override
    public void setCallback(MediaCodec.Callback mCallback) {

    }
}
