package com.android.approjects.musicfx;

import android.content.Context;
import android.media.AudioManager;

/**
 * Convenience class to deal with audio focus. This class deals with everything related to audio
 * focus: it can request and abandon focus, and will intercept focus change events and deliver
 * them to a MusicFocusable interface (which, in our case, is implemented by {@link MusicService}).
 *
 * This class can only be used on SDK level 8 and above, since it uses API features that are not
 * available on previous SDK's.
 */
// 只是为做兼容才额外写的辅助函数。
public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
    AudioManager mAudioManager;
    MusicFocusable mFocusable;

    public AudioFocusHelper(Context context, MusicFocusable focusable) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mFocusable = focusable;
    }

    /** Requests audio focus. Returns whether request was successful or not. */
    public boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
    }

    /** Abandons audio focus. Returns whether request was successful or not. */
    public boolean abandonFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this);
    }

    /**
     * Called by AudioManager on audio focus changes. We implement this by calling our
     * MusicFocusable appropriately to relay the message.
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        if (mFocusable == null)
            return ;

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mFocusable.onGainedAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mFocusable.onLostAudioFocus(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mFocusable.onLostAudioFocus(true);
                break;
            default:
        }
    }
}
