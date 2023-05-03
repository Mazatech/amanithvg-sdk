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
package com.mazatech.amanithvg.tutorial07;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.GLES11Ext.GL_BGRA;
import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11Ext.*;

import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.GestureDetector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.openvg.AmanithVG;
import javax.microedition.khronos.openvg.AmanithVG.VGConfigMzt;

public class TutorialView extends GLSurfaceView implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {

    static final int TUTORIAL_NO_CMD = -1;
    static final int TUTORIAL_CHANGE_BLEND_MODE_CMD = 0;
    static final int TUTORIAL_ABOUT_CMD = 1;

    // gesture recognizer
    private final GestureDetector gestureDetector;
    // the host activity
    Context activity;

    // AmanithVG variables
    private AmanithVG vg;
    private long vgContext;
    private long vgSurface;
    // AmanithVG rendering backend, SRE or GLE (see resources -> values -> bools.xml)
    private final boolean sreBackend;
    // OpenGL texture used to blit the AmanithVG SRE surface
    int[] blitTexture;
    // OpenGL ES support of BGRA textures
    boolean bgraSupport;
    // OpenGL ES support of npot textures
    boolean npotSupport;
    // a possible pending action triggered by a menu item click
    int menuPendingAction;
    // the view renderer
    Renderer renderer;
    // the tutorial instance
    Tutorial tutorial;

    TutorialView(Context context) {

        super(context);

        activity = context;
        // check if we have to use AmanithVG SRE (or GLE)
        sreBackend = getResources().getBoolean(R.bool.sreBackend);

        // ask for a 32-bit surface with alpha
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // setup the context factory for OpenGL ES 1.1 rendering
        setEGLContextFactory(new ContextFactory(this));
        // ask for 32bit RGBA, at least 8bit depth, 4 bit stencil
        if (sreBackend) {
            // AmanithVG SRE: ask for 32bit RGBA (we are not interested in depth, stencil nor aa samples)
            setEGLConfigChooser(new ConfigChooser(8, 8, 8, 8, 0, 0, 0));
        }
        else {
            // AmanithVG GLE: ask for 32bit RGBA, at least 8bit depth, 4 bit stencil, and the highest aa samples
            setEGLConfigChooser(new ConfigChooser(8, 8, 8, 8, 8, 4, 1));
        }
        setPreserveEGLContextOnPause(true);

        // set the renderer responsible for frame rendering
        renderer = new TutorialViewRenderer(this);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        // setup gesture recognizer
        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);

        // request focus
        setFocusable(true);
        requestFocus();

