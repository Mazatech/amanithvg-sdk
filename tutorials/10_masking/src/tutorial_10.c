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
#include "tutorial_10.h"
#include "ship.c"
#include "mask_paths.c"
#include "mask_images.c"
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define MOUSE_BUTTON_NONE  0
#define MOUSE_BUTTON_LEFT  1
#define MOUSE_BUTTON_RIGHT 2

#define X_COORD 0
#define Y_COORD 1

#define CONTROL_POINT_NONE  0
#define CONTROL_POINT_MASK0 1
#define CONTROL_POINT_MASK1 2

typedef enum {
    MaskPath0 = 0,
    MaskPath1 = 1,
    MaskImage0 = 2,
    MaskImage1 = 3
} AlphaPrimitiveType;

// path and paint objects
static VGPath ship = VG_INVALID_HANDLE;
static VGPath shipBackground = VG_INVALID_HANDLE;
static VGPath spotMask0 = VG_INVALID_HANDLE;
static VGPath spotMask1 = VG_INVALID_HANDLE;
static VGPath controlPoint = VG_INVALID_HANDLE;
static VGPath imageBounds = VG_INVALID_HANDLE;
static VGPaint solidCol = VG_INVALID_HANDLE;
// mask images
static VGImage starMaskImage = VG_INVALID_HANDLE;
static VGImage cloudMaskImage = VG_INVALID_HANDLE;
static const VGint maskImageSize = 512;

// current alpha mask configuration
static AlphaPrimitiveType mask0Type = MaskPath0;
static VGfloat mask0Pos[2] = { 0.0f };
static AlphaPrimitiveType mask1Type = MaskPath1;
static VGfloat mask1Pos[2] = { 0.0f };
static VGMaskOperation maskOperation = VG_UNION_MASK;

// control points
static VGfloat controlPointsRadius = 14.0f;
static VGint pickedControlPoint = CONTROL_POINT_NONE;
static VGboolean mustUpdateMask = VG_FALSE;

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

    // create a color paint, used to draw control points and paths
    solidCol = vgCreatePaint();
    vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
}

static void genPaths(const VGint surfaceWidth,
                     const VGint surfaceHeight) {

    // create the ship
    ship = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vgAppendPathData(ship, 239, ship_commands, ship_coordinates);
    // ship background
    shipBackground = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguRect(shipBackground, 0.0f, 0.0f, (VGfloat)surfaceWidth, (VGfloat)surfaceHeight);
    // first mask path
    spotMask0 = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vgAppendPathData(spotMask0, 74, mask_path0_commands, mask_path0_coordinates);
    // second mask path
    spotMask1 = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vgAppendPathData(spotMask1, 67, mask_path1_commands, mask_path1_coordinates);
    // create the draggable control point
    controlPoint = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
    // create image bounds
    imageBounds = vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    vguRect(imageBounds, -(VGfloat)(maskImageSize / 2 - 1), -(VGfloat)(maskImageSize / 2 - 1), (VGfloat)(maskImageSize - 2), (VGfloat)(maskImageSize - 2));
}

static void genImages(void) {

    // first mask image
    starMaskImage = vgCreateImage(VG_A_8, maskImageSize, maskImageSize, VG_IMAGE_QUALITY_NONANTIALIASED);
    vgImageSubData(starMaskImage, mask_image0, maskImageSize, VG_A_8, 0, 0, maskImageSize, maskImageSize);
    // second mask image
    cloudMaskImage = vgCreateImage(VG_A_8, maskImageSize, maskImageSize, VG_IMAGE_QUALITY_NONANTIALIASED);
    vgImageSubData(cloudMaskImage, mask_image1, maskImageSize, VG_A_8, 0, 0, maskImageSize, maskImageSize);
}

