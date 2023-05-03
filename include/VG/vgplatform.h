/****************************************************************************
** Copyright (C) 2004-2023 Mazatech S.r.l. All rights reserved.
**
** This file is part of AmanithVG software, an OpenVG implementation.
**
** Khronos and OpenVG are trademarks of The Khronos Group Inc.
** OpenGL is a registered trademark and OpenGL ES is a trademark of
** Silicon Graphics, Inc.
**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
** For any information, please contact info@mazatech.com
**
****************************************************************************/

/*
   This file is a modified version of the sample implementation of vgplatform.h
   The original copyright and permission notice, RELATED TO THIS FILE ONLY, are reported below:
*/

/* $Revision: 6810 $ on $Date:: 2008-10-29 07:31:37 -0700 #$ */

/*------------------------------------------------------------------------
 *
 * VG platform specific header Reference Implementation
 * ----------------------------------------------------
 *
 * Copyright (c) 2008 The Khronos Group Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and /or associated documentation files
 * (the "Materials "), to deal in the Materials without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Materials,
 * and to permit persons to whom the Materials are furnished to do so,
 * subject to the following conditions: 
 *
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Materials. 
 *
 * THE MATERIALS ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE MATERIALS OR
 * THE USE OR OTHER DEALINGS IN THE MATERIALS.
 *
 *//**
 * \file
 * \brief VG platform specific header
 *//*-------------------------------------------------------------------*/

#ifndef VGPLATFORM_H
#define VGPLATFORM_H

#ifdef __cplusplus
extern "C" {
#endif

/* Compiler detection */
#if defined(_MSC_VER)
    #define AM_CC_MSVC _MSC_VER
#elif defined(__clang__)
    /* Clang */
    #define AM_CC_CLANG
    #define AM_CC_HAS_CLASS_VISIBILITY
#elif (defined(__GNUC__) && !defined(__ARMCC_VERSION)) || (defined(__SYMBIAN32__) && defined (__GCC32__))
    #define AM_CC_GCC
    #if (__GNUC__ >= 4) || (__GNUC__ == 3 && __GNUC_MINOR__ >= 4)
        #define AM_CC_HAS_CLASS_VISIBILITY
    #else
        #undef AM_CC_HAS_CLASS_VISIBILITY
    #endif
#elif defined(__ARMCC_VERSION) || (defined(__SYMBIAN32__) && defined(__ARMCC_2__))
    #define AM_CC_ARMCC __ARMCC_VERSION
#elif defined(__RENESAS_VERSION__)
    /* Renesas CS+ */
    #define AM_CC_RENESAS
#elif defined(__ghs__)
    /* Green Hills compiler */
    #define AM_CC_GHS
#else
    #error Unsupported compiler!
#endif

/* VG_API_CALL definition */
#if defined(AM_MAKE_STATIC_LIBRARY)
    #undef AM_MAKE_DYNAMIC_LIBRARY
    /* For pre-linked static libraries, we want to show/export only OpenVG API public functions
    unless we want to link/pre-link AmanithVG within another static library (e.g. AmanithSVG): in
    such case we want to export all AmanithVG symbols as extern and perform the pre-link
    optimization when compiling the other static library (AmanithSVG), so all OpenVG functions will
    be hidden */
    #if (defined(AM_CC_GCC) || defined(AM_CC_CLANG))
        #if defined(AM_SVG)
            #define VG_API_CALL extern
        #else
            #if defined(AM_CC_HAS_CLASS_VISIBILITY)
                #define VG_API_CALL __attribute__((visibility("default"))) extern
            #else
                #define VG_API_CALL extern
            #endif
        #endif
    #elif defined(AM_CC_ARMCC)
        // ARM Compiler 6 (armclang)
        #define VG_API_CALL __attribute__((visibility("default"))) extern
    #else
        #define VG_API_CALL
    #endif
#else
    #if defined(AM_CC_MSVC) || defined(AM_CC_ARMCC) || (defined(AM_CC_GCC) && (defined(_WIN32_WCE) || defined(__MINGW32__) || defined(__MINGW64__)))
        #if defined(AM_MAKE_DYNAMIC_LIBRARY)
            #define VG_API_CALL __declspec(dllexport)
        #else
            #define VG_API_CALL __declspec(dllimport)
        #endif
    #elif (defined(AM_CC_GCC) || defined(AM_CC_CLANG))
        #if defined(AM_MAKE_DYNAMIC_LIBRARY)
            #if defined(AM_CC_HAS_CLASS_VISIBILITY)
                // 'extern' keyword can be omitted, because it is the default
                #define VG_API_CALL __attribute__((visibility("default")))
            #else
                // 'extern' keyword can be omitted, because it is the default
                #define VG_API_CALL
            #endif
        #else
            #define VG_API_CALL extern
        #endif
    #else
        #undef VG_API_CALL
    #endif
#endif

/* VGU_API_CALL definition */
#define VGU_API_CALL VG_API_CALL

#ifndef VG_API_ENTRY
    #define VG_API_ENTRY
#endif

#ifndef VG_API_EXIT
    #define VG_API_EXIT
#endif

#ifndef VGU_API_ENTRY
    #define VGU_API_ENTRY
#endif

#ifndef VGU_API_EXIT
    #define VGU_API_EXIT
#endif

typedef float          VGfloat;
typedef signed char    VGbyte;
typedef unsigned char  VGubyte;
typedef signed short   VGshort;
typedef signed int     VGint;
typedef unsigned int   VGuint;
typedef unsigned int   VGbitfield;

#ifndef VG_VGEXT_PROTOTYPES
    #define VG_VGEXT_PROTOTYPES
#endif

#ifdef __cplusplus 
} /* extern "C" */
#endif

#endif /* VGPLATFORM_H */
