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

public interface VG11 extends VG101 {

    // Color Transformation
    public static final int VG_COLOR_TRANSFORM                          = 0x1170;
    public static final int VG_COLOR_TRANSFORM_VALUES                   = 0x1171;
    
    // Glyph origin
    public static final int VG_GLYPH_ORIGIN                             = 0x1122;
    
    // VGMatrixMode
    public static final int VG_MATRIX_GLYPH_USER_TO_SURFACE             = 0x1404;
    
    // VGImageFormat
    public static final int VG_A_1                                      = 13;
    public static final int VG_A_4                                      = 14;
    
    // VGFontParamType
    public static final int VG_FONT_NUM_GLYPHS                          = 0x2F00;

    /*-------------------------------------------------------------------------------
                                  Masking and Clearing
    -------------------------------------------------------------------------------*/
    public void vgMask(VGHandle mask, int operation, int x, int y, int width, int height);
    public void vgRenderToMask(VGPath path, int paintModes, int operation);
    public VGMaskLayer vgCreateMaskLayer(int width, int height);
    public void vgDestroyMaskLayer(VGMaskLayer maskLayer);
    public void vgFillMaskLayer(VGMaskLayer maskLayer, int x, int y, int width, int height, float value);
    public void vgCopyMask(VGMaskLayer maskLayer, int dx, int dy, int sx, int sy, int width, int height);
    
    /*-------------------------------------------------------------------------------
                                         Text
    -------------------------------------------------------------------------------*/
    public VGFont vgCreateFont(int glyphCapacityHint);
    public void vgDestroyFont(VGFont font);
    public void vgSetGlyphToPath(VGFont font, int glyphIndex, VGPath path, boolean isHinted, final float[] glyphOrigin, int glyphOriginOffset, final float[] escapement, int escapementOffset);
    public void vgSetGlyphToImage(VGFont font, int glyphIndex, VGImage image, final float[] glyphOrigin, int glyphOriginOffset, final float[] escapement, int escapementOffset);
    public void vgClearGlyph(VGFont font, int glyphIndex);
    public void vgDrawGlyph(VGFont font, int glyphIndex, int paintModes, boolean allowAutoHinting);
    public void vgDrawGlyphs(VGFont font, int glyphCount, final int[] glyphIndices, int glyphIndicesOffset, final float[] adjustments_x, int adjustments_x_offset, final float[] adjustments_y, int adjustments_y_offset, int paintModes, boolean allowAutoHinting);
}
