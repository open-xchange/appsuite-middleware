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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagement;
import com.openexchange.oauth.provider.grant.GrantView;
import com.openexchange.oauth.provider.grant.OAuthGrant;
import com.openexchange.oauth.provider.scope.OAuthScopeProvider;
import com.openexchange.oauth.provider.scope.Scope;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * The management service to be used by the OAuth provider implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 * @see OAuthResourceService
 */
@SingletonService
public interface OAuthProviderService {

    // -------------------------------------- Client Handling -------------------------------------- \\

    /**
     * The max. number of clients that a user is allowed to grant access to
     */
    public static final int MAX_CLIENTS_PER_USER = 50;

    /**
     * Gets the management instance for clients.
     *
     * @return The management instance
     */
    ClientManagement getClientManagement();

    // -------------------------------- Authorization Code Handling -------------------------------- \\
    //  Manages authorization codes generated for/redeemed by OAuth client applications.

    /**
     * The default timeout for an generated authorization code in milliseconds.
     */
    public static final long AUTH_CODE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10L);

    /**
     * Generates a new authorization code that is bound to the given client identifier, redirect URI and scope.
     *
     * @param contextId The context ID
     * @param user The user ID
     * @param clientId The client identifier
     * @param redirectURI The redirect URI
     * @param scope The scope, must have been validated before
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return A new authorization code
     * @throws OXException If operation fails
     */
    String generateAuthorizationCodeFor(String clientId, String redirectURI, Scope scope, int userId, int contextId) throws OXException;

    /**
     * Redeems the passed authorization code for an access token.
     *
     * @param client The client
     * @param redirectURI The redirect URI
     * @param authCode The authorization code
     * @return A newly created access token or <code>null</code> if the code was invalid
     * @throws OXException If redeem operation fails
     */
    OAuthGrant redeemAuthCode(Client client, String redirectURI, String authCode) throws OXException;

    // ------------------------------------ Grant Handling ----------------------------------- \\


    /**
     * Redeems the passed authorization code for a new access token. The
     * refresh token itself may also be changed due to security reasons.
     *
     * @param client The client
     * @param refreshToken The refresh token
     * @return The updated grant
     * @throws OXException If redeem operation fails
     */
    OAuthGrant redeemRefreshToken(Client client, String refreshToken) throws OXException;

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

    // --------------------------------------- Helper methods -------------------------------------- \\

    /**
     * Checks if the given scope is valid in terms of syntax and server-side provided scopes.
     *
     * @param scope The scope to check
     * @return <code>true</code> if the scope contains at least one token and all tokens belong
     * to registered providers. Otherwise <code>false</code>.
     */
    boolean isValidScope(String scope);

    /**
     * Gets the scope provider for a given scope ID (must be non-qualified).
     *
     * @param token The scope token
     * @return The provider or <code>null</code> if none can be found
     */
    OAuthScopeProvider getScopeProvider(String token);

}
