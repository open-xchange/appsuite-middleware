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

package com.openexchange.geolocation.maxmind.binary.osgi;

import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.geolocation.GeoLocationStorageService;
import com.openexchange.geolocation.maxmind.binary.MaxMindBinaryStorage;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MaxMindGeoLocationBinaryActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class MaxMindGeoLocationBinaryActivator extends HousekeepingActivator implements Reloadable {

    private static final String DATABASE_PATH_PROPERTY = "com.openexchange.geolocation.maxmind.databasePath";
    private static final Logger LOG = LoggerFactory.getLogger(MaxMindGeoLocationBinaryActivator.class);

    private ServiceRegistration<GeoLocationStorageService> storageRegistration;
    private MaxMindBinaryStorage storage;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(Reloadable.class, this);
        reinit(getService(ConfigurationService.class));
    }

    @Override
    protected void stopBundle() throws Exception {
        reinit(null);
        super.stopBundle();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            reinit(configService);
        } catch (Exception e) {
            LOG.error("Failed to re-initialize MaxMind geo-location storage", e);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(DATABASE_PATH_PROPERTY).build();
    }

    /**
     * Re-initialises the configuration
     * 
     * @param configService The configuration service
     * @throws Exception If the storage cannot be initialised
     */
    private synchronized void reinit(ConfigurationService configService) throws Exception {
        // Close & unregister a previously initialised storage
        MaxMindBinaryStorage storage = this.storage;
        if (null != storage) {
            storage.close();
            this.storage = null;
        }
        ServiceRegistration<GeoLocationStorageService> storageRegistration = this.storageRegistration;
        if (null != storageRegistration) {
            storageRegistration.unregister();
            this.storageRegistration = null;
        }
        // Re-initialise storage service if configuration service is available
        if (null == configService) {
            LOG.warn("The configuration service is absent. Cannot reload configuration for MaxMindBinary Storage.");
            return;
        }
        String databasePath = configService.getProperty(DATABASE_PATH_PROPERTY);
        if (Strings.isEmpty(databasePath)) {
            LOG.warn("Property '{}' not configured, skipping storage registration.", DATABASE_PATH_PROPERTY);
            return;
        }
        storage = new MaxMindBinaryStorage(databasePath);
        this.storage = storage;
        this.storageRegistration = context.registerService(GeoLocationStorageService.class, storage, null);
    }
}
