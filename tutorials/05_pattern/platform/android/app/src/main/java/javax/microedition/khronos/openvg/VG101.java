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

public interface VG101 extends VG {

    // VGboolean
    public static final int VG_FALSE                                    = 0;
    public static final int VG_TRUE                                     = 1;

    public static final int VG_MAXSHORT                                 = 0x7FFF;
    public static final int VG_MAXINT                                   = 0x7FFFFFFF;

    // VGErrorCode
    public static final int VG_NO_ERROR                                 = 0;
    public static final int VG_BAD_HANDLE_ERROR                         = 0x1000;
    public static final int VG_ILLEGAL_ARGUMENT_ERROR                   = 0x1001;
    public static final int VG_OUT_OF_MEMORY_ERROR                      = 0x1002;
    public static final int VG_PATH_CAPABILITY_ERROR                    = 0x1003;
    public static final int VG_UNSUPPORTED_IMAGE_FORMAT_ERROR           = 0x1004;
    public static final int VG_UNSUPPORTED_PATH_FORMAT_ERROR            = 0x1005;
    public static final int VG_IMAGE_IN_USE_ERROR                       = 0x1006;
    public static final int VG_NO_CONTEXT_ERROR                         = 0x1007;

    // VGParamType
    // Mode settings
    public static final int VG_MATRIX_MODE                              = 0x1100;
    public static final int VG_FILL_RULE                                = 0x1101;
    public static final int VG_IMAGE_QUALITY                            = 0x1102;
    public static final int VG_RENDERING_QUALITY                        = 0x1103;
    public static final int VG_BLEND_MODE                               = 0x1104;
    public static final int VG_IMAGE_MODE                               = 0x1105;
    // Scissoring rectangles
    public static final int VG_SCISSOR_RECTS                            = 0x1106;
    // Stroke parameters
    public static final int VG_STROKE_LINE_WIDTH                        = 0x1110;
    public static final int VG_STROKE_CAP_STYLE                         = 0x1111;
    public static final int VG_STROKE_JOIN_STYLE                        = 0x1112;
    public static final int VG_STROKE_MITER_LIMIT                       = 0x1113;
    public static final int VG_STROKE_DASH_PATTERN                      = 0x1114;
    public static final int VG_STROKE_DASH_PHASE                        = 0x1115;
    public static final int VG_STROKE_DASH_PHASE_RESET                  = 0x1116;
    // Edge fill color for VG_TILE_FILL tiling mode
    public static final int VG_TILE_FILL_COLOR                          = 0x1120;
    // Color for vgClear
    public static final int VG_CLEAR_COLOR                              = 0x1121;
    // Enable/disable alpha masking and scissoring
    public static final int VG_MASKING                                  = 0x1130;
    public static final int VG_SCISSORING                               = 0x1131;
    // Pixel layout hint information
    public static final int VG_PIXEL_LAYOUT                             = 0x1140;
    public static final int VG_SCREEN_LAYOUT                            = 0x1141;
    // Source format selection for image filters
    public static final int VG_FILTER_FORMAT_LINEAR                     = 0x1150;
    public static final int VG_FILTER_FORMAT_PREMULTIPLIED              = 0x1151;
    // Destination write enable mask for image filters
    public static final int VG_FILTER_CHANNEL_MASK                      = 0x1152;
    // Implementation limits (read-only)
    public static final int VG_MAX_SCISSOR_RECTS                        = 0x1160;
    public static final int VG_MAX_DASH_COUNT                           = 0x1161;
    public static final int VG_MAX_KERNEL_SIZE                          = 0x1162;
    public static final int VG_MAX_SEPARABLE_KERNEL_SIZE                = 0x1163;
    public static final int VG_MAX_COLOR_RAMP_STOPS                     = 0x1164;
    public static final int VG_MAX_IMAGE_WIDTH                          = 0x1165;
    public static final int VG_MAX_IMAGE_HEIGHT                         = 0x1166;
    public static final int VG_MAX_IMAGE_PIXELS                         = 0x1167;
    public static final int VG_MAX_IMAGE_BYTES                          = 0x1168;
    public static final int VG_MAX_FLOAT                                = 0x1169;
    public static final int VG_MAX_GAUSSIAN_STD_DEVIATION               = 0x116A;

