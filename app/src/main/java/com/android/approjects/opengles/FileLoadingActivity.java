package com.android.approjects.opengles;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import androidx.annotation.Nullable;

import static com.android.approjects.opengles.setup.ES2SetupActivity.LOGTAG;

public class FileLoadingActivity extends Activity {
    static {
        System.loadLibrary("es2");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences savedValues = getSharedPreferences("saveValues", MODE_PRIVATE);
        int programRuns = savedValues.getInt("programRuns", 1);

        Log.d(LOGTAG, "This application has been run " + programRuns + " times");
        programRuns++;

        SharedPreferences.Editor editor = savedValues.edit();
        editor.putInt("programRuns", programRuns);
        editor.commit();

        String privateAssetDirectory = getFilesDir().getAbsolutePath();
        String publicAssetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath();
        String cacheAssetDirectory = getCacheDir().getAbsolutePath();

        Log.i(LOGTAG, "privateAssetDirectory's path is equal to: " + privateAssetDirectory);
        Log.i(LOGTAG, "publicAssetDirectory's path is equal to: " + publicAssetDirectory);
        Log.i(LOGTAG, "cacheAssetDirectory's path is equal to: " + cacheAssetDirectory);

        String assetFileName = "assetFile.txt";

        extractAsset(assetFileName, privateAssetDirectory);
        extractAsset(assetFileName, publicAssetDirectory);
        extractAsset(assetFileName, cacheAssetDirectory);

        init(privateAssetDirectory + "/" + assetFileName,
                publicAssetDirectory + "/" + assetFileName, cacheAssetDirectory + "/" + assetFileName);
    }

    public native static void init(String privateFile, String publicFile, String cacheFile);

    /* [extractAssetBeginning] */
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
            /* [extractAssetBeginning] */
            /* [extractAssets] */
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
            /* [extractAssets] */
            /*  [extractAssetsErrorChecking] */
            catch(Exception e)
            {
                Log.e(LOGTAG, "Failure in extractAssets(): " + e.toString() + " " + assetPath+assetName);
            }
            if(fileTest.exists())
            {
                Log.d(LOGTAG,"File Extracted successfully");
            }
            /*  [extractAssetsErrorChecking] */
        }
    }
}
