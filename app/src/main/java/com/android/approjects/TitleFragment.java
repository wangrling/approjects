package com.android.approjects;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.approjects.musicfx.MusicFXActivity;

import androidx.annotation.Nullable;

public class TitleFragment extends Fragment implements
        AdapterView.OnItemClickListener {

    private String[] mAppTitles = {
            "MusicFX"
    };

    private Class<?>[] mAppMainClasses = {
            MusicFXActivity.class
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(getContext());

        listView.setAdapter(new ArrayAdapter<>(
                getContext(), android.R.layout.simple_list_item_1,  mAppTitles
        ));

        return listView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(getActivity(), mAppMainClasses[position]));
    }
}
