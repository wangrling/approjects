package com.android.approjects.deskclock;

import com.android.approjects.deskclock.provider.Alarm;

/**
 * The main activity of the application which displays 4 different tabs contains alarms, world
 * clock, timers and a stopwatch.
 */

public class DeskClock extends BaseActivity implements
        FabContainer, LabelDialogFragment.AlarmLabelDialogHandler {


    @Override
    public void onDialogLabelSet(Alarm alarm, String label, String tag) {

    }
}
