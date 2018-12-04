package com.android.approjects.universalmusicplayer.model;

import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Holder class that encapsulates a MediaMetadata and allows the actual metadata to be modified
 * without requiring to rebuild the collections the metadata is in.
 */

// 对MediaMetadataCompat的封装。

public class MutableMediaMetadata {

    public MediaMetadataCompat metadata;
    private final String trackId;

    public MutableMediaMetadata(String trackId, MediaMetadataCompat metadata) {
        this.metadata = metadata;
        this.trackId = trackId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != MutableMediaMetadata.class)
            return false;

        MutableMediaMetadata that = (MutableMediaMetadata) obj;

        return TextUtils.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() {
        return trackId.hashCode();
    }
}
