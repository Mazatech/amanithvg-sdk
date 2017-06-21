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

import java.nio.*;

public class AmanithVG implements VG11, VGU11, VG11Ext {

    private static final int ARRAY_TYPE_BYTE    = 1;
    private static final int ARRAY_TYPE_SHORT   = 2;
    private static final int ARRAY_TYPE_INT     = 3;
    private static final int ARRAY_TYPE_FLOAT   = 4;

    /*-------------------------------------------------------------------------------
                            Utility for common error checking
    -------------------------------------------------------------------------------*/
    private void checkContextParameterLength(int type, int count, final int[] values, int offset) {

        int paramSize;
        
        if (type == VG_SCISSOR_RECTS || type == VG_STROKE_DASH_PATTERN) {
            // for variable length parameters, we must ensure a 'count' number of entries inside 'values' array
            paramSize = count;
        }
        else {
            // for fixed length parameters, we must ensure a 'vgGetVectorSize(type)' number of entries inside 'values' array
            paramSize = AmanithVGJNI.vgGetVectorSize(type);
        }
    
        if ((paramSize > 0) && (values == null)) {
            throw new IllegalArgumentException("values == null");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if ((values != null) && (values.length - offset < paramSize)) {
            throw new IllegalArgumentException("not enough remaining entries (values)");
        }
    }

    private void checkContextParameterLength(int type, int count, final float[] values, int offset) {

        int paramSize;
        
        if (type == VG_SCISSOR_RECTS || type == VG_STROKE_DASH_PATTERN) {
            // for variable length parameters, we must ensure a 'count' number of entries inside 'values' array
            paramSize = count;
        }
        else {
            // for fixed length parameters, we must ensure a 'vgGetVectorSize(type)' number of entries inside 'values' array
            paramSize = AmanithVGJNI.vgGetVectorSize(type);
        }

        if ((paramSize > 0) && (values == null)) {
            throw new IllegalArgumentException("values == null");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if ((values != null) && (values.length - offset < paramSize)) {
            throw new IllegalArgumentException("not enough remaining entries (values)");
        }
    }

    private void checkObjectParameterLength(VGHandle object, int paramType, int count, final int[] values, int offset) {

        int paramSize;
        
        if (paramType == VG_PAINT_COLOR_RAMP_STOPS) {
            // for variable length parameters, we must ensure a 'count' number of entries inside 'values' array
            paramSize = count;
        }
        else {
            // for fixed length parameters, we must ensure a 'vgGetParameterVectorSize(paramType)' number of entries inside 'values' array
            paramSize = AmanithVGJNI.vgGetParameterVectorSize(VGHandle.getHandle(object), paramType);
        }
    
        if (values == null) {
            throw new IllegalArgumentException("values == null");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if (values.length - offset < paramSize) {
            throw new IllegalArgumentException("not enough remaining entries (values)");
        }
    }

    private void checkObjectParameterLength(VGHandle object, int paramType, int count, final float[] values, int offset) {

        int paramSize;
        
        if (paramType == VG_PAINT_COLOR_RAMP_STOPS) {
            // for variable length parameters, we must ensure a 'count' number of entries inside 'values' array
            paramSize = count;
        }
        else {
            // for fixed length parameters, we must ensure a 'vgGetParameterVectorSize(paramType)' number of entries inside 'values' array
            paramSize = AmanithVGJNI.vgGetParameterVectorSize(VGHandle.getHandle(object), paramType);
        }
    
        if (values == null) {
            throw new IllegalArgumentException("values == null");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if (values.length - offset < paramSize) {
            throw new IllegalArgumentException("not enough remaining entries (values)");
        }
    }

    private void checkMatrixLength(final float[] m, int offset, int matrixNeededSize) {
        
        if (m == null) {
            throw new IllegalArgumentException("matrix == null");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        if (m.length - offset < matrixNeededSize) {
            throw new IllegalArgumentException("not enough remaining entries (matrix)");
        }
    }

    /*-------------------------------------------------------------------------------
                               Get error and pipeline flush
    -------------------------------------------------------------------------------*/
    public int vgGetError() {

        return AmanithVGJNI.vgGetError();
    }

    public void vgFlush() {

        AmanithVGJNI.vgFlush();
    }

    public void vgFinish() {

        AmanithVGJNI.vgFinish();
    }

    /*-------------------------------------------------------------------------------
                                  Getters and Setters
    -------------------------------------------------------------------------------*/
    public void vgSetf(int type, float value) {

        AmanithVGJNI.vgSetf(type, value);
    }

    public void vgSeti(int type, int value) {

        AmanithVGJNI.vgSeti(type, value);
    }

    public void vgSeti(int type, boolean value) {

        AmanithVGJNI.vgSeti(type, value ? VG_TRUE : VG_FALSE);
    }

    public void vgSetfv(int type, int count, final float[] values, int offset) {
    
        // check arguments
        checkContextParameterLength(type, count, values, offset);
        AmanithVGJNI.vgSetfvA(type, count, values, offset);
    }

    public void vgSetfv(int type, int count, final float[] values) {

        vgSetfv(type, count, values, 0);
    }

    public void vgSetiv(int type, int count, final int[] values, int offset) {
    
        // check arguments
        checkContextParameterLength(type, count, values, offset);
        AmanithVGJNI.vgSetivA(type, count, values, offset);
    }

    public void vgSetiv(int type, int count, final int[] values) {

        vgSetiv(type, count, values, 0);
    }

    public float vgGetf(int type) {

        return AmanithVGJNI.vgGetf(type);
    }

    public int vgGeti(int type) {

        return AmanithVGJNI.vgGeti(type);
    }

    public int vgGetVectorSize(int type) {

        return AmanithVGJNI.vgGetVectorSize(type);
    }

    public void vgGetfv(int type, int count, float[] values, int offset) {
    
        // check arguments
        checkContextParameterLength(type, count, values, offset);
        AmanithVGJNI.vgGetfvA(type, count, values, offset);
    }

    public void vgGetfv(int type, int count, float[] values) {

        vgGetfv(type, count, values, 0);
    }

    public void vgGetiv(int type, int count, int[] values, int offset) {
    
        // check arguments
        checkContextParameterLength(type, count, values, offset);
        AmanithVGJNI.vgGetivA(type, count, values, offset);
    }

    public void vgGetiv(int type, int count, int[] values) {

        vgGetiv(type, count, values, 0);
    }

    public void vgSetParameterf(VGHandle object, int paramType, float value) {

        AmanithVGJNI.vgSetParameterf(VGHandle.getHandle(object), paramType, value);
    }

    public void vgSetParameteri(VGHandle object, int paramType, int value) {

        AmanithVGJNI.vgSetParameteri(VGHandle.getHandle(object), paramType, value);
    }

    public void vgSetParameterfv(VGHandle object, int paramType, int count, final float[] values, int offset) {

        // check arguments
        checkObjectParameterLength(object, paramType, count, values, offset);
        AmanithVGJNI.vgSetParameterfvA(VGHandle.getHandle(object), paramType, count, values, offset);
    }

    public void vgSetParameterfv(VGHandle object, int paramType, int count, final float[] values) {

        vgSetParameterfv(object, paramType, count, values, 0);
    }

    public void vgSetParameteriv(VGHandle object, int paramType, int count, final int[] values, int offset) {

        // check arguments
        checkObjectParameterLength(object, paramType, count, values, offset);
        AmanithVGJNI.vgSetParameterivA(VGHandle.getHandle(object), paramType, count, values, offset);
    }

    public void vgSetParameteriv(VGHandle object, int paramType, int count, final int[] values) {

        vgSetParameteriv(object, paramType, count, values, 0);
    }

    public float vgGetParameterf(VGHandle object, int paramType) {
    
        return AmanithVGJNI.vgGetParameterf(VGHandle.getHandle(object), paramType);
    }

    public int vgGetParameteri(VGHandle object, int paramType) {

        return AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(object), paramType);
    }

    public int vgGetParameterVectorSize(VGHandle object, int paramType) {

        return AmanithVGJNI.vgGetParameterVectorSize(VGHandle.getHandle(object), paramType);
    }

    public void vgGetParameterfv(VGHandle object, int paramType, int count, float[] values, int offset) {

        // check arguments
        checkObjectParameterLength(object, paramType, count, values, offset);
        AmanithVGJNI.vgGetParameterfvA(VGHandle.getHandle(object), paramType, count, values, offset);
    }

    public void vgGetParameterfv(VGHandle object, int paramType, int count, float[] values) {

        vgGetParameterfv(object, paramType, count, values, 0);
    }

    public void vgGetParameteriv(VGHandle object, int paramType, int count, int[] values, int offset) {

        // check arguments
        checkObjectParameterLength(object, paramType, count, values, offset);
        AmanithVGJNI.vgGetParameterivA(VGHandle.getHandle(object), paramType, count, values, offset);
    }

    public void vgGetParameteriv(VGHandle object, int paramType, int count, int[] values) {

        vgGetParameteriv(object, paramType, count, values, 0);
    }

    /*-------------------------------------------------------------------------------
                                   Matrix Manipulation
    -------------------------------------------------------------------------------*/
    public void vgLoadIdentity() {

        AmanithVGJNI.vgLoadIdentity();
    }

    public void vgLoadMatrix(final float[] m, int offset) {

        // check arguments
        checkMatrixLength(m, offset, 9);
        AmanithVGJNI.vgLoadMatrixA(m, offset);
    }

    public void vgLoadMatrix(final float[] m) {

        vgLoadMatrix(m, 0);
    }

    public void vgGetMatrix(float[] m, int offset) {

        // check arguments
        checkMatrixLength(m, offset, 9);
        AmanithVGJNI.vgGetMatrixA(m, offset);
    }

    public void vgGetMatrix(float[] m) {

        vgGetMatrix(m, 0);
    }

    public void vgMultMatrix(final float[] m, int offset) {
        
        // check arguments
        checkMatrixLength(m, offset, 9);
        AmanithVGJNI.vgMultMatrixA(m, offset);
    }

    public void vgMultMatrix(final float[] m) {

        vgMultMatrix(m, 0);
    }

    public void vgTranslate(float tx, float ty) {

        AmanithVGJNI.vgTranslate(tx, ty);
    }

    public void vgScale(float sx, float sy) {

        AmanithVGJNI.vgScale(sx, sy);
    }

    public void vgShear(float shx, float shy) {

        AmanithVGJNI.vgShear(shx, shy);
    }

    public void vgRotate(float angle) {

        AmanithVGJNI.vgRotate(angle);
    }

    /*-------------------------------------------------------------------------------
                                  Masking and Clearing
    -------------------------------------------------------------------------------*/
    public void vgMask(VGImage mask, int operation, int x, int y, int width, int height) {

        AmanithVGJNI.vgMask(VGHandle.getHandle(mask), operation, x, y, width, height);
    }

    public void vgMask(VGHandle mask, int operation, int x, int y, int width, int height) {

        AmanithVGJNI.vgMask(VGHandle.getHandle(mask), operation, x, y, width, height);
    }

    public void vgRenderToMask(VGPath path, int paintModes, int operation) {

        AmanithVGJNI.vgRenderToMask(VGHandle.getHandle(path), paintModes, operation);
    }

    public VGMaskLayer vgCreateMaskLayer(int width, int height) {

        int handle = AmanithVGJNI.vgCreateMaskLayer(width, height);
        return (handle == 0) ? null : new VGMaskLayer(handle);
    }

    public void vgDestroyMaskLayer(VGMaskLayer maskLayer) {

        AmanithVGJNI.vgDestroyMaskLayer(VGHandle.getHandle(maskLayer));
    }

    public void vgFillMaskLayer(VGMaskLayer maskLayer, int x, int y, int width, int height, float value) {

        AmanithVGJNI.vgFillMaskLayer(VGHandle.getHandle(maskLayer), x, y, width, height, value);
    }

    public void vgCopyMask(VGMaskLayer maskLayer, int dx, int dy, int sx, int sy, int width, int height) {

        AmanithVGJNI.vgCopyMask(VGHandle.getHandle(maskLayer), dx, dy, sx, sy, width, height);
    }

    public void vgClear(int x, int y, int width, int height) {

        AmanithVGJNI.vgClear(x, y, width, height);
    }

    /*-------------------------------------------------------------------------------
                                          Paths
    -------------------------------------------------------------------------------*/
    public VGPath vgCreatePath(int pathFormat, int datatype, float scale, float bias, int segmentCapacityHint, int coordCapacityHint, int capabilities) {

        int handle = AmanithVGJNI.vgCreatePath(pathFormat, datatype, scale, bias, segmentCapacityHint, coordCapacityHint, capabilities);
        return (handle == 0) ? null : new VGPath(handle);
    }

    public void vgClearPath(VGPath path, int capabilities) {

        AmanithVGJNI.vgClearPath(VGHandle.getHandle(path), capabilities);
    }

    public void vgDestroyPath(VGPath path) {

        AmanithVGJNI.vgDestroyPath(VGHandle.getHandle(path));
    }

    public void vgRemovePathCapabilities(VGPath path, int capabilities) {

        AmanithVGJNI.vgRemovePathCapabilities(VGHandle.getHandle(path), capabilities);
    }

    public int vgGetPathCapabilities(VGPath path) {

        return AmanithVGJNI.vgGetPathCapabilities(VGHandle.getHandle(path));
    }

    public void vgAppendPath(VGPath dstPath, VGPath srcPath) {

        AmanithVGJNI.vgAppendPath(VGHandle.getHandle(dstPath), VGHandle.getHandle(srcPath));
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final byte[] pathData, int pathDataOffset) {
        
        // check arguments
        if (pathSegments == null)
            throw new IllegalArgumentException("pathSegments == null");
        if (pathSegmentsOffset < 0)
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (pathDataOffset < 0)
            throw new IllegalArgumentException("pathDataOffset < 0");

        // check if the path is compatible with byte coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_S_8)
            throw new IllegalArgumentException("path does not support byte coordinates (pathData)");

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_BYTE);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final byte[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final short[] pathData, int pathDataOffset) {

        // check arguments
        if (pathSegments == null)
            throw new IllegalArgumentException("pathSegments == null");
        if (pathSegmentsOffset < 0)
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (pathDataOffset < 0)
            throw new IllegalArgumentException("pathDataOffset < 0");

        // check if the path is compatible with short coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_S_16)
            throw new IllegalArgumentException("path does not support short integer coordinates (pathData)");

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_SHORT);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final short[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final int[] pathData, int pathDataOffset) {

        // check arguments
        if (pathSegments == null)
            throw new IllegalArgumentException("pathSegments == null");
        if (pathSegmentsOffset < 0)
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (pathDataOffset < 0)
            throw new IllegalArgumentException("pathDataOffset < 0");

        // check if the path is compatible with integer coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_S_32)
            throw new IllegalArgumentException("path does not support integer coordinates (pathData)");

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_INT);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final int[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final float[] pathData, int pathDataOffset) {

        // check arguments
        if (pathSegments == null)
            throw new IllegalArgumentException("pathSegments == null");
        if (pathSegmentsOffset < 0)
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (pathDataOffset < 0)
            throw new IllegalArgumentException("pathDataOffset < 0");

        // check if the path is compatible with float coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_F)
            throw new IllegalArgumentException("path does not support float coordinates (pathData)");

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_FLOAT);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final float[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final byte[] pathData, int offset) {

        // check arguments
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        // check if the path is compatible with byte coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_S_8)
            throw new IllegalArgumentException("path does not support byte coordinates (pathData)");

        AmanithVGJNI.vgModifyPathCoordsA(VGHandle.getHandle(dstPath), startIndex, numSegments, pathData, offset, ARRAY_TYPE_BYTE);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final byte[] pathData) {

        vgModifyPathCoords(dstPath, startIndex, numSegments, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final short[] pathData, int offset) {

        // check arguments
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        // check if the path is compatible with short coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_S_16)
            throw new IllegalArgumentException("path does not support short integer coordinates (pathData)");

        AmanithVGJNI.vgModifyPathCoordsA(VGHandle.getHandle(dstPath), startIndex, numSegments, pathData, offset, ARRAY_TYPE_SHORT);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final short[] pathData) {

        vgModifyPathCoords(dstPath, startIndex, numSegments, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final int[] pathData, int offset) {

        // check arguments
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        // check if the path is compatible with integer coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_S_32)
            throw new IllegalArgumentException("path does not support integer coordinates (pathData)");

        AmanithVGJNI.vgModifyPathCoordsA(VGHandle.getHandle(dstPath), startIndex, numSegments, pathData, offset, ARRAY_TYPE_INT);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final int[] pathData) {

        vgModifyPathCoords(dstPath, startIndex, numSegments, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final float[] pathData, int offset) {

        // check arguments
        if (pathData == null)
            throw new IllegalArgumentException("pathData == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        // check if the path is compatible with float coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if (AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR && pathDataType != VG101.VG_PATH_DATATYPE_F)
            throw new IllegalArgumentException("path does not support float coordinates (pathData)");

        AmanithVGJNI.vgModifyPathCoordsA(VGHandle.getHandle(dstPath), startIndex, numSegments, pathData, offset, ARRAY_TYPE_FLOAT);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final float[] pathData) {

        vgModifyPathCoords(dstPath, startIndex, numSegments, pathData, 0);
    }

    public void vgTransformPath(VGPath dstPath, VGPath srcPath) {

        AmanithVGJNI.vgTransformPath(VGHandle.getHandle(dstPath), VGHandle.getHandle(srcPath));
    }

    public boolean vgInterpolatePath(VGPath dstPath, VGPath startPath, VGPath endPath, float amount) {

        return AmanithVGJNI.vgInterpolatePath(VGHandle.getHandle(dstPath), VGHandle.getHandle(startPath), VGHandle.getHandle(endPath), amount);
    }

    public float vgPathLength(VGPath path, int startSegment, int numSegments) {

        return AmanithVGJNI.vgPathLength(VGHandle.getHandle(path), startSegment, numSegments);
    }

    public void vgPointAlongPath(VGPath path, int startSegment, int numSegments, float distance, float[] values, int offset) {

        // check arguments
        if (values == null)
            throw new IllegalArgumentException("values == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
        if (values.length - offset < 4)
            throw new IllegalArgumentException("not enough remaining entries (values)");

        AmanithVGJNI.vgPointAlongPathA(VGHandle.getHandle(path), startSegment, numSegments, distance, values, offset);
    }

    public void vgPointAlongPath(VGPath path, int startSegment, int numSegments, float distance, float[] values) {

        vgPointAlongPath(path, startSegment, numSegments, distance, values, 0);
    }

    public void vgPathBounds(VGPath path, float[] values, int offset) {

        // check arguments
        if (values == null)
            throw new IllegalArgumentException("values == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
        if (values.length - offset < 4)
            throw new IllegalArgumentException("not enough remaining entries (values)");
            
        AmanithVGJNI.vgPathBoundsA(VGHandle.getHandle(path), values, offset);
    }

    public void vgPathBounds(VGPath path, float[] values) {

        vgPathBounds(path, values, 0);
    }

    public void vgPathTransformedBounds(VGPath path, float[] values, int offset) {

        // check arguments
        if (values == null)
            throw new IllegalArgumentException("values == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
        if (values.length - offset < 4)
            throw new IllegalArgumentException("not enough remaining entries (values)");
            
        AmanithVGJNI.vgPathTransformedBoundsA(VGHandle.getHandle(path), values, offset);
    }

    public void vgPathTransformedBounds(VGPath path, float[] values) {

        vgPathTransformedBounds(path, values, 0);
    }

    public void vgDrawPath(VGPath path, int paintModes) {

        AmanithVGJNI.vgDrawPath(VGHandle.getHandle(path), paintModes);
    }

    /*-------------------------------------------------------------------------------
                                          Paint
    -------------------------------------------------------------------------------*/
    public VGPaint vgCreatePaint() {

        int handle = AmanithVGJNI.vgCreatePaint();
        return (handle == 0) ? null : new VGPaint(handle);
    }

    public void vgDestroyPaint(VGPaint paint) {

        AmanithVGJNI.vgDestroyPaint(VGHandle.getHandle(paint));
    }

    public void vgSetPaint(VGPaint paint, int paintModes) {

        AmanithVGJNI.vgSetPaint(VGHandle.getHandle(paint), paintModes);
    }

    public VGPaint vgGetPaint(int paintMode) {

        int handle = AmanithVGJNI.vgGetPaint(paintMode);
        return (handle == 0) ? null : new VGPaint(handle);
    }

    public void vgSetColor(VGPaint paint, int rgba) {

        AmanithVGJNI.vgSetColor(VGHandle.getHandle(paint), rgba);
    }

    public int vgGetColor(VGPaint paint) {

        return AmanithVGJNI.vgGetColor(VGHandle.getHandle(paint));
    }

    public void vgPaintPattern(VGPaint paint, VGImage pattern) {

        AmanithVGJNI.vgPaintPattern(VGHandle.getHandle(paint), VGHandle.getHandle(pattern));
    }

    /*-------------------------------------------------------------------------------
                                          Images
    -------------------------------------------------------------------------------*/
    public VGImage vgCreateImage(int format, int width, int height, int allowedQuality) {

        int handle = AmanithVGJNI.vgCreateImage(format, width, height, allowedQuality);
        return (handle == 0) ? null : new VGImage(handle);
    }

    public void vgDestroyImage(VGImage image) {

        AmanithVGJNI.vgDestroyImage(VGHandle.getHandle(image));
    }

    public void vgClearImage(VGImage image, int x, int y, int width, int height) {

        AmanithVGJNI.vgClearImage(VGHandle.getHandle(image), x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final byte[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final byte[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final short[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final short[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final int[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final int[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, byte[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgGetImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, byte[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, short[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgGetImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, short[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, int[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgGetImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, int[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public VGImage vgChildImage(VGImage parent, int x, int y, int width, int height) {

        int handle = AmanithVGJNI.vgChildImage(VGHandle.getHandle(parent), x, y, width, height);
        return (handle == 0) ? null : new VGImage(handle);
    }

    public VGImage vgGetParent(VGImage image) {

        int handle = AmanithVGJNI.vgGetParent(VGHandle.getHandle(image));
        return (handle == 0) ? null : new VGImage(handle);
    }

    public void vgCopyImage(VGImage dst, int dx, int dy, VGImage src, int sx, int sy, int width, int height, boolean dither) {

        AmanithVGJNI.vgCopyImage(VGHandle.getHandle(dst), dx, dy, VGHandle.getHandle(src), sx, sy, width, height, dither);
    }

    public void vgDrawImage(VGImage image) {

        AmanithVGJNI.vgDrawImage(VGHandle.getHandle(image));
    }

    public void vgSetPixels(int dx, int dy, VGImage src, int sx, int sy, int width, int height) {

        AmanithVGJNI.vgSetPixels(dx, dy, VGHandle.getHandle(src), sx, sy, width, height);
    }

    public void vgWritePixels(final byte[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        AmanithVGJNI.vgWritePixelsA(data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final byte[] data, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final short[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgWritePixelsA(data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final short[] data, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final int[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
            
        AmanithVGJNI.vgWritePixelsA(data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final int[] data, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgGetPixels(VGImage dst, int dx, int dy, int sx, int sy, int width, int height) {

        AmanithVGJNI.vgGetPixels(VGHandle.getHandle(dst), dx, dy, sx, sy, width, height);
    }

    public void vgReadPixels(byte[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        AmanithVGJNI.vgReadPixelsA(data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(byte[] data, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(short[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        AmanithVGJNI.vgReadPixelsA(data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(short[] data, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(int[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        // check arguments
        if (data == null)
            throw new IllegalArgumentException("data == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");

        AmanithVGJNI.vgReadPixelsA(data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(int[] data, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }
    
    public void vgCopyPixels(int dx, int dy, int sx, int sy, int width, int height) {

        AmanithVGJNI.vgCopyPixels(dx, dy, sx, sy, width, height);
    }

    /*-------------------------------------------------------------------------------
                                          Text
    -------------------------------------------------------------------------------*/
    public VGFont vgCreateFont(int glyphCapacityHint) {

        int handle = AmanithVGJNI.vgCreateFont(glyphCapacityHint);
        return (handle == 0) ? null : new VGFont(handle);   
    }

    public void vgDestroyFont(VGFont font) {

        AmanithVGJNI.vgDestroyFont(VGHandle.getHandle(font));
    }

    public void vgSetGlyphToPath(VGFont font, int glyphIndex, VGPath path, boolean isHinted, final float[] glyphOrigin, int glyphOriginOffset, final float[] escapement, int escapementOffset) {

        // check arguments
        if (glyphOrigin == null)
            throw new IllegalArgumentException("glyphOrigin == null");
        if (glyphOriginOffset < 0)
            throw new IllegalArgumentException("glyphOriginOffset < 0");
        if (glyphOrigin.length - glyphOriginOffset < 2)
            throw new IllegalArgumentException("not enough remaining entries (glyphOrigin)");
        if (escapement == null)
            throw new IllegalArgumentException("escapement == null");
        if (escapementOffset < 0)
            throw new IllegalArgumentException("escapementOffset < 0");
        if (escapement.length - escapementOffset < 2)
            throw new IllegalArgumentException("not enough remaining entries (escapement)");

        AmanithVGJNI.vgSetGlyphToPathA(VGHandle.getHandle(font), glyphIndex, VGHandle.getHandle(path), isHinted, glyphOrigin, glyphOriginOffset, escapement, escapementOffset);
    }

    public void vgSetGlyphToPath(VGFont font, int glyphIndex, VGPath path, boolean isHinted, final float[] glyphOrigin, final float[] escapement) {

        vgSetGlyphToPath(font, glyphIndex, path, isHinted, glyphOrigin, 0, escapement, 0);
    }

    public void vgSetGlyphToImage(VGFont font, int glyphIndex, VGImage image, final float[] glyphOrigin, int glyphOriginOffset, final float[] escapement, int escapementOffset) {

        // check arguments
        if (glyphOrigin == null)
            throw new IllegalArgumentException("glyphOrigin == null");
        if (glyphOriginOffset < 0)
            throw new IllegalArgumentException("glyphOriginOffset < 0");
        if (glyphOrigin.length - glyphOriginOffset < 2)
            throw new IllegalArgumentException("not enough remaining entries (glyphOrigin)");
        if (escapement == null)
            throw new IllegalArgumentException("escapement == null");
        if (escapementOffset < 0)
            throw new IllegalArgumentException("escapementOffset < 0");
        if (escapement.length - escapementOffset < 2)
            throw new IllegalArgumentException("not enough remaining entries (escapement)");

        AmanithVGJNI.vgSetGlyphToImageA(VGHandle.getHandle(font), glyphIndex, VGHandle.getHandle(image), glyphOrigin, glyphOriginOffset, escapement, escapementOffset);
    }

    public void vgSetGlyphToImage(VGFont font, int glyphIndex, VGImage image, final float[] glyphOrigin, final float[] escapement) {

        vgSetGlyphToImage(font, glyphIndex, image, glyphOrigin, 0, escapement, 0);
    }

    public void vgClearGlyph(VGFont font, int glyphIndex) {

        AmanithVGJNI.vgClearGlyph(VGHandle.getHandle(font), glyphIndex);
    }

    public void vgDrawGlyph(VGFont font, int glyphIndex, int paintModes, boolean allowAutoHinting) {

        AmanithVGJNI.vgDrawGlyph(VGHandle.getHandle(font), glyphIndex, paintModes, allowAutoHinting);
    }

    public void vgDrawGlyphs(VGFont font, int glyphCount, final int[] glyphIndices, int glyphIndicesOffset, final float[] adjustments_x, int adjustments_x_offset, final float[] adjustments_y, int adjustments_y_offset, int paintModes, boolean allowAutoHinting) {

        // check glyphIndices
        if (glyphIndices == null)
            throw new IllegalArgumentException("glyphIndices == null");
        if (glyphIndicesOffset < 0)
            throw new IllegalArgumentException("glyphIndicesOffset < 0");
        if (glyphIndices.length - glyphIndicesOffset < glyphCount)
            throw new IllegalArgumentException("not enough remaining entries (glyphIndices)");
        // check horizontal adjustments
        if (adjustments_x != null) {
            if (adjustments_x_offset < 0)
                throw new IllegalArgumentException("adjustments_x_offset < 0");
            if (adjustments_x.length - adjustments_x_offset < glyphCount)
                throw new IllegalArgumentException("not enough remaining entries (adjustments_x)");
        }
        // check vertical adjustments
        if (adjustments_y != null) {
            if (adjustments_y_offset < 0)
                throw new IllegalArgumentException("adjustments_y_offset < 0");
            if (adjustments_y.length - adjustments_y_offset < glyphCount)
                throw new IllegalArgumentException("not enough remaining entries (adjustments_y)");
        }

        AmanithVGJNI.vgDrawGlyphsA(VGHandle.getHandle(font), glyphCount, glyphIndices, glyphIndicesOffset, adjustments_x, adjustments_x_offset, adjustments_y, adjustments_y_offset, paintModes, allowAutoHinting);
    }

    public void vgDrawGlyphs(VGFont font, int glyphCount, final int[] glyphIndices, final float[] adjustments_x, final float[] adjustments_y, int paintModes, boolean allowAutoHinting) {

        vgDrawGlyphs(font, glyphCount, glyphIndices, 0, adjustments_x, 0, adjustments_y, 0, paintModes, allowAutoHinting);
    }

    public void vgDrawGlyphs(VGFont font, final int[] glyphIndices, final float[] adjustments_x, final float[] adjustments_y, int paintModes, boolean allowAutoHinting) {

        // this check is necessary because we are going to access glyphIndices.length field
        if (glyphIndices == null)
            throw new IllegalArgumentException("glyphIndices == null");

        vgDrawGlyphs(font, glyphIndices.length, glyphIndices, 0, adjustments_x, 0, adjustments_y, 0, paintModes, allowAutoHinting);
    }

    /*-------------------------------------------------------------------------------
                                      Image Filters
    -------------------------------------------------------------------------------*/
    public void vgColorMatrix(VGImage dst, VGImage src, final float[] matrix, int offset) {

        // check arguments
        checkMatrixLength(matrix, offset, 20);
        AmanithVGJNI.vgColorMatrixA(VGHandle.getHandle(dst), VGHandle.getHandle(src), matrix, offset);
    }

    public void vgColorMatrix(VGImage dst, VGImage src, final float[] matrix) {

        vgColorMatrix(dst, src, matrix, 0);
    }

    public void vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, int offset, float scale, float bias, int tilingMode) {

        // check arguments
        if (kernel == null)
            throw new IllegalArgumentException("kernel == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
        if (kernel.length - offset < kernelWidth * kernelHeight)
            throw new IllegalArgumentException("not enough remaining entries (kernel)");
        
        AmanithVGJNI.vgConvolveA(VGHandle.getHandle(dst), VGHandle.getHandle(src), kernelWidth, kernelHeight, shiftX, shiftY, kernel, offset, scale, bias, tilingMode);
    }

    public void vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, float scale, float bias, int tilingMode) {

        vgConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernel, 0, scale, bias, tilingMode);
    }

    public void vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, int kernelXOffset, final short[] kernelY, int kernelYOffset, float scale, float bias, int tilingMode) {

        // check arguments
        if (kernelX == null)
            throw new IllegalArgumentException("kernelX == null");
        if (kernelXOffset < 0)
            throw new IllegalArgumentException("kernelXOffset < 0");
        if (kernelX.length - kernelXOffset < kernelWidth)
            throw new IllegalArgumentException("not enough remaining entries (kernelX)");
        if (kernelY == null)
            throw new IllegalArgumentException("kernelY == null");
        if (kernelYOffset < 0)
            throw new IllegalArgumentException("kernelYOffset < 0");
        if (kernelY.length - kernelYOffset < kernelHeight)
            throw new IllegalArgumentException("not enough remaining entries (kernelY)");

        AmanithVGJNI.vgSeparableConvolveA(VGHandle.getHandle(dst), VGHandle.getHandle(src), kernelWidth, kernelHeight, shiftX, shiftY, kernelX, kernelXOffset, kernelY, kernelYOffset, scale, bias, tilingMode);
    }

    public void vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, final short[] kernelY, float scale, float bias, int tilingMode) {

        vgSeparableConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernelX, 0, kernelY, 0, scale, bias, tilingMode);
    }

    public void vgGaussianBlur(VGImage dst, VGImage src, float stdDeviationX, float stdDeviationY, int tilingMode) {

        AmanithVGJNI.vgGaussianBlur(VGHandle.getHandle(dst), VGHandle.getHandle(src), stdDeviationX, stdDeviationY, tilingMode);
    }

    public void vgLookup(VGImage dst, VGImage src, final byte[] redLUT, int redLUTOffset, final byte[] greenLUT, int greenLUTOffset, final byte[] blueLUT, int blueLUTOffset, final byte[] alphaLUT, int alphaLUTOffset, boolean outputLinear, boolean outputPremultiplied) {

        // check arguments
        if (redLUT == null)
            throw new IllegalArgumentException("redLUT == null");
        if (redLUTOffset < 0)
            throw new IllegalArgumentException("redLUTOffset < 0");
        if (redLUT.length - redLUTOffset < 256)
            throw new IllegalArgumentException("not enough remaining entries (redLUT)");
        if (greenLUT == null)
            throw new IllegalArgumentException("greenLUT == null");
        if (greenLUTOffset < 0)
            throw new IllegalArgumentException("greenLUTOffset < 0");
        if (greenLUT.length - greenLUTOffset < 256)
            throw new IllegalArgumentException("not enough remaining entries (greenLUT)");
        if (blueLUT == null)
            throw new IllegalArgumentException("blueLUT == null");
        if (blueLUTOffset < 0)
            throw new IllegalArgumentException("blueLUTOffset < 0");
        if (blueLUT.length - blueLUTOffset < 256)
            throw new IllegalArgumentException("not enough remaining entries (blueLUT)");
        if (alphaLUT == null)
            throw new IllegalArgumentException("alphaLUT == null");
        if (alphaLUTOffset < 0)
            throw new IllegalArgumentException("alphaLUTOffset < 0");
        if (alphaLUT.length - alphaLUTOffset < 256)
            throw new IllegalArgumentException("not enough remaining entries (alphaLUT)");          

        AmanithVGJNI.vgLookupA(VGHandle.getHandle(dst), VGHandle.getHandle(src), redLUT, redLUTOffset, greenLUT, greenLUTOffset, blueLUT, blueLUTOffset, alphaLUT, alphaLUTOffset, outputLinear, outputPremultiplied);
    }

    public void vgLookup(VGImage dst, VGImage src, final byte[] redLUT, final byte[] greenLUT, final byte[] blueLUT, final byte[] alphaLUT, boolean outputLinear, boolean outputPremultiplied) {

        vgLookup(dst, src, redLUT, 0, greenLUT, 0, blueLUT, 0, alphaLUT, 0, outputLinear, outputPremultiplied);
    }

    public void vgLookupSingle(VGImage dst, VGImage src, final int[] lookupTable, int offset, int sourceChannel, boolean outputLinear, boolean outputPremultiplied) {

        // check arguments
        if (lookupTable == null)
            throw new IllegalArgumentException("lookupTable == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
        if (lookupTable.length - offset < 256)
            throw new IllegalArgumentException("not enough remaining entries (lookupTable)");

        AmanithVGJNI.vgLookupSingleA(VGHandle.getHandle(dst), VGHandle.getHandle(src), lookupTable, offset, sourceChannel, outputLinear, outputPremultiplied);
    }

    public void vgLookupSingle(VGImage dst, VGImage src, final int[] lookupTable, int sourceChannel, boolean outputLinear, boolean outputPremultiplied) {

        vgLookupSingle(dst, src, lookupTable, 0, sourceChannel, outputLinear, outputPremultiplied);
    }

    /*-------------------------------------------------------------------------------
                                    Hardware Queries
    -------------------------------------------------------------------------------*/
    public int vgHardwareQuery(int key, int setting) {

        return AmanithVGJNI.vgHardwareQuery(key, setting);
    }

    public java.lang.String vgGetString(int name) {

        java.lang.String s = AmanithVGJNI.vgGetString(name);
        return s;
    }

    /*-------------------------------------------------------------------------------
                                          VGU11
    -------------------------------------------------------------------------------*/
    public int vguLine(VGPath path, float x0, float y0, float x1, float y1) {

        return AmanithVGJNI.vguLine(VGHandle.getHandle(path), x0, y0, x1, y1);
    }

    public int vguPolygon(VGPath path, final float[] points, int offset, int count, boolean closed) {

        // check arguments
        if (points == null)
            throw new IllegalArgumentException("points == null");
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
        if (points.length - offset < count * 2)
            throw new IllegalArgumentException("not enough remaining entries (points)");
        
        return AmanithVGJNI.vguPolygonA(VGHandle.getHandle(path), points, offset, count, closed);
    }

    public int vguPolygon(VGPath path, final float[] points, int count, boolean closed) {

        return vguPolygon(path, points, 0, count, closed);
    }

    public int vguRect(VGPath path, float x, float y, float width, float height) {

        return AmanithVGJNI.vguRect(VGHandle.getHandle(path), x, y, width, height);
    }

    public int vguRoundRect(VGPath path, float x, float y, float width, float height, float arcWidth, float arcHeight) {

        return AmanithVGJNI.vguRoundRect(VGHandle.getHandle(path), x, y, width, height, arcWidth, arcHeight);
    }

    public int vguEllipse(VGPath path, float cx, float cy, float width, float height) {

        return AmanithVGJNI.vguEllipse(VGHandle.getHandle(path), cx, cy, width, height);
    }

    public int vguArc(VGPath path, float x, float y, float width, float height, float startAngle, float angleExtent, int arcType) {

        return AmanithVGJNI.vguArc(VGHandle.getHandle(path), x, y, width, height, startAngle, angleExtent, arcType);
    }

    public int vguComputeWarpQuadToSquare(float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset) {

        // check arguments
        checkMatrixLength(matrix, offset, 9);
        return AmanithVGJNI.vguComputeWarpQuadToSquareA(sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, offset);
    }

    public int vguComputeWarpQuadToSquare(float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix) {

        return vguComputeWarpQuadToSquare(sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, 0);
    }

    public int vguComputeWarpSquareToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float[] matrix, int offset) {

        // check arguments
        checkMatrixLength(matrix, offset, 9);
        return AmanithVGJNI.vguComputeWarpSquareToQuadA(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, matrix, offset);
    }

    public int vguComputeWarpSquareToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float[] matrix) {

        return vguComputeWarpSquareToQuad(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, matrix, 0);
    }

    public int vguComputeWarpQuadToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset) {

        // check arguments
        checkMatrixLength(matrix, offset, 9);
        return AmanithVGJNI.vguComputeWarpQuadToQuadA(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, offset);
    }

    public int vguComputeWarpQuadToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix) {

        return vguComputeWarpQuadToQuad(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, 0);
    }

    /*-------------------------------------------------------------------------------
                                        VG11Ext
    -------------------------------------------------------------------------------*/
    
    public void vgIterativeAverageBlurKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, int tilingMode) { }
    public void vgParametricFilterKHR(VGImage dst, VGImage src, VGImage blur, float strength, float offsetX, float offsetY, int filterFlags, VGPaint highlightPaint, VGPaint shadowPaint) { }
    public int vguDropShadowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int shadowColorRGBA) { return VGU_NO_ERROR; }
    public int vguGlowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, int filterFlags, int allowedQuality, int glowColorRGBA) { return VGU_NO_ERROR; }
    public int vguBevelKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int highlightColorRGBA, int shadowColorRGBA) { return VGU_NO_ERROR; }
    public int vguGradientGlowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final float[] glowColorRampStops, int offset) { return VGU_NO_ERROR; }
    public int vguGradientGlowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final FloatBuffer glowColorRampStops) { return VGU_NO_ERROR; }
    public int vguGradientBevelKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final float[] bevelColorRampStops, int offset) { return VGU_NO_ERROR; }
    public int vguGradientBevelKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final FloatBuffer bevelColorRampStops) { return VGU_NO_ERROR; }
    public void vgProjectiveMatrixNDS(boolean enable) { }
    public int vguTransformClipLineNDS(final float Ain, final float Bin, final float Cin, final float[] matrix, int matrixOffset, final boolean inverse, float[] Aout, int AoutOffset, float[] Bout, int BoutOffset, float[] Cout, int CoutOffset) { return VGU_NO_ERROR; }
    public int vguTransformClipLineNDS(final float Ain, final float Bin, final float Cin, final FloatBuffer matrix, final int inverse, FloatBuffer Aout, FloatBuffer Bout, FloatBuffer Cout) { return VGU_NO_ERROR; }
    
    // VG_MZT_clip_path
    public void vgClipPathPushMZT(VGPath path) {

        AmanithVGJNI.vgClipPathPushMZT(VGHandle.getHandle(path));
    }

    public void vgClipPathPopMZT() {

        AmanithVGJNI.vgClipPathPopMZT();
    }

    public void vgClipPathClearMZT() {

        AmanithVGJNI.vgClipPathClearMZT();
    }

    // VG_MZT_filters
    public void vgColorMatrixMZT(VGImage img, final float[] matrix, int offset) {

        // check arguments
        checkMatrixLength(matrix, offset, 20);
        AmanithVGJNI.vgColorMatrixMZT(VGHandle.getHandle(img), matrix, offset);
    }

    public void vgColorMatrixMZT(VGImage img, final float[] matrix) {

        vgColorMatrixMZT(img, matrix, 0);
    }

    public long vgPrivContextCreateMZT(long sharedContext) {

        return AmanithVGJNI.vgPrivContextCreateMZT(sharedContext);
    }

    public void vgPrivContextDestroyMZT(long context) {

        AmanithVGJNI.vgPrivContextDestroyMZT(context);
    }

	public int vgPrivSurfaceMaxDimensionGetMZT() {

		return AmanithVGJNI.vgPrivSurfaceMaxDimensionGetMZT();
	}

    public long vgPrivSurfaceCreateMZT(int width, int height, boolean linearColorSpace, boolean alphaPremultiplied, boolean alphaMask) {

        return AmanithVGJNI.vgPrivSurfaceCreateMZT(width, height, linearColorSpace, alphaPremultiplied, alphaMask);
    }

    public long vgPrivSurfaceCreateFromImageMZT(VGImage image, boolean alphaMask) {

        return AmanithVGJNI.vgPrivSurfaceCreateFromImageMZT(VGHandle.getHandle(image), alphaMask);
    }

    public boolean vgPrivSurfaceResizeMZT(long surface, int width, int height) {

        return AmanithVGJNI.vgPrivSurfaceResizeMZT(surface, width, height);
    }

    public void vgPrivSurfaceDestroyMZT(long surface) {

        AmanithVGJNI.vgPrivSurfaceDestroyMZT(surface);
    }

    public int vgPrivGetSurfaceWidthMZT(final long surface) {

        return AmanithVGJNI.vgPrivGetSurfaceWidthMZT(surface);
    }

    public int vgPrivGetSurfaceHeightMZT(final long surface) {

        return AmanithVGJNI.vgPrivGetSurfaceHeightMZT(surface);
    }

    public int vgPrivGetSurfaceFormatMZT(final long surface) {

        return AmanithVGJNI.vgPrivGetSurfaceFormatMZT(surface);
    }

    public java.nio.ByteBuffer vgPrivGetSurfacePixelsMZT(final long surface) {

        return AmanithVGJNI.vgPrivGetSurfacePixelsMZT(surface);
    }

    public boolean vgPrivSurfaceCopyPixelsMZT(final long surface, int[] dstPixels, int offset, boolean redBlueSwap) {

        boolean result = false;
        int srfWidth = vgPrivGetSurfaceWidthMZT(surface);
        int srfHeight = vgPrivGetSurfaceHeightMZT(surface);
        
        // check arguments
        if ((srfWidth <= 0) || (srfHeight <= 0)) {
            throw new IllegalArgumentException("invalid surface");
        }
        else
        if (dstPixels == null) {
            throw new IllegalArgumentException("dstPixels == null");
        }
        else
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        else
        if (dstPixels.length - offset < srfWidth * srfHeight) {
            throw new IllegalArgumentException("not enough remaining entries (dstPixels)");
        }
        else {
            AmanithVGJNI.vgPrivCopySurfacePixelsMZT(surface, dstPixels, offset, redBlueSwap);
            result = true;
        }
        return result;
    }

    public boolean vgPrivSurfaceCopyPixelsMZT(final long surface, int[] dstPixels, boolean redBlueSwap) {

        return vgPrivSurfaceCopyPixelsMZT(surface, dstPixels, 0, redBlueSwap);
    }

    public boolean vgPrivMakeCurrentMZT(long context, long surface) {

        return AmanithVGJNI.vgPrivMakeCurrentMZT(context, surface);
    }

    public int vgGetSurfaceWidthMZT() {

        return AmanithVGJNI.vgGetSurfaceWidthMZT();
    }

    public int vgGetSurfaceHeightMZT() {

        return AmanithVGJNI.vgGetSurfaceHeightMZT();
    }

    public int vgGetSurfaceFormatMZT() {

        return AmanithVGJNI.vgGetSurfaceFormatMZT();
    }

    public java.nio.ByteBuffer vgGetSurfacePixelsMZT() {

        return AmanithVGJNI.vgGetSurfacePixelsMZT();
    }

    public boolean vgSurfaceCopyPixelsMZT(int[] dstPixels, int offset, boolean redBlueSwap) {

        boolean result = false;
        int srfWidth = vgGetSurfaceWidthMZT();
        int srfHeight = vgGetSurfaceHeightMZT();
        
        // check arguments
        if ((srfWidth <= 0) || (srfHeight <= 0)) {
            throw new IllegalArgumentException("invalid current surface");
        }
        else
        if (dstPixels == null) {
            throw new IllegalArgumentException("dstPixels == null");
        }
        else
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        else
        if (dstPixels.length - offset < srfWidth * srfHeight) {
            throw new IllegalArgumentException("not enough remaining entries (dstPixels)");
        }
        else {
            AmanithVGJNI.vgCopySurfacePixelsMZT(dstPixels, offset, redBlueSwap);
            result = true;
        }
        return result;
    }
    
    public void vgSurfaceCopyPixelsMZT(int[] dstPixels, boolean redBlueSwap) {

        vgSurfaceCopyPixelsMZT(dstPixels, 0, redBlueSwap);
    }

    public void vgPostSwapBuffersMZT() {

        AmanithVGJNI.vgPostSwapBuffersMZT();
    }
}
