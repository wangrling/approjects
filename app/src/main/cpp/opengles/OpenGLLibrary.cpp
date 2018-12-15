//
// Created by wangrl on 18-12-15.
//

#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <stdlib.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "Matrix.h"
#include "Texture.h"

#define LOG_TAG "OpenGL"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


GLuint loadShader(GLenum shaderType, const char* shaderSource)
{
    GLuint shader = glCreateShader(shaderType);
    if (shader)
    {
        glShaderSource(shader, 1, &shaderSource, NULL);
        glCompileShader(shader);

        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);

        if (!compiled)
        {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);

            if (infoLen)
            {
                char * buf = (char*) malloc(infoLen);

                if (buf)
                {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not Compile Shader %d:\n%s\n", shaderType, buf);
                    free(buf);
                }

                glDeleteShader(shader);
                shader = 0;
            }
        }
    }

    return shader;
}

GLuint createProgram(const char* vertexSource, const char* fragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource);
    if (!vertexShader)
    {
        return 0;
    }

    GLuint fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (!fragmentShader)
    {
        return 0;
    }

    GLuint program = glCreateProgram();

    if (program)
    {
        glAttachShader(program , vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;

        glGetProgramiv(program , GL_LINK_STATUS, &linkStatus);

        if( linkStatus != GL_TRUE)
        {
            GLint bufLength = 0;

            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);

            if (bufLength)
            {
                char* buf = (char*) malloc(bufLength);

                if (buf)
                {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOGE("Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }

    return program;
}

// Graphics Setup
extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_graphicssetup_OpenGLLibrary_init(JNIEnv *env, jclass type,
        jint width, jint height) {

    // TODO
    LOGI("Hello From the Native Side!!");


}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_graphicssetup_OpenGLLibrary_step(JNIEnv *env, jclass type) {

    // TODO
    /* Sleeping to avoid thrashing the Android log. */
    sleep(5);
    LOGI("New Frame Ready to be Drawn!!!!");
}

namespace SimpleCube {
    // 作用于每个点
    static const char glVertexShader[] = {
            "attribute vec4 vertexPosition;                 \n"
            "attribute vec3 vertexColour;                   \n"
            "varying vec3 fragColour;                       \n"
            "uniform mat4 projection;                       \n"
            "uniform mat4 modelView;                        \n"
            "void main() {                                  \n"
            "   gl_Position = projection * modelView * vertexPosition;  \n"
            "   fragColour = vertexColour;                              \n"
            "}                                                          \n"
    };
    static const char glFragmentShader[] = {
            "precision mediump float;                       \n"
            "varying vec3 fragColour;                       \n"
            "void main() {                                  \n"
            "   gl_FragColor = vec4(fragColour, 1.0);       \n"
            "}                                              \n"
    };

    GLuint simpleCubeProgram;
    GLuint vertexLocation;
    GLuint vertexColourLocation;
    GLuint projectionLocation;
    GLuint modelViewLocation;

    float projectionMatrix[16];
    float modelViewMatrix[16];
    float angle = 0;

    bool setupGraphics(int width, int height) {
        simpleCubeProgram = createProgram(glVertexShader, glFragmentShader);

        if (simpleCubeProgram == 0)
        {
            LOGE ("Could not create program");
            return false;
        }

        vertexLocation = glGetAttribLocation(simpleCubeProgram, "vertexPosition");
        vertexColourLocation = glGetAttribLocation(simpleCubeProgram, "vertexColour");
        projectionLocation = glGetUniformLocation(simpleCubeProgram, "projection");
        modelViewLocation = glGetUniformLocation(simpleCubeProgram, "modelView");

        matrixPerspective(projectionMatrix, 45, (float)width/ float(height), 0.1f, 100);
        glEnable(GL_DEPTH_TEST);

        glViewport(0, 0, width, height);

        return true;
    }

    GLfloat cubeVertices[] = {-1.0f,  1.0f, -1.0f, /* Back. */
                              1.0f,  1.0f, -1.0f,
                              -1.0f, -1.0f, -1.0f,
                              1.0f, -1.0f, -1.0f,
                              -1.0f,  1.0f,  1.0f, /* Front. */
                              1.0f,  1.0f,  1.0f,
                              -1.0f, -1.0f,  1.0f,
                              1.0f, -1.0f,  1.0f,
                              -1.0f,  1.0f, -1.0f, /* Left. */
                              -1.0f, -1.0f, -1.0f,
                              -1.0f, -1.0f,  1.0f,
                              -1.0f,  1.0f,  1.0f,
                              1.0f,  1.0f, -1.0f, /* Right. */
                              1.0f, -1.0f, -1.0f,
                              1.0f, -1.0f,  1.0f,
                              1.0f,  1.0f,  1.0f,
                              -1.0f, -1.0f, -1.0f, /* Top. */
                              -1.0f, -1.0f,  1.0f,
                              1.0f, -1.0f,  1.0f,
                              1.0f, -1.0f, -1.0f,
                              -1.0f,  1.0f, -1.0f, /* Bottom. */
                              -1.0f,  1.0f,  1.0f,
                              1.0f,  1.0f,  1.0f,
                              1.0f,  1.0f, -1.0f
    };

    GLfloat colour[] = {1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 1.0f
    };

    GLushort indices[] = {0, 2, 3, 0, 1, 3, 4, 6, 7, 4, 5, 7, 8, 9, 10, 11, 8, 10, 12, 13, 14, 15, 12, 14, 16, 17, 18, 16, 19, 18, 20, 21, 22, 20, 23, 22};

    void renderFrame() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        matrixIdentityFunction(modelViewMatrix);

        matrixRotateX(modelViewMatrix, angle);
        matrixRotateY(modelViewMatrix, angle);

        // Z轴正向指向观察者。
        matrixTranslate(modelViewMatrix, 0, 0, -10.0f);

        glUseProgram(simpleCubeProgram);
        glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, GL_FALSE, 0, cubeVertices);
        glEnableVertexAttribArray(vertexLocation);

        glVertexAttribPointer(vertexColourLocation, 3, GL_FLOAT, GL_FALSE, 0, colour);
        glEnableVertexAttribArray(vertexColourLocation);

        glUniformMatrix4fv(projectionLocation, 1, GL_FALSE, projectionMatrix);
        glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);

        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, indices);

        angle += 1;

        if (angle > 360)
            angle -= 360;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_simplecube_GraphicsView_init(JNIEnv *env, jclass type,
                                                                  jint width, jint height) {

    // TODO
    SimpleCube::setupGraphics(width, height);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_simplecube_GraphicsView_step(JNIEnv *env, jclass type) {

    // TODO
    SimpleCube::renderFrame();
}


namespace TextureCube {
    static const char glVertexShader[] = {
            "attribute vec4 vertexPosition;\n"
            "attribute vec2 vertexTextureCord;\n"
            "varying vec2 textureCord;\n"
            "uniform mat4 projection;\n"
            "uniform mat4 modelView;\n"
            "void main()\n"
            "{\n"
            "    gl_Position = projection * modelView * vertexPosition;\n"
            "    textureCord = vertexTextureCord;\n"
            "}\n"
    };

    static const char glFragmentShader[] = {
            "precision mediump float;\n"
            "uniform sampler2D texture;\n"
            "varying vec2 textureCord;\n"
            "void main()\n"
            "{\n"
            "    gl_FragColor = texture2D(texture, textureCord);\n"
            "}\n"
    };

    GLuint glProgram;
    GLuint vertexLocation;
    GLuint samplerLocation;
    GLuint projectionLocation;
    GLuint modelViewLocation;
    GLuint textureCordLocation;
    GLuint textureId;

    float projectionMatrix[16];
    float modelViewMatrix[16];
    float angle = 0;

    bool setupGraphics(int width, int height) {
        glProgram = createProgram(glVertexShader, glFragmentShader);

        if (!glProgram)
        {
            LOGE ("Could not create program");
            return false;
        }

        vertexLocation = glGetAttribLocation(glProgram, "vertexPosition");
        textureCordLocation = glGetAttribLocation(glProgram, "vertexTextureCord");
        projectionLocation = glGetUniformLocation(glProgram, "projection");
        modelViewLocation = glGetUniformLocation(glProgram, "modelView");
        samplerLocation = glGetUniformLocation(glProgram, "texture");

        /* Setup the perspective. */
        matrixPerspective(projectionMatrix, 45, (float)width / (float)height, 0.1f, 100);
        glEnable(GL_DEPTH_TEST);

        glViewport(0, 0, width, height);

        /* load the texture. */
        textureId = loadSimpleTexture();
        if (textureId == 0) {
            return false;
        } else
            return true;
    }

    GLfloat textureCords[] = { 1.0f, 1.0f, /* Back. */
                               0.0f, 1.0f,
                               1.0f, 0.0f,
                               0.0f, 0.0f,
                               0.0f, 1.0f, /* Front. */
                               1.0f, 1.0f,
                               0.0f, 0.0f,
                               1.0f, 0.0f,
                               0.0f, 1.0f, /* Left. */
                               0.0f, 0.0f,
                               1.0f, 0.0f,
                               1.0f, 1.0f,
                               1.0f, 1.0f, /* Right. */
                               1.0f, 0.0f,
                               0.0f, 0.0f,
                               0.0f, 1.0f,
                               0.0f, 1.0f, /* Top. */
                               0.0f, 0.0f,
                               1.0f, 0.0f,
                               1.0f, 1.0f,
                               0.0f, 0.0f, /* Bottom. */
                               0.0f, 1.0f,
                               1.0f, 1.0f,
                               1.0f, 0.0f
    };

    void renderFrame() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear (GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        matrixIdentityFunction(modelViewMatrix);

        matrixRotateX(modelViewMatrix, angle);
        matrixRotateY(modelViewMatrix, angle);

        matrixTranslate(modelViewMatrix, 0.0f, 0.0f, -10.0f);

        glUseProgram(glProgram);
        glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, GL_FALSE, 0, SimpleCube::cubeVertices);
        glEnableVertexAttribArray(vertexLocation);

        // EnableAttributes
        glVertexAttribPointer(textureCordLocation, 2, GL_FLOAT, GL_FALSE, 0, textureCords);
        glEnableVertexAttribArray(textureCordLocation);
        glUniformMatrix4fv(projectionLocation, 1, GL_FALSE,projectionMatrix);
        glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);

        /* Set the sampler texture unit to 0. */
        glUniform1i(samplerLocation, 0);

        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, SimpleCube::indices);

        angle += 1;
        if (angle > 360)
        {
            angle -= 360;
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_TextureCubeActivity_init(JNIEnv *env, jclass type, jint width,
                                                              jint height) {

    // TODO
    TextureCube::setupGraphics(width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_TextureCubeActivity_step(JNIEnv *env, jclass type) {

    // TODO
    TextureCube::renderFrame();
}

namespace Lighting {
    static const char  glVertexShader[] = {
            "attribute vec4 vertexPosition;\n"
            "attribute vec3 vertexColour;\n"
            /* [Add a vertex normal attribute.] */
            "attribute vec3 vertexNormal;\n"
            /* [Add a vertex normal attribute.] */
            "varying vec3 fragColour;\n"
            "uniform mat4 projection;\n"
            "uniform mat4 modelView;\n"
            "void main()\n"
            "{\n"
            /* [Setup scene vectors.] */
            "    vec3 transformedVertexNormal = normalize((modelView * vec4(vertexNormal, 0.0)).xyz);"
            "    vec3 inverseLightDirection = normalize(vec3(0.0, 1.0, 1.0));\n"
            "    fragColour = vec3(0.0);\n"
            /* [Setup scene vectors.] */
            "\n"
            /* [Calculate the diffuse component.] */
            "    vec3 diffuseLightIntensity = vec3(1.0, 1.0, 1.0);\n"
            "    vec3 vertexDiffuseReflectionConstant = vertexColour;\n"
            "    float normalDotLight = max(0.0, dot(transformedVertexNormal, inverseLightDirection));\n"
            "    fragColour += normalDotLight * vertexDiffuseReflectionConstant * diffuseLightIntensity;\n"
            /* [Calculate the diffuse component.] */
            "\n"
            /* [Calculate the ambient component.] */
            "    vec3 ambientLightIntensity = vec3(0.1, 0.1, 0.1);\n"
            "    vec3 vertexAmbientReflectionConstant = vertexColour;\n"
            "    fragColour += vertexAmbientReflectionConstant * ambientLightIntensity;\n"
            /* [Calculate the ambient component.] */
            "\n"
            /* [Calculate the specular component.] */
            "    vec3 inverseEyeDirection = normalize(vec3(0.0, 0.0, 1.0));\n"
            "    vec3 specularLightIntensity = vec3(1.0, 1.0, 1.0);\n"
            "    vec3 vertexSpecularReflectionConstant = vec3(1.0, 1.0, 1.0);\n"
            "    float shininess = 2.0;\n"
            "    vec3 lightReflectionDirection = reflect(vec3(0) - inverseLightDirection, transformedVertexNormal);\n"
            "    float normalDotReflection = max(0.0, dot(inverseEyeDirection, lightReflectionDirection));\n"
            "    fragColour += pow(normalDotReflection, shininess) * vertexSpecularReflectionConstant * specularLightIntensity;\n"
            /* [Calculate the specular component.] */
            "\n"
            "    /* Make sure the fragment colour is between 0 and 1. */"
            "    clamp(fragColour, 0.0, 1.0);\n"
            "\n"
            "    gl_Position = projection * modelView * vertexPosition;\n"
            "}\n"
    };

    static const char  glFragmentShader[] =
            "precision mediump float;\n"
            "varying vec3 fragColour;\n"
            "void main()\n"
            "{\n"
            "    gl_FragColor = vec4(fragColour, 1.0);\n"
            "}\n";

    GLuint lightingProgram;
    GLuint vertexLocation;
    GLuint vertexColourLocation;
    /* [Global variable to hold vertex normal attribute location.] */
    GLuint vertexNormalLocation;
    /* [Global variable to hold vertex normal attribute location.] */
    GLuint projectionLocation;
    GLuint modelViewLocation;

    float projectionMatrix[16];
    float modelViewMatrix[16];
    float angle = 0;

    bool setupGraphics(int width, int height) {
        lightingProgram = createProgram(glVertexShader, glFragmentShader);

        if (lightingProgram == 0)
        {
            LOGE ("Could not create program");
            return false;
        }

        vertexLocation = glGetAttribLocation(lightingProgram, "vertexPosition");
        vertexColourLocation = glGetAttribLocation(lightingProgram, "vertexColour");
        /* [Get vertex normal attribute location.] */
        vertexNormalLocation = glGetAttribLocation(lightingProgram, "vertexNormal");
        /* [Get vertex normal attribute location.] */
        projectionLocation = glGetUniformLocation(lightingProgram, "projection");
        modelViewLocation = glGetUniformLocation(lightingProgram, "modelView");

        /* Setup the perspective */
        matrixPerspective(projectionMatrix, 45, (float)width / (float)height, 0.1f, 100);
        glEnable(GL_DEPTH_TEST);

        glViewport(0, 0, width, height);

        return true;
    }

    GLfloat normals[] = { 1.0f,  1.0f, -1.0f, /* Back. */
                          -1.0f,  1.0f, -1.0f,
                          1.0f, -1.0f, -1.0f,
                          -1.0f, -1.0f, -1.0f,
                          0.0f,  0.0f, -1.0f,
                          -1.0f,  1.0f,  1.0f, /* Front. */
                          1.0f,  1.0f,  1.0f,
                          -1.0f, -1.0f,  1.0f,
                          1.0f, -1.0f,  1.0f,
                          0.0f,  0.0f,  1.0f,
                          -1.0f,  1.0f, -1.0f, /* Left. */
                          -1.0f,  1.0f,  1.0f,
                          -1.0f, -1.0f, -1.0f,
                          -1.0f, -1.0f,  1.0f,
                          -1.0f,  0.0f,  0.0f,
                          1.0f,  1.0f,  1.0f, /* Right. */
                          1.0f,  1.0f, -1.0f,
                          1.0f, -1.0f,  1.0f,
                          1.0f, -1.0f, -1.0f,
                          1.0f,  0.0f,  0.0f,
                          -1.0f, -1.0f,  1.0f, /* Bottom. */
                          1.0f, -1.0f,  1.0f,
                          -1.0f, -1.0f, -1.0f,
                          1.0f, -1.0f, -1.0f,
                          0.0f, -1.0f,  0.0f,
                          -1.0f,  1.0f, -1.0f, /* Top. */
                          1.0f,  1.0f, -1.0f,
                          -1.0f,  1.0f,  1.0f,
                          1.0f,  1.0f,  1.0f,
                          0.0f,  1.0f,  0.0f
    };

    GLfloat verticies[] = { 1.0f,  1.0f, -1.0f, /* Back. */
                            -1.0f,  1.0f, -1.0f,
                            1.0f, -1.0f, -1.0f,
                            -1.0f, -1.0f, -1.0f,
                            0.0f,  0.0f, -2.0f,
                            -1.0f,  1.0f,  1.0f, /* Front. */
                            1.0f,  1.0f,  1.0f,
                            -1.0f, -1.0f,  1.0f,
                            1.0f, -1.0f,  1.0f,
                            0.0f,  0.0f,  2.0f,
                            -1.0f,  1.0f, -1.0f, /* Left. */
                            -1.0f,  1.0f,  1.0f,
                            -1.0f, -1.0f, -1.0f,
                            -1.0f, -1.0f,  1.0f,
                            -2.0f,  0.0f,  0.0f,
                            1.0f,  1.0f,  1.0f, /* Right. */
                            1.0f,  1.0f, -1.0f,
                            1.0f, -1.0f,  1.0f,
                            1.0f, -1.0f, -1.0f,
                            2.0f,  0.0f,  0.0f,
                            -1.0f, -1.0f,  1.0f, /* Bottom. */
                            1.0f, -1.0f,  1.0f,
                            -1.0f, -1.0f, -1.0f,
                            1.0f, -1.0f, -1.0f,
                            0.0f, -2.0f,  0.0f,
                            -1.0f,  1.0f, -1.0f, /* Top. */
                            1.0f,  1.0f, -1.0f,
                            -1.0f,  1.0f,  1.0f,
                            1.0f,  1.0f,  1.0f,
                            0.0f,  2.0f,  0.0f
    };

    GLfloat colour[] = {1.0f, 0.0f, 0.0f, /* Back. */
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, /* Front. */
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 1.0f, /* Left. */
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, /* Right. */
                        1.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 0.0f,
                        1.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 1.0f, /* Bottom. */
                        0.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f,
                        0.0f, 1.0f, 1.0f,
                        1.0f, 0.0f, 1.0f, /* Top. */
                        1.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 1.0f
    };

    GLushort indices[] = {0,  2,  4,  0,  4,  1,  1,  4,  3,  2,  3,  4,  /* Back. */
                          5,  7,  9,  5,  9,  6,  6,  9,  8,  7,  8,  9,  /* Front. */
                          10, 12, 14, 10, 14, 11, 11, 14, 13, 12, 13, 14, /* Left. */
                          15, 17, 19, 15, 19, 16, 16, 19, 18, 17, 18, 19, /* Right. */
                          20, 22, 24, 20, 24, 21, 21, 24, 23, 22, 23, 24, /* Bottom. */
                          25, 27, 29, 25, 29, 26, 26, 29, 28, 27, 28, 29  /* Top. */
    };

    void renderFrame() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        matrixIdentityFunction(modelViewMatrix);

        matrixRotateX(modelViewMatrix, angle);
        matrixRotateY(modelViewMatrix, angle);

        matrixTranslate(modelViewMatrix, 0.0f, 0.0f, -10.0f);

        glUseProgram(lightingProgram);
        glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, GL_FALSE, 0, verticies);
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(vertexColourLocation, 3, GL_FLOAT, GL_FALSE, 0, colour);
        glEnableVertexAttribArray(vertexColourLocation);

        // Upload vertex normals.
        glVertexAttribPointer(vertexNormalLocation, 3, GL_FLOAT, GL_FALSE, 0, normals);
        glEnableVertexAttribArray(vertexNormalLocation);

        glUniformMatrix4fv(projectionLocation, 1, GL_FALSE, projectionMatrix);
        glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);

        /* [Draw the object.] */
        glDrawElements(GL_TRIANGLES, 72, GL_UNSIGNED_SHORT, indices);
        /* [Draw the object.] */

        angle += 1;
        if (angle > 360)
        {
            angle -= 360;
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_LightingActivity_init(JNIEnv *env, jclass type, jint width,
                                                           jint height) {

    // TODO
    Lighting::setupGraphics(width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_LightingActivity_step(JNIEnv *env, jclass type) {

    // TODO
    Lighting::renderFrame();
}

namespace NormalMapping {

    /* [vertexShader] */
    static const char glVertexShader[] =
            "attribute vec4 vertexPosition;\n"
            "attribute vec2 vertexTextureCord;\n"
            "attribute vec3 vertexNormal;\n"
            "attribute vec3 vertexColor; \n"
            "attribute vec3 vertexTangent;\n"
            "attribute vec3 vertexBiNormal;\n"
            "varying vec2 textureCord;\n"
            "varying vec3 varyingColor; \n"
            "varying vec3 inverseLightDirection;\n"
            "varying vec3 inverseEyeDirection;\n"
            "uniform mat4 projection;\n"
            "uniform mat4 modelView;\n"
            "void main()\n"
            "{\n"
            "   vec3 worldSpaceVertex =(modelView * vertexPosition).xyz;"
            "   vec3 transformedVertexNormal = normalize((modelView *  vec4(vertexNormal, 0.0)).xyz);"

            "   inverseLightDirection = normalize(vec3(0.0, 0.0, 1.0));\n"
            "   inverseEyeDirection = normalize((vec3(0.0, 0.0, 1.0)- worldSpaceVertex ).xyz);\n"

            "   gl_Position = projection * modelView * vertexPosition;\n"
            "   textureCord = vertexTextureCord;\n"
            "   varyingColor = vertexColor;\n"

            "   vec3 transformedTangent = normalize((modelView * vec4(vertexTangent, 0.0)).xyz);\n"
            "   vec3 transformedBinormal = normalize((modelView * vec4(vertexBiNormal, 0.0)).xyz);\n"
            "   mat3 tangentMatrix = mat3(transformedTangent, transformedBinormal, transformedVertexNormal);\n"
            "   inverseLightDirection =inverseLightDirection * tangentMatrix;\n"
            "   inverseEyeDirection = inverseEyeDirection * tangentMatrix;\n"
            "}\n";
/* [vertexShader] */
/* [fragmentShader] */
    static const char glFragmentShader[] =
            "precision mediump float;\n"
            "uniform sampler2D texture;\n"
            "varying vec2 textureCord;\n"
            "varying vec3 varyingColor;\n"
            "varying vec3 inverseLightDirection;\n"
            "varying vec3 inverseEyeDirection;\n"
            "varying vec3 transformedVertexNormal;\n"
            "void main()\n"
            "{\n"
            "   vec3 fragColor = vec3(0.0,0.0,0.0); \n"
            "   vec3 normal = texture2D(texture, textureCord).xyz;"
            "   normal = normalize(normal * 2.0 -1.0);"
            /* Calculate the diffuse component. */
            "   vec3 diffuseLightIntensity = vec3(1.0, 1.0, 1.0);\n"
            "   float normalDotLight = max(0.0, dot(normal, inverseLightDirection));\n"
            "   fragColor += normalDotLight * varyingColor *diffuseLightIntensity;\n"
            /* Calculate the ambient component. */
            "   vec3 ambientLightIntensity = vec3(0.1, 0.1, 0.1);\n"
            "   fragColor +=  ambientLightIntensity * varyingColor;\n"
            /* Calculate the specular component. */
            "   vec3 specularLightIntensity = vec3(1.0, 1.0, 1.0);\n"
            "   vec3 vertexSpecularReflectionConstant = vec3(1.0, 1.0, 1.0);\n"
            "   float shininess = 2.0;\n"
            "   vec3 lightReflectionDirection = reflect(vec3(0) - inverseLightDirection, normal);\n"
            "   float normalDotReflection = max(0.0, dot(inverseEyeDirection, lightReflectionDirection));\n"
            "   fragColor += pow(normalDotReflection, shininess) * vertexSpecularReflectionConstant * specularLightIntensity;\n"
            "   /* Make sure the fragment colour is between 0 and 1. */"
            "   clamp(fragColor, 0.0, 1.0);\n"
            "   gl_FragColor = vec4(fragColor,1.0);\n"
            "}\n";
/* [fragmentShader] */

    GLuint glProgram;

/* [LocationVariables] */
    GLuint vertexLocation;
    GLuint samplerLocation;
    GLuint projectionLocation;
    GLuint modelViewLocation;
    GLuint textureCordLocation;
    GLuint colorLocation;
    GLuint textureId;
    GLuint vertexNormalLocation;
    GLuint tangentLocation;
    GLuint biNormalLocation;
/* [LocationVariables] */

    float projectionMatrix[16];
    float modelViewMatrix[16];
    float angle = 0;

    bool setupGraphics(jint width, jint height) {
        glProgram = createProgram(glVertexShader, glFragmentShader);

        if (!glProgram)
        {
            LOGE ("Could not create program");
            return false;
        }

        /* [setLocation] */
        vertexLocation = glGetAttribLocation(glProgram, "vertexPosition");
        textureCordLocation = glGetAttribLocation(glProgram, "vertexTextureCord");
        projectionLocation = glGetUniformLocation(glProgram, "projection");
        modelViewLocation = glGetUniformLocation(glProgram, "modelView");
        samplerLocation = glGetUniformLocation(glProgram, "texture");
        vertexNormalLocation = glGetAttribLocation(glProgram, "vertexNormal");
        colorLocation = glGetAttribLocation(glProgram, "vertexColor");
        tangentLocation = glGetAttribLocation(glProgram, "vertexTangent");
        biNormalLocation = glGetAttribLocation(glProgram, "vertexBiNormal");
        /* [setLocation] */

        /* Setup the perspective. */
        matrixPerspective(projectionMatrix, 45, (float)width / (float)height, 0.1f, 100);
        glEnable(GL_DEPTH_TEST);

        glViewport(0, 0, width, height);

        /* Load the Texture. */
        textureId = loadTexture();
        if(textureId == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /* [vertexColourTangentNormal] */
    GLfloat cubeVertices[] = {-1.0f,  1.0f, -1.0f, /* Back. */
                              1.0f,  1.0f, -1.0f,
                              -1.0f, -1.0f, -1.0f,
                              1.0f, -1.0f, -1.0f,
                              -1.0f,  1.0f,  1.0f, /* Front. */
                              1.0f,  1.0f,  1.0f,
                              -1.0f, -1.0f,  1.0f,
                              1.0f, -1.0f,  1.0f,
                              -1.0f,  1.0f, -1.0f, /* Left. */
                              -1.0f, -1.0f, -1.0f,
                              -1.0f, -1.0f,  1.0f,
                              -1.0f,  1.0f,  1.0f,
                              1.0f,  1.0f, -1.0f, /* Right. */
                              1.0f, -1.0f, -1.0f,
                              1.0f, -1.0f,  1.0f,
                              1.0f,  1.0f,  1.0f,
                              -1.0f, 1.0f, -1.0f, /* Top. */
                              -1.0f, 1.0f,  1.0f,
                              1.0f, 1.0f,  1.0f,
                              1.0f, 1.0f, -1.0f,
                              -1.0f, - 1.0f, -1.0f, /* Bottom. */
                              -1.0f,  -1.0f,  1.0f,
                              1.0f, - 1.0f,  1.0f,
                              1.0f,  -1.0f, -1.0f
    };

    GLfloat normals[] =     {0.0f, 0.0f, -1.0f,            /* Back */
                             0.0f, 0.0f, -1.0f,
                             0.0f, 0.0f, -1.0f,
                             0.0f, 0.0f, -1.0f,
                             0.0f, 0.0f, 1.0f,            /* Front */
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, 1.0f,
                             -1.0f, 0.0, 0.0f,            /* Left */
                             -1.0f, 0.0f, 0.0f,
                             -1.0f, 0.0f, 0.0f,
                             -1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,             /* Right */
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             0.0f, 1.0f, 0.0f,             /* Top */
                             0.0f, 1.0f, 0.0f,
                             0.0f, 1.0f, 0.0f,
                             0.0f, 1.0f, 0.0f,
                             0.0f, -1.0f, 0.0f,            /* Bottom */
                             0.0f, -1.0f, 0.0f,
                             0.0f, -1.0f, 0.0f,
                             0.0f, -1.0f, 0.0f
    };

    GLfloat colour[] =      {1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             0.0f, 1.0f, 0.0f,
                             0.0f, 1.0f, 0.0f,
                             0.0f, 1.0f, 0.0f,
                             0.0f, 1.0f, 0.0f,
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, 1.0f,
                             1.0f, 1.0f, 0.0f,
                             1.0f, 1.0f, 0.0f,
                             1.0f, 1.0f, 0.0f,
                             1.0f, 1.0f, 0.0f,
                             0.0f, 1.0f, 1.0f,
                             0.0f, 1.0f, 1.0f,
                             0.0f, 1.0f, 1.0f,
                             0.0f, 1.0f, 1.0f,
                             1.0f, 0.0f, 1.0f,
                             1.0f, 0.0f, 1.0f,
                             1.0f, 0.0f, 1.0f,
                             1.0f, 0.0f, 1.0f
    };

    GLfloat tangents[] =    {-1.0f, 0.0f, 0.0f,            /* Back */
                             -1.0f, 0.0f, 0.0f,
                             -1.0f, 0.0f, 0.0f,
                             -1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,                /* Front */
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             0.0f, 0.0f, 1.0f,                /* Left */
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, 1.0f,
                             0.0f, 0.0f, -1.0f,                /* Right */
                             0.0f, 0.0f, -1.0f,
                             0.0f, 0.0f, -1.0f,
                             0.0f, 0.0f, -1.0f,
                             1.0f, 0.0f, 0.0f,                /* Top */
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,                /* Bottom */
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f,
                             1.0f, 0.0f, 0.0f
    };

    GLfloat biNormals[] = {    0.0f, 1.0f, 0.0f,                /* Back */
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,                /* Front */
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,                /* Left */
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,                 /* Right */
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 1.0f, 0.0f,
                               0.0f, 0.0f, -1.0f,                /* Top */
                               0.0f, 0.0f, -1.0f,
                               0.0f, 0.0f, -1.0f,
                               0.0f, 0.0f, -1.0f,
                               0.0f, 0.0f, 1.0f,                /* Bottom */
                               0.0f, 0.0f, 1.0f,
                               0.0f, 0.0f, 1.0f,
                               0.0f, 0.0f, 1.0f

    };
    GLfloat textureCords[] = {1.0f, 1.0f, /* Back. */
                              0.0f, 1.0f,
                              1.0f, 0.0f,
                              0.0f, 0.0f,
                              0.0f, 1.0f, /* Front. */
                              1.0f, 1.0f,
                              0.0f, 0.0f,
                              1.0f, 0.0f,
                              0.0f, 1.0f, /* Left. */
                              0.0f, 0.0f,
                              1.0f, 0.0f,
                              1.0f, 1.0f,
                              1.0f, 1.0f, /* Right. */
                              1.0f, 0.0f,
                              0.0f, 0.0f,
                              0.0f, 1.0f,
                              0.0f, 1.0f, /* Top. */
                              0.0f, 0.0f,
                              1.0f, 0.0f,
                              1.0f, 1.0f,
                              0.0f, 0.0f, /* Bottom. */
                              0.0f, 1.0f,
                              1.0f, 1.0f,
                              1.0f, 0.0f
    };

    GLushort indices[] = {0, 3, 2, 0, 1, 3, 4, 6, 7, 4, 7, 5,  8, 9, 10, 8, 11, 10, 12, 13, 14, 15, 12, 14, 16, 17, 18, 16, 19, 18, 20, 21, 22, 20, 23, 22};
/* [vertexColourTangentNormal] */

    void renderFrame() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear (GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        matrixIdentityFunction(modelViewMatrix);

        matrixRotateX(modelViewMatrix, angle);
        matrixRotateY(modelViewMatrix, angle);

        matrixTranslate(modelViewMatrix, 0.0f, 0.0f, -10.0f);

        glUseProgram(glProgram);

        /* [supplyData] */
        glVertexAttribPointer(vertexLocation, 3, GL_FLOAT, GL_FALSE, 0, cubeVertices);
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(textureCordLocation, 2, GL_FLOAT, GL_FALSE, 0, textureCords);
        glEnableVertexAttribArray(textureCordLocation);
        glVertexAttribPointer(colorLocation, 3, GL_FLOAT, GL_FALSE, 0, colour);
        glEnableVertexAttribArray(colorLocation);
        glVertexAttribPointer(vertexNormalLocation, 3, GL_FLOAT, GL_FALSE, 0, normals);
        glEnableVertexAttribArray(vertexNormalLocation);
        glVertexAttribPointer(biNormalLocation, 3, GL_FLOAT, GL_FALSE, 0, biNormals);
        glEnableVertexAttribArray(biNormalLocation);
        glVertexAttribPointer(tangentLocation, 3, GL_FLOAT, GL_FALSE, 0, tangents);
        glEnableVertexAttribArray(tangentLocation);
        glUniformMatrix4fv(projectionLocation, 1, GL_FALSE,projectionMatrix);
        glUniformMatrix4fv(modelViewLocation, 1, GL_FALSE, modelViewMatrix);
        /* [supplyData] */

        /* Set the sampler texture unit to 0. */
        glUniform1i(samplerLocation, 0);

        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_SHORT, indices);

        angle += 1;
        if (angle > 360)
        {
            angle -= 360;
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_NormalMappingActivity_init(JNIEnv *env, jclass type,
                                                                jint width, jint height) {

    // TODO
    NormalMapping::setupGraphics(width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_approjects_opengles_NormalMappingActivity_step(JNIEnv *env, jclass type) {

    // TODO
    NormalMapping::renderFrame();
}

