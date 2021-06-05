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

package com.openexchange.consistency;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link ConsistencyService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface ConsistencyService {

    /**
     * Checks and/or repairs the configuration database
     * 
     * @param repair flag to trigger the repair
     * @return A {@link List} with repair information
     * @throws OXException if an error is occurred
     */
    List<String> checkOrRepairConfigDB(boolean repair) throws OXException;

    /**
     * Lists all missing files in the specified context
     * 
     * @param contextId the context identifier
     * @return A {@link List} with all missing files
     * @throws OXException if an error is occurred
     */
    List<String> listMissingFilesInContext(int contextId) throws OXException;

    /**
     * Lists all missing files in the specified filestore
     * 
     * @param filestoreId the filestore identifier
     * @return A {@link List} with all missing files
     * @throws OXException if an error is occurred
     */
    Map<Entity, List<String>> listMissingFilesInFilestore(int filestoreId) throws OXException;

    /**
     * Lists all missing files in the specified database
     * 
     * @param databaseId the database identifier
     * @return A {@link List} with all missing files
     * @throws OXException if an error is occurred
     */
    Map<Entity, List<String>> listMissingFilesInDatabase(int databaseId) throws OXException;

    /**
     * Lists all missing files
     * 
     * @return A {@link List} with all missing files
     * @throws OXException if an error is occurred
     */
    Map<Entity, List<String>> listAllMissingFiles() throws OXException;

    /**
     * Lists all unassigned files in the specified context
     * 
     * @param contextId The context identifier
     * @return A {@link List} with all unassigned files
     * @throws OXException if an error is occurred
     */
    List<String> listUnassignedFilesInContext(int contextId) throws OXException;

    /**
     * Lists all unassigned files in the specified filestore
     * 
     * @param filestoreId The filestore identifier
     * @return A {@link List} with all unassigned files
     * @throws OXException if an error is occurred
     */
    Map<Entity, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws OXException;

    /**
     * Lists all unassigned files in the specified database
     * 
     * @param databaseId The database identifier
     * @return A {@link List} with all unassigned files
     * @throws OXException if an error is occurred
     */
    Map<Entity, List<String>> listUnassignedFilesInDatabase(int databaseId) throws OXException;

    /**
     * Lists all unassigned files
     * 
     * @return A {@link List} with all unassigned files
     * @throws OXException if an error is occurred
     */
    Map<Entity, List<String>> listAllUnassignedFiles() throws OXException;

    /**
     * Repairs all files in the specified context by using the specified resolver policy
     * 
     * @param contextId The context identifier
     * @param repairPolicy The repair policy
     * @param repairAction The repair action
     * @throws OXException if an error is occurred
     */
    void repairFilesInContext(int contextId, RepairPolicy repairPolicy, RepairAction repairAction) throws OXException;

    /**
     * Repairs all files in the specified filestore by using the specified resolver policy
     * 
     * @param filestoreId The filestore identifier
     * @param repairPolicy The repair policy
     * @param repairAction The repair action
     * @throws OXException if an error is occurred
     */
    void repairFilesInFilestore(int filestoreId, RepairPolicy repairPolicy, RepairAction repairAction) throws OXException;

    /**
     * Repairs all files in the specified database by using the specified resolver policy
     * 
     * @param databaseId The database identifier
     * @param repairPolicy The repair policy
     * @param repairAction The repair action
     * @throws OXException if an error is occurred
     */
    void repairFilesInDatabase(int databaseId, RepairPolicy repairPolicy, RepairAction repairAction) throws OXException;

    /**
     * Repairs all files by using the specified resolver policy
     * 
     * @param repairPolicy The repair policy
     * @param repairAction The repair action
     * @throws OXException if an error is occurred
     */
    void repairAllFiles(RepairPolicy repairPolicy, RepairAction repairAction) throws OXException;
}
