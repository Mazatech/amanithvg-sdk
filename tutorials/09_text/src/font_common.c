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
#include "font_common.h"
#include <string.h>
#include <math.h>

static VGuint* tmpGlyphIndices = NULL;
static VGfloat* tmpAdjustmentsX = NULL;
static VGfloat* tmpAdjustmentsY = NULL;
static size_t tmpArraysSize = 0;

// compare two mapped characters
static VGint mappedCharsCompare(const void* arg0,
                                const void* arg1) {

    const MappedChar* ch0 = (const MappedChar*)arg0;
    const MappedChar* ch1 = (const MappedChar*)arg1;

    return (VGint)ch0->charCode - (VGint)ch1->charCode;
}

// given a character code, return its glyph index
VGint glyphIndexFromCharCode(const Font* font,
                             const VGint charCode) {

    VGint glyphIndex = -1;

    if (font != NULL) {
        if (font->charCodesMap != NULL) {
            const MappedChar ch = { charCode, 0 };
            // "character code" to "glyph index" map is sorted by ascending character
            // codes, so we can perform a binary search, in order to speedup things
            const MappedChar* found = (MappedChar*)bsearch(&ch, font->charCodesMap, font->charCodesMapSize, sizeof(MappedChar), mappedCharsCompare);
            if (found) {
                glyphIndex = found->glyphIndex;
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
static VGint glyphsCompare(const void* arg0,
                           const void* arg1) {

    const Glyph* glyph0 = (const Glyph*)arg0;
    const Glyph* glyph1 = (const Glyph*)arg1;

    return (VGint)glyph0->glyphIndex - (VGint)glyph1->glyphIndex;
}

// given a glyph index, return the associated Glyph structure
const Glyph* glyphFromGlyphIndex(const Font* font,
                                 const VGint glyphIndex) {

    Glyph glyph;

    glyph.glyphIndex = glyphIndex;
    // glyphs are sorted by ascending glyph index, so we can
    // perform a binary search, in order to speedup things
    return (const Glyph*)bsearch(&glyph, font->glyphs, font->glyphsCount, sizeof(Glyph), glyphsCompare);
}

// given a character code, return the associated Glyph structure
const Glyph* glyphFromCharCode(const Font* font,
                               const VGint charCode) {

    const VGint glyphIndex = glyphIndexFromCharCode(font, charCode);
    return (glyphIndex >= 0) ? glyphFromGlyphIndex(font, glyphIndex) : NULL;
}

// compare two kerning entries
static VGint kerningsCompare(const void* arg0,
                             const void* arg1) {

    const KerningEntry* krn0 = (const KerningEntry*)arg0;
    const KerningEntry* krn1 = (const KerningEntry*)arg1;

    return (VGint)krn0->key - (VGint)krn1->key;
}

// given a couple of glyph indices, return the relative kerning (NULL if kerning is zero)
const KerningEntry* kerningFromGlyphIndices(const Font* font,
                                            const VGint leftGlyphIndex,
                                            const VGint rightGlyphIndex) {

    const KerningEntry krn = {
        // the key
        ((VGuint)leftGlyphIndex << 16) + (VGuint)rightGlyphIndex,
        0.0f, 0.0f
    };

    return (const KerningEntry*)bsearch(&krn, font->kerningTable, font->kerningTableSize, sizeof(KerningEntry), kerningsCompare);
}

// given a couple of character codes, return the relative kerning (NULL if kerning is zero)
const KerningEntry* kerningFromCharCodes(const Font* font,
                                         const VGint leftCharCode,
                                         const VGint rightCharCode) {

    const VGint leftGlyphIndex = glyphIndexFromCharCode(font, leftCharCode);
    const VGint rightGlyphIndex = glyphIndexFromCharCode(font, rightCharCode);

    // if both glyph indices have been found, try searching for the kerning information
    return ((leftGlyphIndex >= 0) && (rightGlyphIndex >= 0)) ? kerningFromGlyphIndices(font, leftGlyphIndex, rightGlyphIndex) : NULL;
}

// reserve temporary memory to store glyph indices and kernings, for printing/drawing purposes
static VGboolean tempMemoryReserve(const size_t strLen) {

    if (tmpArraysSize < strLen) {

        if (tmpArraysSize == 0) {
            // first allocation
            tmpGlyphIndices = (VGuint*)malloc(strLen * sizeof(VGuint));
            if (tmpGlyphIndices != NULL) {
                tmpAdjustmentsX = (VGfloat*)malloc(strLen * sizeof(VGfloat));
                if (tmpAdjustmentsX != NULL) {
                    tmpAdjustmentsY = (VGfloat*)malloc(strLen * sizeof(VGfloat));
                    if (tmpAdjustmentsY != NULL) {
                        // allocation done!
                        tmpArraysSize = strLen;
                    }
                    else {
                        // allocation has failed, release unused memory
                        free(tmpGlyphIndices);
                        tmpGlyphIndices = NULL;
                        free(tmpAdjustmentsX);
                        tmpAdjustmentsX = NULL; 
                    }
                }
                else {
                    // allocation has failed, release unused memory
                    free(tmpGlyphIndices);
                    tmpGlyphIndices = NULL;
                }
            }
        }
        else {
            // a reallocation/expansion
            VGuint* glyphIndices = (VGuint*)realloc(tmpGlyphIndices, strLen * sizeof(VGuint));
            VGfloat* adjustmentsX = (VGfloat*)realloc(tmpAdjustmentsX, strLen * sizeof(VGfloat));
            VGfloat* adjustmentsY = (VGfloat*)realloc(tmpAdjustmentsY, strLen * sizeof(VGfloat));
            if ((glyphIndices != NULL) && (adjustmentsX != NULL) && (adjustmentsY != NULL)) {
                // reallocation done!
                tmpGlyphIndices = glyphIndices;
                tmpAdjustmentsX = adjustmentsX;
                tmpAdjustmentsY = adjustmentsY;
                tmpArraysSize = strLen;
            }
            else {
                // reallocation has failed, release unused memory
                if (glyphIndices != NULL) {
                    free(glyphIndices);
                }
                if (adjustmentsX != NULL) {
                    free(adjustmentsX);
                }
                if (adjustmentsY != NULL) {
                    free(adjustmentsY);
                }
            }
        }
    }

    return (tmpArraysSize >= strLen) ? VG_TRUE : VG_FALSE;
}

static void textLineBuild(const Font* font,
                          const char* str,
                          const size_t strLen) {

    size_t i;
    VGint leftGlyphIndex = glyphIndexFromCharCode(font, str[0]);

    // first glyph index
    tmpGlyphIndices[0] = leftGlyphIndex;
    for (i = 1; i < strLen; ++i) {
        const VGint rightGlyphIndex = glyphIndexFromCharCode(font, str[i]);
        const KerningEntry* krn = kerningFromGlyphIndices(font, leftGlyphIndex, rightGlyphIndex);
        // append glyph index
        tmpGlyphIndices[i] = rightGlyphIndex;
        // initialize adjustment
        tmpAdjustmentsX[i - 1] = 0.0f;
        tmpAdjustmentsY[i - 1] = 0.0f;
        // add kerning info
        if (krn != NULL) {
            tmpAdjustmentsX[i - 1] += krn->x;
            tmpAdjustmentsY[i - 1] += krn->y;
        }
        leftGlyphIndex = rightGlyphIndex;
    }
    // last adjustment entry
    tmpAdjustmentsX[strLen - 1] = 0.0f;
    tmpAdjustmentsY[strLen - 1] = 0.0f;
}

// get text line width, in object space
VGfloat textLineWidth(const Font* font,
                      const char* str) {

    VGfloat result = 0.0f;

    if ((font != NULL) && (str != NULL)) {
        const size_t strLen = strlen(str);
        // be sure to have enough temporary memory to store glyph indices and kernings
        if ((strLen > 0) && tempMemoryReserve(strLen)) {
            // start at origin
            VGfloat glyphOrigin[2] = { 0.0f, 0.0f };
            // build the sequence of glyph indices and kerning data
            textLineBuild(font, str, strLen);
            // calculate the metrics of the glyph sequence
            vgSetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin);
            vgSeti(VG_MATRIX_MODE, VG_MATRIX_GLYPH_USER_TO_SURFACE);
            vgLoadIdentity();
            vgDrawGlyphs(font->openvgHandle, (VGint)strLen, tmpGlyphIndices, tmpAdjustmentsX, tmpAdjustmentsY, 0, VG_FALSE);
            vgGetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin);
            result = glyphOrigin[0];
        }
    }

    return result;
}

// draw a line of text
void textLineDraw(const Font* font,
                  const char* str,
                  const VGbitfield paintModes) {

    if ((font != NULL) && (str != NULL) && (paintModes > 0)) {
        const size_t strLen = strlen(str);
        // be sure to have enough temporary memory to store glyph indices and kernings
        if ((strLen > 0) && tempMemoryReserve(strLen)) {
            // build the sequence of glyph indices and kerning data
            textLineBuild(font, str, strLen);
            // draw glyphs
            vgDrawGlyphs(font->openvgHandle, (VGint)strLen, tmpGlyphIndices, tmpAdjustmentsX, tmpAdjustmentsY, paintModes, VG_FALSE);
        }
    }
}

// draw a text along the given path
void textAlongPathDraw(const Font* font,
                       const VGPath path,
                       const char* str,
                       const VGfloat fontSize,
                       const VGbitfield paintModes) {


    if ((font != NULL) && (path != VG_INVALID_HANDLE) && (str != NULL) && (fontSize > 0.0f) && (paintModes > 0)) {
        const size_t strLen = strlen(str);
        // be sure to have enough temporary memory to store glyph indices and kernings
        if ((strLen > 0) && tempMemoryReserve(strLen)) {

            size_t i;
            VGfloat cursor = 0.0f;
            // set glypth origin to (0, 0)
            const VGfloat glyphOrigin[2] = { 0.0f, 0.0f };
            const VGint pathSegmentsCount = vgGetParameteri(path, VG_PATH_NUM_SEGMENTS);

            // build the sequence of glyph indices and kerning data
            textLineBuild(font, str, strLen);

            // loop over all characters
            for (i = 0; i < strLen; ++i) {
                // get glyph
                VGfloat x, y, tgx, tgy;
                const Glyph* glyph = glyphFromGlyphIndex(font, tmpGlyphIndices[i]);
                const VGfloat halfEscapement = glyph->escapement[0] * 0.5f;

                // evaluate path
                cursor += halfEscapement;
                vgPointAlongPath(path, 0, pathSegmentsCount, cursor * fontSize, &x, &y, &tgx, &tgy);

                // draw glyph
                vgSetfv(VG_GLYPH_ORIGIN, 2, glyphOrigin);
                vgSeti(VG_FILL_RULE, glyph->fillRule);
                vgSeti(VG_MATRIX_MODE, VG_MATRIX_GLYPH_USER_TO_SURFACE);
                vgLoadIdentity();
                vgTranslate(x, y);
                vgRotate((VGfloat)atan2(tgy, tgx) * 57.2957795f);
                vgScale(fontSize, fontSize);
                vgTranslate(-halfEscapement, 0.0f);
                vgDrawGlyph(font->openvgHandle, tmpGlyphIndices[i], paintModes, VG_FALSE);

                // advance cursor
                cursor += halfEscapement + tmpAdjustmentsX[i];
            }
        }
    }
}

// to be called after finishing using these font utilities
void fontCommonFinish(void) {

    if (tmpGlyphIndices != NULL) {
        free(tmpGlyphIndices);
        tmpGlyphIndices = NULL;
    }

    if (tmpAdjustmentsX != NULL) {
        free(tmpAdjustmentsX);
        tmpAdjustmentsX = NULL;
    }

    if (tmpAdjustmentsY != NULL) {
        free(tmpAdjustmentsY);
        tmpAdjustmentsY = NULL;
    }

    tmpArraysSize = 0;    
}
