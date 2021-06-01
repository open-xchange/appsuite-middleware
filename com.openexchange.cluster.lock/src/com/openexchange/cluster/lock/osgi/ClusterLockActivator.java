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

package com.openexchange.cluster.lock.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.internal.ClusterLockServiceDatabaseImpl;
import com.openexchange.cluster.lock.internal.ClusterLockServiceHazelcastImpl;
import com.openexchange.cluster.lock.internal.Unregisterer;
import com.openexchange.cluster.lock.internal.groupware.ClusterLockConvertUtf8ToUtf8mb4Task;
import com.openexchange.cluster.lock.internal.groupware.ClusterLockCreateTableTask;
import com.openexchange.cluster.lock.internal.groupware.CreateClusterLockTable;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

/**
 * {@link ClusterLockActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterLockActivator extends HousekeepingActivator implements Unregisterer {

    private ServiceTracker<HazelcastInstance, HazelcastInstance> tracker;

    /**
     * Initializes a new {@link ClusterLockActivator}.
     */
    public ClusterLockActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastConfigurationService.class, DatabaseService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final HazelcastConfigurationService hzConfigService = getService(HazelcastConfigurationService.class);
        final boolean enabled = hzConfigService.isEnabled();

        if (false == enabled) {
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new ClusterLockCreateTableTask(), new ClusterLockConvertUtf8ToUtf8mb4Task()));
            registerService(CreateTableService.class, new CreateClusterLockTable(), null);
            registerService(ClusterLockService.class, new ClusterLockServiceDatabaseImpl(this));
        } else {
            registerService(ClusterLockService.class, new ClusterLockServiceHazelcastImpl(this, this));
            tracker = track(HazelcastInstance.class, new HazelcastClusterLockServiceTracker(context));
            openTrackers();
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterService(ClusterLockService.class);
        super.stopBundle();
    }

    @Override
    public <S> void registerService(Class<S> clazz, S service) {
        super.registerService(clazz, service);
    }

    @Override
    public <S> void unregisterService(S service) {
        super.unregisterService(service);
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
        final BundleContext context = this.context;
        if (null != context) {
            context.registerService(HazelcastInstanceNotActiveException.class, notActiveException, null);
        }
    }

}
