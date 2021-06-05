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

package com.openexchange.ajax.multifactor.u2fclient;

import com.google.common.io.BaseEncoding;

/**
 * {@link U2FEncodings} - Util class to provide commomn encodings required for U2F
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class U2FEncodings {

    private static BaseEncoding BASE64_ENCODER = BaseEncoding.base64Url().omitPadding();
    private static BaseEncoding HEX_ENCODER    = BaseEncoding.base16().lowerCase();

    /**
     * Encodes the given data as Base64URL.
     *
     * @param data The data to encode
     * @return The encoded data
     */
    public static String encodeBase64Url(byte[] data) {
        return BASE64_ENCODER.encode(data);
    }

    /**
     * Encodes the given data as Base64URL.
     *
     * @param data The data to encode
     * @return The encoded data
     */
    public static String encodeBase64Url(String data) {
        return BASE64_ENCODER.encode(data.getBytes());
    }

    /**
     * Decodes the given base64 data
     *
     * @param data The data to decode
     * @return The decoded data
     */
    public static byte[] decodeBase64(String data) {
        return BASE64_ENCODER.decode(data);
    }

    /**
     * Decodes the given HEX data
     *
     * @param data the data to decode
     * @return The decoded data
     */
    public static byte[] decodeHex(String data) {
        return HEX_ENCODER.decode(data);
    }
}
