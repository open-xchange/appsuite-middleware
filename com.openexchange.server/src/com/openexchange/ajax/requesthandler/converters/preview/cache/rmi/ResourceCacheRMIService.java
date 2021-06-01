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

package com.openexchange.ajax.requesthandler.converters.preview.cache.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link ResourceCacheRMIService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface ResourceCacheRMIService extends Remote {

    public static final String RMI_NAME = "ResourceCacheRMIService";

    public static final String DOMAIN = "com.openexchange.preview.cache";

    /**
     * Clears all cache entries.
     *
     * @throws RemoteException If operation fails
     */
    void clear() throws RemoteException;

    /**
     * Clears all cache entries for given context.
     *
     * @param contextId The context identifier
     * @throws RemoteException If operation fails
     */
    void clearFor(int contextId) throws RemoteException;

    /**
     * Sanitizes broken/corrupt MIME types currently held in database for specified context.
     *
     * @param contextId The context identifier
     * @param invalids A comma-separated list of MIME types that should be considered as broken/corrupt
     * @return A result description
     * @throws RemoteException If operation fails
     */
    String sanitizeMimeTypesInDatabaseFor(int contextId, String invalids) throws RemoteException;
}
