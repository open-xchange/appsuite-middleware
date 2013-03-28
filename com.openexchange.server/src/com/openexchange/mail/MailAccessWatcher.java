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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail;

import static com.openexchange.java.Autoboxing.l;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.log.Log;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MailAccessWatcher} - Keeps track of connected instances of {@link MailAccess} and allows a forced close if connection time exceeds
 * allowed time
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessWatcher {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccessWatcher.class));

    private static final ConcurrentMap<MailAccess<?, ?>, Long> MAIL_ACCESSES = new NonBlockingHashMap<MailAccess<?, ?>, Long>();

    private static boolean initialized = false;

    private static volatile ScheduledTimerTask watcherTask;

    /**
     * Initializes and starts mail connection watcher if not done, yet
     */
    static synchronized void init() {
        if (initialized) {
            return;
        }
        if (MailProperties.getInstance().isWatcherEnabled()) {
            /*
             * Start task
             */
            final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
            if (null != timer) {
                final int frequencyMillis = MailProperties.getInstance().getWatcherFrequency();
                watcherTask = timer.scheduleWithFixedDelay(new WatcherTask(MAIL_ACCESSES, LOG), 1000, frequencyMillis);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Mail connection watcher successfully established and ready for tracing");
            }
        }
        initialized = true;
    }

    /**
     * Stops mail connection watcher if currently running
     */
    static synchronized void stop() {
        if (!initialized) {
            return;
        }
        if (MailProperties.getInstance().isWatcherEnabled()) {
            if (null != watcherTask) {
                watcherTask.cancel(false);
                final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
                if (null != timer) {
                    timer.purge();
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Mail connection watcher successfully stopped");
            }
        }
        MAIL_ACCESSES.clear();
        initialized = false;
    }

    /**
     * Prevent instantiation
     */
    private MailAccessWatcher() {
        super();
    }

    /**
     * Adds specified mail access to this watcher's tracing if not already added before. If already present its timestamp is updated.
     * <p>
     * Watcher is established if not running, yet
     *
     * @param mailAccess The mail access to add
     */
    public static void addMailAccess(final MailAccess<?, ?> mailAccess) {
        /*
         * Insert or update time stamp
         */
        MAIL_ACCESSES.put(mailAccess, Long.valueOf(System.currentTimeMillis()));
    }

    /**
     * Removes specified mail access from this watcher's tracing
     *
     * @param mailAccess The mail access to remove
     */
    public static void removeMailAccess(final MailAccess<?, ?> mailAccess) {
        MAIL_ACCESSES.remove(mailAccess);
    }

    /**
     * Gets the number of currently tracked mail accesses.
     *
     * @return The number of currently tracked mail accesses
     */
    public static int getNumberOfMailAccesses() {
        return MAIL_ACCESSES.size();
    }

    /**
     * Gets the number of currently tracked idling mail accesses.
     *
     * @return The number of currently tracked idling mail accesses
     */
    public static int getNumberOfIdlingMailAccesses() {
        int count = 0;
        for (final MailAccess<?, ?> mailAccess : MAIL_ACCESSES.keySet()) {
            if (mailAccess.isConnectedUnsafe() && mailAccess.isWaiting()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Helper class.
     */
    private static class WatcherTask implements Runnable {

        private final ConcurrentMap<MailAccess<?, ?>, Long> map;
        private final org.apache.commons.logging.Log logger;
        private final boolean traceEnabled;
        private final String lineSeparator;

        public WatcherTask(final ConcurrentMap<MailAccess<?, ?>, Long> mailAccesses, final org.apache.commons.logging.Log logger) {
            super();
            map = mailAccesses;
            this.logger = logger;
            traceEnabled = logger.isTraceEnabled();
            lineSeparator = System.getProperty("line.separator");
        }

        @Override
        public void run() {
            try {
                if (map.isEmpty()) {
                    /*
                     * Nothing to trace...
                     */
                    return;
                }
                final StringBuilder sb = new StringBuilder(512);
                final List<MailAccess<?, ?>> exceededAcesses = new LinkedList<MailAccess<?, ?>>();
                final int watcherTime = MailProperties.getInstance().getWatcherTime();
                final long now = System.currentTimeMillis();
                for (final Iterator<Entry<MailAccess<?, ?>, Long>> iter = map.entrySet().iterator(); iter.hasNext();) {
                    final Entry<MailAccess<?, ?>, Long> e = iter.next();
                    final MailAccess<?, ?> mailAccess = e.getKey();
                    if (mailAccess.isConnected()) {
                        if (mailAccess.isWaiting()) {
                            if (traceEnabled) {
                                logger.trace(new com.openexchange.java.StringAllocator("Idling/waiting mail connection:").append(lineSeparator).append(mailAccess.getTrace()).toString());
                            }
                        } else {
                            final Long val = e.getValue();
                            if (null != val) {
                                final long duration = (now - l(val));
                                if (duration > watcherTime) {
                                    sb.setLength(0);
                                    sb.append("UNCLOSED MAIL CONNECTION AFTER ");
                                    sb.append(duration).append("msec:");
                                    if (Log.appendTraceToMessage()) {
                                        sb.append(lineSeparator);
                                        sb.append(mailAccess.getTrace());
                                        logger.info(sb.toString());
                                    } else {
                                        mailAccess.logTrace(sb, logger);
                                    }
                                    exceededAcesses.add(mailAccess);
                                }
                            }
                        }
                    } else {
                        /*
                         * Remove closed connection from watcher
                         */
                        iter.remove();
                    }
                }
                /*
                 * Remove/Close exceeded accesses if allowed to
                 */
                if (!exceededAcesses.isEmpty()) {
                    if (MailProperties.getInstance().isWatcherShallClose()) {
                        for (final MailAccess<?, ?> mailAccess : exceededAcesses) {
                            try {
                                sb.setLength(0);
                                sb.append("CLOSING MAIL CONNECTION BY WATCHER:").append(lineSeparator).append(mailAccess.toString());
                                mailAccess.close(false);
                                sb.append(lineSeparator).append("    DONE");
                                logger.info(sb.toString());
                            } finally {
                                map.remove(mailAccess);
                            }
                        }
                    } else {
                        for (final MailAccess<?, ?> mailAccess : exceededAcesses) {
                            map.remove(mailAccess);
                        }
                    }
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    } // End of WatcherTask class

}
