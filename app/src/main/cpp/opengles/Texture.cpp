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

/* [loadTexture] */
void loadTexture( const char * texture, unsigned int level, unsigned int width, unsigned int height)
{
    GLubyte * theTexture;
    theTexture = (GLubyte *)malloc(sizeof(GLubyte) * width * height * CHANNELS_PER_PIXEL);

    FILE * theFile = fopen(texture, "r");

    if(theFile == NULL)
    {
        LOGE("Failure to load the texture");
        return;
    }

    fread(theTexture, width * height * CHANNELS_PER_PIXEL, 1, theFile);

    /* Load the texture. */
    glTexImage2D(GL_TEXTURE_2D, level, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, theTexture);

    /* Set the filtering mode. */
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST_MIPMAP_NEAREST);

    free(theTexture);
}
/* [loadTexture] */

/* [loadCompressedTexture] */
void loadCompressedTexture( const char * texture, unsigned int level)
{
    GLushort paddedWidth;
    GLushort paddedHeight;
    GLushort width;
    GLushort height;
    GLubyte textureHead[16];
    GLubyte * theTexture;

    FILE * theFile = fopen(texture, "rb");

    if(theFile == NULL)
    {
        LOGE("Failure to load the texture");
        return;
    }

    fread(textureHead, 16, 1, theFile);

    paddedWidth = (textureHead[8] << 8) | textureHead[9];
    paddedHeight = (textureHead[10] << 8) | textureHead[11];
    width = (textureHead[12] << 8) | textureHead[13];
    height = (textureHead[14] << 8) | textureHead[15];

    theTexture = (GLubyte *)malloc(sizeof(GLubyte) * ((paddedWidth * paddedHeight) >> 1));
    fread(theTexture, (paddedWidth * paddedHeight) >> 1, 1, theFile);

    /* Load the texture. */
    glCompressedTexImage2D(GL_TEXTURE_2D, level, GL_ETC1_RGB8_OES, width, height, 0, (paddedWidth * paddedHeight) >> 1, theTexture);

    /* Set the filtering mode. */
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST_MIPMAP_NEAREST);

    free(theTexture);
    fclose(theFile);
}
/* [loadCompressedTexture] */

namespace GlesSDK {

    void Texture::loadBmpImageData(const char *fileName, int *imageWidthPtr, int *imageHeightPtr,
                                   unsigned char **textureDataPtrPtr) {
        ASSERT(fileName          != NULL,
               "Invalid file name.");
        ASSERT(textureDataPtrPtr != NULL,
               "Cannot use NULL pointer to store image data.");

        tagBITMAPFILEHEADER bitmapFileHeader;
        tagBITMAPINFOHEADER bitmapInfoHeader;
        FILE*               file              = NULL;
        unsigned char*      loadedTexture     = NULL;

        /* Try to open file. */
        file = fopen(fileName, "rb");

        ASSERT(file != NULL, "Failed to open file");

        /* Try to read the bitmap file header. */
        readBitmapFileHeader(file, &bitmapFileHeader);

        /* Try to read the bitmap info header. */
        readBitmapInforHeader(file, &bitmapInfoHeader);

        /* Try to allocate memory to store texture image data. */
        loadedTexture = (unsigned char*) malloc(bitmapInfoHeader.biSizeImage);

        ASSERT(loadedTexture != NULL, "Could not allocate memory to store texture image data.");

        /* Move the file pointer to the begging of the bitmap image data. */
        fseek(file, bitmapFileHeader.bfOffBits, 0);

        /* Read in the image data. */
        fread(loadedTexture, bitmapInfoHeader.biSizeImage, 1, file);

        unsigned char tempElement;

        /* As data in bmp file is stored in BGR, we need to convert it into RGB. */
        for (unsigned int imageIdx  = 0;
             imageIdx  < bitmapInfoHeader.biSizeImage;
             imageIdx += 3)
        {
            tempElement                 = loadedTexture[imageIdx];
            loadedTexture[imageIdx]     = loadedTexture[imageIdx + 2];
            loadedTexture[imageIdx + 2] = tempElement;
        }

        /* At the end, close the file. */
        fclose(file);

        /* Return retrieved data. */
        *textureDataPtrPtr = loadedTexture;

        /* Store the image dimensions if requested. */
        if (imageHeightPtr != NULL)
        {
            *imageHeightPtr = bitmapInfoHeader.biHeight;
        }

        if (imageWidthPtr != NULL)
        {
            *imageWidthPtr = bitmapInfoHeader.biWidth;
        }
    }

