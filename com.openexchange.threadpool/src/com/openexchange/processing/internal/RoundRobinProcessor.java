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

package com.openexchange.processing.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.processing.Processor;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.TimerService;

/**
 * {@link RoundRobinProcessor} - A processor that manages its tasks using round-robin behavior.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.1
 */
public class RoundRobinProcessor implements Processor {

    /** The logger */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RoundRobinProcessor.class);

    private final ExecutorService pool;
    private final int numThreads;
    final BlockingDeque<TaskManager> roundRobinQueue;
    final Map<Object, TaskManager> taskManagers;
    final AtomicInteger numberOfActiveSelectors;
    final AtomicBoolean stopped;

    /**
     * Initializes a new {@link RoundRobinProcessor}.
     */
    public RoundRobinProcessor(String name, int numThreads) {
        super();
        if (numThreads <= 0) {
            throw new IllegalArgumentException("numThreads must not be equal to/less than zero");
        }
        this.numThreads = numThreads;

        // Initialize fixed thread pool
        ProcessorThreadPoolExecutor newPool = new ProcessorThreadPoolExecutor(name, numThreads);
        newPool.prestartAllCoreThreads();
        pool = newPool;
        taskManagers = new HashMap<Object, TaskManager>(256);
        roundRobinQueue = new LinkedBlockingDeque<TaskManager>();

        // Start selector threads
        stopped = new AtomicBoolean(false);
        numberOfActiveSelectors = new AtomicInteger();
        for (int i = numThreads; i-- > 0;) {
            newPool.execute(new Selector());
            numberOfActiveSelectors.incrementAndGet();
        }
    }

    /**
     * Checks whether to consider task managers map as empty.
     * <p>
     * Must only be accessed synchronized.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    protected boolean considerTaskManagersEmpty() {
        if (taskManagers.isEmpty()) {
            return true;
        }

        for (TaskManager taskManager : taskManagers.values()) {
            if (false == taskManager.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the next task from specified task manager
     *
     * @param manager The task manager
     * @return The next task or <code>null</code>
     */
    protected Runnable getNextTaskFrom(TaskManager manager) {
        return manager.remove();
    }

    /**
     * Checks whether specified task is allowed to be added to this processor.
     *
     * @param task The task to check
     * @return <code>true</code> if allowed/granted; otherwise <code>false</code>
     */
    protected boolean allowNewTask(Runnable task) {
        return true;
    }

    /**
     * Handles if a task could not be offered to processor
     *
     * @param task The task that could not be offered
     */
    protected void handleFailedTaskOffer(Runnable task) {
        // Nothing
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
            num = numberOfActiveSelectors.get();
            if (num >= numThreads) {
                return;
            }
        } while (!numberOfActiveSelectors.compareAndSet(num, num + 1));

        // Start a new Selector
        pool.execute(new Selector());
    }

    private void haltThreadsAndShutDownPool() {
        for (int i = numThreads; i-- > 0;) {
            roundRobinQueue.offerFirst(TaskManager.POISON);
        }

        try {
            pool.shutdownNow();
        } catch (final Exception x) {
            // Ignore
        }
    }

    @Override
    public void stop() {
        if (!stopped.compareAndSet(false, true)) {
            // Already stopped
            return;
        }

        haltThreadsAndShutDownPool();
    }

    @Override
    public void stopWhenEmpty() throws InterruptedException {
        if (!stopped.compareAndSet(false, true)) {
            // Already stopped
            return;
        }

        synchronized (taskManagers) {
            while (false == considerTaskManagersEmpty()) {
                taskManagers.wait();
            }
        }

        haltThreadsAndShutDownPool();
    }

    @Override
    public boolean execute(Object optKey, Runnable task) {
        if (stopped.get()) {
            return false;
        }

        // Acquire grant
        if (!allowNewTask(task)) {
            return false;
        }

        // Check number of currently running Selector instances
        try {
            scheduleNewSelectorIfNeeded();
        } catch (RejectedExecutionException x) {
            handleFailedTaskOffer(task);
            return false;
        }

        // Determine the key to use
        Object key = null == optKey ? Thread.currentThread() : optKey;

        // Schedule task
        TaskManager newManager = null;
        synchronized (taskManagers) {
            // Stopped meanwhile...?
            if (stopped.get()) {
                handleFailedTaskOffer(task);
                return false;
            }

            // Add task to either new or existing TaskManager instance
            TaskManager existingManager = taskManagers.get(key);
            try {
                if (existingManager == null) {
                    // None present, yet. Create a new executer.
                    newManager = new DefaultTaskManager(task, key);
                    taskManagers.put(key, newManager);
                } else {
                    // Use existing one
                    existingManager.add(task);
                }
            } catch (RuntimeException e) {
                // Adding to manager failed
                handleFailedTaskOffer(task);
                throw e;
            }
        }

        // Add to round-robin queue in case task manager was newly created
        if (null != newManager) {
            try {
                roundRobinQueue.offerLast(newManager);
            } catch (RuntimeException e) {
                // Adding to manager failed
                handleFailedTaskOffer(task);
                throw e;
            }
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
     * The Selector waiting for incoming processing tasks.
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
                LOGGER.info("Processor selector '{}' terminated", currentThread.getName());
                return;
            }

            boolean decrementCount = true;
            try {
                // Perform processing until aborted
                boolean proceed = true;
                while (proceed) {
                    try {
                        // Await next slot
                        TaskManager manager = roundRobinQueue.takeFirst();

                        // Check slot for POISON
                        if (TaskManager.POISON == manager) {
                            // Poisoned...
                            LOGGER.info("Processor selector '{}' terminated", currentThread.getName());
                            return;
                        }

                        // Acquire next task from slot
                        Runnable task;
                        synchronized (taskManagers) {
                            task = getNextTaskFrom(manager);
                            if (null == task) {
                                taskManagers.remove(manager.getExecuterKey());
                            }
                            taskManagers.notify();
                        }

                        // Check task
                        if (null != task) {
                            // Re-add slot to round-robin queue for next processing
                            roundRobinQueue.offerLast(manager);

                            // Perform task
                            task.run();

                            if (Thread.interrupted()) {
                                // Cleared interrupted status after run() method

                                // Check status
                                if (stopped.get()) {
                                    // Stopped...
                                    LOGGER.info("Processor selector '{}' terminated", currentThread.getName());
                                    return;
                                }

                                // Otherwise orderly terminate this Selector & re-schedule another Selector
                                proceed = false;
                                LOGGER.info("Processor selector '{}' terminated. Going to schedule a new selector for further processing.", currentThread.getName());

                                // Ensure counter is decremented prior to one-shot task becoming active
                                numberOfActiveSelectors.decrementAndGet();
                                decrementCount = false;

                                TimerService optService = ThreadPools.getTimerService();
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
                            LOGGER.info("Processor selector '{}' terminated", currentThread.getName());
                            return;
                        }
                    } catch (InterruptedException e) {
                        // Handle in outer try-catch clause
                        throw e;
                    } catch (RuntimeException e) {
                        LOGGER.info("Processing failed.", e);
                    } catch (StackOverflowError e) {
                        LOGGER.info("Processing failed.", e);
                    } catch (Throwable t) {
                        // The Exception or Error that caused execution to terminate abruptly.
                        ExceptionUtils.handleThrowable(t);

                        LOGGER.info("Processing failed", t);
                    }
                }
            } catch (InterruptedException e) {
                // Keep interrupted status
                currentThread.interrupt();
                LOGGER.info("Processor selector '{}' interrupted", currentThread.getName(), e);
            } finally {
                // Decrement count
                if (decrementCount) {
                    numberOfActiveSelectors.decrementAndGet();
                }
            }

            LOGGER.info("Processor selector '{}' terminated", currentThread.getName());
        }
    } // End of class Selector

}
