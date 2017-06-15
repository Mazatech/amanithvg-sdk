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
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xatom.h>
#include <X11/keysym.h>
#ifndef AM_SRE
    // AmanithVG GLE uses OpenGL as rendering backend
    #include <GL/glx.h>
#endif
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>
#include "tutorial_03.h"

#define WINDOW_TITLE "AmanithVG Tutorial 03 - Press F1 for help"

// default window dimensions
#define INITIAL_WINDOW_WIDTH 512
#define INITIAL_WINDOW_HEIGHT 512

#ifndef AM_SRE
    // GLX_SGI_swap_control
    typedef int (*MY_PFNGLXSWAPINTERVALSGIPROC)(int interval);
    // GLX_EXT_swap_control
    typedef void (*MY_PFNGLXSWAPINTERVALEXTPROC)(Display *dpy,
                                                 GLXDrawable drawable,
                                                 int interval);
    // OpenGL context and surface/drawable
    GLXContext glContext = 0;
    GLXWindow glWindow = 0;
#else
    // image used to blit AmanithVG SRE surface
    XImage* blitImage;
#endif

// X11 variables
Display* display;
VGint screenDepth;
Window window;
GC windowGfxContext;
Atom atom_DELWIN;
Atom atom_PROTOCOLS;
XFontStruct* fontInfo;

// OpenVG variables
void* vgContext = NULL;
void* vgWindowSurface = NULL;

// FPS counter
VGuint time0, time1;
VGuint framesCounter;
char infoMessage[2048];
VGboolean displayHelp, displayAbout, done;

/*****************************************************************
                            OpenVG
*****************************************************************/
static VGboolean openvgInit(const VGuint width,
                            const VGuint height) {

    // create an OpenVG context
    vgContext = vgPrivContextCreateMZT(NULL);
    if (!vgContext) {
        return VG_FALSE;
    }

    // create a window surface (sRGBA premultiplied color space)
    vgWindowSurface = vgPrivSurfaceCreateMZT(width, height, VG_FALSE, VG_TRUE, VG_TRUE);
    if (!vgWindowSurface) {
        vgPrivContextDestroyMZT(vgContext);
        return VG_FALSE;
    }

    // bind context and surface
    if (vgPrivMakeCurrentMZT(vgContext, vgWindowSurface) == VG_FALSE) {
        vgPrivSurfaceDestroyMZT(vgWindowSurface);
        vgPrivContextDestroyMZT(vgContext);
        return VG_FALSE;
    }

    return VG_TRUE;
}

static void openvgDestroy(void) {
    
    // unbind context and surface
    vgPrivMakeCurrentMZT(NULL, NULL);
    // destroy OpenVG surface
    vgPrivSurfaceDestroyMZT(vgWindowSurface);
    // destroy OpenVG context
    vgPrivContextDestroyMZT(vgContext);
}

// get the width of OpenVG drawing surface, in pixels
static VGint openvgSurfaceWidthGet(void) {

    return vgPrivGetSurfaceWidthMZT(vgWindowSurface);
}

// get the height of OpenVG drawing surface, in pixels
static VGint openvgSurfaceHeightGet(void) {
    
    return vgPrivGetSurfaceHeightMZT(vgWindowSurface);
}

// get the maximum surface dimension supported by the OpenVG backend
static VGint openvgSurfaceMaxDimensionGet(void) {

    return vgPrivSurfaceMaxDimensionGetMZT();
}

/*****************************************************************
                       Windowing system
*****************************************************************/
static VGuint getTimeMS(void) {

    struct timeval tp;
    struct timezone tzp;

    gettimeofday(&tp, &tzp);
    return (VGuint)((tp.tv_sec * 1000) + (tp.tv_usec / 1000));
}

static void stringDraw(const char *msg, const int x, const int y) {
    
    XDrawImageString(display, window, windowGfxContext, x, y, msg, strlen(msg));
}

static void textDraw(const char *msg, int x, int y) {

    VGint font_height = fontInfo->ascent + fontInfo->descent;
    VGint i = 0, j = 0;
    char str[255];

    y += font_height;   
    while (msg[j] != '\0') {
        if (msg[j] != '\n') {
            str[i] = msg[j];
            i++;
        }
        else {
            str[i] = '\0';
            stringDraw(str, x, y);
            y += font_height;
            i = 0;
        }
        j++;
    }
    if (i > 0) {
        stringDraw(str, x, y);
    }
}

static void windowFontLoad(void) {

    char fontName[] = "9x15";

    // load font and get font information structure
    if ((fontInfo = XLoadQueryFont(display, fontName)) == NULL) {
        fprintf(stderr, "Cannot open %s font.\n", fontName);
    }
}

