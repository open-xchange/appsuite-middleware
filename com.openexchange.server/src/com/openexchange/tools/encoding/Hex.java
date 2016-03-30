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

package com.openexchange.tools.encoding;

/**
 * Conversion methods for strings with hexadezimal coded bytes.
 *
 * @author <a href="mailto:m.klein@open-xchange.com">Marcus Klein</a>
 */
public class Hex {

    private Hex() {
        super();
    }

    /**
     * Converts the given byte array to a string with hexadezimal coded bytes. Each byte is represented with its two digit hexadezimal
     * presentation. E.g. dezimal 10 will be represented with hexadezimal 0a. The sign of a byte is used as 8th bit therefore all negative
     * values get hexadezimal values from 80 to ff.
     *
     * @param b The byte array to convert.
     * @return A string with hexadezimal coded bytes.
     */
    public static String toHexString(final byte[] b) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            if (b[i] < 0x10 && b[i] >= 0) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b[i] < 0 ? 256 + b[i] : b[i]));
        }
        return sb.toString();
    }

    public static String toHex(final byte b) {
        final StringBuffer sb = new StringBuffer(2);
        if (b >= 0 && b < 0x10) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(b < 0 ? 256 + b : b));
        return sb.toString();
    }

    /**
     * Converts the hexadezimal coded bytes in the given string to a byte array.
     *
     * @param hex
     * @return
     */
    public static byte[] toByteArray(final String hex) throws NumberFormatException {
        final int length = hex.length() >> 1;
        final byte[] retval = new byte[length];
        for (int i = 0; i < length; i++) {
            retval[i] = toByte(hex.substring(i << 1, (i << 1) + 2));
        }
        return retval;
    }

    public static byte toByte(final String hex) {
        final int value = Integer.parseInt(hex, 16);
        return (byte) (value < 128 ? value : value - 256);
    }
}
