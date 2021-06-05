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

package com.openexchange.push.imapidle.locking;

import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;


/**
 * {@link ImapIdleClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ImapIdleClusterLock {

    /**
     * The default timeout (10 minutes) for an acquired cluster lock in milliseconds.
     */
    public static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10L);

    /** The cluster lock type */
    public static enum Type {
        HAZELCAST,
        DATABASE,
        LOCAL,
        NONE;
    }

    /** Signals the result of an acquisition attempt for a cluster lock */
    public static enum AcquisitionResult {
        /**
         * The was no such cluster lock, hence successfully acquired
         */
        ACQUIRED_NEW,
        /**
         * The existent cluster lock timed out, hence successfully acquired
         */
        ACQUIRED_TIMED_OUT,
        /**
         * The session associated with existent cluster lock is gone, hence successfully acquired
         */
        ACQUIRED_NO_SUCH_SESSION,
        /**
         * The cluster lock is still held by another listener
         */
        NOT_ACQUIRED;
    }

    /**
     * Gets the cluster lock type
     *
     * @return The type
     */
    Type getType();

    /**
     * Attempts to acquires the lock for given user
     *
     * @param sessionInfo The associated session
     * @return The acquire result
     * @throws OXException
     */
    AcquisitionResult acquireLock(SessionInfo sessionInfo) throws OXException;

    /**
     * Refreshed the lock for given user.
     *
     * @param sessionInfo The associated session
     * @throws OXException If refresh operation fails
     */
    void refreshLock(SessionInfo sessionInfo) throws OXException;

    /**
     * Releases the possibly held lock for given user.
     *
     * @param sessionInfo The associated session
     * @throws OXException If release operation fails
     */
    void releaseLock(SessionInfo sessionInfo) throws OXException;

}
