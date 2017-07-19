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
#import "View.h"
#include <math.h>

@implementation View

/*****************************************************************
                               EGL
*****************************************************************/
+ (Class)layerClass {
    return [CAEAGLLayer class];
}

- (void) eaglLayerSetup {

    eaglLayer = (CAEAGLLayer*)self.layer;
    eaglLayer.opaque = YES;

    if ([[UIScreen mainScreen] respondsToSelector:@selector(displayLinkWithTarget:selector:)] && ([UIScreen mainScreen].scale == 2.0)) {
        // Retina display
        self.contentScaleFactor = 2.0;
        eaglLayer.contentsScale = 2;
    }
}

- (VGboolean) eaglContextSetup {

    // OpenGL ES 1.1 context
    eaglContext = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES1];
    if (!eaglContext) {
        NSLog(@"Failed to initialize OpenGL ES 1.1 context");
        return VG_FALSE;
    }
    
    if (![EAGLContext setCurrentContext:eaglContext]) {
        NSLog(@"Failed to set current OpenGL context");
        return VG_FALSE;
    }

    return VG_TRUE;
}

/*****************************************************************
                            OpenGL ES
*****************************************************************/
- (VGboolean) glesFrameBufferCreate {

    GLint width, height;
    GLenum status;

#ifdef AM_SRE
    // generate framebuffer object
    glGenFramebuffersOES(1, &frameBuffer);
    glBindFramebufferOES(GL_FRAMEBUFFER_OES, frameBuffer);
    // create and attach color buffer
    glGenRenderbuffersOES(1, &colorRenderBuffer);
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, colorRenderBuffer);
    [eaglContext renderbufferStorage:GL_RENDERBUFFER_OES fromDrawable:eaglLayer];
    glFramebufferRenderbufferOES(GL_FRAMEBUFFER_OES, GL_COLOR_ATTACHMENT0_OES, GL_RENDERBUFFER_OES, colorRenderBuffer);
    // retrieve the height and width of the color renderbuffer (see https://developer.apple.com/library/ios/documentation/3ddrawing/conceptual/opengles_programmingguide/WorkingwithEAGLContexts/WorkingwithEAGLContexts.html)
    // "Here, the code retrieves the width and height from the color renderbuffer after its storage is allocated.
    // Your app does this because the actual dimensions of the color renderbuffer are calculated based on the layer's bounds and scale factor.
    // Other renderbuffers attached to the framebuffer must have the same dimensions. In addition to using the height and width to allocate
    // the depth buffer, use them to assign the OpenGL ES viewport and to help determine the level of detail required in your app’s textures and models"
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_WIDTH_OES, &width);
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_HEIGHT_OES, &height);
    // final checkup
    status = glCheckFramebufferStatusOES(GL_FRAMEBUFFER_OES);
    if (status != GL_FRAMEBUFFER_COMPLETE_OES) {
        NSLog(@"glesFrameBufferCreate: failed to build a complete framebuffer object; status code: %x", status);
        return VG_FALSE;
    }
