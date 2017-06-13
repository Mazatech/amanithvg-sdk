/****************************************************************************
** Copyright (C) 2004-2017 Mazatech S.r.l. All rights reserved.
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
#include "tutorial_01.h"
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define PAINT_COLOR 0
#define PAINT_LINGRAD 1
#define PAINT_RADGRAD 2
#define PAINT_CONGRAD 3
#define PAINT_PATTERN 4

#define PATTERN_WIDTH 64
#define PATTERN_HEIGHT 64

#define MOUSE_BUTTON_NONE 0
#define MOUSE_BUTTON_LEFT 1
#define MOUSE_BUTTON_RIGHT 2

// the flower-like path
VGPath path;
// paints
VGPaint color;
VGPaint linGrad;
VGPaint radGrad;
VGPaint conGrad;
VGint conGradRepeats;
VGboolean conGradSupported;
VGPaint pattern;
VGImage patternImage;
// current paint states
VGuint paintIndex;
VGboolean linearInterpolation;
VGboolean smoothRampSupported;
VGColorRampSpreadMode spreadMode;
VGTilingMode tilingMode;
VGboolean animate;
VGboolean scissoring;
VGuint scissorRectsConf;
VGboolean masking;
// current transformation
VGfloat rotation;
VGfloat scale;
VGfloat translation[2];
// mouse state
VGint mouseButton;
VGint oldMouseX, oldMouseY;

// check if a string can be found in an OpenVG extension string
static VGboolean extensionFind(const char* string,
                               const char* extensions) {

    char *position, *terminator;
    const char* start = extensions;

    while (1) {
        position = (char *)strstr(start, string);
        if (!position) {
            return VG_FALSE;
        }
        terminator = position + strlen(string);
        if (position == start || *(position - 1) == ' ') {
            if (*terminator == ' ' || *terminator == '\0') {
                break;
            }
        }
        start = terminator;
    }

    return VG_TRUE;
}

static void extensionsCheck(void) {

    // get the list of supported OpenVG extensions
    const char* extensions = (const char*)vgGetString(VG_EXTENSIONS);
    // check for the support of VG_MZT_conical_gradient extension
    conGradSupported = extensionFind("VG_MZT_conical_gradient", extensions);
    // check for the support of VG_MZT_color_ramp_interpolation extension
    smoothRampSupported = extensionFind("VG_MZT_color_ramp_interpolation", extensions);
}

static void genPaints(void) {

    VGfloat colKeys[25], col[4];
    VGfloat radGradParams[5], linGradParams[4];
    VGfloat conGradParams[5];
    VGuint *pixels, i, j;

    color = vgCreatePaint();
    linGrad = vgCreatePaint();
    radGrad = vgCreatePaint();
    if (conGradSupported) {
        conGrad = vgCreatePaint();
    }
    pattern = vgCreatePaint();

    // solid color
    col[0] = 0.8f;
    col[1] = 0.5f;
    col[2] = 0.1f;
    col[3] = 1.0f;
    vgSetParameteri(color, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    vgSetParameterfv(color, VG_PAINT_COLOR, 4, col);

    // linear gradient
    colKeys[0]  = 0.00f; colKeys[1]  = 0.4f; colKeys[2]  = 0.0f; colKeys[3]  = 0.6f; colKeys[4]  = 1.0f;
    colKeys[5]  = 0.25f; colKeys[6]  = 0.9f; colKeys[7]  = 0.5f; colKeys[8]  = 0.1f; colKeys[9]  = 1.0f;
    colKeys[10] = 0.50f; colKeys[11] = 0.8f; colKeys[12] = 0.8f; colKeys[13] = 0.0f; colKeys[14] = 1.0f;
    colKeys[15] = 0.75f; colKeys[16] = 0.0f; colKeys[17] = 0.3f; colKeys[18] = 0.5f; colKeys[19] = 1.0f;
    colKeys[20] = 1.00f; colKeys[21] = 0.4f; colKeys[22] = 0.0f; colKeys[23] = 0.6f; colKeys[24] = 1.0f;
    linGradParams[0] = 320.0f; linGradParams[1] = 320.0f;
    linGradParams[2] = 192.0f; linGradParams[3] = 192.0f;
    vgSetParameteri(linGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_LINEAR_GRADIENT);
    vgSetParameterfv(linGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
    vgSetParameterfv(linGrad, VG_PAINT_LINEAR_GRADIENT, 4, linGradParams);
    vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);
    // radial gradient
    colKeys[0]  = 0.00f; colKeys[1]  = 0.4f; colKeys[2]  = 0.0f; colKeys[3]  = 0.6f; colKeys[4]  = 1.0f;
    colKeys[5]  = 0.25f; colKeys[6]  = 0.9f; colKeys[7]  = 0.5f; colKeys[8]  = 0.1f; colKeys[9]  = 1.0f;
    colKeys[10] = 0.50f; colKeys[11] = 0.8f; colKeys[12] = 0.8f; colKeys[13] = 0.0f; colKeys[14] = 1.0f;
    colKeys[15] = 0.75f; colKeys[16] = 0.0f; colKeys[17] = 0.3f; colKeys[18] = 0.5f; colKeys[19] = 1.0f;
    colKeys[20] = 1.00f; colKeys[21] = 0.4f; colKeys[22] = 0.0f; colKeys[23] = 0.6f; colKeys[24] = 1.0f;
    radGradParams[0] = 256.0f; radGradParams[1] = 256.0f;
    radGradParams[2] = 200.0f; radGradParams[3] = 200.0f;
    radGradParams[4] = 100.0f;
    vgSetParameteri(radGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_RADIAL_GRADIENT);
    vgSetParameterfv(radGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
    vgSetParameterfv(radGrad, VG_PAINT_RADIAL_GRADIENT, 5, radGradParams);
    vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);
    // conical gradient
    if (conGradSupported) {
        colKeys[0]  = 0.00f; colKeys[1]  = 0.4f; colKeys[2]  = 0.0f; colKeys[3]  = 0.6f; colKeys[4]  = 1.0f;
        colKeys[5]  = 0.25f; colKeys[6]  = 0.9f; colKeys[7]  = 0.5f; colKeys[8]  = 0.1f; colKeys[9]  = 1.0f;
        colKeys[10] = 0.50f; colKeys[11] = 0.8f; colKeys[12] = 0.8f; colKeys[13] = 0.0f; colKeys[14] = 1.0f;
        colKeys[15] = 0.75f; colKeys[16] = 0.0f; colKeys[17] = 0.3f; colKeys[18] = 0.5f; colKeys[19] = 1.0f;
        colKeys[20] = 1.00f; colKeys[21] = 0.4f; colKeys[22] = 0.0f; colKeys[23] = 0.6f; colKeys[24] = 1.0f;
        conGradParams[0] = 256.0f; conGradParams[1] = 256.0f;
        conGradParams[2] = 200.0f; conGradParams[3] = 200.0f;
        conGradRepeats = 1;
        conGradParams[4] = (VGfloat)conGradRepeats;
        vgSetParameteri(conGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_CONICAL_GRADIENT_MZT);
        vgSetParameterfv(conGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
        vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
        vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);
    }
    // pattern
    patternImage = vgCreateImage(VG_sRGBA_8888_PRE, PATTERN_WIDTH, PATTERN_HEIGHT, VG_IMAGE_QUALITY_FASTER);
    pixels = (VGuint *)malloc(PATTERN_WIDTH * PATTERN_HEIGHT * sizeof(VGuint));
    for (i = 0; i < PATTERN_HEIGHT; ++i) {
        for (j = 0; j < PATTERN_WIDTH; ++j) {
            if (i < PATTERN_HEIGHT / 2) {
                pixels[i * PATTERN_WIDTH + j] = (j < (PATTERN_WIDTH / 2)) ? 0xFF6030FF : 0xFFB060FF;
            }
            else {
                pixels[i * PATTERN_WIDTH + j] = (j < (PATTERN_WIDTH / 2)) ? 0xFF9090FF : 0xFF30B0FF;
            }
        }
    }
    vgImageSubData(patternImage, (const void *)pixels, PATTERN_WIDTH * sizeof(VGuint), VG_sRGBA_8888_PRE, 0, 0, PATTERN_WIDTH, PATTERN_HEIGHT);
    vgSetParameteri(pattern, VG_PAINT_TYPE, VG_PAINT_TYPE_PATTERN);
    vgSetParameteri(pattern, VG_PAINT_PATTERN_TILING_MODE, VG_TILE_PAD);
    vgPaintPattern(pattern, patternImage);
    free(pixels);
}

static void genPaths(void) {

    VGubyte commands[] = {
        VG_MOVE_TO,
        VG_CUBIC_TO,
        VG_CUBIC_TO,
        VG_CUBIC_TO,
        VG_CUBIC_TO,
        VG_CLOSE_PATH
    };

    VGfloat coordinates[] = {
        // move to
        236.0f, 276.0f,
        // cubic to
        56.0f, 426.0f, 56.0f, 86.0f, 236.0f, 236.0f,
        // cubic to
        86.0f, 56.0f, 426.0f, 56.0f, 276.0f, 236.0f,
        // cubic to
        456.0f, 86.0f, 456.0f, 426.0f, 276.0f, 276.0f,
        // cubic to
        426.0f, 456.0f, 86.0f, 456.0f, 236.0f, 276.0f
    };

    path = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vgAppendPathData(path, sizeof(commands) / sizeof(VGubyte), commands, coordinates);
}

static void genAlphaMask(const VGint surfaceWidth,
                         const VGint surfaceHeight) {

    VGImage alphaImage = vgCreateImage(VG_A_8, surfaceWidth, surfaceHeight, VG_IMAGE_QUALITY_NONANTIALIASED);

    if (alphaImage != VG_INVALID_HANDLE) {
        // allocate pixels to be passed to OpenVG mask
        VGubyte* alphaPixels = (VGubyte *)malloc(surfaceWidth * surfaceHeight * sizeof(VGubyte));
        if (alphaPixels != NULL) {
            // generate a sort of circular gradient (opaque at the center, transparent at the external border)
            VGint x, y;
            VGubyte *alphaMask = alphaPixels;
            VGfloat radius = (surfaceWidth < surfaceHeight) ? (VGfloat)(surfaceWidth / 4) : (VGfloat)(surfaceHeight / 4);
            VGfloat radiusSqr = radius * radius;
            for (y = 0; y < surfaceHeight; ++y) {
                for (x = 0; x < surfaceWidth; ++x) {
                    VGfloat distSqr = ((VGfloat)((x - surfaceWidth / 2) * (x - surfaceWidth / 2) + (y - surfaceHeight / 2) * (y - surfaceHeight / 2))) / radiusSqr;
                    if (distSqr > 1.0f) {
                        distSqr = 1.0f;
                    }
                    *alphaMask++ = (VGubyte)((1.0f - distSqr) * 255.0f);
                }
            }
            // upload alpha pixels to the image
            vgImageSubData(alphaImage, alphaPixels, surfaceWidth, VG_A_8, 0, 0, surfaceWidth, surfaceHeight);
            // upload alpha image to the OpenVG alpha mask
            vgMask(alphaImage, VG_SET_MASK, 0, 0, surfaceWidth, surfaceHeight);
            // release temporary memory used to generate alpha mask pixels
            free(alphaPixels);
        }
        // destroy the temporary image
        vgDestroyImage(alphaImage);
    }
}

void tutorialInit(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    // an opaque dark grey
    VGfloat clearColor[4] = { 0.2f, 0.3f, 0.4f, 1.0f };
    VGfloat tileColor[4] = { 0.1f, 0.6f, 0.3f, 1.0f };

    // check for OpenVG extensions
    extensionsCheck();
    // generate the flower path
    genPaths();
    // generate all the paints
    genPaints();
    // generate alpha mask
    genAlphaMask(surfaceWidth, surfaceHeight);
    // set some default parameters for the OpenVG context
    vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgSetfv(VG_TILE_FILL_COLOR, 4, tileColor);
    vgSetf(VG_STROKE_LINE_WIDTH, 20.0f);
    vgSetf(VG_STROKE_CAP_STYLE, VG_CAP_BUTT);
    vgSetf(VG_STROKE_JOIN_STYLE, VG_JOIN_BEVEL);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);

    paintIndex = PAINT_COLOR;
    linearInterpolation = VG_TRUE;
    spreadMode = VG_COLOR_RAMP_SPREAD_PAD;
    tilingMode = VG_TILE_PAD;
    // start with no rotation and put the path at the center of screen
    animate = VG_FALSE;
    rotation = 0.0f;
    scale = 1.0f;
    translation[0] = 0.0f;
    translation[1] = 0.0f;
    // disable scissoring and masking
    scissoring = VG_FALSE;
    scissorRectsConf = 1;
    toggleScissorRects(surfaceWidth, surfaceHeight);
    masking = VG_FALSE;
}

void tutorialDestroy(void) {

    // release path
    vgDestroyPath(path);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(color);
    vgDestroyPaint(linGrad);
    vgDestroyPaint(radGrad);
    if (conGradSupported) {
        vgDestroyPaint(conGrad);
    }
    vgDestroyImage(patternImage);
    vgDestroyPaint(pattern);
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {
    
    // regenerate alpha mask
    genAlphaMask(surfaceWidth, surfaceHeight);
    // regenerate scissor rectangles
    scissorRectsConf++;
    toggleScissorRects(surfaceWidth, surfaceHeight);
    // move the path back to the center
    rotation = 0.0f;
    scale = 1.0f;
    translation[0] = 0.0f;
    translation[1] = 0.0f;
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    VGfloat scaleX = (VGfloat)surfaceWidth / 512.0f;
    VGfloat scaleY = (VGfloat)surfaceHeight / 512.0f;

    if (animate == VG_TRUE) {
        rotation += 0.05f;
    }

    // we don't want to apply scissoring to the vgClear (i.e. we want to clear the whole drawing surface)
    vgSeti(VG_SCISSORING, VG_FALSE);
    vgClear(0, 0, surfaceWidth, surfaceHeight);

    // take care to include the current 'rotation' and 'translation' during the user-to-surface path matrix construction
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(translation[0], translation[1]);
    vgScale(scaleX, scaleY);
    vgTranslate(256.0f, 256.0f);
    vgScale(scale, scale);
    vgRotate((VGfloat)rotation);
    vgTranslate(-256.0f, -256.0f);

    vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
    vgLoadIdentity();
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_STROKE_PAINT_TO_USER);
    vgLoadIdentity();

    switch (paintIndex) {

        case PAINT_COLOR:
            vgSetPaint(color, VG_FILL_PATH | VG_STROKE_PATH);
            break;

        case PAINT_LINGRAD:
            vgSetPaint(linGrad, VG_FILL_PATH | VG_STROKE_PATH);
            break;

        case PAINT_RADGRAD:
            vgSetPaint(radGrad, VG_FILL_PATH | VG_STROKE_PATH);
            break;

        case PAINT_CONGRAD:
            if (conGradSupported) {
                vgSetPaint(conGrad, VG_FILL_PATH | VG_STROKE_PATH);
            }
            else {
                vgSetPaint(pattern, VG_FILL_PATH | VG_STROKE_PATH);
                vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
                vgTranslate((512.0f - (VGfloat)PATTERN_WIDTH) * 0.5f, (512.0f - (VGfloat)PATTERN_HEIGHT) * 0.5f);
                vgSeti(VG_MATRIX_MODE, VG_MATRIX_STROKE_PAINT_TO_USER);
                vgTranslate((512.0f - (VGfloat)PATTERN_WIDTH) * 0.5f, (512.0f - (VGfloat)PATTERN_HEIGHT) * 0.5f);
            }
            break;

        case PAINT_PATTERN:
            if (conGradSupported) {
                vgSetPaint(pattern, VG_FILL_PATH | VG_STROKE_PATH);
                vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
                vgTranslate((512.0f - (VGfloat)PATTERN_WIDTH) * 0.5f, (512.0f - (VGfloat)PATTERN_HEIGHT) * 0.5f);
                vgSeti(VG_MATRIX_MODE, VG_MATRIX_STROKE_PAINT_TO_USER);
                vgTranslate((512.0f - (VGfloat)PATTERN_WIDTH) * 0.5f, (512.0f - (VGfloat)PATTERN_HEIGHT) * 0.5f);
            }
            break;

        default:
            vgSetPaint(color, VG_FILL_PATH | VG_STROKE_PATH);
            break;
    }

    // set scissoring
    vgSeti(VG_SCISSORING, scissoring);
    // set alpha mask
    vgSeti(VG_MASKING, masking);
    // draw the flower path
    vgDrawPath(path, VG_FILL_PATH);
}

/*****************************************************************
                        interactive options
*****************************************************************/
void togglePaint(void) {

    paintIndex = (conGradSupported) ? ((paintIndex + 1) % 5) : ((paintIndex + 1) % 4);
}

