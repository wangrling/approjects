package com.android.approjects.deskclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

public class ThemeUtils {

    /** Temporary array used internally to resolve attributes. */
    private static final int[] TEMP_ATTR = new int[1];

    private ThemeUtils() {
        // Prevent instantiation.
    }


    /**
     * Convenience method for retrieving a themed color value.
     *
     * @param context the {@link Context} to resolve the theme attribute against
     * @param attr    the attribute corresponding to the color to resolve
     * @return the color value of the resolved attribute
     */
    @ColorInt
    public static int resolveColor(Context context, @AttrRes int attr) {
        return resolveColor(context, attr, null);
    }

    /**
     * Convenience method for retrieving a themed color value.
     *
     * @param context  the {@link Context} to resolve the theme attribute against
     * @param attr     the attribute corresponding to the color to resolve
     * @param stateSet an array of {@link android.view.View} states
     * @return the color value of the resolved attribute
     */
    public static int resolveColor(Context context, @AttrRes int attr, @AttrRes int[] stateSet) {
        final TypedArray a;
        synchronized (TEMP_ATTR) {
            TEMP_ATTR[0] = attr;
            a = context.obtainStyledAttributes(TEMP_ATTR);
        }

        try {
            if (stateSet == null) {
                return a.getColor(0, Color.RED);
            }

            final ColorStateList colorStateList = a.getColorStateList(0);
            if (colorStateList != null) {
                return colorStateList.getColorForState(stateSet, Color.RED);
            }
            return Color.RED;
        } finally {
            a.recycle();
        }
    }
}
