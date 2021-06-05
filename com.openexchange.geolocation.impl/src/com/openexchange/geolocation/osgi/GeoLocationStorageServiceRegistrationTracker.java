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

package com.openexchange.geolocation.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.geolocation.GeoLocationStorageService;
import com.openexchange.geolocation.impl.GeoLocationServiceImpl;
import com.openexchange.geolocation.impl.GeoLocationStorageServiceRegistry;
import com.openexchange.server.ServiceLookup;

/**
 * {@link GeoLocationStorageServiceRegistrationTracker}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class GeoLocationStorageServiceRegistrationTracker implements ServiceTrackerCustomizer<GeoLocationStorageService, GeoLocationStorageService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoLocationStorageServiceRegistrationTracker.class);

    private final BundleContext context;
    private final ServiceLookup services;
    private ServiceRegistration<GeoLocationService> serviceRegistration;

    /**
     * Initialises a new {@link GeoLocationStorageServiceRegistrationTracker}.
     */
    public GeoLocationStorageServiceRegistrationTracker(BundleContext context, ServiceLookup services) {
        super();
        this.context = context;
        this.services = services;
    }

    @Override
    public GeoLocationStorageService addingService(ServiceReference<GeoLocationStorageService> reference) {
        GeoLocationStorageService storage = context.getService(reference);
        GeoLocationStorageServiceRegistry.getInstance().registerServiceProvider(storage.getProviderId(), storage);
        registerServiceIfNeeded();
        LOGGER.info("Registered the GeoLocationStorageService provider {}.", storage.getProviderId());
        return storage;
    }

    @Override
    public void modifiedService(ServiceReference<GeoLocationStorageService> reference, GeoLocationStorageService service) {
        // no-op
    }

    @Override
    public void removedService(ServiceReference<GeoLocationStorageService> reference, GeoLocationStorageService service) {
        GeoLocationStorageServiceRegistry.getInstance().unregisterServiceProvider(service.getProviderId());
        context.ungetService(reference);
        LOGGER.info("Unregistered the GeoLocationStorageService provider {}.", service.getProviderId());
        if (false == GeoLocationStorageServiceRegistry.getInstance().hasStorages() && serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
            LOGGER.info("Unregistered the GeoLocationService.");
        }
    }

    /**
     * Registers the {@link GeoLocationService} and its RMI counterpart if needed
     */
    private void registerServiceIfNeeded() {
        if (serviceRegistration != null) {
            return;
        }
        serviceRegistration = context.registerService(GeoLocationService.class, new GeoLocationServiceImpl(services), null);
        LOGGER.info("Registered the GeoLocationService.");
    }
}