void toggleColorInterpolation(void) {
    
    if (smoothRampSupported) {
        if (linearInterpolation) {
            linearInterpolation = VG_FALSE;
            // upload new parameters to the OpenVG backend
            vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
            vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
            if (conGradSupported) {
                vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
            }
        }
        else {
            linearInterpolation = VG_TRUE;
            // upload new parameters to the OpenVG backend
            vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
            vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
            if (conGradSupported) {
                vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
            }
        }
    }
}

void toggleSpreadMode(void) {
    
    if (spreadMode == VG_COLOR_RAMP_SPREAD_PAD) {
        spreadMode = VG_COLOR_RAMP_SPREAD_REPEAT;
    }
    else
    if (spreadMode == VG_COLOR_RAMP_SPREAD_REPEAT) {
        spreadMode = VG_COLOR_RAMP_SPREAD_REFLECT;
    }
    else {
        spreadMode = VG_COLOR_RAMP_SPREAD_PAD;
    }

    // upload the new spread mode to the OpenVG backend
    vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
    vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
    if (conGradSupported) {
        vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
    }
}

void toggleTilingMode(void) {
    
    if (tilingMode == VG_TILE_PAD) {
        tilingMode = VG_TILE_REPEAT;
    }
    else
    if (tilingMode == VG_TILE_REPEAT) {
        tilingMode = VG_TILE_REFLECT;
    }
    else
    if (tilingMode == VG_TILE_REFLECT) {
        tilingMode = VG_TILE_FILL;
    }
    else {
        tilingMode = VG_TILE_PAD;
    }
    // upload the new tiling mode to the OpenVG backend
    vgSetParameteri(pattern, VG_PAINT_PATTERN_TILING_MODE, tilingMode);
}

