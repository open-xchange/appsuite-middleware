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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * Interface to different SQL implementations for storing tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
abstract class TaskStorage {

    /**
     * Singleton attribute.
     */
    private static final TaskStorage SINGLETON = new RdbTaskStorage();

    /**
     * Default constructor.
     */
    protected TaskStorage() {
        super();
    }

    /**
     * @return the singleton implementation.
     */
    public static TaskStorage getInstance() {
        return SINGLETON;
    }

    /**
     * Stores a task object.
     * @param ctx Context
     * @param con writable database connection.
     * @param task Task to store.
     * @param type storage type of the task (ACTIVE, DELETED).
     * @throws TaskException
     */
    abstract void insertTask(final Context ctx, final Connection con,
        final Task task, final StorageType type) throws TaskException;

    /**
     * Updates a task without touching folder mappings and participants.
     * @param ctx Context.
     * @param con writable database connection.
     * @param task Task.
     * @param lastRead timestamp when the client read the task last.
     * @param modified modified attributes.
     * @param type storage type of the task (ACTIVE, DELETED).
     * @throws TaskException if an exception occurs.
     */
    abstract void updateTask(Context ctx, Connection con, Task task,
        Date lastRead, int[] modified, StorageType type) throws TaskException;

    /**
     * Deletes a task.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task to delete.
     * @param lastRead timestamp when the task was last read.
     * @param type ACTIVE or DELETED.
     * @throws TaskException if the task has been changed in the meantime or an
     * exception occurred.
     */
    abstract void delete(Context ctx, Connection con, int taskId, Date lastRead,
        StorageType type) throws TaskException;

    /**
     * Counts tasks in a folder.
     * @param ctx Context.
     * @param userId unique identifier of the user. This parameter is only
     * required if only own tasks should be counted.
     * @param folderId unique identifier of the folder.
     * @param onlyOwn <code>true</code> if only own object can be seen in the
     * folder.
     * @param noPrivate <code>true</code> if private tasks should not be listed
     * (shared folder).
     * @return number of tasks in the folder.
     * @throws TaskException if an error occurs while counting the task.
     */
    abstract int countTasks(Context ctx, int userId, int folderId,
        boolean onlyOwn, boolean noPrivate) throws TaskException;

    /**
     * This method is currently unimplemented.
     * @param ctx Context.
     * @param taskIds unique identifier of the tasks.
     * @param columns attributes of the returned tasks that should be loaded.
     * @return a task iterator.
     * @throws TaskException if an error occurs.
     */
    protected abstract SearchIterator load(Context ctx, int[] taskIds,
        int[] columns) throws TaskException;

    /**
     * This method lists tasks in a folder.
     * @param ctx Context.
     * @param folderId unique identifier of the folder.
     * @param from Iterator should only return tasks that position in the list
     * is after this from.
     * @param until Iterator should only return tasks that position in the list
     * is before this from.
     * @param orderBy identifier of the column that should be used for sorting.
     * If no ordering is necessary give <code>0</code>.
     * @param orderDir sorting direction.
     * @param columns Columns of the tasks that should be loaded.
     * @param onlyOwn <code>true</code> if only own tasks can be seen.
     * @param userId unique identifier of the user (own tasks).
     * @param noPrivate <code>true</code> if private tasks should not be listed
     * (shared folder).
     * @return a SearchIterator for iterating over all returned tasks.
     * @throws TaskException if an error occurs while listing tasks.
     */
    public abstract TaskIterator list(Context ctx, int folderId,
        int from, int until, int orderBy, String orderDir, int[] columns,
        boolean onlyOwn, int userId, boolean noPrivate) throws TaskException;

    /**
     * Searches for tasks. Currently not all search options are available.
     * @param session Session.
     * @param search search object with the search parameters.
     * @param orderBy identifier of the column that should be used for sorting.
     * If no ordering is necessary give <code>0</code>.
     * @param orderDir sorting direction.
     * @param columns Attributes of the tasks that should be loaded.
     * @param all list of folders where all tasks can be seen.
     * @param own list of folders where own tasks can be seen.
     * @param shared list of shared folders with tasks that can be seen.
     * @return an iterator for all found tasks.
     * @throws TaskException if an error occurs.
     */
    abstract SearchIterator search(SessionObject session,
        TaskSearchObject search, int orderBy, String orderDir, int[] columns,
        List<Integer> all, List<Integer> own, List<Integer> shared)
        throws TaskException;

    /**
     * This method only reads the task without participants and folders.
     * @param context Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task.
     * @param type storage type of the task.
     * @return a task object without participants and folder.
     * @throws TaskException if an error occurs.
     */
    abstract Task selectTask(Context context, Connection con, int taskId,
        StorageType type) throws TaskException;

    /**
     * This method only reads the task without participants and folders.
     * @param ctx Context.
     * @param taskId unique identifier of the task.
     * @param type storage type of the task.
     * @return a task object without participants and folder.
     * @throws TaskException if an error occurs.
     */
    Task selectTask(final Context ctx, final int taskId, final StorageType type)
        throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectTask(ctx, con, taskId, type);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    public abstract boolean containsNotSelfCreatedTasks(SessionObject session,
        int folderId) throws TaskException;

}
