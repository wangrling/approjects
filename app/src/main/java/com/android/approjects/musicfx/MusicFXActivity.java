package com.android.approjects.musicfx;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.android.approjects.R;

import java.util.Formatter;
import java.util.Locale;

import androidx.annotation.Nullable;

/**
 * 主要是使用Virtualizer, BassBoost, Equalizer, PresetRevert四种音效。
 *
 * <ul>
 *   // Alter the frequency response of a particular music source or of the main output mix.
 *   <li> {@link android.media.audiofx.Equalizer}</li>
 *   // An effect to spatialize audio channels.
 *   <li> {@link android.media.audiofx.Virtualizer}</li>
 *   // Audio effect to boost or amplify low frequencies of the sound.
 *   <li> {@link android.media.audiofx.BassBoost}</li>
 *   // adding some reverb (回音) in a music playback context.
 *   <li> {@link android.media.audiofx.PresetReverb}</li>
 *
 *   <li> {@link android.media.audiofx.EnvironmentalReverb}</li>
 *   <li> {@link android.media.audiofx.DynamicsProcessing}</li>
 * </ul>
 */

public class MusicFXActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    // package visible
    final static String TAG = "MusicFX";

    /**
     * Max number of EQ bands supported.
     */
    // EQ bands有什么作用？
    // private final static int EQUALIZER_BANDS = 32;
    private final static int EQUALIZER_BANDS = 8;

    // 定义20dB的范围。
    /**
     * Max levels per EQ band in millibels (1dB = 100 mB).
     */
    private final static int EQUALIZER_MAX_LEVEL = 1000;

    /**
     * Min levels per EQ band in millibels (1dB = 100 mB).
     */
    private final static int EQUALIZER_MIN_LEVEL = -1000;

    /**
     * Indicates if Virtualizer effect is supported.
     */
    private boolean mVirtualizerSupported;
    private boolean mVirtualizerIsHeadphoneOnly;

    /**
     * Indicates if BassBoost effect is supported.
     */
    private boolean mBassBoostSupported;

    /**
     * Indicates if Equalizer effect is supported.
     */
    private boolean mEqualizerSupported;

    /**
     * Indicates if Preset Reverb effect is supported.
     */
    private boolean mPresetReverbSupported;

    // Equalizer fields
    private final SeekBar[] mEqualizerSeekBar = new SeekBar[EQUALIZER_BANDS];
    private int mNumberEqualizerBands;

    // Preset有什么作用？
    private int mEQPresetUserPos = 1;
    private int mEQPreset;
    private int mEQPresetPrevious;
    private int[] mEQPresetUserBandLevelsPrev;
    private String[] mEQPresetNames;

    private int mPRPreset;
    private int mPRPresetPrevious;

    private boolean mIsHeadsetOn = false;
    private CompoundButton mToggleSwitch;

    private StringBuilder mFormatBuilder = new StringBuilder();

    private Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    /**
     * Mapping for the EQ widget ids per band.
     */
    private static final int[][] EQViewElementIds = {
            { R.id.eq_band0_textview, R.id.eq_band0_seekbar },
            { R.id.eq_band1_textview, R.id.eq_band1_seekbar },
            { R.id.eq_band2_textview, R.id.eq_band2_seekbar },
            { R.id.eq_band3_textview, R.id.eq_band3_seekbar },
            { R.id.eq_band4_textview, R.id.eq_band4_seekbar },
            { R.id.eq_band5_textview, R.id.eq_band5_seekbar },
            { R.id.eq_band6_textview, R.id.eq_band6_seekbar },
            { R.id.eq_band7_textview, R.id.eq_band7_seekbar }

    };

    // Preset Reverb fields.
    // Array containing the PR preset names.
    // 房间不同，回音的类型也不同。
    private static final String[] PresetReverbPresetStrings = {
            "None", "SmallRoom", "MediumRoom", "LargeRoom",
            "MediumHall", "LargeHall", "Plate"
    };

    private Context mContext;

    private String mCallingPackageName = "empty";

    /**
     * Audio session field.
     */
    private int mAudioSession = AudioEffect.ERROR_BAD_VALUE;

    // Broadcast receiver to handle wired and Bluetooth A2dp headset events.
    // A2dp不熟悉
    // 系统会发出相关的广播。
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final boolean isHeadsetOnPrev = mIsHeadsetOn;

            final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                mIsHeadsetOn = (intent.getIntExtra("state", 0) == 1) ||
                        audioManager.isBluetoothA2dpOn();
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {

                // 和蓝牙相关
                final int deviceClass = ((BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getBluetoothClass()
                        .getDeviceClass();

                if ((deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES) ||
                        deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
                    mIsHeadsetOn = true;
                }
            } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                // 类似于拔出耳机
                mIsHeadsetOn = audioManager.isBluetoothA2dpOn() || audioManager.isWiredHeadsetOn();
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {

                final int deviceClass = ((BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getBluetoothClass()
                        .getDeviceClass();
                if ((deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
                        || (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)) {
                    mIsHeadsetOn = audioManager.isWiredHeadsetOn();
                }
            }

            if (isHeadsetOnPrev != mIsHeadsetOn) {
                updateUIHeadset();
            }
        }
    };


    /**
     * Declares and initializes all objects and widgets in the layouts and the CheckBox and SeekBar
     * onchange methods on creation.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        // Receive intent
        // Get calling intent
        final Intent intent = getIntent();
        mAudioSession = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                AudioEffect.ERROR_BAD_VALUE);

        Log.v(TAG, "audio session: " + mAudioSession);

        // 为什么要获取调用的包明？
        mCallingPackageName = getCallingPackage();

        // check for errors
        if (mCallingPackageName == null) {
            Log.e(TAG, "Package name is null.");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        setResult(RESULT_OK);

        Log.v(TAG, mCallingPackageName + " (" + mAudioSession + ")");

        ControlPanelEffect.initEffectsPreferences(mContext, mCallingPackageName, mAudioSession);

        // Query available effects.
        final AudioEffect.Descriptor[] effects = AudioEffect.queryEffects();

        // Determine available/supported effects.
        for (final AudioEffect.Descriptor effect : effects) {
            Log.v(TAG, effect.name.toString() + ", type: " + effect.type.toString());
            if (effect.type.equals(AudioEffect.EFFECT_TYPE_VIRTUALIZER)) {
                mVirtualizerSupported = true;
                mVirtualizerIsHeadphoneOnly = !isVirtualizerTransauralSupported();
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_BASS_BOOST)) {
                mBassBoostSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_EQUALIZER)) {
                mEqualizerSupported = true;
            } else if (effect.type.equals(AudioEffect.EFFECT_TYPE_PRESET_REVERB)) {
                mPresetReverbSupported = true;
            }
        }

        // 已经布置好所有的调节界面。
        setContentView(R.layout.activity_fx_music);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
