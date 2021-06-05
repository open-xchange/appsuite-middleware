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

package com.openexchange.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * {@link RegistryServiceTrackerCustomizer} can be used to remember discovered services in an {@link AbstractServiceRegistry}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RegistryServiceTrackerCustomizer<T> implements ServiceTrackerCustomizer<T, T> {

    /** The bundle context */
    protected final BundleContext context;

    /** The service registry to add the tracked service to */
    protected final AbstractServiceRegistry registry;

    /** The class of the service to track */
    protected final Class<T> serviceClass;

    /**
     * Initializes a new {@link RegistryServiceTrackerCustomizer}.
     *
     * @param context The bundle context
     * @param registry The registry
     * @param clazz The service class to track
     */
    public RegistryServiceTrackerCustomizer(final BundleContext context, final AbstractServiceRegistry registry, final Class<T> clazz) {
        super();
        this.context = context;
        this.registry = registry;
        this.serviceClass = clazz;
    }

    @Override
    public T addingService(final ServiceReference<T> reference) {
        final T tmp = context.getService(reference);
        if (serviceClass.isInstance(tmp)) {
            registry.addService(serviceClass, tmp);
            serviceAcquired(tmp);
            return tmp;
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<T> reference, final T service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<T> reference, final T service) {
        if (null != service) {
            final T removedService = registry.removeService(serviceClass);
            if (null != removedService) {
                context.ungetService(reference);
                serviceReleased(removedService);
            }
        }
    }

    /**
     * A hook for additional actions for newly tracked service instance.
     * <p>
     * Sub-classes may cast service using {@link #serviceClass} member.
     *
     * @param service The newly tracked service
     */
    protected void serviceAcquired(final T service) {
        // Nothing to do in basic implementation
    }

    /**
     * A hook for additional actions for a removed tracked service instance.
     * <p>
     * Sub-classes may cast service using {@link #serviceClass} member.
     *
     * @param service The removed tracked service
     */
    protected void serviceReleased(final T service) {
        // Nothing to do in basic implementation
    }

}
