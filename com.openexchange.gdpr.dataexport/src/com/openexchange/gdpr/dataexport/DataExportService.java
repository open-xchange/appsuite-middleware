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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link DataExportService} - The service for data export tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
@SingletonService
public interface DataExportService {

    /**
     * Gets the data export configuration.
     *
     * @return The data export configuration
     */
    DataExportConfig getConfig();

    /**
     * Plans scheduling of data export tasks.
     *
     * @throws OXException If scheduling fails
     */
    void planSchedule() throws OXException;

    /**
     * Submits a data export task for specified user.
     *
     * @param args The arguments for the submitted data export
     * @param session The session providing user data
     * @return The optional UUID referencing the submitted task
     * @throws OXException If task cannot be submitted or there is already such a task for denoted user
     */
    Optional<UUID> submitDataExportTaskIfAbsent(DataExportArguments args, Session session) throws OXException;

    /**
     * Requests to cancel the data export task (if any) for specified user.
     * <p>
     * Any generated resources/artifacts are deleted when task gets stopped.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a task has been successfully requested for being canceled; otherwise <code>false</code> if there was no such task
     * @throws OXException If request to cancel data export task fails
     */
    boolean cancelDataExportTask(int userId, int contextId) throws OXException;

    /**
     * Requests to cancel the data export tasks (if any) for specified context.
     * <p>
     * Any generated resources/artifacts are deleted when tasks get stopped.
     *
     * @param contextId The context identifier
     * @return The identifiers of such tasks that were successfully requested for being canceled
     * @throws OXException If request to cancel data export tasks fails
     */
    List<UUID> cancelDataExportTasks(int contextId) throws OXException;

    /**
     * Deletes a terminated (either done or failed) data export task (if any) for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a task has been successfully deleted; otherwise <code>false</code> if there was no such task
     * @throws OXException If deletion of data export task fails
     */
    boolean deleteDataExportTask(int userId, int contextId) throws OXException;

    /**
     * Gets listing of all modules available for given user.
     *
     * @param session The session providing user data
     * @return The available modules.
     */
    List<Module> getAvailableModules(Session session) throws OXException;

    /**
     * Gets the optional data export task (if any) for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The optional data export task
     * @throws OXException If optional UUID cannot be returned
     */
    Optional<DataExportTask> getDataExportTask(int userId, int contextId) throws OXException;

    /**
     * Gets the data export for specified user.
     *
     * @param session The session providing user data
     * @return The data export
     * @throws OXException If data export cannot be returned or is not yet completed
     */
    Optional<DataExport> getDataExport(Session session) throws OXException;

    /**
     * Gets the data export download for specified user.
     *
     * @param number The package number to download
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The data export
     * @throws OXException If data export cannot be returned or is not yet completed
     */
    DataExportDownload getDataExportDownload(int number, int userId, int contextId) throws OXException;

    /**
     * Gets all data export tasks for given context.
     *
     * @param contextId The context identifier
     * @return The data export tasks
     * @throws OXException If data export tasks cannot be returned
     */
    List<DataExportTask> getDataExportTasks(int contextId) throws OXException;

    /**
     * Gets all data export tasks.
     *
     * @param checkValidity Whether to check validity of queried tasks
     * @return The data export tasks
     * @throws OXException If data export tasks cannot be returned
     */
    List<DataExportTask> getDataExportTasks(boolean checkValidity) throws OXException;

    /**
     * Removes the completed data export task (if any) and any associated resources for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a task has been successfully removed; otherwise <code>false</code> if there was no such task
     * @throws OXException If removing data export task fails
     */
    boolean removeDataExport(int userId, int contextId) throws OXException;

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

}
