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

import java.util.Iterator;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.java.ConcurrentList;

/**
 * {@link NearRegistryServiceTracker} - A near-registry service tracker.
 * <p>
 * Occurrences of specified service type are collected and available via {@link #getServiceList()}.<br>
 * This is intended to replace {@link #getServices()} since it requires to obtain tracker's mutex on each invocation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NearRegistryServiceTracker<S> extends ServiceTracker<S, S> implements ServiceListing<S> {

    private final List<S> services;

    /**
     * Initializes a new {@link NearRegistryServiceTracker}.
     *
     * @param context The bundle context
     * @param clazz The service class
     */
    public NearRegistryServiceTracker(final BundleContext context, final Class<S> clazz) {
        super(context, clazz, null);
        services = new ConcurrentList<S>();
    }

    @Override
    public List<S> getServiceList() {
        return services;
    }

    @Override
    public Iterator<S> iterator() {
        return services.iterator();
    }

    @Override
    public S addingService(final ServiceReference<S> reference) {
        S service = context.getService(reference);

        S serviceToAdd = onServiceAvailable(service);
        if (services.add(serviceToAdd)) {
            return service;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(final ServiceReference<S> reference, final S service) {
        services.remove(service);
        context.ungetService(reference);
    }

    /**
     * Invoked when a tracked service is available.
     *
     * @param service The available service
     * @return The service to add
     */
    protected S onServiceAvailable(S service) {
        return service;
    }

}
