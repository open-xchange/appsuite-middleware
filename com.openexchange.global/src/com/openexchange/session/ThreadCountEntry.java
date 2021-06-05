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

package com.openexchange.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link ThreadCountEntry} - A thread count entry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadCountEntry implements Comparable<ThreadCountEntry> {

    private static final Object PRESENT = new Object();

    /**
     * The read-write lock.
     */
    private final ReadWriteLock readWriteLock;

    /**
     * The session identifier.
     */
    private final String sessionId;

    /**
     * The count.
     */
    private int count;

    /**
     * The thread set.
     */
    private final Map<Thread, Object> threads;

    /**
     * Initializes a new {@link ThreadCountEntry}.
     */
    public ThreadCountEntry(final String sessionId) {
        super();
        this.sessionId = sessionId;
        readWriteLock = new ReentrantReadWriteLock();
        count = 0;
        threads = new HashMap<Thread, Object>(16);
    }

    @Override
    public int compareTo(final ThreadCountEntry o) {
        final int thisVal = this.get();
        final int anotherVal = o.get();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Gets the session identifier.
     *
     * @return The session identifier
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the current count.
     *
     * @return The current count.
     */
    public int get() {
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return count;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the available threads at invocation time.
     *
     * @return The available threads
     */
    public Set<Thread> getThreads() {
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return new HashSet<Thread>(threads.keySet());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the available threads if total number is equal to or greater than specified threshold at invocation time.
     *
     * @param threshold The threshold
     * @return The available threads
     */
    public Set<Thread> getThreads(final int threshold) {
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            if (count < threshold) {
                return Collections.emptySet();
            }
            return new HashSet<Thread>(threads.keySet());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Resets the count.
     */
    public void reset() {
        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            count = 0;
            threads.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Atomically increments by one the current count.
     *
     * @return The updated value
     */
    public int incrementAndGet() {
        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (null != threads.put(Thread.currentThread(), PRESENT)) {
                return count;
            }
            return ++count;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Atomically decrements by one the current count.
     *
     * @return The updated value
     */
    public int decrementAndGet() {
        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            final Object removed = threads.remove(Thread.currentThread());
            if (null == removed || count <= 0) {
                return count;
            }
            return --count;
        } finally {
            writeLock.unlock();
        }
    }

}
