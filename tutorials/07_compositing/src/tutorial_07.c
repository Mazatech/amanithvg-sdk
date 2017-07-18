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
#include "tutorial_07.h"
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define MOUSE_BUTTON_NONE  0
#define MOUSE_BUTTON_LEFT  1
#define MOUSE_BUTTON_RIGHT 2

#define X_COORD 0
#define Y_COORD 1

#define CONTROL_POINT_NONE      0
#define CONTROL_POINT_SRC_IMAGE 1
#define CONTROL_POINT_DST_IMAGE 2

// path and paint objects
static VGPath flower = VG_INVALID_HANDLE;
static VGPath controlPoint = VG_INVALID_HANDLE;
static VGPath imageBounds = VG_INVALID_HANDLE;
static VGPaint solidCol = VG_INVALID_HANDLE;
static VGPaint paintSrc = VG_INVALID_HANDLE;
static VGPaint paintDst = VG_INVALID_HANDLE;

// SRC and DST images
static VGImage srcImage = VG_INVALID_HANDLE;
static VGImage dstImage = VG_INVALID_HANDLE;
static VGImageFormat imagesFormat = VG_sRGBA_8888_PRE;
static VGint imagesSize = 0;
static VGfloat srcImagePos[2] = { 0.0f };
static VGfloat dstImagePos[2] = { 0.0f };

// current paint states
static VGboolean extBlendModesSupported = VG_FALSE;
static VGBlendMode blendMode = VG_BLEND_SRC_OVER;

// control points
static VGfloat controlPointsRadius = 14.0f;
static VGint pickedControlPoint = CONTROL_POINT_NONE;

// mouse state
static VGint oldMouseX = 0;
static VGint oldMouseY = 0;
static VGint mouseButton = MOUSE_BUTTON_NONE;

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
    // check for the support of VG_MZT_advanced_blend_modes extension
    extBlendModesSupported = extensionFind("VG_MZT_advanced_blend_modes", extensions);
}

// calculate the distance between two points
static VGfloat distance(const VGfloat x0,
                        const VGfloat y0,
                        const VGfloat x1,
                        const VGfloat y1) {

    VGfloat dx = x0 - x1;
    VGfloat dy = y0 - y1;
    return (VGfloat)hypot(dx, dy);
}

