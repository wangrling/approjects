package com.android.approjects.opengles;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

public class OpenGLESActivity extends ListActivity {
    public static final String TAG = "OpenGLES";

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String CLASS_NAME = "class_name";

    private static final String[][] TESTS = {
            {
                "Fist Android Native Application",
                    "An introduction and walkthrough of Android native applications.",
                    "firstnative.FirstNativeActivity"
            },
            {
                "Introduction to shaders",
                    "A quick introduction to the programmable graphics pipeline introduced in OpenGL ES 2.0.",
                    "simpletriangle.SimpleTriangleActivity"
            },
            /*
            {
                "Graphics Setup",
                    "Setting up your application ready for graphics.",
                    "graphicssetup.GraphicsSetupActivity"
            },
            */
            {
                "Simple Triangle",
                    "How to create your first triangle.",
                    "simpletriangle.SimpleTriangleActivity"
            },
            {
                "Simple Cube",
                    "Introduction in transformations and movement in OpenGL ES 2.0.",
                    "simplecube.SimpleCubeActivity"
            },
            {
                "Texture Cube",
                "How to start texturing your objects to make them look realistic.",
                    "TextureCubeActivity"
            },
            {
                "Lighting",
                    "Basic lighting using OpenGL ES 2.0.",
                    "LightingActivity"
            },
            {
                "Normal Mapping",
                    "Basic Normal Mapping tutorial that uses Normal Maps.",
                    "NormalMappingActivity"
            }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new SimpleAdapter(this, createActivityList(),
                android.R.layout.two_line_list_item, new String[] {TITLE, DESCRIPTION},
                new int[] {android.R.id.text1, android.R.id.text2}));
    }

    /**
     * Creates the list of of activities from the string arrays.
     */
    private List<Map<String, Object>> createActivityList() {
        List<Map<String, Object>> testList = new ArrayList<>();

        for (String[] test : TESTS) {
            Map<String, Object> tmp = new HashMap<>();
            tmp.put(TITLE, test[0]);
            tmp.put(DESCRIPTION, test[1]);
            Intent intent = new Intent();
            // Do the class name resolution here, so we crash up front rather than when the
            // activity list item is selected if the class name is wrong.
            try {
                Class cls = Class.forName("com.android.approjects.opengles." + test[2]);
                intent.setClass(this, cls);
                tmp.put(CLASS_NAME, intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            testList.add(tmp);
        }

        return testList;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, Object> map = (Map<String, Object>) l.getItemAtPosition(position);
        Intent intent = (Intent) map.get(CLASS_NAME);
        startActivity(intent);
    }
}
