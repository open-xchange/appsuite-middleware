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

package com.openexchange.share.servlet.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.tools.encoding.URLCoder;

/**
 * Holds a relative redirect location to the login page.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LoginLocation {

    /**
     * The default allowed attributes consisting of:
     * <ul>
     * <li><code>"login_name"</code></li>
     * <li><code>"login_type"</code></li>
     * <li><code>"share"</code></li>
     * <li><code>"target"</code></li>
     * </ul>
     */
    public static final Collection<String> DEFAULT_ALLOWED_ATTRIBUTES = Collections.unmodifiableCollection(Arrays.asList("login_name", "login_type", "share", "target"));

    /**
     * Builds the redirect location using specified token
     *
     * @param token The token
     * @param location The associated login location
     * @param allowedAttributes Specifies those attributes kept in given <code>LoginLocation</code> instance that are allowed to be passed to client
     * @return The redirect location
     */
    public static String buildRedirectWith(String token, LoginLocation location, Collection<String> allowedAttributes) {
        StringBuilder sb = new StringBuilder(96);
        sb.append(ShareRedirectUtils.getLoginLink()).append("#!");
        sb.append("&token=").append(token);

        // Add attributes
        if (null != allowedAttributes) {
            Map<String, String> attributes = location.asMap();
            for (String allowedAttribute : allowedAttributes) {
                if (false == Strings.isEmpty(allowedAttribute)) {
                    String value = attributes.get(allowedAttribute);
                    if (false == Strings.isEmpty(value)) {
                        sb.append('&').append(allowedAttribute).append('=').append(value);
                    }
                }
            }
        }

        return sb.toString();
    }


    // ----------------------------------------------------------------------------------------------------------------------------

    private final Map<String, String> parameters;

    /**
     * Initializes a new {@link LoginLocation}
     */
    public LoginLocation() {
        super();
        parameters = new LinkedHashMap<String, String>(8);
    }

    /**
     * Appends the login type suitable for the supplied authentication mode.
     *
     * @param authentication The authentication mode
     * @return The builder
     */
    public LoginLocation loginType(AuthenticationMode authentication) {
        switch (authentication) {
            case GUEST:
                return loginType(LoginType.GUEST);
            case GUEST_PASSWORD:
                return loginType(LoginType.GUEST_PASSWORD);
            case ANONYMOUS_PASSWORD:
                return loginType(LoginType.ANONYMOUS_PASSWORD);
            default:
                throw new UnsupportedOperationException("No login type for " + authentication);
        }
    }

    /**
     * Appends the login type suitable for the supplied identifier.
     *
     * @param loginType The login type to set
     * @return The builder
     */
    public LoginLocation loginType(LoginType loginType) {
        return parameter("login_type", loginType.getId());
    }

    /**
     * Appends the (base) token of the accessed share.
     *
     * @param token The share token to append
     * @return The builder
     */
    public LoginLocation share(String token) {
        return parameter("share", token);
    }

    /**
     * Appends the path to a specific share target.
     *
     * @param targetPath The share target path to append, or <code>null</code> if not specified
     * @return The builder
     */
    public LoginLocation target(ShareTargetPath targetPath) {
        return null != targetPath ? parameter("target", targetPath.get()) : this;
    }

    /**
     * Appends a message to pass to the client along with the redirect location.
     *
     * @param type The message type
     * @param message The message
     * @return The builder
     */
    public LoginLocation message(MessageType type, String message) {
        parameter("message_type", type.toString());
        parameter("message", message);
        return this;
    }

    /**
     * Sets the login name, i.e. the guest users email address.
     *
     * @param name The login name
     * @return
     */
    public LoginLocation loginName(String name) {
        return parameter("login_name", name);
    }

    /**
     * Adds an additional parameter to the builder instance.
     *
     * @param name The parameter name
     * @param value The parameter value
     * @return The builder
     */
    public LoginLocation parameter(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Gets the map view for this login location
     *
     * @return The parameters map
     */
    public Map<String, String> asMap() {
        return parameters;
    }

    /**
     * Builds and returns the relative redirect location, ready to use in the <code>Location</code> header of a HTTP response.
     *
     * @return The built redirect URL
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append(ShareRedirectUtils.getLoginLink()).append("#!");
        for (Entry<String, String> entry : parameters.entrySet()) {
            sb.append('&').append(entry.getKey()).append('=').append(URLCoder.encode(entry.getValue(), Charsets.UTF_8));
        }
        return sb.toString();
    }

}
