//
// Created by wangrl on 18-12-15.
//

#ifndef APPROJECTS_TEXTURE_H
#define APPROJECTS_TEXTURE_H

#include <GLES2/gl2.h>
#include "Common.h"

#include <cstdio>
#include <cstdlib>

/**
 * \brief Loads a simple 3 x 3 static texture into OpenGL ES.
 * \return Returns the handle to the texture object.
 */
 GLuint loadSimpleTexture();

/**
* \brief Loads a 256 x 256 texture from the Android filesystem
* \return Returns the handle to the texture object.
*/
GLuint loadTexture();

/**
 * \brief Loads a desired texture into memory at an appropriate mipmap level.
 * \param texture The name of the texture file to be loaded from the system.
 * \param level The mipmap level that the texture should be loaded into.
 * \param width The width of the texture to be loaded
 * \param height The height of the texture to be loaded.
 */
void loadTexture( const char * texture, unsigned int level, unsigned int width, unsigned int height);

/**
 * \brief Loads a compressed texture into memory at an appropriate mipmap level.
 * \param texture The name of the texture file to be loeaded from the system.
 * \param level The mipmap level that the texture should be loaded into.
 */
void loadCompressedTexture( const char * texture, unsigned int level);

namespace GlesSDK {
    struct tagBITMAPFILEHEADER
    {
        short bfType;
        int   bfSize;
        short bfReserved1;
        short bfReserved2;
        int   bfOffBits;
    };

    struct tagBITMAPINFOHEADER
    {
        int   biSize;
        int   biWidth;
        int   biHeight;
        short biPlanes;
        short biBitCount;
        int   biCompression;
        int   biSizeImage;
        int   biXPelsPerMeter;
        int   biYPelsPerMeter;
        int   biClrUsed;
        int   biClrImportant;
    };

    /**
     * \brief Functions for working with textures.
     */
    class Texture
    {
    private:
        /**
         * \brief Read BMP file header.
         *
         * \param filePtr             File pointer where BMP file header data is stored.
         *                            Cannot be NULL.
         * \param bitmapFileHeaderPtr Deref will be used to store loaded data.
         *                            Cannot be NULL.
         */
        static void readBitmapFileHeader(FILE* filePtr, tagBITMAPFILEHEADER* bitmapFileHeaderPtr);
        /**
         * \brief Read BMP info header.
         *
         * \param filePtr             File pointer where BMP info header data is stored.
         *                            Cannot be NULL.
         * \param bitmapInfoHeaderPtr Deref will be used to store loaded data.
         *                            Cannot be NULL.
         */
        static void readBitmapInforHeader(FILE* filePtr, tagBITMAPINFOHEADER* bitmapInfoHeaderPtr);

    public:
        /**
         * \brief Load BMP texture data from a file into memory.
         *
         * \param fileName          The filename of the texture to be loaded.
         *                          Cannot be NULL.
         * \param imageWidthPtr     Deref will be used to store image width.
         * \param imageHeightPtr    Deref will be used to store image height.
         * \param textureDataPtrPtr Pointer to a memory where loaded texture data will be stored.
         *                          Cannot be NULL.
         */
        static void loadBmpImageData(const char*     fileName,
                                     int*            imageWidthPtr,
                                     int*            imageHeightPtr,
                                     unsigned char** textureDataPtrPtr);
    };
}

#endif //APPROJECTS_TEXTURE_H
