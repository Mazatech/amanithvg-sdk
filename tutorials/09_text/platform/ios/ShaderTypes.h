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

#ifndef SHADER_TYPES_H
#define SHADER_TYPES_H

#include <simd/simd.h>

// this structure defines the layout of vertices sent to the vertex shaders; this header is shared between the .metal shader and C code
// to guarantee that the layout of the vertex array in the C code matches the layout that the .metal vertex shaders expect
typedef struct {
    // positions in pixel space
    vector_float2 position;
    // 2D texture coordinate
    vector_float2 textureCoordinate;
} TexturedVertex;

#endif /* SHADER_TYPES_H */
