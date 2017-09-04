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
#include <windows.h>
#include <stdio.h>
#include <time.h>
#ifndef AM_SRE
    // AmanithVG GLE uses OpenGL as rendering backend
    #include <GL/gl.h>
    #include "wglext.h"
#endif
#include "tutorial_02.h"
#include "resource.h"
#if defined(_MSC_VER)
    // take care of Visual Studio .Net 2005+ C deprecations
    #if _MSC_VER >= 1400
        #if !defined(_CRT_SECURE_NO_DEPRECATE)
            #define _CRT_SECURE_NO_DEPRECATE 1
        #endif
        #if !defined(_CRT_NONSTDC_NO_DEPRECATE)
            #define _CRT_NONSTDC_NO_DEPRECATE 1
        #endif
        #pragma warning (disable:4996)
    #endif
#endif
#define CLASS_NAME "AmanithVG"
#define WINDOW_TITLE "AmanithVG Tutorial 02 - Press F1 for help"

// default window dimensions
#define INITIAL_WINDOW_WIDTH 512
#define INITIAL_WINDOW_HEIGHT 512

// Windows variables
HDC deviceContext = NULL;
HWND nativeWindow = NULL;
HINSTANCE applicationInstance;
#ifndef AM_SRE
    // AmanithVG GLE
    HGLRC glRenderingContext = NULL;
    VGint multisampleFormat = 0;
    VGboolean multisampleSupported = VG_FALSE;
#else
    // AmanithVG SRE
    VGubyte vgSurfaceBitmapBuffer[sizeof(BITMAPINFO) + 16];
    BITMAPINFO* vgSurfaceBitmapInfo;
#endif
// pressed keys buffer
VGboolean keysPressed[256] = { VG_FALSE };
// OpenVG variables
void* vgContext = NULL;
void* vgWindowSurface = NULL;
// FPS counter
VGuint time0, time1;
VGuint framesCounter;

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
static void messageDialog(const char* title,
                          const char* message) {

    MessageBox(NULL, message, title, MB_OK);
}

static VGuint getTimeMS(void) {

    return (VGuint)GetTickCount();
}

LRESULT CALLBACK windowMessagesHandler(HWND hWnd,
                                       UINT uMsg,
                                       WPARAM wParam,
                                       LPARAM lParam) {

    MINMAXINFO* minmaxInfo;

    switch (uMsg) {

        case WM_SYSCOMMAND:
            // disable screensaver and monitor powersave
            switch (wParam) {
                case SC_SCREENSAVE:
                case SC_MONITORPOWER:
                return 0;
            }
            break;

        case WM_CLOSE:
            PostQuitMessage(0);
            return 0;

        case WM_KEYDOWN:
            keysPressed[wParam] = VG_TRUE;
            return 0;

        case WM_KEYUP:
            keysPressed[wParam] = VG_FALSE;
            return 0;

        case WM_GETMINMAXINFO:
            // clamp window dimensions according to the maximum surface dimension supported by the OpenVG backend
            minmaxInfo = (MINMAXINFO*)lParam;
            minmaxInfo->ptMaxSize.x = openvgSurfaceMaxDimensionGet();
            minmaxInfo->ptMaxSize.y = minmaxInfo->ptMaxSize.x;
            minmaxInfo->ptMaxTrackSize = minmaxInfo->ptMaxSize;
            return 0;

        case WM_SIZE:
            // resize AmanithVG surface
            vgPrivSurfaceResizeMZT(vgWindowSurface, LOWORD(lParam), HIWORD(lParam));
            // inform tutorial that surface has been resized
            tutorialResize(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());
            return 0;

        case WM_LBUTTONDOWN:
            // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
            mouseLeftButtonDown(LOWORD(lParam), openvgSurfaceHeightGet() - HIWORD(lParam));
            return 0;

        case WM_RBUTTONDOWN:
            // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
            mouseRightButtonDown(LOWORD(lParam), openvgSurfaceHeightGet() - HIWORD(lParam));
            return 0;
        
        case WM_LBUTTONUP:
            // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
            mouseLeftButtonUp(LOWORD(lParam), openvgSurfaceHeightGet() - HIWORD(lParam));
            return 0;

        case WM_RBUTTONUP:
            // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
            mouseRightButtonUp(LOWORD(lParam), openvgSurfaceHeightGet() - HIWORD(lParam));
            return 0;

        case WM_MOUSEMOVE:
            // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
            mouseMove(LOWORD(lParam), openvgSurfaceHeightGet() - HIWORD(lParam));
            return 0;
    }

    return DefWindowProc(hWnd, uMsg, wParam, lParam);
}

