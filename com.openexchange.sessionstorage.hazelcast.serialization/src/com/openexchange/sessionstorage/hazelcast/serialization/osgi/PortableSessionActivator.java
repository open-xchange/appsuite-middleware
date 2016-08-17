/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionExistenceCheckFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionFactory;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteLookupFactory;


/**
 * {@link PortableSessionActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableSessionActivator implements BundleActivator {

    private volatile List<ServiceRegistration<CustomPortableFactory>> portablesRegistrations;
    private volatile List<ServiceTracker<?, ?>> trackers;

    /**
     * Initializes a new {@link PortableSessionActivator}.
     */
    public PortableSessionActivator() {
        super();
    }

    @Override
    public void start(BundleContext context) throws Exception {
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

            List<ServiceRegistration<CustomPortableFactory>> portablesRegistrations = new ArrayList<ServiceRegistration<CustomPortableFactory>>(4);
            this.portablesRegistrations = portablesRegistrations;

            // Create & register portable session factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionExistenceCheckFactory(), null));

            // Create & register portable factory
            portablesRegistrations.add(context.registerService(CustomPortableFactory.class, new PortableSessionRemoteLookupFactory(), null));

            logger.info("Successfully started bundle {}", context.getBundle().getSymbolicName());
        } catch (Exception e) {
            logger.error("Failed to start bundle {}", context.getBundle().getSymbolicName(), e);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
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
