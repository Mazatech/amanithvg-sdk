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
#import <QuartzCore/QuartzCore.h>
#ifdef AM_SRE
    #import <Metal/Metal.h>
    #import <MetalKit/MetalKit.h>
    #include <simd/simd.h>
#else
    #import <UIKit/UIKit.h>
    #include <OpenGLES/ES1/gl.h>
    #include <OpenGLES/ES1/glext.h>
#endif
#include "tutorial_01.h"

#ifdef AM_SRE
@interface View : MTKView <MTKViewDelegate, UIGestureRecognizerDelegate> {
    // Metal command queue
    id<MTLCommandQueue> mtlCommandQueue;
    id<MTLLibrary> mtlLibrary;
    // Metal rendering pipelines
    id<MTLRenderPipelineState> mtlSurfacePipelineState;
    // Metal texture used to blit the AmanithVG SRE surface
    id<MTLTexture> blitTexture;
    MTLTextureDescriptor* blitTextureDescriptor;
#else
@interface View : UIView <UIGestureRecognizerDelegate> {
    CAEAGLLayer* eaglLayer;
    // OpenGL ES 1.1 context
    EAGLContext* eaglContext;
    // DisplayLink
    CADisplayLink* displayLink;
    // framebuffers (resolved and multi-sampled)
    GLuint resolvedFrameBuffer;
    GLuint sampledFrameBuffer;
    // OpenGL ES color buffers (resolved and multi-sampled)
    GLuint resolvedColorRenderBuffer;
    GLuint sampledColorRenderBuffer;
    // OpenGL ES depth buffer (multi-sampled)
    GLuint sampledDepthRenderBuffer;
#endif
    // keep track of backing bounds
    int colorRenderBufferWidth;
    int colorRenderBufferHeight;
    // keep track of OpenVG initialization
    VGboolean vgInitialized;
    // OpenVG context
    void* vgContext;
    // OpenVG surface
    void* vgWindowSurface;
    // touch events
    float lastScale;
    float lastRotation;
}

- (void) initView;

@end
