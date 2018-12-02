package com.android.approjects.musicfx;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

import static com.android.approjects.musicfx.MusicFXActivity.TAG;

/**
 * The Common class defines constants to be used by the control panels.
 */
public class ControlPanelEffect {

    /**
     * Audio session priority
     */
    private static final int PRIORITY = 0;

    /**
     * The control mode specifies if control panel updates effects and preferences or only
     * preferences.
     */
    // 改变音效，如果当前有audio session就应用到播放中。
    static enum ControlMode {
        /**
         * Control panel updates effects and preferences. Applicable when audio session is delivered
         * by user.
         */
        CONTROL_EFFECTS,
        /**
         * Control panel only updates preferences. Applicable when there was no audio or invalid
         * session provided by user.
         */
        CONTROL_PREFERENCES
    }

    static enum Key {
        // 全局控制
        global_enabled,

        // 和Virtualizer相关
        virt_enabled, virt_strength_supported, virt_strength, virt_type,

        // 和BassBoost相关
        bb_enabled, bb_strength,

        te_enabled, te_strength,

        avl_enabled,

        lm_enabled, lm_strength,

        eq_enabled, eq_num_bands, eq_level_range, eq_center_freq, eq_band_level,
        eq_num_presets, eq_preset_name, eq_preset_user_band_level,
        eq_preset_user_band_level_default, eq_preset_opensl_es_band_level,
        eq_preset_ci_extreme_band_level, eq_current_preset,

        // 和PresetRevert相关
        pr_enabled, pr_current_preset
    }

    // Effect/audio session Mappings
    /**
     * Hashmap initial capacity
     */
    private static final int HASHMAP_INITIAL_CAPACITY = 16;

    /**
     * Hashmap load factor
     */
    private static final float HASHMAP_LOAD_FACTOR = 0.75f;
    /**
     * ConcurrentHashMap concurrency level
     */
    private static final int HASHMAP_CONCURRENCY_LEVEL = 2;

    /**
     * Map containing the Virtualizer audio session, effect mappings.
     */
    // 后面详细学习ConcurrentHashMap
    private static final ConcurrentHashMap<Integer, Virtualizer> mVirtualizerInstances =
            new ConcurrentHashMap<>(
                    HASHMAP_INITIAL_CAPACITY, HASHMAP_LOAD_FACTOR, HASHMAP_CONCURRENCY_LEVEL);

    /**
     * Map containing the BB audio session, effect mappings.
     */
    private static final ConcurrentHashMap<Integer, BassBoost> mBassBoostInstances =
            new ConcurrentHashMap<>(HASHMAP_INITIAL_CAPACITY, HASHMAP_LOAD_FACTOR, HASHMAP_CONCURRENCY_LEVEL);

    /**
     * Map containing the EQ audio session, effect mappings.
     */
    private static final ConcurrentHashMap<Integer, Equalizer> mEQInstances =
            new ConcurrentHashMap<>(HASHMAP_INITIAL_CAPACITY, HASHMAP_LOAD_FACTOR, HASHMAP_CONCURRENCY_LEVEL);

    /**
     * Map containing the PR audio session, effect mappings.
     */
    private static final ConcurrentHashMap<Integer, PresetReverb> mPresetReverbInstances =
            new ConcurrentHashMap<>(HASHMAP_INITIAL_CAPACITY, HASHMAP_LOAD_FACTOR, HASHMAP_CONCURRENCY_LEVEL);

    /**
     * Map containing the package name, audio session mappings.
     */
    private static final ConcurrentHashMap<String, Integer> mPackageSessions =
            new ConcurrentHashMap<>(HASHMAP_INITIAL_CAPACITY, HASHMAP_LOAD_FACTOR, HASHMAP_CONCURRENCY_LEVEL);

    // Defaults
    final static boolean GLOBAL_ENABLED_DEFAULT = false;
    private final static boolean VIRTUALIZER_ENABLED_DEFAULT = true;
    private final static int VIRTUALIZER_STRENGTH_DEFAULT = 0;
    private final static boolean BASS_BOOST_ENABLED_DEFAULT = true;
    private final static int BASS_BOOST_STRENGTH_DEFAULT = 667;
    private final static boolean PRESET_REVERB_ENABLED_DEFAULT = false;
    private final static int PRESET_REVERB_CURRENT_PRESET_DEFAULT = 0; // None

