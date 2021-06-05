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

package com.openexchange.twitter;

import com.openexchange.exception.OXException;

/**
 * {@link TwitterService} - The <a href="http://twitter.com/">twitter</a> service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface TwitterService {

    /**
     * Gets the access token for specified user credentials.
     *
     * @param twitterId The twitter id
     * @param password The twitter password
     * @return The access token for specified user credentials
     * @throws OXException If OAuth twitter access token cannot be returned
     */
    public TwitterAccessToken getTwitterAccessToken(String twitterId, String password) throws OXException;

    /**
     * Gets the OAuth twitter access instance for the authenticating user.
     *
     * @param token The twitter token
     * @param tokenSecret The twitter token secret
     * @return The authenticated twitter access
     * @throws OXException If OAuth twitter access cannot be returned
     * @see #getTwitterAccessToken(String, String)
     */
    public TwitterAccess getOAuthTwitterAccess(String token, String tokenSecret) throws OXException;

    /**
     * Gets the twitter access instance for the authenticating user.
     *
     * @param twitterId The twitter id
     * @param password The twitter password
     * @return The authenticated twitter access
     * @deprecated Use {@link #getOAuthTwitterAccess(String, String)} instead
     */
    @Deprecated
    public TwitterAccess getTwitterAccess(String twitterId, String password);

    /**
     * Gets an unauthenticated twitter access instance.
     *
     * @return An unauthenticated twitter access
     */
    public TwitterAccess getUnauthenticatedTwitterAccess();

    /**
     * Creates a new instance of {@link Paging}.
     *
     * @return A new instance of {@link Paging}
     */
    public Paging newPaging();

}
