package com.android.approjects.grafika;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.android.approjects.grafika.util.InputSurface;
import com.android.approjects.grafika.util.MediaUtils;
import com.android.approjects.grafika.util.OutputSurface;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * MediaCodec创建四部曲
 * (1) 定义MediaFormat;
 * (2) 根据MediaFormat创建MediaCodec;
 * (3) configure;
 * (4) start;
 *
 */

public class MediaCodecTest {
    private static final String TAG = "MediaCodecTest";
    private static final boolean VERBOSE = true;

    // parameters for the video encoder
    // H.264 Advanced Video Coding
    private static final String MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final int BIT_RATE = 2000000;            // 2Mbps
    private static final int FRAME_RATE = 15;               // 15fps
    private static final int IFRAME_INTERVAL = 10;          // 10 seconds between I-frames
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    // parameters for the audio encoder
    private static final String MIME_TYPE_AUDIO = MediaFormat.MIMETYPE_AUDIO_AAC;
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_AAC_PROFILE = 2; /* OMX_AUDIO_AACObjectLC */
    private static final int AUDIO_CHANNEL_COUNT = 2; // mono
    private static final int AUDIO_BIT_RATE = 128000;

    private static final int TIMEOUT_USEC = 100000;
    private static final int TIMEOUT_USEC_SHORT = 100;

    private boolean mVideoEncoderHadError = false;
    private boolean mAudioEncoderHadError = false;
    private volatile boolean mVideoEncodingOngoing = false;

    private static String videoFileName = "video_480x360_mp4_h264_1350kbps_30fps_aac_stereo_192kbps_44100hz";

    // The test should fail if the decoder never produces output frames for the input.
    // Time out decoding, as we have no way to query whether the decoder will produce output.
    private static final int DECODING_TIMEOUT_MS = 10000;

    @Test
    public void testException() throws Exception {
        boolean tested = false;
        // audio decoder (MP3 should be present on all Android devices)
        // 是否支持MP3播放
        // "audio/mpeg"
        MediaFormat format = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_MPEG, 44100 /* sampleRate */
                , 2 /* channelCount */);
        tested = verifyException(format, false /* isEncoder */) || tested;