    // VGRenderingQuality
    public static final int VG_RENDERING_QUALITY_NONANTIALIASED         = 0x1200;
    public static final int VG_RENDERING_QUALITY_FASTER                 = 0x1201;
    public static final int VG_RENDERING_QUALITY_BETTER                 = 0x1202;

    // VGPixelLayout
    public static final int VG_PIXEL_LAYOUT_UNKNOWN                     = 0x1300;
    public static final int VG_PIXEL_LAYOUT_RGB_VERTICAL                = 0x1301;
    public static final int VG_PIXEL_LAYOUT_BGR_VERTICAL                = 0x1302;
    public static final int VG_PIXEL_LAYOUT_RGB_HORIZONTAL              = 0x1303;
    public static final int VG_PIXEL_LAYOUT_BGR_HORIZONTAL              = 0x1304;

    // VGMatrixMode
    public static final int VG_MATRIX_PATH_USER_TO_SURFACE              = 0x1400;
    public static final int VG_MATRIX_IMAGE_USER_TO_SURFACE             = 0x1401;
    public static final int VG_MATRIX_FILL_PAINT_TO_USER                = 0x1402;
    public static final int VG_MATRIX_STROKE_PAINT_TO_USER              = 0x1403;

    // VGMaskOperation
    public static final int VG_CLEAR_MASK                               = 0x1500;
    public static final int VG_FILL_MASK                                = 0x1501;
    public static final int VG_SET_MASK                                 = 0x1502;
    public static final int VG_UNION_MASK                               = 0x1503;
    public static final int VG_INTERSECT_MASK                           = 0x1504;
    public static final int VG_SUBTRACT_MASK                            = 0x1505;

    // Path format
    public static final int VG_PATH_FORMAT_STANDARD                     = 0;

    // VGPathDatatype
    public static final int VG_PATH_DATATYPE_S_8                        = 0;
    public static final int VG_PATH_DATATYPE_S_16                       = 1;
    public static final int VG_PATH_DATATYPE_S_32                       = 2;
    public static final int VG_PATH_DATATYPE_F                          = 3;

    // VGPathAbsRel
    public static final byte VG_ABSOLUTE                                = 0;
    public static final byte VG_RELATIVE                                = 1;

    // VGPathSegment
    public static final byte VG_CLOSE_PATH                              = ( 0 << 1);
    public static final byte VG_MOVE_TO                                 = ( 1 << 1);
    public static final byte VG_LINE_TO                                 = ( 2 << 1);
    public static final byte VG_HLINE_TO                                = ( 3 << 1);
    public static final byte VG_VLINE_TO                                = ( 4 << 1);
    public static final byte VG_QUAD_TO                                 = ( 5 << 1);
    public static final byte VG_CUBIC_TO                                = ( 6 << 1);
    public static final byte VG_SQUAD_TO                                = ( 7 << 1);
    public static final byte VG_SCUBIC_TO                               = ( 8 << 1);
    public static final byte VG_SCCWARC_TO                              = ( 9 << 1);
    public static final byte VG_SCWARC_TO                               = (10 << 1);
    public static final byte VG_LCCWARC_TO                              = (11 << 1);
    public static final byte VG_LCWARC_TO                               = (12 << 1);

