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

package com.openexchange.lock;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * {@link LockService} - Provides exclusive locks for arbitrary identifiers.
 * <p>
 * The locks a re self-managed and therefore are cleansed after certain amount of time (default idle time is 150 seconds).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface LockService {

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

    /**
     * The empty lock, doing nothing on invocations.
     */
    public static final Lock EMPTY_LOCK = new Lock() {

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
    };

    /**
     * Gets the (volatile) lock for given identifier.
     *
     * @param identifier The identifier
     * @return The associated lock
     * @throws OXException If lock cannot be returned
     */
    Lock getLockFor(String identifier) throws OXException;

    /**
     * Gets the (self-cleaning) lock for given identifier.
     * <p>
     * When invoking {@link Lock#unlock()} the lock instance will be removed from this lock service.
     *
     * @param identifier The identifier
     * @return The associated lock
     * @throws OXException If lock cannot be returned
     */
    Lock getSelfCleaningLockFor(String identifier) throws OXException;

    /**
     * Removes the lock for given identifier.
     *
     * @param identifier The identifier
     */
    void removeLockFor(String identifier);

    /**
     * Gets the access control for specified number of permits.
     * <pre>
     * AccessControl accessControl = lockService.getAccessControlFor(...);
     * try {
     *     accessControl.acquireGrant();
     *      ...
     * } catch (InterruptedException e) {
     *     Thread.currentThread().interrupt();
     *     throw ...
     * } finally {
     *    accessControl.close();
     * }
     * </pre>
     *
     * @param identifier The identifier associated with the access control
     * @param permits The number of permits
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The access control
     * @throws OXException If access control cannot be returned
     */
    AccessControl getAccessControlFor(String identifier, int permits, int userId, int contextId) throws OXException;

}
