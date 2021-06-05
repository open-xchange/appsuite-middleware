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

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationProperty;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.GeoLocationStorageService;
import com.openexchange.geolocation.exceptions.GeoLocationExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GeoLocationServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class GeoLocationServiceImpl implements GeoLocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationServiceImpl.class);

    private final ServiceLookup services;

    /**
     * Initialises a new {@link GeoLocationServiceImpl}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public GeoLocationServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public GeoInformation getGeoInformation(int contextId, InetAddress ipAddress) throws OXException {
        return getStorage(contextId).getGeoInformation(contextId, ipAddress);
    }

    /**
     * Retrieves the appropriate GeoLocationStorageService (if any) from the registry
     *
     * @param contextId The context identifier
     * @return The {@link GeoLocationStorageService}
     * @throws OXException if the configured storage provider is unknown or if there are no storage providers registered.
     */
    private GeoLocationStorageService getStorage(int contextId) throws OXException {
        LeanConfigurationService leanConfig = services.getServiceSafe(LeanConfigurationService.class);
        String property = leanConfig.getProperty(-1, contextId, GeoLocationProperty.PROVIDER);
        if (Strings.isNotEmpty(property)) {
            return GeoLocationStorageServiceRegistry.getInstance().getStorageServiceProvider(property);
        }

        List<GeoLocationStorageService> availableServices = GeoLocationStorageServiceRegistry.getInstance().getAvailableServiceProviders();
        if (availableServices.isEmpty()) {
            OXException e = GeoLocationExceptionCodes.STORAGE_SERVICE_PROVIDER_NOT_CONFIGURED_FOR_CONTEXT.create(GeoLocationProperty.PROVIDER.getFQPropertyName(), I(contextId));
            LOGGER.warn("{}", e.getMessage());
            throw e;
        }
        GeoLocationStorageService service = availableServices.get(0);
        LOGGER.debug("The property '{}' is empty! The geo location storage service provider '{}' was selected for context with id '{}'", GeoLocationProperty.PROVIDER.getFQPropertyName(), service.getProviderId(), I(contextId));
        return service;
    }
}
