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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.tools.images.scheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.images.osgi.Services;

/**
 * {@link Scheduler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class Scheduler {

    /** The logger */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Scheduler.class);

    private static interface TaskManager {

        /**
         * Removes the next available task from this executer
         *
         * @return The next task or <code>null</code>
         */
        Runnable remove();

        /**
         * Adds given task to this executer.
         *
         * @param task The task to add
         */
        void add(Runnable task);

        /**
         * Gets the key object
         *
         * @return The key object
         */
        Object getExecuterKey();
    }

    /** The poison element */
    static final TaskManager POISON = new TaskManager() {

        @Override
        public Runnable remove() {
            return null;
        }

        @Override
        public void add(Runnable task) {
            // Nothing
        }

        @Override
        public Object getExecuterKey() {
            return null;
        }
    };

    private static volatile Scheduler instance;

    /**
     * Shuts-down the scheduler
     */
    public static void shutDown() {
        Scheduler tmp = instance;
        if (null != tmp) {
            synchronized (Scheduler.class) {
                tmp = instance;
                if (null != tmp) {
                    tmp.stop();
                    instance = null;
                }
            }
        }
    }

    /**
     * Gets the scheduler instance.
     *
     * @return The instance
     */
    public static Scheduler getInstance() {
        Scheduler tmp = instance;
        if (null == tmp) {
            synchronized (Scheduler.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new Scheduler();
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    // ------------------------------------------------------------------------------------------------ //

    private final ExecutorService pool;
    private final int numThreads;
    final BlockingDeque<TaskManager> roundRobinQueue;
    final Map<Object, TaskManager> taskManagers;
    final AtomicInteger numberOfRunningScheduler;
    final AtomicBoolean stopped;

    /**
     * Initializes a new {@link Scheduler}.
     */
    private Scheduler() {
        super();
        // Determine number of threads to utilize
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        int defaultNumThreads = 10;
        int numThreads = null == configService ? defaultNumThreads : configService.getIntProperty("com.openexchange.tools.images.scheduler.numThreads", defaultNumThreads);
        this.numThreads = numThreads;

        // Initialize fixed thread pool
        SchedulerThreadPoolExecutor newPool = new SchedulerThreadPoolExecutor(numThreads);
        newPool.prestartAllCoreThreads();
        pool = newPool;
        taskManagers = new HashMap<Object, TaskManager>(256);
        roundRobinQueue = new LinkedBlockingDeque<TaskManager>();

        // Start selector threads
        stopped = new AtomicBoolean(false);
        numberOfRunningScheduler = new AtomicInteger();
        for (int i = numThreads; i-- > 0;) {
            newPool.execute(new Selector());
            numberOfRunningScheduler.incrementAndGet();
        }
    }

    /**
     * Creates a new <code>Selector</code> instance.
     *
     * @return The new <code>Selector</code> instance
     */
    public Selector newSelector() {
        return new Selector();
    }

    /**
     * Shuts-down this scheduler.
     */
    private void stop() {
        stopped.set(true);

        for (int i = numThreads; i-- > 0;) {
            roundRobinQueue.offerFirst(POISON);
        }

        try {
            pool.shutdownNow();
        } catch (final Exception x) {
            // Ignore
        }
    }

    /**
     * Checks if a new <code>Selector</code> is supposed to be created
     *
     * @return <code>true</code> if a new <code>Selector</code> is supposed to be created; otherwise <code>false</code>
     * @throws RejectedExecutionException If the possibly needed <code>Selector</code> cannot be accepted for execution
     */
    protected void scheduleNewSelectorIfNeeded() {
        // Check number of currently running Selector instances
        int num;
        do {
            num = numberOfRunningScheduler.get();
            if (num >= numThreads) {
                return;
            }
        } while (!numberOfRunningScheduler.compareAndSet(num, num + 1));

        // Start a new Selector
        pool.execute(new Selector());
    }

    /**
     * Schedules the specified task for being executed associated with given key (if any).
     *
     * @param optKey The optional key; if <code>null</code> calling {@link Thread} instance is referenced as key
     * @param task The task to execute
     * @return <code>true</code> if successfully scheduled for execution; otherwise <code>false</code> to signal that task cannot be accepted
     */
    public boolean execute(Object optKey, Runnable task) {
        if (stopped.get()) {
            return false;
        }

        // Check number of currently running Selector instances
        try {
            scheduleNewSelectorIfNeeded();
        } catch (RejectedExecutionException x) {
            return false;
        }

        // Determine the key to use
        Object key = null == optKey ? Thread.currentThread() : optKey;

        // Add to task to either an existing or to a newly created task manager
        TaskManager newManager = null;
        synchronized (taskManagers) {
            TaskManager existingManager = taskManagers.get(key);
            if (existingManager == null) {
                // None present, yet. Create a new executer.
                newManager = new TaskManagerImpl(task, key);
                taskManagers.put(key, newManager);
            } else {
                // Use existing one
                existingManager.add(task);
            }
        }

        // Add to round-robin queue in case task manager was newly created
        if (null != newManager) {
            roundRobinQueue.offerLast(newManager);
        }

        // Otherwise passed to an already existing executer
        return true;
    }

    // ----------------------------------------------------------------------------------------------- //

    private final class SelectorAdder implements Runnable {

        SelectorAdder() {
            super();
        }

        @Override
        public void run() {
            try {
                // Check number of currently running Selector instances
                scheduleNewSelectorIfNeeded();
            } catch (Exception e) {
                LOGGER.warn("Failed to accept new Selector for execution", e);
            }
        }
    }

    /**
     * The Selector waiting for incoming image processing tasks.
     */
    public final class Selector implements Runnable {

        Selector() {
            super();
        }

        @Override
        public void run() {
            // Remember associated worker thread
            Thread currentThread = Thread.currentThread();

            if (stopped.get()) {
                // Stopped...
                LOGGER.info("Image transformation selector '{}' terminated", currentThread.getName());
                return;
            }

            boolean decrementCount = true;
            try {
                // Perform image processing until aborted
                boolean proceed = true;
                while (proceed) {
                    try {
                        // Await next slot
                        TaskManager manager = roundRobinQueue.takeFirst();

                        // Check slot for POISON
                        if (POISON == manager) {
                            // Poisoned...
                            LOGGER.info("Image transformation selector '{}' terminated", currentThread.getName());
                            return;
                        }

                        // Acquire next task from slot
                        Runnable task;
                        synchronized (taskManagers) {
                            task = manager.remove();
                            if (null == task) {
                                taskManagers.remove(manager.getExecuterKey());
                            }
                        }

                        // Check task
                        if (null != task) {
                            // Re-add slot to round-robin queue for next processing
                            roundRobinQueue.offerLast(manager);

                            // Perform (image transformation) task
                            task.run();

                            if (Thread.interrupted()) {
                                // Cleared interrupted status after run() method

                                // Check status
                                if (stopped.get()) {
                                    // Stopped...
                                    LOGGER.info("Image transformation selector '{}' terminated", currentThread.getName());
                                    return;
                                }

                                // Otherwise orderly terminate this Selector & re-schedule another Selector
                                proceed = false;
                                LOGGER.info("Image transformation selector '{}' terminated. Going to schedule a new selector for further processing.", currentThread.getName());

                                // Ensure counter is decremented prior to one-shot task becoming active
                                numberOfRunningScheduler.decrementAndGet();
                                decrementCount = false;

                                TimerService optService = Services.optService(TimerService.class);
                                if (null != optService) {
                                    optService.schedule(new SelectorAdder(), 250, TimeUnit.MILLISECONDS);
                                }

                                // Leave...
                                return;
                            }
                        }

                        // Check status
                        if (stopped.get()) {
                            // Stopped...
                            LOGGER.info("Image transformation selector '{}' terminated", currentThread.getName());
                            return;
                        }
                    } catch (InterruptedException e) {
                        // Handle in outer try-catch clause
                        throw e;
                    } catch (RuntimeException e) {
                        LOGGER.info("Image transformation failed.", e);
                    } catch (StackOverflowError e) {
                        LOGGER.info("Image transformation failed.", e);
                    } catch (Throwable t) {
                        // The Exception or Error that caused execution to terminate abruptly.
                        ExceptionUtils.handleThrowable(t);

                        LOGGER.info("Image transformation failed", t);
                    }
                }
            } catch (InterruptedException e) {
                // Keep interrupted status
                currentThread.interrupt();
                LOGGER.info("Image transformation selector '{}' interrupted", currentThread.getName(), e);
            } finally {
                // Decrement count
                if (decrementCount) {
                    numberOfRunningScheduler.decrementAndGet();
                }
            }

            LOGGER.info("Image transformation selector '{}' terminated", currentThread.getName());
        }
    } // End of class Selector

    private final class TaskManagerImpl implements TaskManager {

        private final LinkedList<Runnable> tasks = new LinkedList<Runnable>();
        private final Object taskKey;

        TaskManagerImpl(final Runnable task, final Object key) {
            super();
            taskKey = key;
            tasks.offer(task);
        }

        @Override
        public Object getExecuterKey() {
            return taskKey;
        }

        @Override
        public Runnable remove() { // Gets only called when holding lock
            return tasks.poll();
        }

        @Override
        public void add(Runnable task) { // Gets only called when holding lock
            tasks.offer(task);
        }
    } // End of class TaskManagerImpl

}
