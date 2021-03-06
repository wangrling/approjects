//
// Created by wangrl on 18-12-16.
//
#include "Common.h"
#include <cstdio>
#include "Shader.h"

namespace GlesSDK {

    char *Shader::loadShader(const char *filename) {
        FILE *file = fopen(filename, "rb");
        ASSERT(file != NULL, "Cannot read shader file.");

        /* Seek end of file. */
        fseek(file, 0, SEEK_END);

        /* Record the size of the file for memory allocation. */
        long length = ftell(file);

        /* Seek start of file again. */
        fseek(file, 0, SEEK_SET);

        char *shader = (char *)calloc(length + 1, sizeof(char));

        ASSERT(shader != NULL, "Cannot allocate memory for shader source.");

        /* Read in the file */
        size_t numberOfBytesRead = fread(shader, sizeof(char), length, file);

        ASSERT(numberOfBytesRead == length, "An error ocurred while reading shader source.");

        shader[length] = '\0';

        fclose(file);

        return shader;
    }

    void Shader::processShader(GLuint *shaderObjectIdPtr, const char *filename, GLint shaderType) {
        ASSERT(shaderObjectIdPtr != NULL,
               "NULL pointer used to store generated shader object ID.");

        ASSERT(shaderType == GL_FRAGMENT_SHADER || shaderType == GL_VERTEX_SHADER,
               "Invalid shader object type.");

        GLint       compileStatus = GL_FALSE;
        const char *strings[1]    = { NULL };

        /* Create shader and load into GL. */
        *shaderObjectIdPtr = GL_CHECK(glCreateShader(shaderType));
        strings[0]         = loadShader(filename);

        GL_CHECK(glShaderSource(*shaderObjectIdPtr, 1, strings, NULL));

        /* Clean up shader source. */
        free((void *)(strings[0]));
        strings[0] = NULL;

        /* Try compiling the shader. */


        GL_CHECK(glCompileShader(*shaderObjectIdPtr));

        GL_CHECK(glGetShaderiv(*shaderObjectIdPtr, GL_COMPILE_STATUS, &compileStatus));

        /* Dump debug info (source and log) if compilation failed. */
        if (compileStatus != GL_TRUE)
        {
            char *debugSource = NULL;
            char *errorLog    = NULL;
            GLint length;

            /* Get shader source. */
            GL_CHECK(glGetShaderiv(*shaderObjectIdPtr, GL_SHADER_SOURCE_LENGTH, &length));

            debugSource = (char*) malloc(length);

            if (debugSource != NULL)
            {
                GL_CHECK(glGetShaderSource(*shaderObjectIdPtr, length, NULL, debugSource));

                LOGE("Debug source START:\n%s\nDebug source END\n\n", debugSource);

                free(debugSource);

                debugSource = NULL;
            }

            /* Now get the info log. */
            GL_CHECK(glGetShaderiv(*shaderObjectIdPtr, GL_INFO_LOG_LENGTH, &length));

            errorLog = (char*) malloc(length);

            if (errorLog != NULL)
            {
                GL_CHECK(glGetShaderInfoLog(*shaderObjectIdPtr, length, NULL, errorLog));

                LOGE("Log START:\n%s\nLog END\n\n", errorLog);

                free(errorLog);

                errorLog = NULL;
            }
        }

        ASSERT(compileStatus == GL_TRUE, "Shader compilation FAILED!");
    }
}
