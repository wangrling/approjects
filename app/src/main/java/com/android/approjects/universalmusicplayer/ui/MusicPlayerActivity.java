package com.android.approjects.universalmusicplayer.ui;

import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.widget.BaseAdapter;

import com.android.approjects.R;
import com.android.approjects.universalmusicplayer.utils.LogHelper;

import androidx.annotation.Nullable;

/**
 * Main activity for the music player.
 * This class hold the MediaBrowser and the MediaController instances. It will create a MediaBrowser
 * when it is created and connect/disconnect on start/stop. Thus, a MediaBrowser will be always
 * connected while this activity is running.
 */

public class MusicPlayerActivity extends BaseActivity implements
        MediaBrowserFragment.MediaFragmentListener {

    private static final String SAVED_MEDIA_ID = "com.android.approjects.ump.MEDIA_ID";
    private static final String FRAGMENT_TAG = "ump_list_container";

    public static final String EXTRA_START_FULLSCREEN =
            "com.android.approjects.ump.EXTRA_START_FULLSCREEN";
    /**
     * Optionally used with {@link #EXTRA_START_FULLSCREEN} to carry a MediaDescription to
     * the {@link FullScreenPlayerActivity}, speeding up the screen rendering
     * while the {@link MediaControllerCompat} is connecting.
     */
    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.android.approjects.CURRENT_MEDIA_DESCRIPTION";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.d("MusicPlayerActivity onCreate");

        setContentView(R.layout.activity_player);

        // Only check if a full screen player is needed on the first time.
        if (savedInstanceState == null) {
            startFullScreenActivityIfNeeded(getIntent());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String mediaId = getMediaId();
        if (mediaId != null) {
            outState.putString(SAVED_MEDIA_ID, mediaId);
        }

        super.onSaveInstanceState(outState);
    }


}
