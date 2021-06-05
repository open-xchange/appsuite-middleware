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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

/**
 * {@link BoundedCompletionService} - Enhances {@link ThreadPoolCompletionService} by a bounded behavior.
 * <p>
 * If a proper bound is set - aka <code>concurrencyLevel</code> - it defines the max. number of concurrent threads executing submitted tasks
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class BoundedCompletionService<V> extends ThreadPoolCompletionService<V> {

    private final Semaphore semaphore;

    /**
     * Initializes a new {@link BoundedCompletionService}.
     *
     * @param threadPoolService The thread pool
     * @param concurrencyLevel The max. number of concurrent threads executing submitted tasks
     */
    public BoundedCompletionService(final ThreadPoolService threadPoolService, final int concurrencyLevel) {
        super(threadPoolService);
        if (concurrencyLevel <= 0) {
            throw new IllegalArgumentException("concurrencyLevel must be greater than zero");
        }
        semaphore = new Semaphore(concurrencyLevel);
    }

    /**
     * Initializes a new {@link BoundedCompletionService}.
     *
     * @param threadPoolService The thread pool
     * @param completionQueue The blocking queue
     * @param behavior The refused execution behavior to apply
     * @param concurrencyLevel The max. number of concurrent threads executing submitted tasks
     */
    public BoundedCompletionService(final ThreadPoolService threadPoolService, final BlockingQueue<Future<V>> completionQueue, final RefusedExecutionBehavior<V> behavior, final int concurrencyLevel) {
        super(threadPoolService, completionQueue, behavior);
        if (concurrencyLevel <= 0) {
            throw new IllegalArgumentException("concurrencyLevel must be greater than zero");
        }
        semaphore = new Semaphore(concurrencyLevel);
    }

    @Override
    protected void submitFutureTask(final FutureTask<V> f) {
        try {
            semaphore.acquire();
            super.submitFutureTask(f);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void taskDone(final Future<V> task) {
        semaphore.release();
        super.taskDone(task);
    }

}
