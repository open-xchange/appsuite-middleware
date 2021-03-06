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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link NonBlockingSynchronizer} - Non-blocking reentrant synchronizer; also useful to wrap an existing {@link Runnable runnable}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NonBlockingSynchronizer implements Synchronizer, Runnable {

    private static final Object PRESENT = new Object();

    private volatile Runnable runnable;

    private volatile boolean obtainLock;

    private final AtomicInteger running;

    private final AtomicInteger writeCounter;

    private final Lock runLock;

    private final Map<Thread, Object> reentrant;

    /**
     * Initializes a new {@link NonBlockingSynchronizer}.
     */
    public NonBlockingSynchronizer() {
        this(null);
    }

    /**
     * Initializes a new {@link NonBlockingSynchronizer} wrapping given {@link Runnable runnable}.
     *
     * @param runnable The runnable to synchronize
     */
    public NonBlockingSynchronizer(final Runnable runnable) {
        super();
        writeCounter = new AtomicInteger();
        running = new AtomicInteger();
        runLock = new ReentrantLock();
        this.runnable = runnable;
        reentrant = new ConcurrentHashMap<Thread, Object>();
    }

    /**
     * Gets the runnable.
     *
     * @return The runnable
     */
    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Sets the runnable.
     *
     * @param runnable The runnable to set
     */
    public void setRunnable(final Runnable runnable) {
        this.runnable = runnable;
    }

    /*-
     * In opposite to NonBlockingBlocker enabling the 1 bit and disabling the 1 bit take place
     * in same setSynchronized() method. The setSynchronized() method just switches whether a
     * thread which invokes acquire() must obtain a mutual-exclusive lock or not.
     */

    /**
     * Sets whether to synchronize access or not.
     * <p>
     * Must not be called from a thread which already acquired this synchronizer.
     *
     * @param synchronize <code>true</code> to synchronize access; otherwise <code>false</code>
     * @return This non-blocking synchronizer with new synchronize policy applied
     */
    public Runnable setSynchronized(final boolean synchronize) {
        if (reentrant.containsKey(Thread.currentThread())) {
            throw new IllegalStateException("Current thread acquired synchronizer, but wants to alter sync mode");
        }
        int value = writeCounter.get();
        while ((value & 1) == 1) {
            value = writeCounter.get();
        }
        while (!writeCounter.compareAndSet(value, value + 1)) {
            while (((value = writeCounter.get()) & 1) == 1) {
                // Nothing
            }
        }
        while (running.get() > 0) {
            // Nothing to do
        }
        obtainLock = synchronize;
        writeCounter.getAndIncrement();
        return this;
    }

    @Override
    public Lock acquire() {
        if (reentrant.containsKey(Thread.currentThread())) {
            // Reentrant thread
            return null;
        }
        int save = 0;
        Lock lock = null;
        do {
            while (((save = writeCounter.get()) & 1) == 1) {
                // Nothing
            }
            lock = obtainLock ? runLock : null;
        } while (save != writeCounter.get());
        running.incrementAndGet();
        reentrant.put(Thread.currentThread(), PRESENT);
        if (null != lock) {
            lock.lock();
        }
        return lock;
    }

    @Override
    public void release(final Lock lock) {
        if (null != lock) {
            lock.unlock();
        }
        reentrant.remove(Thread.currentThread());
        running.decrementAndGet();
    }

    /**
     * Executes wrapped {@link Runnable runnable} according to applied synchronization policy.
     * <p>
     * This method does nothing if runnable is <code>null</code>.
     */
    @Override
    public void run() {
        final Lock lock = acquire();
        try {
            if (null != runnable) {
                runnable.run();
            }
        } finally {
            release(lock);
        }
    }

    @Override
    public void synchronize() {
        setSynchronized(true);
    }

    @Override
    public void unsynchronize() {
        setSynchronized(false);
    }

}
