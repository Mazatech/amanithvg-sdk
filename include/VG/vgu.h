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
   This file is a modified version of the sample implementation of vgu.h, version 1.1.
   The original copyright and permission notice, RELATED TO THIS FILE ONLY, are reported below:
*/

/**************************************************************************
*                                                                         *
* Sample implementation of vgu.h, version 1.1                             *
*                                                                         *
* Copyright © 2008 The Khronos Group Inc.                                 *
*                                                                         *
* Permission is hereby granted, free of charge, to any person obtaining   *
* a copy of this software and associated documentation files (the         *
* "Software"), to deal in the Software without restriction, including     *
* without limitation the rights to use, copy, modify, merge, publish,     *
* distribute, sublicense, and/or sell copies of the Software, and to      *
* permit persons to whom the Software is furnished to do so, subject      *
* to the following conditions:                                            *
* The above copyright notice and this permission notice shall be          *
* included in all copies or substantial portions of the Software.         *
*                                                                         *
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,         *
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF      *
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  *
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY    *
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,    *
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE       *
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                  *
*                                                                         *
**************************************************************************/

#ifndef VGU_H
#define VGU_H

#if defined(__cplusplus)
extern "C" {
#endif

#include <VG/openvg.h>

#define VGU_VERSION_1_0 1
#define VGU_VERSION_1_1 2

#ifndef VGU_API_CALL
    #error VGU_API_CALL must be defined
#endif

#ifndef VGU_API_ENTRY
    #error VGU_API_ENTRY must be defined 
#endif

#ifndef VGU_API_EXIT
    #error VGU_API_EXIT must be defined 
#endif

typedef enum {
    VGU_NO_ERROR                                 = 0,
    VGU_BAD_HANDLE_ERROR                         = 0xF000,
    VGU_ILLEGAL_ARGUMENT_ERROR                   = 0xF001,
    VGU_OUT_OF_MEMORY_ERROR                      = 0xF002,
    VGU_PATH_CAPABILITY_ERROR                    = 0xF003,
    VGU_BAD_WARP_ERROR                           = 0xF004,

    VGU_ERROR_CODE_FORCE_SIZE                    = VG_MAX_ENUM
} VGUErrorCode;

typedef enum {
    VGU_ARC_OPEN                                 = 0xF100,
    VGU_ARC_CHORD                                = 0xF101,
    VGU_ARC_PIE                                  = 0xF102,

    VGU_ARC_TYPE_FORCE_SIZE                      = VG_MAX_ENUM
} VGUArcType;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguLine(VGPath path,
                                                VGfloat p0x,
                                                VGfloat p0y,
                                                VGfloat p1x,
                                                VGfloat p1y) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguPolygon(VGPath path,
                                                   const VGfloat *points,
                                                   VGint count,
                                                   VGboolean closed) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguRect(VGPath path,
                                                VGfloat x,
                                                VGfloat y,
                                                VGfloat width,
                                                VGfloat height) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguRoundRect(VGPath path,
                                                     VGfloat x,
                                                     VGfloat y,
                                                     VGfloat width,
                                                     VGfloat height,
                                                     VGfloat arcWidth,
                                                     VGfloat arcHeight) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguEllipse(VGPath path,
                                                   VGfloat cx,
                                                   VGfloat cy,
                                                   VGfloat width,
                                                   VGfloat height) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguArc(VGPath path,
                                               VGfloat x,
                                               VGfloat y,
                                               VGfloat width,
                                               VGfloat height,
                                               VGfloat startAngle,
                                               VGfloat angleExtent,
                                               VGUArcType arcType) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguComputeWarpQuadToSquare(VGfloat sx0,
                                                                   VGfloat sy0,
                                                                   VGfloat sx1,
                                                                   VGfloat sy1,
                                                                   VGfloat sx2,
                                                                   VGfloat sy2,
                                                                   VGfloat sx3,
                                                                   VGfloat sy3,
                                                                   VGfloat *matrix) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguComputeWarpSquareToQuad(VGfloat dx0,
                                                                   VGfloat dy0,
                                                                   VGfloat dx1,
                                                                   VGfloat dy1,
                                                                   VGfloat dx2,
                                                                   VGfloat dy2,
                                                                   VGfloat dx3,
                                                                   VGfloat dy3,
                                                                   VGfloat *matrix) VGU_API_EXIT;

VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguComputeWarpQuadToQuad(VGfloat dx0,
                                                                 VGfloat dy0,
                                                                 VGfloat dx1,
                                                                 VGfloat dy1,
                                                                 VGfloat dx2,
                                                                 VGfloat dy2,
                                                                 VGfloat dx3,
                                                                 VGfloat dy3,
                                                                 VGfloat sx0,
                                                                 VGfloat sy0,
                                                                 VGfloat sx1,
                                                                 VGfloat sy1,
                                                                 VGfloat sx2,
                                                                 VGfloat sy2,
                                                                 VGfloat sx3,
                                                                 VGfloat sy3,
                                                                 VGfloat *matrix) VGU_API_EXIT;
#if defined(__cplusplus)
} /* extern "C" */
#endif

#endif /* #ifndef VGU_H */