#else
    // resolved framebuffer
    glGenFramebuffersOES(1, &resolvedFrameBuffer);
    glBindFramebufferOES(GL_FRAMEBUFFER_OES, resolvedFrameBuffer);
    // create and attach color buffer
    glGenRenderbuffersOES(1, &resolvedColorRenderBuffer);
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, resolvedColorRenderBuffer);
    [eaglContext renderbufferStorage:GL_RENDERBUFFER_OES fromDrawable:eaglLayer];
    glFramebufferRenderbufferOES(GL_FRAMEBUFFER_OES, GL_COLOR_ATTACHMENT0_OES, GL_RENDERBUFFER_OES, resolvedColorRenderBuffer);
    // final checkup
    status = glCheckFramebufferStatusOES(GL_FRAMEBUFFER_OES);
    if (status != GL_FRAMEBUFFER_COMPLETE_OES) {
        NSLog(@"glesFrameBufferCreate: failed to build a complete framebuffer object; status code: %x", status);
        return VG_FALSE;
    }
    // retrieve the height and width of the color renderbuffer
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_WIDTH_OES, &width);
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_HEIGHT_OES, &height);
    // multi-sampled framebuffer
    glGenFramebuffersOES(1, &sampledFrameBuffer);
    glBindFramebufferOES(GL_FRAMEBUFFER_OES, sampledFrameBuffer);
    // create and attach color buffer
    glGenRenderbuffersOES(1, &sampledColorRenderBuffer);
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, sampledColorRenderBuffer);
    glRenderbufferStorageMultisampleAPPLE(GL_RENDERBUFFER_OES, 4, GL_RGBA8_OES, width, height);
    glFramebufferRenderbufferOES(GL_FRAMEBUFFER_OES, GL_COLOR_ATTACHMENT0_OES, GL_RENDERBUFFER_OES, sampledColorRenderBuffer);
    // create and attach depth+stencil buffer
    glGenRenderbuffersOES(1, &sampledDepthRenderBuffer);
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, sampledDepthRenderBuffer);
    glRenderbufferStorageMultisampleAPPLE(GL_RENDERBUFFER_OES, 4, GL_DEPTH24_STENCIL8_OES, width, height);
    glFramebufferRenderbufferOES(GL_FRAMEBUFFER_OES, GL_DEPTH_ATTACHMENT_OES, GL_RENDERBUFFER_OES, sampledDepthRenderBuffer);
    glFramebufferRenderbufferOES(GL_FRAMEBUFFER_OES, GL_STENCIL_ATTACHMENT_OES, GL_RENDERBUFFER_OES, sampledDepthRenderBuffer);
    // final checkup
    status = glCheckFramebufferStatusOES(GL_FRAMEBUFFER_OES);
    if (status != GL_FRAMEBUFFER_COMPLETE_OES) {
        NSLog(@"Failed to build a complete multi-sampled framebuffer object; status code: %x", status);
        return VG_FALSE;
    }
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, sampledColorRenderBuffer);
#endif
    // set viewport
    glViewport(0, 0, width, height);
    // keep track of color buffer dimensions
    colorRenderBufferWidth = width;
    colorRenderBufferHeight = height;
    return VG_TRUE;
}

- (void) glesFrameBufferDestroy {

#ifdef AM_SRE
    if (frameBuffer) {
        glDeleteFramebuffersOES(1, &frameBuffer);
        frameBuffer = 0;
    }
    if (colorRenderBuffer) {
        glDeleteRenderbuffersOES(1, &colorRenderBuffer);
        colorRenderBuffer = 0;
    }
#else
    if (resolvedFrameBuffer) {
        glDeleteFramebuffersOES(1, &resolvedFrameBuffer);
        resolvedFrameBuffer = 0;
    }
    if (resolvedColorRenderBuffer) {
        glDeleteRenderbuffersOES(1, &resolvedColorRenderBuffer);
        resolvedColorRenderBuffer = 0;
    }
    if (sampledFrameBuffer) {
        glDeleteFramebuffersOES(1, &sampledFrameBuffer);
        sampledFrameBuffer = 0;
    }
    if (sampledColorRenderBuffer) {
        glDeleteRenderbuffersOES(1, &sampledColorRenderBuffer);
        resolvedColorRenderBuffer = 0;
    }
    if (sampledDepthRenderBuffer) {
        glDeleteRenderbuffersOES(1, &sampledDepthRenderBuffer);
        sampledDepthRenderBuffer = 0;
    }
#endif
}

#ifdef AM_SRE

- (void) glesInit {

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

#endif

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

    vgInitialized = VG_TRUE;
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
    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, blitTexture);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    // select best format (BGRA), in order to avoid swizzling
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, [self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet], 0, GL_BGRA, GL_UNSIGNED_BYTE, surfacePixels);
}

