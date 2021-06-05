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
package com.openexchange.oidc;

import com.openexchange.authentication.AuthenticationService;
import com.openexchange.config.lean.Property;


/**
 * Contains all properties of the OpenID feature.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public enum OIDCProperty implements Property {
    /**
     * enabled - Switch to enable disable the core OpenID feature.
     */
    enabled(OIDCProperty.PREFIX, Boolean.FALSE),
    /**
     * startDefaultBackend - Start the default core OpenID backend.
     */
    startDefaultBackend(OIDCProperty.PREFIX, Boolean.FALSE),
    /**
     * enablePasswordGrant - Whether an {@link AuthenticationService} shall be registered, that
     * handles username/password logins for the core login servlet by using the Resource Owner
     * Password Credentials Grant {@link https://tools.ietf.org/html/rfc6749#section-4.3} to authenticate.
     */
    enablePasswordGrant(OIDCProperty.PREFIX, Boolean.FALSE);


    public static final String EMPTY = "";
    public static final String PREFIX = "com.openexchange.oidc.";

    private final String fqn;
    private final Object defaultValue;

    private OIDCProperty(String prefix, Object defaultValue) {
        this.fqn = prefix + name();
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public <T> T getDefaultValue(Class<T> clazz) {
        if (defaultValue.getClass().isAssignableFrom(clazz)) {
            return clazz.cast(defaultValue);
        }
        throw new IllegalArgumentException("The object cannot be converted to the specified type '" + clazz.getCanonicalName() + "'");

    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
