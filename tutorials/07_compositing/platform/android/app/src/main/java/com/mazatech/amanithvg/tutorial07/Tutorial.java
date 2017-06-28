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
package com.mazatech.amanithvg.tutorial07;

import android.graphics.PointF;

import java.util.Random;

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
    private VGPath flower;
    private VGPath controlPoint;
    private VGPath imageBounds;
    private float controlPointsRadius;
    // paint objects
    private VGPaint solidCol;
    private VGPaint paintSrc;
    private VGPaint paintDst;
    // image objects
    private VGImage srcImage;
    private VGImage dstImage;
    private int imagesFormat;
    private int imagesSize;
    private float[] srcImagePos;
    private float[] dstImagePos;
    // current blend mode
    private int blendMode;
    private boolean extBlendModesSupported;
    private int pickedControlPoint;
    // touch state
    float oldTouchX;
    float oldTouchY;
    int touchState;

    private static final int TOUCH_MODE_NONE = 0;
    private static final int TOUCH_MODE_DOWN = 1;

    private static final int X_COORD = 0;
    private static final int Y_COORD = 1;

    private static final int CONTROL_POINT_NONE = 0;
    private static final int CONTROL_POINT_SRC_IMAGE = 1;
    private static final int CONTROL_POINT_DST_IMAGE = 2;

    private static final float[] noDash = { 0 };

    Tutorial(AmanithVG vgInstance) {

        vg = vgInstance;
        srcImage = null;
        dstImage = null;
        imagesSize = 0;
        srcImagePos = new float[] { 0.0f, 0.0f };
        dstImagePos = new float[] { 0.0f, 0.0f };
        extBlendModesSupported = false;
        blendMode = VG_BLEND_SRC_OVER;
        pickedControlPoint = CONTROL_POINT_NONE;
        controlPointsRadius = 14.0f;
        oldTouchX = 0.0f;
        oldTouchY = 0.0f;
        touchState = TOUCH_MODE_NONE;
    }

    private void extensionsCheck() {

        String extensions = vg.vgGetString(VG_EXTENSIONS);

        // check OpenVG extensions
        extBlendModesSupported = extensions.contains("VG_MZT_advanced_blend_modes");
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

    /*
    private void setDashPattern(int pattern) {

        if (pattern == 0) {
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 0, noDash);
        }
        else {
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 4, dashPatterns[pattern - 1]);
            vg.vgSetf(VG_STROKE_DASH_PHASE, dashPhase);
        }
    }
    */

    private void genPaints() {

        float white[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        // color stops used to generate SRC image
        float srcStops[] = new float[] {
            0.00f, 0.60f, 0.05f, 0.10f, 1.00f,
            1.00f, 0.30f, 0.90f, 0.10f, 0.30f
        };
        float srcGrad[] = new float[] {
            -160.0f, 0.0f,
             160.0f, 0.0f
        };
        // color stops used to generate DST image
        float dstStops[] = new float[] {
            0.00f, 0.90f, 0.80f, 0.00f, 0.90f,
            1.00f, 0.00f, 0.20f, 0.80f, 0.40f
        };
        float dstGrad[] = new float[] {
            -160.0f, 0.0f,
             160.0f, 0.0f
        };

        // create a white color paint, used to draw control points and images bounds
        solidCol = vg.vgCreatePaint();
        vg.vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
        vg.vgSetParameterfv(solidCol, VG_PAINT_COLOR, 4, white);

        // create paint that will be used to generate SRC image
        paintSrc = vg.vgCreatePaint();
        vg.vgSetParameteri(paintSrc, VG_PAINT_TYPE, VG_PAINT_TYPE_LINEAR_GRADIENT);
        vg.vgSetParameterfv(paintSrc, VG_PAINT_COLOR_RAMP_STOPS, 10, srcStops);
        vg.vgSetParameterfv(paintSrc, VG_PAINT_LINEAR_GRADIENT, 4, srcGrad);
        vg.vgSetParameteri(paintSrc, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);

        // create paint that will be used to generate DST image
        paintDst = vg.vgCreatePaint();
        vg.vgSetParameteri(paintDst, VG_PAINT_TYPE, VG_PAINT_TYPE_LINEAR_GRADIENT);
        vg.vgSetParameterfv(paintDst, VG_PAINT_COLOR_RAMP_STOPS, 10, dstStops);
        vg.vgSetParameterfv(paintDst, VG_PAINT_LINEAR_GRADIENT, 4, dstGrad);
        vg.vgSetParameteri(paintDst, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);
    }

    private void genPaths() {

        // flower-like path commands
        byte flowerCmds[] = new byte[] {
            VG_MOVE_TO,
            VG_CUBIC_TO,
            VG_CUBIC_TO,
            VG_CUBIC_TO,
            VG_CUBIC_TO,
            VG_CLOSE_PATH
        };
        // flower-like path coordinates
        float flowerCoords[] = new float[] {
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
        flower = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vgAppendPathData(flower, 6, flowerCmds, flowerCoords);
        // create the draggable control point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
        // create image bounds
        imageBounds = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    }

    private void genImages(final int surfaceWidth,
                           final int surfaceHeight) {


        float scl;
        float black[] = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
        float imgCenter =  (float)(imagesSize / 2);
        int minDim = (surfaceWidth < surfaceHeight) ? surfaceWidth : surfaceHeight;

        // 3/4 of the minimum surface dimension
        imagesSize = (minDim * 3) / 4;
        scl = ((float)imagesSize / 384.0f) * 1.1f;

        // destroy previous images
        if (srcImage != null) {
            vg.vgDestroyImage(srcImage);
            srcImage = null;
        }
        if (dstImage != null) {
            vg.vgDestroyImage(dstImage);
            dstImage = null;
        }
        // create images, using the same format of drawing surface (in order to speedup grabbing and rendering)
        srcImage = vg.vgCreateImage(imagesFormat, imagesSize, imagesSize, VG_IMAGE_QUALITY_NONANTIALIASED);
        dstImage = vg.vgCreateImage(imagesFormat, imagesSize, imagesSize, VG_IMAGE_QUALITY_NONANTIALIASED);

        // clear surface with a transparent black
        vg.vgSetfv(VG_CLEAR_COLOR, 4, black);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        // generate SRC image
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);
        vg.vgSetPaint(paintSrc, VG_FILL_PATH);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(imgCenter, imgCenter);
        vg.vgRotate(180.0f);
        vg.vgScale(scl, scl);
        vg.vgDrawPath(flower, VG_FILL_PATH);
        vg.vgGetPixels(srcImage, 0, 0, 0, 0, imagesSize, imagesSize);
        // generate DST image
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);
        vg.vgSetPaint(paintDst, VG_FILL_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(imgCenter, imgCenter);
        vg.vgRotate(45.0f);
        vg.vgScale(scl, scl);
        vg.vgDrawPath(flower, VG_FILL_PATH);
        vg.vgGetPixels(dstImage, 0, 0, 0, 0, imagesSize, imagesSize);
        // reset images position
        srcImagePos[X_COORD] = imgCenter + (((float)surfaceWidth - imagesSize) / 4.0f);
        srcImagePos[Y_COORD] = imgCenter + (((float)surfaceHeight - imagesSize) / 4.0f);
        dstImagePos[X_COORD] = ((float)surfaceWidth - imgCenter) - (((float)surfaceWidth - imagesSize) / 4.0f);
        dstImagePos[Y_COORD] = ((float)surfaceHeight - imgCenter) - (((float)surfaceHeight - imagesSize) / 4.0f);
        // update path used to draw images bounds
        vg.vgClearPath(imageBounds, VG_PATH_CAPABILITY_ALL);
        vg.vguRect(imageBounds, 0.0f, 0.0f, (float)imagesSize, (float)imagesSize);
    }

    void init(int surfaceWidth,
              int surfaceHeight,
              int preferredImageFormat) {

        // make sure to have well visible (and draggable) control points
        controlPointsRadius = ((float)Math.min(surfaceWidth, surfaceHeight) / 512.0f) * 14.0f;
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
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSeti(VG_IMAGE_QUALITY, VG_IMAGE_QUALITY_NONANTIALIASED);
        vg.vgSeti(VG_IMAGE_MODE, VG_DRAW_IMAGE_NORMAL);
    }

    void destroy() {

        // release paths
        vg.vgDestroyPath(flower);
        vg.vgDestroyPath(controlPoint);
        vg.vgDestroyPath(imageBounds);
        // release paints
        vg.vgSetPaint(null, VG_FILL_PATH | VG_STROKE_PATH);
        vg.vgDestroyPaint(solidCol);
        vg.vgDestroyPaint(paintSrc);
        vg.vgDestroyPaint(paintDst);
        // release images
        vg.vgDestroyImage(srcImage);
        vg.vgDestroyImage(dstImage);
    }

    void resize(int surfaceWidth,
                int surfaceHeight) {

        // re-generate images, taking care of new surface dimensions
        genImages(surfaceWidth, surfaceHeight);
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    void draw(int surfaceWidth,
              int surfaceHeight) {

        float dashPattern[] = new float[] { 10.0f, 10.0f };
        float imgCenter = (float)(imagesSize / 2);
        // an opaque dark grey
        float clearColor[] = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };

        // clear the whole drawing surface
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // draw DST image
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_IMAGE_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(dstImagePos[X_COORD] - imgCenter, dstImagePos[Y_COORD] - imgCenter);
        vg.vgDrawImage(dstImage);
        // draw SRC image, setting the current blend mode
        vg.vgSeti(VG_BLEND_MODE, blendMode);
        vg.vgLoadIdentity();
        vg.vgTranslate(srcImagePos[X_COORD] - imgCenter, srcImagePos[Y_COORD] - imgCenter);
        vg.vgDrawImage(srcImage);

        // draw control points
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
        vg.vgSetfv(VG_STROKE_DASH_PATTERN, 0, noDash);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(dstImagePos[X_COORD], dstImagePos[Y_COORD]);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(srcImagePos[X_COORD], srcImagePos[Y_COORD]);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);

        // draw images bounds
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 1.0f);
        vg.vgSetfv(VG_STROKE_DASH_PATTERN, 2, dashPattern);
        vg.vgLoadIdentity();
        vg.vgTranslate(dstImagePos[X_COORD] - imgCenter, dstImagePos[Y_COORD] - imgCenter);
        vg.vgDrawPath(imageBounds, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(srcImagePos[X_COORD] - imgCenter, srcImagePos[Y_COORD] - imgCenter);
        vg.vgDrawPath(imageBounds, VG_STROKE_PATH);
    }

    // get current blend mode
    int getBlendMode() {

        return (int)blendMode;
    }

    /*****************************************************************
                            interactive options
    *****************************************************************/
    void toggleBlendMode() {

        int newBlendMode;

        switch ((int)blendMode) {
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
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        float distSrc, distDst;

        // calculate touch distance from control points
        distSrc = distance(x, y, srcImagePos[X_COORD], srcImagePos[Y_COORD]);
        distDst = distance(x, y, dstImagePos[X_COORD], dstImagePos[Y_COORD]);
        // check if we have picked a gradient control point
        if (distSrc < distDst) {
            pickedControlPoint = (distSrc < controlPointsRadius) ? CONTROL_POINT_SRC_IMAGE : CONTROL_POINT_NONE;
        }
        else {
            pickedControlPoint = (distDst < controlPointsRadius) ? CONTROL_POINT_DST_IMAGE : CONTROL_POINT_NONE;
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
                // assign the new control point position
                if (pickedControlPoint == CONTROL_POINT_SRC_IMAGE) {
                    srcImagePos[X_COORD] = x;
                    srcImagePos[Y_COORD] = y;
                }
                else {
                    dstImagePos[X_COORD] = x;
                    dstImagePos[Y_COORD] = y;
                }
            }
        }
        // keep track of current touch position
        oldTouchX = x;
        oldTouchY = y;
    }

    void touchDoubleTap(float x,
                        float y) {

        toggleBlendMode();
    }
}
