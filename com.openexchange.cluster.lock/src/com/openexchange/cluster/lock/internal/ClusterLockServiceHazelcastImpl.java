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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.openexchange.cluster.lock.ClusterTask;
import com.openexchange.cluster.lock.policies.RetryPolicy;
import com.openexchange.cluster.lock.policies.RunOnceRetryPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ClusterLockServiceHazelcastImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterLockServiceHazelcastImpl extends AbstractClusterLockServiceImpl {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ClusterLockServiceHazelcastImpl.class);

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
    private HazelcastInstance getHazelcastInstance() throws OXException {
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

        private String taskName;

        /**
         * Initialises a new {@link RefreshLockTask}.
         * 
         * @param taskName The cluster task's name to refresh
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
}
