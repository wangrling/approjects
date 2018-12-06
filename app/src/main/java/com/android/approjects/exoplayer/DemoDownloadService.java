package com.android.approjects.exoplayer;

import android.app.Notification;

import com.android.approjects.AppApplication;
import com.android.approjects.R;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;

import androidx.annotation.Nullable;

/**
 * A service for downloading media.
 *
 * {@link DownloadTracker#startServiceWithAction(DownloadAction)}
 */

public class DemoDownloadService extends DownloadService {

    private static final String CHANNEL_ID = "download_channel";
    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    public DemoDownloadService() {
        super(FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                CHANNEL_ID, R.string.exo_download_notification_channel_name);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return ((AppApplication)getApplication()).getDownloadManager();
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @Override
    protected Notification getForegroundNotification(DownloadManager.TaskState[] taskStates) {
        return DownloadNotificationUtil.buildProgressNotification(
                this,
                R.drawable.exo_controls_play,
                CHANNEL_ID,
                null,
                null,
                taskStates);
    }

    // 任务下载完成或者失败回调。
    @Override
    protected void onTaskStateChanged(DownloadManager.TaskState taskState) {
        if (taskState.action.isRemoveAction) {
            return ;
        }

        Notification notification = null;
        if (taskState.state == DownloadManager.TaskState.STATE_COMPLETED) {
            notification =
                    DownloadNotificationUtil.buildDownloadCompletedNotification(
                            this,
                            R.drawable.exo_controls_play,
                            CHANNEL_ID,
                            null, Util.fromUtf8Bytes(taskState.action.data));
        } else if (taskState.state == DownloadManager.TaskState.STATE_FAILED) {
            notification =
                    DownloadNotificationUtil.buildDownloadFailedNotification(
                            this,
                            R.drawable.exo_controls_play,
                            CHANNEL_ID,
                            null,
                            Util.fromUtf8Bytes(taskState.action.data));
        }

        int notificationId = FOREGROUND_NOTIFICATION_ID + 1 + taskState.taskId;
        NotificationUtil.setNotification(this, notificationId, notification);
    }
}
