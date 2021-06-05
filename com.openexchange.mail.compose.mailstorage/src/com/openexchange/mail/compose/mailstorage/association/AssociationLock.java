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

package com.openexchange.mail.compose.mailstorage.association;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link AssociationLock} - A lock for an obtained association.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class AssociationLock {

    /** The result when lock has been obtained*/
    public static enum LockResult {
        /** Lock has been immediately acquired by thread */
        IMMEDIATE_ACQUISITION,
        /** Lock could <b>not</b> be immediately acquired and thread had to wait until lock became available */
        WAITED_ACQUISITION,;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Lock lock;

    /**
     * Initializes a new {@link AssociationLock}.
     */
    public AssociationLock() {
        super();
        lock = new ReentrantLock();
    }

    /**
     * Acquires the lock.
     *
     * @return The lock result
     */
    public LockResult lock() {
        if (lock.tryLock()) {
            // Lock immediately acquired
            return LockResult.IMMEDIATE_ACQUISITION;
        }

        // Necessarily wait until getting lock
        lock.lock();
        return LockResult.WAITED_ACQUISITION;
    }

    /**
     * Releases the lock.
     */
    public void unlock() {
        lock.unlock();
    }

}
