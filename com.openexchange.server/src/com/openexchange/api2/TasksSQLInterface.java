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

package com.openexchange.api2;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * API interface for tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface TasksSQLInterface {

    /**
     * Lists tasks in a folder from given parameter to given parameter
     *
     * @param folderId The Folder ID
     * @param from Start position in list
     * @param to End position in list
     * @param orderBy Column id to sort. 0 means no order by
     * @param order Order direction (asc or desc)
     * @param cols The columns filled to the dataobject
     * @return A SearchIterator contains Task objects
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Task> getTaskList(int folderId, int from, int to, int orderBy, Order order, int cols[]) throws OXException;

    /**
     * Lists all tasks that match the given search
     *
     * @param searchObject The SearchObject
     * @param cols fields that will be added to the data object
     * @return A SearchIterator contains Task
     * @throws OXException
     */
    SearchIterator<Task> getTasksByExtendedSearch(TaskSearchObject searchObj, int orderBy, Order order, int[] cols) throws OXException;
    
    /**
     * Find all tasks that match the criteria specified in the {@link TaskSearchObject}
     *  
     * @param searchObj the search object
     * @param orderBy order by the specified column
     * @param order order (asc or desc)
     * @param cols The columns filled to the dataobject
     * @return a {@link SearchIterator} with all found tasks
     * @throws OXException
     */
    SearchIterator<Task> findTask(TaskSearchObject searchObj, int orderBy, Order order, int[] cols) throws OXException;

    /**
     * Loads one tasks by the given ID
     *
     * @param objectId The Object ID
     * @return return the task object
     * @throws OXException, OXPermissionException
     */
    Task getTaskById(int objectId, int inFolder) throws OXException;

    /**
     * Lists all modified objects in a folder
     *
     * @param folderID The Folder ID
     * @param since all modification >= since
     * @return A SearchIterator contains Task objects
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Task> getModifiedTasksInFolder(int folderId, int[] cols, Date since) throws OXException;

    /**
     * Lists all deleted objects in a folder
     *
     * @param folderID The Folder ID
     * @param since all modification >= since
     * @return A SearchIterator contains Task objects
     * @throws OXException, OXPermissionException, OXFolderObjectNotFoundException
     */
    SearchIterator<Task> getDeletedTasksInFolder(int folderId, int[] cols, Date since) throws OXException;

    /**
     * Loads a range of tasks by the given IDs
     *
     * @param objectIdAndInFolder[] array with two dimensions. First dimension contains a seond array with two values. 1. value is object_id
     *            2. value if folder_id
     * @param cols The columns filled to the dataobject
     * @return A SearchIterator contains Task objects
     * @throws OXException
     */
    SearchIterator<Task> getObjectsById(int[][] objectIdAndInFolder, int cols[]) throws OXException;

    /**
     * Insert the task The lastModified attribute is empty, it will be filled with the actual timestamp.
     *
     * @param taskobject
     * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException,
     *             OXObjectNotFoundException
     */
    void insertTaskObject(Task taskobject) throws OXException;

    /**
     * update the Task the lastModified attribute is empty, it will be filled with the actual timestamp.
     *
     * @param taskobject
     * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException,
     *             OXObjectNotFoundException
     */
    void updateTaskObject(Task taskobject, int inFolder, Date clientLastModified) throws OXException;

    /**
     * deletes the Task The objectId is a mandatory field in the task object
     *
     * @param taskId the unique identifier of the task to delete.
     * @throws OXException, OXPermissionException, OXFolderNotFoundException, OXConflictException, OXMandatoryFieldException,
     *             OXObjectNotFoundException
     */
    void deleteTaskObject(int taskId, int inFolder, Date clientLastModified) throws OXException;

    /**
     * set the confirmation of the user
     *
     * @param objectId The object ID
     * @param userId The user ID
     * @param confirm The confirm status
     * @param confirmMessage The confirm message
     * @throws OXException if setting the confirmation fails.
     */
    Date setUserConfirmation(int objectId, int userId, int confirm, String confirmMessage) throws OXException;

    /**
     * Counts the tasks that a specific user can see in a folder. The context and the user are passed within the session given to
     * instantiate the implementation of this interface. The methods respects folder permissions having an effect on the tasks that can be
     * seen - this are currently a user can only see tasks created by him or non private tasks in a shared folder.
     *
     * @param folder in this folder the tasks should be counted.
     * @return
     * @throws OXException
     */
    int countTasks(FolderObject folder) throws OXException;
}
