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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote.util;

import java.io.ByteArrayOutputStream;

/**
 * {@link HexUtils}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HexUtils {

    // -------------------------------------------------------------- Constants

    /**
     * Table for HEX to DEC byte translation.
     */
    public static final int[] DEC = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 00, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10,
        11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11,
        12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, };

    /**
     * Table for DEC to HEX byte translation.
     */
    public static final byte[] HEX = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a',
        (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };

    // --------------------------------------------------------- Static Methods

    /**
     * Convert a String of hexadecimal digits into the corresponding byte array by encoding each two hexadecimal digits as a byte.
     *
     * @param digits Hexadecimal digits representation
     * @exception IllegalArgumentException if an invalid hexadecimal digit is found, or the input string contains an odd number of
     *                hexadecimal digits
     */
    public static byte[] convert(final String digits) {
        if (null == digits) {
            return null;
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < digits.length(); i += 2) {
            final char c1 = digits.charAt(i);
            if ((i + 1) >= digits.length()) {
                throw new IllegalArgumentException("hexUtil.odd");
            }
            final char c2 = digits.charAt(i + 1);
            byte b = 0;
            if ((c1 >= '0') && (c1 <= '9')) {
                b += ((c1 - '0') * 16);
            } else if ((c1 >= 'a') && (c1 <= 'f')) {
                b += ((c1 - 'a' + 10) * 16);
            } else if ((c1 >= 'A') && (c1 <= 'F')) {
                b += ((c1 - 'A' + 10) * 16);
            } else {
                throw new IllegalArgumentException("hexUtil.bad");
            }
            if ((c2 >= '0') && (c2 <= '9')) {
                b += (c2 - '0');
            } else if ((c2 >= 'a') && (c2 <= 'f')) {
                b += (c2 - 'a' + 10);
            } else if ((c2 >= 'A') && (c2 <= 'F')) {
                b += (c2 - 'A' + 10);
            } else {
                throw new IllegalArgumentException("hexUtil.bad");
            }
            baos.write(b);
        }
        return (baos.toByteArray());

    }

    /**
     * Convert a byte array into a printable format containing a String of hexadecimal digit characters (two per byte).
     *
     * @param bytes Byte array representation
     */
    public static String convert(final byte bytes[]) {
        final StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(convertDigit((bytes[i] >> 4)));
            sb.append(convertDigit((bytes[i] & 0x0f)));
        }
        return (sb.toString());

    }

    /**
     * Convert 4 hex digits to an int, and return the number of converted bytes.
     *
     * @param hex Byte array containing exactly four hexadecimal digits
     * @exception IllegalArgumentException if an invalid hexadecimal digit is included
     */
    public static int convert2Int(final byte[] hex) {
        // Code from Ajp11, from Apache's JServ

        // assert b.length==4
        // assert valid data
        int len;
        if (hex.length < 4) {
            return 0;
        }
        if (DEC[hex[0]] < 0) {
            throw new IllegalArgumentException("hexUtil.bad");
        }
        len = DEC[hex[0]];
        len = len << 4;
        if (DEC[hex[1]] < 0) {
            throw new IllegalArgumentException("hexUtil.bad");
        }
        len += DEC[hex[1]];
        len = len << 4;
        if (DEC[hex[2]] < 0) {
            throw new IllegalArgumentException("hexUtil.bad");
        }
        len += DEC[hex[2]];
        len = len << 4;
        if (DEC[hex[3]] < 0) {
            throw new IllegalArgumentException("hexUtil.bad");
        }
        len += DEC[hex[3]];
        return len;
    }

    /**
     * [Private] Convert the specified value (0 .. 15) to the corresponding hexadecimal digit.
     *
     * @param value Value to be converted
     */
    private static char convertDigit(int value) {

        value &= 0x0f;
        if (value >= 10) {
            return ((char) (value - 10 + 'a'));
        } else {
            return ((char) (value + '0'));
        }

    }
}