static VGboolean windowCreate(const char* title,
                              const VGuint width,
                              const VGuint height) {

    VGint screen, screenWidth, screenHeight;
    XSetWindowAttributes windowAttributes;
    XSizeHints windowSizeHints;
#ifndef AM_SRE
    XVisualInfo* visualInfo;
    MY_PFNGLXSWAPINTERVALSGIPROC sgiSwapInterval;
    MY_PFNGLXSWAPINTERVALEXTPROC extSwapInterval;
    // OpenGL surface configuration
    GLXFBConfig* fbConfigs;
    VGint fbConfigsCount = 0;
    VGint glAttributes[] = {
        GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
        GLX_RENDER_TYPE, GLX_RGBA_BIT,
        // request a double-buffered color buffer with the maximum number of bits per component
        GLX_DOUBLEBUFFER, True,
        GLX_RED_SIZE, 1,
        GLX_GREEN_SIZE, 1,
        GLX_BLUE_SIZE, 1,
        GLX_ALPHA_SIZE, 1,
        GLX_DEPTH_SIZE, 1,
        GLX_STENCIL_SIZE, 1,
        GLX_SAMPLE_BUFFERS, 1,
        GLX_SAMPLES, 8,
        None
    };
#else
    Visual* screenVisual;
#endif

    // open a display on the current root window
    display = XOpenDisplay(NULL);
    if (display == NULL) {
        fprintf(stderr, "Unable to open display.\n");
        return VG_FALSE;
    }

    // get the default screen associated with the previously opened display
    screen = DefaultScreen(display);
#ifdef AM_SRE
    // get the default visual
    screenVisual = DefaultVisual(display, screen);
#endif
    // get screen bitdepth
    screenDepth = DefaultDepth(display, screen);
    
    // run only on a 32bpp display
    if ((screenDepth != 24) && (screenDepth != 32)) {
        fprintf(stderr, "Cannot find 32bit pixel format on the current display.\n");
        XCloseDisplay(display);
        return VG_FALSE;
    }

    // get screen dimensions
    screenWidth = DisplayWidth(display, screen);
    screenHeight = DisplayHeight(display, screen);

#ifndef AM_SRE
    // request a suitable framebuffer configuration for AmanithVG GLE; first try 8 samples
    fbConfigs = glXChooseFBConfig(display, screen, glAttributes, &fbConfigsCount);
    if ((!fbConfigs) || (fbConfigsCount < 1)) {
        // try 6 samples
        glAttributes[21] = 6;
        fbConfigs = glXChooseFBConfig(display, screen, glAttributes, &fbConfigsCount);
        if ((!fbConfigs) || (fbConfigsCount < 1)) {
            // try 4 samples
            glAttributes[21] = 4;
            fbConfigs = glXChooseFBConfig(display, screen, glAttributes, &fbConfigsCount);
            if ((!fbConfigs) || (fbConfigsCount < 1)) {
                // try 2 samples
                glAttributes[21] = 2;
                fbConfigs = glXChooseFBConfig(display, screen, glAttributes, &fbConfigsCount);
                if ((!fbConfigs) || (fbConfigsCount < 1)) {
                    // no fsaa
                    glAttributes[19] = 0;
                    glAttributes[21] = 0;
                    fbConfigs = glXChooseFBConfig(display, screen, glAttributes, &fbConfigsCount);
                    if ((!fbConfigs) || (fbConfigsCount < 1)) {
                        // try with no stencil buffer
                        glAttributes[17] = 0;
                        fbConfigs = glXChooseFBConfig(display, screen, glAttributes, &fbConfigsCount);
                        if ((!fbConfigs) || (fbConfigsCount < 1)) {
                            fprintf(stderr, "Cannot find a suitable framebuffer configuration.\n");
                            return VG_FALSE;
                        }
                    }
                }
            }
        }
    }

    // create an X colormap and window with a visual matching the first returned framebuffer config
    visualInfo = glXGetVisualFromFBConfig(display, fbConfigs[0]);
    windowAttributes.colormap = XCreateColormap(display, RootWindow(display, visualInfo->screen), visualInfo->visual, AllocNone);
#endif

    // initialize window's attribute structure
    windowAttributes.border_pixel = BlackPixel(display, screen);
    windowAttributes.background_pixel = 0xCCCCCCCC;
    windowAttributes.backing_store = NotUseful;

    // create the window centered on the screen
#ifndef AM_SRE
    window = XCreateWindow(display, RootWindow(display, visualInfo->screen), (screenWidth - width) / 2, (screenHeight - height) / 2, width, height, 0, visualInfo->depth, InputOutput, visualInfo->visual, CWColormap | CWBorderPixel | CWBackPixel | CWBackingStore, &windowAttributes);
#else
    window = XCreateWindow(display, DefaultRootWindow(display), (screenWidth - width) / 2, (screenHeight - height) / 2, width, height, 0, screenDepth, InputOutput, screenVisual, CWBackPixel | CWBorderPixel | CWBackingStore, &windowAttributes);
#endif

    if (window == None) {
        fprintf(stderr, "Unable to create the window.\n");
        XCloseDisplay(display);
        return VG_FALSE;
    }
    // set the window's name
    XStoreName(display, window, title);
    // tell the server to report mouse and key-related events
    XSelectInput(display, window, KeyPressMask | KeyReleaseMask | ButtonPressMask | Button1MotionMask | Button2MotionMask | Button3MotionMask | StructureNotifyMask | ExposureMask);
    // initialize window's sizehint definition structure
    windowSizeHints.flags = PPosition | PMinSize | PMaxSize;
    windowSizeHints.x = 0;
    windowSizeHints.y = 0;
    windowSizeHints.min_width = 1;
    // clamp window dimensions according to the maximum surface dimension supported by the OpenVG backend
    windowSizeHints.max_width = openvgSurfaceMaxDimensionGet();
    if (screenWidth < windowSizeHints.max_width) {
        windowSizeHints.max_width = screenWidth;
    }
    windowSizeHints.min_height = 1;
    windowSizeHints.max_height = windowSizeHints.max_width;
    if (screenHeight < windowSizeHints.max_height) {
        windowSizeHints.max_height = screenHeight;
    }
    // set the window's sizehint
    XSetWMNormalHints(display, window, &windowSizeHints);
    // clear the window
    XClearWindow(display, window);

#ifndef AM_SRE
    // create a GLX context for OpenGL rendering
    glContext = glXCreateNewContext(display, fbConfigs[0], GLX_RGBA_TYPE, NULL, True);
    if (!glContext) {
        fprintf(stderr, "Unable to create the GLX context.\n");
        XDestroyWindow(display, window);
        XCloseDisplay(display);
        return VG_FALSE;
    }
    // create a GLX window to associate the frame buffer configuration with the created X window
    glWindow = glXCreateWindow(display, fbConfigs[0], window, NULL);
    // bind the GLX context to the Window
    glXMakeContextCurrent(display, glWindow, glWindow, glContext);
    // GLX_EXT_swap_control
    extSwapInterval = (MY_PFNGLXSWAPINTERVALEXTPROC)glXGetProcAddressARB((const GLubyte *)"glXSwapIntervalEXT");
    if (extSwapInterval) {
        extSwapInterval(display, glWindow, 0);
    }
    else {
        // GLX_SGI_swap_control
        sgiSwapInterval = (MY_PFNGLXSWAPINTERVALSGIPROC)glXGetProcAddressARB((const GLubyte *)"glXSwapIntervalSGI");
        if (sgiSwapInterval) {
            sgiSwapInterval(0);
        }
    }
#endif

    // put the window on top of the others
    XMapRaised(display, window);
    // clear event queue
    XFlush(display);
    
    // get the default graphic context
#ifndef AM_SRE
    windowGfxContext = DefaultGC(display, visualInfo->screen);
#else
    windowGfxContext = DefaultGC(display, screen);
#endif

    XSetForeground(display, windowGfxContext, BlackPixel(display, screen));
    XSetBackground(display, windowGfxContext, 0xCCCCCCCC);

#ifndef AM_SRE
    XFree(visualInfo);
#else
    // create the image used for blitting purposes
    blitImage = XCreateImage(display, CopyFromParent, screenDepth, ZPixmap, 0, NULL, width, height, 32, width * 4);
#endif

    atom_DELWIN = XInternAtom(display, "WM_DELETE_WINDOW", False);
    atom_PROTOCOLS = XInternAtom(display, "WM_PROTOCOLS", False);
    XChangeProperty(display, window, atom_PROTOCOLS, XA_ATOM, 32, PropModeReplace, (unsigned char *)&atom_DELWIN, 1);

    // load font
    windowFontLoad();
    return VG_TRUE;
}

