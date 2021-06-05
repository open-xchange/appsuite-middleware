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

package org.glassfish.grizzly.http.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * {@link JSessionDomainEncoder} - Generates an <code>application/x-www-form-urlencoded</code> versions of domain strings that additionally have the safe
 * character "." and "-" replaced by the according URL-Encoding.
 *
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class JSessionDomainEncoder {

    /**
     * Initializes a new {@link JSessionDomainEncoder}.
     */
    private JSessionDomainEncoder() {
        super();
    }

    /**
     * Generates an application/x-www-form-urlencoded version of specified domain string that additionally has the safe character "." and
     * "-" replaced by the according URL-Encoding.
     *
     * @param domain The domain to encode
     * @return The URL-encoded text that additionally has dots and dashes replaced by their URLEncodings
     * @throws IllegalStateException if the running Java platform doesn't support the iso-8859-1 encoding which is mandatory since 1.4.2
     */
    public static String urlEncode(final String domain) {
        try {
            return replaceDotsAndDashes(URLEncoder.encode(domain, "iso-8859-1"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Every implementation of the Java platform is required to support the iso-8859-1 encoding", e);
        }
    }

    private static String replaceDotsAndDashes(String str) {
        if (null == str) {
            return null;
        }

        int length = str.length();
        if (length == 0) {
            return str;
        }

        StringBuilder sb = null;
        for (int i = 0, k = length; k-- > 0; i++) {
            char ch = str.charAt(i);
            if (ch == '.') {
                if (null == sb) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(str, 0, i);
                    }
                }
                sb.append("%2E");
            } else if (ch == '-') {
                if (null == sb) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(str, 0, i);
                    }
                }
                sb.append("%2D");
            } else {
                // Allowed character
                if (null != sb) {
                    sb.append(ch);
                }
            }
        }

        return null == sb ? str : sb.toString();
    }

}
