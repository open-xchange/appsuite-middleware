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

package com.openexchange.caching.events;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link CacheEventService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SingletonService
public interface CacheEventService {

    /**
     * Gets the associated configuration
     *
     * @return The configuration
     */
    CacheEventConfiguration getConfiguration();

    /**
     * Publishes a cache event to the registered listeners.
     *
     * @param sender The sender of the event
     * @param event The cache event
     * @param fromRemote If event was remotely received
     */
    void notify(Object sender, CacheEvent event, boolean fromRemote);

    /**
     * Registers a cache listener to receive cache events for all regions.
     *
     * @param listener The cache listener to add
     */
    void addListener(CacheListener listener);

    /**
     * Removes a registered cache listener.
     *
     * @param listener The cache listener to remove
     */
    void removeListener(CacheListener listener);

    /**
     * Adds a cache listener to receive cache events for a specific region.
     *
     * @param region The region name the listener is interested in
     * @param listener The cache listener to add
     */
    void addListener(String region, CacheListener listener);

    /**
     * Removes a registered cache listener.
     *
     * @param region The region name the listener was interested in
     * @param listener The cache listener to remove
     */
    void removeListener(String region, CacheListener listener);

}
