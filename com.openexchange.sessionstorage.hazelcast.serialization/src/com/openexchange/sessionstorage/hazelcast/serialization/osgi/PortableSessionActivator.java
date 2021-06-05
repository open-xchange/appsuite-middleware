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

package com.openexchange.sessionstorage.hazelcast.serialization.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableMultipleActiveSessionRemoteLookupFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableMultipleSessionRemoteLookupFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionCollectionFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionExistenceCheckFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteLookupFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteRetrievalFactory;


/**
 * {@link PortableSessionActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableSessionActivator implements BundleActivator {

    private List<ServiceRegistration<CustomPortableFactory>> portablesRegistrations;
    private List<ServiceTracker<?, ?>> trackers;

    /**
     * Initializes a new {@link PortableSessionActivator}.
     */
    public PortableSessionActivator() {
        super();
    }

    @Override
    public synchronized void start(BundleContext context) throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PortableSessionActivator.class);
        try {
            // Trackers
            List<ServiceTracker<?, ?>> trackers = new ArrayList<ServiceTracker<?, ?>>(4);
            this.trackers = trackers;
            {
                ServiceTracker<SessiondService, SessiondService> tracker = new ServiceTracker<SessiondService, SessiondService>(context, SessiondService.class, new SessiondServiceTracker(context));
                trackers.add(tracker);
                tracker.open();
            }
            {
                ServiceTracker<ObfuscatorService, ObfuscatorService> tracker = new ServiceTracker<ObfuscatorService, ObfuscatorService>(context, ObfuscatorService.class, new ObfuscatorServiceTracker(context));
                trackers.add(tracker);
                tracker.open();
            }

            List<ServiceRegistration<CustomPortableFactory>> portablesRegistrations = new ArrayList<ServiceRegistration<CustomPortableFactory>>(8);
            this.portablesRegistrations = portablesRegistrations;

            // Create & register portable session factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionExistenceCheckFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionRemoteLookupFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionRemoteRetrievalFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionCollectionFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableMultipleSessionRemoteLookupFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableMultipleActiveSessionRemoteLookupFactory(), null));

            logger.info("Successfully started bundle {}", context.getBundle().getSymbolicName());
        } catch (Exception e) {
            logger.error("Failed to start bundle {}", context.getBundle().getSymbolicName(), e);
        }
    }

    @Override
    public synchronized void stop(BundleContext context) throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(PortableSessionActivator.class);
        try {
            {
                List<ServiceTracker<?, ?>> trackers = this.trackers;
                if (null != trackers) {
                    this.trackers = null;
                    for (ServiceTracker<?,?> tracker : trackers) {
                        tracker.close();
                    }
                }
            }

            {
                List<ServiceRegistration<CustomPortableFactory>> portablesRegistrations = this.portablesRegistrations;
                if (null != portablesRegistrations) {
                    this.portablesRegistrations = null;
                    for (ServiceRegistration<CustomPortableFactory> registration : portablesRegistrations) {
                        registration.unregister();
                    }
                }
            }
            logger.info("Successfully stopped bundle {}", context.getBundle().getSymbolicName());
        } catch (Exception e) {
            logger.error("Failed to stop bundle {}", context.getBundle().getSymbolicName(), e);
        }
    }

}
