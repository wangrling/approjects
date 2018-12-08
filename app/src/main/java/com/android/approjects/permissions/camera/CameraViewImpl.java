package com.android.approjects.permissions.camera;

abstract class CameraViewImpl {
    protected final Callback mCallback;

    protected final PreviewImpl mPreview;

    CameraViewImpl(Callback callback, PreviewImpl preview) {
        mCallback = callback;
        mPreview = preview;
    }


    interface Callback {
        void onCameraOpened();

        void onCameraClosed();

        void onPictureTaken();
    }
}
