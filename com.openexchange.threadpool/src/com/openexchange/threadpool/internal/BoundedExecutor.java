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

package com.openexchange.threadpool.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/**
 * {@link BoundedExecutor} - Accomplishes the saturation policy to make execute block when the work queue is full.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoundedExecutor {

    private final Executor executor;
    private final Semaphore semaphore;

    /**
     * Initializes a new {@link BoundedExecutor}.
     *
     * @param executor The executor to delegate execution to
     * @param bound The capacity boundary; actually the pool size plus the number of queued tasks you want to allow
     */
    public BoundedExecutor(Executor executor, int bound) {
        super();
        this.executor = executor;
        this.semaphore = new Semaphore(bound);
    }

    /**
     * Submits specified task to executor; waits if no queue space or worker thread is immediately available.
     *
     * @param command The command to submit
     * @throws InterruptedException If interrupted while waiting for queue space or thread to become available
     * @throws RejectedExecutionException If given command cannot be accepted for execution.
     */
    public void submitTask(Runnable command) throws InterruptedException {
        if (null == command) {
            return;
        }
        Semaphore semaphore = this.semaphore;
        semaphore.acquire();
        try {
            executor.execute(new SemaphoredRunnable(semaphore, command));
        } catch (RejectedExecutionException e) {
            semaphore.release();
            throw e;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------

    private static final class SemaphoredRunnable implements Runnable {

        private final Semaphore semaphore;
        private final Runnable command;

        SemaphoredRunnable(Semaphore semaphore, Runnable command) {
            super();
            this.semaphore = semaphore;
            this.command = command;
        }

        @Override
        public void run() {
            try {
                command.run();
            } finally {
                semaphore.release();
            }
        }
    }

}
