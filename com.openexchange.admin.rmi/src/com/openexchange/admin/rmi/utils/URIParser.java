/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.rmi.utils;

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
            } catch (final URISyntaxException e) {
                return false;
            }
        }
        int port;
        try {
            port = Integer.parseInt(matcher.group(3));
        } catch (final NumberFormatException e) {
            return false;
        }
        if (port < 0 || port > 65535) {
            return false;
        }
        try {
            new URI(matcher.group(1), null, matcher.group(2), port, null, null, null);
            return true;
        } catch (final URISyntaxException e) {
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
            } catch (final URISyntaxException e) {
                return new URI(usedScheme, null, "localhost", usedPort, null, null, null);
            }
        } catch (final URISyntaxException e) {
            /*
             * Cannot sanitize
             */
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
                return defaults.getPort();
            }
            return iPort;
        } catch (final NumberFormatException e) {
            if (URIDefaults.NULL.equals(defaults)) {
                throw new URISyntaxException(input, e.getMessage());
            }
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