- (void) blitTextureResize :(const VGuint)width :(const VGuint)height {

    // resize the OpenGL texture used to blit AmanithVG SRE drawing surface
    glBindTexture(GL_TEXTURE_2D, blitTexture);
    // select best format (BGRA), in order to avoid swizzling
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_BGRA, GL_UNSIGNED_BYTE, vgPrivGetSurfacePixelsMZT(vgWindowSurface));
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
    const VGfloat w = (VGfloat)surfaceWidth / (VGfloat)colorRenderBufferWidth;
    const VGfloat h = (VGfloat)surfaceHeight / (VGfloat)colorRenderBufferHeight;
    // 4 vertices
    const GLfloat xy[] = {
        -1.0f, -1.0f,
        -1.0f + (w * 2.0f), -1.0f,
        -1.0f, -1.0f + (h * 2.0f),
        -1.0f + (w * 2.0f), -1.0f + (h * 2.0f)
    };
    static const GLfloat uv[] = {
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    };
    
    glClear(GL_COLOR_BUFFER_BIT);
    // simply put a quad, covering the whole window
    glBindTexture(GL_TEXTURE_2D, blitTexture);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, surfaceWidth, surfaceHeight, GL_BGRA, GL_UNSIGNED_BYTE, surfacePixels);
    glEnable(GL_TEXTURE_2D);
    glVertexPointer(2, GL_FLOAT, 0, xy);
    glEnableClientState(GL_VERTEX_ARRAY);
    glTexCoordPointer(2, GL_FLOAT, 0, uv);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}

#endif // AM_SRE

- (void) render :(CADisplayLink*)displayLink {

#ifdef AM_SRE
    // draw OpenVG content
    tutorialDraw([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
    // blit AmanithVG drawing surface, using a texture
    [self blitTextureDraw];
#else
    glBindFramebufferOES(GL_FRAMEBUFFER_OES, sampledFrameBuffer);
    // draw OpenVG content
    tutorialDraw([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
    // "resolve" the multi-sampled framebuffer
    glBindFramebufferOES(GL_DRAW_FRAMEBUFFER_APPLE, resolvedFrameBuffer);
    glBindFramebufferOES(GL_READ_FRAMEBUFFER_APPLE, sampledFrameBuffer);
    glResolveMultisampleFramebufferAPPLE();
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, resolvedColorRenderBuffer);
#endif

    // present color buffer
    [eaglContext presentRenderbuffer:GL_RENDERBUFFER_OES];
    // acknowledge AmanithVG that we have performed a swapbuffers
    vgPostSwapBuffersMZT();
}

- (VGboolean)resizeFromLayer:(CAEAGLLayer *)layer {

    GLenum status;
    GLint glWidth, glHeight;
    VGint vgWidth, vgHeight;

#ifdef AM_SRE
    glBindFramebufferOES(GL_FRAMEBUFFER_OES, frameBuffer);
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, colorRenderBuffer);
    [eaglContext renderbufferStorage:GL_RENDERBUFFER_OES fromDrawable:layer];
    // retrieve the height and width of the color renderbuffer
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_WIDTH_OES, &glWidth);
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_HEIGHT_OES, &glHeight);
    status = glCheckFramebufferStatusOES(GL_FRAMEBUFFER_OES);
    if (status != GL_FRAMEBUFFER_COMPLETE_OES) {
        NSLog(@"resizeFromLayer: failed to resize the framebuffer object; status code: %x", status);
        return VG_FALSE;
    }
#else
    glBindFramebufferOES(GL_FRAMEBUFFER_OES, resolvedFrameBuffer);
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, resolvedColorRenderBuffer);
    [eaglContext renderbufferStorage:GL_RENDERBUFFER_OES fromDrawable:layer];
    // retrieve the height and width of the color renderbuffer
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_WIDTH_OES, &glWidth);
    glGetRenderbufferParameterivOES(GL_RENDERBUFFER_OES, GL_RENDERBUFFER_HEIGHT_OES, &glHeight);
    // final checkup
    status = glCheckFramebufferStatusOES(GL_FRAMEBUFFER_OES);
    if (status != GL_FRAMEBUFFER_COMPLETE_OES) {
        NSLog(@"resizeFromLayer: failed to resize the framebuffer object; status code: %x", status);
        return VG_FALSE;
    }
    glBindFramebufferOES(GL_FRAMEBUFFER_OES, sampledFrameBuffer);
    // resize sampled color buffer
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, sampledColorRenderBuffer);
    glRenderbufferStorageMultisampleAPPLE(GL_RENDERBUFFER_OES, 4, GL_RGBA8_OES, glWidth, glHeight);
    // resize depth and stencil buffers
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, sampledDepthRenderBuffer);
    glRenderbufferStorageMultisampleAPPLE(GL_RENDERBUFFER_OES, 4, GL_DEPTH24_STENCIL8_OES, glWidth, glHeight);
    // final checkup
    status = glCheckFramebufferStatusOES(GL_FRAMEBUFFER_OES);
    if (status != GL_FRAMEBUFFER_COMPLETE_OES) {
        NSLog(@"resizeFromLayer: failed to resize the multi-sampled framebuffer object; status code: %x", status);
        return VG_FALSE;
    }
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, sampledColorRenderBuffer);
#endif

    // resize AmanithVG surface
    vgPrivSurfaceResizeMZT(vgWindowSurface, glWidth, glHeight);
    vgWidth = [self openvgSurfaceWidthGet];
    vgHeight = [self openvgSurfaceHeightGet];
