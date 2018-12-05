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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.groupware.update;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link UpdateTaskService} - The RMI service for update tasks
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface UpdateTaskService extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = UpdateTaskService.class.getSimpleName();

    /**
     * Executes all pending update tasks for the specified context.
     * 
     * @param contextId the context identifier
     */
    List<Map<String, Object>> runUpdate(int contextId) throws RemoteException;

    /**
     * Executes all pending update tasks for the specified schema.
     * 
     * @param schemaName The schema name
     */
    List<Map<String, Object>> runUpdate(String schemaName) throws RemoteException;

    /**
     * Schedules an asynchronous task to execute all update tasks on all available schemata.
     * 
     * @param throwExceptionOnFailure Whether a possible exception is supposed to abort process
     * @return The job identifier of the scheduled task
     */
    String runAllUpdates(boolean throwExceptionOnFailure) throws RemoteException;

    /**
     * Force (re-)run of update task denoted by given class name for the specified context.
     * 
     * @param contextId The context identifier
     * @param taskName The class name of the task
     */
    void forceUpdateTask(int contextId, String taskName) throws RemoteException;

    /**
     * Force (re-)run of update task denoted by given class name for the specified schema.
     * 
     * @param schemaName The schema name
     * @param taskName The class name of the task
     */
    void forceUpdateTask(String schemaName, String taskName) throws RemoteException;

    /**
     * Force (re-)run of update task denoted by given class name on all schemata.
     * 
     * @param taskName The task name
     */
    void forceUpdateTaskOnAllSchemata(String taskName) throws RemoteException;

    /**
     * Returns a list with all executed tasks
     * 
     * @param schemaName The schema name
     * @return a list with all executed tasks
     */
    List<Map<String, Object>> getExecutedTasksList(String schemaName) throws RemoteException;

    /**
     * Returns the status of a scheduled update task job
     * 
     * @param jobId The job identifier
     * @return The status of the job
     * @throws RemoteException
     */
    String getJobStatus(String jobId) throws RemoteException;

    /**
     * Returns an unmodifiable {@link Map} with all {@link NamespaceAwareUpdateTask}s
     * 
     * @return an unmodifiable {@link Map} with all {@link NamespaceAwareUpdateTask}s
     */
    Map<String, Set<String>> getNamespaceAware() throws RemoteException;
}
