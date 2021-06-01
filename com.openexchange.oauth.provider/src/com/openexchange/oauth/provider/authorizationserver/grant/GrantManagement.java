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

package com.openexchange.oauth.provider.authorizationserver.grant;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * Service interface to manage OAuth 2.0 grants in cases where the middleware acts as
 * an authorization server.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@SingletonService
public interface GrantManagement {

    /**
     * The default timeout for an generated authorization code in milliseconds.
     */
    public static final long AUTH_CODE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10L);

    /**
     * The default expiration time for a generated access token in milliseconds.
     */
    public static final long DEFAULT_EXPIRATION = TimeUnit.HOURS.toMillis(1L);

    /**
     * Generates a new authorization code that is bound to the given client identifier, redirect URI and scope.
     *
     * @param contextId The context ID
     * @param user The user ID
     * @param clientId The client identifier
     * @param redirectURI The redirect URI
     * @param scope The scope, must have been validated before
     * @param session The session
     * @return A new authorization code
     * @throws OXException If operation fails
     */
    String generateAuthorizationCodeFor(String id, String redirectURI, Scope scope, Session session) throws OXException;

    /**
     * Redeems the passed authorization code for an access token.
     *
     * @param client The client
     * @param redirectURI The redirect URI
     * @param authCode The authorization code
     * @return A newly created access token or <code>null</code> if the code was invalid
     * @throws OXException If redeem operation fails
     */
    Grant redeemAuthCode(Client client, String redirectURI, String authCode) throws OXException;

    /**
     * Redeems the passed authorization code for a new access token. The
     * refresh token itself may also be changed due to security reasons.
     *
     * @param client The client
     * @param refreshToken The refresh token
     * @return The updated grant
     * @throws OXException If redeem operation fails
     */
    Grant redeemRefreshToken(Client client, String refreshToken) throws OXException;
    
    /**
     * Gets the grant associated with specified access token
     *
     * @param accessToken The access token to look-up by
     * @return The associated grant
     * @throws OXException If such a grant cannot be returned
     */
    Grant getGrantByAccessToken(String accessToken) throws OXException;

    /**
     * Revokes a grant by its refresh token.
     *
     * @param refreshToken The token
     * @return <code>true</code> if the grant was revoked, <code>false</code> if no grant existed for the given token
     */
    boolean revokeByRefreshToken(String refreshToken) throws OXException;

    /**
     * Revokes a grant by its access token.
     *
     * @param accessToken The token
     * @return <code>true</code> if the grant was revoked, <code>false</code> if no grant existed for the given token
     */
    boolean revokeByAccessToken(String accessToken) throws OXException;

    /**
     * Gets all grants of a user as client-centric views.
     *
     * @param contextId The contest ID
     * @param userId The user ID
     * @return An immutable iterator of {@link GrantView}s.
     */
    Iterator<GrantView> getGrants(int contextId, int userId) throws OXException;

    /**
     * Revokes all grants of a user to a certain client.
     *
     * @param clientId The client ID
     * @param contextId The context ID
     * @param userId The user ID
     * @throws OXException
     */
    void revokeGrants(String clientId, int contextId, int userId) throws OXException;

    /**
     * Returns the scope provider for the given scope token.
     *
     * @param token The scope token
     * @return The provider or <code>null</code> if none exists for the token
     */
    OAuthScopeProvider getScopeProvider(String token);

}
