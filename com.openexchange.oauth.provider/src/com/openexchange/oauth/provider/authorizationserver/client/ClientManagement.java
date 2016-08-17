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
