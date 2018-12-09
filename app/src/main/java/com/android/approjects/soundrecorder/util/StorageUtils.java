package com.android.approjects.soundrecorder.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.android.approjects.R;

import java.io.File;

public class StorageUtils {
    private static final String TAG = "StorageUtils";
    private static final int VOLUME_SDCARD_INDEX = 1;

    public static final int STORAGE_PATH_PHONE_INDEX = 0;
    public static final int STORAGE_PATH_SD_INDEX = 1;
    private static final String FOLDER_NAME = "SoundRecorder";
    public static final String FM_RECORDING_FOLDER_NAME = "FMRecording";
    public static final String CALL_RECORDING_FOLDER_NAME = "CallRecord";
    private static final String STORAGE_PATH_EXTERNAL_ROOT = Environment
            .getExternalStorageDirectory().toString();
    private static final String STORAGE_PATH_LOCAL_PHONE =
            STORAGE_PATH_EXTERNAL_ROOT + File.separator + FOLDER_NAME;
    private static final String STORAGE_PATH_FM_RECORDING = STORAGE_PATH_EXTERNAL_ROOT
            + File.separator + FM_RECORDING_FOLDER_NAME;
    private static final String STORAGE_PATH_CALL_RECORDING = STORAGE_PATH_EXTERNAL_ROOT
            + File.separator + CALL_RECORDING_FOLDER_NAME;
    private static String sSdDirectory;

    private static final int SD_STORAGE_FREE_BLOCK = 1;
    private static final double PHONE_STORAGE_FREE_BLOCK_PERCENT = 5 / 100.0;

    private static String getSdDirectory(Context context) {
        if (sSdDirectory == null) {
            sSdDirectory = StorageManagerWrapper.getSdDirectory(context);
        }
        return sSdDirectory;
    }

    public static String getSdState(Context context) {
        String sdPath = getSdDirectory(context);

        if (sdPath != null) {
            StorageVolumeWrapper.setBaseInstance(context,new File(sdPath));
            String state = StorageVolumeWrapper.getState();
            if (state != null) {
                return state;
            } else {
                return Environment.MEDIA_UNKNOWN;
            }
        }
        return Environment.MEDIA_UNMOUNTED;
    }

    public static boolean isPhoneStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean isSdMounted(Context context) {
        return getSdState(context).equals(Environment.MEDIA_MOUNTED);
    }

    public static String applyCustomStoragePath(Context context) {
        return Environment.getExternalStorageDirectory().toString() + File.separator
                + "Recording";
    }

    public static String getPhoneStoragePath() {
        return STORAGE_PATH_LOCAL_PHONE;
    }

    public static String getSdStoragePath(Context context) {
        return StorageUtils.getSdDirectory(context) + File.separator + FOLDER_NAME;
    }

    public static String getFmRecordingStoragePath() {
        return STORAGE_PATH_FM_RECORDING;
    }

    public static String getCallRecordingStoragePath() {
        return STORAGE_PATH_CALL_RECORDING;
    }

    /**
     * Is there any point of trying to start recording?
     */
    public static boolean diskSpaceAvailable(Context context, int path) {
        return getAvailableBlocks(context, path) > 0;
    }

    private static long getAvailableBlocks(Context context, int path) {
        long blocks;
        if (path == StorageUtils.STORAGE_PATH_SD_INDEX) {
            StatFs fs = new StatFs(getSdDirectory(context));
            // keep one free block
            blocks = fs.getAvailableBlocksLong() - SD_STORAGE_FREE_BLOCK;
        } else {
            StatFs fs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            // keep 5% free block
            blocks = fs.getAvailableBlocksLong()
                    - (long)(fs.getBlockCountLong() * PHONE_STORAGE_FREE_BLOCK_PERCENT);
        }
        return blocks;
    }

    public static long getAvailableBlockSize(Context context, int path) {
        StatFs fs;
        if (path == StorageUtils.STORAGE_PATH_SD_INDEX) {
            fs = new StatFs(getSdDirectory(context));
        } else {
            fs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        return getAvailableBlocks(context, path) * fs.getBlockSizeLong();
    }
}
