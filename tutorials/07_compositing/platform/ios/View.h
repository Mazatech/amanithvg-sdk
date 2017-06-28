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
#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#include <OpenGLES/ES1/gl.h>
#include <OpenGLES/ES1/glext.h>
#include "tutorial_07.h"

@interface View : UIView <UIGestureRecognizerDelegate> {

    CAEAGLLayer* eaglLayer;
    // OpenGL ES 1.1 context
    EAGLContext* eaglContext;
    // DisplayLink
    CADisplayLink* displayLink;

#ifdef AM_SRE
    // framebuffer
    GLuint frameBuffer;
    // OpenGL ES color buffer
    GLuint colorRenderBuffer;
#else
    // framebuffers (resolved and multi-sampled)
    GLuint resolvedFrameBuffer;
    GLuint sampledFrameBuffer;
    // OpenGL ES color buffers (resolved and multi-sampled)
    GLuint resolvedColorRenderBuffer;
    GLuint sampledColorRenderBuffer;
    // OpenGL ES depth buffer (multi-sampled)
    GLuint sampledDepthRenderBuffer;
#endif
    int colorRenderBufferWidth;
    int colorRenderBufferHeight;
    // keep track of OpenVG initialization
    VGboolean vgInitialized;
    // OpenVG context
    void* vgContext;
    // OpenVG surface
    void* vgWindowSurface;
    // OpenGL texture used to blit the AmanithVG SRE surface
    GLuint blitTexture;
    // touch events
    float lastScale;
    float lastRotation;
}

@end
