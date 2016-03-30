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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.server.impl.DBPool;

/**
 * Interface to different SQL implementations for storing tasks.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class TaskStorage {

    /**
     * An array holding the column IDs of attributes that are preserved when storing a 'tombstone' representing a deleted task. This
     * includes properties to identify the deleted task, as well as all other mandatory fields.
     */
    public static final int[] TOMBSTONE_ATTRS = {
        Task.OBJECT_ID, Task.FOLDER_ID, Task.UID, Task.FILENAME, Task.LAST_MODIFIED, Task.CREATION_DATE,
        Task.CREATED_BY, Task.MODIFIED_BY, Task.PRIVATE_FLAG, Task.RECURRENCE_TYPE, Task.NUMBER_OF_ATTACHMENTS
    };

    private static final int[] UPDATE_DUMMY_ATTRS = new int[] { Task.CREATION_DATE, Task.LAST_MODIFIED, Task.CREATED_BY, Task.MODIFIED_BY };

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
     *
     * @param ctx Context
     * @param con writable database connection.
     * @param task Task to store.
     * @param type storage type of the task (one of ACTIVE, DELETED).
     * @throws OXException if inserting the task fails.
     */
    public void insertTask(Context ctx, Connection con, Task task, StorageType type) throws OXException {
        insertTask(ctx, con, task, type, null);
    }

    /**
     * Stores a task object.
     *
     * @param ctx Context
     * @param con writable database connection.
     * @param task Task to store.
     * @param type storage type of the task (one of ACTIVE, DELETED).
     * @param columns Columns of the tasks that should be stored, or <code>null</code> if not restricted.
     * @throws OXException if inserting the task fails.
     */
    public abstract void insertTask(Context ctx, Connection con, Task task, StorageType type, int[] columns) throws OXException;

    /**
     * Stores a task object.
     *
     * @param ctx Context
     * @param con writable database connection.
     * @param task Task to store.
     * @param type storage type of the task (one of ACTIVE, DELETED).
     * @param optional <code>true</code> to ignore an already existing task.
     * @param columns Columns of the tasks that should be stored, or <code>null</code> if not restricted.
     * @throws OXException if some SQL or database connection problem occurs.
     */
    public void insertTask(final Context ctx, final Connection con, final Task task, final StorageType type, final boolean optional, int[] columns) throws OXException {
        if (optional) {
            final boolean exists = existsTask(ctx, con, task.getObjectID(), type);
            if (exists) {
                updateTask(ctx, con, task, new Date(Long.MAX_VALUE), UPDATE_DUMMY_ATTRS, type);
            } else {
                insertTask(ctx, con, task, type, columns);
            }
        } else {
            insertTask(ctx, con, task, type, columns);
        }
    }

    /**
     * Checks if a database entry for the task with the given identifier exists.
     *
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task to check.
     * @param type storage type of the task (one of ACTIVE, DELETED).
     * @return <code>true</code> if the storage contains a task with the given identifier.
     * @throws OXException if some SQL or database connection problem occurs.
     */
    public abstract boolean existsTask(Context ctx, Connection con, int taskId, StorageType type) throws OXException;

    /**
     * Updates a task without touching folder mappings and participants.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param task Task.
     * @param lastRead timestamp when the client read the task last.
     * @param modified modified attributes.
     * @param type storage type of the task (one of ACTIVE, DELETED).
     * @throws OXException if an exception occurs.
     */
    public abstract void updateTask(Context ctx, Connection con, Task task, Date lastRead, int[] modified, StorageType type) throws OXException;

    /**
     * Deletes a task.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task to delete.
     * @param lastRead timestamp when the task was last read.
     * @param type ACTIVE or DELETED.
     * @throws OXException if the task has been changed in the meantime or an exception occurred.
     */
    public void delete(final Context ctx, final Connection con, final int taskId, final Date lastRead, final StorageType type) throws OXException {
        delete(ctx, con, taskId, lastRead, type, true);
    }

    /**
     * Deletes a task.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task to delete.
     * @param lastRead timestamp when the task was last read.
     * @param type one of ACTIVE or DELETED.
     * @param sanityCheck <code>true</code> to check if task is really deleted.
     * @throws OXException if the task has been changed in the meantime or an exception occurred or there is no task to delete and
     *             sanityCheck is <code>true</code>.
     */
    public abstract void delete(Context ctx, Connection con, int taskId, Date lastRead, StorageType type, boolean sanityCheck) throws OXException;

    /**
     * Counts tasks in a folder.
     *
     * @param ctx Context.
     * @param userId unique identifier of the user. This parameter is only required if only own tasks should be counted.
     * @param folderId unique identifier of the folder.
     * @param onlyOwn <code>true</code> if only own object can be seen in the folder.
     * @param noPrivate <code>true</code> if private tasks should not be listed (shared folder).
     * @return number of tasks in the folder.
     * @throws OXException if an error occurs while counting the task.
     */
    public abstract int countTasks(Context ctx, int userId, int folderId, boolean onlyOwn, boolean noPrivate) throws OXException;

    /**
     * This method is currently unimplemented.
     *
     * @param ctx Context.
     * @param taskIds unique identifier of the tasks.
     * @param columns attributes of the returned tasks that should be loaded.
     * @return a task iterator.
     * @throws OXException if an error occurs.
     */
    protected abstract TaskIterator load(Context ctx, int[] taskIds, int[] columns) throws OXException;

    /**
     * This method lists tasks in a folder.
     *
     * @param ctx Context.
     * @param folderId unique identifier of the folder.
     * @param from Iterator should only return tasks that position in the list is after this from.
     * @param until Iterator should only return tasks that position in the list is before this from.
     * @param orderBy identifier of the column that should be used for sorting. If no ordering is necessary give <code>0</code>.
     * @param order sorting direction.
     * @param columns Columns of the tasks that should be loaded.
     * @param onlyOwn <code>true</code> if only own tasks can be seen.
     * @param userId unique identifier of the user (own tasks).
     * @param noPrivate <code>true</code> if private tasks should not be listed (shared folder).
     * @return a SearchIterator for iterating over all returned tasks.
     * @throws OXException if an error occurs while listing tasks.
     */
    public abstract TaskIterator list(Context ctx, int folderId, int from, int until, int orderBy, Order order, int[] columns, boolean onlyOwn, int userId, boolean noPrivate) throws OXException;

    /**
     * This method lists tasks in a folder.
     *
     * @param ctx Context.
     * @param folderId unique identifier of the folder.
     * @param from Iterator should only return tasks that position in the list is after this from.
     * @param until Iterator should only return tasks that position in the list is before this from.
     * @param orderBy identifier of the column that should be used for sorting. If no ordering is necessary give <code>0</code>.
     * @param order sorting direction.
     * @param columns Columns of the tasks that should be loaded.
     * @param onlyOwn <code>true</code> if only own tasks can be seen.
     * @param userId unique identifier of the user (own tasks).
     * @param noPrivate <code>true</code> if private tasks should not be listed (shared folder).
     * @param con Connection to database
     * @return a SearchIterator for iterating over all returned tasks.
     * @throws OXException if an error occurs while listing tasks.
     */
    public abstract TaskIterator list(Context ctx, int folderId, int from, int until, int orderBy, Order order, int[] columns, boolean onlyOwn, int userId, boolean noPrivate, Connection con) throws OXException;

    /**
     * Searches for tasks. Currently not all search options are available.
     * Folder lists must not be null but empty lists if there are no such folderId's.
     *
     * @param session Session.
     * @param search search object with the search parameters.
     * @param orderBy identifier of the column that should be used for sorting. If no ordering is necessary give <code>0</code>.
     * @param order sorting direction.
     * @param columns Attributes of the tasks that should be loaded.
     * @param all list of folders where all tasks can be seen.
     * @param own list of folders where own tasks can be seen.
     * @param shared list of shared folders with tasks that can be seen.
     * @return an iterator for all found tasks.
     * @throws OXException if an error occurs.
     */
    public abstract TaskIterator search(Context ctx, int userId, TaskSearchObject search, int orderBy, Order order, int[] columns, List<Integer> all, List<Integer> own, List<Integer> shared) throws OXException;

    /**
     * This method only reads the task without participants and folders.
     *
     * @param context Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task.
     * @param type storage type of the task.
     * @return a task object without participants and folder.
     * @throws OXException if an error occurs.
     */
    public abstract Task selectTask(Context context, Connection con, int taskId, StorageType type) throws OXException;

    /**
     * This method only reads the task without participants and folders.
     *
     * @param ctx Context.
     * @param taskId unique identifier of the task.
     * @param type storage type of the task.
     * @return a task object without participants and folder.
     * @throws OXException if an error occurs.
     */
    public Task selectTask(final Context ctx, final int taskId, final StorageType type) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            return selectTask(ctx, con, taskId, type);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    public abstract boolean containsNotSelfCreatedTasks(Context ctx, Connection con, int userId, int folderId) throws OXException;

    /**
     * Counts the number of tasks existing for the given context.
     * @param ctx Context.
     * @return the number of tasks stored for the given context.
     * @throws OXException if reading from the persistent storage fails.
     */
    abstract int countTasks(Context ctx) throws OXException;

    /**
     * Counts the number of tasks existing for the given context.
     * @param contextId The context id.
     * @param con The database connection to use.
     * @return the number of tasks stored for the given context.
     * @throws OXException if reading from the persistent storage fails.
     */
    abstract int countTasks(int contextId, Connection con) throws SQLException, OXException;
}
