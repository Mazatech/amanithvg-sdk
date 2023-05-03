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

package javax.microedition.khronos.openvg;

class AmanithVGJNI {

    /*-------------------------------------------------------------------------------
                               Get error and pipeline flush
    -------------------------------------------------------------------------------*/
    public final static native int vgGetError();
    public final static native void vgFlush();
    public final static native void vgFinish();

    /*-------------------------------------------------------------------------------
                                  Getters and Setters
    -------------------------------------------------------------------------------*/
    public final static native void vgSetf(int type, float value);
    public final static native void vgSeti(int type, int value);
    public final static native void vgSetfvA(int type, int count, final float[] values, int offset);
    public final static native void vgSetivA(int type, int count, final int[] values, int offset);
    public final static native float vgGetf(int type);
    public final static native int vgGeti(int type);
    public final static native int vgGetVectorSize(int type);
    public final static native void vgGetfvA(int type, int count, float[] values, int offset);
    public final static native void vgGetivA(int type, int count, int[] values, int offset);
    public final static native void vgSetParameterf(int object, int paramType, float value);
    public final static native void vgSetParameteri(int object, int paramType, int value);
    public final static native void vgSetParameterfvA(int object, int paramType, int count, final float[] values, int offset);
    public final static native void vgSetParameterivA(int object, int paramType, int count, final int[] values, int offset);
    public final static native float vgGetParameterf(int object, int paramType);
    public final static native int vgGetParameteri(int object, int paramType);
    public final static native int vgGetParameterVectorSize(int object, int paramType);
    public final static native void vgGetParameterfvA(int object, int paramType, int count, float[] values, int offset);
    public final static native void vgGetParameterivA(int object, int paramType, int count, int[] values, int offset);

    /*-------------------------------------------------------------------------------
                                   Matrix Manipulation
    -------------------------------------------------------------------------------*/
    public final static native void vgLoadIdentity();
    public final static native void vgLoadMatrixA(final float[] m, int offset);
    public final static native void vgGetMatrixA(float[] m, int offset);
    public final static native void vgMultMatrixA(final float[] m, int offset);
    public final static native void vgTranslate(float tx, float ty);
    public final static native void vgScale(float sx, float sy);
    public final static native void vgShear(float shx, float shy);
    public final static native void vgRotate(float angle);

    /*-------------------------------------------------------------------------------
                                  Masking and Clearing
    -------------------------------------------------------------------------------*/
    public final static native void vgMask(int mask, int operation, int x, int y, int width, int height);
    public final static native void vgRenderToMask(int path, int paintModes, int operation);
    public final static native int vgCreateMaskLayer(int width, int height);
    public final static native void vgDestroyMaskLayer(int maskLayer);
    public final static native void vgFillMaskLayer(int maskLayer, int x, int y, int width, int height, float value);
    public final static native void vgCopyMask(int maskLayer, int dx, int dy, int sx, int sy, int width, int height);
    public final static native void vgClear(int x, int y, int width, int height);

    /*-------------------------------------------------------------------------------
                                          Paths
    -------------------------------------------------------------------------------*/
    public final static native int vgCreatePath(int pathFormat, int datatype, float scale, float bias, int segmentCapacityHint, int coordCapacityHint, int capabilities);
    public final static native void vgClearPath(int path, int capabilities);
    public final static native void vgDestroyPath(int path);
    public final static native void vgRemovePathCapabilities(int path, int capabilities);
    public final static native int vgGetPathCapabilities(int path);
    public final static native void vgAppendPath(int dstPath, int srcPath);
    public final static native void vgAppendPathDataAA(int dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final Object pathData, int pathDataOffset, int pathDataArrayType);
    public final static native void vgModifyPathCoordsA(int dstPath, int startIndex, int numSegments, final Object pathData, int offset, int arrayType);
    public final static native void vgTransformPath(int dstPath, int srcPath);
    public final static native boolean vgInterpolatePath(int dstPath, int startPath, int endPath, float amount);
    public final static native float vgPathLength(int path, int startSegment, int numSegments);
    public final static native void vgPointAlongPathA(int path, int startSegment, int numSegments, float distance, float[] values, int offset);
    public final static native void vgPathBoundsA(int path, float[] values, int offset);
    public final static native void vgPathTransformedBoundsA(int path, float[] values, int offset);
    public final static native void vgDrawPath(int path, int paintModes);

