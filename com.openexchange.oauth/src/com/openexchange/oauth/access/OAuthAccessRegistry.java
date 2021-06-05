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

package com.openexchange.oauth.access;

import java.util.concurrent.Callable;
import com.openexchange.exception.OXException;

/**
 * {@link OAuthAccessRegistry} - A registry for in-use OAuth accesses by a certain user.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface OAuthAccessRegistry {

    /**
     * Adds the specified {@link OAuthAccess} to the registry if there is none associated with specified user, yet.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param oauthAccess The {@link OAuthAccess}
     * @return The previous associated {@link OAuthAccess}, or <code>null</code> if there was none (and specified instance was added)
     */
    OAuthAccess addIfAbsent(int contextId, int userId, int oauthAccountId, OAuthAccess oauthAccess);

    /**
     * Adds the specified {@link OAuthAccess} to the registry if there is none associated with specified user, yet.
     * <p>
     * Executes the given <code>executeIfAdded</code> instance (if not <code>null</code>) in case the given {@link OAuthAccess} instance is successfully added to this registry.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param oauthAccess The {@link OAuthAccess}
     * @param executeIfAdded An optional task to perform in case given {@link OAuthAccess} instance is added
     * @return The previous associated {@link OAuthAccess}, or <code>null</code> if there was none (and specified instance was added)
     * @throws OXException If executing the optional task fails
     */
    <V> OAuthAccess addIfAbsent(int contextId, int userId, int oauthAccountId, OAuthAccess oauthAccess, Callable<V> executeIfAdded) throws OXException;

    /**
     * Checks the presence of the {@link OAuthAccess} associated with the given user/context/account tuple
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true if such an {@link OAuthAccess} is present; <code>false</code> otherwise
     */
    boolean contains(int contextId, int userId, int oauthAccountId);

    /**
     * Retrieves the {@link OAuthAccess} associated with the given user/context/account tuple
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The {@link OAuthAccess} that is associated with the tuple, or <code>null</code> if none exists
     */
    OAuthAccess get(int contextId, int userId, int oauthAccountId);

    /**
     * Removes the {@link OAuthAccess} associated with the specified user/context tuple, if no more accesses for that tuple are present
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if an {@link OAuthAccess} for the specified tuple was found and removed; <code>false</code> otherwise
     */
    boolean removeIfLast(int contextId, int userId);

    /**
     * Purges the {@link OAuthAccess} associated with the specified user/context/account tuple.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if an {@link OAuthAccess} for the specified tuple was found and purged; <code>false</code> otherwise
     */
    boolean purgeUserAccess(int contextId, int userId, int oauthAccountId);

    /**
     * Returns the service identifier of this registry
     *
     * @return the service identifier of this registry
     */
    String getServiceId();

}