#ifdef AM_SRE
    [self blitTextureResize :vgWidth :vgHeight];
#endif    

    // inform tutorial that surface has been resized
    tutorialResize(vgWidth, vgHeight);

    // set viewport
    glViewport(0, 0, glWidth, glHeight);
    // keep track of color buffer dimensions
    colorRenderBufferWidth = glWidth;
    colorRenderBufferHeight = glHeight;
    return VG_TRUE;
}

// if our view is resized, we'll be asked to layout subviews: this is the perfect opportunity to also update
// the framebuffer so that it is the same size as our display area
-(void)layoutSubviews {

    [EAGLContext setCurrentContext:eaglContext];
    if (vgInitialized) {
        [self resizeFromLayer:(CAEAGLLayer*)self.layer];
    }
}

- (void) displayLinkCreate {

    displayLink = [CADisplayLink displayLinkWithTarget:self selector:@selector(render:)];
}

- (void) displayLinkStart {

    [displayLink addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
}

- (void) displayLinkStop {

    [displayLink invalidate];
}

- (void) gesturesSetup {

    // pan gesture
    UIPanGestureRecognizer* panRecognizer = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(move:)];
    [panRecognizer setMinimumNumberOfTouches:1];
    [panRecognizer setMaximumNumberOfTouches:1];
    [panRecognizer setDelegate:self];
    [self addGestureRecognizer:panRecognizer];

    // pinch gesture
    UIPinchGestureRecognizer* pinchRecognizer = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(scale:)];
    [pinchRecognizer setDelegate:self];
    [self addGestureRecognizer:pinchRecognizer];

    // rotate gesture
    UIRotationGestureRecognizer* rotationRecognizer = [[UIRotationGestureRecognizer alloc] initWithTarget:self action:@selector(rotate:)];
    [rotationRecognizer setDelegate:self];
    [self addGestureRecognizer:rotationRecognizer];
    
    // tap gesture
    UITapGestureRecognizer* tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(doubleTap:)];
    [tapRecognizer setNumberOfTapsRequired:2];
    [tapRecognizer setDelegate:self];
    [self addGestureRecognizer:tapRecognizer];
}

