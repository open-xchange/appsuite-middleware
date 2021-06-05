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

package com.openexchange.authentication.application.storage.rdb;

import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.Property;

/**
 * {@link AppPasswordStorageProperty}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public enum AppPasswordStorageProperty implements Property {

    /**
     * Configures whether the database-backed application password storage is generally enabled or not.
     */
    ENABLED("enabled", Boolean.TRUE),

    /**
     * Defines the name part of the context lookup source value used for determining the context of a user.
     */
    CONTEXT_LOOKUP_NAME_PART("contextLookupNamePart", NamePart.DOMAIN.getConfigName()),

    /**
     * Configures where the login name used for application credentials is taken from. Possible options are <code>session</code> to use
     * the login information from the actual regular session as-is, <code>mail</code> to take over the user's primary mail address,
     * <code>username</code> to use the user's name, or <code>synthetic</code> to construct the name from the stored login mappings
     * associated with the user and context, separated by the <code>@</code>-character.
     * <p/>
     * Defaults to <code>session</code>.
     */
    LOGIN_NAME_SOURCE("loginNameSource", LoginNameSource.SESSION.name()),

    /**
     * When enabled, encrypts and stores the user's regular session password in the application password authentication. Required if
     * applications accessing external systems like the mail server need their individual credentials rather than master- or OAuth-based
     * authentication.
     */
    STORE_USER_PASSWORD("storeUserPassword", Boolean.FALSE)

    ;

    private final Object defaultValue;
    private final String fqn;

    /**
     * Initializes a new {@link AppPasswordStorageProperty}.
     *
     * @param suffix The property name suffix
     * @param defaultValue The property's default value
     */
    private AppPasswordStorageProperty(String suffix, Object defaultValue) {
        this.defaultValue = defaultValue;
        this.fqn = "com.openexchange.authentication.application.storage.rdb." + suffix;
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
