# Sets the minimum version of CMake required to build the native
# library.
cmake_minimum_required(VERSION 3.4.1)

add_library( flacJNI
             SHARED
             libFLAC/flac_jni.cc
             libFLAC/flac_parser.cc
            )

find_library(loglib log)

include_directories(opengles)
add_library( es2
            SHARED
            opengles/ES2Source.cpp
            opengles/SimpleMatrix.cpp
            opengles/Texture.cpp
        )
target_link_libraries(es2 ${loglib} GLESv3)

add_library(es3
        SHARED
        opengles/ES3Source.cpp
        opengles/Texture.cpp
        opengles/Shader.cpp
        opengles/Timer.cpp
        opengles/CubeModel.cpp
        opengles/PlaneModel.cpp
        opengles/Matrix.cpp
        )
target_link_libraries(es3 ${loglib} GLESv3)

# Creates the project's shared lib: libnative-lib.so.
# The lib is loaded by this project's Java code in MainActivity.java:
#     System.loadLibrary("native-lib");
# The lib name in both places must match.
# add_library( native-lib
#             SHARED
#             src/main/cpp/native-lib.cpp )

#find_library(log-lib
# log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

# target_link_libraries( # Specifies the target library.
#                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
#                       ${log-lib} )