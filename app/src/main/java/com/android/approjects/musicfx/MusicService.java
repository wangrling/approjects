package com.android.approjects.musicfx;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.approjects.R;

import java.io.IOException;

import androidx.annotation.Nullable;

public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MusicFocusable, MusicRetrieverTask.PreparedListener {

    // The tag we put on debug messages
    final static String TAG = "RandomMusicPlayer";

    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_TOGGLE_PLAYBACK =
            "com.android.approjects.musicfx.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.android.approjects.musicfx.action.PLAY";
    public static final String ACTION_PAUSE = "com.android.approjects.musicfx.action.PAUSE";
    public static final String ACTION_STOP = "com.android.approjects.musicfx.action.STOP";
    public static final String ACTION_SKIP = "com.android.approjects.musicfx.action.SKIP";
    public static final String ACTION_REWIND = "com.android.approjects.musicfx.action.REWIND";
    public static final String ACTION_URL = "com.android.approjects.musicfx.action.URL";

    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;

    // our media player
    MediaPlayer mPlayer = null;

    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    public int getRandomMusicSession() {
        if (mPlayer != null) {
            return mPlayer.getAudioSessionId();
        } else {
            return -1;
        }
    }

    // indicates the state our service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped,    // media player is stopped and not prepared to play
        Preparing,  // media player is preparing...
        Playing,    // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused      // playback paused (media player ready!)
    };

    State mState = State.Retrieving;

    // if in Retrieving mode, this flag indicates whether we should start playing immediately
    // when we are ready or not.
    boolean mStartPlayingAfterRetrieve = false;

    // if mStartPlayingAfterRetrieve is true, this variable indicates the URL that we should
    // start playing when we are ready. If null, we should play a random song from the device
    Uri mWhatToPlayAfterRetrieve = null;

    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    };

    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // title of the song we are currently playing
    String mSongTitle = "";

    // whether the song we are playing is streaming from the network
    boolean mIsStreaming = false;

    // Wifi lock that we hold when streaming files from the internet, in order to prevent the
    // device from shutting off the Wifi radio
    WifiManager.WifiLock mWifiLock;

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;

    // Our instance of our MusicRetriever, which handles scanning for media and
    // providing titles and URIs as we need.
    MusicRetriever mRetriever;

    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mDummyAlbumArt;

    AudioManager mAudioManager;
    NotificationManager mNotificationManager;

    Notification.Builder mNotificationBuilder = null;

    @Override
    public void onCreate() {
        Log.i(TAG, "creating MusicService");

        // Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "music.lock");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Create the retriever and start an asynchronous task that will prepare it.
        mRetriever = new MusicRetriever(getContentResolver());
        // 获取音乐
        (new MusicRetrieverTask(mRetriever, this)).execute();

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.dummy_album_art);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "onStartCommand action = " + action);
        if (action.equals(ACTION_TOGGLE_PLAYBACK))
            processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY))
            processPlayRequest();
        else if (action.equals(ACTION_PAUSE))
            processPauseRequest();
        else if (action.equals(ACTION_SKIP))
            processSkipRequest();
        else if (action.equals(ACTION_STOP))
            processStopRequest();
        else if (action.equals(ACTION_REWIND))
            processRewindRequest();
        else if (action.equals(ACTION_URL))
            processAddRequest(intent);

        // Means we started the service, but don't want it to
        // restart in case it's killed.
        return START_NOT_STICKY;
    }

    // 将private改成public状态。
    public void processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused)
            mPlayer.seekTo(0);
    }

    public void processSkipRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            playNextSong(null);
        }
    }

    // 暂停
    public void processPauseRequest() {
        if (mState == State.Retrieving) {
            // If we are still retrieving media, clear the flag that indicates we should start
            // playing when we're ready
            mStartPlayingAfterRetrieve = false;
            return ;
        }

        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            // While paused, we always retain the MediaPlayer
            // do not give up audio focus
            relaxResources(false);
        }
    }

    public void processPlayRequest() {
        Log.d(TAG, "processPlayRequest state = " + mState);
        if (mState == State.Retrieving) {
            // If we are still retrieving media, just set the flag to start playing when we're
            // ready.
            mWhatToPlayAfterRetrieve = null;    // play a random song
            mStartPlayingAfterRetrieve = true;
            return ;
        }

        tryToGetAudioFocus();

        // actually play the song
        if (mState == State.Stopped) {
            // If we're stopped, just go ahead to the next song and start playing
            playNextSong(null);
        } else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;
            setUpAsForeground(mSongTitle + " (playing)");
            configAndStartMediaPlayer();
        }
    }

    /**
     * Makes sure the media player exist and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    // 需要创建MediaPlayer实例
    private void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        } else {
            mPlayer.reset();
        }
    }

    // 相当于耳机的开关键
    public void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }

    public void processStopRequest() {
        Log.d(TAG, "processStopRequest");
        processStopRequest(false);
    }

    private void processStopRequest(boolean force) {
        Log.d(TAG, "processStopRequest mState = " + mState);
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;

            // Lets go of all resources...
            relaxResources(true);
            giveUpAudioFocus();

            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    private void relaxResources(boolean releasePlayer) {
        // Stop being a foreground service
        stopForeground(true);

        Log.d(TAG, "releaseResources");

        // Stop and release the Media Player, if it's available
        if (releasePlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        // We can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld())
            mWifiLock.release();
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null) {
            mAudioFocus = AudioFocus.NoFocusNoDuck;
        }
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    private void configAndStartMediaPlayer() {
        Log.d(TAG, "configAndStartMediaPlayer mAudioFocus = " + mAudioFocus);
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we known we have to
            // resume playback once we get the focus back.
            if (mPlayer.isPlaying())
                mPlayer.pause();
            return ;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck) {
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
        } else
            mPlayer.setVolume(1.0f, 1.0f);

        if (!mPlayer.isPlaying())
            mPlayer.start();
    }

    // 使用网址播放
    public void processAddRequest(Intent intent) {
        // user wants to play a song directly by URL or path. The URL or path comes in the "data"
        // part of the Intent. This Intent is sent by {@link MainActivity} after the user
        // specifies the URL/path via an alert box.
        if (mState == State.Retrieving) {
            // We'll play the requested URL right after we finish retrieving.
            mWhatToPlayAfterRetrieve = intent.getData();
            mStartPlayingAfterRetrieve = true;
        } else if (mState == State.Playing || mState == State.Paused || mState == State.Stopped) {
            Log.i(TAG, "Playing from URL/path: " + intent.getData().toString());
            tryToGetAudioFocus();
            playNextSong(intent.getData().toString());
        }
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus()) {
            mAudioFocus = AudioFocus.Focused;
        }
    }

    /**
     * Starts playing the next song. If manualUrl is null, the next song will be randomly selected
     * from our Media Retriever (that is, it will be a random song in the user's device). If
     * manualUrl is non-null, then it specifies the URL or path to the song that will be played
     * next.
     */
    void playNextSong(String manualUrl) {
        mState = State.Stopped;
        relaxResources(false);      // release everything except MediaPlayer.

        try {
            MusicRetriever.Item playingItem = null;
            if (manualUrl != null) {
                // set the source of the media player to the manual URL or path.
                createMediaPlayerIfNeeded();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(manualUrl);
                mIsStreaming = manualUrl.startsWith("http:") || manualUrl.startsWith("https:");

                playingItem = new MusicRetriever.Item(0, null, manualUrl, null, 0);
            } else {
                mIsStreaming = false;       // playing a locally available song.
                playingItem = mRetriever.getRandomItem();
                Log.d(TAG, "playingItem title " + playingItem.getTitle());
                if (playingItem == null) {
                    Toast.makeText(this,
                            "No available music to play. Place some music on your external storage "
                                    + "device (e.g. your SD card) and try again.",
                            Toast.LENGTH_LONG).show();
                    processStopRequest(true); // stop everything!
                    return;
                }

                // set the source of media player a content URI.
                createMediaPlayerIfNeeded();
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(getApplicationContext(), playingItem.getURI());
            }

            mSongTitle = playingItem.getTitle();
            mState = State.Preparing;

            setUpAsForeground(mSongTitle + " (loading)");

            // Starts preparing the media player in the background. When it's done, it will call
            // out OnPreparedListener (this is, the onPrepared() method on this class, since we
            // set the listener to 'this').
            //
            // Until the media player is prepared, we *cannot* call start() on it!
            mPlayer.prepareAsync();

            // If we are streaming from the internet, we want to hold a wifi lock, which prevents
            // the Wifi radio from going to sleep while the song is playing. If, on the other hand,
            // we are *not* streaming, we want to release the lock if we were holding it before.
            if (mIsStreaming)
                mWifiLock.acquire();
            else if (mWifiLock.isHeld()) mWifiLock.release();

        } catch (IOException ex) {
            Log.e("MusicService", "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Called when media player is done playing current song. */
    @Override
    public void onCompletion(MediaPlayer mp) {
        // The media player finished playing the current song, so we go ahead and start the next.
        playNextSong(null);
    }

    /** Called when media player is done preparing. */
    @Override
    public void onPrepared(MediaPlayer mp) {
        // The media player is done preparing. That means we can start playing!
        Log.d(TAG, "MediaPlayer onPrepared");
        mState = State.Playing;
        updateNotification(mSongTitle + " (playing)");
        configAndStartMediaPlayer();
    }

    void updateNotification(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), RandomMusicActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentText(text)
                .setContentIntent(pi);

        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    private void setUpAsForeground(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), RandomMusicActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification object.
        mNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.exo_icon_play)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("RandomMusicPlayer")
                .setContentText(text)
                .setContentIntent(pi)
                .setOngoing(true);

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
                Toast.LENGTH_SHORT).show();

        Log.e(TAG, "Error: what = " + String.valueOf(what) + ", extra = " + String.valueOf(extra));

        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();

        return true;    // true indicates we handled the error.
    }

    @Override
    public void onGainedAudioFocus() {
        Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        // restarted media player with new focus settings.
        if (mState == State.Playing) {
            configAndStartMediaPlayer();
        }
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck"
                : "no duck"), Toast.LENGTH_SHORT).show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying()) {
            configAndStartMediaPlayer();
        }
    }

    @Override
    public void onMusicRetrieverPrepared() {
        Log.d(TAG, "onMusicRetrieverPrepared mStartPlayingAfterRetrieve = " +
                mStartPlayingAfterRetrieve);
        // Done retrieving!
        mState = State.Stopped;
        // If the flag indicates we should starting playing after retrieving, let's do that now.
        if (mStartPlayingAfterRetrieve) {
            tryToGetAudioFocus();
            playNextSong(mWhatToPlayAfterRetrieve == null ?
                    null : mWhatToPlayAfterRetrieve.toString());
        }
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        Log.d(TAG, "onDestroy");
        // mState = State.Stopped;
        // relaxResources(true);
        // giveUpAudioFocus();
    }

    public class ServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    private final IBinder mBinder = new ServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
