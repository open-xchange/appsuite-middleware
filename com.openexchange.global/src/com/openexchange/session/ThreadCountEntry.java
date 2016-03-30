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