static void windowDestroy(void) {

#ifndef AM_SRE
    // release OpenGL context
    if (glRenderingContext) {
        wglMakeCurrent(NULL, NULL);
        wglDeleteContext(glRenderingContext);
    }
#endif

    // release device context
    ReleaseDC(nativeWindow, deviceContext);
    // destroy window
    DestroyWindow(nativeWindow);
    // unregister class
    UnregisterClass(CLASS_NAME, applicationInstance);
}

static HWND windowCreateImpl(const char* title,
                             const VGuint width,
                             const VGuint height,
                             const DWORD dwExStyle,
                             const DWORD dwStyle) {

    WNDCLASS wc;
    RECT rect;
    VGint x, y, w, h;

    // register window class
    wc.style = CS_OWNDC | CS_VREDRAW | CS_HREDRAW;
    wc.lpfnWndProc = windowMessagesHandler;
    wc.cbClsExtra = 0;
    wc.cbWndExtra = 0;

    applicationInstance = GetModuleHandle(NULL);
    wc.hInstance = applicationInstance;
    wc.hIcon = LoadIcon(applicationInstance, MAKEINTRESOURCE(IDI_ICON1));
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = NULL;
    wc.lpszMenuName = NULL;
    wc.lpszClassName = CLASS_NAME;
    RegisterClass(&wc);
        
    // calculate window size
    rect.left = 0;
    rect.top = 0;
    rect.right = width;
    rect.bottom = height;
    AdjustWindowRect(&rect, dwStyle, 0);
    w = rect.right - rect.left;
    h = rect.bottom - rect.top;

    // center window on the screen
    x = (GetSystemMetrics(SM_CXSCREEN) - w) / 2;
    y = (GetSystemMetrics(SM_CYSCREEN) - h) / 2;

    // create window
    return CreateWindowEx(dwExStyle, CLASS_NAME, title, dwStyle, x, y, w, h, NULL, NULL, applicationInstance, NULL);
}

#ifndef AM_SRE

// check for the support of the given OpenGL extension
static VGboolean openglExtensionSupported(const char* extension) {

    const char *p;
    const size_t extlen = strlen(extension);
    const char* supported = NULL;
    PROC wglGetExtString = wglGetProcAddress("wglGetExtensionsStringARB");

    if (wglGetExtString) {
        supported = ((char*(__stdcall*)(HDC))wglGetExtString)(wglGetCurrentDC());
    }
    if (supported == NULL) {
        supported = (char*)glGetString(GL_EXTENSIONS);
    }
    if (supported == NULL) {
        return VG_FALSE;
    }
    for (p = supported;; p++) {
        p = strstr(p, extension);
        if (p == NULL) {
            return VG_FALSE;
        }
        if ((p == supported || p[-1] == ' ') && (p[extlen] == '\0' || p[extlen] == ' ')) {
            return VG_TRUE;
        }
    }
    return VG_FALSE;
}

