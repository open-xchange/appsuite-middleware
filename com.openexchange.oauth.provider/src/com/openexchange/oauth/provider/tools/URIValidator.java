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

package com.openexchange.oauth.provider.tools;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;


/**
 * Helper class to validate redirect URIs.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class URIValidator {

    private static final Set<String> URI_LOCALHOSTS = new HashSet<>();
    static  {
        URI_LOCALHOSTS.add("localhost");
        URI_LOCALHOSTS.add("127.0.0.1");
        URI_LOCALHOSTS.add("[::1]");
    }

    /**
     * Checks whether a given String is a valid redirect URI in terms of http://tools.ietf.org/html/rfc6749#section-3.1.2.
     * If the URI is a web location it's protocol is also verified to be 'https' unless the host part points to localhost.
     *
     * @param sURI The URI to validate
     * @return <code>true</code> if the URI is valid, otherwise <code>false</code>
     */
    public static boolean isValidRedirectURI(String sURI) {
        try {
            URI uri = new URI(sURI);
            if (!uri.isAbsolute()) {
                return false; // Redirect URIs must always be absolute.
            }

            String fragment = uri.getFragment();
            if (fragment != null) {
                return false; // Redirect URIs must not contain fragments.
            }

            if (uri.getScheme().equals("http")) {
                String host = uri.getHost();
                if (host == null) {
                    return false; // Redirect URIs pointing to web locations must contain a valid host part.
                }

                if (!URI_LOCALHOSTS.contains(host.toLowerCase())) {
                    return false; // Redirect URIs may only point to localhost when using plain HTTP as protocol.
                }
            }

            if (uri.getScheme().equals("https") && uri.getHost() == null) {
                return false; // Redirect URIs pointing to web locations must contain a valid host part.
            }

            return true;
        } catch (URISyntaxException e) {
            return false; // Redirect URIs must follow the syntax described in RFC 3986.
        }
    }

    /**
     * Checks equality of two URIs. Both string parameters are considered valid redirect URIs (see {@link #isValidRedirectURI(String)}).
     * URIs that have not been checked to be valid must not be passed.
     *
     * @param sURI1 URI 1
     * @param sURI2 URI 2
     * @return <code>true</code> if the URIs are equal, otherwise <code>false</code>
     */
    public static boolean urisEqual(String sURI1, String sURI2) {
        try {
            URI uri1 = new URI(sURI1).normalize();
            URI uri2 = new URI(sURI2).normalize();

            String scheme1 = uri1.getScheme();
            String scheme2 = uri2.getScheme();
            if (!scheme1.equalsIgnoreCase(scheme2)) {
                return false;
            }

            if (!stringsEqual(uri1.getHost(), uri2.getHost(), true)) {
                return false;
            }

            int port1 = uri1.getPort();
            int port2 = uri2.getPort();
            if (port1 != port2) {
                if (port1 < 0) {
                    if (scheme1.equalsIgnoreCase("http")) {
                        port1 = 80;
                    } else if (scheme1.equalsIgnoreCase("https")) {
                        port1 = 443;
                    }
                }

                if (port2 < 0) {
                    if (scheme2.equalsIgnoreCase("http")) {
                        port2 = 80;
                    } else if (scheme2.equalsIgnoreCase("https")) {
                        port2 = 443;
                    }
                }

                if (port1 != port2) {
                    return false;
                }
            }

            if (!stringsEqual(uri1.getHost(), uri2.getHost(), true)) {
                return false;
            }

            if (!stringsEqual(uri1.getPath(), uri2.getPath(), false)) {
                return false;
            }

            if (!stringsEqual(uri1.getQuery(), uri2.getQuery(), false)) {
                return false;
            }

            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static boolean stringsEqual(String str1, String str2, boolean ignoreCase) {
        if (str1 != null && str2 != null) {
            if (ignoreCase) {
                if (!str1.equalsIgnoreCase(str2)) {
                    return false;
                }
            } else {
                if (!str1.equals(str2)) {
                    return false;
                }
            }
        } else if ((str1 == null && str2 != null) || (str1 != null && str2 == null)) {
            return false;
        }

        return true;
    }

}
