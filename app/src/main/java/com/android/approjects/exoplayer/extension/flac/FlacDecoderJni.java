package com.android.approjects.exoplayer.extension.flac;

import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.util.FlacStreamInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * JNI wrapper for the libflac Flac decoder.
 */

final class FlacDecoderJni {

    public long getDecodePosition() {
        return 0;
    }

    public void decodeSampleWithBackTrackPosition(ByteBuffer outputByteBuffer, long lastDecodePosition)
            throws InterruptedException, IOException, FlacFrameDecodeException {

    }

    public long getLastFrameTimeStamp() {
        return 0;
    }

    public void reset(long position) {
    }

    public void release() {

    }

    public long getSeekPosition(long timeUs) {
        return 0;
    }

    public long getDecoderPosition() {
        return 0;
    }

    public FlacStreamInfo decodeMetadata() {
        return null;
    }

    /**
     * Exception to be thrown if {@link #decodeSample(ByteBuffer)} fails
     * to decode a frame.
     */
    public static final class FlacFrameDecodeException extends Exception {

        public final int errorCode;

        public FlacFrameDecodeException(String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }
    }

    // The same buffer size which libflac has
    private static final int TEMP_BUFFER_SIZE = 8192;

    private final long nativeDecoderContext;

    private ByteBuffer byteBufferData;
    private ExtractorInput extractorInput;
    private boolean endOfExtractorInput;
    private byte[] tempBuffer;

    public FlacDecoderJni() throws FlacDecoderException {
        if (!FlacLibrary.isAvailable()) {
            throw new FlacDecoderException("Failed to load decoder native libraries.");
        }

        nativeDecoderContext = flacInit();
        if (nativeDecoderContext == 0) {
            throw new FlacDecoderException("Failed to initialize decoder");
        }
    }

    /**
     * Set data to be parsed by libflac.
     * @param byteBufferData Source {@link java.nio.ByteBuffer}
     */
    public void setData(ByteBuffer byteBufferData) {
        this.byteBufferData = byteBufferData;
        this.extractorInput = null;
        this.tempBuffer = null;
    }

    /**
     * Sets data to be parsed by libflac.
     * @param extractorInput Source {@link ExtractorInput}
     */
    public void setData(ExtractorInput extractorInput) {
        this.byteBufferData = null;
        this.extractorInput = extractorInput;
        if (tempBuffer == null) {
            this.tempBuffer = new byte[TEMP_BUFFER_SIZE];
        }

        endOfExtractorInput = false;
    }

    public boolean isEndOfData() {
        if (byteBufferData != null) {
            return byteBufferData.remaining() == 0;
        } else if (extractorInput != null) {
            return endOfExtractorInput;
        }
        return true;
    }

    private native long flacInit();

}
