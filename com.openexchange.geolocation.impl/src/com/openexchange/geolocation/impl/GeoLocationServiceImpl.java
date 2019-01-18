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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationExceptionCodes;
import com.openexchange.geolocation.GeoLocationIPUtils;
import com.openexchange.geolocation.GeoLocationProperty;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.GeoLocationStorageService;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.GeoLocationService#getGeoInformation(com.openexchange.session.Session, java.lang.String)
     */
    @Override
    public GeoInformation getGeoInformation(int contextId, String ipAddress) throws OXException {
        return getStorage(contextId).getGeoInformation(contextId, GeoLocationIPUtils.convertIp(ipAddress));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.geolocation.GeoLocationService#getGeoInformation(com.openexchange.session.Session, double, double, int)
     */
    @Override
    public GeoInformation getGeoInformation(int contextId, double latitude, double longitude, int radius) throws OXException {
        return getStorage(contextId).getGeoInformation(contextId, latitude, longitude, radius);
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
        if (Strings.isEmpty(property)) {
            List<GeoLocationStorageService> availableServices = GeoLocationStorageServiceRegistry.getInstance().getAvailableServiceProviders();
            if (availableServices.isEmpty()) {
                OXException e = GeoLocationExceptionCodes.STORAGE_SERVICE_PROVIDER_NOT_CONFIGURED.create(GeoLocationProperty.PROVIDER.getFQPropertyName(), contextId);
                LOGGER.warn("{}", e.getMessage());
                throw e;
            }
            GeoLocationStorageService service = availableServices.get(0);
            LOGGER.debug("The property '{}' is empty! The geo location storage service provider '{}' was selected for context with id '{}'", GeoLocationProperty.PROVIDER.getFQPropertyName(), service.getProviderId(), contextId);
            return service;
        }
        try {
            return GeoLocationStorageServiceRegistry.getInstance().getStorageServiceProvider(property);
        } catch (IllegalArgumentException e) {
            throw GeoLocationExceptionCodes.UNKNOWN_STORAGE_SERVICE_PROVIDER.create(property);
        }
    }
}