static VGboolean windowMultisampleInit(void) {

    VGint pixelFormat;
    VGint valid;
    VGuint numFormats;
    PFNWGLCHOOSEPIXELFORMATARBPROC wglChoosePixelFormatARB;
    VGfloat fAttributes[] = {
        0.0f,
        0.0f
    };
    VGint iAttributes[] = {
        WGL_DRAW_TO_WINDOW_ARB, GL_TRUE,
        WGL_SUPPORT_OPENGL_ARB, GL_TRUE,
        WGL_ACCELERATION_ARB, WGL_FULL_ACCELERATION_ARB,
        WGL_COLOR_BITS_ARB, 24,
        WGL_ALPHA_BITS_ARB, 8,
        WGL_DEPTH_BITS_ARB, 16,
        WGL_STENCIL_BITS_ARB, 8,
        WGL_DOUBLE_BUFFER_ARB, GL_TRUE,
        WGL_SAMPLE_BUFFERS_ARB, GL_TRUE,
        WGL_SAMPLES_ARB, 8,
        0, 0
    };

    // see if the string exists in WGL
    if (!openglExtensionSupported("WGL_ARB_multisample")) {
        multisampleSupported = VG_FALSE;
        return VG_FALSE;
    }

    // get our pixel format
    wglChoosePixelFormatARB = (PFNWGLCHOOSEPIXELFORMATARBPROC)wglGetProcAddress("wglChoosePixelFormatARB");
    if (!wglChoosePixelFormatARB) {
        multisampleSupported = VG_FALSE;
        return VG_FALSE;
    }

    // first we check to see if we can get a pixel format for 8 samples
    valid = wglChoosePixelFormatARB(deviceContext, iAttributes, fAttributes, 1, &pixelFormat, &numFormats);
    if (valid && numFormats >= 1) {
        multisampleSupported = VG_TRUE;
        multisampleFormat = pixelFormat;
        return VG_TRUE;
    }

    // our pixel format with 8 samples failed, test for 6 samples
    iAttributes[19] = 6;
    valid = wglChoosePixelFormatARB(deviceContext, iAttributes, fAttributes, 1, &pixelFormat, &numFormats);
    if (valid && numFormats >= 1) {
        multisampleSupported = VG_TRUE;
        multisampleFormat = pixelFormat;
        return VG_TRUE;
    }

    // our pixel format with 6 samples failed, test for 4 samples
    iAttributes[19] = 4;
    valid = wglChoosePixelFormatARB(deviceContext, iAttributes, fAttributes, 1, &pixelFormat, &numFormats);
    if (valid && numFormats >= 1) {
        multisampleSupported = VG_TRUE;
        multisampleFormat = pixelFormat;
        return VG_TRUE;
    }

    // our pixel format with 4 samples failed, test for 2 samples
    iAttributes[19] = 2;
    valid = wglChoosePixelFormatARB(deviceContext, iAttributes, fAttributes, 1, &pixelFormat, &numFormats);
    if (valid && numFormats >= 1) {
        multisampleSupported = VG_TRUE;
        multisampleFormat = pixelFormat;
        return VG_TRUE;
    }

    multisampleSupported = VG_FALSE;
    multisampleFormat = 0;
    return VG_FALSE;
}

#endif

