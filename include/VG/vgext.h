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
    VG_API_CALL void VG_API_ENTRY vgResetStatisticsAM(const VGbitfield statistics) VG_API_EXIT;
    #define vgResetStatisticsMZT vgResetStatisticsAM
    /*
        In the statistics build, this returns statistics gathered since the last vgResetStatisticsAM.
        Errors: VG_ILLEGAL_ARGUMENT_ERROR: if statistic is not one of the values from VGStatisticInfo enumeration.
    */
    VG_API_CALL VGint VG_API_ENTRY vgGetStatisticiAM(const VGStatisticInfoMzt statistic) VG_API_EXIT;
    #define vgGetStatisticiMZT vgGetStatisticiAM
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

        Errors:
            VG_BAD_HANDLE_ERROR if path is not a valid path handle or if it is VG_INVALID_HANDLE, or is not shared with the current context
            VG_OUT_OF_MEMORY_ERROR if we have already reached the maximum number of clip paths that can be pushed
    */
    VG_API_CALL void VG_API_ENTRY vgClipPathPushAM(VGPath path) VG_API_EXIT;
    #define vgClipPathPushMZT vgClipPathPushAM

    /*
        Pop out the last pushed clip path.
    */
    VG_API_CALL void VG_API_ENTRY vgClipPathPopAM(void) VG_API_EXIT;
    #define vgClipPathPopMZT vgClipPathPopAM

    /*
        Clear the whole clip paths queue.
    */
    VG_API_CALL void VG_API_ENTRY vgClipPathClearAM(void) VG_API_EXIT;
    #define vgClipPathClearMZT vgClipPathClearAM

#endif // VG_VGEXT_PROTOTYPES

    typedef void (VG_API_ENTRYP PFNVGCLIPPATHPUSHMZTPROC) (VGPath path);
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
        Equal to the standard vgColorMatrix filter: in this case the filter will be applied on the specified image (i.e. the given image is
        both the source and the destination).
    */
    VG_API_CALL void VG_API_ENTRY vgColorMatrixAM(VGImage img,
                                                  const VGfloat *matrix) VG_API_EXIT;
    #define vgColorMatrixMZT vgColorMatrixAM
