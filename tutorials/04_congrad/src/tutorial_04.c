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
#include "tutorial_04.h"
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
static VGPaint conGrad = VG_INVALID_HANDLE;
static VGPaint solidCol = VG_INVALID_HANDLE;

// conical gradient parameters
static VGfloat conGradCenter[2] = { 0.0f };
static VGfloat conGradTarget[2] = { 0.0f };
static VGfloat conGradRepeats = 2.0f;

// current paint states
static VGboolean linearInterpolation = VG_TRUE;
static VGboolean smoothRampSupported = VG_FALSE;
static VGColorRampSpreadMode spreadMode = VG_COLOR_RAMP_SPREAD_PAD;

// mouse state
static VGint oldMouseX = 0;
static VGint oldMouseY = 0;
static VGint mouseButton = MOUSE_BUTTON_NONE;

// keep track of "path user to surface" transformation
static VGfloat userToSurfaceScale = 1.0f;
static VGfloat userToSurfaceTranslation[2] = { 0.0f };
static VGfloat controlPointsRadius = 14.0f;
static VGint pickedControlPoint = CONTROL_POINT_NONE;

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
    // check for the support of VG_MZT_color_ramp_interpolation extension
    smoothRampSupported = extensionFind("VG_MZT_color_ramp_interpolation", extensions);
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

// calculate "path user to surface" transformation
static void userToSurfaceCalc(const VGint surfaceWidth,
                              const VGint surfaceHeight) {

    VGint halfDim = (surfaceWidth < surfaceHeight) ? (surfaceWidth / 2) : (surfaceHeight / 2);

    userToSurfaceScale = (VGfloat)halfDim * 0.9f;
    userToSurfaceTranslation[X_COORD] = (VGfloat)(surfaceWidth / 2);
    userToSurfaceTranslation[Y_COORD] = (VGfloat)(surfaceHeight / 2);
}

// calculate the position of conical gradient control points, in surface space
static void gradientParamsGet(VGfloat srfCenterPoint[],
                              VGfloat srfTargetPoint[]) {

    // start point (apply the "path user to surface" transformation)
    srfCenterPoint[X_COORD] = (conGradCenter[X_COORD] * userToSurfaceScale) + userToSurfaceTranslation[X_COORD];
    srfCenterPoint[Y_COORD] = (conGradCenter[Y_COORD] * userToSurfaceScale) + userToSurfaceTranslation[Y_COORD];
    // target point (apply the "path user to surface" transformation)
    srfTargetPoint[X_COORD] = (conGradTarget[X_COORD] * userToSurfaceScale) + userToSurfaceTranslation[X_COORD];
    srfTargetPoint[Y_COORD] = (conGradTarget[Y_COORD] * userToSurfaceScale) + userToSurfaceTranslation[Y_COORD];
}

// set the position of conical gradient control points, in surface space
static void gradientParamsSet(const VGfloat srfCenterPoint[],
                              const VGfloat srfTargetPoint[]) {

    // conical gradient parameters
    VGfloat conGradParams[5];

    // apply the inverse "path user to surface" transformation
    conGradCenter[X_COORD] = (srfCenterPoint[X_COORD] - userToSurfaceTranslation[X_COORD]) / userToSurfaceScale;
    conGradCenter[Y_COORD] = (srfCenterPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD]) / userToSurfaceScale;
    conGradTarget[X_COORD] = (srfTargetPoint[X_COORD] - userToSurfaceTranslation[X_COORD]) / userToSurfaceScale;
    conGradTarget[Y_COORD] = (srfTargetPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD]) / userToSurfaceScale;

    // upload new gradient parameters to the OpenVG backend
    conGradParams[0] = conGradCenter[X_COORD];
    conGradParams[1] = conGradCenter[Y_COORD];
    conGradParams[2] = conGradTarget[X_COORD];
    conGradParams[3] = conGradTarget[Y_COORD];
    conGradParams[4] = conGradRepeats;
    vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
}

// reset gradient parameters
static void gradientParamsReset(const VGint surfaceWidth,
                                const VGint surfaceHeight) {

    VGfloat gradCenter[2] = { (VGfloat)surfaceWidth * 0.5f, (VGfloat)surfaceHeight * 0.5f };
    VGfloat gradTarget[2] = { (VGfloat)surfaceWidth * 0.75f, (VGfloat)surfaceHeight * 0.5f };
    gradientParamsSet(gradCenter, gradTarget);
}

