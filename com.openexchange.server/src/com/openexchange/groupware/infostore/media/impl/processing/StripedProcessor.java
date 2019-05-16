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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.infostore.media.impl.processing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link StripedProcessor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class StripedProcessor {

    /** The logger constant */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StripedProcessor.class);

    private final ThreadPoolExecutor pool;
    private final int nThreads;
    final Map<String, Stripe> task2Stripe;
    final BlockingDeque<Stripe> roundRobinQueue;
    final Map<UserAndContext, Stripe> stripes;
    final AtomicInteger numberOfActiveSelectors;
    final AtomicBoolean stopped;

    /**
     * Initializes a new {@link StripedProcessor}.
     */
    public StripedProcessor(String name, int nThreads) {
        super();
        if (nThreads <= 0) {
            throw new IllegalArgumentException("nThreads must not be equal to/less than zero");
        }
        this.nThreads = nThreads;

        ThreadPoolExecutor newPool = ThreadPools.newThreadPoolExecutor(name, 0, nThreads, 0, true);
        newPool.prestartAllCoreThreads();
        pool = newPool;

        stripes = new HashMap<UserAndContext, Stripe>(256);
        roundRobinQueue = new LinkedBlockingDeque<Stripe>();
        stopped = new AtomicBoolean(false);
        numberOfActiveSelectors = new AtomicInteger(0);

        task2Stripe = new HashMap<String, Stripe>(128, 0.9F);
    }

    /**
     * Shuts-down this striped processor.
     */
    public void stop() {
        if (!stopped.compareAndSet(false, true)) {
            // Already stopped
            return;
        }

        haltSelectorssAndShutDownPool();
    }

    private void haltSelectorssAndShutDownPool() {
        for (int i = nThreads; i-- > 0;) {
            roundRobinQueue.offerFirst(Stripe.POISON);
        }

        try {
            pool.shutdownNow();
        } catch (@SuppressWarnings("unused") final Exception x) {
            // Ignore
        }
    }

    /**
     * Spawns a new <code>Selector</code>
     *
     * @return <code>true</code> if a new <code>Selector</code> has been accepted for execution; otherwise <code>false</code>
     */
    protected boolean scheduleNewSelector() {
        if (stopped.get()) {
            return false;
        }

        // Start a new Selector
        try {
            pool.execute(new Selector());
            numberOfActiveSelectors.incrementAndGet();
            return true;
        } catch (@SuppressWarnings("unused") java.util.concurrent.RejectedExecutionException e) {
            // Apparently enough running...
            return false;
        }
    }

    /**
     * Schedules the specified task for being executed associated with given key.
     * <p>
     * Any existing task with the same key will be replaced.
     *
     * @param taskKey The key for the task
     * @param task The task to execute
     * @param session The session providing user/context information
     * @return <code>true</code> if successfully scheduled for execution; otherwise <code>false</code> to signal that task cannot be accepted
     */
    public boolean execute(String taskKey, Runnable task, Session session) {
        return execute(taskKey, task, session.getUserId(), session.getContextId());
    }

    /**
     * Schedules the specified task for being executed associated with given key.
     *
     * @param taskKey The key for the task
     * @param task The task to execute
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully scheduled for execution; otherwise <code>false</code> to signal that task cannot be accepted
     */
    public boolean execute(String taskKey, Runnable task, int userId, int contextId) {
        // Generate both - the key for user-associated stripe & the extraction task
        UserAndContext stripeKey = UserAndContext.newInstance(userId, contextId);
        ExtractionTask extractionTask = new ExtractionTask(taskKey, task, stripeKey);

        Stripe newStripe = null;
        synchronized (stripes) {
            // Stopped...?
            if (stopped.get()) {
                return false;
            }

            // Check for a Stripe that is already processing such a task
            Stripe processingStripe = task2Stripe.get(taskKey);
            if (null != processingStripe) {
                // Such a task is already processed by a certain stripe
                processingStripe.add(extractionTask);
            } else {
                // Add task to either new or existing Stripe instance
                Stripe existingStripe = stripes.get(stripeKey);
                try {
                    if (existingStripe == null) {
                        // None present, yet. Create a new Stripe.
                        newStripe = new Stripe(extractionTask, stripeKey);
                        stripes.put(stripeKey, newStripe);
                        task2Stripe.put(taskKey, newStripe);
                    } else {
                        // Use existing one
                        existingStripe.add(extractionTask);
                        task2Stripe.put(taskKey, existingStripe);
                    }
                } catch (RuntimeException e) {
                    // Adding to Stripe failed
                    throw e;
                }
            }
        }

        // Add to round-robin queue in case Stripe was newly created
        if (null != newStripe) {
            try {
                roundRobinQueue.offerLast(newStripe);
                scheduleNewSelector();
            } catch (RuntimeException e) {
                // Adding to round-robin queue failed
                throw e;
            }
        }

        // Otherwise passed to an already existing Stripe
        return true;
    }

    /**
     * Checks if a task associated with given task key is currently enqueued in scheduling queue.
     *
     * @param taskKey The task key
     * @return <code>true</code> if enqueued; otherwise <code>false</code> if there is no such task or task is already in execution
     */
    public boolean isEnqueued(String taskKey) {
        synchronized (stripes) {
            return task2Stripe.containsKey(taskKey);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------

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
                return;
            }

            boolean decrementCount = true;
            try {
                // Perform processing until aborted
                boolean proceed = true;
                while (proceed) {
                    try {
                        // Await next slot
                        Stripe stripe = roundRobinQueue.pollFirst(10, TimeUnit.SECONDS);

                        if (null == stripe || Stripe.POISON == stripe) {
                            // Timed out or poisoned...
                            LOGGER.debug("Extraction selector '{}' terminated", currentThread.getName());
                            return;
                        }

                        // Acquire next task from stripe
                        ExtractionTask task;
                        synchronized (stripes) {
                            task = stripe.poll();
                            if (null == task) {
                                stripes.remove(stripe.getStripeKey());
                            } else {
                                task2Stripe.remove(task.getKey());
                            }
                        }

                        // Check task
                        if (null != task) {
                            // Re-add stripe to round-robin queue for next processing
                            roundRobinQueue.offerLast(stripe);

                            UserAndContext stripeKey = task.getStripeKey();
                            LogProperties.putUserProperties(stripeKey.getUserId(), stripeKey.getContextId());
                            try {
                                task.run();
                            } finally {
                                LogProperties.removeUserProperties();
                            }

                            if (Thread.interrupted()) {
                                // Cleared interrupted status after run() method

                                // Check status
                                if (stopped.get()) {
                                    // Stopped...
                                    LOGGER.info("Extraction selector '{}' terminated", currentThread.getName());
                                    return;
                                }

                                // Otherwise orderly terminate this Selector & re-schedule another Selector
                                proceed = false;
                                LOGGER.info("Extraction selector '{}' terminated. Going to schedule a new selector for further processing.", currentThread.getName());

                                // Ensure counter is decremented prior to new Selector becoming active
                                numberOfActiveSelectors.decrementAndGet();
                                decrementCount = false;

                                // Schedule a new Selector as this one is about to die
                                scheduleNewSelector();

                                // Leave...
                                return;
                            }
                        }

                        // Check status
                        if (stopped.get()) {
                            // Stopped...
                            LOGGER.info("Extraction selector '{}' terminated", currentThread.getName());
                            return;
                        }
                    } catch (InterruptedException e) {
                        // Handle in outer try-catch clause
                        throw e;
                    } catch (RuntimeException e) {
                        LOGGER.info("Extraction failed.", e);
                    } catch (StackOverflowError e) {
                        LOGGER.info("Extraction failed.", e);
                    } catch (Throwable t) {
                        // The Exception or Error that caused execution to terminate abruptly.
                        ExceptionUtils.handleThrowable(t);
                        LOGGER.info("Extraction failed", t);
                    }
                }
            } catch (InterruptedException e) {
                // Keep interrupted status
                currentThread.interrupt();
                LOGGER.info("Extraction selector '{}' interrupted", currentThread.getName(), e);
            } finally {
                // Decrement count
                if (decrementCount) {
                    numberOfActiveSelectors.decrementAndGet();
                }
            }

            LOGGER.info("Extraction selector '{}' terminated", currentThread.getName());
        }
    }

}