static VGfloat setAlphaPrimitive(const VGint surfaceWidth,
                                 const VGint surfaceHeight,
                                 const AlphaPrimitiveType type,
                                 const VGfloat pos[]) {

    VGfloat scl = 1.0f;

    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(pos[X_COORD], pos[Y_COORD]);

    if ((type == MaskPath0) || (type == MaskPath1)) {
        // vector path
        const VGint minDim = (surfaceWidth < surfaceHeight) ? surfaceWidth : surfaceHeight;
        // 3/4 of the minimum surface dimension
        const VGint size = (minDim * 3) / 4;
        // first alpha path is centered at [0.35; 0.347]
        // second alpha path is centered at [0.35; 0.38]
        const VGfloat tx = (type == MaskPath0) ? 0.35f : 0.35f;
        const VGfloat ty = (type == MaskPath0) ? 0.347f : 0.38f;

        scl = ((VGfloat)size) * 1.1f;
        vgScale(scl, scl);
        vgTranslate(-tx, -ty);
    }

    return scl;
}

// draw a single alpha mask primitive
static void drawAlphaMaskPrimitive(const VGint surfaceWidth,
                                   const VGint surfaceHeight,
                                   const AlphaPrimitiveType type,
                                   const VGfloat pos[],
                                   const VGMaskOperation operation) {

    // setup transformation for the apha mask primitive
    setAlphaPrimitive(surfaceWidth, surfaceHeight, type, pos);

    if ((type == MaskPath0) || (type == MaskPath1)) {
        // vgRenderToMask always modifies the whole alpha mask
        vgRenderToMask((type == MaskPath0) ? spotMask0 : spotMask1, VG_FILL_PATH, operation);
    }
    else {
        // alpha image
        const VGImage mask = (type == MaskImage0) ? starMaskImage : cloudMaskImage;
        // vgMask just modifies the specified region
        vgMask(mask, operation, (VGint)pos[X_COORD] - (maskImageSize / 2), (VGint)pos[Y_COORD] - (maskImageSize / 2), maskImageSize, maskImageSize);
    }
}

// update the whole alpha mask (i.e. draw the current two alpha primitives)
static void drawAlphaMask(const VGint surfaceWidth,
                          const VGint surfaceHeight) {

    // vgRenderToMask always modifies the whole alpha mask
    //
    // vgMask just modifies the specified region, so in order to be consistent
    // we ensure to clear alpha mask as a pre-step
    if ((mask0Type == MaskImage0) || (mask0Type == MaskImage1)) {
        vgMask(VG_INVALID_HANDLE, VG_CLEAR_MASK, 0, 0, surfaceWidth, surfaceHeight);
    }
    drawAlphaMaskPrimitive(surfaceWidth, surfaceHeight, mask0Type, mask0Pos, VG_SET_MASK);
    drawAlphaMaskPrimitive(surfaceWidth, surfaceHeight, mask1Type, mask1Pos, maskOperation);
}

// draw the contours of current alpha primitives
static void drawAlphaMaskSilhouette(const VGint surfaceWidth,
                                    const VGint surfaceHeight) {

    VGfloat scl;
    static const VGfloat dashPatternPath[2] = { 0.01f, 0.01f };
    static const VGfloat dashPatternImage[2] = { 10.0f, 10.0f };

    // disable masking and set a white color
    vgSeti(VG_MASKING, VG_FALSE);
    vgSetColor(solidCol, 0xFFFFFFFF);

    // because the stroke will be scaled according to the "path user to surface" transformation, we
    // adjust the stroke line width in order to have always a one pixel wide outline
    scl = setAlphaPrimitive(surfaceWidth, surfaceHeight, mask0Type, mask0Pos);
    vgSetf(VG_STROKE_LINE_WIDTH, 1.0f / scl);
    if ((mask0Type == MaskPath0) || (mask0Type == MaskPath1)) {
        // path
        vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternPath);
        vgDrawPath((mask0Type == MaskPath0) ? spotMask0 : spotMask1, VG_STROKE_PATH);
    }
    else {
        // alpha image
        vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternImage);
        vgDrawPath(imageBounds, VG_STROKE_PATH);
    }

    // because the stroke will be scaled according to the "path user to surface" transformation, we
    // adjust the stroke line width in order to have always a one pixel wide outline
    scl = setAlphaPrimitive(surfaceWidth, surfaceHeight, mask1Type, mask1Pos);
    vgSetf(VG_STROKE_LINE_WIDTH, 1.0f / scl);
    if ((mask1Type == MaskPath0) || (mask1Type == MaskPath1)) {
        // path
        vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternPath);
        vgDrawPath((mask1Type == MaskPath0) ? spotMask0 : spotMask1, VG_STROKE_PATH);
    }
    else {
        // alpha image
        vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternImage);
        vgDrawPath(imageBounds, VG_STROKE_PATH);
    }
}

