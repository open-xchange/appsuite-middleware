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

package com.openexchange.service.messaging.internal.receipt;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.service.messaging.MessagingServiceExceptionCode;
import com.openexchange.service.messaging.internal.Constants;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MessagingMessageParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingMessageParser {

    private final byte[] b;

    private int pos;

    /**
     * Initializes a new {@link MessagingMessageParser}.
     */
    public MessagingMessageParser(final byte[] b) {
        super();
        this.b = b;
        pos = 0;
    }

    /**
     * Gets the parsed contiguous message.
     *
     * @return The parsed contiguous message
     * @throws OXException If parsing fails
     */
    public MessagingParsedMessage parse() throws OXException {
        try {
            /*
             * Ensure magic bytes
             */
            {
                final int[] magic = new int[] { (b[pos++] & 0xff), (b[pos++] & 0xff), (b[pos++] & 0xff) };
                if (!Arrays.equals(Constants.MAGIC, magic)) {
                    throw MessagingServiceExceptionCode.BROKEN_MAGIC_BYTES.create(Arrays.toString(magic));
                }
            }
            /*
             * Get message's UUID
             */
            final UUID uuid = UUIDs.toUUID(getByteSequence(UUIDs.UUID_BYTE_LENGTH));
            /*
             * Check if starting package or continued package
             */
            final int prefixCode = (b[pos++] & 0xff);
            /*
             * Contiguous package
             */
            final boolean contiguous = parseBoolean();
            final MessagingParsedMessage ret;
            switch (prefixCode) {
            case Constants.PREFIX_CODE_START:
                final String topic = parseString();
                ret = new MessagingParsedMessage(uuid, prefixCode, topic, getRemainingBytes());
                break;
            case Constants.PREFIX_CODE_DATA:
                final int cnum = parseInt();
                ret = new MessagingParsedMessage(uuid, prefixCode, cnum, getRemainingBytes());
                break;
            default:
                throw MessagingServiceExceptionCode.UNKNOWN_PREFIX_CODE.create(Integer.valueOf(prefixCode));
            }
            ret.setContiguous(contiguous);
            return ret;
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw MessagingServiceExceptionCode.INVALID_MSG_PACKAGE.create(e, new Object[0]);
        }
    }

    /**
     * Gets the next <i>unsigned</i> byte.
     *
     * @return The next <i>unsigned</i> byte
     */
    private int nextByte() {
        return (b[pos++] & 0xff);
    }

    private String parseString() throws OXException {
        return parseString(nextByte(), nextByte());
    }

    private static final int ASCII_LIMIT = 127;

    private static final int STRING_TERMINATOR = 0x00;

    /**
     * First two bytes, which indicate length of string, already consumed.
     */
    private String parseString(final int firstByte, final int secondByte) throws OXException {
        /*
         * Special byte 0xFF indicates absence of current string value.
         */
        if ((Constants.REQUEST_TERMINATOR == firstByte) && (Constants.REQUEST_TERMINATOR == secondByte)) {
            return "";
        }
        boolean encoded = false;
        final int strLength = ((firstByte) << 8) + secondByte;
        final StringBuilder sb = new StringBuilder(strLength);
        for (int strIndex = 0; strIndex < strLength; strIndex++) {
            final int b = nextByte();
            if (b > ASCII_LIMIT) {
                /*
                 * Non-ascii character
                 */
                encoded = true;
                sb.append('=').append(Integer.toHexString(b));
            } else {
                sb.append((char) b);
            }
        }
        if (nextByte() != STRING_TERMINATOR) {
            throw MessagingServiceExceptionCode.UNPARSEABLE_STRING.create();
        }
        if (encoded) {
            return new String(decodeQuotedPrintable(sb.toString().getBytes(com.openexchange.java.Charsets.US_ASCII)), com.openexchange.java.Charsets.UTF_8);
        }
        return sb.toString();
    }

    private static final byte ESCAPE_CHAR = '=';

    /**
     * Decodes an array quoted-printable characters into an array of original bytes. Escaped characters are converted back to their original
     * representation.
     * <p>
     * This function implements a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in RFC 1521.
     * </p>
     *
     * @param bytes The array of quoted-printable characters
     * @return The array of original bytes
     * @throws OXException If quoted-printable decoding fails
     */
    private static final byte[] decodeQuotedPrintable(final byte[] bytes) throws OXException {
        if (bytes == null) {
            return null;
        }
        final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            final int b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    final int u = Character.digit((char) bytes[++i], 16);
                    final int l = Character.digit((char) bytes[++i], 16);
                    if (u == -1 || l == -1) {
                        throw MessagingServiceExceptionCode.INVALID_QUOTED_PRINTABLE.create();
                    }
                    buffer.write((char) ((u << 4) + l));
                } catch (final ArrayIndexOutOfBoundsException e) {
                    throw MessagingServiceExceptionCode.INVALID_QUOTED_PRINTABLE.create();
                }
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Parses the next <code>int</code> value (which consumes next two bytes).
     *
     * @return The next <code>int</code> value
     */
    private int parseInt() {
        return ((b[pos++] & 0xff) << 8) + (b[pos++] & 0xff);
    }

    /**
     * Parses a boolean value.
     *
     * @return The boolean value
     */
    private boolean parseBoolean() {
        return ((b[pos++] & 0xff) > 0);
    }

    /**
     * Gets the next bytes (which consumes next <code>numOfBytes</code> bytes).
     *
     * @param numOfBytes The number of bytes to return
     * @return The next bytes
     */
    private byte[] getByteSequence(final int numOfBytes) {
        final byte[] retval = new byte[numOfBytes];
        final int available = b.length - pos;
        System.arraycopy(b, pos, retval, 0, numOfBytes > available ? available : numOfBytes);
        pos += numOfBytes;
        return retval;
    }

    /**
     * Gets the remaining bytes.
     *
     * @return The next bytes
     */
    private byte[] getRemainingBytes() {
        final int available = b.length - pos;
        final byte[] retval = new byte[available];
        System.arraycopy(b, pos, retval, 0, available);
        pos += available;
        return retval;
    }

}
