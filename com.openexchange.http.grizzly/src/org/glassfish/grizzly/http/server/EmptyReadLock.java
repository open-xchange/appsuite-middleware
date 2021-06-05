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

package org.glassfish.grizzly.http.server;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link EmptyReadLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class EmptyReadLock extends java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock {

    private static final long serialVersionUID = 6392600524153514128L;

    private static final EmptyReadLock INSTANCE = new EmptyReadLock();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static EmptyReadLock getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link EmptyReadLock}.
     */
    private EmptyReadLock() {
        super(new ReentrantReadWriteLock());
    }

    @Override
    public void unlock() {
        // ignore
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return true;
    }

    @Override
    public boolean tryLock() {
        return true;
    }

    @Override
    public Condition newCondition() {
        return EMPTY_CONDITION;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // ignore
    }

    @Override
    public void lock() {
        // ignore
    }

    static final Condition EMPTY_CONDITION = new Condition() {

        @Override
        public void signalAll() {
            // Nothing to do
        }

        @Override
        public void signal() {
            // Nothing to do
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            return true;
        }

        @Override
        public void awaitUninterruptibly() {
            // Nothing to do
        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return 0;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void await() throws InterruptedException {
            // Nothing to do
        }
    };

}
