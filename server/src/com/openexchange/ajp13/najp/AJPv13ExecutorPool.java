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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajp13.najp;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.monitoring.MonitoringInfo;

/**
 * {@link AJPv13ExecutorPool}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ExecutorPool {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13ExecutorPool.class);

    private static final class AJPv13ThreadPoolExecutor extends ThreadPoolExecutor {

        private static final AtomicInteger numRunning = new AtomicInteger();

        /**
         * Initializes a new {@link AJPv13ThreadPoolExecutor}.
         * 
         * @param keepAliveTime When the number of threads is greater than the core, this is the maximum time that excess idle threads will
         *            wait for new tasks before terminating.
         * @param unit The time unit for the <code>keepAliveTime</code> argument.
         */
        public AJPv13ThreadPoolExecutor(final long keepAliveTime, final TimeUnit unit) {
            super(
                AJPv13Config.getAJPListenerPoolSize(),
                Integer.MAX_VALUE,
                keepAliveTime,
                unit,
                new SynchronousQueue<Runnable>(),
                new NamingThreadFactory());
        }

        @Override
        protected void beforeExecute(final Thread t, final Runnable r) {
            super.beforeExecute(t, r);
            changeNumberOfRunningAJPTasks(true);
        }

        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            changeNumberOfRunningAJPTasks(false);
            super.afterExecute(r, t);
        }

        /**
         * Increments/decrements the number of running AJP tasks.
         * 
         * @param increment whether to increment or to decrement
         */
        private static void changeNumberOfRunningAJPTasks(final boolean increment) {
            MonitoringInfo.setNumberOfRunningAJPListeners(increment ? numRunning.incrementAndGet() : numRunning.decrementAndGet());
        }

    }

    private final AtomicBoolean started;

    private final AJPv13TaskWatcher watcher;

    private ExecutorService pool;

    /**
     * Initializes a new {@link AJPv13ExecutorPool}.
     */
    public AJPv13ExecutorPool() {
        super();
        started = new AtomicBoolean();
        watcher = new AJPv13TaskWatcher();
    }

    /**
     * Starts this pool that creates new threads as needed, but will reuse previously constructed threads when they are available.
     */
    public void startUp() {
        if (!started.compareAndSet(false, true)) {
            LOG.info("AJP executor pool already started; start-up aborted.");
        }
        pool = new AJPv13ThreadPoolExecutor(10L, TimeUnit.SECONDS);
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and returns a list of the tasks that were
     * awaiting execution.
     */
    public List<Runnable> shutDownNow() {
        if (!started.compareAndSet(true, false)) {
            LOG.info("AJP executor not started; abrupt shut-down aborted.");
        }
        watcher.stop();
        return pool.shutdownNow();
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no
     * additional effect if already shut down.
     */
    public void shutDown() {
        if (!started.compareAndSet(true, false)) {
            LOG.info("AJP executor not started; graceful shut-down aborted.");
        }
        try {
            pool.shutdown();
            pool.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            // Restore interrupted flag for borrowed thread if not already set
            if (!Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
            }
        } finally {
            watcher.stop();
        }
    }

    /**
     * Checks if this executor has been shut down or has not been started.
     * 
     * @return <code>true</code> if this executor has been shut down or has not been started; otherwise <code>false</code>
     */
    public boolean isShutdown() {
        if (!started.get()) {
            return true;
        }
        return pool.isShutdown();
    }

    /**
     * Handles given client socket.
     * 
     * @param client The client socket to handle
     */
    public void handleSocket(final Socket client) {
        final AJPv13TaskWatcher.WatcherFutureTask task = watcher.new WatcherFutureTask(new AJPv13Task(client));
        pool.execute(task);
        watcher.addListener(task);
    }

    /*-
     * Thread factory class
     */

    private static final class NamingThreadFactory implements java.util.concurrent.ThreadFactory {

        private static final int NAME_LENGTH = 17;

        // private final ThreadGroup group;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        public NamingThreadFactory() {
            super();
            // final java.lang.SecurityManager s = System.getSecurityManager();
            // group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
            namePrefix = "AJPListener-";
        }

        public Thread newThread(final Runnable r) {
            // final Thread t = new Thread(group, r, getThreadName(
            // threadNumber.getAndIncrement(),
            // new StringBuilder(NAME_LENGTH).append(namePrefix)), 0);
            // if (t.isDaemon()) {
            // t.setDaemon(false);
            // }
            // if (t.getPriority() != Thread.NORM_PRIORITY) {
            // t.setPriority(Thread.NORM_PRIORITY);
            // }

            final Thread t = new Thread(r, getThreadName(threadNumber.getAndIncrement(), new StringBuilder(NAME_LENGTH).append(namePrefix)));
            t.setUncaughtExceptionHandler(new LoggingUncaughtExceptionhandler());
            return t;
        }

        private static String getThreadName(final int threadNumber, final StringBuilder sb) {
            for (int i = threadNumber; i < 10000; i *= 10) {
                sb.append('0');
            }
            return sb.append(threadNumber).toString();
        }

    } // End of thread factory implementation

    /*-
     * Uncaught exception handler class
     */

    private static final class LoggingUncaughtExceptionhandler implements UncaughtExceptionHandler {

        private static final org.apache.commons.logging.Log UEHLOG = org.apache.commons.logging.LogFactory.getLog(LoggingUncaughtExceptionhandler.class);

        public LoggingUncaughtExceptionhandler() {
            super();
        }

        public void uncaughtException(final Thread t, final Throwable e) {
            UEHLOG.fatal("Thread terminated with exception: " + t.getName(), e);
        }

    } // End of uncaught exception handler class
}
