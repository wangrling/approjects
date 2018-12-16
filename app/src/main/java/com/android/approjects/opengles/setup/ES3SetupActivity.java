package com.android.approjects.opengles.setup;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import androidx.annotation.Nullable;

public class ES3SetupActivity extends Activity {
    static {
        System.loadLibrary("es3");
    }

    protected static Context applicationContext = null;
    protected static String assetsDirectory = null;
    protected static String LOGTAG = "OpenGL";
    protected ES3SetupView es3SetupView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 子类必须先初始化ES3SetupView显示。
        setContentView(es3SetupView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        es3SetupView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        es3SetupView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public  void extractAsset(String assetName)
    {
        File file = new File(assetsDirectory + assetName);

        if(file.exists()) {
            Log.d(LOGTAG, assetName +  " already exists. No extraction needed.\n");
        } else {
            Log.d(LOGTAG, assetName + " doesn't exist. Extraction needed. \n");

            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(assetsDirectory + assetName,"rw");
                AssetManager assetManager     = applicationContext.getResources().getAssets();
                InputStream inputStream      = assetManager.open(assetName);

                byte buffer[] = new byte[1024];
                int count     = inputStream.read(buffer, 0, 1024);

                while (count > 0) {
                    randomAccessFile.write(buffer, 0, count);

                    count = inputStream.read(buffer, 0, 1024);
                }

                randomAccessFile.close();
                inputStream.close();
            } catch(Exception e) {
                Log.e(LOGTAG, "Failure in extractAssets(): " + e.toString() + " " + assetsDirectory + assetName);
            }

            if(file.exists()) {
                Log.d(LOGTAG,"File extracted successfully");
            }
        }
    }
}
