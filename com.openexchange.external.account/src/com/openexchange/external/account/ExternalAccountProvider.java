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

package com.openexchange.external.account;

import java.sql.Connection;
import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;

/**
 * {@link ExternalAccountProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public interface ExternalAccountProvider {

    /**
     * Returns the module for the provider
     *
     * @return the module for the provider
     */
    @NonNull
    ExternalAccountModule getModule();

    /**
     * Lists all {@link ExternalAccount}s of the specified provider and
     * in the specified context
     *
     * @param contextId The context identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws OXException if an error is occurred
     */
    List<ExternalAccount> list(int contextId) throws OXException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context and of the the specified provider.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws OXException if an error is occurred
     */
    List<ExternalAccount> list(int contextId, int userId) throws OXException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context and of the the specified service,
     * i.e. the service of the concrete {@link ExternalAccountModule}.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param serviceId The service identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws OXException if an error is occurred
     */
    List<ExternalAccount> list(int contextId, int userId, String serviceId) throws OXException;

    /**
     * Lists all {@link ExternalAccount}s of the specified service,
     * i.e. the service of the concrete {@link ExternalAccountModule}, and
     * in the specified context
     *
     * @param contextId The context identifier
     * @param serviceId The service identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws OXException if an error is occurred
     */
    List<ExternalAccount> list(int contextId, String serviceId) throws OXException;

    /**
     * Deletes the external account with the specified identifier
     * of the specified user in the specified context
     *
     * @param id The account's identifier
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean delete(int id, int contextId, int userId) throws OXException;

    /**
     * Deletes the external account with the specified identifier
     * of the specified user in the specified context
     *
     * @param id The account's identifier
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param connection The writable connection to use for deletion
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    boolean delete(int id, int contextId, int userId, Connection connection) throws OXException;
}
