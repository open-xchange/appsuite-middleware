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

package com.openexchange.conversion.engine.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataSource;

/**
 * {@link ConversionEngineRegistry} - The registry for {@link DataSource data sources} and {@link DataHandler data handlers}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConversionEngineRegistry {

    private final static ConversionEngineRegistry instance = new ConversionEngineRegistry();

    /**
     * Gets the instance of {@link ConversionEngineRegistry}
     *
     * @return The instance of {@link ConversionEngineRegistry}
     */
    public static ConversionEngineRegistry getInstance() {
        return instance;
    }

    private final Map<String, DataHandler> handlers;

    private final Map<String, DataSource> sources;

    /**
     * Initializes a new {@link ConversionEngineRegistry}
     */
    private ConversionEngineRegistry() {
        super();
        handlers = new ConcurrentHashMap<String, DataHandler>();
        sources = new ConcurrentHashMap<String, DataSource>();
    }

    /**
     * Puts a data handler into this registry
     *
     * @param identifier The identifier
     * @param dataHandler The data handler
     */
    public void putDataHandler(final String identifier, final DataHandler dataHandler) {
        handlers.put(identifier, dataHandler);

    }

    /**
     * Puts a data handler into this registry
     *
     * @param identifier The identifier
     * @param dataSource The data source
     */
    public void putDataSource(final String identifier, final DataSource dataSource) {
        sources.put(identifier, dataSource);

    }

    /**
     * Gets the data handler associated with specified identifier
     *
     * @param identifier The identifier
     * @return The data handler associated with specified identifier or <code>null</code>
     */
    public DataHandler getDataHandler(final String identifier) {
        return handlers.get(identifier);
    }

    /**
     * Gets the data source associated with specified identifier
     *
     * @param identifier The identifier
     * @return The data source associated with specified identifier or <code>null</code>
     */
    public DataSource getDataSource(final String identifier) {
        return identifier == null ? null : sources.get(identifier);
    }

    /**
     * Removes the data handler associated with specified identifier
     *
     * @param identifier The identifier
     * @return The removed data handler or <code>null</code> if none was associated with specified identifier
     */
    public DataHandler removeDataHandler(final String identifier) {
        return handlers.remove(identifier);
    }

    /**
     * Removes the data source associated with specified identifier
     *
     * @param identifier The identifier
     * @return The removed data source or <code>null</code> if none was associated with specified identifier
     */
    public DataSource removeDataSource(final String identifier) {
        return sources.remove(identifier);
    }

    /**
     * Clears this registry completely
     */
    public void clearAll() {
        handlers.clear();
        sources.clear();
    }
}
