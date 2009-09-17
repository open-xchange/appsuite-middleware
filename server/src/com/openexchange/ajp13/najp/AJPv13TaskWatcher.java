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

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link AJPv13TaskWatcher} - Keeps track of submitted AJP tasks.
 * <p>
 * AJP tasks are actively put right after their submission, but automatically removed through {@link AJPv13Task#afterExecute(Throwable)}
 * method.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13TaskWatcher {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13TaskWatcher.class);

    private ScheduledTimerTask task;

    private final ConcurrentMap<Long, AJPv13Task> tasks;

    /**
     * Initializes a new {@link AJPv13TaskWatcher}.
     * 
     * @param threadPoolService The thread pool service
     */
    public AJPv13TaskWatcher(final ThreadPoolService threadPoolService) {
        super();
        tasks = new ConcurrentHashMap<Long, AJPv13Task>();
        if (AJPv13Config.getAJPWatcherEnabled()) {
            /*
             * Start task if enabled
             */
            final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
            if (null != timer) {
                task =
                    timer.scheduleWithFixedDelay(
                        new Task(tasks.values(), threadPoolService, LOG),
                        1000,
                        AJPv13Config.getAJPWatcherFrequency(),
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Adds given task to this watcher.
     * 
     * @param task The AJP task to add
     */
    public void addTask(final AJPv13Task task) {
        if (null != tasks.putIfAbsent(task.getNum(), task)) {
            LOG.error(MessageFormat.format("AJP with number {0} task could not be added to watcher!", task.getNum()));
        }
    }

    /**
     * Removes given task from this watcher
     * 
     * @param task The AJP task to remove
     */
    public void removeTask(final AJPv13Task task) {
        tasks.remove(task.getNum());
    }

    /**
     * Stops this watcher (and cancels all tracked AJP tasks).
     */
    public void stop() {
        for (final Iterator<AJPv13Task> i = tasks.values().iterator(); i.hasNext();) {
            i.next().cancel();
            i.remove();
        }
        tasks.clear();
        if (null != task) {
            task.cancel(false);
            task = null;
            final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
            if (null != timer) {
                timer.purge();
            }
        }
    }

    private static class Task implements Runnable {

        private final Collection<AJPv13Task> tasks;

        private final org.apache.commons.logging.Log log;

        private final ThreadPoolService threadPoolService;

        /**
         * Initializes a new {@link Task}
         * 
         * @param tasks The map to iterate
         * @param threadPoolService The thread pool service
         * @param log The logger instance to use
         */
        public Task(final Collection<AJPv13Task> tasks, final ThreadPoolService threadPoolService, final org.apache.commons.logging.Log log) {
            super();
            this.tasks = tasks;
            this.log = log;
            this.threadPoolService = threadPoolService;
        }

        public void run() {
            try {
                final boolean enabled = AJPv13Config.getAJPWatcherPermission();
                final AtomicInteger countWaiting;
                final AtomicInteger countProcessing;
                final AtomicInteger countExceeded;
                if (enabled) {
                    countWaiting = new AtomicInteger();
                    countProcessing = new AtomicInteger();
                    countExceeded = new AtomicInteger();
                } else {
                    countWaiting = null;
                    countProcessing = null;
                    countExceeded = null;
                }
                /*
                 * Create a list of tasks
                 */
                final Collection<com.openexchange.threadpool.Task<Object>> tasks =
                    new ArrayList<com.openexchange.threadpool.Task<Object>>();
                for (final Iterator<AJPv13Task> iter = this.tasks.iterator(); iter.hasNext();) {
                    tasks.add(new TaskRunCallable(iter.next(), enabled, countWaiting, countProcessing, countExceeded, log));
                }
                /*
                 * Invoke all and wait for being executed
                 */
                threadPoolService.invokeAll(tasks);
                /*
                 * All threads are listening longer than specified max listener running time
                 */
                if (enabled && countProcessing.get() > 0 && countExceeded.get() == countProcessing.get()) {
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
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    } // End of class Task

    private static final class TaskRunCallable implements com.openexchange.threadpool.Task<Object> {

        private final AJPv13Task task;

        private final boolean enabled;

        private final AtomicInteger waiting;

        private final AtomicInteger processing;

        private final AtomicInteger exceeded;

        private final org.apache.commons.logging.Log log;

        public TaskRunCallable(final AJPv13Task task, final boolean enabled, final AtomicInteger waiting, final AtomicInteger processing, final AtomicInteger exceeded, final org.apache.commons.logging.Log log) {
            super();
            this.task = task;
            this.enabled = enabled;
            this.exceeded = exceeded;
            this.processing = processing;
            this.waiting = waiting;
            this.log = log;
        }

        public Object call() {
            if (enabled && task.isWaitingOnAJPSocket()) {
                waiting.incrementAndGet();
            }
            if (task.isProcessing()) {
                /*
                 * Task is currently processing and is NOT marked as a long-running task
                 */
                if (enabled) {
                    processing.incrementAndGet();
                }
                final long currentProcTime = (System.currentTimeMillis() - task.getProcessingStartTime());
                if (currentProcTime > AJPv13Config.getAJPWatcherMaxRunningTime()) {
                    if (enabled) {
                        exceeded.incrementAndGet();
                    }
                    if (!task.isLongRunning() && log.isInfoEnabled()) {
                        final Throwable t = new Throwable();
                        t.setStackTrace(task.getStackTrace());
                        log.info(new StringBuilder(128).append("AJP Listener \"").append(task.getThreadName()).append(
                            "\" exceeds max. running time of ").append(AJPv13Config.getAJPWatcherMaxRunningTime()).append(
                            "msec -> Processing time: ").append(currentProcTime).append("msec").toString(), t);
                    }
                    /*
                     * Check if keep-alive shall be sent
                     */
                    final AJPv13ConnectionImpl ajpConnection = task.getAJPConnection();
                    if ((System.currentTimeMillis() - ajpConnection.getLastWriteAccess()) > AJPv13Config.getAJPWatcherMaxRunningTime()) {
                        if (log.isInfoEnabled()) {
                            log.info(new StringBuilder(128).append("Sending KEEP-ALIVE for AJP Listener \"").append(task.getThreadName()).append(
                                '"').toString());
                        }
                        /*
                         * Send "keep-alive" package
                         */
                        try {
                            keepAlive(
                                task.getAJPConnection(),
                                log.isInfoEnabled() ? task.getSocket().getRemoteSocketAddress().toString() : null);
                        } catch (final AJPv13Exception e) {
                            log.error("AJP KEEP-ALIVE failed.", e);
                        } catch (final IOException e) {
                            log.error("AJP KEEP-ALIVE failed.", e);
                        }
                    }
                }
            }
            return null;
        }

        private void keepAlive(final AJPv13ConnectionImpl ajpConnection, final String remoteAddress) throws IOException, AJPv13Exception {
            /*
             * Send "keep-alive" package; first poll connection by sending outstanding data
             */
            final AJPv13RequestHandler ajpRequestHandler = ajpConnection.getAjpRequestHandler();
            ajpConnection.blockOutputStream(true);
            try {
                final OutputStream out = ajpConnection.getOutputStream();
                final byte[] remainingData = ajpRequestHandler.getAndClearResponseData();
                if (remainingData.length > 0) {
                    /*
                     * Send response headers first.
                     */
                    ajpRequestHandler.doWriteHeaders(out);
                    /*
                     * Send rest of data cut into MAX_BODY_CHUNK_SIZE pieces
                     */
                    int offset = 0;
                    final int maxLen = AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE;
                    while (offset < remainingData.length) {
                        final int b = (remainingData.length - offset);
                        final int curLen = ((maxLen <= b) ? maxLen : b); // Math.min(int a, int b)
                        out.write(AJPv13Response.getSendBodyChunkBytes(remainingData, offset, curLen));
                        out.flush();
                        offset += curLen;
                    }
                    if (log.isInfoEnabled()) {
                        log.info(new StringBuilder().append("Flushed available data to socket \"").append(remoteAddress).append(
                            "\" to initiate a KEEP-ALIVE poll."));
                    }
                } else {
                    /*
                     * No outstanding data; poll connection through requesting an empty data chunk.
                     */
                    ajpConnection.blockInputStream(true);
                    try {
                        out.write(AJPv13Response.getGetBodyChunkBytes(0));
                        out.flush();
                        if (log.isInfoEnabled()) {
                            log.info(new StringBuilder().append("Flushed empty GET-BODY request to socket \"").append(remoteAddress).append(
                                "\" to initiate a KEEP-ALIVE poll."));
                        }
                        /*
                         * Swallow expected empty body chunk
                         */
                        final int bodyRequestDataLength = ajpConnection.readInitialBytes(true, false);
                        if (bodyRequestDataLength > 0 && parseInt(ajpConnection.getPayloadData(bodyRequestDataLength, true)) > 0) {
                            log.warn("Got a non-empty data chunk from web server although an empty one was requested");
                        } else if (log.isInfoEnabled()) {
                            log.info(new StringBuilder().append("Swallowed empty REQUEST-BODY from socket \"").append(remoteAddress).append(
                                "\" initiated by former KEEP-ALIVE poll."));
                        }
                    } finally {
                        ajpConnection.blockInputStream(false);
                    }
                }
            } finally {
                ajpConnection.blockOutputStream(false);
            }
        } // End of keepAlive()

        private static int parseInt(final byte[] payloadData) {
            return ((payloadData[0] & 0xff) << 8) + (payloadData[1] & 0xff);
        }

        public void afterExecute(final Throwable t) {
            // NOP
        }

        public void beforeExecute(final Thread t) {
            // NOP
        }

        public void setThreadName(final ThreadRenamer threadRenamer) {
            // NOP
        }

    } // End of TaskRunCallable class

}
