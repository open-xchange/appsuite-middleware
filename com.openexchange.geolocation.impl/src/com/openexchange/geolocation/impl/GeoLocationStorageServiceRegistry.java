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

package com.openexchange.geolocation.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoLocationExceptionCodes;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.GeoLocationStorageService;

/**
 * {@link GeoLocationStorageServiceRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class GeoLocationStorageServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationRMIServiceImpl.class);

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
