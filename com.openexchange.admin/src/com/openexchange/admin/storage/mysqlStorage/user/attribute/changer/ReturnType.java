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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ReturnType} enumeration - Defines all data types for the contact attributes
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum ReturnType {
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    BOOLEAN("Boolean"),
    DATE("Date", "java.util.");

    private static final String PREFIX = "java.lang.";
    private final String name;
    private final String prefix;

    private static final Map<String, ReturnType> RETURN_TYPES;
    static {
        Map<String, ReturnType> returnTypes = new HashMap<>(8);
        for (ReturnType type : ReturnType.values()) {
            returnTypes.put(type.getWithPrefix(), type);
        }
        RETURN_TYPES = Collections.unmodifiableMap(returnTypes);
    }

    /**
     * Initialises a new {@link ReturnType}.
     * 
     * @param name The name of the {@link ReturnType}
     */
    private ReturnType(String name) {
        this(name, PREFIX);
    }

    /**
     * Initialises a new {@link ReturnType}.
     * 
     * @param name The name of the {@link ReturnType}
     * @param prefix The prefix
     */
    private ReturnType(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
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
     * Gets the name with prefix
     *
     * @return The name with prefix
     */
    public String getWithPrefix() {
        return prefix + getName();
    }

    /**
     * Get the specified {@link ReturnType}
     * 
     * @param type The specified return type
     * @return The {@link ReturnType} or <code>null</code> if none exists
     */
    public static ReturnType getReturnType(String type) {
        return RETURN_TYPES.get(type);
    }
}
