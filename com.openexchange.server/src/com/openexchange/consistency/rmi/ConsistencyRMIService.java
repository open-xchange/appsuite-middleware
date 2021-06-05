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

package com.openexchange.consistency.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * {@link ConsistencyRMIService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface ConsistencyRMIService extends Remote {

    public static final String RMI_NAME = ConsistencyRMIService.class.getSimpleName();

    /**
     * Checks and/or repairs the configuration database
     * 
     * @param repair flag to trigger the repair
     * @return A {@link List} with repair information
     * @throws RemoteException if an error is occurred
     */
    List<String> checkOrRepairConfigDB(boolean repair) throws RemoteException;

    /**
     * Lists all missing files in the specified context
     * 
     * @param contextId the context identifier
     * @return A {@link List} with all missing files
     * @throws RemoteException if an error is occurred
     */
    List<String> listMissingFilesInContext(int contextId) throws RemoteException;

    /**
     * Lists all missing files in the specified filestore
     * 
     * @param filestoreId the filestore identifier
     * @return A {@link List} with all missing files
     * @throws RemoteException if an error is occurred
     */
    Map<ConsistencyEntity, List<String>> listMissingFilesInFilestore(int filestoreId) throws RemoteException;

    /**
     * Lists all missing files in the specified database
     * 
     * @param databaseId the database identifier
     * @return A {@link List} with all missing files
     * @throws RemoteException if an error is occurred
     */
    Map<ConsistencyEntity, List<String>> listMissingFilesInDatabase(int databaseId) throws RemoteException;

    /**
     * Lists all missing files
     * 
     * @return A {@link List} with all missing files
     * @throws RemoteException if an error is occurred
     */
    Map<ConsistencyEntity, List<String>> listAllMissingFiles() throws RemoteException;

    /**
     * Lists all unassigned files in the specified context
     * 
     * @param contextId The context identifier
     * @return A {@link List} with all unassigned files
     * @throws RemoteException if an error is occurred
     */
    List<String> listUnassignedFilesInContext(int contextId) throws RemoteException;

    /**
     * Lists all unassigned files in the specified filestore
     * 
     * @param filestoreId The filestore identifier
     * @return A {@link List} with all unassigned files
     * @throws RemoteException if an error is occurred
     */
    Map<ConsistencyEntity, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws RemoteException;

    /**
     * Lists all unassigned files in the specified database
     * 
     * @param databaseId The database identifier
     * @return A {@link List} with all unassigned files
     * @throws RemoteException if an error is occurred
     */
    Map<ConsistencyEntity, List<String>> listUnassignedFilesInDatabase(int databaseId) throws RemoteException;

    /**
     * Lists all unassigned files
     * 
     * @return A {@link List} with all unassigned files
     * @throws RemoteException if an error is occurred
     */
    Map<ConsistencyEntity, List<String>> listAllUnassignedFiles() throws RemoteException;

    /**
     * Repairs all files in the specified context by using the specified resolver policy
     * 
     * @param contextId The context identifier
     * @param resolverPolicy The name of the resolver policy
     * @throws RemoteException if an error is occurred
     */
    void repairFilesInContext(int contextId, String repairPolicy, String repairAction) throws RemoteException;

    /**
     * Repairs all files in the specified filestore by using the specified resolver policy
     * 
     * @param filestoreId The filestore identifier
     * @param resolverPolicy The name of the resolver policy
     * @throws RemoteException if an error is occurred
     */
    void repairFilesInFilestore(int filestoreId, String repairPolicy, String repairAction) throws RemoteException;

    /**
     * Repairs all files in the specified database by using the specified resolver policy
     * 
     * @param databaseId The database identifier
     * @param resolverPolicy The name of the resolver policy
     * @throws RemoteException if an error is occurred
     */
    void repairFilesInDatabase(int databaseId, String repairPolicy, String repairAction) throws RemoteException;

    /**
     * Repairs all files by using the specified resolver policy
     * 
     * @param resolverPolicy The name of the resolver policy
     * @throws RemoteException if an error is occurred
     */
    void repairAllFiles(String repairPolicy, String repairAction) throws RemoteException;
}
