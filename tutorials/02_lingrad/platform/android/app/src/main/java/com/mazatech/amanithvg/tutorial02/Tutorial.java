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
package com.mazatech.amanithvg.tutorial02;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11Ext.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private final AmanithVG vg;
    // path objects
    private VGPath filledCircle;
    private VGPath controlPoint;
    // paint objects
    private VGPaint solidCol;
    private VGPaint linGrad;
    // linear gradient parameters
    private final PointF linGradStart;
    private final PointF linGradEnd;
    // keep track if new gradient parameters must be uploaded to the OpenVG backend
    private boolean updatePoints;
    // current paint states
    private boolean linearInterpolation;
    private boolean smoothRampSupported;
    private int spreadMode;
    // keep track of "path user to surface" transformation
    private float userToSurfaceScale;
    private final PointF userToSurfaceTranslation;
    private float controlPointsRadius;
    private int pickedControlPoint;
    // touch state
    private int touchState;

    private static final int TOUCH_MODE_NONE = 0;
    private static final int TOUCH_MODE_DOWN = 1;

    private static final int CONTROL_POINT_NONE = 0;
    private static final int CONTROL_POINT_START = 1;
    private static final int CONTROL_POINT_END = 2;

    Tutorial(final AmanithVG vgInstance) {

        vg = vgInstance;
        filledCircle = null;
        controlPoint = null;
        solidCol = null;
        linGrad = null;
        linGradStart = new PointF(0.0f, 0.0f);
        linGradEnd = new PointF(0.0f, 0.0f);
        updatePoints = false;
        linearInterpolation = true;
        smoothRampSupported = false;
        spreadMode = VG_COLOR_RAMP_SPREAD_PAD;
        userToSurfaceScale = 1.0f;
        userToSurfaceTranslation = new PointF(0.0f, 0.0f);
        controlPointsRadius = 14.0f;
        pickedControlPoint = CONTROL_POINT_NONE;
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
        userToSurfaceTranslation.set((float)(surfaceWidth / 2), (float)(surfaceHeight / 2));
    }

    // calculate the position of linear gradient control points, in surface space
    private void gradientParamsGet(@NonNull PointF srfStartPoint,
                                   @NonNull PointF srfEndPoint) {

        // start point (apply the "path user to surface" transformation)
        srfStartPoint.x = (linGradStart.x * userToSurfaceScale) + userToSurfaceTranslation.x;
        srfStartPoint.y = (linGradStart.y * userToSurfaceScale) + userToSurfaceTranslation.y;
        // end point (apply the "path user to surface" transformation)
        srfEndPoint.x = (linGradEnd.x * userToSurfaceScale) + userToSurfaceTranslation.x;
        srfEndPoint.y = (linGradEnd.y * userToSurfaceScale) + userToSurfaceTranslation.y;
    }

    // set the position of linear gradient control points, in surface space
    private void gradientParamsSet(@NonNull final PointF srfStartPoint,
                                   @NonNull final PointF srfEndPoint) {

        // apply the inverse "path user to surface" transformation
        linGradStart.x = (srfStartPoint.x - userToSurfaceTranslation.x) / userToSurfaceScale;
        linGradStart.y = (srfStartPoint.y - userToSurfaceTranslation.y) / userToSurfaceScale;
        linGradEnd.x = (srfEndPoint.x - userToSurfaceTranslation.x) / userToSurfaceScale;
        linGradEnd.y = (srfEndPoint.y - userToSurfaceTranslation.y) / userToSurfaceScale;

        // we need to upload new gradient parameters to the OpenVG backend
        // NB: must be performed within the rendering thread at the next 'draw' call
        updatePoints = true;
    }

    // reset gradient parameters
    private void gradientParamsReset(final int surfaceWidth,
                                     final int surfaceHeight) {

        PointF gradStart = new PointF((float)surfaceWidth * 0.25f, (float)surfaceHeight * 0.5f);
        PointF gradEnd = new PointF((float)surfaceWidth * 0.75f, (float)surfaceHeight * 0.5f);
        gradientParamsSet(gradStart, gradEnd);
    }

    // upload new gradient parameters to the OpenVG backend
    private void gradientParamsUpload() {

        // linear gradient parameters
        float[] linGradParams = new float[] {
            linGradStart.x,
            linGradStart.y,
            linGradEnd.x,
            linGradEnd.y
        };

        vg.vgSetParameterfv(linGrad, VG_PAINT_LINEAR_GRADIENT, 4, linGradParams);
    }

    private void genPaints() {

        float[] white = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        // linear gradient color keys
        float[] colKeys = new float[] {
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
        // create linear gradient
        linGrad = vg.vgCreatePaint();
        vg.vgSetParameteri(linGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_LINEAR_GRADIENT);
        vg.vgSetParameterfv(linGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
        vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
        if (smoothRampSupported) {
            vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, linearInterpolation ? VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT : VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
        }
    }

    private void genPaths() {

        // create the circle that will be filled by the linear gradient paint
        filledCircle = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(filledCircle, 0.0f, 0.0f, 2.0f, 2.0f);
        // create the draggable gradient point
        controlPoint = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vguEllipse(controlPoint, 0.0f, 0.0f, controlPointsRadius * 2.0f, controlPointsRadius * 2.0f);
    }

    void init(int surfaceWidth,
              int surfaceHeight) {

        // an opaque dark grey
        float[] clearColor = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };

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
        vg.vgDestroyPaint(linGrad);
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

        PointF gradStart = new PointF();
        PointF gradEnd = new PointF();

        // clear the whole drawing surface
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // upload new gradient parameters to the OpenVG backend, if needed
        if (updatePoints) {
            gradientParamsUpload();
            updatePoints = false;
        }

        // calculate "path user to surface" transformation
        userToSurfaceCalc(surfaceWidth, surfaceHeight);
        // upload "path user to surface" transformation to the OpenVG backend
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(userToSurfaceTranslation.x, userToSurfaceTranslation.y);
        vg.vgScale(userToSurfaceScale, userToSurfaceScale);
        // draw the filled circle
        vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
        vg.vgSetPaint(linGrad, VG_FILL_PATH);
        vg.vgDrawPath(filledCircle, VG_FILL_PATH);

        // calculate the position of linear gradient control points, in surface space
        gradientParamsGet(gradStart, gradEnd);

        // draw linear gradient control points
        vg.vgSetPaint(solidCol, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(gradStart.x, gradStart.y);
        vg.vgDrawPath(controlPoint, VG_STROKE_PATH);
        vg.vgLoadIdentity();
        vg.vgTranslate(gradEnd.x, gradEnd.y);
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
                vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
            }
            else {
                linearInterpolation = true;
                // upload new parameters to the OpenVG backend
                vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
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
    }

    /*****************************************************************
                            handle touch events
    *****************************************************************/
    void touchDown(float x,
                   float y) {

        float distStart, distEnd;
        PointF gradStart = new PointF();
        PointF gradEnd = new PointF();

        // get current gradient parameters
        gradientParamsGet(gradStart, gradEnd);
        distStart = distance(x, y, gradStart.x, gradStart.y);
        distEnd = distance(x, y, gradEnd.x, gradEnd.y);
        // check if we have picked a gradient control point
        if (distStart < distEnd) {
            pickedControlPoint = (distStart < controlPointsRadius * 1.1f) ? CONTROL_POINT_START : CONTROL_POINT_NONE;
        }
        else {
            pickedControlPoint = (distEnd < controlPointsRadius * 1.1f) ? CONTROL_POINT_END : CONTROL_POINT_NONE;
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
                // get current gradient parameters
                PointF gradStart = new PointF();
                PointF gradEnd = new PointF();
                gradientParamsGet(gradStart, gradEnd);
                // assign the new control point position
                if (pickedControlPoint == CONTROL_POINT_START) {
                    gradStart.set(x, y);
                }
                else {
                    gradEnd.set(x, y);
                }
                // update gradient parameters
                gradientParamsSet(gradStart, gradEnd);
            }
        }
    }

    void touchDoubleTap(float x,
                        float y) {

        toggleSpreadMode();
    }
}