static void resetAlphaMask(const VGint surfaceWidth,
                           const VGint surfaceHeight) {

    // reset alpha primitives type and position
    mask0Type = MaskPath0;
    mask0Pos[X_COORD] = (VGfloat)(surfaceWidth / 2);
    mask0Pos[Y_COORD] = (VGfloat)(surfaceHeight / 2);
    mask1Type = MaskPath1;
    mask1Pos[X_COORD] = (VGfloat)(surfaceWidth / 2);
    mask1Pos[Y_COORD] = (VGfloat)(surfaceHeight / 2);
    maskOperation = VG_UNION_MASK;
    // generate/draw alpha mask
    drawAlphaMask(surfaceWidth, surfaceHeight);
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
    genPaths(surfaceWidth, surfaceHeight);
    // generate paints
    genPaints();
    // generate mask images
    genImages();
    // reset alpha mask
    resetAlphaMask(surfaceWidth, surfaceHeight);

    // set some default parameters for the OpenVG context
    vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
    vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
    vgSetPaint(solidCol, VG_FILL_PATH | VG_STROKE_PATH);
}

void tutorialDestroy(void) {

    // release paths
    vgDestroyPath(ship);
    vgDestroyPath(shipBackground);
    vgDestroyPath(spotMask0);
    vgDestroyPath(spotMask1);
    vgDestroyPath(controlPoint);
    vgDestroyPath(imageBounds);
    // release paints
    vgSetPaint(VG_INVALID_HANDLE, VG_FILL_PATH | VG_STROKE_PATH);
    vgDestroyPaint(solidCol);
    // release images
    vgDestroyImage(starMaskImage);
    vgDestroyImage(cloudMaskImage);
}

void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight) {

    // reset alpha mask
    resetAlphaMask(surfaceWidth, surfaceHeight);
    // resize ship background
    vgClearPath(shipBackground, VG_PATH_CAPABILITY_ALL);
    vguRect(shipBackground, 0.0f, 0.0f, (VGfloat)surfaceWidth, (VGfloat)surfaceHeight);
    pickedControlPoint = CONTROL_POINT_NONE;
}

