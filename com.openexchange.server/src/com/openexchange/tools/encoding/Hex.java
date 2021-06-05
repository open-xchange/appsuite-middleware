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
