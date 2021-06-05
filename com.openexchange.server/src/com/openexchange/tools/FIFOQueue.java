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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class represents a fast (and optional thread-safe) implementation of a FIFO (<code>first-in-first-out</code>) queue backed by an
 * array of generic objects, thus this queue is capacity bounded. This class is only useful if programmer knows the size of the queue in
 * advance.
 * <p>
 * If this queue is created with enabled synchronization mechanism a <code>{@link ReadWriteLock}</code> is used for mutually exclusive
 * access
 *
 * @see ReadWriteLock
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FIFOQueue<T> {

    private final T[] array;

    private int start, end;

    private boolean full;

    private final Lock r, w;

    private final boolean isSynchronized;

    /**
     * Constructor which invokes {@link #FIFOQueue(int, boolean)} with last <code>boolean</code> parameter (<code>isSynchronized</code>) set
     * to <code>true</code>
     *
     * @param maxsize The max. size of this queue
     * @see #FIFOQueue(Class, int, boolean)
     */
    public FIFOQueue(final int maxsize) {
        this(maxsize, true);
    }

    /**
     * Constructor
     *
     * @param maxsize The max. size of this queue
     * @param isSynchronized Whether this queue is synchronized (mutually exclusive) for multiple threads accessing this queue
     */
    public FIFOQueue(final int maxsize, final boolean isSynchronized) {
        final @SuppressWarnings("unchecked") T[] ts = (T[]) new Object[maxsize];
        array = ts;
        start = end = 0;
        full = false;
        this.isSynchronized = isSynchronized;
        if (isSynchronized) {
            final ReadWriteLock rwLock = new ReentrantReadWriteLock();
            r = rwLock.readLock();
            w = rwLock.writeLock();
        } else {
            r = null;
            w = null;
        }
    }

    private void acquireReadLock() {
        if (isSynchronized) {
            r.lock();
        }
    }

    private void releaseReadLock() {
        if (isSynchronized) {
            r.unlock();
        }
    }

    private void acquireWriteLock() {
        if (isSynchronized) {
            w.lock();
        }
    }

    private void releaseWriteLock() {
        if (isSynchronized) {
            w.unlock();
        }
    }

    /**
     * Checks if this queue is empty
     *
     * @return <code>true</code> if queue is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        acquireReadLock();
        try {
            return isEmpty0();
        } finally {
            releaseReadLock();
        }
    }

    /**
     * This method is only entered when holding a lock.
     */
    private boolean isEmpty0() {
        return ((start == end) && !full);
    }

    /**
     * Checks if this queue is full
     *
     * @return <code>true</code> if queue is full; otherwise <code>false</code>
     */
    public boolean isFull() {
        acquireReadLock();
        try {
            return full;
        } finally {
            releaseReadLock();
        }
    }

    /**
     * Gets the number of contained objects in queue
     *
     * @return The number of contained objects
     */
    public int size() {
        acquireReadLock();
        try {
            if (full) {
                return array.length;
            } else if (isEmpty0()) {
                return 0;
            } else {
                return start - end;
            }
        } finally {
            releaseReadLock();
        }
    }

    /**
     * Enqueues given object to queue's tail
     *
     * @param obj The object to enqueue
     */
    public void enqueue(final T obj) {
        acquireWriteLock();
        try {
            if (!full) {
                array[start = (++start % array.length)] = obj;
            }
            if (start == end) {
                full = true;
            }
        } finally {
            releaseWriteLock();
        }
    }

    /**
     * Dequeues the first object (head) in queue
     *
     * @return The dequeued object
     */
    public T dequeue() {
        acquireWriteLock();
        try {
            if (full) {
                full = false;
            } else if (isEmpty0()) {
                return null;
            }
            final T retval = array[end = (++end % array.length)];
            /*
             * Free reference for garbage collector
             */
            array[end] = null;
            return retval;
        } finally {
            releaseWriteLock();
        }
    }

    /**
     * Peeks (and does not remove) the first object (head) in queue
     *
     * @return The first object
     */
    public T get() {
        acquireReadLock();
        try {
            if (isEmpty0()) {
                return null;
            }
            final int tmp = end;
            final T retval = array[end = (++end % array.length)];
            /*
             * Since we do not remove from queue
             */
            end = tmp;
            return retval;
        } finally {
            releaseReadLock();
        }
    }

}
