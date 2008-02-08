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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class implementes the storage methods for tasks using a relational
 * database. The used SQL is currently optimized for MySQL. Maybe other
 * databases need SQL optimized in another way.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbTaskStorage extends TaskStorage {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(RdbTaskStorage.class);

    /**
     * This SQL statement counts the tasks in a folder.
     */
    private static final String COUNT_TASKS = "SELECT COUNT(task.id) "
        + "FROM task JOIN task_folder USING (cid,id) WHERE task.cid=? AND "
        + "task_folder.folder=?";

    /**
     * SQL statements for selecting modified tasks.
     */
    private static final Map<StorageType, String> LIST_MODIFIED =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * Additional where clause if only own objects can be seen.
     */
    private static final String ONLY_OWN = " AND created_from=?";

    /**
     * Additional where claus if no private tasks should be selected in a shared
     * folder.
     */
    private static final String NO_PRIVATE = " AND private=false";

    /**
     * Prevent instantiation
     */
    RdbTaskStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SearchIterator<Task> load(final Context ctx, final int[] taskIds,
        final int[] columns) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            // TODO implement load of multiple tasks.
            return null;
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Deletes a task from the given table.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task to delete.
     * @param lastRead timestamp when the task was last read.
     * @param type ACTIVE or DELETED.
     * @throws TaskException if the task has been changed in the meantime.
     */
    @Override
    void delete(final Context ctx, final Connection con,
        final int taskId, final Date lastRead, final StorageType type)
        throws TaskException {
        final String sql = "DELETE FROM @table@ WHERE cid=? AND id=? "
            + "AND last_modified<=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql.replace("@table@", SQL.TASK_TABLES
                .get(type)));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setLong(pos++, lastRead.getTime());
            final int count = stmt.executeUpdate();
            if (1 != count) {
                throw new TaskException(Code.MODIFIED);
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countTasks(final Context ctx, final int userId,
        final int folderId, final boolean onlyOwn, final boolean noPrivate)
        throws TaskException {
        int number = 0;
        number = countTasks(ctx, folderId, onlyOwn, userId, noPrivate);
        return number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskIterator list(final Context ctx, final int folderId,
        final int from, final int to, final int orderBy, final String orderDir,
        final int[] columns, final boolean onlyOwn, final int userId,
        final boolean noPrivate)
        throws TaskException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getFields(columns, false));
        sql.append(" FROM task JOIN task_folder USING (cid,id) ");
        sql.append("WHERE task.cid=? AND task_folder.folder=?");
        if (onlyOwn) {
            sql.append(ONLY_OWN);
        }
        if (noPrivate) {
            sql.append(NO_PRIVATE);
        }
        sql.append(SQL.getOrder(orderBy, orderDir));
        sql.append(SQL.getLimit(from, to));
        Connection con = null;
        try {
            con = DBPool.pickup(ctx);
            final PreparedStatement stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            if (onlyOwn) {
                stmt.setInt(pos++, userId);
            }
            return new TaskIterator(ctx, userId, stmt.executeQuery(),
                folderId, columns, StorageType.ACTIVE);
        } catch (SQLException e) {
            DBPool.closeWriterSilent(ctx, con);
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    SearchIterator<Task> search(final Context ctx, final int userId,
        final TaskSearchObject search, final int orderBy, final String orderDir,
        final int[] columns, final List<Integer> all, final List<Integer> own,
        final List<Integer> shared) throws TaskException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getFields(columns, true));
        sql.append(" FROM task JOIN task_folder USING (cid,id) ");
        sql.append("WHERE task.cid=? AND ");
        sql.append(SQL.allFoldersWhere(all, own, shared));
        final String rangeCondition = SQL.getRangeWhere(search);
        if (rangeCondition.length() > 0) {
            sql.append(" AND ");
            sql.append(rangeCondition);
        }
        final String patternCondition = SQL.getPatternWhere(search);
        if (patternCondition.length() > 0) {
            sql.append(" AND ");
            sql.append(patternCondition);
        }
        sql.append(SQL.getOrder(orderBy, orderDir));
        Connection con = null;
        try {
            con = DBPool.pickup(ctx);
            final PreparedStatement stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            if (all.size() + own.size() + shared.size() > 0) {
                for (int i : all) {
                    stmt.setInt(pos++, i);
                }
                for (int i : own) {
                    stmt.setInt(pos++, i);
                    stmt.setInt(pos++, userId);
                }
                for (int i : shared) {
                    stmt.setInt(pos++, i);
                }
            }
            if (rangeCondition.length() > 0) {
                for (Date date : search.getRange()) {
                    stmt.setTimestamp(pos++, new Timestamp(date.getTime()));
                }
            }
            if (patternCondition.length() > 0) {
                final String pattern = search.getPattern().replace('*', '%')
                    .replace('?', '_');
                stmt.setString(pos++, pattern);
                stmt.setString(pos++, pattern);
                stmt.setString(pos++, pattern);
            }
            if (LOG.isTraceEnabled()) {
				LOG.trace(stmt);
			}
            final ResultSet result = stmt.executeQuery();
//            return new PrefetchIterator(new TaskIterator(ctx,
//                session.getUserObject().getId(), result, -1, columns,
//                StorageType.ACTIVE));
            return new TaskIterator(ctx, userId, result, -1, columns,
                StorageType.ACTIVE);
        } catch (SQLException e) {
            DBPool.closeReaderSilent(ctx, con);
            throw new TaskException(Code.SEARCH_FAILED, e, e.getMessage());
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
    }

    /**
     * Counts the tasks in a folder.
     * @param ctx Context.
     * @param folderId unique identifier of the folder.
     * @param onlyOwn <code>true</code> if only own objects can be seen.
     * @param userId unique identifier of the user.
     * @param noPrivate <code>true</code> if the folder is a shared folder.
     * @return the number of tasks in that folder.
     * @throws TaskException if an error occurs.
     */
    private int countTasks(final Context ctx, final int folderId,
        final boolean onlyOwn, final int userId, final boolean noPrivate)
        throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        int number = 0;
        try {
            final StringBuilder sql = new StringBuilder(COUNT_TASKS);
            if (onlyOwn) {
                sql.append(ONLY_OWN);
            }
            if (noPrivate) {
                sql.append(NO_PRIVATE);
            }
            final PreparedStatement stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            if (onlyOwn) {
                stmt.setInt(pos++, userId);
            }
            final ResultSet result = stmt.executeQuery();
            if (result.next()) {
                number = result.getInt(1);
            }
            result.close();
            stmt.close();
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        return number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void insertTask(final Context ctx, final Connection con,
        final Task task, final StorageType type) throws TaskException {
        final StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO ");
        insert.append(SQL.TASK_TABLES.get(type));
        insert.append(" (");
        int values = 0;
        for (Mapper<?> mapper : Mapping.MAPPERS) {
            if (mapper.isSet(task)) {
                insert.append(mapper.getDBColumnName());
                insert.append(',');
                values++;
            }
        }
        insert.append("cid,id) VALUES (");
        for (int i = 0; i < values; i++) {
            insert.append("?,");
        }
        insert.append("?,?)");
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(insert.toString());
            int pos = 1;
            for (int i = 0; i < Mapping.MAPPERS.length; i++) {
                if (Mapping.MAPPERS[i].isSet(task)) {
                    Mapping.MAPPERS[i].toDB(stmt, pos++, task);
                }
            }
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, task.getObjectID());
            stmt.execute();
        } catch (DataTruncation e) {
            throw parseTruncated(e);
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Task selectTask(final Context ctx, final Connection con,
        final int taskId, final StorageType type) throws TaskException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getAllFields());
        sql.append(" FROM ");
        sql.append(SQL.TASK_TABLES.get(type));
        sql.append(" WHERE cid=? AND id=?");
        PreparedStatement stmt = null;
        ResultSet result = null;
        Task task = null;
        try {
            stmt = con.prepareStatement(sql.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, taskId);
            result = stmt.executeQuery();
            if (result.next()) {
                task = new Task();
                int pos = 1;
                for (Mapper<?> mapper : Mapping.MAPPERS) {
                    mapper.fromDB(result, pos++, task);
                }
                task.setObjectID(taskId);
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        if (null == task) {
            throw new TaskException(Code.TASK_NOT_FOUND, Integer.valueOf(taskId),
                Integer.valueOf(ctx.getContextId()));
        }
        return task;
    }

    /**
     * Updates a task in the database.
     * @param ctx Context.
     * @param con writable database connection.
     * @param type ACTIVE or DELETED.
     * @param task Task with the updated values.
     * @param lastRead timestamp when the client last requested the object.
     * @param modified attributes of the task that should be updated.
     * @throws TaskException if no task is updated.
     */
    @Override
    void updateTask(final Context ctx, final Connection con, final Task task,
        final Date lastRead, final int[] modified, final StorageType type)
        throws TaskException {
        if (modified.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(SQL.TASK_TABLES.get(type));
        sql.append(" SET ");
        for (int i : modified) {
            sql.append(Mapping.getMapping(i).getDBColumnName());
            sql.append("=?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(" WHERE cid=? AND id=? AND last_modified<=?");
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            for (int i : modified) {
                Mapping.getMapping(i).toDB(stmt, pos++, task);
            }
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, task.getObjectID());
            stmt.setLong(pos++, lastRead.getTime());
            final int updatedRows = stmt.executeUpdate();
            if (0 == updatedRows) {
                throw new TaskException(Code.MODIFIED);
            }
        } catch (DataTruncation e) {
            throw parseTruncated(e);
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * Parses the truncated fields out of the DataTruncation exception and
     * transforms this to a TaskException.
     * @param exc DataTruncation exception.
     * @return a TaskException.
     */
    private TaskException parseTruncated(final DataTruncation exc) {
        final String[] fields = DBUtils.parseTruncatedFields(exc);
        final StringBuilder sFields = new StringBuilder();
        for (String field : fields) {
            sFields.append(field);
            sFields.append(", ");
        }
        sFields.setLength(sFields.length() - 1);
        final TaskException tske = new TaskException(Code.TRUNCATED, exc,
            sFields.toString());
        final int[] truncated = SQL.findTruncated(fields);
        for (int i : truncated) {
            tske.addTruncatedId(i);
        }
        return tske;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsNotSelfCreatedTasks(final Context ctx,
        final int userId, final int folderId) throws TaskException {
        final String sql = "SELECT COUNT(id) FROM task JOIN task_folder "
            + "USING (cid,id) WHERE task.cid=? AND folder=? AND "
            + "created_from!=?";
        boolean retval = true;
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            final PreparedStatement stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, userId);
            final ResultSet result = stmt.executeQuery();
            if (result.next()) {
                retval = result.getInt(1) > 0;
            } else {
                throw new TaskException(Code.NO_COUNT_RESULT);
            }
            result.close();
            stmt.close();
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        return retval;
    }

    static {
        LIST_MODIFIED.put(StorageType.ACTIVE, "SELECT @fields@ FROM task JOIN "
            + "task_folder USING (cid,id) "
            + "WHERE task.cid=? AND folder=? AND last_modified>=?");
    }
}