    /*-------------------------------------------------------------------------------
                                          Paint
    -------------------------------------------------------------------------------*/
    public final static native int vgCreatePaint();
    public final static native void vgDestroyPaint(int paint);
    public final static native void vgSetPaint(int paint, int paintModes);
    public final static native int vgGetPaint(int paintMode);
    public final static native void vgSetColor(int paint, int rgba);
    public final static native int vgGetColor(int paint);
    public final static native void vgPaintPattern(int paint, int pattern);

    /*-------------------------------------------------------------------------------
                                          Images
    -------------------------------------------------------------------------------*/
    public final static native int vgCreateImage(int format, int width, int height, int allowedQuality);
    public final static native void vgDestroyImage(int image);
    public final static native void vgClearImage(int image, int x, int y, int width, int height);
    public final static native void vgImageSubDataA(int image, final Object data, int offset, int arrayType, int dataStride, int dataFormat, int x, int y, int width, int height);
    public final static native void vgGetImageSubDataA(int image, Object data, int offset, int arrayType, int dataStride, int dataFormat, int x, int y, int width, int height);
    public final static native int vgChildImage(int parent, int x, int y, int width, int height);
    public final static native int vgGetParent(int image);
    public final static native void vgCopyImage(int dst, int dx, int dy, int src, int sx, int sy, int width, int height, boolean dither);
    public final static native void vgDrawImage(int image);
    public final static native void vgSetPixels(int dx, int dy, int src, int sx, int sy, int width, int height);
    public final static native void vgWritePixelsA(final Object data, int offset, int arrayType, int dataStride, int dataFormat, int dx, int dy, int width, int height);
    public final static native void vgGetPixels(int dst, int dx, int dy, int sx, int sy, int width, int height);
    public final static native void vgReadPixelsA(Object data, int offset, int arrayType, int dataStride, int dataFormat, int sx, int sy, int width, int height);
    public final static native void vgCopyPixels(int dx, int dy, int sx, int sy, int width, int height);

    /*-------------------------------------------------------------------------------
                                         Text
    -------------------------------------------------------------------------------*/
    public final static native int vgCreateFont(int glyphCapacityHint);
    public final static native void vgDestroyFont(int font);
    public final static native void vgSetGlyphToPathA(int font, int glyphIndex, int path, boolean isHinted, final float[] glyphOrigin, int glyphOriginOffset, final float[] escapement, int escapementOffset);
    public final static native void vgSetGlyphToImageA(int font, int glyphIndex, int image, final float[] glyphOrigin, int glyphOriginOffset, final float[] escapement, int escapementOffset);
    public final static native void vgClearGlyph(int font, int glyphIndex);
    public final static native void vgDrawGlyph(int font, int glyphIndex, int paintModes, boolean allowAutoHinting);
    public final static native void vgDrawGlyphsA(int font, int glyphCount, final int[] glyphIndices, int glyphIndicesOffset, final float[] adjustments_x, int adjustments_x_offset, final float[] adjustments_y, int adjustments_y_offset, int paintModes, boolean allowAutoHinting);
    
    /*-------------------------------------------------------------------------------
                                      Image Filters
    -------------------------------------------------------------------------------*/
    public final static native void vgColorMatrixA(int dst, int src, final float[] matrix, int offset);
    public final static native void vgConvolveA(int dst, int src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, int offset, float scale, float bias, int tilingMode);
    public final static native void vgSeparableConvolveA(int dst, int src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, int kernelXOffset, final short[] kernelY, int kernelYOffset, float scale, float bias, int tilingMode);
    public final static native void vgGaussianBlur(int dst, int src, float stdDeviationX, float stdDeviationY, int tilingMode);
    public final static native void vgLookupA(int dst, int src, final byte[] redLUT, int redLUTOffset, final byte[] greenLUT, int greenLUTOffset, final byte[] blueLUT, int blueLUTOffset, final byte[] alphaLUT, int alphaLUTOffset, boolean outputLinear, boolean outputPremultiplied);
    public final static native void vgLookupSingleA(int dst, int src, final int[] lookupTable, int offset, int sourceChannel, boolean outputLinear, boolean outputPremultiplied);

    /*-------------------------------------------------------------------------------
                                    Hardware Queries
    -------------------------------------------------------------------------------*/
    public final static native int vgHardwareQuery(int key, int setting);
    public final static native java.lang.String vgGetString(int name);
    
