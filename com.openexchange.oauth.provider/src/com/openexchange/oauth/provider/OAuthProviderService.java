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

import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * {@link OAuthProviderService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface OAuthProviderService {

    // -------------------------------------- Client Handling -------------------------------------- \\

    /**
     * Gets the client identified by the given identifier.
     *
     * @param clientId The clients identifier
     * @return The client or <code>null</code> if there is no such client
     * @throws OXException If operation fails
     */
    Client getClientById(String clientId) throws OXException;

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param clientData The client data to create the client from
     * @return The newly created client
     * @throws OXException If create operation fails
     */
    Client registerClient(ClientData clientData) throws OXException;

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @return The updated client
     * @throws OXException If update operation fails
     */
    Client updateClient(String clientId, ClientData clientData) throws OXException;

    /**
     * Unregisters an existing client
     *
     * @param clientId The client identifier
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws OXException If un-registration fails
     */
    boolean unregisterClient(String clientId) throws OXException;

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param clientId The client identifier
     * @return The client with revoked/new secret
     * @throws OXException If revoke operation fails
     */
    Client revokeClientSecret(String clientId) throws OXException;

    /**
     * Enables denoted client
     *
     * @param clientId The client identifier
     * @throws OXException If client could not be enabled
     */
    void enableClient(String clientId) throws OXException;

    /**
     * Disables denoted client
     *
     * @param clientId The client identifier
     * @throws OXException If client could not be disabled
     */
    void disableClient(String clientId) throws OXException;



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
     * @param scope The scope string, must have been validated before
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return A new authorization code
     * @throws OXException If operation fails
     */
    String generateAuthorizationCodeFor(String clientId, String redirectURI, String scopeString, int userId, int contextId) throws OXException;

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

    // ------------------------------------ Access Code Handling ----------------------------------- \\



    OAuthGrant redeemRefreshToken(Client client, String refreshToken);

    // --------------------------------------- Helper methods -------------------------------------- \\

    /**
     * Checks if the given scope string is valid in terms of syntax and server-side provided scopes.
     *
     * @param scopeString The scope string to check
     * @return <code>true</code> if the scope string contains at least one scope and all scopes belong
     * to registered providers. Otherwise <code>false</code>.
     */
    boolean isValidScopeString(String scopeString);

}
