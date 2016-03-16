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

package com.openexchange.proxy.servlet;

import java.util.UUID;

/**
 * {@link UUIDs} - Utility class for {@link UUID}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UUIDs {

    /**
     * Initializes a new {@link UUIDs}.
     */
    private UUIDs() {
        super();
    }

    /**
     * Gets the unformatted string representation of specified {@link UUID} instance.<br>
     * Example:<br>
     * <code>
     * &nbsp;&nbsp;067e6162-3b6f-4ae2-a171-2470b63dff00
     * </code><br>
     * is converted to<br>
     * <code>
     * &nbsp;&nbsp;067e61623b6f4ae2a1712470b63dff00
     * </code>
     *
     * @param uuid The {@link UUID} instance
     * @return The unformatted string representation
     */
    public static String getUnformattedString(final UUID uuid) {
        return new String(encodeHex(toByteArray(uuid)));
    }

    private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static char[] encodeHex(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = digits[(0xF0 & data[i]) >>> 4];
            out[j++] = digits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * Gets the UUID from specified unformatted string.
     *
     * @param unformattedString The unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID
     * @throws IllegalArgumentException If specified string has an odd length or contains an illegal hexadecimal character
     */
    public static UUID fromUnformattedString(final String unformattedString) {
        return toUUID(decodeHex(unformattedString));
    }

    private static byte[] decodeHex(final String data) throws IllegalArgumentException {
        final int len = data.length();
        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }
        final byte[] out = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data.charAt(j), j) << 4;
            j++;
            f = f | toDigit(data.charAt(j), j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
        return out;
    }

    private static int toDigit(final char ch, final int index) throws IllegalArgumentException {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException(
                new StringBuilder("Illegal hexadecimal character \"").append(ch).append("\" at index ").append(index).toString());
        }
        return digit;
    }

    /**
     * Gets the byte array of specified {@link UUID} instance.
     *
     * @param uuid The {@link UUID} instance
     * @return The byte array of specified {@link UUID} instance
     */
    public static byte[] toByteArray(final UUID uuid) {
        return append(toBytes(uuid.getMostSignificantBits()), toBytes(uuid.getLeastSignificantBits()));
    }

    private static final int UUID_BYTE_LENGTH = 16;

    /**
     * Generates a new {@link UUID} instance from specified byte array.
     *
     * @param bytes The byte array
     * @return A new {@link UUID} instance
     * @throws IllegalArgumentException If passed byte array is <code>null</code> or its length is not 16
     */
    public static UUID toUUID(final byte[] bytes) {
        if (null == bytes) {
            throw new IllegalArgumentException("Byte array is null.");
        }
        if (bytes.length != UUID_BYTE_LENGTH) {
            throw new IllegalArgumentException("UUID must be contructed using a 16 byte array.");
        }
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    /**
     * Appends specified byte arrays.
     *
     * @param first The first byte array
     * @param second The second byte array to append
     * @return A new byte array containing specified byte arrays
     */
    private static byte[] append(final byte[] first, final byte[] second) {
        final byte[] bytes = new byte[first.length + second.length];
        System.arraycopy(first, 0, bytes, 0, first.length);
        System.arraycopy(second, 0, bytes, first.length, second.length);
        return bytes;
    }

    /**
     * Builds a <code>byte</code> array with length 8 from a <code>long</code>.
     *
     * @param n The number
     * @return The filled <code>byte</code> array
     */
    private static byte[] toBytes(final long n) {
        final byte[] b = new byte[8];
        long byteVal = n;
        for (int i = 7; i > 0; i--) {
            b[i] = (byte) byteVal;
            byteVal >>>= 8;
        }
        b[0] = (byte) byteVal;
        return b;
    }

}
