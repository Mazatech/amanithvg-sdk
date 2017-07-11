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
package com.mazatech.amanithvg.tutorial09;

import android.graphics.PointF;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private AmanithVG vg;
    // path objects
    private VGPath textLine;
    private VGPath textCurve;
    private VGPath textCurveControlPolygon;
    private VGPath controlPoint;
    // paint objects
    private VGPaint solidCol;
    // straight text parameters
    private static final String straightText = "AmanithVG engine";
    private float straightTextFontSize;
    // wavy text parameters
    private static final String wavyText = "OpenVG text example";
    private float wavyTextFontSize;
    // control points (2 for straight text, 4 for wavy text)
    private PointF[] controlPoints;
    private float controlPointsRadius;
    private int pickedControlPoint;
    boolean mustUpdatePathsAndFont;
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

        vg = vgInstance;
        textLine = null;
        textCurve = null;
        textCurveControlPolygon = null;
        controlPoint = null;
        solidCol = null;
        straightTextFontSize = 0.0f;
        wavyTextFontSize = 0.0f;
        controlPoints = new PointF[6];
        for (int i = 0; i < 6; ++i) {
            controlPoints[i] = new PointF(0.0f, 0.0f);
        }
        controlPointsRadius = 14.0f;
        pickedControlPoint = CONTROL_POINT_NONE;
        mustUpdatePathsAndFont = false;
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

        // create color paint, used to draw text and control points
        solidCol = vg.vgCreatePaint();
        vg.vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    }

    private void genPaths() {

        // create the straight text underline
        textLine = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        // create the text curve
        textCurve = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        // create the text curve control polygon
        textCurveControlPolygon = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        // create the draggable control point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
    }

    // given the current control points, update all involved OpenVG paths
    private void updatePaths() {

        byte[] textCurveCmds = new byte[] {
            VG_MOVE_TO,
            VG_CUBIC_TO_ABS
        };

        float[] textCurveCoords = new float[] {
            controlPoints[2].x, controlPoints[2].y,
            controlPoints[3].x, controlPoints[3].y,
            controlPoints[4].x, controlPoints[4].y,
            controlPoints[5].x, controlPoints[5].y
        };

        // straight text underline
        vg.vgClearPath(textLine, VG_PATH_CAPABILITY_ALL);
        vg.vguLine(textLine, controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y);
        // wavy text path and control polygon
        vg.vgClearPath(textCurve, VG_PATH_CAPABILITY_ALL);
        vg.vgAppendPathData(textCurve, 2, textCurveCmds, textCurveCoords);
        vg.vgClearPath(textCurveControlPolygon, VG_PATH_CAPABILITY_ALL);
        vg.vguPolygon(textCurveControlPolygon, textCurveCoords, 4, false);
    }

    private void textSizeUpdate() {

        float objSpaceWidth, srfSpaceLen;

        // calculate the text width, in object space
        objSpaceWidth = FontCommon.textLineWidth(vg, VeraFont.font, straightText);
        // calculate the text width, in surface space
        srfSpaceLen = distance(controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y);
        // calculate font size (avoiding division by zero)
        straightTextFontSize = srfSpaceLen / (objSpaceWidth + 0.001f);
        if (straightTextFontSize < 1.0f) {
            straightTextFontSize = 1.0f;
        }
        
        // calculate the text width, in object space
        objSpaceWidth = FontCommon.textLineWidth(vg, VeraFont.font, wavyText);
        // calculate the text width, in surface space
        srfSpaceLen = vg.vgPathLength(textCurve, 0, vg.vgGetParameteri(textCurve, VG_PATH_NUM_SEGMENTS));
        // calculate font size (avoiding division by zero)
        wavyTextFontSize = srfSpaceLen / (objSpaceWidth + 0.001f);
        if (wavyTextFontSize < 1.0f) {
            wavyTextFontSize = 1.0f;
        }
    }

    // reset text parameters (control points and font size), according to the specified drawing surface dimensions
    private void textParametersReset(final int surfaceWidth,
                                     final int surfaceHeight) {

        // straight text parameters
        controlPoints[0].x = (float)surfaceWidth * 0.1f;
        controlPoints[0].y = (float)surfaceHeight * 0.8f;
        controlPoints[1].x = (float)surfaceWidth * 0.9f;
        controlPoints[1].y = controlPoints[0].y;

        // wavy text parameters
        controlPoints[2].x = (float)surfaceWidth * 0.1f;
        controlPoints[2].y = (float)surfaceHeight * 0.2f;
        controlPoints[3].x = (float)surfaceWidth * 0.35f;
        controlPoints[3].y = (float)surfaceHeight * 0.6f;
        controlPoints[4].x = (float)surfaceWidth * 0.65f;
        controlPoints[4].y = controlPoints[2].y;
        controlPoints[5].x = (float)surfaceWidth * 0.9f;
        controlPoints[5].y = controlPoints[3].y;

        // update OpenVG paths
        updatePaths();
        // calculate font size, according to control points
        textSizeUpdate();
    }

    void init(int surfaceWidth,
              int surfaceHeight) {

        // an opaque dark grey
        float clearColor[] = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };

        // make sure to have well visible (and draggable) control points
        controlPointsRadius = ((float)Math.min(surfaceWidth, surfaceHeight) / 512.0f) * 14.0f;
        if (controlPointsRadius < 14.0f) {
            controlPointsRadius = 14.0f;
        }

        // generate paths
        genPaths();
        // generate paints
        genPaints();
        // generate fonts
        VeraFont.init(vg);
        // reset text parameters (control points and font size)
        textParametersReset(surfaceWidth, surfaceHeight);
        // set some default parameters for the OpenVG context
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    }

    void destroy() {

        // release paths
        vg.vgDestroyPath(textCurve);
        vg.vgDestroyPath(textCurveControlPolygon);
        vg.vgDestroyPath(controlPoint);
        // release paints
        vg.vgSetPaint(null, VG_FILL_PATH | VG_STROKE_PATH);
        vg.vgDestroyPaint(solidCol);
        // release fonts
        VeraFont.destroy(vg);
    }

    void resize(int surfaceWidth,
                int surfaceHeight) {

        // reset text parameters (control points and font size)
        textParametersReset(surfaceWidth, surfaceHeight);
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    private void straightTextDraw() {

        int i;
        float[] glyphOrigin = new float[] { 0.0f, 0.0f };
        float[] straightTextDir = new float[] {
            controlPoints[1].x - controlPoints[0].x,
            controlPoints[1].y - controlPoints[0].y
        };
        // calculate text rotation (radians)
        float rotation = (float)Math.atan2(straightTextDir[Y_COORD], straightTextDir[X_COORD]);

        vg.vgSetPaint(solidCol, VG_FILL_PATH);
        vg.vgSetColor(solidCol, 0x2C92FAFF);
        vg.vgSeti(VG_FILL_RULE, VG_NON_ZERO);
        // draw straight text
        vg.vgSetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_GLYPH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(controlPoints[0].x, controlPoints[0].y);
        vg.vgRotate(rotation * 57.2957795f);
        vg.vgScale(straightTextFontSize, straightTextFontSize);
        FontCommon.textLineDraw(vg, VeraFont.font, straightText, VG_FILL_PATH);

        // draw underline
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgSetColor(solidCol, 0xFFFFFFFF);
        vg.vgLoadIdentity();
        vg.vgDrawPath(textLine, VG_STROKE_PATH);

        // draw control points
        for (i = 0; i < 2; ++i) {
            vg.vgLoadIdentity();
            vg.vgTranslate(controlPoints[i].x, controlPoints[i].y);
            vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        }
    }

    private void wavyTextDraw() {

        int i;

        // draw wavy text
        vg.vgSetPaint(solidCol, VG_FILL_PATH);
        vg.vgSetColor(solidCol, 0xFFBF0DFF);
        FontCommon.textAlongPathDraw(vg, VeraFont.font, textCurve, wavyText, wavyTextFontSize, VG_FILL_PATH);

        // draw trail path
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgSetColor(solidCol, 0xFFFFFFFF);
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgDrawPath(textCurve, VG_STROKE_PATH);
        vg.vgDrawPath(textCurveControlPolygon, VG_STROKE_PATH);

        // draw control points
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        for (i = 2; i < 6; ++i) {
            vg.vgLoadIdentity();
            vg.vgTranslate(controlPoints[i].x, controlPoints[i].y);
            vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        }
    }

    void draw(int surfaceWidth,
              int surfaceHeight) {

        if (mustUpdatePathsAndFont) {
            mustUpdatePathsAndFont = false;
            // update OpenVG paths
            updatePaths();
            // update font size
            textSizeUpdate();
        }

        // clear the whole drawing surface
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);
        // draw straight text
        straightTextDraw();
        // draw wavy text
        wavyTextDraw();
    }

    /*****************************************************************
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        int i, closestPoint = -1;
        float minDist = 3.402823466e+38f;

        // loop over control points
        for (i = 0; i < 6; ++i) {
            // calculate distance from mouse position
            float dist = distance(x, y, controlPoints[i].x, controlPoints[i].y);
            // keep track of minimum distance
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
                // assign the new control point position
                controlPoints[pickedControlPoint].x = x;
                controlPoints[pickedControlPoint].y = y;
                // we update paths and font within the 'draw' method, in order to
                // stick to the rendering thread
                mustUpdatePathsAndFont = true;
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
