/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mail.text;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import com.openexchange.java.Streams;

/**
 * Enriched2HtmlConverter - converts text content of MIME type 'text/enriched' to regular html content
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Enriched2HtmlConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Enriched2HtmlConverter.class);

    private static enum ParaType {

        PT_LEFT(1, "left"), PT_RIGHT(2, "right"), PT_IN(3, "in"), PT_OUT(4, "out");

        public final int type;

        public final String str;

        private ParaType(final int type, final String str) {
            this.type = type;
            this.str = str;
        }

    }

    private static final int EOF = -1;

    private boolean doReset;

    private String font;

    private String color;

    private int size;

    private int excerpt;

    private int paraType;

    private int paramCounter;

    private int nofill;

    private int newlineCounter;

    private boolean colorParam;

    private boolean fontParam;

    private boolean paraParam;

    /**
     * Initializes a new {@link Enriched2HtmlConverter}.
     */
    public Enriched2HtmlConverter() {
        super();
    }

    /**
     * Converts specified enriched text ("text/enriched" or its predecessor "text/richtext") to HTML.
     *
     * @param enrichedText The enriched text
     * @return The HTML content
     */
    public static final String convertEnriched2Html(final String enrichedText) {
        return new Enriched2HtmlConverter().convert(enrichedText);
    }

    private final void reset() {
        font = null;
        color = null;
        size = 0;
        excerpt = 0;
        paraType = 0;
        paramCounter = 0;
        nofill = 0;
        newlineCounter = 0;
        colorParam = false;
        fontParam = false;
        paraParam = false;
    }

    private static final char CHAR_LT = '<';

    private static final char CHAR_GT = '>';

    private static final char CHAR_SLASH = '/';

    private static final char CHAR_LF = '\n';

    private static final char CHAR_AMP = '&';

    private static final char CHAR_SPACE = ' ';

    private static final char CHAR_QT = '"';

    private static final String HTML_LOWERTHAN = "&lt;";

    private static final String HTML_GREATERTHAN = "&gt;";

    private static final String HTML_AMP = "&amp;";

    private static final String HTML_BREAK = "<br>\n";

    private static final String HTML_DL_DD = "<dl><dd>";

    private static final String HTML_END_DL = "</dl>";

    private static final String HTML_PRE = "<pre>\n";

    private static final String HTML_END_PRE = "</pre>\n";

    private static final String HTML_DIV = "div";

    private static final String HTML_END_DIV = "</div>";

    private static final String HTML_DIV_ALIGN = "<div align=left>";

    private static final String HTML_CENTER = "center";

    private static final String HTML_UNDERLINED = "u";

    private static final String HTML_TT = "tt";

    private static final String HTML_ITALIC = "i";

    private static final String HTML_BOLD = "b";

    public final String convert(final String enrichedText) {
        try {
            if (doReset) {
                reset();
            } else {
                /*
                 * First invocation
                 */
                doReset = true;
            }
            final StringBuilder sb = new StringBuilder(enrichedText.length());
            StringReader input = null;
            try {
                input = new StringReader(enrichedText.replaceAll("\r\n", "\n"));
                int c = EOF;
                final StringBuilder tokenBuilder = new StringBuilder();
                /*
                 * Convert until '-1' (EOF) is reached
                 */
                c = input.read();
                while (c != EOF) {
                    boolean readNext = true;
                    if (c == CHAR_LT) {
                        if (newlineCounter == 1) {
                            sb.append(CHAR_SPACE);
                        }
                        newlineCounter = 0;
                        final int next = input.read();
                        if (next == CHAR_LT) {
                            if (paramCounter <= 0) {
                                sb.append(HTML_LOWERTHAN);
                            }
                        } else {
                            /*
                             * A starting tag
                             */
                            handleEnrichedTag(getTagName(next, input), sb);
                        }
                    } else {
                        if (paramCounter > 0) {
                            if (c != EOF) {
                                tokenBuilder.append(Character.toLowerCase((char) c));
                            }
                            while (((c = input.read()) != EOF) && (c != CHAR_LT)) {
                                tokenBuilder.append(Character.toLowerCase((char) c));
                            }
                            if (c == EOF) {
                                break;
                            }
                            final String token = tokenBuilder.toString();
                            tokenBuilder.setLength(0);
                            readNext = false;
                            if (colorParam) {
                                color = token;
                                openFont(size, font, color, sb);
                            } else if (fontParam) {
                                font = token;
                                openFont(size, font, color, sb);
                            } else if (paraParam) {
                                if (ParaType.PT_LEFT.str.equals(token)) {
                                    paraType = ParaType.PT_LEFT.type;
                                    sb.append(HTML_DL_DD);
                                } else if (ParaType.PT_RIGHT.str.equals(token)) {
                                    paraType = ParaType.PT_RIGHT.type;
                                } else if (ParaType.PT_IN.str.equals(token)) {
                                    paraType = ParaType.PT_IN.type;
                                } else if (ParaType.PT_OUT.str.equals(token)) {
                                    paraType = ParaType.PT_OUT.type;
                                }
                            }
                        } else if ((c == CHAR_LF) && (nofill <= 0)) {
                            if (++newlineCounter > 1) {
                                sb.append(HTML_BREAK);
                                if (excerpt > 0) {
                                    sb.append(HTML_GREATERTHAN).append(CHAR_SPACE);
                                }
                            }
                        } else if (c == CHAR_GT) {
                            sb.append(HTML_GREATERTHAN);
                        } else if (c == CHAR_AMP) {
                            sb.append(HTML_AMP);
                        } else {
                            if (newlineCounter == 1) {
                                sb.append(CHAR_SPACE);
                            }
                            newlineCounter = 0;
                            sb.append((char) c);
                        }
                    }
                    if (readNext) {
                        c = input.read();
                    }
                }
                sb.append(CHAR_LF);
            } finally {
                Streams.close(input);
            }
            return sb.toString();
        } catch (final IOException e) {
            LOG.error("", e);
            return enrichedText;
        }
    }

    private static final String ENRICHED_BOLD = "bold";

    private static final String ENRICHED_ITALIC = "italic";

    private static final String ENRICHED_FIXED = "fixed";

    private static final String ENRICHED_UNDERLINE = "underline";

    private static final String ENRICHED_CENTER = "center";

    private static final String ENRICHED_FLUSHLEFT = "flushleft";

    private static final String ENRICHED_BIGGER = "bigger";

    private static final String ENRICHED_SMALLER = "smaller";

    private static final String ENRICHED_INDENT = "indent";

    private static final String ENRICHED_EXCERPT = "excerpt";

    private static final String ENRICHED_COLOR = "color";

    private static final String ENRICHED_FONTFAMILY = "fontfamily";

    private static final String ENRICHED_PARAINDENT = "paraindent";

    private static final String ENRICHED_FLUSHBOTH = "flushboth";

    private static final String ENRICHED_INDENTRIGHT = "indentright";

    private static final String ENRICHED_PARAM = "param";

    private static final String ENRICHED_NOFILL = "nofill";

    private static final String ENRICHED_PREFIX = "x-tad-";

    private final void handleEnrichedTag(final String tagArg, final StringBuilder sb) {
        String tag;
        final boolean isEndTag = tagArg.charAt(0) == CHAR_SLASH;
        if (isEndTag) {
            tag = tagArg.substring(1);
        } else {
            tag = tagArg;
        }
        if (tag.startsWith(ENRICHED_PREFIX)) {
            tag = tag.substring(6);
        }
        /*
         * Map
         */
        if (ENRICHED_BOLD.equals(tag)) {
            mapSimpleTag(HTML_BOLD, isEndTag, sb);
        } else if (ENRICHED_ITALIC.equals(tag)) {
            mapSimpleTag(HTML_ITALIC, isEndTag, sb);
        } else if (ENRICHED_FIXED.equals(tag)) {
            mapSimpleTag(HTML_TT, isEndTag, sb);
        } else if (ENRICHED_UNDERLINE.equals(tag)) {
            mapSimpleTag(HTML_UNDERLINED, isEndTag, sb);
        } else if (ENRICHED_CENTER.equals(tag)) {
            mapSimpleTag(HTML_CENTER, isEndTag, sb);
        } else if (ENRICHED_FLUSHLEFT.equals(tag)) {
            sb.append(isEndTag ? HTML_END_DIV : HTML_DIV_ALIGN);
        } else if (ENRICHED_BIGGER.equals(tag)) {
            size = isEndTag ? size - 2 : size + 2;
            if (isEndTag) {
                closeFont(sb);
            } else {
                openFont(size, font, color, sb);
            }
        } else if (ENRICHED_SMALLER.equals(tag)) {
            size = isEndTag ? size + 2 : size - 2;
            if (isEndTag) {
                closeFont(sb);
            } else {
                openFont(size, font, color, sb);
            }
        } else if (ENRICHED_INDENT.equals(tag)) {
            sb.append(isEndTag ? HTML_END_DL : HTML_DL_DD);
        } else if (ENRICHED_EXCERPT.equals(tag)) {
            excerpt = isEndTag ? excerpt - 1 : excerpt + 1;
        } else if (ENRICHED_COLOR.equals(tag)) {
            if (isEndTag) {
                colorParam = false;
                color = null;
                closeFont(sb);
            } else {
                colorParam = true;
            }
        } else if (ENRICHED_FONTFAMILY.equals(tag)) {
            if (isEndTag) {
                fontParam = false;
                font = null;
                closeFont(sb);
            } else {
                fontParam = true;
            }
        } else if (ENRICHED_PARAINDENT.equals(tag)) {
            if (isEndTag) {
                paraParam = false;
                if (paraType == ParaType.PT_LEFT.type) {
                    sb.append(HTML_END_DL);
                }
                paraType = 0;
            } else {
                paraParam = true;
            }
        } else if (ENRICHED_FLUSHBOTH.equals(tag)) {
            mapSimpleTag(HTML_DIV, isEndTag, sb);
        } else if (ENRICHED_INDENTRIGHT.equals(tag)) {
            sb.append(isEndTag ? HTML_END_DL : HTML_DL_DD);
        } else if (ENRICHED_PARAM.equals(tag)) {
            paramCounter = isEndTag ? paramCounter - 1 : paramCounter + 1;
        } else if (ENRICHED_NOFILL.equals(tag)) {
            if (isEndTag) {
                nofill--;
                sb.append(HTML_END_PRE);
            } else {
                nofill++;
                sb.append(HTML_PRE);
            }
        } else {
            /*
             * Unknown tag
             */
            sb.append('?').append(HTML_LOWERTHAN);
            if (isEndTag) {
                sb.append(CHAR_SLASH);
            }
            sb.append(tag).append(HTML_GREATERTHAN);
        }
    }

    private static final void mapSimpleTag(final String rpl, final boolean isEndTag, final StringBuilder sb) {
        sb.append(CHAR_LT);
        if (isEndTag) {
            sb.append(CHAR_SLASH);
        }
        sb.append(rpl).append(CHAR_GT);
    }

    private static final String FONT_PREFIX = "<font";

    private static final String FONT_SIZE_PLUS = " size=+";

    private static final String FONT_SIZE_MINUS = " size=-";

    private static final String FONT_COLOR = " color=\"";

    private static final String FONT_FACE = " face=\"";

    private static final void openFont(final int size, final String font, final String color, final StringBuilder sb) {
        sb.append(FONT_PREFIX);
        if (size > 0) {
            sb.append(FONT_SIZE_PLUS).append(size);
        } else if (size < 0) {
            sb.append(FONT_SIZE_MINUS).append(size);
        }
        if (color != null) {
            sb.append(FONT_COLOR).append(color).append(CHAR_QT);
        }
        if (font != null) {
            sb.append(FONT_FACE).append(font).append(CHAR_QT);
        }
        sb.append(CHAR_GT);
    }

    private static final String FONT_CLOSE_TAG = "</font>";

    private static final void closeFont(final StringBuilder sb) {
        sb.append(FONT_CLOSE_TAG);
    }

    private static final String getTagName(final int firstChar, final Reader r) throws IOException {
        /*
         * Assume last read character was '<'
         */
        final StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase((char) firstChar));
        int level = 1;
        while (level > 0) {
            final int c = r.read();
            if (c == EOF) {
                break; // EOF
            }
            if (c == CHAR_LT) {
                level++;
            } else if (c == CHAR_GT) {
                level--;
            } else {
                result.append(Character.toLowerCase((char) c));
            }
        }
        return result.toString();
    }

}
