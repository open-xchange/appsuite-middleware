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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.lock.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    /**
     * Releases this access control.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; background-color:#FFDDDD;">May only be invoked one time per thread!</div>
     * <p>
     *
     * @return <code>true</code> if released; otherwise <code>false</code>
     */
    @Override
    public boolean release() {
        lock.lock();
        try {
            inUse--;
            grants--;

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