void removeGradientRepeat(void) {

    if (conGradSupported) {
        if (conGradRepeats > 1) {
            VGfloat conGradParams[5];
            // decrease repeats
            conGradRepeats--;
            conGradParams[0] = 256.0f; conGradParams[1] = 256.0f;
            conGradParams[2] = 200.0f; conGradParams[3] = 200.0f;
            conGradParams[4] = (VGfloat)conGradRepeats;
            // upload new parameters to the OpenVG backend
            vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
        }
    }
}

void addGradientRepeat(void) {

    if (conGradSupported) {
        if (conGradRepeats < 4.0f) {
            VGfloat conGradParams[5];
            // increase repeats
            conGradRepeats++;
            conGradParams[0] = 256.0f; conGradParams[1] = 256.0f;
            conGradParams[2] = 200.0f; conGradParams[3] = 200.0f;
            conGradParams[4] = (VGfloat)conGradRepeats;
            // upload new parameters to the OpenVG backend
            vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
        }
    }
}

void toggleAnimation(void) {

    // toggle animation
    animate = (animate == VG_TRUE) ? VG_FALSE : VG_TRUE;
}

void toggleScissoring(void) {

    // toggle scissoring
    scissoring = (scissoring == VG_TRUE) ? VG_FALSE : VG_TRUE;
}

