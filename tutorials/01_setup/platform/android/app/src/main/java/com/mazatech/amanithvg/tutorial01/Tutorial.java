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
package com.mazatech.amanithvg.tutorial01;

import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.VGImage;
import javax.microedition.khronos.openvg.VGPaint;
import javax.microedition.khronos.openvg.VGPath;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11Ext.*;

class Tutorial {

    // AmanithVG instance, passed through the constructor
    private AmanithVG vg;
    // clover-like path
    private VGPath path;
    // paint objects
    private VGPaint color;
    private VGPaint linGrad;
    private VGPaint radGrad;
    private VGPaint conGrad;
    private float conGradRepeats;
    private boolean conGradSupported;
    private VGPaint pattern;
    private VGImage patternImage;
    // current paint type
    private int paintIndex;
    private boolean linearInterpolation;
    private boolean smoothRampSupported;
    // current gradients spread mode
    private int spreadMode;
    // current pattern tiling mode
    private int tilingMode;
    private boolean animate;
    private boolean scissoring;
    private int scissorRectsConf;
    private boolean masking;
    // clover transformation
    private float rotation;
    private float scale;
    private float[] translation;

    private static final int PAINT_COLOR = 0;
    private static final int PAINT_LINGRAD = 1;
    private static final int PAINT_RADGRAD = 2;
    private static final int PAINT_CONGRAD = 3;
    private static final int PAINT_PATTERN = 4;
    private static final int PATTERN_WIDTH = 64;
    private static final int PATTERN_HEIGHT = 64;

    Tutorial(AmanithVG vgInstance) {

        vg = vgInstance;
        translation = new float[2];
    }

    private void extensionsCheck() {

        String extensions = vg.vgGetString(VG_EXTENSIONS);

        // check OpenVG extensions
        conGradSupported = extensions.contains("VG_MZT_conical_gradient");
        smoothRampSupported = extensions.contains("VG_MZT_color_ramp_interpolation");
    }

