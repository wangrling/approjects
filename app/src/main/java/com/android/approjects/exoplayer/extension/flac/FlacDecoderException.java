package com.android.approjects.exoplayer.extension.flac;

import com.google.android.exoplayer2.audio.AudioDecoderException;

/**
 * Thrown when an Flac decoder error occurs.
 */

public final class FlacDecoderException extends AudioDecoderException {

    public FlacDecoderException(String message) {
        super(message);
    }

    FlacDecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