- (void) move :(id)sender {

    CGPoint translatedPoint = [(UIPanGestureRecognizer*)sender locationInView:self];
    translatedPoint.x *= [[UIScreen mainScreen] scale];
    translatedPoint.y *= [[UIScreen mainScreen] scale];
 
    if ([(UIPanGestureRecognizer*)sender state] == UIGestureRecognizerStateBegan) {
        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        mouseLeftButtonDown((VGint)translatedPoint.x, colorRenderBufferHeight - (VGint)translatedPoint.y);
    }
    else
    if ([(UIPanGestureRecognizer*)sender state] == UIGestureRecognizerStateEnded) {
        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        mouseLeftButtonUp((VGint)translatedPoint.x, colorRenderBufferHeight - (VGint)translatedPoint.y);
    }
    else {
        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        mouseMove((VGint)translatedPoint.x, colorRenderBufferHeight - (VGint)translatedPoint.y);
    }
}

- (void) scale :(id)sender {

    if ([(UIPinchGestureRecognizer*)sender state] == UIGestureRecognizerStateBegan) {
        lastScale = [(UIPinchGestureRecognizer*)sender scale];
    }
    else {
        VGfloat scl = [(UIPinchGestureRecognizer*)sender scale];
        touchPinch(scl - lastScale);
        lastScale = scl;
    }
}
 
- (void) rotate :(id)sender {
 
    if ([(UIRotationGestureRecognizer*)sender state] == UIGestureRecognizerStateBegan) {
        lastRotation = [(UIRotationGestureRecognizer*)sender rotation];
    }
    else {
        VGfloat rot = [(UIRotationGestureRecognizer*)sender rotation];
        touchRotate(-35.0f * (rot - lastRotation));
        lastRotation = rot;
    }
}

- (void) doubleTap :(id)sender {

    if ([(UITapGestureRecognizer*)sender state] == UIGestureRecognizerStateEnded) {
        CGPoint tapPoint = [(UIGestureRecognizer*)sender locationInView:self];
        tapPoint.x *= [[UIScreen mainScreen] scale];
        tapPoint.y *= [[UIScreen mainScreen] scale];
        // we apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        touchDoubleTap((VGint)tapPoint.x, colorRenderBufferHeight - (VGint)tapPoint.y);
    }
}

- (id) initWithFrame :(CGRect)frame {

    vgInitialized = VG_FALSE;
    self = [super initWithFrame:frame];

    if (self) {
        // setup EAGL layer
        [self eaglLayerSetup];
        // setup EAGL context
        if ([self eaglContextSetup]) {
            // setup framebuffer
            if ([self glesFrameBufferCreate]) {
                // setup gestures
                [self setMultipleTouchEnabled:YES];
                [self gesturesSetup];
                // init OpenVG
                if ([self openvgInit :colorRenderBufferWidth :colorRenderBufferHeight]) {
                #ifdef AM_SRE
                    // set basic OpenGL states and viewport
                    [self glesInit];
                    // generate OpenGL ES texture used to blit the AmanithVG SRE surface
                    [self blitTextureGenerate];
                #endif
                    // init tutorial application
                    tutorialInit([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
                }
                else {
                    NSLog(@"Unable to initialize AmanithVG");
                    exit(EXIT_FAILURE);
                }
            }
            else {
                exit(EXIT_FAILURE);
            }
        }
        else {
            exit(EXIT_FAILURE);
        }

        // create and activate the display link (i.e. render loop)
        [self displayLinkCreate];
        [self displayLinkStart];
    }

    return self;
}

- (void) dealloc {

    // stop the display link BEFORE releasing anything in the view
    [self displayLinkStop];
    displayLink = nil;

    // destroy OpenVG resources created by the tutorial
    tutorialDestroy();

#ifdef AM_SRE
    // destroy texture used to blit AmanithVG SRE surface
    [self blitTextureDestroy];
#endif

    // destroy OpenVG context and surface
    [self openvgDestroy];

    // destroy OpenGL ES buffers
    [self glesFrameBufferDestroy];

    // unbind OpenGL context
    if ([EAGLContext currentContext] == eaglContext) {
        [EAGLContext setCurrentContext:nil];
    }
    eaglContext = nil;

    [super dealloc];
}

@end
