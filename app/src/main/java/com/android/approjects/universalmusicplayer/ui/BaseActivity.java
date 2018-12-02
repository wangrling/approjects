package com.android.approjects.universalmusicplayer.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

import com.android.approjects.R;
import com.android.approjects.universalmusicplayer.MusicService;
import com.android.approjects.universalmusicplayer.utils.NetworkHelper;

import androidx.annotation.Nullable;

/**
 * Base activity for activities that need to show a playback control fragment
 * when media is playing.
 */

public class BaseActivity extends ActionBarCastActivity
        implements MediaBrowserProvider {

    // Browses media content offered by a {@link MediaBrowserServiceCompat}.
    private MediaBrowserCompat mMediaBrowser;

    private PlaybackControlsFragment mControlsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "BaseActivity onCreate");

        // Connect a media browser just to get the media session token. There are
        // other ways this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "BaseActivity onStart");

        mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);

        if (mControlsFragment == null) {
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }

        hidePlaybackControls();

        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "BaseActivity onStop");

        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(this);

        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCallback);
        }

        mMediaBrowser.disconnect();
    }


    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    protected void onMediaControllerConnected() {
        // empty implementation, can be overridden by clients.
    }

    protected void showPlaybackControls() {
        Log.d(TAG, "showPlaybackControls");
        if (NetworkHelper.isOnline(this)) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                    .show(mControlsFragment)
                    .commit();
        }
    }

    protected void hidePlaybackControls() {
        Log.d(TAG, "hidePlaybackControls");
        getSupportFragmentManager().beginTransaction()
                .hide(mControlsFragment).commit();
    }
}
