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

package com.openexchange.osgi.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * {@link DeferredActivatorRMIService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface DeferredActivatorRMIService extends Remote {

    public static final String RMI_NAME = "DeferredActivatorRMIService";

    /**
     * Gets a list of canonical class names of services needed for start-up, but currently not (yet) available for specified bundle.
     *
     * @param name The bundle name
     * @return A list of canonical class names of missing services
     * @throws RemoteException if an error is occurred
     */
    List<String> listMissingServices(String name) throws RemoteException;

    /**
     * Gets a list of canonical class names of services needed for start-up, but currently not (yet) available.
     *
     * @return A canonical class names of missing services mapped to bundle
     * @throws RemoteException if an error is occurred
     */
    Map<String, List<String>> listAllMissingServices() throws RemoteException;

    /**
     * Checks if activator for specified bundle is active; meaning all needed services are available.
     *
     * @param name The bundle name
     * @return <code>true</code> if active; otherwise <code>false</code>
     * @throws RemoteException if an error is occurred
     */
    boolean isActive(String name) throws RemoteException;

    /**
     * Lists all available bundles.
     *
     * @return All available bundles
     * @throws RemoteException if an error is occurred
     */
    List<String> listAvailableBundles() throws RemoteException;
}
