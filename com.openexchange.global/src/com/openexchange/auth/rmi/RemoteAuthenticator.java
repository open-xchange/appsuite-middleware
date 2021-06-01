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

package com.openexchange.auth.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link RemoteAuthenticator} - The remote stub for administrative authentication
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.2
 */
public interface RemoteAuthenticator extends Remote {

    /** The RMI name */
    public static final String RMI_NAME = RemoteAuthenticator.class.getSimpleName();

    /**
     * Authenticates the master administrator
     *
     * @param login The login for master administrator
     * @param password The password for master administrator
     * @throws RemoteException If credentials are invalid
     */
    void doAuthentication(String login, String password) throws RemoteException;

    /**
     * Authenticates the context administrator
     *
     * @param login The login for context administrator
     * @param password The password for context administrator
     * @param contextId The context identifier
     * @throws RemoteException If credentials are invalid
     */
    void doAuthentication(String login, String password, int contextId) throws RemoteException;

    /**
     * Authenticates all users within a context.
     *
     * @param login The login
     * @param password The password
     * @param contextId The context identifier
     * @throws RemoteException If credentials are invalid
     */
    void doUserAuthentication(String login, String password, int contextId) throws RemoteException;

    /**
     * Checks if master authentication has been disabled for associated machine.
     *
     * @return <code>true</code> if master authentication has been disabled; otherwise <code>false</code>
     * @throws RemoteException If operation fails
     */
    boolean isMasterAuthenticationDisabled() throws RemoteException;

    /**
     * Checks if context authentication has been disabled for associated machine.
     *
     * @return <code>true</code> if context authentication has been disabled; otherwise <code>false</code>
     * @throws RemoteException If operation fails
     */
    boolean isContextAuthenticationDisabled() throws RemoteException;

}
