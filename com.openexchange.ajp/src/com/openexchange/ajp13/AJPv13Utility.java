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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

/**
 * {@link AJPv13Utility} - Provides some utility methods for AJP processing
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13Utility {

    /**
     * Initializes a new {@link AJPv13Utility}
     */
    private AJPv13Utility() {
        super();
    }

    private static final Pattern P_DOT = Pattern.compile("\\.");

    private static final Pattern P_MINUS = Pattern.compile("-");

    private static final URLCodec URL_CODEC = new URLCodec(CharEncoding.ISO_8859_1);

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String urlEncode(final String s) {
        try {
            return isEmpty(s) ? s : P_MINUS.matcher(P_DOT.matcher(URL_CODEC.encode(s)).replaceAll("%2E")).replaceAll("%2D");
        } catch (final EncoderException e) {
            return s;
        }
    }

    /**
     * URL decodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String decodeUrl(final String s, final String charset) {
        try {
            return isEmpty(s) ? s : (isEmpty(charset) ? URL_CODEC.decode(s) : URL_CODEC.decode(s, charset));
        } catch (final DecoderException e) {
            return s;
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }

    /** Checks for an empty string */
    public static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /**
     * Dumps given AJP package's bytes
     *
     * @param bytes The AJP package's bytes
     * @return A string representing formatted AJP package's bytes for logging purpose
     */
    public static String dumpBytes(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        final String space = "    ";
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(1024);
        int c = 0;
        int l = 0;
        for (final byte b : bytes) {
            if (c % 16 == 0) {
                sb.append('\r').append('\n');
                c = 0;
                final String hex = Integer.toHexString(l).toUpperCase();
                l += 16;
                final int nOZ = 4 - hex.length();
                for (int i = 0; i < nOZ; i++) {
                    sb.append('0');
                }
                sb.append(hex).append(space);
            } else {
                sb.append(' ');
            }
            final String s = Integer.toHexString(b & 0xff).toUpperCase();
            if (s.length() == 1) {
                sb.append('0');
            }
            sb.append(s);
            c++;
        }
        return sb.toString();
    }

    /**
     * Dumps given AJP package's bytes
     *
     * @param magic1 The first magic byte
     * @param magic2 The second magic byte
     * @param bytes The remaining AJP package's bytes
     * @return A string representing formatted AJP package's bytes for logging purpose
     */
    public static String dumpBytes(final byte magic1, final byte magic2, final byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        final String space = "    ";
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(1024);
        sb.append("0000").append(space).append(Integer.toHexString(magic1).toUpperCase()).append(' ').append(
            Integer.toHexString(magic2).toUpperCase());
        int c = 2;
        int l = 0;
        for (final byte b : bytes) {
            if (c == 16) {
                sb.append('\r').append('\n');
                c = 0;
                l += 16;
                final String hex = Integer.toHexString(l).toUpperCase();
                final int nOZ = 4 - hex.length();
                for (int i = 0; i < nOZ; i++) {
                    sb.append('0');
                }
                sb.append(hex).append(space);
            } else {
                sb.append(' ');
            }
            final String s = Integer.toHexString(b & 0xff).toUpperCase();
            if (s.length() == 1) {
                sb.append('0');
            }
            sb.append(s);
            c++;
        }
        return sb.toString();
    }

    /**
     * Dumps specified byte.
     *
     * @param b The byte
     * @return A string representing the byte
     */
    public static String dumpByte(final byte b) {
        final String s = Integer.toHexString(b & 0xff).toUpperCase();
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(4).append("0x");
        if (s.length() == 1) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Parses specified bytes' <code>int</code> value.
     *
     * @return The <code>int</code> value
     */
    public static int parseInt(final byte higher, final byte lower) {
        return ((higher & 0xff) << 8) + (lower & 0xff);
    }
}