#endif // VG_VGEXT_PROTOTYPES
    typedef void (VG_API_ENTRYP PFNVGCOLORMATRIXMZTPROC) (VGImage img,
                                                          const VGfloat *matrix);
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

    Such behavior is the one needed by the SVG masking (see https://www.w3.org/TR/SVG/masking.html).
  */
  VG_API_CALL void VG_API_ENTRY vgMaskAM(VGHandle mask,
                                         VGMaskOperation operation,
                                         VGint x,
                                         VGint y,
                                         VGint width,
                                         VGint height) VG_API_EXIT;
    #define vgMaskMZT vgMaskAM
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
*------------------------------------------------------------------------------*/
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

  NB: memory facilities settings can be modified only when no allocations has been made yet!!!
  Typically before "initializing" AmanithVG by creating a context and a drawing surface; or after having destroyed
  all handles, surfaces and contexts.

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
*/
VG_API_CALL VGboolean VG_API_ENTRY vgMemoryDebugSetAM(VGint maxAllocableAmount,
                                                      char* logBuffer,
                                                      VGuint logBufferCapacity,
                                                      VGbitfield logLevel,
                                                      VGboolean enableConsistencyChecks,
                                                      VGboolean enableMemoryRetrivial) VG_API_EXIT;
#define vgMemoryDebugSetMZT vgMemoryDebugSetAM

/*
  Description
  Get the total allocated memory by the library.

  Return
  A negative number if the memory debug facilities have not been enabled through the vgMemoryDebugSetMZT function; else
  the total allocated memory by the library.
*/
VG_API_CALL VGint VG_API_ENTRY vgMemoryAllocatedGetAM(void) VG_API_EXIT;
#define vgMemoryAllocatedGetMZT vgMemoryAllocatedGetAM

/*-------------------------------------------------------------------------------
* EGL-like layer
*------------------------------------------------------------------------------*/
/*
  Description
    Create and initialize an OpenVG context, specifying an optional shared context.
    All shareable data (OpenVG handles) will be shared by the given shared context, all other
    contexts shared context already shares with, and the newly created context.

  Parameters
    sharedContext: NULL or a pointer to a previously created context.
  
  Return
    NULL if a memory allocation error occurred.
    NULL if specified shared context is non NULL but it's not valid (i.e. was not created using this function).
    A valid pointer to the newly created context, if the operation is successful.
*/
VG_API_CALL void* VG_API_ENTRY vgPrivContextCreateAM(void *sharedContext) VG_API_EXIT;
#define vgPrivContextCreateMZT vgPrivContextCreateAM

/*
  Description
    Destroy a previously created OpenVG context.

  Parameters
    context: pointer to a (valid) previously created context.
*/
VG_API_CALL void VG_API_ENTRY vgPrivContextDestroyAM(void *context) VG_API_EXIT;
#define vgPrivContextDestroyMZT vgPrivContextDestroyAM

/*
  Description
    Get the maximum dimension allowed for drawing surfaces.

  Notes
    This is the maximum valid value that can be specified as 'width' and 'height' for the vgPrivSurfaceCreateMZT and vgPrivSurfaceResizeMZT functions.
    Bigger values are silently clamped to it.

*/
VG_API_CALL VGint VG_API_ENTRY vgPrivSurfaceMaxDimensionGetAM(void);
#define vgPrivSurfaceMaxDimensionGetMZT vgPrivSurfaceMaxDimensionGetAM

/*
  Description
    Create and initialize a drawing surface. In the detail this function allocates:
      - a 32bit drawing surface (AmanithVG SRE)
      - a 16bit drawing surface (AmanithVG SRE Lite)
      - an 8bit alphaMask buffer, if alphaMask parameter is VG_TRUE (AmanithVG SRE / SRE Lite / GLE / GLS)

  Parameters
    width: desired width, in pixels.
    height: desired height, in pixels.
    linearColorSpace: VG_TRUE for linear color space, VG_FALSE for sRGB color space. This parameter is ignored in AmanithVG SRE Lite, since it always uses non-linear color space.
    alphaPremultiplied VG_TRUE for premultiplied alpha format, VG_FALSE for unpremultiplied alpha format.
    This parameter is ignored in AmanithVG SRE Lite, since its drawing surfaces do not have the alpha component.
    NB: AmanithVG GLE supports only premultiplied surface formats, so it ignores this value.
    alphaMask: VG_TRUE if the drawing surface must support/contain OpenVG alpha mask, else VG_FALSE.

  Return
    NULL if a memory allocation error occurred.
    NULL if width or height are less than or equal zero.
    A valid pointer to the newly created surface, if the operation is successful.

  Notes
    Specified surface width and height are silently clamped to the value returned by the vgPrivSurfaceMaxDimensionGetMZT function;
    the user should call vgPrivGetSurfaceWidthMZT, vgPrivGetSurfaceHeightMZT after vgPrivSurfaceCreateMZT in order to check real drawing surface dimensions.

  Hints
    For best performance use non-linear premultiplied color space.
*/
VG_API_CALL void* VG_API_ENTRY vgPrivSurfaceCreateAM(VGint width,
                                                     VGint height,
                                                     VGboolean linearColorSpace,
                                                     VGboolean alphaPremultiplied,
                                                     VGboolean alphaMask) VG_API_EXIT;
#define vgPrivSurfaceCreateMZT vgPrivSurfaceCreateAM


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
*/
VG_API_CALL void* VG_API_ENTRY vgPrivSurfaceCreateFromImageAM(VGImage image,
                                                              VGboolean alphaMask) VG_API_EXIT;
#define vgPrivSurfaceCreateFromImageMZT vgPrivSurfaceCreateFromImageAM


/*
  Description
    Resize the dimensions of the specified drawing surface. This function:
      - reallocates the drawing surface pixels buffer, according to new specified dimensions (AmanithVG SRE / SRE Lite).
      - if the surface contains the alpha mask buffer, it reallocates that 8bit buffer according to new specified dimensions (AmanithVG SRE / SRE Lite / GLE / GLS).

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
    Specified surface width and height are silently clamped to the value returned by the vgPrivSurfaceMaxDimensionGetMZT function;
    the user should call vgPrivGetSurfaceWidthMZT, vgPrivGetSurfaceHeightMZT after vgPrivSurfaceResizeMZT in order to check real drawing surface dimensions.
    The specified surface must be valid (so not destroyed); to be only referenced (e.g. made current but destroyed) it's not enough.
*/
VG_API_CALL VGboolean VG_API_ENTRY vgPrivSurfaceResizeAM(void *surface,
                                                         VGint width,
                                                         VGint height) VG_API_EXIT;
#define vgPrivSurfaceResizeMZT vgPrivSurfaceResizeAM

/*
  Description
    Destroy a previously created drawing surface.

  Parameters
    surface: pointer to a (valid) previously created drawing surface.
*/
VG_API_CALL void VG_API_ENTRY vgPrivSurfaceDestroyAM(void *surface) VG_API_EXIT;
#define vgPrivSurfaceDestroyMZT vgPrivSurfaceDestroyAM

/*
  Description
    Get the width (in pixels) of the given drawing surface.

  Parameters
    surface: a valid (i.e. still referenced) drawing surface.

  Return
    0 if the surface is not referenced, else the surface width.
*/
VG_API_CALL VGint VG_API_ENTRY vgPrivGetSurfaceWidthAM(const void *surface) VG_API_EXIT;
#define vgPrivGetSurfaceWidthMZT vgPrivGetSurfaceWidthAM

/*
  Description
    Get the height (in pixels) of the given drawing surface.

  Parameters
    surface: a valid (i.e. still referenced) drawing surface.

  Return
    0 if the surface is not referenced, else the surface height.
*/
VG_API_CALL VGint VG_API_ENTRY vgPrivGetSurfaceHeightAM(const void *surface) VG_API_EXIT;
#define vgPrivGetSurfaceHeightMZT vgPrivGetSurfaceHeightAM

/*
  Description
    Get the format of the given drawing surface.
*/
VG_API_CALL VGImageFormat VG_API_ENTRY vgPrivGetSurfaceFormatAM(const void *surface) VG_API_EXIT;
#define vgPrivGetSurfaceFormatMZT vgPrivGetSurfaceFormatAM

/*
  Description
    Get the direct access to the pixels of the given drawing surface.
    It should be used only to blit the surface on the screen, according to the platform graphic subsystem.

  Return
    NULL, in AmanithVG SRE/SRE Lite if the surface is bound to an OpenVG image.
    NULL, in AmanithVG GLE / GLS.
    A valid pointer in all other cases.
*/
VG_API_CALL const VGubyte* VG_API_ENTRY vgPrivGetSurfacePixelsAM(const void *surface) VG_API_EXIT;
#define vgPrivGetSurfacePixelsMZT vgPrivGetSurfacePixelsAM

/*
  Description
    Bind the specified context to the given drawing surface.

  Parameters
    _context: NULL or a pointer to a (valid) previously created OpenVG context.
    _surface: NULL or a pointer to a (valid) previously created drawing surface.
  
  Return
    VG_FALSE if one parameter (context or surface) is NULL and the other is not NULL.
    VG_FALSE if the surface is bound to an OpenVG image and such image is used by OpenVG (e.g. is used as a paint pattern or is used as a font glyph).
    VG_TRUE if the operation is successful.

  Notes
    In AmanithVG GLE / GLS this function returns VG_FALSE if GL preconditions (e.g. the presence of depth or stencil buffer) are not satisfied.
*/
VG_API_CALL VGboolean VG_API_ENTRY vgPrivMakeCurrentAM(void *context,
                                                       void *surface) VG_API_EXIT;
#define vgPrivMakeCurrentMZT vgPrivMakeCurrentAM

/*
  Description
    Get the width (in pixels) of the drawing surface made current.
    If no surface has been made current, 0 is returned.
*/
VG_API_CALL VGint VG_API_ENTRY vgGetSurfaceWidthAM(void) VG_API_EXIT;
#define vgGetSurfaceWidthMZT vgGetSurfaceWidthAM

/*
  Description
    Get the height (in pixels) of the drawing surface made current.
    If no surface has been made current, 0 is returned.
*/
VG_API_CALL VGint VG_API_ENTRY vgGetSurfaceHeightAM(void) VG_API_EXIT;
#define vgGetSurfaceHeightMZT vgGetSurfaceHeightAM

/*
  Description
    Get the format of the drawing surface made current.
    If no surface has been made current, VG_IMAGE_FORMAT_FORCE_SIZE is returned.
*/
VG_API_CALL VGImageFormat VG_API_ENTRY vgGetSurfaceFormatAM(void) VG_API_EXIT;
#define vgGetSurfaceFormatMZT vgGetSurfaceFormatAM

/*
  Description
    Get the direct access to the pixels of the drawing surface made current.
    It should be used only to blit the surface on the screen, according to the platform graphic subsystem.

  Return
    NULL, in AmanithVG SRE/SRE Lite if the surface made current is bound to an OpenVG image.
    NULL, in AmanithVG GLE / GLS.
    A valid pointer in all other cases.
*/
VG_API_CALL const VGubyte* VG_API_ENTRY vgGetSurfacePixelsAM(void) VG_API_EXIT;
#define vgGetSurfacePixelsMZT vgGetSurfacePixelsAM

/*
  Description
    Reset depth and stencil buffers to a valid state for the next frame (AmanithVG GLE / GLS).

  Notes
    In AmanithVG SRE/SRE Lite this function does nothing.
*/
VG_API_CALL void VG_API_ENTRY vgPostSwapBuffersAM(void) VG_API_EXIT;
#define vgPostSwapBuffersMZT vgPostSwapBuffersAM


/* This is a generic function pointer type, whose name indicates it must be cast to the proper type *and calling convention* before use. */
typedef void (*__vgMustCastToProperFunctionPointerType)(void);
/* Now, define eglGetProcAddress using the generic function ptr. type */
VG_API_CALL __vgMustCastToProperFunctionPointerType VG_API_ENTRY vgGetProcAddressAM(const char* procname) VG_API_EXIT;
#define vgGetProcAddressMZT vgGetProcAddressAM

#ifdef __cplusplus 
} /* extern "C" */
#endif

#endif /* _VGEXT_H */
