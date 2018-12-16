//
// Created by wangrl on 18-12-16.
//

#include "Common.h"
#include "CubeModel.h"

#include <cstdlib>

// 获取立方体的顶点坐标和法向量。

namespace GlesSDK {

    void CubeModel::getNormals(float **normalsPtrPtr, int *numberOfCoordinatesPtr) {

    }

    void
    CubeModel::getTriangleRepresentation(float **coordinatesPtrPtr, int *numberOfCoordinatesPtr,
                                         float scalingFactor) {
        ASSERT(coordinatesPtrPtr != NULL,
               "Cannot use null pointer while calculating coordinates");

        /* Index of an array we will put new point coordinates at. */
        int       currentIndex                    = 0;
        /* 6 faces of open cube, 2 triangles for each face, 3 points of triangle, 3 coordinates for each point. */
        const int numberOfCubeTriangleCoordinates = NUMBER_OF_CUBE_FACES        *
                                                    NUMBER_OF_TRIANGLES_IN_QUAD *
                                                    NUMBER_OF_TRIANGLE_VERTICES *
                                                    NUMBER_OF_POINT_COORDINATES;

        /* Allocate memory for result array. */
        *coordinatesPtrPtr = (float*) malloc(numberOfCubeTriangleCoordinates * sizeof(float));

        /* Is allocation successful?. */
        ASSERT(*coordinatesPtrPtr != NULL,
               "Could not allocate memory for result array.")

        /* Example:
         * Coordinates for cube points:
         * A -1.0f,  1.0f,  1.0f
         * B -1.0f,  1.0f, -1.0f
         * C  1.0f,  1.0f, -1.0f
         * D  1.0f,  1.0f,  1.0f
         * E -1.0f, -1.0f,  1.0f
         * F -1.0f, -1.0f, -1.0f
         * G  1.0f, -1.0f, -1.0f
         * H  1.0f, -1.0f,  1.0f
         * Create 2 triangles for each face of the cube. Vertices are written in clockwise order.
         *       B ________ C
         *      / |     /  |
         *  A ......... D  |
         *    .   |   .    |
         *    .  F|_ _.___ |G
         *    . /     .  /
         *  E ......... H
         */

        const Vec3f pointA = {-1.0f,  1.0f,  1.0f};
        const Vec3f pointB = {-1.0f,  1.0f, -1.0f};
        const Vec3f pointC = { 1.0f,  1.0f, -1.0f};
        const Vec3f pointD = { 1.0f,  1.0f,  1.0f};
        const Vec3f pointE = {-1.0f, -1.0f,  1.0f};
        const Vec3f pointF = {-1.0f, -1.0f, -1.0f};
        const Vec3f pointG = { 1.0f, -1.0f, -1.0f};
        const Vec3f pointH = { 1.0f, -1.0f,  1.0f};

        /* Fill the array with coordinates. */
        /* Top face. */
        /* A */
        (*coordinatesPtrPtr)[currentIndex++] = pointA.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.z;
        /* B */
        (*coordinatesPtrPtr)[currentIndex++] = pointB.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.z;
        /* C */
        (*coordinatesPtrPtr)[currentIndex++] = pointC.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.z;

        /* A */
        (*coordinatesPtrPtr)[currentIndex++] = pointA.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.z;
        /* C */
        (*coordinatesPtrPtr)[currentIndex++] = pointC.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.z;
        /* D */
        (*coordinatesPtrPtr)[currentIndex++] = pointD.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.z;

        /* Bottom face. */
        /* F */
        (*coordinatesPtrPtr)[currentIndex++] = pointF.x;;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.y;;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.z;;
        /* E */
        (*coordinatesPtrPtr)[currentIndex++] = pointE.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.z;
        /* H */
        (*coordinatesPtrPtr)[currentIndex++] = pointH.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.z;

        /* F */
        (*coordinatesPtrPtr)[currentIndex++] = pointF.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.z;
        /* H */
        (*coordinatesPtrPtr)[currentIndex++] = pointH.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.z;
        /* G */
        (*coordinatesPtrPtr)[currentIndex++] = pointG.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.z;

        /* Back face. */
        /* G */
        (*coordinatesPtrPtr)[currentIndex++] = pointG.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.z;
        /* C */
        (*coordinatesPtrPtr)[currentIndex++] = pointC.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.z;
        /* B */
        (*coordinatesPtrPtr)[currentIndex++] = pointB.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.z;

        /* G */
        (*coordinatesPtrPtr)[currentIndex++] = pointG.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.z;
        /* B */
        (*coordinatesPtrPtr)[currentIndex++] = pointB.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.z;
        /* F */
        (*coordinatesPtrPtr)[currentIndex++] = pointF.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.z;

        /* Front face. */
        /* E */
        (*coordinatesPtrPtr)[currentIndex++] = pointE.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.z;
        /* A */
        (*coordinatesPtrPtr)[currentIndex++] = pointA.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.z;
        /* D */
        (*coordinatesPtrPtr)[currentIndex++] = pointD.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.z;

        /* E */
        (*coordinatesPtrPtr)[currentIndex++] = pointE.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.z;
        /* D */
        (*coordinatesPtrPtr)[currentIndex++] = pointD.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.z;
        /* H */
        (*coordinatesPtrPtr)[currentIndex++] = pointH.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.z;

        /* Right face. */
        /* H */
        (*coordinatesPtrPtr)[currentIndex++] = pointH.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.z;
        /* D */
        (*coordinatesPtrPtr)[currentIndex++] = pointD.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointD.z;
        /* C */
        (*coordinatesPtrPtr)[currentIndex++] = pointC.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.z;

        /* H */
        (*coordinatesPtrPtr)[currentIndex++] = pointH.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointH.z;
        /* C */
        (*coordinatesPtrPtr)[currentIndex++] = pointC.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointC.z;
        /* G */
        (*coordinatesPtrPtr)[currentIndex++] = pointG.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointG.z;

        /* Left face. */
        /* F */
        (*coordinatesPtrPtr)[currentIndex++] = pointF.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.z;
        /* B */
        (*coordinatesPtrPtr)[currentIndex++] = pointB.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointB.z;
        /* A */
        (*coordinatesPtrPtr)[currentIndex++] = pointA.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.z;

        /* F */
        (*coordinatesPtrPtr)[currentIndex++] = pointF.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointF.z;
        /* A */
        (*coordinatesPtrPtr)[currentIndex++] = pointA.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointA.z;
        /* E */
        (*coordinatesPtrPtr)[currentIndex++] = pointE.x;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.y;
        (*coordinatesPtrPtr)[currentIndex++] = pointE.z;

        /* Calculate size of a cube. */
        if (scalingFactor != 1.0f)
        {
            for (int i = 0; i < numberOfCubeTriangleCoordinates; i++)
            {
                (*coordinatesPtrPtr)[i] *= scalingFactor;
            }
        }

        if (numberOfCoordinatesPtr != NULL)
        {
            *numberOfCoordinatesPtr = numberOfCubeTriangleCoordinates;
        }

    }
}