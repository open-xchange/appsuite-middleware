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

package com.openexchange.guest.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.guest.GuestService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * Tracker for the {@link GuestService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public final class GuestServiceServiceTracker implements ServiceTrackerCustomizer<GuestService, GuestService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link GuestService}.
     */
    public GuestServiceServiceTracker(final BundleContext context) {
        super();
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GuestService addingService(final ServiceReference<GuestService> reference) {
        final GuestService service = context.getService(reference);
        ServerServiceRegistry.getInstance().addService(GuestService.class, service);
        return service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifiedService(final ServiceReference<GuestService> reference, final GuestService service) {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removedService(final ServiceReference<GuestService> reference, final GuestService service) {
        context.ungetService(reference);
        ServerServiceRegistry.getInstance().removeService(GuestService.class);
    }
}