static void windowDestroy(void) {

    // delete used font
    XFreeFont(display, fontInfo);

#ifndef AM_SRE
    // unbind OpenGL context / surface
    glXMakeContextCurrent(display, 0, 0, 0);
    // destroy OpenGL surface/drawable
    glXDestroyWindow(display, glWindow);
    // destroy OpenGL context
    glXDestroyContext(display, glContext);
#else
    // restore XImage's buffer pointer
    blitImage->data = NULL;
    // destroy the XImage
    XDestroyImage(blitImage);
#endif

    // Close the window
    XDestroyWindow(display, window);
    // Close the display
    XCloseDisplay(display);
}

static void windowTitleUpdate(void) {

    time1 = getTimeMS();
    // print frame rate every second
    if (time1 - time0 > 1000) {
        char title[128];
        VGfloat fps = ((VGfloat)framesCounter * 1000.0f / (VGfloat)(time1 - time0));
        sprintf(title, "(%d fps) "WINDOW_TITLE, (VGint)fps);
        XStoreName(display, window, title);
        // reset frames counter
        framesCounter = 0;
        time0 = time1;
    }
}

static void windowResize(const VGint w,
                         const VGint h) {

    // resize AmanithVG surface
    vgPrivSurfaceResizeMZT(vgWindowSurface, w, h);

#ifdef AM_SRE
{
    // get AmanithVG surface actual dimensions
    VGint surfaceWidth = openvgSurfaceWidthGet();
    VGint surfaceHeight = openvgSurfaceHeightGet();
    // restore XImage's buffer pointer
    blitImage->data = NULL;
    // destroy the XImage
    XDestroyImage(blitImage);
    // create a new XImage structure
    blitImage = XCreateImage(display, CopyFromParent, screenDepth, ZPixmap, 0, NULL, surfaceWidth, surfaceHeight, 32, surfaceWidth * 4);
}
#endif

    // inform tutorial that surface has been resized
    tutorialResize(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());
}

