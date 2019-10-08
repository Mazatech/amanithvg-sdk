/****************************************************************************
** Copyright (C) 2004-2019 Mazatech S.r.l. All rights reserved.
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
#ifdef AM_SRE
    #import <Metal/Metal.h>
    #import <MetalKit/MetalKit.h>
    // header shared between C code here, which executes Metal API commands, and .metal files, which uses these types as inputs to the shaders.
    #import "ShaderTypes.h"
#else
    #import <QuartzCore/CVDisplayLink.h>
    #import <OpenGL/OpenGL.h>
    #include <OpenGL/gl.h>
#endif
#include <time.h>
#include <sys/time.h>
#include "tutorial_03.h"

// default window dimensions
#define INITIAL_WINDOW_WIDTH 512
#define INITIAL_WINDOW_HEIGHT 512
#define WINDOW_TITLE "AmanithVG Tutorial 03 - Press F1 for help"

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
#ifdef AM_SRE
@interface TutorialView : MTKView <MTKViewDelegate, NSWindowDelegate> {
    // Metal command queue
    id<MTLCommandQueue> mtlCommandQueue;
    id<MTLLibrary> mtlLibrary;
    // Metal rendering pipelines
    id<MTLRenderPipelineState> mtlSurfacePipelineState;
    // Metal texture used to blit the AmanithVG SRE surface
    id<MTLTexture> blitTexture;
    MTLTextureDescriptor* blitTextureDescriptor;
#else
@interface TutorialView : NSOpenGLView <NSWindowDelegate> {
    // a Core Video display link 
    CVDisplayLinkRef displayLink;
#endif
    // keep track of backing bounds
    VGint colorRenderBufferWidth;
    VGint colorRenderBufferHeight;
    // OpenVG context
    void* vgContext;
    // OpenVG surface
    void* vgWindowSurface;
    // fps counter
    VGuint time0, time1;
    VGuint framesCounter;
}

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

- (const void*) openvgSurfacePixelsGet {

    return vgPrivGetSurfacePixelsMZT(vgWindowSurface);
}

- (void) blitTextureResize :(const VGuint)width :(const VGuint)height {

    [self blitTextureDestroy];
    // set texture dimensions, in pixels
    blitTextureDescriptor.width = width;
    blitTextureDescriptor.height = height;
    // create the texture from the device by using the descriptor
    blitTexture = [[self device] newTextureWithDescriptor:blitTextureDescriptor];
}

- (void) blitTextureGenerate {

    blitTextureDescriptor = [[MTLTextureDescriptor alloc] init];
    // indicate that each pixel has a blue, green, red, and alpha channel, where each channel is
    // an 8-bit unsigned normalized value (i.e. 0 maps to 0.0 and 255 maps to 1.0)
    blitTextureDescriptor.pixelFormat = MTLPixelFormatBGRA8Unorm;
    [self blitTextureResize :[self openvgSurfaceWidthGet] :[self openvgSurfaceHeightGet]];
}

- (void) blitTextureDestroy {

    if (blitTexture != nil) {
        [blitTexture setPurgeableState:MTLPurgeableStateEmpty];
        [blitTexture release];
        blitTexture = nil;
    }
}

- (void) blitTextureDraw {

    id<MTLCommandBuffer> commandBuffer = [mtlCommandQueue commandBuffer];
    // obtain a render pass descriptor generated from the view's drawable textures
    MTLRenderPassDescriptor* passDescriptor = [self currentRenderPassDescriptor];

    if (passDescriptor != nil) {

        vector_uint2 viewportSize;
        id<MTLRenderCommandEncoder> commandEncoder = [commandBuffer renderCommandEncoderWithDescriptor:passDescriptor];
        // get AmanithVG surface dimensions and pixels pointer
        VGint surfaceWidth = [self openvgSurfaceWidthGet];
        VGint surfaceHeight = [self openvgSurfaceHeightGet];
        const void* surfacePixels = [self openvgSurfacePixelsGet];
        // triangle strip
        const TexturedVertex rectVertices[4] = {
            // pixel positions                 texture coordinates
            { { 0.0f, 0.0f },                  { 0.0f, 1.0f } },
            { { surfaceWidth, 0.0f },          { 1.0f, 1.0f } },
            { { 0.0f, surfaceHeight },         { 0.0f, 0.0f } },
            { { surfaceWidth, surfaceHeight }, { 1.0f, 0.0f } }
        };
        MTLRegion region = {
            { 0, 0, 0 },                        // MTLOrigin
            { surfaceWidth, surfaceHeight, 1 }  // MTLSize
        };

        // upload AmanithVG surface pixels onto the texture
        [blitTexture replaceRegion:region mipmapLevel:0 withBytes:surfacePixels bytesPerRow:surfaceWidth*4];

        // set the region of the drawable to draw into
        [commandEncoder setViewport:(MTLViewport){ 0.0, 0.0, colorRenderBufferWidth, colorRenderBufferHeight, 0.0, 1.0 }];
        [commandEncoder setRenderPipelineState:mtlSurfacePipelineState];
        [commandEncoder setFragmentTexture:blitTexture atIndex:0];
        // put the texture
        viewportSize.x = colorRenderBufferWidth;
        viewportSize.y = colorRenderBufferHeight;
        [commandEncoder setVertexBytes:rectVertices length:sizeof(rectVertices) atIndex:0];
        [commandEncoder setVertexBytes:&viewportSize length:sizeof(viewportSize) atIndex:1];
        [commandEncoder drawPrimitives:MTLPrimitiveTypeTriangleStrip vertexStart:0 vertexCount:4];
        // we have finished
        [commandEncoder endEncoding];
        [commandBuffer presentDrawable:[self currentDrawable]];
    }

    // finalize rendering and push the command buffer to the GPU
    [commandBuffer commit];
    // acknowledge AmanithVG that we have performed a swapbuffers
    vgPostSwapBuffersMZT();
}

- (VGboolean) metalInit:(id<MTLDevice>)device {

    VGboolean ok = VG_TRUE;
    __autoreleasing NSError* error = nil;

    self.device = device;
    self.colorPixelFormat = MTLPixelFormatBGRA8Unorm;
    self.framebufferOnly = YES;
    self.autoResizeDrawable = YES;
    self.clearColor = MTLClearColorMake(0.0, 0.0, 0.0, 1.0);
    self.delegate = self;

    // keep track of Metal command queue
    mtlCommandQueue = [[self device] newCommandQueue];
    mtlLibrary = [[self device] newDefaultLibrary];

    // configure a pipeline descriptor that is used to blit AmanithVG SRE surface
    id<MTLFunction> surfaceVertexFunction = [mtlLibrary newFunctionWithName:@"texturedVertexShader"];
    id<MTLFunction> surfaceFragmentFunction = [mtlLibrary newFunctionWithName:@"texturedFragmentShader"];
    MTLRenderPipelineDescriptor* surfacePipelineStateDescriptor = [[MTLRenderPipelineDescriptor alloc] init];
    surfacePipelineStateDescriptor.label = @"Draw surface texture pipeline";
    surfacePipelineStateDescriptor.vertexFunction = surfaceVertexFunction;
    surfacePipelineStateDescriptor.fragmentFunction = surfaceFragmentFunction;
    surfacePipelineStateDescriptor.colorAttachments[0].pixelFormat = [self colorPixelFormat];
    mtlSurfacePipelineState = [[self device] newRenderPipelineStateWithDescriptor:surfacePipelineStateDescriptor error:&error];
    if (!mtlSurfacePipelineState) {
        NSLog(@"metalInit: failed to create surface pipeline state: %@", error);
        ok = VG_FALSE;
    }
    return ok;
}

// implementation of MTKView and MTKViewDelegate methods
- (id) initWithFrame :(CGRect)frameRect device:(id<MTLDevice>)device {

    self = [super initWithFrame:frameRect device:device];
    if (self != nil) {
        // initialize Metal pipelines
        if ([self metalInit:device]) {
            // NB: drawableSizeWillChange event is NOT called when the view/window is first opened
            // so we must initialize backing bounds variables here
            NSRect backedRect = [self convertRectToBacking:frameRect];
            colorRenderBufferWidth = backedRect.size.width;
            colorRenderBufferHeight = backedRect.size.height;
            // init OpenVG
            vgContext = NULL;
            vgWindowSurface = NULL;
            if ([self openvgInit :colorRenderBufferWidth :colorRenderBufferHeight]) {
                // init tutorial application (OpenVG related code)
                tutorialInit([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
                // generate Metal texture used to blit the AmanithVG SRE surface
                [self blitTextureGenerate];
                // start frame counter
                time0 = [self getTimeMS];
                framesCounter = 0;
            }
            else {
                NSLog(@"initWithFrame: unable to initialize AmanithVG");
                exit(EXIT_FAILURE);
            }
        }
        else {
            NSLog(@"initWithFrame: unable to initialize Metal");
            exit(EXIT_FAILURE);
        }
    }

    return self;
}

// called whenever view changes orientation or layout is changed; NB: this method is NOT called when the view/window is first opened
- (void) mtkView:(nonnull MTKView *)view drawableSizeWillChange:(CGSize)size {

    VGint surfaceWidth, surfaceHeight;

    (void)view;
    // save the size of the drawable to pass to the vertex shader
    colorRenderBufferWidth = size.width;
    colorRenderBufferHeight = size.height;

    // resize AmanithVG surface
    vgPrivSurfaceResizeMZT(vgWindowSurface, colorRenderBufferWidth, colorRenderBufferHeight);
    surfaceWidth = [self openvgSurfaceWidthGet];
    surfaceHeight = [self openvgSurfaceHeightGet];
    [self blitTextureResize :surfaceWidth :surfaceHeight];

    // inform tutorial that surface has been resized
    tutorialResize(surfaceWidth, surfaceHeight);
}

// MTKView by default has its own loop continuously calling the delegate's drawInMTKView method
- (void) drawInMTKView :(nonnull MTKView *)view {

    (void)view;

    // draw OpenVG content
    tutorialDraw([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
    // blit AmanithVG drawing surface, using a texture
    [self blitTextureDraw];
    // advance the frames counter
    framesCounter++;
}

#else

// Core Video display link
- (CVReturn)getFrameForTime :(const CVTimeStamp *)outputTime {

    // deltaTime is unused in this application, but here's how to calculate it using display link info
    // double deltaTime = 1.0 / (outputTime->rateScalar * (double)outputTime->videoTimeScale / (double)outputTime->videoRefreshPeriod);
    (void)outputTime;

    // there is no autorelease pool when this method is called because it will be called from a background thread
    // it's important to create one or app can leak objects
    @autoreleasepool {
        [self drawRect];
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
        NSOpenGLPFASupersample,
        NSOpenGLPFADepthSize, 24,
        NSOpenGLPFAStencilSize, 8,
        NSOpenGLPFASampleBuffers, 1,
        NSOpenGLPFASamples, 8,
        (NSOpenGLPixelFormatAttribute)0
    };
    NSOpenGLPixelFormat *format = [[NSOpenGLPixelFormat alloc] initWithAttributes:attributes];

    if (!format) {
        NSLog(@"initWithFrame: unable to create pixel format");
        exit(EXIT_FAILURE);
    }

    self = [super initWithFrame: frameRect pixelFormat: format];
    [format release];

    // initialize private members
    vgContext = NULL;
    vgWindowSurface = NULL;
    return self;
}

- (void) prepareOpenGL {

    // take care of Retina display
    [self setWantsBestResolutionOpenGLSurface:YES];
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

    // get view dimensions in pixels, taking care of Retina display
    // see https://developer.apple.com/library/archive/documentation/GraphicsImaging/Conceptual/OpenGL-MacProgGuide/EnablingOpenGLforHighResolution/EnablingOpenGLforHighResolution.html
    NSRect backingBounds = [self convertRectToBacking:[self bounds]];

    // init OpenVG
    if ([self openvgInit :(VGint)backingBounds.size.width :(VGint)backingBounds.size.height]) {

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
    else {
        NSLog(@"prepareOpenGL: unable to initialize AmanithVG");
        exit(EXIT_FAILURE);
    }
}

- (void) drawRect {

    if ([self lockFocusIfCanDraw]) {
    
        [[self openGLContext] makeCurrentContext];

        // we draw on a secondary thread through the display link when resizing the view, -reshape is called automatically on the main thread
        // add a mutex around to avoid the threads accessing the context simultaneously when resizing
        CGLLockContext([[self openGLContext] CGLContextObj]);

        // draw OpenVG content
        tutorialDraw([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);

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

        // update the receiver's drawable object
        [[self openGLContext] update];

        // get view dimensions in pixels, taking care of Retina display
        // see https://developer.apple.com/library/archive/documentation/GraphicsImaging/Conceptual/OpenGL-MacProgGuide/EnablingOpenGLforHighResolution/EnablingOpenGLforHighResolution.html
        NSRect backingBounds = [self convertRectToBacking:[self bounds]];
        
        // resize AmanithVG drawing surface
        vgPrivSurfaceResizeMZT(vgWindowSurface, (VGint)backingBounds.size.width, (VGint)backingBounds.size.height);
        // acknowledge tutorial about the resize
        tutorialResize([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);

        // unlock the context
        CGLUnlockContext([[self openGLContext] CGLContextObj]);
        [self unlockFocus];
    }
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
    strcat(msg, "Mouse: Move gradient control points.\n");
    strcat(msg, "I: Change color interpolation.\n");
    strcat(msg, "S: Change spread mode.\n");
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

- (void) dealloc {

#ifdef AM_SRE
    // destroy used textures
    [self blitTextureDestroy];
    // destroy Metal command queue
    [mtlCommandQueue release];
    mtlCommandQueue = nil;
#else
    // stop the display link BEFORE releasing anything in the view
    // otherwise the display link thread may call into the view and crash
    // when it encounters something that has been release
    CVDisplayLinkStop(displayLink);
    // release the display link
    CVDisplayLinkRelease(displayLink);
#endif
    // destroy OpenVG resources created by the tutorial
    tutorialDestroy();
    // destroy OpenVG context and surface
    [self openvgDestroy];
    [super dealloc];
}

// mouse and keyboard events
- (void) mouseDown: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint:p fromView:nil];
    p = [self convertPointToBacking:p];
    mouseLeftButtonDown(p.x, p.y);
}

- (void) mouseUp: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint:p fromView:nil];
    p = [self convertPointToBacking:p];
    mouseLeftButtonUp(p.x, p.y);
}

- (void) mouseDragged:(NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint:p fromView:nil];
    p = [self convertPointToBacking:p];
    mouseMove(p.x, p.y);
}

- (void) rightMouseDown: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint:p fromView:nil];
    p = [self convertPointToBacking:p];
    mouseRightButtonDown(p.x, p.y);
}

- (void) rightMouseUp: (NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint:p fromView:nil];
    p = [self convertPointToBacking:p];
    mouseRightButtonUp(p.x, p.y);
}

- (void) rightMouseDragged:(NSEvent *)theEvent {

    NSPoint p;

    // convert window location into view location
    p = [theEvent locationInWindow];
    p = [self convertPoint:p fromView:nil];
    p = [self convertPointToBacking:p];
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
            // I
            case 'i':
            case 'I':
                toggleColorInterpolation();
                break;
            // S
            case 's':
            case 'S':
                toggleSpreadMode();
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

#ifdef AM_SRE
    // nothing to do
#else
    // Stop the display link when the window is closing because default
    // OpenGL render buffers will be destroyed. If display link continues to
    // fire without renderbuffers, OpenGL draw calls will set errors.
    CVDisplayLinkStop(displayLink);
#endif
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

        NSScreen* screen = [NSScreen mainScreen];
        NSRect frame = NSMakeRect(0, 0, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);

        // take care of Retina display
        frame = [screen convertRectFromBacking :frame];

        // get application
        NSApplication* app = [NSApplication sharedApplication];
        [NSApp setActivationPolicy: NSApplicationActivationPolicyRegular];

        // create the window
        NSWindow* window = [[NSWindow alloc] initWithContentRect:frame styleMask:NSWindowStyleMaskTitled | NSWindowStyleMaskClosable | NSWindowStyleMaskMiniaturizable | NSWindowStyleMaskResizable backing:NSBackingStoreBuffered defer: TRUE];
        [window setAcceptsMouseMovedEvents:YES];
        [window setTitle: @ WINDOW_TITLE];

    #ifdef AM_SRE
        // create the Metal view
        id<MTLDevice> mtlDevice = MTLCreateSystemDefaultDevice();
        TutorialView* view = [[TutorialView alloc] initWithFrame:frame device:mtlDevice];
    #else
        // create the OpenGL view
        TutorialView* view = [[TutorialView alloc] initWithFrame:frame];
    #endif

        // link the view to the window
        [window setDelegate: view];
        [window setContentView: view];
        [window makeFirstResponder: view];
        // do not allow a content size bigger than the maximum surface dimension that AmanithVG can handle
        NSRect maxRect = NSMakeRect(0.0f, 0.0f, [view openvgSurfaceMaxDimensionGet], [view openvgSurfaceMaxDimensionGet]);
        maxRect = [screen convertRectFromBacking :maxRect];
        [window setContentMaxSize: maxRect.size];
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
