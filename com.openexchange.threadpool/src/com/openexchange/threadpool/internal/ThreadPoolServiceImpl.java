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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.MDC;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link ThreadPoolServiceImpl} - A thread pool backed by a {@link ThreadPoolExecutor} instance which is accessible via
 * {@link #getExecutor()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadPoolServiceImpl implements ThreadPoolService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadPoolServiceImpl.class);

    /**
     * Creates a new {@link ThreadPoolServiceImpl} with the given properties.
     *
     * @param properties The properties
     * @return A new {@link ThreadPoolServiceImpl} instance
     */
    public static ThreadPoolServiceImpl newInstance(final ThreadPoolProperties properties) {
        return newInstance(
            properties.getCorePoolSize(),
            properties.getMaximumPoolSize(),
            properties.getKeepAliveTime(),
            properties.getWorkQueue(),
            properties.getWorkQueueSize(),
            properties.isBlocking(),
            properties.getRefusedExecutionBehavior());
    }

    /**
     * Creates a new {@link ThreadPoolServiceImpl} with the given initial parameters.
     *
     * @param corePoolSize The number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize The maximum number of threads to allow in the pool.
     * @param keepAliveTime When the number of threads is greater than the core, this is the maximum time in milliseconds that excess idle
     *            threads will wait for new tasks before terminating.
     * @param workQueue The queue to use for holding tasks before they are executed.
     * @param workQueueSize The size of the work queue; zero for unlimited size
     * @param blocking <code>true</code> for a blocking behavior; otherwise <code>false</code>
     * @param refusedExecutionBehavior The default behavior to obey when execution is blocked because the thread bounds and queue capacities
     *            are reached.
     * @return A new {@link ThreadPoolServiceImpl} instance
     * @throws IllegalArgumentException If corePoolSize, or keepAliveTime less than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize or either <tt>workQueue</tt> or <tt>refusedExecutionBehavior</tt> cannot
     *             be resolved.
     * @throws NullPointerException If <tt>workQueue</tt> or <tt>refusedExecutionBehavior</tt> are <code>null</code>.
     */
    public static ThreadPoolServiceImpl newInstance(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final String workQueue, final int workQueueSize, final boolean blocking, final String refusedExecutionBehavior) {
        return new ThreadPoolServiceImpl(corePoolSize, maximumPoolSize, keepAliveTime, workQueue, workQueueSize, blocking, refusedExecutionBehavior);
    }

    private final CustomThreadPoolExecutor threadPoolExecutor;

    private final int corePoolSize;

    /**
     * Creates a new <tt>ThreadPoolServiceImpl</tt> with the given initial parameters.
     *
     * @param corePoolSize The number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize The maximum number of threads to allow in the pool.
     * @param keepAliveTime When the number of threads is greater than the core, this is the maximum time in milliseconds that excess idle
     *            threads will wait for new tasks before terminating.
     * @param workQueue The queue to use for holding tasks before they are executed.
     * @param workQueueSize The size of the work queue
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>threadFactory</tt> are <code>null</code>.
     */
    private ThreadPoolServiceImpl(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final String workQueue, final int workQueueSize, final boolean blocking, final String refusedExecutionBehavior) {
        final QueueType queueType = QueueType.getQueueType(workQueue);
        if (null == queueType) {
            throw new IllegalArgumentException("Unknown queue type: " + workQueue);
        }
        final RejectedExecutionType ret = RejectedExecutionType.getRejectedExecutionType(refusedExecutionBehavior);
        if (null == ret) {
            throw new IllegalArgumentException("Unknown refused execution behavior: " + refusedExecutionBehavior);
        }
        this.corePoolSize = getCorePoolSize(corePoolSize);
        if (QueueType.LINKED.equals(queueType) && corePoolSize < maximumPoolSize) {
            final ScalingQueue scalingQueue = workQueueSize > 0 ? new ScalingQueue(workQueueSize) : new ScalingQueue();
            threadPoolExecutor =
                new CustomThreadPoolExecutor(
                    this.corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.MILLISECONDS,
                    scalingQueue,
                    new CustomThreadFactory("OXWorker-"));
            scalingQueue.setThreadPoolExecutor(threadPoolExecutor);
            scalingQueue.setThreadPool(this);
            final DelegatingRejectedExecutionHandler reh = new DelegatingRejectedExecutionHandler(ret.getHandler(), this);
            threadPoolExecutor.setRejectedExecutionHandler(scalingQueue.createRejectedExecutionHandler(reh));
            threadPoolExecutor.setBlocking(false);
        } else {
            threadPoolExecutor =
                new CustomThreadPoolExecutor(
                    queueType.isFixedSize() ? maximumPoolSize : this.corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.MILLISECONDS,
                    queueType.newWorkQueue(workQueueSize),
                    new CustomThreadFactory("OXWorker-"));
            final DelegatingRejectedExecutionHandler reh = new DelegatingRejectedExecutionHandler(ret.getHandler(), this);
            threadPoolExecutor.setRejectedExecutionHandler(reh);
            threadPoolExecutor.setBlocking(blocking);
        }
    }

    private static int getCorePoolSize(final int desiredCorePoolSize) {
        final int minCorePoolSize = Runtime.getRuntime().availableProcessors() + 1;
        if (desiredCorePoolSize < minCorePoolSize) {
            LOG.warn("\n\n\tConfigured pool size of {} through property \"com.openexchange.threadpool.corePoolSize\" does not obey the rule\n\tfor minimum core pool size: {} (number of CPUs) + 1 = {}. Using {} as core pool size.\n", desiredCorePoolSize, Runtime.getRuntime().availableProcessors(), minCorePoolSize, minCorePoolSize);
            return minCorePoolSize;
        }
        return desiredCorePoolSize;
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     *
     * @param timeout The maximum time in milliseconds to wait
     * @return <code>true</code> if this thread pool terminated and <code>false</code> if the timeout elapsed before termination
     * @throws InterruptedException If interrupted while waiting
     * @see java.util.concurrent.ThreadPoolExecutor#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    public boolean awaitTermination(final long timeout) throws InterruptedException {
        return threadPoolExecutor.awaitTermination(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no
     * additional effect if already shut down.
     */
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    /**
     * Starts all core threads, causing them to idly wait for work. This overrides the default policy of starting core threads only when new
     * tasks are executed.
     */
    public void prestartAllCoreThreads() {
        threadPoolExecutor.prestartAllCoreThreads();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and returns a list of the tasks that were
     * awaiting execution.
     * <p>
     * This implementation cancels tasks via Thread.interrupt, so if any tasks mask or fail to respond to interrupts, they may never
     * terminate.
     *
     * @return A list of tasks that never commenced execution
     */
    public List<Runnable> shutdownNow() {
        return threadPoolExecutor.shutdownNow();
    }

    @Override
    public int getActiveCount() {
        return threadPoolExecutor.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return threadPoolExecutor.getCompletedTaskCount();
    }

    @Override
    public int getLargestPoolSize() {
        return threadPoolExecutor.getLargestPoolSize();
    }

    @Override
    public int getPoolSize() {
        return threadPoolExecutor.getPoolSize();
    }

    @Override
    public long getTaskCount() {
        return threadPoolExecutor.getTaskCount();
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Task<T>> tasks) throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        final List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            {
                @SuppressWarnings("unchecked")
                final Map<String, Object> mdcMap = MDC.getCopyOfContextMap();
                for (final Task<T> t : tasks) {
                    final CustomFutureTask<T> ftask = new CustomFutureTask<T>(t, mdcMap);
                    futures.add(ftask);
                    threadPoolExecutor.execute(ftask);
                }
            }

            for (final Future<T> f : futures) {
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (final CancellationException ignore) {
                        LOG.debug("", ignore);
                    } catch (final ExecutionException ignore) {
                        LOG.debug("", ignore);
                    }
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done) {
                for (final Future<T> f : futures) {
                    f.cancel(true);
                }
            }
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Task<T>> tasks, final long timeout) throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        long nanos = TimeUnit.MILLISECONDS.toNanos(timeout);
        final List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false;
        try {
            {
                @SuppressWarnings("unchecked")
                final Map<String, Object> mdcMap = MDC.getCopyOfContextMap();
                for (final Task<T> t : tasks) {
                    futures.add(new CustomFutureTask<T>(t, mdcMap));
                }
            }

            long lastTime = System.nanoTime();
            /*
             * Interleave time checks and calls to execute in case executor doesn't have any/much parallelism.
             */
            final Iterator<Future<T>> it = futures.iterator();
            while (it.hasNext()) {
                threadPoolExecutor.execute((Runnable) (it.next()));
                final long now = System.nanoTime();
                nanos -= now - lastTime;
                lastTime = now;
                if (nanos <= 0) {
                    return futures;
                }
            }

            for (final Future<T> f : futures) {
                if (!f.isDone()) {
                    if (nanos <= 0) {
                        return futures;
                    }
                    try {
                        f.get(nanos, TimeUnit.NANOSECONDS);
                    } catch (final CancellationException ignore) {
                        LOG.debug("", ignore);
                    } catch (final ExecutionException ignore) {
                        LOG.debug("", ignore);
                    } catch (final TimeoutException toe) {
                        return futures;
                    }
                    final long now = System.nanoTime();
                    nanos -= now - lastTime;
                    lastTime = now;
                }
            }
            done = true;
            return futures;
        } finally {
            if (!done) {
                for (final Future<T> f : futures) {
                    f.cancel(true);
                }
            }
        }
    }

    @Override
    public <T> CompletionFuture<T> invoke(final Collection<? extends Task<T>> tasks) {
        if (tasks == null) {
            throw new NullPointerException();
        }
        final CompletionService<T> completionService = new ExecutorCompletionService<T>(threadPoolExecutor);
        for (final Task<T> task : tasks) {
            completionService.submit(task);
        }
        return new CompletionFutureImpl<T>(completionService);
    }

    @Override
    public <T> CompletionFuture<T> invoke(final Task<T>[] tasks) {
        if (tasks == null) {
            throw new NullPointerException();
        }
        final CompletionService<T> completionService = new ExecutorCompletionService<T>(threadPoolExecutor);
        for (final Task<T> task : tasks) {
            completionService.submit(task);
        }
        return new CompletionFutureImpl<T>(completionService);
    }

    @Override
    public <T> CompletionFuture<T> invoke(final Collection<? extends Task<T>> tasks, final RefusedExecutionBehavior<T> behavior) {
        if (tasks == null) {
            throw new NullPointerException();
        }
        @SuppressWarnings("unchecked")
        final Map<String, Object> mdcMap = MDC.getCopyOfContextMap();
        final CompletionService<T> completionService = new CustomExecutorCompletionService<T>(threadPoolExecutor, behavior, mdcMap);
        for (final Task<T> task : tasks) {
            completionService.submit(task);
        }
        return new CompletionFutureImpl<T>(completionService);
    }

    /**
     * Gets the thread pool executor.
     *
     * @return The thread pool executor
     */
    public CustomThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    @Override
    public ExecutorService getExecutor() {
        return new DelegateExecutorService(threadPoolExecutor);
    }

    @Override
    public ExecutorService getFixedExecutor() {
        return new FixedExecutorService(corePoolSize, threadPoolExecutor);
    }

    @Override
    public ExecutorService getFixedExecutor(final int size) {
        return new FixedExecutorService(size, threadPoolExecutor);
    }

    @Override
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPoolExecutor.isTerminated();
    }

    @Override
    public <T> Future<T> submit(final Task<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        @SuppressWarnings("unchecked")
        final Map<String, Object> mdcMap = MDC.getCopyOfContextMap();
        final CustomFutureTask<T> ftask = new CustomFutureTask<T>(task, mdcMap);
        threadPoolExecutor.execute(ftask);
        return ftask;
    }

    @Override
    public <T> Future<T> submit(final Task<T> task, final RefusedExecutionBehavior<T> refusedExecutionBehavior) {
        if (task == null) {
            throw new NullPointerException();
        }
        @SuppressWarnings("unchecked")
        final Map<String, Object> mdcMap = MDC.getCopyOfContextMap();
        final CustomFutureTask<T> ftask = new CustomFutureTask<T>(task, refusedExecutionBehavior, mdcMap);
        threadPoolExecutor.execute(ftask);
        return ftask;
    }

    /*-
     * ######################## HELPER CLASSES ########################
     */

    private static final class CustomExecutorCompletionService<V> implements CompletionService<V> {

        private final Executor executor;
        private final RefusedExecutionBehavior behavior;
        private final BlockingQueue<Future<V>> completionQueue;
        private final Map<String, Object> mdcMap;

        /**
         * FutureTask extension to enqueue upon completion
         */
        private class CustomQueueingFuture extends CustomFutureTask<V> {

            CustomQueueingFuture(final Task<V> task, final Map<String, Object> mdcMap) {
                super(task, behavior, mdcMap);
            }

            @Override
            protected void done() {
                completionQueue.add(this);
            }
        }

        /**
         * Creates an {@link CustomExecutorCompletionService} using the supplied executor for base task execution and a
         * {@link LinkedBlockingQueue} as a completion queue.
         *
         * @param executor the executor to use
         * @param mdcMap The MDC map
         * @throws NullPointerException if executor is <tt>null</tt>
         */
        public CustomExecutorCompletionService(final Executor executor, final RefusedExecutionBehavior behavior, Map<String, Object> mdcMap) {
            if (executor == null) {
                throw new NullPointerException();
            }
            this.mdcMap = mdcMap;
            this.behavior = behavior;
            this.executor = executor;
            this.completionQueue = new LinkedBlockingQueue<Future<V>>();
        }

        @Override
        public Future<V> submit(final Callable<V> task) {
            if (task == null) {
                throw new NullPointerException();
            }
            final CustomQueueingFuture f = new CustomQueueingFuture((Task<V>) task, mdcMap);
            executor.execute(f);
            return f;
        }

        @Override
        public Future<V> submit(final Runnable task, final V result) {
            throw new UnsupportedOperationException("ThreadPoolServiceImpl.CustomExecutorCompletionService.submit()");
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

    }

    private static final class CompletionFutureImpl<V> implements CompletionFuture<V> {

        private final CompletionService<V> completionService;

        public CompletionFutureImpl(final CompletionService<V> completionService) {
            super();
            this.completionService = completionService;
        }

        @Override
        public Future<V> poll() {
            return completionService.poll();
        }

        @Override
        public Future<V> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
            return completionService.poll(timeout, unit);
        }

        @Override
        public Future<V> take() throws InterruptedException {
            return completionService.take();
        }

    } // End of CompletionFutureImpl

}
