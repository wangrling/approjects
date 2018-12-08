package com.android.approjects.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.android.approjects.R;
import com.android.approjects.permissions.camera.CameraViewFragment;
import com.android.approjects.permissions.logger.Log;
import com.android.approjects.permissions.logger.LogFilter;
import com.android.approjects.permissions.logger.LogFragment;
import com.android.approjects.permissions.logger.LogWrapper;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class PermissionsActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "PermissionsActivity";

    /**
     * Id to identify a camera permission request.
     */
    private static final int REQUEST_CAMERA = 0;

    /**
     * Id to identify a contacts permission request.
     */
    private static final int REQUEST_CONTACTS = 1;

    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;


    /**
     * Called when the 'Camera' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showCamera(View view) {
        Log.i(TAG, "Show camera button pressed. Checking permission.");
        // BEGIN_INCLUDE(camera_permission)
        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            requestCameraPermission();
        } else {
            // Camera permissions is already available, show the camera preview.
            Log.i(TAG,
                    "CAMERA permission has already been granted. Displaying camera preview.");
            showCameraPreview();
        }
        // END_INCLUDE(camera_permission)
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG, "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(PermissionsActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    })
                    .show();
        } else {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
        // END_INCLUDE(camera_permission_request)
    }

    /**
     * Display the {@link CameraViewFragment} in the content area if the required Camera
     * permission has been granted.
     */
    private void showCameraPreview() {
        getFragmentManager().beginTransaction()
                .replace(R.id.permissions_content_fragment, CameraViewFragment.newInstance())
                .addToBackStack("camera")
                .commit();
    }



    /**
     * Called when the 'Contacts' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showContacts(View v) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);

        mLayout = findViewById(R.id.permissions_root_view);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            RuntimePermissionFragment fragment = new RuntimePermissionFragment();
            transaction.replace(R.id.permissions_content_fragment, fragment);
            transaction.commit();
        }

        initializeLogging();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Set up targets to receive log data.
     */
    public void initializeLogging() {
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log.method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        LogFilter logFilter = new LogFilter();
        logWrapper.setNext(logFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getFragmentManager()
                .findFragmentById(R.id.log_fragment);
        logFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }
}
