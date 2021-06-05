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

import java.util.concurrent.RejectedExecutionException;

/**
 * {@link RefusedExecutionBehavior} - The behavior for tasks that cannot be executed by a {@link ThreadPoolService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface RefusedExecutionBehavior<V> {

    /**
     * The result constant representing a discarded task.
     * <p>
     * This constant is supposed to be returned by {@link #refusedExecution(Task, ThreadPoolService)} to signal that task has been
     * discarded.
     */
    public static final Object DISCARDED = new Object();

    /**
     * Method that may be invoked by a {@link ThreadPoolService} when it cannot accept a task. This may occur when no more threads or queue
     * slots are available because their bounds would be exceeded, or upon shutdown of the thread pool. In the absence of other
     * alternatives, the method may throw an unchecked {@link RejectedExecutionException}, which will be propagated to the caller of
     * <tt>submit()</tt>.
     *
     * @param task The task requested to be executed
     * @param threadPool The thread pool attempting to execute this task
     * @return Task's result or {@link RefusedExecutionBehavior#DISCARDED DISCARDED} constant if task has been discarded
     * @throws Exception If task execution fails
     * @throws RejectedExecutionException If there is no remedy
     */
    V refusedExecution(Task<V> task, ThreadPoolService threadPool) throws Exception;

}
