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

package javax.microedition.khronos.openvg;

public class VGHandle {

    // in OpenVG, a VGHandle is a 32bit unsigned integer
    protected int mHandle;

    protected VGHandle(int handle) {
        mHandle = handle;
    }

    public VGHandle() {
        mHandle = 0;
    }

    protected static int getHandle(VGHandle obj) {

        return (obj == null) ? 0 : obj.mHandle;
    }
}

