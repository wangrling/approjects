//
// Created by wangrl on 18-12-16.
//

#ifndef APPROJECTS_CUBEMODEL_H
#define APPROJECTS_CUBEMODEL_H

#include "Mathematics.h"
#include "VectorTypes.h"

namespace GlesSDK {
    /**
     * \brief Functions for generating cube shapes.
     */
    class CubeModel
    {
    public:
        /**
         * \brief Create normals for a cube.
         *
         * \param normalsPtrPtr          Deref will be used to store generated coordinates.
         *                               Cannot be null.
         * \param numberOfCoordinatesPtr Number of generated coordinates.
         */
        static void getNormals(float** normalsPtrPtr, int* numberOfCoordinatesPtr);

        /**
         * \brief Compute coordinates of points which make up a cube shape.
         *
         * \param coordinatesPtrPtr      Deref will be used to store generated coordinates.
         *                               Cannot be null.
         * \param numberOfCoordinatesPtr Number of generated coordinates.
         * \param scalingFactor          Scaling factor indicating size of a cube.
         */
        static void getTriangleRepresentation(float** coordinatesPtrPtr,
                                              int*    numberOfCoordinatesPtr,
                                              float   scalingFactor);
    };
}

#endif //APPROJECTS_CUBEMODEL_H