    /*-------------------------------------------------------------------------------
                                          VGU11
    -------------------------------------------------------------------------------*/
    public final static native int vguLine(int path, float x0, float y0, float x1, float y1);
    public final static native int vguPolygonA(int path, final float[] points, int offset, int count, boolean closed);
    public final static native int vguRect(int path, float x, float y, float width, float height);
    public final static native int vguRoundRect(int path, float x, float y, float width, float height, float arcWidth, float arcHeight);
    public final static native int vguEllipse(int path, float cx, float cy, float width, float height);
    public final static native int vguArc(int path, float x, float y, float width, float height, float startAngle, float angleExtent, int arcType);
    public final static native int vguComputeWarpQuadToSquareA(float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset);
    public final static native int vguComputeWarpSquareToQuadA(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float[] matrix, int offset);
    public final static native int vguComputeWarpQuadToQuadA(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset);

    /*-------------------------------------------------------------------------------
                                        VG11Ext
    -------------------------------------------------------------------------------*/
    // VG_MZT_clip_path
    public final static native void vgClipPathPushMZT(int path, boolean advanceLayer);
    public final static native void vgClipPathPopMZT();
    public final static native void vgClipPathClearMZT();

    // VG_MZT_filters
    public final static native void vgColorMatrixMZT(int dst, int src, final float[] matrix, int offset);
    public final static native void vgGaussianBlurMZT(int dst, int src, final float[] axes, int offset, float stdDeviationX, float stdDeviationY, int tilingMode, boolean useFastApprox);
    public final static native void vgLightingMZT(int dstDiffuse, int dstSpecular, int src, float surfaceScale, float diffuseConstant, float specularConstant, float specularExponent, int lightType, final float[] lightData, int ldOffset);
    public final static native void vgMorphologyMZT(int dst, int src, boolean erode, final float[] axes, int offset, int radiusX, int radiusY);
    public final static native void vgTurbulenceMZT(int image, float biasX, float biasY, float scaleX, float scaleY, float baseFrequencyX, float baseFrequencyY, int numOctaves, int seed, boolean stitchTiles, boolean fractalNoise);
    public final static native void vgDisplacementMapMZT(int dst, int src, int map, float scaleX, float scaleY, int tilingMode, int xChannelSelector, int yChannelSelector);
    public final static native void vgCompositeMZT(int dst, int in1, int in2, int operation, float k1, float k2, float k3, float k4);

    // VG_MZT_mask
    public final static native void vgMaskMZT(int mask, int operation, int x, int y, int width, int height);

    // Configuration parameters and thresholds
    public final static native int vgConfigSetMZT(int config, float value);
    public final static native float vgConfigGetMZT(int config);

    // EGL-like layer
    public final static native boolean vgInitializeMZT();
    public final static native void vgTerminateMZT();
    public final static native long vgPrivContextCreateMZT(long sharedContext);
    public final static native void vgPrivContextDestroyMZT(long context);
    public final static native long vgPrivSurfaceCreateMZT(int width, int height, boolean linearColorSpace, boolean alphaPremultiplied, boolean alphaMask);
    public final static native long vgPrivSurfaceCreateFromImageMZT(int image, boolean alphaMask);
    public final static native boolean vgPrivSurfaceResizeMZT(long surface, int width, int height);
    public final static native void vgPrivSurfaceDestroyMZT(long surface);
    public final static native int vgPrivGetSurfaceWidthMZT(final long surface);
    public final static native int vgPrivGetSurfaceHeightMZT(final long surface);
    public final static native int vgPrivGetSurfaceFormatMZT(final long surface);
    public final static native java.nio.ByteBuffer vgPrivGetSurfacePixelsMZT(final long surface);
    public final static native void vgPrivCopySurfacePixelsMZT(final long surface, int[] dstPixels, int offset, boolean redBlueSwap);
    public final static native boolean vgPrivMakeCurrentMZT(long context, long surface);
    public final static native int vgGetSurfaceWidthMZT();
    public final static native int vgGetSurfaceHeightMZT();
    public final static native int vgGetSurfaceFormatMZT();
    public final static native java.nio.ByteBuffer vgGetSurfacePixelsMZT();
    public final static native void vgCopySurfacePixelsMZT(int[] dstPixels, int offset, boolean redBlueSwap);
    public final static native void vgPostSwapBuffersMZT();
}
