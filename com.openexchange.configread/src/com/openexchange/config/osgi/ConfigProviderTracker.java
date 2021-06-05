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

package com.openexchange.config.osgi;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ReinitializableConfigProviderService;


/**
 * {@link ConfigProviderTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ConfigProviderTracker implements ServiceTrackerCustomizer<ConfigProviderService, ConfigProviderService> {

    private final Queue<ReinitializableConfigProviderService> reinitQueue;
    private final BundleContext context;

    /**
     * Initializes a new {@link ConfigProviderTracker}.
     */
    public ConfigProviderTracker(BundleContext context) {
        super();
        this.context = context;
        reinitQueue = new ConcurrentLinkedQueue<ReinitializableConfigProviderService>();
    }

    @Override
    public ConfigProviderService addingService(ServiceReference<ConfigProviderService> reference) {
        ConfigProviderService providerService = context.getService(reference);
        if (providerService instanceof ReinitializableConfigProviderService) {
            reinitQueue.offer((ReinitializableConfigProviderService) providerService);
            return providerService;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<ConfigProviderService> reference, ConfigProviderService service) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<ConfigProviderService> reference, ConfigProviderService service) {
        reinitQueue.remove(service);
        context.ungetService(reference);
    }

    /**
     * Gets the re-initializable configuration provider services
     *
     * @return The re-initializable configuration provider services
     */
    public Collection<ReinitializableConfigProviderService> getReinitQueue() {
        return Collections.unmodifiableCollection(reinitQueue);
    }

}
