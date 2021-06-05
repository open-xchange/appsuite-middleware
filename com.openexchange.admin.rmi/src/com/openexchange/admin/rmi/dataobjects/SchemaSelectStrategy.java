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

package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;

/**
 * Defines a strategy where to create contexts.
 * The default strategy should be automatic where the server decides on the current schema load, which will be determined on every call.
 * In memory just loads the schema load once and handles/updates the information in memory. This might be more error-prone, but is also more efficient.
 * If a schema is provided, the context will be created in this schema regardless of it's load.
 *
 * Only one strategy can be configured. Setting one strategy automatically disables the other strategies.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class SchemaSelectStrategy implements Serializable {

    /** The strategy enumeration */
    public static enum Strategy {
        SCHEMA, AUTOMATIC;
    }

    private static final long serialVersionUID = 2888117829864032432L;

    private static final SchemaSelectStrategy AUTOMATIC_SELECT_STRATEGY = new SchemaSelectStrategy(Strategy.AUTOMATIC);

    /**
     * Creates a schema strategy for auto-determining schema name
     *
     * @return The appropriate schema strategy
     */
    public static SchemaSelectStrategy automatic() {
        return AUTOMATIC_SELECT_STRATEGY;
    }

    /**
     * Creates a schema strategy for a pre-defined schema name
     *
     * @param schemaName The schema name
     * @return The appropriate schema strategy
     */
    public static SchemaSelectStrategy schema(String schemaName) {
        return new SchemaSelectStrategy(Strategy.SCHEMA, schemaName);
    }

    /**
     * Creates the default strategy
     *
     * @return The defualt strategy
     */
    public static SchemaSelectStrategy getDefault() {
        return automatic();
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private final Strategy strategy;
    private final String schema;

    /**
     * Initializes a new {@link SchemaSelectStrategy}.
     */
    private SchemaSelectStrategy(Strategy strategy) {
        this(strategy, null);
    }

    /**
     * Initializes a new {@link SchemaSelectStrategy}.
     */
    private SchemaSelectStrategy(Strategy strategy, String schemaName) {
        super();
        this.strategy = strategy;
        this.schema = schemaName;
    }

    /**
     * Returns the configured schema.
     *
     * @return The schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns the strategy.
     *
     * @return The strategy
     */
    public Strategy getStrategy() {
        return strategy;
    }

}
