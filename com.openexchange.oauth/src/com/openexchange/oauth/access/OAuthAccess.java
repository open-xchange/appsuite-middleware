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

import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;

/**
 * {@link OAuthAccess} - Wraps the concrete client that is supposed to be used to access OAuth account's resources.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface OAuthAccess {

    /**
     * Initializes the {@link OAuthAccess}
     * <ul>
     * <li>Get & set the {@link OAuthAccount}</li>
     * <li>Create the relevant {@link OAuthClient}</li>
     * <li>Apply the access token to the {@link OAuthClient}</li>
     * </ul>
     *
     * @throws OXException if the {@link OAuthAccess} cannot be initialized
     */
    void initialize() throws OXException;

    /**
     * Ensures that the access is not expired
     *
     * @return The non-expired access
     * @throws OXException if the check fails
     */
    OAuthAccess ensureNotExpired() throws OXException;

    /**
     * Returns the {@link OAuthAccount} that is bound with this {@link OAuthAccess}
     *
     * @return The {@link OAuthAccount}
     */
    OAuthAccount getOAuthAccount();

    /**
     * Pings the account to check accessibility/availability.
     *
     * @return <code>true</code>for a successful ping attempt; <code>false</code>otherwise
     * @throws OXException If the account cannot be pinged
     */
    boolean ping() throws OXException;

    /**
     * Disposes the instance
     */
    void dispose();

    /**
     * Retrieves the client
     *
     * @param type the client type
     * @return The {@link OAuthClient}
     * @throws OXException if the client cannot be initialised or returned
     */
    <T> OAuthClient<T> getClient() throws OXException;

    /**
     * Returns the account identifier of this {@link OAuthAccess}
     *
     * @return the account identifier of this {@link OAuthAccess}
     */
    int getAccountId() throws OXException;
}
