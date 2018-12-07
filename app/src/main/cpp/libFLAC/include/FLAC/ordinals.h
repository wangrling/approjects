//
// Created by wang on 18-12-7.
//

#ifndef FLAC_ORDINALS_H
#define FLAC_ORDINALS_H

#if defined(_MSC_VER) && _MSC_VER < 1600

/* Microsoft Visual Studio earlier than the 2010 version did not provide
 * the 1999 ISO C Standard header file <stdint.h>.
 */

typedef __int8 FLAC__int8;
typedef unsigned __int8 FLAC__uint8;

typedef __int16 FLAC__int16;
typedef __int32 FLAC__int32;
typedef __int64 FLAC__int64;
typedef unsigned __int16 FLAC__uint16;
typedef unsigned __int32 FLAC__uint32;
typedef unsigned __int64 FLAC__uint64;

#else

/* For MSVC 2010 and everything else which provides <stdint.h>. */

#include <stdint.h>

typedef int8_t FLAC__int8;
typedef uint8_t FLAC__uint8;

typedef int16_t FLAC__int16;
typedef int32_t FLAC__int32;
typedef int64_t FLAC__int64;
typedef uint16_t FLAC__uint16;
typedef uint32_t FLAC__uint32;
typedef uint64_t FLAC__uint64;

#endif

typedef int FLAC__bool;

typedef FLAC__uint8 FLAC__byte;


#ifdef true
#undef true
#endif

#ifdef false
#undef false
#endif

#ifndef __cplusplus
#define true 1
#define false 0
#endif

#endif //APPROJECTS_ORDINALS_H
