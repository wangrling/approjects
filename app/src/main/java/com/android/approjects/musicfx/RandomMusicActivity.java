package com.android.approjects.musicfx;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.approjects.R;

import androidx.annotation.Nullable;

/**
 * Main activity: shows media player buttons. This activity shows the media player buttons and
 * lets the user click them. No media handling is done here -- everything is done by passing
 * Intents to our {@link MusicService}.
 * */

public class RandomMusicActivity extends Activity implements
        View.OnClickListener {

    /**
     * The URL we suggest as default when adding by URL. This is just so that the user doesn't
     * have to find an URL to test this sample.
     */
    // 暗香
    final String SUGGESTED_URL = "http://101.200.36.231/audiolibrary/nx.mp3";

    Button mPlayButton;
    Button mPauseButton;
    Button mSkipButton;
    Button mRewindButton;
    Button mStopButton;
    Button mEjectButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_random_music);

        mPlayButton = findViewById(R.id.play);
        mPauseButton = findViewById(R.id.pause);
        mSkipButton = findViewById(R.id.skip);
        mRewindButton = findViewById(R.id.rewind);
        mStopButton = findViewById(R.id.stop);
        mEjectButton = findViewById(R.id.eject);

        mPlayButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mSkipButton.setOnClickListener(this);
        mRewindButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mEjectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Send the correct intent to the MusicService, according to the button that was clicked.
        Intent intent = new Intent(this, MusicService.class);

        if (v == mPlayButton) {
            intent.setAction(MusicService.ACTION_PLAY);
            startService(intent);
        } else if (v == mPauseButton) {
            intent.setAction(MusicService.ACTION_PAUSE);
            startService(intent);
        } else if (v == mSkipButton) {
            intent.setAction(MusicService.ACTION_SKIP);
            startService(intent);
        } else if (v == mRewindButton) {
            intent.setAction(MusicService.ACTION_REWIND);
            startService(intent);
        } else if (v == mStopButton) {
            intent.setAction(MusicService.ACTION_STOP);
            startService(intent);
        } else if (v == mEjectButton) {
            showUrlDialog();
        }
    }

    /**
     * Shows an alert dialog where the user can input a URL. After showing the dialog, if the user
     * confirms, sends the appropriate intent to the {@link MusicService} to cause that URL to be
     * played.
     */
    void showUrlDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Manual Input");
        alertBuilder.setMessage("Enter a URL (must be http://)");
        final EditText input = new EditText(this);
        alertBuilder.setView(input);

        input.setText(SUGGESTED_URL);

        alertBuilder.setPositiveButton("Play!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Send an intent with the URL of the song to play. This is expected by
                // MusicService.
                Intent i = new Intent(RandomMusicActivity.this, MusicService.class);
                i.setAction(MusicService.ACTION_URL);
                Uri uri = Uri.parse(input.getText().toString());
                i.setData(uri);
                startService(i);
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int whichButton) {}
        });

        alertBuilder.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                Intent intent = new Intent(this, MusicService.class);
                intent.setAction(MusicService.ACTION_TOGGLE_PLAYBACK);
                startService(intent);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
