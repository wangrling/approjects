package com.android.approjects.exoplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.approjects.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * Track media that has been downloaded.
 *
 * <p>
 *     Tracked downloads are persisted using an an {@link ActionFile}, however
 *     in a real application it's expected that state will be stored directly
 *     in the application's media database, so that it can be queried efficiently
 *     together with other information about the media.
 * </p>
 */

public class DownloadTracker implements DownloadManager.Listener {

    /**
     * Listens for changes in the tracked downloads.
     */
    public interface Listener {
        /**
         * Called when the tracked downloads changed.
         */
        void onDownloadsChanged();
    }

    private static final String TAG = "DownloadTracker";

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final TrackNameProvider trackNameProvider;
    // Set
    private final CopyOnWriteArraySet<Listener> listeners;
    // 保存下载的网址
    private final HashMap<Uri, DownloadAction> trackedDownloadStates;
    private final ActionFile actionFile;
    private final Handler actionFileWriteHandler;

    public DownloadTracker(Context context, DataSource.Factory dataSourceFactory,
                           File actionFile, DownloadAction.Deserializer... deserializers) {
        this.context = context;
        this.dataSourceFactory = dataSourceFactory;
        this.actionFile = new ActionFile(actionFile);
        trackNameProvider = new DefaultTrackNameProvider(context.getResources());
        listeners = new CopyOnWriteArraySet<>();
        trackedDownloadStates = new HashMap<>();
        HandlerThread actionFileWriteThread = new HandlerThread("DownloadTracker");
        actionFileWriteThread.start();
        actionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());
        loadTrackedActions(
                deserializers.length > 0 ? deserializers : DownloadAction.getDefaultDeserializers());
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(Uri uri) {
        return trackedDownloadStates.containsKey(uri);
    }

    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        if (!trackedDownloadStates.containsKey(uri)) {
            return Collections.emptyList();
        }

        return trackedDownloadStates.get(uri).getKeys();
    }

    public void toggleDownload(Activity activity, String name, Uri uri, String extension) {
        if (isDownloaded(uri)) {
            DownloadAction removeAction =
                    getDownloadHelper(uri, extension).getRemoveAction(Util.getUtf8Bytes(name));
            startServiceWithAction(removeAction);
        } else {
            StartDownloadDialogHelper helper;

        }
    }

    private void startServiceWithAction(DownloadAction removeAction) {

    }

    @Override
    public void onInitialized(DownloadManager downloadManager) {

    }

    @Override
    public void onTaskStateChanged(DownloadManager downloadManager, DownloadManager.TaskState taskState) {

    }

    @Override
    public void onIdle(DownloadManager downloadManager) {

    }

    private DownloadHelper getDownloadHelper(Uri uri, String extension) {
        int type = Util.inferContentType(uri, extension);

        switch (type) {
            case C.TYPE_DASH:
                return new DashDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_SS:
                return new SsDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_HLS:
                return new HlsDownloadHelper(uri, dataSourceFactory);
            case C.TYPE_OTHER:
                return new ProgressiveDownloadHelper(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private final class StartDownloadDialogHelper implements
            DownloadHelper.Callback, DialogInterface.OnClickListener {

        private final DownloadHelper downloadHelper;
        private final String name;
        private final AlertDialog.Builder builder;
        private final View dialogView;
        private final List<TrackKey> trackKeys;
        private final ArrayAdapter<String> trackTitles;
        private final ListView representationList;

        public StartDownloadDialogHelper(
                Activity activity, DownloadHelper downloadHelper, String name) {
            this.downloadHelper = downloadHelper;
            this.name = name;
            builder = new AlertDialog.Builder(activity)
                    .setTitle("Download")
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, null);

            // Inflate with the builder's context to ensure the correct style is used.
            LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());
            dialogView = dialogInflater.inflate(R.layout.layout_download_dialog);

            trackKeys = new ArrayList<>();
            trackTitles = new ArrayAdapter<>(
                    builder.getContext(), android.R.layout.simple_list_item_multiple_choice);
            representationList = dialogView.findViewById(R.id.representation_list);
            representationList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            representationList.setAdapter(trackTitles);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

        }



        @Override
        public void onPrepared(DownloadHelper helper) {

        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {

        }
    }
}
