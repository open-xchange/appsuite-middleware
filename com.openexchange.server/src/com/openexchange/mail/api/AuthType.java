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

package com.openexchange.mail.api;

import java.util.EnumSet;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * {@link AuthType} - The authentication type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public enum AuthType {

    /**
     * The login authentication type.
     */
    LOGIN("login", false),
    /**
     * The XOAUTH2 authentication type; see <a href="https://developers.google.com/gmail/xoauth2_protocol">https://developers.google.com/gmail/xoauth2_protocol</a>
     */
    OAUTH("XOAUTH2", true),
    /**
     * The OAUTHBEARER authentication type; see <a href="https://tools.ietf.org/html/rfc7628">https://tools.ietf.org/html/rfc7628</a>.
     */
    OAUTHBEARER("OAUTHBEARER", true),
    ;

    private final String name;
    private final boolean oauth;

    private AuthType(String name, boolean oauth) {
        this.name = name;
        this.oauth = oauth;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether this authentication type implies OAuth.
     *
     * @return <code>true</code> if OAuth is implied; otherwise <code>false</code>
     */
    public boolean isOAuth() {
        return oauth;
    }

    private static final Map<String, AuthType> MAP;
    static {
        ImmutableMap.Builder<String, AuthType> builder = ImmutableMap.builder();
        for (AuthType authType : AuthType.values()) {
            builder.put(Strings.asciiLowerCase(authType.name), authType);
        }
        // Legacy behavior
        builder.put("oauth", AuthType.OAUTH);
        MAP = builder.build();
    }

    /**
     * Parses specified string into an AuthType.
     *
     * @param authTypeStr The string to parse to an AuthType
     * @return An appropriate AuthType or <code>null</code> if string could not be parsed to an AuthType
     */
    public static final AuthType parse(String authTypeStr) {
        return null == authTypeStr ? null : MAP.get(Strings.asciiLowerCase(authTypeStr));
    }

    private static final EnumSet<AuthType> OAUTH_TYPES = EnumSet.of(AuthType.OAUTH, AuthType.OAUTHBEARER);

    /**
     * Checks if given auth type is one of known OAuth-based types; either XOAUTH2 or OAUTHBEARER.
     *
     * @param authType The auth type to check
     * @return <code>true</code> auth type is one of known OAuth-based types; otherwise <code>false</code>
     */
    public static boolean isOAuthType(AuthType authType) {
        return null != authType && OAUTH_TYPES.contains(authType);
    }

}