void toggleScissorRects(const VGint surfaceWidth,
                        const VGint surfaceHeight) {

    VGint i, *scsRects;
    // first set of scissor rectangles
    VGint scsRectsCfg0[16] = {
        60, 60, 156, 156,
        60, 296, 156, 156,
        296, 60, 156, 156,
        296, 296, 156, 156
    };
    // second set of scissor rectangles
    VGint scsRectsCfg1[16] = {
        22, 22, 156, 332,
        22, 334, 332, 156,
        178, 22, 332, 156,
        334, 178, 156, 332
    };

    // select rectangles configuration
    scissorRectsConf++;
    scsRects = (scissorRectsConf & 1) ? scsRectsCfg0 : scsRectsCfg1;
    for (i = 0; i < 16; ++i) {
        scsRects[i] = (i & 1) ? (VGint)floor(((scsRects[i] / 512.0f) * (VGfloat)surfaceHeight) + 0.5f)
                              : (VGint)floor(((scsRects[i] / 512.0f) * (VGfloat)surfaceWidth) + 0.5f);
    }
    // upload scissor rectangles to the OpenVG backend
    vgSetiv(VG_SCISSOR_RECTS, 16, scsRects);
}

void toggleMasking(void) {

    // toggle masking
    masking = (masking == VG_TRUE) ? VG_FALSE : VG_TRUE;
}

