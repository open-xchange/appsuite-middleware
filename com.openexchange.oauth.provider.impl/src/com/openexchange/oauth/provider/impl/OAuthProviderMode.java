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

package com.openexchange.oauth.provider.impl;

import java.util.Arrays;
import java.util.List;
import com.openexchange.oauth.provider.impl.introspection.OAuthIntrospectionAuthorizationService;
import com.openexchange.oauth.provider.impl.jwt.OAuthJwtAuthorizationService;

/**
 * {@link OAuthProviderMode} - defines available modes for OAuth provider
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public enum OAuthProviderMode {

    /**
     * The OAuth provider also acts as a authorization server.
     * This mode enables {@link DefaultAuthorizationService}.
     */
    AUTH_SEVER("auth_server"),

    /**
     * The OAuth provider expects JWT and is able to parse and validate it.
     * This mode enables {@link OAuthJwtAuthorizationService}
     */
    EXPECT_JWT("expect_jwt"),

    /**
     * The OAuthProvider uses token introspection to verify a received token.
     * This mode enables {@link OAuthIntrospectionAuthorizationService}
     */
    TOKEN_INTROSPECTION("token_introspection");

    private final String mode;

    /**
     * Initializes a new {@link OAuthProviderMode}.
     */
    private OAuthProviderMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns the mode string associated with the {@link OAuthProviderMode}.
     *
     * @return the mode string
     */
    public String getProviderModeString() {
        return mode;
    }

    /**
     * Return the corresponding {@link OAuthProviderMode} or {@link OAuthProviderMode#AUTH_SEVER} in case the given mode string is unknown.
     * 
     * @param input String representation of an {@link OAuthProviderMode}.
     * @return The corresponding {@link OAuthProviderMode}
     */
    public static OAuthProviderMode getProviderMode(String input) {
        List<OAuthProviderMode> modes = Arrays.asList(OAuthProviderMode.values());
        return modes.stream().filter(mode -> mode.getProviderModeString().equals(input)).findAny().orElse(AUTH_SEVER);
    }
}