    // VGPathCommand
    public static final byte VG_MOVE_TO_ABS                             = VG_MOVE_TO    | VG_ABSOLUTE;
    public static final byte VG_MOVE_TO_REL                             = VG_MOVE_TO    | VG_RELATIVE;
    public static final byte VG_LINE_TO_ABS                             = VG_LINE_TO    | VG_ABSOLUTE;
    public static final byte VG_LINE_TO_REL                             = VG_LINE_TO    | VG_RELATIVE;
    public static final byte VG_HLINE_TO_ABS                            = VG_HLINE_TO   | VG_ABSOLUTE;
    public static final byte VG_HLINE_TO_REL                            = VG_HLINE_TO   | VG_RELATIVE;
    public static final byte VG_VLINE_TO_ABS                            = VG_VLINE_TO   | VG_ABSOLUTE;
    public static final byte VG_VLINE_TO_REL                            = VG_VLINE_TO   | VG_RELATIVE;
    public static final byte VG_QUAD_TO_ABS                             = VG_QUAD_TO    | VG_ABSOLUTE;
    public static final byte VG_QUAD_TO_REL                             = VG_QUAD_TO    | VG_RELATIVE;
    public static final byte VG_CUBIC_TO_ABS                            = VG_CUBIC_TO   | VG_ABSOLUTE;
    public static final byte VG_CUBIC_TO_REL                            = VG_CUBIC_TO   | VG_RELATIVE;
    public static final byte VG_SQUAD_TO_ABS                            = VG_SQUAD_TO   | VG_ABSOLUTE;
    public static final byte VG_SQUAD_TO_REL                            = VG_SQUAD_TO   | VG_RELATIVE;
    public static final byte VG_SCUBIC_TO_ABS                           = VG_SCUBIC_TO  | VG_ABSOLUTE;
    public static final byte VG_SCUBIC_TO_REL                           = VG_SCUBIC_TO  | VG_RELATIVE;
    public static final byte VG_SCCWARC_TO_ABS                          = VG_SCCWARC_TO | VG_ABSOLUTE;
    public static final byte VG_SCCWARC_TO_REL                          = VG_SCCWARC_TO | VG_RELATIVE;
    public static final byte VG_SCWARC_TO_ABS                           = VG_SCWARC_TO  | VG_ABSOLUTE;
    public static final byte VG_SCWARC_TO_REL                           = VG_SCWARC_TO  | VG_RELATIVE;
    public static final byte VG_LCCWARC_TO_ABS                          = VG_LCCWARC_TO | VG_ABSOLUTE;
    public static final byte VG_LCCWARC_TO_REL                          = VG_LCCWARC_TO | VG_RELATIVE;
    public static final byte VG_LCWARC_TO_ABS                           = VG_LCWARC_TO  | VG_ABSOLUTE;
    public static final byte VG_LCWARC_TO_REL                           = VG_LCWARC_TO  | VG_RELATIVE;

    // VGPathCapabilities
    public static final int VG_PATH_CAPABILITY_APPEND_FROM              = (1 <<  0);
    public static final int VG_PATH_CAPABILITY_APPEND_TO                = (1 <<  1);
    public static final int VG_PATH_CAPABILITY_MODIFY                   = (1 <<  2);
    public static final int VG_PATH_CAPABILITY_TRANSFORM_FROM           = (1 <<  3);
    public static final int VG_PATH_CAPABILITY_TRANSFORM_TO             = (1 <<  4);
    public static final int VG_PATH_CAPABILITY_INTERPOLATE_FROM         = (1 <<  5);
    public static final int VG_PATH_CAPABILITY_INTERPOLATE_TO           = (1 <<  6);
    public static final int VG_PATH_CAPABILITY_PATH_LENGTH              = (1 <<  7);
    public static final int VG_PATH_CAPABILITY_POINT_ALONG_PATH         = (1 <<  8);
    public static final int VG_PATH_CAPABILITY_TANGENT_ALONG_PATH       = (1 <<  9);
    public static final int VG_PATH_CAPABILITY_PATH_BOUNDS              = (1 << 10);
    public static final int VG_PATH_CAPABILITY_PATH_TRANSFORMED_BOUNDS  = (1 << 11);
    public static final int VG_PATH_CAPABILITY_ALL                      = ((1 << 12) - 1);

