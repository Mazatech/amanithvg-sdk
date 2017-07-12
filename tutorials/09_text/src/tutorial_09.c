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
#include "tutorial_09.h"
#include "vera_font.h"
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define MOUSE_BUTTON_NONE  0
#define MOUSE_BUTTON_LEFT  1
#define MOUSE_BUTTON_RIGHT 2

#define X_COORD 0
#define Y_COORD 1

#define CONTROL_POINT_NONE  -1

// a control point
typedef struct {
    VGfloat x, y;
} ControlPoint;

// path and paint objects
static VGPath textLine = VG_INVALID_HANDLE;
static VGPath textCurve = VG_INVALID_HANDLE;
static VGPath textCurveControlPolygon = VG_INVALID_HANDLE;
static VGPath controlPoint = VG_INVALID_HANDLE;
static VGPaint solidCol = VG_INVALID_HANDLE;

// straight text parameters
static const char* straightText = "AmanithVG engine";
static VGfloat straightTextFontSize = 0.0f;

// wavy text parameters
static const char* wavyText = "OpenVG text example";
static VGfloat wavyTextFontSize = 0.0f;

// control points (2 for straight text, 4 for wavy text)
static ControlPoint controlPoints[6] = { { 0.0f, 0.0f } };
static VGfloat controlPointsRadius = 14.0f;
static VGint pickedControlPoint = CONTROL_POINT_NONE;
static VGboolean mustUpdatePathsAndFont = VG_FALSE;

// mouse state
static VGint oldMouseX = 0;
static VGint oldMouseY = 0;
static VGint mouseButton = MOUSE_BUTTON_NONE;

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

    // create color paint, used to draw text and control points
    solidCol = vgCreatePaint();
    vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
}

static void genPaths(void) {

    // create the straight text underline
    textLine = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    // create the text curve
    textCurve = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    // create the text curve control polygon
    textCurveControlPolygon = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    // create the draggable control point
    controlPoint = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
}

// given the current control points, update all involved OpenVG paths
static void updatePaths(void) {

    static const VGubyte textCurveCmds[] = {
        VG_MOVE_TO,
        VG_CUBIC_TO_ABS
    };

    const VGfloat textCurveCoords[] = {
        controlPoints[2].x, controlPoints[2].y,
        controlPoints[3].x, controlPoints[3].y,
        controlPoints[4].x, controlPoints[4].y,
        controlPoints[5].x, controlPoints[5].y
    };

    // straight text underline
    vgClearPath(textLine, VG_PATH_CAPABILITY_ALL);
    vguLine(textLine, controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y);
    // wavy text path and control polygon
    vgClearPath(textCurve, VG_PATH_CAPABILITY_ALL);
    vgAppendPathData(textCurve, 2, textCurveCmds, textCurveCoords);
    vgClearPath(textCurveControlPolygon, VG_PATH_CAPABILITY_ALL);
    vguPolygon(textCurveControlPolygon, textCurveCoords, 4, VG_FALSE);
}

static void textSizeUpdate(void) {

    VGfloat objSpaceWidth, srfSpaceLen;

    // calculate the text width, in object space
    objSpaceWidth = textLineWidth(&vera_font, straightText);
    // calculate the text width, in surface space
    srfSpaceLen = distance(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y);
    // calculate font size (avoiding division by zero)
    straightTextFontSize = srfSpaceLen / (objSpaceWidth + 0.001f);
    if (straightTextFontSize < 1.0f) {
        straightTextFontSize = 1.0f;
    }
    
    // calculate the text width, in object space
    objSpaceWidth = textLineWidth(&vera_font, wavyText);
    // calculate the text width, in surface space
    srfSpaceLen = vgPathLength(textCurve, 0, vgGetParameteri(textCurve, VG_PATH_NUM_SEGMENTS));
    // calculate font size (avoiding division by zero)
    wavyTextFontSize = srfSpaceLen / (objSpaceWidth + 0.001f);
    if (wavyTextFontSize < 1.0f) {
        wavyTextFontSize = 1.0f;
    }
}

// reset text parameters (control points and font size), according to the specified drawing surface dimensions
static void textParametersReset(const VGint surfaceWidth,
                                const VGint surfaceHeight) {

    // straight text parameters
    controlPoints[0].x = (VGfloat)surfaceWidth * 0.1f;
    controlPoints[0].y = (VGfloat)surfaceHeight * 0.8f;
    controlPoints[1].x = (VGfloat)surfaceWidth * 0.9f;
    controlPoints[1].y = controlPoints[0].y;

    // wavy text parameters
    controlPoints[2].x = (VGfloat)surfaceWidth * 0.1f;
    controlPoints[2].y = (VGfloat)surfaceHeight * 0.2f;
    controlPoints[3].x = (VGfloat)surfaceWidth * 0.35f;
    controlPoints[3].y = (VGfloat)surfaceHeight * 0.6f;
    controlPoints[4].x = (VGfloat)surfaceWidth * 0.65f;
    controlPoints[4].y = controlPoints[2].y;
    controlPoints[5].x = (VGfloat)surfaceWidth * 0.9f;
    controlPoints[5].y = controlPoints[3].y;

    // update OpenVG paths
    updatePaths();
    // calculate font size, according to control points
    textSizeUpdate();
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

    // generate paths
    genPaths();
    // generate paints
    genPaints();
    // generate fonts
    veraFontInit();
    // reset text parameters (control points and font size)
    textParametersReset(surfaceWidth, surfaceHeight);
    // set some default parameters for the OpenVG context
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
}

