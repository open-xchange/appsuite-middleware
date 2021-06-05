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

package com.openexchange.oauth;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link OAuthAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface OAuthAccountStorage {

    /**
     * Stores the specified {@link OAuthAccount} in the storage
     *
     * @param session The {@link Session}
     * @param account The {@link OAuthAccount} to store
     * @return The identifier of the stored account
     * @throws OXException if the account cannot be stored
     */
    int storeAccount(Session session, OAuthAccount account) throws OXException;

    /**
     * Gets the specified account.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return The account
     * @throws OXException If account does not exist, or if any other error is occurred
     */
    default OAuthAccount getAccount(Session session, int accountId) throws OXException {
        return getAccount(session, accountId, true);
    }

    /**
     * Gets the specified account.
     *
     * @param session The session
     * @param accountId The account identifier
     * @param loadSecrets Whether to load account's token and secret strings (provided that such an account is found) or to leave them blank
     * @return The account
     * @throws OXException If account does not exist, or if any other error is occurred
     */
    OAuthAccount getAccount(Session session, int accountId, boolean loadSecrets) throws OXException;

    /**
     * Returns the account with the specified identifier for the specified
     * user in the specified context
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The account
     * @throws OXException If account does not exist, or if any other error is occurred
     */
    OAuthAccount getAccount(int contextId, int userId, int accountId) throws OXException;

    /**
     * Deletes the specified account.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws OXException If deletion fails
     */
    boolean deleteAccount(Session session, int accountId) throws OXException;

    /**
     * Updates the specified account
     *
     * @param session The {@link Session}
     * @param account the {@link OAuthAccount} to update
     * @throws OXException if the update fails
     */
    void updateAccount(Session session, OAuthAccount account) throws OXException;

    /**
     * Update the specified account.
     * <p>
     * The arguments may provide:
     * <ul>
     * <li>display name; {@link OAuthConstants#ARGUMENT_DISPLAY_NAME}</li>
     * <li>request token; {@link OAuthConstants#ARGUMENT_REQUEST_TOKEN}</li>
     * <li>enabled scopes; {@link OAuthConstants#ARGUMENT_SCOPES}</li>
     * <li>user password is <b>mandatory</b> if request token shall be updated; {@link OAuthConstants#ARGUMENT_PASSWORD}</li>
     * </ul>
     *
     * @param session The session
     * @param accountId The account identifier
     * @param arguments The arguments to update
     *
     * @throws OXException If update fails
     */
    void updateAccount(Session session, int accountId, Map<String, Object> arguments) throws OXException;

    /**
     * Searches for an {@link OAuthAccount} with the specified user identity for the specified provider
     *
     * @param session the {@link Session}
     * @param userIdentity The user identity
     * @param serviceId The service provider identifier
     * @param loadSecrets Whether to load account's token and secret strings (provided that such an account is found) or to leave them blank
     * @return The {@link OAuthAccount} or <code>null</code> if no account is found
     * @throws OXException if an error is occurred
     */
    OAuthAccount findByUserIdentity(Session session, String userIdentity, String serviceId, boolean loadSecrets) throws OXException;

    /**
     * Returns <code>true</code> if the specified account of the specified provider has a user identity
     *
     * @param session The {@link Session}
     * @param accountId The account identifier
     * @param serviceId The service identifier
     * @return <code>true</code> if the specified account of the specified provider has a user identity;
     *         <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean hasUserIdentity(Session session, int accountId, String serviceId) throws OXException;

    /**
     * Gets all accounts belonging to specified user.
     *
     * @param session The {@link Session}
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<OAuthAccount> getAccounts(Session session) throws OXException;

    /**
     * Gets all accounts belonging to specified user with given service identifier.
     *
     * @param session The {@link Session}
     * @param serviceMetaData The identifier of service meta data
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<OAuthAccount> getAccounts(Session session, String serviceMetaData) throws OXException;

}
