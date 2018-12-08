package com.android.approjects.permissions.camera;

/**
 * Encapsulates all the operations related to camera preview in a backward-compatible manner.
 */

abstract class PreviewImpl {

    interface Callback {

        void onSurfaceChanged();
    }
}
