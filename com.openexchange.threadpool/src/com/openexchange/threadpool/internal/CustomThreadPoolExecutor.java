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

import java.io.Closeable;
import java.io.File;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.slf4j.MDC;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogProperties;
import com.openexchange.startup.CloseableControlService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.MdcProvider;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.TaskWrapper;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.threadpool.osgi.ThreadPoolActivator;
import com.openexchange.threadpool.osgi.ThreadPoolServiceRegistry;

/**
 * {@link CustomThreadPoolExecutor} - Copied from Java6's <tt>ThreadPoolExecutor</tt> written by Doug Lea.
 * <p>
 * An {@link ExecutorService} that executes each submitted task using one of possibly several pooled threads, normally configured using
 * {@link Executors} factory methods.
 * <p>
 * Thread pools address two different problems: they usually provide improved performance when executing large numbers of asynchronous
 * tasks, due to reduced per-task invocation overhead, and they provide a means of bounding and managing the resources, including threads,
 * consumed when executing a collection of tasks. Each <tt>ThreadPoolExecutor</tt> also maintains some basic statistics, such as the number
 * of completed tasks.
 * <p>
 * Use the following guide when manually configuring and tuning this class:
 * <dl>
 * <dt>Core and maximum pool sizes</dt>
 * <dd>A <tt>CustomThreadPoolExecutor</tt> will automatically adjust the pool size (see {@link CustomThreadPoolExecutor#getPoolSize})
 * according to the bounds set by corePoolSize (see {@link CustomThreadPoolExecutor#getCorePoolSize}) and maximumPoolSize (see
 * {@link CustomThreadPoolExecutor#getMaximumPoolSize}). When a new task is submitted in method {@link CustomThreadPoolExecutor#execute},
 * and fewer than corePoolSize threads are running, a new thread is created to handle the request, even if other worker threads are idle. If
 * there are more than corePoolSize but less than maximumPoolSize threads running, a new thread will be created only if the queue is full.
 * By setting corePoolSize and maximumPoolSize the same, you create a fixed-size thread pool. By setting maximumPoolSize to an essentially
 * unbounded value such as <tt>Integer.MAX_VALUE</tt>, you allow the pool to accommodate an arbitrary number of concurrent tasks. Most
 * typically, core and maximum pool sizes are set only upon construction, but they may also be changed dynamically using
 * {@link CustomThreadPoolExecutor#setCorePoolSize} and {@link CustomThreadPoolExecutor#setMaximumPoolSize}.
 * <dd>
 * <dt>On-demand construction
 * <dd>By default, even core threads are initially created and started only when needed by new tasks, but this can be overridden dynamically
 * using method {@link CustomThreadPoolExecutor#prestartCoreThread} or {@link CustomThreadPoolExecutor#prestartAllCoreThreads}.</dd>
 * <dt>Creating new threads</dt>
 * <dd>New threads are created using a {@link java.util.concurrent.ThreadFactory}. If not otherwise specified, a
 * {@link Executors#defaultThreadFactory} is used, that creates threads to all be in the same {@link ThreadGroup} and with the same
 * <tt>NORM_PRIORITY</tt> priority and non-daemon status. By supplying a different ThreadFactory, you can alter the thread's name, thread
 * group, priority, daemon status, etc. If a <tt>ThreadFactory</tt> fails to create a thread when asked by returning <code>null</code> from
 * <tt>newThread</tt>, the executor will continue, but might not be able to execute any tasks.</dd>
 * <dt>Keep-alive times</dt>
 * <dd>If the pool currently has more than corePoolSize threads, excess threads will be terminated if they have been idle for more than the
 * keepAliveTime (see {@link CustomThreadPoolExecutor#getKeepAliveTime}). This provides a means of reducing resource consumption when the
 * pool is not being actively used. If the pool becomes more active later, new threads will be constructed. This parameter can also be
 * changed dynamically using method {@link CustomThreadPoolExecutor#setKeepAliveTime}. Using a value of <tt>Long.MAX_VALUE</tt>
 * {@link TimeUnit#NANOSECONDS} effectively disables idle threads from ever terminating prior to shut down.</dd>
 * <dt>Queuing</dt>
 * <dd>Any {@link BlockingQueue} may be used to transfer and hold submitted tasks. The use of this queue interacts with pool sizing:
 * <ul>
 * <li>If fewer than corePoolSize threads are running, the Executor always prefers adding a new thread rather than queuing.</li>
 * <li>If corePoolSize or more threads are running, the Executor always prefers queuing a request rather than adding a new thread.</li>
 * <li>If a request cannot be queued, a new thread is created unless this would exceed maximumPoolSize, in which case, the task will be
 * rejected.</li>
 * </ul>
 * There are three general strategies for queuing:
 * <ol>
 * <li> <em> Direct handoffs.</em> A good default choice for a work queue is a {@link SynchronousQueue} that hands off tasks to threads
 * without otherwise holding them. Here, an attempt to queue a task will fail if no threads are immediately available to run it, so a new
 * thread will be constructed. This policy avoids lockups when handling sets of requests that might have internal dependencies. Direct
 * handoffs generally require unbounded maximumPoolSizes to avoid rejection of new submitted tasks. This in turn admits the possibility of
 * unbounded thread growth when commands continue to arrive on average faster than they can be processed.</li>
 * <li><em> Unbounded queues.</em> Using an unbounded queue (for example a {@link LinkedBlockingQueue} without a predefined capacity) will
 * cause new tasks to be queued in cases where all corePoolSize threads are busy. Thus, no more than corePoolSize threads will ever be
 * created. (And the value of the maximumPoolSize therefore doesn't have any effect.) This may be appropriate when each task is completely
 * independent of others, so tasks cannot affect each others execution; for example, in a web page server. While this style of queuing can
 * be useful in smoothing out transient bursts of requests, it admits the possibility of unbounded work queue growth when commands continue
 * to arrive on average faster than they can be processed.</li>
 * <li><em>Bounded queues.</em> A bounded queue (for example, an {@link ArrayBlockingQueue}) helps prevent resource exhaustion when used
 * with finite maximumPoolSizes, but can be more difficult to tune and control. Queue sizes and maximum pool sizes may be traded off for
 * each other: Using large queues and small pools minimizes CPU usage, OS resources, and context-switching overhead, but can lead to
 * artificially low throughput. If tasks frequently block (for example if they are I/O bound), a system may be able to schedule time for
 * more threads than you otherwise allow. Use of small queues generally requires larger pool sizes, which keeps CPUs busier but may
 * encounter unacceptable scheduling overhead, which also decreases throughput.</li>
 * </ol>
 * </dd>
 * <dt>Rejected tasks</dt>
 * <dd>New tasks submitted in method {@link CustomThreadPoolExecutor#execute} will be <em>rejected</em> when the Executor has been shut
 * down, and also when the Executor uses finite bounds for both maximum threads and work queue capacity, and is saturated. In either case,
 * the <tt>execute</tt> method invokes the {@link RejectedExecutionHandler#rejectedExecution} method of its {@link RejectedExecutionHandler}
 * . Four predefined handler policies are provided:
 * <ol>
 * <li>In the default {@link CustomThreadPoolExecutor.AbortPolicy}, the handler throws a runtime {@link RejectedExecutionException} upon
 * rejection.</li>
 * <li>In {@link CustomThreadPoolExecutor.CallerRunsPolicy}, the thread that invokes <tt>execute</tt> itself runs the task. This provides a
 * simple feedback control mechanism that will slow down the rate that new tasks are submitted.</li>
 * <li>In {@link CustomThreadPoolExecutor.DiscardPolicy}, a task that cannot be executed is simply dropped.</li>
 * <li>In {@link CustomThreadPoolExecutor.DiscardOldestPolicy}, if the executor is not shut down, the task at the head of the work queue is
 * dropped, and then execution is retried (which can fail again, causing this to be repeated.)</li>
 * </ol>
 * It is possible to define and use other kinds of {@link RejectedExecutionHandler} classes. Doing so requires some care especially when
 * policies are designed to work only under particular capacity or queuing policies.</dd>
 * <dt>Hook methods</dt>
 * <dd>This class provides <tt>protected</tt> overridable {@link CustomThreadPoolExecutor#beforeExecute} and
 * {@link CustomThreadPoolExecutor#afterExecute} methods that are called before and after execution of each task. These can be used to
 * manipulate the execution environment; for example, reinitializing ThreadLocals, gathering statistics, or adding log entries.
 * Additionally, method {@link CustomThreadPoolExecutor#terminated} can be overridden to perform any special processing that needs to be
 * done once the Executor has fully terminated.
 * <p>
 * If hook or callback methods throw exceptions, internal worker threads may in turn fail and abruptly terminate.</dd>
 * <dt>Queue maintenance</dt>
 * <dd>Method {@link CustomThreadPoolExecutor#getQueue} allows access to the work queue for purposes of monitoring and debugging. Use of
 * this method for any other purpose is strongly discouraged. Two supplied methods, {@link CustomThreadPoolExecutor#remove} and
 * {@link CustomThreadPoolExecutor#purge} are available to assist in storage reclamation when large numbers of queued tasks become
 * cancelled.</dd>
 * </dl>
 * <p>
 * <b>Extension example</b>. Most extensions of this class override one or more of the protected hook methods. For example, here is a
 * subclass that adds a simple pause/resume feature:
 *
 * <pre>
 * class PausableThreadPoolExecutor extends CustomThreadPoolExecutor {
 *   private boolean isPaused;
 *   private ReentrantLock pauseLock = new ReentrantLock();
 *   private Condition unpaused = pauseLock.newCondition();
 *   public PausableThreadPoolExecutor(...) { super(...); }
 *
 *   protected void beforeExecute(Thread t, Runnable r) {
 *     super.beforeExecute(t, r);
 *     pauseLock.lock();
 *     try {
 *       while (isPaused) unpaused.await();
 *     } catch(InterruptedException ie) {
 *       t.interrupt();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void pause() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = true;
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 *
 *   public void resume() {
 *     pauseLock.lock();
 *     try {
 *       isPaused = false;
 *       unpaused.signalAll();
 *     } finally {
 *       pauseLock.unlock();
 *     }
 *   }
 * }
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomThreadPoolExecutor.class);

    static final Object PRESENT = new Object();

    private static final AtomicReference<CloseableControlService> REF_CLOSEABLE_CONTROL = ThreadPoolActivator.REF_CLOSEABLE_CONTROL;

    /**
     * Only used to force toArray() to produce a Runnable[].
     */
    private static final Runnable[] EMPTY_RUNNABLE_ARRAY = new Runnable[0];

    /**
     * Permission for checking shutdown
     */
    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

    /**
     * Sequence number to break scheduling ties, and in turn to guarantee FIFO order among tied entries.
     */
    private static final AtomicLong sequencer = new AtomicLong(0);

    /**
     * Base of nanosecond timings, to avoid wrapping
     */
    private static final long NANO_ORIGIN = System.nanoTime();

    /**
     * Queue used for holding tasks and handing off to worker threads.
     */
    private final BlockingQueue<Runnable> workQueue;

    /**
     * The delayed work queue for scheduled execution.
     */
    private final DelayedWorkQueue delayedWorkQueue;

    /**
     * Lock held on updates to poolSize, corePoolSize, maximumPoolSize, and workers set.
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * Wait condition to support awaitTermination
     */
    private final Condition termination = mainLock.newCondition();

    /**
     * Set containing all worker threads in pool.
     */
    private final ConcurrentMap<Worker, Object> workers = new NonBlockingHashMap<Worker, Object>(1024);
    private final Set<Worker> workerSet = workers.keySet();

    /**
     * The flag whether to monitor threads.
     */
    protected final boolean monitorThreads;
    private volatile ScheduledFuture<?> monitorFuture;

    /**
     * The consumer thread to fetch from delayed work queue and add to work queue.
     */
    private final Thread consumerThread;

    /**
     * The watcher thread.
     */
    private final Thread watcherThread;

    /**
     * The task for consuming from delayed work queue.
     */
    private final DelayedQueueConsumer delayedQueueConsumer;

    /**
     * The task for watching active threads.
     */
    private final ActiveTaskWatcher activeTaskWatcher;

    /**
     * The number of threads that are actively executing tasks
     */
    private final AtomicInteger activeCount = new AtomicInteger();

    /**
     * Timeout in nanoseconds for idle threads waiting for work. Threads use this timeout only when there are more than corePoolSize
     * present. Otherwise they wait forever for new work.
     */
    private volatile long keepAliveTime;

    /**
     * Core pool size, updated only while holding mainLock, but volatile to allow concurrent readability even during updates.
     */
    private volatile int corePoolSize;

    /**
     * Maximum pool size, updated only while holding mainLock but volatile to allow concurrent readability even during updates.
     */
    private volatile int maximumPoolSize;

    /**
     * Current pool size, updated only while holding mainLock but volatile to allow concurrent readability even during updates.
     */
    private final AtomicInteger poolSize = new AtomicInteger();

    /**
     * False if should cancel/suppress periodic tasks on shutdown.
     */
    private volatile boolean continueExistingPeriodicTasksAfterShutdown;

    /**
     * False if should cancel non-periodic tasks on shutdown.
     */
    private volatile boolean executeExistingDelayedTasksAfterShutdown;

    /**
     * Lifecycle state
     */
    volatile int runState;

    // Special values for runState
    /** Normal, not-shutdown mode */
    static final int RUNNING = 0;

    /** Controlled shutdown mode */
    static final int SHUTDOWN = 1;

    /** Immediate shutdown mode */
    static final int STOP = 2;

    /** Final state */
    static final int TERMINATED = 3;

    /**
     * Handler called when saturated or shutdown in execute.
     */
    private volatile RejectedExecutionHandler handler;

    /**
     * Whether this executor has a blocking behavior. Meaning caller is blocked until space becomes available in working queue. Otherwise
     * {@link RejectedExecutionException} are be thrown in capacity bounds are reached.
     */
    private volatile boolean blocking;

    /**
     * Factory for new threads.
     */
    private volatile ThreadFactory threadFactory;

    /**
     * Tracks largest attained pool size.
     */
    private int largestPoolSize;

    /**
     * Counter for completed tasks. Updated only on termination of worker threads.
     */
    private long completedTaskCount;

    /**
     * The default rejected execution handler
     */
    private static final RejectedExecutionHandler DEFAULT_HANDLER = new AbortPolicy();

    /**
     * Invoke the rejected execution handler for the given command.
     */
    void rejectCustom(final Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * Sequence number to break scheduling ties, and in turn to guarantee FIFO order among tied entries.
     *
     * @return The sequencer
     */
    AtomicLong getSequencer() {
        return sequencer;
    }

    /**
     * Create and return a new thread running firstTask as its first task. Call only while holding mainLock
     *
     * @param firstTask the task the new thread should run first (or <code>null</code> if none)
     * @return the new thread, or <code>null</code> if threadFactory fails to create thread
     */
    private Thread addThread(final Runnable firstTask) {
        final Worker w = new Worker(firstTask);
        final Thread t = threadFactory.newThread(w);
        if (null != t) {
            // Log spawning of a new thread
            if (null != firstTask) {
                final Object stringer = new Object() {

                    @Override
                    public String toString() {
                        final Object task;
                        if (firstTask instanceof CustomFutureTask) {
                            final Task<?> tsk = ((CustomFutureTask<?>) firstTask).getTask();
                            task = tsk instanceof TaskWrapper ? ((TaskWrapper) tsk).getWrapped() : tsk;
                        } else if (firstTask instanceof ScheduledFutureTask) {
                            task = ((ScheduledFutureTask<?>) firstTask).getWrapped();
                        } else {
                            task = firstTask;
                        }
                        return task.getClass().getName();
                    }
                };
                LOG.debug("Spawned new thread for {}", stringer, new Throwable("Thread-Creation-Watcher"));
            }
            // Continue initialization worker
            w.thread = t;
            workers.put(w, PRESENT);
            final int nt = poolSize.incrementAndGet();
            if (nt > largestPoolSize) {
                largestPoolSize = nt;
            }
        }
        return t;
    }

    /**
     * Create and start a new thread running firstTask as its first task, only if fewer than corePoolSize threads are running.
     *
     * @param firstTask the task the new thread should run first (or <code>null</code> if none)
     * @return true if successful.
     */
    boolean addIfUnderCorePoolSize(final Runnable firstTask) {
        Thread t = null;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (poolSize.get() < corePoolSize) {
                t = addThread(firstTask);
            }
        } finally {
            mainLock.unlock();
        }
        if (null == t) {
            return false;
        }
        t.start();
        return true;
    }

    /**
     * Create and start a new thread only if fewer than maximumPoolSize threads are running. The new thread runs as its first task the next
     * task in queue, or if there is none, the given task.
     *
     * @param firstTask the task the new thread should run first (or <code>null</code> if none)
     * @return <code>null</code> on failure, else the first task to be run by new thread.
     */
    Runnable addIfUnderMaximumPoolSize(final Runnable firstTask) {
        Thread t = null;
        Runnable next = null;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (poolSize.get() < maximumPoolSize) {
                next = workQueue.poll();
                if (null == next) {
                    next = firstTask;
                }
                t = addThread(next);
            }
        } finally {
            mainLock.unlock();
        }
        if (null == t) {
            return null;
        }
        t.start();
        return next;
    }

    /**
     * Get the next task for a worker thread to run.
     *
     * @return the task
     * @throws InterruptedException if interrupted while waiting for task
     */
    Runnable getTaskCustom() throws InterruptedException {
        for (;;) {
            switch (runState) {
            case RUNNING: {
                if (poolSize.get() <= corePoolSize) {
                    /*
                     * Prefer "normal" work queue before taking from delayed work queue
                     */
                    return workQueue.take();
                }

                final long timeout = keepAliveTime;
                if (timeout <= 0) {
                    return null;
                }
                /*
                 * Prefer "normal" work queue before taking from delayed work queue
                 */
                final Runnable r = workQueue.poll(timeout, TimeUnit.NANOSECONDS);
                if (null != r) {
                    return r;
                }
                if (poolSize.get() > corePoolSize) {
                    return null;
                }
                // else, after timeout, pool shrank so shouldn't die, so retry
                break;
            }

            case SHUTDOWN: {
                // Help drain queue
                final Runnable r = workQueue.poll();
                if (null != r) {
                    return r;
                }

                // Check if can terminate
                if (workQueue.isEmpty()) {
                    interruptIdleWorkersCustom();
                    return null;
                }

                // There could still be delayed tasks in queue.
                // Wait for one, re-checking state upon interruption
                try {
                    return workQueue.take();
                } catch (final InterruptedException ignore) {
                    LOG.debug("", ignore);
                }
                break;
            }

            case STOP:
                return null;
            default:
                assert false;
            }
        }
    }

    /**
     * Wake up all threads that might be waiting for tasks.
     */
    void interruptIdleWorkersCustom() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (final Worker w : workerSet) {
                w.interruptIfIdle();
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Perform bookkeeping for a terminated worker thread.
     *
     * @param w the worker
     */
    void workerDone(final Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            LogProperties.dropTempFiles();
            completedTaskCount += w.completedTasks.get();
            workers.remove(w);
            if (poolSize.decrementAndGet() > 0) {
                return;
            }

            // Else, this is the last thread. Deal with potential shutdown.

            final int state = runState;
            assert state != TERMINATED;

            if (state != STOP) {
                // If there are queued tasks but no threads, create
                // replacement thread. We must create it initially
                // idle to avoid orphaned tasks in case addThread
                // fails. This also handles case of delayed tasks
                // that will sometime later become runnable.
                if (!workQueue.isEmpty()) {
                    final Thread t = addThread(null);
                    if (null != t) {
                        t.start();
                    }
                    return;
                }

                // Otherwise, we can exit without replacement
                if (state == RUNNING) {
                    return;
                }
            }

            // Either state is STOP, or state is SHUTDOWN and there is
            // no work to do. So we can terminate.
            termination.signalAll();
            runState = TERMINATED;
            // fall through to call terminate() outside of lock.
        } finally {
            mainLock.unlock();
        }

        assert runState == TERMINATED;
        terminated();
    }

    /**
     * Returns nanosecond time offset by origin
     */
    static final long now() {
        return System.nanoTime() - NANO_ORIGIN;
    }

    /**
     * Returns the trigger time of delayed action.
     */
    private long triggerTime(final long delay, final TimeUnit unit) {
        return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
    }

    /**
     * Returns the trigger time of delayed action.
     */
    long triggerTime(final long delay) {
        return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
    }

    /**
     * Constrains the values of all delays in the queue to be within Long.MAX_VALUE of each other, to avoid overflow in compareTo. This may
     * occur if a task is eligible to be dequeued, but has not yet been, while some other task is added with a delay of Long.MAX_VALUE.
     */
    private long overflowFree(final long delay) {
        final Delayed head = (Delayed) delayedWorkQueue.peek();
        long d = delay;
        if (null != head) {
            final long headDelay = head.getDelay(TimeUnit.NANOSECONDS);
            if (headDelay < 0 && (delay - headDelay < 0)) {
                d = Long.MAX_VALUE + headDelay;
            }
        }
        return d;
    }

    /**
     * Cancel and clear the queue of all tasks that should not be run due to shutdown policy.
     */
    void cancelUnwantedTasks() {
        final boolean keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy();
        final boolean keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy();
        if (!keepDelayed && !keepPeriodic) {
            delayedWorkQueue.clear();
        } else if (keepDelayed || keepPeriodic) {
            Object[] entries = delayedWorkQueue.toArray();
            for (int i = 0; i < entries.length; ++i) {
                final Object e = entries[i];
                if (e instanceof ScheduledFutureTask<?>) {
                    final ScheduledFutureTask<?> t = (ScheduledFutureTask<?>) e;
                    if (t.isPeriodic() ? !keepPeriodic : !keepDelayed) {
                        t.cancel(false);
                    }
                }
            }
            entries = null;
            purge();
        }
    }

    /**
     * Specialized variant of ThreadPoolExecutor.execute for delayed tasks.
     */
    private void delayedExecute(final Runnable command) {
        if (isShutdown()) {
            rejectCustom(command);
            return;
        }
        // Prestart a thread if necessary. We cannot prestart it
        // running the task because the task (probably) shouldn't be
        // run yet, so thread will just idle until delay elapses.
        if (getPoolSize() < getCorePoolSize()) {
            prestartCoreThread();
        }

        delayedWorkQueue.add(command);
    }

    /**
     * Worker threads
     */
    private class Worker implements Runnable {

        /**
         * The runLock is acquired and released surrounding each task execution. It mainly protects against interrupts that are intended to
         * cancel the worker thread from instead interrupting the task being run.
         */
        private final ReentrantLock runLock = new ReentrantLock();

        /**
         * Initial task to run before entering run loop
         */
        private Runnable firstTask;

        /**
         * Per thread last-start time stamp.
         */
        volatile long lastStart;

        /**
         * Per thread completed task counter; accumulated into completedTaskCount upon termination.
         */
        final AtomicLong completedTasks;

        /**
         * Thread this worker is running in. Acts as a final field, but cannot be set until thread is created.
         */
        Thread thread;

        Worker(final Runnable firstTask) {
            completedTasks = new AtomicLong();
            this.firstTask = firstTask;
        }

        boolean isActive() {
            return runLock.isLocked();
        }

        /**
         * Interrupt thread if not running a task
         */
        void interruptIfIdle() {
            final ReentrantLock runLock = this.runLock;
            if (runLock.tryLock()) {
                try {
                    thread.interrupt();
                } finally {
                    runLock.unlock();
                }
            }
        }

        /**
         * Cause thread to die even if running a task.
         */
        void interruptNow() {
            thread.interrupt();
        }

        /**
         * Run a single task between before/after methods.
         *
         * @throws RuntimeException If task execution fails unexpectedly
         */
        private void runTask(final Runnable task) {
            final ReentrantLock runLock = this.runLock;
            runLock.lock();
            try {
                // Abort now if immediate cancel. Otherwise, we have
                // committed to run this task.
                if (runState == STOP) {
                    return;
                }

                if (monitorThreads) {
                    lastStart = System.currentTimeMillis();
                }

                // Prepare thread
                Thread.interrupted(); // clear interrupt status on entry
                MDC.clear(); // Drop possible log properties

                boolean ran = false;
                beforeExecute(thread, task);
                try {
                    task.run();
                    ran = true;
                    afterExecute(task, null);
                    completedTasks.incrementAndGet();
                } catch (final RuntimeException ex) {
                    if (!ran) {
                        afterExecute(task, ex);
                    }
                    // Else the exception occurred within
                    // afterExecute itself in which case we don't
                    // want to call it again.
                    throw ex;
                }
            } finally {
                runLock.unlock();
            }
        }

        /**
         * Main run loop
         */
        @Override
        public void run() {
            try {
                Runnable task = firstTask;
                firstTask = null;
                while ((null != task) || (null != (task = getTaskCustom()))) {
                    runTask(task);
                    task = null; // unnecessary but can help GC
                }
            } catch (final InterruptedException ie) {
                // fall through
            } catch (final RuntimeException re) {
                // Task execution failed horribly
                LOG.error("Task execution failed unexpectedly", re);
            } finally {
                workerDone(this);
            }
        }
    }

    private class ScheduledFutureTask<V> extends FutureTask<V> implements ScheduledFuture<V> {

        /** Sequence number to break ties FIFO */
        private final long sequenceNumber;

        /** The time the task is enabled to execute in nanoTime units */
        private long time;

        /**
         * Period in nanoseconds for repeating tasks. A positive value indicates fixed-rate execution. A negative value indicates
         * fixed-delay execution. A value of 0 indicates a non-repeating task.
         */
        private final long period;

        /**
         * The task being executed.
         */
        private final Object task;

        /**
         * Creates a one-shot action with given nanoTime-based trigger time
         */
        ScheduledFutureTask(final Runnable r, final V result, final long ns) {
            super(r, result);
            this.task = r;
            this.time = ns;
            this.period = 0;
            this.sequenceNumber = getSequencer().getAndIncrement();
        }

        /**
         * Creates a periodic action with given nano time and period
         */
        ScheduledFutureTask(final Runnable r, final V result, final long ns, final long period) {
            super(r, result);
            this.task = r;
            this.time = ns;
            this.period = period;
            this.sequenceNumber = getSequencer().getAndIncrement();
        }

        /**
         * Creates a one-shot action with given nanoTime-based trigger
         */
        ScheduledFutureTask(final Callable<V> callable, final long ns) {
            super(callable);
            this.task = callable;
            this.time = ns;
            this.period = 0;
            this.sequenceNumber = getSequencer().getAndIncrement();
        }

        /**
         * Gets the actual task instance
         *
         * @return The actual task instance
         */
        public Object getWrapped() {
            return task;
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return unit.convert(time - now(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(final Delayed other) {
            if (other == this) {
                return 0;
            }
            if (other instanceof ScheduledFutureTask<?>) {
                final ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
                final long diff = time - x.time;
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                } else if (sequenceNumber < x.sequenceNumber) {
                    return -1;
                } else {
                    return 1;
                }
            }
            final long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }

        /**
         * Returns true if this is a periodic (not a one-shot) action.
         *
         * @return true if periodic
         */
        boolean isPeriodic() {
            return period != 0;
        }

        /**
         * Run a periodic task
         */
        private void runPeriodic() {
            final boolean ok = super.runAndReset();
            final boolean down = isShutdown();
            // Reschedule if not canceled and not shutdown or policy allows
            if (ok && (!down || (getContinueExistingPeriodicTasksAfterShutdownPolicy() && !isTerminating()))) {
                final long p = period;
                if (p > 0) {
                    time += p;
                } else {
                    time = triggerTime(-p);
                }
                getDelayedWorkQueue().add(this);
            }
            // This might have been the final executed delayed
            // task. Wake up threads to check.
            else if (down) {
                interruptIdleWorkersCustom();
            }
        }

        /**
         * Overrides FutureTask version so as to reset/requeue if periodic.
         */
        @Override
        public void run() {
            if (isPeriodic()) {
                runPeriodic();
            } else {
                super.run();
            }
        }
    }

    private final class DelayedQueueConsumer implements Runnable {

        volatile boolean cancelTasksOnShutdown;

        public DelayedQueueConsumer() {
            super();
            cancelTasksOnShutdown = false;
        }

        @Override
        public void run() {
            final Thread currentThread = Thread.currentThread();
            try {
                final boolean run = true;
                while (run && !currentThread.isInterrupted()) {
                    final Runnable command;
                    try {
                        command = getDelayedWorkQueue().take();
                    } catch (final InterruptedException e) {
                        // Ignore
                        continue;
                    }
                    /*
                     * Loop until scheduled task was fed to work queue
                     */
                    for (;;) {
                        if (isShutdown()) {
                            if (cancelTasksOnShutdown) {
                                // An orderly shutdown
                                cancelUnwantedTasks();
                            }
                            return;
                        }
                        /*
                         * Delegate task to a new thread if under core pool size
                         */
                        if ((getPoolSize() < getCorePoolSize()) && addIfUnderCorePoolSize(command)) {
                            break;
                        }
                        /*
                         * Offer task to work queue to let it be executed by an existing thread
                         */
                        if (getQueue().offer(command)) {
                            break;
                        }
                        /*
                         * At last spawn a new thread if under max. pool size
                         */
                        final Runnable r = addIfUnderMaximumPoolSize(command);
                        if (r == command) {
                            break;
                        }
                        /*
                         * All trials failed
                         */
                        if (null == r) {
                            /*-
                             * TODO: Periodic or one-time command could not be fed into work queue, by now "just try again!"
                             * Otherwise uncomment following lines to trigger rejected execution handler, but:
                             * For what caller?! Actually the caller is this thread therefore the handler should not throw
                             * an exception!
                             */
                            /*
                             * Run in this thread
                             */
                            try {
                                command.run();
                            } catch (final Exception e) {
                                LOG.error("", e);
                            }
                            // rejectCustom(command);
                            break;
                        }
                        // else retry
                    }
                }
            } catch (final Exception e) {
                LOG.error("{} thread aborted execution due to an exception! TimerService is no more active!", currentThread.getName(), e);
            }
        }
    }

    /**
     * An annoying wrapper class to convince generics compiler to use a DelayQueue<ScheduledFutureTask> as a BlockingQueue<Runnable>
     */
    private static class DelayedWorkQueue extends AbstractCollection<Runnable> implements BlockingQueue<Runnable> {

        final DelayQueue<ScheduledFutureTask<?>> dq = new DelayQueue<ScheduledFutureTask<?>>();

        public DelayedWorkQueue() {
            super();
        }

        @Override
        public Runnable poll() {
            return dq.poll();
        }

        @Override
        public Runnable peek() {
            return dq.peek();
        }

        @Override
        public Runnable take() throws InterruptedException {
            return dq.take();
        }

        @Override
        public Runnable poll(final long timeout, final TimeUnit unit) throws InterruptedException {
            return dq.poll(timeout, unit);
        }

        @Override
        public boolean add(final Runnable x) {
            return dq.add((ScheduledFutureTask<?>) x);
        }

        @Override
        public boolean offer(final Runnable x) {
            return dq.offer((ScheduledFutureTask<?>) x);
        }

        @Override
        public void put(final Runnable x) {
            dq.put((ScheduledFutureTask<?>) x);
        }

        @Override
        public boolean offer(final Runnable x, final long timeout, final TimeUnit unit) {
            return dq.offer((ScheduledFutureTask<?>) x, timeout, unit);
        }

        @Override
        public Runnable remove() {
            return dq.remove();
        }

        @Override
        public Runnable element() {
            return dq.element();
        }

        @Override
        public void clear() {
            dq.clear();
        }

        @Override
        public int drainTo(final Collection<? super Runnable> c) {
            return dq.drainTo(c);
        }

        @Override
        public int drainTo(final Collection<? super Runnable> c, final int maxElements) {
            return dq.drainTo(c, maxElements);
        }

        @Override
        public int remainingCapacity() {
            return dq.remainingCapacity();
        }

        @Override
        public boolean remove(final Object x) {
            return dq.remove(x);
        }

        @Override
        public boolean contains(final Object x) {
            return dq.contains(x);
        }

        @Override
        public int size() {
            return dq.size();
        }

        @Override
        public boolean isEmpty() {
            return dq.isEmpty();
        }

        @Override
        public Object[] toArray() {
            return dq.toArray();
        }

        @Override
        public <T> T[] toArray(final T[] array) {
            return dq.toArray(array);
        }

        @Override
        public Iterator<Runnable> iterator() {
            return new Iterator<Runnable>() {

                private final Iterator<ScheduledFutureTask<?>> it = dq.iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Runnable next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }
    }

    private final class ActiveTaskWatcher implements Runnable {
        private final ReentrantLock lock = new ReentrantLock(true);
        private final Condition notEmpty = lock.newCondition();
        private final ConcurrentMap<Long, TaskInfo> tasks;
        private final TaskInfo poison = new TaskInfo(null);
        private final long minWaitTime;
        private final long maxRunningTime;
        private final String lineSeparator;

        ActiveTaskWatcher() {
            super();
            lineSeparator = System.getProperty("line.separator");
            tasks = new NonBlockingHashMap<Long, TaskInfo>(8192);
            final ConfigurationService service = ThreadPoolServiceRegistry.getService(ConfigurationService.class);
            minWaitTime = null == service ? 20000L : service.getIntProperty("com.openexchange.requestwatcher.frequency", 20000);
            maxRunningTime = null == service ? 60000L : service.getIntProperty("com.openexchange.requestwatcher.maxRequestAge", 60000);
        }

        void stopWhenFinished() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                tasks.put(Long.valueOf(Long.MAX_VALUE), poison);
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        void addTask(final long number, final Thread thread, final Map<String, Object> logProperties) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                tasks.put(Long.valueOf(number), new TaskInfo(thread, logProperties));
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        void removeTask(final long number) {
            tasks.remove(Long.valueOf(number));
        }

        @Override
        public void run() {
            try {
                final String lineSeparator = this.lineSeparator;
                final long maxRunningTime = this.maxRunningTime;
                final TaskInfo poison = this.poison;
                final long minWaitTimeNanos = TimeUnit.MILLISECONDS.toNanos(minWaitTime);
                for (;;) {
                    try {
                        LockSupport.parkNanos(minWaitTimeNanos);
                        if (tasks.isEmpty()) {
                            final ReentrantLock lock = this.lock;
                            lock.lockInterruptibly();
                            try {
                                try {
                                    while (tasks.isEmpty()) {
                                        notEmpty.await();
                                    }
                                } catch (final InterruptedException ie) {
                                    notEmpty.signal(); // propagate to non-interrupted thread
                                    throw ie;
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                        // Check for exceeded tasks
                        final long max = System.currentTimeMillis() - maxRunningTime;
                        final StringBuilder logBuilder = new StringBuilder(1024);
                        boolean poisoned = false;
                        for (final TaskInfo taskInfo : tasks.values()) {
                            if (poison == taskInfo) {
                                poisoned = true;
                                break;
                            }
                            if (taskInfo.stamp < max) {
                                final Thread thread = taskInfo.t;
                                final Map<String, Object> logProperties = taskInfo.logProperties;
                                logBuilder.setLength(0);
                                if (null != logProperties) {
                                    final Map<String, String> sorted = new TreeMap<String, String>();
                                    for (final Map.Entry<String, Object> entry : logProperties.entrySet()) {
                                        final String propertyName = entry.getKey();
                                        final Object value = entry.getValue();
                                        if (null != value) {
                                            sorted.put(propertyName, value.toString());
                                        }
                                    }
                                    for (final Map.Entry<String, String> entry : sorted.entrySet()) {
                                        logBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append(lineSeparator);
                                    }
                                    logBuilder.append(lineSeparator);
                                }
                                logBuilder.append("Worker \"").append(thread.getName());
                                logBuilder.append("\" exceeds max. running time of ").append(maxRunningTime);
                                logBuilder.append("msec -> Processing time: ").append(System.currentTimeMillis() - taskInfo.stamp);
                                logBuilder.append("msec");
                                final Throwable t = new FastThrowable();
                                t.setStackTrace(thread.getStackTrace());
                                LOG.info(logBuilder.toString(), t);
                            }
                        }
                        if (poisoned) {
                            return;
                        }
                    } catch (InterruptedException e) {
                        LOG.debug("Watcher run interrupted", e);
                    } catch (final Exception e) {
                        LOG.error("Watcher run aborted due to an exception!", e);
                    }
                }
            } catch (final Exception e) {
                LOG.error("Watcher aborted execution due to an exception! Watcher is no more active!", e);
            }
        }

        void appendStackTrace(final StackTraceElement[] trace, final StringBuilder sb) {
            if (null == trace) {
                return;
            }
            final String lineSeparator = this.lineSeparator;
            for (final StackTraceElement ste : trace) {
                final String className = ste.getClassName();
                if (null != className) {
                    sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                    if (ste.isNativeMethod()) {
                        sb.append("(Native Method)");
                    } else {
                        final String fileName = ste.getFileName();
                        if (null == fileName) {
                            sb.append("(Unknown Source)");
                        } else {
                            final int lineNumber = ste.getLineNumber();
                            sb.append('(').append(fileName);
                            if (lineNumber >= 0) {
                                sb.append(':').append(lineNumber);
                            }
                            sb.append(')');
                        }
                    }
                    sb.append(lineSeparator);
                }
            }
        }
    }

    private static final class TaskInfo {
        final Thread t;
        final long stamp;
        final Map<String, Object> logProperties;

        TaskInfo(final Thread t) {
            this(t, null);
        }

        TaskInfo(final Thread t, final Map<String, Object> logProperties) {
            super();
            this.t = t;
            stamp = System.currentTimeMillis();
            this.logProperties = logProperties;
        }
    }

    private static final class FastThrowable extends Throwable {

        FastThrowable() {
            super();
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    // Public methods

    /**
     * Creates a new <tt>CustomThreadPoolExecutor</tt> with the given initial parameters and default thread factory and handler. It may be
     * more convenient to use one of the {@link Executors} factory methods instead of this general purpose constructor.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will hold only the <tt>Runnable</tt> tasks
     *            submitted by the <tt>execute</tt> method.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> is <code>null</code>
     */
    public CustomThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, false, Executors.defaultThreadFactory(), DEFAULT_HANDLER);
    }

    /**
     * Creates a new <tt>CustomThreadPoolExecutor</tt> with the given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will hold only the <tt>Runnable</tt> tasks
     *            submitted by the <tt>execute</tt> method.
     * @param threadFactory the factory to use when the executor creates a new thread.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>threadFactory</tt> are <code>null</code>.
     */
    public CustomThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, false, threadFactory, DEFAULT_HANDLER);
    }

    /**
     * Creates a new <tt>CustomThreadPoolExecutor</tt> with the given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will hold only the <tt>Runnable</tt> tasks
     *            submitted by the <tt>execute</tt> method.
     * @param handler the handler to use when execution is blocked because the thread bounds and queue capacities are reached.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>handler</tt> are <code>null</code>.
     */
    public CustomThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, false, Executors.defaultThreadFactory(), handler);
    }

    /**
     * Creates a new <tt>CustomThreadPoolExecutor</tt> with the given initial parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait
     *            for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will hold only the <tt>Runnable</tt> tasks
     *            submitted by the <tt>execute</tt> method.
     * @param blocking Whether this executor has a blocking behavior. Meaning caller is blocked until space becomes available in working
     *            queue. Otherwise {@link RejectedExecutionException} are be thrown in capacity bounds are reached. <code>true</code>
     *            requires the passed work queue to have a boundary restriction.
     * @param threadFactory the factory to use when the executor creates a new thread.
     * @param handler the handler to use when execution is blocked because the thread bounds and queue capacities are reached.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if maximumPoolSize less than or equal to zero,
     *             or if corePoolSize greater than maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>threadFactory</tt> or <tt>handler</tt> are <code>null</code>.
     */
    public CustomThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final boolean blocking, final ThreadFactory threadFactory, final RejectedExecutionHandler handler) {
        super(0, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1));
        if ((corePoolSize < 0) || (maximumPoolSize <= 0) || (maximumPoolSize < corePoolSize) || (keepAliveTime < 0)) {
            throw new IllegalArgumentException();
        }
        if ((null == workQueue) || (null == threadFactory) || (null == handler)) {
            throw new NullPointerException();
        }
        this.blocking = blocking;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
        delayedWorkQueue = new DelayedWorkQueue();
        executeExistingDelayedTasksAfterShutdown = true;
        /*
         * Start consumer thread
         */
        delayedQueueConsumer = new DelayedQueueConsumer();
        consumerThread = new Thread(delayedQueueConsumer, "DelayedQueueConsumer");
        consumerThread.start();
        /*
         * Start watcher thread
         */
        activeTaskWatcher = new ActiveTaskWatcher();
        watcherThread = new Thread(activeTaskWatcher, "ActiveTaskWatcher");
        watcherThread.start();
        /*
         * Monitor threads
         */
        monitorThreads = false;
    }

    /**
     * Starts to monitor active threads for elapsed ones.
     *
     * @param maxRunningMillis The max. allowed number if milliseconds a thread is allowed to be active
     * @param delayMillis The frequency in milliseconds when to check for possible elapsed threads
     */
    public void startMonitorThreads(final long maxRunningMillis, final long delayMillis) {
        if (monitorThreads) {
            final Set<Worker> workerSet = this.workerSet;
            final Runnable monitorThread = new Runnable() {

                @Override
                public void run() {
                    final long stamp = System.currentTimeMillis() - maxRunningMillis;
                    for (final Worker worker : workerSet) {
                        if (worker.isActive() && worker.lastStart < stamp) {
                            // Elapsed worker detected
                            worker.interruptNow();
                            // worker calls itself workerDone(this); in its main run() loop to perform bookkeeping
                        }
                    }
                }
            };
            monitorFuture = scheduleWithFixedDelay(monitorThread, delayMillis, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops to monitor threads.
     */
    public void stopMonitorThreads() {
        final ScheduledFuture<?> monitorFuture = this.monitorFuture;
        if (null != monitorFuture) {
            monitorFuture.cancel(false);
            this.monitorFuture = null;
        }
    }

    @Override
    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        if ((null == command) || (null == unit)) {
            throw new NullPointerException();
        }
        final ScheduledFutureTask<?> t = new ScheduledFutureTask<Boolean>(command, null, triggerTime(delay, unit));
        if (runState != RUNNING) {
            rejectCustom(t);
        } else {
            delayedExecute(t);
        }
        return t;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        if ((null == callable) || (null == unit)) {
            throw new NullPointerException();
        }
        final ScheduledFutureTask<V> t = new ScheduledFutureTask<V>(callable, triggerTime(delay, unit));
        if (runState != RUNNING) {
            rejectCustom(t);
        } else {
            delayedExecute(t);
        }
        return t;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        if ((null == command) || (null == unit)) {
            throw new NullPointerException();
        }
        if (period <= 0) {
            throw new IllegalArgumentException();
        }
        final ScheduledFutureTask<?> t =
            new ScheduledFutureTask<Object>(command, null, triggerTime(initialDelay, unit), unit.toNanos(period));
        if (runState != RUNNING) {
            rejectCustom(t);
        } else {
            delayedExecute(t);
        }
        return t;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        if ((null == command) || (null == unit)) {
            throw new NullPointerException();
        }
        if (delay <= 0) {
            throw new IllegalArgumentException();
        }
        final ScheduledFutureTask<?> t =
            new ScheduledFutureTask<Boolean>(command, null, triggerTime(initialDelay, unit), unit.toNanos(-delay));
        if (runState != RUNNING) {
            rejectCustom(t);
        } else {
            delayedExecute(t);
        }
        return t;
    }

    @Override
    public Future<?> submit(final Runnable task) {
        throw new UnsupportedOperationException("CustomThreadPoolExecutor.submit(Runnable task)");
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        throw new UnsupportedOperationException("CustomThreadPoolExecutor.submit(Runnable task, T result)");
    }

    @Override
    public <T> Future<T> submit(final Callable<T> callable) {
        if (null == callable) {
            throw new NullPointerException();
        }
        final Task<T> task;
        if (callable instanceof Task<?>) {
            task = (Task<T>) callable;
        } else {
            task = new AbstractTask<T>() {

                @Override
                public T call() throws Exception {
                    return callable.call();
                }
            };
        }
        final CustomFutureTask<T> ftask = new CustomFutureTask<T>(task, callable instanceof MdcProvider ? ((MdcProvider) callable).getMdc() : MDC.getCopyOfContextMap());
        execute(ftask);
        return ftask;
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable throwable) {
        super.afterExecute(r, throwable);
        closeCloseables();
        deleteTempFiles();
        MDC.clear(); // Drop possible log properties
        if (r instanceof CustomFutureTask<?>) {
            final CustomFutureTask<?> customFutureTask = (CustomFutureTask<?>) r;
            if (customFutureTask.isTrackable()) {
                activeTaskWatcher.removeTask(customFutureTask.getNumber());
            }
            customFutureTask.getTask().afterExecute(throwable);
            /*
             * Restore original name
             */
            ((CustomThread) Thread.currentThread()).restoreName();
        } else if (r instanceof ScheduledFutureTask<?>) {
            ((CustomThread) Thread.currentThread()).restoreName();
        }
        activeCount.decrementAndGet();
    }

    @Override
    protected void beforeExecute(final Thread thread, final Runnable r) {
        activeCount.incrementAndGet();
        thread.setUncaughtExceptionHandler(CustomUncaughtExceptionhandler.getInstance());
        if (r instanceof CustomFutureTask<?>) {
            final CustomFutureTask<?> customFutureTask = (CustomFutureTask<?>) r;
            final Task<?> task = customFutureTask.getTask();
            task.setThreadName((ThreadRenamer) thread);
            task.beforeExecute(thread);
            // MDC map for executing thread
            {
                Map<String, Object> mdc = customFutureTask.getMdc();
                if (null != mdc) {
                    MDC.setContextMap(mdc);
                }
            }
            // Check if trackable
            if (customFutureTask.isTrackable()) {
                activeTaskWatcher.addTask(customFutureTask.getNumber(), thread, customFutureTask.getMdc());
            }
        } else if (r instanceof MdcProvider) {
            // MDC map for executing thread
            final Map<String, Object> mdc = ((MdcProvider) r).getMdc();
            if (null != mdc) {
                MDC.setContextMap(mdc);
            }
        } else if (r instanceof ScheduledFutureTask<?>) {
            ((ThreadRenamer) thread).renamePrefix("OXTimer");
        }
        super.beforeExecute(thread, r);
    }

    /**
     * Executes the given task sometime in the future. The task may execute in a new thread or in an existing pooled thread. If the task
     * cannot be submitted for execution, either because this executor has been shutdown or because its capacity has been reached, the task
     * is handled by the current <tt>RejectedExecutionHandler</tt>.
     *
     * @param command the task to execute
     * @throws RejectedExecutionException at discretion of <tt>RejectedExecutionHandler</tt>, if task cannot be accepted for execution
     * @throws NullPointerException if command is <code>null</code>
     */
    @Override
    public void execute(final Runnable command) {
        if (null == command) {
            throw new NullPointerException();
        }

        final Runnable mdcCommand = command instanceof MdcProvider ? command : new MDCProvidingRunnable(command, MDC.getCopyOfContextMap());
        if (blocking) {
            if (runState != RUNNING) {
                rejectCustom(mdcCommand);
                return;
            }
            if (poolSize.get() < corePoolSize && addIfUnderCorePoolSize(mdcCommand)) {
                return;
            }
            /*
             * Wait for space in work queue
             */
            boolean acquired = false;
            do {
                try {
                    workQueue.put(mdcCommand);
                    acquired = true;
                } catch (final InterruptedException e) {
                    /*
                     * wait forever!
                     */
                }
            } while (!acquired);
        } else {
            for (;;) {
                if (runState != RUNNING) {
                    rejectCustom(mdcCommand);
                    return;
                }
                if (poolSize.get() < corePoolSize && addIfUnderCorePoolSize(mdcCommand)) {
                    return;
                }
                /*
                 * Non-blocking behavior, just offer and accept a possible rejected execution exception
                 */
                if (workQueue.offer(mdcCommand)) {
                    return;
                }
                final Runnable r = addIfUnderMaximumPoolSize(mdcCommand);
                if (r == mdcCommand) {
                    return;
                }
                if (null == r) {
                    rejectCustom(mdcCommand);
                    return;
                }
                // else retry
            }
        }
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no
     * additional effect if already shut down.
     *
     * @throws SecurityException if a security manager exists and shutting down this ExecutorService may manipulate threads that the caller
     *             is not permitted to modify because it does not hold {@link java.lang.RuntimePermission}<tt>("modifyThread")</tt>, or the
     *             security manager's <tt>checkAccess</tt> method denies access.
     */
    @Override
    public void shutdown() {
        // Fail if caller doesn't have modifyThread permission. We
        // explicitly check permissions directly because we can't trust
        // implementations of SecurityManager to correctly override
        // the "check access" methods such that our documented
        // security policy is implemented.
        final SecurityManager security = System.getSecurityManager();
        if (security != null) {
            java.security.AccessController.checkPermission(shutdownPerm);
        }

        boolean fullyTerminated = false;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            delayedQueueConsumer.cancelTasksOnShutdown = true;
            consumerThread.interrupt();
            activeTaskWatcher.stopWhenFinished();
            watcherThread.interrupt();
            if (workers.size() > 0) {
                // Check if caller can modify worker threads. This
                // might not be true even if passed above check, if
                // the SecurityManager treats some threads specially.
                if (null != security) {
                    for (final Worker w : workerSet) {
                        security.checkAccess(w.thread);
                    }
                }

                final int state = runState;
                if (RUNNING == state) {
                    runState = SHUTDOWN;
                }

                try {
                    for (final Worker w : workerSet) {
                        w.interruptIfIdle();
                    }
                } catch (final SecurityException se) {
                    // If SecurityManager allows above checks, but
                    // then unexpectedly throws exception when
                    // interrupting threads (which it ought not do),
                    // back out as cleanly as we can. Some threads may
                    // have been killed but we remain in non-shutdown
                    // state.
                    runState = state;
                    throw se;
                }
            } else { // If no workers, trigger full termination now
                fullyTerminated = true;
                runState = TERMINATED;
                termination.signalAll();
            }
        } finally {
            mainLock.unlock();
        }
        if (fullyTerminated) {
            terminated();
        }
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and returns a list of the tasks that were
     * awaiting execution.
     * <p>
     * This implementation cancels tasks via {@link Thread#interrupt}, so if any tasks mask or fail to respond to interrupts, they may never
     * terminate.
     *
     * @return list of tasks that never commenced execution
     * @throws SecurityException if a security manager exists and shutting down this ExecutorService may manipulate threads that the caller
     *             is not permitted to modify because it does not hold {@link java.lang.RuntimePermission}<tt>("modifyThread")</tt>, or the
     *             security manager's <tt>checkAccess</tt> method denies access.
     */
    @Override
    public List<Runnable> shutdownNow() {
        // Almost the same code as shutdown()
        final SecurityManager security = System.getSecurityManager();
        if (security != null) {
            java.security.AccessController.checkPermission(shutdownPerm);
        }

        boolean fullyTerminated = false;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            consumerThread.interrupt();
            activeTaskWatcher.stopWhenFinished();
            watcherThread.interrupt();
            if (workers.size() > 0) {
                if (security != null) {
                    for (final Worker w : workerSet) {
                        security.checkAccess(w.thread);
                    }
                }

                final int state = runState;
                if (state != TERMINATED) {
                    runState = STOP;
                }
                try {
                    for (final Worker w : workerSet) {
                        w.interruptNow();
                    }
                } catch (final SecurityException se) {
                    runState = state; // back out;
                    throw se;
                }
            } else { // If no workers, trigger full termination now
                fullyTerminated = true;
                runState = TERMINATED;
                termination.signalAll();
            }
        } finally {
            mainLock.unlock();
        }
        if (fullyTerminated) {
            terminated();
        }
        return Arrays.asList(workQueue.toArray(EMPTY_RUNNABLE_ARRAY));
    }

    @Override
    public boolean isShutdown() {
        return runState != RUNNING;
    }

    /**
     * Returns true if this executor is in the process of terminating after <tt>shutdown</tt> or <tt>shutdownNow</tt> but has not completely
     * terminated. This method may be useful for debugging. A return of <tt>true</tt> reported a sufficient period after shutdown may
     * indicate that submitted tasks have ignored or suppressed interruption, causing this executor not to properly terminate.
     *
     * @return true if terminating but not yet terminated.
     */
    @Override
    public boolean isTerminating() {
        return runState == STOP;
    }

    @Override
    public boolean isTerminated() {
        return runState == TERMINATED;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runState == TERMINATED) {
                    return true;
                }
                if (nanos <= 0) {
                    return false;
                }
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Invokes <tt>shutdown</tt> when this executor is no longer referenced.
     */
    @Override
    protected void finalize() {
        super.finalize();
        shutdown();
    }

    /**
     * Sets the thread factory used to create new threads.
     *
     * @param threadFactory the new thread factory
     * @throws NullPointerException if threadFactory is <code>null</code>
     * @see #getThreadFactory
     */
    @Override
    public void setThreadFactory(final ThreadFactory threadFactory) {
        if (null == threadFactory) {
            throw new NullPointerException();
        }
        this.threadFactory = threadFactory;
    }

    /**
     * Returns the thread factory used to create new threads.
     *
     * @return the current thread factory
     * @see #setThreadFactory
     */
    @Override
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * Sets whether this executor has a blocking behavior. Meaning caller is blocked until space becomes available in working queue.
     * Otherwise {@link RejectedExecutionException} are be thrown in capacity bounds are reached.
     *
     * @param blocking <code>true</code> if blocking; otherwise <code>false</code>
     */
    public void setBlocking(final boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * Checks whether this executor has a blocking behavior. Meaning caller is blocked until space becomes available in working queue.
     * Otherwise {@link RejectedExecutionException} are be thrown in capacity bounds are reached.
     *
     * @return <code>true</code> if blocking; otherwise <code>false</code>
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * Sets a new handler for unexecutable tasks.
     *
     * @param handler the new handler
     * @throws NullPointerException if handler is <code>null</code>
     * @see #getRejectedExecutionHandler
     */
    @Override
    public void setRejectedExecutionHandler(final RejectedExecutionHandler handler) {
        if (null == handler) {
            throw new NullPointerException();
        }
        this.handler = handler;
    }

    /**
     * Returns the current handler for unexecutable tasks.
     *
     * @return the current handler
     * @see #setRejectedExecutionHandler
     */
    @Override
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    /**
     * Returns the task queue used by this executor. Access to the task queue is intended primarily for debugging and monitoring. This queue
     * may be in active use. Retrieving the task queue does not prevent queued tasks from executing.
     *
     * @return the task queue
     */
    @Override
    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    /**
     * Gets the delayed work queue.
     *
     * @return The delayed work queue
     */
    public DelayedWorkQueue getDelayedWorkQueue() {
        return delayedWorkQueue;
    }

    /**
     * Removes this task from the executor's internal queue if it is present, thus causing it not to be run if it has not already started.
     * <p>
     * This method may be useful as one part of a cancellation scheme. It may fail to remove tasks that have been converted into other forms
     * before being placed on the internal queue. For example, a task entered using <tt>submit</tt> might be converted into a form that
     * maintains <tt>Future</tt> status. However, in such cases, method {@link CustomThreadPoolExecutor#purge} may be used to remove those
     * Futures that have been canceled.
     *
     * @param task the task to remove
     * @return true if the task was removed
     */
    @Override
    public boolean remove(final Runnable task) {
        if (task instanceof ScheduledFutureTask<?>) {
            return delayedWorkQueue.remove(task);
        }
        return workQueue.remove(task);
    }

    /**
     * Tries to remove from the work queue all {@link Future} tasks that have been canceled. This method can be useful as a storage
     * reclamation operation, that has no other impact on functionality. Canceled tasks are never executed, but may accumulate in work
     * queues until worker threads can actively remove them. Invoking this method instead tries to remove them now. However, this method may
     * fail to remove tasks in the presence of interference by other threads.
     */
    @Override
    public void purge() {
        // Fail if we encounter interference during traversal
        try {
            for (final Iterator<Runnable> it = workQueue.iterator(); it.hasNext();) {
                final Runnable r = it.next();
                if (r instanceof Future<?>) {
                    final Future<?> c = (Future<?>) r;
                    if (c.isCancelled()) {
                        it.remove();
                    }
                }
            }
            for (final Iterator<Runnable> it = delayedWorkQueue.iterator(); it.hasNext();) {
                final Runnable r = it.next();
                if (r instanceof Future<?>) {
                    final Future<?> c = (Future<?>) r;
                    if (c.isCancelled()) {
                        it.remove();
                    }
                }
            }
        } catch (final ConcurrentModificationException ex) {
            return;
        }
    }

    /**
     * Sets the core number of threads. This overrides any value set in the constructor. If the new value is smaller than the current value,
     * excess existing threads will be terminated when they next become idle. If larger, new threads will, if needed, be started to execute
     * any queued tasks.
     *
     * @param corePoolSize the new core size
     * @throws IllegalArgumentException if <tt>corePoolSize</tt> less than zero
     * @see #getCorePoolSize
     */
    @Override
    public void setCorePoolSize(final int corePoolSize) {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException();
        }
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int extra = this.corePoolSize - corePoolSize;
            this.corePoolSize = corePoolSize;
            if (extra < 0) {
                int n = workQueue.size();
                // We have to create initially-idle threads here
                // because we otherwise have no recourse about
                // what to do with a dequeued task if addThread fails.
                while (extra++ < 0 && n-- > 0 && poolSize.get() < corePoolSize) {
                    final Thread t = addThread(null);
                    if (t != null) {
                        t.start();
                    } else {
                        break;
                    }
                }
            } else if (extra > 0 && poolSize.get() > corePoolSize) {
                final Iterator<Worker> it = workerSet.iterator();
                while (it.hasNext() && extra-- > 0 && poolSize.get() > corePoolSize && workQueue.remainingCapacity() == 0) {
                    it.next().interruptIfIdle();
                }
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the core number of threads.
     *
     * @return the core number of threads
     * @see #setCorePoolSize
     */
    @Override
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Starts a core thread, causing it to idly wait for work. This overrides the default policy of starting core threads only when new
     * tasks are executed. This method will return <tt>false</tt> if all core threads have already been started.
     *
     * @return true if a thread was started
     */
    @Override
    public boolean prestartCoreThread() {
        return addIfUnderCorePoolSize(null);
    }

    /**
     * Starts all core threads, causing them to idly wait for work. This overrides the default policy of starting core threads only when new
     * tasks are executed.
     *
     * @return the number of threads started.
     */
    @Override
    public int prestartAllCoreThreads() {
        int n = 0;
        while (addIfUnderCorePoolSize(null)) {
            ++n;
        }
        return n;
    }

    /**
     * Sets the maximum allowed number of threads. This overrides any value set in the constructor. If the new value is smaller than the
     * current value, excess existing threads will be terminated when they next become idle.
     *
     * @param maximumPoolSize the new maximum
     * @throws IllegalArgumentException if maximumPoolSize less than zero or the {@link #getCorePoolSize core pool size}
     * @see #getMaximumPoolSize
     */
    @Override
    public void setMaximumPoolSize(final int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException();
        }
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int extra = this.maximumPoolSize - maximumPoolSize;
            this.maximumPoolSize = maximumPoolSize;
            if (extra > 0 && poolSize.get() > maximumPoolSize) {
                final Iterator<Worker> it = workerSet.iterator();
                while (it.hasNext() && extra > 0 && poolSize.get() > maximumPoolSize) {
                    it.next().interruptIfIdle();
                    --extra;
                }
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the maximum allowed number of threads.
     *
     * @return the maximum allowed number of threads
     * @see #setMaximumPoolSize
     */
    @Override
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * Sets the time limit for which threads may remain idle before being terminated. If there are more than the core number of threads
     * currently in the pool, after waiting this amount of time without processing a task, excess threads will be terminated. This overrides
     * any value set in the constructor.
     *
     * @param time the time to wait. A time value of zero will cause excess threads to terminate immediately after executing tasks.
     * @param unit the time unit of the time argument
     * @throws IllegalArgumentException if time less than zero
     * @see #getKeepAliveTime
     */
    @Override
    public void setKeepAliveTime(final long time, final TimeUnit unit) {
        if (time < 0) {
            throw new IllegalArgumentException();
        }
        keepAliveTime = unit.toNanos(time);
    }

    /**
     * Returns the thread keep-alive time, which is the amount of time which threads in excess of the core pool size may remain idle before
     * being terminated.
     *
     * @param unit the desired time unit of the result
     * @return the time limit
     * @see #setKeepAliveTime
     */
    @Override
    public long getKeepAliveTime(final TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    /**
     * Set policy on whether to continue executing existing periodic tasks even when this executor has been <tt>shutdown</tt>. In this case,
     * these tasks will only terminate upon <tt>shutdownNow</tt>, or after setting the policy to <tt>false</tt> when already shutdown. This
     * value is by default false.
     *
     * @param value if true, continue after shutdown, else don't.
     * @see #getExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(final boolean value) {
        continueExistingPeriodicTasksAfterShutdown = value;
        if (!value && isShutdown()) {
            cancelUnwantedTasks();
        }
    }

    /**
     * Get the policy on whether to continue executing existing periodic tasks even when this executor has been <tt>shutdown</tt>. In this
     * case, these tasks will only terminate upon <tt>shutdownNow</tt> or after setting the policy to <tt>false</tt> when already shutdown.
     * This value is by default false.
     *
     * @return true if will continue after shutdown.
     * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy
     */
    public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
        return continueExistingPeriodicTasksAfterShutdown;
    }

    /**
     * Set policy on whether to execute existing delayed tasks even when this executor has been <tt>shutdown</tt>. In this case, these tasks
     * will only terminate upon <tt>shutdownNow</tt>, or after setting the policy to <tt>false</tt> when already shutdown. This value is by
     * default true.
     *
     * @param value if true, execute after shutdown, else don't.
     * @see #getExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(final boolean value) {
        executeExistingDelayedTasksAfterShutdown = value;
        if (!value && isShutdown()) {
            cancelUnwantedTasks();
        }
    }

    /**
     * Get policy on whether to execute existing delayed tasks even when this executor has been <tt>shutdown</tt>. In this case, these tasks
     * will only terminate upon <tt>shutdownNow</tt>, or after setting the policy to <tt>false</tt> when already shutdown. This value is by
     * default true.
     *
     * @return true if will execute after shutdown.
     * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy
     */
    public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
        return executeExistingDelayedTasksAfterShutdown;
    }

    /* Statistics */

    /**
     * Returns the current number of threads in the pool.
     *
     * @return the number of threads
     */
    @Override
    public int getPoolSize() {
        return poolSize.get();
    }

    /**
     * Returns the approximate number of threads that are actively executing tasks.
     *
     * @return the number of threads
     */
    @Override
    public int getActiveCount() {
        return activeCount.get();
    }

    /**
     * Returns the largest number of threads that have ever simultaneously been in the pool.
     *
     * @return the number of threads
     */
    @Override
    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate total number of tasks that have been scheduled for execution. Because the states of tasks and threads may
     * change dynamically during computation, the returned value is only an approximation, but one that does not ever decrease across
     * successive calls.
     *
     * @return the number of tasks
     */
    @Override
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (final Worker w : workerSet) {
                n += w.completedTasks.get();
                if (w.isActive()) {
                    ++n;
                }
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the approximate total number of tasks that have completed execution. Because the states of tasks and threads may change
     * dynamically during computation, the returned value is only an approximation, but one that does not ever decrease across successive
     * calls.
     *
     * @return the number of tasks
     */
    @Override
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (final Worker w : workerSet) {
                n += w.completedTasks.get();
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Method invoked when the Executor has terminated. Default implementation does nothing. Note: To properly nest multiple overridings,
     * subclasses should generally invoke <tt>super.terminated</tt> within this method.
     */
    @Override
    protected void terminated() {
        // Override if necessary
    }

    /**
     * A handler for rejected tasks that runs the rejected task directly in the calling thread of the <tt>execute</tt> method, unless the
     * executor has been shut down, in which case the task is discarded.
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {

        /**
         * Creates a <tt>CallerRunsPolicy</tt>.
         */
        public CallerRunsPolicy() {
            super();
        }

        /**
         * Executes task r in the caller's thread, unless the executor has been shut down, in which case the task is discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    /**
     * A handler for rejected tasks that throws a <tt>RejectedExecutionException</tt>.
     */
    public static class AbortPolicy implements RejectedExecutionHandler {

        /**
         * Creates an <tt>AbortPolicy</tt>.
         */
        public AbortPolicy() {
            super();
        }

        /**
         * Always throws RejectedExecutionException.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         * @throws RejectedExecutionException always.
         */
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Thread pool is overloaded!");
        }
    }

    /**
     * A handler for rejected tasks that silently discards the rejected task.
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {

        /**
         * Creates a <tt>DiscardPolicy</tt>.
         */
        public DiscardPolicy() {
            super();
        }

        /**
         * Does nothing, which has the effect of discarding task r.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            // Discard silently
        }
    }

    /**
     * A handler for rejected tasks that discards the oldest unhandled request and then retries <tt>execute</tt>, unless the executor is
     * shut down, in which case the task is discarded.
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {

        /**
         * Creates a <tt>DiscardOldestPolicy</tt> for the given executor.
         */
        public DiscardOldestPolicy() {
            super();
        }

        /**
         * Obtains and ignores the next task that the executor would otherwise execute, if one is immediately available, and then retries
         * execution of task r, unless the executor is shut down, in which case task r is instead discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }

    /**
     * Enhances a {@link Runnable} by MDC properties.
     */
    private static final class MDCProvidingRunnable implements Runnable, MdcProvider {

        private final Runnable delegate;
        private final Map<String, Object> mdc;

        MDCProvidingRunnable(Runnable delegate, Map<String, Object> mdc) {
            super();
            this.delegate = delegate;
            this.mdc = mdc;
        }

        @Override
        public Map<String, Object> getMdc() {
            return mdc;
        }

        @Override
        public void run() {
            delegate.run();
        }
    }

    /**
     * Closes tracked {@link Closeable} instances.
     */
    static void closeCloseables() {
        CloseableControlService closeableControl = REF_CLOSEABLE_CONTROL.get();
        if (null != closeableControl) {
            closeableControl.closeAll();
        }
    }

    /**
     * Deletes tracked temporary files.
     */
    static void deleteTempFiles() {
        String[] tempFiles = LogProperties.getAndRemoveTempFiles();
        if (null != tempFiles) {
            for (String path : tempFiles) {
                File f = new File(path);
                f.delete();
            }
        }
    }

}
