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

package com.openexchange.database.cleanup.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * {@link DatabaseCleanUpRMIService} - The RMI interface for clean-up job module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public interface DatabaseCleanUpRMIService extends Remote {

    /** The RMI name */
    public static final String RMI_NAME = DatabaseCleanUpRMIService.class.getSimpleName();

    /**
     * Gets the currently submitted clean-up jobs.
     *
     * @return The identifiers of currently submitted clean-up jobs
     * @throws RemoteException If clean-up jobs cannot be returned
     */
    List<String> getCleanUpJobs() throws RemoteException;

}
