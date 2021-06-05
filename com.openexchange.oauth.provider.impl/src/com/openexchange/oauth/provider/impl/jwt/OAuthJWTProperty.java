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

package com.openexchange.oauth.provider.impl.jwt;

import com.openexchange.config.lean.Property;

/**
 * {@link OAuthJWTProperty}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.10.5
 */
public enum OAuthJWTProperty implements Property {

    /**
     * Specifies a JWKS URI used to fetch signature keys for validation.
     * This URI can either be local file path or an URL.
     *
     * Example:
     *  file:/Users/sebastianlutz/git/core/com.openexchange.oauth.provider.impl/conf/jwk.json,
     *  http://127.0.0.1:8085/auth/realms/demo/protocol/openid-connect/certs
     *
     */
    JWKS_URI("jwksUri", OAuthJWTProperty.EMPTY),

    /**
     * Path for loading a JWK set from a local JSON file
     */
    JWKS_JSON_PATH("jwksJson.path", OAuthJWTProperty.EMPTY);

    public static final String PREFIX = "com.openexchange.oauth.provider.jwt.";
    private static final String EMPTY = "";
    private final String fqn;
    private final Object defaultValue;

    /**
     * Initializes a new {@link OAuthJWTProperty}.
     *
     * @param suffix the suffix
     * @param defaultValue the default value
     */
    private OAuthJWTProperty(String suffix, Object defaultValue) {
        this.fqn = PREFIX + suffix;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
