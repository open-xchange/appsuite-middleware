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

package com.openexchange.tools.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import com.openexchange.java.Charsets;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * The class <code>QuotedPrintable</code> offers static methods to encode/decode <code>String</code> instances with quoted-printable codec
 * based on <code>javax.mail.internet.MimeUtility</code> class
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CodecUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CodecUtils.class);

    private static final String ENCODING_QP = "quoted-printable";

    private static final String ENCODING_BASE64 = "base64";

    private static final String ENCODING_7BIT = "7bit";

    private static final String ENCODING_8BIT = "8bit";

    private static final String ENCODING_BINARY = "binary";

    private static final String ENCODING_UUENCODE = "uuencode";

    private static final String[] ENCODINGS = new String[] {
        ENCODING_QP, ENCODING_BASE64, ENCODING_7BIT, ENCODING_8BIT, ENCODING_BINARY, ENCODING_UUENCODE };

    private static final String ENCODE_Q = "Q";

    private static final String ENCODE_B = "B";

    private static final String REGEX_PREFIX = "((\\?=)? ?=\\?";

    private static final String REGEX_APPENDIX = "\\?(?:Q|B)\\?)|(\\?=)";

    private static final String[] RPL = { "_", "\\r", "\\n" };

    private static final String[] SUB = { " ", "=0D", "=0A" };

    private CodecUtils() {
        super();
    }

    /**
     * Encodes specified original string with given character encoding and transfer encoding <code>QUOTED PRINTABLE</code>.
     *
     * @param originalStr The original string to encode
     * @param charset The character encoding
     * @return The quoted-printable encoded string
     * @throws UnsupportedEncodingException If specified character encoding is not supported
     */
    public static String encodeQuotedPrintable(final String originalStr, final String charset) throws UnsupportedEncodingException {
        String encStr = MimeUtility.encodeText(originalStr, charset, ENCODE_Q);
        encStr = encStr.replaceAll(new StringBuilder().append(REGEX_PREFIX).append(charset).append(REGEX_APPENDIX).toString(), "");
        for (int i = 0; i < RPL.length; i++) {
            encStr = encStr.replaceAll(RPL[i], SUB[i]);
        }
        return encStr;
    }

    /**
     * Encodes specified original string with given character encoding and transfer encoding <code>BASE 64</code>.
     *
     * @param originalStr The original string to encode
     * @param charset The character encoding
     * @return The base64 encoded string
     * @throws UnsupportedEncodingException If specified character encoding is not supported
     */
    public static String encodeBase64(final String originalStr, final String charset) throws UnsupportedEncodingException {
        String encStr = MimeUtility.encodeText(originalStr, charset, ENCODE_B);
        encStr = encStr.replaceAll(new StringBuilder().append(REGEX_PREFIX).append(charset).append(REGEX_APPENDIX).toString(), "");
        return encStr;
    }

    /**
     * Decodes the specified possibly decoded string using the following encodings: "quoted-printable", "base64", "7bit", "8bit", "binary",
     * and "uuencode". If not encoded the string is returned as is.
     *
     * @param encoded The (possibly) encoded string
     * @param charset The charset encoding to use
     * @return The decoded string if encoded; otherwise the specified string itself
     */
    public static String decode(final String encoded, final String charset) {
        for (int i = 0; i < ENCODINGS.length; i++) {
            if ((i == 0 || i == 1) && !isAscii(encoded)) {
                /*
                 * An quoted-printable or base64 encoded string cannot contains non-ascii characters
                 */
                continue;
            }
            String result = null;
            try {
                result = decode(encoded, ENCODINGS[i], charset);
            } catch (final IOException e) {
                LOG.debug("", e);
                result = encoded;
            } catch (final MessagingException e) {
                LOG.debug("", e);
                result = encoded;
            }
            if (!encoded.equals(result)) {
                return result;
            }
        }
        return encoded;
    }

    /**
     * Decodes specified quoted-printable encoded string using given character encoding.
     *
     * @param quotedPrintableStr The quoted-printable encoded string
     * @param charset The character encoding
     * @return The quoted-printable decoded string
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public static String decodeQuotedPrintable(final String quotedPrintableStr, final String charset) throws IOException, MessagingException {
        return decode(quotedPrintableStr, ENCODING_QP, charset);
    }

    /**
     * Decodes specified base64 encoded string using given character encoding.
     *
     * @param base64Str The base64 encoded string
     * @param charset The character encoding
     * @return The base64 decoded string
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If a messaging error occurs
     */
    public static String decodeBase64(final String base64Str, final String charset) throws IOException, MessagingException {
        return decode(base64Str, ENCODING_BASE64, charset);
    }

    /**
     * Decodes specified encoded string using given transfer-encoding and character-encoding.
     *
     * @param encodedStr The encoded string
     * @param transferEncoding The transfer encoding
     * @param charset The character encoding
     * @return The decoded string
     * @throws IOException If an I/O error occurs
     * @throws MessagingException If a messaging error occurs
     */
    private static String decode(final String encodedStr, final String transferEncoding, final String charset) throws IOException, MessagingException {
        final InputStream inStream = MimeUtility.decode(
            new UnsynchronizedByteArrayInputStream(encodedStr.getBytes(charset)),
            transferEncoding);
        final UnsynchronizedByteArrayOutputStream decodedBytes = new UnsynchronizedByteArrayOutputStream();
        int k = -1;
        final byte[] buffer = new byte[512];
        while ((k = inStream.read(buffer)) > 0) {
            decodedBytes.write(buffer, 0, k);
        }
        return new String(decodedBytes.toByteArray(), Charsets.forName(charset));
    }

    /**
     * Checks whether the specified string only consists of ASCII 7 bit characters.
     *
     * @param s the string to check
     * @return <code>true</code> if less than 128; otherwise <code>false</code>
     */
    private static boolean isAscii(final String s) {
        final int length = s.length();
        boolean isAscii = true;
        for (int i = 0; i < length && isAscii; i++) {
            isAscii = (s.charAt(i) < 128);
        }
        return isAscii;
    }

}
