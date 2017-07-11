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
#import <Cocoa/Cocoa.h>
#import <QuartzCore/CVDisplayLink.h>
#import <OpenGL/OpenGL.h>
#include <OpenGL/gl.h>
#include <time.h>
#include <sys/time.h>
#include "tutorial_09.h"

// default window dimensions
#define INITIAL_WINDOW_WIDTH 512
#define INITIAL_WINDOW_HEIGHT 512
#define WINDOW_TITLE "AmanithVG Tutorial 09 - Press F1 for help"

// The 10.12 SDK adds new symbols and immediately deprecates the old ones
#if MAC_OS_X_VERSION_MAX_ALLOWED < 101200
    #define NSAlertStyleInformational NSInformationalAlertStyle
    #define NSWindowStyleMaskTitled NSTitledWindowMask
    #define NSWindowStyleMaskClosable NSClosableWindowMask
    #define NSWindowStyleMaskMiniaturizable NSMiniaturizableWindowMask
    #define NSWindowStyleMaskResizable NSResizableWindowMask
    #define NSEventMaskAny NSAnyEventMask
#endif

/*****************************************************************
                        Global variables
*****************************************************************/
VGboolean done;

/*****************************************************************
                        View (interface)
*****************************************************************/
@interface TutorialView : NSOpenGLView <NSWindowDelegate> {

    // OpenVG context
    void *vgContext;
    // OpenVG surface
    void *vgWindowSurface;
#ifdef AM_SRE
    // OpenGL texture used to blit the AmanithVG SRE surface
    GLuint blitTexture;
#endif
    // a Core Video display link 
    CVDisplayLinkRef displayLink;
    // fps counter
    VGuint time0, time1;
    VGuint framesCounter;
}

/*****************************************************************
                            OpenVG
*****************************************************************/
- (VGboolean) openvgInit :(const VGuint)width :(const VGuint)height;
- (void) openvgDestroy;
- (VGint) openvgSurfaceWidthGet;
- (VGint) openvgSurfaceHeightGet;
- (VGint) openvgSurfaceMaxDimensionGet;
#ifdef AM_SRE
    // setup the texture that will be used to blit AmanithVG SRE surface
    - (void) blitTextureGenerate;
    - (void) blitTextureResize :(const VGuint)width :(const VGuint)height;
    - (void) blitTextureDestroy;
    - (void) blitTextureDraw;
#endif

/*****************************************************************
                       Windowing system
*****************************************************************/
- (void) messageDialog :(const char*)title :(const char*)message;
- (void) aboutDialog;
- (void) helpDialog;
- (VGuint) getTimeMS;
- (void) windowTitleUpdate;
// Core Video display link
- (CVReturn)getFrameForTime :(const CVTimeStamp *)outputTime;
// implementation of NSOpenGLView methods
- (id) initWithFrame :(NSRect)frameRect;
- (void) prepareOpenGL;
- (void) drawRect :(NSRect)dirtyRect;
- (void) reshape;
- (void) dealloc;
// mouse and keyboard events
- (void) mouseDown :(NSEvent *)theEvent;
- (void) mouseUp :(NSEvent *)theEvent;
- (void) mouseDragged:(NSEvent *)theEvent;
- (void) rightMouseDown :(NSEvent *)theEvent;
- (void) rightMouseUp :(NSEvent *)theEvent;
- (void) rightMouseDragged:(NSEvent *)theEvent;
- (void) keyDown :(NSEvent *)theEvent;
- (BOOL) acceptsFirstResponder;
- (BOOL) becomeFirstResponder;
- (BOOL) resignFirstResponder;
- (BOOL) isFlipped;
// menu handlers
- (void) applicationTerminate :(id)sender;

@end

/*****************************************************************
                       View (implementation)
*****************************************************************/
@implementation TutorialView

