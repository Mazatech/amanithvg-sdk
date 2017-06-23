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
package com.mazatech.amanithvg.tutorial06;

import android.graphics.PointF;

import java.util.Random;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11Ext.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private AmanithVG vg;
    // path objects
    private VGPath strokedFlower;
    private VGPath controlPoint;
    private VGPath controlPolygon;
    private float controlPointsRadius;
    // paint objects
    private VGPaint solidCol;
    // current stroke configuration
    private int dashPattern;
    private float dashPhase;
    private boolean dashAnimate;
    private int joinStyle;
    private int startCapStyle;
    private int endCapStyle;
    private boolean separableCapsSupported;
    // keep track of "path user to surface" transformation
    private float userToSurfaceScale;
    private float[] userToSurfaceTranslation;
    private PointF[] controlPoints;
    private int pickedControlPoint;
    // random numbers generator
    Random rnd;
    // touch state
    float oldTouchX;
    float oldTouchY;
    int touchState;

    private static final int TOUCH_MODE_NONE = 0;
    private static final int TOUCH_MODE_DOWN = 1;

    private static final int X_COORD = 0;
    private static final int Y_COORD = 1;

    private static final int CONTROL_POINT_NONE = -1;

    // OpenVG path commands used to build the 'strokedFlower' path
    private static final byte[] flowerCmds = {
        VG_MOVE_TO_ABS,
        VG_CUBIC_TO_ABS,
        VG_CUBIC_TO_ABS,
        VG_CUBIC_TO_ABS,
        VG_CUBIC_TO_ABS,
        VG_CLOSE_PATH
    };
    // OpenVG path commands used to build the 'controlPolygon' path
    private static final byte[] polygonCmds = {
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
    };

    private static final float[] noDash = { 0 };

    // dash patterns
    private static final float[][] dashPatterns = {
        { 30.0f, 30.0f,  5.0f, 45.0f },
        { 15.0f, 40.0f, 15.0f, 40.0f },
        { 20.0f, 25.0f, 20.0f, 45.0f },
        {  5.0f, 45.0f, 35.0f, 25.0f }
    };

    Tutorial(AmanithVG vgInstance) {

        vg = vgInstance;
        dashPattern = 0;
        dashPhase = 0.0f;
        dashAnimate = false;
        joinStyle = VG_JOIN_MITER;
        startCapStyle = VG_CAP_ROUND;
        endCapStyle = VG_CAP_ROUND;
        separableCapsSupported = false;
        userToSurfaceScale = 1.0f;
        userToSurfaceTranslation = new float[] { 0.0f, 0.0f };
        controlPoints = new PointF[12];
        for (int i = 0; i < 12; ++i) {
            controlPoints[i] = new PointF(0.0f, 0.0f);
        }
        pickedControlPoint = CONTROL_POINT_NONE;
        controlPointsRadius = 14.0f;
        rnd = new Random();
        oldTouchX = 0.0f;
        oldTouchY = 0.0f;
        touchState = TOUCH_MODE_NONE;
    }

    private void extensionsCheck() {

        String extensions = vg.vgGetString(VG_EXTENSIONS);

        // check OpenVG extensions
        separableCapsSupported = extensions.contains("VG_MZT_separable_cap_style");
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

    // calculate "path user to surface" transformation
    private void userToSurfaceCalc(int surfaceWidth,
                                   int surfaceHeight) {

        int halfDim = (surfaceWidth < surfaceHeight) ? (surfaceWidth / 2) : (surfaceHeight / 2);

        userToSurfaceScale = ((float)halfDim / 256.0f) * 0.9f;
        userToSurfaceTranslation[X_COORD] = (float)(surfaceWidth / 2);
        userToSurfaceTranslation[Y_COORD] = (float)(surfaceHeight / 2);
    }

    // calculate the position of a control point, in surface space
    private void controlPointGet(final float[] userSpaceControlPoint,
                                 float[] srfControlPoint) {

        // apply the "path user to surface" transformation
        srfControlPoint[X_COORD] = (userSpaceControlPoint[X_COORD] * userToSurfaceScale) + userToSurfaceTranslation[X_COORD];
        srfControlPoint[Y_COORD] = (userSpaceControlPoint[Y_COORD] * userToSurfaceScale) + userToSurfaceTranslation[Y_COORD];
    }

    // set the position of a control point, in surface space
    private void controlPointSet(final float[] srfControlPoint,
                                 float[] userSpaceControlPoint) {

        // apply the inverse "path user to surface" transformation
        userSpaceControlPoint[X_COORD] = (srfControlPoint[X_COORD] - userToSurfaceTranslation[X_COORD]) / userToSurfaceScale;
        userSpaceControlPoint[Y_COORD] = (srfControlPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD]) / userToSurfaceScale;
    }

    // reset control points
    private void controlPointsReset(final int surfaceWidth,
                                    final int surfaceHeight) {

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

    private void setDashPattern(int pattern) {

        if (pattern == 0) {
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 0, noDash);
        }
        else {
            vg.vgSetfv(VG_STROKE_DASH_PATTERN, 4, dashPatterns[pattern - 1]);
            vg.vgSetf(VG_STROKE_DASH_PHASE, dashPhase);
        }
    }

    private void genPaints() {

        // create a color paint, used to draw stroke, control points, and control polygon
        solidCol = vg.vgCreatePaint();
        vg.vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
    }

    private void genPaths() {

        // create path that will be stroked
        strokedFlower = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        // create the path control polygon
        controlPolygon = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        // create the draggable gradient point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
    }

    // given the current control points, update all involved OpenVG paths
    private void updatePaths() {

        int i;
        float pathCoords[] = new float[28];

        // update the stroked flower
        vg.vgClearPath(strokedFlower, VG_PATH_CAPABILITY_ALL);
        for (i = 0; i < 12; ++i) {
            pathCoords[i * 2] = controlPoints[i].x;
            pathCoords[(i * 2) + 1] = controlPoints[i].y;
        }
        pathCoords[i * 2] = controlPoints[0].x;
        pathCoords[(i * 2) + 1] = controlPoints[0].y;
        vg.vgAppendPathData(strokedFlower, 6, flowerCmds, pathCoords);

        // update control polygon
        vg.vgClearPath(controlPolygon, VG_PATH_CAPABILITY_ALL);
        for (i = 0; i < 12; ++i) {
            pathCoords[i * 2] = controlPoints[i].x;
            pathCoords[(i * 2) + 1] = controlPoints[i].y;
        }
        vg.vgAppendPathData(controlPolygon, 14, polygonCmds, pathCoords);
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
        vg.vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    }

    void destroy() {

        // release paths
        vg.vgDestroyPath(strokedFlower);
        vg.vgDestroyPath(controlPoint);
        vg.vgDestroyPath(controlPolygon);
        // release paints
        vg.vgSetPaint(null, VG_FILL_PATH | VG_STROKE_PATH);
        vg.vgDestroyPaint(solidCol);
    }

    void resize(int surfaceWidth,
                int surfaceHeight) {

        // calculate "path user to surface" transformation
        userToSurfaceCalc(surfaceWidth, surfaceHeight);
        // reset control points
        controlPointsReset(surfaceWidth, surfaceHeight);
        // given the current control points, update all involved OpenVG paths
        updatePaths();
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    void draw(int surfaceWidth,
              int surfaceHeight) {

        int i;

        // animate dash phase, if needed
        if (dashAnimate && (dashPattern != 0)) {
            dashPhase += 0.3f;
            if (dashPhase >= 110.0f) {
                dashPhase = 0.0f;
            }
        }

        // clear the whole drawing surface
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // calculate "path user to surface" transformation
        userToSurfaceCalc(surfaceWidth, surfaceHeight);
        // upload "path user to surface" transformation to the OpenVG backend
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(userToSurfaceTranslation[X_COORD], userToSurfaceTranslation[Y_COORD]);
        vg.vgScale(userToSurfaceScale, userToSurfaceScale);

        // draw stroked flower path
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 12.0f);
        vg.vgSeti(VG_STROKE_JOIN_STYLE, joinStyle);
        if (separableCapsSupported) {
            vg.vgSeti(VG_STROKE_START_CAP_STYLE_MZT, startCapStyle);
            vg.vgSeti(VG_STROKE_END_CAP_STYLE_MZT, endCapStyle);
        }
        else {
            vg.vgSeti(VG_STROKE_CAP_STYLE, startCapStyle);
        }

        setDashPattern(dashPattern);
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgSetColor(solidCol, 0xFFBF0DFF);
        vg.vgDrawPath(strokedFlower, VG_STROKE_PATH);

        // set an opaque white stroke
        vg.vgSetColor(solidCol, 0xFFFFFFFF);
        // draw control polygon path: because the stroke will be scaled according to the "path user to surface"
        // transformation, we adjust the stroke line width in order to have always a 2 pixels wide outline
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f / userToSurfaceScale);
        vg.vgSeti(VG_STROKE_JOIN_STYLE, VG_JOIN_MITER);
        vg.vgSeti(VG_STROKE_CAP_STYLE, VG_CAP_BUTT);
        setDashPattern(0);
        // draw control polygon path
        vg.vgDrawPath(controlPolygon, VG_STROKE_PATH);

        // draw control points
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
        for (i = 0; i < 12; ++i) {
            float srfSpacePoint[] = new float[2];
            float userSpacePoint[] = new float[] { controlPoints[i].x, controlPoints[i].y };
            // get control point position, in surface space
            controlPointGet(userSpacePoint, srfSpacePoint);
            vg.vgLoadIdentity();
            vg.vgTranslate(srfSpacePoint[X_COORD], srfSpacePoint[Y_COORD]);
            // draw a single control point
            vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        }
    }

    /*****************************************************************
                            interactive options
    *****************************************************************/
    void toggleJoinStyle() {

        int newStyle;

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

    void toggleStartCapStyle() {

        if (dashPattern != 0) {
            int newStyle;
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

    void toggleEndCapStyle() {

        if (dashPattern != 0) {
            if (separableCapsSupported) {
                int newStyle;
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

    void toggleDash() {

        dashPattern++;
        if (dashPattern > 4) {
            dashPattern = 0;
        }
    }

    void toggleDashAnimation() {

        dashAnimate = !dashAnimate;
    }

    private void randomStroke() {

        dashPattern = Math.abs(rnd.nextInt()) % 5;
        joinStyle = (Math.abs(rnd.nextInt()) % 3) + VG_JOIN_MITER;
        startCapStyle = (Math.abs(rnd.nextInt()) % 3) + VG_CAP_BUTT;
        endCapStyle = (Math.abs(rnd.nextInt()) % 3) + VG_CAP_BUTT;
    }

    /*****************************************************************
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        int i, closestPoint = -1;
        float minDist = 3.402823466e+38f;

        for (i = 0; i < 12; ++i) {
            float dist;
            float srfSpacePoint[] = new float[2];
            float userSpacePoint[] = new float[] { controlPoints[i].x, controlPoints[i].y };
            // get i-thm control point, in surface space
            controlPointGet(userSpacePoint, srfSpacePoint);
            // get its distance from mouse position
            dist = distance(x, y, srfSpacePoint[X_COORD], srfSpacePoint[Y_COORD]);
            if (dist < minDist) {
                minDist = dist;
                closestPoint = i;
            }
        }
        // check if we have picked a control point
        pickedControlPoint = ((closestPoint >= 0) && (minDist < controlPointsRadius)) ? closestPoint : CONTROL_POINT_NONE;
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
                float userSpacePoint[] = new float[2];
                float srfSpacePoint[] = new float[] { x, y };
                // set the new position for the selected control point
                controlPointSet(srfSpacePoint, userSpacePoint);
                controlPoints[pickedControlPoint].x = userSpacePoint[X_COORD];
                controlPoints[pickedControlPoint].y = userSpacePoint[Y_COORD];
                updatePaths();
            }
        }

        // keep track of current touch position
        oldTouchX = x;
        oldTouchY = y;
    }

    void touchDoubleTap(float x,
                        float y) {

        randomStroke();
    }
}
