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
        encStr = encStr.replaceAll(new StringBuilder().append(REGEX_PREFIX).append(charset).append(REGEX_APPENDIX).toString(), "");
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
        final InputStream inStream = MimeUtility.decode(
            new UnsynchronizedByteArrayInputStream(quotedPrintableStr.getBytes(charset)),
            ENCODING_QP);
        final UnsynchronizedByteArrayOutputStream decodedBytes = new UnsynchronizedByteArrayOutputStream();
        int k = -1;
        final byte[] buffer = new byte[512];
        while ((k = inStream.read(buffer)) > 0) {
            decodedBytes.write(buffer, 0, k);
        }
        return new String(decodedBytes.toByteArray(), Charsets.forName(charset));
    }

}
