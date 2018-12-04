package com.android.approjects.universalmusicplayer.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.approjects.R;
import com.android.approjects.universalmusicplayer.MusicService;
import com.android.approjects.universalmusicplayer.utils.LogHelper;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the {@link MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */


public class MediaBrowserFragment extends Fragment {

    private BrowseAdapter mBrowserAdapter;
    private String mMediaId;

    // 点击list item时activity会有相关的操作。
    private MediaFragmentListener mMediaFragmentListener;
    private View mErrorView;
    private TextView mErrorMessage;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // If used on an activity that doesn't implement MediaFragmentListener, it
        // will throw an exception as expected:
        mMediaFragmentListener = (MediaFragmentListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogHelper.d("MediaBrowserFragment onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_list_music, container, false);

        mErrorView = rootView.findViewById(R.id.playback_error);
        mErrorMessage = mErrorView.findViewById(R.id.error_message);

        mBrowserAdapter = new BrowserAdapter(getActivity());

        ListView listView = rootView.findViewById(R.id.list_view);
        listView.setAdapter(mBrowserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkForUserVisibleErrors(false);
                MediaBrowserCompat.MediaItem item = mBrowserAdapter.getItem(position);
                mMediaFragmentListener.onMediaItemSelected(item);
            }
        });

        return rootView;
    }

    // An adapter for showing the list of browsed MediaItem's.
    private static class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {

        public BrowseAdapter(@NonNull Context context) {
            super(context, R.layout.media_list_item, new ArrayList<MediaBrowserCompat.MediaItem>());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MediaBrowserCompat.MediaItem item = getItem(position);
            // 定义ListItem如何显示。
            return MediaItemViewHolder.setupListView((Activity) getContext(), convertView,
                    parent, item);
        }
    }
}
