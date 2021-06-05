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

package com.openexchange.halo.events.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.events.EventsContactHalo;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link EventsHaloActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventsHaloActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link EventsHaloActivator}.
     */
    public EventsHaloActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FolderService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        /*
         * register events halo based on availability of id based calendar access factory
         */
        final BundleContext context = this.context;
        track(IDBasedCalendarAccessFactory.class, new ServiceTrackerCustomizer<IDBasedCalendarAccessFactory, IDBasedCalendarAccessFactory>() {

            private ServiceRegistration<HaloContactDataSource> eventsHaloRegistration;

            @Override
            public synchronized IDBasedCalendarAccessFactory addingService(ServiceReference<IDBasedCalendarAccessFactory> serviceReference) {
                IDBasedCalendarAccessFactory calendarAccessFactory = context.getService(serviceReference);
                eventsHaloRegistration = context.registerService(HaloContactDataSource.class, new EventsContactHalo(calendarAccessFactory, getService(FolderService.class)), null);
                return calendarAccessFactory;
            }

            @Override
            public void modifiedService(ServiceReference<IDBasedCalendarAccessFactory> serviceReference, IDBasedCalendarAccessFactory service) {
                // nothing to do
            }

            @Override
            public synchronized void removedService(ServiceReference<IDBasedCalendarAccessFactory> serviceReference, IDBasedCalendarAccessFactory service) {
                ServiceRegistration<HaloContactDataSource> eventsHaloRegistration = this.eventsHaloRegistration;
                if (null != eventsHaloRegistration) {
                    this.eventsHaloRegistration = null;
                    eventsHaloRegistration.unregister();
                }
                context.ungetService(serviceReference);
            }
        });
        openTrackers();
    }

}
