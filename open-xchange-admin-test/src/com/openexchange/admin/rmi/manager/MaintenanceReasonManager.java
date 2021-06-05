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

package com.openexchange.admin.rmi.manager;

import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;

/**
 * {@link MaintenanceReasonManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MaintenanceReasonManager extends AbstractManager {

    private static MaintenanceReasonManager INSTANCE;

    /**
     * Gets the instance of the {@link MaintenanceReasonManager}
     * 
     * @param host The rmi host
     * @param masterCredentials the master {@link Credentials}
     * @return The {@link MaintenanceReasonManager} instance
     */
    public static MaintenanceReasonManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new MaintenanceReasonManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link MaintenanceReasonManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    public MaintenanceReasonManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates a maintenance reason
     * 
     * @param maintenanceReason The {@link MaintenanceReason} to create
     * @return The created {@link MaintenanceReason}
     * @throws Exception if an error is occurred
     */
    public MaintenanceReason create(MaintenanceReason maintenanceReason) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        MaintenanceReason mr = utilInterface.createMaintenanceReason(maintenanceReason, getMasterCredentials());
        managedObjects.put(mr.getId(), mr);
        return mr;
    }

    /**
     * Lists all maintenance reasons that match the specified pattern
     * 
     * @return An array with all maintenance reasons that match the specified pattern
     * @throws Exception if an error is occurred
     */
    public MaintenanceReason[] search(String pattern) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listMaintenanceReason(pattern, getMasterCredentials());
    }

    /**
     * Deletes the specified {@link MaintenanceReason}
     * 
     * @param maintenanceReason The {@link MaintenanceReason} to delete
     * @throws Exception if an error is occurred
     */
    public void delete(MaintenanceReason maintenanceReason) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        utilInterface.deleteMaintenanceReason(new MaintenanceReason[] { maintenanceReason }, getMasterCredentials());
    }

    @Override
    void clean(Object object) throws Exception {
        delete((MaintenanceReason) object);
    }
}