static void deltaZoom(const VGfloat delta) {

    scale += delta * scale;
    if (scale < 0.005f) {
        scale = 0.005f;
    }
    else
    if (scale > 50.000f) {
        scale = 50.000f;
    }
}

/*****************************************************************
                        handle mouse events
*****************************************************************/
void mouseLeftButtonDown(const VGint x,
                         const VGint y) {

    // keep track of current mouse position
    oldMouseX = x;
    oldMouseY = y;
    mouseButton = MOUSE_BUTTON_LEFT;
}

void mouseLeftButtonUp(const VGint x,
                       const VGint y) {

    (void)x;
    (void)y;
    mouseButton = MOUSE_BUTTON_NONE;
}

void mouseRightButtonDown(const VGint x,
                          const VGint y) {

    // keep track of current mouse position
    oldMouseX = x;
    oldMouseY = y;
    mouseButton = MOUSE_BUTTON_RIGHT;
}

void mouseRightButtonUp(const VGint x,
                        const VGint y) {

    (void)x;
    (void)y;
    mouseButton = MOUSE_BUTTON_NONE;
}

void mouseMove(const VGint x,
               const VGint y) {

    VGint dx = x - oldMouseX;
    VGint dy = y - oldMouseY;
    VGint delta = (abs(dx) > abs(dy)) ? dx : dy;

    switch (mouseButton) {

        case MOUSE_BUTTON_LEFT:
            translation[0] += (VGfloat)dx;
            translation[1] += (VGfloat)dy;
            break;

        case MOUSE_BUTTON_RIGHT:
            deltaZoom((VGfloat)delta * 0.003f);
            break;

        default:
            // nothing to do
            break;
    }

    // keep track of current mouse position
    oldMouseX = x;
    oldMouseY = y;
}

/*****************************************************************
                        handle touch events
*****************************************************************/
void touchDoubleTap(const VGint x,
                    const VGint y) {

    (void)x;
    (void)y;
    togglePaint();
}

void touchPinch(const VGfloat deltaScl) {

    deltaZoom(deltaScl);
}

void touchRotate(const VGfloat deltaRot) {

    rotation += deltaRot;
}