static void genPaints(void) {

    VGfloat white[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
    // color stops used to generate SRC image
    const VGfloat srcStops[] = {
        0.00f, 0.60f, 0.05f, 0.10f, 1.00f,
        1.00f, 0.30f, 0.90f, 0.10f, 0.30f
    };
    const VGfloat srcGrad[] = {
        -160.0f, 0.0f,
         160.0f, 0.0f
    };
    // color stops used to generate DST image
    const VGfloat dstStops[] = {
        0.00f, 0.90f, 0.80f, 0.00f, 0.90f,
        1.00f, 0.00f, 0.20f, 0.80f, 0.40f
    };
    const VGfloat dstGrad[] = {
        -160.0f, 0.0f,
         160.0f, 0.0f
    };

    // create a white color paint, used to draw control points and images bounds
    solidCol = vgCreatePaint();
    vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    vgSetParameterfv(solidCol, VG_PAINT_COLOR, 4, white);

    // create paint that will be used to generate SRC image
    paintSrc = vgCreatePaint();
    vgSetParameteri(paintSrc, VG_PAINT_TYPE, VG_PAINT_TYPE_LINEAR_GRADIENT);
    vgSetParameterfv(paintSrc, VG_PAINT_COLOR_RAMP_STOPS, 10, srcStops);
    vgSetParameterfv(paintSrc, VG_PAINT_LINEAR_GRADIENT, 4, srcGrad);
    vgSetParameteri(paintSrc, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);

    // create paint that will be used to generate DST image
    paintDst = vgCreatePaint();
    vgSetParameteri(paintDst, VG_PAINT_TYPE, VG_PAINT_TYPE_LINEAR_GRADIENT);
    vgSetParameterfv(paintDst, VG_PAINT_COLOR_RAMP_STOPS, 10, dstStops);
    vgSetParameterfv(paintDst, VG_PAINT_LINEAR_GRADIENT, 4, dstGrad);
    vgSetParameteri(paintDst, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);
}

static void genPaths(void) {

    // flower-like path commands
    VGubyte flowerCmds[] = {
        VG_MOVE_TO,
        VG_CUBIC_TO,
        VG_CUBIC_TO,
        VG_CUBIC_TO,
        VG_CUBIC_TO,
        VG_CLOSE_PATH
    };
    // flower-like path coordinates
    VGfloat flowerCoords[] = {
        // move to
        -20.0f, 20.0f,
        // cubic to
        -200.0f, 170.0f, -200.0f, -170.0f, -20.0f, -20.0f,
        // cubic to
        -170.0f, -200.0f, 170.0f, -200.0f, 20.0f, -20.0f,
        // cubic to
        200.0f, -170.0f, 200.0f, 170.0f, 20.0f, 20.0f,
        // cubic to
        170.0f, 200.0f, -170.0f, 200.0f, -20.0f, 20.0f
    };

    // create the flower-like path: it will be used to generate images
    flower = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vgAppendPathData(flower, 6, flowerCmds, flowerCoords);
    // create the draggable control point
    controlPoint = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
    // create image bounds
    imageBounds = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
}

static void genImages(const VGint surfaceWidth,
                      const VGint surfaceHeight) {


    VGfloat scl, imgCenter;
    VGfloat black[4] = { 0.0f, 0.0f, 0.0f, 0.0f };
    VGint minDim = (surfaceWidth < surfaceHeight) ? surfaceWidth : surfaceHeight;
    
    // 3/4 of the minimum surface dimension
    imagesSize = (minDim * 3) / 4;
    imgCenter =  (VGfloat)(imagesSize / 2);
    scl = ((VGfloat)imagesSize / 384.0f) * 1.1f;

    // destroy previous images
    vgDestroyImage(srcImage);
    vgDestroyImage(dstImage);
    // create images, using the same format of drawing surface (in order to speedup grabbing and rendering)
    srcImage = vgCreateImage(imagesFormat, imagesSize, imagesSize, VG_IMAGE_QUALITY_NONANTIALIASED);
    dstImage = vgCreateImage(imagesFormat, imagesSize, imagesSize, VG_IMAGE_QUALITY_NONANTIALIASED);

    // clear surface with a transparent black
    vgSetfv(VG_CLEAR_COLOR, 4, black);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    // generate SRC image
    vgClear(0, 0, surfaceWidth, surfaceHeight);
    vgSetPaint(paintSrc, VG_FILL_PATH);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(imgCenter, imgCenter);
    vgRotate(180.0f);
    vgScale(scl, scl);
    vgDrawPath(flower, VG_FILL_PATH);
    vgGetPixels(srcImage, 0, 0, 0, 0, imagesSize, imagesSize);
    // generate DST image
    vgClear(0, 0, surfaceWidth, surfaceHeight);
    vgSetPaint(paintDst, VG_FILL_PATH);
    vgLoadIdentity();
    vgTranslate(imgCenter, imgCenter);
    vgRotate(45.0f);
    vgScale(scl, scl);
    vgDrawPath(flower, VG_FILL_PATH);
    vgGetPixels(dstImage, 0, 0, 0, 0, imagesSize, imagesSize);
    // reset images position
    srcImagePos[X_COORD] = imgCenter + (((VGfloat)surfaceWidth - imagesSize) / 4.0f);
    srcImagePos[Y_COORD] = imgCenter + (((VGfloat)surfaceHeight - imagesSize) / 4.0f);
    dstImagePos[X_COORD] = ((VGfloat)surfaceWidth - imgCenter) - (((VGfloat)surfaceWidth - imagesSize) / 4.0f);
    dstImagePos[Y_COORD] = ((VGfloat)surfaceHeight - imgCenter) - (((VGfloat)surfaceHeight - imagesSize) / 4.0f);
    // update path used to draw images bounds
    vgClearPath(imageBounds, VG_PATH_CAPABILITY_ALL);
    vguRect(imageBounds, 0.0f, 0.0f, (VGfloat)imagesSize, (VGfloat)imagesSize);
}

void tutorialInit(const VGint surfaceWidth,
                  const VGint surfaceHeight,
                  const VGImageFormat preferredImageFormat) {

    // make sure to have well visible (and draggable) control points
    controlPointsRadius = ((VGfloat)((surfaceWidth < surfaceHeight) ? surfaceWidth : surfaceHeight) / 512.0f) * 14.0f;
    if (controlPointsRadius < 14.0f) {
        controlPointsRadius = 14.0f;
    }
    // keep track of preferred image format
    imagesFormat = preferredImageFormat;

    // check for OpenVG extensions
    extensionsCheck();
    // generate paths
    genPaths();
    // generate paints
    genPaints();
    // generate SRC and DST images
    genImages(surfaceWidth, surfaceHeight);

    // set some default parameters for the OpenVG context
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSeti(VG_IMAGE_QUALITY, VG_IMAGE_QUALITY_NONANTIALIASED);
    vgSeti(VG_IMAGE_MODE, VG_DRAW_IMAGE_NORMAL);
}

void tutorialDestroy(void) {

    // release paths
    vgDestroyPath(flower);
    vgDestroyPath(controlPoint);
    vgDestroyPath(imageBounds);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(solidCol);
    vgDestroyPaint(paintSrc);
    vgDestroyPaint(paintDst);
    // release images
    vgDestroyImage(srcImage);
    vgDestroyImage(dstImage);
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {

    // re-generate images, taking care of new surface dimensions
    genImages(surfaceWidth, surfaceHeight);
    pickedControlPoint = CONTROL_POINT_NONE;
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    VGfloat dashPattern[2] = { 10.0f, 10.0f };
    VGfloat imgCenter = (VGfloat)(imagesSize / 2);
    // an opaque black
    VGfloat clearColor[4] = { 0.0f, 0.0f, 0.0f, 1.0f };

    // clear the whole drawing surface
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgClear(0, 0, surfaceWidth, surfaceHeight);

    // draw DST image
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_IMAGE_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(dstImagePos[X_COORD] - imgCenter, dstImagePos[Y_COORD] - imgCenter);
    vgDrawImage(dstImage);
    // draw SRC image, setting the current blend mode
    vgSeti(VG_BLEND_MODE, blendMode);
    vgLoadIdentity();
    vgTranslate(srcImagePos[X_COORD] - imgCenter, srcImagePos[Y_COORD] - imgCenter);
    vgDrawImage(srcImage);

    // draw control points
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
    vgSetfv(VG_STROKE_DASH_PATTERN, 0, NULL);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    vgSetPaint(solidCol, VG_STROKE_PATH);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(dstImagePos[X_COORD], dstImagePos[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);
    vgLoadIdentity();
    vgTranslate(srcImagePos[X_COORD], srcImagePos[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);

    // draw images bounds
    vgSetf(VG_STROKE_LINE_WIDTH, 1.0f);
    vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPattern);
    vgLoadIdentity();
    vgTranslate(dstImagePos[X_COORD] - imgCenter, dstImagePos[Y_COORD] - imgCenter);
    vgDrawPath(imageBounds, VG_STROKE_PATH);
    vgLoadIdentity();
    vgTranslate(srcImagePos[X_COORD] - imgCenter, srcImagePos[Y_COORD] - imgCenter);
    vgDrawPath(imageBounds, VG_STROKE_PATH);
}

// get current blend mode
VGint getBlendMode(void) {

    return (VGint)blendMode;
}

/*****************************************************************
                        interactive options
*****************************************************************/
void toggleBlendMode(void) {

    VGint newBlendMode;

    switch ((VGint)blendMode) {
        case VG_BLEND_SRC:
            newBlendMode = VG_BLEND_SRC_OVER;
            break;
        case VG_BLEND_SRC_OVER:
            newBlendMode = VG_BLEND_DST_OVER;
            break;
        case VG_BLEND_DST_OVER:
            newBlendMode = VG_BLEND_SRC_IN;
            break;
        case VG_BLEND_SRC_IN:
            newBlendMode = VG_BLEND_DST_IN;
            break;
        case VG_BLEND_DST_IN:
            newBlendMode = VG_BLEND_MULTIPLY;
            break;
        case VG_BLEND_MULTIPLY:
            newBlendMode = VG_BLEND_SCREEN;
            break;
        case VG_BLEND_SCREEN:
            newBlendMode = VG_BLEND_DARKEN;
            break;
        case VG_BLEND_DARKEN:
            newBlendMode = VG_BLEND_LIGHTEN;
            break;
        case VG_BLEND_LIGHTEN:
            newBlendMode = VG_BLEND_ADDITIVE;
            break;
        case VG_BLEND_ADDITIVE:
            newBlendMode = extBlendModesSupported ? VG_BLEND_CLEAR_MZT : VG_BLEND_SRC;
            break;
        // VG_MZT_advanced_blend_modes: extended blend modes supported by both AmanithVG SRE and AmanithVG GLE
        case VG_BLEND_CLEAR_MZT:
            newBlendMode = VG_BLEND_DST_MZT;
            break;
        case VG_BLEND_DST_MZT:
            newBlendMode = VG_BLEND_SRC_OUT_MZT;
            break;
        case VG_BLEND_SRC_OUT_MZT:
            newBlendMode = VG_BLEND_DST_OUT_MZT;
            break;
        case VG_BLEND_DST_OUT_MZT:
            newBlendMode = VG_BLEND_SRC_ATOP_MZT;
            break;
        case VG_BLEND_SRC_ATOP_MZT:
            newBlendMode = VG_BLEND_DST_ATOP_MZT;
            break;
        case VG_BLEND_DST_ATOP_MZT:
            newBlendMode = VG_BLEND_XOR_MZT;
            break;
        case VG_BLEND_XOR_MZT:
            newBlendMode = VG_BLEND_EXCLUSION_MZT;
            break;
        case VG_BLEND_EXCLUSION_MZT:
            newBlendMode = VG_BLEND_SRC;
            break;
        default:
            newBlendMode = blendMode;
            break;
    }

    blendMode = newBlendMode;
}

/*****************************************************************
                        handle mouse events
*****************************************************************/
void mouseLeftButtonDown(const VGint x,
                         const VGint y) {

    VGfloat distSrc, distDst;
    VGfloat mouseX = (VGfloat)x;
    VGfloat mouseY = (VGfloat)y;

    // calculate mouse distance from control points
    distSrc = distance(mouseX, mouseY, srcImagePos[X_COORD], srcImagePos[Y_COORD]);
    distDst = distance(mouseX, mouseY, dstImagePos[X_COORD], dstImagePos[Y_COORD]);
    // check if we have picked a control point
    if (distSrc < distDst) {
        pickedControlPoint = (distSrc < controlPointsRadius * 1.1f) ? CONTROL_POINT_SRC_IMAGE : CONTROL_POINT_NONE;
    }
    else {
        pickedControlPoint = (distDst < controlPointsRadius * 1.1f) ? CONTROL_POINT_DST_IMAGE : CONTROL_POINT_NONE;
    }
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
    pickedControlPoint = CONTROL_POINT_NONE;
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

    if (mouseButton == MOUSE_BUTTON_LEFT) {
        if (pickedControlPoint != CONTROL_POINT_NONE) {
            VGfloat* controlPoint = (pickedControlPoint == CONTROL_POINT_SRC_IMAGE) ? srcImagePos : dstImagePos;
            // assign the new control point position
            controlPoint[X_COORD] = (VGfloat)x;
            controlPoint[Y_COORD] = (VGfloat)y;
        }
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

    toggleBlendMode();
}

void touchPinch(const VGfloat deltaScl) {

    (void)deltaScl;
}

void touchRotate(const VGfloat deltaRot) {

    (void)deltaRot;
}
