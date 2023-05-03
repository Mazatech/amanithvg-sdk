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

package javax.microedition.khronos.openvg;
import android.support.annotation.NonNull;

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
        
        if ((type == VG_SCISSOR_RECTS) || (type == VG_STROKE_DASH_PATTERN)) {
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
        
        if ((type == VG_SCISSOR_RECTS) || (type == VG_STROKE_DASH_PATTERN)) {
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

    private void checkArrayLength(final float[] values, int offset, int arrayNeededSize, java.lang.String arrayName) {
        
        if (values == null) {
            throw new IllegalArgumentException(arrayName + " == null");
        }

        if (offset < 0) {
            throw new IllegalArgumentException(arrayName + " offset < 0");
        }

        if ((values.length - offset) < arrayNeededSize) {
            throw new IllegalArgumentException("not enough remaining entries (" + arrayName + ")");
        }
    }

    /*-------------------------------------------------------------------------------
                                    VG101, VG11
    -------------------------------------------------------------------------------*/
    public enum VGMaskOperation {
        Clear(VG_CLEAR_MASK),
        Fill(VG_FILL_MASK),
        Set(VG_SET_MASK),
        Union(VG_UNION_MASK),
        Intersect(VG_INTERSECT_MASK),
        Subtract(VG_SUBTRACT_MASK);
        VGMaskOperation(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGPathDatatype {
        Signed8(VG_PATH_DATATYPE_S_8),
        Signed16(VG_PATH_DATATYPE_S_16),
        Signed32(VG_PATH_DATATYPE_S_32),
        Float(VG_PATH_DATATYPE_F);
        VGPathDatatype(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGTilingMode {
        Fill(VG_TILE_FILL),
        Pad(VG_TILE_PAD),
        Repeat(VG_TILE_REPEAT),
        Reflect(VG_TILE_REFLECT);
        VGTilingMode(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGImageFormat {
        sRGBX8888(VG_sRGBX_8888),
        sRGBA8888(VG_sRGBA_8888),
        sRGBA8888Pre(VG_sRGBA_8888_PRE),
        sRGB565(VG_sRGB_565),
        sRGBA5551(VG_sRGBA_5551),
        sRGBA4444(VG_sRGBA_4444),
        sL8(VG_sL_8),
        lRGBX8888(VG_lRGBX_8888),
        lRGBA8888(VG_lRGBA_8888),
        lRGBA8888Pre(VG_lRGBA_8888_PRE),
        lL8(VG_lL_8),
        A8(VG_A_8),
        BW1(VG_BW_1),
        A1(VG_A_1),
        A4(VG_A_4),
        sXRGB8888(VG_sXRGB_8888),
        sARGB8888(VG_sARGB_8888),
        sARGB8888_PRE(VG_sARGB_8888_PRE),
        sARGB1555(VG_sARGB_1555),
        sARGB4444(VG_sARGB_4444),
        lXRGB8888(VG_lXRGB_8888),
        lARGB8888(VG_lARGB_8888),
        lARGB8888_PRE(VG_lARGB_8888_PRE),
        sBGRX8888(VG_sBGRX_8888),
        sBGRA8888(VG_sBGRA_8888),
        sBGRA8888Pre(VG_sBGRA_8888_PRE),
        sBGR565(VG_sBGR_565),
        sBGRA5551(VG_sBGRA_5551),
        sBGRA4444(VG_sBGRA_4444),
        lBGRX8888(VG_lBGRX_8888),
        lBGRA8888(VG_lBGRA_8888),
        lBGRA8888Pre(VG_lBGRA_8888_PRE),
        sXBGR8888(VG_sXBGR_8888),
        sABGR8888(VG_sABGR_8888),
        sABGR8888Pre(VG_sABGR_8888_PRE),
        sABGR1555(VG_sABGR_1555),
        sABGR4444(VG_sABGR_4444),
        lXBGR8888(VG_lXBGR_8888),
        lABGR8888(VG_lABGR_8888),
        lABGR8888Pre(VG_lABGR_8888_PRE);
        VGImageFormat(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGImageChannel {
        Red(VG_RED),
        Green(VG_GREEN),
        Blue(VG_BLUE),
        Alpha(VG_ALPHA);
        VGImageChannel(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGHardwareQueryType {
        ImageFormat(VG_IMAGE_FORMAT_QUERY),
        PathDatatype(VG_PATH_DATATYPE_QUERY);
        VGHardwareQueryType(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGHardwareQueryResult {
        Accelerated(VG_HARDWARE_ACCELERATED),
        Unaccelerated(VG_HARDWARE_UNACCELERATED);
        VGHardwareQueryResult(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        public static VGHardwareQueryResult fromValue(int vgEnum) {
            return _allValues[vgEnum];
        }
        private final int _vgEnum;
        private static final VGHardwareQueryResult[] _allValues = values();
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
        checkArrayLength(m, offset, 9, "m");
        AmanithVGJNI.vgLoadMatrixA(m, offset);
    }

    public void vgLoadMatrix(final float[] m) {

        vgLoadMatrix(m, 0);
    }

    public void vgGetMatrix(float[] m, int offset) {

        // check arguments
        checkArrayLength(m, offset, 9, "m");
        AmanithVGJNI.vgGetMatrixA(m, offset);
    }

    public void vgGetMatrix(float[] m) {

        vgGetMatrix(m, 0);
    }

    public void vgMultMatrix(final float[] m, int offset) {
        
        // check arguments
        checkArrayLength(m, offset, 9, "m");
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
    /**
     * This method uses a direct integer value for specifying the masking operation, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgMask(VGImage mask, VGMaskOperation operation, int x, int y, int width, int height)} instead.
     */
    public void vgMask(VGImage mask, int operation, int x, int y, int width, int height) {

        AmanithVGJNI.vgMask(VGHandle.getHandle(mask), operation, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the masking operation, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgMask(VGMaskLayer mask, VGMaskOperation operation, int x, int y, int width, int height)} instead.
     */
    public void vgMask(VGMaskLayer mask, int operation, int x, int y, int width, int height) {

        AmanithVGJNI.vgMask(VGHandle.getHandle(mask), operation, x, y, width, height);
    }

    public void vgMask(VGImage mask, @NonNull VGMaskOperation operation, int x, int y, int width, int height) {

        vgMask(mask, operation.getValue(), x, y, width, height);
    }

    public void vgMask(VGMaskLayer mask, @NonNull VGMaskOperation operation, int x, int y, int width, int height) {

        vgMask(mask, operation.getValue(), x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the masking operation, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgRenderToMask(VGPath path, int paintModes, VGMaskOperation operation)} instead.
     */
    public void vgRenderToMask(VGPath path, int paintModes, int operation) {

        AmanithVGJNI.vgRenderToMask(VGHandle.getHandle(path), paintModes, operation);
    }

    public void vgRenderToMask(VGPath path, int paintModes, @NonNull VGMaskOperation operation) {

        vgRenderToMask(path, paintModes, operation.getValue());
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
    /**
     * This method uses a direct integer value for specifying the path format and coordinates data type, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgCreatePath(VGPathDatatype datatype, float scale, float bias, int segmentCapacityHint, int coordCapacityHint, int capabilities)} instead.
     */
    public VGPath vgCreatePath(int pathFormat, int datatype, float scale, float bias, int segmentCapacityHint, int coordCapacityHint, int capabilities) {

        int handle = AmanithVGJNI.vgCreatePath(pathFormat, datatype, scale, bias, segmentCapacityHint, coordCapacityHint, capabilities);
        return (handle == 0) ? null : new VGPath(handle);
    }

    public VGPath vgCreatePath(@NonNull VGPathDatatype datatype, float scale, float bias, int segmentCapacityHint, int coordCapacityHint, int capabilities) {

        return vgCreatePath(VG_PATH_FORMAT_STANDARD, datatype.getValue(), scale, bias, segmentCapacityHint, coordCapacityHint, capabilities);
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
        if (pathSegments == null) {
            throw new IllegalArgumentException("pathSegments == null");
        }
        if (pathSegmentsOffset < 0) {
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        }
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (pathDataOffset < 0) {
            throw new IllegalArgumentException("pathDataOffset < 0");
        }

        // check if the path is compatible with byte coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_S_8)) {
            throw new IllegalArgumentException("path does not support byte coordinates (pathData)");
        }

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_BYTE);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final byte[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final short[] pathData, int pathDataOffset) {

        // check arguments
        if (pathSegments == null) {
            throw new IllegalArgumentException("pathSegments == null");
        }
        if (pathSegmentsOffset < 0) {
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        }
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (pathDataOffset < 0) {
            throw new IllegalArgumentException("pathDataOffset < 0");
        }

        // check if the path is compatible with short coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_S_16)) {
            throw new IllegalArgumentException("path does not support short integer coordinates (pathData)");
        }

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_SHORT);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final short[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final int[] pathData, int pathDataOffset) {

        // check arguments
        if (pathSegments == null) {
            throw new IllegalArgumentException("pathSegments == null");
        }
        if (pathSegmentsOffset < 0) {
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        }
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (pathDataOffset < 0) {
            throw new IllegalArgumentException("pathDataOffset < 0");
        }

        // check if the path is compatible with integer coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_S_32)) {
            throw new IllegalArgumentException("path does not support integer coordinates (pathData)");
        }

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_INT);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final int[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, int pathSegmentsOffset, final float[] pathData, int pathDataOffset) {

        // check arguments
        if (pathSegments == null) {
            throw new IllegalArgumentException("pathSegments == null");
        }
        if (pathSegmentsOffset < 0) {
            throw new IllegalArgumentException("pathSegmentsOffset < 0");
        }
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (pathDataOffset < 0) {
            throw new IllegalArgumentException("pathDataOffset < 0");
        }

        // check if the path is compatible with float coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_F)) {
            throw new IllegalArgumentException("path does not support float coordinates (pathData)");
        }

        AmanithVGJNI.vgAppendPathDataAA(VGHandle.getHandle(dstPath), numSegments, pathSegments, pathSegmentsOffset, pathData, pathDataOffset, ARRAY_TYPE_FLOAT);
    }

    public void vgAppendPathData(VGPath dstPath, int numSegments, final byte[] pathSegments, final float[] pathData) {

        vgAppendPathData(dstPath, numSegments, pathSegments, 0, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final byte[] pathData, int offset) {

        // check arguments
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        // check if the path is compatible with byte coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_S_8)) {
            throw new IllegalArgumentException("path does not support byte coordinates (pathData)");
        }

        AmanithVGJNI.vgModifyPathCoordsA(VGHandle.getHandle(dstPath), startIndex, numSegments, pathData, offset, ARRAY_TYPE_BYTE);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final byte[] pathData) {

        vgModifyPathCoords(dstPath, startIndex, numSegments, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final short[] pathData, int offset) {

        // check arguments
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        // check if the path is compatible with short coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_S_16)) {
            throw new IllegalArgumentException("path does not support short integer coordinates (pathData)");
        }

        AmanithVGJNI.vgModifyPathCoordsA(VGHandle.getHandle(dstPath), startIndex, numSegments, pathData, offset, ARRAY_TYPE_SHORT);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final short[] pathData) {

        vgModifyPathCoords(dstPath, startIndex, numSegments, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final int[] pathData, int offset) {

        // check arguments
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        // check if the path is compatible with integer coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_S_32)) {
            throw new IllegalArgumentException("path does not support integer coordinates (pathData)");
        }

        AmanithVGJNI.vgModifyPathCoordsA(VGHandle.getHandle(dstPath), startIndex, numSegments, pathData, offset, ARRAY_TYPE_INT);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final int[] pathData) {

        vgModifyPathCoords(dstPath, startIndex, numSegments, pathData, 0);
    }

    public void vgModifyPathCoords(VGPath dstPath, int startIndex, int numSegments, final float[] pathData, int offset) {

        // check arguments
        if (pathData == null) {
            throw new IllegalArgumentException("pathData == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        // check if the path is compatible with float coordinates
        int pathDataType = AmanithVGJNI.vgGetParameteri(VGHandle.getHandle(dstPath), VG101.VG_PATH_DATATYPE);
        if ((AmanithVGJNI.vgGetError() == VG101.VG_NO_ERROR) && (pathDataType != VG101.VG_PATH_DATATYPE_F)) {
            throw new IllegalArgumentException("path does not support float coordinates (pathData)");
        }

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
        if (values == null) {
            throw new IllegalArgumentException("values == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        if ((values.length - offset) < 4) {
            throw new IllegalArgumentException("not enough remaining entries (values)");
        }

        AmanithVGJNI.vgPointAlongPathA(VGHandle.getHandle(path), startSegment, numSegments, distance, values, offset);
    }

    public void vgPointAlongPath(VGPath path, int startSegment, int numSegments, float distance, float[] values) {

        vgPointAlongPath(path, startSegment, numSegments, distance, values, 0);
    }

    public void vgPathBounds(VGPath path, float[] values, int offset) {

        // check arguments
        if (values == null) {
            throw new IllegalArgumentException("values == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        if ((values.length - offset) < 4) {
            throw new IllegalArgumentException("not enough remaining entries (values)");
        }
            
        AmanithVGJNI.vgPathBoundsA(VGHandle.getHandle(path), values, offset);
    }

    public void vgPathBounds(VGPath path, float[] values) {

        vgPathBounds(path, values, 0);
    }

    public void vgPathTransformedBounds(VGPath path, float[] values, int offset) {

        // check arguments
        if (values == null) {
            throw new IllegalArgumentException("values == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        if ((values.length - offset) < 4) {
            throw new IllegalArgumentException("not enough remaining entries (values)");
        }
            
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
    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgCreateImage(VGImageFormat format, int width, int height, int allowedQuality)} instead.
     */
    public VGImage vgCreateImage(int format, int width, int height, int allowedQuality) {

        int handle = AmanithVGJNI.vgCreateImage(format, width, height, allowedQuality);
        return (handle == 0) ? null : new VGImage(handle);
    }

    public VGImage vgCreateImage(@NonNull VGImageFormat format, int width, int height, int allowedQuality) {

        return vgCreateImage(format.getValue(), width, height, allowedQuality);
    }

    public void vgDestroyImage(VGImage image) {

        AmanithVGJNI.vgDestroyImage(VGHandle.getHandle(image));
    }

    public void vgClearImage(VGImage image, int x, int y, int width, int height) {

        AmanithVGJNI.vgClearImage(VGHandle.getHandle(image), x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgImageSubData(VGImage image, byte[] data, int offset, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgImageSubData(VGImage image, final byte[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgImageSubData(VGImage image, byte[] data, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgImageSubData(VGImage image, final byte[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgImageSubData(VGImage image, short[] data, int offset, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgImageSubData(VGImage image, final short[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgImageSubData(VGImage image, short[] data, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgImageSubData(VGImage image, final short[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgImageSubData(VGImage image, int[] data, int offset, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgImageSubData(VGImage image, final int[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgImageSubData(VGImage image, int[] data, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgImageSubData(VGImage image, final int[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final byte[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, offset, dataStride, dataFormat.getValue(), x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final byte[] data, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final short[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, offset, dataStride, dataFormat.getValue(), x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final short[] data, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final int[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, offset, dataStride, dataFormat.getValue(), x, y, width, height);
    }

    public void vgImageSubData(VGImage image, final int[] data, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGetImageSubData(VGImage image, byte[] data, int offset, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgGetImageSubData(VGImage image, byte[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgGetImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGetImageSubData(VGImage image, byte[] data, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgGetImageSubData(VGImage image, byte[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGetImageSubData(VGImage image, short[] data, int offset, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgGetImageSubData(VGImage image, short[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgGetImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGetImageSubData(VGImage image, short[] data, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgGetImageSubData(VGImage image, short[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGetImageSubData(VGImage image, int[] data, int offset, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgGetImageSubData(VGImage image, int[] data, int offset, int dataStride, int dataFormat, int x, int y, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgGetImageSubDataA(VGHandle.getHandle(image), data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGetImageSubData(VGImage image, int[] data, int dataStride, VGImageFormat dataFormat, int x, int y, int width, int height)} instead.
     */
    public void vgGetImageSubData(VGImage image, int[] data, int dataStride, int dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, byte[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, offset, dataStride, dataFormat.getValue(), x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, byte[] data, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, short[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, offset, dataStride, dataFormat.getValue(), x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, short[] data, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, 0, dataStride, dataFormat, x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, int[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

        vgGetImageSubData(image, data, offset, dataStride, dataFormat.getValue(), x, y, width, height);
    }

    public void vgGetImageSubData(VGImage image, int[] data, int dataStride, @NonNull VGImageFormat dataFormat, int x, int y, int width, int height) {

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

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgWritePixels(byte[] data, int offset, int dataStride, VGImageFormat dataFormat, int dx, int dy, int width, int height)} instead.
     */
    public void vgWritePixels(final byte[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        AmanithVGJNI.vgWritePixelsA(data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, dx, dy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgWritePixels(byte[] data, int dataStride, VGImageFormat dataFormat, int dx, int dy, int width, int height)} instead.
     */
    public void vgWritePixels(final byte[] data, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgWritePixels(short[] data, int offset, int dataStride, VGImageFormat dataFormat, int dx, int dy, int width, int height)} instead.
     */
    public void vgWritePixels(final short[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgWritePixelsA(data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, dx, dy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgWritePixels(short[] data, int dataStride, VGImageFormat dataFormat, int dx, int dy, int width, int height)} instead.
     */
    public void vgWritePixels(final short[] data, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgWritePixels(int[] data, int offset, int dataStride, VGImageFormat dataFormat, int dx, int dy, int width, int height)} instead.
     */
    public void vgWritePixels(final int[] data, int offset, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
            
        AmanithVGJNI.vgWritePixelsA(data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, dx, dy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgWritePixels(int[] data, int dataStride, VGImageFormat dataFormat, int dx, int dy, int width, int height)} instead.
     */
    public void vgWritePixels(final int[] data, int dataStride, int dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final byte[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, offset, dataStride, dataFormat.getValue(), dx, dy, width, height);
    }

    public void vgWritePixels(final byte[] data, int dataStride, @NonNull VGImageFormat dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final short[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, offset, dataStride, dataFormat.getValue(), dx, dy, width, height);
    }

    public void vgWritePixels(final short[] data, int dataStride, @NonNull VGImageFormat dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgWritePixels(final int[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, offset, dataStride, dataFormat.getValue(), dx, dy, width, height);
    }

    public void vgWritePixels(final int[] data, int dataStride, @NonNull VGImageFormat dataFormat, int dx, int dy, int width, int height) {

        vgWritePixels(data, 0, dataStride, dataFormat, dx, dy, width, height);
    }

    public void vgGetPixels(VGImage dst, int dx, int dy, int sx, int sy, int width, int height) {

        AmanithVGJNI.vgGetPixels(VGHandle.getHandle(dst), dx, dy, sx, sy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgReadPixels(byte[] data, int offset, int dataStride, VGImageFormat dataFormat, int sx, int sy, int width, int height)} instead.
     */
    public void vgReadPixels(byte[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        AmanithVGJNI.vgReadPixelsA(data, offset, ARRAY_TYPE_BYTE, dataStride, dataFormat, sx, sy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgReadPixels(byte[] data, int dataStride, VGImageFormat dataFormat, int sx, int sy, int width, int height) } instead.
     */
    public void vgReadPixels(byte[] data, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgReadPixels(short[] data, int offset, int dataStride, VGImageFormat dataFormat, int sx, int sy, int width, int height)} instead.
     */
    public void vgReadPixels(short[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        AmanithVGJNI.vgReadPixelsA(data, offset, ARRAY_TYPE_SHORT, dataStride, dataFormat, sx, sy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgReadPixels(short[] data, int dataStride, VGImageFormat dataFormat, int sx, int sy, int width, int height)} instead.
     */
    public void vgReadPixels(short[] data, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgReadPixels(int[] data, int offset, int dataStride, VGImageFormat dataFormat, int sx, int sy, int width, int height)} instead.
     */
    public void vgReadPixels(int[] data, int offset, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        // check arguments
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }

        AmanithVGJNI.vgReadPixelsA(data, offset, ARRAY_TYPE_INT, dataStride, dataFormat, sx, sy, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the image format, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgReadPixels(int[] data, int dataStride, VGImageFormat dataFormat, int sx, int sy, int width, int height)} instead.
     */
    public void vgReadPixels(int[] data, int dataStride, int dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(byte[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, offset, dataStride, dataFormat.getValue(), sx, sy, width, height);
    }

    public void vgReadPixels(byte[] data, int dataStride, @NonNull VGImageFormat dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(short[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, offset, dataStride, dataFormat.getValue(), sx, sy, width, height);
    }

    public void vgReadPixels(short[] data, int dataStride, @NonNull VGImageFormat dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, 0, dataStride, dataFormat, sx, sy, width, height);
    }

    public void vgReadPixels(int[] data, int offset, int dataStride, @NonNull VGImageFormat dataFormat, int sx, int sy, int width, int height) {

        vgReadPixels(data, offset, dataStride, dataFormat.getValue(), sx, sy, width, height);
    }

    public void vgReadPixels(int[] data, int dataStride, @NonNull VGImageFormat dataFormat, int sx, int sy, int width, int height) {

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
        if (glyphOrigin == null) {
            throw new IllegalArgumentException("glyphOrigin == null");
        }
        if (glyphOriginOffset < 0) {
            throw new IllegalArgumentException("glyphOriginOffset < 0");
        }
        if ((glyphOrigin.length - glyphOriginOffset) < 2) {
            throw new IllegalArgumentException("not enough remaining entries (glyphOrigin)");
        }
        if (escapement == null) {
            throw new IllegalArgumentException("escapement == null");
        }
        if (escapementOffset < 0) {
            throw new IllegalArgumentException("escapementOffset < 0");
        }
        if ((escapement.length - escapementOffset) < 2) {
            throw new IllegalArgumentException("not enough remaining entries (escapement)");
        }

        AmanithVGJNI.vgSetGlyphToPathA(VGHandle.getHandle(font), glyphIndex, VGHandle.getHandle(path), isHinted, glyphOrigin, glyphOriginOffset, escapement, escapementOffset);
    }

    public void vgSetGlyphToPath(VGFont font, int glyphIndex, VGPath path, boolean isHinted, final float[] glyphOrigin, final float[] escapement) {

        vgSetGlyphToPath(font, glyphIndex, path, isHinted, glyphOrigin, 0, escapement, 0);
    }

    public void vgSetGlyphToImage(VGFont font, int glyphIndex, VGImage image, final float[] glyphOrigin, int glyphOriginOffset, final float[] escapement, int escapementOffset) {

        // check arguments
        if (glyphOrigin == null) {
            throw new IllegalArgumentException("glyphOrigin == null");
        }
        if (glyphOriginOffset < 0) {
            throw new IllegalArgumentException("glyphOriginOffset < 0");
        }
        if ((glyphOrigin.length - glyphOriginOffset) < 2) {
            throw new IllegalArgumentException("not enough remaining entries (glyphOrigin)");
        }
        if (escapement == null) {
            throw new IllegalArgumentException("escapement == null");
        }
        if (escapementOffset < 0) {
            throw new IllegalArgumentException("escapementOffset < 0");
        }
        if ((escapement.length - escapementOffset) < 2) {
            throw new IllegalArgumentException("not enough remaining entries (escapement)");
        }

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
        if (glyphIndices == null) {
            throw new IllegalArgumentException("glyphIndices == null");
        }
        if (glyphIndicesOffset < 0) {
            throw new IllegalArgumentException("glyphIndicesOffset < 0");
        }
        if ((glyphIndices.length - glyphIndicesOffset) < glyphCount) {
            throw new IllegalArgumentException("not enough remaining entries (glyphIndices)");
        }
        // check horizontal adjustments
        if (adjustments_x != null) {
            if (adjustments_x_offset < 0) {
                throw new IllegalArgumentException("adjustments_x_offset < 0");
            }
            if ((adjustments_x.length - adjustments_x_offset) < glyphCount) {
                throw new IllegalArgumentException("not enough remaining entries (adjustments_x)");
            }
        }
        // check vertical adjustments
        if (adjustments_y != null) {
            if (adjustments_y_offset < 0) {
                throw new IllegalArgumentException("adjustments_y_offset < 0");
            }
            if ((adjustments_y.length - adjustments_y_offset) < glyphCount) {
                throw new IllegalArgumentException("not enough remaining entries (adjustments_y)");
            }
        }

        AmanithVGJNI.vgDrawGlyphsA(VGHandle.getHandle(font), glyphCount, glyphIndices, glyphIndicesOffset, adjustments_x, adjustments_x_offset, adjustments_y, adjustments_y_offset, paintModes, allowAutoHinting);
    }

    public void vgDrawGlyphs(VGFont font, int glyphCount, final int[] glyphIndices, final float[] adjustments_x, final float[] adjustments_y, int paintModes, boolean allowAutoHinting) {

        vgDrawGlyphs(font, glyphCount, glyphIndices, 0, adjustments_x, 0, adjustments_y, 0, paintModes, allowAutoHinting);
    }

    public void vgDrawGlyphs(VGFont font, final int[] glyphIndices, final float[] adjustments_x, final float[] adjustments_y, int paintModes, boolean allowAutoHinting) {

        // this check is necessary because we are going to access glyphIndices.length field
        if (glyphIndices == null) {
            throw new IllegalArgumentException("glyphIndices == null");
        }

        vgDrawGlyphs(font, glyphIndices.length, glyphIndices, 0, adjustments_x, 0, adjustments_y, 0, paintModes, allowAutoHinting);
    }

    /*-------------------------------------------------------------------------------
                                      Image Filters
    -------------------------------------------------------------------------------*/
    public void vgColorMatrix(VGImage dst, VGImage src, final float[] matrix, int offset) {

        // check arguments
        checkArrayLength(matrix, offset, 20, "matrix");
        AmanithVGJNI.vgColorMatrixA(VGHandle.getHandle(dst), VGHandle.getHandle(src), matrix, offset);
    }

    public void vgColorMatrix(VGImage dst, VGImage src, final float[] matrix) {

        vgColorMatrix(dst, src, matrix, 0);
    }

    /**
     * This method uses a direct integer value for specifying the tiling mode, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, short[] kernel, int offset, float scale, float bias, VGTilingMode tilingMode)} instead.
     */
    public void vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, int offset, float scale, float bias, int tilingMode) {

        // check arguments
        if (kernel == null) {
            throw new IllegalArgumentException("kernel == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        if ((kernel.length - offset) < (kernelWidth * kernelHeight)) {
            throw new IllegalArgumentException("not enough remaining entries (kernel)");
        }
        
        AmanithVGJNI.vgConvolveA(VGHandle.getHandle(dst), VGHandle.getHandle(src), kernelWidth, kernelHeight, shiftX, shiftY, kernel, offset, scale, bias, tilingMode);
    }

    /**
     * This method uses a direct integer value for specifying the tiling mode, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, short[] kernel, float scale, float bias, VGTilingMode tilingMode)} instead.
     */
    public void vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, float scale, float bias, int tilingMode) {

        vgConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernel, 0, scale, bias, tilingMode);
    }

    public void vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, int offset, float scale, float bias, @NonNull VGTilingMode tilingMode) {

        vgConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernel, offset, scale, bias, tilingMode.getValue());
    }

    public void vgConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernel, float scale, float bias, @NonNull VGTilingMode tilingMode) {

        vgConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernel, 0, scale, bias, tilingMode);
    }

    /**
     * This method uses a direct integer value for specifying the tiling mode, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, short[] kernelX, int kernelXOffset, short[] kernelY, int kernelYOffset, float scale, float bias, VGTilingMode tilingMode)} instead.
     */
    public void vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, int kernelXOffset, final short[] kernelY, int kernelYOffset, float scale, float bias, int tilingMode) {

        // check arguments
        if (kernelX == null) {
            throw new IllegalArgumentException("kernelX == null");
        }
        if (kernelXOffset < 0) {
            throw new IllegalArgumentException("kernelXOffset < 0");
        }
        if ((kernelX.length - kernelXOffset) < kernelWidth) {
            throw new IllegalArgumentException("not enough remaining entries (kernelX)");
        }
        if (kernelY == null) {
            throw new IllegalArgumentException("kernelY == null");
        }
        if (kernelYOffset < 0) {
            throw new IllegalArgumentException("kernelYOffset < 0");
        }
        if ((kernelY.length - kernelYOffset) < kernelHeight) {
            throw new IllegalArgumentException("not enough remaining entries (kernelY)");
        }

        AmanithVGJNI.vgSeparableConvolveA(VGHandle.getHandle(dst), VGHandle.getHandle(src), kernelWidth, kernelHeight, shiftX, shiftY, kernelX, kernelXOffset, kernelY, kernelYOffset, scale, bias, tilingMode);
    }

    /**
     * This method uses a direct integer value for specifying the tiling mode, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, short[] kernelX, short[] kernelY, float scale, float bias, VGTilingMode tilingMode)} instead.
     */
    public void vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, final short[] kernelY, float scale, float bias, int tilingMode) {

        vgSeparableConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernelX, 0, kernelY, 0, scale, bias, tilingMode);
    }

    public void vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, int kernelXOffset, final short[] kernelY, int kernelYOffset, float scale, float bias, @NonNull VGTilingMode tilingMode) {

        vgSeparableConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernelX, kernelXOffset, kernelY, kernelYOffset, scale, bias, tilingMode.getValue());
    }

    public void vgSeparableConvolve(VGImage dst, VGImage src, int kernelWidth, int kernelHeight, int shiftX, int shiftY, final short[] kernelX, final short[] kernelY, float scale, float bias, @NonNull VGTilingMode tilingMode) {

        vgSeparableConvolve(dst, src, kernelWidth, kernelHeight, shiftX, shiftY, kernelX, 0, kernelY, 0, scale, bias, tilingMode);
    }

    /**
     * This method uses a direct integer value for specifying the tiling mode, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGaussianBlur(VGImage dst, VGImage src, float stdDeviationX, float stdDeviationY, VGTilingMode tilingMode)} instead.
     */
    public void vgGaussianBlur(VGImage dst, VGImage src, float stdDeviationX, float stdDeviationY, int tilingMode) {

        AmanithVGJNI.vgGaussianBlur(VGHandle.getHandle(dst), VGHandle.getHandle(src), stdDeviationX, stdDeviationY, tilingMode);
    }

    public void vgGaussianBlur(VGImage dst, VGImage src, float stdDeviationX, float stdDeviationY, @NonNull VGTilingMode tilingMode) {

        vgGaussianBlur(dst, src, stdDeviationX, stdDeviationY, tilingMode.getValue());
    }

    public void vgLookup(VGImage dst, VGImage src, final byte[] redLUT, int redLUTOffset, final byte[] greenLUT, int greenLUTOffset, final byte[] blueLUT, int blueLUTOffset, final byte[] alphaLUT, int alphaLUTOffset, boolean outputLinear, boolean outputPremultiplied) {

        // check arguments
        if (redLUT == null) {
            throw new IllegalArgumentException("redLUT == null");
        }
        if (redLUTOffset < 0) {
            throw new IllegalArgumentException("redLUTOffset < 0");
        }
        if ((redLUT.length - redLUTOffset) < 256) {
            throw new IllegalArgumentException("not enough remaining entries (redLUT)");
        }
        if (greenLUT == null) {
            throw new IllegalArgumentException("greenLUT == null");
        }
        if (greenLUTOffset < 0) {
            throw new IllegalArgumentException("greenLUTOffset < 0");
        }
        if ((greenLUT.length - greenLUTOffset) < 256) {
            throw new IllegalArgumentException("not enough remaining entries (greenLUT)");
        }
        if (blueLUT == null) {
            throw new IllegalArgumentException("blueLUT == null");
        }
        if (blueLUTOffset < 0) {
            throw new IllegalArgumentException("blueLUTOffset < 0");
        }
        if ((blueLUT.length - blueLUTOffset) < 256) {
            throw new IllegalArgumentException("not enough remaining entries (blueLUT)");
        }
        if (alphaLUT == null) {
            throw new IllegalArgumentException("alphaLUT == null");
        }
        if (alphaLUTOffset < 0) {
            throw new IllegalArgumentException("alphaLUTOffset < 0");
        }
        if ((alphaLUT.length - alphaLUTOffset) < 256) {
            throw new IllegalArgumentException("not enough remaining entries (alphaLUT)");
        }

        AmanithVGJNI.vgLookupA(VGHandle.getHandle(dst), VGHandle.getHandle(src), redLUT, redLUTOffset, greenLUT, greenLUTOffset, blueLUT, blueLUTOffset, alphaLUT, alphaLUTOffset, outputLinear, outputPremultiplied);
    }

    public void vgLookup(VGImage dst, VGImage src, final byte[] redLUT, final byte[] greenLUT, final byte[] blueLUT, final byte[] alphaLUT, boolean outputLinear, boolean outputPremultiplied) {

        vgLookup(dst, src, redLUT, 0, greenLUT, 0, blueLUT, 0, alphaLUT, 0, outputLinear, outputPremultiplied);
    }

    /**
     * This method uses a direct integer value for specifying the color source channel, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgLookupSingle(VGImage dst, VGImage src, int[] lookupTable, int offset, VGImageChannel sourceChannel, boolean outputLinear, boolean outputPremultiplied)} instead.
     */
    public void vgLookupSingle(VGImage dst, VGImage src, final int[] lookupTable, int offset, int sourceChannel, boolean outputLinear, boolean outputPremultiplied) {

        // check arguments
        if (lookupTable == null) {
            throw new IllegalArgumentException("lookupTable == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        if ((lookupTable.length - offset) < 256) {
            throw new IllegalArgumentException("not enough remaining entries (lookupTable)");
        }

        AmanithVGJNI.vgLookupSingleA(VGHandle.getHandle(dst), VGHandle.getHandle(src), lookupTable, offset, sourceChannel, outputLinear, outputPremultiplied);
    }

    /**
     * This method uses a direct integer value for specifying the color source channel, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgLookupSingle(VGImage dst, VGImage src, int[] lookupTable, VGImageChannel sourceChannel, boolean outputLinear, boolean outputPremultiplied)} instead.
     */
    public void vgLookupSingle(VGImage dst, VGImage src, final int[] lookupTable, int sourceChannel, boolean outputLinear, boolean outputPremultiplied) {

        vgLookupSingle(dst, src, lookupTable, 0, sourceChannel, outputLinear, outputPremultiplied);
    }

    public void vgLookupSingle(VGImage dst, VGImage src, final int[] lookupTable, int offset, @NonNull VGImageChannel sourceChannel, boolean outputLinear, boolean outputPremultiplied) {

        vgLookupSingle(dst, src, lookupTable, offset, sourceChannel.getValue(), outputLinear, outputPremultiplied);
    }

    public void vgLookupSingle(VGImage dst, VGImage src, final int[] lookupTable, @NonNull VGImageChannel sourceChannel, boolean outputLinear, boolean outputPremultiplied) {

        vgLookupSingle(dst, src, lookupTable, 0, sourceChannel, outputLinear, outputPremultiplied);
    }

    /*-------------------------------------------------------------------------------
                                    Hardware Queries
    -------------------------------------------------------------------------------*/
    /**
     * This method uses a direct integer value for specifying the query type, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgHardwareQuery(VGHardwareQueryType key, int setting)} instead.
     */
    public int vgHardwareQuery(int key, int setting) {

        return AmanithVGJNI.vgHardwareQuery(key, setting);
    }

    public VGHardwareQueryResult vgHardwareQuery(VGHardwareQueryType key, int setting) {

        return VGHardwareQueryResult.fromValue(vgHardwareQuery(key.getValue(), setting));
    }

    public java.lang.String vgGetString(int name) {

        java.lang.String s = AmanithVGJNI.vgGetString(name);
        return s;
    }

    /*-------------------------------------------------------------------------------
                                          VGU11
    -------------------------------------------------------------------------------*/
    public enum VGUArcType {
        Open(VGU_ARC_OPEN),
        Chord(VGU_ARC_CHORD),
        Pie(VGU_ARC_PIE);
        VGUArcType(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public int vguLine(VGPath path, float x0, float y0, float x1, float y1) {

        return AmanithVGJNI.vguLine(VGHandle.getHandle(path), x0, y0, x1, y1);
    }

    public int vguPolygon(VGPath path, final float[] points, int offset, int count, boolean closed) {

        // check arguments
        if (points == null) {
            throw new IllegalArgumentException("points == null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0");
        }
        if ((points.length - offset) < (count * 2)) {
            throw new IllegalArgumentException("not enough remaining entries (points)");
        }
        
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

    /**
     * This method uses a direct integer value for specifying the arc type, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vguArc(VGPath path, float x, float y, float width, float height, float startAngle, float angleExtent, VGUArcType arcType)} instead.
     */
    public int vguArc(VGPath path, float x, float y, float width, float height, float startAngle, float angleExtent, int arcType) {

        return AmanithVGJNI.vguArc(VGHandle.getHandle(path), x, y, width, height, startAngle, angleExtent, arcType);
    }

    public int vguArc(VGPath path, float x, float y, float width, float height, float startAngle, float angleExtent, @NonNull VGUArcType arcType) {

        return vguArc(path, x, y, width, height, startAngle, angleExtent, arcType.getValue());
    }

    public int vguComputeWarpQuadToSquare(float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset) {

        // check arguments
        checkArrayLength(matrix, offset, 9, "matrix");
        return AmanithVGJNI.vguComputeWarpQuadToSquareA(sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, offset);
    }

    public int vguComputeWarpQuadToSquare(float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix) {

        return vguComputeWarpQuadToSquare(sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, 0);
    }

    public int vguComputeWarpSquareToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float[] matrix, int offset) {

        // check arguments
        checkArrayLength(matrix, offset, 9, "matrix");
        return AmanithVGJNI.vguComputeWarpSquareToQuadA(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, matrix, offset);
    }

    public int vguComputeWarpSquareToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float[] matrix) {

        return vguComputeWarpSquareToQuad(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, matrix, 0);
    }

    public int vguComputeWarpQuadToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix, int offset) {

        // check arguments
        checkArrayLength(matrix, offset, 9, "matrix");
        return AmanithVGJNI.vguComputeWarpQuadToQuadA(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, offset);
    }

    public int vguComputeWarpQuadToQuad(float dx0, float dy0, float dx1, float dy1, float dx2, float dy2, float dx3, float dy3, float sx0, float sy0, float sx1, float sy1, float sx2, float sy2, float sx3, float sy3, float[] matrix) {

        return vguComputeWarpQuadToQuad(dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3, sx0, sy0, sx1, sy1, sx2, sy2, sx3, sy3, matrix, 0);
    }

    /*-------------------------------------------------------------------------------
                                        VG11Ext
    -------------------------------------------------------------------------------*/
    public enum VGConfigMzt {

        // read-only
        MaxCurrentThreads(VG_CONFIG_MAX_CURRENT_THREADS_MZT),
        MaxSurfaceDimension(VG_CONFIG_MAX_SURFACE_DIMENSION_MZT),
        // geometry
        CurvesQuality(VG_CONFIG_CURVES_QUALITY_MZT),
        RadialGrandientsQuality(VG_CONFIG_RADIAL_GRADIENTS_QUALITY_MZT),
        ConicalGrandientsQuality(VG_CONFIG_CONICAL_GRADIENTS_QUALITY_MZT),
        // memory
        CallsBeforeMemoryRecovery(VG_CONFIG_CALLS_BEFORE_MEMORY_RECOVERY_MZT),
        // rasterizer cache - AmanithVG SRE only
        MaxCachingBoxWidth(VG_CONFIG_MAX_CACHING_BOX_WIDTH_MZT),
        MaxCachingBoxHeight(VG_CONFIG_MAX_CACHING_BOX_HEIGHT_MZT),
        // OpenGL / OpenGL ES - AmanithVG GLE only
        ForceRectTexturesDisabled(VG_CONFIG_FORCE_RECT_TEXTURES_DISABLED_MZT),
        ForceMirroredRepeatDisabled(VG_CONFIG_FORCE_MIRRORED_REPEAT_DISABLED_MZT),
        ForceClampToBorderDisabled(VG_CONFIG_FORCE_CLAMP_TO_BORDER_DISABLED_MZT),
        ForceBlendMixMaxDisabled(VG_CONFIG_FORCE_BLEND_MIN_MAX_DISABLED_MZT),
        ForceDot3Disabled(VG_CONFIG_FORCE_DOT3_DISABLED_MZT),
        ForceVboDisabled(VG_CONFIG_FORCE_VBO_DISABLED_MZT),
        MaxPermittedTextureUnits(VG_CONFIG_MAX_PERMITTED_TEXTURE_UNITS_MZT),
        MaxTextureSize(VG_CONFIG_MAX_TEXTURE_SIZE_MZT),
        ForceBuffersDisabled(VG_CONFIG_FORCE_BUFFERS_DISABLED_MZT),
        SupposePersistentBuffers(VG_CONFIG_SUPPOSE_PERSISTENT_BUFFERS_MZT),
        ForceScissorDisabled(VG_CONFIG_FORCE_SCISSOR_DISABLED_MZT),
        ForceColorMaskingDisabled(VG_CONFIG_FORCE_COLOR_MASKING_DISABLED_MZT),
        ForceMipmapsOnGradients(VG_CONFIG_FORCE_MIPMAPS_ON_GRADIENTS_MZT),
        ForceDitheringOnGradients(VG_CONFIG_FORCE_DITHERING_ON_GRADIENTS_MZT),
        ForceDitheringOnImages(VG_CONFIG_FORCE_DITHERING_ON_IMAGES_MZT),
        ForceRgbaTextures(VG_CONFIG_FORCE_RGBA_TEXTURES_MZT),
        ForceImageTextureBorders(VG_CONFIG_FORCE_IMAGE_TEXTURE_BORDERS_MZT),
        ImageTextureBordersMode(VG_CONFIG_IMAGE_TEXTURE_BORDERS_MODE_MZT),
        // standard deviation factor for Gaussian blur filter
        FilterGaussianSigmaFactor(VG_CONFIG_FILTER_GAUSSIAN_SIGMA_FACTOR_MZT);

        VGConfigMzt(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGLightTypeMzt {
        Distant(VG_LIGHT_TYPE_DISTANT_MZT),
        Point(VG_LIGHT_TYPE_POINT_MZT),
        Spot(VG_LIGHT_TYPE_SPOT_MZT);
        VGLightTypeMzt(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }

    public enum VGCompositeOpMzt {
        Over(VG_COMPOSITE_OVER_MZT),
        Multiply(VG_COMPOSITE_MULTIPLY_MZT),
        Screen(VG_COMPOSITE_SCREEN_MZT),
        Darken(VG_COMPOSITE_DARKEN_MZT),
        Lighten(VG_COMPOSITE_LIGHTEN_MZT),
        In(VG_COMPOSITE_IN_MZT),
        Out(VG_COMPOSITE_OUT_MZT),
        Atop(VG_COMPOSITE_ATOP_MZT),
        Xor(VG_COMPOSITE_XOR_MZT),
        Arithmetic(VG_COMPOSITE_ARITHMETIC_MZT);
        VGCompositeOpMzt(int vgEnum) {
            _vgEnum = vgEnum;
        }
        public int getValue() {
            return _vgEnum;
        }
        private final int _vgEnum;
    }
    
    public void vgIterativeAverageBlurKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, int tilingMode) { }
    public void vgParametricFilterKHR(VGImage dst, VGImage src, VGImage blur, float strength, float offsetX, float offsetY, int filterFlags, VGPaint highlightPaint, VGPaint shadowPaint) { }
    public int vguDropShadowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int shadowColorRGBA) { return VGU_NO_ERROR; }
    public int vguGlowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, int filterFlags, int allowedQuality, int glowColorRGBA) { return VGU_NO_ERROR; }
    public int vguBevelKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int highlightColorRGBA, int shadowColorRGBA) { return VGU_NO_ERROR; }
    public int vguGradientGlowKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final float[] glowColorRampStops, int offset) { return VGU_NO_ERROR; }
    public int vguGradientBevelKHR(VGImage dst, VGImage src, float dimX, float dimY, int iterative, float strength, float distance, float angle, int filterFlags, int allowedQuality, int stopsCount, final float[] bevelColorRampStops, int offset) { return VGU_NO_ERROR; }
    public void vgProjectiveMatrixNDS(boolean enable) { }
    public int vguTransformClipLineNDS(final float Ain, final float Bin, final float Cin, final float[] matrix, int matrixOffset, final boolean inverse, float[] Aout, int AoutOffset, float[] Bout, int BoutOffset, float[] Cout, int CoutOffset) { return VGU_NO_ERROR; }

    // VG_MZT_clip_path
    public void vgClipPathPushMZT(VGPath path, boolean advanceLayer) {

        AmanithVGJNI.vgClipPathPushMZT(VGHandle.getHandle(path), advanceLayer);
    }

    public void vgClipPathPopMZT() {

        AmanithVGJNI.vgClipPathPopMZT();
    }

    public void vgClipPathClearMZT() {

        AmanithVGJNI.vgClipPathClearMZT();
    }

    // VG_MZT_filters
    public void vgColorMatrixMZT(VGImage dst, VGImage src, final float[] matrix, int offset) {

        // check arguments
        checkArrayLength(matrix, offset, 20, "matrix");
        AmanithVGJNI.vgColorMatrixMZT(VGHandle.getHandle(dst), VGHandle.getHandle(src), matrix, offset);
    }

    public void vgColorMatrixMZT(VGImage dst, VGImage src, final float[] matrix) {

        vgColorMatrixMZT(dst, src, matrix, 0);
    }

    /**
     * This method uses a direct integer value for specifying the tiling mode, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGaussianBlurMZT(VGImage dst, VGImage src, float[] axes, int offset, float stdDeviationX, float stdDeviationY, VGTilingMode tilingMode, boolean useFastApprox)} instead.
     */
    public void vgGaussianBlurMZT(VGImage dst, VGImage src, final float[] axes, int offset, float stdDeviationX, float stdDeviationY, int tilingMode, boolean useFastApprox) {

        // check axes, if needed
        if (axes != null) {
            checkArrayLength(axes, offset, 4, "axes");
        }

        // check standard deviations
        if (stdDeviationX < 0) {
            throw new IllegalArgumentException("stdDeviationX < 0");
        }
        // check specular constant
        if (stdDeviationY < 0) {
            throw new IllegalArgumentException("stdDeviationY < 0");
        }

        AmanithVGJNI.vgGaussianBlurMZT(VGHandle.getHandle(dst), VGHandle.getHandle(src), axes, offset, stdDeviationX, stdDeviationY, tilingMode, useFastApprox);
    }

    /**
     * This method uses a direct integer value for specifying the tiling mode, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgGaussianBlurMZT(VGImage dst, VGImage src, float[] axes, float stdDeviationX, float stdDeviationY, VGTilingMode tilingMode, boolean useFastApprox)} instead.
     */
    public void vgGaussianBlurMZT(VGImage dst, VGImage src, final float[] axes, float stdDeviationX, float stdDeviationY, int tilingMode, boolean useFastApprox) {

        vgGaussianBlurMZT(dst, src, axes, 0, stdDeviationX, stdDeviationY, tilingMode, useFastApprox);
    }

    public void vgGaussianBlurMZT(VGImage dst, VGImage src, final float[] axes, int offset, float stdDeviationX, float stdDeviationY, @NonNull VGTilingMode tilingMode, boolean useFastApprox) {

        vgGaussianBlurMZT(dst, src, axes, offset, stdDeviationX, stdDeviationY, tilingMode.getValue(), useFastApprox);
    }

    public void vgGaussianBlurMZT(VGImage dst, VGImage src, final float[] axes, float stdDeviationX, float stdDeviationY, @NonNull VGTilingMode tilingMode, boolean useFastApprox) {

        vgGaussianBlurMZT(dst, src, axes, 0, stdDeviationX, stdDeviationY, tilingMode, useFastApprox);
    }

    /**
     * This method uses a direct integer value for specifying the light type, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgLightingMZT(VGImage dstDiffuse, VGImage dstSpecular, VGImage src, float surfaceScale, float diffuseConstant, float specularConstant, float specularExponent, VGLightTypeMzt lightType, float[] lightData, int ldOffset)} instead.
     */
    public void vgLightingMZT(VGImage dstDiffuse, VGImage dstSpecular, VGImage src, float surfaceScale, float diffuseConstant, float specularConstant, float specularExponent, int lightType, final float[] lightData, int ldOffset) {

        // check arguments
        switch (lightType) {
            case VG_LIGHT_TYPE_DISTANT_MZT:
                checkArrayLength(lightData, ldOffset, 5, "lightData");
                break;
            case VG_LIGHT_TYPE_POINT_MZT:
                checkArrayLength(lightData, ldOffset, 6, "lightData");
                break;
            case VG_LIGHT_TYPE_SPOT_MZT:
                checkArrayLength(lightData, ldOffset, 12, "lightData");
                break;
            default:
                // nothing to do
                break;
        }

        // check diffuse constant
        if (diffuseConstant < 0) {
            throw new IllegalArgumentException("diffuseConstant < 0");
        }
        // check specular constant
        if (specularConstant < 0) {
            throw new IllegalArgumentException("specularConstant < 0");
        }

        AmanithVGJNI.vgLightingMZT(VGHandle.getHandle(dstDiffuse), VGHandle.getHandle(dstSpecular), VGHandle.getHandle(src), surfaceScale, diffuseConstant, specularConstant, specularExponent, lightType, lightData, ldOffset);
    }

    public void vgLightingMZT(VGImage dstDiffuse, VGImage dstSpecular, VGImage src, float surfaceScale, float diffuseConstant, float specularConstant, float specularExponent, @NonNull VGLightTypeMzt lightType, final float[] lightData, int ldOffset) {

        vgLightingMZT(dstDiffuse, dstSpecular, src, surfaceScale, diffuseConstant, specularConstant, specularExponent, lightType.getValue(), lightData, ldOffset);
    }

    public void vgLightingMZT(VGImage dstDiffuse, VGImage dstSpecular, VGImage src, float surfaceScale, float diffuseConstant, float specularConstant, float specularExponent, @NonNull VGLightTypeMzt lightType, final float[] lightData) {

        // int dstDiffuse, int dstSpecular, int src, float surfaceScale, float diffuseConstant, float specularConstant, float specularExponent, int lightType, final float[] lightData, int ldOffset
        vgLightingMZT(dstDiffuse, dstSpecular, src, surfaceScale, diffuseConstant, specularConstant, specularExponent, lightType, lightData, 0);
    }

    public void vgMorphologyMZT(VGImage dst, VGImage src, boolean erode, final float[] axes, int offset, int radiusX, int radiusY) {

        // check axes, if needed
        if (axes != null) {
            checkArrayLength(axes, offset, 4, "axes");
        }
        // check radii
        if (radiusX <= 0) {
            throw new IllegalArgumentException("radiusX < 0");
        }
        if (radiusY <= 0) {
            throw new IllegalArgumentException("radiusY < 0");
        }

        AmanithVGJNI.vgMorphologyMZT(VGHandle.getHandle(dst), VGHandle.getHandle(src), erode, axes, offset, radiusX, radiusY);
    }

    public void vgMorphologyMZT(VGImage dst, VGImage src, boolean erode, final float[] axes, int radiusX, int radiusY) {

        vgMorphologyMZT(dst, src, erode, axes, 0, radiusX, radiusY);
    }

    public void vgTurbulenceMZT(VGImage image, float biasX, float biasY, float scaleX, float scaleY, float baseFrequencyX, float baseFrequencyY, int numOctaves, int seed, boolean stitchTiles, boolean fractalNoise) {

        if (baseFrequencyX < 0) {
            throw new IllegalArgumentException("baseFrequencyX < 0");
        }
        if (baseFrequencyY < 0) {
            throw new IllegalArgumentException("baseFrequencyY < 0");
        }
        if (numOctaves <= 0) {
            throw new IllegalArgumentException("numOctaves <= 0");
        }

        AmanithVGJNI.vgTurbulenceMZT(VGHandle.getHandle(image), biasX, biasY, scaleX, scaleY, baseFrequencyX, baseFrequencyY, numOctaves, seed, stitchTiles, fractalNoise);
    }

    /**
     * This method uses a direct integer value for the channel selectors, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgDisplacementMapMZT(VGImage dst, VGImage src, VGImage map, float scaleX, float scaleY, VGTilingMode tilingMode, VGImageChannel xChannelSelector, VGImageChannel yChannelSelector)} instead.
     */
    public void vgDisplacementMapMZT(VGImage dst, VGImage src, VGImage map, float scaleX, float scaleY, int tilingMode, int xChannelSelector, int yChannelSelector) {

        AmanithVGJNI.vgDisplacementMapMZT(VGHandle.getHandle(dst), VGHandle.getHandle(src), VGHandle.getHandle(map), scaleX, scaleY, tilingMode, xChannelSelector, yChannelSelector);
    }

    public void vgDisplacementMapMZT(VGImage dst, VGImage src, VGImage map, float scaleX, float scaleY, @NonNull VGTilingMode tilingMode, @NonNull VGImageChannel xChannelSelector, @NonNull VGImageChannel yChannelSelector) {

        vgDisplacementMapMZT(dst, src, map, scaleX, scaleY, tilingMode.getValue(), xChannelSelector.getValue(), yChannelSelector.getValue());
    }

    /**
     * This method uses a direct integer value for the composite operation, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgCompositeMZT(VGImage dst, VGImage in1, VGImage in2, VGCompositeOpMzt operation, float k1, float k2, float k3, float k4)} instead.
     */
    public void vgCompositeMZT(VGImage dst, VGImage in1, VGImage in2, int operation, float k1, float k2, float k3, float k4) {

        AmanithVGJNI.vgCompositeMZT(VGHandle.getHandle(dst), VGHandle.getHandle(in1), VGHandle.getHandle(in2), operation, k1, k2, k3, k4);
    }

    public void vgCompositeMZT(VGImage dst, VGImage in1, VGImage in2, VGCompositeOpMzt operation, float k1, float k2, float k3, float k4) {

        vgCompositeMZT(dst, in1, in2, operation.getValue(), k1, k2, k3, k4);
    }

    // VG_MZT_mask
    /**
     * This method uses a direct integer value for specifying the masking operation, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgMaskMZT(VGImage mask, VGMaskOperation operation, int x, int y, int width, int height)} instead.
     */
    public void vgMaskMZT(VGImage mask, int operation, int x, int y, int width, int height) {

        AmanithVGJNI.vgMaskMZT(VGHandle.getHandle(mask), operation, x, y, width, height);
    }

    /**
     * This method uses a direct integer value for specifying the masking operation, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgMaskMZT(VGMaskLayer mask, VGMaskOperation operation, int x, int y, int width, int height)} instead.
     */
    public void vgMaskMZT(VGMaskLayer mask, int operation, int x, int y, int width, int height) {

        AmanithVGJNI.vgMaskMZT(VGHandle.getHandle(mask), operation, x, y, width, height);
    }

    public void vgMaskMZT(VGImage mask, @NonNull VGMaskOperation operation, int x, int y, int width, int height) {

        vgMaskMZT(mask, operation.getValue(), x, y, width, height);
    }

    public void vgMaskMZT(VGMaskLayer mask, @NonNull VGMaskOperation operation, int x, int y, int width, int height) {

        vgMaskMZT(mask, operation.getValue(), x, y, width, height);
    }

    // Configuration parameters and thresholds
    /**
     * This method uses a direct integer value for specifying the configuration parameter, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgConfigSetMZT(VGConfigMzt config, float value)} instead.
     */
    public int vgConfigSetMZT(int config, float value) {

        return AmanithVGJNI.vgConfigSetMZT(config, value);
    }

    public int vgConfigSetMZT(VGConfigMzt config, float value) {

        return AmanithVGJNI.vgConfigSetMZT(config.getValue(), value);
    }

    /**
     * This method uses a direct integer value for specifying the configuration parameter, which can lead to unwanted "illegal argument error" <br>
     * Use {@link #vgConfigGetMZT(VGConfigMzt config)} instead.
     */
    public float vgConfigGetMZT(int config) {

        return AmanithVGJNI.vgConfigGetMZT(config);
    }

    public float vgConfigGetMZT(VGConfigMzt config) {

        return AmanithVGJNI.vgConfigGetMZT(config.getValue());
    }

    // EGL-like API
    public boolean vgInitializeMZT() {

        return AmanithVGJNI.vgInitializeMZT();
    }

    public void vgTerminateMZT() {

        AmanithVGJNI.vgTerminateMZT();
    }

    public long vgPrivContextCreateMZT(long sharedContext) {

        return AmanithVGJNI.vgPrivContextCreateMZT(sharedContext);
    }

    public void vgPrivContextDestroyMZT(long context) {

        AmanithVGJNI.vgPrivContextDestroyMZT(context);
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
        if ((dstPixels.length - offset) < (srfWidth * srfHeight)) {
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
