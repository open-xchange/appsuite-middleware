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

package com.openexchange.contact.provider;

import com.openexchange.config.lean.Property;

/**
 * {@link ContactProviderProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public enum ContactProviderProperty implements Property {

    /**
     * Defines whether a provider is enabled or disabled
     * Defaults to <code>true</code>
     */
    enabled(Boolean.TRUE);

    private final Object defaultValue;
    private final String fqn;

    public static final String OPTIONAL_NAME = "providerId";
    private static final String PREFIX = "com.openexchange.contact.";

    /**
     * Initialises a new {@link ContactProviderProperty}.
     *
     * @param defaultValue The default value of the property
     */
    private ContactProviderProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
        this.fqn = PREFIX + "[" + OPTIONAL_NAME + "]." + name();
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
