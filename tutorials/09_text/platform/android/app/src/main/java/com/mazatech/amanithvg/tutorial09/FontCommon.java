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
package com.mazatech.amanithvg.tutorial09;

import java.util.Arrays;
import java.util.Comparator;

import javax.microedition.khronos.openvg.VG11;
import javax.microedition.khronos.openvg.VGFont;
import javax.microedition.khronos.openvg.VGPath;

import static javax.microedition.khronos.openvg.VG101.*;
import static javax.microedition.khronos.openvg.VG11.*;

public class FontCommon {

    public static class MappedChar {
        int charCode;
        int glyphIndex;
        // constructor
        MappedChar(int charCode, int glyphIndex) {
            this.charCode = charCode;
            this.glyphIndex = glyphIndex;
        }
    }

    public static class Glyph {
        // glyph index within the OpenVG font object
        int glyphIndex;
        // the advance width for this glyph
        float[] escapement;
        // OpenVG path commands defining the glyph geometry
        //int commandsCount;
        final byte[] commands;
        // OpenVG path coordinates defining the glyph geometry
        //int coordinatesCount;
        final float[] coordinates;
        // fill rule of glyph geometry
        int fillRule;
        // constructor
        Glyph(int glyphIndex, final float[] escapement, final byte[] commands, final float[] coordinates, int fillRule) {
            this.glyphIndex = glyphIndex;
            this.escapement = escapement;
            this.commands = commands;
            this.coordinates = coordinates;
            this.fillRule = fillRule;
        }
    }

    public static class KerningEntry {
        // the key representing two glyph indices ((leftGlyphIndex << 16) + rightGlyphIndex)
        int key;
        // the kerning amount relative to the chars couple
        float x;
        float y;
        // constructor
        KerningEntry(int key, float x, float y) {
            this.key = key;
            this.x = x;
            this.y = y;
        }
    }

    public static class Font {
        // OpenVG font object
        VGFont openvgHandle;
        // "character code" to "glyph index" map, sorted by ascending character codes
        final MappedChar[] charCodesMap;
        // glyphs data
        final Glyph[] glyphs;
        // kerning table
        final KerningEntry[] kerningTable;
        // constructor
        Font(VGFont openvgHandle, final MappedChar[] charCodesMap, final Glyph[] glyphs, final KerningEntry[] kerningTable) {
            this.openvgHandle = openvgHandle;
            this.charCodesMap = charCodesMap;
            this.glyphs = glyphs;
            this.kerningTable = kerningTable;
        }
    }

    // compare two mapped characters
    private static Comparator<MappedChar> mappedCharsCompare = new Comparator<MappedChar>() {
        public int compare(MappedChar ch0, MappedChar ch1) {
            return ch0.charCode - ch1.charCode;
        }
    };

    // given a character code, return its glyph index
    static int glyphIndexFromCharCode(final Font font,
                                      final int charCode) {

        int glyphIndex = -1;

        if (font != null) {
            if (font.charCodesMap != null) {
                MappedChar ch = new MappedChar(charCode, 0);
                // "character code" to "glyph index" map is sorted by ascending character
                // codes, so we can perform a binary search, in order to speedup things
                int found = Arrays.binarySearch(font.charCodesMap, ch, mappedCharsCompare);
                if (found >= 0) {
                    glyphIndex = font.charCodesMap[found].glyphIndex;
                }
            }
            else {
                // in this case glyph indices coincide with character codes
                glyphIndex = charCode;
            }
        }

        return glyphIndex;
    }

    // compare two glyphs, by their glyph indices
    private static Comparator<Glyph> glyphsCompare = new Comparator<Glyph>() {
        public int compare(Glyph glyph0, Glyph glyph1) {
            return glyph0.glyphIndex - glyph1.glyphIndex;
        }
    };

    // given a glyph index, return the associated Glyph structure
    static Glyph glyphFromGlyphIndex(final Font font,
                                     final int glyphIndex) {

        Glyph glyph = new Glyph(glyphIndex, null, null, null, 0);
        // glyphs are sorted by ascending glyph index, so we can
        // perform a binary search, in order to speedup things
        int found = Arrays.binarySearch(font.glyphs, glyph, glyphsCompare);
        return (found >= 0) ? font.glyphs[found]: null;
    }