/*****************************************************************
                            OpenVG
*****************************************************************/
- (VGboolean) openvgInit :(const VGuint)width :(const VGuint)height {

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

- (void) openvgDestroy {

    // unbind context and surface
    vgPrivMakeCurrentMZT(NULL, NULL);
    // destroy OpenVG surface
    vgPrivSurfaceDestroyMZT(vgWindowSurface);
    // destroy OpenVG context
    vgPrivContextDestroyMZT(vgContext);
}

// get the width of OpenVG drawing surface, in pixels
- (VGint) openvgSurfaceWidthGet {

    return vgPrivGetSurfaceWidthMZT(vgWindowSurface);
}

// get the height of OpenVG drawing surface, in pixels
- (VGint) openvgSurfaceHeightGet {

    return vgPrivGetSurfaceHeightMZT(vgWindowSurface);
}

// get the maximum surface dimension supported by the OpenVG backend
- (VGint) openvgSurfaceMaxDimensionGet {

    return vgPrivSurfaceMaxDimensionGetMZT();
}

#ifdef AM_SRE

- (void) blitTextureGenerate {

    // get AmanithVG SRE surface pixels pointer
    void* surfacePixels = (void*)vgPrivGetSurfacePixelsMZT(vgWindowSurface);

    // generate a 2D rectangular texture
    glGenTextures(1, &blitTexture);
    glEnable(GL_TEXTURE_RECTANGLE_ARB);
    glDisable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_RECTANGLE_ARB, blitTexture);
    glTexParameteri(GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_STORAGE_HINT_APPLE, GL_STORAGE_CACHED_APPLE);
    glPixelStorei(GL_UNPACK_CLIENT_STORAGE_APPLE, GL_TRUE);
    glTexParameteri(GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_RECTANGLE_ARB, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    // select best format, in order to avoid swizzling
    glTexImage2D(GL_TEXTURE_RECTANGLE_ARB, 0, GL_RGBA, [self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet], 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, surfacePixels);

    // setup basic OpenGL states
    glEnable(GL_MULTISAMPLE);
    glDisable(GL_LIGHTING);
    glShadeModel(GL_FLAT);
    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    glDisable(GL_CULL_FACE);
    glDisable(GL_ALPHA_TEST);
    glDisable(GL_SCISSOR_TEST);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_BLEND);
    glDepthMask(GL_FALSE);
    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
}

- (void) blitTextureResize :(const VGuint)width :(const VGuint)height {

    // resize the OpenGL texture used to blit AmanithVG SRE drawing surface
    glBindTexture(GL_TEXTURE_RECTANGLE_ARB, blitTexture);
    glTexImage2D(GL_TEXTURE_RECTANGLE_ARB, 0, GL_RGBA, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, vgPrivGetSurfacePixelsMZT(vgWindowSurface));
}

- (void) blitTextureDestroy {

    if (blitTexture != 0) {
        glDeleteTextures(1, &blitTexture);
    }
}

- (void) blitTextureDraw {

    // get AmanithVG surface dimensions and pixels pointer
    VGint surfaceWidth = [self openvgSurfaceWidthGet];
    VGint surfaceHeight = [self openvgSurfaceHeightGet];
    void* surfacePixels = (void*)vgPrivGetSurfacePixelsMZT(vgWindowSurface);
    
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    // simply put a quad, covering the whole window
    glBindTexture(GL_TEXTURE_RECTANGLE_ARB, blitTexture);
    glTexSubImage2D(GL_TEXTURE_RECTANGLE_ARB, 0, 0, 0, surfaceWidth, surfaceHeight, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, surfacePixels);
    glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(-1.0f, 1.0f);
        glTexCoord2f(0.0f, surfaceHeight);
        glVertex2f(-1.0f, -1.0f);
        glTexCoord2f(surfaceWidth, surfaceHeight);
        glVertex2f(1.0f, -1.0f);
        glTexCoord2f(surfaceWidth, 0.0f);
        glVertex2f(1.0f, 1.0f);
    glEnd();
}

#endif // AM_SRE

/*****************************************************************
                       Windowing system
*****************************************************************/
- (void) messageDialog :(const char*)title :(const char*)message {

    NSString* sMessage;
    NSAlert* alert = [[NSAlert alloc] init];

    (void)title;
    [alert addButtonWithTitle:@"OK"];
    // set message
    sMessage = [NSString stringWithCString:message encoding:NSASCIIStringEncoding];
    [alert setMessageText:sMessage];
    [alert setAlertStyle:NSAlertStyleInformational];
    // display the modal dialog
    [alert runModal];
    [alert release];
}

- (void) aboutDialog {

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
    [self messageDialog :"About AmanithVG" :msg];
}

- (void) helpDialog {

    char msg[1024];

    strcpy(msg, "F2: About AmanithVG.\n");
    strcat(msg, "F1: Help.\n");
    strcat(msg, "Mouse: Move text control points.\n");
    [self messageDialog :"Command keys" :msg];
}

// utility functions
- (VGuint) getTimeMS {

    struct timeval tp;
    struct timezone tzp;
    gettimeofday(&tp, &tzp);
    return (VGuint)((tp.tv_sec * 1000) + (tp.tv_usec / 1000));
}

