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

#endif //APPROJECTS_TEXTURE_H
