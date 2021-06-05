/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        if (orig == null) {
            return null;
        }
        String encoded = orig;
        if (!isASCII(orig)) {
            if (internetExplorer) {
                try {
                    final Charset charset = Charset.forName(encoding);
                    encoded = URLCoder.encode(orig, charset);
                } catch (UnsupportedCharsetException uce) {
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
