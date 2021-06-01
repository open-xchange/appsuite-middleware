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

package com.openexchange.gdpr.dataexport;

import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * {@link Module} - Represents a module that should be considered during a data export consisting of an obligatory module identifier and
 * optional additional properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class Module {

    private final static Map<String, Module> INSTANCES;
    static {
        ImmutableMap.Builder<String, Module> instances = ImmutableMap.builderWithExpectedSize(6);

        String id = "mail";
        instances.put(id, new Module(id));

        id = "calendar";
        instances.put(id, new Module(id));

        id = "contacts";
        instances.put(id, new Module(id));

        id = "tasks";
        instances.put(id, new Module(id));

        id = "drive";
        instances.put(id, new Module(id));

        INSTANCES = instances.build();
    }

    /**
     * Gets the module for specified identifier.
     *
     * @param id The module identifier
     * @return The module
     */
    public static Module valueOf(String id) {
        return valueOf(id, null);
    }

    /**
     * Gets the module for specified identifier and (optional) properties.
     *
     * @param id The module identifier
     * @param properties The optional module properties
     * @return The module
     */
    public static Module valueOf(String id, Map<String, Object> properties) {
        String lcid = Strings.asciiLowerCase(id);

        if (properties == null || properties.isEmpty()) {
            Module module = INSTANCES.get(lcid);
            return module == null ? new Module(lcid) : module;
        }

        return new Module(lcid, properties);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String id;
    private final Map<String, Object> properties;
    private int hash;

    /**
     * Initializes a new {@link Module}.
     *
     * @param id The module identifier
     */
    private Module(String id) {
        this(id, null);
    }

    /**
     * Initializes a new {@link Module}.
     *
     * @param id The module identifier
     * @param properties The optional module properties
     */
    private Module(String id, Map<String, Object> properties) {
        super();
        this.id = id;
        this.properties = properties == null || properties.isEmpty() ? null : ImmutableMap.copyOf(properties);
        hash = 0;
    }

    /**
     * Gets the module identifier.
     *
     * @return The module identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the optional properties.
     *
     * @return The properties
     */
    public Optional<Map<String, Object>> getProperties() {
        return properties == null ? Optional.empty() : Optional.of(properties);
    }

    @Override
    public int hashCode() {
        // Does not need to be thread-safe
        int h = hash;
        if (h == 0) {
            h = 31 * 1 + ((id == null) ? 0 : id.hashCode());
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Module other = (Module) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Module [");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (properties != null) {
            builder.append("properties=").append(properties);
        }
        builder.append("]");
        return builder.toString();
    }

}
