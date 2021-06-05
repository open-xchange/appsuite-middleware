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

package com.openexchange.monitoring.sockets;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * {@link SocketLoggerRMIService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public interface SocketLoggerRMIService extends Remote {

    /** The registration name for this RMI interface */
    public static final String RMI_NAME = SocketLoggerRMIService.class.getSimpleName();

    /**
     * Registers a logger with the specified name
     *
     * @param name The name of the logger
     * @throws RemoteException if an error is occurred or if the logger is black-listed
     */
    void registerLoggerFor(String name) throws RemoteException;

    /**
     * Unregisters the logger with the specified name
     *
     * @param name The name of the logger
     */
    void unregisterLoggerFor(String name) throws RemoteException;

    /**
     * Returns an unmodifiable {@link Set} with all registered logger names
     *
     * @return an unmodifiable {@link Set} with all registered logger names
     * @throws RemoteException if an error is occurred
     */
    Set<String> getRegisteredLoggers() throws RemoteException;

    /**
     * Returns an unmodifiable {@link Set} with all blacklisted logger names
     *
     * @return an unmodifiable {@link Set} with all blacklisted logger names
     * @throws RemoteException if an error is occurred
     */
    Set<String> getBlacklistedLoggers() throws RemoteException;
}
