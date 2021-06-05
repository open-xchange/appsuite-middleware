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

package com.openexchange.capabilities;

import java.io.Serializable;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 *
 * {@link ConfigurationProperty}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
public final class ConfigurationProperty implements Serializable {

    private static final long serialVersionUID = -4968698023048792666L;

    private final String scope;
    private final String name;
    private final String value;
    private final Map<String, String> metadata;
    private final boolean sysEnvVariable;

    /**
     *
     * Initializes a new {@link UserProperty}.
     *
     * @param scope The scope
     * @param name The name
     * @param value The value
     * @param sysEnvVariable <code>true</code> to signal that a server-scoped property's value originates from a system environment variable; otherwise <code>false</code>
     */
    public ConfigurationProperty(String scope, String name, String value, boolean sysEnvVariable) {
        this(scope, name, value, ImmutableMap.of(), sysEnvVariable);
    }

    /**
     *
     * Initializes a new {@link UserProperty}.
     *
     * @param scope The scope
     * @param name The name
     * @param value The value
     * @param metadata The metadata
     * @param sysEnvVariable <code>true</code> to signal that a server-scoped property's value originates from a system environment variable; otherwise <code>false</code>
     */
    public ConfigurationProperty(String scope, String name, String value, Map<String, String> metadata, boolean sysEnvVariable) {
        this.scope = scope;
        this.name = name;
        this.value = value;
        this.metadata = metadata;
        this.sysEnvVariable = sysEnvVariable;
    }

    /**
     * Checks if server-scoped property value originates from a system environment variable.
     *
     * @return <code>true</code> if value originates from a system environment variable; otherwise <code>false</code>
     */
    public boolean isSysEnvVariable() {
        return sysEnvVariable;
    }

    /**
     * Gets the scope
     *
     * @return The scope
     */
    public String getScope() {
        return scope;
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
     * Gets the value
     *
     * @return The value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the metadata
     *
     * @return the metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
