package com.android.approjects;

import android.app.Fragment;
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

import com.android.approjects.musicfx.MusicFXActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

import static com.android.approjects.AppProjectsActivity.TAG;

public class TitleFragment extends Fragment {

    private static List<Pair<String, String>> mAppInfos = new ArrayList<>();

    static {
        mAppInfos.add(new Pair("UniversalMusicPlayer",
                "Implement an audio media app that works across multiple form factors."));

        // 具体实现还需要等UniversalMusicPlayer写完。
        mAppInfos.add(new Pair("MusicFX", "Equalizer Virtualizer BassBoost PresetReverb"));
    }

    private Class<?>[] mAppMainClasses = {
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
            public Pair<String, String> getItem(int position) {
                return mAppInfos.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = getActivity().getLayoutInflater().
                        inflate(android.R.layout.simple_list_item_2, parent, false);

                ((TextView) view.findViewById(android.R.id.text1)).setText(mAppInfos.get(position).first);
                ((TextView) view.findViewById(android.R.id.text2)).setText(mAppInfos.get(position).second);

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