    // VGPathParamType
    public static final int VG_PATH_FORMAT                              = 0x1600;
    public static final int VG_PATH_DATATYPE                            = 0x1601;
    public static final int VG_PATH_SCALE                               = 0x1602;
    public static final int VG_PATH_BIAS                                = 0x1603;
    public static final int VG_PATH_NUM_SEGMENTS                        = 0x1604;
    public static final int VG_PATH_NUM_COORDS                          = 0x1605;

    // VGCapStyle
    public static final int VG_CAP_BUTT                                 = 0x1700;
    public static final int VG_CAP_ROUND                                = 0x1701;
    public static final int VG_CAP_SQUARE                               = 0x1702;

    // VGJoinStyle
    public static final int VG_JOIN_MITER                               = 0x1800;
    public static final int VG_JOIN_ROUND                               = 0x1801;
    public static final int VG_JOIN_BEVEL                               = 0x1802;

    // VGFillRule
    public static final int VG_EVEN_ODD                                 = 0x1900;
    public static final int VG_NON_ZERO                                 = 0x1901;

    // VGPaintMode
    public static final int VG_STROKE_PATH                              = (1 << 0);
    public static final int VG_FILL_PATH                                = (1 << 1);

    // VGPaintParamType
    // Color paint parameters
    public static final int VG_PAINT_TYPE                               = 0x1A00;
    public static final int VG_PAINT_COLOR                              = 0x1A01;
    public static final int VG_PAINT_COLOR_RAMP_SPREAD_MODE             = 0x1A02;
    public static final int VG_PAINT_COLOR_RAMP_PREMULTIPLIED           = 0x1A07;
    public static final int VG_PAINT_COLOR_RAMP_STOPS                   = 0x1A03;
    // Linear gradient paint parameters
    public static final int VG_PAINT_LINEAR_GRADIENT                    = 0x1A04;
    // Radial gradient paint parameters
    public static final int VG_PAINT_RADIAL_GRADIENT                    = 0x1A05;
    // Pattern paint parameters
    public static final int VG_PAINT_PATTERN_TILING_MODE                = 0x1A06;

    // VGPaintType
    public static final int VG_PAINT_TYPE_COLOR                         = 0x1B00;
    public static final int VG_PAINT_TYPE_LINEAR_GRADIENT               = 0x1B01;
    public static final int VG_PAINT_TYPE_RADIAL_GRADIENT               = 0x1B02;
    public static final int VG_PAINT_TYPE_PATTERN                       = 0x1B03;

    // VGColorRampSpreadMode
    public static final int VG_COLOR_RAMP_SPREAD_PAD                    = 0x1C00;
    public static final int VG_COLOR_RAMP_SPREAD_REPEAT                 = 0x1C01;
    public static final int VG_COLOR_RAMP_SPREAD_REFLECT                = 0x1C02;

    // VGTilingMode
    public static final int VG_TILE_FILL                                = 0x1D00;
    public static final int VG_TILE_PAD                                 = 0x1D01;
    public static final int VG_TILE_REPEAT                              = 0x1D02;
    public static final int VG_TILE_REFLECT                             = 0x1D03;

