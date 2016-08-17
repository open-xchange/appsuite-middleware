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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.oauth.provider.impl.client.storage;

import java.util.List;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientData;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;

/**
 * {@link OAuthClientStorage} - The storage for clients.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface OAuthClientStorage {

    /**
     * Gets all clients for the given context group.
     *
     * @param groupId id of the context group to list the clients for
     * @return A list of clients
     */
    List<Client> getClients(String groupId) throws ClientManagementException;

    /**
     * Gets the client identified by the given context group and client identifier.
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The clients identifier
     * @return The client or <code>null</code> if there is no such client
     * @throws ClientManagementException If operation fails
     */
    Client getClientById(String groupId, String clientId) throws ClientManagementException;

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param groupId id of the context group the client shall be assigned to
     * @param clientId The ID for the client
     * @param secret The client secret
     * @param clientData The client data to create the client from
     * @return The newly created client
     * @throws ClientManagementException If create operation fails
     */
    Client registerClient(String groupId, String clientId, String secret, ClientData clientData) throws ClientManagementException;

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @param clientData The client data
     * @return The updated client
     * @throws ClientManagementException If update operation fails
     */
    Client updateClient(String groupId, String clientId, ClientData clientData) throws ClientManagementException;

    /**
     * Unregisters an existing client
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws ClientManagementException If un-registration fails
     */
    boolean unregisterClient(String groupId, String clientId) throws ClientManagementException;

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @param secret The new client secret
     * @return The client with revoked/new secret
     * @throws ClientManagementException If revoke operation fails
     */
    Client revokeClientSecret(String groupId, String clientId, String secret) throws ClientManagementException;

    /**
     * Enables denoted client
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @return <code>true</code> if the client was enabled, <code>false</code> if it was not in disabled state before
     * @throws ClientManagementException If client could not be enabled
     */
    boolean enableClient(String groupId, String clientId) throws ClientManagementException;

    /**
     * Disables denoted client
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @return <code>true</code> if the client was disabled, <code>false</code> if it was not in enabled state before
     * @throws ClientManagementException If client could not be disabled
     */
    boolean disableClient(String groupId, String clientId) throws ClientManagementException;

    /**
     * Invalidates denoted client from cache
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     */
    void invalidateClient(String groupId, String clientId);

}
