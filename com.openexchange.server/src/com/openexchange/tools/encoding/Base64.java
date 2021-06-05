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

import com.openexchange.java.Charsets;

/**
 * Central entry point for a base64 en/decoder.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.com">Martin Kauss</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Base64 {

    /**
     * Prevent instantiation
     */
    private Base64() {
        super();
    }

    /**
     * Encodes some binary data into base64.
     *
     * @param bytes binary data to encode.
     * @return a string containing the encoded data.
     */
    public static String encode(final byte[] bytes) {
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Converts the string using UTF-8 character set encoding and encodes then into base64.
     *
     * @param source string the encode.
     * @return the base64 data for the string.
     */
    public static String encode(final String source) {
        return encode(Charsets.getBytes(source, Charsets.UTF_8));
    }

    /**
     * Decodes some base64 data.
     *
     * @param source string to decode.
     * @return the decoded data.
     */
    public static byte[] decode(final String source) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(Charsets.toAsciiBytes(source));
    }
}
