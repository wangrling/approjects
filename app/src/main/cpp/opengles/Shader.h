//
// Created by wangrl on 18-12-16.
//

#ifndef APPROJECTS_SHADER_H
#define APPROJECTS_SHADER_H

#include <GLES3/gl3.h>

namespace GlesSDK {
    /**
    * \brief Functions for working with OpenGL ES shaders.
    */
    class Shader
    {
    private:
        /**
        * \brief Load shader source from a file into memory.
        *
        * \param filename File name of the shader to load.
        *
        * \return A character array containing the contents of the shader source file.
        */
        static char *loadShader(const char *filename);

    public:
        /**
        * \brief Create shader, load in source, compile, and dump debug as necessary.
        *
        *  \note Loads the OpenGL ES Shading Language code into memory.
        *        Creates a shader using with the required shaderType using glCreateShader(shaderType) and then compiles it using glCompileShader.
        *        The output from the compilation is checked for success and a log of the compilation errors is printed in the case of failure.
        *
        * \param shaderObjectIdPtr Deref will be used to store generated shader object ID.
        *                          Cannot be NULL.
        * \param filename          Name of a file containing OpenGL ES SL source code.
        * \param shaderType        Passed to glCreateShader to define the type of shader being processed.
        *                          Must be GL_VERTEX_SHADER or GL_FRAGMENT_SHADER.
        */
        static void processShader(GLuint *shaderObjectIdPtr, const char *filename, GLint shaderType);
    };
}

#endif //APPROJECTS_SHADER_H
