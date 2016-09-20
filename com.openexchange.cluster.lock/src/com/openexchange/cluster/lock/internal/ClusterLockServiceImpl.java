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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.openexchange.cluster.lock.ClusterLockService;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.cluster.lock.policies.RetryPolicy;
import com.openexchange.cluster.lock.policies.RunOnceRetryPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link ClusterLockServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterLockServiceImpl implements ClusterLockService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ClusterLockServiceImpl.class);

    private final Unregisterer unregisterer;
    private final ServiceLookup services;

    /** Defines the threshold for the lock refresh in seconds */
    private static final long REFRESH_LOCK_THRESHOLD = TimeUnit.SECONDS.toNanos(20);

    /** Defines the ttl for a cluster lock in seconds */
    private static final long LOCK_TTL = TimeUnit.SECONDS.toNanos(30);

    /**
     * Initializes a new {@link ClusterLockServiceImpl}.
     */
    public ClusterLockServiceImpl(ServiceLookup services, Unregisterer unregisterer) {
        super();
        this.services = services;
        this.unregisterer = unregisterer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#acquireClusterLock(com.openexchange.cluster.lock.ClusterTask)
     */
    @Override
    public <T> boolean acquireClusterLock(ClusterTask<T> clusterTask) throws OXException {
        HazelcastInstance hzInstance = getHazelcastInstance();

        IMap<String, Long> clusterLocks = hzInstance.getMap(ClusterLockType.ClusterTaskLocks.name());
        long timeNow = System.nanoTime();
        Long timeThen = clusterLocks.putIfAbsent(clusterTask.getTaskName(), timeNow);
        if (timeThen == null) {
            return true;
        }

        if (!leaseExpired(timeNow, timeThen)) {
            return false;
        }

        return clusterLocks.replace(clusterTask.getTaskName(), timeThen, timeNow);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#releaseClusterLock(com.openexchange.cluster.lock.ClusterTask)
     */
    @Override
    public <T> void releaseClusterLock(ClusterTask<T> clusterTask) throws OXException {
        HazelcastInstance hzInstance = getHazelcastInstance();
        IMap<String, Long> map = hzInstance.getMap(ClusterLockType.ClusterTaskLocks.name());
        map.remove(clusterTask.getTaskName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#runClusterTask(com.openexchange.cluster.lock.ClusterTask)
     */
    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask) throws OXException {
        return runClusterTask(clusterTask, new RunOnceRetryPolicy());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#runClusterTask(com.openexchange.cluster.lock.ClusterTask, com.openexchange.cluster.lock.RetryPolicy)
     */
    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask, RetryPolicy retryPolicy) throws OXException {
        do {
            // Acquire the lock
            boolean lockAcquired = acquireClusterLock(clusterTask);
            ScheduledTimerTask timerTask = null;
            try {
                if (lockAcquired) {
                    LOGGER.debug("Cluster lock for cluster task '{}' acquired with retry policy '{}'", clusterTask.getTaskName(), retryPolicy.getClass().getSimpleName());
                    TimerService service = services.getService(TimerService.class);
                    timerTask = service.scheduleAtFixedRate(new RefreshLockTask(clusterTask.getTaskName()), REFRESH_LOCK_THRESHOLD, REFRESH_LOCK_THRESHOLD);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#runTask(com.openexchange.cluster.lock.ClusterTask)
     */
    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask, long waitTime) throws OXException {
        return runClusterTask(clusterTask, waitTime, TimeUnit.SECONDS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#runClusterTask(com.openexchange.cluster.lock.ClusterTask, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public <T> T runClusterTask(ClusterTask<T> clusterTask, long waitTime, TimeUnit timeUnit) throws OXException {
        HazelcastInstance hzInstance = getHazelcastInstance();
        if (hzInstance == null) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }

        // Acquire the lock
        ILock lock = hzInstance.getLock(clusterTask.getTaskName());
        try {
            if (lock.tryLock(waitTime, timeUnit)) {
                try {
                    LOGGER.debug("Cluster lock for cluster task '{}' acquired. Performing...", clusterTask.getTaskName());
                    return clusterTask.perform();
                } finally {
                    LOGGER.debug("Cluster task '{}' completed. Releasing cluster lock.", clusterTask.getTaskName());
                    lock.unlock();
                }
            } else {
                throw ClusterLockExceptionCodes.UNABLE_TO_ACQUIRE_CLUSTER_LOCK_EXPIRED.create(clusterTask.getTaskName(), waitTime, timeUnit.name().toLowerCase());
            }
        } catch (InterruptedException e) {
            throw ClusterLockExceptionCodes.INTERRUPTED.create(e, clusterTask.getTaskName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#acquirePeriodicClusterLock(java.lang.String, long)
     */
    @Override
    public Lock acquirePeriodicClusterLock(final String action, final long period) throws OXException {
        final long now = System.currentTimeMillis();
        final ConcurrentMap<String, Long> map = getPeriodicHzMap();
        final Long timestamp = map.get(action);
        if (timestamp != null) {
            if (now - timestamp.longValue() < period) {
                throw ClusterLockExceptionCodes.CLUSTER_PERIODIC_LOCKED.create(period, action, period - (now - timestamp));
            }
        }
        HazelcastInstance hazelcastInstance = getHazelcastInstance();
        if (hazelcastInstance == null) {
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }
        final Lock lock = hazelcastInstance.getLock(action);
        final Long futureTS = map.putIfAbsent(action, Long.valueOf(now));
        if (futureTS != null) {
            throw ClusterLockExceptionCodes.CLUSTER_PERIODIC_LOCKED.create(period, action, period - (now - futureTS));
        }

        final TimerService timerService = services.getService(TimerService.class);
        timerService.schedule(new ReleasePeriodicClusterLock(action), period, TimeUnit.MILLISECONDS);

        return lock;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cluster.lock.ClusterLockService#releasePeriodicClusterLock(java.lang.String)
     */
    @Override
    public void releasePeriodicClusterLock(String action) throws OXException {
        final ConcurrentMap<String, Long> map = getPeriodicHzMap();
        map.remove(action);
    }

    ///////////////////////////////////// HELPERS ///////////////////////////////////////

    private HazelcastInstance getHazelcastInstance() throws OXException {
        HazelcastInstance hazelcastInstance = services.getOptionalService(HazelcastInstance.class);
        if (hazelcastInstance == null) {
            LOGGER.warn("The Hazelcast service is not available on this node.");
            throw ServiceExceptionCode.absentService(HazelcastInstance.class);
        }
        return hazelcastInstance;
    }

    private IMap<String, Long> getPeriodicHzMap() throws OXException {
        try {
            HazelcastInstance hazelcastInstance = getHazelcastInstance();
            if (hazelcastInstance == null) {
                throw ServiceExceptionCode.absentService(HazelcastInstance.class);
            }
            return hazelcastInstance.getMap("PeriodicClusterLocks");
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    /**
     * Handles the {@link HazelcastInstanceNotActiveException}
     * 
     * @param e The {@link HazelcastInstanceNotActiveException} to handle
     * @return A 'SERVICE_UNAVAILABLE' {@link OXException}
     */
    private OXException handleNotActiveException(HazelcastInstanceNotActiveException e) {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(ClusterLockServiceImpl.class);
        logger.warn("Encountered a {} error. {} will be shut-down!", HazelcastInstanceNotActiveException.class.getSimpleName(), ClusterLockServiceImpl.class);
        unregisterer.propagateNotActive(e);
        unregisterer.unregister();
        return ServiceExceptionCode.absentService(HazelcastInstance.class);
    }

    private class ReleasePeriodicClusterLock implements Runnable {

        private final String action;

        /**
         * Initializes a new {@link ReleasePeriodicClusterLock}.
         * 
         * @param action
         * @param period
         */
        public ReleasePeriodicClusterLock(String action) {
            super();
            this.action = action;
        }

        @Override
        public void run() {
            try {
                releasePeriodicClusterLock(action);
            } catch (OXException e) {
                final Logger log = LoggerFactory.getLogger(ClusterLockServiceImpl.class);
                log.warn("Unable to release periodic lock for action {}", action, e);
            }
        }
    }

    /**
     * {@link RefreshLockTask} - Refreshes the lock expiration timestamp for the specified task
     */
    private class RefreshLockTask implements Runnable {

        private String taskName;

        /**
         * Initialises a new {@link ClusterLockServiceImpl.RefreshLockTask}.
         */
        public RefreshLockTask(String taskName) {
            super();
            this.taskName = taskName;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                HazelcastInstance hzInstance = getHazelcastInstance();
                if (hzInstance == null) {
                    throw ServiceExceptionCode.absentService(HazelcastInstance.class);
                }

                IMap<String, Long> clusterLocks = hzInstance.getMap(ClusterLockType.ClusterTaskLocks.name());
                LOGGER.debug("Refreshing lock for cluster task '{}'", taskName);
                long timeNow = System.nanoTime();
                clusterLocks.put(taskName, timeNow);
            } catch (OXException e) {
                LOGGER.error("{}", e.getMessage(), e);
                return;
            }
        }
    }

    /**
     * Verifies whether the lease time was expired
     * 
     * @param timeNow The time now
     * @param timeThen The time then
     * @return <code>true</code> if the lease time was expired; <code>false</code> otherwise
     */
    private boolean leaseExpired(long timeNow, long timeThen) {
        return (timeNow - timeThen > LOCK_TTL);
    }
}
