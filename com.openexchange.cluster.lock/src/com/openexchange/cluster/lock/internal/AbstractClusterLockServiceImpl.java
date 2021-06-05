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

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.exception.OXException;
import com.openexchange.policy.retry.RetryPolicy;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link AbstractClusterLockServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractClusterLockServiceImpl implements ClusterLockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClusterLockServiceImpl.class);

    /** Defines the threshold for the lock refresh in milliseconds */
    private static final long REFRESH_LOCK_THRESHOLD_MILLIS = TimeUnit.SECONDS.toMillis(20);

    /** Defines the TTL for a cluster lock in milliseconds */
    private static final long LOCK_TTL_MILLIS = TimeUnit.SECONDS.toMillis(30);

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractClusterLockServiceImpl}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    protected AbstractClusterLockServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Verifies whether the lease time was expired
     *
     * @param timeNow The time now
     * @param timeThen The time then
     * @return <code>true</code> if the lease time was expired; <code>false</code> otherwise
     */
    protected boolean leaseExpired(long timeNow, long timeThen) {
        return (timeNow - timeThen > LOCK_TTL_MILLIS);
    }

    /**
     * Runs the specified {@link ClusterTask} with the specified {@link RetryPolicy} and the specified refresh lock {@link Runnable} task
     *
     * @param clusterTask The {@link ClusterTask} to execute
     * @param retryPolicy The {@link RetryPolicy}
     * @param refreshLockTask The {@link Runnable} refresh lock task
     * @return The result {@link T} of the {@link ClusterTask} execution
     * @throws OXException if an error is occurred during the execution of the task, or if the lock acquisition failed
     */
    protected <T> T runClusterTask(ClusterTask<T> clusterTask, RetryPolicy retryPolicy, Runnable refreshLockTask) throws OXException {
        do {
            // Acquire the lock
            boolean lockAcquired = acquireClusterLock(clusterTask);
            ScheduledTimerTask timerTask = null;
            try {
                if (lockAcquired) {
                    LOGGER.debug("Cluster lock for cluster task '{}' acquired with retry policy '{}'", clusterTask.getTaskName(), retryPolicy.getClass().getSimpleName());
                    TimerService service = services.getService(TimerService.class);
                    timerTask = service.scheduleAtFixedRate(refreshLockTask, REFRESH_LOCK_THRESHOLD_MILLIS, REFRESH_LOCK_THRESHOLD_MILLIS, TimeUnit.MILLISECONDS);
                    T t = clusterTask.perform();
                    LOGGER.debug("Cluster task '{}' completed.", clusterTask.getTaskName());
                    return t;
                }
                LOGGER.debug("Another node is performing the cluster task '{}'. Cluster lock was not acquired.", clusterTask.getTaskName(), retryPolicy.getClass().getSimpleName());
            } finally {
                LOGGER.debug("Releasing cluster lock held by the cluster task '{}'.", clusterTask.getTaskName());
                if (lockAcquired) {
                    if (timerTask != null) {
                        timerTask.cancel();
                    }
                    releaseClusterLock(clusterTask);
                }
            }
        } while (retryPolicy.isRetryAllowed());

        // Failed to acquire lock permanently
        throw ClusterLockExceptionCodes.UNABLE_TO_ACQUIRE_CLUSTER_LOCK.create(clusterTask.getTaskName());
    }
}
