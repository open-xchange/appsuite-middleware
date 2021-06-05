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

package com.openexchange.threadpool.behavior;

import java.util.concurrent.RejectedExecutionException;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.internal.CustomThreadPoolExecutor;

/**
 * {@link DiscardOldestBehavior} - Implements "Discard-Oldest" behavior.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DiscardOldestBehavior implements RefusedExecutionBehavior<Object> {

    private static final DiscardOldestBehavior INSTANCE = new DiscardOldestBehavior();

    /**
     * Gets the "Discard-Oldest" behavior.
     *
     * @return The "Discard-Oldest" behavior
     */
    @SuppressWarnings("unchecked")
    public static <V> RefusedExecutionBehavior<V> newInstance() {
        return (RefusedExecutionBehavior<V>) INSTANCE;
    }

    /**
     * Initializes a new {@link DiscardOldestBehavior}.
     */
    private DiscardOldestBehavior() {
        super();
    }

    /**
     * Obtains and ignores the next task that the executor would otherwise execute, if one is immediately available, and then retries
     * execution of task, unless the executor is shut down, in which case task is instead discarded.
     *
     * @param task The task requested to be executed
     * @param threadPool The thread pool attempting to execute this task
     * @return Task's result or {@link RefusedExecutionBehavior#DISCARDED DISCARDED} constant if pool is shut down.
     * @throws Exception If task execution fails
     * @throws RejectedExecutionException If there is no remedy
     */
    @Override
    public Object refusedExecution(final Task<Object> task, final ThreadPoolService threadPool) throws Exception {
        if (!threadPool.isShutdown()) {
            ((CustomThreadPoolExecutor) threadPool.getExecutor()).getQueue().poll();
            return task.call();
        }
        return DISCARDED;
    }

}
