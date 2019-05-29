/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
     * @return The data export tasks
     * @throws OXException If data export tasks cannot be returned
     */
    List<DataExportTask> getDataExportTasks() throws OXException;

    /**
     * Removes the completed data export task (if any) and any associated resources for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a task has been successfully removed; otherwise <code>false</code> if there was no such task
     * @throws OXException If removing data export task fails
     */
    boolean removeDataExport(int userId, int contextId) throws OXException;

}