    // VGImageFormat
    public static final int VG_sRGBX_8888                               =  0;
    public static final int VG_sRGBA_8888                               =  1;
    public static final int VG_sRGBA_8888_PRE                           =  2;
    public static final int VG_sRGB_565                                 =  3;
    public static final int VG_sRGBA_5551                               =  4;
    public static final int VG_sRGBA_4444                               =  5;
    public static final int VG_sL_8                                     =  6;
    public static final int VG_lRGBX_8888                               =  7;
    public static final int VG_lRGBA_8888                               =  8;
    public static final int VG_lRGBA_8888_PRE                           =  9;
    public static final int VG_lL_8                                     = 10;
    public static final int VG_A_8                                      = 11;
    public static final int VG_BW_1                                     = 12;
    // {A,X}RGB channel ordering
    public static final int VG_sXRGB_8888                               =  0 | (1 << 6);
    public static final int VG_sARGB_8888                               =  1 | (1 << 6);
    public static final int VG_sARGB_8888_PRE                           =  2 | (1 << 6);
    public static final int VG_sARGB_1555                               =  4 | (1 << 6);
    public static final int VG_sARGB_4444                               =  5 | (1 << 6);
    public static final int VG_lXRGB_8888                               =  7 | (1 << 6);
    public static final int VG_lARGB_8888                               =  8 | (1 << 6);
    public static final int VG_lARGB_8888_PRE                           =  9 | (1 << 6);
    // BGR{A,X} channel ordering
    public static final int VG_sBGRX_8888                               =  0 | (1 << 7);
    public static final int VG_sBGRA_8888                               =  1 | (1 << 7);
    public static final int VG_sBGRA_8888_PRE                           =  2 | (1 << 7);
    public static final int VG_sBGR_565                                 =  3 | (1 << 7);
    public static final int VG_sBGRA_5551                               =  4 | (1 << 7);
    public static final int VG_sBGRA_4444                               =  5 | (1 << 7);
    public static final int VG_lBGRX_8888                               =  7 | (1 << 7);
    public static final int VG_lBGRA_8888                               =  8 | (1 << 7);
    public static final int VG_lBGRA_8888_PRE                           =  9 | (1 << 7);
    // {A,X}BGR channel ordering
    public static final int VG_sXBGR_8888                               =  0 | (1 << 6) | (1 << 7);
    public static final int VG_sABGR_8888                               =  1 | (1 << 6) | (1 << 7);
    public static final int VG_sABGR_8888_PRE                           =  2 | (1 << 6) | (1 << 7);
    public static final int VG_sABGR_1555                               =  4 | (1 << 6) | (1 << 7);
    public static final int VG_sABGR_4444                               =  5 | (1 << 6) | (1 << 7);
    public static final int VG_lXBGR_8888                               =  7 | (1 << 6) | (1 << 7);
    public static final int VG_lABGR_8888                               =  8 | (1 << 6) | (1 << 7);
    public static final int VG_lABGR_8888_PRE                           =  9 | (1 << 6) | (1 << 7);

    // VGImageQuality
    public static final int VG_IMAGE_QUALITY_NONANTIALIASED             = (1 << 0);
    public static final int VG_IMAGE_QUALITY_FASTER                     = (1 << 1);
    public static final int VG_IMAGE_QUALITY_BETTER                     = (1 << 2);

    // VGImageParamType
    public static final int VG_IMAGE_FORMAT                             = 0x1E00;
    public static final int VG_IMAGE_WIDTH                              = 0x1E01;
    public static final int VG_IMAGE_HEIGHT                             = 0x1E02;

    // VGImageMode
    public static final int VG_DRAW_IMAGE_NORMAL                        = 0x1F00;
    public static final int VG_DRAW_IMAGE_MULTIPLY                      = 0x1F01;
    public static final int VG_DRAW_IMAGE_STENCIL                       = 0x1F02;

    // VGImageChannel
    public static final int VG_RED                                      = (1 << 3);
    public static final int VG_GREEN                                    = (1 << 2);
    public static final int VG_BLUE                                     = (1 << 1);
    public static final int VG_ALPHA                                    = (1 << 0);

    // VGBlendMode
    public static final int VG_BLEND_SRC                                = 0x2000;
    public static final int VG_BLEND_SRC_OVER                           = 0x2001;
    public static final int VG_BLEND_DST_OVER                           = 0x2002;
    public static final int VG_BLEND_SRC_IN                             = 0x2003;
    public static final int VG_BLEND_DST_IN                             = 0x2004;
    public static final int VG_BLEND_MULTIPLY                           = 0x2005;
    public static final int VG_BLEND_SCREEN                             = 0x2006;
    public static final int VG_BLEND_DARKEN                             = 0x2007;
    public static final int VG_BLEND_LIGHTEN                            = 0x2008;
    public static final int VG_BLEND_ADDITIVE                           = 0x2009;

