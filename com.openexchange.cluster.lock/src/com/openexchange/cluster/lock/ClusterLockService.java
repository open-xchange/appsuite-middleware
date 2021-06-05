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

package com.openexchange.cluster.lock;

import com.openexchange.exception.OXException;
import com.openexchange.policy.retry.RetryPolicy;

/**
 * {@link ClusterLockService}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface ClusterLockService {

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
     * for this specific task. The current thread will acquire the lock for a predefined amount
     * of time. This method will either run the cluster task or not depending on whether the
     * cluster lock was acquired. If not an exception will be thrown
     * 
     * @param clusterTask The {@link ClusterTask} to perform
     * @return The result {@link T}
     * @throws OXException if an error is occurred during the execution of the task or if the acquisition
     *             of the cluster lock fails
     */
    <T> T runClusterTask(ClusterTask<T> clusterTask) throws OXException;

    /**
     * Runs the specified cluster task while previously acquiring a lock on the entire cluster
     * for this specific task. The current thread will acquire the lock for a predefined amount
     * of time. The amount of retries to acquire the lock is depended on the specified {@link RetryPolicy}.
     * If the lock is not acquired after the predefined amount of retries an exception will be thrown.
     * 
     * @param clusterTask The {@link ClusterTask} to perform
     * @param retryPolicy The {@link RetryPolicy} for acquiring a lock
     * @return The result {@link T}
     * @throws OXException if an error is occurred during the execution or if the acquisition
     *             of the cluster lock fails
     */
    <T> T runClusterTask(ClusterTask<T> clusterTask, RetryPolicy retryPolicy) throws OXException;
}
