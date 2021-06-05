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

package com.openexchange.webdav.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;


/**
 * {@link ApacheURLDecoder}
 *
 * A URLDecoder that recovers for some of Apaches quirks.
 * For example Apache likes to decode plus signs itself even when told not to do so, so it effectively only
 * decodes plus signs.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ApacheURLDecoder {

    private static final Pattern SPLIT = Pattern.compile("\\+");

    /**
     * @param string The String to decode
     * @param encoding The encoding to assume for special characters
     * @return
     * @throws UnsupportedEncodingException
     */
    public String decode(String string, String encoding) throws UnsupportedEncodingException {
        String[] chunks = SPLIT.split(string, 0);
        StringBuilder decoded = new StringBuilder(string.length());
        boolean endsWithPlus = string.endsWith("+");
        for (int i = 0; i < chunks.length; i++) {
            String chunk = chunks[i];
            decoded.append(URLDecoder.decode(chunk, encoding));
            if (i != chunks.length - 1 || endsWithPlus) {
                decoded.append('+');
            }
        }

        return decoded.toString();
    }

    // Can be turned into an interface (maybe even a service?) when we want to support different http servers

}
