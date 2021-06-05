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

package com.openexchange.database.migration.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * {@link DBMigrationRMIService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface DBMigrationRMIService extends Remote {

    public static final String RMI_NAME = "DBMigrationRMIService";

    /**
     * Force running the core configdb changelog
     * 
     * @param schemaName The name of the schema for which to force the migration
     * @throws RemoteException if an error is occurred
     */
    void forceMigration(String schemaName) throws RemoteException;

    /**
     * Roll-back to the given tag of a change set of the core change log
     *
     * @param schemaName The name of the schema for which to roll-back the migration
     * @param changeSetTag the change set tag
     * @throws RemoteException if an error is occurred
     */
    void rollbackMigration(String schemaName, String changeSetTag) throws RemoteException;

    /**
     * Releases all configdb migration locks. Use this in case no lock can be acquired by liquibase.
     * 
     * @param schemaName The name of the schema for which to release the locks
     * @throws RemoteException if an error is occurred
     */
    void releaseLocks(String schemaName) throws RemoteException;

    /**
     * Gets a human-readable migration status string for the configdb.
     *
     * @param schemaName The name of the schema for which to get the migration status
     * @return The status
     * @throws RemoteException if an error is occurred
     */
    String getMigrationStatus(String schemaName) throws RemoteException;

    /**
     * Gets a human-readable lock status string for the configdb.
     *
     * @param schemaName The name of the schema for which to get the lock status
     * @return The status
     * @throws RemoteException if an error is occurred
     */
    String getLockStatus(String schemaName) throws RemoteException;
}
