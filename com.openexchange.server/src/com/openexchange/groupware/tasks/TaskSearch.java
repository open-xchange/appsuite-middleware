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