void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight) {

    const VGint minDim = (surfaceWidth < surfaceHeight) ? surfaceWidth : surfaceHeight;
    const VGfloat shipScl = (VGfloat)((minDim * 3) / 5);

    if (mustUpdateMask) {
        mustUpdateMask = VG_FALSE;
        drawAlphaMask(surfaceWidth, surfaceHeight);
    }

    // clear the whole drawing surface
    vgClear(0, 0, surfaceWidth, surfaceHeight);

    // enable masking
    vgSeti(VG_MASKING, VG_TRUE);
    // blue background
    vgSetColor(solidCol, 0x3F6FBFFF);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgDrawPath(shipBackground, VG_FILL_PATH);
    // the yellow ship (its geometric center is [0.64; 0.3])
    vgSetColor(solidCol, 0xFFBF0DFF);
    vgLoadIdentity();
    vgTranslate((VGfloat)(surfaceWidth / 2), (VGfloat)(surfaceHeight / 2));
    vgScale(shipScl, shipScl);
    vgTranslate(-0.64f, -0.3f);
    vgDrawPath(ship, VG_FILL_PATH);

    // draw alpha mask contours
    drawAlphaMaskSilhouette(surfaceWidth, surfaceHeight);

    // draw control points
    vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
    vgSetfv(VG_STROKE_DASH_PATTERN, 0, NULL);
    vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
    vgLoadIdentity();
    vgTranslate(mask0Pos[X_COORD], mask0Pos[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);
    vgLoadIdentity();
    vgTranslate(mask1Pos[X_COORD], mask1Pos[Y_COORD]);
    vgDrawPath(controlPoint, VG_STROKE_PATH);
}

// get current mask operation
VGMaskOperation getMaskOperation(void) {

    return maskOperation;
}

/*****************************************************************
                        interactive options
*****************************************************************/
static AlphaPrimitiveType toggleAlphaPrimitive(const AlphaPrimitiveType current) {

    AlphaPrimitiveType newPrimitive;

    switch (current) {
        case MaskPath0:
            newPrimitive = MaskPath1;
            break;
        case MaskPath1:
            newPrimitive = MaskImage0;
            break;
        case MaskImage0:
            newPrimitive = MaskImage1;
            break;
        case MaskImage1:
            newPrimitive = MaskPath0;
            break;
        default:
            newPrimitive = current;
            break;
    }

    return newPrimitive;
}

void toggleMask1(void) {

    mask0Type = toggleAlphaPrimitive(mask0Type);
    // we update alpha mask within the 'tutorialDraw' function, in order to
    // stick to the rendering thread
    mustUpdateMask = VG_TRUE;
}

void toggleMask2(void) {

    mask1Type = toggleAlphaPrimitive(mask1Type);
    // we update alpha mask within the 'tutorialDraw' function, in order to
    // stick to the rendering thread
    mustUpdateMask = VG_TRUE;
}

void toggleMaskOperation(void) {

    VGMaskOperation newOp;

    switch (maskOperation) {
        case VG_UNION_MASK:
            newOp = VG_INTERSECT_MASK;
            break;
        case VG_INTERSECT_MASK:
            newOp = VG_SUBTRACT_MASK;
            break;
        case VG_SUBTRACT_MASK:
            newOp = VG_UNION_MASK;
            break;
        default:
            newOp = maskOperation;
            break;
    }

    maskOperation = newOp;

    // we update alpha mask within the 'tutorialDraw' function, in order to
    // stick to the rendering thread
    mustUpdateMask = VG_TRUE;
}

static void randomMask(void) {

    mask0Type = (AlphaPrimitiveType)(rand() % 4);
    mask1Type = (AlphaPrimitiveType)(rand() % 4);
    maskOperation = (VGMaskOperation)((rand() % 3) + VG_UNION_MASK);
    // we update alpha mask within the 'tutorialDraw' function, in order to
    // stick to the rendering thread
    mustUpdateMask = VG_TRUE;
}

/*****************************************************************
                        handle mouse events
*****************************************************************/
void mouseLeftButtonDown(const VGint x,
                         const VGint y) {

    VGfloat distMask0, distMask1;
    VGfloat mouseX = (VGfloat)x;
    VGfloat mouseY = (VGfloat)y;

    // calculate mouse distance from control points
    distMask0 = distance(mouseX, mouseY, mask0Pos[X_COORD], mask0Pos[Y_COORD]);
    distMask1 = distance(mouseX, mouseY, mask1Pos[X_COORD], mask1Pos[Y_COORD]);
    // check if we have picked a control point
    if (distMask0 < distMask1) {
        pickedControlPoint = (distMask0 < controlPointsRadius * 1.1f) ? CONTROL_POINT_MASK0 : CONTROL_POINT_NONE;
    }
    else {
        pickedControlPoint = (distMask1 < controlPointsRadius * 1.1f) ? CONTROL_POINT_MASK1 : CONTROL_POINT_NONE;
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
            VGfloat* controlPoint = (pickedControlPoint == CONTROL_POINT_MASK0) ? mask0Pos : mask1Pos;
            // assign the new control point position
            controlPoint[X_COORD] = (VGfloat)x;
            controlPoint[Y_COORD] = (VGfloat)y;
            // we update alpha mask within the 'tutorialDraw' function, in order to
            // stick to the rendering thread
            mustUpdateMask = VG_TRUE;
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

    randomMask();
}

void touchPinch(const VGfloat deltaScl) {

    (void)deltaScl;
}

void touchRotate(const VGfloat deltaRot) {

    (void)deltaRot;
}
