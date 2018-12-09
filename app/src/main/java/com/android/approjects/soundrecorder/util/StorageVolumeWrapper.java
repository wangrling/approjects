package com.android.approjects.soundrecorder.util;

import android.content.Context;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.File;

public class StorageVolumeWrapper{
    private static final String TAG = "StorageVolumeWrapper";

    private static StorageVolume mStorageVolume = null;

    /*must set Instance before use this class*/
    public static StorageVolume getBaseInstance(Context context) {
        return mStorageVolume;
    }

    /*must set Instance before use it*/
    public static void setBaseInstance(StorageVolume base) {
        mStorageVolume = base;
    }

    /*must call it to set Instance before use this class*/
    public static void setBaseInstance(Context context, File file) {
        StorageVolume vol = StorageManagerWrapper.getStorageVolume(context,file);

        mStorageVolume = vol;
    }

    public static String getPath() {
        String path = null;

        if (mStorageVolume != null) {
            // path = mStorageVolume.getPath();
        }else{
            Log.e(TAG, "getPath mStorageVolume is null");
        }

        return path;
    }

    public static String getState() {
        String state = null;

        if (mStorageVolume != null) {
            state = mStorageVolume.getState();
        }else{
            Log.e(TAG, "getState mStorageVolume is null");
        }

        return state;
    }

}
