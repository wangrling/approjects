package com.android.approjects.universalmusicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载图片，并对图片进行缩放。
 */

public class BitmapHelper {

    // Max read limit that we allow our input stream to mark/reset.
    private static final int MAX_READ_LIMIT_PER_IMG = 1024 * 1024;

    public static Bitmap scaleBitmap(Bitmap src, int maxWidth, int maxHeight) {
        double scaleFactor = Math.min(((double) maxWidth) / src.getWidth(),
                ((double) maxHeight) / src.getHeight());
        // 同比例缩小放大
        return Bitmap.createScaledBitmap(src, (int) (src.getWidth() * scaleFactor),
                (int)(src.getHeight() * scaleFactor), false);
    }

    public static Bitmap scaleBitmap(int scaleFactor, InputStream is) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        // Decode the image file into a Bitmap sized to fill the View.
        bmOptions.inJustDecodeBounds = false;
        // 数值越大，得到的图片越小。
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(is, null, bmOptions);
    }

    public static int findScaleFactor(int targetW, int targetH, InputStream is) {
        // Get the dimensions of the bitmap.
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, bmOptions);
        int actualW = bmOptions.outWidth;
        int actualH = bmOptions.outHeight;

        // Determine how much to scale down the image.
        return Math.min(actualW/targetW, actualH/targetH);
    }

    public static Bitmap fetchAndRescaleBitmap(String uri, int width, int height) throws IOException {
        URL url = new URL(uri);

        BufferedInputStream is = null;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            is.mark(MAX_READ_LIMIT_PER_IMG);
            int scaleFactor = findScaleFactor(width, height, is);

            LogHelper.d("Scaling bitmap " + uri + " by factor " + scaleFactor + " to support " +
                    width + "" + height + " requested dimension.");
            is.reset();

            return scaleBitmap(scaleFactor, is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