static VGboolean windowCreate(const char* title,
                              const VGuint width,
                              const VGuint height) {

    // create window
    nativeWindow = windowCreateImpl(title, width, height, WS_EX_APPWINDOW | WS_EX_WINDOWEDGE, WS_TILEDWINDOW | WS_CLIPSIBLINGS | WS_CLIPCHILDREN);
    if (!nativeWindow) {
        return VG_FALSE;
    }

    // get window dc
    deviceContext = GetDC(nativeWindow);
    if (!deviceContext) {
        return VG_FALSE;
    }

#ifndef AM_SRE
{
    // AmanithVG GLE
    GLuint pixelFormat;
    PFNWGLGETEXTENSIONSSTRINGEXTPROC wglGetExtensionsStringEXT = NULL;
    static PIXELFORMATDESCRIPTOR pfd = {
        sizeof(PIXELFORMATDESCRIPTOR),
        1,                                      // Version Number
        PFD_DRAW_TO_WINDOW |                    // Format Must Support Window
        PFD_SUPPORT_OPENGL |                    // Format Must Support OpenGL
        PFD_DOUBLEBUFFER,                       // Must Support Double Buffering
        PFD_TYPE_RGBA,                          // Request An RGBA Format
        24,                                     // Select Our Color Depth
        0, 0, 0, 0, 0, 0,                       // Color Bits Ignored
        8,                                      // 8bit Alpha Buffer
        0,                                      // Shift Bit Ignored
        0,                                      // No Accumulation Buffer
        0, 0, 0, 0,                             // Accumulation Bits Ignored
        16,                                     // 16Bit Z-Buffer (Depth Buffer)  
        8,                                      // 8 bits Stencil Buffer
        0,                                      // No Auxiliary Buffer
        PFD_MAIN_PLANE,                         // Main Drawing Layer
        0,                                      // Reserved
        0, 0, 0                                 // Layer Masks Ignored
    };

    if (!multisampleSupported) {
        if (!(pixelFormat = ChoosePixelFormat(deviceContext, &pfd))) {
            windowDestroy();
            MessageBox(NULL, "Can't find a suitable pixel format.", "ERROR", MB_OK | MB_ICONEXCLAMATION);
            return VG_FALSE;
        }
    }
    else {
        pixelFormat = multisampleFormat;    
    }
        
    if (!SetPixelFormat(deviceContext, pixelFormat, &pfd)) {
        windowDestroy();
        MessageBox(NULL, "Can't set the pixel format.", "ERROR", MB_OK | MB_ICONEXCLAMATION);
        return VG_FALSE;
    }

    glRenderingContext = wglCreateContext(deviceContext);
    if (!glRenderingContext) {
        windowDestroy();
        MessageBox(NULL, "Can't create a GL rendering context.", "ERROR", MB_OK | MB_ICONEXCLAMATION);
        return VG_FALSE;
    }

    if (!wglMakeCurrent(deviceContext, glRenderingContext)) {
        windowDestroy();
        MessageBox(NULL, "Can't activate the GL rendering context.", "ERROR", MB_OK | MB_ICONEXCLAMATION);
        return VG_FALSE;
    }
    
    if (!multisampleSupported) {
        if (windowMultisampleInit()) {
            windowDestroy();
            return windowCreate(title, width, height);
        }
    }

    // check WGL_EXT_swap_control support
    wglGetExtensionsStringEXT = (PFNWGLGETEXTENSIONSSTRINGEXTPROC)wglGetProcAddress("wglGetExtensionsStringEXT");
    if (wglGetExtensionsStringEXT != NULL && strstr(wglGetExtensionsStringEXT(), "WGL_EXT_swap_control")) {
        PFNWGLSWAPINTERVALEXTPROC wglSwapIntervalEXT = (PFNWGLSWAPINTERVALEXTPROC)wglGetProcAddress("wglSwapIntervalEXT");
        // disable vsync
        if (wglSwapIntervalEXT != NULL) {
            wglSwapIntervalEXT(0);
        }
    }
    
    return VG_TRUE;
}
#else
{
    VGuint i;
    // AmanithVG SRE
    for (i = 0; i < sizeof(BITMAPINFOHEADER) + 16; ++i) {
        vgSurfaceBitmapBuffer[i] = 0;
    }
    // create bitmap header
    vgSurfaceBitmapInfo = (BITMAPINFO *)&vgSurfaceBitmapBuffer;
    vgSurfaceBitmapInfo->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
    vgSurfaceBitmapInfo->bmiHeader.biPlanes = 1;
    vgSurfaceBitmapInfo->bmiHeader.biCompression = BI_BITFIELDS;
    return VG_TRUE;
}
#endif
}

static void windowTitleUpdate(void) {

    time1 = getTimeMS();
    // print frame rate every second
    if (time1 - time0 > 1000) {
        char title[128];
        VGfloat fps = ((VGfloat)framesCounter * 1000.0f / (VGfloat)(time1 - time0));
        sprintf(title, "(%d fps) "WINDOW_TITLE, (VGint)fps);
        SetWindowText(nativeWindow, title);
        // reset frames counter
        framesCounter = 0;
        time0 = time1;
    }
}

