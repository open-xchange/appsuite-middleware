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

package com.openexchange.tools;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link NonBlockingRWLock} - A non-blocking read-write lock accomplished by a <b>compare-and-set</b> mechanism.
 * <p>
 * The write lock can only be acquired exclusively, but the read lock can be held by multiple threads simultaneously, as long as no write
 * lock is acquired.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NonBlockingRWLock {

    /**
     * Default max. number of concurrent write accesses
     */
    private static final int DEFAULT = 10000;

    private final int maxConcurrentWrites;

    private final AtomicInteger writeCounter;

    private final Lock writeLock;

    /**
     * Initializes a new {@link NonBlockingRWLock}
     *
     * @param infiniteConcurrentWrites <code>true</code> to enable infinite concurrent write accesses; otherwise <code>false</code> to use
     *            default value of <code>10000</code> for max. number of concurrent write accesses
     */
    public NonBlockingRWLock(final boolean infiniteConcurrentWrites) {
        super();
        if (infiniteConcurrentWrites) {
            maxConcurrentWrites = -1;
            writeCounter = new AtomicInteger();
            writeLock = new ReentrantLock();
        } else {
            maxConcurrentWrites = DEFAULT;
            writeCounter = new AtomicInteger();
            writeLock = new ReentrantLock();
        }
    }

    /**
     * Initializes a new {@link NonBlockingRWLock}
     *
     * @param maxConcurrentWrites The max. number of concurrent write accesses; a value equal to or less than zero means infinite concurrent
     *            write accesses
     */
    public NonBlockingRWLock(final int maxConcurrentWrites) {
        super();
        if (maxConcurrentWrites <= 0) {
            this.maxConcurrentWrites = -1;
        } else {
            this.maxConcurrentWrites = maxConcurrentWrites;
        }
        writeCounter = new AtomicInteger();
        writeLock = new ReentrantLock();
    }

    /**
     * Acquires the read lock and returns current state.
     * <p>
     * The general contract of acquiring and finally releasing a read lock is:
     *
     * <pre>
     * int state;
     * do {
     *     state = myRWLock.acquireRead();
     *     // your code here
     * } while (!myRWLock.releaseRead(state));
     * </pre>
     *
     * @return The current state
     */
    public int acquireRead() {
        int state = writeCounter.get();
        while ((state & 1) == 1) {
            /*
             * Write access in progress
             */
            state = writeCounter.get();
        }
        return state;
    }

    /**
     * Acquires the write lock exclusively.
     * <p>
     * The general contract of acquiring and finally releasing a write lock is:
     *
     * <pre>
     * myRWLock.acquireWrite();
     * try {
     *     // your code here
     * } finally {
     *     myRWLock.releaseWrite();
     * }
     * </pre>
     */
    public void acquireWrite() {
        writeLock.lock();
        if (maxConcurrentWrites == -1) {
            writeCounter.getAndIncrement();
        } else {
            writeCounter.set(writeCounter.incrementAndGet() % maxConcurrentWrites);
        }
    }

    /**
     * Releases the previously acquired read lock and indicates if previous state still equals current state. See {@link #acquireRead()} how
     * to deal with the return value.
     *
     * @param prevState The previous state when read lock was acquired
     * @return <code>true</code> if previous state still equals current state and thus read lock has been released successfully; otherwise
     *         <code>false</code>
     */
    public boolean releaseRead(final int prevState) {
        return (prevState == writeCounter.get());
    }

    /**
     * Releases the previously acquired exclusive write lock.
     *
     * @see #acquireWrite()
     */
    public void releaseWrite() {
        if (maxConcurrentWrites == -1) {
            writeCounter.getAndIncrement();
        } else {
            writeCounter.set(writeCounter.incrementAndGet() % maxConcurrentWrites);
        }
        writeLock.unlock();
    }
}