- (void) windowTitleUpdate {

    time1 = [self getTimeMS];
    // print frame rate every second
    if (time1 - time0 > 1000) {
        NSWindow *window = [self window];
        if (window) {
            char title[128];
            VGfloat fps = ((VGfloat)framesCounter * 1000.0f / (VGfloat)(time1 - time0));
            sprintf(title, "(%d fps) "WINDOW_TITLE, (VGint)fps);
            // set window title
            [window setTitle:[NSString stringWithCString:title encoding:NSASCIIStringEncoding]];
        }
        // reset frames counter
        framesCounter = 0;
        time0 = time1;
    }
}

// Core Video display link
- (CVReturn)getFrameForTime :(const CVTimeStamp *)outputTime {

    // deltaTime is unused in this application, but here's how to calculate it using display link info
    // double deltaTime = 1.0 / (outputTime->rateScalar * (double)outputTime->videoTimeScale / (double)outputTime->videoRefreshPeriod);
    (void)outputTime;

    // there is no autorelease pool when this method is called because it will be called from a background thread
    // it's important to create one or app can leak objects
    @autoreleasepool {
        [self drawRect:[self bounds]];
    }

    return kCVReturnSuccess;
}

static CVReturn displayLinkCallback(CVDisplayLinkRef displayLink,
                                    const CVTimeStamp* now,
                                    const CVTimeStamp* outputTime,
                                    CVOptionFlags flagsIn,
                                    CVOptionFlags* flagsOut,
                                    void* displayLinkContext) {

    (void)displayLink;
    (void)now;
    (void)outputTime;
    (void)flagsIn;
    (void)flagsOut;
    
    CVReturn result = [(__bridge TutorialView*)displayLinkContext getFrameForTime:outputTime];
    return result;
}

// implementation of NSOpenGLView methods
- (id) initWithFrame :(NSRect)frameRect {

    NSOpenGLPixelFormatAttribute attributes[] = {
        NSOpenGLPFAAccelerated,
        NSOpenGLPFANoRecovery,
        NSOpenGLPFADoubleBuffer,
        NSOpenGLPFAOpenGLProfile, NSOpenGLProfileVersionLegacy,
        NSOpenGLPFAColorSize, 32,
    #ifndef AM_SRE
        // AmanithVG GLE
        NSOpenGLPFASupersample,
        NSOpenGLPFADepthSize, 24,
        NSOpenGLPFAStencilSize, 8,
        NSOpenGLPFASampleBuffers, 1,
        NSOpenGLPFASamples, 8,
    #endif
        (NSOpenGLPixelFormatAttribute)0
    };
    NSOpenGLPixelFormat *format = [[NSOpenGLPixelFormat alloc] initWithAttributes:attributes];

    if (!format) {
        NSLog(@"Unable to create pixel format.");
        exit(EXIT_FAILURE);
    }

    self = [super initWithFrame: frameRect pixelFormat: format];
    [format release];

    // initialize private members
    vgContext = NULL;
    vgWindowSurface = NULL;
#ifdef AM_SRE
    blitTexture = 0;
#endif
    return self;
}

