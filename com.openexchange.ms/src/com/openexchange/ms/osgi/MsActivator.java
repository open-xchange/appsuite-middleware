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

package com.openexchange.ms.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.caching.CacheService;
import com.openexchange.database.DatabaseService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.ms.MsEventConstants;
import com.openexchange.ms.MsService;
import com.openexchange.ms.PortableMsService;
import com.openexchange.ms.internal.HzMsService;
import com.openexchange.ms.internal.Services;
import com.openexchange.ms.internal.Unregisterer;
import com.openexchange.ms.internal.portable.PortableContextInvalidationCallableFactory;
import com.openexchange.ms.internal.portable.PortableHzMsService;
import com.openexchange.ms.internal.portable.PortableMessageFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

/**
 * {@link MsActivator} - The activator for <i>"com.openexchange.ms"</i> bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MsActivator extends HousekeepingActivator implements Unregisterer {

    private ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker;

    /**
     * Initializes a new {@link MsActivator}.
     */
    public MsActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, TimerService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        Services.setServiceLookup(this);
        Unregisterer.INSTANCE_REF.set(this);
        final HazelcastConfigurationService configService = getService(HazelcastConfigurationService.class);
        final boolean enabled = configService.isEnabled();
        if (enabled) {
            /*
             * create & register portable message factory
             */
            registerService(CustomPortableFactory.class, new PortableMessageFactory());
            registerService(CustomPortableFactory.class, new PortableContextInvalidationCallableFactory());
            /*
             * start ms services based on hazelcast instance's lifecycle
             */
            final BundleContext context = this.context;
            ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker = new ServiceTracker<HazelcastInstance, HazelcastInstance>(context, HazelcastInstance.class, new ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>() {

                private HzMsService msService;

                @Override
                public synchronized HazelcastInstance addingService(final ServiceReference<HazelcastInstance> reference) {
                    if (msService != null) {
                        return null;
                    }

                    // Get HazelcastInstance from service reference
                    HazelcastInstance hazelcastInstance = context.getService(reference);
                    HzMsService msService = new HzMsService(hazelcastInstance);
                    this.msService = msService;
                    registerService(MsService.class, msService);

                    PortableMsService portableMsService = new PortableHzMsService(hazelcastInstance);
                    registerService(PortableMsService.class, portableMsService);

                    registerEventHandler(msService);
                    return hazelcastInstance;
                }

                @Override
                public void modifiedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance hazelcastInstance) {
                    // Ignore
                }

                @Override
                public synchronized void removedService(final ServiceReference<HazelcastInstance> reference, final HazelcastInstance hazelcastInstance) {
                    if (null != hazelcastInstance) {
                        HzMsService msService = this.msService;
                        if (null != msService) {
                            unregisterServices();
                            msService.shutDown();
                            this.msService = null;
                        }
                        context.ungetService(reference);
                    }
                }
            });
            hzTracker.open();
            this.hzTracker = hzTracker;
            trackService(DatabaseService.class);
            trackService(CacheService.class);

            // Open other
            openTrackers();
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker = this.hzTracker;
        if (null != hzTracker) {
            hzTracker.close();
            this.hzTracker = null;
        }

        super.stopBundle();
        Services.setServiceLookup(null);
        Unregisterer.INSTANCE_REF.set(null);
    }

    @Override
    public synchronized void unregisterMsService() {
        ServiceTracker<HazelcastInstance, HazelcastInstance> hzTracker = this.hzTracker;
        if (null != hzTracker) {
            hzTracker.close();
            this.hzTracker = null;
        }
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        BundleContext context = this.context;
        if (null != context) {
            registerService(HazelcastInstanceNotActiveException.class, notActiveException);
        }
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    public void unregisterServices() {
        super.unregisterServices();
    }

    /**
     * Registers the event handler.
     *
     * @param msService The associated service used to remotely re-publish received events.
     */
    protected void registerEventHandler(final HzMsService msService) {
        // Register event handler
        final Dictionary<String, Object> dict = new Hashtable<String, Object>(2);
        dict.put(EventConstants.EVENT_TOPIC, MsEventConstants.getAllTopics());
        registerService(EventHandler.class, new MsEventHandlerImpl(msService), dict);
    }

}
