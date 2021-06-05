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

package com.openexchange.java;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * {@link SequentialCompletionService} - Uses a single thread to execute submitted tasks sequentially.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SequentialCompletionService<V> implements CompletionService<V>, Closeable {

    @SuppressWarnings("rawtypes")
    static final FutureTask POISON = new EmptyFutureTask<Object>();

    private static class EmptyCallable<V> implements Callable<V> {

        EmptyCallable() {
            super();
        }

        @Override
        public V call() throws Exception {
            return null;
        }
    }

    private static class EmptyFutureTask<V> extends FutureTask<V> {

        EmptyFutureTask() {
            super(new EmptyCallable<V>());
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------

    private static class ExecuterCallable<V> implements Callable<Void> {

        private final BlockingQueue<FutureTask<V>> submittedTasks;
        private final BlockingQueue<Future<V>> requestTaskQueue;

        ExecuterCallable(BlockingQueue<FutureTask<V>> submittedTasks, BlockingQueue<Future<V>> requestTaskQueue) {
            super();
            this.submittedTasks = submittedTasks;
            this.requestTaskQueue = requestTaskQueue;
        }

        @SuppressWarnings("unchecked")
        void cancel() {
            submittedTasks.add(POISON);
        }

        @Override
        public Void call() throws Exception {
            boolean keepOn = true;
            List<FutureTask<V>> tasks = new ArrayList<FutureTask<V>>();

            while (keepOn) {
                FutureTask<V> first = submittedTasks.take();
                if (POISON == first) {
                    return null;
                }
                // Await its completion
                execute(first);

                tasks.clear();
                submittedTasks.drainTo(tasks);

                for (FutureTask<V> c : tasks) {
                    if (POISON == c) {
                        return null;
                    }
                    // Await its completion
                    execute(c);
                }
            }

            return null;
        }

        private void execute(FutureTask<V> task) {
            task.run();
            requestTaskQueue.offer(task);
        }
    } // End of ConsumerCallable class

    // -----------------------------------------------------------------------------------------------------------------------

    private final BlockingQueue<FutureTask<V>> submittedTasks;
    private final BlockingQueue<Future<V>> requestTaskQueue;
    private final ExecuterCallable<V> consumer;

    /**
     * Initializes a new {@link SequentialCompletionService}.
     *
     * @throws RejectedExecutionException If there is no vacant thread in thread pool
     */
    public SequentialCompletionService(ExecutorService executor) {
        super();
        if (executor == null) {
            throw new NullPointerException();
        }
        BlockingQueue<FutureTask<V>> submittedTasks = new LinkedBlockingQueue<FutureTask<V>>();
        BlockingQueue<Future<V>> requestTaskQueue = new LinkedBlockingQueue<Future<V>>();
        ExecuterCallable<V> consumer = new ExecuterCallable<V>(submittedTasks, requestTaskQueue);
        executor.submit(consumer);
        this.consumer = consumer;
        this.requestTaskQueue = requestTaskQueue;
        this.submittedTasks = submittedTasks;
    }

    /**
     * Shuts down.
     */
    public void shutDown() {
        consumer.cancel();
    }

    @Override
    public Future<V> submit(Callable<V> task) {
        FutureTask<V> ft = new FutureTask<V>(task);
        submittedTasks.offer(ft);
        return ft;
    }

    @Override
    public Future<V> submit(Runnable task, V result) {
        FutureTask<V> ft = new FutureTask<V>(task, result);
        submittedTasks.offer(ft);
        return ft;
    }

    @Override
    public Future<V> take() throws InterruptedException {
        return requestTaskQueue.take();
    }

    @Override
    public Future<V> poll() {
        return requestTaskQueue.poll();
    }

    @Override
    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return requestTaskQueue.poll(timeout, unit);
    }

    @Override
    public void close() {
        shutDown();
    }

}