    void Texture::readBitmapFileHeader(FILE *filePtr, tagBITMAPFILEHEADER *bitmapFileHeaderPtr) {
        ASSERT(filePtr             != NULL &&
               bitmapFileHeaderPtr != NULL,
               "Invalid arguments used to read bitmap file header.");

        fread(&bitmapFileHeaderPtr->bfType,      sizeof(bitmapFileHeaderPtr->bfType),      1, filePtr);
        fread(&bitmapFileHeaderPtr->bfSize,      sizeof(bitmapFileHeaderPtr->bfSize),      1, filePtr);
        fread(&bitmapFileHeaderPtr->bfReserved1, sizeof(bitmapFileHeaderPtr->bfReserved1), 1, filePtr);
        fread(&bitmapFileHeaderPtr->bfReserved2, sizeof(bitmapFileHeaderPtr->bfReserved2), 1, filePtr);
        fread(&bitmapFileHeaderPtr->bfOffBits,   sizeof(bitmapFileHeaderPtr->bfOffBits),   1, filePtr);

        /* Make sure that file type is valid. */
        ASSERT(bitmapFileHeaderPtr->bfType == 0x4D42,
               "Invalid file type read");
    }

    void Texture::readBitmapInforHeader(FILE *filePtr, tagBITMAPINFOHEADER *bitmapInfoHeaderPtr) {
        ASSERT(filePtr != NULL &&
               bitmapInfoHeaderPtr != NULL,
               "Invalid arguments used to read bitmap info header.");

        fread(&bitmapInfoHeaderPtr->biSize,          sizeof(bitmapInfoHeaderPtr->biSize),          1, filePtr);
        fread(&bitmapInfoHeaderPtr->biWidth,         sizeof(bitmapInfoHeaderPtr->biWidth),         1, filePtr);
        fread(&bitmapInfoHeaderPtr->biHeight,        sizeof(bitmapInfoHeaderPtr->biHeight),        1, filePtr);
        fread(&bitmapInfoHeaderPtr->biPlanes,        sizeof(bitmapInfoHeaderPtr->biPlanes),        1, filePtr);
        fread(&bitmapInfoHeaderPtr->biBitCount,      sizeof(bitmapInfoHeaderPtr->biBitCount),      1, filePtr);
        fread(&bitmapInfoHeaderPtr->biCompression,   sizeof(bitmapInfoHeaderPtr->biCompression),   1, filePtr);
        fread(&bitmapInfoHeaderPtr->biSizeImage,     sizeof(bitmapInfoHeaderPtr->biSizeImage),     1, filePtr);
        fread(&bitmapInfoHeaderPtr->biXPelsPerMeter, sizeof(bitmapInfoHeaderPtr->biXPelsPerMeter), 1, filePtr);
        fread(&bitmapInfoHeaderPtr->biYPelsPerMeter, sizeof(bitmapInfoHeaderPtr->biYPelsPerMeter), 1, filePtr);
        fread(&bitmapInfoHeaderPtr->biClrUsed,       sizeof(bitmapInfoHeaderPtr->biClrUsed),       1, filePtr);
        fread(&bitmapInfoHeaderPtr->biClrImportant,  sizeof(bitmapInfoHeaderPtr->biClrImportant),  1, filePtr);
    }
}
