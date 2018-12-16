//
// Created by wangrl on 18-12-16.
//

#ifndef APPROJECTS_VECTORTYPES_H
#define APPROJECTS_VECTORTYPES_H

#include <cmath>

namespace GlesSDK {
    /** \brief A 3D floating point vector
     *
     * Class containing three floating point numbers, useful for representing 3D coordinates.
     */
    class Vec3f {
    public:
        float x, y, z;

        /**
         * \brief Calculate dot product between two 3D floating point vectors.
         *
         * \param[in] vector1 First floating point vector that will be used to compute product.
         * \param[in] vector2 Second floating point vector that will be used to compute product.
         *
         * \return Floating point value that is a result of dot product of vector1 and vector2.
         */
        static float dot(Vec3f& vector1, Vec3f& vector2)
        {
            return (vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z);
        }

        /**
         * \brief Normalize 3D floating point vector.
         */
        void normalize(void)
        {
            float length = sqrt(x * x + y * y + z * z);

            x /= length;
            y /= length;
            z /= length;
        }

        /** \brief Calculate cross product between two 3D floating point vectors.
         *
         * \param[in] vector1 First floating point vector that will be used to compute cross product.
         * \param[in] vector2 Second floating point vector that will be used to compute cross product.
         *
         * \return Floating point vector that is a result of cross product of vector1 and vector2.
         */
        static Vec3f cross(const Vec3f& vector1, const Vec3f& vector2)
        {
            /* Floating point vector to be returned. */
            Vec3f crossProduct;

            crossProduct.x = (vector1.y * vector2.z) - (vector1.z * vector2.y);
            crossProduct.y = (vector1.z * vector2.x) - (vector1.x * vector2.z);
            crossProduct.z = (vector1.x * vector2.y) - (vector1.y * vector2.x);

            return crossProduct;
        }
    };

    /** \brief A 4D floating point vector
     *
     * Class containing four floating point numbers.
     */
    class Vec4f {
    public:
        float x, y, z, w;
        /**
         * \brief Normalize 4D floating point vector.
         */
        void normalize(void)
        {
            float length = sqrt(x * x + y * y + z * z + w * w);

            x /= length;
            y /= length;
            z /= length;
            w /= length;
        }
    };
}

#endif //APPROJECTS_VECTORTYPES_H
