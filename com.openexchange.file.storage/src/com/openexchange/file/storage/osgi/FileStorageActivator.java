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

package com.openexchange.file.storage.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.internal.FileStorageConfigReloadable;
import com.openexchange.file.storage.internal.FileStorageQuotaProvider;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.secret.SecretService;

/**
 * {@link FileStorageActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class FileStorageActivator extends HousekeepingActivator {

    private OSGIFileStorageServiceRegistry registry;
    private OSGIFileStorageAccountManagerLookupService lookupService;

    /**
     * Initializes a new {@link FileStorageActivator}.
     */
    public FileStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileStorageActivator.class);
        try {
            log.info("starting bundle: com.openexchange.file.storage");
            Services.setServices(this);
            /*
             * Start registry tracking
             */
            final OSGIFileStorageServiceRegistry registry = new OSGIFileStorageServiceRegistry();
            registry.start(context);
            this.registry = registry;
            /*
             * Start provider tracking
             */
            final OSGIFileStorageAccountManagerLookupService lookupService = new OSGIFileStorageAccountManagerLookupService();
            lookupService.start(context);
            this.lookupService = lookupService;
            /*
             * Track SecretService
             */
            trackService(SecretService.class);
            openTrackers();
            /*
             * Register services
             */
            registerService(FileStorageServiceRegistry.class, registry);
            registerService(FileStorageAccountManagerLookupService.class, lookupService);
            registerService(QuotaProvider.class, new FileStorageQuotaProvider(registry));
            registerService(Reloadable.class, FileStorageConfigReloadable.getInstance());
        } catch (Exception e) {
            log.error("Starting bundle \"com.openexchange.file.storage\" failed.", e);
            throw e;
        }
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileStorageActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.file.storage");
            unregisterServices();
            /*
             * Stop look-up service
             */
            final OSGIFileStorageAccountManagerLookupService lookupService = this.lookupService;
            if (null != lookupService) {
                lookupService.stop();
                this.lookupService = null;
            }
            /*
             * Stop registry
             */
            final OSGIFileStorageServiceRegistry registry = this.registry;
            if (null != registry) {
                registry.stop();
                this.registry = null;
            }
            Services.setServices(null);
            super.stopBundle();
        } catch (Exception e) {
            log.error("Stopping bundle \"com.openexchange.file.storage\" failed.", e);
            throw e;
        }
    }

}
