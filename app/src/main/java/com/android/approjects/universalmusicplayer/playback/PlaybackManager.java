package com.android.approjects.universalmusicplayer.playback;

import android.content.res.Resources;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.android.approjects.universalmusicplayer.model.MusicProvider;
import com.android.approjects.universalmusicplayer.utils.LogHelper;

public class PlaybackManager {

    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.android.approjects.ump.THUMBS_UP";

    private MusicProvider mMusicProvider;
    private QueueManager mQueueManager;
    private Resources mResources;


    // 在MediaService.setCallback地方注册，监听client发过来的动作。
    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            LogHelper.d("MediaSessionCallback play");
            if (mQueueManager.getCurrentMusic() == null) {
                mQueueManager.setRandomQueue();
            }
            handlePlayRequest();
        }

    }

    // MusicService实现
    public interface PlaybackServiceCallback {
        void onPlaybackStart();
        void onNotificationRequired();
        void onPlaybackStop();
        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
