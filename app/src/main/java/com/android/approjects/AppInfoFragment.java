package com.android.approjects;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.approjects.exoplayer.SampleChooserActivity;
import com.android.approjects.musicfx.MusicFXActivity;
import com.android.approjects.universalmusicplayer.ui.MusicPlayerActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

import static com.android.approjects.AppProjectsActivity.TAG;

public class AppInfoFragment extends Fragment {

    private static List<Pair<String, Integer>> mAppInfos = new ArrayList<>();

    static {
        mAppInfos.add(new Pair<>("ExoPlayer", R.drawable.exoplayer));

        // 数据没有办法显示
        mAppInfos.add(new Pair("UniversalMusicPlayer", R.drawable.ump));

        // 具体实现还需要等UniversalMusicPlayer写完。
        mAppInfos.add(new Pair("MusicFX", R.drawable.musicfx));
    }

    private Class<?>[] mAppMainClasses = {
            SampleChooserActivity.class,
            MusicPlayerActivity.class,
            MusicFXActivity.class
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(getContext());

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mAppInfos.size();
            }

            @Override
            public Pair<String, Integer> getItem(int position) {
                return mAppInfos.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = getActivity().getLayoutInflater().
                        inflate(R.layout.fragment_list_item, parent, false);

                ((TextView) view.findViewById(android.R.id.text1)).setText(mAppInfos.get(position).first);
                (view.findViewById(android.R.id.icon))
                        .setBackground(getResources().getDrawable(mAppInfos.get(position).second, null));

                return view;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick position = " + position);
                getActivity().startActivity(new Intent(getActivity(), mAppMainClasses[position]));
            }
        });

        return listView;
    }
}
