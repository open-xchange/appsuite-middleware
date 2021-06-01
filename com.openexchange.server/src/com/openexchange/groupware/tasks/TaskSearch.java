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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * Interface to different SQL implementations for searching for tasks and its
 * participants.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a> (find method)
 */
abstract class TaskSearch {

    /**
     * Singleton instance.
     */
    private static final TaskSearch SINGLETON = new RdbTaskSearch();

    /**
     * Default constructor.
     */
    protected TaskSearch() {
        super();
    }

    /**
     * @return the singleton implementation.
     */
    public static TaskSearch getInstance() {
        return SINGLETON;
    }

    /**
     * Finds all delegated tasks of one user.
     * @param ctx Context.
     * @param con readable database connection.
     * @param userId unique identifier of the user.
     * @param type storage type of task that should be searched.
     * @return an int array with all task identifier found.
     * @throws OXException if an exception occurs.
     */
    abstract int[] findUserTasks(Context ctx, Connection con, int userId, StorageType type) throws OXException;

    /**
     * List tasks in a folder that are modified since the specified date.
     * @param ctx Context.
     * @param folderId unique identifier of the folder.
     * @param type ACTIVE or DELETED.
     * @param columns Columns of the tasks that should be loaded.
     * @param since timestamp since that the task are modified.
     * @param onlyOwn <code>true</code> if only own tasks can be seen.
     * @param userId unique identifier of the user (own tasks).
     * @param noPrivate <code>true</code> if private tasks should not be listed
     * (shared folder).
     * @return a SearchIterator for iterating over all returned tasks.
     * @throws OXException if an error occurs while listing modified tasks.
     */
    abstract SearchIterator<Task> listModifiedTasks(Context ctx, int folderId,
        StorageType type, int[] columns, Date since, boolean onlyOwn,
        int userId, boolean noPrivate) throws OXException;
    
    
    /**
     * Search for tasks via the find API
     * @param context context
     * @param userID userID
     * @param searchObject the search object
     * @param columns columns to select
     * @param orderBy order by column
     * @param order asc or desc
     * @param all a list of folders ids where all tasks can be seen.
     * @param own a list of folder ids owned by the user
     * @param shared a list of folder ids where the user has at least read access
     * @return
     * @throws OXException
     */
    public abstract SearchIterator<Task> find(Context context, int userID, TaskSearchObject searchObject, int[] columns, int orderBy, Order order, List<Integer> all, List<Integer> own, List<Integer> shared) throws OXException;
}
