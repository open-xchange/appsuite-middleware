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

package com.openexchange.tools.oxfolder.permissionLoader;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Gate} - A thread gate in either unbounded or bounded manner.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Gate {

    private static enum GateState {
        OPEN, CLOSED;
    }

    private static interface PassChecker {

        boolean tryPass();

        void pass() throws InterruptedException;

        void signalDone();
    }

    private static final class InfinitePassChecker implements PassChecker {

        /**
         * Initializes a new {@link Gate.InfinitePassChecker}.
         */
        public InfinitePassChecker() {
            super();
        }

        @Override
        public void pass() {
            // Nope
        }

        @Override
        public void signalDone() {
            // Nope
        }

        @Override
        public boolean tryPass() {
            return true;
        }
    }

    private static final class LimitedPassChecker implements PassChecker {

        private final Semaphore semaphore;

        /**
         * Initializes a new {@link Gate.SemaphorePassChecker}.
         */
        public LimitedPassChecker(final int permits) {
            super();
            semaphore = new Semaphore(permits);
        }

        @Override
        public void pass() throws InterruptedException {
            semaphore.acquire();
        }

        @Override
        public boolean tryPass() {
            return semaphore.tryAcquire();
        }

        @Override
        public void signalDone() {
            semaphore.release();
        }

    }

    private transient final AtomicReference<GateState> gate;

    private transient final PassChecker passChecker;

    private transient final Lock lock;

    private transient final Condition available;

    /**
     * Initializes a new closed {@link Gate}.
     *
     * @param maxParallelPasses A positive integer to restrict max. number of concurrent passers; otherwise zero or a negative integer for
     *            infinite passers
     */
    public Gate(final int maxParallelPasses) {
        super();
        gate = new AtomicReference<GateState>(GateState.CLOSED);
        passChecker = maxParallelPasses > 0 ? new LimitedPassChecker(maxParallelPasses) : new InfinitePassChecker();
        lock = new ReentrantLock();
        available = lock.newCondition();
    }

    /**
     * Passes this gate. The calling thread waits if gate is closed or max. number of allowed concurrent passers is exceeded.
     * <p>
     * It is recommended practice to <em>always</em> immediately follow a call to {@code pass} with a {@code try} block, most typically in a
     * before/after construction such as:
     *
     * <pre>
     *  public void m() {
     *      Gate gate = ...;
     *      gate.pass();  // Awaits until allowed to pass the gate
     *      try {
     *          // ... method body
     *      } finally {
     *          gate.signalDone()
     *      }
     *  }
     * </pre>
     *
     * @throws InterruptedException If calling thread is interrupted while awaiting to pass this gate
     */
    public void pass() throws InterruptedException {
        if (GateState.CLOSED == gate.get()) {
            final Lock lock = this.lock;
            lock.lock();
            try {
                available.await();
            } finally {
                lock.unlock();
            }
        }
        passChecker.pass();
    }

    /**
     * Tries to pass this gate and does not wait if not immediately able to do so.
     * <p>
     * It is recommended practice to <em>always</em> immediately follow a call to {@code tryPass} with a {@code try} block, most typically
     * in a before/after construction such as:
     *
     * <pre>
     * public void m() {
     *  Gate gate = ...;
     *  if (gate.tryPass()) {  // True if allowed to pass the gate
     *      try {
     *          // ... method body
     *      } finally {
     *          gate.signalDone()
     *      }
     *  }
     * }
     * </pre>
     *
     * @return <code>true</code> if calling thread can immediately pass the gate; otherwise <code>false</code>.
     */
    public boolean tryPass() {
        if (GateState.CLOSED == gate.get()) {
            return false;
        }
        return passChecker.tryPass();
    }

    /**
     * Signals that a passer finished its task (to signal that to other possibly waiting threads).
     */
    public void signalDone() {
        passChecker.signalDone();
    }

    /**
     * Closes this gate. Threads which already passed the gate are allowed to terminate. Newly arriving threads are prevented from passing
     * this gate.
     *
     * @return <code>true</code> if caller closed the gate; otherwise <code>false</code> if already closed
     */
    public boolean close() {
        GateState gateState;
        do {
            gateState = gate.get();
            if (GateState.CLOSED == gateState) {
                // Already closed
                return false;
            }
        } while (!gate.compareAndSet(gateState, GateState.CLOSED));
        return true;
    }

    /**
     * Opens this gate. Possibly waiting threads are notified to pass this gate.
     *
     * @return <code>true</code> if caller opened the gate; otherwise <code>false</code> if already open
     */
    public boolean open() {
        GateState gateState;
        do {
            gateState = gate.get();
            if (GateState.OPEN == gateState) {
                // Already open
                return false;
            }
        } while (!gate.compareAndSet(gateState, GateState.OPEN));
        // Notify OPEN state
        final Lock lock = this.lock;
        lock.lock();
        try {
            available.signalAll();
        } finally {
            lock.unlock();
        }
        return true;
    }

}
