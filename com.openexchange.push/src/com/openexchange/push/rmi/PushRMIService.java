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

package com.openexchange.push.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * {@link PushRMIService} - The RMI service for mail push
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface PushRMIService extends Remote {

    public static final String RMI_NAME = PushRMIService.class.getSimpleName();

    /**
     * Lists push users running on this node
     *
     * @return The push users running on this node
     * @throws RemoteException If push users cannot be returned
     */
    List<List<String>> listPushUsers() throws RemoteException;

    /**
     * Lists client registrations on this node
     *
     * @return The registered clients running on this node
     * @throws RemoteException If push users cannot be returned
     */
    List<List<String>> listClientRegistrations() throws RemoteException;

    /**
     * Unregisters the permanent listener for specified push user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param clientId The client identifier
     * @return <code>true</code> on successful un-registration; otherwise <code>false</code>
     * @throws RemoteException If un-registration fails
     */
    boolean unregisterPermanentListenerFor(int userId, int contextId, String clientId) throws RemoteException;
}
