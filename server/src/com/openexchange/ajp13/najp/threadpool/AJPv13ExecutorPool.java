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

package com.openexchange.ajp13.najp.threadpool;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.NotCompliantMBeanException;
import com.openexchange.ajp13.monitoring.AJPv13Monitors;
import com.openexchange.ajp13.najp.AJPv13ListenerMonitor;
import com.openexchange.ajp13.najp.AJPv13Task;
import com.openexchange.ajp13.najp.AJPv13TaskWatcher;

/**
 * {@link AJPv13ExecutorPool} - The AJP thread pool for handling accepted client sockets.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AJPv13ExecutorPool {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13ExecutorPool.class);

    private final AtomicBoolean started;

    private final AJPv13ListenerMonitor listenerMonitor;

    private AJPv13TaskWatcher watcher;

    private AJPv13ThreadPoolExecutor pool;

    /**
     * Initializes a new {@link AJPv13ExecutorPool}.
     */
    public AJPv13ExecutorPool() {
        super();
        started = new AtomicBoolean();
        AJPv13ListenerMonitor tmp = null;
        try {
            tmp = new AJPv13ListenerMonitor(this);
        } catch (final NotCompliantMBeanException e) {
            LOG.error(e.getMessage(), e);
        }
        listenerMonitor = tmp;
    }

    /**
     * Starts this pool that creates new threads as needed, but will reuse previously constructed threads when they are available.
     */
    public void startUp() {
        if (!started.compareAndSet(false, true)) {
            LOG.info("AJP executor pool already started; start-up aborted.");
            return;
        }
        watcher = new AJPv13TaskWatcher();
        pool = new AJPv13ThreadPoolExecutor(10L, TimeUnit.SECONDS, watcher);
        pool.prestartAllCoreThreads();
        AJPv13Monitors.setListenerMonitor(listenerMonitor);
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and returns a list of the tasks that were
     * awaiting execution.
     */
    public List<Runnable> shutDownNow() {
        if (!started.compareAndSet(true, false)) {
            LOG.info("AJP executor not started; abrupt shut-down aborted.");
            return new ArrayList<Runnable>(0);
        }
        try {
            watcher.stop();
            return pool.shutdownNow();
        } finally {
            watcher = null;
            pool = null;
            AJPv13Monitors.releaseListenerMonitor();
        }
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no
     * additional effect if already shut down.
     */
    public void shutDown() {
        if (!started.compareAndSet(true, false)) {
            LOG.info("AJP executor not started; graceful shut-down aborted.");
            return;
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
            watcher = null;
            pool = null;
            AJPv13Monitors.releaseListenerMonitor();
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
        final AJPv13TaskWatcher.WatcherFutureTask task = watcher.new WatcherFutureTask(new AJPv13Task(client, listenerMonitor));
        pool.execute(task);
        watcher.addListener(task);
    }

    /**
     * Returns the current number of threads in the pool.
     * 
     * @return The current number of threads in the pool.
     */
    public int getPoolSize() {
        return pool == null ? 0 : pool.getPoolSize();
    }

    /**
     * Returns the approximate number of threads that are actively executing tasks.
     * 
     * @return The approximate number of threads that are actively executing tasks.
     */
    public int getActiveCount() {
        return pool == null ? 0 : pool.getActiveCount();
    }
}
