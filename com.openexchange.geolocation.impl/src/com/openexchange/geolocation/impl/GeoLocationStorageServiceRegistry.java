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

package com.openexchange.geolocation.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.GeoLocationStorageService;
import com.openexchange.geolocation.exceptions.GeoLocationExceptionCodes;

/**
 * {@link GeoLocationStorageServiceRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class GeoLocationStorageServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationStorageServiceRegistry.class);

    public static final GeoLocationStorageServiceRegistry INSTANCE = new GeoLocationStorageServiceRegistry();

    /**
     * Returns the instance of the registry
     * 
     * @return the instance of the registry
     */
    public static final GeoLocationStorageServiceRegistry getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<String, GeoLocationStorageService> registry;

    /**
     * Initialises a new {@link GeoLocationStorageServiceRegistryImpl}.
     */
    private GeoLocationStorageServiceRegistry() {
        super();
        registry = new ConcurrentHashMap<>(2);
    }

    /**
     * Registers the specified {@link GeoLocationService}
     * 
     * @param providerId The provider's id
     * @param service The {@link GeoLocationService} to register
     */
    public void registerServiceProvider(String providerId, GeoLocationStorageService service) {
        GeoLocationStorageService raced = registry.putIfAbsent(providerId, service);
        if (raced != null) {
            LOGGER.warn("A GeoLocation service for '{}' was previously registered! Check your bundles!", providerId);
            return;
        }
    }

    /**
     * Unregisters the {@link GeoLocationService} with the specified identifier
     * 
     * @param providerId The provider's identifier
     */
    public void unregisterServiceProvider(String providerId) {
        registry.remove(providerId);
    }

    /**
     * Returns an unmodifiable list all available service providers
     * 
     * @return an unmodifiable list all available service providers
     */
    public List<GeoLocationStorageService> getAvailableServiceProviders() {
        return Collections.unmodifiableList(new LinkedList<>(registry.values()));
    }

    /**
     * Checks whether the registry has any storages registered
     * 
     * @return <code>true</code> if the registry has at least one storage registered,
     *         <code>false</code> otherwise
     */
    public boolean hasStorages() {
        return false == registry.isEmpty();
    }

    /**
     * Retrieves the {@link GeoLocationStorageService} that is registered with the specified
     * storageServiceProvider identifier
     * 
     * @param providerId The storage service provider identifier
     * @return The {@link GeoLocationStorageService}
     * @throws OXException if no storage service provider is registered with that identifier
     */
    public GeoLocationStorageService getStorageServiceProvider(String providerId) throws OXException {
        GeoLocationStorageService service = registry.get(providerId);
        if (service == null) {
            throw GeoLocationExceptionCodes.UNKNOWN_STORAGE_SERVICE_PROVIDER.create(providerId);
        }
        return service;
    }
}
