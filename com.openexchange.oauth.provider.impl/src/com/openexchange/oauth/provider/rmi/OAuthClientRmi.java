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

package com.openexchange.oauth.provider.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.ClientData;


/**
 * {@link OAuthClientRmi}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface OAuthClientRmi extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = OAuthClientRmi.class.getSimpleName();

    /**
     * Gets the client identified by the given identifier.
     *
     * @param clientId The clients identifier
     * @return The client or <code>null</code> if there is no such client
     * @throws RemoteException If operation fails
     */
    Client getClientById(String clientId) throws RemoteException;

    /**
     * Registers (adds) a client according to given client data.
     *
     * @param clientData The client data to create the client from
     * @return The newly created client
     * @throws RemoteException If create operation fails
     */
    Client registerClient(ClientData clientData) throws RemoteException;

    /**
     * Updates an existing client's attributes according to given client data.
     *
     * @param clientId The client identifier
     * @param clientData The client data
     * @return The updated client
     * @throws RemoteException If update operation fails
     */
    Client updateClient(String clientId, ClientData clientData) throws RemoteException;

    /**
     * Unregisters an existing client
     *
     * @param clientId The client identifier
     * @return <code>true</code> if and only if such a client existed and has been successfully deleted; otherwise <code>false</code>
     * @throws RemoteException If un-registration fails
     */
    boolean unregisterClient(String clientId) throws RemoteException;

    /**
     * Revokes a client's current secret and generates a new one.
     *
     * @param clientId The client identifier
     * @return The client with revoked/new secret
     * @throws RemoteException If revoke operation fails
     */
    Client revokeClientSecret(String clientId) throws RemoteException;

    /**
     * Enables denoted client
     *
     * @param clientId The client identifier
     * @throws RemoteException If client could not be enabled
     */
    void enableClient(String clientId) throws RemoteException;

    /**
     * Disables denoted client
     *
     * @param clientId The client identifier
     * @throws RemoteException If client could not be disabled
     */
    void disableClient(String clientId) throws RemoteException;

}
