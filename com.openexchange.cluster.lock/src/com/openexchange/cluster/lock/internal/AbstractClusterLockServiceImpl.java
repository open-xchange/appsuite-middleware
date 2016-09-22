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

package com.openexchange.cluster.lock.internal;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.cluster.lock.policies.RetryPolicy;
import com.openexchange.exception.OXException;
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

    /** Defines the threshold for the lock refresh in seconds */
    static final long REFRESH_LOCK_THRESHOLD = TimeUnit.SECONDS.toNanos(20);

    /** Defines the TTL for a cluster lock in seconds */
    static final long LOCK_TTL = TimeUnit.SECONDS.toNanos(30);

    protected ServiceLookup services;

    /**
     * Initialises a new {@link AbstractClusterLockServiceImpl}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    protected AbstractClusterLockServiceImpl(ServiceLookup services) {
        super();
    }

    /**
     * Verifies whether the lease time was expired
     * 
     * @param timeNow The time now
     * @param timeThen The time then
     * @return <code>true</code> if the lease time was expired; <code>false</code> otherwise
     */
    protected boolean leaseExpired(long timeNow, long timeThen) {
        return (timeNow - timeThen > LOCK_TTL);
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
                    timerTask = service.scheduleAtFixedRate(refreshLockTask, REFRESH_LOCK_THRESHOLD, REFRESH_LOCK_THRESHOLD);
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