- (void) prepareOpenGL {

    [super prepareOpenGL];

    // the reshape function may have changed the thread to which our OpenGL
    // context is attached before prepareOpenGL and initGL are called.  So call
    // makeCurrentContext to ensure that our OpenGL context current to this 
    // thread (i.e. makeCurrentContext directs all OpenGL calls on this thread
    // to [self openGLContext])
    [[self openGLContext] makeCurrentContext];

    // do not synchronize buffer swaps with vertical refresh rate
    GLint swapInt = 0;
    [[self openGLContext] setValues:&swapInt forParameter:NSOpenGLCPSwapInterval];

    // get frame dimensions
    NSSize bound = [self frame].size;
    VGint width = (VGint)bound.width;
    VGint height = (VGint)bound.height;

    // init OpenVG
    if ([self openvgInit :width :height]) {
    #ifdef AM_SRE
        // create and setup the texture used to blit AmanithVG SRE surface
        [self blitTextureGenerate];
    #endif
    }
    else {
        NSLog(@"Unable to initialize AmanithVG.");
        exit(EXIT_FAILURE);
    }

    // init tutorial application (OpenVG related code)
    tutorialInit([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);

    // create a display link capable of being used with all active displays
    CVDisplayLinkCreateWithActiveCGDisplays(&displayLink);
    
    // set the renderer output callback function
    CVDisplayLinkSetOutputCallback(displayLink, &displayLinkCallback, (__bridge void*)self);
    
    // set the display link for the current renderer
    CGLContextObj cglContext = [[self openGLContext] CGLContextObj];
    CGLPixelFormatObj cglPixelFormat = [[self pixelFormat] CGLPixelFormatObj];
    CVDisplayLinkSetCurrentCGDisplayFromOpenGLContext(displayLink, cglContext, cglPixelFormat);
    
    // activate the display link
    CVDisplayLinkStart(displayLink);

    // start frame counter
    time0 = [self getTimeMS];
    framesCounter = 0;
}

- (void) drawRect :(NSRect)dirtyRect {

    (void)dirtyRect;

    if ([self lockFocusIfCanDraw]) {
    
        [[self openGLContext] makeCurrentContext];

        // we draw on a secondary thread through the display link when resizing the view, -reshape is called automatically on the main thread
        // add a mutex around to avoid the threads accessing the context simultaneously when resizing
        CGLLockContext([[self openGLContext] CGLContextObj]);

        // draw OpenVG content
        tutorialDraw([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
    #ifdef AM_SRE
        // blit AmanithVG SRE drawing surface, using a texture
        [self blitTextureDraw];
    #endif

        // copy a double-buffered context’s back buffer to its front buffer
        CGLFlushDrawable([[self openGLContext] CGLContextObj]);

        // acknowledge AmanithVG that we have performed a swapbuffers
        vgPostSwapBuffersMZT();

        // unlock the context
        CGLUnlockContext([[self openGLContext] CGLContextObj]);
        [self unlockFocus];

        // advance the frames counter
        framesCounter++;
    }
}

// this method is called whenever the window/control is reshaped, it is also called when the control is first opened
- (void) reshape {

    [super reshape];

    if ([self lockFocusIfCanDraw]) {

        [[self openGLContext] makeCurrentContext];

        // we draw on a secondary thread through the display link, however, when resizing the view, -drawRect is called on the main thread
        // add a mutex around to avoid the threads accessing the context simultaneously when resizing
        CGLLockContext([[self openGLContext] CGLContextObj]);

        // get new dimensions
        NSSize bound = [self frame].size;
        
        // resize AmanithVG drawing surface
        vgPrivSurfaceResizeMZT(vgWindowSurface, (VGint)bound.width, (VGint)bound.height);
        VGint surfaceWidth = [self openvgSurfaceWidthGet];
        VGint surfaceHeight = [self openvgSurfaceHeightGet];
        tutorialResize(surfaceWidth, surfaceHeight);

    #ifdef AM_SRE
        // resize OpenGL viewport
        glViewport(0, 0, (GLsizei)bound.width, (GLsizei)bound.height);
        // resize the OpenGL texture used to blit AmanithVG SRE drawing surface
        [self blitTextureResize :surfaceWidth :surfaceHeight];
    #endif

        // unlock the context
        CGLUnlockContext([[self openGLContext] CGLContextObj]);
        [self unlockFocus];
    }
}

- (void) dealloc {

    // stop the display link BEFORE releasing anything in the view
    // otherwise the display link thread may call into the view and crash
    // when it encounters something that has been release
    CVDisplayLinkStop(displayLink);

    // release the display link
    CVDisplayLinkRelease(displayLink);

    // destroy OpenVG resources created by the tutorial
    tutorialDestroy();

#ifdef AM_SRE
    // destroy texture used to blit AmanithVG SRE surface
    [self blitTextureDestroy];
#endif
    // destroy OpenVG context and surface
    [self openvgDestroy];

    [super dealloc];
}

// mouse and keyboard events
- (void) mouseDown: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint: p fromView: nil];
    mouseLeftButtonDown(p.x, p.y);
}

- (void) mouseUp: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint: p fromView: nil];
    mouseLeftButtonUp(p.x, p.y);
}

- (void) mouseDragged:(NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint: p fromView: nil];
    mouseMove(p.x, p.y);
}

- (void) rightMouseDown: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint: p fromView: nil];
    mouseRightButtonDown(p.x, p.y);
}

- (void) rightMouseUp: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint: p fromView: nil];
    mouseRightButtonUp(p.x, p.y);
}

- (void) rightMouseDragged:(NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint: p fromView: nil];
    mouseMove(p.x, p.y);
}

