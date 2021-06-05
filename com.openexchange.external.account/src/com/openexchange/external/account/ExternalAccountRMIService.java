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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * {@link ExternalAccountRMIService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public interface ExternalAccountRMIService extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "ExternalAccountRMIService";

    /**
     * Lists all {@link ExternalAccount}s in the specified context.
     *
     * @param contextId The context identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified provider and
     * in the specified context
     *
     * @param contextId The context identifier
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, String providerId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified module and
     * in the specified context
     *
     * @param contextId The context identifier
     * @param module The module
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, ExternalAccountModule module) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified module and
     * in the specified context and for the specified user
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param module The module
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId, ExternalAccountModule module) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context and of the specified provider.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param providerId The provider identifier
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId, String providerId) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified context the specified provider
     * and the specified module.
     *
     * @param contextId The context identifier
     * @param providerId The provider identifier
     * @param module The module
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, String providerId, ExternalAccountModule module) throws RemoteException;

    /**
     * Lists all {@link ExternalAccount}s of the specified user
     * in the specified context of the specified provider and the specified module.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param providerId The provider identifier
     * @param module The module
     * @return A {@link List} with all {@link ExternalAccount}s
     * @throws RemoteException
     */
    List<ExternalAccount> list(int contextId, int userId, String providerId, ExternalAccountModule module) throws RemoteException;

    /**
     * Deletes the external account with the specified identifier
     * of the specified user in the specified context
     *
     * @param id The account's identifier
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param module The module
     * @return <code>true</code> if the account was successfully deleted; <code>false</code> otherwise
     * @throws RemoteException
     */
    boolean delete(int id, int contextId, int userId, ExternalAccountModule module) throws RemoteException;
}
