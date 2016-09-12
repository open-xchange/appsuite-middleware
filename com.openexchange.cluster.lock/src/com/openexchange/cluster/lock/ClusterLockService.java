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

package com.openexchange.cluster.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;

/**
 * {@link ClusterLockService}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface ClusterLockService {

    /**
     * Acquire a cluster lock.
     * 
     * @param node The action that is going to be performed cluster-wise
     * @return The lock
     * @throws OXException if the cluster is already locked for that action
     */
    public Lock acquireClusterLock(String action) throws OXException;

    /**
     * Release a cluster lock previously acquired via {@link ClusterLockService.acquireClusterLock}.
     * 
     * @param action the action that was performed cluster-wise
     * @param lock The lock
     * @throws OXException
     */
    void releaseClusterLock(String action, Lock lock) throws OXException;

    /**
     * Acquires a cluster lock for the specified {@link ClusterTask}
     * 
     * @param clusterTask The {@link ClusterTask} for which to acquire the cluster lock
     * @return <code>true</code> if the cluster lock was successfully acquired, <code>false</code> otherwise
     * @throws OXException if an error is occurred during the acquisition of the cluster lock
     */
    <T> boolean acquireClusterLock(ClusterTask<T> clusterTask) throws OXException;

    /**
     * Releases the cluster lock that was previously acquired for the specified {@link ClusterTask}
     * 
     * @param clusterTask The {@link ClusterTask} for which to release the lock
     * @throws OXException if an error is occurred during the release of the cluster lock
     */
    <T> void releaseClusterLock(ClusterTask<T> clusterTask) throws OXException;

    /**
     * Runs the specified cluster task while previously acquiring a lock on the entire cluster
     * for this specific task. The current thread will acquire the lock for an indefinite amount
     * of time, i.e. until it's done; threads waiting to acquire the same lock will wait indefinitely.
     * 
     * @param clusterTask The {@link ClusterTask} to perform
     * @return The result {@link T}
     * @throws OXException if an error is occurred during the execution
     */
    <T> T runClusterTask(ClusterTask<T> clusterTask) throws OXException;

    /**
     * Runs the specified cluster task while previously acquiring a lock on the entire cluster
     * for this specific task. The current thread will wait for the specified amount of seconds
     * to acquire a lock. If the time expires an {@link OXException} will be thrown.
     * 
     * @param clusterTask The {@link ClusterTask} to perform
     * @param waitTime The amount of time to wait in order to acquire a lock
     * @return {@link T}
     * @throws OXException if an error is occurred during the execution
     */
    <T> T runClusterTask(ClusterTask<T> clusterTask, long waitTime) throws OXException;

    /**
     * Runs the specified cluster task while previously acquiring a lock on the entire cluster
     * for this specific task. The current thread will wait for the specified amount of time units
     * to acquire a lock. If the time expires an {@link OXException} will be thrown.
     * 
     * @param clusterTask The {@link ClusterTask} to perform
     * @param waitTime The amount of time to wait in order to acquire a lock
     * @param timeUnit The {@link TimeUnit}
     * @return {@link T}
     * @throws OXException if an error is occurred during the execution
     */
    <T> T runClusterTask(ClusterTask<T> clusterTask, long waitTime, TimeUnit timeUnit) throws OXException;

    /**
     * Acquire a periodic cluster lock.
     * 
     * @param action
     * @param period
     * @return The lock
     * @throws OXException
     */
    public Lock acquirePeriodicClusterLock(String action, long period) throws OXException;

    /**
     * Release a periodic cluster lock.
     * 
     * @param action
     * @throws OXException
     */
    public void releasePeriodicClusterLock(String action) throws OXException;
}
