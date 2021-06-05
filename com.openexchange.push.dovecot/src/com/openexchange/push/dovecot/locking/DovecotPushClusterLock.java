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

package com.openexchange.push.dovecot.locking;

import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;


/**
 * {@link DovecotPushClusterLock}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DovecotPushClusterLock {

    /**
     * The default timeout for an acquired lock in milliseconds.
     */
    public static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10L);

    public static enum Type {
        HAZELCAST("hz"),
        DATABASE("db"),
        NONE("none"),
        ;

        private final String configName;

        private Type(String configName) {
            this.configName = configName;
        }

        /**
         * Gets the configName
         *
         * @return The configName
         */
        public String getConfigName() {
            return configName;
        }

        public static Type parse(String configName) {
            for (Type t : values()) {
                if (t.getConfigName().equalsIgnoreCase(configName)) {
                    return t;
                }
            }

            return null;
        }
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
     * @return <code>true</code> if lock was acquired; otherwise <code>false</code> if another one acquired the lock before
     * @throws OXException
     */
    boolean acquireLock(SessionInfo sessionInfo) throws OXException;

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
