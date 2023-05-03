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

public interface VG11Ext extends VG {

    /*-------------------------------------------------------------------------------
                                     KHR extensions
    *------------------------------------------------------------------------------*/

    // VG_KHR_iterative_average_blur
    public static final int VG_MAX_AVERAGE_BLUR_DIMENSION_KHR                 = 0x116B;
    public static final int VG_AVERAGE_BLUR_DIMENSION_RESOLUTION_KHR          = 0x116C;
    public static final int VG_MAX_AVERAGE_BLUR_ITERATIONS_KHR                = 0x116D;

    public void vgIterativeAverageBlurKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, int tilingMode);

    // VG_KHR_advanced_blending
    public static final int VG_BLEND_OVERLAY_KHR                              = 0x2010;
    public static final int VG_BLEND_HARDLIGHT_KHR                            = 0x2011;
    public static final int VG_BLEND_SOFTLIGHT_SVG_KHR                        = 0x2012;
    public static final int VG_BLEND_SOFTLIGHT_KHR                            = 0x2013;
    public static final int VG_BLEND_COLORDODGE_KHR                           = 0x2014;
    public static final int VG_BLEND_COLORBURN_KHR                            = 0x2015;
    public static final int VG_BLEND_DIFFERENCE_KHR                           = 0x2016;
    public static final int VG_BLEND_SUBTRACT_KHR                             = 0x2017;
    public static final int VG_BLEND_INVERT_KHR                               = 0x2018;
    public static final int VG_BLEND_EXCLUSION_KHR                            = 0x2019;
    public static final int VG_BLEND_LINEARDODGE_KHR                          = 0x201a;
    public static final int VG_BLEND_LINEARBURN_KHR                           = 0x201b;
    public static final int VG_BLEND_VIVIDLIGHT_KHR                           = 0x201c;
    public static final int VG_BLEND_LINEARLIGHT_KHR                          = 0x201d;
    public static final int VG_BLEND_PINLIGHT_KHR                             = 0x201e;
    public static final int VG_BLEND_HARDMIX_KHR                              = 0x201f;
    public static final int VG_BLEND_CLEAR_KHR                                = 0x2020;
    public static final int VG_BLEND_DST_KHR                                  = 0x2021;
    public static final int VG_BLEND_SRC_OUT_KHR                              = 0x2022;
    public static final int VG_BLEND_DST_OUT_KHR                              = 0x2023;
    public static final int VG_BLEND_SRC_ATOP_KHR                             = 0x2024;
    public static final int VG_BLEND_DST_ATOP_KHR                             = 0x2025;
    public static final int VG_BLEND_XOR_KHR                                  = 0x2026;

    // VG_KHR_parametric_filter
    public static final int VG_PF_OBJECT_VISIBLE_FLAG_KHR                     = (1 << 0);
    public static final int VG_PF_KNOCKOUT_FLAG_KHR                           = (1 << 1);
    public static final int VG_PF_OUTER_FLAG_KHR                              = (1 << 2);
    public static final int VG_PF_INNER_FLAG_KHR                              = (1 << 3);
    public static final int VGU_IMAGE_IN_USE_ERROR                            = 0xF010;

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
    public static final int VG_PAINT_COLOR_RAMP_LINEAR_NDS                    = 0x1A10;
    public static final int VG_COLOR_MATRIX_NDS                               = 0x1A11;
    public static final int VG_PAINT_COLOR_TRANSFORM_LINEAR_NDS               = 0x1A12;
    public static final int VG_DRAW_IMAGE_COLOR_MATRIX_NDS                    = 0x1F10;

    // VG_NDS_projective_geometry
    public static final int VG_CLIP_MODE_NDS                                  = 0x1180;
    public static final int VG_CLIP_LINES_NDS                                 = 0x1181;
    public static final int VG_MAX_CLIP_LINES_NDS                             = 0x1182;
    public static final int VG_CLIPMODE_NONE_NDS                              = 0x3000;
    public static final int VG_CLIPMODE_CLIP_CLOSED_NDS                       = 0x3001;
    public static final int VG_CLIPMODE_CLIP_OPEN_NDS                         = 0x3002;
    public static final int VG_CLIPMODE_CULL_NDS                              = 0x3003;
    public static final int VG_RQUAD_TO_NDS                                   = ( 13 << 1 );
    public static final int VG_RCUBIC_TO_NDS                                  = ( 14 << 1 );
    public static final int VG_RQUAD_TO_ABS_NDS                               = (VG_RQUAD_TO_NDS  | VG101.VG_ABSOLUTE);
    public static final int VG_RQUAD_TO_REL_NDS                               = (VG_RQUAD_TO_NDS  | VG101.VG_RELATIVE);
    public static final int VG_RCUBIC_TO_ABS_NDS                              = (VG_RCUBIC_TO_NDS | VG101.VG_ABSOLUTE);
    public static final int VG_RCUBIC_TO_REL_NDS                              = (VG_RCUBIC_TO_NDS | VG101.VG_RELATIVE);

    public void vgProjectiveMatrixNDS(boolean enable);
    public int vguTransformClipLineNDS(final float Ain, final float Bin, final float Cin, final float[] matrix, int matrixOffset, final boolean inverse, float[] Aout, int AoutOffset, float[] Bout, int BoutOffset, float[] Cout, int CoutOffset);

    /*-------------------------------------------------------------------------------
                                  AmanithVG extensions
    *------------------------------------------------------------------------------*/   
    // VGBuffersDisabledMzt
    public static final int VG_BUFFERS_DISABLED_NONE_MZT                      = 0;
    public static final int VG_BUFFERS_DISABLED_DEPTH_MZT                     = 1;
    public static final int VG_BUFFERS_DISABLED_STENCIL_MZT                   = 2;

    // VGForceImageTextureBordersMzt
    public static final int VG_FORCE_IMAGE_TEXTURE_BORDERS_NONE_MZT           = 0;
    public static final int VG_FORCE_IMAGE_TEXTURE_BORDERS_WHOLE_MZT          = -1;

    // VGImageTextureBordersModeMzt
    public static final int VG_TEXTURE_BORDER_MODE_CLEAR_MZT                  = 0;
    public static final int VG_TEXTURE_BORDER_MODE_COPY_MZT                   = 1;
    public static final int VG_TEXTURE_BORDER_MODE_COPY_ZERO_ALPHA_MZT        = 2;

    // Configuration parameters and thresholds (VGConfigMzt)
    //
    // [READ-ONLY]
    //
    // The maximum number of different threads that can "work" (e.g. create surfaces, draw paths, etc) concurrently.
    public static final int VG_CONFIG_MAX_CURRENT_THREADS_MZT                 = 0x5300;
    // The maximum dimension allowed for drawing surfaces, in pixels. This is the maximum valid value that can be specified as
    // 'width' and 'height' for the vgPrivSurfaceCreateMZT and vgPrivSurfaceResizeMZT functions.
    public static final int VG_CONFIG_MAX_SURFACE_DIMENSION_MZT               = 0x5301;
    //
    //
    // [Geometry]
    //
    // Used by AmanithVG geometric kernel to approximate curves with straight line
    // segments(flattening). Valid range is [0; 100], where 100 represents the best quality.
    public static final int VG_CONFIG_CURVES_QUALITY_MZT                      = 0x5302;
    // Used by radial gradient paints, only in non-shader pipelines (AmanithVG GLE only).
    // Valid range is [0; 100], where 100 represents the best quality.
    public static final int VG_CONFIG_RADIAL_GRADIENTS_QUALITY_MZT            = 0x5303;
    // Used by conical gradient paints, only in non-shader pipelines (AmanithVG GLE only).
    // If VG_MZT_conical_gradient extension is not available, this parameter has no effects.
    // Valid range is [0; 100], where 100 represents the best quality.
    public static final int VG_CONFIG_CONICAL_GRADIENTS_QUALITY_MZT           = 0x5304;
    //
    //
    // [Memory]
    //
    // Number of OpenVG calls (handles creation / destruction and drawing functions) to be
    // done before to recover / retrieve unused memory. Must be a positive number.
    // If 0 is specified, unused memory will never be recovered from internal structures and
    // memory pools.
    public static final int VG_CONFIG_CALLS_BEFORE_MEMORY_RECOVERY_MZT        = 0x5305;
    //
    //
    // [Rasterizer cache] - AmanithVG SRE only
    //
    // Disable paths rasterizer caching if (screen space) bounding box width exceeds this value.
    // Valid values are in the range [0; 4096]. Use 0 to disable rasterizer caching.
    public static final int VG_CONFIG_MAX_CACHING_BOX_WIDTH_MZT               = 0x5306;
    // Disable paths rasterizer caching if (screen space) bounding box height exceeds this value.
    // Valid values are in the range [0; 4096]. Use 0 to disable rasterizer caching.
    public static final int VG_CONFIG_MAX_CACHING_BOX_HEIGHT_MZT              = 0x5307;
    //
    //
    // [OpenGL] / [OpenGL ES] - AmanithVG GLE only
    //
    // Avoid the use of GL_EXT_texture_rectangle or GL_ARB_texture_rectangle extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_RECT_TEXTURES_DISABLED_MZT        = 0x5308;
    // Avoid the use of GL_ARB_texture_mirrored_repeat extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_MIRRORED_REPEAT_DISABLED_MZT      = 0x5309;
    // Avoid the use of GL_ARB_texture_border_clamp extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_CLAMP_TO_BORDER_DISABLED_MZT      = 0x530A;
    // Avoid the use of GL_EXT_blend_minmax extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_BLEND_MIN_MAX_DISABLED_MZT        = 0x530B;
    // Avoid the use of GL_EXT_texture_env_dot3 or GL_ARB_texture_env_dot3 extension, even if supported by the GL Graphic System.
    // WARNING: setting this parameter as true will compromise the correct drawing of images in stencil image mode.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_DOT3_DISABLED_MZT                 = 0x530C;
    // Avoid the use of Vertex Buffer Objects (VBO), even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_VBO_DISABLED_MZT                  = 0x530D;
    // Force the maximum number of texture units that AmanithVG can use.
    // Valid values are 1, 2, 3, 4 (AmanithVG uses no more than 4 texture units; at least 2
    // texture units are always required to implement the whole OpenVG features set).
    public static final int VG_CONFIG_MAX_PERMITTED_TEXTURE_UNITS_MZT         = 0x530E;
    // Force the maximum texture size that AmanithVG can use.
    // Valid values are 0 (autodetect), 64, 128, 256, 512, 1024, 2048, 4096, 8192. Other values will be ignored.
    public static final int VG_CONFIG_MAX_TEXTURE_SIZE_MZT                    = 0x530F;
    // When both depth and stencil buffers are available on the GL context, it forces the specified buffer to be unused by AmanithVG.
    // Valid values are defined by the VGBuffersDisabledMzt enum type. Other values will be ignored.
    public static final int VG_CONFIG_FORCE_BUFFERS_DISABLED_MZT              = 0x5310;
    // Suppose depth and stencil buffers to be persistent after a swapBuffers call.
    // Please note that, while on desktop platforms persistent buffers are common,
    // the same is not so common on embedded (OpenGL ES) platforms.
    // WARNING: setting this parameter as true on GL Graphic System with non-persistent
    // buffers, will compromise a correct rendering on some specific OpenVG features.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_SUPPOSE_PERSISTENT_BUFFERS_MZT          = 0x5311;
    // Avoid the use of GL scissor feature, even if supported by the GL Graphic System.
    // WARNING: this parameter is provided to address compatibility issues; setting this
    // parameter as true when the stencil buffer is not available to AmanithVG, will
    // compromise a correct rendering on some specific OpenVG features.
    // Furthermore it will have a negative impact on performance.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_SCISSOR_DISABLED_MZT              = 0x5312;
    // Avoid the use of GL color masking feature, even if supported by the GL Graphic System.
    // WARNING: this parameter is provided to address compatibility issues; setting this parameter
    // as true will compromise a correct rendering on some specific OpenVG features.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_COLOR_MASKING_DISABLED_MZT        = 0x5313;
    // Force the use of mipmaps on gradient textures.
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_MIPMAPS_ON_GRADIENTS_MZT          = 0x5314;
    // Force dithering on gradient textures, when the drawing surface is configured
    // to have less than 8bit per color component (e.g.RGB565).
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_DITHERING_ON_GRADIENTS_MZT        = 0x5315;
    // Force dithering on image textures, when the drawing surface is configured
    // to have less than 8bit per color component (e.g.RGB565).
    // Valid values are VG_FALSE and VG_TRUE
    public static final int VG_CONFIG_FORCE_DITHERING_ON_IMAGES_MZT           = 0x5316;
    // If VG_TRUE, it forces GL_RGBA texture format even for opaque paint / images.
    // If VG_FALSE, GL_RGB texture format for opaque paint / images will be used.
    public static final int VG_CONFIG_FORCE_RGBA_TEXTURES_MZT                 = 0x5317;
    // If different than VG_FORCE_IMAGE_TEXTURE_BORDERS_NONE_MZT, it forces the upload of VGImage textures with an additional
    // filled border. If VG_FORCE_IMAGE_TEXTURE_BORDERS_NONE_MZT, images are uploaded without additional borders.
    // Negative values are treated as VG_FORCE_IMAGE_TEXTURE_BORDERS_WHOLE_MZT.
    // WARNING: this parameter is provided to address compatibility issues related to u-v coordinates
    // generation throught the GL_TEXTURE matrix, on not conformant GL Graphic System. By setting this parameter as
    // VG_FORCE_IMAGE_TEXTURE_BORDERS_WHOLE_MZT or to a positive number, it will require additional memory and could
    // impact on performance (when drawing images for the first time).
    // Valid values are defined by the VGForceImageTextureBordersMzt enum type.
    public static final int VG_CONFIG_FORCE_IMAGE_TEXTURE_BORDERS_MZT         = 0x5318;
    // In conjunction with VG_CONFIG_FORCE_IMAGE_TEXTURE_BORDERS_MZT parameter, it specifies
    // how VGImage texture borders must be filled. If VG_TEXTURE_BORDER_MODE_CLEAR_MZT, borders are filled with
    // a transparent black; if VG_TEXTURE_BORDER_MODE_COPY_MZT, borders are filled by duplicating pixels on image
    // edges; if VG_TEXTURE_BORDER_MODE_COPY_ZERO_ALPHA_MZT, borders are filled by duplicating pixels on image edges
    // and overriding their alpha value with 0.
    // Valid values are defined by the VGImageTextureBordersModeMzt enum type.
    public static final int VG_CONFIG_IMAGE_TEXTURE_BORDERS_MODE_MZT          = 0x5319;
    //
    // Standard deviation factor for Gaussian blur filter. It represents the factor by which the
    // sigma value is multiplied to obtain the amplitude (of the Gaussian function) to be taken
    // for the blur. Must be a positive number.
    // The default value of 3.0 guarantees a coverage of the Gaussian curve equal to 99.7%
    public static final int VG_CONFIG_FILTER_GAUSSIAN_SIGMA_FACTOR_MZT        = 0x531A;

    // VGStringIDMzt (extension to the enum VGStringID type)
    public static final int VG_COMPILE_CONFIG_INFO_MZT                        = 0x2305;

    // VG_MZT_separable_cap_style
    public static final int VG_STROKE_START_CAP_STYLE_MZT                     = 0x1192;
    public static final int VG_STROKE_END_CAP_STYLE_MZT                       = 0x1193;

    // VG_MZT_separable_blend_modes
    public static final int VG_STROKE_BLEND_MODE_MZT                          = 0x1190;
    public static final int VG_FILL_BLEND_MODE_MZT                            = 0x1191;

    // VG_MZT_color_ramp_interpolation
    public static final int VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT        = 0x1A91;
    public static final int VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT            = 0x1C90;
    public static final int VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT            = 0x1C91;

    // VG_MZT_conical_gradient
    public static final int VG_PAINT_CONICAL_GRADIENT_MZT                     = 0x1A90;
    public static final int VG_PAINT_TYPE_CONICAL_GRADIENT_MZT                = 0x1B90;

    // VG_MZT_advanced_blend_modes
    public static final int VG_BLEND_CLEAR_MZT                                = 0x2090;
    public static final int VG_BLEND_DST_MZT                                  = 0x2091;
    public static final int VG_BLEND_SRC_OUT_MZT                              = 0x2092;
    public static final int VG_BLEND_DST_OUT_MZT                              = 0x2093;
    public static final int VG_BLEND_SRC_ATOP_MZT                             = 0x2094;
    public static final int VG_BLEND_DST_ATOP_MZT                             = 0x2095;
    public static final int VG_BLEND_XOR_MZT                                  = 0x2096;
    public static final int VG_BLEND_OVERLAY_MZT                              = 0x2097;
    public static final int VG_BLEND_COLOR_DODGE_MZT                          = 0x2098;
    public static final int VG_BLEND_COLOR_BURN_MZT                           = 0x2099;
    public static final int VG_BLEND_HARD_LIGHT_MZT                           = 0x209A;
    public static final int VG_BLEND_SOFT_LIGHT_MZT                           = 0x209B;
    public static final int VG_BLEND_DIFFERENCE_MZT                           = 0x209C;
    public static final int VG_BLEND_EXCLUSION_MZT                            = 0x209D;

    // VG_MZT_clip_path
    public static final int VG_CLIP_RULE_MZT                                  = 0x1194;
    public static final int VG_CLIPPING_MZT                                   = 0x1195;
    public static final int VG_MATRIX_CLIP_USER_TO_SURFACE_MZT                = 0x1405;
    // Push a new clip path
    public void vgClipPathPushMZT(VGPath path, boolean advanceLayer);
    // Pop out the last pushed clip path
    public void vgClipPathPopMZT();
    // Clear the whole clip paths queue
    public void vgClipPathClearMZT();

    // VG_MZT_filters

    // VGLightTypeMzt
    public static final int VG_LIGHT_TYPE_DISTANT_MZT                         = 0x2401;
    public static final int VG_LIGHT_TYPE_POINT_MZT                           = 0x2402;
    public static final int VG_LIGHT_TYPE_SPOT_MZT                            = 0x2403;

    // VGCompositeOpMzt
    public static final int VG_COMPOSITE_OVER_MZT                             = 0x2501;
    public static final int VG_COMPOSITE_MULTIPLY_MZT                         = 0x2502;
    public static final int VG_COMPOSITE_SCREEN_MZT                           = 0x2503;
    public static final int VG_COMPOSITE_DARKEN_MZT                           = 0x2504;
    public static final int VG_COMPOSITE_LIGHTEN_MZT                          = 0x2505;
    public static final int VG_COMPOSITE_IN_MZT                               = 0x2506;
    public static final int VG_COMPOSITE_OUT_MZT                              = 0x2507;
    public static final int VG_COMPOSITE_ATOP_MZT                             = 0x2508;
    public static final int VG_COMPOSITE_XOR_MZT                              = 0x2509;
    public static final int VG_COMPOSITE_ARITHMETIC_MZT                       = 0x250A;

    public void vgColorMatrixMZT(VGImage dst, VGImage src, final float[] matrix, int offset);
    void vgGaussianBlurMZT(VGImage dst, VGImage src, final float[] axes, int offset, float stdDeviationX, float stdDeviationY, int tilingMode, boolean useFastApprox);
    public void vgLightingMZT(VGImage dstDiffuse, VGImage dstSpecular, VGImage src, float surfaceScale, float diffuseConstant, float specularConstant, float specularExponent, int lightType, final float[] lightData, int ldOffset);
    public void vgMorphologyMZT(VGImage dst, VGImage src, boolean erode, final float[] axes, int offset, int radiusX, int radiusY);
    public void vgTurbulenceMZT(VGImage image, float biasX, float biasY, float scaleX, float scaleY, float baseFrequencyX, float baseFrequencyY, int numOctaves, int seed, boolean stitchTiles, boolean fractalNoise);
    public void vgDisplacementMapMZT(VGImage dst, VGImage src, VGImage map, float scaleX, float scaleY, int tilingMode, int xChannelSelector, int yChannelSelector);
    public void vgCompositeMZT(VGImage dst, VGImage in1, VGImage in2, int operation, float k1, float k2, float k3, float k4);

    // VG_MZT_mask
    public void vgMaskMZT(VGImage mask, int operation, int x, int y, int width, int height);
    public void vgMaskMZT(VGMaskLayer mask, int operation, int x, int y, int width, int height);

    // configure parameters and thresholds for the AmanithVG library
    public int vgConfigSetMZT(int config, float value);
    // get the current value relative to the specified configuration parameter
    public float vgConfigGetMZT(int config);

    // initialize the library; after initialization, it is possible to create contexts and drawing surfaces
    public boolean vgInitializeMZT();
    // terminate the library; all "pending" resources (e.g. contexts and drawing surfaces) not yet destroyed are released and deallocated
    public void vgTerminateMZT();
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
