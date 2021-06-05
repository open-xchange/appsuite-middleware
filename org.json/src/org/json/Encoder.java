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

package org.json;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

/**
 * {@link Encoder} - Helper class used for efficient encoding of JSON String values (including JSON field names) into Strings or UTF-8 byte
 * arrays.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Encoder {

    private static final Encoder INSTANCE = new Encoder();

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    public static Encoder getInstance() {
        return INSTANCE;
    }

    private final JsonStringEncoder encoder;

    /**
     * Initializes a new {@link Encoder}.
     */
    private Encoder() {
        super();
        encoder = JsonStringEncoder.getInstance();
    }

    /**
     * Method that will quote text contents using JSON standard quoting, and return results as a character array
     */
    public char[] quoteAsString(String input) {
        return encoder.quoteAsString(input);
    }

    /**
     * Will quote given JSON String value using standard quoting, encode results as UTF-8, and return result as a byte array.
     */
    public byte[] quoteAsUTF8(String text) {
        return encoder.quoteAsUTF8(text);
    }

    /**
     * Will encode given String as UTF-8 (without any quoting), return resulting byte array.
     */
    public byte[] encodeAsUTF8(String text) {
        return encoder.encodeAsUTF8(text);
    }

}
