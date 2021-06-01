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

package org.json.osgi;

import org.json.FileBackedJSONStringProvider;
import org.json.helpers.FileBackedJSON;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link JSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class JSONActivator implements BundleActivator {

    private ServiceTracker<FileBackedJSONStringProvider, FileBackedJSONStringProvider> thresholdJSONStringProviderTracker;

    /**
     * Initializes a new {@link JSONActivator}.
     */
    public JSONActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        ServiceTrackerCustomizer<FileBackedJSONStringProvider, FileBackedJSONStringProvider> customizer = new ServiceTrackerCustomizer<FileBackedJSONStringProvider, FileBackedJSONStringProvider>() {

            @Override
            public void removedService(ServiceReference<FileBackedJSONStringProvider> reference, FileBackedJSONStringProvider service) {
                FileBackedJSON.setFileBackedJSONStringProvider(null);
                context.ungetService(reference);
            }

            @Override
            public void modifiedService(ServiceReference<FileBackedJSONStringProvider> reference, FileBackedJSONStringProvider service) {
                // Ignore
            }

            @Override
            public FileBackedJSONStringProvider addingService(ServiceReference<FileBackedJSONStringProvider> reference) {
                FileBackedJSONStringProvider provider = context.getService(reference);
                FileBackedJSON.setFileBackedJSONStringProvider(provider);
                return provider;
            }
        };
        thresholdJSONStringProviderTracker = new ServiceTracker<>(context, FileBackedJSONStringProvider.class, customizer);
        thresholdJSONStringProviderTracker.open();
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        ServiceTracker<FileBackedJSONStringProvider, FileBackedJSONStringProvider> tracker = this.thresholdJSONStringProviderTracker;
        if (null != tracker) {
            this.thresholdJSONStringProviderTracker = null;
            tracker.close();
        }
    }

}
