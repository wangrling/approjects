package com.android.approjects.cameraraw;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * 1. 打开相机，获取流，
 * 2. 对流进行处理，显示到屏幕上。
 * 2. 进行拍照保存。
 * 3. 进行录像保存。
 * 4. 增加解码器。
 * 3. 将流发送到服务器。
 */

public class CameraRawActivity extends Activity {

    public static final String TAG = "CameraRawActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
