package com.android.approjects;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

public class AppProjectsActivity extends Activity
        implements FragmentManager.OnBackStackChangedListener {

    final static String TAG = "AppProjects";

    private static final String TITLE_FRAGMENT_TAG =
            "com.android.approjects.TITLE_FRAGMENT_TAG";

    // A handle to the main screen view.
    View mMainView;


    /*
     * This callback is invoked when the Activity is first created. It sets up the Activity's
     * window and initializes the Fragments associated with the Activity.
     */
    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects_app);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        // Inflates the main View, which will be the hst View for the fragments.
        mMainView = getLayoutInflater().inflate(R.layout.activity_projects_app, null);

        // Gets an instance of the support library FragmentManager
        FragmentManager localFragmentManager = getFragmentManager();

        /**
         * Adds the back stack change listener defined in this Activity as the listener
         * for the FragmentManager.
         */
        localFragmentManager.addOnBackStackChangedListener(this);

        // If the incoming state of the Activity is null, sets the initial view to be thumbnails.
        if (null == savedInstanceState) {
            // Starts a Fragment transaction to track the stack
            FragmentTransaction localFragmentTransaction = localFragmentManager
                    .beginTransaction();

            // Adds the AppInfoFragment to the host View
            localFragmentTransaction.add(R.id.fragment_host,
                    new AppInfoFragment(), TITLE_FRAGMENT_TAG);

            // Commits this transaction to display the Fragment
            localFragmentTransaction.commit();
        }
    }

    @Override
    public void onBackStackChanged() {

    }

    @Override
    protected void onDestroy() {

        // Sets the main View to null.
        mMainView = null;

        super.onDestroy();
    }
}
