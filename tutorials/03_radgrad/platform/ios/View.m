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
#import "View.h"
#ifdef AM_SRE
    // header shared between C code here, which executes Metal API commands, and .metal files, which uses these types as inputs to the shaders.
    #import "ShaderTypes.h"
#endif
#include <math.h>

@implementation View

#ifdef AM_SRE

- (VGboolean) metalInit {

    VGboolean ok = VG_TRUE;
    __autoreleasing NSError* error = nil;

    self.colorPixelFormat = MTLPixelFormatBGRA8Unorm;
    self.framebufferOnly = YES;
    self.autoResizeDrawable = YES;
    self.clearColor = MTLClearColorMake(1.0, 1.0, 1.0, 1.0);
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

#else

/*****************************************************************
                               EGL
*****************************************************************/
+ (Class)layerClass {
    return [CAEAGLLayer class];
}

- (void) eaglLayerSetup {

    eaglLayer = (CAEAGLLayer*)self.layer;
    eaglLayer.opaque = YES;

    if ([[UIScreen mainScreen] respondsToSelector:@selector(displayLinkWithTarget:selector:)]) {
        // take care of Retina display
        if ([UIScreen mainScreen].nativeScale > 0.0f) {
            self.contentScaleFactor = [UIScreen mainScreen].nativeScale;
            eaglLayer.contentsScale = [UIScreen mainScreen].nativeScale;
        }
    }
}

- (VGboolean) eaglContextSetup {

    // OpenGL ES 1.1 context
    eaglContext = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES1];
    if (!eaglContext) {
        NSLog(@"eaglContextSetup: failed to initialize OpenGL ES 1.1 context");
        return VG_FALSE;
    }
    
    if (![EAGLContext setCurrentContext:eaglContext]) {
        NSLog(@"eaglContextSetup: failed to set current OpenGL context");
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
        NSLog(@"glesFrameBufferCreate: failed to build a complete multi-sampled framebuffer object; status code: %x", status);
        return VG_FALSE;
    }
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, sampledColorRenderBuffer);

    // set viewport
    glViewport(0, 0, width, height);
    // keep track of color buffer dimensions
    colorRenderBufferWidth = width;
    colorRenderBufferHeight = height;
    return VG_TRUE;
}

- (void) glesFrameBufferDestroy {

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
}

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

#endif  // AM_SRE

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

// get access to OpenVG drawing surface pixels
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
}

#else

- (void) render :(CADisplayLink*)displayLink {

    glBindFramebufferOES(GL_FRAMEBUFFER_OES, sampledFrameBuffer);
    // draw OpenVG content
    tutorialDraw([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
    // "resolve" the multi-sampled framebuffer
    glBindFramebufferOES(GL_DRAW_FRAMEBUFFER_APPLE, resolvedFrameBuffer);
    glBindFramebufferOES(GL_READ_FRAMEBUFFER_APPLE, sampledFrameBuffer);
    glResolveMultisampleFramebufferAPPLE();
    glBindRenderbufferOES(GL_RENDERBUFFER_OES, resolvedColorRenderBuffer);
    // present color buffer
    [eaglContext presentRenderbuffer:GL_RENDERBUFFER_OES];
    // acknowledge AmanithVG that we have performed a swapbuffers
    vgPostSwapBuffersMZT();
}

- (VGboolean)resizeFromLayer:(CAEAGLLayer *)layer {

    GLenum status;
    GLint glWidth, glHeight;

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

    // resize AmanithVG surface
    vgPrivSurfaceResizeMZT(vgWindowSurface, glWidth, glHeight);
    // inform tutorial that surface has been resized
    tutorialResize([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);

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

#endif // AM_SRE

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
    translatedPoint.x *= [[UIScreen mainScreen] nativeScale];
    translatedPoint.y *= [[UIScreen mainScreen] nativeScale];

    if ([(UIPanGestureRecognizer*)sender state] == UIGestureRecognizerStateBegan) {
        // we must apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        mouseLeftButtonDown((VGint)translatedPoint.x, colorRenderBufferHeight - (VGint)translatedPoint.y);
    }
    else
    if ([(UIPanGestureRecognizer*)sender state] == UIGestureRecognizerStateEnded) {
        // we must apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        mouseLeftButtonUp((VGint)translatedPoint.x, colorRenderBufferHeight - (VGint)translatedPoint.y);
    }
    else {
        // we must apply a flip on y direction in order to be consistent with the OpenVG coordinates system
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
        tapPoint.x *= [[UIScreen mainScreen] nativeScale];
        tapPoint.y *= [[UIScreen mainScreen] nativeScale];
        // we must apply a flip on y direction in order to be consistent with the OpenVG coordinates system
        touchDoubleTap((VGint)tapPoint.x, colorRenderBufferHeight - (VGint)tapPoint.y);
    }
}

- (void) initView {

    VGboolean ok;

    vgInitialized = VG_FALSE;
#ifdef AM_SRE
    ok = [self metalInit];
    colorRenderBufferWidth = [self drawableSize].width;
    colorRenderBufferHeight = [self drawableSize].height;
#else
    ok = VG_FALSE;
    // setup EAGL layer
    [self eaglLayerSetup];
    // setup EAGL context
    if ([self eaglContextSetup]) {
        // setup framebuffer
        ok = [self glesFrameBufferCreate];
    }
#endif

    if (ok) {
        // setup gestures
        [self setMultipleTouchEnabled:YES];
        [self gesturesSetup];
        // init OpenVG
        if ([self openvgInit :colorRenderBufferWidth :colorRenderBufferHeight]) {
            // init tutorial application (OpenVG related code)
            tutorialInit([self openvgSurfaceWidthGet], [self openvgSurfaceHeightGet]);
        #ifdef AM_SRE
            // generate Metal texture used to blit the AmanithVG SRE surface
            [self blitTextureGenerate];
        #else
            // create and activate the display link (i.e. render loop)
            [self displayLinkCreate];
            [self displayLinkStart];
        #endif
        }
        else {
            NSLog(@"initView: unable to initialize AmanithVG");
            exit(EXIT_FAILURE);
        }
    }
    else {
        exit(EXIT_FAILURE);
    }
}

- (void) dealloc {

#ifdef AM_SRE
    // destroy OpenVG resources created by the tutorial
    tutorialDestroy();
    // destroy texture used to blit AmanithVG SRE surface
    [self blitTextureDestroy];
    // destroy OpenVG context and surface
    [self openvgDestroy];
#else
    // stop the display link BEFORE releasing anything in the view
    [self displayLinkStop];
    displayLink = nil;
    // destroy OpenVG resources created by the tutorial
    tutorialDestroy();
    // destroy OpenVG context and surface
    [self openvgDestroy];
    // destroy OpenGL ES buffers
    [self glesFrameBufferDestroy];
    // unbind OpenGL context
    if ([EAGLContext currentContext] == eaglContext) {
        [EAGLContext setCurrentContext:nil];
    }
    eaglContext = nil;
#endif

    [super dealloc];
}

@end
