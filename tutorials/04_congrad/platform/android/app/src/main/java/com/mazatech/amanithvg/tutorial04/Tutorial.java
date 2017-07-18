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
package com.mazatech.amanithvg.tutorial04;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11Ext.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private AmanithVG vg;
    // path objects
    private VGPath filledCircle;
    private VGPath controlPoint;
    // paint objects
    private VGPaint solidCol;
    private VGPaint conGrad;
    // conical gradient parameters
    private float[] conGradCenter;
    private float[] conGradTarget;
    private float conGradRepeats;
    // current paint states
    private boolean linearInterpolation;
    private boolean smoothRampSupported;
    private int spreadMode;
    // keep track of "path user to surface" transformation
    private float userToSurfaceScale;
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

    Tutorial(AmanithVG vgInstance) {

        vg = vgInstance;
        filledCircle = null;
        controlPoint = null;
        solidCol = null;
        conGrad = null;
        conGradCenter = new float[] { 0.0f, 0.0f };
        conGradTarget = new float[] { 0.0f, 0.0f };
        conGradRepeats = 2.0f;
        linearInterpolation = true;
        smoothRampSupported = false;
        spreadMode = VG_COLOR_RAMP_SPREAD_PAD;
        userToSurfaceScale = 1.0f;
        userToSurfaceTranslation = new float[] { 0.0f, 0.0f };
        controlPointsRadius = 14.0f;
        pickedControlPoint = CONTROL_POINT_NONE;
        oldTouchX = 0.0f;
        oldTouchY = 0.0f;
        touchState = TOUCH_MODE_NONE;
    }

    private void extensionsCheck() {

        String extensions = vg.vgGetString(VG_EXTENSIONS);

        // check OpenVG extensions
        smoothRampSupported = extensions.contains("VG_MZT_color_ramp_interpolation");
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

        userToSurfaceScale = (float)halfDim * 0.9f;
        userToSurfaceTranslation[X_COORD] = (float)(surfaceWidth / 2);
        userToSurfaceTranslation[Y_COORD] = (float)(surfaceHeight / 2);
    }

    // calculate the position of conical gradient control points, in surface space
    private void gradientParamsGet(float[] srfCenterPoint,
                                   float[] srfTargetPoint) {

        // start point (apply the "path user to surface" transformation)
        srfCenterPoint[X_COORD] = (conGradCenter[X_COORD] * userToSurfaceScale) + userToSurfaceTranslation[X_COORD];
        srfCenterPoint[Y_COORD] = (conGradCenter[Y_COORD] * userToSurfaceScale) + userToSurfaceTranslation[Y_COORD];
        // target point (apply the "path user to surface" transformation)
        srfTargetPoint[X_COORD] = (conGradTarget[X_COORD] * userToSurfaceScale) + userToSurfaceTranslation[X_COORD];
        srfTargetPoint[Y_COORD] = (conGradTarget[Y_COORD] * userToSurfaceScale) + userToSurfaceTranslation[Y_COORD];
    }

    // set the position of conical gradient control points, in surface space
    private void gradientParamsSet(final float[] srfCenterPoint,
                                   final float[] srfTargetPoint) {

        // conical gradient parameters
        float conGradParams[] = new float[5];

        // apply the inverse "path user to surface" transformation
        conGradCenter[X_COORD] = (srfCenterPoint[X_COORD] - userToSurfaceTranslation[X_COORD]) / userToSurfaceScale;
        conGradCenter[Y_COORD] = (srfCenterPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD]) / userToSurfaceScale;
        conGradTarget[X_COORD] = (srfTargetPoint[X_COORD] - userToSurfaceTranslation[X_COORD]) / userToSurfaceScale;
        conGradTarget[Y_COORD] = (srfTargetPoint[Y_COORD] - userToSurfaceTranslation[Y_COORD]) / userToSurfaceScale;

        // upload new gradient parameters to the OpenVG backend
        conGradParams[0] = conGradCenter[X_COORD];
        conGradParams[1] = conGradCenter[Y_COORD];
        conGradParams[2] = conGradTarget[X_COORD];
        conGradParams[3] = conGradTarget[Y_COORD];
        conGradParams[4] = conGradRepeats;
        vg.vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
    }

    // reset gradient parameters
    private void gradientParamsReset(final int surfaceWidth,
                                     final int surfaceHeight) {

        float[] gradCenter = new float[] { (float)surfaceWidth * 0.5f, (float)surfaceHeight * 0.5f };
        float[] gradTarget = new float[] { (float)surfaceWidth * 0.75f, (float)surfaceHeight * 0.5f };
        gradientParamsSet(gradCenter, gradTarget);
    }

    private void genPaints() {

        float white[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        // linear gradient color keys
        float colKeys[] = new float[] {
            0.00f, 0.40f, 0.00f, 0.60f, 1.00f,
            0.25f, 0.90f, 0.50f, 0.10f, 1.00f,
            0.50f, 0.80f, 0.80f, 0.00f, 1.00f,
            0.75f, 0.00f, 0.30f, 0.50f, 1.00f,
            1.00f, 0.40f, 0.00f, 0.60f, 1.00f
        };
        // create a white color paint, used to draw gradient control points
        solidCol = vg.vgCreatePaint();
        vg.vgSetParameteri(solidCol, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
        vg.vgSetParameterfv(solidCol, VG_PAINT_COLOR, 4, white);
        // create conical gradient
        conGrad = vg.vgCreatePaint();
        vg.vgSetParameteri(conGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_CONICAL_GRADIENT_MZT);
        vg.vgSetParameterfv(conGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
        vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
        if (smoothRampSupported) {
            vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, linearInterpolation ? VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT : VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
        }
    }

    private void genPaths() {

        // create the circle that will be filled by the conical gradient paint
        filledCircle = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(filledCircle, 0.0f, 0.0f, 2.0f, 2.0f);
        // create the draggable gradient point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
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
        // reset gradient parameters
        gradientParamsReset(surfaceWidth, surfaceHeight);
        // set some default parameters for the OpenVG context
        vg.vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 2.0f);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);
    }

    void destroy() {

        // release paths
        vg.vgDestroyPath(filledCircle);
        vg.vgDestroyPath(controlPoint);
        // release paints
        vg.vgSetPaint(null, VG_FILL_PATH | VG_STROKE_PATH);
        vg.vgDestroyPaint(solidCol);
        vg.vgDestroyPaint(conGrad);
    }

    void resize(int surfaceWidth,
                int surfaceHeight) {

        // calculate "path user to surface" transformation
        userToSurfaceCalc(surfaceWidth, surfaceHeight);
        // reset gradient parameters
        gradientParamsReset(surfaceWidth, surfaceHeight);
        pickedControlPoint = CONTROL_POINT_NONE;
    }

    void draw(int surfaceWidth,
              int surfaceHeight) {

        float gradCenter[] = new float[2];
        float gradTarget[] = new float[2];

        // clear the whole drawing surface
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // calculate "path user to surface" transformation
        userToSurfaceCalc(surfaceWidth, surfaceHeight);
        // upload "path user to surface" transformation to the OpenVG backend
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(userToSurfaceTranslation[X_COORD], userToSurfaceTranslation[Y_COORD]);
        vg.vgScale(userToSurfaceScale, userToSurfaceScale);
        // draw the filled circle
        vg.vgSetPaint(conGrad, VG_FILL_PATH);
        vg.vgDrawPath(filledCircle, VG_FILL_PATH);

        // calculate the position of conical gradient control points, in surface space
        gradientParamsGet(gradCenter, gradTarget);

        // draw conical gradient control points
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(gradCenter[X_COORD], gradCenter[Y_COORD]);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(gradTarget[X_COORD], gradTarget[Y_COORD]);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
    }

    /*****************************************************************
                            interactive options
    *****************************************************************/
    void toggleColorInterpolation() {

        if (smoothRampSupported) {
            if (linearInterpolation) {
                linearInterpolation = false;
                // upload new parameters to the OpenVG backend
                vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
            }
            else {
                linearInterpolation = true;
                // upload new parameters to the OpenVG backend
                vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
            }
        }
    }

    void toggleSpreadMode() {

        if (spreadMode == VG_COLOR_RAMP_SPREAD_PAD) {
            spreadMode = VG_COLOR_RAMP_SPREAD_REPEAT;
        }
        else
        if (spreadMode == VG_COLOR_RAMP_SPREAD_REPEAT) {
            spreadMode = VG_COLOR_RAMP_SPREAD_REFLECT;
        }
        else {
            spreadMode = VG_COLOR_RAMP_SPREAD_PAD;
        }

        // upload the new spread mode to the OpenVG backend
        vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
    }

    /*****************************************************************
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        float distCenter, distTarget;
        float gradCenter[] = new float[2];
        float gradTarget[] = new float[2];

        // get current gradient parameters
        gradientParamsGet(gradCenter, gradTarget);
        distCenter = distance(x, y, gradCenter[X_COORD], gradCenter[Y_COORD]);
        distTarget = distance(x, y, gradTarget[X_COORD], gradTarget[Y_COORD]);
        // check if we have picked a gradient control point
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
                // get current gradient parameters
                float gradCenter[] = new float[2];
                float gradTarget[] = new float[2];
                gradientParamsGet(gradCenter, gradTarget);
                // assign the new control point position
                if (pickedControlPoint == CONTROL_POINT_CENTER) {
                    gradCenter[X_COORD] = x;
                    gradCenter[Y_COORD] = y;
                }
                else {
                    gradTarget[X_COORD] = x;
                    gradTarget[Y_COORD] = y;
                }
                // update gradient parameters
                gradientParamsSet(gradCenter, gradTarget);
            }
        }
        // keep track of current touch position
        oldTouchX = x;
        oldTouchY = y;
    }

    void touchDoubleTap(float x,
                        float y) {

        toggleSpreadMode();
    }
}
