package com.android.approjects.universalmusicplayer.utils;

import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

public class QueueHelper {


    public static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return queue != null && index >= 0 && index <= queue.size();
    }
}
