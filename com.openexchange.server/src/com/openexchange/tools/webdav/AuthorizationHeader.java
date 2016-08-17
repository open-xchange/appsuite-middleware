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