void tutorialDestroy(void) {

    // release paths
    vgDestroyPath(textCurve);
    vgDestroyPath(textCurveControlPolygon);
    vgDestroyPath(controlPoint);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(solidCol);
    // release fonts
    veraFontDestroy();
    // release font utilities
    fontCommonFinish();
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {

    // reset text parameters (control points and font size)
    textParametersReset(surfaceWidth, surfaceHeight);
    pickedControlPoint = CONTROL_POINT_NONE;
}

static void straightTextDraw(void) {

    VGint i;
    VGfloat glyphOrigin[2] = { 0.0f, 0.0f };
    VGfloat straightTextDir[2] = {
        controlPoints[1].x - controlPoints[0].x,
        controlPoints[1].y - controlPoints[0].y
    };
    // calculate text rotation (radians)
    VGfloat rotation = (VGfloat)atan2(straightTextDir[Y_COORD], straightTextDir[X_COORD]);

    vgSetPaint(solidCol, VG_FILL_PATH);
    vgSetColor(solidCol, 0x2C92FAFF);
    vgSeti(VG_FILL_RULE, VG_NON_ZERO);
    // draw straight text
    vgSetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_GLYPH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(controlPoints[0].x, controlPoints[0].y);
    vgRotate(rotation * 57.2957795f);
    vgScale(straightTextFontSize, straightTextFontSize);
    textLineDraw(&vera_font, straightText, VG_FILL_PATH);

    // draw underline
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgSetPaint(solidCol, VG_STROKE_PATH);
    vgSetColor(solidCol, 0xFFFFFFFF);
    vgLoadIdentity();
    vgDrawPath(textLine, VG_STROKE_PATH);

    // draw control points
    for (i = 0; i < 2; ++i) {
        vgLoadIdentity();
        vgTranslate(controlPoints[i].x, controlPoints[i].y);
        vgDrawPath(controlPoint, VG_STROKE_PATH);
    }
}

static void wavyTextDraw(void) {

    VGint i;

    // draw wavy text
    vgSetPaint(solidCol, VG_FILL_PATH);
    vgSetColor(solidCol, 0xFFBF0DFF);
    textAlongPathDraw(&vera_font, textCurve, wavyText, wavyTextFontSize, VG_FILL_PATH);

    // draw trail path
    vgSetPaint(solidCol, VG_STROKE_PATH);
    vgSetColor(solidCol, 0xFFFFFFFF);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgDrawPath(textCurve, VG_STROKE_PATH);
    vgDrawPath(textCurveControlPolygon, VG_STROKE_PATH);

    // draw control points
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    for (i = 2; i < 6; ++i) {
        vgLoadIdentity();
        vgTranslate(controlPoints[i].x, controlPoints[i].y);
        vgDrawPath(controlPoint, VG_STROKE_PATH);
    }
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    if (mustUpdatePathsAndFont) {
        mustUpdatePathsAndFont = VG_FALSE;
        // update OpenVG paths
        updatePaths();
        // update font size
        textSizeUpdate();
    }

    // clear the whole drawing surface
    vgClear(0, 0, surfaceWidth, surfaceHeight);
    // draw straight text
    straightTextDraw();
    // draw wavy text
    wavyTextDraw();
}

/*****************************************************************
                        handle mouse events
*****************************************************************/
void mouseLeftButtonDown(const VGint x,
                         const VGint y) {

    VGint i, closestPoint = -1;
    VGfloat minDist = 3.402823466e+38f;
    VGfloat mouseX = (VGfloat)x;
    VGfloat mouseY = (VGfloat)y;

    // loop over control points
    for (i = 0; i < 6; ++i) {
        // calculate distance from mouse position
        const VGfloat dist = distance(mouseX, mouseY, controlPoints[i].x, controlPoints[i].y);
        // keep track of minimum distance
        if (dist < minDist) {
            minDist = dist;
            closestPoint = i;
        }
    }
    // check if we have picked a control point
    pickedControlPoint = ((closestPoint >= 0) && (minDist < controlPointsRadius * 1.1f)) ? closestPoint : CONTROL_POINT_NONE;

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
            // assign the new control point position
            controlPoints[pickedControlPoint].x = (VGfloat)x;
            controlPoints[pickedControlPoint].y = (VGfloat)y;
            // we update paths and font within the 'tutorialDraw' function, in order to
            // stick to the rendering thread
            mustUpdatePathsAndFont = VG_TRUE;
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
}

void touchPinch(const VGfloat deltaScl) {

    (void)deltaScl;
}

void touchRotate(const VGfloat deltaRot) {

    (void)deltaRot;
}
