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

package com.openexchange.gdpr.dataexport.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * {@link DataExportRMIService} - The RMI interface for GDPR data export module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface DataExportRMIService extends Remote {

    /** The RMI name */
    public static final String RMI_NAME = DataExportRMIService.class.getSimpleName();

    /** The ID column */
    public static final String COLUMN_ID = "id";

    /** The task column */
    public static final String COLUMN_TASK = "task";

    /** The module column */
    public static final String COLUMN_MODULE = "module";

    /** The user column */
    public static final String COLUMN_USER = "user";

    /** The context column */
    public static final String COLUMN_CONTEXT = "context";

    /** The package column */
    public static final String COLUMN_PACKAGE = "package";

    /** The creation time column */
    public static final String COLUMN_CREATION_TIME = "creation time";

    /** The start time column */
    public static final String COLUMN_START_TIME = "start time";

    /** The status column */
    public static final String COLUMN_STATUS = "status";

    /** The info column */
    public static final String COLUMN_INFO = "info";

    /** The location column */
    public static final String COLUMN_LOCATION = "location";

    /** The filestore column */
    public static final String COLUMN_FILESTORE = "filestore";

    /** The filestore URI column */
    public static final String COLUMN_FILESTORE_URI = "URI";

    /** The work items column */
    public static final String COLUMN_WORK_ITEMS = "work items";

    /** The result files column */
    public static final String COLUMN_RESULT_FILES = "result files";

    /** The number column */
    public static final String COLUMN_NUMBER = "number";

    /** The file name column */
    public static final String COLUMN_FILE_NAME = "file name";

    /** The content type column */
    public static final String COLUMN_CONTENT_TYPE = "content type";

    /** The size column */
    public static final String COLUMN_SIZE = "size";

    /**
     * Requests to cancel the data export task (if any) for specified user.
     * <p>
     * Any generated resources/artifacts are deleted when task gets stopped.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a task has been successfully stopped; otherwise <code>false</code> if there was no such task
     * @throws RemoteException If request to cancel data export task fails
     */
    boolean cancelDataExportTask(int userId, int contextId) throws RemoteException;

    /**
     * Requests to cancel the data export task (if any) for specified context.
     * <p>
     * Any generated resources/artifacts are deleted when tasks get stopped.
     *
     * @param contextId The context identifier
     * @return The identifiers of such tasks that were successfully requested for being canceled
     * @throws RemoteException If request to cancel data export tasks fails
     */
    List<String> cancelDataExportTasks(int contextId) throws RemoteException;

    /**
     * Gets the data export task associated with given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The data export task
     * @throws RemoteException If data export task cannot be returned
     */
    Map<String, Object> getDataExportTask(int userId, int contextId) throws RemoteException;

    /**
     * Gets data export tasks for given context.
     *
     * @param contextId The context identifier
     * @return The data export tasks
     * @throws RemoteException If data export tasks cannot be returned
     */
    List<Map<String, Object>> getDataExportTasks(int contextId) throws RemoteException;

    /**
     * Gets all data export tasks.
     *
     * @return The data export tasks
     * @throws RemoteException If data export tasks cannot be returned
     */
    List<Map<String, Object>> getDataExportTasks() throws RemoteException;

    /**
     * Gets all export work items which don't have a valid filestore location.
     *
     * @return The export task items
     * @throws RemoteException If an error occurs
     */
    List<Map<String, Object>> getOrphanedWorkItems() throws RemoteException;

    /**
     * Gets all filestore locations which are missing their export task item.
     *
     * @param filestoreIds The filestoraIds
     * @return The filestore locations
     * @throws RemoteException If an error occurs
     */
    List<Map<String, Object>> getOrphanedFileStoreLocations(List<Integer> filestoreIds) throws RemoteException;

    /**
     * Gets all result files which don't have a valid filestore location.
     *
     * @return The result files
     * @throws RemoteException If an error occurs
     */
    List<Map<String, Object>> getOrphanedResultFiles() throws RemoteException;

    /**
     * Fixes all orphaned filestore locations, export work items and result files.
     * Filestore locations will be removed, export task items and result files will be reset.
     *
     * @param filestoreIds The filestoraIds
     * @return The total number of fixed entries.
     * @throws RemoteException If an error occurs
     */
    int fixOrphanedEntries(List<Integer> filestoreIds) throws RemoteException;

}
