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

package com.openexchange.unifiedinbox.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.TaskWrapper;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.threadpool.Trackable;

/**
 * {@link UnifiedInboxCompletionService} - A {@link TrackingCompletionService} that uses a supplied {@link Executor} to execute tasks. This
 * class arranges that submitted tasks are, upon completion, placed on a queue accessible using <tt>take</tt>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxCompletionService<V> {

    /** The result for a submitted task */
    public static class Result<V> {

        private final V result;
        private final Throwable error;

        Result(V result) {
            super();
            this.result = result;
            this.error = null;
        }

        Result(Throwable error) {
            super();
            this.result = null;
            this.error = error;
        }

        /**
         * Gets the result
         *
         * @return The result
         * @throws ExecutionException If computing the result caused an error
         */
        public V get() throws ExecutionException {
            if (null != error) {
                throw new ExecutionException(error);
            }
            return result;
        }
    }

    private static <V> QueueingTask<V> wrapperFor(Task<V> task, BlockingQueue<Result<V>> queue) {
        return (task instanceof Trackable) ? new TrackableQueueingTask(task, queue) : new QueueingTask<>(task, queue);
    }

    /**
     * To enqueue upon completion
     */
    private static class QueueingTask<V> implements Task<V>, TaskWrapper {

        private final Task<V> task;
        private final BlockingQueue<Result<V>> queue;

        protected QueueingTask(Task<V> task, BlockingQueue<Result<V>> queue) {
            super();
            this.task = task;
            this.queue = queue;
        }

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {
            task.setThreadName(threadRenamer);
        }

        @Override
        public void beforeExecute(Thread t) {
            task.beforeExecute(t);
        }

        @Override
        public void afterExecute(Throwable t) {
            task.afterExecute(t);
        }

        @Override
        public V call() throws Exception {
            try {
                V result = task.call();
                queue.add(new Result<V>(result));
                return result;
            } catch (Exception e) {
                queue.add(new Result<V>(e));
                throw e;
            } catch (Throwable t) {
                queue.add(new Result<V>(t));
                throw t;
            }
        }

        @Override
        public Object getWrapped() {
            return task;
        }
    }

    /**
     * To enqueue upon completion
     */
    private static class TrackableQueueingTask<V> extends QueueingTask<V> implements Trackable {

        protected TrackableQueueingTask(Task<V> task, BlockingQueue<Result<V>> queue) {
            super(task, queue);
        }

    }

    // --------------------------------------------------------------------------------------------------------------

    private final ThreadPoolService threadPool;
    private final BlockingQueue<Result<V>> completionQueue;
    private long start;
    private long duration;
    private int count;

    /**
     * Initializes a new {@link UnifiedInboxCompletionService}.
     *
     * @param threadPool The thread pool to use
     * @throws NullPointerException If thread pool is <tt>null</tt>
     */
    public UnifiedInboxCompletionService(ThreadPoolService threadPool) {
        super();
        if (threadPool == null) {
            throw new NullPointerException();
        }
        this.threadPool = threadPool;
        this.completionQueue = new LinkedBlockingQueue<Result<V>>();
    }

    /**
     * Submits specified task for execution.
     *
     * @param task The task to execute
     */
    public void submit(final Task<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }

        QueueingTask<V> wrapper = wrapperFor(task, completionQueue);
        threadPool.submit(wrapper);
        if (++count == 1) {
            // First element submitted
            start = System.currentTimeMillis();
        }
    }

    public Result<V> take() throws InterruptedException {
        final Result<V> r = completionQueue.take();
        if (0 == --count) {
            duration = (System.currentTimeMillis() - start);
        }
        return r;
    }

    public Result<V> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final Result<V> r = completionQueue.poll(timeout, unit);
        if (null != r && 0 == --count) {
            duration = (System.currentTimeMillis() - start);
        }
        return r;
    }

    public long getDuration() {
        return duration;
    }
}
