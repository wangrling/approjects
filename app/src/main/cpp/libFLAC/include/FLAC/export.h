//
// Created by wang on 18-12-7.
//

#ifndef FLAC__EXPORT_H
#define FLAC__EXPORT_H

/**
 * \file include/FLAC/export.h
 *
 * \brief
 * This module contains #defines and symbols for exporting function
 * calls, and providing version information and compiled-in features.
 *
 * See the \link flac_export export \endlink module.
 */

/**
 * \defgroup flac_export FLAC/export.h: export symbols
 * \ingroup flac
 *
 * \brief
 * This module contains #defines and symbols for exporting function
 * calls, and providing version information and compiled-in features.
 *
 * If you are compiling with MSVC and will link to the static library
 * (libFLAC.lib) you should define FLAC__NO_DLL in your project to
 * make sure the symbols are exported properly.
 */

// MSVC stands for Microsoft Visual C++
// Dynamic-link library (or DLL) is Microsoft's implementation of the
// shared library concept in the Microsoft Windows and OS/2 operating systems.

// 定义FLAC_API

#if defined(FLAC__NO_DLL)
#define FLAC_API

#elif defined(_MSC_VER)
#ifdef FLAC_API_EXPORTS
// __declspec, Microsoft specific extension to the C++ language which
// allows you to attribute a type or function with storage class information.
#define	FLAC_API __declspec(dllexport)
#else
#define FLAC_API __declspec(dllimport)
#endif

#elif defined(FLAC__USE_VISIBILITY_ATTR)
// __attribute__ ((attribute-list))
// 修改符号可见性
#define FLAC_API __attribute__ ((visibility ("default")))

#else
#define FLAC_API

#endif


/** These #defines will mirror the libtool-based library version number, see
 * http://www.gnu.org/software/libtool/manual/libtool.html#Libtool-versioning
 */
#define FLAC_API_VERSION_CURRENT 11
#define FLAC_API_VERSION_REVISION 0     /**<see above> */
#define FLAC_API_VERSION_AGE 3      /**<see above> */

#ifdef __cplusplus
extern "C" {
#endif

    /** \c 1 if the library has been compiled with supported for Ogg FLAC, else \c 0. */
    extern FLAC_API int FLAC_API_SUPPORTS_OGG_FLAC;

#ifdef __cplusplus
};
#endif

#endif //FLAC_EXPORT_H
