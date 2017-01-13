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

package com.openexchange.unifiedinbox.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.openexchange.threadpool.Task;
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
    private static class QueueingTask<V> implements Task<V> {

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
