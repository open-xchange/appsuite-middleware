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

package com.openexchange.java.util;

/**
 * {@link Tools} - A utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tools {

    /**
     * Initializes a new {@link Tools}.
     */
    private Tools() {
        super();
    }

    /** The radix for base <code>10</code>. */
    private static final int RADIX = 10;
    private static final int INT_LIMIT = -Integer.MAX_VALUE;
    private static final int INT_MULTMIN = INT_LIMIT / RADIX;

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    public static final int getUnsignedInteger(final String s) {
        if (s == null) {
            return -1;
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

        int digit = digit(s.charAt(i++));
        if (digit < 0) {
            return -1;
        }
        result = -digit;

        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            if (result < INT_MULTMIN) {
                return -1;
            }
            result *= RADIX;
            if (result < INT_LIMIT + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

    private static final long LONG_LIMIT = -Long.MAX_VALUE;
    private static final long LONG_MULTMIN = LONG_LIMIT / RADIX;

    /**
     * Parses a positive <code>long</code> value from passed {@link String} instance.
     * <p>
     * Note that neither the character <code>L</code> (<code>'&#92;u004C'</code>) nor <code>l</code> (<code>'&#92;u006C'</code>) is
     * permitted to appear at the end of the string as a type indicator, as would be permitted in Java programming language source code.
     *
     * @param s The string to parse
     * @return The parsed positive <code>long</code> value or <code>-1</code> if parsing failed
     */
    public static long getUnsignedLong(final String s) {
        if (s == null) {
            return -1;
        }

        int max = s.length();
        if (max <= 0 || s.charAt(0) == '-') {
            return -1;
        }

        long result = 0;
        int i = 0;

        int digit = digit(s.charAt(i++));
        if (digit < 0) {
            return -1;
        }
        result = -digit;

        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            if (result < LONG_MULTMIN) {
                return -1;
            }
            result *= RADIX;
            if (result < LONG_LIMIT + digit) {
                return -1;
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
