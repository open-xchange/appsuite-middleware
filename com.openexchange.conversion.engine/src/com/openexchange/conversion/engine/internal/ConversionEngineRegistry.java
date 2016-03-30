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
        return sources.get(identifier);
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
