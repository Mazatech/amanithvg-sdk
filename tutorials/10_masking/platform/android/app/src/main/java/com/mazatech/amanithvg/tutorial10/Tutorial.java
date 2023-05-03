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
package com.mazatech.amanithvg.tutorial10;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import java.util.Random;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;
import javax.microedition.khronos.openvg.VGImage;

import static javax.microedition.khronos.openvg.VG101.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private final AmanithVG vg;
    // path objects
    private VGPath ship;
    private VGPath shipBackground;
    private VGPath spotMask0;
    private VGPath spotMask1;
    private VGPath controlPoint;
    private VGPath imageBounds;
    // paint objects
    private VGPaint solidCol;
    // mask images
    private VGImage starMaskImage;
    private VGImage cloudMaskImage;
    private static final int maskImageSize = 512;
    // current alpha mask configuration
    private int mask0Type;
    private final PointF mask0Pos;
    private int mask1Type;
    private final PointF mask1Pos;
    private int maskOperation;
    // control points
    private float controlPointsRadius;
    private int pickedControlPoint;
    private boolean mustUpdateMask;
    // random numbers generator
    private final Random rnd;
    // touch state
    private int touchState;

    private static final int TOUCH_MODE_NONE = 0;
    private static final int TOUCH_MODE_DOWN = 1;

    private static final int CONTROL_POINT_NONE = 0;
    private static final int CONTROL_POINT_MASK0 = 1;
    private static final int CONTROL_POINT_MASK1 = 2;

    private static final float[] noDash = { 0 };
    private static final float[] dashPatternPath = { 0.01f, 0.01f };
    private static final float[] dashPatternImage = { 10.0f, 10.0f };

    // alpha primitive type
    private static final int MaskPath0 = 0;
    private static final int MaskPath1 = 1;
    private static final int MaskImage0 = 2;
    private static final int MaskImage1 = 3;

    Tutorial(final AmanithVG vgInstance) {

        vg = vgInstance;
        ship = null;
        shipBackground = null;
        spotMask0 = null;
        spotMask1 = null;
        ship = null;
        controlPoint = null;
        imageBounds = null;
        solidCol = null;
        starMaskImage = null;
        cloudMaskImage = null;
        mask0Type = MaskPath0;
        mask0Pos = new PointF(0.0f, 0.0f);
        mask1Type = MaskPath1;
        mask1Pos = new PointF(0.0f, 0.0f);
        maskOperation = VG_UNION_MASK;
        controlPointsRadius = 14.0f;
        pickedControlPoint = CONTROL_POINT_NONE;
        mustUpdateMask = false;
        rnd = new Random();
        touchState = TOUCH_MODE_NONE;
    }

    // calculate the distance between two points
    private float distance(float x0,
                           float y0,
                           float x1,
                           float y1) {

        float dx = x0 - x1;
        float dy = y0 - y1;
        return (float)Math.hypot(dx, dy);
    }

    private void genPaints() {

        // create a color paint, used to draw control points and paths
        solidCol = vg.vgCreatePaint();
        vg.vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    }

    private void genPaths(final int surfaceWidth,
                          final int surfaceHeight) {

        // create the ship
        ship = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vgAppendPathData(ship, 239, Ship.ship_commands, Ship.ship_coordinates);
        // ship background
        shipBackground = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguRect(shipBackground, 0.0f, 0.0f, (float)surfaceWidth, (float)surfaceHeight);
        // first mask path
        spotMask0 = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vgAppendPathData(spotMask0, 74, MaskPaths.mask_path0_commands, MaskPaths.mask_path0_coordinates);
        // second mask path
        spotMask1 = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vgAppendPathData(spotMask1, 67, MaskPaths.mask_path1_commands, MaskPaths.mask_path1_coordinates);
        // create the draggable control point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
        // create image bounds
        imageBounds = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguRect(imageBounds, -(float)(maskImageSize / 2 - 1), -(float)(maskImageSize / 2 - 1), (float)(maskImageSize - 2), (float)(maskImageSize - 2));
    }

    private void genImages(final byte[] rawStarData,
                           final byte[] rawCloudData) {

        // first mask image
        starMaskImage = vg.vgCreateImage(VG_A_8, maskImageSize, maskImageSize, VG_IMAGE_QUALITY_NONANTIALIASED);
        vg.vgImageSubData(starMaskImage, rawStarData, maskImageSize, VG_A_8, 0, 0, maskImageSize, maskImageSize);
        // second mask image
        cloudMaskImage = vg.vgCreateImage(VG_A_8, maskImageSize, maskImageSize, VG_IMAGE_QUALITY_NONANTIALIASED);
        vg.vgImageSubData(cloudMaskImage, rawCloudData, maskImageSize, VG_A_8, 0, 0, maskImageSize, maskImageSize);
    }

    private float setAlphaPrimitive(int surfaceWidth,
                                    int surfaceHeight,
                                    int type,
                                    @NonNull final PointF pos) {

        float scl = 1.0f;

        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(pos.x, pos.y);

        if ((type == MaskPath0) || (type == MaskPath1)) {
            // vector path
            int minDim = Math.min(surfaceWidth, surfaceHeight);
            // 3/4 of the minimum surface dimension
            int size = (minDim * 3) / 4;
            // first alpha path is centered at [0.35; 0.347]
            // second alpha path is centered at [0.35; 0.38]
            float tx = (type == MaskPath0) ? 0.35f : 0.35f;
            float ty = (type == MaskPath0) ? 0.347f : 0.38f;

            scl = ((float)size) * 1.1f;
            vg.vgScale(scl, scl);
            vg.vgTranslate(-tx, -ty);
        }

        return scl;
    }

    // draw a single alpha mask primitive
    private void drawAlphaMaskPrimitive(int surfaceWidth,
                                        int surfaceHeight,
                                        int type,
                                        final PointF pos,
                                        int operation) {

        // setup transformation for the apha mask primitive
        setAlphaPrimitive(surfaceWidth, surfaceHeight, type, pos);

        if ((type == MaskPath0) || (type == MaskPath1)) {
            // vgRenderToMask always modifies the whole alpha mask
            vg.vgRenderToMask((type == MaskPath0) ? spotMask0 : spotMask1, VG_FILL_PATH, operation);
        }
        else {
            // alpha image
            VGImage mask = (type == MaskImage0) ? starMaskImage : cloudMaskImage;
            // vgMask just modifies the specified region
            vg.vgMask(mask, operation, (int)pos.x - (maskImageSize / 2), (int)pos.y - (maskImageSize / 2), maskImageSize, maskImageSize);
        }
    }

    // update the whole alpha mask (i.e. draw the current two alpha primitives)
    private void drawAlphaMask(int surfaceWidth,
                               int surfaceHeight) {

        // vgRenderToMask always modifies the whole alpha mask
        //
        // vgMask just modifies the specified region, so in order to be consistent
        // we ensure to clear alpha mask as a pre-step
        if ((mask0Type == MaskImage0) || (mask0Type == MaskImage1)) {
            vg.vgMask((VGImage)null, VG_CLEAR_MASK, 0, 0, surfaceWidth, surfaceHeight);
        }
        drawAlphaMaskPrimitive(surfaceWidth, surfaceHeight, mask0Type, mask0Pos, VG_SET_MASK);
        drawAlphaMaskPrimitive(surfaceWidth, surfaceHeight, mask1Type, mask1Pos, maskOperation);
    }

    // draw the contours of current alpha primitives
    private void drawAlphaMaskSilhouette(int surfaceWidth,
                                         int surfaceHeight) {

        float scl;

        // disable masking and set a white color
        vg.vgSeti(VG_MASKING, VG_FALSE);
        vg.vgSetColor(solidCol, 0xFFFFFFFF);

        // because the stroke will be scaled according to the "path user to surface" transformation, we
        // adjust the stroke line width in order to have always a one pixel wide outline
        scl = setAlphaPrimitive(surfaceWidth, surfaceHeight, mask0Type, mask0Pos);
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 1.0f / scl);
        if ((mask0Type == MaskPath0) || (mask0Type == MaskPath1)) {
            // path
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternPath);
            vg.vgDrawPath((mask0Type == MaskPath0) ? spotMask0 : spotMask1, VG_STROKE_PATH);
        }
        else {
            // alpha image
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternImage);
            vg.vgDrawPath(imageBounds, VG_STROKE_PATH);
        }

        // because the stroke will be scaled according to the "path user to surface" transformation, we
        // adjust the stroke line width in order to have always a one pixel wide outline
        scl = setAlphaPrimitive(surfaceWidth, surfaceHeight, mask1Type, mask1Pos);
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 1.0f / scl);
        if ((mask1Type == MaskPath0) || (mask1Type == MaskPath1)) {
            // path
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternPath);
            vg.vgDrawPath((mask1Type == MaskPath0) ? spotMask0 : spotMask1, VG_STROKE_PATH);
        }
        else {
            // alpha image
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPatternImage);
            vg.vgDrawPath(imageBounds, VG_STROKE_PATH);
        }
    }

    private void resetAlphaMask(int surfaceWidth,
                                int surfaceHeight) {

        // reset alpha primitives type and position
        mask0Type = MaskPath0;
        mask0Pos.set((float)(surfaceWidth / 2), (float)(surfaceHeight / 2));
        mask1Type = MaskPath1;
        mask1Pos.set((float)(surfaceWidth / 2), (float)(surfaceHeight / 2));
        maskOperation = VG_UNION_MASK;
        // generate/draw alpha mask
        drawAlphaMask(surfaceWidth, surfaceHeight);
    }

    void init(int surfaceWidth,
              int surfaceHeight,
              final byte[] rawStarData,
              final byte[] rawCloudData) {

        // an opaque dark grey
        float[] clearColor = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };

        // make sure to have well visible (and draggable) control points
        controlPointsRadius = ((float)Math.min(surfaceWidth, surfaceHeight) / 512.0f) * 14.0f;
        if (controlPointsRadius < 14.0f) {
            controlPointsRadius = 14.0f;
        }

        // generate paths
        genPaths(surfaceWidth, surfaceHeight);
        // generate paints
        genPaints();
        // generate mask images
        genImages(rawStarData, rawCloudData);
        // reset alpha mask
        resetAlphaMask(surfaceWidth, surfaceHeight);

        // set some default parameters for the OpenVG context
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSetPaint(solidCol, VG_FILL_PATH | VG_STROKE_PATH);
    }

    void destroy() {

        // release paths
        vg.vgDestroyPath(ship);
        vg.vgDestroyPath(shipBackground);
        vg.vgDestroyPath(spotMask0);
        vg.vgDestroyPath(spotMask1);
        vg.vgDestroyPath(controlPoint);
        vg.vgDestroyPath(imageBounds);
        // release paints
        vg.vgSetPaint(null, VG_FILL_PATH | VG_STROKE_PATH);
        vg.vgDestroyPaint(solidCol);
        // release images
        vg.vgDestroyImage(starMaskImage);
        vg.vgDestroyImage(cloudMaskImage);
    }

    void resize(int surfaceWidth,
                int surfaceHeight) {

        // reset alpha mask
        resetAlphaMask(surfaceWidth, surfaceHeight);
        // resize ship background
        vg.vgClearPath(shipBackground, VG_PATH_CAPABILITY_ALL);
        vg.vguRect(shipBackground, 0.0f, 0.0f, (float)surfaceWidth, (float)surfaceHeight);
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    void draw(int surfaceWidth,
              int surfaceHeight) {

        int minDim = Math.min(surfaceWidth, surfaceHeight);
        float shipScl = (float)((minDim * 3) / 5);

        if (mustUpdateMask) {
            mustUpdateMask = false;
            drawAlphaMask(surfaceWidth, surfaceHeight);
        }

        // clear the whole drawing surface
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // enable masking
        vg.vgSeti(VG_MASKING, VG_TRUE);
        // blue background
        vg.vgSetColor(solidCol, 0x3F6FBFFF);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgDrawPath(shipBackground, VG_FILL_PATH);
        // the yellow ship (its geometric center is [0.64; 0.3])
        vg.vgSetColor(solidCol, 0xFFBF0DFF);
        vg.vgLoadIdentity();
        vg.vgTranslate((float)(surfaceWidth / 2), (float)(surfaceHeight / 2));
        vg.vgScale(shipScl, shipScl);
        vg.vgTranslate(-0.64f, -0.3f);
        vg.vgDrawPath(ship, VG_FILL_PATH);

        // draw alpha mask contours
        drawAlphaMaskSilhouette(surfaceWidth, surfaceHeight);

        // draw control points
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
        vg.vgSetfv(VG_STROKE_DASH_PATTERN, 0, noDash);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(mask0Pos.x, mask0Pos.y);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(mask1Pos.x, mask1Pos.y);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
    }

    // get current mask operation
    int getMaskOperation() {

        return maskOperation;
    }

    /*****************************************************************
                            interactive options
    *****************************************************************/
    private int toggleAlphaPrimitive(final int current) {

        int newPrimitive;

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

    void toggleMask1() {

        mask0Type = toggleAlphaPrimitive(mask0Type);
        // we update alpha mask within the 'draw' method, in order to stick to the rendering thread
        mustUpdateMask = true;
    }

    void toggleMask2() {

        mask1Type = toggleAlphaPrimitive(mask1Type);
        // we update alpha mask within the 'draw' method, in order to stick to the rendering thread
        mustUpdateMask = true;
    }

    void toggleMaskOperation() {

        int newOp;

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

        // we update alpha mask within the 'draw' method, in order to stick to the rendering thread
        mustUpdateMask = true;
    }

    private void randomMask() {

        mask0Type = (Math.abs(rnd.nextInt()) % 4);
        mask1Type = (Math.abs(rnd.nextInt()) % 4);
        maskOperation = (Math.abs(rnd.nextInt()) % 3) + VG_UNION_MASK;
        // we update alpha mask within the 'tutorialDraw' function, in order to
        // stick to the rendering thread
        mustUpdateMask = true;
    }

    /*****************************************************************
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        float distMask0, distMask1;

        // calculate touch distance from control points
        distMask0 = distance(x, y, mask0Pos.x, mask0Pos.y);
        distMask1 = distance(x, y, mask1Pos.x, mask1Pos.y);
        // check if we have picked a gradient control point
        if (distMask0 < distMask1) {
            pickedControlPoint = (distMask0 < controlPointsRadius * 1.1f) ? CONTROL_POINT_MASK0 : CONTROL_POINT_NONE;
        }
        else {
            pickedControlPoint = (distMask1 < controlPointsRadius * 1.1f) ? CONTROL_POINT_MASK1 : CONTROL_POINT_NONE;
        }
        touchState = TOUCH_MODE_DOWN;
    }

    void touchUp(float x,
                 float y) {

        touchState = TOUCH_MODE_NONE;
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    void touchMove(float x,
                   float y) {

        if (touchState == TOUCH_MODE_DOWN) {
            if (pickedControlPoint != CONTROL_POINT_NONE) {
                // assign the new control point position
                if (pickedControlPoint == CONTROL_POINT_MASK0) {
                    mask0Pos.x = x;
                    mask0Pos.y = y;
                }
                else {
                    mask1Pos.x = x;
                    mask1Pos.y = y;
                }
                // we update alpha mask within the 'draw' method, in order to
                // stick to the rendering thread
                mustUpdateMask = true;
            }
        }
    }

    void touchDoubleTap(float x,
                        float y) {

        randomMask();
    }
}
