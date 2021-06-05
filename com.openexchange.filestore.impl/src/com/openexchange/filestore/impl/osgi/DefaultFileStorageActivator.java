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

package com.openexchange.filestore.impl.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.filestore.FileStorage2EntitiesResolver;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.FileStorageService;
import com.openexchange.filestore.impl.CompositeFileStorageService;
import com.openexchange.filestore.impl.DbFileStorage2EntitiesResolver;

/**
 * {@link DefaultFileStorageActivator} - The activator for the {@link FileStorageService} service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DefaultFileStorageActivator implements BundleActivator {

    private ServiceRegistration<FileStorageService> fileStorageServiceRegistration;
    private ServiceRegistration<FileStorage2EntitiesResolver> fileStorage2EntitiesResolverRegistration;
    private ServiceTracker<FileStorageProvider, FileStorageProvider> fileStorageProviderTracker;

    /**
     * Initializes a new {@link DefaultFileStorageActivator}.
     */
    public DefaultFileStorageActivator() {
        super();
    }

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        CompositeFileStorageService service = new CompositeFileStorageService(context);

        ServiceTracker<FileStorageProvider, FileStorageProvider> tracker = new ServiceTracker<>(context, FileStorageProvider.class, service);
        this.fileStorageProviderTracker = tracker;
        tracker.open();

        fileStorageServiceRegistration = context.registerService(FileStorageService.class, service, null);
        fileStorage2EntitiesResolverRegistration = context.registerService(FileStorage2EntitiesResolver.class, new DbFileStorage2EntitiesResolver(), null);
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        ServiceRegistration<FileStorageService> reg = this.fileStorageServiceRegistration;
        if (null != reg) {
            reg.unregister();
            this.fileStorageServiceRegistration = null;
        }

        ServiceRegistration<FileStorage2EntitiesResolver> reg2 = this.fileStorage2EntitiesResolverRegistration;
        if (null != reg2) {
            reg2.unregister();
            this.fileStorage2EntitiesResolverRegistration = null;
        }

        ServiceTracker<FileStorageProvider, FileStorageProvider> tracker = this.fileStorageProviderTracker;
        if (null != tracker) {
            tracker.close();
            this.fileStorageProviderTracker = null;
        }
    }

}
