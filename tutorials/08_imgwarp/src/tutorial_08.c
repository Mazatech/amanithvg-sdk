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
#include "tutorial_08.h"
#include "girl_data.c"
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
static VGPath controlPoint = VG_INVALID_HANDLE;
static VGPath imageBounds = VG_INVALID_HANDLE;
static VGPath girlPaths[NUM_PATHS] = { VG_INVALID_HANDLE };
static VGPaint solidCol = VG_INVALID_HANDLE;

// girl image, dimensions and format
static VGImage girlImage = VG_INVALID_HANDLE;
static VGint imageWidth = 0;
static VGint imageHeight = 0;
static VGImageFormat imageFormat = VG_sRGBA_8888_PRE;

// image control points
static ControlPoint imageControlPoints[4] = { { 0.0f, 0.0f } };
static VGfloat controlPointsRadius = 14.0f;
static VGint pickedControlPoint = CONTROL_POINT_NONE;
static VGboolean mustUpdatePaths = VG_FALSE;

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

    // create a color paint, used to draw girl paths and image control points
    solidCol = vgCreatePaint();
    vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
}

static void genPaths(void) {

    VGuint i;

    // create girl paths
    for (i = 0; i < NUM_PATHS; ++i) {
        girlPaths[i] = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vgAppendPathData(girlPaths[i], girlPathsData[i].nCommands, (VGubyte*)girlPathsData[i].cmds, girlPathsData[i].coords);
        // remove all capabilities, in order to free some memory
        vgRemovePathCapabilities(girlPaths[i], VG_PATH_CAPABILITY_ALL);
    }

    // create the draggable gradient point
    controlPoint = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
    // create image bounds
    imageBounds = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
}

static void genImageBounds(void) {

    VGfloat boundsCoords[] = {
        imageControlPoints[0].x, imageControlPoints[0].y,
        imageControlPoints[1].x, imageControlPoints[1].y,
        imageControlPoints[2].x, imageControlPoints[2].y,
        imageControlPoints[3].x, imageControlPoints[3].y
    };

    vgClearPath(imageBounds, VG_PATH_CAPABILITY_ALL);
    vguPolygon(imageBounds, boundsCoords, 4, VG_TRUE);
}

