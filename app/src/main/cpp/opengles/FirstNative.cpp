//
// Created by wang on 18-12-14.
//

#include <jni.h>
#include <android/log.h>

#define LOG_TAG "libNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_firstnative_FirstNativeLibrary_init(JNIEnv *env, jclass type) {

    // TODO
    LOGI("Hello From the Native Side!!");
}

