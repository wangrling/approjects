package com.android.approjects.grafika;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.android.approjects.grafika.gles.EglCore;

import java.io.File;
import java.io.IOException;

/**
 * Base class for generated movies.
 */

public abstract class GeneratedMovie implements Content {
    private static final String TAG = GrafikaActivity.TAG;
    private static final boolean VERBOSE = false;

    private static final int IFRAME_INTERNAL = 5;

    // set by sub-class to indicate that the movie has been generated
    // TODO: remove this now?
    protected boolean mMovieReady = false;

    // "live" state during recording.
    private MediaCodec.BufferInfo mBufferInfo;
    private MediaCodec mEncoder;
    private MediaMuxer mMuxer;
    private EglCore mEglCore;
    private WindowSurface mInputSurface;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    /**
     * Creates the movie content. Usually called from a async task thread.
     */
    public abstract void create(File outputFile, ContentManager.ProgressUpdater prog);

    /**
     * Returns true if the codec has a software implementation.
     */
    private static boolean isSoftwareCodec(MediaCodec codec) {
        String codecName = codec.getCodecInfo().getName();

        return ("OMX.google.h264.encoder".equals(codecName));
    }

    /**
     * Prepares the video encoder, muxer, and an EGL input surface.
     */
    protected void prepareEncoder(String mimeType, int width, int height, int bitRate,
                                  int framesPerSecond, File outputFile) throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);

        // Set some properties. Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, framesPerSecond);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERNAL);
        if (VERBOSE)
            Log.d(TAG, "format: " + format);

        // Create a MediaCodec encoder, and configure it with our format. Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        mEncoder = MediaCodec.createEncoderByType(mimeType);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Log.v(TAG, "encoder is " + mEncoder.getCodecInfo().getName());

        Surface surface;
        try {
            surface = mEncoder.createInputSurface();
        } catch (IllegalStateException ise) {
            // This is generally the first time we ever try to encode something through a
            // Surface, so specialize the message a bit if we can guess at why it's failing.
            // TODO: failure message should come out of strings.xml for i18n
            if (isSoftwareCodec(mEncoder)) {
                throw new RuntimeException("Can't use input surface with software codec: " +
                        mEncoder.getCodecInfo().getName(),
                        ise);
            } else {
                throw new RuntimeException("Failed to create input surface", ise);
            }
        }
    }
}
