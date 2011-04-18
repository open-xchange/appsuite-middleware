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
import com.openexchange.ajp13.AJPv13Request;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.BlockableBufferedOutputStream;
import com.openexchange.ajp13.exception.AJPv13Exception;
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13TaskWatcher.class);

    private ScheduledTimerTask scheduledTimerTask;

    private final ConcurrentMap<Long, AJPv13Task> tasks;

    /**
     * Initializes a new {@link AJPv13TaskWatcher}.
     * 
     * @param threadPoolService The thread pool service
     */
    public AJPv13TaskWatcher(final ThreadPoolService threadPoolService) {
        super();
        tasks = new ConcurrentHashMap<Long, AJPv13Task>();
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
    public void addTask(final AJPv13Task task) {
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

        private final Collection<AJPv13Task> tasks;

        private final org.apache.commons.logging.Log log;

        private final ThreadPoolService threadPoolService;

        /**
         * Initializes a new {@link TimerTaskRunnable}
         * 
         * @param tasks The map to iterate
         * @param threadPoolService The thread pool service
         * @param log The logger instance to use
         */
        public TimerTaskRunnable(final Collection<AJPv13Task> tasks, final ThreadPoolService threadPoolService, final org.apache.commons.logging.Log log) {
            super();
            this.tasks = tasks;
            this.log = log;
            this.threadPoolService = threadPoolService;
        }

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
                    for (final AJPv13Task ajPv13Task : this.tasks) {
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
                    for (final AJPv13Task ajPv13Task : this.tasks) {
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

        private final AJPv13Task task;

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
        public TaskRunCallable(final AJPv13Task task, final boolean logExceededTasks, final org.apache.commons.logging.Log log) {
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
        public TaskRunCallable(final AJPv13Task task, final AtomicInteger waiting, final AtomicInteger processing, final AtomicInteger exceeded, final boolean logExceededTasks, final org.apache.commons.logging.Log log) {
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

        private void handleExceededTask(final long currentProcTime) {
            /*
             * Log exceeded task if it is not marked as a long-running task
             */
            if (!task.isLongRunning() && logExceededTasks && info) {
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
                /*
                 * Send "keep-alive" package
                 */
                try {
                    keepAlive();
                } catch (final AJPv13Exception e) {
                    log.error("AJP KEEP-ALIVE failed.", e);
                } catch (final IOException e) {
                    log.error("AJP KEEP-ALIVE failed.", e);
                }
            }
        }

        /**
         * Performs AJP-style keep-alive poll to web server to avoid connection timeout.
         * 
         * @throws IOException If an I/O error occurs
         * @throws AJPv13Exception If an AJP error occurs
         */
        private void keepAlive() throws IOException, AJPv13Exception {
            /*
             * Send "keep-alive" package depending on current request handler's state.
             */
            final AJPv13ConnectionImpl ajpConnection = task.getAJPConnection();
            final AJPv13RequestHandler ajpRequestHandler = ajpConnection.getAjpRequestHandler();
            ajpConnection.blockOutputStream(true);
            try {
                if (!ajpRequestHandler.isEndResponseSent()) {
                    final String remoteAddress = info ? task.getSocket().getRemoteSocketAddress().toString() : null;
                    final BlockableBufferedOutputStream out = ajpConnection.getOutputStream();
                    if (ajpRequestHandler.isHeadersSent()) {
                        /*
                         * SEND_HEADERS package already flushed to web server. Keep-Alive needs to be performed by flushing available data
                         * or an empty SEND_BODY package.
                         */
                        final byte[] remainingData = ajpRequestHandler.getAndClearResponseData();
                        if (remainingData.length > 0) {
                            /*
                             * Flush available data cut into MAX_BODY_CHUNK_SIZE chunks
                             */
                            keepAliveSendAvailableData(remoteAddress, out, remainingData);
                        } else {
                            /*
                             * Empty SEND_BODY package.
                             */
                            keepAliveSendEmptyBody(remoteAddress, out);
                        }
                    } else {
                        /*
                         * Pending SEND_HEADERS package. Keep-Alive needs to be performed by requesting an empty data chunk.
                         */
                        keepAliveGetEmptyBody(ajpConnection, remoteAddress, out);
                    }
                }
            } finally {
                ajpConnection.blockOutputStream(false);
            }
        } // End of keepAlive()

        private void keepAliveSendAvailableData(final String remoteAddress, final BlockableBufferedOutputStream out, final byte[] remainingData) throws IOException, AJPv13Exception {
            AJPv13Request.writeChunked(remainingData, out);
            if (log.isDebugEnabled()) {
                log.debug(new StringBuilder().append("AJP KEEP-ALIVE: Flushed available data to socket \"").append(remoteAddress).append(
                    "\" to initiate a KEEP-ALIVE poll."));
            }
        }

        private void keepAliveSendEmptyBody(final String remoteAddress, final BlockableBufferedOutputStream out) throws IOException, AJPv13Exception {
            AJPv13Request.writeEmpty(out);
            if (log.isDebugEnabled()) {
                log.debug(new StringBuilder().append("AJP KEEP-ALIVE: Flushed empty SEND-BODY-CHUNK response to socket \"").append(
                    remoteAddress).append("\" to initiate a KEEP-ALIVE poll."));
            }
        }

        private void keepAliveGetEmptyBody(final AJPv13ConnectionImpl ajpConnection, final String remoteAddress, final OutputStream out) throws IOException, AJPv13Exception {
            ajpConnection.blockInputStream(true);
            try {
                out.write(AJPv13Response.getGetBodyChunkBytes(0));
                out.flush();
                if (log.isDebugEnabled()) {
                    log.debug(new StringBuilder().append("AJP KEEP-ALIVE: Flushed empty GET-BODY request to socket \"").append(remoteAddress).append(
                        "\" to initiate a KEEP-ALIVE poll."));
                }
                /*
                 * Swallow expected empty body chunk
                 */
                final int bodyRequestDataLength = ajpConnection.readInitialBytes(true, false);
                if (bodyRequestDataLength > 0 && parseInt(ajpConnection.getPayloadData(bodyRequestDataLength, true)) > 0) {
                    log.warn("AJP KEEP-ALIVE: Got a non-empty data chunk from web server although an empty one was requested");
                } else if (log.isDebugEnabled()) {
                    log.debug(new StringBuilder().append("AJP KEEP-ALIVE: Swallowed empty REQUEST-BODY from socket \"").append(remoteAddress).append(
                        "\" initiated by former KEEP-ALIVE poll."));
                }
            } finally {
                ajpConnection.blockInputStream(false);
            }
        }

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
