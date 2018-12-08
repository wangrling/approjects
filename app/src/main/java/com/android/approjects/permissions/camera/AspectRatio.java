package com.android.approjects.permissions.camera;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.collection.SparseArrayCompat;

import static com.google.common.math.IntMath.gcd;

/**
 * Immutable class for describing proportional relationship between width and height.
 */

public class AspectRatio implements Comparable<AspectRatio>, Parcelable {

    private final static SparseArrayCompat<SparseArrayCompat<AspectRatio>> sCache =
        new SparseArrayCompat<>(16);

    private final int mX;
    private final int mY;

    /**
     * Returns an instance of {@link AspectRatio} specified by {@code x} and {@code y} values.
     * The values {@code x} and {@code} will be reduced by their greatest common divider.
     *
     * @param x The width
     * @param y The height
     * @return An instance of {@link AspectRatio}
     */
    public static AspectRatio of(int x, int y) {
        int gcd = gcd(x, y);
        x /= gcd;
        y /= gcd;
        SparseArrayCompat<AspectRatio> arrayX = sCache.get(x);
        if (arrayX == null) {
            AspectRatio ratio = new AspectRatio(x, y);
            arrayX = new SparseArrayCompat<>();
            arrayX.put(y, ratio);
            sCache.put(x, arrayX);
            return ratio;
        } else {
            AspectRatio ratio = arrayX.get(y);
            if (ratio == null) {
                ratio = new AspectRatio(x, y);
                arrayX.put(y, ratio);
            }
            return ratio;
        }
    }

    /**
     * Parse an {@link AspectRatio} from a {@link String} formatted like "4:3".
     *
     * @param s The string representation of the aspect ratio
     * @return The aspect ratio
     * @throws IllegalArgumentException when the format is incorrect.
     */
    public static AspectRatio parse(String s) {
        int position = s.indexOf(':');
        if (position == -1) {
            throw new IllegalArgumentException("Malformed aspect ratio: " + s);
        }
        try {
            int x = Integer.parseInt(s.substring(0, position));
            int y = Integer.parseInt(s.substring(position + 1));
            return AspectRatio.of(x, y);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed aspect ratio: " + s, e);
        }
    }

    private AspectRatio(int x, int y) {
        mX = x;
        mY = y;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public static final Creator<AspectRatio> CREATOR = new Creator<AspectRatio>() {
        @Override
        public AspectRatio createFromParcel(Parcel in) {
            int x = in.readInt();
            int y = in.readInt();
            return AspectRatio.of(x, y);
        }

        @Override
        public AspectRatio[] newArray(int size) {
            return new AspectRatio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mX);
        dest.writeInt(mY);
    }

    @Override
    public int compareTo(AspectRatio o) {
        return 0;
    }
}
