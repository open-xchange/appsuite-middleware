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

package com.openexchange.cluster.lock.internal;

import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.map.IMap;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.exception.OXException;
import com.openexchange.policy.retry.RetryPolicy;
import com.openexchange.policy.retry.RunOnceRetryPolicy;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ClusterLockServiceHazelcastImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterLockServiceHazelcastImpl extends AbstractClusterLockServiceImpl {

    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ClusterLockServiceHazelcastImpl.class);

    private final Unregisterer unregisterer;

    /**
     * Initialises a new {@link ClusterLockServiceHazelcastImpl}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param unregisterer The {@link Unregisterer} instance
     */
    public ClusterLockServiceHazelcastImpl(ServiceLookup services, Unregisterer unregisterer) {
        super(services);
        this.unregisterer = unregisterer;
    }

    @Override
    public <T> boolean acquireClusterLock(ClusterTask<T> clusterTask) throws OXException {
        HazelcastInstance hzInstance = getHazelcastInstance();

        IMap<String, Long> clusterLocks = hzInstance.getMap(ClusterLockType.ClusterTaskLocks.name());
        long timeNow = System.currentTimeMillis();
        Long timeThen = clusterLocks.putIfAbsent(clusterTask.getTaskName(), L(timeNow));
        if (timeThen == null) {
            return true;
        }

        if (!leaseExpired(timeNow, l(timeThen))) {
            return false;
        }

        return clusterLocks.replace(clusterTask.getTaskName(), timeThen, L(timeNow));
    }

    @Override
    public <T> void releaseClusterLock(ClusterTask<T> clusterTask) throws OXException {
        HazelcastInstance hzInstance = getHazelcastInstance();
        IMap<String, Long> map = hzInstance.getMap(ClusterLockType.ClusterTaskLocks.name());
        map.remove(clusterTask.getTaskName());
    }

    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask) throws OXException {
        return runClusterTask(clusterTask, new RunOnceRetryPolicy());
    }

    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask, RetryPolicy retryPolicy) throws OXException {
        try {
            return runClusterTask(clusterTask, retryPolicy, new RefreshLockTask(clusterTask.getTaskName()));
        } catch (HazelcastInstanceNotActiveException e) {
            LOGGER.warn("Encountered a {} error. {} will be shut-down!", HazelcastInstanceNotActiveException.class.getSimpleName(), ClusterLockServiceHazelcastImpl.class, e);
            unregisterer.propagateNotActive(e);
            unregisterer.unregister();
        }

        // Failed to acquire lock permanently
        throw ClusterLockExceptionCodes.UNABLE_TO_ACQUIRE_CLUSTER_LOCK.create(clusterTask.getTaskName());
    }

    ///////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Returns the {@link HazelcastInstance}. If the instance cannot be returned (i.e. due its absence)
     * then an {@link OXException} will be thrown
     *
     * @return The {@link HazelcastInstance}
     * @throws OXException if the {@link HazelcastInstance} is absent
     */
    HazelcastInstance getHazelcastInstance() throws OXException {
        HazelcastInstance hazelcastInstance = services.getOptionalService(HazelcastInstance.class);
        if (hazelcastInstance == null) {
            LOGGER.warn("The Hazelcast service is not available on this node.");
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }
        return hazelcastInstance;
    }

    /**
     * {@link RefreshLockTask} - Refreshes the lock expiration timestamp for the specified task
     */
    private class RefreshLockTask implements Runnable {

        private final String taskName;

        /**
         * Initialises a new {@link RefreshLockTask}.
         *
         * @param taskName The cluster task's name to refresh
         */
        public RefreshLockTask(String taskName) {
            super();
            this.taskName = taskName;
        }

        @Override
        public void run() {
            try {
                HazelcastInstance hzInstance = getHazelcastInstance();

                IMap<String, Long> clusterLocks = hzInstance.getMap(ClusterLockType.ClusterTaskLocks.name());
                LOGGER.debug("Refreshing lock for cluster task '{}'", taskName);
                long timeNow = System.currentTimeMillis();
                clusterLocks.put(taskName, L(timeNow));
            } catch (OXException e) {
                LOGGER.error("{}", e.getMessage(), e);
                return;
            }
        }
    }
}