static void windowBuffersSwap(void) {

#ifndef AM_SRE
    // AmanithVG GLE
    SwapBuffers(deviceContext);
#else
    // AmanithVG SRE get drawing surface dimensions and pixels pointer
    VGint surfaceWidth = openvgSurfaceWidthGet();
    VGint surfaceHeight = openvgSurfaceHeightGet();
    VGImageFormat surfaceFormat = vgPrivGetSurfaceFormatMZT(vgWindowSurface);
    void* surfacePixels = (void*)vgPrivGetSurfacePixelsMZT(vgWindowSurface);

    switch (surfaceFormat) {

        // RGB{A,X} channel ordering
        case VG_sRGBX_8888:
        case VG_sRGBA_8888:
        case VG_sRGBA_8888_PRE:
        case VG_lRGBX_8888:
        case VG_lRGBA_8888:
        case VG_lRGBA_8888_PRE:
            vgSurfaceBitmapInfo->bmiHeader.biBitCount = 32;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[0] = 0xFF000000;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[1] = 0x00FF0000;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[2] = 0x0000FF00;
            break;

        // {A,X}RGB channel ordering
        case VG_sXRGB_8888:
        case VG_sARGB_8888:
        case VG_sARGB_8888_PRE:
        case VG_lXRGB_8888:
        case VG_lARGB_8888:
        case VG_lARGB_8888_PRE:
            vgSurfaceBitmapInfo->bmiHeader.biBitCount = 32;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[0] = 0x00FF0000;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[1] = 0x0000FF00;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[2] = 0x000000FF;
            break;

        // BGR{A,X} channel ordering
        case VG_sBGRX_8888:
        case VG_sBGRA_8888:
        case VG_sBGRA_8888_PRE:
        case VG_lBGRX_8888:
        case VG_lBGRA_8888:
        case VG_lBGRA_8888_PRE:
            vgSurfaceBitmapInfo->bmiHeader.biBitCount = 32;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[0] = 0x0000FF00;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[1] = 0x00FF0000;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[2] = 0xFF000000;
            break;

        // {A,X}BGR channel ordering
        case VG_sXBGR_8888:
        case VG_sABGR_8888:
        case VG_sABGR_8888_PRE:
        case VG_lXBGR_8888:
        case VG_lABGR_8888:
        case VG_lABGR_8888_PRE:
            vgSurfaceBitmapInfo->bmiHeader.biBitCount = 32;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[0] = 0x000000FF;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[1] = 0x0000FF00;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[2] = 0x00FF0000;
            break;

        // support for AmanithVG SRE Lite (16bpp internal drawing surfaces)
        case VG_sRGB_565:
            vgSurfaceBitmapInfo->bmiHeader.biBitCount = 16;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[0] = 0xF800;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[1] = 0x07E0;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[2] = 0x001F;
            break;

        case VG_sBGR_565:
            vgSurfaceBitmapInfo->bmiHeader.biBitCount = 16;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[0] = 0x001F;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[1] = 0x07E0;
            ((unsigned long *)vgSurfaceBitmapInfo->bmiColors)[2] = 0xF800;
            break;

        default:
            break;
    }

    vgSurfaceBitmapInfo->bmiHeader.biWidth = surfaceWidth;
    vgSurfaceBitmapInfo->bmiHeader.biHeight = -surfaceHeight;
    SetDIBitsToDevice(deviceContext, 0, 0, surfaceWidth, surfaceHeight, 0, 0, 0, surfaceHeight, surfacePixels, vgSurfaceBitmapInfo, DIB_RGB_COLORS);
#endif

    // acknowledge AmanithVG that we have performed a swapbuffers
    vgPostSwapBuffersMZT();
}

