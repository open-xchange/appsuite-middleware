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

package com.openexchange.tools.net;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link URIParser}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class URIParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(URIParser.class);

    // The magic is the not being there of the dot character that would allow the match to IPv4 addresses. Place here \. in the pattern to
    // break it.
    private static final Pattern IPV6_PATTERN = Pattern.compile("^(?:(?:([a-zA-Z][0-9a-zA-Z]*)://\\[)|\\[)?([0-9a-zA-Z:]*?)(?:\\]|(?:\\]:(.*)))?$");

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(?:([a-zA-Z][0-9a-zA-Z]*)://)?(.*?)(?::(.*))?$");

    private URIParser() {
        super();
    }

    /**
     * Parses specified URL string.
     *
     * @param input The URL string
     * @param defaults The defaults for parsing or {@link URIDefaults#NULL} for no defaults
     * @return The parsed URI instance
     * @throws URISyntaxException If parsing fails
     */
    public static URI parse(final String input, final URIDefaults defaults) throws URISyntaxException {
        if (null == input || 0 == input.length()) {
            return null;
        }
        Matcher matcher;
        if ((matcher = IPV6_PATTERN.matcher(input)).matches()) {
            // Nothing to do
        } else if ((matcher = IPV4_PATTERN.matcher(input)).matches()) {
            // Nothing to do
        } else {
            // Try fallback.
            return new URI(input);
        }
        final URIDefaults defs = null == defaults ? URIDefaults.NULL : defaults;
        final int port = parsePort(input, matcher.group(3), defs);
        final String scheme = matcher.group(1);
        final int usedPort = applyDefault(port, scheme, defs);
        final String usedScheme = applyDefault(scheme, port, defs);
        return new URI(usedScheme, null, matcher.group(2), usedPort, null, null, null);
    }

    /**
     * Checks if specified input is a valid URI.
     *
     * @param input The input to check
     * @return <code>true</code> if specified input is a valid URI; otherwise <code>false</code>
     */
    public static boolean isValid(final String input) {
        if (null == input || 0 == input.length()) {
            return false;
        }
        Matcher matcher;
        if ((matcher = IPV6_PATTERN.matcher(input)).matches()) {
            // Nothing to do
        } else if ((matcher = IPV4_PATTERN.matcher(input)).matches()) {
            // Nothing to do
        } else {
            // Try to parse
            try {
                new URI(input);
                return true;
            } catch (URISyntaxException e) {
                return false;
            }
        }
        int port;
        try {
            port = Integer.parseInt(matcher.group(3));
        } catch (NumberFormatException e) {
            return false;
        }
        if (port < 0 || port > 65535) {
            return false;
        }
        try {
            new URI(matcher.group(1), null, matcher.group(2), port, null, null, null);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Tries to sanitize specified broken URI string.
     *
     * @param input The broken URI string
     * @param defaults The defaults for parsing
     * @return The sanitized URI or <code>null</code> if not able to sanitize
     */
    public static URI sanitize(final String input, final URIDefaults defaults) {
        if (null == input || 0 == input.length()) {
            return null;
        }
        try {
            Matcher matcher;
            if ((matcher = IPV6_PATTERN.matcher(input)).matches()) {
                // Nothing to do
            } else if ((matcher = IPV4_PATTERN.matcher(input)).matches()) {
                // Nothing to do
            } else {
                /*
                 * Unknown pattern. Cannot sanitize
                 */
                return null;
            }
            final URIDefaults defs = null == defaults ? URIDefaults.NULL : defaults;
            final int port = parsePort(input, matcher.group(3), defs);
            final String scheme = matcher.group(1);
            final int usedPort = applyDefault(port, scheme, defs);
            final String usedScheme = applyDefault(scheme, port, defs);
            try {
                return new URI(usedScheme, null, matcher.group(2), usedPort, null, null, null);
            } catch (URISyntaxException e) {
                return new URI(usedScheme, null, "localhost", usedPort, null, null, null);
            }
        } catch (URISyntaxException e) {
            /*
             * Cannot sanitize
             */
            LOG.warn("Couldn't sanitize URI: {}", input, e);
            return null;
        }
    }

    private static int parsePort(final String input, final String port, final URIDefaults defaults) throws URISyntaxException {
        if (null == port) {
            return -1;
        }
        try {
            final int iPort = Integer.parseInt(port);
            /*
             * A valid port value is between 0 and 65535
             */
            if (iPort < 0 || iPort > 65535) {
                if (URIDefaults.NULL.equals(defaults)) {
                    throw new URISyntaxException(input, "A valid port value is between 0 and 65535, but is: " + port);
                }
                LOG.warn("Invalid port: {}", port);
                return defaults.getPort();
            }
            return iPort;
        } catch (NumberFormatException e) {
            if (URIDefaults.NULL.equals(defaults)) {
                throw new URISyntaxException(input, e.getMessage());
            }
            LOG.warn("Couldn't parse port", e);
            return defaults.getPort();
        }
    }

    private static int applyDefault(final int port, final String scheme, final URIDefaults defaults) {
        if (-1 == port) {
            if (null != defaults.getSSLProtocol() && defaults.getSSLProtocol().equals(scheme)) {
                return defaults.getSSLPort();
            }
            return defaults.getPort();
        }
        return port;
    }

    private static String applyDefault(final String scheme, final int port, final URIDefaults defaults) {
        if (null == scheme) {
            if (defaults.getSSLPort() == port) {
                return defaults.getSSLProtocol();
            }
            return defaults.getProtocol();
        }
        return scheme;
    }
}
