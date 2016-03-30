/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    private final class QueueingFuture extends FutureTask<V> {

        QueueingFuture(final Callable<V> c) {
            super(c);
        }

        QueueingFuture(final Runnable t, final V r) {
            super(t, r);
        }

        @Override
        protected void done() {
            taskDone(this);
        }
    }

    /**
     * FutureTask extension to enqueue upon completion
     */
    private final class QueueingTaskFuture extends FutureTask<V> {

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
            } catch (final Exception ex) {
                if (!ran) {
                    t.afterExecute(ex);
                }
                // Else the exception occurred within
                // afterExecute itself in which case we don't
                // want to call it again.
            }
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
        super();
        if (threadPoolService == null) {
            throw new NullPointerException();
        }
        this.threadPoolService = threadPoolService;
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
        numberOfSubmits = new AtomicInteger(0);
        behavior = CallerRunsBehavior.getInstance();
        submittedFutures = new LinkedList<Future<V>>();
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
        Future<V> submitted = threadPoolService.submit(ThreadPools.task(f, (V) null, trackable), behavior);
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
