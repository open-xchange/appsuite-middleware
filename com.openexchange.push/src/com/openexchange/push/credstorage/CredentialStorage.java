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

package com.openexchange.push.credstorage;

import com.openexchange.exception.OXException;

/**
 * {@link CredentialStorage} - The credential storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public interface CredentialStorage {

    /**
     * Gets the credentials for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The credentials or <code>null</code> if there are no credentials for the given user
     * @throws OXException If returning credentials fails
     */
    Credentials getCredentials(int userId, int contextId) throws OXException;

    /**
     * Stores the given credentials
     *
     * @param credentials The credentials
     * @throws OXException If store operation fails
     */
    void storeCredentials(Credentials credentials) throws OXException;

    /**
     * Deletes the credentials for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The deleted credentials or <code>null</code> if there are no credentials for the given user
     * @throws OXException If delete operation fails
     */
    Credentials deleteCredentials(int userId, int contextId) throws OXException;

}
