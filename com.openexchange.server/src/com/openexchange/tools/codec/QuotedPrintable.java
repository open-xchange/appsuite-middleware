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

package com.openexchange.tools.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
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
public final class QuotedPrintable {

    private static final String ENCODING_QP = "QUOTED-PRINTABLE";

    private static final String ENCODE_Q = "Q";

    private static final String REGEX_PREFIX = "((\\?=)? ?=\\?";

    private static final String REGEX_APPENDIX = "\\?Q\\?)|(\\?=)";

    private static final String[] RPL = { "_", "\\r", "\\n" };

    private static final String[] SUB = { " ", "=0D", "=0A" };

    private QuotedPrintable() {
        super();
    }

    private static final ConcurrentMap<String, Pattern> REGEXES = new ConcurrentHashMap<>(8);

    private static Pattern getRegexFor(String charset) {
        Pattern pattern = REGEXES.get(charset);
        if (pattern != null) {
            return pattern;
        }

        pattern = Pattern.compile(new StringBuilder(REGEX_PREFIX).append(charset).append(REGEX_APPENDIX).toString());
        REGEXES.put(charset, pattern);
        return pattern;
    }

    /**
     * Encodes specified original string with given character encoding and transfer encoding <code>QUOTED PRINTABLE</code>.
     *
     * @param originalStr The original string to encode
     * @param charset The character encoding
     * @return The quoted-printable encoded string
     * @throws UnsupportedEncodingException If specified character encoding is not supported
     */
    public static String encodeString(final String originalStr, final String charset) throws UnsupportedEncodingException {
        String encStr = MimeUtility.encodeText(originalStr, charset, ENCODE_Q);
        encStr = getRegexFor(charset).matcher(encStr).replaceAll("");
        for (int i = 0; i < RPL.length; i++) {
            encStr = encStr.replaceAll(RPL[i], SUB[i]);
        }
        return encStr;
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
    public static String decodeString(final String quotedPrintableStr, final String charset) throws IOException, MessagingException {
        final InputStream inStream = MimeUtility.decode(new UnsynchronizedByteArrayInputStream(quotedPrintableStr.getBytes(charset)), ENCODING_QP);
        final UnsynchronizedByteArrayOutputStream decodedBytes = new UnsynchronizedByteArrayOutputStream();
        byte[] buffer = new byte[512];
        for (int k; (k = inStream.read(buffer)) > 0;) {
            decodedBytes.write(buffer, 0, k);
        }
        return new String(decodedBytes.toByteArray(), Charsets.forName(charset));
    }

}