    // EQ defaults
    private final static boolean EQUALIZER_ENABLED_DEFAULT = true;
    private final static String EQUALIZER_PRESET_NAME_DEFAULT = "Preset";
    private final static short EQUALIZER_NUMBER_BANDS_DEFAULT = 5;
    private final static short EQUALIZER_NUMBER_PRESETS_DEFAULT = 0;
    private final static short[] EQUALIZER_BAND_LEVEL_RANGE_DEFAULT = { -1500, 1500 };
    private final static int[] EQUALIZER_CENTER_FREQ_DEFAULT = { 60000, 230000, 910000, 3600000,
            14000000 };
    private final static short[] EQUALIZER_PRESET_CIEXTREME_BAND_LEVEL = { 0, 800, 400, 100, 1000 };
    private final static short[] EQUALIZER_PRESET_USER_BAND_LEVEL_DEFAULT = { 0, 0, 0, 0, 0 };

    // EQ effect properties which are invariable over all EQ effects sessions
    private static short[] mEQBandLevelRange = EQUALIZER_BAND_LEVEL_RANGE_DEFAULT;
    private static short mEQNumBands = EQUALIZER_NUMBER_BANDS_DEFAULT;
    private static int[] mEQCenterFreq = EQUALIZER_CENTER_FREQ_DEFAULT;
    private static short mEQNumPresets = EQUALIZER_NUMBER_PRESETS_DEFAULT;
    private static short[][] mEQPresetOpenSLESBandLevel =
            new short[EQUALIZER_NUMBER_PRESETS_DEFAULT][EQUALIZER_NUMBER_BANDS_DEFAULT];
    private static String[] mEQPresetNames;
    private static boolean mIsEQInitialized = false;
    private final static Object mEQInitLock = new Object();

    /**
     * Default int argument used in methods to see that the arg is a dummy. Used for method
     * overloading.
     */
    private final static int DUMMY_ARGUMENT = -1;

    // 开始写几个长函数！

    public static void initEffectsPreferences(final Context context, final String packageName,
                                              final int audioSession) {
        final SharedPreferences prefs = context.getSharedPreferences(packageName,
                Context.MODE_PRIVATE);
    }

    public static void setParameterBoolean(final Context context, final String packageName,
                                           final int audioSession, final Key key, final boolean value) {

    }

    public static Boolean getParameterBoolean(final Context context, final String packageName,
                                              final int audioSession, final Key key) {
        final SharedPreferences prefs = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);

        boolean value = false;

        try {
            value = prefs.getBoolean(key.toString(), value);
        }catch (final RuntimeException e) {
            Log.e(TAG, "getParameterBoolean: " + key + "; " + value + "; " + e);
        }

        return value;
    }


    // 函数重载
    public static void setParameterInt(final Context context, final String packageName,
                                       final int audioSession, final Key key,
                                       final int arg0, final int arg1) {

    }

    public static void setParameterInt(final Context context, final String packageName,
                                       final int audioSession, final Key key, final int arg) {

    }

    public static void setParameterInt(final Context context, final String packageName,
                                       final int audioSession, final Key key) {

    }

    public static int getParameterInt(final Context  context, final String packageName,
                                      final int audioSession, final String key) {

        int value = 0;


        return value;
    }

    public static int getParameterInt(final Context  context, final String packageName,
                                      final int audioSession, final Key key) {
        return getParameterInt(context, packageName, audioSession, key.toString());
    }

    public static int getParameterInt(final Context context, final String packageName,
                                      final int audioSession, final Key key, final int arg) {
        return getParameterInt(context, packageName, audioSession, key.toString() + arg);
    }

    public static int getParameterInt(final Context context, final String packageName,
                                      final int audioSession, final Key key, final int arg0, final int arg1) {
        return getParameterInt(context, packageName, audioSession, key.toString() + arg0 + "_"
                + arg1);
    }


    public static int[] getParameterIntArray(final Context context, final String packageName,
                                             final int audioSession, final Key key) {
        final SharedPreferences prefs = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);

        int[] intArray = null;


        return intArray;
    }

    public static String getParameterString(final Context context, final String packageName,
                                            final int audioSession, final String key) {
        String value = "";


        return value;
    }

    public static String getParameterString(final Context context, final String packageName,
                                            final int audioSession, final Key key, final int arg) {
        return getParameterString(context, packageName, audioSession, key.toString() + arg);
    }
}
