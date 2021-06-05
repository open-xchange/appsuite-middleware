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

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link FixedExecutorService} - An {@link ExecutorService} backed by another {@link ExecutorService} using a fixed number of active
 * threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FixedExecutorService extends AbstractExecutorService {

    private final class PollingRunnable implements Runnable {

        private final Runnable initialTask;

        PollingRunnable(Runnable initialTask) {
            super();
            this.initialTask = initialTask;
        }

        @Override
        public void run() {
            initialTask.run();

            for (Runnable nextTask; (nextTask = pollFromQueue()) != null;) {
                nextTask.run();
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ExecutorService executorService;
    private final int size;
    private final Lock lock;
    private final Queue<Runnable> queue;
    private int count;

    /**
     * Initializes a new {@link FixedExecutorService}.
     *
     * @param size The number of threads that are allowed being used concurrently
     * @param executorService The executor to delegate to
     */
    public FixedExecutorService(final int size, final ExecutorService executorService) {
        super();
        lock = new ReentrantLock();
        this.size = size;
        queue = new LinkedList<Runnable>();
        count = 0;
        this.executorService = executorService;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(final Runnable command) {
        lock.lock();
        try {
            if (queue.isEmpty() && count < size) {
                /*
                 * Pass to execute() and leave
                 */
                executorService.execute(new PollingRunnable(command));
                count++;
            } else {
                /*
                 * Too many in-progress commands or queue not empty. Enqueue and leave
                 */
                queue.offer(command);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Polls next available command from queue. If queue is not empty, command is immediately passed to {@link #execute(Runnable)} method;
     * otherwise counter is decremented to signal a free resource for further processing of passed {@link Runnable commands}.
     *
     * @return <code>Runnable</code> if queue was not empty ; otherwise <code>null</code>
     */
    protected Runnable pollFromQueue() {
        lock.lock();
        try {
            final Runnable command = queue.poll();
            if (null != command) {
                /*
                 * Continue executing next queued command
                 */
                return command;
            }
            /*
             * No queued command available. Signal free thread for processing.
             */
            count--;
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void shutdown() {
        // No shut-down
        throw new UnsupportedOperationException("Shutdown not allowed.");
    }

    @Override
    public List<Runnable> shutdownNow() {
        // No shut-down
        throw new UnsupportedOperationException("Shutdown not allowed.");
    }

}
