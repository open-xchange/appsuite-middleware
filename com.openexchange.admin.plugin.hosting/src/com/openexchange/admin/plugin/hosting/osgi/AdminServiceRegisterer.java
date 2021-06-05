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

package com.openexchange.admin.plugin.hosting.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.admin.plugin.hosting.services.AdminServiceRegistry;

/**
 * Puts a found services into the {@link AdminServiceRegistry}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class AdminServiceRegisterer<S> implements ServiceTrackerCustomizer<S,S> {

    private final Class<S> clazz;
    private final BundleContext context;

    /**
     * Initializes a new {@link AdminServiceRegisterer}.
     * @param context
     */
    public AdminServiceRegisterer(Class<S> clazz, BundleContext context) {
        super();
        this.clazz = clazz;
        this.context = context;
    }

    @Override
    public S addingService(ServiceReference<S> reference) {
        S service = context.getService(reference);
        AdminServiceRegistry.getInstance().addService(clazz, service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<S> reference, S service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<S> reference, S service) {
        AdminServiceRegistry.getInstance().removeService(clazz);
        context.ungetService(reference);
    }
}