    // given a character code, return the associated Glyph structure
    static Glyph glyphFromCharCode(final Font font,
                                   final int charCode) {

        int glyphIndex = glyphIndexFromCharCode(font, charCode);
        return (glyphIndex >= 0) ? glyphFromGlyphIndex(font, glyphIndex) : null;
    }

    // compare two kerning entries
    private static Comparator<KerningEntry> kerningsCompare = new Comparator<KerningEntry>() {
        public int compare(KerningEntry krn0, KerningEntry krn1) {
            return krn0.key - krn1.key;
        }
    };

    // given a couple of glyph indices, return the relative kerning (NULL if kerning is zero)
    static KerningEntry kerningFromGlyphIndices(final Font font,
                                                final int leftGlyphIndex,
                                                final int rightGlyphIndex) {

        KerningEntry krn = new KerningEntry(
            // the key
            (leftGlyphIndex << 16) + rightGlyphIndex,
            0.0f, 0.0f
        );
        int found = Arrays.binarySearch(font.kerningTable, krn, kerningsCompare);
        return (found >= 0) ? font.kerningTable[found]: null;
    }

    // given a couple of character codes, return the relative kerning (NULL if kerning is zero)
    static KerningEntry kerningFromCharCodes(final Font font,
                                             final int leftCharCode,
                                             final int rightCharCode) {

        int leftGlyphIndex = glyphIndexFromCharCode(font, leftCharCode);
        int rightGlyphIndex = glyphIndexFromCharCode(font, rightCharCode);

        // if both glyph indices have been found, try searching for the kerning information
        return ((leftGlyphIndex >= 0) && (rightGlyphIndex >= 0)) ? kerningFromGlyphIndices(font, leftGlyphIndex, rightGlyphIndex) : null;
    }

    private static int[] tmpGlyphIndices = null;
    private static float[] tmpAdjustmentsX = null;
    private static float[] tmpAdjustmentsY = null;

    // reserve temporary memory to store glyph indices and kernings, for printing/drawing purposes
    private static boolean tempMemoryReserve(final int strLen) {

        if (tmpGlyphIndices == null) {
            // first allocation
            tmpGlyphIndices = new int[strLen];
            tmpAdjustmentsX = new float[strLen];
            tmpAdjustmentsY = new float[strLen];
        }
        else
        if (tmpGlyphIndices.length < strLen) {
            // a reallocation/expansion
            int[] glyphIndices = new int[strLen];
            float[] adjustmentsX = new float[strLen];
            float[] adjustmentsY = new float[strLen];
            tmpGlyphIndices = glyphIndices;
            tmpAdjustmentsX = adjustmentsX;
            tmpAdjustmentsY = adjustmentsY;
        }

        return ((tmpGlyphIndices.length >= strLen) && (tmpAdjustmentsX.length >= strLen) && (tmpAdjustmentsY.length >= strLen));
    }

    private static void textLineBuild(final Font font,
                                      final String str) {

        int i, j;
        int leftGlyphIndex = glyphIndexFromCharCode(font, str.charAt(0));

        // first glyph index
        tmpGlyphIndices[0] = leftGlyphIndex;
        j = str.length();
        for (i = 1; i < j; ++i) {
            int rightGlyphIndex = glyphIndexFromCharCode(font, str.charAt(i));
            KerningEntry krn = kerningFromGlyphIndices(font, leftGlyphIndex, rightGlyphIndex);
            // append glyph index
            tmpGlyphIndices[i] = rightGlyphIndex;
            // initialize adjustment
            tmpAdjustmentsX[i - 1] = 0.0f;
            tmpAdjustmentsY[i - 1] = 0.0f;
            // add kerning info
            if (krn != null) {
                tmpAdjustmentsX[i - 1] += krn.x;
                tmpAdjustmentsY[i - 1] += krn.y;
            }
            leftGlyphIndex = rightGlyphIndex;
        }
        // last adjustment entry
        tmpAdjustmentsX[j - 1] = 0.0f;
        tmpAdjustmentsY[j - 1] = 0.0f;
    }

