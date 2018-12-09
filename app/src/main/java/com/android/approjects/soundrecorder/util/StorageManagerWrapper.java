package com.android.approjects.soundrecorder.util;

import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class StorageManagerWrapper {
    private static final String TAG = "StorageManagerWrapper";
    private static final int VOLUME_SDCARD_INDEX = 1;

    private static StorageManager mStorageManager;

    public static StorageManager getStorageManager(Context context) {
        if (mStorageManager == null) {
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }

        return mStorageManager;
    }

    public static StorageVolume[] getVolumeList(Context context) {
        StorageVolume[] volumes = null;

        try {
            // volumes = getStorageManager(context).getVolumeList();
            List<StorageVolume> volumeList = getStorageManager(context).getStorageVolumes();
            volumes = new StorageVolume[volumeList.size()];
            for (int i = 0; i < volumeList.size(); i++) {
                volumes[i] = volumeList.get(i);
            }

        } catch (Exception e) {
            Log.e(TAG, "couldn't talk to MountService", e);
        }

        return volumes;
     }

    public static StorageVolume getStorageVolume(Context context,File file) {
        return getStorageManager(context).getStorageVolume(file);
    }


    public static String getSdDirectory(Context context) {
        String sdDirectory = null;

        try {
            final StorageVolume[] volumes = StorageManagerWrapper.getVolumeList(context);
            if (volumes.length > VOLUME_SDCARD_INDEX) {
                StorageVolume volume = volumes[VOLUME_SDCARD_INDEX];
                if (volume.isRemovable()) {
                    // 对应用程序隐藏
                    // sdDirectory = volume.getPath();

                    // 应该可以使用反射
                    // Class<? extends StorageVolume> classes = volume.getClass();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "couldn't talk to MountService", e);
        }

        return sdDirectory;
    }
}
