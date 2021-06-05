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
 * {@link RegistryCustomizer}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class RegistryCustomizer<S> implements ServiceTrackerCustomizer<S, S> {

    private final BundleContext context;

    private final Class<S> clazz;

    private final ServiceRegistry registry;

    public RegistryCustomizer(final BundleContext context, final Class<S> clazz, final ServiceRegistry registry) {
        super();
        this.context = context;
        this.clazz = clazz;
        this.registry = registry;
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        final S service = context.getService(reference);
        registry.addService(clazz, service);
        return service;
    }

    @Override
    public void modifiedService(final ServiceReference<S> reference, final S service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        registry.removeService(clazz);
        context.ungetService(reference);
    }
}
