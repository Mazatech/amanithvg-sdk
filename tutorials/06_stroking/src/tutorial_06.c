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
#include "tutorial_06.h"
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>

#define MOUSE_BUTTON_NONE  0
#define MOUSE_BUTTON_LEFT  1
#define MOUSE_BUTTON_RIGHT 2

#define X_COORD 0
#define Y_COORD 1

#define CONTROL_POINT_NONE -1

// a control point
typedef struct {
    VGfloat x, y;
} ControlPoint;

// OpenVG path commands used to build the 'strokedFlower' path
static const VGubyte flowerCmds[6] = {
    VG_MOVE_TO_ABS,
    VG_CUBIC_TO_ABS,
    VG_CUBIC_TO_ABS,
    VG_CUBIC_TO_ABS,
    VG_CUBIC_TO_ABS,
    VG_CLOSE_PATH
};

// OpenVG path commands used to build the 'controlPolygon' path
static const VGubyte polygonCmds[14] = {
    VG_MOVE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_LINE_TO_ABS,
    VG_CLOSE_PATH
}

// dash patterns
static const VGfloat dashPatterns[4][4] = {
    { 30.0f, 30.0f,  5.0f, 45.0f },
    { 15.0f, 40.0f, 15.0f, 40.0f },
    { 20.0f, 25.0f, 20.0f, 45.0f },
    {  5.0f, 45.0f, 35.0f, 25.0f }
};

VGPath strokedFlower = VG_INVALID_HANDLE;
VGPath controlPoint = VG_INVALID_HANDLE;
VGPath controlPolygon = VG_INVALID_HANDLE;
VGPaint solidCol = VG_INVALID_HANDLE;
VGfloat controlPointsRadius = 14.0f;

// current stroke configuration
VGint dashPattern = 0;
VGfloat dashPhase = 0.0f;
VGboolean dashAnimate = VG_FALSE;
VGJoinStyle joinStyle = VG_JOIN_MITER;
VGCapStyle startCapStyle = VG_CAP_ROUND;
VGCapStyle endCapStyle = VG_CAP_ROUND;
VGboolean separableCapsSupported = VG_FALSE;

// mouse state
VGint oldMouseX = 0;
VGint oldMouseY = 0;
VGint mouseButton = MOUSE_BUTTON_NONE;

// keep track of "path user to surface" transformation
VGfloat userToSurfaceScale = 1.0f;
VGfloat userToSurfaceTranslation[2] = { 0.0f };
ControlPoint controlPoints[12] = { { 0.0f, 0.0f } };
VGint pickedControlPoint = CONTROL_POINT_NONE;

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
    separableCapsSupported = extensionFind("VG_MZT_separable_cap_style", extensions);
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

    userToSurfaceScale = ((VGfloat)halfDim / 256.0f) * 0.9f;
    userToSurfaceTranslation[X_COORD] = (VGfloat)(surfaceWidth / 2);
    userToSurfaceTranslation[Y_COORD] = (VGfloat)(surfaceHeight / 2);
}

// calculate the position of a control point, in surface space
static void controlPointGet(const VGfloat userSpaceControlPoint[],
                            VGfloat srfControlPoint[]) {

    // apply the "path user to surface" transformation
    srfControlPoint[X_COORD] = (userSpaceControlPoint[X_COORD] * userToSurfaceScale) + userToSurfaceTranslation[X_COORD];
    srfControlPoint[Y_COORD] = (userSpaceControlPoint[Y_COORD] * userToSurfaceScale) + userToSurfaceTranslation[Y_COORD];
}

// set the position of a control point, in surface space
static void controlPointSet(const VGfloat srfControlPoint[],
                            VGfloat userSpaceControlPoint[]) {

    // apply the inverse "path user to surface" transformation
    userSpaceControlPoint[X_COORD] = (srfControlPoint[X_COORD] - userToSurfaceTranslation[X_COORD]) / userToSurfaceScale;
    userSpaceControlPoint[Y_COORD] = (srfControlPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD]) / userToSurfaceScale;
}

// reset control points
static void controlPointsReset(const VGint surfaceWidth,
                               const VGint surfaceHeight) {

    (void)surfaceWidth;
    (void)surfaceHeight;

    controlPoints[ 0].x =  -48.0f; controlPoints[ 0].y =   48.0f;
    controlPoints[ 1].x = -240.0f; controlPoints[ 1].y =  240.0f;
    controlPoints[ 2].x =   48.0f; controlPoints[ 2].y =  240.0f;
    controlPoints[ 3].x =   48.0f; controlPoints[ 3].y =   48.0f;
    controlPoints[ 4].x =  240.0f; controlPoints[ 4].y =  240.0f;
    controlPoints[ 5].x =  240.0f; controlPoints[ 5].y =  -48.0f;
    controlPoints[ 6].x =   48.0f; controlPoints[ 6].y =  -48.0f;
    controlPoints[ 7].x =  240.0f; controlPoints[ 7].y = -240.0f;
    controlPoints[ 8].x =  -48.0f; controlPoints[ 8].y = -240.0f;
    controlPoints[ 9].x =  -48.0f; controlPoints[ 9].y =  -48.0f;
    controlPoints[10].x = -240.0f; controlPoints[10].y = -240.0f;
    controlPoints[11].x = -240.0f; controlPoints[11].y =   48.0f;
}

