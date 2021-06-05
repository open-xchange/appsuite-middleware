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


package com.openexchange.xing.exception;

import java.io.BufferedReader;
import java.io.IOException;
import com.openexchange.java.Streams;

/**
 * Indicates there was trouble parsing a response from Xing.
 */
public class XingParseException extends XingException {
    private static final long serialVersionUID = 1L;

    /*
     * Takes a BufferedReader so it can be reset back to the beginning and read
     * again into the body variable.
     */
    public XingParseException(BufferedReader reader) {
        super("failed to parse: " + stringifyBody(reader));
    }

    public static String stringifyBody(BufferedReader reader) {
        if (null == reader) {
            return "";
        }

        try {
            reader.reset();
        } catch (IOException ioe) {
            // Ignore
        }

        try {
            String str = Streams.reader2string(reader);
            return null == str ? "" : str;
        } catch (IOException e) {
            // Ignore
        }
        return "";
    }

    public XingParseException(String message) {
        super(message);
    }
}
