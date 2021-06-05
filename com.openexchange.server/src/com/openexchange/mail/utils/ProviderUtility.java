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

package com.openexchange.mail.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;

/**
 * {@link ProviderUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ProviderUtility {

    /**
     * Initializes a new {@link ProviderUtility}.
     */
    private ProviderUtility() {
        super();
    }

    /**
     * Turns given server URL to a String; e.g. <code>"mail.company.org:143"</code>.
     *
     * @param serverUrl The server URL
     * @param defaultPort The default port to use if server URL does not specify a port
     * @return The server URL; e.g. <code>"mail.company.org:143"</code>
     * @throws OXException if the server URL can not be parsed.
     */
    public static String toSocketAddrString(String serverUrl, int defaultPort) throws OXException {
        final URI uri;
        try {
            uri = URIParser.parse(serverUrl, new URIDefaults() {
                @Override
                public String getProtocol() {
                    return null;
                }
                @Override
                public String getSSLProtocol() {
                    return null;
                }
                @Override
                public int getPort() {
                    return defaultPort;
                }
                @Override
                public int getSSLPort() {
                    return defaultPort;
                }});
        } catch (URISyntaxException e) {
            throw MailAccountExceptionCodes.URI_PARSE_FAILED.create(e, serverUrl);
        }
        return new StringBuilder(uri.getHost()).append(':').append(uri.getPort()).toString();
    }

    /**
     * Extracts the protocol from specified server URL:<br>
     * <code>(&lt;protocol&gt;://)?(&lt;host&gt;)(:&lt;port&gt;)?</code>
     *
     * @param serverUrl The server URL
     * @param fallback The fallback protocol if URL does not contain a protocol
     * @return Extracted protocol or <code>fallback</code> parameter
     */
    public static String extractProtocol(String serverUrl, String fallback) {
        if (serverUrl == null) {
            return fallback;
        }
        final int len = serverUrl.length();
        String protocol = null;
        /*
         * Parse protocol out of URL
         */
        final int pos = serverUrl.indexOf(':');
        if (pos <= 0) {
            return fallback;
        }
        char c = '\0';
        for (int i = pos; (null == protocol) && (i < len) && ((c = serverUrl.charAt(i)) != '/'); i++) {
            if ((c == ':') && ((c = serverUrl.charAt(i + 1)) == '/') && ((c = serverUrl.charAt(i + 2)) == '/')) {
                final String s = serverUrl.substring(0, i).toLowerCase(Locale.ENGLISH);
                if (isValidProtocol(s)) {
                    protocol = s;
                }
            }
        }
        if (null == protocol) {
            return fallback;
        }
        return protocol;
    }

    private static boolean isValidProtocol(String protocol) {
        final int len = protocol.length();
        if (len < 1) {
            return false;
        }
        char c = protocol.charAt(0);
        if (!Character.isLetter(c)) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            c = protocol.charAt(i);
            if (!Character.isLetterOrDigit(c) && (c != '.') && (c != '+') && (c != '-')) {
                return false;
            }
        }
        return true;
    }
}