static void aboutDialog(void) {

    char msg[2048];
    char yearStr[64];
    time_t t = time(NULL);
    struct tm *ltm = localtime(&t);

    strcpy(msg, "AmanithVG - www.mazatech.com\n");
    strcat(msg, "Copyright 2004-");
    strftime(yearStr, sizeof(yearStr), "%Y", ltm);
    strcat(msg, yearStr);
    strcat(msg, " by Mazatech Srl. All Rights Reserved.\n\n");

    strcat(msg, "OpenVG driver information:\n\n");
    // vendor
    strcat(msg, "Vendor: ");
    strcat(msg, (const char *)vgGetString(VG_VENDOR));
    strcat(msg, "\n");
    // renderer
    strcat(msg, "Renderer: ");
    strcat(msg, (const char *)vgGetString(VG_RENDERER));
    strcat(msg, "\n");
    // version
    strcat(msg, "Version: ");
    strcat(msg, (const char *)vgGetString(VG_VERSION));
    strcat(msg, "\n");
    // extensions
    strcat(msg, "Extensions: ");
    strcat(msg, (const char *)vgGetString(VG_EXTENSIONS));
    strcat(msg, "\n\n");
    messageDialog("About AmanithVG", msg);
}

static void helpDialog(void) {

    char msg[1024];

    strcpy(msg, "F2: About AmanithVG.\n");
    strcat(msg, "F1: Help.\n");
    strcat(msg, "Mouse: Move gradient control points.\n");
    strcat(msg, "I: Change color interpolation.\n");
    strcat(msg, "S: Change spread mode.\n");
    messageDialog("Command keys", msg);
}

static VGboolean processKeysPressure(void) {

    VGboolean closeApp = VG_FALSE;

    // ESC
    if (keysPressed[VK_ESCAPE]) {
        keysPressed[VK_ESCAPE] = VG_FALSE;
        closeApp = VG_TRUE;
    }
    else
    // F1
    if (keysPressed[VK_F1]) {
        keysPressed[VK_F1] = VG_FALSE;
        helpDialog();
    }
    else
    // F2           
    if (keysPressed[VK_F2]) {
        keysPressed[VK_F2] = VG_FALSE;
        aboutDialog();
    }
    else
    // I
    if ((keysPressed['i']) || (keysPressed['I'])) {
        keysPressed['i'] = VG_FALSE;
        keysPressed['I'] = VG_FALSE;
        toggleColorInterpolation();
    }
    else
    // S
    if ((keysPressed['s']) || (keysPressed['S'])) {
        keysPressed['s'] = VG_FALSE;
        keysPressed['S'] = VG_FALSE;
        toggleSpreadMode();
    }

    return closeApp;
}

int WINAPI WinMain(HINSTANCE hInstance,
                   HINSTANCE hPrevInstance,
                   LPSTR lpCmdLine,
                   int nCmdShow) {

    MSG msg;
    VGboolean done = VG_FALSE;

    (void)hInstance;
    (void)hPrevInstance;
    (void)lpCmdLine;
    (void)nCmdShow;

    // create window
    if (!windowCreate(WINDOW_TITLE, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT)) {
        return 0;
    }

    // init OpenVG
    if (!openvgInit(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT)) {
        windowDestroy();
        return 0;
    }

    // init application
    tutorialInit(openvgSurfaceWidthGet(), openvgSurfaceHeightGet());

    // show window
    ShowWindow(nativeWindow, SW_NORMAL);
    SetForegroundWindow(nativeWindow);
    SetFocus(nativeWindow);

    // start frames counter
    time0 = getTimeMS();
    framesCounter = 0;

    // enter main loop
    while (!done) {

        if (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE)) {
            if (msg.message == WM_QUIT) {
                done = VG_TRUE;
            }
            else {
                TranslateMessage(&msg);
                DispatchMessage(&msg);
            }
        }
        else {
            // handle key pressure
            done = processKeysPressure();
            if (!done) {
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
    }

    // destroy OpenVG resources created by the tutorial
    tutorialDestroy();
    // destroy OpenVG context and surface
    openvgDestroy();
    windowDestroy();
    return (int)(msg.wParam);
}