static void windowBuffersSwap(void) {

#ifndef AM_SRE
    // AmanithVG GLE
    glXSwapBuffers(display, glWindow);
#else
    // AmanithVG SRE get drawing surface dimensions and pixels pointer
    VGint surfaceWidth = openvgSurfaceWidthGet();
    VGint surfaceHeight = openvgSurfaceHeightGet();
    void* surfacePixels = (void*)vgPrivGetSurfacePixelsMZT(vgWindowSurface);

    // set XImage's data buffer value with the AmanithVG SRE buffer pointer
    blitImage->data = (char *)surfacePixels;
    // put the buffer onto the window
    XPutImage(display, window, windowGfxContext, blitImage, 0, 0, 0, 0, surfaceWidth, surfaceHeight);
#endif

    // acknowledge AmanithVG that we have performed a swapbuffers
    vgPostSwapBuffersMZT();
}

static void aboutDialog(void) {

    VGint i;
    char tmp[1024];
    char yearStr[64];
    time_t t = time(NULL);
    struct tm *ltm = localtime(&t);

    strcpy(infoMessage, "AmanithVG - www.mazatech.com\n");
    strcat(infoMessage, "Copyright 2004-");
    strftime(yearStr, sizeof(yearStr), "%Y", ltm);
    strcat(infoMessage, yearStr);
    strcat(infoMessage, " by Mazatech Srl. All Rights Reserved.\n\n");

    strcat(infoMessage, "OpenVG driver information:\n\n");
    // vendor
    strcat(infoMessage, "Vendor: ");
    strcat(infoMessage, (const char *)vgGetString(VG_VENDOR));
    strcat(infoMessage, "\n");
    // renderer
    strcat(infoMessage, "Renderer: ");
    strcat(infoMessage, (const char *)vgGetString(VG_RENDERER));
    strcat(infoMessage, "\n");
    // version
    strcat(infoMessage, "Version: ");
    strcat(infoMessage, (const char *)vgGetString(VG_VERSION));
    strcat(infoMessage, "\n");
    // extensions
    strcat(infoMessage, "Extensions: ");
    
    strcpy(tmp, "\n");
    strcat(tmp, (const char *)vgGetString(VG_EXTENSIONS));
    for (i = 0; i < (VGint)strlen(tmp); ++i) {
        if (tmp[i] == ' ') {
            tmp[i] = '\n';
        }
    }
    strcat(infoMessage, tmp);
    displayAbout = VG_TRUE;
    displayHelp = VG_FALSE;
}

static void helpDialog(void) {

    strcpy(infoMessage, "F2: About AmanithVG.\n");
    strcat(infoMessage, "F1: Help.\n");
    strcat(infoMessage, "Mouse: Move gradient control points.\n");
    strcat(infoMessage, "I: Change color interpolation.\n");
    strcat(infoMessage, "S: Change spread mode.\n");
    displayAbout = VG_FALSE;
    displayHelp = VG_TRUE;
}

