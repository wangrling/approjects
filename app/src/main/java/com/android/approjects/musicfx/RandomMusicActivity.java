package com.android.approjects.musicfx;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.approjects.R;

import androidx.annotation.Nullable;

/**
 * Main activity: shows media player buttons. This activity shows the media player buttons and
 * lets the user click them. No media handling is done here -- everything is done by passing
 * Intents to our {@link MusicService}.
 * */

public class RandomMusicActivity extends Activity implements
        View.OnClickListener {

    private static final String TAG = "RandomMusic";

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

    MusicService mMusicService;

    int mRandomMusicSession;

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

        mMusicServiceBound = false;

        mRandomMusicSession = -1;
    }

    private void bindMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        Log.d(TAG, "bindService");
        bindService(intent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMusicService() {
        unbindService(mMusicServiceConnection);
        mMusicServiceBound = false;
    }

    private boolean mMusicServiceBound;
    private ServiceConnection mMusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.ServiceBinder mServiceBinder = (MusicService.ServiceBinder) service;
            Log.d(TAG, "onServiceConnected getService");
            mMusicService = mServiceBinder.getService();
            mMusicServiceBound = true;
            mMusicService.processPlayRequest();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicServiceBound = false;
        }
    };

    @Override
    public void onClick(View v) {
        /*
        // 添加音效处理，修改程序为BindService模式
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
        */

        if (v == mPlayButton) {
            if (mMusicServiceBound == false) {
                bindMusicService();
            }
            // bindMusicService是一个异步的过程，所以要等绑定完之后才能
            if (mMusicService != null) {
                mMusicService.processPlayRequest();
            }
        } else if (v == mPauseButton) {
            mMusicService.processPauseRequest();
        } else if (v == mSkipButton) {
            mMusicService.processSkipRequest();
        } else if (v == mRewindButton) {
            mMusicService.processRewindRequest();
        } else if (v == mStopButton) {
            if (mMusicServiceBound) {
                mMusicService.processStopRequest();
                unbindMusicService();
            }
        } else if (v == mEjectButton) {
            showUrlDialog();
        }
     }

     void getRandomMusicSession() {
        if (mMusicService != null) {
            mRandomMusicSession = mMusicService.getRandomMusicSession();
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
                // startService(i);
                mMusicService.processAddRequest(i);
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

    // bind LocalService
    LocalService mService;
    boolean mBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        /*
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        */
        // bindMusicService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*
        unbindService(mConnection);
        mBound = false;
        */
    }

    @Override
    public void onBackPressed() {
        if (mMusicServiceBound) {
            mMusicService.processStopRequest();
            unbindMusicService();
        }
        super.onBackPressed();
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    public void onButtonClick(View v) {
        if (mBound) {
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            int num = mService.getRandomNumber();
            Toast.makeText(this, "number: " + num, Toast.LENGTH_SHORT).show();
        }
    }

    /** Define callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and gat LocalService instance
            LocalService.LocalBinder binder =  (LocalService.LocalBinder) service;
            mService = binder.getService();
            mMusicServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicServiceBound =false;
        }
    };

    // Messenger service
    /** Messenger for communication with the service. */
    Messenger mMessengerService = null;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mMessengerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /**
             * This is called when the connection with the service has been
             * established, giving us the object we can use to
             * interact with the service. We are communicating with the
             * service using a Messenger, so here we get a client-side
             * representation of that from the raw IBinder object.
             */
            mMessengerService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    public void sayHello(View v) {
        if (!mBound)
            return;
        // Create and send a message to the service, using a supported 'what' value.
        Message msg = Message.obtain(null, MessengerService.MSG_SAY_HELLO, 0, 0);
        try {
            mMessengerService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