        blitTexture = new int[] { 0 };
        menuPendingAction = TUTORIAL_NO_CMD;
    }

    // return the power of two value greater (or equal) to a specified value
    private int pow2Get(int value) {

        int result;

        if (value >= 0x40000000) {
            result = 0x40000000;
        }
        else {
            result = 1;
            while (result < value) {
                result <<= 1;
            }
        }

        return result;
    }

    private String blendModeStr(int blendMode) {

        String result;

        switch (blendMode) {
            case VG_BLEND_SRC:
                result = "SRC";
                break;
            case VG_BLEND_SRC_OVER:
                result = "SRC OVER";
                break;
            case VG_BLEND_DST_OVER:
                result = "DST OVER";
                break;
            case VG_BLEND_SRC_IN:
                result = "SRC IN";
                break;
            case VG_BLEND_DST_IN:
                result = "DST IN";
                break;
            case VG_BLEND_MULTIPLY:
                result = "MULTIPLY";
                break;
            case VG_BLEND_SCREEN:
                result = "SCREEN";
                break;
            case VG_BLEND_DARKEN:
                result = "DARKEN";
                break;
            case VG_BLEND_LIGHTEN:
                result = "LIGHTEN";
                break;
            case VG_BLEND_ADDITIVE:
                result = "ADDITIVE";
                break;
            // VG_MZT_advanced_blend_modes (extended blend modes)
            case VG_BLEND_CLEAR_MZT:
                result = "CLEAR";
                break;
            case VG_BLEND_DST_MZT:
                result = "DST";
                break;
            case VG_BLEND_SRC_OUT_MZT:
                result = "SRC OUT";
                break;
            case VG_BLEND_DST_OUT_MZT:
                result = "DST OUT";
                break;
            case VG_BLEND_SRC_ATOP_MZT:
                result = "SRC ATOP";
                break;
            case VG_BLEND_DST_ATOP_MZT:
                result = "DST ATOP";
                break;
            case VG_BLEND_XOR_MZT:
                result = "XOR";
                break;
            case VG_BLEND_OVERLAY_MZT:
                result = "OVERLAY";
                break;
            case VG_BLEND_COLOR_DODGE_MZT:
                result = "COLOR DODGE";
                break;
            case VG_BLEND_COLOR_BURN_MZT:
                result = "COLOR BURN";
                break;
            case VG_BLEND_HARD_LIGHT_MZT:
                result = "HARD LIGHT";
                break;
            case VG_BLEND_SOFT_LIGHT_MZT:
                result = "SOFT LIGHT";
                break;
            case VG_BLEND_DIFFERENCE_MZT:
                result = "DIFFERENCE";
                break;
            case VG_BLEND_EXCLUSION_MZT:
                result = "EXCLUSION";
                break;
            default:
                result = "";
                break;
        }

        return result;
    }

    private void activityTitleUpdate() {

        // make sure to run this code within the ui thread (sometimes this function is called
        // from the GL renderering thread, so this precaution is necessary)
        ((Activity)activity).runOnUiThread(new Runnable() {
            public void run() {
                final String appName = getResources().getString(R.string.app_name);
                final String title = appName + " (" + blendModeStr(tutorial.getBlendMode()) + ")";
                ((Activity)activity).setTitle(title);
            }
        });
    }

    /*****************************************************************
                                 OpenGL ES
    *****************************************************************/
    private void glesInit(@NonNull GL11 gl) {

        String extensions = gl.glGetString(GL11.GL_EXTENSIONS);

        // check useful GL extensions
        bgraSupport = ((extensions.contains("GL_EXT_texture_format_BGRA8888")) || (extensions.contains("GL_IMG_texture_format_BGRA8888")));
        npotSupport = ((extensions.contains("GL_OES_texture_npot")) || (extensions.contains("GL_APPLE_texture_2D_limited_npot")));

        if (sreBackend) {

            // set basic OpenGL states
            gl.glDisable(GL11.GL_LIGHTING);
            gl.glShadeModel(GL11.GL_FLAT);
            gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
            gl.glDisable(GL11.GL_CULL_FACE);
            gl.glDisable(GL11.GL_ALPHA_TEST);
            gl.glDisable(GL11.GL_SCISSOR_TEST);
            gl.glDisable(GL11.GL_DEPTH_TEST);
            gl.glDisable(GL11.GL_STENCIL_TEST);
            gl.glDisable(GL11.GL_BLEND);
            gl.glDepthMask(false);
            gl.glColorMask(true, true, true, true);
            gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            gl.glMatrixMode(GL11.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glMatrixMode(GL11.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glViewport(0, 0, getWidth(), getHeight());
            gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            // generate OpenGL ES texture used to blit the AmanithVG SRE surface
            gl.glGenTextures(1, blitTexture, 0);
            gl.glEnable(GL11.GL_TEXTURE_2D);
            gl.glBindTexture(GL11.GL_TEXTURE_2D, blitTexture[0]);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
            blitTextureGenerate(gl);
        }
    }

    private @NonNull FloatBuffer glesFloatBuffer(@NonNull float[] arr) {

        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    /*****************************************************************
                                  OpenVG
    *****************************************************************/
    private boolean openvgInit(GL11 gl) {

        boolean ok;

        // create AmanithVG class
        vg = new AmanithVG();

        // initialize AmanithVG
        vgContext = 0;
        vgSurface = 0;
        // set quality parameters (range is [0; 100], where 100 represents the best quality)
        vg.vgConfigSetMZT(VGConfigMzt.CurvesQuality, 75.0f);
        vg.vgConfigSetMZT(VGConfigMzt.RadialGrandientsQuality, 75.0f);
        vg.vgConfigSetMZT(VGConfigMzt.ConicalGrandientsQuality, 75.0f);
        // set other parameters, if desired, before to call vgInitializeMZT
        ok = vg.vgInitializeMZT();

        if (ok) {
            // create an OpenVG context
            vgContext = vg.vgPrivContextCreateMZT(0);
            if (vgContext != 0) {
                // create a window surface (sRGBA premultiplied color space)
                vgSurface = vg.vgPrivSurfaceCreateMZT(getWidth(), getHeight(), false, true, true);
                if (vgSurface != 0) {
                    // bind context and surface
                    ok = vg.vgPrivMakeCurrentMZT(vgContext, vgSurface);
                }
                else {
                    // error when creating the drawing surface
                    ok = false;
                }
            }
            else {
                // error when creating the context
                ok = false;
            }
        }

        // is something went wrong, release allocated OpenVG resources
        if (!ok) {
            if (vgSurface != 0) {
                vg.vgPrivSurfaceDestroyMZT(vgSurface);
            }
            if (vgContext != 0) {
                vg.vgPrivContextDestroyMZT(vgContext);
            }
            // terminate AmanithVG
            vg.vgTerminateMZT();
        }
        else {
            glesInit(gl);
        }

        return ok;
    }

    private void openvgResize(GL11 gl, int width, int height) {

        // resize AmanithVG drawing surface
        if (vg.vgPrivSurfaceResizeMZT(vgSurface, width, height)) {
            if (sreBackend) {
                // resize texture used to blit AmanithVG SRE surface
                blitTextureResize(gl);
                // update OpenGL viewport
                gl.glViewport(0, 0, width, height);
            }
        }
    }

    private void openvgDestroy() {

        // unbind context and surface
        vg.vgPrivMakeCurrentMZT(0, 0);
        // destroy OpenVG surface
        vg.vgPrivSurfaceDestroyMZT(vgSurface);
        // destroy OpenVG context
        vg.vgPrivContextDestroyMZT(vgContext);
        // terminate AmanithVG
        vg.vgTerminateMZT();
    }

    // get the width of OpenVG drawing surface, in pixels
    private int openvgSurfaceWidthGet() {

        return vg.vgPrivGetSurfaceWidthMZT(vgSurface);
    }

    // get the height of OpenVG drawing surface, in pixels
    private int openvgSurfaceHeightGet() {

        return vg.vgPrivGetSurfaceHeightMZT(vgSurface);
    }

    // get the format of OpenVG drawing surface
    private int openvgSurfaceFormatGet() {

        return vg.vgPrivGetSurfaceFormatMZT(vgSurface);
    }

    // get the maximum surface dimension supported by the OpenVG backend
    private int openvgSurfaceMaxDimensionGet() {

        return (int)vg.vgConfigGetMZT(VGConfigMzt.MaxSurfaceDimension);
    }

    // get OpenVG surface pixels (a direct buffer that wraps the native AmanithVG heap memory)
    private java.nio.ByteBuffer openvgSurfacePixelsGet() {

        return vg.vgPrivGetSurfacePixelsMZT(vgSurface);
    }

    /*****************************************************************
                                 Tutorial
    *****************************************************************/
    private void tutorialInit() {

        tutorial = new Tutorial(vg);
        // init tutorial application (as a preferred image format, we pass the drawing surface
        // one, in order to speedup "read pixels" operations and rendering)
        tutorial.init(openvgSurfaceWidthGet(), openvgSurfaceHeightGet(), openvgSurfaceFormatGet());
        // update title
        activityTitleUpdate();
    }

    private void tutorialDestroy() {

        // destroy OpenVG resources created by the tutorial
        tutorial.destroy();
    }

    private void tutorialDraw(GL11 gl) {

        // blit OpenVG content (AmanithVG SRE only)
        if (sreBackend) {
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT);
            // draw OpenVG content
            tutorial.draw(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());
            blitTextureDraw(gl);
        }
        else {
            // draw OpenVG content
            tutorial.draw(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());
        }

        // acknowledge AmanithVG that we have performed a swapbuffers
        vg.vgPostSwapBuffersMZT();
    }

    private void tutorialResize() {

        tutorial.resize(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());
    }

    private boolean tutorialMenuOptionConsumable(int option) {

        boolean consumable;

        switch (option) {

            case TUTORIAL_CHANGE_BLEND_MODE_CMD:
            case TUTORIAL_ABOUT_CMD:
                consumable = true;
                break;

            default:
                consumable = false;
                break;
        }

        return consumable;
    }

    private void tutorialMenuPendingActionPerform() {

        switch (menuPendingAction) {

            case TUTORIAL_CHANGE_BLEND_MODE_CMD:
                tutorial.toggleBlendMode();
                // update title
                activityTitleUpdate();
                break;

            default:
                // should be unreachable code
                break;
        }

        // reset to "no actions required"
        menuPendingAction = TUTORIAL_NO_CMD;
    }

    boolean tutorialMenuOption(int option) {

        boolean consumable = tutorialMenuOptionConsumable(option);

        if (consumable) {
            // this can be consumed from the calling thread
            if (option == TUTORIAL_ABOUT_CMD) {
                aboutDialog();
            }
            else {
                // all OpenVG related actions must be consumed within the rendering thread
                // (i.e. the thread where OpenVG context and surfaces have been created)
                menuPendingAction = option;
            }
        }

        return consumable;
    }

    private void tutorialTouchDown(float x, float y) {

        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        this.tutorial.touchDown(x, (float)getHeight() - y);
    }

    private void tutorialTouchUp(float x, float y) {

        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        this.tutorial.touchUp(x, (float)getHeight() - y);
    }

    private void tutorialTouchMove(float x, float y) {

        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        this.tutorial.touchMove(x, (float)getHeight() - y);
    }

    private void tutorialTouchDoubleTap(float x, float y) {

        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        this.tutorial.touchDoubleTap(x, (float)getHeight() - y);
        // update title
        activityTitleUpdate();
    }

    private void blitTextureUpdate(GL11 gl) {

        // get AmanithVG surface dimensions
        int surfaceWidth = openvgSurfaceWidthGet();
        int surfaceHeight = openvgSurfaceHeightGet();

        if (bgraSupport) {
            // get AmanithVG surface pixels
            java.nio.ByteBuffer surfacePixels = openvgSurfacePixelsGet();
            gl.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, surfaceWidth, surfaceHeight, GL_BGRA, GL11.GL_UNSIGNED_BYTE, surfacePixels);
        }
        else {
            int[] rgbaPixels = new int[surfaceWidth * surfaceHeight];
            if (vg.vgPrivSurfaceCopyPixelsMZT(vgSurface, rgbaPixels, true)) {
                gl.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, surfaceWidth, surfaceHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, IntBuffer.wrap(rgbaPixels));
            }
        }
    }

    private void blitTextureGenerate(@NonNull GL11 gl) {

        int[] maxTextureSize = new int[1];
        // get AmanithVG surface dimensions
        int surfaceWidth = openvgSurfaceWidthGet();
        int surfaceHeight = openvgSurfaceHeightGet();

        // get maximum texture size
        gl.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        if ((surfaceWidth <= maxTextureSize[0]) && (surfaceHeight <= maxTextureSize[0])) {
            int format = bgraSupport ? GL_BGRA : GL11.GL_RGBA;
            int texWidth = npotSupport ? surfaceWidth : pow2Get(surfaceWidth);
            int texHeight = npotSupport ? surfaceHeight : pow2Get(surfaceHeight);
            // generate the OpenGL texture
            gl.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, texWidth, texHeight, 0, format, GL11.GL_UNSIGNED_BYTE, null);
            // upload pixels
            blitTextureUpdate(gl);
        }
    }

    private void blitTextureResize(GL11 gl) {

        blitTextureGenerate(gl);
    }

    private void blitTextureDraw(GL11 gl) {

        float u, v;
        float[] xy = new float[8];
        float[] uv = new float[8];
        // get AmanithVG surface dimensions
        int surfaceWidth = openvgSurfaceWidthGet();
        int surfaceHeight = openvgSurfaceHeightGet();
        float w = (float)surfaceWidth / (float)getWidth();
        float h = (float)surfaceHeight / (float)getHeight();

        if (npotSupport) {
            u = 1.0f;
            v = 1.0f;
        }
        else {
            // greater (or equal) power of two values
            float texWidth = (float)pow2Get(surfaceWidth);
            float texHeight = (float)pow2Get(surfaceHeight);
            u = (surfaceWidth - 0.5f) / texWidth;
            v = (surfaceHeight - 0.5f) / texHeight;
        }

        // geometric coordinates
        xy[0] = -1.0f;  xy[1] = -1.0f;
        xy[2] =  -1.0f + (w * 2.0f);  xy[3] = -1.0f;
        xy[4] = -1.0f;  xy[5] =  -1.0f + (h * 2.0f);
        xy[6] =  -1.0f + (w * 2.0f);  xy[7] =  -1.0f + (h * 2.0f);
        // texture coordinates
        uv[0] = 0.0f;  uv[1] =    v;
        uv[2] =    u;  uv[3] =    v;
        uv[4] = 0.0f;  uv[5] = 0.0f;
        uv[6] =    u;  uv[7] = 0.0f;

        // update blit texture
        blitTextureUpdate(gl);
        // simply put a quad
        gl.glVertexPointer(2, GL11.GL_FLOAT, 0, glesFloatBuffer(xy));
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, glesFloatBuffer(uv));
        gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
    }

    private static class ContextFactory implements GLSurfaceView.EGLContextFactory {

        // the view that created this context factory
        final private TutorialView view;

        ContextFactory(TutorialView myView) {

            // keep track of view
            view = myView;
        }

        public EGLContext createContext(@NonNull EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {

            int[] contextAttribs = {
                EGL_CONTEXT_CLIENT_VERSION, 1,
                EGL10.EGL_NONE
            };
            return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, contextAttribs);
        }

        public void destroyContext(@NonNull EGL10 egl, EGLDisplay display, EGLContext context) {

            // NB: because GLSurfaceView.Renderer has no 'onSurfaceDestroyed' event, this is the only
            // possible place to intercept rendering termination within the rendering thread

            // destroy OpenVG resources created by the tutorial
            view.tutorialDestroy();
            // destroy OpenVG context and surface
            view.openvgDestroy();

            egl.eglDestroyContext(display, context);
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {

        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil, int samples) {

            // keep track of desired settings
            redSize = r;
            greenSize = g;
            blueSize = b;
            alphaSize = a;
            depthSize = depth;
            stencilSize = stencil;
            aaSamples = samples;
        }

        private int findConfigAttrib(@NonNull EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, tmpValue)) {
                return tmpValue[0];
            }
            return defaultValue;
        }

        private EGLConfig chooseConfigImpl(@NonNull EGL10 egl, EGLDisplay display, @NonNull EGLConfig[] configs) {

            EGLConfig result = null;

            for (EGLConfig config : configs) {
                // we need at least depthSize and stencilSize bits
                int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
                if ((d < depthSize) || (s < stencilSize)) {
                    continue;
                }
                // we want an exact match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);

                if ((r == redSize) && (g == greenSize) && (b == blueSize) && (a == alphaSize)) {

                    int sampleBuffers = findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLE_BUFFERS, 0);
                    int samples = findConfigAttrib(egl, display, config, EGL10.EGL_SAMPLES, 0);

                    if (aaSamples == 0) {
                        // we don't want sample buffers, so discard those configurations that have them
                        if (sampleBuffers == 0) {
                            result = config;
                            break;
                        }
                    }
                    else {
                        if ((sampleBuffers > 0) && (samples > 0)) {
                            // select the highest number of aa samples (AmanithVG GLE)
                            if (samples > aaSamples) {
                                aaSamples = samples;
                                result = config;
                            }
                        }
                    }
                }
            }

            return result;
        }

        private EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {

            return chooseConfigImpl(egl, display, configs);
        }

        // must be implemented by a GLSurfaceView.EGLConfigChooser
        public EGLConfig chooseConfig(@NonNull EGL10 egl, EGLDisplay display) {

            // get the number of minimally matching EGL configurations
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, configAttribs, null, 0, num_config);
            int numConfigs = num_config[0];
            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }
            // allocate then read the array of minimally matching EGL configs
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, configAttribs, configs, numConfigs, num_config);
            // now return the "best" one
            return chooseConfig(egl, display, configs);
        }

        // subclasses can adjust these values
        private final int redSize;
        private final int greenSize;
        private final int blueSize;
        private final int alphaSize;
        private final int depthSize;
        private final int stencilSize;
        private int aaSamples;
        private final int[] tmpValue = new int[1];
        // we start with a minimum size of 4 bits for red/green/blue, but will perform actual matching in chooseConfig() below.
        private static final int EGL_OPENGL_ES_BIT = 1;
        private static final int[] configAttribs = {
            EGL10.EGL_RED_SIZE, 4,
            EGL10.EGL_GREEN_SIZE, 4,
            EGL10.EGL_BLUE_SIZE, 4,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES_BIT,
            EGL10.EGL_NONE
        };
    }

    private static class TutorialViewRenderer implements GLSurfaceView.Renderer {

        // the view that created this renderer
        private final TutorialView view;

        TutorialViewRenderer(TutorialView myView) {

            // keep track of view
            view = myView;
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            if (view.openvgInit((GL11)gl)) {
                view.tutorialInit();
            }
        }

        public void onDrawFrame(GL10 gl) {

            // check and perform a possible pending action
            view.tutorialMenuPendingActionPerform();
            view.tutorialDraw((GL11)gl);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {

            // called when the surface changed size; in the detail, it is called after the surface
            // is created and whenever the OpenGL ES surface size changes
            view.openvgResize((GL11)gl, width, height);
            view.tutorialResize();
        }
    }

    private void messageDialog(String title, String msg) {

        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();

        // show message
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.show();
    }

    private void aboutDialog() {

        String msg = "";
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        msg += "AmanithVG - www.mazatech.com\n";
        msg += "Copyright 2004-" + year + " by Mazatech Srl. All Rights Reserved.\n\n";
        msg += "OpenVG driver information:\n\n";
        // vendor
        msg += "Vendor: " + vg.vgGetString(VG_VENDOR) + "\n";
        // renderer
        msg += "Renderer: " + vg.vgGetString(VG_RENDERER) + "\n";
        // version
        msg += "Version: " + vg.vgGetString(VG_VERSION) + "\n";
        // extensions
        msg += "Extensions: " + vg.vgGetString(VG_EXTENSIONS);
        messageDialog("About AmanithVG", msg);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!gestureDetector.onTouchEvent(event)) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    tutorialTouchDown(event.getX(), event.getY());
                    performClick();
                    break;

                case MotionEvent.ACTION_UP:
                    tutorialTouchUp(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_MOVE:
                    tutorialTouchMove(event.getX(), event.getY());
                    break;
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean performClick() {

        super.performClick();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            tutorialTouchDoubleTap(event.getX(), event.getY());
            return true;
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }
}
