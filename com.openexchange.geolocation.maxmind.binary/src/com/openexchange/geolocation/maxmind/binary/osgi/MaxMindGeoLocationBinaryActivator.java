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
 *    trademarks of the OX Software GmbH. group of companies.
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
            LOG.error("Failed to re-initialize MaxMind ge-location storage", e);
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