        // audio encoder (AMR-WB may not be present on some Android devices)
        format = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AMR_WB, 16000 /* sampleRate */,
                1 /* channelCount */);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 19850);
        tested = verifyException(format, true /* isEncoder */) || tested;

        // video decoder (H.264/AVC may not be present on some Android devices)
        format = createMediaFormat();
        tested = verifyException(format, false /* isEncoder */) || tested;

        // video encoder (H.264/AVC may not be present on some Android devices)
        tested = verifyException(format, true /* isEncoder */) || tested;

        // signal test is skipped due to no device media codecs.
        if (!tested) {
            // 直接把失败的名字打印出来。
            // MediaCodecTest: SKIPPING testException(): cannot find any compatible device codecs
            MediaUtils.skipTest(TAG, "cannot find any compatible device codecs");
        }
    }

    private static void logMediaCodecException(MediaCodec.CodecException ex) {
        if (ex.isRecoverable()) {
            Log.w(TAG, "CodecException Recoverable: " + ex.getErrorCode());
        } else if (ex.isTransient()) {
            Log.w(TAG, "CodecException Transient: " + ex.getErrorCode());
        } else {
            Log.w(TAG, "CodecException Fatal: " + ex.getErrorCode());
        }
    }

    private static boolean verifyException(MediaFormat format, boolean isEncoder) throws IOException {
        String mimeType = format.getString(MediaFormat.KEY_MIME);
        if (!supportsCodec(mimeType, isEncoder)) {
            Log.i(TAG, "No " + (isEncoder ? "encoder" : "decoder") +
                    " found for mimeType = " + mimeType);
            return false;
        }

        final boolean isVideoEncoder = isEncoder && mimeType.startsWith("video/");

        // create codec (enter Initialized State)
        MediaCodec codec;

        // create improperly
        final String methodName = isEncoder ? "createEncoderByType" : "createDecoderByType";
        try {
            codec = createCodecByType(null, isEncoder);
            fail(methodName + " should return NullPointerException on null");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            codec = createCodecByType("foobarplan9", isEncoder); // invalid type
            fail(methodName + " should return IllegalArgumentException on invalid type");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            codec = MediaCodec.createByCodecName("foobarplan9"); // invalid name
            fail(methodName + " should return IllegalArgumentException on invalid name");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // correct
        codec = createCodecByType(format.getString(MediaFormat.KEY_MIME), isEncoder);

        // test a few commands
        try {
            codec.start();
            fail("start should return IllegalStateException when in Initialized state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            fail("start should not return MediaCodec.CodecException on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            codec.flush();
            fail("flush should return IllegalStateException when in Initialized state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            fail("flush should not return MediaCodec.CodecException on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        // obtaining the codec info now is fine.
        MediaCodecInfo codecInfo = codec.getCodecInfo();
        try {
            int bufIndex = codec.dequeueInputBuffer(0);
            fail("dequeueInputBuffer should return IllegalStateException"
                    + " when in the Initialized state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            fail("dequeueInputBuffer should not return MediaCodec.CodecException"
                    + " on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int bufIndex = codec.dequeueOutputBuffer(info, 0);
            fail("dequeueOutputBuffer should return IllegalStateException"
                    + " when in the Initialized state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            fail("dequeueOutputBuffer should not return MediaCodec.CodecException"
                    + " on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        // 执行MediaCodec相关函数之前需要进行配置
        // configure (enter Configure State).
        try {
            codec.configure(format, null /* surface */, null /* crypto */,
                    isEncoder ? 0 : MediaCodec.CONFIGURE_FLAG_ENCODE /* flags */);
            fail("configure needs MediaCodec.CONFIGURE_FLAG_ENCODE for encoders only");
        } catch (MediaCodec.CodecException e) {
            // expected
            logMediaCodecException(e);
        } catch (IllegalStateException e) {
            fail("configure should not return IllegalStateException when improperly configured");
        }

        // correct
        codec.configure(format, null /* surface */, null /* crypto */,
                isEncoder ? MediaCodec.CONFIGURE_FLAG_ENCODE : 0 /* flags */);

        // test a few commands
        try {
            codec.flush();
            fail("flush should return IllegalStateException when in Configured state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            fail("flush should not return MediaCodec.CodecException on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            Surface surface = codec.createInputSurface();
            if (!isEncoder) {
                fail("createInputSurface should not work on a decoder");
            }
        } catch (IllegalStateException |
                IllegalArgumentException e) { // expected for decoder and audio encoder
            if (isVideoEncoder) {
                throw e;
            }
        }

        // test getInputBuffers before start()
        try {
            ByteBuffer[] buffers = codec.getInputBuffers();
            fail("getInputBuffers called before start() should throw exception");
        } catch (IllegalStateException e) {
            // expected
        }

        // Configure完成

        // start codec (enter Executing state)
        codec.start();

        // test getInputBuffers after start()
        try {
            ByteBuffer[] buffers = codec.getInputBuffers();
            if (buffers == null) {
                fail("getInputBuffers called after start() should not return null");
            }
            if (isVideoEncoder && buffers.length > 0) {
                fail("getInputBuffers returned non-zero length array with input surface");
            }
        } catch (IllegalStateException e) {
            fail("getInputBuffers called after start() shouldn't throw exception");
        }

        // test a few commands
        try {
            codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
            fail("configure should return IllegalStateException when in Executing state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            // TODO: consider configuring after a flush.
            fail("configure should not return MediaCodec.CodecException on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        // two flushes should be fine.
        codec.flush();
        codec.flush();

        // stop codec (enter Initialized state)
        // two stops should be find.
        codec.stop();
        codec.stop();

        // release codec (enter Uninitialized state)
        // two releases should be fine.
        codec.release();
        codec.release();

        try {
            codecInfo = codec.getCodecInfo();
            fail("getCodecInfo should should return IllegalStateException" +
                    " when in Uninitialized state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            fail("getCodecInfo should not return MediaCodec.CodecException on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        try {
            codec.stop();
            fail("stop should return IllegalStateException when in Uninitialized state");
        } catch (MediaCodec.CodecException e) {
            logMediaCodecException(e);
            fail("stop should not return MediaCodec.CodecException on wrong state");
        } catch (IllegalStateException e) {
            // expected
        }

        return true;
    }

    /**
     * Tests:
     * <br> calling createInputSurface() before configure() throws exception.
     * <br> calling createInputSurface() after start() throws exception.
     * <br> calling createInputSurface() with a non-Surface color format is not required to throw
     * exception.
     */
    @Test
    public void testCreateInputSurfaceErrors() {
        if (!supportsCodec(MIME_TYPE, true)) {
            Log.i(TAG, "No encoder found for mimeType= " + MIME_TYPE);
            return;
        }

        MediaFormat format = createMediaFormat();
        MediaCodec encoder = null;
        Surface surface = null;

        // Replace color format with something that isn't COLOR_FormatSurface.
        MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);
        int colorFormat = findNonSurfaceColorFormat(codecInfo, MIME_TYPE);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);

        try {
            try {
                encoder = MediaCodec.createByCodecName(codecInfo.getName());
            } catch (IOException e) {
                fail("failed to create codec " + codecInfo.getName());
            }
            try {
                surface = encoder.createInputSurface();
                fail("createInputSurface should not work pre-configure");
            } catch (IllegalStateException ise) {
                // good
            }
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();
            try {
                surface = encoder.createInputSurface();
                fail("createInputSurface should not work post-start");
            } catch (IllegalStateException ise) {
                // good
            }
        } finally {
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }
        }
        assertNull(surface);
    }

    /**
     * Tests:
     * <br> signaling end-of-stream before any data is sent works
     * <br> signaling EOS twice throws exception
     * <br> submitting a frame after EOS throws exception [TODO]
     */
    @Test
    public void testSignalSurfaceEOS() {
        if (!supportsCodec(MIME_TYPE, true)) {
            Log.i(TAG, "No encoder found for mimeType= " + MIME_TYPE);
            return;
        }

        MediaFormat format = createMediaFormat();
        MediaCodec encoder = null;
        InputSurface inputSurface = null;

        try {
            try {
                encoder = MediaCodec.createEncoderByType(MIME_TYPE);
            } catch (IOException e) {
                fail("failed to create " + MIME_TYPE + " encoder");
            }
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = new InputSurface(encoder.createInputSurface());
            inputSurface.makeCurrent();
            encoder.start();

            // send an immediate EOS
            encoder.signalEndOfInputStream();

            try {
                encoder.signalEndOfInputStream();
                fail("should not be able to signal EOS twice");
            } catch (IllegalStateException ise) {
                // good
            }
            // submit a frame post-EOS
            GLES20.glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            try {
                inputSurface.swapBuffers();
                if (false) {    // TODO
                    fail("should not be able to submit frame after EOS");
                }
            } catch (Exception ex) {
                // good
            }
        } finally {
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }
            if (inputSurface != null) {
                inputSurface.release();
            }
        }
    }

    /**
     * Tests:
     * <br> stopping with buffers in flight doesn't crash or hang.
     */
    public void testAbruptStop() {
        if (!supportsCodec(MIME_TYPE, true)) {
            Log.i(TAG, "No encoder found for mimeType= " + MIME_TYPE);
            return;
        }

        // There appears to be a race, so run it several times with a short delay between runs
        // to allow any previous activity to shut down.
        for (int i = 0; i < 50; i++) {
            Log.d(TAG, "testAbruptStop " + i);
            doTestAbruptStop();
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        }
    }

    private void doTestAbruptStop() {
        MediaFormat format = createMediaFormat();
        MediaCodec encoder = null;
        InputSurface inputSurface = null;

        try {
            try {
                encoder = MediaCodec.createEncoderByType(MIME_TYPE);
            } catch (IOException e) {
                fail("failed to create " + MIME_TYPE + " encoder");
            }
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = new InputSurface(encoder.createInputSurface());
            inputSurface.makeCurrent();
            encoder.start();

            int totalBuffers = encoder.getOutputBuffers().length;
            if (VERBOSE) Log.d(TAG, "Total buffers: " + totalBuffers);

            // Submit several frames quickly, without draining the encoder output, to try to
            // ensure that we've got some queued up when we call stop().  If we do too many
            // we'll block in swapBuffers().
            for (int i = 0; i < totalBuffers; i++) {
                GLES20.glClearColor(0.0f, (i % 8) / 8.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                inputSurface.swapBuffers();
            }
            Log.d(TAG, "stopping");
            encoder.stop();
            Log.d(TAG, "stopped");
        } finally {
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }
            if (inputSurface != null) {
                inputSurface.release();
            }
        }
    }

    public void testReleaseAfterFlush() throws IOException, InterruptedException {
        String mimes[] = new String[] {
                MIME_TYPE, MIME_TYPE_AUDIO};

        for (String mime : mimes) {
            if (!MediaUtils.checkEncoder(mime)) {
                continue;
            }
            testReleaseAfterFlush(mime);
        }
    }

    private void testReleaseAfterFlush(String mime) throws IOException, InterruptedException {
        CountDownLatch buffersExhausted = null;
        CountDownLatch codecFlushed = null;
        AtomicInteger numBuffers = null;

        // sync flush from same thread
        MediaCodec encoder = MediaCodec.createEncoderByType(mime);
        runReleaseAfterFlush(mime, encoder, buffersExhausted, codecFlushed, numBuffers);
    }

    private static void runReleaseAfterFlush(
            String mime,
            MediaCodec encoder,
            CountDownLatch buffersExhausted,
            CountDownLatch codecFlushed,
            AtomicInteger numBuffers) {
        InputSurface inputSurface = null;

            inputSurface = initCodecAndSurface(mime, encoder);

    }

    private static InputSurface initCodecAndSurface(String mime, MediaCodec encoder) {
        MediaFormat format;
        InputSurface inputSurface = null;
        if (mime.startsWith("audio/")) {
            format = MediaFormat.createAudioFormat(mime, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_COUNT);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, AUDIO_AAC_PROFILE);
            format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } else if (MIME_TYPE.equals(mime)) {
            CodecInfo info = getAvcSupportedFormatInfo();
            format = MediaFormat.createVideoFormat(mime, info.mMaxW, info.mMaxH);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, info.mBitRate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, info.mFps);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            OutputSurface outputSurface = new OutputSurface(1, 1);
            encoder.configure(format, outputSurface.getSurface(), null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = new InputSurface(encoder.createInputSurface());
            inputSurface.makeCurrent();
        } else {
            throw new IllegalArgumentException("unsupported mime type: " + mime);
        }
        encoder.start();
        return inputSurface;
    }

    private static CodecInfo getAvcSupportedFormatInfo() {
        MediaCodecInfo mediaCodecInfo = selectCodec(MIME_TYPE);
        MediaCodecInfo.CodecCapabilities cap = mediaCodecInfo.getCapabilitiesForType(MIME_TYPE);
        if (cap == null) { // not supported
            return null;
        }
        CodecInfo info = new CodecInfo();
        int highestLevel = 0;
        for (MediaCodecInfo.CodecProfileLevel lvl : cap.profileLevels) {
            if (lvl.level > highestLevel) {
                highestLevel = lvl.level;
            }
        }
        int maxW = 0;
        int maxH = 0;
        int bitRate = 0;
        int fps = 0; // frame rate for the max resolution
        switch(highestLevel) {
            // Do not support Level 1 to 2.
            case MediaCodecInfo.CodecProfileLevel.AVCLevel1:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel11:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel12:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel13:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel1b:
            case MediaCodecInfo.CodecProfileLevel.AVCLevel2:
                return null;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel21:
                maxW = 352;
                maxH = 576;
                bitRate = 4000000;
                fps = 25;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel22:
                maxW = 720;
                maxH = 480;
                bitRate = 4000000;
                fps = 15;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel3:
                maxW = 720;
                maxH = 480;
                bitRate = 10000000;
                fps = 30;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel31:
                maxW = 1280;
                maxH = 720;
                bitRate = 14000000;
                fps = 30;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel32:
                maxW = 1280;
                maxH = 720;
                bitRate = 20000000;
                fps = 60;
                break;
            case MediaCodecInfo.CodecProfileLevel.AVCLevel4: // only try up to 1080p
            default:
                maxW = 1920;
                maxH = 1080;
                bitRate = 20000000;
                fps = 30;
                break;
        }
        info.mMaxW = maxW;
        info.mMaxH = maxH;
        info.mFps = fps;
        info.mBitRate = bitRate;
        Log.i(TAG, "AVC Level 0x" + Integer.toHexString(highestLevel) + " bit rate " + bitRate +
                " fps " + info.mFps + " w " + maxW + " h " + maxH);

        return info;
    }


    // wrap MediaCodec encoder and decoder creation
    private static MediaCodec createCodecByType(String type, boolean isEncoder) throws IOException {
        if (isEncoder) {
            return MediaCodec.createEncoderByType(type);
        }
        return MediaCodec.createDecoderByType(type);
    }



    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no
     * match was found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        // FIXME: select codecs based on the complete use-case, not just the mime
        MediaCodecList mcl = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        for (MediaCodecInfo info : mcl.getCodecInfos()) {
            if (!info.isEncoder()) {
                continue;
            }

            String[] types = info.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Creates a MediaFormat with the basic set of values.
     */
    private static MediaFormat createMediaFormat() {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, WIDTH, HEIGHT);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        return format;
    }

    private static class CodecInfo {
        public int mMaxW;
        public int mMaxH;
        public int mFps;
        public int mBitRate;
    };

    // 获取所有的MimeType类型
    private static boolean supportsCodec(String mimeType, boolean encoder) {
        MediaCodecList list = new MediaCodecList(MediaCodecList.ALL_CODECS);
        for (MediaCodecInfo info : list.getCodecInfos()) {
            if (encoder && !info.isEncoder()) {
                continue;
            }
            if (!encoder && info.isEncoder()) {
                continue;
            }

            for (String type : info.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a color format that is supported by the codec and isn't COLOR_FormatSurface.  Throws
     * an exception if none found.
     */
    private  static int findNonSurfaceColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (colorFormat != MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface) {
                return colorFormat;
            }
        }
        fail("couldn't find a good color format for " + codecInfo.getName() + " / " + MIME_TYPE);
        return 0;   // not reached
    }
}
