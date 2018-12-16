//
// Created by wangrl on 18-12-16.
//

#ifndef APPROJECTS_PLANEMODEL_H
#define APPROJECTS_PLANEMODEL_H

#include "VectorTypes.h"
#include "SimpleMatrix.h"

namespace GlesSDK {
    /**
     * \brief Functions for generating Plane shapes.
     */
    class PlaneModel
    {
    public:
        /**
        * \brief Get normals for plane placed in XZ space.
        *
        * \param normalsPtrPtr          Deref will be used to store generated normals. Cannot be null.
        * \param numberOfCoordinatesPtr Number of generated coordinates.
        */
        static void getNormals(float** normalsPtrPtr, int* numberOfCoordinatesPtr);

        /**
         * \brief Get coordinates of points which make up a plane. The plane is located in XZ space.
         *
         * \param coordinatesPtrPtr      Deref will be used to store generated coordinates. Cannot be null.
         * \param numberOfCoordinatesPtr Number of generated coordinates.
         * \param scalingFactor          Scaling factor indicating size of a plane.
         */
        static void getTriangleRepresentation(float** coordinatesPtrPtr, int* numberOfCoordinatesPtr, float scalingFactor);
    };
}

#endif //APPROJECTS_PLANEMODEL_H
