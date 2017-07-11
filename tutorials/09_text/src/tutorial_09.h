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

#ifndef TUTORIAL_09_H
#define TUTORIAL_09_H

#include <VG/openvg.h>
#include <VG/vgu.h>
#include <VG/vgext.h>

void tutorialInit(const VGint surfaceWidth,
                  const VGint surfaceHeight);
void tutorialDestroy(void);
void tutorialResize(const VGint surfaceWidth,
                    const VGint surfaceHeight);
void tutorialDraw(const VGint surfaceWidth,
                  const VGint surfaceHeight);

// handle mouse events
void mouseLeftButtonDown(const VGint x,
                         const VGint y);
void mouseLeftButtonUp(const VGint x,
                       const VGint y);
void mouseRightButtonDown(const VGint x,
                          const VGint y);
void mouseRightButtonUp(const VGint x,
                        const VGint y);
void mouseMove(const VGint x,
               const VGint y);
// handle touch events
void touchDoubleTap(const VGint x,
                    const VGint y);
void touchPinch(const VGfloat deltaScl);
void touchRotate(const VGfloat deltaRot);

#endif
