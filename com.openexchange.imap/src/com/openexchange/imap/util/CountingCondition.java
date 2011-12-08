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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.imap.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

/**
 * {@link CountingCondition}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CountingCondition {

    private final Condition condition;

    private final AtomicInteger counter;

    /**
     * Initializes a new {@link CountingCondition}.
     */
    public CountingCondition(final Condition condition) {
        super();
        this.condition = condition;
        counter = new AtomicInteger(0);
    }

    /**
     * Gets the number of threads currently awaiting on this condition.
     * 
     * @return The number of waiting threads
     */
    public int getCount() {
        return counter.get();
    }

    /**
     * Causes the current thread to wait until it is signalled or {@linkplain Thread#interrupt interrupted}.
     * <p>
     * The lock associated with this {@code Condition} is atomically released and the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until <em>one</em> of four things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #signal} method for this {@code Condition} and the current thread happens to be chosen as
     * the thread to be awakened; or
     * <li>Some other thread invokes the {@link #signalAll} method for this {@code Condition}; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the current thread, and interruption of thread suspension is
     * supported; or
     * <li>A &quot;<em>spurious wakeup</em>&quot; occurs.
     * </ul>
     * <p>
     * In all cases, before this method can return the current thread must re-acquire the lock associated with this condition. When the
     * thread returns it is <em>guaranteed</em> to hold this lock.
     * <p>
     * If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting and interruption of thread suspension is supported,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's interrupted status is cleared. It is not specified, in the first
     * case, whether or not the test for interruption occurs before the lock is released.
     * <p>
     * <b>Implementation Considerations</b>
     * <p>
     * The current thread is assumed to hold the lock associated with this {@code Condition} when this method is called. It is up to the
     * implementation to determine if this is the case and if not, how to respond. Typically, an exception will be thrown (such as
     * {@link IllegalMonitorStateException}) and the implementation must document that fact.
     * <p>
     * An implementation can favor responding to an interrupt over normal method return in response to a signal. In that case the
     * implementation must ensure that the signal is redirected to another waiting thread, if there is one.
     * 
     * @throws InterruptedException If the current thread is interrupted (and interruption of thread suspension is supported)
     */
    public void await() throws InterruptedException {
        counter.incrementAndGet();
        try {
            condition.await();
        } finally {
            counter.decrementAndGet();
        }
    }

    /**
     * Wakes up all waiting threads.
     * <p>
     * If any threads are waiting on this condition then they are all woken up. Each thread must re-acquire the lock before it can return
     * from {@code await}.
     */
    public void signalAll() {
        condition.signalAll();
    }

}
