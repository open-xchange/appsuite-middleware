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

package com.openexchange.oauth.provider.impl.grant;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.impl.tools.UserizedToken;


/**
 * {@link OAuthGrantStorage}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface OAuthGrantStorage {

    /**
     * The max. number of grants a client may occupy for one user
     */
    public static final int MAX_GRANTS_PER_CLIENT = 10;

    /**
     * Saves the given grant. If the number of existing grants for the according
     * client-user-combination is equals {@link #MAX_GRANTS_PER_CLIENT}, the oldest
     * grant is removed to enforce this limit.
     *
     * @param grant The grant to save
     * @throws OXException When saving fails
     */
    public void saveGrant(StoredGrant grant) throws OXException;

    /**
     * Updates the grant belonging to the passed refresh token. The refresh token is overriden by the
     * one stored in the grant object during the update.
     *
     * @param refreshToken The refresh token to identify the grant
     * @param grant The updated grant data
     * @throws OXException When updating fails
     */
    public void updateGrant(UserizedToken refreshToken, StoredGrant grant) throws OXException;

    public void deleteGrantsByClientId(String clientId) throws OXException;

    public StoredGrant getGrantByAccessToken(UserizedToken accessToken) throws OXException;

    public StoredGrant getGrantByRefreshToken(UserizedToken refreshToken) throws OXException;

    /**
     * Counts all grants for distinct clients of a given user.
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @return The number of grants (>= 0)
     * @throws OXException
     */
    public int countDistinctGrants(int contextId, int userId) throws OXException;

    /**
     * Deletes a grant by its refresh token
     *
     * @param refreshToken The token
     * @return <code>true</code> if the grant was revoked, <code>false</code> if no grant existed for the given token
     * @throws OXException
     */
    public boolean deleteGrantByRefreshToken(UserizedToken refreshToken) throws OXException;

    /**
     * Deletes a grant by its access token
     *
     * @param accessToken The token
     * @return <code>true</code> if the grant was revoked, <code>false</code> if no grant existed for the given token
     * @throws OXException
     */
    public boolean deleteGrantByAccessToken(UserizedToken accessToken) throws OXException;

    /**
     * Gets all stored grants for a user.
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @return The list of grants
     * @throws OXException
     */
    public List<StoredGrant> getGrantsForUser(int contextId, int userId) throws OXException;

    /**
     * Deletes all grants for a user that belong to a certain client.
     *
     * @param clientId The client ID
     * @param contextId The context ID
     * @param userId The user ID
     * @throws OXException
     */
    public void deleteGrantsByClientAndUser(String clientId, int contextId, int userId) throws OXException;

}
