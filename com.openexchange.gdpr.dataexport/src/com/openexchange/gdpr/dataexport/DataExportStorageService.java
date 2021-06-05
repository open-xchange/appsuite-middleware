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

package com.openexchange.gdpr.dataexport;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DataExportStorageService} - Responsible for create, read, update and delete operations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
@SingletonService
public interface DataExportStorageService extends DataExportStatusChecker {

    /**
     * Gets the data export configuration.
     *
     * @return The data export configuration
     */
    DataExportConfig getConfig();

    /**
     * Creates given task if there is currently no task associated with given user.
     *
     * @param task The data export task to create
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if created; otherwise <code>false</code> if there is already a task for given user
     * @throws OXException If creation fails
     */
    boolean createIfAbsent(DataExportTask task, int userId, int contextId) throws OXException;

    /**
     * Gets all data export tasks.
     *
     * @return The data export tasks
     * @throws OXException If data export tasks cannot be returned
     */
    List<DataExportTask> getDataExportTasks() throws OXException;

    /**
     * Gets all data export tasks that are currently considered as running or are candidates for being executed.
     *
     * @return The running data export tasks
     * @throws OXException If running data export tasks cannot be returned
     */
    List<DataExportTask> getRunningDataExportTasks() throws OXException;

    /**
     * Checks if there are data export tasks that are currently considered as running or are candidates for being executed.
     *
     * @return <code>true</code> if there are such data export tasks; otherwise <code>false</code>
     * @throws OXException If check for running data export tasks fails
     */
    boolean hasRunningDataExportTasks() throws OXException;

    /**
     * Deletes those tasks that are
     * <ul>
     * <li> Completed (done or failed) and exceed configured max. time-to-live or</li>
     * <li> Aborted and exceed configured expiration time or</li>
     * </ul>
     * and returns tasks with pending notification
     *
     * @return The data export tasks with pending notification
     * @throws OXException If tasks cannot be deleted
     */
    List<DataExportTaskInfo> deleteCompletedOrAbortedTasksAndGetTasksWithPendingNotification() throws OXException;

    /**
     * Gets all data export tasks with pending notification.
     *
     * @return The data export tasks with pending notification
     * @throws OXException If data export tasks cannot be returned
     */
    List<DataExportTaskInfo> getDataExportTasksWithPendingNotification() throws OXException;

    /**
     * Gets the next job for a data export task that needs to be processed.
     * <p>
     * The status of the job-associated task has been atomically set to {@link DataExportStatus#RUNNING}.
     *
     * @return The optional job to process
     * @throws OXException If job cannot be returned
     */
    Optional<DataExportJob> getNextDataExportJob() throws OXException;

