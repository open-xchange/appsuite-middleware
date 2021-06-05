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

package com.openexchange.java;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link BlockingAtomicReference} - An atomic reference with support for blocking take.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class BlockingAtomicReference<V> {

    /** Main lock guarding all access */
    final ReentrantLock lock;

    /** Condition for waiting takes */
    private final Condition notNull;

    /** The wrapped reference */
    private V reference;

    /**
     * Initializes a new {@link BlockingAtomicReference}.
     */
    public BlockingAtomicReference() {
        this(null);
    }

    /**
     * Initializes a new {@link BlockingAtomicReference}.
     */
    public BlockingAtomicReference(V reference) {
        super();
        this.reference = reference;
        lock = new ReentrantLock();
        notNull = lock.newCondition();
    }

    /**
     * Gets this reference's value, waiting if necessary for a non-null value to become available.
     *
     * @return The value
     * @throws InterruptedException If interrupted while waiting
     */
    public V get() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (reference == null) {
                notNull.await();
            }
            return reference;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets this reference's value, waiting if necessary for a non-null value to become available.
     *
     * @return The value
     */
    public V getUninterruptibly() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            while (reference == null) {
                notNull.awaitUninterruptibly();
            }
            return reference;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets this reference's value, waiting up to the specified wait time if necessary for a non-null value to become available.
     *
     * @param timeout How long to wait before giving up, in units of {@code unit}
     * @param unit A {@code TimeUnit} determining how to interpret the {@code timeout} parameter
     * @return The value or <code>null</code> if the specified waiting time elapse
     * @throws InterruptedException If interrupted while waiting
     */
    public V get(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (reference == null) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notNull.awaitNanos(nanos);
            }
            return reference;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Peeks the current value w/o waiting.
     *
     * @return The value; might be <code>null</code>
     */
    public V peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return reference; // null if not set
        } finally {
            lock.unlock();
        }
    }

    /**
     * (Atomically) Sets the given value for this reference.
     *
     * @param value The value to set; may be <code>null</code>
     */
    public void set(V value) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            reference = value;
            if (value != null) {
                notNull.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically sets the value to the given updated value if the current value {@code ==} the expected value.
     *
     * @param expect The expected value
     * @param update The new value; must not be <code>null</code>
     * @return <code>true</code> if successful. <code>false</code> indicates that the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(V expect, V update) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (expect == reference) {
                reference = update;
                if (update != null) {
                    notNull.signal();
                }
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

}
