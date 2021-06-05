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

package com.openexchange.oauth.provider.authorizationserver.client;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;


/**
 * Service interface to manage OAuth client applications in cases where the middleware acts as an
 * authorization server.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@SingletonService
public interface ClientManagement {

    /**
     * The default context group ID.
     */
    public static final String DEFAULT_GID = "default";

    /**
     * The max. number of clients that a user is allowed to grant access to
     */
    public static final int MAX_CLIENTS_PER_USER = 50;

    /**
     * Gets all clients for the given context group.
     *
     * @param contextGroup The context group to to list the clients for. Pass {@link #DEFAULT_GID} in deployments without multiple context groups.
     * @param groupId The context group ID. Pass {@link #DEFAULT_GID} in deployments without multiple context groups.
     * @return A list of clients
     */
    List<Client> getClients(String contextGroup) throws ClientManagementException;

    /**
     * Gets the client identified by the given identifier.
     *
     * @param clientId The clients identifier
     * @return The client or <code>null</code> if there is no such client
     * @throws OXException If operation fails
     */
    Client getClientById(String clientId) throws ClientManagementException;

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param contextGroup The context group to create the client in. Pass {@link #DEFAULT_GID} in deployments without multiple context groups.
     * @param clientData The client data to create the client from
     * @return The newly created client
     * @throws OXException If create operation fails
     */
    Client registerClient(String contextGroup, ClientData clientData) throws ClientManagementException;

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @return The updated client
     * @throws OXException If update operation fails
     */
    Client updateClient(String clientId, ClientData clientData) throws ClientManagementException;

    /**
     * Unregisters an existing client
     *
     * @param clientId The client identifier
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws OXException If un-registration fails
     */
    boolean unregisterClient(String clientId) throws ClientManagementException;

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param clientId The client identifier
     * @return The client with revoked/new secret
     * @throws OXException If revoke operation fails
     */
    Client revokeClientSecret(String clientId) throws ClientManagementException;

    /**
     * Enables denoted client
     *
     * @param clientId The client identifier
     * @return
     * @throws OXException If client could not be enabled
     */
    boolean enableClient(String clientId) throws ClientManagementException;

    /**
     * Disables denoted client
     *
     * @param clientId The client identifier
     * @return
     * @throws OXException If client could not be disabled
     */
    boolean disableClient(String clientId) throws ClientManagementException;

}
