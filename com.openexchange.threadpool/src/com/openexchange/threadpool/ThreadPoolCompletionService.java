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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link ThreadPoolCompletionService} - A {@link CompletionService} that uses a supplied {@link ThreadPoolService} to execute tasks. This
 * class arranges that submitted tasks are, upon completion, placed on a queue accessible using <tt>take</tt>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ThreadPoolCompletionService<V> implements CancelableCompletionService<V> {

    /**
     * FutureTask extension to enqueue upon completion
     */
    private final class QueueingFuture extends FutureTask<V> implements TaskWrapper {

        private final Object task;

        QueueingFuture(final Callable<V> c) {
            super(c);
            this.task = c;
        }

        QueueingFuture(final Runnable t, final V r) {
            super(t, r);
            this.task = r;
        }

        @Override
        protected void done() {
            taskDone(this);
        }

        @Override
        public Object getWrapped() {
            return task;
        }
    }

    /**
     * FutureTask extension to enqueue upon completion
     */
    private final class QueueingTaskFuture extends FutureTask<V> implements TaskWrapper {

        private final Task<V> t;

        QueueingTaskFuture(final Task<V> t) {
            super(t);
            this.t = t;
        }

        @Override
        public void run() {
            boolean ran = false;
            t.beforeExecute(Thread.currentThread());
            try {
                super.run();
                ran = true;
                t.afterExecute(null);
            } catch (Exception ex) {
                if (!ran) {
                    t.afterExecute(ex);
                }
                // Else the exception occurred within
                // afterExecute itself in which case we don't
                // want to call it again.
            }
        }

        @Override
        public Object getWrapped() {
            return t;
        }

        @Override
        protected void done() {
            taskDone(this);
        }
    }

    private final ThreadPoolService threadPoolService;
    private final BlockingQueue<Future<V>> completionQueue;
    private final RefusedExecutionBehavior<V> behavior;
    private final List<Future<V>> submittedFutures;
    private final AtomicInteger numberOfSubmits;
    private boolean trackable;

    /**
     * Initializes a new {@link ThreadPoolCompletionService} with caller-runs behavior and an unbound {@link BlockingQueue}.
     *
     * @param threadPoolService The thread pool to use
     * @throws NullPointerException If threadPoolService is <tt>null</tt>
     */
    public ThreadPoolCompletionService(final ThreadPoolService threadPoolService) {
        this(threadPoolService, new LinkedBlockingQueue<Future<V>>(), CallerRunsBehavior.getInstance());
    }

    /**
     * Initializes a new {@link ThreadPoolCompletionService}.
     *
     * @param threadPoolService The thread pool to use
     * @param completionQueue The queue to use as the completion queue normally one dedicated for use by this service
     * @param behavior The behavior to apply to submitted tasks
     * @throws NullPointerException If either threadPoolService, completionQueue, or behavior is <tt>null</tt>
     */
    public ThreadPoolCompletionService(final ThreadPoolService threadPoolService, final BlockingQueue<Future<V>> completionQueue, final RefusedExecutionBehavior<V> behavior) {
        super();
        if (threadPoolService == null) {
            throw new NullPointerException();
        }
        if (completionQueue == null) {
            throw new NullPointerException();
        }
        if (behavior == null) {
            throw new NullPointerException();
        }
        this.threadPoolService = threadPoolService;
        this.completionQueue = completionQueue;
        this.behavior = behavior;
        numberOfSubmits = new AtomicInteger(0);
        submittedFutures = new LinkedList<Future<V>>();
    }

    /**
     * Sets whether submitted tasks are trackable.
     *
     * @param trackable <code>true</code> if trackable; otherwise <code>false</code>
     * @return This completion service with new behavior applied
     */
    public ThreadPoolCompletionService<V> setTrackable(final boolean trackable) {
        this.trackable = trackable;
        return this;
    }

    /**
     * Submits given task.
     *
     * @param task The task
     * @return The associated {@link Future}
     */
    public Future<V> submit(final Task<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingTaskFuture f = new QueueingTaskFuture(task);
        submitFutureTask(f);
        return f;
    }

    @Override
    public Future<V> submit(final Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture f = new QueueingFuture(task);
        submitFutureTask(f);
        return f;
    }

    @Override
    public Future<V> submit(final Runnable task, final V result) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture f = new QueueingFuture(task, result);
        submitFutureTask(f);
        return f;
    }

    /**
     * Gets the number of currently submitted tasks.
     *
     * @return The number of submitted tasks
     */
    public int getNumberOfSubmits() {
        return numberOfSubmits.get();
    }

    /**
     * Submits specified queueing future task.
     *
     * @param f The queueing future task
     */
    protected void submitFutureTask(final FutureTask<V> f) {
        Future<V> submitted = threadPoolService.submit(ThreadPools.task(f, null, trackable), behavior);
        numberOfSubmits.incrementAndGet();
        submittedFutures.add(submitted);
    }

    /**
     * Invoked if a task has been executed.
     */
    protected void taskDone(final Future<V> task) {
        completionQueue.add(task);
    }

    @Override
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    @Override
    public Future<V> poll() {
        return completionQueue.poll();
    }

    @Override
    public Future<V> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

    @Override
    public void cancel(final boolean mayInterruptIfRunning) {
        while (!submittedFutures.isEmpty()) {
            submittedFutures.remove(0).cancel(mayInterruptIfRunning);
        }
    }

}
