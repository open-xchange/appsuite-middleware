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

package com.openexchange.tools.webdav;

import com.openexchange.java.Strings;

/**
 * Encapsulates the basically parsed value of an HTTP <code>Authorization</code> header.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AuthorizationHeader {

    private final String value;

    private final String scheme;

    private final String authString;


    private AuthorizationHeader(String value, String scheme, String authString) {
        super();
        this.value = value;
        this.scheme = scheme;
        this.authString = authString;
    }

    /**
     * Parses the given header value, e.g. <code>Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==</code>
     * and creates a new {@link AuthorizationHeader} instance.
     *
     * @param headerValue The value
     * @return A new {@link AuthorizationHeader} instance
     * @throws IllegalArgumentException If the passed value was <code>null</code> or generally malformed.
     */
    public static AuthorizationHeader parse(String headerValue) throws IllegalArgumentException {
        if (headerValue == null) {
            throw new IllegalArgumentException("Invalid authorization header: null");
        }

        String scheme = com.openexchange.tools.servlet.http.Authorization.extractAuthScheme(headerValue);
        if (Strings.isEmpty(scheme)) {
            throw new IllegalArgumentException("Invalid authorization header: " + headerValue);
        }

        scheme = scheme.trim();

        try {
            String authString = headerValue.substring(headerValue.indexOf(scheme) + scheme.length() + 1);
            return new AuthorizationHeader(headerValue, scheme, authString);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid authorization header: " + headerValue);
        }
    }

    /**
     * Parses the given header value, e.g. <code>Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==</code>
     * and creates a new {@link AuthorizationHeader} instance.
     *
     * @param headerValue The value
     * @return A new {@link AuthorizationHeader} instance or <code>null</code> if the passed value was <code>null</code> or generally malformed
     */
    public static AuthorizationHeader parseSafe(String headerValue) {
        try {
            AuthorizationHeader header = parse(headerValue);
            return header;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    /**
     * Gets the raw header value as it was passed to the {@link #parse(String)} or {@link #parseSafe(String)}
     * method.
     *
     * @return The value
     */
    public String getRawValue() {
        return value;
    }


    /**
     * Gets the authorization scheme.
     *
     * @return The scheme
     */
    public String getScheme() {
        return scheme;
    }


    /**
     * Gets the string part that follows the scheme, i.e. for <code>Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==</code>
     * <code>dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==</code> would be returned.
     *
     * @return The auth string
     */
    public String getAuthString() {
        return authString;
    }


}
