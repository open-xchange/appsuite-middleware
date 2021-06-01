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

package com.openexchange.cluster.timer.impl.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.cluster.timer.impl.ClusterTimerServiceImpl;
import com.openexchange.cluster.timer.impl.Unregisterer;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.timer.TimerService;

/**
 * {@link ClusterTimerActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ClusterTimerActivator extends HousekeepingActivator implements Unregisterer {

    private ServiceTracker<HazelcastInstance, HazelcastInstance> tracker;

    /**
     * Initializes a new {@link ClusterTimerActivator}.
     */
    public ClusterTimerActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger LOG = LoggerFactory.getLogger(ClusterTimerActivator.class);
        /*
         * register cluster timer service
         */
        LOG.info("Starting bundle: \"com.openexchange.cluster.timer\"");
        registerService(ClusterTimerService.class, new ClusterTimerServiceImpl(ClusterTimerActivator.this, ClusterTimerActivator.this));
        if (false == getService(HazelcastConfigurationService.class).isEnabled()) {
            /*
             * node-local operation as fallback
             */
            LOG.warn("Hazelcast services are disabled, no tracking of cluster-wide task executions available.");
        } else {
            /*
             * track hazelcast for cluster-wide controlled execution
             */
            this.tracker = track(HazelcastInstance.class, new SimpleRegistryListener<HazelcastInstance>() {

                @Override
                public void added(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    addService(HazelcastInstance.class, service);
                }

                @Override
                public void removed(ServiceReference<HazelcastInstance> ref, HazelcastInstance service) {
                    removeService(HazelcastInstance.class);
                }

            });
            openTrackers();
        }
    }

    @Override
    public void unregister() {
        ServiceTracker<HazelcastInstance, HazelcastInstance> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }
    }

    @Override
    public void propagateNotActive(HazelcastInstanceNotActiveException notActiveException) {
        BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

}
