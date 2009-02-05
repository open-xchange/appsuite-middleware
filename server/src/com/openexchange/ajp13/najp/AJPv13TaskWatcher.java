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
import java.util.Collection;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13Connection;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.server.ServerTimer;

/**
 * {@link AJPv13TaskWatcher} - Keeps track of submitted AJP tasks.
 * <p>
 * AJP tasks are actively put right after their submission, but automatically removed through wrapping
 * {@link java.util.concurrent.FutureTask future task}'s <i><code>done()</code></i> method.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJPv13TaskWatcher {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13TaskWatcher.class);

    static final AtomicLong COUNTER = new AtomicLong();

    /**
     * A wrapping future task to cancel an AJP task and remove from watcher when finished.
     */
    public final class WatcherFutureTask extends FutureTask<Object> {

        final Long num;

        final AJPv13Task ajpTask;

        /**
         * Initializes a new {@link WatcherFutureTask}.
         * 
         * @param ajpTask The AJP task to wrap
         */
        public WatcherFutureTask(final AJPv13Task ajpTask) {
            super(ajpTask, null);
            this.num = Long.valueOf(COUNTER.incrementAndGet());
            this.ajpTask = ajpTask;
        }

        @Override
        protected void done() {
            removeListener(this);
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            ajpTask.cancel();
            return super.cancel(mayInterruptIfRunning);
        }
    }

    private Task task;

    private final ConcurrentMap<Long, WatcherFutureTask> listeners;

    /**
     * Initializes a new {@link AJPv13TaskWatcher}.
     */
    public AJPv13TaskWatcher() {
        super();
        listeners = new ConcurrentHashMap<Long, WatcherFutureTask>();
        if (AJPv13Config.getAJPWatcherEnabled()) {
            /*
             * Start task if enabled
             */
            ServerTimer.getTimer().schedule((task = new Task(listeners.values(), LOG)), 1000, AJPv13Config.getAJPWatcherFrequency());
        }
    }

    void addListener(final WatcherFutureTask task) {
        listeners.putIfAbsent(task.num, task);
    }

    void removeListener(final WatcherFutureTask task) {
        listeners.remove(task.num);
    }

    void stop() {
        for (final Iterator<WatcherFutureTask> i = listeners.values().iterator(); i.hasNext();) {
            i.next().cancel(true);
            i.remove();
        }
        listeners.clear();
        if (null != task) {
            task.cancel();
            task = null;
            ServerTimer.getTimer().purge();
        }
    }

    private static class Task extends TimerTask {

        private final Collection<WatcherFutureTask> listeners;

        private final org.apache.commons.logging.Log log;

        /**
         * Initializes a new {@link Task}
         * 
         * @param listeners The map to iterate
         * @param log The logger instance to use
         */
        public Task(final Collection<WatcherFutureTask> listeners, final org.apache.commons.logging.Log log) {
            super();
            this.listeners = listeners;
            this.log = log;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                int countWaiting = 0;
                int countProcessing = 0;
                int countExceeded = 0;
                for (final Iterator<WatcherFutureTask> iter = listeners.iterator(); iter.hasNext();) {
                    final AJPv13Task task = iter.next().ajpTask;
                    if (task.isWaitingOnAJPSocket()) {
                        countWaiting++;
                    }
                    if (task.isProcessing()) {
                        /*
                         * At least one listener is currently processing
                         */
                        countProcessing++;
                        final long currentProcTime = (System.currentTimeMillis() - task.getProcessingStartTime());
                        if (currentProcTime > AJPv13Config.getAJPWatcherMaxRunningTime()) {
                            countExceeded++;
                            if (log.isInfoEnabled()) {
                                final Throwable t = new Throwable();
                                t.setStackTrace(task.getStackTrace());
                                log.info(new StringBuilder(128).append("AJP Listener \"").append(task.getThreadName()).append(
                                    "\" exceeds max. running time of ").append(AJPv13Config.getAJPWatcherMaxRunningTime()).append(
                                    "msec -> Processing time: ").append(currentProcTime).append("msec").toString(), t);
                            }
                            /*
                             * Check if keep-alive shall be sent
                             */
                            if (task.getKeepAliveCounter() == 0 || (System.currentTimeMillis() - task.getKeepAliveStamp()) > AJPv13Config.getAJPWatcherMaxRunningTime()) {
                                if (log.isInfoEnabled()) {
                                    log.info(new StringBuilder(128).append("Sending KEEP-ALIVE for AJP Listener \"").append(
                                        task.getThreadName()).append('"').toString());
                                }
                                /*
                                 * Send "keep-alive" package
                                 */
                                keepAlive(task.getAJPConnection(), task.getSocket().getRemoteSocketAddress().toString());
                                /*
                                 * Remember time stamp
                                 */
                                task.incrementKeepAliveCounter();
                                task.setKeepAliveStamp(System.currentTimeMillis());
                            }
                        }
                    }
                }
                /*
                 * All threads are listening longer than specified max listener running time
                 */
                if (AJPv13Config.getAJPWatcherPermission() && countProcessing > 0 && countExceeded == countProcessing) {
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
                } else {
                    if (log.isTraceEnabled()) {
                        final String delimStr = "\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n";
                        log.trace(new StringBuilder(128 + delimStr.length()).append(delimStr).append("AJP-Watcher's run done: ").append(
                            "    Waiting=").append(countWaiting).append("    Running=").append(countProcessing).append("    Exceeded=").append(
                            countExceeded).append("    Total=").append(listeners.size()).append(delimStr).toString());
                    }
                }
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        private void keepAlive(final AJPv13Connection ajpConnection, final String remoteAddress) throws IOException, AJPv13Exception {
            /*
             * Send "keep-alive" package; first poll connection by sending outstanding data
             */
            final AJPv13RequestHandler ajpRequestHandler = ajpConnection.getAjpRequestHandler();
            final OutputStream out = ajpConnection.getOutputStream();
            ajpConnection.synchronizeOutputStream(true);
            try {
                byte[] remainingData = ajpRequestHandler.getAndClearResponseData();
                if (remainingData.length > 0) {
                    /*
                     * Send response headers first.
                     */
                    ajpRequestHandler.doWriteHeaders(out);
                    /*
                     * Send rest of data cut into MAX_BODY_CHUNK_SIZE pieces
                     */
                    if (remainingData.length > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE) {
                        final byte[] currentData = new byte[AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE];
                        do {
                            final byte[] tmp = new byte[remainingData.length - currentData.length];
                            System.arraycopy(remainingData, 0, currentData, 0, currentData.length);
                            System.arraycopy(remainingData, currentData.length, tmp, 0, tmp.length);
                            out.write(AJPv13Response.getSendBodyChunkBytes(currentData));
                            remainingData = tmp;
                        } while (remainingData.length > AJPv13Response.MAX_SEND_BODY_CHUNK_SIZE);
                    }
                    if (remainingData.length > 0) {
                        /*
                         * Send final SEND_BODY_CHUNK package
                         */
                        out.write(AJPv13Response.getSendBodyChunkBytes(remainingData));
                        out.flush();
                    }
                    if (log.isInfoEnabled()) {
                        log.info(new StringBuilder().append("Flushed available to socket \"").append(remoteAddress).append(
                            "\" to initiate a KEEP-ALIVE poll."));
                    }
                } else {
                    /*
                     * No outstanding data; poll connection through requesting data
                     */
                    out.write(AJPv13Response.getGetBodyChunkBytes(0));
                    out.flush();
                    if (log.isInfoEnabled()) {
                        log.info(new StringBuilder().append("Flushed empty GET-BODY request to socket \"").append(remoteAddress).append(
                            "\" to initiate a KEEP-ALIVE poll."));
                    }
                }
            } finally {
                ajpConnection.synchronizeOutputStream(false);
            }
        } // End of keepAlive()

    } // End of class Task

}
