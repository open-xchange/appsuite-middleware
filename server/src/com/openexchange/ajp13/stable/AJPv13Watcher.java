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

package com.openexchange.ajp13.stable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.Timer;

/**
 * {@link AJPv13Watcher} - A watcher for AJP listeners which keeps track of their run time.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AJPv13Watcher {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13Watcher.class);

    private static ScheduledTimerTask task;

    private static final ConcurrentMap<Integer, AJPv13Listener> listeners = new ConcurrentHashMap<Integer, AJPv13Listener>();

    static void initializeAJPv13Watcher() {
        if (AJPv13Config.getAJPWatcherEnabled()) {
            /*
             * Start task
             */
            final Timer timer = ServerServiceRegistry.getInstance().getService(Timer.class);
            if (timer != null) {
                task = timer.scheduleWithFixedDelay(new Task(listeners, LOG), 1000, AJPv13Config.getAJPWatcherFrequency());
            }
        }
    }

    static void resetAJPv13Watcher() {
        if (null != task) {
            task.cancel(false);
            task = null;
            final Timer timer = ServerServiceRegistry.getInstance().getService(Timer.class);
            if (timer != null) {
                timer.purge();
            }
        }
    }

    static void addListener(final AJPv13Listener listener) {
        listeners.putIfAbsent(Integer.valueOf(listener.getListenerNumber()), listener);
    }

    static AJPv13Listener removeListener(final int listenerNum) {
        return listeners.remove(Integer.valueOf(listenerNum));
    }

    static int getNumOfListeners() {
        return listeners.size();
    }

    /**
     * A thread-safe method to stop all listeners sequentially and clears them from map
     */
    static void stopListeners() {
        stopAllListeners();
    }

    /**
     * Stops all listeners sequentially and clears them from map
     */
    private static void stopAllListeners() {
        try {
            for (final Iterator<AJPv13Listener> iter = listeners.values().iterator(); iter.hasNext();) {
                final AJPv13Listener l = iter.next();
                l.stopListener();
                iter.remove();
            }
        } finally {
            listeners.clear();
        }
    }

    private static class Task implements Runnable {

        private final Map<Integer, AJPv13Listener> listeners;

        private final org.apache.commons.logging.Log log;

        /**
         * Initializes a new {@link Task}
         * 
         * @param listeners The map to iterate
         * @param log The logger instance to use
         */
        public Task(final Map<Integer, AJPv13Listener> listeners, final org.apache.commons.logging.Log log) {
            super();
            this.listeners = listeners;
            this.log = log;
        }

        public void run() {
            try {
                int countWaiting = 0;
                int countProcessing = 0;
                int countExceeded = 0;
                for (final Iterator<AJPv13Listener> iter = listeners.values().iterator(); iter.hasNext();) {
                    final AJPv13Listener l = iter.next();
                    if (l.isWaitingOnAJPSocket()) {
                        countWaiting++;
                    }
                    if (l.isProcessing()) {
                        /*
                         * At least one listener is currently processing
                         */
                        countProcessing++;
                        final long currentProcTime = (System.currentTimeMillis() - l.getProcessingStartTime());
                        if (currentProcTime > AJPv13Config.getAJPWatcherMaxRunningTime()) {
                            if (log.isInfoEnabled()) {
                                final Throwable t = new Throwable();
                                t.setStackTrace(l.getStackTrace());
                                log.info(new StringBuilder(128).append("AJP Listener \"").append(l.getListenerName()).append(
                                    "\" exceeds max. running time of ").append(AJPv13Config.getAJPWatcherMaxRunningTime()).append(
                                    "msec -> Processing time: ").append(currentProcTime).append("msec").toString(), t);
                            }
                            countExceeded++;
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
    }

}
