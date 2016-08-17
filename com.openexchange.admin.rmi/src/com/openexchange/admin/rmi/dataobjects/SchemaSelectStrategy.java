/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;

/**
 * Defines a strategy where to create contexts.
 * The default strategy should be automatic where the server decides on the current schema load, which willl be determined on every call.
 * In memory just loads the schema load once and handles/updates the information in memory. This might be more error-prone, but is also more efficient.
 * If a schema is provided, the context will be created in this schema regardless of it's load.
 *
 * Only one strategy can be configured. Setting one strategy automaticalle disables the other strategies.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class SchemaSelectStrategy implements Serializable {

    /** The strategy enumeration */
    public static enum Strategy {
        SCHEMA, AUTOMATIC, IN_MEMORY;
    }

    private static final long serialVersionUID = 2888117829864032432L;

    /**
     * Creates a schema strategy for auto-determining schema name
     *
     * @return The appropriate schema strategy
     */
    public static SchemaSelectStrategy automatic() {
        return new SchemaSelectStrategy(Strategy.AUTOMATIC);
    }

    /**
     * Creates a schema strategy for fetching the schema name from in-memory cache.
     *
     * @return The appropriate schema strategy
     */
    public static SchemaSelectStrategy inMemory() {
        return new SchemaSelectStrategy(Strategy.IN_MEMORY);
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
