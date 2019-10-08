/****************************************************************************
** Copyright (C) 2004-2019 Mazatech S.r.l. All rights reserved.
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

package javax.microedition.khronos.openvg;

public interface VGU101 extends VGU {

    // VGUErrorCode
    public static final int VGU_NO_ERROR                                = 0;
    public static final int VGU_BAD_HANDLE_ERROR                        = 0xF000;
    public static final int VGU_ILLEGAL_ARGUMENT_ERROR                  = 0xF001;
    public static final int VGU_OUT_OF_MEMORY_ERROR                     = 0xF002;
    public static final int VGU_PATH_CAPABILITY_ERROR                   = 0xF003;
    public static final int VGU_BAD_WARP_ERROR                          = 0xF004;

    // VGUArcType
    public static final int VGU_ARC_OPEN                                = 0xF100;
    public static final int VGU_ARC_CHORD                               = 0xF101;
    public static final int VGU_ARC_PIE                                 = 0xF102;

    public int vguLine(VGPath path, float x0, float y0, float x1, float y1);
    public int vguPolygon(VGPath path, final float[] points, int offset, int count, boolean closed);
    public int vguRect(VGPath path, float x, float y, float width, float height);
    public int vguRoundRect(VGPath path, float x, float y, float width, float height, float arcWidth, float arcHeight);
    public int vguEllipse(VGPath path, float cx, float cy, float width, float height);
    public int vguArc(VGPath path, float x, float y, float width, float height, float startAngle, float angleExtent, int arcType);
    public int vguComputeWarpQuadToSquare(float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset);
    public int vguComputeWarpSquareToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float[] matrix, int offset);
    public int vguComputeWarpQuadToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset);
}