static void genImage(const VGint surfaceWidth,
                     const VGint surfaceHeight) {

    VGuint i;
    VGint drawWidth, drawHeight;
    // opaque white
    VGfloat white[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
    // find a suitable uniform scale
    VGfloat sx = (VGfloat)surfaceWidth / 620.0f;
    VGfloat sy = (VGfloat)surfaceHeight / 754.0f;
    VGfloat s = 0.98f * ((sx < sy) ? sx : sy);

    // calculate image dimensions
    imageWidth = (VGint)(620.0f * s);
    imageHeight = (VGint)(754.0f * s);

    // destroy previous image
    vgDestroyImage(girlImage);
    // create image again, using the same format of drawing surface (in order to speedup grabbing and rendering)
    girlImage = vgCreateImage(imageFormat, imageWidth, imageHeight, VG_IMAGE_QUALITY_BETTER);
    // clear the whole drawing surface
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    vgSetfv(VG_CLEAR_COLOR, 4, white);
    vgClear(0, 0, surfaceWidth, surfaceHeight);
    // generate the image at the lower-left origin (0, 0) of the surface
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgScale(s, s);
    vgTranslate(0.0f, 752.0f);
    vgScale(1.0f, -1.0f);
    vgTranslate(-47.0f, 1.0f);
    vgSetPaint(solidCol, VG_FILL_PATH);
    // draw all the paths
    for (i = 0; i < NUM_PATHS; ++i) {
        vgSetColor(solidCol, girlPathsData[i].color);
        vgDrawPath(girlPaths[i], VG_FILL_PATH);
    }
    vgGetPixels(girlImage, 0, 0, 0, 0, imageWidth, imageHeight);
    drawWidth = (VGint)(imageWidth * 0.75f);
    drawHeight = (VGint)(imageHeight * 0.75f);
    // reset control points as a centered rectangular region (with the same image dimensions)
    imageControlPoints[0].x = (VGfloat)((surfaceWidth - drawWidth) / 2);
    imageControlPoints[0].y = (VGfloat)((surfaceHeight - drawHeight) / 2);
    imageControlPoints[1].x = imageControlPoints[0].x + (VGfloat)drawWidth;
    imageControlPoints[1].y = imageControlPoints[0].y;
    imageControlPoints[2].x = imageControlPoints[1].x;
    imageControlPoints[2].y = imageControlPoints[0].y + (VGfloat)drawHeight;
    imageControlPoints[3].x = imageControlPoints[0].x;
    imageControlPoints[3].y = imageControlPoints[2].y;
    // generate image bounds path
    genImageBounds();
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
    imageFormat = preferredImageFormat;

    // generate paths
    genPaths();
    // generate paints
    genPaints();
    // generate girl image
    genImage(surfaceWidth, surfaceHeight);
    // set some default parameters for the OpenVG context
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
    vgSeti(VG_STROKE_JOIN_STYLE, VG_JOIN_BEVEL);
    vgSeti(VG_STROKE_CAP_STYLE, VG_CAP_BUTT);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSeti(VG_IMAGE_QUALITY, VG_IMAGE_QUALITY_BETTER);
    vgSeti(VG_IMAGE_MODE, VG_DRAW_IMAGE_NORMAL);
}

void tutorialDestroy(void) {

    VGuint i;

    // release paths
    for (i = 0; i < NUM_PATHS; ++i) {
        vgDestroyPath(girlPaths[i]);
    }
    vgDestroyPath(controlPoint);
    vgDestroyPath(imageBounds);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(solidCol);
    // release girl image
    vgDestroyImage(girlImage);
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {

    // re-generate image, taking care of new surface dimensions
    genImage(surfaceWidth, surfaceHeight);
    pickedControlPoint = CONTROL_POINT_NONE;
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    VGint i;
    VGfloat warpMatrix[9];
    VGfloat clearColor[4] = { 1.0f, 1.0f, 1.0f, 1.0f };

    if (mustUpdatePaths) {
        mustUpdatePaths = VG_FALSE;
        genImageBounds();
    }

    // clear the whole drawing surface
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgClear(0, 0, surfaceWidth, surfaceHeight);

    // calculate warp matrix
    vguComputeWarpQuadToQuad(// destination
                             imageControlPoints[0].x, imageControlPoints[0].y,
                             imageControlPoints[1].x, imageControlPoints[1].y,
                             imageControlPoints[2].x, imageControlPoints[2].y,
                             imageControlPoints[3].x, imageControlPoints[3].y,
                             // source
                             0.0f, 0.0f,
                             (VGfloat)imageWidth, 0.0f,
                             (VGfloat)imageWidth, (VGfloat)imageHeight,
                             0.0f, (VGfloat)imageHeight,
                             // the output matrix
                             warpMatrix);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_IMAGE_USER_TO_SURFACE);
    vgLoadIdentity();
    vgLoadMatrix(warpMatrix);
    // draw girl image
    vgDrawImage(girlImage);

    // draw image bounds
    vgSetColor(solidCol, 0x000000FF);
    vgSetPaint(solidCol, VG_STROKE_PATH);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgDrawPath(imageBounds, VG_STROKE_PATH);

    // draw image control points
    for (i = 0; i < 4; ++i) {
        vgLoadIdentity();
        vgTranslate(imageControlPoints[i].x, imageControlPoints[i].y);
        vgDrawPath(controlPoint, VG_STROKE_PATH);
    }
}

/*****************************************************************
                        interactive options
*****************************************************************/

/*****************************************************************
                        handle mouse events
*****************************************************************/
void mouseLeftButtonDown(const VGint x,
                         const VGint y) {

    VGint i, closestPoint = -1;
    VGfloat minDist = 3.402823466e+38f;
    VGfloat mouseX = (VGfloat)x;
    VGfloat mouseY = (VGfloat)y;

    for (i = 0; i < 4; ++i) {
        // take i-thm control point and calculate its distance from mouse position
        VGfloat dist = distance(mouseX, mouseY, imageControlPoints[i].x, imageControlPoints[i].y);
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
            // set the new position for the selected control point
            imageControlPoints[pickedControlPoint].x = (VGfloat)x;
            imageControlPoints[pickedControlPoint].y = (VGfloat)y;
            // we update paths within the 'tutorialDraw' function, in order to
            // stick to the rendering thread
            mustUpdatePaths = VG_TRUE;
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
