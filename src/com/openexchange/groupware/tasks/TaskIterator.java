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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskStorage.StorageType;
import com.openexchange.server.DBPool;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * This class implements the SearchIterator for tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskIterator implements SearchIterator {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TaskIterator.class);

    /**
     * Context.
     */
    private final transient Context ctx;

    /**
     * unique identifier of the user for reminders.
     */
    private final transient int userId;

    /**
     * This ResultSet contains the data for this iterator.
     */
    private final transient ResultSet result;

    /**
     * Folder through that the objects are requested.
     */
    private final transient int folderId;

    /**
     * Wanted columns of the task.
     */
    private final transient int[] columns;

    /**
     * ACTIVE or DELETED.
     */
    private StorageType type;

    /**
     * Default constructor.
     * @param ctx Context.
     * @param userId unique identifier of the user if we have to load reminders.
     * @param result This iterator iterates over this ResultSet.
     * @param folderId Unique identifier of the folder through that the tasks
     * are requested.
     * @param columns Array of fields that the returned tasks should contain.
     * @param type ACTIVE or DELETED.
     */
    TaskIterator(final Context ctx, final int userId, final ResultSet result,
        final int folderId, final int[] columns, final StorageType type) {
        super();
        this.ctx = ctx;
        this.userId = userId;
        this.result = result;
        this.folderId = folderId;
        this.columns = columns;
        this.type = type;
    }

    /**
     * Local attribute to discover that hasNext() is called without calling
     * next().
     */
    private transient boolean hasNextCalled;

    /**
     * Remember the return value of hasNext() between multiple calls.
     */
    private transient boolean hasNextRetval;

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        if (!hasNextCalled) {
            hasNextCalled = true;
            try {
                hasNextRetval = result.next();
            } catch (SQLException e) {
                LOG.error("Error while getting next task result.", e);
            }
        }
        return hasNextRetval;
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSize() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Task next() throws SearchIteratorException {
        hasNextCalled = false;
        final Task retval = new Task();
        boolean readParticipants = false;
        boolean readAlarm = false;
        int counter = 1;
        for (int i = 0; i < columns.length; i++) {
            final Mapper mapper = Mapping.getMapping(columns[i]);
            if (mapper == null) {
                switch (columns[i]) {
                case Task.PARTICIPANTS:
                    readParticipants = true;
                    break;
                case Task.FOLDER_ID:
                    if (-1 == folderId) {
                        try {
                            retval.setParentFolderID(result.getInt(counter++));
                        } catch (SQLException e) {
                            throw new SearchIteratorException(new TaskException(
                                TaskException.Code.SQL_ERROR, e,
                                e.getMessage()));
                        }
                    } else {
                        retval.setParentFolderID(folderId);
                    }
                    break;
                case Task.ALARM:
                    readAlarm = true;
                    break;
                default:
                    throw new SearchIteratorException(new TaskException(
                        TaskException.Code.UNKNOWN_ATTRIBUTE, columns[i]));
                }
            } else {
                try {
                    mapper.fromDB(result, counter++, retval);
                } catch (SQLException e) {
                    throw new SearchIteratorException(new TaskException(
                        TaskException.Code.SQL_ERROR, e, e.getMessage()));
                }
            }
        }
        if (readParticipants) {
            loadParticipants(retval);
        }
        if (readAlarm) {
            try {
                Tools.loadReminder(ctx, userId, retval);
            } catch (TaskException e) {
                throw new SearchIteratorException(e);
            }
        }
        return retval;
    }

    /**
     * @param task Task.
     * @throws SearchIteratorException if an error occurs.
     */
    private void loadParticipants(final Task task)
        throws SearchIteratorException {
        try {
            task.setParticipants(TaskLogic.createParticipants(
                TaskStorage.getInstance().selectParticipants(ctx,
                task.getObjectID(), type)));
        } catch (TaskException e) {
            throw new SearchIteratorException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SearchIteratorException {
        Connection con = null;
        Statement stmt = null;
        try {
            stmt = result.getStatement();
            con = stmt.getConnection();
            result.close();
        } catch (SQLException e) {
            throw new SearchIteratorException(new TaskException(
                TaskException.Code.SQL_ERROR, e, e.getMessage()));
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    throw new SearchIteratorException(new TaskException(
                        TaskException.Code.SQL_ERROR, e, e.getMessage()));
                }
            }
            if (null != con) {
                DBPool.closeReaderSilent(ctx, con);
                con = null;
            }
        }
    }

}
