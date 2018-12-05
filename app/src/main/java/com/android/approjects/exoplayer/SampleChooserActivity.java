package com.android.approjects.exoplayer;

import android.app.Activity;
import android.view.View;
import android.widget.ExpandableListView;

/**
 * An activity for selecting from a list of media samples.
 */

public class SampleChooserActivity extends Activity
        implements DownloadTracker.Listener, ExpandableListView.OnChildClickListener {
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
    }

    @Override
    public void onDownloadsChanged() {

    }
}