    // VGHardwareQueryType
    public static final int VG_IMAGE_FORMAT_QUERY                       = 0x2100;
    public static final int VG_PATH_DATATYPE_QUERY                      = 0x2101;

    // VGHardwareQueryResult
    public static final int VG_HARDWARE_ACCELERATED                     = 0x2200;
    public static final int VG_HARDWARE_UNACCELERATED                   = 0x2201;

    // VGStringID
    public static final int VG_VENDOR                                   = 0x2300;
    public static final int VG_RENDERER                                 = 0x2301;
    public static final int VG_VERSION                                  = 0x2302;
    public static final int VG_EXTENSIONS                               = 0x2303;

    public int vgGetError();
    public void vgFlush();
    public void vgFinish();

    /*-------------------------------------------------------------------------------
                                     Getters and Setters
    -------------------------------------------------------------------------------*/
    public void vgSetf(int type, float value);
    public void vgSeti(int type, int value);
    public void vgSetfv(int type, int count, final float[] values, int offset);
    public void vgSetiv(int type, int count, final int[] values, int offset);
    public float vgGetf(int type);
    public int vgGeti(int type);
    public int vgGetVectorSize(int type);
    public void vgGetfv(int type, int count, float[] values, int offset);
    public void vgGetiv(int type, int count, int[] values, int offset);
    public void vgSetParameterf(VGHandle object, int paramType, float value);
    public void vgSetParameteri(VGHandle object, int paramType, int value);
    public void vgSetParameterfv(VGHandle object, int paramType, int count, final float[] values, int offset);
    public void vgSetParameteriv(VGHandle object, int paramType, int count, final int[] values, int offset);
    public float vgGetParameterf(VGHandle object, int paramType);
    public int vgGetParameteri(VGHandle object, int paramType);
    public int vgGetParameterVectorSize(VGHandle object, int paramType);
    public void vgGetParameterfv(VGHandle object, int paramType, int count, float[] values, int offset);
    public void vgGetParameteriv(VGHandle object, int paramType, int count, int[] values, int offset);

    /*-------------------------------------------------------------------------------
                                   Matrix Manipulation
    -------------------------------------------------------------------------------*/
    public void vgLoadIdentity();
    public void vgLoadMatrix(final float[] m, int offset);
    public void vgGetMatrix(float[] m, int offset);
    public void vgMultMatrix(final float[] m, int offset);
    public void vgTranslate(float tx, float ty);
    public void vgScale(float sx, float sy);
    public void vgShear(float shx, float shy);
    public void vgRotate(float angle);

    /*-------------------------------------------------------------------------------
                                  Masking and Clearing
    -------------------------------------------------------------------------------*/
    public void vgMask(VGImage mask, int operation, int x, int y, int width, int height);
    public void vgClear(int x, int y, int width, int height);

