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

package com.openexchange.oauth.provider.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;


/**
 * {@link RemoteClientManagement} - The RMI stub for OAuth Client management.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface RemoteClientManagement extends Remote {

    /**
     * The default context group ID.
     */
    public static final String DEFAULT_GID = "default";

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = RemoteClientManagement.class.getSimpleName();

    /**
     * Gets all clients for the given context group.
     *
     * @param contextGroup The context group ID. Pass {@link #DEFAULT_GID} in deployments without multiple context groups.
     * @param credentials The master admin credentials
     * @return A list of clients
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    List<ClientDto> getClients(String contextGroup, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

    /**
     * Gets the client identified by the given identifier.
     *
     * @param clientId The clients identifier
     * @param credentials The master admin credentials
     * @return The client or <code>null</code> if there is no such client
     * @throws RemoteClientManagementException If operation fails
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    ClientDto getClientById(String clientId, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param contextGroup The context group ID. Pass {@link #DEFAULT_GID} in deployments without multiple context groups.
     * @param clientData The client data to create the client from
     * @param credentials The master admin credentials
     * @return The newly created client
     * @throws RemoteClientManagementException If create operation fails
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    ClientDto registerClient(String contextGroup, ClientDataDto clientData, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @param credentials The master admin credentials
     * @return The updated client
     * @throws RemoteClientManagementException If update operation fails
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    ClientDto updateClient(String clientId, ClientDataDto clientData, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

    /**
     * Unregisters an existing client
     *
     * @param clientId The client identifier
     * @param credentials The master admin credentials
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws RemoteClientManagementException If un-registration fails
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    boolean unregisterClient(String clientId, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param clientId The client identifier
     * @param credentials The master admin credentials
     * @return The client with revoked/new secret
     * @throws RemoteClientManagementException If revoke operation fails
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    ClientDto revokeClientSecret(String clientId, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

    /**
     * Enables denoted client
     *
     * @param clientId The client identifier
     * @param credentials The master admin credentials
     * @return <code>true</code> if enabling was successful, <code>false</code> if the client was already enabled
     * @throws RemoteClientManagementException If client could not be enabled
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    boolean enableClient(String clientId, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

    /**
     * Disables denoted client
     *
     * @param clientId The client identifier
     * @param credentials The master admin credentials
     * @return <code>true</code> if disabling was successful, <code>false</code> if the client was already disabled
     * @throws RemoteClientManagementException If client could not be disabled
     * @throws InvalidCredentialsException If the passed credentials are invalid
     */
    boolean disableClient(String clientId, Credentials credentials) throws RemoteClientManagementException, RemoteException, InvalidCredentialsException;

}
