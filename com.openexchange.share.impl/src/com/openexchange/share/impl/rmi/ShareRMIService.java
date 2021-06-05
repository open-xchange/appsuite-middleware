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

package com.openexchange.share.impl.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link ShareRMIService}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface ShareRMIService extends Remote {

    public static final String RMI_NAME = ShareRMIService.class.getSimpleName();

    /**
     * Lists all shares in supplied context.
     *
     * @param contextId The contextId
     * @return The shares
     * @throws RemoteException On error
     */
    String listShares(int contextId) throws RemoteException;

    /**
     * Lists all shares in supplied context for the supplied guest user id.
     *
     * @param contextId The contextId
     * @param guestId The guest user id
     * @return The shares
     * @throws RemoteException On error
     */
    String listShares(int contextId, int guestId) throws RemoteException;

    /**
     * List share identified by supplied token
     *
     * @param token The token
     * @return The share
     * @throws RemoteException On error
     */
    String listShares(String token) throws RemoteException;

    /**
     * Removes all targets identified by supplied token.
     * 
     * @param token The token
     * @param path The share path
     * @throws RemoteException
     */
    int removeShare(String token, String path) throws RemoteException;

    /**
     * Removes all targets in supplied context identified by supplied token.
     * 
     * @param shareToken The token
     * @param targetPath The share path
     * @param contextId The contextId
     * @throws RemoteException
     */
    int removeShare(String shareToken, String targetPath, int contextId) throws RemoteException;

    /**
     * Remove all shares from supplied context.
     *
     * @param contextId The contextId
     * @throws RemoteException On error
     */
    int removeShares(int contextId) throws RemoteException;

    /**
     * Removes all shares in supplied context for the supplied guest user.
     *
     * @param contextId The contextId
     * @param guestId The guest user id
     * @throws RemoteException On error
     */
    int removeShares(int contextId, int guestId) throws RemoteException;
}
