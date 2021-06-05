/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
