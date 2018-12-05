package com.android.approjects.universalmusicplayer.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.android.approjects.R;
import com.android.approjects.universalmusicplayer.MusicService;
import com.android.approjects.universalmusicplayer.utils.LogHelper;
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

    //　控制播放
    private PlaybackControlsFragment mControlsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.d("BaseActivity onCreate");

        // Connect a media browser just to get the media session token. There are
        // other ways this can be done, for example by sharing the session token directly.
        // 是否启动MusicService?
        // 启动MusicService!
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LogHelper.d("BaseActivity onStart");

        mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);

        if (mControlsFragment == null) {
            throw new IllegalStateException("Missing fragment with id 'fragment_playback_controls'." +
                    " Cannot continue.");
        }

        hidePlaybackControls();

        LogHelper.d("BaseActivity connects to the media browse service.");
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        LogHelper.d("BaseActivity onStop");

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
        LogHelper.d("showPlaybackControls");
        if (NetworkHelper.isOnline(this)) {
            getSupportFragmentManager().beginTransaction()
                    .show(mControlsFragment)
                    .commit();
        }
    }

    protected void hidePlaybackControls() {
        LogHelper.d("BaseActivity hidePlaybackControls");
        getSupportFragmentManager().beginTransaction()
                .hide(mControlsFragment).commit();
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }

        // 播放状态就显示播放控制栏
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = getSupportMediaController();
        if (mediaController == null) {
            mediaController = new MediaControllerCompat(this, token);
            // 通过MediaBrowser获得MediaController
            setSupportMediaController(mediaController);
        }

        mediaController.registerCallback(mMediaControllerCallback);

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            LogHelper.d("BaseActivity connectionCallback.onConnected: " +
                    "hiding controls because metadata is null.");
            hidePlaybackControls();
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }

        onMediaControllerConnected();
    }

    // Callback that ensures that we are showing the controls.
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogHelper.d("mediaControllerCallback.onPlaybackStateChanged: " +
                                "hiding controls because state is " + state.getState());
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogHelper.d("mediaControllerCallback.onMetadataChanged: " +
                                "hiding controls because metadata is null");
                        hidePlaybackControls();
                    }
                }
            };

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.d("BaseActivity onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        LogHelper.e("BaseActivity could not connect media controller");
                        hidePlaybackControls();
                    }
                }
            };
}
