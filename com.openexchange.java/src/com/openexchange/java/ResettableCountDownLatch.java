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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * {@link ResettableCountDownLatch} - Mainly a copy of Doug Lea's great {@link CountDownLatch} implementation, but extended by
 * {@link #reset()} method that allows to reset the latch to its initial state.
 *
 * @author Doug Lea; mainly...
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> {@link #reset()}
 */
public final class ResettableCountDownLatch {

    /**
     * Synchronization control For CountDownLatch. Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 4982264981922014378L;

        final int startCount;

        Sync(int count) {
            this.startCount = count;
            setState(startCount);
        }

        int getCount() {
            return getState();
        }

        @Override
        public int tryAcquireShared(int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        @Override
        public boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0) {
                    return false;
                }
                int nextc = c - 1;
                if (compareAndSetState(c, nextc)) {
                    return nextc == 0;
                }
            }
        }

        public void reset() {
            setState(startCount);
        }
    }

    private final Sync sync;

    /**
     * Constructs a {@code CountDownLatch} initialized with the given count.
     *
     * @param count the number of times {@link #countDown} must be invoked before threads can pass through {@link #await}
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public ResettableCountDownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        this.sync = new Sync(count);
    }

    /**
     * Causes the current thread to wait until the latch has counted down to zero, unless the thread is {@linkplain Thread#interrupt
     * interrupted}.
     * <p>
     * If the current count is zero then this method returns immediately.
     * <p>
     * If the current count is greater than zero then the current thread becomes disabled for thread scheduling purposes and lies dormant
     * until one of two things happen:
     * <ul>
     * <li>The count reaches zero due to invocations of the {@link #countDown} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the current thread.
     * </ul>
     * <p>
     * If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's interrupted status is cleared.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * Resets the latch to its initial state.
     */
    public void reset() {
        sync.reset();
    }

    /**
     * Causes the current thread to wait until the latch has counted down to zero, unless the thread is {@linkplain Thread#interrupt
     * interrupted}, or the specified waiting time elapses.
     * <p>
     * If the current count is zero then this method returns immediately with the value {@code true}.
     * <p>
     * If the current count is greater than zero then the current thread becomes disabled for thread scheduling purposes and lies dormant
     * until one of three things happen:
     * <ul>
     * <li>The count reaches zero due to invocations of the {@link #countDown} method; or
     * <li>Some other thread {@linkplain Thread#interrupt interrupts} the current thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     * <p>
     * If the count reaches zero then the method returns with the value {@code true}.
     * <p>
     * If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's interrupted status is cleared.
     * <p>
     * If the specified waiting time elapses then the value {@code false} is returned. If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if the count reached zero and {@code false} if the waiting time elapsed before the count reached zero
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * Decrements the count of the latch, releasing all waiting threads if the count reaches zero.
     * <p>
     * If the current count is greater than zero then it is decremented. If the new count is zero then all waiting threads are re-enabled
     * for thread scheduling purposes.
     * <p>
     * If the current count equals zero then nothing happens.
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * Returns the current count.
     * <p>
     * This method is typically used for debugging and testing purposes.
     *
     * @return the current count
     */
    public long getCount() {
        return sync.getCount();
    }

    /**
     * Returns a string identifying this latch, as well as its state. The state, in brackets, includes the String {@code "Count ="} followed
     * by the current count.
     *
     * @return a string identifying this latch, as well as its state
     */
    @Override
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
