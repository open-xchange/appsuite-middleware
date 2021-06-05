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

package com.openexchange.groupware.settings.extensions.osgi;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.groupware.settings.extensions.ServicePublisher;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class OSGiServicePublisher implements ServicePublisher {

    private final ConcurrentMap<Object, ServiceRegistration<?>> serviceRegistrations;
    private final BundleContext context;

    /**
     * Initializes a new {@link OSGiServicePublisher}.
     *
     * @param context The OSGi bundle context
     */
    public OSGiServicePublisher(final BundleContext context) {
        super();
        this.context = context;
        serviceRegistrations = new ConcurrentHashMap<Object, ServiceRegistration<?>>(4, 0.9F, 1);
    }

    @Override
    public <S> void publishService(final Class<S> clazz, final S service) {
        ServiceRegistration<S> registration = context.registerService(clazz, service, null);
        if (null == serviceRegistrations.putIfAbsent(service, registration)) {
            return;
        }

        // Already contained...
        registration.unregister();
    }

    @Override
    public <S> void removeService(final Class<S> clazz, final S service) {
        ServiceRegistration<S> registration = (ServiceRegistration<S>) serviceRegistrations.remove(service);
        if (null != registration) {
            registration.unregister();
        }
    }

    @Override
    public void removeAllServices() {
        for (Iterator<ServiceRegistration<?>> it = serviceRegistrations.values().iterator(); it.hasNext();) {
            ServiceRegistration<?> registration = it.next();
            it.remove();
            registration.unregister();
        }
    }

}
