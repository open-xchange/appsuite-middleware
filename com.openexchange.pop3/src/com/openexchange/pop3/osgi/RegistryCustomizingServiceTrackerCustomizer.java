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

package com.openexchange.pop3.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.pop3.services.POP3ServiceRegistry;

/**
 * {@link RegistryCustomizingServiceTrackerCustomizer} - Service tracker customizer that puts/removes the tracked/untracked service in/from registry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RegistryCustomizingServiceTrackerCustomizer<S> implements ServiceTrackerCustomizer<S, S> {

    private final BundleContext context;
    private final Class<S> serviceClass;

    /**
     * Initializes a new {@link RegistryCustomizingServiceTrackerCustomizer}.
     */
    public RegistryCustomizingServiceTrackerCustomizer(Class<S> serviceClass, BundleContext context) {
        super();
        this.serviceClass = serviceClass;
        this.context = context;
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        S addedService = context.getService(reference);
        POP3ServiceRegistry.getServiceRegistry().addService(serviceClass, addedService);
        return addedService;
    }

    @Override
    public void modifiedService(final ServiceReference<S> reference, final S service) {
        // Nothing to do
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        if (null != service) {
            try {
                POP3ServiceRegistry.getServiceRegistry().removeService(serviceClass);
            } finally {
                context.ungetService(reference);
            }
        }
    }

}
