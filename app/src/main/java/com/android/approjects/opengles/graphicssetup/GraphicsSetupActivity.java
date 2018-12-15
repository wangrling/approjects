package com.android.approjects.opengles.graphicssetup;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import androidx.annotation.Nullable;

public class GraphicsSetupActivity extends Activity {

    static {
        System.loadLibrary("opengles");
    }

    public static String LOGTAG = "OpenGL";

    protected GraphicsView graphicsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOGTAG, "Creating New Tutorial View");
        // graphicsView = new GraphicsView(getApplication());
        setContentView(graphicsView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        graphicsView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        graphicsView.onResume();
    }

    public void extractAsset(String assetName, String assetPath)
    {
        File fileTest = new File(assetPath, assetName);

        if(fileTest.exists())
        {
            Log.d(LOGTAG, assetName +  " already exists no extraction needed\n");
        }
        else
        {
            Log.d(LOGTAG, assetName + " doesn't exist extraction needed \n");
            try
            {
                RandomAccessFile out = new RandomAccessFile(fileTest,"rw");
                AssetManager am = getResources().getAssets();

                InputStream inputStream = am.open(assetName);
                byte buffer[] = new byte[1024];
                int count = inputStream.read(buffer, 0, 1024);

                while (count > 0)
                {
                    out.write(buffer, 0, count);
                    count = inputStream.read(buffer, 0, 1024);
                }
                out.close();
                inputStream.close();
            }
            catch(Exception e)
            {
                Log.e(LOGTAG, "Failure in extractAssets(): " + e.toString() + " " + assetPath+assetName);
            }
            if(fileTest.exists())
            {
                Log.d(LOGTAG,"File Extracted successfully");
            }
        }
    }
}
