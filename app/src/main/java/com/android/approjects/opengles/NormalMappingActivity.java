package com.android.approjects.opengles;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.android.approjects.opengles.setup.ES2SetupActivity;
import com.android.approjects.opengles.setup.ES2SetupView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.Nullable;

public class NormalMappingActivity extends ES2SetupActivity {

    private static Context applicationContext;
    private static String assetDirectory = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        applicationContext = getApplicationContext();
        assetDirectory = applicationContext.getFilesDir().getPath() + "/";

        extractAsset("normalMap256.raw");

        graphicsView = new NormalMappingView(getApplication());

        super.onCreate(savedInstanceState);
    }


    private class NormalMappingView extends ES2SetupView {
        public NormalMappingView(Context context) {
            super(context);

            setRenderer(new GLSurfaceView.Renderer() {
                @Override
                public void onSurfaceCreated(GL10 gl, EGLConfig config) {

                }

                @Override
                public void onSurfaceChanged(GL10 gl, int width, int height) {
                    init(width, height);
                }

                @Override
                public void onDrawFrame(GL10 gl) {
                    step();
                }
            });
        }
    }

    public static native void init(int width, int height);

    public static native void step();

    private void extractAsset(String assetName) {
        File fileTest = new File(assetDirectory + assetName);

        if(fileTest.exists()) {
            Log.d(LOGTAG,assetName +  " already exists no extraction needed\n" + " " + assetDirectory + assetName);
        } else {
            Log.d(LOGTAG, assetName + " doesn't exist extraction needed \n");
            try {
                // 将程序里面的文件保存到手机里。
                RandomAccessFile out = new RandomAccessFile(assetDirectory + assetName,"rw");
                AssetManager am = applicationContext.getResources().getAssets();
                InputStream inputStream = am.open(assetName);

                byte buffer[] = new byte[1024];
                int count = inputStream.read(buffer, 0,1024);

                while (count > 0) {
                    out.write(buffer, 0, count);
                    count = inputStream.read(buffer, 0, 2014);
                }

                out.close();
                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOGTAG, "Failure in extractAssets(): " + e.toString() + " " + assetDirectory + assetName);
                e.printStackTrace();
            }
            if (fileTest.exists()) {
                Log.d(LOGTAG, "File Extracted successfully");
            }
        }
    }
}
