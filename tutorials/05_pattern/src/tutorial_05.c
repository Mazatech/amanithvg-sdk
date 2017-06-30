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
#include "tutorial_05.h"
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define MOUSE_BUTTON_NONE  0
#define MOUSE_BUTTON_LEFT  1
#define MOUSE_BUTTON_RIGHT 2

#define X_COORD 0
#define Y_COORD 1

#define CONTROL_POINT_NONE   0
#define CONTROL_POINT_CENTER 1
#define CONTROL_POINT_TARGET 2

// path and paint objects
static VGPath filledCircle = VG_INVALID_HANDLE;
static VGPath controlPoint = VG_INVALID_HANDLE;
static VGPath controlBounds = VG_INVALID_HANDLE;
static VGPaint solidCol = VG_INVALID_HANDLE;
static VGPaint pattern = VG_INVALID_HANDLE;

// pottern image
static VGImage patternImage = VG_INVALID_HANDLE;
static VGuint patternImageSize = 0;
static VGImageFormat patternImageFormat = VG_sRGBA_8888_PRE;

// pattern parameters
static VGTilingMode tilingMode = VG_TILE_FILL;
static VGfloat patternCenter[2] = { 0.0f };
static VGfloat patternTarget[2] = { 0.0f };

// mouse state
static VGint oldMouseX = 0;
static VGint oldMouseY = 0;
static VGint mouseButton = MOUSE_BUTTON_NONE;

// keep track of "path user to surface" translation
static VGfloat userToSurfaceTranslation[2] = { 0.0f };
static VGfloat controlPointsRadius = 14.0f;
static VGint pickedControlPoint = CONTROL_POINT_NONE;

// calculate the distance between two points
static VGfloat distance(const VGfloat x0,
                        const VGfloat y0,
                        const VGfloat x1,
                        const VGfloat y1) {

    VGfloat dx = x0 - x1;
    VGfloat dy = y0 - y1;
    return (VGfloat)hypot(dx, dy);
}

// calculate and upload paint / path transformations to the OpenVG backend
static void setMatrices(const VGint surfaceWidth,
                        const VGint surfaceHeight) {

    // calculate pattern direction
    VGfloat dir[2] = {
        patternTarget[X_COORD] - patternCenter[X_COORD],
        patternTarget[Y_COORD] - patternCenter[Y_COORD]
    };
    // calculate pattern scale
    VGfloat l = (VGfloat)hypot(dir[X_COORD], dir[Y_COORD]);
    VGfloat paintToUserScale = l / (VGfloat)(patternImageSize);
    // calculate pattern rotation
    VGfloat rotRadians = (VGfloat)atan2(dir[Y_COORD], dir[X_COORD]);
    VGfloat rotDegrees = rotRadians * 57.2957795f;
    // calculate "user to surface" transformation
    VGfloat userToSurfaceScale = (VGfloat)((surfaceWidth < surfaceHeight) ? (surfaceWidth / 2) : (surfaceHeight / 2)) * 0.9f;
    userToSurfaceTranslation[X_COORD] = (VGfloat)(surfaceWidth / 2);
    userToSurfaceTranslation[Y_COORD] = (VGfloat)(surfaceHeight / 2);

    // "paint to user" transformation, upload matrix to the OpenVG backend
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
    vgLoadIdentity();
    vgTranslate(patternCenter[X_COORD] / userToSurfaceScale, patternCenter[Y_COORD] / userToSurfaceScale);
    vgScale(paintToUserScale / userToSurfaceScale, paintToUserScale / userToSurfaceScale);
    vgRotate(rotDegrees);

    // "user to surface" transformation, upload matrix to the OpenVG backend
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(userToSurfaceTranslation[X_COORD], userToSurfaceTranslation[Y_COORD]);
    vgScale(userToSurfaceScale, userToSurfaceScale);
}

// calculate the position of pattern control points, in surface space
static void patternParamsGet(VGfloat srfCenterPoint[],
                             VGfloat srfTargetPoint[]) {

    srfCenterPoint[X_COORD] = patternCenter[X_COORD] + userToSurfaceTranslation[X_COORD];
    srfCenterPoint[Y_COORD] = patternCenter[Y_COORD] + userToSurfaceTranslation[Y_COORD];
    srfTargetPoint[X_COORD] = patternTarget[X_COORD] + userToSurfaceTranslation[X_COORD];
    srfTargetPoint[Y_COORD] = patternTarget[Y_COORD] + userToSurfaceTranslation[Y_COORD];
}