void setDashPattern(const VGint pattern) {

    if (pattern == 0) {
        vgSetfv(VG_STROKE_DASH_PATTERN, 0, NULL);
    }
    else {
        vgSetfv(VG_STROKE_DASH_PATTERN, 4, dashPatterns[pattern - 1]);
        vgSetf(VG_STROKE_DASH_PHASE, dashPhase);
    }
}

static void genPaints(void) {

    // create a color paint, used to draw stroke, control points, and control polygon
    solidCol = vgCreatePaint();
    vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
}

static void genPaths(void) {

    // create path that will be stroked
    strokedFlower = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    // create the path control polygon
    controlPolygon = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    // create the draggable gradient point
    controlPoint = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
}

// given the current control points, update all involved OpenVG paths
static void updatePaths(void) {

    VGint i;
    VGfloat pathCoords[28];

    // update the stroked flower
    vgClearPath(strokedFlower, VG_PATH_CAPABILITY_ALL);
    for (i = 0; i < 12; ++i) {
        pathCoords[i * 2] = controlPoints[i].x;
        pathCoords[(i * 2) + 1] = controlPoints[i].y;
    }
    pathCoords[i * 2] = controlPoints[0].x;
    pathCoords[(i * 2) + 1] = controlPoints[0].y;
    vgAppendPathData(strokedFlower, 6, flowerCmds, pathCoords);

    // update control polygon
    vgClearPath(controlPolygon, VG_PATH_CAPABILITY_ALL);
    for (i = 0; i < 12; ++i) {
        pathCoords[i * 2] = controlPoints[i].x;
        pathCoords[(i * 2) + 1] = controlPoints[i].y;
    }
    vgAppendPathData(controlPolygon, 14, polygonCmds, pathCoords);
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

    // reset random numbers generator
    srand((unsigned)time(NULL));

    // check for OpenVG extensions
    extensionsCheck();
    // calculate "path user to surface" transformation
    userToSurfaceCalc(surfaceWidth, surfaceHeight);
    // generate paths
    genPaths();
    // generate paints
    genPaints();
    // reset control points
    controlPointsReset(surfaceWidth, surfaceHeight);
    // given the current control points, update all involved OpenVG paths
    updatePaths();
    // set some default parameters for the OpenVG context
    vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
}

void tutorialDestroy(void) {

    // release paths
    vgDestroyPath(strokedFlower);
    vgDestroyPath(controlPoint);
    vgDestroyPath(controlPolygon);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(solidCol);
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {

    // calculate "path user to surface" transformation
    userToSurfaceCalc(surfaceWidth, surfaceHeight);
    // reset control points
    controlPointsReset(surfaceWidth, surfaceHeight);
    // given the current control points, update all involved OpenVG paths
    updatePaths();
    pickedControlPoint = CONTROL_POINT_NONE;
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    VGint i;

    // animate dash phase, if needed
    if (dashAnimate && (dashPattern != 0)) {
        dashPhase += 0.01f;
        if (dashPhase >= 110.0f) {
            dashPhase = 0.0f;
        }
    }

    // clear the whole drawing surface
    vgClear(0, 0, surfaceWidth, surfaceHeight);

    // calculate "path user to surface" transformation
    userToSurfaceCalc(surfaceWidth, surfaceHeight);
    // upload "path user to surface" transformation to the OpenVG backend
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(userToSurfaceTranslation[X_COORD], userToSurfaceTranslation[Y_COORD]);
    vgScale(userToSurfaceScale, userToSurfaceScale);

    // draw stroked flower path
    vgSetf(VG_STROKE_LINE_WIDTH, 12.0f);
    vgSeti(VG_STROKE_JOIN_STYLE, joinStyle);
    if (separableCapsSupported) {
        vgSeti(VG_STROKE_START_CAP_STYLE_MZT, startCapStyle);
        vgSeti(VG_STROKE_END_CAP_STYLE_MZT, endCapStyle);
    }
    else {
        vgSeti(VG_STROKE_CAP_STYLE, startCapStyle);
    }
    setDashPattern(dashPattern);
    vgSetPaint(solidCol, VG_STROKE_PATH);
    vgSetColor(solidCol, 0xFFBF0DFF);
    vgDrawPath(strokedFlower, VG_STROKE_PATH);

    // set an opaque white stroke
    vgSetColor(solidCol, 0xFFFFFFFF);
    // draw control polygon path: because the stroke will be scaled according to the "path user to surface"
    // transformation, we adjust the stroke line width in order to have always a 2 pixels wide outline
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f / userToSurfaceScale);
    vgSeti(VG_STROKE_JOIN_STYLE, VG_JOIN_MITER);
    vgSeti(VG_STROKE_CAP_STYLE, VG_CAP_BUTT);
    setDashPattern(0);
    // draw control polygon path
    vgDrawPath(controlPolygon, VG_STROKE_PATH);

    // draw control points
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
    for (i = 0; i < 12; ++i) {
        VGfloat srfSpacePoint[2];
        VGfloat userSpacePoint[2] = { controlPoints[i].x, controlPoints[i].y };
        // get control point position, in surface space
        controlPointGet(userSpacePoint, srfSpacePoint);
        vgLoadIdentity();
        vgTranslate(srfSpacePoint[X_COORD], srfSpacePoint[Y_COORD]);
        // draw a single control point
        vgDrawPath(controlPoint, VG_STROKE_PATH);
    }
}

