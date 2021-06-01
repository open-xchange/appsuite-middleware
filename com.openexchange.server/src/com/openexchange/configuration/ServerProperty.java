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

package com.openexchange.configuration;

import com.openexchange.config.lean.Property;

/**
 * {@link ServerProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum ServerProperty implements Property {
    /**
     * <p>Specifies the redirect URI/URL during cluster migration to which a client
     * is redirected in case is landed on an unsuitable node (running incompatible
     * application code).</p>
     * <p>E.g. a user gets routed to a node running application code in version X,
     * but that account has already been migrated to application code in version Y.</p>
     * <p>No default value</p>
     */
    migrationRedirectURL(ServerProperty.EMPTY);

    private static final String EMPTY = "";
    private static final String PREFIX = "com.openexchange.server.";

    private final Object defaultValue;

    /**
     * Initialises a new {@link ServerProperty}.
     */
    private ServerProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
