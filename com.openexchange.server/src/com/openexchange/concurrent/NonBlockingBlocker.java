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

package com.openexchange.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link NonBlockingBlocker} - Non-blocking/wait-free reentrant blocker; also useful to wrap an existing {@link Runnable runnable}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NonBlockingBlocker implements Blocker, Runnable {

    private static final Object PRESENT = new Object();

    private volatile Runnable runnable;

    private final Map<Thread, Object> running;

    private final AtomicInteger sync;

    private final AtomicInteger mutex;

    private volatile Thread owner;

    /**
     * Initializes a new {@link NonBlockingBlocker}.
     */
    public NonBlockingBlocker() {
        this(null);
    }

    /**
     * Initializes a new {@link NonBlockingBlocker} wrapping given {@link Runnable runnable}.
     *
     * @param runnable The runnable to block
     */
    public NonBlockingBlocker(final Runnable runnable) {
        super();
        sync = new AtomicInteger();
        mutex = new AtomicInteger();
        running = new ConcurrentHashMap<Thread, Object>(4, 0.9f, 1);
        this.runnable = runnable;
    }

    private int lock() {
        int value;
        // Wait for an even value
        while (((value = mutex.get()) & 1) == 1) {
            // nothing in body
        }
        // Set blocked: Atomically increment by 1 by CAS operation. Wait for an even value if CAS operation fails.
        while (!mutex.compareAndSet(value, value | 1)) {
            while (((value = mutex.get()) & 1) == 1) {
                // nothing
            }
        }
        return value | 1;
    }

    private void unlock(final int value) {
        mutex.set(value + 1);
    }

    /*-
     * In opposite to NonBlockingSynchronizer enabling the 1 bit and disabling the 1 bit take place in
     * different methods. blocks() sets the 1 bit. While set, no other thread is able to acquire().
     * The unblock() methods disables the 1 bit, then allowing other threads to acquire.
     */

    @Override
    public void block() {
        final Thread cur = Thread.currentThread();
        if (cur == owner) {
            // This thread already blocks
            return;
        }
        final int lock = lock();
        try {
            // Already blocked?
            int value;
            while (((value = sync.get()) & 1) == 1) {
                // Nothing
            }
            // Set blocked: Atomically increment by 1 by CAS operation. Wait for an even value if CAS operation fails.
            while (!sync.compareAndSet(value, value + 1)) {
                while (((value = sync.get()) & 1) == 1) {
                    // Nothing
                }
            }
            owner = cur;
            // Wait for other threads leaving
            while (!running.isEmpty()) {
                // Nothing to do
            }
        } finally {
            unlock(lock);
        }
    }

    @Override
    public void acquire() {
        final Thread currentThread = Thread.currentThread();
        if (currentThread == owner) {
            // Owning thread!
            return;
        }
        if (running.containsKey(currentThread)) {
            // Reentrant: Already acquired
            return;
        }
        final int lock = lock();
        try {
            int save;
            do {
                while (((save = sync.get()) & 1) == 1) {
                    // Nothing
                }
            } while (save != sync.get());
            running.put(currentThread, PRESENT);
        } finally {
            unlock(lock);
        }
    }

    @Override
    public void unblock() {
        if (null == owner || Thread.currentThread() != owner) {
            throw new IllegalMonitorStateException(new StringBuilder(32).append("Thread ").append(Thread.currentThread().getName()).append(
                " does not own this blocker").toString());
        }
        // Set unblocked
        sync.getAndIncrement();
        owner = null;
    }

    @Override
    public void release() {
        final Thread currentThread = Thread.currentThread();
        if (currentThread == owner) {
            // Owning thread!
            return;
        }
        running.remove(currentThread);
    }

    @Override
    public void run() {
        acquire();
        try {
            if (runnable != null) {
                runnable.run();
            }
        } finally {
            release();
        }
    }

    /**
     * Checks if current thread holds this blocker.
     *
     * @return <code>true</code> if current thread holds this blocker; otherwise <code>false</code>
     */
    public boolean holdsBlock() {
        return (owner == Thread.currentThread());
    }

}
