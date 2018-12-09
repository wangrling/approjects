package com.android.approjects.soundrecorder.util;

import android.content.Context;

import com.android.approjects.R;

public class Utils {
    static final int SECOND_PER_MINUTES = 60;
    static final int SECOND_PER_HOUR = 60 * SECOND_PER_MINUTES;

    public static String timeToString(Context context, long time) {
        long hour = time / SECOND_PER_HOUR;
        long minutes = time / SECOND_PER_MINUTES - hour * SECOND_PER_MINUTES;
        long second = time % SECOND_PER_MINUTES;

        String timerFormat;
        if (hour > 0) {
            timerFormat = "%d:%02d:%02d";
            return String.format(timerFormat, hour, minutes, second);
        } else {
            timerFormat = "%02d:%02d";
            return String.format(timerFormat, minutes, second);
        }
    }
}
