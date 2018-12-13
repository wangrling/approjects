package com.android.approjects.grafika.activity;

import com.android.approjects.grafika.GrafikaActivity;

/**
 * Movie player callback.
 * <p>
 * The goal here is to play back frames at the original rate.  This is done by introducing
 * a pause before the frame is submitted to the renderer.
 * <p>
 * This is not coordinated with VSYNC.  Since we can't control the display's refresh rate, and
 * the source material has time stamps that specify when each frame should be presented,
 * we will have to drop or repeat frames occasionally.
 * <p>
 * Thread restrictions are noted in the method descriptions.  The FrameCallback overrides should
 * only be called from the MoviePlayer.
 */

public class SpeedControlCallback implements MoviePlayer.FrameCallback {

    private static final String TAG = GrafikaActivity.TAG;
    private static final boolean CHECK_SLEEP_TIME = false;

    private static final long ONE_MILLION = 1000000L;

    private long mPrevPresentUsec;
    private long mPrevMonoUsec;
    private long mFixedFrameDurationUsec;
    private boolean mLoopReset;

    /**
     * Sets a fixed playback rate.  If set, this will ignore the presentation time stamp
     * in the video file.  Must be called before playback thread starts.
     */
    public void setFixedPlaybackRate(int fps) {
        mFixedFrameDurationUsec = ONE_MILLION / fps;
    }

    @Override
    public void preRender(long presentationTimeUsec) {

    }

    @Override
    public void postRender() {

    }

    @Override
    public void loopReset() {
        mLoopReset = true;
    }
}
