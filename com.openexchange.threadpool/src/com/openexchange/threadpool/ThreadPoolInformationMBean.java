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

package com.openexchange.threadpool;

/**
 * {@link ThreadPoolInformationMBean} - The MBean for thread pool information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ThreadPoolInformationMBean {

    /**
     * The thread pool domain.
     */
    public static final String THREAD_POOL_DOMAIN = "com.openexchange.threadpool";

    /**
     * Returns the current number of threads in the pool.
     *
     * @return The number of threads
     */
    int getPoolSize();

    /**
     * Returns the approximate number of threads that are actively executing tasks.
     *
     * @return The number of threads
     */
    int getActiveCount();

    /**
     * Returns the largest number of threads that have ever simultaneously been in the pool.
     *
     * @return The number of threads
     */
    int getLargestPoolSize();

    /**
     * Returns the approximate total number of tasks that have been scheduled for execution. Because the states of tasks and threads may
     * change dynamically during computation, the returned value is only an approximation, but one that does not ever decrease across
     * successive calls.
     *
     * @return The number of tasks
     */
    long getTaskCount();

    /**
     * Returns the approximate total number of tasks that have completed execution. Because the states of tasks and threads may change
     * dynamically during computation, the returned value is only an approximation, but one that does not ever decrease across successive
     * calls.
     *
     * @return The number of tasks
     */
    long getCompletedTaskCount();

}
