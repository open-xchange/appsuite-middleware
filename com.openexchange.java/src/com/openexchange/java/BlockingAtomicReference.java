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
 *    trademarks of the OX Software GmbH. group of companies.
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
     * @param value The value to set; must not be <code>null</code>
     */
    public void set(V value) {
        if (value == null) {
            throw new NullPointerException();
        }

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            reference = value;
            notNull.signal();
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
        if (update == null) {
            throw new NullPointerException();
        }

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (expect == reference) {
                reference = update;
                notNull.signal();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

}
