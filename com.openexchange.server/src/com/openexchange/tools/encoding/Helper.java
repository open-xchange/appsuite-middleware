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

package com.openexchange.tools.encoding;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Pattern;
import javax.mail.internet.MimeUtility;

/**
 * This class contains some helpers for encoding. It only contains simple methods that encode some things.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Helper {

    private Helper() {
        super();
    }

    private static final Pattern PAT_BSLASH = Pattern.compile("\\\\");

    private static final Pattern PAT_QUOTE = Pattern.compile("\"");

    /**
     * Escapes every <code>'"'</code> and <code>'\'</code> character in given string with a heading <code>'\'</code> character.
     *
     * @param value The value to escape
     * @return The escaped value
     */
    public static String escape(final String value) {
        return PAT_QUOTE.matcher(PAT_BSLASH.matcher(value).replaceAll("\\\\\\\\")).replaceAll("\\\\\\\"");
    }

    /**
     * Encodes a filename according RFC2047 and RFC2231. This is used to encode file names for use in http headers as content-disposition
     * for downloading a file to the client. Encoding is only done if the original filename contains non ascii characters. Return the header
     * to the client with the following format:
     * <ul>
     * <li>header attribute name: Content-Disposition</li>
     * <li>header attribute value: filename=&quot;<with this method encoded filename>&quot;</li>
     * </ul>
     *
     * @param orig filename containing non ascii characters
     * @param encoding Character encoding to be used.
     * @param internetExplorer set this true if the client is a Microsoft InternetExplorer
     * @return the encoded filename that can be put directly into the filename of the content-disposition header
     * @throws UnsupportedEncodingException if the given encoding is not supported by java.
     */
    public static String encodeFilename(final String orig, final String encoding, final boolean internetExplorer) throws UnsupportedEncodingException {
        if(orig == null) {
            return null;
        }
        String encoded = orig;
        if (!isASCII(orig)) {
            if (internetExplorer) {
                try {
                    final Charset charset = Charset.forName(encoding);
                    encoded = URLCoder.encode(orig, charset);
                } catch (final UnsupportedCharsetException uce) {
                    throw new UnsupportedEncodingException(uce.getMessage());
                }
            } else {
                encoded = MimeUtility.encodeText(orig, encoding, "B");
            }
        }
        return encoded;
    }

    /**
     * Encodes a filename according RFC2047 and RFC2231. This is used to encode file names for use in http headers as content-disposition
     * for downloading a file to the client. Encoding is only done if the original filename contains non ascii characters. Return the header
     * to the client with the following format:
     * <ul>
     * <li>header attribute name: Content-Disposition</li>
     * <li>header attribute value: filename=&quot;<with this method encoded filename>&quot;</li>
     * </ul>
     * This method encodes especially for Internet Explorer.
     *
     * @param orig filename containing non ascii characters
     * @param charset Char set to be used.
     * @return the encoded filename that can be put directly into the filename of the content-disposition header
     */
    public static String encodeFilenameForIE(String orig, Charset charset) {
        final String retval;
        if (isASCII(orig)) {
            retval = orig;
        } else {
            retval = URLCoder.encode(orig, charset);
        }
        return retval;
    }

    /**
     * Encodes a filename according RFC2047 and RFC2231. This is used to encode file names for use in http headers as content-disposition
     * for downloading a file to the client. Encoding is only done if the original filename contains non ascii characters. Return the header
     * to the client with the following format:
     * <ul>
     * <li>header attribute name: Content-Disposition</li>
     * <li>header attribute value: filename=&quot;<with this method encoded filename>&quot;</li>
     * </ul>
     *
     * @param orig filename containing non ascii characters
     * @param charset Char set to be used.
     * @return the encoded filename that can be put directly into the filename of the content-disposition header
     * @throws UnsupportedEncodingException if the given encoding is not supported by java.
     */
    public static String encodeFilename(String orig, String charset) throws UnsupportedEncodingException {
        final String retval;
        if (isASCII(orig)) {
            retval = orig;
        } else {
            retval = MimeUtility.encodeText(orig, charset, "B");
        }
        return retval;
    }

    private static boolean isASCII(String fileName) {
        boolean retval = true;
        final int length = fileName.length();
        for (int i = length; retval && --i >= 0;) {
            final char c = fileName.charAt(i);
            retval &= c < 0x7f; // non-ascii characters
            retval &= c > 0x21; // space and control characters
            retval &= c != '\u002a'; // *
            retval &= c != '\u0025'; // %
            retval &= c != '\''; // '
        }
        return retval;
    }
}