- (void) keyDown:(NSEvent *)theEvent {

    char *chars = (char *)[[theEvent characters] cStringUsingEncoding: NSMacOSRomanStringEncoding];

    if (chars) {
        switch (chars[0]) {
            // ESC
            case 27:
                done = VG_TRUE;
                break;
            default:
                [super keyDown:theEvent];
                break;
        }
    }
    else {
        switch ([theEvent keyCode]) {
            // F1
            case 122:
                [self helpDialog];
                break;
            // F2
            case 120:
                [self aboutDialog];
                break;
            default:
                [super keyDown:theEvent];
                break;
        }
    }
}

- (BOOL) acceptsFirstResponder {
    
    // as first responder, the receiver is the first object in the responder chain to be sent key events and action messages
    return YES;
}

- (BOOL) becomeFirstResponder {

    return YES;
}

- (BOOL) resignFirstResponder {

    return YES;
}

- (BOOL) isFlipped {

    return NO;
}

// menu handlers
- (void) applicationTerminate :(id)sender {

    (void)sender;
    // exit from main loop
    done = VG_TRUE;
}

// from NSWindowDelegate
- (void)windowWillClose:(NSNotification *)note {

    (void)note;

    // Stop the display link when the window is closing because default
    // OpenGL render buffers will be destroyed. If display link continues to
    // fire without renderbuffers, OpenGL draw calls will set errors.
    CVDisplayLinkStop(displayLink);
    done = VG_TRUE;
}

@end

/*****************************************************************
                              Main
*****************************************************************/
void applicationMenuPopulate(NSMenu* subMenu,
                             TutorialView* view) {

    // quit application
    NSMenuItem* menuItem = [subMenu addItemWithTitle:[NSString stringWithFormat:@"%@", NSLocalizedString(@"Quit", nil)] action:@selector(applicationTerminate:) keyEquivalent:@"q"];
    [menuItem setTarget:view];
}

void mainMenuPopulate(TutorialView* view) {

    NSMenuItem* menuItem;
    NSMenu* subMenu;
    // create main menu = menu bar
    NSMenu* mainMenu = [[NSMenu alloc] initWithTitle:@"MainMenu"];
    
    // the titles of the menu items are for identification purposes only and shouldn't be localized; the strings in the menu bar come
    // from the submenu titles, except for the application menu, whose title is ignored at runtime
    menuItem = [mainMenu addItemWithTitle:@"Apple" action:NULL keyEquivalent:@""];
    subMenu = [[NSMenu alloc] initWithTitle:@"Apple"];
    [NSApp performSelector:@selector(setAppleMenu:) withObject:subMenu];
    applicationMenuPopulate(subMenu, view);
    [mainMenu setSubmenu:subMenu forItem:menuItem];
    [NSApp setMainMenu:mainMenu];
}

void applicationMenuCreate(TutorialView* view) {

    mainMenuPopulate(view);
}

int main(int argc, char *argv[]) {

    (void)argc;
    (void)argv;

    @autoreleasepool {

        NSRect frame = NSMakeRect(0, 0, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);

        // get application
        NSApplication* app = [NSApplication sharedApplication];
        [NSApp setActivationPolicy: NSApplicationActivationPolicyRegular];

        // create the window
        NSWindow* window = [[NSWindow alloc] initWithContentRect:frame styleMask:NSWindowStyleMaskTitled | NSWindowStyleMaskClosable | NSWindowStyleMaskMiniaturizable | NSWindowStyleMaskResizable backing:NSBackingStoreBuffered defer: TRUE];
        [window setAcceptsMouseMovedEvents:YES];
        [window setTitle: @ WINDOW_TITLE];

        // create the OpenGL view
        TutorialView* view = [[TutorialView alloc] initWithFrame: frame];

        // link the view to the window
        [window setDelegate: view];
        [window setContentView: view];
        [window makeFirstResponder: view];
        [window setMaxSize: NSMakeSize([view openvgSurfaceMaxDimensionGet], [view openvgSurfaceMaxDimensionGet])];
        [view release];

        // center the window
        [window center];
        [window makeKeyAndOrderFront: nil];

        // create and populate the menu
        applicationMenuCreate(view);
        [app finishLaunching];

        // enter main loop
        done = VG_FALSE;
        while (!done) {
            // dispatch events
            NSEvent* event = [app nextEventMatchingMask: NSEventMaskAny untilDate: [NSDate dateWithTimeIntervalSinceNow: 0.0] inMode: NSDefaultRunLoopMode dequeue: true];
            if (event != nil) {
                [app sendEvent: event];
                [app updateWindows];
            }
            else {
                // modify UI (in this case window title, in order to show FPS) within the main thread
                [view windowTitleUpdate];
            }
        }

    } // @autoreleasepool

    return EXIT_SUCCESS;
}