    /**
     * Marks specified task as paused; making it available for another processor.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully marked; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean markPaused(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Marks specified task as done; making it available for the user.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully marked; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean markDone(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Marks specified task as aborted.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully marked; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean markAborted(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Marks specified task as failed.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully marked; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean markFailed(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Marks specified task as pending.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully marked; otherwise <code>false</code>
     * @throws OXException If operation fails
     */
    boolean markPending(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Sets the marker that notification has been sent out for specified task.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if marker has been set; otherwise <code>false</code> (if marker has already been set before)
     * @throws OXException If operation fails
     */
    boolean setNotificationSent(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Un-sets the marker that notification has been sent out for specified task.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if marker has been unset; otherwise <code>false</code> (if marker has already been unset before)
     * @throws OXException If operation fails
     */
    boolean unsetNotificationSent(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Marks the given work item as done and sets given file storage location.
     *
     * @param fileStorageLocation The file storage location
     * @param taskId The identifier of parental task
     * @param moduleId The work item identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If operation fails
     */
    void markWorkItemDone(String fileStorageLocation, UUID taskId, String moduleId, int userId, int contextId) throws OXException;

    /**
     * Marks the given work item as paused and sets given file storage location.
     *
     * @param fileStorageLocation The file storage location
     * @param taskId The identifier of parental task
     * @param moduleId The work item identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If operation fails
     */
    void markWorkItemPaused(String fileStorageLocation, UUID taskId, String moduleId, int userId, int contextId) throws OXException;

    /**
     * Marks the given work item as failed and sets optional failure information.
     *
     * @param jFailureInfo The optional failure information
     * @param taskId The identifier for the data export task
     * @param moduleId The work item identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If operation fails
     */
    void markWorkItemFailed(Optional<JSONObject> jFailureInfo, UUID taskId, String moduleId, int userId, int contextId) throws OXException;

    /**
     * Marks the given work item as pending.
     *
     * @param taskId The identifier for the data export task
     * @param moduleId The work item identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    void markWorkItemPending(UUID taskId, String moduleId, int userId, int contextId) throws OXException;

    /**
     * Gets the data export task for specified identifier
     *
     * @param taskId The identifier for the data export task
     * @return The optional data export task
     * @throws OXException If data export task cannot be returned
     */
    Optional<DataExportTask> getDataExportTask(UUID taskId) throws OXException;

    /**
     * Gets the data export task for specified user (if any)
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional data export task
     * @throws OXException If data export task cannot be returned
     */
    Optional<DataExportTask> getDataExportTask(int userId, int contextId) throws OXException;

    /**
     * Gets the data export tasks for specified context (if any)
     *
     * @param contextId The context identifier
     * @return The data export tasks for given context or an empty list
     * @throws OXException If data export task cannot be returned
     */
    List<DataExportTask> getDataExportTasks(int contextId) throws OXException;

    /**
     * Gets the last-accessed time stamp for the data export task of specified user (if any)
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional last-accessed time stamp
     * @throws OXException If last-accessed time stamp cannot be returned
     */
    Optional<Date> getLastAccessedTimeStamp(int userId, int contextId) throws OXException;

    /**
     * Gets the result files' locations for the data export task of specified user (if any)
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional result files' locations
     * @throws OXException If result files cannot be returned
     */
    Optional<FileLocations> getDataExportResultFiles(int userId, int contextId) throws OXException;

    /**
     * Gets the result files' locations for the data export task of specified user (if any)
     *
     * @param number The package number
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional result files' locations
     * @throws OXException If result files cannot be returned
     */
    Optional<FileLocation> getDataExportResultFile(int number, int userId, int contextId) throws OXException;

    /**
     * Sets the given save-point for task-associated module export.
     *
     * @param taskId The identifier of the data export task
     * @param moduleId The identifier of the module, for which data is exported
     * @param savePoint The save-point to set; if no data set the save-point is dropped
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If setting the save-point fails
     */
    void setSavePoint(UUID taskId, String moduleId, DataExportSavepoint savePoint, int userId, int contextId) throws OXException;

    /**
     * Gets the save-point for task-associated module export.
     *
     * @param taskId The identifier of the data export task
     * @param moduleId The identifier of the module, for which data is exported
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The save-point
     * @throws OXException If retrieving the save-point fails
     */
    DataExportSavepoint getSavePoint(UUID taskId, String moduleId, int userId, int contextId) throws OXException;

    /**
     * Deletes the task (if any) and all of its currently associated resources.
     *
     * @param taskId The identifier of the task to delete
     * @return <code>true</code> if such a task has been successfully deleted; otherwise <code>false</code> if there was no such task
     * @throws OXException If deletion fails
     */
    boolean deleteDataExportTask(UUID taskId) throws OXException;

    /**
     * Deletes the task (if any) that is associated with given user and all of its currently associated resources.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a task has been successfully deleted; otherwise <code>false</code> if there was no such task
     * @throws OXException If deletion fails
     */
    boolean deleteDataExportTask(int userId, int contextId) throws OXException;

    /**
     * Adds specified result file to given task.
     *
     * @param fileStorageLocation The file storage location referencing the result file
     * @param number The file's number
     * @param size The file's size (in bytes)
     * @param taskId The task identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If operation fails
     */
    void addResultFile(String fileStorageLocation, int number, long size, UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Deletes the result files from given task
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If operation fails
     */
    void deleteResultFiles(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Drops the intermediate file artifacts from task-associated work items.
     *
     * @param taskId The identifier of the data export task
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If operation fails
     */
    void dropIntermediateFiles(UUID taskId, int userId, int contextId) throws OXException;

    /**
     * Attempts to increment the fail count for given provider.
     *
     * @param taskId The identifier of the data export task
     * @param moduleId The identifier of the provider
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if successfully incremented; otherwise <code>false</code> if max. fail count already reached
     * @throws OXException If operation fails
     */
    boolean incrementFailCount(UUID taskId, String moduleId, int userId, int contextId) throws OXException;

}
