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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link RegistryCustomizer} - Registers/unregisters a certain service in/from {@link ServerServiceRegistry}.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class RegistryCustomizer<T> implements ServiceTrackerCustomizer<T,T> {

    private final BundleContext context;
    private final Class<T> clazz;

    /**
     * Initializes a new {@link RegistryCustomizer}.
     *
     * @param context The bundle context
     * @param clazz The class of the service to register
     */
    public RegistryCustomizer(final BundleContext context, final Class<T> clazz) {
        super();
        this.context = context;
        this.clazz = clazz;
    }

    @Override
    public T addingService(final ServiceReference<T> serviceReference) {
        final T service = context.getService(serviceReference);
        ServerServiceRegistry.getInstance().addService(clazz, customize(service));
        return service;
    }

    /**
     * Hook to modify tracked service instance if needed.
     * <p>
     * By default the service is returned as-is.
     *
     * @param service The tracked service
     * @return The possibly modified service
     */
    protected T customize(final T service) {
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<T> serviceReference, final T o) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<T> serviceReference, final T o) {
        ServerServiceRegistry.getInstance().removeService(clazz);
        context.ungetService(serviceReference);
    }
}
