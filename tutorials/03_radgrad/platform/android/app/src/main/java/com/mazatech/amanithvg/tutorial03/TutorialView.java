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
package com.mazatech.amanithvg.tutorial03;

import android.content.Context;
import android.graphics.PixelFormat;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.GLES11Ext.GL_BGRA;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;

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
import javax.microedition.khronos.openvg.VG101;

public class TutorialView extends GLSurfaceView {

    public static final int TUTORIAL_CHANGE_SPREAD_MODE_CMD = 0;
    public static final int TUTORIAL_CHANGE_COLOR_INTERPOLATION_CMD = 1;
    public static final int TUTORIAL_ABOUT_CMD = 2;

    // AmanithVG variables
    private AmanithVG vg;
    private long vgContext;
    private long vgSurface;
    // AmanithVG rendering backend, SRE or GLE (see resources -> values -> bools.xml)
    private boolean sreBackend;
    // OpenGL texture used to blit the AmanithVG SRE surface
    int[] blitTexture;

    // OpenGL ES support of BGRA textures
    boolean bgraSupport;
    // OpenGL ES support of npot textures
    boolean npotSupport;

    // the view renderer
    Renderer renderer;
    // the tutorial instance
    Tutorial tutorial;

    TutorialView(Context context) {

        super(context);

        // check if we have to use AmanithVG SRE (or GLE)
        sreBackend = getResources().getBoolean(R.bool.sreBackend);

        // ask for a 32-bit surface with alpha
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // setup the context factory for OpenGL ES 1.1 rendering
        setEGLContextFactory(new ContextFactory());
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

        // request focus
        setFocusable(true);
        requestFocus();
    }

    // return the power of two value greater (or equal) to a specified value
    private int pow2Get(int value) {

        int v = 1;

        if (value >= 0x40000000) {
            return 0x40000000;
        }
        while (v < value) {
            v <<= 1;
        }
        return v;
    }

    /*****************************************************************
                                 OpenGL ES
    *****************************************************************/
    private void glesInit(GL11 gl) {

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

            blitTexture = new int[1];
            blitTexture[0] = 0;
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

    private FloatBuffer glesFloatBuffer(float[] arr) {

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

        // create AmanithVG class
        vg = new AmanithVG();

        // create an OpenVG context
        vgContext = vg.vgPrivContextCreateMZT(0);
        if (vgContext == 0) {
            return false;
        }

        // create a window surface (sRGBA premultiplied color space)
        vgSurface = vg.vgPrivSurfaceCreateMZT(getWidth(), getHeight(), false, true, true);
        if (vgSurface == 0) {
            vg.vgPrivContextDestroyMZT(vgContext);
            return false;
        }

        // bind context and surface
        if (!vg.vgPrivMakeCurrentMZT(vgContext, vgSurface)) {
            vg.vgPrivSurfaceDestroyMZT(vgSurface);
            vg.vgPrivContextDestroyMZT(vgContext);
            return false;
        }

        glesInit(gl);

        return true;
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
    }

    // get the width of OpenVG drawing surface, in pixels
    private int openvgSurfaceWidthGet() {

        return vg.vgPrivGetSurfaceWidthMZT(vgSurface);
    }

    // get the height of OpenVG drawing surface, in pixels
    private int openvgSurfaceHeightGet() {

        return vg.vgPrivGetSurfaceHeightMZT(vgSurface);
    }

    // get the maximum surface dimension supported by the OpenVG backend
    private int openvgSurfaceMaxDimensionGet() {

        return vg.vgPrivSurfaceMaxDimensionGetMZT();
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
        tutorial.init(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());
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

    boolean tutorialMenuOption(int option) {

        boolean consumed = true;

        switch (option) {

            case TUTORIAL_CHANGE_SPREAD_MODE_CMD:
                tutorial.toggleSpreadMode();
                break;

            case TUTORIAL_CHANGE_COLOR_INTERPOLATION_CMD:
                tutorial.toggleColorInterpolation();
                break;

            case TUTORIAL_ABOUT_CMD:
                aboutDialog();
                break;

            default:
                consumed = false;
                break;
        }

        return consumed;
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

    private void blitTextureGenerate(GL11 gl) {

        int[] maxTextureSize = new int[1];
        // get AmanithVG surface dimensions
        int surfaceWidth = openvgSurfaceWidthGet();
        int surfaceHeight = openvgSurfaceHeightGet();

        // get maximum texture size
        gl.glGetIntegerv(GL11.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        if ((surfaceWidth <= maxTextureSize[0]) && (surfaceHeight <= maxTextureSize[0])) {
            int format = (bgraSupport) ? GL_BGRA : GL11.GL_RGBA;
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

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {

            int[] contextAttribs = {
                EGL_CONTEXT_CLIENT_VERSION, 1,
                EGL10.EGL_NONE
            };
            return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, contextAttribs);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {

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

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, tmpValue)) {
                return tmpValue[0];
            }
            return defaultValue;
        }

        private EGLConfig chooseConfigImpl(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {

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
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

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
        private int redSize;
        private int greenSize;
        private int blueSize;
        private int alphaSize;
        private int depthSize;
        private int stencilSize;
        private int aaSamples;
        private int[] tmpValue = new int[1];
        // we start with a minimum size of 4 bits for red/green/blue, but will perform actual matching in chooseConfig() below.
        private static int EGL_OPENGL_ES_BIT = 1;
        private static int[] configAttribs = {
            EGL10.EGL_RED_SIZE, 4,
            EGL10.EGL_GREEN_SIZE, 4,
            EGL10.EGL_BLUE_SIZE, 4,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES_BIT,
            EGL10.EGL_NONE
        };
    }

    private static class TutorialViewRenderer implements GLSurfaceView.Renderer {

        private TutorialView view;

        TutorialViewRenderer(TutorialView myview) {

            // keep track of view
            view = myview;
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            if (view.openvgInit((GL11)gl)) {
                view.tutorialInit();
            }
        }

        public void onDrawFrame(GL10 gl) {

            view.tutorialDraw((GL11)gl);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {

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
        msg += "Vendor: " + vg.vgGetString(VG101.VG_VENDOR) + "\n";
        // renderer
        msg += "Renderer: " + vg.vgGetString(VG101.VG_RENDERER) + "\n";
        // version
        msg += "Version: " + vg.vgGetString(VG101.VG_VERSION) + "\n";
        // extensions
        msg += "Extensions: " + vg.vgGetString(VG101.VG_EXTENSIONS);
        messageDialog("About AmanithVG", msg);
    }

    @Override
    protected void onDetachedFromWindow() {

        // destroy OpenVG resources created by the tutorial
        tutorialDestroy();
        // destroy OpenVG context and surface
        openvgDestroy();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                tutorialTouchDown(event.getX(), event.getY());
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
}
