package com.android.approjects.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import com.android.approjects.R;
import com.android.approjects.permissions.camera.CameraViewFragment;
import com.android.approjects.permissions.logger.Log;
import com.android.approjects.permissions.logger.LogFilter;
import com.android.approjects.permissions.logger.LogFragment;
import com.android.approjects.permissions.logger.LogWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.security.Permissions;

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

    private static final int REQUEST_STORAGE = 2;

    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;

    /**
     * Permissions required to read and write contacts. Used by the {@link ContactsFragment}.
     */
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // 存储相关权限申请。
    public void showStorage(View view) {
        Log.i(TAG, "Show storage button pressed. Checking permission.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions();
        } else {
            Log.i(TAG,
                    "Storage permissions have already been granted.");
        }
    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i(TAG, "Displaying storage permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar.make(mLayout, R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(PermissionsActivity.this, PERMISSIONS_CONTACT,
                                            REQUEST_CONTACTS);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
        }
    }

    // 相机相关权限申请。
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


    // 联系人相关权限申请。
    /**
     * Called when the 'Contacts' button is clicked.
     * Callback is defined in resource layout definition.
     */
    public void showContacts(View v) {
        Log.i(TAG, "Show contacts button pressed. Checking permissions.");

        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Contacts permissions have not been granted.
            Log.i(TAG, "Contact permissions has NOT been granted. Requesting permissions.");
            requestContactsPermissions();

        } else {

            // Contact permissions have been granted. Show the contacts fragment.
            Log.i(TAG,
                    "Contact permissions have already been granted. Displaying contact details.");
            showContactDetails();
        }
    }

    private void requestContactsPermissions() {
        // BEGIN_INCLUDE(contacts_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_CONTACTS)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Log.i(TAG,
                    "Displaying contacts permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar.make(mLayout, R.string.permission_contacts_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(PermissionsActivity.this, PERMISSIONS_CONTACT,
                                            REQUEST_CONTACTS);
                        }
                    })
                    .show();
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }
        // END_INCLUDE(contacts_permission_request)
    }

    /**
     * Display the {@link ContactsFragment} in the content area if the required contacts
     * permissions
     * have been granted.
     */
    private void showContactDetails() {
        getFragmentManager().beginTransaction()
                .replace(R.id.permissions_content_fragment, ContactsFragment.newInstance())
                .addToBackStack("contacts")
                .commit();
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