// set the position of pattern control points, in surface space
static void patternParamsSet(const VGfloat srfCenterPoint[],
                             const VGfloat srfTargetPoint[]) {

    patternCenter[X_COORD] = srfCenterPoint[X_COORD] - userToSurfaceTranslation[X_COORD];
    patternCenter[Y_COORD] = srfCenterPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD];
    patternTarget[X_COORD] = srfTargetPoint[X_COORD] - userToSurfaceTranslation[X_COORD];
    patternTarget[Y_COORD] = srfTargetPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD];
}

// reset pattern control points
static void patternParamsReset(const VGint surfaceWidth,
                               const VGint surfaceHeight) {

    (void)surfaceWidth;
    (void)surfaceHeight;

    patternCenter[X_COORD] = -(VGfloat)(patternImageSize / 2);
    patternCenter[Y_COORD] = -(VGfloat)(patternImageSize / 2);
    patternTarget[X_COORD] = patternCenter[X_COORD] + (VGfloat)(patternImageSize);
    patternTarget[Y_COORD] = patternCenter[Y_COORD];
}

static void genPaints(void) {

    VGuint *pixels, i, j, blocks;
    VGfloat white[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
    static const VGuint patternColors[16] = {
        0xFF6030FF, 0xFFB060FF, 0xFF9090FF, 0xFF30B0FF,
        0x60FF30FF, 0xB0FF60FF, 0x90FF90FF, 0x30FFB0FF,
        0x6030FFFF, 0xB060FFFF, 0x9090FFFF, 0x30B0FFFF,
        0x303030FF, 0x606060FF, 0x909090FF, 0xB0B0B0FF
    };

    // create a white color paint, used to draw control points
    solidCol = vgCreatePaint();
    vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    vgSetParameterfv(solidCol, VG_PAINT_COLOR, 4, white);
    // create pattern image
    patternImage = vgCreateImage(patternImageFormat, patternImageSize, patternImageSize, VG_IMAGE_QUALITY_BETTER);
    pixels = (VGuint *)malloc(patternImageSize * patternImageSize * sizeof(VGuint));
    blocks = patternImageSize / 4;
    for (i = 0; i < patternImageSize; ++i) {
        VGuint y = i / blocks;
        for (j = 0; j < patternImageSize; ++j) {
            VGuint x = j / blocks;
            pixels[i * patternImageSize + j] = patternColors[y * 4 + x];
        }
    }
    vgImageSubData(patternImage, (const void *)pixels, patternImageSize * sizeof(VGuint), VG_sRGBA_8888_PRE, 0, 0, patternImageSize, patternImageSize);
    // create pattern
    pattern = vgCreatePaint();
    vgSetParameteri(pattern, VG_PAINT_TYPE, VG_PAINT_TYPE_PATTERN);
    vgSetParameteri(pattern, VG_PAINT_PATTERN_TILING_MODE, tilingMode);
    vgPaintPattern(pattern, patternImage);
    free(pixels);
}

static void genPaths(void) {

    // create the circle that will be filled by the pattern paint
    filledCircle = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(filledCircle, 0.0f, 0.0f, 2.0f, 2.0f);
    // create the draggable gradient point
    controlPoint = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
    // create pattern bounds
    controlBounds = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
}

static void genPatternBounds(const VGfloat srfCenterPoint[],
                             const VGfloat srfTargetPoint[]) {

    static const VGubyte boundsCmd[5] = {
        VG_MOVE_TO_ABS,
        VG_LINE_TO_ABS,
        VG_LINE_TO_ABS,
        VG_LINE_TO_ABS,
        VG_CLOSE_PATH
    };
    VGfloat dx = srfTargetPoint[X_COORD] - srfCenterPoint[X_COORD];
    VGfloat dy = srfTargetPoint[Y_COORD] - srfCenterPoint[Y_COORD];
    VGfloat boundsCoords[] = {
        srfCenterPoint[X_COORD], srfCenterPoint[Y_COORD],
        srfTargetPoint[X_COORD], srfTargetPoint[Y_COORD],
        srfTargetPoint[X_COORD] - dy, srfTargetPoint[Y_COORD] + dx,
        srfCenterPoint[X_COORD] - dy, srfCenterPoint[Y_COORD] + dx
    };

    vgClearPath(controlBounds, VG_PATH_CAPABILITY_ALL);
    vgAppendPathData(controlBounds, 5, boundsCmd, boundsCoords);
}

void tutorialInit(const VGint surfaceWidth,
                  const VGint surfaceHeight
                  const VGImageFormat preferredImageFormat) {

    // an opaque dark grey
    VGfloat clearColor[4] = { 0.2f, 0.2f, 0.2f, 1.0f };
    VGfloat tileColor[4] = { 0.1f, 0.6f, 0.3f, 1.0f };
    VGint minDim = (surfaceWidth < surfaceHeight) ? surfaceWidth : surfaceHeight;

    // make sure to have well visible (and draggable) control points
    controlPointsRadius = ((VGfloat)minDim / 512.0f) * 14.0f;
    if (controlPointsRadius < 14.0f) {
        controlPointsRadius = 14.0f;
    }
    patternImageSize = (minDim >= 1024) ? 128 : 64;
    patternImageFormat = preferredImageFormat;

    // reset pattern parameters
    patternParamsReset(surfaceWidth, surfaceHeight);
    // calculate and upload paint / path transformations to the OpenVG backend
    setMatrices(surfaceWidth, surfaceHeight);
    // generate paths
    genPaths();
    // generate paints
    genPaints();
    // set some default parameters for the OpenVG context
    vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgSetfv(VG_TILE_FILL_COLOR, 4, tileColor);
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
    vgSeti(VG_IMAGE_QUALITY, VG_IMAGE_QUALITY_BETTER);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
}

void tutorialDestroy(void) {

    // release paths
    vgDestroyPath(filledCircle);
    vgDestroyPath(controlPoint);
    vgDestroyPath(controlBounds);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(solidCol);
    vgPaintPattern(pattern, VG_INVALID_HANDLE);
    vgDestroyImage(patternImage);
    vgDestroyPaint(pattern);
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {

    // calculate and upload paint / path transformations to the OpenVG backend
    setMatrices(surfaceWidth, surfaceHeight);
    // reset pattern parameters
    patternParamsReset(surfaceWidth, surfaceHeight);
    pickedControlPoint = CONTROL_POINT_NONE;
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    VGfloat center[2], target[2];

    // clear the whole drawing surface
    vgClear(0, 0, surfaceWidth, surfaceHeight);

    // calculate and upload paint / path transformations to the OpenVG backend
    setMatrices(surfaceWidth, surfaceHeight);

    // draw the filled circle
    vgSetPaint(pattern, VG_FILL_PATH);
    vgDrawPath(filledCircle, VG_FILL_PATH);

    // calculate the position of pattern control points, in surface space
    patternParamsGet(center, target);

    // draw pattern bounds
    genPatternBounds(center, target);
    vgLoadIdentity();
    vgDrawPath(controlBounds, VG_STROKE_PATH);

    // draw pattern control points
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgSetPaint(solidCol, VG_STROKE_PATH);
    vgLoadIdentity();
    vgTranslate(center[X_COORD], center[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);
    vgLoadIdentity();
    vgTranslate(target[X_COORD], target[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);
}

/*****************************************************************
                        interactive options
*****************************************************************/
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

/*****************************************************************
                        handle mouse events
*****************************************************************/
void mouseLeftButtonDown(const VGint x,
                         const VGint y) {

    VGfloat center[2], target[2];
    VGfloat distCenter, distTarget;
    VGfloat mouseX = (VGfloat)x;
    VGfloat mouseY = (VGfloat)y;

    // get current pattern parameters
    patternParamsGet(center, target);
    // calculate mouse distance from control points
    distCenter = distance(mouseX, mouseY, center[X_COORD], center[Y_COORD]);
    distTarget = distance(mouseX, mouseY, target[X_COORD], target[Y_COORD]);
    // check if we have picked a control point
    if (distCenter < distTarget) {
        pickedControlPoint = (distCenter < controlPointsRadius) ? CONTROL_POINT_CENTER : CONTROL_POINT_NONE;
    }
    else {
        pickedControlPoint = (distTarget < controlPointsRadius) ? CONTROL_POINT_TARGET : CONTROL_POINT_NONE;
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
            VGfloat center[2], target[2];
            VGfloat mouseX = (VGfloat)x;
            VGfloat mouseY = (VGfloat)y;
            VGfloat dx = mouseX - oldMouseX;
            VGfloat dy = mouseY - oldMouseY;
            // get current pattern parameters
            patternParamsGet(center, target);
            // update selected control point
            if (pickedControlPoint == CONTROL_POINT_CENTER) {
                center[X_COORD] += dx;
                center[Y_COORD] += dy;
                target[X_COORD] += dx;
                target[Y_COORD] += dy;
            }
            else {
                target[X_COORD] = mouseX;
                target[Y_COORD] = mouseY;
            }
            // update pattern parameters
            patternParamsSet(center, target);
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

    toggleTilingMode();
}

void touchPinch(const VGfloat deltaScl) {

    (void)deltaScl;
}

void touchRotate(const VGfloat deltaRot) {

    (void)deltaRot;
}
