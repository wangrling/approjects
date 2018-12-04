package com.android.approjects.universalmusicplayer;

import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.android.approjects.universalmusicplayer.model.MusicProvider;
import com.android.approjects.universalmusicplayer.playback.PlaybackManager;
import com.android.approjects.universalmusicplayer.utils.LogHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.android.approjects.universalmusicplayer.utils.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static com.android.approjects.universalmusicplayer.utils.MediaIDHelper.MEDIA_ID_ROOT;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with you media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which give a seamless playback
 * experience to the user.
 *
 * To implement a MediaBrowserService, you need to:
 *
 * <ul>
 *     <li> Extend {@link MediaBrowserService}, implementing the media browsing
 *          related methods {@link MediaBrowserService#onGetRoot} and
 *          {@link MediaBrowserService#onLoadChildren};</li>
 *
 *     <li> In onCreate, start a new {@link MediaSession} and notify its parent
 *          with the session's token {@link MediaBrowserService#setSessionToken};</li>
 *
 *     <li> Set a callback on the
 *          {@link MediaSession#setCallback(MediaSession.Callback)}.
 *          The callback will receive all the user's actions, like play, pause, etc;</li>
 *
 *     <li> Handle all the actual music playing using any method your app prefers (for example,
 *        {@link MediaPlayer})</li>
 *
 *     <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 *          {@link MediaSession#setPlaybackState(PlaybackState)}
 *          {@link MediaSession#setMetadata(MediaMetadata)} and
 *          {@link MediaSession#setQueue(java.util.List)})</li>
 *
 *     <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 *          MediaBrowserService</li>
 * </ul>
 */


public class MusicService extends MediaBrowserServiceCompat implements
        PlaybackManager.PlaybackServiceCallback {

    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.android.approjects.ump.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    private MusicProvider mMusicProvider;
    private PlaybackManager mPlaybackManager;

    private MediaSessionCompat mSession;

    private MediaNotificationManager mMediaNotificationManager;
    private Bundle mSessionExtras;

    private final DelayedStopHandler mDelayedStopHandler =
            new DelayedStopHandler(this);

    // 没有实现
    private PackageValidator mPackageValidator;

    // 什么时候会创建MusicService?
    @Override
    public void onCreate() {
        super.onCreate();

        LogHelper.d("MusicService onCreate");

        mMusicProvider = new MusicProvider();

        // To make the app more responsive, fetch and cache catalog information now.
        // This can help improve the response time in the method
        // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
        mMusicProvider.retrieveMediaAsync(null /* Callback */);


    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        LogHelper.d("onGetRoot: clientPackageName = " + clientPackageName +
                "; clientUid = " + clientUid + "; rootHints = " + rootHints);

        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        // 假的PackageValidator实现
        if (PackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            return new BrowserRoot(MEDIA_ID_ROOT, null);
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }

    }

    // 加载音乐播放列表
    // 通过subscribe函数获取音乐列表。
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        LogHelper.d("OnLoadChildren: parentMediaId = " + parentId);
        if (MEDIA_ID_EMPTY_ROOT.equals(parentId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            // If music library is ready, return immediately.
            result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
        } else {
            // Otherwise, only return results when the music library is retrieved.
            result.detach();
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {

                @Override
                public void onMusicCatalogReady(boolean success) {

                }
            });
        }
    }

    @Override
    public void onPlaybackStart() {

    }

    @Override
    public void onNotificationRequired() {

    }

    @Override
    public void onPlaybackStop() {

    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        // Weak reference objects, which do not prevent their referents from being
        // made finalizable, finalized, and then reclaimed.
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    LogHelper.d("Ignoring delayed stop since the media player is in use.");
                    return ;
                }

                LogHelper.d("Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }
}