/*****************************************************************
                        interactive options
*****************************************************************/
void toggleJoinStyle(void) {

    VGJoinStyle newStyle;

    switch (joinStyle) {
        case VG_JOIN_MITER:
            newStyle = VG_JOIN_ROUND;
            break;
        case VG_JOIN_ROUND:
            newStyle = VG_JOIN_BEVEL;
            break;
        default:
            newStyle = VG_JOIN_MITER;
            break;
    }

    joinStyle = newStyle;
}

void toggleStartCapStyle(void) {

    if (dashPattern != 0) {
        VGCapStyle newStyle;
        // cycle over cap styles
        switch (startCapStyle) {
            case VG_CAP_BUTT:
                newStyle = VG_CAP_ROUND;
                break;
            case VG_CAP_ROUND:
                newStyle = VG_CAP_SQUARE;
                break;
            default:
                newStyle = VG_CAP_BUTT;
                break;
        }
        startCapStyle = newStyle;
    }
}

void toggleEndCapStyle(void) {

    if (dashPattern != 0) {
        if (separableCapsSupported) {
            VGCapStyle newStyle;
            // cycle over cap styles
            switch (endCapStyle) {
                case VG_CAP_BUTT:
                    newStyle = VG_CAP_ROUND;
                    break;
                case VG_CAP_ROUND:
                    newStyle = VG_CAP_SQUARE;
                    break;
                default:
                    newStyle = VG_CAP_BUTT;
                    break;
            }
            endCapStyle = newStyle;
        }
        else {
            toggleStartCapStyle();
        }
    }
}

void toggleDash(void) {

    dashPattern++;
    if (dashPattern > 4) {
        dashPattern = 0;
    }
}

void toggleDashAnimation(void) {

    dashAnimate = (dashAnimate) ? VG_FALSE : VG_TRUE;
}

static void randomStroke(void) {

    dashPattern = rand() % 5;
    joinStyle = (VGJoinStyle)((rand() % 3) + VG_JOIN_MITER);
    startCapStyle = (VGCapStyle)((rand() % 3) + VG_CAP_BUTT);
    endCapStyle = (VGCapStyle)((rand() % 3) + VG_CAP_BUTT);
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

    for (i = 0; i < 12; ++i) {
        VGfloat srfSpacePoint[2], dist;
        VGfloat userSpacePoint[2] = { controlPoints[i].x, controlPoints[i].y };
        // get i-thm control point, in surface space
        controlPointGet(userSpacePoint, srfSpacePoint);
        // get its distance from mouse position
        dist = distance(mouseX, mouseY, srfSpacePoint[X_COORD], srfSpacePoint[Y_COORD]);
        if (dist < minDist) {
            minDist = dist;
            closestPoint = i;
        }
    }
    // check if we have picked a control point
    pickedControlPoint = ((closestPoint >= 0) && (minDist < controlPointsRadius)) ? closestPoint : CONTROL_POINT_NONE;

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
            VGfloat userSpacePoint[2];
            VGfloat srfSpacePoint[2] = { (VGfloat)x, (VGfloat)y };
            // set the new position for the selected control point
            controlPointSet(srfSpacePoint, userSpacePoint);
            controlPoints[pickedControlPoint].x = userSpacePoint[X_COORD];
            controlPoints[pickedControlPoint].y = userSpacePoint[Y_COORD];
            updatePaths();
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

    randomStroke();
}

void touchPinch(const VGfloat deltaScl) {

    (void)deltaScl;
}

// delta rotation is expressed in radians
void touchRotate(const VGfloat deltaRot) {

    if ((!dashAnimate) && (dashPattern != 0)) {
        dashPhase += deltaRot * 35.0f;
    }
}
