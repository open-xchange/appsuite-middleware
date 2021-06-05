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

package com.openexchange.database.internal;

import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;

/**
 * {@link CacheLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class CacheLock {

    /**
     * Gets the cache lock backed by given lock instance
     *
     * @param lock The lock to use
     * @return The cache lock
     */
    public static CacheLock cacheLockFor(Lock lock) {
        return null == lock ? null : new CommonCacheLock(lock);
    }

    /**
     * Gets the cache lock backed by given access control instance
     *
     * @param accessControl The access control to use
     * @return The cache lock
     */
    public static CacheLock cacheLockFor(AccessControl accessControl) {
        return null == accessControl ? null : new AccessControlCacheLock(accessControl);
    }

    // ---------------------------------------------------------------

    /**
     * Initializes a new {@link CacheLock}.
     */
    protected CacheLock() {
        super();
    }

    /**
     * Acquires the lock.
     *
     * @throws OXException If interrupted while waiting for lock
     */
    public abstract void lock() throws OXException;

    /**
     * Releases the lock.
     */
    public abstract void unlock();

    // ---------------------------------------------------------------

    private static class CommonCacheLock extends CacheLock {

        private final Lock lock;

        CommonCacheLock(Lock lock) {
            super();
            this.lock = lock;
        }

        @Override
        public void lock() {
            lock.lock();
        }

        @Override
        public void unlock() {
            lock.unlock();
        }
    }

    private static class AccessControlCacheLock extends CacheLock {

        private final AccessControl accessControl;

        AccessControlCacheLock(AccessControl accessControl) {
            super();
            this.accessControl = accessControl;
        }

        @Override
        public void lock() throws OXException {
            try {
                accessControl.acquireGrant();
            } catch (InterruptedException e) {
                // Keep interrupted state
                Thread.currentThread().interrupt();
                throw OXException.general("Interrupted while acquiring grant", e);
            }
        }

        @Override
        public void unlock() {
            accessControl.release();
        }
    }
}
