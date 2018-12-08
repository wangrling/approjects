package com.android.approjects.permissions.camera;

import android.content.Context;
import android.view.Display;

/**
 * Monitors the value returned from {@link Display#getRotation()}.
 */

abstract class DisplayOrientationDetector {


    public DisplayOrientationDetector(Context context) {

    }

    public abstract void onDisplayOrientationChanged(int displayOrientation);
}
