package com.android.approjects.opengles;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.android.approjects.opengles.graphicssetup.GraphicsSetupActivity;
import com.android.approjects.opengles.graphicssetup.GraphicsView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.Nullable;

public class MipmappingActivity extends GraphicsSetupActivity {

    String textureName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        String privateAssetDirectory = getFilesDir().getAbsolutePath();
        Log.d(LOGTAG, privateAssetDirectory);
        textureName = "level6.raw";
        extractAsset(textureName, privateAssetDirectory);
        textureName = "level7.raw";
        extractAsset(textureName, privateAssetDirectory);
        textureName = "level8.raw";
        extractAsset(textureName, privateAssetDirectory);
        textureName = "level9.raw";
        extractAsset(textureName, privateAssetDirectory);

        textureName = "level6.pkm";
        extractAsset(textureName, privateAssetDirectory);
        textureName = "level7.pkm";
        extractAsset(textureName, privateAssetDirectory);
        textureName = "level8.pkm";
        extractAsset(textureName, privateAssetDirectory);
        textureName = "level9.pkm";
        extractAsset(textureName, privateAssetDirectory);

        graphicsView = new MipmappingView(this);

        super.onCreate(savedInstanceState);
    }

    private class MipmappingView extends GraphicsView {
        public MipmappingView(Context context) {
            super(context);

            setRenderer(new Renderer() {
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

    public native static void init(int width, int height);
    public native static void step();
}
