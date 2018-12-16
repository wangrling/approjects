//
// Created by wangrl on 18-12-16.
//

#include "Timer.h"
#include <sys/time.h>
namespace GlesSDK {

    Timer::Timer() :
            startTime(),
            currentTime(),
            lastIntervalTime(0.0f),
            frameCount(0),
            fpsTime(0.0f),
            fps(0.0f) {
        startTime.tv_sec    = 0;
        startTime.tv_usec   = 0;
        currentTime.tv_sec  = 0;
        currentTime.tv_usec = 0;

        reset();
    }

    void Timer::reset() {
        // 开始时间
        gettimeofday(&startTime, NULL);

        lastIntervalTime = 0.0;

        frameCount = 0;
        fpsTime    = 0.0f;
    }

    // 相对开始的时间
    float Timer::getTime() {
        gettimeofday(&currentTime, NULL);

        float seconds      = (currentTime.tv_sec - startTime.tv_sec);
        float milliseconds = (float(currentTime.tv_usec - startTime.tv_usec)) / 1000000.0f;

        return seconds + milliseconds;
    }
}