    // get text line width, in object space
    static float textLineWidth(VG11 vg,
                               final Font font,
                               final String str) {

        float result = 0.0f;

        if ((vg != null) && (font != null) && (str != null)) {
            int strLen = str.length();
            // be sure to have enough temporary memory to store glyph indices and kernings
            if ((strLen > 0) && tempMemoryReserve(strLen)) {
                // start at origin
                float[] glyphOrigin = new float[] { 0.0f, 0.0f };
                // build the sequence of glyph indices and kerning data
                textLineBuild(font, str);
                // calculate the metrics of the glyph sequence
                vg.vgSetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin, 0);
                vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_GLYPH_USER_TO_SURFACE);
                vg.vgLoadIdentity();
                vg.vgDrawGlyphs(font.openvgHandle, strLen, tmpGlyphIndices, 0, tmpAdjustmentsX, 0, tmpAdjustmentsY, 0, 0, false);
                vg.vgGetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin, 0);
                result = glyphOrigin[0];
            }
        }

        return result;
    }

    // draw a line of text
    static void textLineDraw(VG11 vg,
                             final Font font,
                             final String str,
                             final int paintModes) {

        if ((vg != null) && (font != null) && (str != null) && (paintModes > 0)) {
            int strLen = str.length();
            // be sure to have enough temporary memory to store glyph indices and kernings
            if ((strLen > 0) && tempMemoryReserve(strLen)) {
                // build the sequence of glyph indices and kerning data
                textLineBuild(font, str);
                // draw glyphs
                vg.vgDrawGlyphs(font.openvgHandle, strLen, tmpGlyphIndices, 0, tmpAdjustmentsX, 0, tmpAdjustmentsY, 0, paintModes, false);
            }
        }
    }

    // draw a text along the given path
    static void textAlongPathDraw(VG11 vg,
                                  final Font font,
                                  final VGPath path,
                                  final String str,
                                  final float fontSize,
                                  final int paintModes) {


        if ((font != null) && (path != null) && (str != null) && (fontSize > 0.0f) && (paintModes > 0)) {
            int strLen = str.length();
            // be sure to have enough temporary memory to store glyph indices and kernings
            if ((strLen > 0) && tempMemoryReserve(strLen)) {

                int i;
                float cursor = 0.0f;
                // allocate space for path evaluation
                float[] values = new float[4];
                // set glypth origin to (0, 0)
                float[] glyphOrigin = new float[] { 0.0f, 0.0f };
                int pathSegmentsCount = vg.vgGetParameteri(path, VG_PATH_NUM_SEGMENTS);

                // build the sequence of glyph indices and kerning data
                textLineBuild(font, str);

                // loop over all characters
                for (i = 0; i < strLen; ++i) {
                    // get glyph
                    Glyph glyph = glyphFromGlyphIndex(font, tmpGlyphIndices[i]);
                    float halfEscapement = glyph.escapement[0] * 0.5f;

                    // evaluate path
                    cursor += halfEscapement;
                    vg.vgPointAlongPath(path, 0, pathSegmentsCount, cursor * fontSize, values, 0);

                    // draw glyph
                    vg.vgSetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin, 0);
                    vg.vgSeti(VG_FILL_RULE, glyph.fillRule);
                    vg.vgSeti(VG_MATRIX_MODE, VG_MATRIX_GLYPH_USER_TO_SURFACE);
                    vg.vgLoadIdentity();
                    vg.vgTranslate(values[0], values[1]);
                    vg.vgRotate((float)Math.atan2(values[3], values[2]) * 57.2957795f);
                    vg.vgScale(fontSize, fontSize);
                    vg.vgTranslate(-halfEscapement, 0.0f);
                    vg.vgDrawGlyph(font.openvgHandle, tmpGlyphIndices[i], paintModes, false);

                    // advance cursor
                    cursor += halfEscapement + tmpAdjustmentsX[i];
                }
            }
        }
    }
}
