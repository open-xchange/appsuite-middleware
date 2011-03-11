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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
public class URIParser {

    // The magic is the not being there of the dot character that would allow the match to IPv4 addresses. Place here \. in the pattern to
    // break it.
    private static final Pattern IPV6_PATTERN = Pattern.compile("^(?:(?:([a-zA-Z][0-9a-zA-Z]*)://\\[)|\\[)?([0-9a-zA-Z:]*?)(?:\\]|(?:\\]:(.*)))?$");

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(?:([a-zA-Z][0-9a-zA-Z]*)://)?(.*?)(?::(.*))?$");

    private URIParser() {
        super();
    }

    public static final URI parse(String s) throws URISyntaxException {
        return parse(s, URIDefaults.NULL);
    }

    public static final URI parse(String s, URIDefaults defaults) throws URISyntaxException {
        Matcher matcher6 = IPV6_PATTERN.matcher(s);
        Matcher matcher4 = IPV4_PATTERN.matcher(s);
        final Matcher matcher;
        if (matcher6.matches()) {
            matcher = matcher6;
        } else if (matcher4.matches()) {
            matcher = matcher4;
        } else {
            // Try fallback.
            return new URI(s);
        }
        final int port = parsePort(matcher.group(3));
        final String scheme = matcher.group(1);
        final int usedPort = applyDefault(port, scheme, defaults);
        final String usedScheme = applyDefault(scheme, port, defaults);
        return new URI(usedScheme, null, matcher.group(2), usedPort, null, null, null);
    }

    private static final int parsePort(String port) {
        int retval;
        if (null != port) {
            retval = Integer.parseInt(port);
        } else {
            retval = -1;
        }
        return retval;
    }

    private static final int applyDefault(int port, String scheme, URIDefaults defaults) {
        if (-1 == port) {
            if (null != defaults.getSSLProtocol() && defaults.getSSLProtocol().equals(scheme)) {
                return defaults.getSSLPort();
            }
            return defaults.getPort();
        }
        return port;
    }

    private static final String applyDefault(String scheme, int port, URIDefaults defaults) {
        if (null == scheme) {
            if (defaults.getSSLPort() == port) {
                return defaults.getSSLProtocol();
            }
            return defaults.getProtocol();
        }
        return scheme;
    }
}
