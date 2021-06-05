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

package com.openexchange.sessiond.impl;

/**
 *
 * {@link SessiondConfigInterface}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public interface SessiondConfigInterface {

    long getSessionContainerTimeout();

    long getLongTermSessionContainerTimeout();

    int getNumberOfSessionContainers();

    int getMaxSessions();

    int getMaxSessionsPerClient();

    long getLifeTime();

    long getLongLifeTime();

    long getRandomTokenTimeout();

    int getNumberOfLongTermSessionContainers();

    /**
     * Whether to enforce putting sessions into session storage asynchronously.
     *
     * @return <code>true</code> for async put; otherwise <code>false</code>
     */
    boolean isAsyncPutToSessionStorage();

    /**
     * Gets a key to encrypt passwords when putting session into storage.
     *
     * @return The obfuscation key
     */
    String getObfuscationKey();

    /**
     * Gets a value indicating whether sessions that were rotated out of the containers are implicitly also removed from the session
     * storage or not. This may not be desired if a session is still in use by another node in the cluster.
     *
     * @return <code>true</code> if a session timeout should also remove the session from the distributed storage, <code>false</code>, otherwise
     */
    boolean isRemoveFromSessionStorageOnTimeout();

}
