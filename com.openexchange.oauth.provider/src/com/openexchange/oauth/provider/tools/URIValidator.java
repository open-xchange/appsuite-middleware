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

package com.openexchange.oauth.provider.tools;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import com.google.common.collect.ImmutableSet;


/**
 * Helper class to validate redirect URIs.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class URIValidator {

    private static final Set<String> URI_LOCALHOSTS = ImmutableSet.of("localhost", "127.0.0.1", "[::1]");

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
        } catch (@SuppressWarnings("unused") URISyntaxException e) {
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
        } catch (@SuppressWarnings("unused") URISyntaxException e) {
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
