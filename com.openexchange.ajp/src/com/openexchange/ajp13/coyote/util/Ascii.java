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

/**
 * {@link Ascii}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Ascii {

    /*
     * Character translation tables.
     */

    private static final byte[] toUpper = new byte[256];

    private static final byte[] toLower = new byte[256];

    /*
     * Character type tables.
     */

    private static final boolean[] isAlpha = new boolean[256];

    private static final boolean[] isUpper = new boolean[256];

    private static final boolean[] isLower = new boolean[256];

    private static final boolean[] isWhite = new boolean[256];

    private static final boolean[] isDigit = new boolean[256];

    /*
     * Initialize character translation and type tables.
     */

    static {
        for (int i = 0; i < 256; i++) {
            toUpper[i] = (byte) i;
            toLower[i] = (byte) i;
        }

        for (int lc = 'a'; lc <= 'z'; lc++) {
            final int uc = lc + 'A' - 'a';

            toUpper[lc] = (byte) uc;
            toLower[uc] = (byte) lc;
            isAlpha[lc] = true;
            isAlpha[uc] = true;
            isLower[lc] = true;
            isUpper[uc] = true;
        }

        isWhite[' '] = true;
        isWhite['\t'] = true;
        isWhite['\r'] = true;
        isWhite['\n'] = true;
        isWhite['\f'] = true;
        isWhite['\b'] = true;

        for (int d = '0'; d <= '9'; d++) {
            isDigit[d] = true;
        }
    }

    /**
     * Returns the upper case equivalent of the specified ASCII character.
     */

    public static int toUpper(final int c) {
        return toUpper[c & 0xff] & 0xff;
    }

    /**
     * Returns the lower case equivalent of the specified ASCII character.
     */

    public static int toLower(final int c) {
        return toLower[c & 0xff] & 0xff;
    }

    /**
     * Returns true if the specified ASCII character is upper or lower case.
     */

    public static boolean isAlpha(final int c) {
        return isAlpha[c & 0xff];
    }

    /**
     * Returns true if the specified ASCII character is upper case.
     */

    public static boolean isUpper(final int c) {
        return isUpper[c & 0xff];
    }

    /**
     * Returns true if the specified ASCII character is lower case.
     */

    public static boolean isLower(final int c) {
        return isLower[c & 0xff];
    }

    /**
     * Returns true if the specified ASCII character is white space.
     */

    public static boolean isWhite(final int c) {
        return isWhite[c & 0xff];
    }

    /**
     * Returns true if the specified ASCII character is a digit.
     */

    public static boolean isDigit(final int c) {
        return isDigit[c & 0xff];
    }

    /**
     * Parses an unsigned integer from the specified subarray of bytes.
     *
     * @param b the bytes to parse
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     * @exception NumberFormatException if the integer format was invalid
     */
    public static int parseInt(final byte[] b, int off, int len) throws NumberFormatException {
        int c;

        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }

        int n = c - '0';

        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            n = n * 10 + c - '0';
        }

        return n;
    }

    public static int parseInt(final char[] b, int off, int len) throws NumberFormatException {
        int c;

        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }

        int n = c - '0';

        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            n = n * 10 + c - '0';
        }

        return n;
    }

    /**
     * Parses an unsigned long from the specified subarray of bytes.
     *
     * @param b the bytes to parse
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     * @exception NumberFormatException if the long format was invalid
     */
    public static long parseLong(final byte[] b, int off, int len) throws NumberFormatException {
        int c;

        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }

        long n = c - '0';
        long m;

        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            m = n * 10 + c - '0';

            if (m < n) {
                // Overflow
                throw new NumberFormatException();
            } else {
                n = m;
            }
        }

        return n;
    }

    public static long parseLong(final char[] b, int off, int len) throws NumberFormatException {
        int c;

        if (b == null || len <= 0 || !isDigit(c = b[off++])) {
            throw new NumberFormatException();
        }

        long n = c - '0';
        long m;

        while (--len > 0) {
            if (!isDigit(c = b[off++])) {
                throw new NumberFormatException();
            }
            m = n * 10 + c - '0';

            if (m < n) {
                // Overflow
                throw new NumberFormatException();
            } else {
                n = m;
            }
        }

        return n;
    }

}
