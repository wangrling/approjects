package com.android.approjects.exoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.approjects.AppApplication;
import com.android.approjects.R;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * An activity for selecting from a list of media samples.
 */

public class SampleChooserActivity extends Activity
        implements DownloadTracker.Listener, ExpandableListView.OnChildClickListener {

    private static final String TAG = "SampleChooserActivity";

    private boolean useExtensionRenderers;
    private DownloadTracker downloadTracker;
    private SampleAdapter sampleAdapter;
    private MenuItem preferExtensionDecodersMenuItem;
    private MenuItem randomAbrMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chooser_sample);
        sampleAdapter = new SampleAdapter();
        ExpandableListView sampleListView = findViewById(R.id.sample_list);
        sampleListView.setAdapter(sampleAdapter);
        sampleListView.setOnChildClickListener(this);

        Intent intent = getIntent();
        String dataUri = intent.getDataString();
        String[] uris;
        if (dataUri != null) {
            uris = new String[] {
                    dataUri
            };
        } else {
            ArrayList<String> uriList = new ArrayList<>();
            AssetManager assetManager = getAssets();
            try {
                for (String asset : assetManager.list("")) {
                    //相对路径中以.exolist.json结尾的文件。
                    if (asset.endsWith(".exolist.json")) {
                        uriList.add("asset:///" + asset);
                    }
                }
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), R.string.sample_list_load_error, Toast.LENGTH_LONG)
                        .show();
            }
            uris = new String[uriList.size()];
            uriList.toArray(uris);
            Arrays.sort(uris);
        }

        AppApplication application = (AppApplication) getApplication();
        useExtensionRenderers = application.useExtensionRenderers();
        downloadTracker = application.getDownloadTracker();

        // 解析json文件
        SampleListLoader loaderTask = new SampleListLoader();
        loaderTask.execute(uris);

        // Start the download service if it should be running but it's not currently.
        // Starting the service in the foreground causes notification flicker if there is no scheduled
        // action. Starting it in the background throws an exception if the app is in the background too
        // (e.g. if device screen is locked).
        try {
            DownloadService.start(this, DemoDownloadService.class);
        } catch (IllegalStateException e) {
            DownloadService.startForeground(this, DemoDownloadService.class);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sample_chooser_menu, menu);
        preferExtensionDecodersMenuItem = menu.findItem(R.id.prefer_extension_decoders);
        preferExtensionDecodersMenuItem.setVisible(useExtensionRenderers);
        randomAbrMenuItem = menu.findItem(R.id.random_abr);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        downloadTracker.addListener(this);
        sampleAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        downloadTracker.removeListener(this);
        super.onStop();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        Sample sample = (Sample) view.getTag();
        // 启动播放界面
        startActivity(sample.buildIntent(
                this,
                isNonNullAndChecked(preferExtensionDecodersMenuItem),
                isNonNullAndChecked(randomAbrMenuItem) ?
                        PlayerActivity.ABR_ALGORITHM_RANDOM :
                        PlayerActivity.ABR_ALGORITHM_DEFAULT));

        return true;
    }

    private static boolean isNonNullAndChecked(MenuItem menuItem) {
        // Temporary workaround for layouts that do not inflate the options menu.
        return menuItem != null && menuItem.isChecked();
    }

    @Override
    public void onDownloadsChanged() {
        sampleAdapter.notifyDataSetChanged();
    }

    private SampleGroup getGroup(String groupName, List<SampleGroup> groups) {
        for (int i = 0; i < groups.size(); i++) {
            if (Util.areEqual(groupName, groups.get(i).title)) {
                return groups.get(i);
            }
        }
        SampleGroup group = new SampleGroup(groupName);
        groups.add(group);
        return group;
    }

    // Group下面还可以拓展List.
    private final class SampleAdapter extends BaseExpandableListAdapter implements View.OnClickListener {

        private List<SampleGroup> sampleGroups;

        public SampleAdapter() {
            sampleGroups = Collections.emptyList();
        }

        public void setSampleGroups(List<SampleGroup> sampleGroups) {
            this.sampleGroups = sampleGroups;
            notifyDataSetChanged();
        }

        @Override
        public void onClick(View view) {
            onSampleDownloadButtonClicked((Sample) view.getTag());
        }

        @Override
        public int getGroupCount() {
            return sampleGroups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).samples.size();
        }

        @Override
        public SampleGroup getGroup(int groupPosition) {
            return sampleGroups.get(groupPosition);
        }

        @Override
        public Sample getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).samples.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(
                        android.R.layout.simple_expandable_list_item_1, parent ,false);
            }
            ((TextView) view).setText(getGroup(groupPosition).title);
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.sample_list_item, parent, false);
                View downloadButton = view.findViewById(R.id.download_button);
                downloadButton.setOnClickListener(this);
                downloadButton.setFocusable(false);
            }

            initializeChildView(view, getChild(groupPosition, childPosition));

            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private void initializeChildView(View view, Sample sample) {
            view.setTag(sample);
            TextView sampleTitle = view.findViewById(R.id.sample_title);
            sampleTitle.setText(sample.name);

            boolean canDownload = getDownloadUnsupportedStringId(sample) == 0;
            boolean isDownloaded = canDownload && downloadTracker.isDownloaded(((UriSample) sample).uri);

            ImageButton downloadButton = view.findViewById(R.id.download_button);
            downloadButton.setTag(sample);
            downloadButton.setColorFilter(
                    canDownload ? (isDownloaded ? 0xFF42A5F5 : 0xFFBDBDBD) : 0xFFEEEEEE);
            downloadButton.setImageResource(
                    isDownloaded ? R.drawable.ic_download_done : R.drawable.ic_download);
        }
    }

    private void onSampleDownloadButtonClicked(Sample sample) {
        int downloadUnsupportedStringId = getDownloadUnsupportedStringId(sample);
        if (downloadUnsupportedStringId != 0) {
            Toast.makeText(getApplicationContext(), downloadUnsupportedStringId, Toast.LENGTH_LONG)
                    .show();
        } else {
            UriSample uriSample = (UriSample) sample;

            // 下载文件的地方
            downloadTracker.toggleDownload(this, sample.name, uriSample.uri, uriSample.extension);
        }
    }

    private int getDownloadUnsupportedStringId(Sample sample) {
        if (sample instanceof PlaylistSample) {
            return R.string.download_playlist_unsupported;
        }
        UriSample uriSample = (UriSample) sample;
        if (uriSample.drmInfo != null) {
            return R.string.download_drm_unsupported;
        }
        if (uriSample.adTagUri != null) {
            return R.string.download_ads_unsupported;
        }
        String scheme = uriSample.uri.getScheme();
        if (!("http".equals(scheme) || "https".equals(scheme))) {
            return R.string.download_scheme_unsupported;
        }
        return 0;
    }

    private void onSampleGroups(final List<SampleGroup> groups, boolean sawError) {
        if (sawError) {
            Toast.makeText(getApplicationContext(), R.string.sample_list_load_error, Toast.LENGTH_LONG)
                    .show();
        }
        sampleAdapter.setSampleGroups(groups);
    }

    private final class SampleListLoader extends AsyncTask<String, Void, List<SampleGroup>> {

        private boolean sawError;

        @Override
        protected List<SampleGroup> doInBackground(String... uris) {
            List<SampleGroup> result = new ArrayList<>();
            Context context = getApplicationContext();
            String userAgent = Util.getUserAgent(context, "ExoPlayerSample");
            DataSource dataSource =
                    new DefaultDataSource(context, userAgent, false);
            // 想象有多个文件，其实只有一个文件。
            for (String uri : uris) {
                DataSpec dataSpec = new DataSpec(Uri.parse(uri));
                InputStream inputStream = new DataSourceInputStream(dataSource, dataSpec);
                try {
                    readSampleGroups(new JsonReader(new InputStreamReader(inputStream, "UTF-8")), result);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading sample list: " + uri, e);
                    sawError = true;
                } finally {
                    Util.closeQuietly(dataSource);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<SampleGroup> result) {
            onSampleGroups(result, sawError);
        }
    }

    // 第一层数组
    private void readSampleGroups(JsonReader reader, List<SampleGroup> groups) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            readSampleGroup(reader, groups);
        }
        reader.endArray();
    }

    // 第二层数组
    private void readSampleGroup(JsonReader reader, List<SampleGroup> groups) throws IOException {
        String groupName = "";
        ArrayList<Sample> samples = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "name":
                    groupName = reader.nextString();
                    break;
                case "samples":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        samples.add(readEntry(reader, false));
                    }
                    reader.endArray();
                    break;
                case "_comment":
                    reader.nextString();    // Ignore
                    break;
                default:
                    throw new ParserException("Unsupported name: " + name);
            }
        }
        reader.endObject();

        SampleGroup group = getGroup(groupName, groups);
        group.samples.addAll(samples);
    }

    // 实例信息
    private Sample readEntry(JsonReader reader, boolean insidePlaylist) throws IOException {
        String sampleName = null;
        Uri uri = null;
        String extension = null;
        String drmScheme = null;
        String drmLicenseUrl = null;
        String[] drmKeyRequestProperties = null;
        boolean drmMultiSession = false;
        ArrayList<UriSample> playlistSamples = null;
        String adTagUri = null;
        String sphericalStereoMode = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "name":
                    sampleName = reader.nextString();
                    break;
                case "uri":
                    uri = Uri.parse(reader.nextString());
                    break;
                case "extension":
                    extension = reader.nextString();
                    break;
                case "drm_scheme":
                    Assertions.checkState(!insidePlaylist, "Invalid attribute on nested item: drm_scheme");
                    drmScheme = reader.nextString();
                    break;
                case "drm_license_url":
                    Assertions.checkState(!insidePlaylist,
                            "Invalid attribute on nested item: drm_license_url");
                    drmLicenseUrl = reader.nextString();
                    break;
                case "drm_key_request_properties":
                    Assertions.checkState(!insidePlaylist,
                            "Invalid attribute on nested item: drm_key_request_properties");
                    ArrayList<String> drmKeyRequestPropertiesList = new ArrayList<>();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        drmKeyRequestPropertiesList.add(reader.nextName());
                        drmKeyRequestPropertiesList.add(reader.nextString());
                    }
                    reader.endObject();
                    drmKeyRequestProperties = drmKeyRequestPropertiesList.toArray(new String[0]);
                    break;
                case "drm_multi_session":
                    drmMultiSession = reader.nextBoolean();
                    break;
                case "playlist":
                    Assertions.checkState(!insidePlaylist, "Invalid nesting of playlists");
                    playlistSamples = new ArrayList<>();
                    reader.beginArray();
                    while (reader.hasNext()) {
                        playlistSamples.add((UriSample) readEntry(reader, true));
                    }
                    reader.endArray();
                    break;
                case "ad_tag_uri":
                    adTagUri = reader.nextString();
                    break;
                case "spherical_stereo_mode":
                    Assertions.checkState(
                            !insidePlaylist, "Invalid attribute on nested item: spherical_stereo_mode");
                    sphericalStereoMode = reader.nextString();
                    break;
                default:
                    throw new ParserException("Unsupported attribute name: " + name);
            }
        }
        reader.endObject();

        DrmInfo drmInfo =
                drmScheme == null
                        ? null
                        : new DrmInfo(drmScheme, drmLicenseUrl, drmKeyRequestProperties, drmMultiSession);
        if (playlistSamples != null) {
            UriSample[] playlistSamplesArray = playlistSamples.toArray(
                    new UriSample[playlistSamples.size()]);
            return new PlaylistSample(sampleName, drmInfo, playlistSamplesArray);
        } else {
            return new UriSample(
                    sampleName,
                    drmInfo,
                    uri,
                    extension,
                    adTagUri,
                    sphericalStereoMode);
        }
    }

    private static final class SampleGroup {
        public final String title;
        public final List<Sample> samples;

        public SampleGroup(String title) {
            this.title = title;
            this.samples = new ArrayList<>();
        }
    }

    // 数字版权加密
    private static final class DrmInfo {
        public final String drmScheme;
        public final String drmLicenseUrl;
        public final String[] drmKeyRequestProperties;
        public final boolean drmMultiSession;

        public DrmInfo(
                String drmScheme, String drmLicenseUrl, String[] drmKeyRequestProperties,
                boolean drmMultiSession) {
            this.drmScheme = drmScheme;
            this.drmLicenseUrl = drmLicenseUrl;
            this.drmKeyRequestProperties = drmKeyRequestProperties;
            this.drmMultiSession = drmMultiSession;
        }

        public void updateIntent(Intent intent) {
            Assertions.checkNotNull(intent);
            intent.putExtra(PlayerActivity.DRM_SCHEME_EXTRA, drmScheme);
            intent.putExtra(PlayerActivity.DRM_LICENSE_URL_EXTRA, drmLicenseUrl);
            intent.putExtra(PlayerActivity.DRM_KEY_REQUEST_PROPERTIES_EXTRA, drmKeyRequestProperties);
            intent.putExtra(PlayerActivity.DRM_MULTI_SESSION_EXTRA, drmMultiSession);
        }
    }


    private abstract static class Sample {
        public final String name;
        public final DrmInfo drmInfo;

        public Sample(String name, DrmInfo drmInfo) {
            this.name = name;
            this.drmInfo = drmInfo;
        }

        // 启动播放界面
        public Intent buildIntent(
                Context context, boolean preferExtensionDecoders, String abrAlgorithm) {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra(PlayerActivity.PREFER_EXTENSION_DECODERS_EXTRA, preferExtensionDecoders);
            intent.putExtra(PlayerActivity.ABR_ALGORITHM_EXTRA, abrAlgorithm);

            if (drmInfo != null) {
                drmInfo.updateIntent(intent);
            }

            return intent;
        }
    }

    private static final class UriSample extends Sample {

        public final Uri uri;
        public final String extension;
        public final String adTagUri;
        public final String sphericalStereoMode;

        public UriSample(
                String name, DrmInfo drmInfo, Uri uri, String extension,
                String adTagUri, String sphericalStereoMode) {
            super(name, drmInfo);
            this.uri = uri;
            this.extension = extension;
            this.adTagUri = adTagUri;
            this.sphericalStereoMode = sphericalStereoMode;
        }

        @Override
        public Intent buildIntent(Context context, boolean preferExtensionDecoders, String abrAlgorithm) {
            return super.buildIntent(context, preferExtensionDecoders, abrAlgorithm)
                    .setData(uri)
                    .putExtra(PlayerActivity.EXTENSION_EXTRA, extension)
                    .putExtra(PlayerActivity.AD_TAG_URI_EXTRA, adTagUri)
                    .putExtra(PlayerActivity.SPHERICAL_STEREO_MODE_EXTRA, sphericalStereoMode)
                    .setAction(PlayerActivity.ACTION_VIEW);
        }
    }

    private static final class PlaylistSample extends Sample {
        public final UriSample[] children;

        public PlaylistSample(
                String name, DrmInfo drmInfo, UriSample... children) {
            super(name, drmInfo);
            this.children = children;
        }

        @Override
        public Intent buildIntent(Context context, boolean preferExtensionDecoders, String abrAlgorithm) {
            String[] uris = new String[children.length];
            String[] extensions = new String[children.length];
            for (int i = 0; i < children.length; i++) {
                uris[i] = children[i].uri.toString();
                extensions[i] = children[i].extension;
            }

            return super.buildIntent(context, preferExtensionDecoders, abrAlgorithm)
                    .putExtra(PlayerActivity.URI_LIST_EXTRA, uris)
                    .putExtra(PlayerActivity.EXTENSION_LIST_EXTRA, extensions)
                    .setAction(PlayerActivity.ACTION_VIEW_LIST);
        }
    }
}
