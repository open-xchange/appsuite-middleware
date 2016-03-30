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

package com.openexchange.threadpool.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link ScalingQueue} - Enhances {@link LinkedBlockingQueue}.
 * <p>
 * Addresses the need to create an unbounded thread pool with minimum and maximum threads.<br>
 * What most people do is the following:
 *
 * <pre>
 * // similar to j.u.c.Executors.newCachedThreadPool()
 * new ThreadPoolExecutor(coreThreads, maximumThreads, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue());
 * </pre>
 *
 * This construction of thread pool will simply not work as expected. This is due to the logic within the <code>ThreadPoolExecutor</code>
 * where new threads are added if there is a failure to offer a task to the queue. In our case, we use an unbounded
 * <code>LinkedBlockingQueue</code>, where we can always offer a task to the queue. It effectively means that we will never grow above the
 * core pool size and up to the maximum pool size.
 * <p>
 * This queue implementation is a blocking queue that is aware of the <code>ThreadPoolExecutor</code>:<br>
 * It rejects the addition of a new task if there are no threads to handle it. This will cause the thread pool executor to try and allocate
 * a new thread (up to the maximum threads). If there are no threads, the task will be rejected. In our case, if the task is rejected, we
 * would like to put it back to the queue. This is a simple thing to do with <code>ThreadPoolExecutor</code> since we can implement our own
 * <code>RejectedExecutionHandler</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ScalingQueue extends LinkedBlockingQueue<Runnable> {

    /**
     * A {@link RejectedExecutionHandler} that re-inserts rejected tasks into <code>ThreadPoolExecutor</code>'s queue.
     */
    public static final class ForceQueuePolicy implements RejectedExecutionHandler {

        private final ScalingQueue queue;

        private final RejectedExecutionHandler defaultHandler;

        private final ThreadPoolService threadPool;

        /**
         * Initializes a new {@link ForceQueuePolicy}.
         */
        public ForceQueuePolicy(final ScalingQueue queue, final RejectedExecutionHandler defaultHandler, final ThreadPoolService threadPool) {
            super();
            this.queue = queue;
            this.defaultHandler = defaultHandler;
            this.threadPool = threadPool;
        }

        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
            try {
                final ScalingQueue scalingQueue = queue;
                if (null == scalingQueue) {
                    // Strange...
                    executor.getQueue().put(r);
                } else {
                    if (!scalingQueue.superOffer(r)) {
                        /*
                         * Task cannot be re-enqueued, trigger originally associated handler or default handler
                         */
                        if (r instanceof CustomFutureTask<?>) {
                            // Perform task's handler or default if null
                            final CustomFutureTask<?> cft = (CustomFutureTask<?>) r;
                            try {
                                handleTask(cft);
                            } catch (final RejectedExecutionException e) {
                                // No remedy
                                throw e;
                            } catch (final Exception e) {
                                // Signal failed execution
                                cft.setException(e);
                            }
                        }
                        defaultHandler.rejectedExecution(r, executor);
                    }
                }
            } catch (final InterruptedException e) {
                // should never happen since we never wait
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException(e);
            }
        }

        private <V> void handleTask(final CustomFutureTask<V> cft) throws Exception {
            final RefusedExecutionBehavior<V> reb = cft.getRefusedExecutionBehavior();
            if (null != reb) {
                final V result = reb.refusedExecution(cft.getTask(), threadPool);
                if (RefusedExecutionBehavior.DISCARDED == result) {
                    /*
                     * TODO: What to do on discarded task? If cft's set() method is never invoked (either through innerRun() or innerSet()),
                     * a call to get() will block forever.
                     */
                } else {
                    cft.set(result);
                }
            }
        }
    } // End of class ForceQueuePolicy

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2967523498773168654L;

    /**
     * The executor this Queue belongs to
     */
    private ThreadPoolExecutor executor;

    /**
     * The associated thread po<ol service.
     */
    private ThreadPoolService threadPool;

    /**
     * Creates a TaskQueue with a capacity of {@link Integer#MAX_VALUE}.
     */
    public ScalingQueue() {
        super();
    }

    /**
     * Creates a TaskQueue with the given (fixed) capacity.
     *
     * @param capacity the capacity of this queue.
     */
    public ScalingQueue(final int capacity) {
        super(capacity);
    }

    /**
     * Creates an appropriate {@link RejectedExecutionHandler} instance for this queue that re-inserts rejected tasks into this queue.
     *
     * @return The appropriate {@link RejectedExecutionHandler} instance
     */
    public RejectedExecutionHandler createRejectedExecutionHandler(final RejectedExecutionHandler defaultHandler) {
        return new ForceQueuePolicy(this, defaultHandler, threadPool);
    }

    /**
     * Sets the thread pool
     *
     * @param threadPool The thread pool to set
     */
    public synchronized void setThreadPool(final ThreadPoolService threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * Sets the executor this queue belongs to.
     */
    public synchronized void setThreadPoolExecutor(final ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * Inserts the specified task at the tail of this queue if it is possible to do so immediately without exceeding the queue's capacity,
     * returning <tt>true</tt> upon success and <tt>false</tt> if this queue is full.
     * <p>
     * When using a capacity-restricted queue, this method is generally preferable to method {@link BlockingQueue#add add}, which can fail
     * to insert an element only by throwing an exception.
     *
     * @param o The task to add.
     * @return <code>true</code> if it was possible to add the task to this queue, else <code>false</code>
     * @throws NullPointerException If the specified task is <code>null</code>
     */
    public boolean superOffer(final Runnable o) {
        return super.offer(o);
    }

    /**
     * Inserts the specified task at the tail of this queue if there is at least one available thread to run the current task. If all pool
     * threads are actively busy, it rejects the offer.
     *
     * @param o The task to add.
     * @return <code>true</code> if it was possible to add the task to this queue, else <code>false</code>
     * @see ThreadPoolExecutor#execute(Runnable)
     */
    @Override
    public boolean offer(final Runnable o) {
        final int allWorkingThreads = executor.getActiveCount() + super.size();
        return ((allWorkingThreads < executor.getPoolSize()) && super.offer(o));
    }

}
