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

package com.openexchange.net;

import java.util.regex.Pattern;

/**
 * {@link IPAddressUtil}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IPAddressUtil {

    private static final Pattern SPLIT = Pattern.compile("\\.");

    private final static int INADDR4SZ = 4;

    private final static int INADDR16SZ = 16;

    private final static int INT16SZ = 2;

    /**
     * Converts IPv4 address in its textual presentation form into its numeric binary form.
     *
     * @param src a String representing an IPv4 address in standard format
     * @return a byte array representing the IPv4 numeric address
     */
    public static byte[] textToNumericFormatV4(final String src) {
        if (src.length() == 0) {
            return null;
        }

        final byte[] res = new byte[INADDR4SZ];
        String[] s = SPLIT.split(src, -1);
        long val;
        try {
            switch (s.length) {
            case 1:
                /*
                 * When only one part is given, the value is stored directly in the network address without any byte rearrangement.
                 */

                val = parseUnsignedLong(s[0]);
                if (val < 0 || val > 0xffffffffL) {
                    return null;
                }
                res[0] = (byte) ((val >> 24) & 0xff);
                res[1] = (byte) (((val & 0xffffff) >> 16) & 0xff);
                res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
                res[3] = (byte) (val & 0xff);
                break;
            case 2:
                /*
                 * When a two part address is supplied, the last part is interpreted as a 24-bit quantity and placed in the right most three
                 * bytes of the network address. This makes the two part address format convenient for specifying Class A network addresses
                 * as net.host.
                 */

                val = parseUnsignedInteger(s[0]);
                if (val < 0 || val > 0xff) {
                    return null;
                }
                res[0] = (byte) (val & 0xff);
                val = parseUnsignedInteger(s[1]);
                if (val < 0 || val > 0xffffff) {
                    return null;
                }
                res[1] = (byte) ((val >> 16) & 0xff);
                res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
                res[3] = (byte) (val & 0xff);
                break;
            case 3:
                /*
                 * When a three part address is specified, the last part is interpreted as a 16-bit quantity and placed in the right most
                 * two bytes of the network address. This makes the three part address format convenient for specifying Class B net- work
                 * addresses as 128.net.host.
                 */
                for (int i = 0; i < 2; i++) {
                    val = parseUnsignedInteger(s[i]);
                    if (val < 0 || val > 0xff) {
                        return null;
                    }
                    res[i] = (byte) (val & 0xff);
                }
                val = parseUnsignedInteger(s[2]);
                if (val < 0 || val > 0xffff) {
                    return null;
                }
                res[2] = (byte) ((val >> 8) & 0xff);
                res[3] = (byte) (val & 0xff);
                break;
            case 4:
                /*
                 * When four parts are specified, each is interpreted as a byte of data and assigned, from left to right, to the four bytes
                 * of an IPv4 address.
                 */
                for (int i = 0; i < 4; i++) {
                    val = parseUnsignedInteger(s[i]);
                    if (val < 0 || val > 0xff) {
                        return null;
                    }
                    res[i] = (byte) (val & 0xff);
                }
                break;
            default:
                return null;
            }
        } catch (final NumberFormatException e) {
            return null;
        }
        return res;
    }

    /**
     * Convert IPv6 presentation level address to network order binary form. credit: Converted from C code from Solaris 8 (inet_pton) Any
     * component of the string following a per-cent % is ignored.
     *
     * @param src a String representing an IPv6 address in textual format
     * @return a byte array representing the IPv6 numeric address
     */
    public static byte[] textToNumericFormatV6(final String src) {
        // Shortest valid string is "::", hence at least 2 chars
        if (src.length() < 2) {
            return null;
        }

        int colonp;
        char ch;
        boolean saw_xdigit;
        int val;
        final byte[] dst = new byte[INADDR16SZ];

        int srcb_length = src.length();
        final int pc = src.indexOf('%');
        if (pc == srcb_length - 1) {
            return null;
        }

        if (pc != -1) {
            srcb_length = pc;
        }

        colonp = -1;
        int i = 0, j = 0;
        /* Leading :: requires some special handling. */
        if (src.charAt(i) == ':') {
            if (src.charAt(++i) != ':') {
                return null;
            }
        }
        int curtok = i;
        saw_xdigit = false;
        val = 0;
        while (i < srcb_length) {
            ch = src.charAt(i++);
            final int chval = digit(ch, 16);
            if (chval != -1) {
                val <<= 4;
                val |= chval;
                if (val > 0xffff) {
                    return null;
                }
                saw_xdigit = true;
                continue;
            }
            if (ch == ':') {
                curtok = i;
                if (!saw_xdigit) {
                    if (colonp != -1) {
                        return null;
                    }
                    colonp = j;
                    continue;
                } else if (i == srcb_length) {
                    return null;
                }
                if (j + INT16SZ > INADDR16SZ) {
                    return null;
                }
                dst[j++] = (byte) ((val >> 8) & 0xff);
                dst[j++] = (byte) (val & 0xff);
                saw_xdigit = false;
                val = 0;
                continue;
            }
            if (ch == '.' && ((j + INADDR4SZ) <= INADDR16SZ)) {
                final String ia4 = src.substring(curtok, srcb_length);
                /* check this IPv4 address has 3 dots, ie. A.B.C.D */
                int dot_count = 0, index = 0;
                while ((index = ia4.indexOf('.', index)) != -1) {
                    dot_count++;
                    index++;
                }
                if (dot_count != 3) {
                    return null;
                }
                final byte[] v4addr = textToNumericFormatV4(ia4);
                if (v4addr == null) {
                    return null;
                }
                for (int k = 0; k < INADDR4SZ; k++) {
                    dst[j++] = v4addr[k];
                }
                saw_xdigit = false;
                break; /* '\0' was seen by inet_pton4(). */
            }
            return null;
        }
        if (saw_xdigit) {
            if (j + INT16SZ > INADDR16SZ) {
                return null;
            }
            dst[j++] = (byte) ((val >> 8) & 0xff);
            dst[j++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            final int n = j - colonp;

            if (j == INADDR16SZ) {
                return null;
            }
            for (i = 1; i <= n; i++) {
                dst[INADDR16SZ - i] = dst[colonp + n - i];
                dst[colonp + n - i] = 0;
            }
            j = INADDR16SZ;
        }
        if (j != INADDR16SZ) {
            return null;
        }
        final byte[] newdst = convertFromIPv4MappedAddress(dst);
        if (newdst == null) {
            return dst;
        }
        return newdst;
    }

    /**
     * @param src a String representing an IPv4 address in textual format
     * @return a boolean indicating whether src is an IPv4 literal address
     */
    public static boolean isIPv4LiteralAddress(final String src) {
        return textToNumericFormatV4(src) != null;
    }

    /**
     * @param src a String representing an IPv6 address in textual format
     * @return a boolean indicating whether src is an IPv6 literal address
     */
    public static boolean isIPv6LiteralAddress(final String src) {
        return textToNumericFormatV6(src) != null;
    }

    /**
     * Convert IPv4-Mapped address to IPv4 address. Both input and returned value are in network order binary form.
     *
     * @param src a String representing an IPv4-Mapped address in textual format
     * @return a byte array representing the IPv4 numeric address
     */
    public static byte[] convertFromIPv4MappedAddress(final byte[] addr) {
        if (isIPv4MappedAddress(addr)) {
            final byte[] newAddr = new byte[INADDR4SZ];
            System.arraycopy(addr, 12, newAddr, 0, INADDR4SZ);
            return newAddr;
        }
        return null;
    }

    /**
     * Converts specified IPv4 to a mapped IPv6.
     *
     * @param ipv4 The IPv4 string representation
     * @return The mapped IPv6 or <code>null</code>
     */
    public static byte[] convertIPv4ToMappedIPv6(final String ipv4) {
        final String[] octets = SPLIT.split(ipv4, 0);
        if (octets.length != INADDR4SZ) {
            return null;
        }
        final byte[] octetBytes = new byte[INADDR4SZ];
        for (int i = 0; i < INADDR4SZ; ++i) {
            octetBytes[i] = (byte) parseUnsignedInteger(octets[i]);
        }
        return convertIPv4ToMappedIPv6(octetBytes);
    }

    /**
     * Converts specified IPv4 to a mapped IPv6.
     *
     * @param octets The IPv4 bytes
     * @return The mapped IPv6 or <code>null</code>
     */
    public static byte[] convertIPv4ToMappedIPv6(final byte[] octets) {
        if (octets.length != INADDR4SZ) {
            return null;
        }
        final byte ipv4asIpV6addr[] = new byte[INADDR16SZ];
        ipv4asIpV6addr[10] = (byte) 0xff;
        ipv4asIpV6addr[11] = (byte) 0xff;
        ipv4asIpV6addr[12] = octets[0];
        ipv4asIpV6addr[13] = octets[1];
        ipv4asIpV6addr[14] = octets[2];
        ipv4asIpV6addr[15] = octets[3];
        return ipv4asIpV6addr;
    }

    /**
     * Utility routine to check if the InetAddress is an IPv4 mapped IPv6 address.
     *
     * @return a <code>boolean</code> indicating if the InetAddress is an IPv4 mapped IPv6 address; or false if address is IPv4 address.
     */
    private static boolean isIPv4MappedAddress(final byte[] addr) {
        if (addr.length < INADDR16SZ) {
            return false;
        }
        if ((addr[0] == 0x00) && (addr[1] == 0x00) && (addr[2] == 0x00) && (addr[3] == 0x00) && (addr[4] == 0x00) && (addr[5] == 0x00) && (addr[6] == 0x00) && (addr[7] == 0x00) && (addr[8] == 0x00) && (addr[9] == 0x00) && (addr[10] == (byte) 0xff) && (addr[11] == (byte) 0xff)) {
            return true;
        }
        return false;
    }

    private static long parseUnsignedLong(final String s) {
        if (s == null || s.length() <= 0 || s.charAt(0) == '-') {
            return -1L;
        }

        if (s.equals("0")) {
            return 0;
        }

        if (s.startsWith("0x")) {
            // Assume hexadecimal
            return parseUnsignedLong(s.substring(2), 16);
        }
        if (s.startsWith("0")) {
            // Assume octal
            return parseUnsignedLong(s.substring(1), 8);
        }
        return parseUnsignedLong(s, 10);
    }

    /**
     * Parses the string argument as a signed decimal <code>long</code>. The characters in the string must all be decimal digits.
     * <p>
     * Note that neither the character <code>L</code> (<code>'&#92;u004C'</code>) nor <code>l</code> (<code>'&#92;u006C'</code>) is
     * permitted to appear at the end of the string as a type indicator, as would be permitted in Java programming language source code.
     *
     * @param s A <code>String</code> containing the <code>long</code> representation to be parsed
     * @return The <code>long</code> represented by the argument in decimal or <code>-1</code> if the string does not contain a parsable
     *         <code>long</code>.
     */
    private static long parseUnsignedLong(final String s, int radix) {
        final int max = s.length();
        if (max <= 0) {
            return -1;
        }
        long result = 0;
        int i = 0;

        final long limit = -Long.MAX_VALUE;
        final long multmin = limit / radix;
        int digit;

        if (i < max) {
            digit = digit(s.charAt(i++), radix);
            if (digit < 0) {
                return -1L;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = digit(s.charAt(i++), radix);
            if (digit < 0) {
                return -1L;
            }
            if (result < multmin) {
                return -1L;
            }
            result *= radix;
            if (result < limit + digit) {
                return -1L;
            }
            result -= digit;
        }
        return -result;
    }

    private static long parseUnsignedInteger(final String s) {
        if (s == null || s.length() <= 0 || s.charAt(0) == '-') {
            return -1L;
        }

        if (s.equals("0")) {
            return 0;
        }

        if (s.startsWith("0x")) {
            // Assume hexadecimal
            return parseUnsignedInteger(s.substring(2), 16);
        }
        if (s.startsWith("0")) {
            // Assume octal
            return parseUnsignedInteger(s.substring(1), 8);
        }
        return parseUnsignedInteger(s, 10);
    }

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    private static final int parseUnsignedInteger(final String s, int radix) {
        final int max = s.length();
        if (max <= 0) {
            return -1;
        }
        int result = 0;
        int i = 0;

        final int limit = -Integer.MAX_VALUE;
        final int multmin = limit / radix;
        int digit;

        if (i < max) {
            digit = digit(s.charAt(i++), radix);
            if (digit < 0) {
                return -1;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = digit(s.charAt(i++), radix);
            if (digit < 0) {
                return -1;
            }
            if (result < multmin) {
                return -1;
            }
            result *= radix;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

    private static int digit(char c, int radix) {
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
            return radix < 10 ? -1 : 8;
        case '9':
            return radix < 10 ? -1 : 9;
        case 'a':
            return radix < 16 ? -1 : 10;
        case 'b':
            return radix < 16 ? -1 : 11;
        case 'c':
            return radix < 16 ? -1 : 12;
        case 'd':
            return radix < 16 ? -1 : 13;
        case 'e':
            return radix < 16 ? -1 : 14;
        case 'f':
            return radix < 16 ? -1 : 15;
        default:
            return -1;
        }
    }

}
