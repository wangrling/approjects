package com.android.approjects.permissions.camera;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.approjects.R;

import androidx.annotation.Nullable;

/**
 * Displays a {@link CameraView}.
 * An error message is displayed if the Camera is not available.
 * <p>
 * This Fragment is only used to illustrate that access to the Camera API has been granted (or
 * denied) as part of the runtime permissions model. It is not relevant for the use of the
 * permissions API.
 * <p>
 * Implementation is based directly on the documentation at
 * http://developer.android.com/guide/topics/media/camera.html
 */

public class CameraViewFragment extends Fragment {

    private static final String TAG = "CameraPreview";

    public static CameraViewFragment newInstance() {
        return new CameraViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cameraview, null);

        return view;
    }
}
