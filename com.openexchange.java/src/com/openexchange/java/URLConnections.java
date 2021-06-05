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

package com.openexchange.java;

import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link URLConnections} - Utility class for {@link URLConnection}s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class URLConnections {

    /**
     * Prevent instantiation.
     */
    private URLConnections() {
        super();
    }

    private static final Pattern CHARSET_DETECTION = Pattern.compile("charset= *([a-zA-Z-0-9_]+)(;|$)");

    /**
     * Tries to determine the charset from specified URL connection.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">
     * <b>Note</b>: The connection is required to be connected; that is {@link URLConnection#connect()} has been called.
     * </div>
     *
     * @param connection The URL connection to retrieve the charset from
     * @param def The default charset to return
     * @return The charset or <code>def</code>
     */
    public static String getCharsetFrom(URLConnection connection, String def) {
        if (null == connection) {
            return null;
        }

        String mimeType = connection.getContentType();
        if (null == mimeType) {
            return null;
        }

        Pattern charsetPattern = CHARSET_DETECTION;
        Matcher m = charsetPattern.matcher(mimeType);
        return m.find() ? m.group(1) : def;
    }

}
