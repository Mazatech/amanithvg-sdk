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
package com.mazatech.amanithvg.tutorial08;

import android.graphics.PointF;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGImage;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11Ext.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private AmanithVG vg;
    // path objects
    private VGPath controlPoint;
    private VGPath imageBounds;
    private VGPath[] girlPaths;
    // paint objects
    private VGPaint solidCol;
    // girl image, dimensions and format
    private VGImage girlImage;
    private int imageWidth;
    private int imageHeight;
    private int imageFormat;
    // image control points
    private PointF[] imageControlPoints;
    private float controlPointsRadius;
    private int pickedControlPoint;
    boolean mustUpdatePaths;
    // touch state
    float oldTouchX;
    float oldTouchY;
    int touchState;

    private static final int TOUCH_MODE_NONE = 0;
    private static final int TOUCH_MODE_DOWN = 1;

    private static final int X_COORD = 0;
    private static final int Y_COORD = 1;

    private static final int CONTROL_POINT_NONE = -1;

    Tutorial(AmanithVG vgInstance) {

        int i;

        vg = vgInstance;
        controlPoint = null;
        imageBounds = null;
        girlPaths = new VGPath[GirlData.girlPathsData.length];
        for (i = 0; i < GirlData.girlPathsData.length; ++i) {
            girlPaths[i] = null;
        }
        solidCol = null;
        girlImage = null;
        imageWidth = 0;
        imageHeight = 0;
        imageFormat = VG_sRGBA_8888_PRE;
        imageControlPoints = new PointF[4];
        for (i = 0; i < 4; ++i) {
            imageControlPoints[i] = new PointF(0.0f, 0.0f);
        }
        controlPointsRadius = 14.0f;
        pickedControlPoint = CONTROL_POINT_NONE;
        mustUpdatePaths = false;
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

    private void genPaints() {

        // create a color paint, used to draw girl paths and image control points
        solidCol = vg.vgCreatePaint();
        vg.vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    }

    private void genPaths() {

        int i;

        // create girl paths
        for (i = 0; i < GirlData.girlPathsData.length; ++i) {
            girlPaths[i] = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
            vg.vgAppendPathData(girlPaths[i], GirlData.girlPathsData[i].nCommands, GirlData.girlPathsData[i].cmds, GirlData.girlPathsData[i].coords);
            // remove all capabilities, in order to free some memory
            vg.vgRemovePathCapabilities(girlPaths[i], VG_PATH_CAPABILITY_ALL);
        }

        // create the draggable gradient point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
        // create image bounds
        imageBounds = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
    }

    private void genImageBounds() {

        float[] boundsCoords = new float[] {
            imageControlPoints[0].x, imageControlPoints[0].y,
            imageControlPoints[1].x, imageControlPoints[1].y,
            imageControlPoints[2].x, imageControlPoints[2].y,
            imageControlPoints[3].x, imageControlPoints[3].y
        };

        vg.vgClearPath(imageBounds, VG_PATH_CAPABILITY_ALL);
        vg.vguPolygon(imageBounds, boundsCoords, 4, true);
    }

    private void genImage(int surfaceWidth,
                          int surfaceHeight) {

        int i;
        // opaque white
        float[] white = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        // find a suitable uniform scale
        float sx = (float)surfaceWidth / 620.0f;
        float sy = (float)surfaceHeight / 754.0f;
        float s = 0.80f * ((sx < sy) ? sx : sy);

        // calculate image dimensions
        imageWidth = (int)(620.0f * s);
        imageHeight = (int)(754.0f * s);

        // destroy previous image
        vg.vgDestroyImage(girlImage);
        girlImage = null;
        // create image again, using the same format of drawing surface (in order to speedup grabbing and rendering)
        girlImage = vg.vgCreateImage(imageFormat, imageWidth, imageHeight, VG_IMAGE_QUALITY_BETTER);
        // clear the whole drawing surface
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
        vg.vgSetfv(VG_CLEAR_COLOR, 4, white);
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);
        // generate the image at the lower-left origin (0, 0) of the surface
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgScale(s, s);
        vg.vgTranslate(0.0f, 752.0f);
        vg.vgScale(1.0f, -1.0f);
        vg.vgTranslate(-47.0f, 1.0f);
        vg.vgSetPaint(solidCol, VG_FILL_PATH);
        // draw all the paths
        for (i = 0; i < GirlData.girlPathsData.length; ++i) {
            vg.vgSetColor(solidCol, GirlData.girlPathsData[i].color);
            vg.vgDrawPath(girlPaths[i], VG_FILL_PATH);
        }
        vg.vgGetPixels(girlImage, 0, 0, 0, 0, imageWidth, imageHeight);
        // reset control points as a centered rectangular region (with the same image dimensions)
        imageControlPoints[0].x = (float)((surfaceWidth - imageWidth) / 2);
        imageControlPoints[0].y = (float)((surfaceHeight - imageHeight) / 2);
        imageControlPoints[1].x = imageControlPoints[0].x + (float)imageWidth;
        imageControlPoints[1].y = imageControlPoints[0].y;
        imageControlPoints[2].x = imageControlPoints[1].x;
        imageControlPoints[2].y = imageControlPoints[0].y + (float)imageHeight;
        imageControlPoints[3].x = imageControlPoints[0].x;
        imageControlPoints[3].y = imageControlPoints[2].y;
        // generate image bounds path
        genImageBounds();
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
        imageFormat = preferredImageFormat;

        // generate paths
        genPaths();
        // generate paints
        genPaints();
        // generate girl image
        genImage(surfaceWidth, surfaceHeight);
        // set some default parameters for the OpenVG context
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
        vg.vgSeti(VG_STROKE_JOIN_STYLE, VG_JOIN_BEVEL);
        vg.vgSeti(VG_STROKE_CAP_STYLE, VG_CAP_BUTT);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSeti(VG_IMAGE_QUALITY, VG_IMAGE_QUALITY_BETTER);
        vg.vgSeti(VG_IMAGE_MODE, VG_DRAW_IMAGE_NORMAL);
    }

    void destroy() {

        int i;

        // release paths
        for (i = 0; i < GirlData.girlPathsData.length; ++i) {
            vg.vgDestroyPath(girlPaths[i]);
        }
        vg.vgDestroyPath(controlPoint);
        vg.vgDestroyPath(imageBounds);
        // release paints
        vg.vgSetPaint(null, VG_FILL_PATH | VG_STROKE_PATH);
        vg.vgDestroyPaint(solidCol);
        // release girl image
        vg.vgDestroyImage(girlImage);
    }

    void resize(int surfaceWidth,
                int surfaceHeight) {

        // re-generate image, taking care of new surface dimensions
        genImage(surfaceWidth, surfaceHeight);
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    void draw(int surfaceWidth,
              int surfaceHeight) {

        int i;
        float warpMatrix[] = new float[9];
        float[] clearColor = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

        if (mustUpdatePaths) {
            mustUpdatePaths = false;
            genImageBounds();
        }

        // clear the whole drawing surface
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // calculate warp matrix
        vg.vguComputeWarpQuadToQuad(// destination
                                    imageControlPoints[0].x, imageControlPoints[0].y,
                                    imageControlPoints[1].x, imageControlPoints[1].y,
                                    imageControlPoints[2].x, imageControlPoints[2].y,
                                    imageControlPoints[3].x, imageControlPoints[3].y,
                                    // source
                                    0.0f, 0.0f,
                                    (float)imageWidth, 0.0f,
                                    (float)imageWidth, (float)imageHeight,
                                    0.0f, (float)imageHeight,
                                    // the output matrix
                                    warpMatrix);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_IMAGE_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgLoadMatrix(warpMatrix);
        // draw girl image
        vg.vgDrawImage(girlImage);

        // draw image bounds
        vg.vgSetColor(solidCol, 0x000000FF);
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgDrawPath(imageBounds, VG_STROKE_PATH);

        // draw image control points
        for (i = 0; i < 4; ++i) {
            vg.vgLoadIdentity();
            vg.vgTranslate(imageControlPoints[i].x, imageControlPoints[i].y);
            vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        }
    }

    /*****************************************************************
                            interactive options
    *****************************************************************/

    /*****************************************************************
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        int i, closestPoint = -1;
        float minDist = 3.402823466e+38f;

        for (i = 0; i < 4; ++i) {
            // take i-thm control point and calculate its distance from mouse position
            float dist = distance(x, y, imageControlPoints[i].x, imageControlPoints[i].y);
            if (dist < minDist) {
                minDist = dist;
                closestPoint = i;
            }
        }
        // check if we have picked a control point
        pickedControlPoint = ((closestPoint >= 0) && (minDist < controlPointsRadius * 1.1f)) ? closestPoint : CONTROL_POINT_NONE;
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
                // set the new position for the selected control point
                imageControlPoints[pickedControlPoint].x = x;
                imageControlPoints[pickedControlPoint].y = y;
                // we update paths within the 'draw' method, in order to
                // stick to the rendering thread
                mustUpdatePaths = true;
            }
        }
        // keep track of current touch position
        oldTouchX = x;
        oldTouchY = y;
    }

    void touchDoubleTap(float x,
                        float y) {
    }
}
