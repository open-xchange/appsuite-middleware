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

package com.openexchange.ajp13;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.tools.NonBlockingRWLock;

/**
 * {@link AJPv13ListenerPool} - The AJP listener pool
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AJPv13ListenerPool {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJPv13ListenerPool.class);

    /**
     * A capacity-bounded queue
     */
    private static BlockingQueue<AJPv13Listener> LISTENER_QUEUE;

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private static final AtomicInteger listenerNum = new AtomicInteger();

    private static final NonBlockingRWLock RW_LOCK = new NonBlockingRWLock(true);

    private AJPv13ListenerPool() {
        super();
    }

    /**
     * Initializes the pool by putting as many listeners as specified through AJP configuration's
     * {@link AJPv13Config#getAJPListenerPoolSize() listener pool size}.
     */
    static void initPool() {
        if (!initialized.get()) {
            synchronized (initialized) {
                if (!initialized.get()) {
                    final int poolSize = AJPv13Config.getAJPListenerPoolSize();
                    LISTENER_QUEUE = new ArrayBlockingQueue<AJPv13Listener>(poolSize);
                    AJPv13Server.ajpv13ListenerMonitor.setPoolSize(poolSize);
                    AJPv13Server.ajpv13ListenerMonitor.setNumIdle(poolSize);
                    for (int i = 0; i < poolSize; i++) {
                        final AJPv13Listener l = new AJPv13Listener(listenerNum.incrementAndGet(), true);
                        AJPv13Watcher.addListener(l);
                        LISTENER_QUEUE.offer(l);
                    }
                    initialized.set(true);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(new StringBuilder(32).append(poolSize).append(" AJPv13-Listeners created!").toString());
                    }
                }
            }
        }
    }

    /**
     * Resets the pool
     */
    static void resetPool() {
        RW_LOCK.acquireWrite();
        try {
            if (LISTENER_QUEUE != null) {
                /*
                 * Clear queue one-by-one
                 */
                while (!LISTENER_QUEUE.isEmpty()) {
                    try {
                        final AJPv13Listener l = LISTENER_QUEUE.poll();
                        if (!l.stopListener()) {
                            LOG.error(new StringBuilder(128).append("Listener ").append(l.getListenerName()).append(
                                " could NOT be properly stopped."));
                        }
                    } catch (final Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                LISTENER_QUEUE = null;
            }
            initialized.set(false);
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    /**
     * Removes the listener from pool whose number equals specified number
     * 
     * @param num The number of the listener to remove
     */
    static void removeListener(final int num) {
        RW_LOCK.acquireWrite();
        try {
            boolean removed = false;
            for (final Iterator<AJPv13Listener> iter = LISTENER_QUEUE.iterator(); !removed && iter.hasNext();) {
                if (iter.next().getListenerNumber() == num) {
                    iter.remove();
                    removed = true;
                }
            }
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    /**
     * Checks if listener pool has been initialized
     * 
     * @return <code>true</code> if listener pool has been initialized; otherwise <code>false</code>
     */
    static boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Fetches a listener from pool if available, otherwise a new listener is created
     * 
     * @return A pooled or newly created listener
     */
    static AJPv13Listener getListener() {
        AJPv13Listener retval = null;
        boolean add2Watcher = false;
        int state;
        do {
            state = RW_LOCK.acquireRead();
            retval = LISTENER_QUEUE.poll();
            if (retval == null) {
                /*
                 * All pre-created listeners are running. Create & return a new non-pooled listener.
                 */
                retval = new AJPv13Listener(listenerNum.incrementAndGet());
                add2Watcher = true;
            } else {
                add2Watcher = false;
            }
        } while (!RW_LOCK.releaseRead(state));
        if (add2Watcher) {
            AJPv13Watcher.addListener(retval);
        } else {
            AJPv13Server.ajpv13ListenerMonitor.decrementPoolSize();
            AJPv13Server.ajpv13ListenerMonitor.decrementNumIdle();
        }
        return retval;
    }

    /**
     * Puts back the given listener into pool if pool is not full, yet. If <code>enforcedPut</code> is <code>true</code> the listener is
     * going to be put in any case.
     * 
     * @param listener The AJP listener which shall be put back into pool
     * @return <code>true</code> if given listener can be put into pool, <code>false</code> otherwise
     */
    static boolean putBack(final AJPv13Listener listener) {
        RW_LOCK.acquireWrite();
        try {
            final boolean added2Pool = LISTENER_QUEUE.offer(listener);
            if (added2Pool) {
                AJPv13Server.ajpv13ListenerMonitor.incrementPoolSize();
                AJPv13Server.ajpv13ListenerMonitor.incrementNumIdle();
            }
            return added2Pool;
        } finally {
            RW_LOCK.releaseWrite();
        }
    }

    /**
     * Gets the current pool size
     * 
     * @return The current pool size
     */
    static int getPoolSize() {
        return LISTENER_QUEUE.size();
    }

}
