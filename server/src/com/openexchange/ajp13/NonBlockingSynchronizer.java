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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link NonBlockingSynchronizer} - Non-blocking synchronizer; also useful to wrap an existing {@link Runnable runnable}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NonBlockingSynchronizer implements Synchronizer, Runnable {

    private volatile Runnable runnable;

    private volatile boolean obtainLock;

    private final AtomicInteger running;

    private final AtomicInteger writeCounter;

    private final Lock runLock;

    /**
     * Initializes a new {@link NonBlockingSynchronizer}.
     * 
     * @param runnable The task to synchronize
     */
    public NonBlockingSynchronizer() {
        this(null);
    }

    /**
     * Initializes a new {@link NonBlockingSynchronizer} wrapping given {@link Runnable runnable}.
     * 
     * @param runnable The runnable to synchronize
     */
    public NonBlockingSynchronizer(final Runnable runnable) {
        super();
        writeCounter = new AtomicInteger();
        running = new AtomicInteger();
        runLock = new ReentrantLock();
        this.runnable = runnable;
    }

    /**
     * Gets the runnable.
     * 
     * @return The runnable
     */
    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Sets the runnable.
     * 
     * @param runnable The runnable to set
     */
    public void setRunnable(final Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * Sets whether to synchronize access or not.
     * 
     * @param synchronize <code>true</code> to synchronize access; otherwise <code>false</code>
     * @return This non-blocking synchronizer with new synchronize policy applied
     */
    public Runnable setSynchronized(final boolean synchronize) {
        synchronized (this) {
            writeCounter.getAndIncrement();
            try {
                int i = running.get();
                while (i > 0) {
                    i = running.get();
                }
                obtainLock = synchronize;
            } finally {
                writeCounter.getAndIncrement();
            }
            return this;
        }
    }

    public Lock acquire() {
        int state = writeCounter.get();
        while ((state & 1) == 1) {
            /*
             * Synchronized access in progress
             */
            state = writeCounter.get();
        }
        running.incrementAndGet();
        final Lock lock;
        if (!obtainLock) {
            return null;
        }
        lock = runLock;
        lock.lock();
        return lock;
    }

    public void release(final Lock lock) {
        if (null != lock) {
            lock.unlock();
        }
        running.decrementAndGet();
    }

    /**
     * Executes wrapped {@link Runnable runnable} according to applied synchronization policy.
     * <p>
     * This method does nothing if runnable is <code>null</code>.
     */
    public void run() {
        int state = writeCounter.get();
        while ((state & 1) == 1) {
            /*
             * Write access in progress
             */
            state = writeCounter.get();
        }
        running.incrementAndGet();
        final Lock lock;
        if (obtainLock) {
            lock = runLock;
            lock.lock();
        } else {
            lock = null;
        }
        try {
            if (null != runnable) {
                runnable.run();
            }
        } finally {
            if (null != lock) {
                lock.unlock();
            }
            running.decrementAndGet();
        }
    }

    public void synchronize() {
        setSynchronized(true);
    }

    public void unsynchronize() {
        setSynchronized(false);
    }

}
