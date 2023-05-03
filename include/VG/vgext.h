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
   This file is a modified version of the sample implementation of vgext.h, version 1.1
   The original copyright and permission notice, RELATED TO THIS FILE ONLY, are reported below:
*/

/* $Revision: 6810 $ on $Date:: 2008-10-29 07:31:37 -0700 #$ */

/*------------------------------------------------------------------------
 * 
 * VG extensions Reference Implementation
 * -------------------------------------
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
 * \brief VG extensions
 *//*-------------------------------------------------------------------*/

#ifndef VGEXT_H
#define VGEXT_H

#ifdef __cplusplus
extern "C" {
#endif

#include <VG/openvg.h>
#include <VG/vgu.h>

#ifndef VG_API_ENTRYP
#   define VG_API_ENTRYP VG_API_ENTRY*
#endif

#ifndef VGU_API_ENTRYP
#   define VGU_API_ENTRYP VGU_API_ENTRY*
#endif

/*-------------------------------------------------------------------------------
 * KHR extensions
 *------------------------------------------------------------------------------*/

typedef enum  {

#ifndef VG_KHR_iterative_average_blur
    VG_MAX_AVERAGE_BLUR_DIMENSION_KHR        = 0x116B,
    VG_AVERAGE_BLUR_DIMENSION_RESOLUTION_KHR = 0x116C,
    VG_MAX_AVERAGE_BLUR_ITERATIONS_KHR       = 0x116D,
#endif

  VG_PARAM_TYPE_KHR_FORCE_SIZE             = VG_MAX_ENUM
} VGParamTypeKHR;

#ifndef VG_KHR_EGL_image
#define VG_KHR_EGL_image 1
/* VGEGLImageKHR is an opaque handle to an EGLImage */
typedef void* VGeglImageKHR; 

#ifdef VG_VGEXT_PROTOTYPES
VG_API_CALL VGImage VG_API_ENTRY vgCreateEGLImageTargetKHR(VGeglImageKHR image);
#endif
typedef VGImage (VG_API_ENTRYP PFNVGCREATEEGLIMAGETARGETKHRPROC) (VGeglImageKHR image);

#endif


#ifndef VG_KHR_iterative_average_blur
#define VG_KHR_iterative_average_blur 1

#ifdef VG_VGEXT_PROTOTYPES
VG_API_CALL void vgIterativeAverageBlurKHR(VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGTilingMode tilingMode);
#endif 
typedef void (VG_API_ENTRYP PFNVGITERATIVEAVERAGEBLURKHRPROC) (VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGTilingMode tilingMode);

#endif


#ifndef VG_KHR_advanced_blending
#define VG_KHR_advanced_blending 1

typedef enum {
    VG_BLEND_OVERLAY_KHR        = 0x2010,
    VG_BLEND_HARDLIGHT_KHR      = 0x2011,
    VG_BLEND_SOFTLIGHT_SVG_KHR  = 0x2012,
    VG_BLEND_SOFTLIGHT_KHR      = 0x2013,
    VG_BLEND_COLORDODGE_KHR     = 0x2014,
    VG_BLEND_COLORBURN_KHR      = 0x2015,
    VG_BLEND_DIFFERENCE_KHR     = 0x2016,
    VG_BLEND_SUBTRACT_KHR       = 0x2017,
    VG_BLEND_INVERT_KHR         = 0x2018,
    VG_BLEND_EXCLUSION_KHR      = 0x2019,
    VG_BLEND_LINEARDODGE_KHR    = 0x201a,
    VG_BLEND_LINEARBURN_KHR     = 0x201b,
    VG_BLEND_VIVIDLIGHT_KHR     = 0x201c,
    VG_BLEND_LINEARLIGHT_KHR    = 0x201d,
    VG_BLEND_PINLIGHT_KHR       = 0x201e,
    VG_BLEND_HARDMIX_KHR        = 0x201f,
    VG_BLEND_CLEAR_KHR          = 0x2020,
    VG_BLEND_DST_KHR            = 0x2021,
    VG_BLEND_SRC_OUT_KHR        = 0x2022,
    VG_BLEND_DST_OUT_KHR        = 0x2023,
    VG_BLEND_SRC_ATOP_KHR       = 0x2024,
    VG_BLEND_DST_ATOP_KHR       = 0x2025,
    VG_BLEND_XOR_KHR            = 0x2026,

    VG_BLEND_MODE_KHR_FORCE_SIZE= VG_MAX_ENUM
} VGBlendModeKHR;
#endif

#ifndef VG_KHR_parametric_filter
#define VG_KHR_parametric_filter 1 

typedef enum {
    VG_PF_OBJECT_VISIBLE_FLAG_KHR = (1 << 0),
    VG_PF_KNOCKOUT_FLAG_KHR       = (1 << 1),
    VG_PF_OUTER_FLAG_KHR          = (1 << 2),
    VG_PF_INNER_FLAG_KHR          = (1 << 3),

    VG_PF_TYPE_KHR_FORCE_SIZE     = VG_MAX_ENUM
} VGPfTypeKHR;

typedef enum {
    VGU_IMAGE_IN_USE_ERROR           = 0xF010,

    VGU_ERROR_CODE_KHR_FORCE_SIZE    = VG_MAX_ENUM
} VGUErrorCodeKHR;

#ifdef VG_VGEXT_PROTOTYPES
VG_API_CALL void VG_API_ENTRY vgParametricFilterKHR(VGImage dst,VGImage src,VGImage blur,VGfloat strength,VGfloat offsetX,VGfloat offsetY,VGbitfield filterFlags,VGPaint highlightPaint,VGPaint shadowPaint);
VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguDropShadowKHR(VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint shadowColorRGBA);
VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguGlowKHR(VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint glowColorRGBA) ;
VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguBevelKHR(VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint highlightColorRGBA,VGuint shadowColorRGBA);
VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguGradientGlowKHR(VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint stopsCount,const VGfloat* glowColorRampStops);
VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguGradientBevelKHR(VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint stopsCount,const VGfloat* bevelColorRampStops);
#endif
typedef void (VG_API_ENTRYP PFNVGPARAMETRICFILTERKHRPROC) (VGImage dst,VGImage src,VGImage blur,VGfloat strength,VGfloat offsetX,VGfloat offsetY,VGbitfield filterFlags,VGPaint highlightPaint,VGPaint shadowPaint);
typedef VGUErrorCode (VGU_API_ENTRYP PFNVGUDROPSHADOWKHRPROC) (VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint shadowColorRGBA);
typedef VGUErrorCode (VGU_API_ENTRYP PFNVGUGLOWKHRPROC) (VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint glowColorRGBA);
typedef VGUErrorCode (VGU_API_ENTRYP PFNVGUBEVELKHRPROC) (VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint highlightColorRGBA,VGuint shadowColorRGBA);
typedef VGUErrorCode (VGU_API_ENTRYP PFNVGUGRADIENTGLOWKHRPROC) (VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint stopsCount,const VGfloat* glowColorRampStops);
typedef VGUErrorCode (VGU_API_ENTRYP PFNVGUGRADIENTBEVELKHRPROC) (VGImage dst,VGImage src,VGfloat dimX,VGfloat dimY,VGuint iterative,VGfloat strength,VGfloat distance,VGfloat angle,VGbitfield filterFlags,VGbitfield allowedQuality,VGuint stopsCount,const VGfloat* bevelColorRampStops);

#endif


/*-------------------------------------------------------------------------------
 * NDS extensions
 *------------------------------------------------------------------------------*/

#ifndef VG_NDS_paint_generation
#define VG_NDS_paint_generation 1

typedef enum { 
    VG_PAINT_COLOR_RAMP_LINEAR_NDS            = 0x1A10,
    VG_COLOR_MATRIX_NDS                       = 0x1A11,
    VG_PAINT_COLOR_TRANSFORM_LINEAR_NDS       = 0x1A12,

    VG_PAINT_PARAM_TYPE_NDS_FORCE_SIZE        = VG_MAX_ENUM
} VGPaintParamTypeNds;

typedef enum {
    VG_DRAW_IMAGE_COLOR_MATRIX_NDS            = 0x1F10,

    VG_IMAGE_MODE_NDS_FORCE_SIZE              = VG_MAX_ENUM
} VGImageModeNds;
#endif 


#ifndef VG_NDS_projective_geometry
#define VG_NDS_projective_geometry 1

typedef enum {
    VG_CLIP_MODE_NDS                          = 0x1180,
    VG_CLIP_LINES_NDS                         = 0x1181,
    VG_MAX_CLIP_LINES_NDS                     = 0x1182,

    VG_PARAM_TYPE_NDS_FORCE_SIZE              = VG_MAX_ENUM
} VGParamTypeNds;

typedef enum {
    VG_CLIPMODE_NONE_NDS                      = 0x3000,
    VG_CLIPMODE_CLIP_CLOSED_NDS               = 0x3001,
    VG_CLIPMODE_CLIP_OPEN_NDS                 = 0x3002,
    VG_CLIPMODE_CULL_NDS                      = 0x3003,

    VG_CLIPMODE_NDS_FORCE_SIZE = VG_MAX_ENUM
} VGClipModeNds;

typedef enum {
    VG_RQUAD_TO_NDS              = ( 13 << 1 ),
    VG_RCUBIC_TO_NDS             = ( 14 << 1 ),
    
    VG_PATH_SEGMENT_NDS_FORCE_SIZE = VG_MAX_ENUM
} VGPathSegmentNds;

typedef enum {
    VG_RQUAD_TO_ABS_NDS            = (VG_RQUAD_TO_NDS  | VG_ABSOLUTE),
    VG_RQUAD_TO_REL_NDS            = (VG_RQUAD_TO_NDS  | VG_RELATIVE),
    VG_RCUBIC_TO_ABS_NDS           = (VG_RCUBIC_TO_NDS | VG_ABSOLUTE),
    VG_RCUBIC_TO_REL_NDS           = (VG_RCUBIC_TO_NDS | VG_RELATIVE),

    VG_PATH_COMMAND_NDS_FORCE_SIZE = VG_MAX_ENUM
} VGPathCommandNds;

#ifdef VG_VGEXT_PROTOTYPES
VG_API_CALL void VG_API_ENTRY vgProjectiveMatrixNDS(VGboolean enable);
VGU_API_CALL VGUErrorCode VGU_API_ENTRY vguTransformClipLineNDS(const VGfloat Ain,const VGfloat Bin,const VGfloat Cin,const VGfloat* matrix,const VGboolean inverse,VGfloat* Aout,VGfloat* Bout,VGfloat* Cout);
#endif 
typedef void (VG_API_ENTRYP PFNVGPROJECTIVEMATRIXNDSPROC) (VGboolean enable) ;
typedef VGUErrorCode (VGU_API_ENTRYP PFNVGUTRANSFORMCLIPLINENDSPROC) (const VGfloat Ain,const VGfloat Bin,const VGfloat Cin,const VGfloat* matrix,const VGboolean inverse,VGfloat* Aout,VGfloat* Bout,VGfloat* Cout);

#endif

/*-------------------------------------------------------------------------------
 * AmanithVG extensions
 *------------------------------------------------------------------------------*/

/*-------------------------------------------------------------------------------
* VG_MZT_separable_cap_style
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_separable_cap_style)

    #define VG_MZT_separable_cap_style 1

    typedef enum {
        VG_STROKE_START_CAP_STYLE_MZT               = 0x1192,
        VG_STROKE_END_CAP_STYLE_MZT                 = 0x1193,

        VG_PARAM_TYPE0_MZT_FORCE_SIZE               = VG_MAX_ENUM
    } VGParamType0Mzt;

#endif // VG_MZT_separable_cap_style

/*-------------------------------------------------------------------------------
* VG_MZT_separable_blend_modes
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_separable_blend_modes)

    #define VG_MZT_separable_blend_modes 1

    typedef enum {
        VG_STROKE_BLEND_MODE_MZT                    = 0x1190,
        VG_FILL_BLEND_MODE_MZT                      = 0x1191,

        VG_PARAM_TYPE1_MZT_FORCE_SIZE               = VG_MAX_ENUM
    } VGParamType1Mzt;

#endif // VG_MZT_separable_blend_modes

/*-------------------------------------------------------------------------------
* VG_MZT_color_ramp_interpolation
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_color_ramp_interpolation)

    #define VG_MZT_color_ramp_interpolation 1

    typedef enum {
        VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT  = 0x1A91,

        VG_PAINT_PARAM_TYPE0_MZT_FORCE_SIZE         = VG_MAX_ENUM
    } VGPaintParamType0Mzt;

    typedef enum {
        VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT      = 0x1C90,
        VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT      = 0x1C91,

        VG_COLOR_RAMP_INTERPOLATION_TYPE_MZT_FORCE_SIZE = VG_MAX_ENUM
    } VGColorRampInterpolationTypeMzt;

#endif // VG_MZT_color_ramp_interpolation

/*-------------------------------------------------------------------------------
* VG_MZT_conical_gradient
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_conical_gradient)

    #define VG_MZT_conical_gradient 1

    typedef enum {
        VG_PAINT_CONICAL_GRADIENT_MZT               = 0x1A90,

        VG_PAINT_PARAM_TYPE2_MZT_FORCE_SIZE         = VG_MAX_ENUM
    } VGPaintParamType2Mzt;

    typedef enum {
        VG_PAINT_TYPE_CONICAL_GRADIENT_MZT          = 0x1B90,

        VG_PAINT_TYPE_MZT_FORCE_SIZE                = VG_MAX_ENUM
    } VGPaintTypeMzt;

#endif // VG_MZT_conical_gradient

/*-------------------------------------------------------------------------------
* VG_MZT_advanced_blend_modes
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_advanced_blend_modes)

    #define VG_MZT_advanced_blend_modes 1

    typedef enum {
        VG_BLEND_CLEAR_MZT                          = 0x2090,
        VG_BLEND_DST_MZT                            = 0x2091,
        VG_BLEND_SRC_OUT_MZT                        = 0x2092,
        VG_BLEND_DST_OUT_MZT                        = 0x2093,
        VG_BLEND_SRC_ATOP_MZT                       = 0x2094,
        VG_BLEND_DST_ATOP_MZT                       = 0x2095,
        VG_BLEND_XOR_MZT                            = 0x2096,
        VG_BLEND_OVERLAY_MZT                        = 0x2097,
        VG_BLEND_COLOR_DODGE_MZT                    = 0x2098,
        VG_BLEND_COLOR_BURN_MZT                     = 0x2099,
        VG_BLEND_HARD_LIGHT_MZT                     = 0x209A,
        VG_BLEND_SOFT_LIGHT_MZT                     = 0x209B,
        VG_BLEND_DIFFERENCE_MZT                     = 0x209C,
        VG_BLEND_EXCLUSION_MZT                      = 0x209D,

        VG_BLEND_MODE_MZT_FORCE_SIZE                = VG_MAX_ENUM
    } VGBlendModeMzt;

#endif // VG_MZT_advanced_blend_modes

/*-------------------------------------------------------------------------------
* VG_MZT_statistics
*------------------------------------------------------------------------------*/
#if defined(VG_MZT_statistics)

    typedef enum {
        VG_STAT_FLATTENING_POINTS_COUNT_MZT         = (1 << 0),
        VG_STAT_FLATTENING_TIME_MS_MZT              = (1 << 1),
        VG_STAT_FLATTENING_PERFORMED_COUNT_MZT      = (1 << 2),
        VG_STAT_RASTERIZER_TOTAL_TIME_MS_MZT        = (1 << 3),
        VG_STAT_TRIANGULATION_TRIANGLES_COUNT_MZT   = (1 << 4),
        VG_STAT_TRIANGULATION_TIME_MS_MZT           = (1 << 5),
        VG_STAT_STROKER_POINTS_COUNT_MZT            = (1 << 6),
        VG_STAT_STROKER_TIME_MS_MZT                 = (1 << 7),
        VG_STAT_GL_DRAWELEMENTS_COUNT_MZT           = (1 << 8),
        VG_STAT_GL_DRAWARRAYS_COUNT_MZT             = (1 << 9),
        VG_STATISTIC_ALL_MZT                        = ((1 << 10) - 1),
    } VGStatisticInfoMzt;

#if defined(VG_VGEXT_PROTOTYPES)
    /*
        In the statistics build, this resets statistics counters.
        Errors: VG_ILLEGAL_ARGUMENT_ERROR: if statistics is not a valid bitwise OR of values from the VGStatisticInfo enumeration.
    */
    VG_API_CALL void VG_API_ENTRY vgResetStatisticsMZT(const VGbitfield statistics) VG_API_EXIT;

    /*
        In the statistics build, this returns statistics gathered since the last vgResetStatisticsAM.
        Errors: VG_ILLEGAL_ARGUMENT_ERROR: if statistic is not one of the values from VGStatisticInfo enumeration.
    */
    VG_API_CALL VGint VG_API_ENTRY vgGetStatisticiMZT(const VGStatisticInfoMzt statistic) VG_API_EXIT;

#endif // VG_VGEXT_PROTOTYPES
    typedef void (VG_API_ENTRYP PFNVGRESETSTATISTICSMZTPROC) (const VGbitfield statistics);
    typedef VGint (VG_API_ENTRYP PFNVGGETSTATISTICIMZTPROC) (const VGStatisticInfoMzt statistic);
#endif  // VG_MZT_statistics


/*-------------------------------------------------------------------------------
* VG_MZT_clip_path
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_clip_path)

    #define VG_MZT_clip_path 1

    //! Fill rule used by the clip path, this new context parameter can be get/set through vgGet/vgSet functions
    typedef enum {
        VG_CLIP_RULE_MZT                            = 0x1194,
        VG_CLIPPING_MZT                             = 0x1195,

        VG_PARAM_TYPE2_MZT_FORCE_SIZE               = VG_MAX_ENUM
    } VGParamType2Mzt;

    //! A new matrix mode used to manipulate the transformation matrix associated to the clip path
    typedef enum {
    #if defined(OPENVG_VERSION_1_1)
        VG_MATRIX_CLIP_USER_TO_SURFACE_MZT          = 0x1405,
    #else
        VG_MATRIX_CLIP_USER_TO_SURFACE_MZT          = 0x1404,
    #endif

        VG_MATRIX_MODE_MZT_FORCE_SIZE               = VG_MAX_ENUM
    } VGMatrixModeMzt;

#if defined(VG_VGEXT_PROTOTYPES)
    /*
        Push a new clip path.

        If 'advanceLayer' is VG_TRUE, a new clip layer is established, and the given path is "drawn" on it; the
        clip rule assigned to the new clip layer is the current value of VG_CLIP_RULE_MZT context parameter.

        If 'advanceLayer' is VG_FALSE, the given path is added to the current clip layer (if there is still no
        clip layer yet, a new clip layer is established with a clip rule equal to the current value of
        VG_CLIP_RULE_MZT context parameter).

        Errors:
            VG_BAD_HANDLE_ERROR if path is not a valid path handle or if it is VG_INVALID_HANDLE, or is not shared with the current context
            VG_OUT_OF_MEMORY_ERROR if we have already reached the maximum number of clip layers that can be established
    */
    VG_API_CALL void VG_API_ENTRY vgClipPathPushMZT(VGPath path,
                                                    VGboolean advanceLayer) VG_API_EXIT;

    /*
        Pop out the last pushed clip layer.
    */
    VG_API_CALL void VG_API_ENTRY vgClipPathPopMZT(void) VG_API_EXIT;

    /*
        Clear and remove all clip layers.
    */
    VG_API_CALL void VG_API_ENTRY vgClipPathClearMZT(void) VG_API_EXIT;

#endif // VG_VGEXT_PROTOTYPES

    typedef void (VG_API_ENTRYP PFNVGCLIPPATHPUSHMZTPROC) (VGPath path,
                                                           VGboolean advanceLayer);
    typedef void (VG_API_ENTRYP PFNVGCLIPPATHPOPMZTPROC) (void);
    typedef void (VG_API_ENTRYP PFNVGCLIPPATHCLEARMZTPROC) (void);

#endif // VG_MZT_clip_path

/*-------------------------------------------------------------------------------
* VG_MZT_filters
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_filters)
    
    #define VG_MZT_filters 1

#if defined(VG_VGEXT_PROTOTYPES)
    /*
        Equal to the standard vgColorMatrix filter, with the exception that images can overlap (e.g. they could be the same).
    */
    VG_API_CALL void VG_API_ENTRY vgColorMatrixMZT(VGImage dst,
                                                   VGImage src,
                                                   const VGfloat* matrix) VG_API_EXIT;

    /*
        Equal to the standard vgGaussianBlur filter, with the exception that images can overlap (e.g. they could be the same).
        
        If 'axes' is NULL and 'useFastApprox' is 'VG_TRUE', a fast Gaussian blur approximation (through subsequent application
        of separable box filters) is performed.

        If 'axes' is non-NULL (it must be aligned to 4-bytes), a non-separable Gaussian blur is performed along the given 'axes':
        ('axes[0]', 'axes[1]') defines the horizontal axis, ('axes[2]', 'axes[3]') defines the vertical axis.
    */
    VG_API_CALL void VG_API_ENTRY vgGaussianBlurMZT(VGImage dst,
                                                    VGImage src,
                                                    const VGfloat* axes,
                                                    VGfloat stdDeviationX,
                                                    VGfloat stdDeviationY,
                                                    VGTilingMode tilingMode,
                                                    VGboolean useFastApprox) VG_API_EXIT;

    typedef enum {
        /* Distant light, defined by azimuth and elevation angles */
        VG_LIGHT_TYPE_DISTANT_MZT = 0x2401,
        /* Point light, defined by its space location (x, y, z) */
        VG_LIGHT_TYPE_POINT_MZT = 0x2402,
        /* Spot light, defined by its space location (x, y, z), point towards which it is directed (x, y, z), specular exponent, cone angle */
        VG_LIGHT_TYPE_SPOT_MZT = 0x2403,

        VG_LIGHT_TYPE_MZT_FORCE_SIZE = VG_MAX_ENUM
    } VGLightTypeMzt;

    /*
        This filter lights a source graphic using the alpha channel as a bump map. The resulting image is an RGBA image
        based on the light color. The lighting calculation follows the standard diffuse/specular components of the Phong
        lighting model. The resulting images depend on the light color, light position and surface geometry of the input
        bump map. The filter assumes that the viewer is at infinity in the z direction (i.e., the unit vector in the eye
        direction is (0, 0, 1) everywhere).

        'dstDiffuse' is the destination image for the diffuse component of the Phong lighting model:
        diffuse.r = Kd * <N, L> * light.color.r
        diffuse.g = Kd * <N, L> * light.color.g
        diffuse.b = Kd * <N, L> * light.color.b
        diffuse.a = 1

        The generated diffuse pixels are in the color space determined by the value of VG_FILTER_FORMAT_LINEAR (because
        alpha is always 1, the pixel can be thought of as both premultiplied and non-premultiplied), and then converted
        into the destination image space. This means that VG_FILTER_FORMAT_PREMULTIPLIED parameter is not actually used
        (it would not make sense to perform a useless intermediate conversion).

        'dstSpecular' is the destination image for the specular component of the Phong lighting model:
        specular.r = Ks * pow(<N, H>, light.specExp) * light.color.r
        specular.g = Ks * pow(<N, H>, light.specExp) * light.color.g
        specular.b = Ks * pow(<N, H>, light.specExp) * light.color.b
        specular.a = max(specular.r, specular.g, specular.b)

        The generated specular pixels are in the color space determined by the value of VG_FILTER_FORMAT_LINEAR with
        alpha-premultiplication enforced, and then converted into the destination image space. This means that
        VG_FILTER_FORMAT_PREMULTIPLIED parameter is not actually used (it would not make sense to perform a useless
        intermediate conversion).

        For the diffuse lighting, 'diffuseConstant' represents the Kd value in Phong lighting model, and must be non-negative.
        For the specular lighting:
            - 'specularConstant' represents the Ks value in Phong lighting model, and must be non-negative.
            - 'specularExponent' represents the exponent for specular term, larger is more "shiny", valid range is [1; 128].
              Values outside the range are interpreted as the nearest endpoint of the range.

        The 'lightData' array contains the color and the geometric attributes of the light source:
            - [0] = red component of the light source
            - [1] = green component of the light source
            - [2] = blue component of the light source
            - [3+] = < geometric attributes of the light source >, variable length (see below)

        The color components of the light source are expressed in non-premultiplied sRGB, values outside the
        [0, 1] range are interpreted as the nearest endpoint of the range. According to the given 'lightType', the < geometric
        attributes of the light source > is a list of values as follows:

            - 2 entries for VG_LIGHT_TYPE_DISTANT_MZT light type
              [0] = azimuth angle, in degrees
              [1] = elevation angle, in degrees
            - 3 entries for VG_LIGHT_TYPE_POINT_MZT light type
              [0] = x location for the light source
              [1] = y location for the light source
              [2] = z location for the light source
            - 9 entries for VG_LIGHT_TYPE_SPOT_MZT light type
              [0] = x location for the light source
              [1] = y location for the light source
              [2] = z location for the light source
              [3] = x location of the point at which the light source is pointing (i.e. pointsAtX)
              [4] = y location of the point at which the light source is pointing (i.e. pointsAtY)
              [5] = z location of the point at which the light source is pointing (i.e. pointsAtZ)
              [6] = the exponent value controlling the focus for the light source
              [7] = the limiting cone angle which restricts the region where the light is projected, in degrees; valid range is [0; 90]
              [8] = smoothing threshold used to implement edge darkening at the boundary of the cone

        Errors:
            VG_BAD_HANDLE_ERROR if 'src' is not a valid image handle, or is not shared with the current context
            VG_BAD_HANDLE_ERROR if 'dstDiffuse' is different than VG_INVALID_HANDLE and is not a valid image handle or is not shared with the current context
            VG_BAD_HANDLE_ERROR if 'dstSpecular' is different than VG_INVALID_HANDLE and is not a valid image handle or is not shared with the current context
            VG_IMAGE_IN_USE_ERROR if either 'dstDiffuse', 'dstSpecular' or 'src' is currently a rendering target
            VG_ILLEGAL_ARGUMENT_ERROR if 'src' and 'dstDiffuse' images overlap
            VG_ILLEGAL_ARGUMENT_ERROR if 'src' and 'dstSpecular' images overlap
            VG_ILLEGAL_ARGUMENT_ERROR if 'diffuseConstant' is less than zero
            VG_ILLEGAL_ARGUMENT_ERROR if 'specularConstant' is less than zero
            VG_ILLEGAL_ARGUMENT_ERROR if 'lightType' is not one of the values from the VGLightTypeMzt enumeration
            VG_ILLEGAL_ARGUMENT_ERROR if 'lightData' is NULL or not properly aligned

        Notes:
            If 'dstDiffuse' is VG_INVALID_HANDLE, 'diffuseConstant' is ignored and not cheked for errors
            If 'dstSpecular' is VG_INVALID_HANDLE, 'specularConstant' is ignored and not cheked for errors
    */
    VG_API_CALL void VG_API_ENTRY vgLightingMZT(VGImage dstDiffuse,
                                                VGImage dstSpecular,
                                                VGImage src,
                                                VGfloat surfaceScale,
                                                // diffuse lighting
                                                VGfloat diffuseConstant,
                                                // specular lighting
                                                VGfloat specularConstant,
                                                VGfloat specularExponent,
                                                // light information
                                                VGLightTypeMzt lightType,
                                                const VGfloat* lightData) VG_API_EXIT;

    /*
        This filter performs "fattening" or "thinning" of images.
        The dilation (or erosion) kernel is a rectangle with a width of (2 * radiusX) + 1 and a height of (2 * radiusY) + 1.
        A radius value of zero disables the effect along the relative direction.

        In erosion ('erode' = VG_TRUE), the output pixel is the individual component-wise minimum of the
        corresponding R, G, B, A values in the source image's kernel rectangle.

        In dilation ('erode' = VG_FALSE), the output pixel is the individual component-wise maximum of the
        corresponding R, G, B, A values in the source image's kernel rectangle.

        Normally the canonical orthogonal axes (1, 0) - (0, 1) are used (i.e. NULL 'axes' argument) and in this
        case the filter implements a separable fast algorithm. It is possible to specify generic non-orthogonal
        'axes', in a such case the filter implements a slower non-separable algorithm.

        NB: 'src' and 'dst' images can overlap.

        Errors:
            VG_BAD_HANDLE_ERROR if either 'dst' or 'src' is not a valid image handle, or is not shared with the current context
            VG_IMAGE_IN_USE_ERROR if either 'dst' or 'src' is currently a rendering target
            VG_ILLEGAL_ARGUMENT_ERROR if 'axes' is not NULL and not properly aligned
            VG_ILLEGAL_ARGUMENT_ERROR if 'radiusX' or 'radiusY' is less than 0
    */
    VG_API_CALL void VG_API_ENTRY vgMorphologyMZT(VGImage dst,
                                                  VGImage src,
                                                  VGboolean erode,
                                                  const VGfloat* axes,
                                                  VGint radiusX,
                                                  VGint radiusY) VG_API_EXIT;

    /*
        This filter creates an image using the Perlin turbulence function.
        It allows, for example, the synthesis of artificial textures like clouds or marble.
        
        The generated color and alpha values are in the color space determined by the value of
        VG_FILTER_FORMAT_LINEAR and VG_FILTER_FORMAT_PREMULTIPLIED.

        In order to generate (x, y) coordinates for noise generation, each pixel location (px, py)
        is shifted by 'bias' and multiplied by 'scale':

        noise.x = (pixel.x + biasX) * scaleX
        noise.y = (pixel.y + biasY) * scaleY

        An initial seed value is computed based on attribute 'seed'. Then the implementation computes the
        lattice points for R, then continues getting additional pseudo random numbers relative to the last
        generated pseudo random number and computes the lattice points for G, and so on for B and A.

        Errors:
            VG_BAD_HANDLE_ERROR if 'image' is not a valid image handle, or is not shared with the current context
            VG_IMAGE_IN_USE_ERROR if 'image' is currently a rendering target
            VG_ILLEGAL_ARGUMENT_ERROR if 'baseFrequencyX' or 'baseFrequencyY' is less than 0
            VG_ILLEGAL_ARGUMENT_ERROR if 'numOctaves' is less than or equal to 0
    */
    VG_API_CALL void VG_API_ENTRY vgTurbulenceMZT(VGImage image,
                                                  VGfloat biasX,
                                                  VGfloat biasY,
                                                  VGfloat scaleX,
                                                  VGfloat scaleY,
                                                  VGfloat baseFrequencyX,
                                                  VGfloat baseFrequencyY,
                                                  VGint numOctaves,
                                                  VGint seed,
                                                  VGboolean stitchTiles,
                                                  VGboolean fractalNoise) VG_API_EXIT;

    /*
        This filter uses the pixels values from 'map' to spatially displace the 'src' image; result is written to 'dst' image.
        This is the transformation to be performed:

        dst(x, y) = src(x + scaleX * (map(x, y, xChannelSelector) - 0.5), y + scaleY * (map(x, y, yChannelSelector) - 0.5))

        where src(x, y) is the input image and dst(x, y) is the destination. map(x, y, xChannelSelector) and map(x, y, yChannelSelector)
        are the component values of the channel designated by the xChannelSelector and yChannelSelector. For example, to use the red
        component of 'map' to control displacement in x and the green component of 'map to control displacement in y, set 'xChannelSelector'
        to VG_RED and 'yChannelSelector' to VG_GREEN.

        'map' pixels are read and converted to the space defined by the current values of VG_FILTER_FORMAT_PREMULTIPLIED and
        VG_FILTER_FORMAT_LINEAR parameters. Pixels read from 'src' image are then converted to the space of 'dst' image.

        Some mandatory preconditions:
            - 'src' and 'map' images must have the same dimensions
            - 'dst' and 'src' images cannot overlap
            - 'dst' and 'map' images cannot overlap

        NB: 'src' and 'map' images can overlap.

        Errors:
            VG_BAD_HANDLE_ERROR if either 'dst', 'src' or 'map is not a valid image handle, or is not shared with the current context
            VG_IMAGE_IN_USE_ERROR if either 'dst', 'src' or 'map' is currently a rendering target
            VG_ILLEGAL_ARGUMENT_ERROR if 'dst' and 'src' overlap
            VG_ILLEGAL_ARGUMENT_ERROR if 'dst' and 'map' overlap
            VG_ILLEGAL_ARGUMENT_ERROR if 'src' and 'map' images do not have the same dimensions (i.e. different width or height)
            VG_ILLEGAL_ARGUMENT_ERROR if 'tilingMode' is not one of the values from the VGTilingMode enumeration
            VG_ILLEGAL_ARGUMENT_ERROR if either 'xChannelSelector' or 'yChannelSelector' is not one of the values from the VGImageChannel enumeration
    */
    VG_API_CALL void VG_API_ENTRY vgDisplacementMapMZT(VGImage dst,
                                                       VGImage src,
                                                       VGImage map,
                                                       VGfloat scaleX,
                                                       VGfloat scaleY,
                                                       VGTilingMode tilingMode,
                                                       VGImageChannel xChannelSelector,
                                                       VGImageChannel yChannelSelector) VG_API_EXIT;

    typedef enum {
        VG_COMPOSITE_OVER_MZT          = 0x2501,
        VG_COMPOSITE_MULTIPLY_MZT      = 0x2502,
        VG_COMPOSITE_SCREEN_MZT        = 0x2503,
        VG_COMPOSITE_DARKEN_MZT        = 0x2504,
        VG_COMPOSITE_LIGHTEN_MZT       = 0x2505,
        VG_COMPOSITE_IN_MZT            = 0x2506,
        VG_COMPOSITE_OUT_MZT           = 0x2507,
        VG_COMPOSITE_ATOP_MZT          = 0x2508,
        VG_COMPOSITE_XOR_MZT           = 0x2509,
        VG_COMPOSITE_ARITHMETIC_MZT    = 0x250A,

        VG_COMPOSITE_OP_MZT_FORCE_SIZE = VG_MAX_ENUM
    } VGCompositeOpMzt;

    /*
        This filter composites two images together using commonly used blending modes: it performs a pixel-wise combination of two input images.
        Additionally, a component-wise arithmetic operation (with the result clamped between [0..1]) can be applied.
        If the arithmetic operation is chosen, each result pixel is computed using the following formula:

        dst(x, y) = k1 * in1(x, y) * in2(x, y) + k2 * in1(x, y) + k3 * in2(x, y) + k4

        'dst', 'in1', 'in2' images can overlap, but 'in1' and 'in2' images must have the same dimensions (mandatory precondition)

        Errors:
            VG_BAD_HANDLE_ERROR if either 'dst', 'in1' or 'in2 is not a valid image handle, or is not shared with the current context
            VG_IMAGE_IN_USE_ERROR if either 'dst', 'in1' or 'in2' is currently a rendering target
            VG_ILLEGAL_ARGUMENT_ERROR if 'in1' and 'in2' images do not have the same dimensions (i.e. different width or height)
            VG_ILLEGAL_ARGUMENT_ERROR if 'operation' is not one of the values from the VGCompositeOpMzt enumeration
    */
    VG_API_CALL void VG_API_ENTRY vgCompositeMZT(VGImage dst,
                                                 VGImage in1,
                                                 VGImage in2,
                                                 VGCompositeOpMzt operation,
                                                 VGfloat k1,
                                                 VGfloat k2,
                                                 VGfloat k3,
                                                 VGfloat k4) VG_API_EXIT;

#endif // VG_VGEXT_PROTOTYPES
    typedef void (VG_API_ENTRYP PFNVGCOLORMATRIXMZTPROC) (VGImage dst,
                                                          VGImage src,
                                                          const VGfloat* matrix);
    typedef void (VG_API_ENTRYP PFNVGGAUSSIANBLURMZTPROC) (VGImage dst,
                                                           VGImage src,
                                                           const VGfloat* axes,
                                                           VGfloat stdDeviationX,
                                                           VGfloat stdDeviationY,
                                                           VGTilingMode tilingMode,
                                                           VGboolean useFastApprox);

    typedef void(VG_API_ENTRYP PFNVGLIGHTINGMZTPROC) (VGImage dstDiffuse,
                                                      VGImage dstSpecular,
                                                      VGImage src,
                                                      VGfloat surfaceScale,
                                                      // diffuse lighting
                                                      VGfloat diffuseConstant,
                                                      // specular lighting
                                                      VGfloat specularConstant,
                                                      VGfloat specularExponent,
                                                      // light information
                                                      VGLightTypeMzt lightType,
                                                      const VGfloat* lightData);

    typedef void (VG_API_ENTRYP PFNVGMORPHOLOGYMZTPROC) (VGImage dst,
                                                         VGImage src,
                                                         VGboolean erode,
                                                         const VGfloat* axes,
                                                         VGint radiusX,
                                                         VGint radiusY);
    typedef void (VG_API_ENTRYP PFNVGTURBULENCEMZTPROC) (VGImage image,
                                                         VGfloat biasX,
                                                         VGfloat biasY,
                                                         VGfloat scaleX,
                                                         VGfloat scaleY,
                                                         VGfloat baseFrequencyX,
                                                         VGfloat baseFrequencyY,
                                                         VGint numOctaves,
                                                         VGint seed,
                                                         VGboolean stitchTiles,
                                                         VGboolean fractalNoise);

    typedef void (VG_API_ENTRYP PFNVGDISPLACEMENTMAPMZTPROC) (VGImage dst,
                                                              VGImage src,
                                                              VGImage map,
                                                              VGfloat scaleX,
                                                              VGfloat scaleY,
                                                              VGTilingMode tilingMode,
                                                              VGImageChannel xChannelSelector,
                                                              VGImageChannel yChannelSelector);

    typedef void (VG_API_ENTRYP PFNVGCOMPOSITEMZTPROC) (VGImage dst,
                                                        VGImage in1,
                                                        VGImage in2,
                                                        VGCompositeOpMzt operation,
                                                        VGfloat k1,
                                                        VGfloat k2,
                                                        VGfloat k3,
                                                        VGfloat k4);
#endif  // VG_MZT_filters

/*-------------------------------------------------------------------------------
* VG_MZT_mask
*------------------------------------------------------------------------------*/
#if !defined(VG_MZT_mask)
    
    #define VG_MZT_mask 1

#if defined(VG_VGEXT_PROTOTYPES)
  /*
    If the given 'mask' handle refers a VGMaskLayer or an image created with a single-channel format, this function will behave as a standard vgMask
    call with the same given parameters. Here's the list of single-channel formats:

    VG_sL_8
    VG_lL_8
    VG_A_8
    VG_BW_1
    VG_A_1
    VG_A_4

    For all other image formats, the final mask value that will be applied to the OpenVG mask (according to the given operation) is computed as follow:

    - first a luminance value is computed from the color channel values RGB
    - then the computed luminance value is multiplied by the corresponding alpha value to produce the mask value.

    Such behavior is the one requested by the SVG masking feature (see https://www.w3.org/TR/SVG11/masking.html).
  */
  VG_API_CALL void VG_API_ENTRY vgMaskMZT(VGHandle mask,
                                          VGMaskOperation operation,
                                          VGint x,
                                          VGint y,
                                          VGint width,
                                          VGint height) VG_API_EXIT;

#endif // VG_VGEXT_PROTOTYPES
    typedef void (VG_API_ENTRYP PFNVGMASKMZTPROC) (VGHandle mask,
                                                   VGMaskOperation operation,
                                                   VGint x,
                                                   VGint y,
                                                   VGint width,
                                                   VGint height);
#endif  // VG_MZT_mask

/*-------------------------------------------------------------------------------
* Memory debug facilities
*-----------------------------------------------------------------------------*/
typedef enum {
    VG_COMPILE_CONFIG_INFO_MZT        = 0x2305,
    VG_STRING_ID_MZT_FORCE_SIZE       = VG_MAX_ENUM
} VGStringIDMzt;

typedef enum {
    VG_DEBUG_MEMORY_MZT               = (1 << 0),
    VG_DEBUG_API_MZT                  = (1 << 1),
    VG_DEBUG_GL_MZT                   = (1 << 2),
    VG_DEBUG_ALL_MZT                  = ((1 << 3) - 1),
    VG_DEBUG_LEVEL_MZT_FORCE_SIZE     = VG_MAX_ENUM
} VGDebugLevelMzt;

/*
  Description
    Change settings for the memory debug facilities.
    When enabled, the library keeps track of the total allocated memory.

    NB: memory facilities settings can be modified only when no allocations has been made yet.
    The best time to set debug facilities is after the calling to vgInitializeMZT and before
    the creation of OpenVG contexts and a drawing surfaces.

  Parameters
    maxAllocableAmount: the maximum memory amount that the library can allocate. A negative number indicates no allocation limits.
    logBuffer: NULL or a pointer to a characters buffer where the library can log allocation/reallocation/disposal operations.
    logBufferCapacity: the capacity of logBuffer, in bytes. Must be a positive number.
    logLevel: a bitwise OR of the desired VGDebugLevelMzt values.
    enableConsistencyChecks: if VG_TRUE, enable consistency checks on created handles. This could be slow down the runtime execution
    because checks are performed at each handle creation (e.g. vgCreatePath, vgCreatePaint, ...) and during memory retrivial operations.
    enableMemoryRetrivial: if VG_TRUE, enable memory retrivial when "out of memory" condition is reached.

  Return
    VG_TRUE if debug facilities can be enabled/disabled, else VG_FALSE.

  Notes
    This function does not set the internal context error, so it has no influence on the value returned by vgGetError
*/
VG_API_CALL VGboolean VG_API_ENTRY vgMemoryDebugSetMZT(VGint maxAllocableAmount,
                                                       char* logBuffer,
                                                       VGuint logBufferCapacity,
                                                       VGbitfield logLevel,
                                                       VGboolean enableConsistencyChecks,
                                                       VGboolean enableMemoryRetrivial) VG_API_EXIT;

/*
  Description
    Get the total allocated memory by the library.

  Return
    A negative number if the memory debug facilities have not been enabled through the vgMemoryDebugSetMZT function; else
    the total allocated memory by the library.

  Notes
    This function does not set the internal context error, so it has no influence on the value returned by vgGetError
*/
VG_API_CALL VGint VG_API_ENTRY vgMemoryAllocatedGetMZT(void) VG_API_EXIT;

/*-------------------------------------------------------------------------------
* General puprpose utilities
*-----------------------------------------------------------------------------*/

/*
  Description
    Extract the scale factors and rotation angle of the given (affine) matrix.
    The returned angle is expressed in radians.

  Return
    - VG_ILLEGAL_ARGUMENT_ERROR if m is NULL or not properly aligned
    - VG_ILLEGAL_ARGUMENT_ERROR if rotation is not NULL and not properly aligned
    - VG_ILLEGAL_ARGUMENT_ERROR if scaleX is not NULL and not properly aligned
    - VG_ILLEGAL_ARGUMENT_ERROR if scaleY is not NULL and not properly aligned
    
*/
VG_API_CALL VGErrorCode VG_API_ENTRY vgGetMatrixInfoMZT(const VGfloat* m,
                                                        VGfloat* rotation,
                                                        VGfloat* scaleX,
                                                        VGfloat* scaleY) VG_API_EXIT;

/*-------------------------------------------------------------------------------
* Configuration parameters and thresholds
*------------------------------------------------------------------------------*/
typedef enum {
    // The defaul value (no buffers disabled).
    VG_BUFFERS_DISABLED_NONE_MZT                                = 0,
    // When both depth and stencil buffers are available on the GL context, it
    // forces the depth buffer to be unused by AmanithVG GLE.
    VG_BUFFERS_DISABLED_DEPTH_MZT                               = 1,
    // When both depth and stencil buffers are available on the GL context, it
    // forces the stencil buffer to be unused by AmanithVG GLE.
    VG_BUFFERS_DISABLED_STENCIL_MZT                             = 2,

    VG_BUFFERS_DISABLED_MZT_FORCE_SIZE                          = VG_MAX_ENUM
} VGBuffersDisabledMzt;

typedef enum {
    VG_FORCE_IMAGE_TEXTURE_BORDERS_NONE_MZT                     = 0,
    VG_FORCE_IMAGE_TEXTURE_BORDERS_WHOLE_MZT                    = -1,

    VG_FORCE_IMAGE_TEXTURE_BORDERS_MZT_FORCE_SIZE               = VG_MAX_ENUM
} VGForceImageTextureBordersMzt;

typedef enum {
    // Borders are filled with a transparent black.
    VG_TEXTURE_BORDER_MODE_CLEAR_MZT                            = 0,
    // Borders are filled by duplicating pixels on image edges.
    VG_TEXTURE_BORDER_MODE_COPY_MZT                             = 1,
    // Borders are filled by duplicating pixels on image edges and
    // overriding their alpha value with 0.
    VG_TEXTURE_BORDER_MODE_COPY_ZERO_ALPHA_MZT                  = 2,

    VG_TEXTURE_BORDER_MODE_MZT_FORCE_SIZE                       = VG_MAX_ENUM
} VGImageTextureBordersModeMzt;

typedef enum {
    //
    // [READ-ONLY]
    //
    // The maximum number of different threads that can "work" (e.g. create surfaces, draw paths, etc) concurrently.
    VG_CONFIG_MAX_CURRENT_THREADS_MZT                           = 0x5300,
    // The maximum dimension allowed for drawing surfaces, in pixels. This is the maximum valid value that can be specified as
    // 'width' and 'height' for the vgPrivSurfaceCreateMZT and vgPrivSurfaceResizeMZT functions.
    VG_CONFIG_MAX_SURFACE_DIMENSION_MZT                         = 0x5301,
    //
    //
    // [Geometry]
    //
    // Used by AmanithVG geometric kernel to approximate curves with straight line
    // segments (flattening). Valid range is [0; 100], where 100 represents the best quality.
    VG_CONFIG_CURVES_QUALITY_MZT                                = 0x5302,
    // Used by radial gradient paints, only in non-shader pipelines (AmanithVG GLE only).
    // Valid range is [0; 100], where 100 represents the best quality.
    VG_CONFIG_RADIAL_GRADIENTS_QUALITY_MZT                      = 0x5303,
    // Used by conical gradient paints, only in non-shader pipelines (AmanithVG GLE only).
    // If VG_MZT_conical_gradient extension is not available, this parameter has no effects.
    // Valid range is [0; 100], where 100 represents the best quality.
    VG_CONFIG_CONICAL_GRADIENTS_QUALITY_MZT                     = 0x5304,
    //
    //
    // [Memory]
    //
    // Number of OpenVG calls (handles creation / destruction and drawing functions) to be
    // done before to recover / retrieve unused memory. Must be a positive number.
    // If 0 is specified, unused memory will never be recovered from internal structures and
    // memory pools.
    VG_CONFIG_CALLS_BEFORE_MEMORY_RECOVERY_MZT                  = 0x5305,
    //
    //
    // [Rasterizer cache] - AmanithVG SRE only
    //
    // Disable paths rasterizer caching if (screen space) bounding box width exceeds this value.
    // Valid values are in the range [0; 4096]. Use 0 to disable rasterizer caching.
    VG_CONFIG_MAX_CACHING_BOX_WIDTH_MZT                         = 0x5306,
    // Disable paths rasterizer caching if (screen space) bounding box height exceeds this value.
    // Valid values are in the range [0; 4096]. Use 0 to disable rasterizer caching.
    VG_CONFIG_MAX_CACHING_BOX_HEIGHT_MZT                        = 0x5307,
    //
    //
    // [OpenGL] / [OpenGL ES] - AmanithVG GLE only
    //
    // Avoid the use of GL_EXT_texture_rectangle or GL_ARB_texture_rectangle extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_RECT_TEXTURES_DISABLED_MZT                  = 0x5308,
    // Avoid the use of GL_ARB_texture_mirrored_repeat extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_MIRRORED_REPEAT_DISABLED_MZT                = 0x5309,
    // Avoid the use of GL_ARB_texture_border_clamp extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_CLAMP_TO_BORDER_DISABLED_MZT                = 0x530A,
    // Avoid the use of GL_EXT_blend_minmax extension, even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_BLEND_MIN_MAX_DISABLED_MZT                  = 0x530B,
    // Avoid the use of GL_EXT_texture_env_dot3 or GL_ARB_texture_env_dot3 extension, even if supported by the GL Graphic System.
    // WARNING: setting this parameter as true will compromise the correct drawing of images in stencil image mode.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_DOT3_DISABLED_MZT                           = 0x530C,
    // Avoid the use of Vertex Buffer Objects (VBO), even if supported by the GL Graphic System.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_VBO_DISABLED_MZT                            = 0x530D,
    // Force the maximum number of texture units that AmanithVG can use.
    // Valid values are 1, 2, 3, 4 (AmanithVG uses no more than 4 texture units; at least 2
    // texture units are always required to implement the whole OpenVG features set).
    VG_CONFIG_MAX_PERMITTED_TEXTURE_UNITS_MZT                   = 0x530E,
    // Force the maximum texture size that AmanithVG can use.
    // Valid values are 0 (autodetect), 64, 128, 256, 512, 1024, 2048, 4096, 8192. Other values will be ignored.
    VG_CONFIG_MAX_TEXTURE_SIZE_MZT                              = 0x530F,
    // When both depth and stencil buffers are available on the GL context, it forces the specified buffer to be unused by AmanithVG.
    // Valid values are defined by the VGBuffersDisabledMzt enum type. Other values will be ignored.
    VG_CONFIG_FORCE_BUFFERS_DISABLED_MZT                        = 0x5310,
    // Suppose depth and stencil buffers to be persistent after a swapBuffers call.
    // Please note that, while on desktop platforms persistent buffers are common,
    // the same is not so common on embedded (OpenGL ES) platforms.
    // WARNING: setting this parameter as true on GL Graphic System with non-persistent
    // buffers, will compromise a correct rendering on some specific OpenVG features.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_SUPPOSE_PERSISTENT_BUFFERS_MZT                    = 0x5311,
    // Avoid the use of GL scissor feature, even if supported by the GL Graphic System.
    // WARNING: this parameter is provided to address compatibility issues; setting this
    // parameter as true when the stencil buffer is not available to AmanithVG, will
    // compromise a correct rendering on some specific OpenVG features.
    // Furthermore it will have a negative impact on performance.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_SCISSOR_DISABLED_MZT                        = 0x5312,
    // Avoid the use of GL color masking feature, even if supported by the GL Graphic System.
    // WARNING: this parameter is provided to address compatibility issues; setting this parameter
    // as true will compromise a correct rendering on some specific OpenVG features.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_COLOR_MASKING_DISABLED_MZT                  = 0x5313,
    // Force the use of mipmaps on gradient textures.
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_MIPMAPS_ON_GRADIENTS_MZT                    = 0x5314,
    // Force dithering on gradient textures, when the drawing surface is configured
    // to have less than 8bit per color component (e.g.RGB565).
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_DITHERING_ON_GRADIENTS_MZT                  = 0x5315,
    // Force dithering on image textures, when the drawing surface is configured
    // to have less than 8bit per color component (e.g.RGB565).
    // Valid values are VG_FALSE and VG_TRUE
    VG_CONFIG_FORCE_DITHERING_ON_IMAGES_MZT                     = 0x5316,
    // If VG_TRUE, it forces GL_RGBA texture format even for opaque paint / images.
    // If VG_FALSE, GL_RGB texture format for opaque paint / images will be used.
    VG_CONFIG_FORCE_RGBA_TEXTURES_MZT                           = 0x5317,
    // If different than VG_FORCE_IMAGE_TEXTURE_BORDERS_NONE_MZT, it forces the upload of VGImage textures with an additional
    // filled border. If VG_FORCE_IMAGE_TEXTURE_BORDERS_NONE_MZT, images are uploaded without additional borders.
    // Negative values are treated as VG_FORCE_IMAGE_TEXTURE_BORDERS_WHOLE_MZT.
    // WARNING: this parameter is provided to address compatibility issues related to u-v coordinates
    // generation throught the GL_TEXTURE matrix, on not conformant GL Graphic System. By setting this parameter as
    // VG_FORCE_IMAGE_TEXTURE_BORDERS_WHOLE_MZT or to a positive number, it will require additional memory and could
    // impact on performance (when drawing images for the first time).
    // Valid values are defined by the VGForceImageTextureBordersMzt enum type.
    VG_CONFIG_FORCE_IMAGE_TEXTURE_BORDERS_MZT                   = 0x5318,
    // In conjunction with VG_CONFIG_FORCE_IMAGE_TEXTURE_BORDERS_MZT parameter, it specifies
    // how VGImage texture borders must be filled. If VG_TEXTURE_BORDER_MODE_CLEAR_MZT, borders are filled with
    // a transparent black; if VG_TEXTURE_BORDER_MODE_COPY_MZT, borders are filled by duplicating pixels on image
    // edges; if VG_TEXTURE_BORDER_MODE_COPY_ZERO_ALPHA_MZT, borders are filled by duplicating pixels on image edges
    // and overriding their alpha value with 0.
    // Valid values are defined by the VGImageTextureBordersModeMzt enum type.
    VG_CONFIG_IMAGE_TEXTURE_BORDERS_MODE_MZT                    = 0x5319,
    //
    // Standard deviation factor for Gaussian blur filter. It represents the factor by which the
    // sigma value is multiplied to obtain the amplitude (of the Gaussian function) to be taken
    // for the blur. Must be a positive number.
    // The default value of 3.0 guarantees a coverage of the Gaussian curve equal to 99.7%
    VG_CONFIG_FILTER_GAUSSIAN_SIGMA_FACTOR_MZT                  = 0x531A,
    
    VG_CONFIG_MZT_FORCE_SIZE                                    = VG_MAX_ENUM
} VGConfigMzt;

/*
  Description
    Configure parameters and thresholds for the AmanithVG library.
    This function can be called at any time, but it will have effect only if:

    - the library has not been already initialized by a previous call to vgInitializeMZT, or
    - the library has been already initialized (i.e. vgInitializeMZT has been already called) but no contexts
      have been already created

  Return
    VG_ILLEGAL_ARGUMENT_ERROR if the library has been already initialized and one or more contexts have been already created
    VG_ILLEGAL_ARGUMENT_ERROR if the specified value is not valid for the given configuration parameter
    VG_NO_ERROR in all other cases (i.e. in case of success).

  Notes
    Certain VGConfigMzt values refer to read-only parameters. Calling vgConfigSetMZT on these parameters has no effect (VG_NO_ERROR is returned).
    This function does not set the internal context error, so it has no influence on the value returned by vgGetError
*/
VG_API_CALL VGErrorCode VG_API_ENTRY vgConfigSetMZT(VGConfigMzt config,
                                                    VGfloat value) VG_API_EXIT;

/*
  Description
    Get the current value relative to the specified configuration parameter.
    If the given parameter is invalid (i.e. it does not correspond to any
    value of the VGConfigMzt enum type), a negative number is returned.

  Notes
    This function does not set the internal context error, so it has no influence on the value returned by vgGetError
*/
VG_API_CALL VGfloat VG_API_ENTRY vgConfigGetMZT(VGConfigMzt config) VG_API_EXIT;

/*-------------------------------------------------------------------------------
* EGL-like layer
*------------------------------------------------------------------------------*/

/*
  Description
    Initialize the library. After initialization, it is possible to create contexts and drawing surfaces.
    NB: in a multi-thread program it is recommended to initialize the library once before the creation of threads.

  Return
    VG_TRUE is returned on success, else VG_FALSE.

  Notes
    Initializing an already-initialized library is allowed, but the only effect of such a call is to return VG_TRUE.
*/
VG_API_CALL VGboolean VG_API_ENTRY vgInitializeMZT(void) VG_API_EXIT;

/*
  Description
    Terminate the library. All "pending" resources (e.g. contexts and drawing surfaces) not yet destroyed are released and deallocated.
    NB: in a multi-thread program it is recommended to terminate the library once after the termination of all threads.

  Notes
    Terminate an already-terminated library is allowed: in this case nothing is done.
*/
VG_API_CALL void VG_API_ENTRY vgTerminateMZT(void) VG_API_EXIT;

/*
  Description
    Create and initialize an OpenVG context, specifying an optional shared context.
    All shareable data (OpenVG handles) will be shared by the given shared context, all other
    contexts shared context already shares with, and the newly created context.

  Parameters
    sharedContext: NULL or a pointer to a previously created context.
  
  Return
    NULL if the library wasn't initialized by an initial call to the vgInitializeMZT function.
    NULL if a memory allocation error occurred.
    NULL if specified shared context is non NULL but it's not valid (i.e. was not created using this function).
    A valid pointer to the newly created context, if the operation is successful.
*/
VG_API_CALL void* VG_API_ENTRY vgPrivContextCreateMZT(void* sharedContext) VG_API_EXIT;

/*
  Description
    Destroy a previously created OpenVG context.

  Parameters
    context: pointer to a (valid) previously created context.
*/
VG_API_CALL void VG_API_ENTRY vgPrivContextDestroyMZT(void* context) VG_API_EXIT;

/*
  Description
    Create and initialize a drawing surface. In the detail this function allocates:
      - a 32bit drawing surface (AmanithVG SRE)
      - an 8bit alphaMask buffer, if alphaMask parameter is VG_TRUE (AmanithVG SRE / GLE / GLS)

  Parameters
    width: desired width, in pixels.
    height: desired height, in pixels.
    linearColorSpace: VG_TRUE for linear color space, VG_FALSE for sRGB color space.
    alphaPremultiplied: VG_TRUE for premultiplied alpha format, VG_FALSE for unpremultiplied alpha format.
    NB: AmanithVG GLE supports only premultiplied surface formats, so it ignores this value.
    alphaMask: VG_TRUE if the drawing surface must support/contain OpenVG alpha mask, else VG_FALSE.

  Return
    NULL if a memory allocation error occurred.
    NULL if width or height are less than or equal zero.
    A valid pointer to the newly created surface, if the operation is successful.

  Notes
    Specified surface width and height are silently clamped to the value returned by vgConfigGetMZT(VG_CONFIG_MAX_SURFACE_DIMENSION_MZT);
    the user should call vgPrivGetSurfaceWidthMZT, vgPrivGetSurfaceHeightMZT after vgPrivSurfaceCreateMZT in order to check real drawing surface dimensions.

  Hints
    For best performance use non-linear premultiplied color space.
*/
VG_API_CALL void* VG_API_ENTRY vgPrivSurfaceCreateMZT(VGint width,
                                                      VGint height,
                                                      VGboolean linearColorSpace,
                                                      VGboolean alphaPremultiplied,
                                                      VGboolean alphaMask) VG_API_EXIT;

/*
  Description
    Create and initialize a drawing surface, specifying direct memory buffers for pixels and (optionally) alpha mask.
	In the detail:
      - 'pixels' parameter must be a non-NULL 4bytes aligned (AmanithVG SRE) pointer; it must point to a contiguous memory area
	  of a size not less that 'width' * 'height' * 4 (AmanithVG SRE) bytes.
      - 'alphaMaskPixels' parameter, if non NULL, specifies a pointer to the memory area that will store the 8bit alpha mask; if non NULL, it must point to a contiguous
	  memory area of a size not less that 'width' * 'height' bytes

    NB: this function is available on AmanithVG SRE only.

  Parameters
    width: desired width, in pixels.
    height: desired height, in pixels.
    linearColorSpace: VG_TRUE for linear color space, VG_FALSE for sRGB color space.
    alphaPremultiplied: VG_TRUE for premultiplied alpha format, VG_FALSE for unpremultiplied alpha format.
	pixels: a non NULL pointer to the memory area that will be used to store drawing surface pixels; it must be aligned to 4bytes (AmanithVG SRE).
	alphaMaskPixels: an optional pointer to the memory area that will store the 8bit alpha mask; if NULL, OpenVG alpha mask feature will be silently disabled at all.

  Return
    NULL if width or height are less than or equal to zero.
	NULL if 'pixels' parameter is NULL or not properly aligned.
	NULL if 'alphaMaskPixels' is non NULL but equal to 'pixels' parameter.
    A valid pointer to the newly created surface, if the operation is successful.

  Notes
    Specified surface width and height are silently clamped to the value returned by vgConfigGetMZT(VG_CONFIG_MAX_SURFACE_DIMENSION_MZT);
    the user should call vgPrivGetSurfaceWidthMZT, vgPrivGetSurfaceHeightMZT after vgPrivSurfaceCreateMZT in order to check real drawing surface dimensions.
    This function is available on AmanithVG SRE only.

  Hints
    For best performance use non-linear premultiplied color space.
*/
VG_API_CALL void* VG_API_ENTRY vgPrivSurfaceCreateByPointerMZT(VGint width,
															   VGint height,
															   VGboolean linearColorSpace,
															   VGboolean alphaPremultiplied,
															   void* pixels,
															   VGubyte* alphaMaskPixels) VG_API_EXIT;

/*
  Description
    Create a drawing surface bound to the given VGImage.

  Parameters
    image: a VGImage handle.
    alphaMask: VG_TRUE if the drawing surface must support/contain OpenVG alpha mask, else VG_FALSE.

  Return
    NULL if a memory allocation error occurred.
    NULL if the image is not valid for any already created context.
    NULL if the image is used by OpenVG (e.g. is used as a paint pattern or is used as a font glyph).
    A valid pointer to the newly created surface, if the operation is successful.

  Notes
    This function is implemented by AmanithVG SRE only (AmanithVG GLE always returns a NULL value).
*/
VG_API_CALL void* VG_API_ENTRY vgPrivSurfaceCreateFromImageMZT(VGImage image,
                                                               VGboolean alphaMask) VG_API_EXIT;

/*
  Description
    Resize the dimensions of the specified drawing surface. This function:
      - reallocates the drawing surface pixels buffer, according to new specified dimensions (AmanithVG SRE).
      - if the surface contains the alpha mask buffer, it reallocates that 8bit buffer according to new specified dimensions (AmanithVG SRE / GLE / GLS).

  Parameters
    surface: pointer to a (valid) previously created drawing surface.
    width: the new desired width, in pixels.
    height: the new desired width, in pixels.

  Return
    VG_FALSE if a memory allocation error occurred.
    VG_FALSE if width or height are less than or equal zero.
    VG_FALSE if the specified surface is bound to an OpenVG image.
    VG_TRUE if the operation is successful.

  Notes
    Specified surface width and height are silently clamped to the value returned by vgConfigGetMZT(VG_CONFIG_MAX_SURFACE_DIMENSION_MZT);
    the user should call vgPrivGetSurfaceWidthMZT, vgPrivGetSurfaceHeightMZT after vgPrivSurfaceResizeMZT in order to check real drawing surface dimensions.
    The specified surface must be valid (so not destroyed); to be only referenced (e.g. made current but destroyed) it's not enough.
*/
VG_API_CALL VGboolean VG_API_ENTRY vgPrivSurfaceResizeMZT(void* surface,
                                                          VGint width,
                                                          VGint height) VG_API_EXIT;

/*
  Description
    Resize the given drawing surface, specifying new memory buffers for pixels and (optionally) alpha mask.
    NB: this function is available on AmanithVG SRE only.

  Parameters
    surface: pointer to a (valid) previously created drawing surface.
    width: the new desired width, in pixels.
    height: the new desired width, in pixels.
	pixels: a non NULL pointer to the memory area that will store the resized surface pixels; it must be aligned to 4bytes (AmanithVG SRE).
	alphaMaskPixels: an optional pointer to the memory area that will store the resized 8bit alpha mask; if NULL, OpenVG alpha mask feature will be silently disabled at all.

  Return
    VG_FALSE if a memory allocation error occurred.
    VG_FALSE if width or height are less than or equal to zero.
    VG_FALSE if the specified surface is bound to an OpenVG image.
	VG_FALSE if 'pixels' parameter is NULL or not properly aligned.
	VG_FALSE if 'alphaMaskPixels' is non NULL but equal to 'pixels' parameter.
	VG_FALSE if the specified surface has been created through the vgPrivSurfaceCreateMZT function (in this case use vgPrivSurfaceResizeMZT).
    VG_TRUE if the operation is successful.

  Notes
    Specified surface width and height are silently clamped to the value returned by vgConfigGetMZT(VG_CONFIG_MAX_SURFACE_DIMENSION_MZT);
    the user should call vgPrivGetSurfaceWidthMZT, vgPrivGetSurfaceHeightMZT after vgPrivSurfaceResizeByPointerMZT in order to check real drawing surface dimensions.
    The specified surface must be valid (so not destroyed); to be only referenced (e.g. made current but destroyed) it's not enough.
    This function is available on AmanithVG SRE only.
*/
VG_API_CALL VGboolean VG_API_ENTRY vgPrivSurfaceResizeByPointerMZT(void* surface,
																   VGint width,
																   VGint height,
																   void* pixels,
																   VGubyte* alphaMaskPixels) VG_API_EXIT;

/*
  Description
    Destroy a previously created drawing surface.

  Parameters
    surface: pointer to a (valid) previously created drawing surface.
*/
VG_API_CALL void VG_API_ENTRY vgPrivSurfaceDestroyMZT(void* surface) VG_API_EXIT;

/*
  Description
    Get the width (in pixels) of the given drawing surface.

  Parameters
    surface: a valid (i.e. still referenced) drawing surface.

  Return
    0 if the surface is not referenced, else the surface width.
*/
VG_API_CALL VGint VG_API_ENTRY vgPrivGetSurfaceWidthMZT(const void* surface) VG_API_EXIT;

/*
  Description
    Get the height (in pixels) of the given drawing surface.

  Parameters
    surface: a valid (i.e. still referenced) drawing surface.

  Return
    0 if the surface is not referenced, else the surface height.
*/
VG_API_CALL VGint VG_API_ENTRY vgPrivGetSurfaceHeightMZT(const void* surface) VG_API_EXIT;

/*
  Description
    Get the format of the given drawing surface.
*/
VG_API_CALL VGImageFormat VG_API_ENTRY vgPrivGetSurfaceFormatMZT(const void* surface) VG_API_EXIT;

/*
  Description
    Get the direct access to the pixels of the given drawing surface.
    It should be used only to blit the surface on the screen, according to the platform graphic subsystem.

  Return
    NULL, in AmanithVG SRE if the surface is bound to an OpenVG image.
    NULL, in AmanithVG GLE / GLS.
    A valid pointer in all other cases.
*/
VG_API_CALL const VGubyte* VG_API_ENTRY vgPrivGetSurfacePixelsMZT(const void* surface) VG_API_EXIT;

/*
  Description
    Bind the specified context to the given drawing surface.

  Parameters
    context: NULL or a pointer to a (valid) previously created OpenVG context.
    surface: NULL or a pointer to a (valid) previously created drawing surface.
  
  Return
    VG_FALSE if one parameter (context or surface) is NULL and the other is not NULL.
    VG_FALSE if the surface is bound to an OpenVG image and such image is used by OpenVG (e.g. is used as a paint pattern or is used as a font glyph).
    VG_TRUE if the operation is successful.

  Notes
    In AmanithVG GLE / GLS this function returns VG_FALSE if GL preconditions (e.g. the presence of depth or stencil buffer) are not satisfied.
*/
VG_API_CALL VGboolean VG_API_ENTRY vgPrivMakeCurrentMZT(void* context,
                                                        void* surface) VG_API_EXIT;

/*
  Description
    Get the width (in pixels) of the drawing surface made current.
    If no surface has been made current, 0 is returned.
*/
VG_API_CALL VGint VG_API_ENTRY vgGetSurfaceWidthMZT(void) VG_API_EXIT;

/*
  Description
    Get the height (in pixels) of the drawing surface made current.
    If no surface has been made current, 0 is returned.
*/
VG_API_CALL VGint VG_API_ENTRY vgGetSurfaceHeightMZT(void) VG_API_EXIT;

/*
  Description
    Get the format of the drawing surface made current.
    If no surface has been made current, VG_IMAGE_FORMAT_FORCE_SIZE is returned.
*/
VG_API_CALL VGImageFormat VG_API_ENTRY vgGetSurfaceFormatMZT(void) VG_API_EXIT;

/*
  Description
    Get the direct access to the pixels of the drawing surface made current.
    It should be used only to blit the surface on the screen, according to the platform graphic subsystem.

  Return
    NULL, in AmanithVG SRE if the surface made current is bound to an OpenVG image.
    NULL, in AmanithVG GLE / GLS.
    A valid pointer in all other cases.
*/
VG_API_CALL const VGubyte* VG_API_ENTRY vgGetSurfacePixelsMZT(void) VG_API_EXIT;

/*
  Description
    Reset depth and stencil buffers to a valid state for the next frame (AmanithVG GLE / GLS).

  Notes
    In AmanithVG SRE this function does nothing.
*/
VG_API_CALL void VG_API_ENTRY vgPostSwapBuffersMZT(void) VG_API_EXIT;

/* This is a generic function pointer type, whose name indicates it must be cast to the proper type *and calling convention* before use. */
typedef void (*__vgMustCastToProperFunctionPointerType)(void);
/* Now, define eglGetProcAddress using the generic function ptr. type */
VG_API_CALL __vgMustCastToProperFunctionPointerType VG_API_ENTRY vgGetProcAddressMZT(const char* procname) VG_API_EXIT;

#ifdef __cplusplus 
} /* extern "C" */
#endif

#endif /* _VGEXT_H */
