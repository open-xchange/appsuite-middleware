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

package com.openexchange.push.udp.osgi;

import static com.openexchange.push.udp.registry.PushServiceRegistry.getServiceRegistry;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.event.EventFactoryService;
import com.openexchange.folder.FolderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.push.udp.PushHandler;
import com.openexchange.push.udp.PushInit;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

/**
 * {@link PushUDPActivator} - OSGi bundle activator for the push UDP.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class PushUDPActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushUDPActivator.class);

    /**
     * Initializes a new {@link PushUDPActivator}.
     */
    public PushUDPActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, EventAdmin.class, EventFactoryService.class, ContextService.class, FolderService.class, ThreadPoolService.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        getServiceRegistry().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        getServiceRegistry().addService(clazz, getService(clazz));
    }

    @Override
    public void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final ServiceRegistry registry = getServiceRegistry();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (final Class<?> classe : classes) {
                    final Object service = getService(classe);
                    if (null != service) {
                        registry.addService(classe, service);
                    }
                }
            }
            /*
             * Start-up
             */
            PushInit.getInstance().start();
            registerEventHandler();
            /*
             * Service trackers
             */
            rememberTracker(new ServiceTracker<TimerService,TimerService>(context, TimerService.class, new TimerCustomizer(context)));
            openTrackers();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            /*
             * Close service trackers
             */
            super.stopBundle();
            /*
             * Shut-down
             */
            PushInit.getInstance().stop();
            /*
             * Clear service registry
             */
            getServiceRegistry().clearRegistry();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    protected void registerEventHandler() {
        final Hashtable<String, Object> ht = new Hashtable<String, Object>(1);
        ht.put(EventConstants.EVENT_TOPIC, new String[] { EventConstants.EVENT_TOPIC, "com/openexchange/*" });
        registerService(EventHandler.class, new PushHandler(), ht);
    }

}
