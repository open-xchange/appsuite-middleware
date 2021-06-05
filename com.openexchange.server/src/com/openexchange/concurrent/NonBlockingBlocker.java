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
