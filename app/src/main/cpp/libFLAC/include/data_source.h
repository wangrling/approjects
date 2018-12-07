//
// Created by wang on 18-12-7.
//

#ifndef APPROJECTS_DATA_SOURCE_H
#define APPROJECTS_DATA_SOURCE_H

#include <jni.h>
#include <sys/types.h>

class DataSource {
public:
    virtual ~DataSource() {

    }

    // Returns the number of bytes read, or -1 on failure. It's not an error if
    // this returns zero; it just means the given offset is equal to, or
    // beyond, the end of the source.
    virtual ssize_t readAt(off64_t offset, void* const data, size_t size) = 0;

};

#endif //APPROJECTS_DATA_SOURCE_H
