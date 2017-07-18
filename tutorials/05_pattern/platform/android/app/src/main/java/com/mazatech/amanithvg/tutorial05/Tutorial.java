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
package com.mazatech.amanithvg.tutorial05;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;
import javax.microedition.khronos.openvg.VGImage;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11Ext.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private AmanithVG vg;
    // path objects
    private VGPath filledCircle;
    private VGPath controlPoint;
    private VGPath controlBounds;
    // paint objects
    private VGPaint solidCol;
    private VGPaint pattern;
    // pottern image
    private VGImage patternImage;
    private int patternImageSize;
    private int patternImageFormat;
    // pattern parameters
    private int tilingMode;
    private float[] patternCenter;
    private float[] patternTarget;
    // keep track of "path user to surface" translation
    private float[] userToSurfaceTranslation;
    private float controlPointsRadius;
    private int pickedControlPoint;
    // touch state
    private float oldTouchX;
    private float oldTouchY;
    private int touchState;

    private static final int TOUCH_MODE_NONE = 0;
    private static final int TOUCH_MODE_DOWN = 1;

    private static final int X_COORD = 0;
    private static final int Y_COORD = 1;

    private static final int CONTROL_POINT_NONE = 0;
    private static final int CONTROL_POINT_CENTER = 1;
    private static final int CONTROL_POINT_TARGET = 2;

    private static final byte[] boundsCmd = {
        VG_MOVE_TO_ABS,
        VG_LINE_TO_ABS,
        VG_LINE_TO_ABS,
        VG_LINE_TO_ABS,
        VG_CLOSE_PATH
    };

    private static final int[] patternColors = {
        0xFF6030FF, 0xFFB060FF, 0xFF9090FF, 0xFF30B0FF,
        0x60FF30FF, 0xB0FF60FF, 0x90FF90FF, 0x30FFB0FF,
        0x6030FFFF, 0xB060FFFF, 0x9090FFFF, 0x30B0FFFF,
        0x303030FF, 0x606060FF, 0x909090FF, 0xB0B0B0FF
    };

    Tutorial(AmanithVG vgInstance) {

        vg = vgInstance;
        filledCircle = null;
        controlPoint = null;
        controlBounds = null;
        solidCol = null;
        pattern = null;
        patternImage = null;
        patternImageSize = 0;
        patternImageFormat = VG_sRGBA_8888_PRE;
        tilingMode = VG_TILE_FILL;
        patternCenter = new float[] { 0.0f, 0.0f };
        patternTarget = new float[] { 0.0f, 0.0f };
        userToSurfaceTranslation = new float[] { 0.0f, 0.0f };
        controlPointsRadius = 14.0f;
        pickedControlPoint = CONTROL_POINT_NONE;
        oldTouchX = 0.0f;
        oldTouchY = 0.0f;
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

    // calculate and upload paint / path transformations to the OpenVG backend
    private void setMatrices(int surfaceWidth,
                             int surfaceHeight) {

        // calculate pattern direction
        float[] dir = new float[] {
            patternTarget[X_COORD] - patternCenter[X_COORD],
            patternTarget[Y_COORD] - patternCenter[Y_COORD]
        };
        // calculate pattern scale
        float l = (float)Math.hypot(dir[X_COORD], dir[Y_COORD]);
        float paintToUserScale = l / (float)(patternImageSize);
        // calculate pattern rotation
        float rotRadians = (float)Math.atan2(dir[Y_COORD], dir[X_COORD]);
        float rotDegrees = rotRadians * 57.2957795f;
        // calculate "user to surface" transformation
        float userToSurfaceScale = (float)((surfaceWidth < surfaceHeight) ? (surfaceWidth / 2) : (surfaceHeight / 2)) * 0.9f;
        userToSurfaceTranslation[X_COORD] = (float)(surfaceWidth / 2);
        userToSurfaceTranslation[Y_COORD] = (float)(surfaceHeight / 2);

        // "paint to user" transformation, upload matrix to the OpenVG backend
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
        vg.vgLoadIdentity();
        vg.vgTranslate(patternCenter[X_COORD] / userToSurfaceScale, patternCenter[Y_COORD] / userToSurfaceScale);
        vg.vgScale(paintToUserScale / userToSurfaceScale, paintToUserScale / userToSurfaceScale);
        vg.vgRotate(rotDegrees);

        // "user to surface" transformation, upload matrix to the OpenVG backend
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(userToSurfaceTranslation[X_COORD], userToSurfaceTranslation[Y_COORD]);
        vg.vgScale(userToSurfaceScale, userToSurfaceScale);
    }

    // calculate the position of pattern control points, in surface space
    private void patternParamsGet(float[] srfCenterPoint,
                                  float[] srfTargetPoint) {

        srfCenterPoint[X_COORD] = patternCenter[X_COORD] + userToSurfaceTranslation[X_COORD];
        srfCenterPoint[Y_COORD] = patternCenter[Y_COORD] + userToSurfaceTranslation[Y_COORD];
        srfTargetPoint[X_COORD] = patternTarget[X_COORD] + userToSurfaceTranslation[X_COORD];
        srfTargetPoint[Y_COORD] = patternTarget[Y_COORD] + userToSurfaceTranslation[Y_COORD];
    }

    // set the position of pattern control points, in surface space
    private void patternParamsSet(final float[] srfCenterPoint,
                                  final float[] srfTargetPoint) {

        patternCenter[X_COORD] = srfCenterPoint[X_COORD] - userToSurfaceTranslation[X_COORD];
        patternCenter[Y_COORD] = srfCenterPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD];
        patternTarget[X_COORD] = srfTargetPoint[X_COORD] - userToSurfaceTranslation[X_COORD];
        patternTarget[Y_COORD] = srfTargetPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD];
    }

    // reset pattern control points
    private void patternParamsReset(final int surfaceWidth,
                                    final int surfaceHeight) {

        patternCenter[X_COORD] = -(float)(patternImageSize / 2);
        patternCenter[Y_COORD] = -(float)(patternImageSize / 2);
        patternTarget[X_COORD] = patternCenter[X_COORD] + (float)(patternImageSize);
        patternTarget[Y_COORD] = patternCenter[Y_COORD];
    }

    private void genPaints() {

        float white[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

        // create a white color paint, used to draw control points
        solidCol = vg.vgCreatePaint();
        vg.vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
        vg.vgSetParameterfv(solidCol, VG_PAINT_COLOR, 4, white);
        // create pattern image
        patternImage = vg.vgCreateImage(patternImageFormat, patternImageSize, patternImageSize, VG_IMAGE_QUALITY_BETTER);
        int pixels[] = new int[patternImageSize * patternImageSize];
        int blocks = patternImageSize / 4;
        for (int i = 0; i < patternImageSize; ++i) {
            int y = i / blocks;
            for (int j = 0; j < patternImageSize; ++j) {
                int x = j / blocks;
                pixels[i * patternImageSize + j] = patternColors[y * 4 + x];
            }
        }
        vg.vgImageSubData(patternImage, pixels, patternImageSize * 4, VG_sRGBA_8888_PRE, 0, 0, patternImageSize, patternImageSize);
        // create pattern
        pattern = vg.vgCreatePaint();
        vg.vgSetParameteri(pattern, VG_PAINT_TYPE, VG_PAINT_TYPE_PATTERN);
        vg.vgSetParameteri(pattern, VG_PAINT_PATTERN_TILING_MODE, tilingMode);
        vg.vgPaintPattern(pattern, patternImage);
    }

    private void genPaths() {

        // create the circle that will be filled by the pattern paint
        filledCircle = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(filledCircle, 0.0f, 0.0f, 2.0f, 2.0f);
        // create the draggable gradient point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
        // create pattern bounds
        controlBounds = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    }

    private void genPatternBounds(float[] srfCenterPoint,
                                  float[] srfTargetPoint) {

        float dx = srfTargetPoint[X_COORD] - srfCenterPoint[X_COORD];
        float dy = srfTargetPoint[Y_COORD] - srfCenterPoint[Y_COORD];
        float[] boundsCoords = new float[] {
            srfCenterPoint[X_COORD], srfCenterPoint[Y_COORD],
            srfTargetPoint[X_COORD], srfTargetPoint[Y_COORD],
            srfTargetPoint[X_COORD] - dy, srfTargetPoint[Y_COORD] + dx,
            srfCenterPoint[X_COORD] - dy, srfCenterPoint[Y_COORD] + dx
        };
        vg.vgClearPath(controlBounds, VG_PATH_CAPABILITY_ALL);
        vg.vgAppendPathData(controlBounds, 5, boundsCmd, boundsCoords);
    }

    void init(int surfaceWidth,
              int surfaceHeight,
              int preferredImageFormat) {

        // an opaque dark grey
        float clearColor[] = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
        float tileColor[] = new float[] { 0.1f, 0.6f, 0.3f, 1.0f };

        // make sure to have well visible (and draggable) control points
        controlPointsRadius = ((float)Math.min(surfaceWidth, surfaceHeight) / 512.0f) * 14.0f;
        if (controlPointsRadius < 14.0f) {
            controlPointsRadius = 14.0f;
        }
        patternImageSize = (Math.min(surfaceWidth, surfaceHeight) >= 1024) ? 128 : 64;
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
        vg.vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgSetfv(VG_TILE_FILL_COLOR, 4, tileColor);
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
        vg.vgSeti(VG_IMAGE_QUALITY, VG_IMAGE_QUALITY_BETTER);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    }

    void destroy() {

        // release paths
        vg.vgDestroyPath(filledCircle);
        vg.vgDestroyPath(controlPoint);
        vg.vgDestroyPath(controlBounds);
        // release paints
        vg.vgSetPaint(null, VG_FILL_PATH | VG_STROKE_PATH);
        vg.vgDestroyPaint(solidCol);
        vg.vgPaintPattern(pattern, null);
        vg.vgDestroyImage(patternImage);
        vg.vgDestroyPaint(pattern);
    }

    void resize(int surfaceWidth,
                int surfaceHeight) {

        // calculate and upload paint / path transformations to the OpenVG backend
        setMatrices(surfaceWidth, surfaceHeight);
        // reset pattern parameters
        patternParamsReset(surfaceWidth, surfaceHeight);
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    void draw(int surfaceWidth,
              int surfaceHeight) {

        float center[] = new float[2];
        float target[] = new float[2];

        // clear the whole drawing surface
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // calculate and upload paint / path transformations to the OpenVG backend
        setMatrices(surfaceWidth, surfaceHeight);

        // draw the filled circle
        vg.vgSetPaint(pattern, VG_FILL_PATH);
        vg.vgDrawPath(filledCircle, VG_FILL_PATH);

        // calculate the position of pattern control points, in surface space
        patternParamsGet(center, target);

        // draw pattern bounds
        genPatternBounds(center, target);
        vg.vgLoadIdentity();
        vg.vgDrawPath(controlBounds, VG_STROKE_PATH);

        // draw pattern control points
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(center[X_COORD], center[Y_COORD]);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(target[X_COORD], target[Y_COORD]);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
    }

    /*****************************************************************
                            interactive options
    *****************************************************************/
    void toggleTilingMode() {

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
        vg.vgSetParameteri(pattern, VG_PAINT_PATTERN_TILING_MODE, tilingMode);
    }

    /*****************************************************************
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        float distCenter, distTarget;
        float center[] = new float[2];
        float target[] = new float[2];

        // get current pattern parameters
        patternParamsGet(center, target);
        // calculate touch distance from control points
        distCenter = distance(x, y, center[X_COORD], center[Y_COORD]);
        distTarget = distance(x, y, target[X_COORD], target[Y_COORD]);
        // check if we have picked a control point
        if (distCenter < distTarget) {
            pickedControlPoint = (distCenter < controlPointsRadius * 1.1f) ? CONTROL_POINT_CENTER : CONTROL_POINT_NONE;
        }
        else {
            pickedControlPoint = (distTarget < controlPointsRadius * 1.1f) ? CONTROL_POINT_TARGET : CONTROL_POINT_NONE;
        }
        // keep track of current touch position
        oldTouchX = x;
        oldTouchY = y;
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
                float center[] = new float[2];
                float target[] = new float[2];
                float dx = x - oldTouchX;
                float dy = y - oldTouchY;
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
                    target[X_COORD] = x;
                    target[Y_COORD] = y;
                }
                // update pattern parameters
                patternParamsSet(center, target);
            }
        }
        // keep track of current touch position
        oldTouchX = x;
        oldTouchY = y;
    }

    void touchDoubleTap(float x,
                        float y) {

        toggleTilingMode();
    }
}
