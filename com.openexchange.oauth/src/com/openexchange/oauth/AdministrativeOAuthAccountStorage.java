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

import java.sql.Connection;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link AdministrativeOAuthAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public interface AdministrativeOAuthAccountStorage extends OAuthAccountStorage {

    /**
     * Gets all accounts belonging to specified context
     * 
     * @param contextId The context identifier
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<AdministrativeOAuthAccount> listAccounts(int contextId) throws OXException;

    /**
     * Gets all accounts belonging to specified user
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<AdministrativeOAuthAccount> listAccounts(int contextId, int userId) throws OXException;

    /**
     * Gets all accounts belonging to specified context and for the given service identifier
     * 
     * @param contextId The context identifier
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<AdministrativeOAuthAccount> listAccounts(int contextId, String providerId) throws OXException;

    /**
     * Gets all accounts belonging to specified user and for the given service identifier
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link OAuthAccount}s, or an empty {@link List}
     * @throws OXException if the {@link OAuthAccount}s cannot be returned
     */
    List<AdministrativeOAuthAccount> listAccounts(int contextId, int userId, String providerId) throws OXException;

    /**
     * Deletes the specified account
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean deleteAccount(int contextId, int userId, int accountId) throws OXException;

    /**
     * Deletes the specified account
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param connection The writeable connection
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean deleteAccount(int contextId, int userId, int accountId, Connection connection) throws OXException;
}
