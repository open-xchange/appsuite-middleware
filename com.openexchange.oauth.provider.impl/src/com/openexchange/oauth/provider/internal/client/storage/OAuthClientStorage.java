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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal.client.storage;

import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientData;

/**
 * {@link OAuthClientStorage} - The storage for clients.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface OAuthClientStorage {

    /**
     * Gets the client identified by the given context group and client identifier.
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The clients identifier
     * @return The client or <code>null</code> if there is no such client
     * @throws OXException If operation fails
     */
    Client getClientById(String groupId, String clientId) throws OXException;

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param clientData The client data to create the client from
     * @return The newly created client
     * @throws OXException If create operation fails
     */
    Client registerClient(ClientData clientData) throws OXException;

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @return The updated client
     * @throws OXException If update operation fails
     */
    Client updateClient(String clientId, ClientData clientData) throws OXException;

    /**
     * Unregisters an existing client
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws OXException If un-registration fails
     */
    boolean unregisterClient(String groupId, String clientId) throws OXException;

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @return The client with revoked/new secret
     * @throws OXException If revoke operation fails
     */
    Client revokeClientSecret(String groupId, String clientId) throws OXException;

    /**
     * Enables denoted client
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @throws OXException If client could not be enabled
     */
    void enableClient(String groupId, String clientId) throws OXException;

    /**
     * Disables denoted client
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     * @throws OXException If client could not be disabled
     */
    void disableClient(String groupId, String clientId) throws OXException;

    /**
     * Invalidates denoted client from cache
     *
     * @param groupId id of the context group the client is assigned to
     * @param clientId The client identifier
     */
    void invalidateClient(String groupId, String clientId);

}