    /*-------------------------------------------------------------------------------
                                          Paths
    -------------------------------------------------------------------------------*/
    public VGPath vgCreatePath(int pathFormat, int datatype, float scale, float bias, int segmentCapacityHint, int coordCapacityHint, int capabilities);
    public void vgClearPath(VGPath path, int capabilities);
    public void vgDestroyPath(VGPath path);
    public void vgRemovePathCapabilities(VGPath path, int capabilities);
    public int vgGetPathCapabilities(VGPath path);
    public void vgAppendPath(VGPath dstPath, VGPath srcPath);
    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final byte[] pathData, int pathDataOffset);
    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final short[] pathData, int pathDataOffset);
    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final int[] pathData, int pathDataOffset);
    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final float[] pathData, int pathDataOffset);
    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final byte[] pathData, int offset);
    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final short[] pathData, int offset);
    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final int[] pathData, int offset);
    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final float[] pathData, int offset);
    public void vgTransformPath(VGPath dstPath, VGPath srcPath);
    public boolean vgInterpolatePath(VGPath dstPath, VGPath startPath, VGPath endPath, float amount);
    public float vgPathLength(VGPath path, int startSegment, int numSegments);
    public void vgPointAlongPath(VGPath path, int startSegment, int numSegments, float distance, float[] values, int offset);
    public void vgPathBounds(VGPath path, float[] values, int offset);
    public void vgPathTransformedBounds(VGPath path, float[] values, int offset);
    public void vgDrawPath(VGPath path, int paintModes);

    /*-------------------------------------------------------------------------------
                                          Paint
    -------------------------------------------------------------------------------*/
    public VGPaint vgCreatePaint();
    public void vgDestroyPaint(VGPaint paint);
    public void vgSetPaint(VGPaint paint, int paintModes);
    public VGPaint vgGetPaint(int paintMode);
    public void vgSetColor(VGPaint paint, int rgba);
    public int vgGetColor(VGPaint paint);
    public void vgPaintPattern(VGPaint paint, VGImage pattern);

    /*-------------------------------------------------------------------------------
                                          Images
    -------------------------------------------------------------------------------*/
    public VGImage vgCreateImage(int format, int width, int height, int allowedQuality);
    public void vgDestroyImage(VGImage image);
    public void vgClearImage(VGImage image, int x, int y, int width, int height);
    public void vgImageSubData(VGImage image, final byte[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height);
    public void vgImageSubData(VGImage image, final short[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height);
    public void vgImageSubData(VGImage image, final int[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height);
    public void vgGetImageSubData(VGImage image, byte[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height);
    public void vgGetImageSubData(VGImage image, short[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height);
    public void vgGetImageSubData(VGImage image, int[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height);
    public VGImage vgChildImage(VGImage parent, int x, int y, int width, int height);
    public VGImage vgGetParent(VGImage image);
    public void vgCopyImage(VGImage dst, int dx, int dy, VGImage src, int sx, int sy, int width, int height, boolean dither);
    public void vgDrawImage(VGImage image);
    public void vgSetPixels(int dx, int dy, VGImage src, int sx, int sy, int width, int height);
    public void vgWritePixels(final byte[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height);
    public void vgWritePixels(final short[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height);
    public void vgWritePixels(final int[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height);
    public void vgGetPixels(VGImage dst, int dx, int dy, int sx, int sy, int width, int height);
    public void vgReadPixels(byte[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height);
    public void vgReadPixels(short[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height);
    public void vgReadPixels(int[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height);
    public void vgCopyPixels(int dx, int dy, int sx, int sy, int width, int height);
    
    /*-------------------------------------------------------------------------------
                                      Image Filters
    -------------------------------------------------------------------------------*/
    public void vgColorMatrix(VGImage dst, VGImage src, final float[] matrix, int offset);
    public void vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, int offset, float scale, float bias, int tilingMode);
    public void vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, int kernelXOffset, final short[] kernelY, int kernelYOffset, float scale, float bias, int tilingMode);
    public void vgGaussianBlur(VGImage dst, VGImage src, float stdDeviationX, float stdDeviationY, int tilingMode);
    public void vgLookup(VGImage dst, VGImage src, final byte[] redLUT, int redLUTOffset, final byte[] greenLUT, int greenLUTOffset, final byte[] blueLUT, int blueLUTOffset, final byte[] alphaLUT, int alphaLUTOffset, boolean outputLinear, boolean outputPremultiplied);
    public void vgLookupSingle(VGImage dst, VGImage src, final int[] lookupTable, int offset, int sourceChannel, boolean outputLinear, boolean outputPremultiplied);

    /*-------------------------------------------------------------------------------
                                    Hardware Queries
    -------------------------------------------------------------------------------*/
    public int vgHardwareQuery(int key, int setting);
    public java.lang.String vgGetString(int name);
}
