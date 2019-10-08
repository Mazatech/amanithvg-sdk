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

public interface VG11Ext extends VG {

    /*-------------------------------------------------------------------------------
                                     KHR extensions
    *------------------------------------------------------------------------------*/

    // VG_KHR_iterative_average_blur
    public static final int VG_MAX_AVERAGE_BLUR_DIMENSION_KHR           = 0x116B;
    public static final int VG_AVERAGE_BLUR_DIMENSION_RESOLUTION_KHR    = 0x116C;
    public static final int VG_MAX_AVERAGE_BLUR_ITERATIONS_KHR          = 0x116D;

    public void vgIterativeAverageBlurKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, int tilingMode);

    // VG_KHR_advanced_blending
    public static final int VG_BLEND_OVERLAY_KHR                        = 0x2010;
    public static final int VG_BLEND_HARDLIGHT_KHR                      = 0x2011;
    public static final int VG_BLEND_SOFTLIGHT_SVG_KHR                  = 0x2012;
    public static final int VG_BLEND_SOFTLIGHT_KHR                      = 0x2013;
    public static final int VG_BLEND_COLORDODGE_KHR                     = 0x2014;
    public static final int VG_BLEND_COLORBURN_KHR                      = 0x2015;
    public static final int VG_BLEND_DIFFERENCE_KHR                     = 0x2016;
    public static final int VG_BLEND_SUBTRACT_KHR                       = 0x2017;
    public static final int VG_BLEND_INVERT_KHR                         = 0x2018;
    public static final int VG_BLEND_EXCLUSION_KHR                      = 0x2019;
    public static final int VG_BLEND_LINEARDODGE_KHR                    = 0x201a;
    public static final int VG_BLEND_LINEARBURN_KHR                     = 0x201b;
    public static final int VG_BLEND_VIVIDLIGHT_KHR                     = 0x201c;
    public static final int VG_BLEND_LINEARLIGHT_KHR                    = 0x201d;
    public static final int VG_BLEND_PINLIGHT_KHR                       = 0x201e;
    public static final int VG_BLEND_HARDMIX_KHR                        = 0x201f;
    public static final int VG_BLEND_CLEAR_KHR                          = 0x2020;
    public static final int VG_BLEND_DST_KHR                            = 0x2021;
    public static final int VG_BLEND_SRC_OUT_KHR                        = 0x2022;
    public static final int VG_BLEND_DST_OUT_KHR                        = 0x2023;
    public static final int VG_BLEND_SRC_ATOP_KHR                       = 0x2024;
    public static final int VG_BLEND_DST_ATOP_KHR                       = 0x2025;
    public static final int VG_BLEND_XOR_KHR                            = 0x2026;

    // VG_KHR_parametric_filter
    public static final int VG_PF_OBJECT_VISIBLE_FLAG_KHR               = (1 << 0);
    public static final int VG_PF_KNOCKOUT_FLAG_KHR                     = (1 << 1);
    public static final int VG_PF_OUTER_FLAG_KHR                        = (1 << 2);
    public static final int VG_PF_INNER_FLAG_KHR                        = (1 << 3);
    public static final int VGU_IMAGE_IN_USE_ERROR                      = 0xF010;

    public void vgParametricFilterKHR(VGImage dst, VGImage src, VGImage blur, float strength, float offsetX, float offsetY, int filterFlags, VGPaint highlightPaint, VGPaint shadowPaint);
    public int vguDropShadowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int shadowColorRGBA);
    public int vguGlowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, int filterFlags, int allowedQuality, int glowColorRGBA);
    public int vguBevelKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int highlightColorRGBA, int shadowColorRGBA);
    public int vguGradientGlowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final float[] glowColorRampStops, int offset);
    public int vguGradientBevelKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final float[] bevelColorRampStops, int offset);

    /*-------------------------------------------------------------------------------
                                    NDS extensions
    *------------------------------------------------------------------------------*/   

    // VG_NDS_paint_generation
    public static final int VG_PAINT_COLOR_RAMP_LINEAR_NDS              = 0x1A10;
    public static final int VG_COLOR_MATRIX_NDS                         = 0x1A11;
    public static final int VG_PAINT_COLOR_TRANSFORM_LINEAR_NDS         = 0x1A12;
    public static final int VG_DRAW_IMAGE_COLOR_MATRIX_NDS              = 0x1F10;

    // VG_NDS_projective_geometry
    public static final int VG_CLIP_MODE_NDS                            = 0x1180;
    public static final int VG_CLIP_LINES_NDS                           = 0x1181;
    public static final int VG_MAX_CLIP_LINES_NDS                       = 0x1182;
    public static final int VG_CLIPMODE_NONE_NDS                        = 0x3000;
    public static final int VG_CLIPMODE_CLIP_CLOSED_NDS                 = 0x3001;
    public static final int VG_CLIPMODE_CLIP_OPEN_NDS                   = 0x3002;
    public static final int VG_CLIPMODE_CULL_NDS                        = 0x3003;
    public static final int VG_RQUAD_TO_NDS                             = ( 13 << 1 );
    public static final int VG_RCUBIC_TO_NDS                            = ( 14 << 1 );
    public static final int VG_RQUAD_TO_ABS_NDS                         = (VG_RQUAD_TO_NDS  | VG101.VG_ABSOLUTE);
    public static final int VG_RQUAD_TO_REL_NDS                         = (VG_RQUAD_TO_NDS  | VG101.VG_RELATIVE);
    public static final int VG_RCUBIC_TO_ABS_NDS                        = (VG_RCUBIC_TO_NDS | VG101.VG_ABSOLUTE);
    public static final int VG_RCUBIC_TO_REL_NDS                        = (VG_RCUBIC_TO_NDS | VG101.VG_RELATIVE);

    public void vgProjectiveMatrixNDS(boolean enable);
    public int vguTransformClipLineNDS(final float Ain, final float Bin, final float Cin, final float[] matrix, int matrixOffset, final boolean inverse, float[] Aout, int AoutOffset, float[] Bout, int BoutOffset, float[] Cout, int CoutOffset);

    /*-------------------------------------------------------------------------------
                                  AmanithVG extensions
    *------------------------------------------------------------------------------*/   

    // VG_MZT_separable_cap_style
    public static final int VG_STROKE_START_CAP_STYLE_MZT               = 0x1192;
    public static final int VG_STROKE_END_CAP_STYLE_MZT                 = 0x1193;

    // VG_MZT_separable_blend_modes
    public static final int VG_STROKE_BLEND_MODE_MZT                    = 0x1190;
    public static final int VG_FILL_BLEND_MODE_MZT                      = 0x1191;

    // VG_MZT_color_ramp_interpolation
    public static final int VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT  = 0x1A91;
    public static final int VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT      = 0x1C90;
    public static final int VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT      = 0x1C91;

    // VG_MZT_conical_gradient
    public static final int VG_PAINT_CONICAL_GRADIENT_MZT               = 0x1A90;
    public static final int VG_PAINT_TYPE_CONICAL_GRADIENT_MZT          = 0x1B90;

    // VG_MZT_advanced_blend_modes
    public static final int VG_BLEND_CLEAR_MZT                          = 0x2090;
    public static final int VG_BLEND_DST_MZT                            = 0x2091;
    public static final int VG_BLEND_SRC_OUT_MZT                        = 0x2092;
    public static final int VG_BLEND_DST_OUT_MZT                        = 0x2093;
    public static final int VG_BLEND_SRC_ATOP_MZT                       = 0x2094;
    public static final int VG_BLEND_DST_ATOP_MZT                       = 0x2095;
    public static final int VG_BLEND_XOR_MZT                            = 0x2096;
    public static final int VG_BLEND_OVERLAY_MZT                        = 0x2097;
    public static final int VG_BLEND_COLOR_DODGE_MZT                    = 0x2098;
    public static final int VG_BLEND_COLOR_BURN_MZT                     = 0x2099;
    public static final int VG_BLEND_HARD_LIGHT_MZT                     = 0x209A;
    public static final int VG_BLEND_SOFT_LIGHT_MZT                     = 0x209B;
    public static final int VG_BLEND_DIFFERENCE_MZT                     = 0x209C;
    public static final int VG_BLEND_EXCLUSION_MZT                      = 0x209D;

    // VG_MZT_clip_path
    public static final int VG_CLIP_RULE_MZT                            = 0x1194;
    public static final int VG_CLIPPING_MZT                             = 0x1195;
    public static final int VG_MATRIX_CLIP_USER_TO_SURFACE_MZT          = 0x1405;
    // Push a new clip path
    public void vgClipPathPushMZT(VGPath path);
    // Pop out the last pushed clip path
    public void vgClipPathPopMZT();
    // Clear the whole clip paths queue
    public void vgClipPathClearMZT();

    // VG_MZT_filters
    public void vgColorMatrixMZT(VGImage img, final float[] matrix);

    // VG_MZT_mask
    public void vgMaskMZT(VGHandle mask, int operation, int x, int y, int width, int height);

    // create OpenVG context
    public long vgPrivContextCreateMZT(long sharedContext);
    public void vgPrivContextDestroyMZT(long context);
    // create OpenVG surface
    public long vgPrivSurfaceCreateMZT(int width, int height, boolean linearColorSpace, boolean alphaPremultiplied, boolean alphaMask);
    public long vgPrivSurfaceCreateFromImageMZT(VGImage image, boolean alphaMask);
    // resize OpenVG surface
    public boolean vgPrivSurfaceResizeMZT(long surface, int width, int height);
    public void vgPrivSurfaceDestroyMZT(long surface);
    // get dimensions and format of the specified drawing surface
    public int vgPrivGetSurfaceWidthMZT(final long surface);
    public int vgPrivGetSurfaceHeightMZT(final long surface);
    public int vgPrivGetSurfaceFormatMZT(final long surface);
    // get pointer to surface pixels (AmanithVG SRE only)
    public java.nio.ByteBuffer vgPrivGetSurfacePixelsMZT(final long surface);
    // copy surface content into specified pixels buffer (AmanithVG SRE only)
    public boolean vgPrivSurfaceCopyPixelsMZT(final long surface, int[] dstPixels, int offset, boolean redBlueSwap);
    // bind OpenVG context with an OpenVG drawing surface, defining the "current context" and the "current surface"
    public boolean vgPrivMakeCurrentMZT(long context, long surface);
    // get dimensions and format of the currently bound drawing surface
    public int vgGetSurfaceWidthMZT();
    public int vgGetSurfaceHeightMZT();
    public int vgGetSurfaceFormatMZT();
    // get pointer to pixels of the currently bound drawing surface (AmanithVG SRE only)
    public java.nio.ByteBuffer vgGetSurfacePixelsMZT();
    // copy currently bound surface content into specified pixels buffer (AmanithVG SRE only)
    public boolean vgSurfaceCopyPixelsMZT(int[] pixels, int offset, boolean redBlueSwap);
    public void vgPostSwapBuffersMZT();
}
