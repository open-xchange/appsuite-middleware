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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajp13.watcher;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.najp.AJPv13ServerImpl;
import com.openexchange.log.Log;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link AJPv13TaskWatcher} - Keeps track of submitted AJP tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13TaskWatcher {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AJPv13TaskWatcher.class));

    private ScheduledTimerTask scheduledTimerTask;

    private final ConcurrentMap<Long, com.openexchange.ajp13.watcher.Task> tasks;

    /**
     * Initializes a new {@link AJPv13TaskWatcher}.
     *
     * @param threadPoolService The thread pool service
     */
    public AJPv13TaskWatcher(final ThreadPoolService threadPoolService) {
        super();
        tasks = new ConcurrentHashMap<Long, com.openexchange.ajp13.watcher.Task>();
        /*
         * Start keep-alive task
         */
        final TimerService timer = AJPv13ServiceRegistry.getInstance().getService(TimerService.class);
        if (null != timer) {
            scheduledTimerTask =
                timer.scheduleWithFixedDelay(
                    new TimerTaskRunnable(tasks.values(), threadPoolService, LOG),
                    1000,
                    AJPv13Config.getAJPWatcherFrequency(),
                    TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Adds given task to this watcher.
     * <p>
     * Invoked within {@link Task#beforeExecute(Thread)} of an AJP task.
     *
     * @param task The AJP task to add
     */
    public void addTask(final com.openexchange.ajp13.watcher.Task task) {
        if (null != tasks.putIfAbsent(task.getNum(), task)) {
            LOG.error(MessageFormat.format("AJP task with number {0} task could not be added to watcher!", task.getNum()));
        }
    }

    /**
     * Removes given task from this watcher.
     * <p>
     * Invoked within {@link Task#afterExecute(Throwable)} of an AJP task.
     *
     * @param task The AJP task to remove
     */
    public void removeTask(final com.openexchange.ajp13.watcher.Task task) {
        tasks.remove(task.getNum());
    }

    /**
     * Stops this watcher (and cancels all tracked AJP tasks).
     */
    public void stop() {
        for (final Iterator<com.openexchange.ajp13.watcher.Task> i = tasks.values().iterator(); i.hasNext();) {
            i.next().cancel();
            i.remove();
        }
        tasks.clear();
        if (null != scheduledTimerTask) {
            scheduledTimerTask.cancel(false);
            scheduledTimerTask = null;
            final TimerService timer = AJPv13ServiceRegistry.getInstance().getService(TimerService.class);
            if (null != timer) {
                timer.purge();
            }
        }
    }

    private static class TimerTaskRunnable implements Runnable {

        private final Collection<com.openexchange.ajp13.watcher.Task> tasks;

        private final org.apache.commons.logging.Log log;

        private final ThreadPoolService threadPoolService;

        /**
         * Initializes a new {@link TimerTaskRunnable}
         *
         * @param tasks The map to iterate
         * @param threadPoolService The thread pool service
         * @param log The logger instance to use
         */
        public TimerTaskRunnable(final Collection<com.openexchange.ajp13.watcher.Task> tasks, final ThreadPoolService threadPoolService, final org.apache.commons.logging.Log log) {
            super();
            this.tasks = tasks;
            this.log = log;
            this.threadPoolService = threadPoolService;
        }

        @Override
        public void run() {
            try {
                final boolean logExceededTasks = AJPv13Config.getAJPWatcherEnabled();
                if (logExceededTasks && AJPv13Config.getAJPWatcherPermission()) {
                    final AtomicInteger countWaiting = new AtomicInteger();
                    final AtomicInteger countProcessing = new AtomicInteger();
                    final AtomicInteger countExceeded = new AtomicInteger();
                    /*
                     * Create a list of tasks
                     */
                    final Collection<com.openexchange.threadpool.Task<Object>> tasks =
                        new ArrayList<com.openexchange.threadpool.Task<Object>>();
                    for (final com.openexchange.ajp13.watcher.Task ajPv13Task : this.tasks) {
                        tasks.add(new TaskRunCallable(ajPv13Task, countWaiting, countProcessing, countExceeded, true, log));
                    }
                    /*
                     * Invoke all and wait for being executed
                     */
                    threadPoolService.invokeAll(tasks);
                    /*
                     * All threads are listening longer than specified max listener running time
                     */
                    if (countProcessing.get() > 0 && countExceeded.get() == countProcessing.get()) {
                        final String delimStr = "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
                        log.error(new StringBuilder(128 + delimStr.length()).append(delimStr).append(
                            "AJP-Watcher's run done: SYSTEM DEADLOCK DETECTED!").append(" Going to stop and re-initialize system").append(
                            delimStr).toString());
                        /*
                         * Restart AJP Server
                         */
                        try {
                            AJPv13ServerImpl.restartAJPServer();
                        } catch (final AJPv13Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                    /*-
                     *
                    else {
                        if (log.isTraceEnabled()) {
                            final String delimStr = "\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
                            log.trace(new StringBuilder(128 + delimStr.length()).append(delimStr).append("AJP-Watcher's run done: ").append(
                                "    Waiting=").append(countWaiting).append("    Running=").append(countProcessing).append("    Exceeded=").append(
                                countExceeded).append("    Total=").append(listeners.size()).append(delimStr).toString());
                        }
                    }
                     */
                } else {
                    /*
                     * Create a list of tasks
                     */
                    final Collection<com.openexchange.threadpool.Task<Object>> tasks =
                        new ArrayList<com.openexchange.threadpool.Task<Object>>();
                    for (final com.openexchange.ajp13.watcher.Task ajPv13Task : this.tasks) {
                        tasks.add(new TaskRunCallable(ajPv13Task, logExceededTasks, log));
                    }
                    /*
                     * Invoke all and wait for being executed
                     */
                    threadPoolService.invokeAll(tasks);
                }
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    } // End of class TimerTaskRunnable

    private static final class TaskRunCallable implements com.openexchange.threadpool.Task<Object> {

        private final com.openexchange.ajp13.watcher.Task task;

        private final boolean logExceededTasks;

        private final boolean hasPermission;

        private final AtomicInteger waiting;

        private final AtomicInteger processing;

        private final AtomicInteger exceeded;

        private final org.apache.commons.logging.Log log;

        private final boolean info;

        /**
         * Initializes a new {@link TaskRunCallable} to only perform keep-alive on given AJP task.
         *
         * @param task The AJP task
         * @param logExceededTasks Whether to log exceeded tasks
         * @param log The logger
         */
        public TaskRunCallable(final com.openexchange.ajp13.watcher.Task task, final boolean logExceededTasks, final org.apache.commons.logging.Log log) {
            super();
            this.logExceededTasks = logExceededTasks;
            this.task = task;
            hasPermission = false;
            exceeded = null;
            processing = null;
            waiting = null;
            this.log = log;
            info = log.isInfoEnabled();
        }

        /**
         * Initializes a new {@link TaskRunCallable} fully tracking given AJP task.
         *
         * @param task The AJP task
         * @param waiting The waiting counter
         * @param processing The processing counter
         * @param exceeded The exceeded counter
         * @param logExceededTasks Whether to log exceeded tasks
         * @param log The logger
         */
        public TaskRunCallable(final com.openexchange.ajp13.watcher.Task task, final AtomicInteger waiting, final AtomicInteger processing, final AtomicInteger exceeded, final boolean logExceededTasks, final org.apache.commons.logging.Log log) {
            super();
            this.logExceededTasks = logExceededTasks;
            this.task = task;
            hasPermission = true;
            this.exceeded = exceeded;
            this.processing = processing;
            this.waiting = waiting;
            this.log = log;
            info = log.isInfoEnabled();
        }

        @Override
        public Object call() {
            if (hasPermission) {
                if (task.isWaitingOnAJPSocket()) {
                    waiting.incrementAndGet();
                }
                if (task.isProcessing()) {
                    /*
                     * Task is currently processing
                     */
                    processing.incrementAndGet();
                    final long currentProcTime = (System.currentTimeMillis() - task.getProcessingStartTime());
                    if (currentProcTime > AJPv13Config.getAJPWatcherMaxRunningTime()) {
                        /*
                         * Task exceeded max. running time
                         */
                        exceeded.incrementAndGet();
                        handleExceededTask(currentProcTime);
                    }
                }
            } else {
                if (task.isProcessing()) {
                    /*
                     * Task is currently processing
                     */
                    final long currentProcTime = (System.currentTimeMillis() - task.getProcessingStartTime());
                    if (currentProcTime > AJPv13Config.getAJPWatcherMaxRunningTime()) {
                        /*
                         * Task exceeded max. running time
                         */
                        handleExceededTask(currentProcTime);
                    }
                }
            }
            return null;
        }

        private static final long MAX_PROC_TIME = 300000;

        private void handleExceededTask(final long currentProcTime) {
            if (task.isLongRunning()) {
                return;
            }
            /*
             * Log exceeded task if it is not marked as a long-running task
             */
            if (logExceededTasks && info) {
                final Throwable t = new Throwable();
                t.setStackTrace(task.getStackTrace());
                log.info(new StringBuilder(128).append("AJP Listener \"").append(task.getThreadName()).append(
                    "\" exceeds max. running time of ").append(AJPv13Config.getAJPWatcherMaxRunningTime()).append(
                    "msec -> Processing time: ").append(currentProcTime).append("msec").toString(), t);
            }
            if (currentProcTime > MAX_PROC_TIME) {
                task.cancel();
            }
        }

        @Override
        public void afterExecute(final Throwable t) {
            // NOP
        }

        @Override
        public void beforeExecute(final Thread t) {
            // NOP
        }

        @Override
        public void setThreadName(final ThreadRenamer threadRenamer) {
            // NOP
        }

    } // End of TaskRunCallable class

}
