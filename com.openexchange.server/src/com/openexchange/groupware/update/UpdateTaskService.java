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
     * @return The {@link TaskStatus} including the job identifier of the scheduled task and the status text
     */
    TaskStatus runAllUpdates(boolean throwExceptionOnFailure) throws RemoteException;

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
     * Returns a list with all pending update tasks for the specified schema.
     * 
     * @param schemaName The schema name
     * @param pending Whether the pending tasks (registered but neither executed nor excluded) should be returned
     * @param excluded Whether the update tasks excluded via 'excludeupdatetask.properties' should be returned
     * @param namespaceAware Whether the namespace aware excluded tasks should be returned
     * @return a list with all the pending update tasks for the specified schema
     */
    List<Map<String, Object>> getPendingTasksList(String schemaName, boolean pending, boolean excluded, boolean namespaceAware) throws RemoteException;

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
