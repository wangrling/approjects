//
// Created by wangrl on 18-12-15.
//

#ifndef APPROJECTS_TEXTURE_H
#define APPROJECTS_TEXTURE_H

#include <GLES2/gl2.h>

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

#endif //APPROJECTS_TEXTURE_H
