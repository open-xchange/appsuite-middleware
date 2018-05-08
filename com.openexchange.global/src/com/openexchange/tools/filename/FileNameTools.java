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

package com.openexchange.tools.filename;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.emoji.EmojiRegistry;
import com.openexchange.java.Strings;

/**
 * {@link FileNameTools} - A utility class for file names.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.4
 */
public class FileNameTools {

    /**
     * Sanitizes specified file name. The string is normalized to {@link Form#NFC} (canonical decomposition, followed by canonical
     * composition) implicitly.
     * <p>
     * Allows
     * <ul>
     * <li>Space characters</li>
     * <li>Alphanumeric characters</li>
     * <li>Punctuation characters</li>
     * <li>Any letter characters</li>
     * <li>Emoji code points</li>
     * </ul>
     * All other characters are replaced with an underscore (<code>"_"</code>).
     *
     * @param fileName Raw file name
     * @return Sanitized file name
     */
    public static String sanitizeFilename(String fileName) {
        if (Strings.isEmpty(fileName)) {
            return fileName;
        }
        fileName = Normalizer.normalize(fileName, Form.NFC);
        StringBuilder sb = null;
        int len = fileName.length();
        for (int i = 0; i < len; i++) {
            char ch = fileName.charAt(i);
            if (' ' == ch) { // Space
                if (null != sb) {
                    sb.append(ch);
                }
            } else if (Strings.isAsciiLetterOrDigit(ch)) { // [a-zA-Z0-9]
                if (null != sb) {
                    sb.append(ch);
                }
            } else if (Character.isLetterOrDigit(ch)) { // Any letter or digit
                if (null != sb) {
                    sb.append(ch);
                }
            } else if (Strings.isPunctuation(ch) || isAllowedUnicodeBlock(ch)) { // Punctuations
                if (null != sb) {
                    sb.append(ch);
                }
            } else {
                if (i + 1 < len) {
                    char nc = fileName.charAt(i + 1);
                    if (Character.isSurrogatePair(ch, nc)) {
                        i++;
                        int codePoint = Character.toCodePoint(ch, nc);
                        sb = appendOrReplaceCodePoint(codePoint, len, fileName, i, sb);
                    } else {
                        sb = appendOrReplaceCharacter(ch, len, fileName, i, sb);
                    }
                } else {
                    sb = appendOrReplaceCharacter(ch, len, fileName, i, sb);
                }
            }
        }

        return null == sb ? fileName : sb.toString();
    }

    private static final Set<Character.UnicodeBlock> WHITELISTED_UNICODE_BLOCKS;
    static {
        Set<Character.UnicodeBlock> set = new HashSet<Character.UnicodeBlock>();

        // CJK = Chinese, Japanese and Korean
        set.add(Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
        set.add(Character.UnicodeBlock.CJK_COMPATIBILITY);
        set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS);
        set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
        set.add(Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
        set.add(Character.UnicodeBlock.CJK_STROKES);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C);
        set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D);
        set.add(Character.UnicodeBlock.ENCLOSED_ALPHANUMERICS);
        set.add(Character.UnicodeBlock.ENCLOSED_ALPHANUMERIC_SUPPLEMENT);
        set.add(Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS);
        set.add(Character.UnicodeBlock.ENCLOSED_IDEOGRAPHIC_SUPPLEMENT);
        set.add(Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
        set.add(Character.UnicodeBlock.HIRAGANA);
        set.add(Character.UnicodeBlock.KANA_SUPPLEMENT);
        set.add(Character.UnicodeBlock.KANGXI_RADICALS);
        set.add(Character.UnicodeBlock.KATAKANA);
        set.add(Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS);
        WHITELISTED_UNICODE_BLOCKS = Collections.unmodifiableSet(set);
    }

    private static boolean isAllowedUnicodeBlock(char ch) {
        return WHITELISTED_UNICODE_BLOCKS.contains(Character.UnicodeBlock.of(ch));
    }

    private static StringBuilder appendOrReplaceCharacter(char ch, int len, String fileName, int index, StringBuilder builder) {
        StringBuilder sb = builder;
        if (EmojiRegistry.getInstance().isEmoji(ch)) { // Emojis
            if (null != sb) {
                sb.append(ch);
            }
        } else { // Deny other
            if (null == sb) {
                sb = new StringBuilder(len);
                if (index > 0) {
                    sb.append(fileName, 0, index);
                }
            }
            sb.append('_');
        }
        return sb;
    }

    private static StringBuilder appendOrReplaceCodePoint(int codePoint, int len, String fileName, int index, StringBuilder builder) {
        StringBuilder sb = builder;
        if (EmojiRegistry.getInstance().isEmoji(codePoint)) { // Emojis
            if (null != sb) {
                sb.appendCodePoint(codePoint);
            }
        } else { // Deny other
            if (null == sb) {
                sb = new StringBuilder(len);
                if (index > 0) {
                    sb.append(fileName, 0, index);
                }
            }
            sb.append('_');
        }
        return sb;
    }

}
