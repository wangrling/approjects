//
// Created by wangrl on 18-12-16.
//

#ifndef APPROJECTS_TIMER_H
#define APPROJECTS_TIMER_H

#include <linux/time.h>

namespace GlesSDK {
    /**
     * \brief Provides a platform independent high resolution timer.
     * \note The timer measures real time, not CPU time.
     */
    class Timer {
    private:
        int frameCount;
        float fps;
        float lastTime;

        timeval startTime;
        timeval currentTime;
        float   lastIntervalTime;
        float   fpsTime;

    public:
        /**
         * \brief Default Constructor
         */
        Timer();

        /**
         * \brief Resets the timer to 0.0f.
         */
        void reset();

        /**
         * \brief Returns the time passed since object creation or since reset() was last called.
         *
         * \return Float containing the current time.
         */
        float getTime();
    };
}
#endif //APPROJECTS_TIMER_H
