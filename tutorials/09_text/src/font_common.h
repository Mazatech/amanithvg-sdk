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
#ifndef FONT_COMMON_H
#define FONT_COMMON_H

#include <VG/openvg.h>
#include <stdlib.h>

typedef struct {
    VGuint charCode;
    VGuint glyphIndex;
} MappedChar;

typedef struct {
    // glyph index within the OpenVG font object
    VGuint glyphIndex;
    // the advance width for this glyph
    VGfloat escapement[2];
    // OpenVG path commands defining the glyph geometry
    VGint commandsCount;
    const VGubyte* commands;
    // OpenVG path coordinates defining the glyph geometry
    VGint coordinatesCount;
    const VGfloat* coordinates;
    // fill rule of glyph geometry
    VGFillRule fillRule;
} Glyph;

typedef struct {
    // the key representing two glyph indices ((leftGlyphIndex << 16) + rightGlyphIndex)
    VGuint key;
    // the kerning amount relative to the chars couple
    VGfloat x;
    VGfloat y;
} KerningEntry;

typedef struct {
    // OpenVG font object
    VGFont openvgHandle;
    // "character code" to "glyph index" map, sorted by ascending character codes
    const MappedChar* charCodesMap;
    // number of entries within the character codes map
    const VGuint charCodesMapSize;
    // glyphs data
    const Glyph* glyphs;
    // number of glyphs
    const VGuint glyphsCount;
    // kerning table
    const KerningEntry* kerningTable;
    // number of kerning entries
    const VGuint kerningTableSize;
} Font;

#ifdef __cplusplus
extern "C" {
#endif

// given a character code, return its glyph index
VGint glyphIndexFromCharCode(const Font* font,
                             const VGint charCode);

// given a glyph index, return the associated Glyph structure
const Glyph* glyphFromGlyphIndex(const Font* font,
                                 const VGint glyphIndex);

// given a character code, return the associated Glyph structure
const Glyph* glyphFromCharCode(const Font* font,
                               const VGint charCode);

// given a couple of glyph indices, return the relative kerning (NULL if kerning is zero)
const KerningEntry* kerningFromGlyphIndices(const Font* font,
                                            const VGint leftGlyphIndex,
                                            const VGint rightGlyphIndex);

// given a couple of character codes, return the relative kerning (NULL if kerning is zero)
const KerningEntry* kerningFromCharCodes(const Font* font,
                                         const VGint leftCharCode,
                                         const VGint rightCharCode);
// get text line width, in object space
VGfloat textLineWidth(const Font* font,
                      const char* str);

// draw a line of text
void textLineDraw(const Font* font,
                  const char* str,
                  const VGbitfield paintModes);

// draw a text along the given path
void textAlongPathDraw(const Font* font,
                       const VGPath path,
                       const char* str,
                       const VGfloat fontSize,
                       const VGbitfield paintModes);

// to be called after finishing using these font utilities
void fontCommonFinish(void);

#ifdef __cplusplus
}
#endif

#endif /* FONT_COMMON_H */
