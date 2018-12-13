package com.android.approjects.grafika.activity;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;

import com.android.approjects.grafika.GrafikaActivity;

import java.io.File;
import java.io.IOException;

/**
 * Plays the video track from a movie file to a Surface.
 * <p>
 * TODO: needs more advanced shuttle controls (pause/resume, skip)
 */

public class MoviePlayer {

    private static final String TAG = GrafikaActivity.TAG;
    private static final boolean VERBOSE = false;

    // Declare this here to reduce allocations.
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    // May be set/read by different threads.
    private volatile boolean mIsStopRequested;

    private File mSourceFile;
    private Surface mOutputSurface;
    FrameCallback mFrameCallback;
    private boolean mLoop;
    private int mVideoWidth;
    private int mVideoHeight;

    /**
     * Interface to be implemented by class that manages playback UI.
     * <p>
     * Callback methods will be invoked on the UI thread.
     */
    public interface PlayerFeedback {
        void playbackStopped();
    }

    /**
     * Callback invoked when rendering video frames.  The MoviePlayer client must
     * provide one of these.
     */
    public interface FrameCallback {
        /**
         * Called immediately before the frame is rendered.
         * @param presentationTimeUsec The desired presentation time, in microseconds.
         */
        void preRender(long presentationTimeUsec);

        /**
         * Called immediately after the frame render call returns.  The frame may not have
         * actually been rendered yet.
         * TODO: is this actually useful?
         */
        void postRender();

        /**
         * Called after the last frame of a looped movie has been rendered.  This allows the
         * callback to adjust its expectations of the next presentation time stamp.
         */
        void loopReset();
    }

    public MoviePlayer(File sourceFile, Surface outputSurface, FrameCallback frameCallback)
            throws IOException {

    }

    /**
     * Returns the width, in pixels, of the video.
     */
    public int getVideoWidth() {
        return mVideoWidth;
    }

    /**
     * Returns the height, in pixels, of the video.
     */
    public int getVideoHeight() {
        return mVideoHeight;
    }

    /**
     * Sets the loop mode.  If true, playback will loop forever.
     */
    public void setLoopMode(boolean loopMode) {
        mLoop = loopMode;
    }

    /**
     * Asks the player to stop.  Returns without waiting for playback to halt.
     * <p>
     * Called from arbitrary thread.
     */
    public void requestStop() {
        mIsStopRequested = true;
    }


    /**
     * Thread helper for video playback.
     * <p>
     * The PlayerFeedback callbacks will execute on the thread that creates the object,
     * assuming that thread has a looper.  Otherwise, they will execute on the main looper.
     */
    public static class PlayTask implements Runnable {
        private static final int MSG_PLAY_STOPPED = 0;

        private MoviePlayer mPlayer;
        private PlayerFeedback mFeedback;

        private LocalHandler mLocalHandler;

        public PlayTask(MoviePlayer player, PlayerFeedback feedback) {
            mPlayer = player;
            mFeedback = feedback;

            mLocalHandler = new LocalHandler();
        }

        @Override
        public void run() {

        }

        public void setLoopMode(boolean b) {

        }

        public void execute() {

        }

        public void requestStop() {

        }

        public void waitForStop() {

        }

        private static class LocalHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;

                switch (what) {
                    case MSG_PLAY_STOPPED:
                        PlayerFeedback fb = (PlayerFeedback) msg.obj;
                        fb.playbackStopped();
                        break;
                    default:
                        throw new RuntimeException("Unknown msg " + what);
                }
            }
        }
    }
}
