//
// Created by wangrl on 18-12-16.
//

#ifndef APPROJECTS_COMMON_H
#define APPROJECTS_COMMON_H

#include <android/log.h>
#include <cstdio>
#include <cstdlib>
#include <GLES3/gl3.h>
#define LOG_TAG "OpenGL"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,  LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,   LOG_TAG, __VA_ARGS__)
#define ASSERT(x, s)                                                    \
        if (!(x))                                                           \
        {                                                                   \
            LOGE("Assertion failed at %s:%i\n%s\n", __FILE__, __LINE__, s); \
            exit(1);                                                        \
        }

#define GL_CHECK(x)                                                                              \
        x;                                                                                           \
        {                                                                                            \
            GLenum glError = glGetError();                                                           \
            if(glError != GL_NO_ERROR) {                                                             \
                LOGE("glGetError() = %i (0x%.8x) at %s:%i\n", glError, glError, __FILE__, __LINE__); \
                exit(1);                                                                             \
            }                                                                                        \
        }

#endif //APPROJECTS_COMMON_H