static void genPaints(void) {

    VGfloat white[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
    // conical gradient color keys
    VGfloat colKeys[25] = {
        0.00f, 0.40f, 0.00f, 0.60f, 1.00f,
        0.25f, 0.90f, 0.50f, 0.10f, 1.00f,
        0.50f, 0.80f, 0.80f, 0.00f, 1.00f,
        0.75f, 0.00f, 0.30f, 0.50f, 1.00f,
        1.00f, 0.40f, 0.00f, 0.60f, 1.00f
    };
    // create a white color paint, used to draw gradient control points
    solidCol = vgCreatePaint();
    vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    vgSetParameterfv(solidCol, VG_PAINT_COLOR, 4, white);
    // create conical gradient
    conGrad = vgCreatePaint();
    vgSetParameteri(conGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_CONICAL_GRADIENT_MZT);
    vgSetParameterfv(conGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
    vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
    if (smoothRampSupported) {
        vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, linearInterpolation ? VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT : VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
    }
}

static void genPaths(void) {

    // create the circle that will be filled by the conical gradient paint
    filledCircle = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(filledCircle, 0.0f, 0.0f, 2.0f, 2.0f);
    // create the draggable gradient point
    controlPoint = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
}

void tutorialInit(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    // an opaque dark grey
    VGfloat clearColor[4] = { 0.2f, 0.2f, 0.2f, 1.0f };

    // make sure to have well visible (and draggable) control points
    controlPointsRadius = ((VGfloat)((surfaceWidth < surfaceHeight) ? surfaceWidth : surfaceHeight) / 512.0f) * 14.0f;
    if (controlPointsRadius < 14.0f) {
        controlPointsRadius = 14.0f;
    }

    // check for OpenVG extensions
    extensionsCheck();
    // calculate "path user to surface" transformation
    userToSurfaceCalc(surfaceWidth, surfaceHeight);
    // generate paths
    genPaths();
    // generate paints
    genPaints();
    // reset gradient parameters
    gradientParamsReset(surfaceWidth, surfaceHeight);
    // set some default parameters for the OpenVG context
    vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
}

void tutorialDestroy(void) {

    // release paths
    vgDestroyPath(filledCircle);
    vgDestroyPath(controlPoint);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(solidCol);
    vgDestroyPaint(conGrad);
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {

    // calculate "path user to surface" transformation
    userToSurfaceCalc(surfaceWidth, surfaceHeight);
    // reset gradient parameters
    gradientParamsReset(surfaceWidth, surfaceHeight);
    pickedControlPoint = CONTROL_POINT_NONE;
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    VGfloat gradCenter[2], gradTarget[2];

    // clear the whole drawing surface
    vgClear(0, 0, surfaceWidth, surfaceHeight);

    // calculate "path user to surface" transformation
    userToSurfaceCalc(surfaceWidth, surfaceHeight);
    // upload "path user to surface" transformation to the OpenVG backend
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(userToSurfaceTranslation[X_COORD], userToSurfaceTranslation[Y_COORD]);
    vgScale(userToSurfaceScale, userToSurfaceScale);
    // draw the filled circle
    vgSetPaint(conGrad, VG_FILL_PATH);
    vgDrawPath(filledCircle, VG_FILL_PATH);

    // calculate the position of conical gradient control points, in surface space
    gradientParamsGet(gradCenter, gradTarget);

    // draw conical gradient control points
    vgSetPaint(solidCol, VG_STROKE_PATH);
    vgLoadIdentity();
    vgTranslate(gradCenter[X_COORD], gradCenter[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);
    vgLoadIdentity();
    vgTranslate(gradTarget[X_COORD], gradTarget[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);
}

/*****************************************************************
                        interactive options
*****************************************************************/
void toggleColorInterpolation(void) {
    
    if (smoothRampSupported) {
        if (linearInterpolation) {
            linearInterpolation = VG_FALSE;
            // upload new parameters to the OpenVG backend
            vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
        }
        else {
            linearInterpolation = VG_TRUE;
            // upload new parameters to the OpenVG backend
            vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
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
    vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
}

/*****************************************************************
                        handle mouse events
*****************************************************************/
void mouseLeftButtonDown(const VGint x,
                         const VGint y) {

    VGfloat gradCenter[2], gradTarget[2];
    VGfloat distCenter, distTarget;
    VGfloat mouseX = (VGfloat)x;
    VGfloat mouseY = (VGfloat)y;

    // get current gradient parameters
    gradientParamsGet(gradCenter, gradTarget);
    // calculate mouse distance from gradient points
    distCenter = distance(mouseX, mouseY, gradCenter[X_COORD], gradCenter[Y_COORD]);
    distTarget = distance(mouseX, mouseY, gradTarget[X_COORD], gradTarget[Y_COORD]);
    // check if we have picked a gradient control point
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
            VGfloat gradCenter[2], gradTarget[2];
            VGfloat* controlPoint = (pickedControlPoint == CONTROL_POINT_CENTER) ? gradCenter : gradTarget;
            // get current gradient parameters
            gradientParamsGet(gradCenter, gradTarget);
            // assign the new control point position
            controlPoint[X_COORD] = (VGfloat)x;
            controlPoint[Y_COORD] = (VGfloat)y;
            // update gradient parameters
            gradientParamsSet(gradCenter, gradTarget);
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

    toggleSpreadMode();
}

void touchPinch(const VGfloat deltaScl) {

    (void)deltaScl;
}

void touchRotate(const VGfloat deltaRot) {

    (void)deltaRot;
}
