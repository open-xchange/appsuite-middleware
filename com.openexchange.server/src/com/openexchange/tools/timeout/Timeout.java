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

package com.openexchange.tools.timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Timeout} - A simple timeout
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Timeout implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Timeout.class);

    private final Lock lock;

    private final Condition condition;

    private final Thread target;

    private long timeoutMillis;

    private final Thread watcher;

    private boolean loop;

    private boolean enabled;

    /**
     * Timeout for current thread
     *
     * @param timeoutMillis The timeout in milliseconds
     */
    public Timeout(final long timeoutMillis) {
        this(Thread.currentThread(), timeoutMillis);
    }

    /**
     * Timeout for given thread
     *
     * @param target The target thread to kill if timeout elapsed
     * @param timeoutMillis The timeout in milliseconds
     */
    public Timeout(final Thread target, final long timeoutMillis) {
        super();
        lock = new ReentrantLock();
        condition = lock.newCondition();
        this.target = target;
        this.timeoutMillis = timeoutMillis;
        enabled = true;
        watcher = new Thread(this);
        watcher.start();
    }

    /**
     * Tell the timeout that target has finished
     */
    public void done() {
        lock.lock();
        try {
            loop = false;
            enabled = false;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Restart the timeout from zero
     */
    public void reset() {
        lock.lock();
        try {
            loop = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reset
     *
     * @param millis The new timeout in milliseconds
     */
    public void reset(final long timeoutMillis) {
        lock.lock();
        try {
            this.timeoutMillis = timeoutMillis;
            reset();
        } finally {
            lock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        lock.lock();
        try {
            final Thread current = Thread.currentThread();
            current.setPriority(Thread.MAX_PRIORITY);
            if (enabled) {
                do {
                    loop = false;
                    try {
                        condition.await(timeoutMillis, TimeUnit.MILLISECONDS);
                    } catch (final InterruptedException e) {
                        // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                        Thread.currentThread().interrupt();
                        LOG.error("", e);
                    }
                } while (enabled && loop);
            }
            if (enabled && target.isAlive()) {
                LOG.info("Timeout.run(): Stopping thread {}", target.getName());
                target.interrupt();
            }
        } finally {
            lock.unlock();
        }
    }
}
