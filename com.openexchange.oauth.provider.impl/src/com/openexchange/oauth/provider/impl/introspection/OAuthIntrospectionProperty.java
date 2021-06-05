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

package com.openexchange.oauth.provider.impl.introspection;

import com.openexchange.config.lean.Property;

/**
 * {@link OAuthIntrospectionProperty}
 *
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since 7.10.5
 */
public enum OAuthIntrospectionProperty implements Property{

    /**
     * The token introspection endpoint.
     */
    ENDPOINT("endpoint", OAuthIntrospectionProperty.EMPTY),

    /**
     * Enable basic authentication for introspection
     */
    BASIC_AUTH_ENABLED("basicAuthEnabled", Boolean.TRUE),

    /**
     * ID of the OAuth client.
     */
    CLIENT_ID("clientID", OAuthIntrospectionProperty.EMPTY),

    /**
     * Secret of the OAuth client.
     */
    CLIENT_SECRET("clientSecret", OAuthIntrospectionProperty.EMPTY);

    public static final String PREFIX = "com.openexchange.oauth.provider.introspection.";
    private static final String EMPTY = "";
    private final String fqn;
    private final Object defaultValue;

    /**
     * Initializes a new {@link OAuthIntrospectionProperty}.
     *
     * @param suffix the suffix
     * @param defaultValue the default value
     */
    private OAuthIntrospectionProperty(String suffix, Object defaultValue) {
        this.fqn =  PREFIX + suffix;
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