    private void genPaints() {

        float col[] = new float[4];
        float colKeys[] = new float[25];
        float linGradParams[] = new float[4];
        float radGradParams[] = new float[5];
        float conGradParams[] = new float[5];

        // generate paint objects
        color = vg.vgCreatePaint();
        linGrad = vg.vgCreatePaint();
        radGrad = vg.vgCreatePaint();
        if (conGradSupported) {
            conGrad = vg.vgCreatePaint();
        }
        pattern = vg.vgCreatePaint();

        // solid color
        col[0] = 0.8f;
        col[1] = 0.5f;
        col[2] = 0.1f;
        col[3] = 1.0f;
        vg.vgSetParameteri(color, VG_PAINT_TYPE, VG_PAINT_TYPE_COLOR);
        vg.vgSetParameterfv(color, VG_PAINT_COLOR, 4, col);

        // linear gradient
        colKeys[0]  = 0.00f; colKeys[1]  = 0.4f; colKeys[2]  = 0.0f; colKeys[3]  = 0.6f; colKeys[4]  = 1.0f;
        colKeys[5]  = 0.25f; colKeys[6]  = 0.9f; colKeys[7]  = 0.5f; colKeys[8]  = 0.1f; colKeys[9]  = 1.0f;
        colKeys[10] = 0.50f; colKeys[11] = 0.8f; colKeys[12] = 0.8f; colKeys[13] = 0.0f; colKeys[14] = 1.0f;
        colKeys[15] = 0.75f; colKeys[16] = 0.0f; colKeys[17] = 0.3f; colKeys[18] = 0.5f; colKeys[19] = 1.0f;
        colKeys[20] = 1.00f; colKeys[21] = 0.4f; colKeys[22] = 0.0f; colKeys[23] = 0.6f; colKeys[24] = 1.0f;
        linGradParams[0] = 320.0f; linGradParams[1] = 320.0f;
        linGradParams[2] = 192.0f; linGradParams[3] = 192.0f;
        vg.vgSetParameteri(linGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_LINEAR_GRADIENT);
        vg.vgSetParameterfv(linGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
        vg.vgSetParameterfv(linGrad, VG_PAINT_LINEAR_GRADIENT, 4, linGradParams);
        vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);

        // radial gradient
        colKeys[0]  = 0.00f; colKeys[1]  = 0.4f; colKeys[2]  = 0.0f; colKeys[3]  = 0.6f; colKeys[4]  = 1.0f;
        colKeys[5]  = 0.25f; colKeys[6]  = 0.9f; colKeys[7]  = 0.5f; colKeys[8]  = 0.1f; colKeys[9]  = 1.0f;
        colKeys[10] = 0.50f; colKeys[11] = 0.8f; colKeys[12] = 0.8f; colKeys[13] = 0.0f; colKeys[14] = 1.0f;
        colKeys[15] = 0.75f; colKeys[16] = 0.0f; colKeys[17] = 0.3f; colKeys[18] = 0.5f; colKeys[19] = 1.0f;
        colKeys[20] = 1.00f; colKeys[21] = 0.4f; colKeys[22] = 0.0f; colKeys[23] = 0.6f; colKeys[24] = 1.0f;
        radGradParams[0] = 256.0f; radGradParams[1] = 256.0f;
        radGradParams[2] = 200.0f; radGradParams[3] = 200.0f;
        radGradParams[4] = 100.0f;
        vg.vgSetParameteri(radGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_RADIAL_GRADIENT);
        vg.vgSetParameterfv(radGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
        vg.vgSetParameterfv(radGrad, VG_PAINT_RADIAL_GRADIENT, 5, radGradParams);
        vg.vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);

        // conical gradient
        if (conGradSupported) {
            colKeys[0]  = 0.00f; colKeys[1]  = 0.4f; colKeys[2]  = 0.0f; colKeys[3]  = 0.6f; colKeys[4]  = 1.0f;
            colKeys[5]  = 0.25f; colKeys[6]  = 0.9f; colKeys[7]  = 0.5f; colKeys[8]  = 0.1f; colKeys[9]  = 1.0f;
            colKeys[10] = 0.50f; colKeys[11] = 0.8f; colKeys[12] = 0.8f; colKeys[13] = 0.0f; colKeys[14] = 1.0f;
            colKeys[15] = 0.75f; colKeys[16] = 0.0f; colKeys[17] = 0.3f; colKeys[18] = 0.5f; colKeys[19] = 1.0f;
            colKeys[20] = 1.00f; colKeys[21] = 0.4f; colKeys[22] = 0.0f; colKeys[23] = 0.6f; colKeys[24] = 1.0f;
            conGradParams[0] = 256.0f; conGradParams[1] = 256.0f;
            conGradParams[2] = 200.0f; conGradParams[3] = 200.0f;
            conGradRepeats = 1;
            conGradParams[4] = (float)conGradRepeats;
            vg.vgSetParameteri(conGrad, VG_PAINT_TYPE, VG_PAINT_TYPE_CONICAL_GRADIENT_MZT);
            vg.vgSetParameterfv(conGrad, VG_PAINT_COLOR_RAMP_STOPS, 25, colKeys);
            vg.vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
            vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, VG_COLOR_RAMP_SPREAD_PAD);
        }