static void processKeyPressure(KeySym key) {

    // ESC
    if (key == XK_Escape) {
        done = VG_TRUE;
    }
    else
    // F1
    if (key == XK_F1) {
        XClearWindow(display, window);
        helpDialog();
    }
    else
    // F2           
    if (key == XK_F2) {
        XClearWindow(display, window);
        aboutDialog();
    }
    else
    // I
    if (key == XK_i) {
        toggleColorInterpolation();
    }
    else
    // S
    if (key == XK_s) {
        toggleSpreadMode();
    }
}

static void processEvent(XEvent *ev) {

    switch (ev->type) {
        
        case KeyPress:
            displayAbout = VG_FALSE;
            displayHelp = VG_FALSE;
            processKeyPressure(XLookupKeysym(&ev->xkey, 0));
            break;

        case ButtonPress:
            displayAbout = VG_FALSE;
            displayHelp = VG_FALSE;
            if (ev->xbutton.button == Button1) {
                // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
                mouseLeftButtonDown(ev->xbutton.x, openvgSurfaceHeightGet() - ev->xbutton.y);
            }
            else
            if ((ev->xbutton.button == Button2) || (ev->xbutton.button == Button3)) {
                // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
                mouseRightButtonDown(ev->xbutton.x, openvgSurfaceHeightGet() - ev->xbutton.y);
            }
            break;
                
        case ButtonRelease:
            displayAbout = VG_FALSE;
            displayHelp = VG_FALSE;
            if (ev->xbutton.button == Button1) {
                // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
                mouseLeftButtonUp(ev->xbutton.x, openvgSurfaceHeightGet() - ev->xbutton.y);
            }
            else
            if ((ev->xbutton.button == Button2) || (ev->xbutton.button == Button3)) {
                // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
                mouseRightButtonUp(ev->xbutton.x, openvgSurfaceHeightGet() - ev->xbutton.y);
            }
            break;
                
        case MotionNotify:
            displayAbout = VG_FALSE;
            displayHelp = VG_FALSE;
            // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
            mouseMove(ev->xmotion.x, openvgSurfaceHeightGet() - ev->xmotion.y);
            break;
                
        case ClientMessage:
            if ((((XClientMessageEvent *)ev)->message_type == atom_PROTOCOLS) &&
                (((XClientMessageEvent *)ev)->data.l[0] == (long)atom_DELWIN)) {
                done = VG_TRUE;
            }
            break;
    }
}

static void processEvents(void) {

    XEvent event;
    XWindowAttributes windowAttributes;

    if (XCheckWindowEvent(display, window, ExposureMask, &event)) {
        // resizing a window may generate more than one event, and we are only interested into the final size of the window
        while (XCheckWindowEvent(display, window, ExposureMask, &event));
        // get window dimensions
        XGetWindowAttributes(display, window, &windowAttributes);
        // resize AmanithVG surface, in order to match the new window dimensions
        displayAbout = VG_FALSE;
        displayHelp = VG_FALSE;
        windowResize(windowAttributes.width, windowAttributes.height);
    }
    else {
        VGint i = XPending(display);
    
        if (i > 0) {
            // get the next event in queue
            XNextEvent(display, &event);
            processEvent(&event);
            i--;
            for (; i > 0; --i) {
                XNextEvent(display, &event);
            }
        }
    }
}

int main(int argc, char *argv[]) {

    (void)argc;
    (void)argv;

    // create window
    if (!windowCreate(WINDOW_TITLE, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT)) {
        return EXIT_FAILURE;
    }

    // init OpenVG
    if (!openvgInit(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT)) {
        windowDestroy();
        return EXIT_FAILURE;
    }

    // init application
    tutorialInit(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());

    done = VG_FALSE;
    displayHelp = VG_FALSE;
    displayAbout = VG_FALSE;

    // start frames counter
    time0 = getTimeMS();
    framesCounter = 0;

    // enter main loop
    while (!done) {

        // dispatch events, taking care to process keyboard and mouse
        processEvents();
        
        if (displayHelp || displayAbout) {
            textDraw(infoMessage, 10, 10);
        }
        else {
            // update window title (show FPS)
            windowTitleUpdate();
            // draw the scene
            tutorialDraw(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());
            // advance the frames counter
            framesCounter++;
            // present the scene on screen
            windowBuffersSwap();
        }
    }

    // destroy OpenVG resources created by the tutorial
    tutorialDestroy();
    // destroy OpenVG context and surface
    openvgDestroy();
    windowDestroy();
    return EXIT_SUCCESS;
}
