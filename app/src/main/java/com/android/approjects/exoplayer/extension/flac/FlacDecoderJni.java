package com.android.approjects.exoplayer.extension.flac;

import com.google.android.exoplayer2.util.FlacStreamInfo;

/**
 * JNI wrapper for the libflac Flac decoder.
 */

final class FlacDecoderJni {

    private final long nativeDecoderContext;

    public FlacDecoderJni() throws FlacDecoderException {
        if (!FlacLibrary.isAvailable()) {
            throw new FlacDecoderException("Failed to load decoder native libraries.");
        }

        nativeDecoderContext = flacInit();
        if (nativeDecoderContext == 0) {
            throw new FlacDecoderException("Failed to initialize decoder");
        }
    }

    private native long flacInit();

    
}
