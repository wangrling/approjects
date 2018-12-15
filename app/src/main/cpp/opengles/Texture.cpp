//
// Created by wangrl on 18-12-15.
//

#include "Texture.h"

#include <GLES2/gl2ext.h>
#include <cstdio>
#include <cstdlib>
#include <android/log.h>

#define LOG_TAG "OpenGL"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define TEXTURE_WIDTH   256
#define TEXTURE_HEIGHT  256
#define CHANNELS_PER_PIXEL  3

// 生成一个简单的3x3纹理
GLuint loadSimpleTexture() {
    /* Texture Object Handle. */
    GLuint textureId;

    /** 3 x 3 Image, RGBA Channels RAW Format. */
    GLubyte pixels[9 * 4] =
            {
                    18,  140, 171, 255, /* Some Colour Bottom Left. */
                    143, 143, 143, 255, /* Some Colour Bottom Middle. */
                    255, 255, 255, 255, /* Some Colour Bottom Right. */
                    255, 255, 0,   255, /* Yellow Middle Left. */
                    0,   255, 255, 255, /* Some Colour Middle. */
                    255, 0,   255, 255, /* Some Colour Middle Right. */
                    255, 0,   0,   255, /* Red Top Left. */
                    0,   255, 0,   255, /* Green Top Middle. */
                    0,   0,   255, 255, /* Blue Top Right. */
            };

    /* Use tightly packed data. */
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    // Generate a texture oject.
    glGenTextures(1, &textureId);

    glActiveTexture(GL_TEXTURE0);

    glBindTexture(GL_TEXTURE_2D, textureId);

    /* Load the texture. */
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 3, 3, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

    /* Set the filtering mode. */
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    return textureId;
}

GLubyte * theTexture;

// 文件路径/data/user/0/com.android.approjects/files/normalMap256.raw
GLuint loadTexture() {
    static GLuint textureId;
    theTexture = (GLubyte *)malloc(sizeof(GLubyte) * TEXTURE_WIDTH * TEXTURE_HEIGHT * CHANNELS_PER_PIXEL);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    /* Generate a texture object. */
    glGenTextures(1, &textureId);

    /* Activate a texture. */
    glActiveTexture(GL_TEXTURE0);

    /* Bind the texture object. */
    glBindTexture(GL_TEXTURE_2D, textureId);

    FILE* theFile = fopen("/data/user/0/com.android.approjects/files/normalMap256.raw", "r");

    if(theFile == NULL)
    {
        LOGE("Failure to load the texture");
        return 0;
    }

    fread(theTexture, TEXTURE_WIDTH * TEXTURE_HEIGHT * CHANNELS_PER_PIXEL, 1, theFile);

    /* Load the texture */
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, theTexture);

    /* Set the filtering mode. */
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    free(theTexture);

    return textureId;
}
