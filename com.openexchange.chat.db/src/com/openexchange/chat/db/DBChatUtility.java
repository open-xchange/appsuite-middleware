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

package com.openexchange.chat.db;

import java.util.UUID;

/**
 * {@link DBChatUtility} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChatUtility {

    /**
     * Initializes a new {@link DBChatUtility}.
     */
    private DBChatUtility() {
        super();
    }

    /**
     * The unhex-replace string: <code>UNHEX(REPLACE(?,'-',''))</code>
     */
    private static final String UNHEX_REPLACE_STRING = "UNHEX(REPLACE(?,'-',''))";

    /**
     * Appends the unhex-replace string: <code>UNHEX(REPLACE(?,'-',''))</code>
     *
     * @param sb The string builder to append to
     * @return The string builder with unhex-replace string appended
     */
    public static StringBuilder appendUnhexReplaceString(final StringBuilder sb) {
        return sb.append(UNHEX_REPLACE_STRING);
    }

    /**
     * Gets the unhex-replace string: <code>UNHEX(REPLACE(?,'-',''))</code>
     *
     * @return The unhex-replace string
     */
    public static String getUnhexReplaceString() {
        return UNHEX_REPLACE_STRING;
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
            throw new IllegalArgumentException("UUID must be contructed using a byte array with length 16, but is: " + bytes.length);
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

    private static final int DEFAULT = -1;

    private static final int RADIX = 10;

    /**
     * Parses as an unsigned <code>int</code>.
     *
     * @param s The string to parse
     * @return An unsigned <code>int</code> or <code>-1</code>.
     */
    public static int parseUnsignedInt(final String s) {
        if (s == null) {
            return DEFAULT;
        }
        final int max = s.length();
        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        int result = 0;
        int i = 0;

        final long limit = -Long.MAX_VALUE;
        final int multmin = (int) (limit / RADIX);
        int digit;

        if (i < max) {
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return DEFAULT;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return DEFAULT;
            }
            if (result < multmin) {
                return DEFAULT;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return DEFAULT;
            }
            result -= digit;
        }
        return -result;
    }

    private static int digit(final char c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        default:
            return -1;
        }
    }

}
