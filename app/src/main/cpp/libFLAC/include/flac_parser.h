//
// Created by wangrl on 18-12-6.
//

#ifndef APPROJECTS_FLAC_PARSER_H
#define APPROJECTS_FLAC_PARSER_H

#include <stdint.h>

// libFLAC parser
#include "FLAC/stream_decoder.h"
#include "data_source.h"

typedef int status_t;

class FLACParser {
public:
    FLACParser(DataSource *source);
    ~FLACParser();

    bool init();

private:
    DataSource *mDataSource;

    void (*mCopy)(int8_t *dst, const int *const *src, unsigned bytesPerSample,
                  unsigned nSamples, unsigned nChannels);

    // handle to underlying libFLAC parser
    FLAC__StreamDecoder *mDecoder;
};

#endif //APPROJECTS_FLAC_PARSER_H
