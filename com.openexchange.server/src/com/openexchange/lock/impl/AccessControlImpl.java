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

package com.openexchange.lock.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;

/**
 * {@link AccessControlImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class AccessControlImpl implements AccessControl {

    private static final ConcurrentMap<Key, AccessControlImpl> CONTROLS = new ConcurrentHashMap<>(512);

    /**
     * Gets the associated access control for given session
     *
     * @param id The identifier
     * @param max The max. grants
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The access control
     * @throws OXException If access control cannot be returned
     */
    public static AccessControlImpl getAccessControl(String id, int max, int userId, int contextId) throws OXException {
        Key key = Key.newKey(id, userId, contextId);

        AccessControlImpl accessControl = null;
        while (null == accessControl) {
            accessControl = CONTROLS.get(key);
            if (null == accessControl) {
                AccessControlImpl newAccessControl = new AccessControlImpl(max, key);
                accessControl = CONTROLS.putIfAbsent(key, newAccessControl);
                if (null == accessControl) {
                    // Current thread grabbed the slot
                    accessControl = newAccessControl;
                } else if (accessControl.isNotAlive()) {
                    // No more alive... Retry
                    accessControl = null;
                } else if (accessControl.maxAccess != max) {
                    throw OXException.general("Access control requested with different max. number of grants");
                }
            } else if (accessControl.isNotAlive()) {
                // No more alive... Retry
                accessControl = null;
            } else if (accessControl.maxAccess != max) {
                throw OXException.general("Access control requested with different max. number of grants");
            }
        }
        // Leave...
        return accessControl;
    }

    // -------------------------------------------------------------------------------------------------------------

    private final Condition accessible;
    private final Key key;
    private final Lock lock;
    private final int maxAccess;
    private int inUse;
    private int grants;

    /**
     * Initializes a new {@link AccessControl}.
     */
    private AccessControlImpl(int maxAccess, Key key) {
        super();
        this.maxAccess = maxAccess;
        this.key = key;
        lock = new ReentrantLock();
        accessible = lock.newCondition();
        inUse = 1; // Apparently... the creating thread
        grants = 0;
    }

    /**
     * Acquires a grant from this access control; waiting for an available grant if needed.
     *
     * @throws InterruptedException If interrupted while waiting for a grant
     */
    @Override
    public void acquireGrant() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (grants >= maxAccess) {
                accessible.await();
            }
            grants++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryAcquireGrant() {
        lock.lock();
        try {
            if (grants >= maxAccess) {
                return false;
            }
            grants++;
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryAcquireGrant(long timeout, TimeUnit unit) throws InterruptedException {
        long start = System.nanoTime();
        long maxTime = start + unit.toNanos(timeout);
        if (lock.tryLock(maxTime - start, TimeUnit.NANOSECONDS)) {
            try {
                long timeLeft = maxTime - System.nanoTime();
                while (grants >= maxAccess) {
                    if (timeLeft <= 0l) {
                        return false;
                    }

                    timeLeft = accessible.awaitNanos(timeLeft);
                }
                grants++;
                return true;
            } finally {
                lock.unlock();
            }
        }

        return false;
    }

    /**
     * Checks if this access control is not alive
     *
     * @return <code>true</code> if not alive; otherwise <code>false</code> (if still alive)
     */
    private boolean isNotAlive() {
        return !isAlive();
    }

    /**
     * Checks if this access control is still alive
     *
     * @return <code>true</code> if alive; otherwise <code>false</code>
     */
    private boolean isAlive() {
        lock.lock();
        try {
            if (inUse <= 0) {
                return false;
            }

            inUse++;
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean release() {
        return release(true);
    }

    @Override
    public boolean release(boolean acquired) {
        lock.lock();
        try {
            inUse--;
            if (acquired) {
                grants--;
            }

            if (inUse == 0) {
                // The last one to release
                CONTROLS.remove(key);
                return true;
            }

            accessible.signal();
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        release();
    }

    // -------------------------------------------------------------------------------------------------------------

    private static final class Key {

        static Key newKey(String id, int userId, int contextId) {
            return new Key(id, userId, contextId);
        }

        private final int contextId;
        private final int userId;
        private final int hash;
        private final String id;

        /**
         * Initializes a new {@link Key}.
         *
         * @param userId The user identifier
         * @param contextId The context identifier
         */
        Key(String id, int userId, int contextId) {
            super();
            this.id = id;
            this.userId = userId;
            this.contextId = contextId;

            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + userId;
            result = prime * result + (null == id ? 0 : id.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }
    }

}
