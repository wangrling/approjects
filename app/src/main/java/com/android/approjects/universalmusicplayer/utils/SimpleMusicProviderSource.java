package com.android.approjects.universalmusicplayer.utils;

import android.annotation.SuppressLint;
import android.support.v4.media.MediaMetadataCompat;

import com.android.approjects.universalmusicplayer.model.MusicProviderSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleMusicProviderSource  implements MusicProviderSource {

    // Contains metadata about an item, such as the title, artist, etc.
    private List<MediaMetadataCompat> mData = new ArrayList<>();

    @SuppressLint("WrongConstant")
    public void add(String title, String album, String artist, String genre, String source,
                    String iconUrl, long trackNumber, long totalTrackCount, long durationMs) {

        String id = String.valueOf(source.hashCode());

        // noinspection ResourceType
        mData.add(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build());
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        return mData.iterator();
    }
}
