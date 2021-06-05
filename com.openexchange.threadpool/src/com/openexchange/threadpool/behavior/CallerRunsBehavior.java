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
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CallerRunsBehavior} - Implements "Caller-Runs" behavior.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CallerRunsBehavior implements RefusedExecutionBehavior<Object> {

    private static final CallerRunsBehavior INSTANCE = new CallerRunsBehavior();

    /**
     * Gets the "Caller-Runs" behavior.
     *
     * @return The "Caller-Runs" behavior
     */
    @SuppressWarnings("unchecked")
    public static <V> RefusedExecutionBehavior<V> getInstance() {
        return (RefusedExecutionBehavior<V>) INSTANCE;
    }

    /**
     * Initializes a new {@link CallerRunsBehavior}.
     */
    private CallerRunsBehavior() {
        super();
    }

    /**
     * Executes task in the caller's thread, unless the thread pool has been shut down, in which case the task is discarded.
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
            return ThreadPools.execute(task);
        }
        return DISCARDED;
    }

}
