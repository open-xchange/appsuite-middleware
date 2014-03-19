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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
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
    final Map<Object, TaskExecuter> runningThreads;

    /**
     * Initializes a new {@link Scheduler}.
     */
    private Scheduler() {
        super();
        final ConfigurationService configService = Services.getService(ConfigurationService.class);
        final int defaultNumThreads = 10;
        final int numThreads = null == configService ? defaultNumThreads : configService.getIntProperty("com.openexchange.tools.images.scheduler.numThreads", defaultNumThreads);
        final ThreadPoolExecutor newPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads, new SchedulerThreadFactory());
        newPool.prestartAllCoreThreads();
        pool = newPool;
        runningThreads = new HashMap<Object, TaskExecuter>(256);
    }

    /**
     * Shuts-down this scheduler.
     */
    private void stop() {
        pool.shutdown();
    }

    /**
     * This does not block an unrelated thread used to send a synchronous event.
     *
     * @param optKey The optional key; if <code>null</code> calling {@link Thread} instance is referenced as key
     * @param task The task to execute
     */
    public void execute(final Object optKey, final Runnable task) {
        final Object key = null == optKey ? Thread.currentThread() : optKey;
        TaskExecuter executer = null;
        synchronized (runningThreads) {
            final TaskExecuter runningExecutor = runningThreads.get(key);
            if (runningExecutor != null) {
                runningExecutor.add(task);
            } else {
                executer = new TaskExecuter(task, key);
                runningThreads.put(key, executer);
            }
        }
        if (executer != null) {
            executeTask(executer);
        }
    }

    /**
     * Execute the task in a free thread or create a new one.
     *
     * @param executer The task to execute
     */
    private void executeTask(final TaskExecuter executer) {
        try {
            pool.execute(executer);
        } catch (final Throwable t) {
            LOGGER.warn("Couldn't execute image transformation task.", t);
        }
    }

    // ----------------------------------------------------------------------------------------------- //

    private final class TaskExecuter implements Runnable {

        private final List<Runnable> tasks = new LinkedList<Runnable>();
        private final Object m_key;

        public TaskExecuter(final Runnable task, final Object key) {
            m_key = key;
            tasks.add(task);
        }

        @Override
        public void run() {
            boolean running;
            do {
                Runnable task = null;
                synchronized (tasks) {
                    task = tasks.remove(0);
                }

                // Perform image transformation
                task.run();

                // Check for more...
                synchronized (runningThreads) {
                    running = !tasks.isEmpty();
                    if (!running) {
                        runningThreads.remove(m_key);
                    }
                }
            } while (running);
        }

        public void add(final Runnable task) {
            synchronized (tasks) {
                tasks.add(task);
            }
        }
    }

}
