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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.sun.mail.imap;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.mail.URLName;
import org.slf4j.Logger;
import com.sun.mail.imap.QueuingIMAPStore.CountingQueue;
import com.sun.mail.imap.QueuingIMAPStore.ThreadTrace;


/**
 * {@link QueuedIMAPProtocolWatcher} - The watcher for <code>QueuedIMAPProtocol</code> instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.1
 */
public final class QueuedIMAPProtocolWatcher {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QueuedIMAPProtocolWatcher.class);

    /**
     * Checks if watcher has been enabled.
     *
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     */
    static boolean isEnabled() {
        return LOG.isDebugEnabled();
    }

    // ------------------------------------------------------------------------------------ //

    /** This watcher's scheduled future task */
    private volatile ScheduledFuture<?> watcherFuture;

    /** The line separator */
    final String lineSeparator;

    /**
     * Initializes a new {@link QueuedIMAPProtocolWatcher}.
     */
    public QueuedIMAPProtocolWatcher() {
        super();
        lineSeparator = System.getProperty("line.separator");
    }

    /**
     * Shuts-down the watcher.
     */
    public void shutdown() {
        final ScheduledFuture<?> tmp = watcherFuture;
        if (null != tmp) {
            tmp.cancel(false);
            watcherFuture = null;
        }
    }

    /**
     * Attempts to initialize this watcher if not already performed.
     *
     * @param queues The queues to watch
     * @param executor The executor to schedule periodic task
     */
    public void initWatcher(final ConcurrentMap<URLName, CountingQueue> queues, final ScheduledThreadPoolExecutor executor) {
        if (!isEnabled()) {
            LOG.info(QueuedIMAPProtocolWatcher.class.getSimpleName() + " not initialized since configured log level does not imply DEBUG log level.");
            return;
        }
        ScheduledFuture<?> tmp = watcherFuture;
        if (null == tmp) {
            synchronized (this) {
                tmp = watcherFuture;
                if (null == tmp) {
                    final Runnable t = new Runnable() {

                        @Override
                        public void run() {
                            final long minStamp = System.currentTimeMillis() - 30000; // longer than 30 seconds
                            final ConcurrentMap<URLName, CountingQueue> qs = queues;
                            final String sep = lineSeparator;
                            for (final QueuingIMAPStore.CountingQueue q : qs.values()) {
                                // Examine CountingQueue's tracked threads
                                final ConcurrentMap<Thread, ThreadTrace> trackedThreads = q.trackedThreads();
                                if (null != trackedThreads) {
                                    // Iterate threads
                                    for (final Entry<Thread, QueuingIMAPStore.ThreadTrace> entry : trackedThreads.entrySet()) {
                                        final QueuingIMAPStore.ThreadTrace trace = entry.getValue();
                                        if (!trace.protocol.isIdle() && trace.stamp < minStamp) {
                                            final long dur = System.currentTimeMillis() - trace.stamp;
                                            final String msg = formatThread(entry.getKey(), trace.protocol, q, dur, sep);
                                            q.getLogger().fine(msg);
                                            LOG.debug(msg);
                                        }
                                    }
                                }
                            }
                        }
                    };
                    tmp = executor.scheduleWithFixedDelay(t, 10, 10, TimeUnit.SECONDS);
                    watcherFuture = tmp;
                    LOG.info(QueuedIMAPProtocolWatcher.class.getSimpleName() + " successfully initialized.");
                }
            }
        }
    }

    static String formatThread(final Thread thread, final QueuedIMAPProtocol protocol, final QueuingIMAPStore.CountingQueue q, final long dur, final String lineSeparator) {
        final StringBuilder sBuilder = new StringBuilder(8192);
        sBuilder.append("Thread \"").append(thread.getName()).append("\" holds ").append(protocol).append(" for ").append(dur).append("msec.").append(lineSeparator);
        sBuilder.append('(').append(q.getNewCount()).append(" in use for queue ").append(q.hashCode()).append(')').append(lineSeparator);
        final StackTraceElement[] trace = thread.getStackTrace();
        sBuilder.append("    at ").append(trace[0]);
        for (int i = 1; i < trace.length; i++) {
            sBuilder.append(lineSeparator).append("    at ").append(trace[i]);
        }
        return sBuilder.toString();
    }
}
