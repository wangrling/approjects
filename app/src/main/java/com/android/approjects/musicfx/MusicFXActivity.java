package com.android.approjects.musicfx;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.approjects.R;

import java.util.Formatter;
import java.util.Locale;

import androidx.annotation.Nullable;

// 将Activity改成fragment直接将播放和显示弄在一起。


/**
 * 主要是使用Virtualizer (虚拟化), BassBoost (低音增强), Equalizer (均衡器), PresetRevert (回音)四种音效。
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
 *
 * {@link MusicFXActivity}只是界面显示，真正的音效控制全在{@link ControlPanelEffect}.
 *
 * 还要专门写个播放器调用MusicFX来设置音效。
 *
 */

public class MusicFXActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    // package visible
    final static String TAG = "MusicFX";

    /**
     * Max number of EQ bands supported.
     */
    // EQ bands有什么作用？
    // private final static int EQUALIZER_MAX_BANDS = 32;
    private final static int EQUALIZER_MAX_BANDS = 8;

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
    private final SeekBar[] mEqualizerSeekBar = new SeekBar[EQUALIZER_MAX_BANDS];
    private int mNumberEqualizerBands;
    private int mEqualizerMinBandLevel;

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
        // 没有AudioSession怎么打开音效调节？
        final Intent intent = getIntent();
        mAudioSession = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                AudioEffect.ERROR_BAD_VALUE);

        Log.v(TAG, "audio session: " + mAudioSession);

        // 为什么要获取调用的包明？
        // 只是创建SharedPreferences使用。
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

        // 音效的控制面板
        final ViewGroup viewGroup = findViewById(R.id.content_sound_effects);

        // Set accessibility label for bass boost and virtualizer strength seekbars.
        findViewById(R.id.bb_strength_text).setLabelFor(R.id.bb_strength_seekbar);
        findViewById(R.id.vi_strength_text).setLabelFor(R.id.vi_strength_seekbar);

        // Fill array with presets from AudioEffects call.
        // allocate a space for 2 extra strings (CI Extreme & User)
        final int numPresets = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_presets);

        // 为mEQPresetNames赋值
        mEQPresetNames = new String[numPresets + 2];
        for (short i = 0; i < numPresets; i++) {
            mEQPresetNames[i] = ControlPanelEffect.getParameterString(mContext,
                    mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_preset_name, i);
        }
        mEQPresetNames[numPresets] = getString(R.string.ci_extreme);
        mEQPresetNames[numPresets+1] = getString(R.string.user);
        mEQPresetUserPos = numPresets + 1;

        // Watch for button clicks and initialization.
        if (mVirtualizerSupported || mBassBoostSupported || mEqualizerSupported ||
                mPresetReverbSupported) {
            // Set the listener for the main enhancements toggle button.
            // Depending on the state enable the supported effects if they were
            // checked in the setup tab.
            // 显示在ActionBar上面的。
            mToggleSwitch = new Switch(this);
            mToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Set parameter and state
                    ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                            mAudioSession, ControlPanelEffect.Key.global_enabled, isChecked);
                    // Enable Linear layout (in scroll layout) view with all
                    // effect contents depending on checked state
                    // 总的音效开关
                    setEnabledAllChildren(viewGroup, isChecked);
                    // update UI according to headset state
                    updateUIHeadset();
                }
            });

            // Initialize the Virtualizer elements.
            // Set the SeekBar listener.
            if (mVirtualizerSupported) {
                // Show msg when disabled slider (layout) is touched.
                findViewById(R.id.vi_layout).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            showHeadsetMsg();
                        }
                        return false;
                    }
                });

                final SeekBar seekbar = findViewById(R.id.vi_strength_seekbar);
                seekbar.setMax(OpenSLESConstants.VIRTUALIZER_MAX_STRENGTH -
                        OpenSLESConstants.VIRTUALIZER_MIN_STRENGTH);

                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    // Update the parameters while SeekBar changes and set the
                    // effect parameter.

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // set parameter and state
                        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.virt_strength, progress);
                    }

                    // If slider pos was 0 when starting re-enable effect
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (seekBar.getProgress() == 0) {
                            ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                    mAudioSession, ControlPanelEffect.Key.virt_enabled, true);
                        }
                    }

                    // If slider pos = 0 when stopping disable effect
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // disable
                        if (seekBar.getProgress() == 0) {
                            ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                    mAudioSession, ControlPanelEffect.Key.virt_enabled, false);
                        }
                    }
                });

                // 控制虚拟化的开关
                final Switch sw = findViewById(R.id.vi_strength_toggle);
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.virt_enabled, isChecked);
                    }
                });
            }

            // Initialize the Bass Boost elements.
            // Set the SeekBar listener.
            if (mBassBoostSupported) {
                // Show msg when disabled slider (layout) is touched
                // 为什么可以设置监听？
                findViewById(R.id.bb_layout).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            showHeadsetMsg();
                        }
                        // 不再往下传递。
                        return false;
                    }
                });

                final SeekBar seekbar = findViewById(R.id.bb_strength_seekbar);
                seekbar.setMax(OpenSLESConstants.BASSBOOST_MAX_STRENGTH -
                        OpenSLESConstants.BASSBOOST_MIN_STRENGTH);

                seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    // Update the parameters while SeekBar changes and set the
                    // effect parameter.

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // Set parameter and state
                        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.bb_strength, progress);
                    }

                    // If slider pos was 0 when starting re-enable effect
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (seekBar.getProgress() == 0) {
                            ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                    mAudioSession, ControlPanelEffect.Key.bb_enabled, true);
                        }
                    }

                    // If slider pos = 0 when stopping disable effect
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // disable
                        ControlPanelEffect.setParameterBoolean(mContext, mCallingPackageName,
                                mAudioSession, ControlPanelEffect.Key.bb_enabled, false);
                    }
                });
            }

            // Initialize the Equalizer elements.
            if (mEqualizerSupported) {
                mEQPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.eq_current_preset);
                if (mEQPreset >= mEQPresetNames.length) {
                    mEQPreset = 0;
                }
                mEQPresetPrevious = mEQPreset;

                // 初始化在下面两个函数
                // 记住有很多SeekBar
                equalizerSpinnerInit((Spinner) findViewById(R.id.eq_spinner));
                equalizerBandsInit(findViewById(R.id.eq_container));
            }

            // Initialize the Preset Reverb elements.
            // Set Spinner listeners.
            if (mPresetReverbSupported) {
                mPRPreset = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                        mAudioSession, ControlPanelEffect.Key.pr_current_preset);
                mPRPresetPrevious = mPRPreset;

                // 通过Spinner选择是哪种房间形式
                reverbSpinnerInit((Spinner)findViewById(R.id.pr_spinner));
            }

            ActionBar ab = getActionBar();
            final int padding = getResources().getDimensionPixelSize(
                    R.dimen.action_bar_switch_padding);

            mToggleSwitch.setPadding(0, 0, padding, 0);

            ab.setCustomView(mToggleSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT));

            ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

        } else {
            viewGroup.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.no_effects_textview)).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mVirtualizerSupported || mBassBoostSupported || mEqualizerSupported ||
                mPresetReverbSupported) {
            // Listen for broadcast intents that might affect the onscreen UI for headset.
            final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            registerReceiver(mReceiver, intentFilter);

            // Check is wired or Bluetooth headset is connected/on
            final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            mIsHeadsetOn = (audioManager.isWiredHeadsetOn() ||
                    audioManager.isBluetoothA2dpOn());

            Log.v(TAG, "onResume: mIsHeadsetOn: " + mIsHeadsetOn);

            // Update UI
            updateUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister for broadcast intents. (These affect the visible UI,
        // so we only care about them while we're in the foreground.)
        if ((mVirtualizerSupported) || (mBassBoostSupported) || (mEqualizerSupported)
                || (mPresetReverbSupported)) {
            unregisterReceiver(mReceiver);
        }
    }

    private void reverbSpinnerInit(Spinner spinner) {
        // 设置适配器，填充数据。
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, PresetReverbPresetStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != mPRPresetPrevious) {
                    presetReverbSetPreset(position);
                }

                mPRPresetPrevious = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner.setSelection(mPRPreset);
    }

    private void equalizerSpinnerInit(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mEQPresetNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != mEQPresetPrevious) {
                    equalizerSetPreset(position);
                }
                mEQPresetPrevious = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(mEQPreset);
    }

    // ActionBar上面的Toggle开关。
    private void setEnabledAllChildren(final ViewGroup viewGroup, final boolean enabled) {
        final int count = viewGroup.getChildCount();

        for (int i = 0; i < count; i++) {
            final View view = viewGroup.getChildAt(i);
            if (view instanceof  ViewGroup) {
                final ViewGroup vg = (ViewGroup) view;
                setEnabledAllChildren(vg, enabled);
            }
            view.setEnabled(enabled);
        }
    }

    /**
     * Updates UI (checkbox, seekbars, enabled states) according to the current stored preferences.
     */
    // onResume最后调用
    private void updateUI() {
        final boolean isEnabled = ControlPanelEffect.getParameterBoolean(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.global_enabled);
        mToggleSwitch.setChecked(isEnabled);
        setEnabledAllChildren((ViewGroup) findViewById(R.id.content_sound_effects), isEnabled);
        updateUIHeadset();

        if (mVirtualizerSupported) {
            SeekBar bar = findViewById(R.id.vi_strength_seekbar);
            Switch sw = findViewById(R.id.vi_strength_toggle);
            int strength = ControlPanelEffect.getParameterInt(
                    mContext, mCallingPackageName, mAudioSession,
                    ControlPanelEffect.Key.virt_strength);
            bar.setProgress(strength);
            boolean hasStrength = ControlPanelEffect.getParameterBoolean(mContext,
                    mCallingPackageName, mAudioSession,
                    ControlPanelEffect.Key.virt_strength_supported);
            if (hasStrength) {
                sw.setVisibility(View.GONE);
            } else {
                bar.setVisibility(View.GONE);
                sw.setChecked(sw.isEnabled() && strength != 0);
            }
        }
        if (mBassBoostSupported) {
            ((SeekBar) findViewById(R.id.bb_strength_seekbar)).setProgress(ControlPanelEffect
                    .getParameterInt(mContext, mCallingPackageName, mAudioSession,
                            ControlPanelEffect.Key.bb_strength));
        }
        if (mEqualizerSupported) {
            equalizerUpdateDisplay();
        }
        if (mPresetReverbSupported) {
            int reverb = ControlPanelEffect.getParameterInt(
                    mContext, mCallingPackageName, mAudioSession,
                    ControlPanelEffect.Key.pr_current_preset);
            ((Spinner)findViewById(R.id.pr_spinner)).setSelection(reverb);
        }
    }

    /**
     * Updates UI for headset mode. En/disable VI and BB controls depending on headset state
     * (on/off) if effects are on. Do the inverse for their layouts so they can take over
     * control/events.
     */
    // 还要具体在界面上操作。
    private void updateUIHeadset() {
        if (mToggleSwitch.isChecked()) {
            // setEnabled有什么作用？
            // 应该无法通过setText修改TextView的值。
            ((TextView) findViewById(R.id.vi_strength_text)).setEnabled(
                    mIsHeadsetOn || !mVirtualizerIsHeadphoneOnly);
            ((SeekBar) findViewById(R.id.vi_strength_seekbar)).setEnabled(
                    mIsHeadsetOn || !mVirtualizerIsHeadphoneOnly);
            findViewById(R.id.vi_layout).setEnabled(!mIsHeadsetOn || !mVirtualizerIsHeadphoneOnly);
            ((TextView) findViewById(R.id.bb_strength_text)).setEnabled(mIsHeadsetOn);
            ((SeekBar) findViewById(R.id.bb_strength_seekbar)).setEnabled(mIsHeadsetOn);
            findViewById(R.id.bb_layout).setEnabled(!mIsHeadsetOn);
        }
    }

    /**
     * Initializes the equalizer elements. Set the SeekBars and Spinner listeners.
     */
    private void equalizerBandsInit(View eqContainer) {
        // Initialize the N-Band Equalizer elements.
        // 都是读取的配置信息。
        mNumberEqualizerBands = ControlPanelEffect.getParameterInt(mContext, mCallingPackageName,
                mAudioSession, ControlPanelEffect.Key.eq_num_bands);
        mEQPresetUserBandLevelsPrev = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_preset_user_band_level);
        final int[] centerFreqs = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_center_freq);
        final int[] bandLevelRange = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_level_range);

        mEqualizerMinBandLevel = (int) Math.max(EQUALIZER_MIN_LEVEL, bandLevelRange[0]);
        final int mEqualizerMaxBandLevel = (int) Math.min(EQUALIZER_MAX_LEVEL, bandLevelRange[1]);

        for (int band = 0; band < mNumberEqualizerBands; band++) {
            // Unit conversion from mHz to Hz and use k prefix if necessary to display.
            final int centerFreq = centerFreqs[band] / 1000;
            float centerFreqHz = centerFreq;
            String unitPrefix = "";
            if (centerFreqHz >= 1000) {
                centerFreqHz = centerFreqHz / 1000;
                unitPrefix = "k";
            }
            ((TextView) eqContainer.findViewById(EQViewElementIds[band][0])).setText(
                    format("%.0f", centerFreqHz) + unitPrefix +"Hz");
            mEqualizerSeekBar[band] = (SeekBar) eqContainer
                    .findViewById(EQViewElementIds[band][1]);
            eqContainer.findViewById(EQViewElementIds[band][0])
                    .setLabelFor(EQViewElementIds[band][1]);
            mEqualizerSeekBar[band].setMax(mEqualizerMaxBandLevel - mEqualizerMinBandLevel);

            // 监听设置在Activity中
            mEqualizerSeekBar[band].setOnSeekBarChangeListener(this);
        }

        // Hide the inactive Equalizer bands.
        for (int band = mNumberEqualizerBands; band < EQUALIZER_MAX_BANDS; band++) {
            // CenterFreq text
            eqContainer.findViewById(EQViewElementIds[band][0]).setVisibility(View.GONE);
            // SeekBar
            eqContainer.findViewById(EQViewElementIds[band][1]).setVisibility(View.GONE);
        }

        TextView tv = (TextView) findViewById(R.id.max_level_text);
        tv.setText(String.format("+%d dB", (int) Math.ceil(mEqualizerMaxBandLevel / 100)));
        tv = (TextView) findViewById(R.id.center_level_text);
        tv.setText("0 dB");
        tv = (TextView) findViewById(R.id.min_level_text);
        tv.setText(String.format("%d dB", (int) Math.floor(mEqualizerMinBandLevel / 100)));
        equalizerUpdateDisplay();
    }

    private String format(String format, Object... args) {
        mFormatBuilder.setLength(0);
        mFormatter.format(format, args);
        return mFormatBuilder.toString();
    }


    // For the EQ Band SeekBars
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        final int id = seekBar.getId();

        for (short band = 0; band < mNumberEqualizerBands; band++) {
            if (id == EQViewElementIds[band][1]) {
                final short level = (short) (progress + mEqualizerMinBandLevel);
                if (fromUser) {
                    equalizerBandUpdate(band, level);
                }
                break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // get current levels
        final int[] bandLevels = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_band_level);
        // copy current levels to user preset
        for (short band = 0; band < mNumberEqualizerBands; band++) {
            equalizerBandUpdate(band, bandLevels[band]);
        }
        equalizerSetPreset(mEQPresetUserPos);
        ((Spinner)findViewById(R.id.eq_spinner)).setSelection(mEQPresetUserPos);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        equalizerUpdateDisplay();
    }

    /**
     * Updates the EQ by getting the parameters.
     */
    private void equalizerUpdateDisplay() {
        // Update and show the active N-Band Equalizer bands.
        final int[] bandLevels = ControlPanelEffect.getParameterIntArray(mContext,
                mCallingPackageName, mAudioSession, ControlPanelEffect.Key.eq_band_level);

        for (short band = 0; band < mNumberEqualizerBands; band++) {
            final int level = bandLevels[band];
            final int progress = level - mEqualizerMinBandLevel;
            mEqualizerSeekBar[band].setProgress(progress);
        }
    }

    /**
     * Updates/Sets a given EQ band level.
     *
     * @param band Band id
     * @param level EQ band level
     */
    private void equalizerBandUpdate(final int band, final int level) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_band_level, level, band);
    }

    /**
     * Sets the given EQ preset.
     *
     * @param preset EQ preset id
     */
    private void equalizerSetPreset(final int preset) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.eq_current_preset, preset);
        equalizerUpdateDisplay();
    }

    /**
     * Sets the given PR preset.
     *
     * @Param preset PR preset id
     */
    private void presetReverbSetPreset(final int preset) {
        ControlPanelEffect.setParameterInt(mContext, mCallingPackageName, mAudioSession,
                ControlPanelEffect.Key.pr_current_preset, preset);
    }

    /**
     * Show msg that headset needs to be pluged.
     */
    private void showHeadsetMsg() {
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        final Toast toast = Toast.makeText(context, getString(R.string.headset_plug), duration);
        toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);

        toast.show();
    }

    // audio played from the left channel also reaches the right ear of the user, and vice-versa.
    private boolean isVirtualizerTransauralSupported() {
        Virtualizer virt = null;
        boolean transauralSupported = false;
        try {
            // 无法使用AudioSystem
            // virt = new Virtualizer(0, android.media.AudioSystem.newAudioSessionId());
            virt = new Virtualizer(0, mAudioSession);
            transauralSupported = virt.canVirtualize(AudioFormat.CHANNEL_OUT_STEREO,
                    Virtualizer.VIRTUALIZATION_MODE_TRANSAURAL);
        } catch (Exception e) {
        } finally {
            if (virt != null) {
                virt.release();
            }
        }
        return transauralSupported;
    }
}
