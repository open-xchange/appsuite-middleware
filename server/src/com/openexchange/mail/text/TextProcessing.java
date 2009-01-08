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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.mail.text.HTMLProcessing.PATTERN_HREF;
import java.util.regex.Matcher;
import com.openexchange.tools.Collections.SmartIntArray;

/**
 * {@link TextProcessing} - Various methods for text processing
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextProcessing {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(TextProcessing.class);

    private static final String SPLIT_LINES = "\r?\n";

    private static final char CHAR_BREAK = '\n';

    /**
     * Performs the line folding after specified number of characters through parameter <code>linewrap</code>. Occurring HTML links are
     * excluded.
     * <p>
     * If parameter <code>isHtml</code> is set to <code>true</code> the content is returned unchanged.
     * 
     * @param content The plain text content to fold
     * @param linewrap The number of characters which may fit into a line
     * @return The line-folded content
     */
    public static String performLineFolding(final String content, final int linewrap) {
        if (linewrap <= 0) {
            return content;
        }
        final String[] lines = content.split(SPLIT_LINES);
        if (lines.length > 0) {
            final StringBuilder sb = new StringBuilder(content.length() + 128);
            sb.append(foldTextLine(lines[0], linewrap));
            for (int i = 1; i < lines.length; i++) {
                sb.append(CHAR_BREAK).append(foldTextLine(lines[i], linewrap));
            }
            return sb.toString();
        }
        return content;
    }

    private static String foldTextLine(final String line, final int linewrap) {
        return foldTextLineRecursive(line, linewrap, getQuotePrefix(line));
    }

    private static String foldTextLineRecursive(final String line, final int linewrap, final String quote) {
        final int length = line.length();
        if (length <= linewrap) {
            return line;
        }
        final int[] hrefIndices = getHrefIndices(line);
        final int startPos = quote == null ? 0 : quote.length();
        final char c = line.charAt(linewrap);
        final StringBuilder sb = new StringBuilder(length + 5);
        final StringBuilder sub = new StringBuilder(64);
        if (Character.isWhitespace(c)) {
            /*
             * Find last non-whitespace character before
             */
            int i = linewrap - 1;
            int[] sep = null;
            while (i >= startPos) {
                if (!Character.isWhitespace(line.charAt(i))) {
                    if ((sep = isLineBreakInsideHref(hrefIndices, i)) != null) {
                        i = sep[0] - 1;
                        continue;
                    }
                    sb.setLength(0);
                    sub.setLength(0);
                    return sb.append(line.substring(0, i + 1)).append(CHAR_BREAK).append(
                        foldTextLineRecursive(quote == null ? line.substring(linewrap + 1) : sub.append(quote).append(
                            line.substring(linewrap + 1)).toString(), linewrap, quote)).toString();
                }
                i--;
            }
        } else {
            /*
             * Find last whitespace before
             */
            int i = linewrap - 1;
            int[] sep = null;
            while (i >= startPos) {
                if (Character.isWhitespace(line.charAt(i))) {
                    if ((sep = isLineBreakInsideHref(hrefIndices, i)) != null) {
                        i = sep[0] - 1;
                        continue;
                    }
                    sb.setLength(0);
                    sub.setLength(0);
                    return sb.append(line.substring(0, i)).append(CHAR_BREAK).append(
                        foldTextLineRecursive(
                            quote == null ? line.substring(i + 1) : sub.append(quote).append(line.substring(i + 1)).toString(),
                            linewrap,
                            quote)).toString();
                }
                i--;
            }
        }
        final int[] sep = isLineBreakInsideHref(hrefIndices, linewrap);
        if (sep == null) {
            return new StringBuilder(line.length() + 1).append(line.substring(0, linewrap)).append(CHAR_BREAK).append(
                foldTextLineRecursive(quote == null ? line.substring(linewrap) : new StringBuilder().append(quote).append(
                    line.substring(linewrap)).toString(), linewrap, quote)).toString();
        } else if (sep[1] == length) {
            if (sep[0] == startPos) {
                return line;
            }
            return new StringBuilder(line.length() + 1).append(line.substring(0, sep[0])).append(CHAR_BREAK).append(
                foldTextLineRecursive(quote == null ? line.substring(sep[0]) : new StringBuilder().append(quote).append(
                    line.substring(sep[0])).toString(), linewrap, quote)).toString();
        }
        return new StringBuilder(line.length() + 1).append(line.substring(0, sep[1])).append(CHAR_BREAK).append(
            foldTextLineRecursive(
                quote == null ? line.substring(sep[1]) : new StringBuilder().append(quote).append(line.substring(sep[1])).toString(),
                linewrap,
                quote)).toString();
    }

    // private static final Pattern PATTERN_QP = Pattern.compile("((?:\\s?>)+)(\\s?)(.*)");

    private static String getQuotePrefix(final String line) {
        if (line.length() == 0) {
            return null;
        }
        final char[] chars = line.toCharArray();
        final StringBuilder sb = new StringBuilder(8);
        int lastGT = -1;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            if (c == '>') {
                sb.append(c);
                lastGT = i;
            } else if (Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        if (lastGT == -1) {
            /*
             * No '>' character found
             */
            return null;
        }
        /*
         * Allow only 1 whitespace character after last '>' character
         */
        if (lastGT + 2 < sb.length()) {
            sb.delete(lastGT + 2, sb.length());
        }
        return sb.toString();
        // final Matcher m = PATTERN_QP.matcher(line);
        // return m.matches() ? new StringBuilder(m.group(1)).append(m.group(2)).toString() : null;
    }

    private static int[] getHrefIndices(final String line) {
        final SmartIntArray sia = new SmartIntArray(10);
        try {
            final Matcher m = PATTERN_HREF.matcher(line);
            while (m.find()) {
                sia.append(m.start());
                sia.append(m.end());
            }
        } catch (final StackOverflowError error) {
            LOG.error(StackOverflowError.class.getName(), error);
        }
        return sia.toArray();
    }

    private static int[] isLineBreakInsideHref(final int[] hrefIndices, final int linewrap) {
        for (int i = 0; i < hrefIndices.length; i += 2) {
            if ((hrefIndices[i] <= linewrap) && (hrefIndices[i + 1] > linewrap)) {
                return new int[] { hrefIndices[i], hrefIndices[i + 1] };
            }
        }
        /*
         * Not inside a href declaration
         */
        return null;
    }

    /**
     * Initializes a new {@link TextProcessing}
     */
    private TextProcessing() {
        super();
    }
}
