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

package com.openexchange.java;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * {@link SynchronizedBasedReentrantLock} - A {@link Lock} based on Java's <code>synchronized</code>.
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">
 * <b>Note</b>: This class does not support the following methods and invocations will throw an {@link UnsupportedOperationException}:
 * <ul>
 * <li>{@link Lock#tryLock(long, TimeUnit)}</li>
 * <li>{@link Lock#newCondition()}</li>
 * </ul>
 * </div>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SynchronizedBasedReentrantLock implements Lock {

    /**
     * Holds the owner of this lock.
     */
    private Thread owner;

    /**
     * Holds the number of time this lock has been acquired by its owner.
     */
    private long count;

    /**
     * The optional external lock object.
     */
    private Object lock;

    /**
     * Default constructor.
     */
    public SynchronizedBasedReentrantLock() {
        super();
    }

    /**
     * Default constructor.
     * 
     * @param lock The lock object
     */
    public SynchronizedBasedReentrantLock(final Object lock) {
        super();
        this.lock = lock;
    }

    private Object getLockObject() {
        return null == lock ? this : lock;
    }

    /**
     * Acquires the lock.
     */
    @Override
    public void lock() {
        final Thread caller = Thread.currentThread();
        final Object mutex = getLockObject();
        synchronized (mutex) {
            if (caller == owner) {
                count++;
                return;
            }
            try {
                while (owner != null) {
                    mutex.wait();
                }
                owner = caller;
                count = 1;
            } catch (final InterruptedException exception) {
                return;
            }
        }
    }

    /**
     * Acquires the lock only if it not held by another thread.
     * 
     * @return <code>true</code> if the lock was free and was acquired by the current thread, or the lock was already held by the current
     *         thread; <code>false</code> otherwise.
     */
    @Override
    public boolean tryLock() {
        synchronized (getLockObject()) {
            if (owner != null) {
                return false;
            }
            lock();
            return true;
        }
    }

    /**
     * Attempts to release this lock. The lock is actually released if at least as many {@link #unlock} as {@link #lock} have been performed
     * on this {@link ReentrantLock} by the current thread. throws IllegalMonitorStateExeception if the current thread does not hold this
     * lock.
     */
    @Override
    public void unlock() {
        final Object mutex = getLockObject();
        synchronized (mutex) {
            if (Thread.currentThread() != owner) {
                throw new IllegalMonitorStateException("Current thread does not hold this lock");
            }
            if (--count == 0) {
                owner = null;
                mutex.notify();
            }
        }
    }

    /**
     * Returns the thread owner of this {@link ReentrantLock}.
     * 
     * @return the owner of this lock.
     */
    public Thread getOwner() {
        synchronized (getLockObject()) {
            return owner;
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // 'synchronized' listens to interruptions by default
        lock();
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("SynchronizedBasedReentrantLock.tryLock() is not supported.");
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("SynchronizedBasedReentrantLock.newCondition() is not supported.");
    }

}
