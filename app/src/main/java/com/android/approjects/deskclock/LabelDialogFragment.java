package com.android.approjects.deskclock;


import android.app.DialogFragment;

import com.android.approjects.deskclock.provider.Alarm;

public class LabelDialogFragment extends DialogFragment {

    /**
     * The tag that identifies instances of LabelDialogFragment in the fragment manager.
     */
    private static final String TAG = "label_dialog";

    public interface AlarmLabelDialogHandler {
        void onDialogLabelSet(Alarm alarm, String label, String tag);
    }
}