        // pattern
        patternImage = vg.vgCreateImage(VG_sRGBA_8888_PRE, PATTERN_WIDTH, PATTERN_HEIGHT, VG_IMAGE_QUALITY_FASTER);
        int pixels[] = new int[PATTERN_WIDTH * PATTERN_HEIGHT];
        for (int i = 0; i < PATTERN_HEIGHT; ++i) {
            for (int j = 0; j < PATTERN_WIDTH; ++j) {
                if (i < PATTERN_HEIGHT / 2) {
                    pixels[i * PATTERN_WIDTH + j] = (j < (PATTERN_WIDTH / 2)) ? 0xFF6030FF : 0xFFB060FF;
                }
                else {
                    pixels[i * PATTERN_WIDTH + j] = (j < (PATTERN_WIDTH / 2)) ? 0xFF9090FF : 0xFF30B0FF;
                }
            }
        }
        vg.vgImageSubData(patternImage, pixels, PATTERN_WIDTH * 4, VG_sRGBA_8888_PRE, 0, 0, PATTERN_WIDTH, PATTERN_HEIGHT);
        vg.vgSetParameteri(pattern, VG_PAINT_TYPE, VG_PAINT_TYPE_PATTERN);
        vg.vgSetParameteri(pattern, VG_PAINT_PATTERN_TILING_MODE, VG_TILE_PAD);
        vg.vgPaintPattern(pattern, patternImage);
    }

    private void genPaths() {

        byte commands[] = new byte[] {
            VG_MOVE_TO,
            VG_CUBIC_TO,
            VG_CUBIC_TO,
            VG_CUBIC_TO,
            VG_CUBIC_TO,
            VG_CLOSE_PATH
        };

        float coordinates[] = new float[] {
            // move to
            236.0f, 276.0f,
            // cubic to
            56.0f, 426.0f, 56.0f, 86.0f, 236.0f, 236.0f,
            // cubic to
            86.0f, 56.0f, 426.0f, 56.0f, 276.0f, 236.0f,
            // cubic to
            456.0f, 86.0f, 456.0f, 426.0f, 276.0f, 276.0f,
            // cubic to
            426.0f, 456.0f, 86.0f, 456.0f, 236.0f, 276.0f
        };

        path = vg.vgCreatePath(VG_PATH_FORMAT_STANDARD, VG_PATH_DATATYPE_F, 1.0f, 0.0f, 0, 0, VG_PATH_CAPABILITY_ALL);
        vg.vgAppendPathData(path, commands.length , commands, coordinates);
    }

    private void genAlphaMask(int surfaceWidth, int surfaceHeight) {

        VGImage alphaImage = vg.vgCreateImage(VG_A_8, surfaceWidth, surfaceHeight, VG_IMAGE_QUALITY_NONANTIALIASED);

        if (alphaImage != null) {
            // allocate pixels to be passed to OpenVG mask
            byte alphaPixels[] = new byte[surfaceWidth * surfaceHeight];
            // generate a sort of circular gradient (opaque at the center, transparent at the external border)
            float radius = (surfaceWidth < surfaceHeight) ? (float)(surfaceWidth / 4) : (float)(surfaceHeight / 4);
            float radiusSqr = radius * radius;
            for (int y = 0; y < surfaceHeight; ++y) {
                for (int x = 0; x < surfaceWidth; ++x) {
                    float distSqr = ((float)((x - surfaceWidth / 2) * (x - surfaceWidth / 2) + (y - surfaceHeight / 2) * (y - surfaceHeight / 2))) / radiusSqr;
                    if (distSqr > 1.0f) {
                        distSqr = 1.0f;
                    }
                    alphaPixels[y * surfaceWidth + x] = (byte)((1.0f - distSqr) * 255.0f);
                }
            }
            // upload alpha pixels to the image
            vg.vgImageSubData(alphaImage, alphaPixels, surfaceWidth, VG_A_8, 0, 0, surfaceWidth, surfaceHeight);
            // upload alpha image to the OpenVG alpha mask
            vg.vgMask(alphaImage, VG_SET_MASK, 0, 0, surfaceWidth, surfaceHeight);
            // destroy the temporary image
            vg.vgDestroyImage(alphaImage);
        }
    }

    void init(int surfaceWidth, int surfaceHeight) {

        // an opaque dark grey
        float clearColor[] = new float[] { 0.2f, 0.3f, 0.4f, 1.0f };
        float tileColor[] = new float[] { 0.1f, 0.6f, 0.3f, 1.0f };

        // check for OpenVG extensions
        extensionsCheck();
        // generate the flower path
        genPaths();
        // generate all the paints
        genPaints();
        // generate alpha mask
        genAlphaMask(surfaceWidth, surfaceHeight);
        // set some default parameters for the OpenVG context
        vg.vgSeti(VG_FILL_RULE, VG_EVEN_ODD);
        vg.vgSetfv(VG_CLEAR_COLOR, 4, clearColor);
        vg.vgSetfv(VG_TILE_FILL_COLOR, 4, tileColor);
        vg.vgSetf(VG_STROKE_LINE_WIDTH, 20.0f);
        vg.vgSetf(VG_STROKE_CAP_STYLE, VG_CAP_BUTT);
        vg.vgSetf(VG_STROKE_JOIN_STYLE, VG_JOIN_BEVEL);
        vg.vgSeti(VG_RENDERING_QUALITY, VG_RENDERING_QUALITY_BETTER);
        vg.vgSeti(VG_BLEND_MODE, VG_BLEND_SRC);

        paintIndex = PAINT_COLOR;
        linearInterpolation = true;
        spreadMode = VG_COLOR_RAMP_SPREAD_PAD;
        tilingMode = VG_TILE_PAD;
        // start with no rotation and put the path at the center of screen
        animate = false;
        rotation = 0.0f;
        scale = 1.0f;
        translation[0] = 0.0f;
        translation[1] = 0.0f;
        // disable scissoring and masking
        scissoring = false;
        scissorRectsConf = 1;
        toggleScissorRects(surfaceWidth, surfaceHeight);
        masking = false;
    }

    void destroy() {

        // release path
        vg.vgDestroyPath(path);
        // release paints
        vg.vgDestroyPaint(color);
        vg.vgDestroyPaint(linGrad);
        vg.vgDestroyPaint(radGrad);
        if (conGradSupported) {
            vg.vgDestroyPaint(conGrad);
        }
        vg.vgDestroyImage(patternImage);
        vg.vgDestroyPaint(pattern);
    }

    void togglePaint() {

        paintIndex = (conGradSupported) ? ((paintIndex + 1) % 5) : ((paintIndex + 1) % 4);
    }

    void toggleColorInterpolation() {

        if (smoothRampSupported) {
            if (linearInterpolation) {
                linearInterpolation = false;
                // upload new parameters to the OpenVG backend
                vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
                vg.vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
                if (conGradSupported) {
                    vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_SMOOTH_MZT);
                }
            }
            else {
                linearInterpolation = true;
                // upload new parameters to the OpenVG backend
                vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
                vg.vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
                if (conGradSupported) {
                    vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_INTERPOLATION_TYPE_MZT, VG_COLOR_RAMP_INTERPOLATION_LINEAR_MZT);
                }
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
        vg.vgSetParameteri(linGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
        vg.vgSetParameteri(radGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
        if (conGradSupported) {
            vg.vgSetParameteri(conGrad, VG_PAINT_COLOR_RAMP_SPREAD_MODE, spreadMode);
        }
    }

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

    void removeGradientRepeat() {

        if (conGradSupported) {
            if (conGradRepeats > 1) {
                float conGradParams[] = new float[5];
                // decrease repeats
                conGradRepeats--;
                conGradParams[0] = 256.0f; conGradParams[1] = 256.0f;
                conGradParams[2] = 200.0f; conGradParams[3] = 200.0f;
                conGradParams[4] = (float)conGradRepeats;
                // upload new parameters to the OpenVG backend
                vg.vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
            }
        }
    }

    void addGradientRepeat() {

        if (conGradSupported) {
            if (conGradRepeats < 4.0f) {
                float conGradParams[] = new float[5];
                // increase repeats
                conGradRepeats++;
                conGradParams[0] = 256.0f; conGradParams[1] = 256.0f;
                conGradParams[2] = 200.0f; conGradParams[3] = 200.0f;
                conGradParams[4] = (float)conGradRepeats;
                // upload new parameters to the OpenVG backend
                vg.vgSetParameterfv(conGrad, VG_PAINT_CONICAL_GRADIENT_MZT, 5, conGradParams);
            }
        }
    }

    void toggleAnimation() {

        // toggle animation
        animate = !animate;
    }

    void toggleScissoring() {

        // toggle scissoring
        scissoring = !scissoring;
    }

    void toggleScissorRects(int surfaceWidth, int surfaceHeight) {

        // first set of scissor rectangles
        int scsRectsCfg0[] = new int[] {
            60, 60, 156, 156,
            60, 296, 156, 156,
            296, 60, 156, 156,
            296, 296, 156, 156
        };
        // second set of scissor rectangles
        int scsRectsCfg1[] = new int[] {
            22, 22, 156, 332,
            22, 334, 332, 156,
            178, 22, 332, 156,
            334, 178, 156, 332
        };

        // select rectangles configuration
        scissorRectsConf++;
        int scsRects[] = ((scissorRectsConf & 1) != 0) ? scsRectsCfg0 : scsRectsCfg1;
        for (int i = 0; i < 16; ++i) {
            scsRects[i] = ((i & 1) != 0) ? (int)Math.floor(((scsRects[i] / 512.0f) * (float)surfaceHeight) + 0.5f)
                                         : (int)Math.floor(((scsRects[i] / 512.0f) * (float)surfaceWidth) + 0.5f);
        }
        // upload scissor rectangles to the OpenVG backend
        vg.vgSetiv(VG_SCISSOR_RECTS, 16, scsRects);
    }

    void toggleMasking() {

        // toggle masking
        masking = !masking;
    }

    void touchDrag(float dx, float dy) {

        translation[0] += dx;
        translation[1] += dy;
    }

    void touchPinch(float deltaScale) {

        scale += deltaScale * scale;
        if (scale < 0.005f) {
            scale = 0.005f;
        }
        else
        if (scale > 50.000f) {
            scale = 50.000f;
        }
    }

    void resize(int surfaceWidth, int surfaceHeight) {

        // regenerate alpha mask
        genAlphaMask(surfaceWidth, surfaceHeight);
        // regenerate scissor rectangles
        scissorRectsConf++;
        toggleScissorRects(surfaceWidth, surfaceHeight);
        // move the path back to the center
        translation[0] = 0.0f;
        translation[1] = 0.0f;
        scale = 1.0f;
    }

    void draw(int surfaceWidth, int surfaceHeight) {

        float scaleX = (float)surfaceWidth / 512.0f;
        float scaleY = (float)surfaceHeight / 512.0f;

        if (animate) {
            rotation += 0.2f;
        }

        // we don't want to apply scissoring to the vgClear (i.e. we want to clear the whole drawing surface)
        vg.vgSeti(VG_SCISSORING, VG_FALSE);
        vg.vgClear(0, 0, surfaceWidth, surfaceHeight);

        // take care to include the current 'rotation' and 'translation' during the user-to-surface path matrix construction
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_PATH_USER_TO_SURFACE);
        vg.vgLoadIdentity();
        vg.vgTranslate(translation[0], translation[1]);
        vg.vgScale(scaleX, scaleY);
        vg.vgTranslate(256.0f, 256.0f);
        vg.vgScale(scale, scale);
        vg.vgRotate(rotation);
        vg.vgTranslate(-256.0f, -256.0f);

        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
        vg.vgLoadIdentity();
        vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_STROKE_PAINT_TO_USER);
        vg.vgLoadIdentity();

        switch (paintIndex) {

            case PAINT_COLOR:
                vg.vgSetPaint(color, VG_FILL_PATH | VG_STROKE_PATH);
                break;

            case PAINT_LINGRAD:
                vg.vgSetPaint(linGrad, VG_FILL_PATH | VG_STROKE_PATH);
                break;

            case PAINT_RADGRAD:
                vg.vgSetPaint(radGrad, VG_FILL_PATH | VG_STROKE_PATH);
                break;

            case PAINT_CONGRAD:
                if (conGradSupported) {
                    vg.vgSetPaint(conGrad, VG_FILL_PATH | VG_STROKE_PATH);
                }
                else {
                    vg.vgSetPaint(pattern, VG_FILL_PATH | VG_STROKE_PATH);
                    vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
                    vg.vgTranslate((512.0f - (float)PATTERN_WIDTH) * 0.5f, (512.0f - (float)PATTERN_HEIGHT) * 0.5f);
                    vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_STROKE_PAINT_TO_USER);
                    vg.vgTranslate((512.0f - (float)PATTERN_WIDTH) * 0.5f, (512.0f - (float)PATTERN_HEIGHT) * 0.5f);
                }
                break;

            case PAINT_PATTERN:
                if (conGradSupported) {
                    vg.vgSetPaint(pattern, VG_FILL_PATH | VG_STROKE_PATH);
                    vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_FILL_PAINT_TO_USER);
                    vg.vgTranslate((512.0f - (float)PATTERN_WIDTH) * 0.5f, (512.0f - (float)PATTERN_HEIGHT) * 0.5f);
                    vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_STROKE_PAINT_TO_USER);
                    vg.vgTranslate((512.0f - (float)PATTERN_WIDTH) * 0.5f, (512.0f - (float)PATTERN_HEIGHT) * 0.5f);
                }
                break;

            default:
                vg.vgSetPaint(color, VG_FILL_PATH | VG_STROKE_PATH);
                break;
        }

        // set scissoring
        vg.vgSeti(VG_SCISSORING, scissoring);
        // set alpha mask
        vg.vgSeti(VG_MASKING, masking);
        // draw the flower path
        vg.vgDrawPath(path, VG_FILL_PATH);
    }
}
