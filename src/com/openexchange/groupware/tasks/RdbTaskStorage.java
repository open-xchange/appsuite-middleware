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
import static com.openexchange.tools.sql.DBUtils.rollback;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.PrefetchIterator;
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
     * Tables for tasks.
     */
    private static final Map<StorageType, String> TASK_TABLES =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * Tables for participants.
     */
    private static final Map<StorageType, String> PARTS_TABLES =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statements for selecting modified tasks.
     */
    private static final Map<StorageType, String> LIST_MODIFIED =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statements for selecting participants.
     */
    private static final Map<StorageType, String> SELECT_PARTS =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statements for inserting participants.
     */
    private static final Map<StorageType, String> INSERT_PARTS =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statements for deleting participants.
     */
    private static final Map<StorageType, String> DELETE_PARTS =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statement for selecting external participants.
     */
    private static final Map<StorageType, String> SELECT_EXTERNAL =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statement for inserting external participants.
     */
    private static final Map<StorageType, String> INSERT_EXTERNAL =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statement for deleting external participants.
     */
    private static final Map<StorageType, String> DELETE_EXTERNAL =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statements for deleting folder mappings.
     */
    private static final Map<StorageType, String> FOLDER_TABLES =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statements for inserting folder mappings.
     */
    private static final Map<StorageType, String> INSERT_FOLDER =
        new EnumMap<StorageType, String>(StorageType.class);

    /**
     * SQL statement for selecting all folders of a task.
     */
    private static final String SELECT_FOLDERLINK = "SELECT folder,user "
        + "FROM task_folder WHERE cid=? AND id=?";

    /**
     * SQL statement for selecting a folder mapping by the folder identifier.
     */
    private static final String SELECT_FOLDER_BY_IDENTIFIER = "SELECT user "
        + "FROM task_folder WHERE cid=? AND id=? AND folder=?";

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
     * Name of the table that stores the participants.
     */
    private static final String PARTICIPANT_TABLE = "task_participant";

    /**
     * Prevent instanciation.
     */
    RdbTaskStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert(final Context ctx, final Task task,
        final Set<TaskParticipant> participants, final Set<Folder> folders)
        throws TaskException {
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            final int taskId = IDGenerator.getId(ctx, Types.TASK, con);
            task.setObjectID(taskId);
            insertTask(ctx, con, task, StorageType.ACTIVE);
            if (participants.size() != 0) {
                insertParticipants(ctx, con, taskId, participants,
                    StorageType.ACTIVE);
            }
            insertFolder(ctx, con, StorageType.ACTIVE, taskId, folders);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.INSERT_FAILED, e, e.getMessage());
        } catch (TaskException e) {
            rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                final TaskException tske = new TaskException(Code.AUTO_COMMIT,
                    e);
                LOG.error(tske.getMessage(), tske);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final Context ctx, final Task task,
        final Date lastRead, final int[] modified,
        final Set<TaskParticipant> add, final Set<TaskParticipant> remove,
        final Set<Folder> addFolder, final Set<Folder> removeFolder)
        throws TaskException {
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            if (modified.length > 0) {
                updateTask(ctx, con, StorageType.ACTIVE, task, lastRead,
                    modified);
            }
            if (null != add && add.size() > 0) {
                insertParticipants(ctx, con, task.getObjectID(), add,
                    StorageType.ACTIVE);
                deleteParticipants(ctx, con, task.getObjectID(), add,
                    StorageType.REMOVED, false);
            }
            if (null != remove && remove.size() > 0) {
                insertParticipants(ctx, con, task.getObjectID(), remove,
                    StorageType.REMOVED);
                deleteParticipants(ctx, con, task.getObjectID(), remove,
                    StorageType.ACTIVE, true);
            }
            if (null != removeFolder && removeFolder.size() > 0) {
                final int[] identifier = new int[removeFolder.size()];
                int pos = 0;
                for (Folder folder : removeFolder) {
                    identifier[pos++] = folder.getIdentifier();
                }
                deleteFolder(ctx, con, task.getObjectID(), StorageType.ACTIVE,
                    "folder", identifier);
            }
            if (null != addFolder && addFolder.size() > 0) {
                insertFolder(ctx, con, StorageType.ACTIVE,
                    task.getObjectID(), addFolder);
            }
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.UPDATE_FAILED, e, e.getMessage());
        } catch (TaskException e) {
            rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error("Problem setting auto commit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TaskIterator load(final Context ctx, final int[] taskIds,
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
     * {@inheritDoc}
     */
    @Override
    public void delete(final Context ctx, final int taskId, final int userId,
        final Date lastRead) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            final Task task = selectTask(ctx, taskId);
            task.setLastModified(new Date());
            task.setModifiedBy(userId);
            insertTask(ctx, con, task, StorageType.DELETED);
            deleteParticipants(ctx, con, taskId);
            deleteFolder(ctx, con, taskId);
            delete(ctx, con, StorageType.ACTIVE, taskId, lastRead);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.DELETE_FAILED, e, e.getMessage());
        } catch (TaskException e) {
            rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error("Problem setting auto commit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    /**
     * Deletes a task from the given table.
     * @param ctx Context.
     * @param con writable database connection.
     * @param type ACTIVE or DELETED.
     * @param taskId unique identifier of the task to delete.
     * @param lastRead timestamp when the task was last read.
     * @throws SQLException if an SQL error occurs.
     * @throws TaskException if the task has been changed in the meantime.
     */
    private void delete(final Context ctx, final Connection con,
        final StorageType type, final int taskId, final Date lastRead)
        throws SQLException, TaskException {
        final String sql = "DELETE FROM @table@ WHERE cid=? AND id=? "
            + "AND last_modified<=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql.replace("@table@", TASK_TABLES
                .get(type)));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setLong(pos++, lastRead.getTime());
            final int count = stmt.executeUpdate();
            if (1 != count) {
                final Task forTime = selectTask(ctx, con, type, taskId);
                throw new TaskException(Code.MODIFIED,
                    forTime.getLastModified().getTime(), lastRead.getTime());
            }
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
    public SearchIterator list(final Context ctx, final int folderId,
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
            return new PrefetchIterator<Task>(new TaskIterator(ctx, userId,
                stmt.executeQuery(), folderId, columns, StorageType.ACTIVE));
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
    public SearchIterator listModifiedTasks(final Context ctx,
        final int folderId, final StorageType type, final int[] columns,
        final Date since, final boolean onlyOwn, final int userId,
        final boolean noPrivate) throws TaskException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getFields(columns, false));
        sql.append(" FROM ");
        final String taskTable = TASK_TABLES.get(type);
        sql.append(taskTable);
        sql.append(" JOIN ");
        sql.append(FOLDER_TABLES.get(type));
        sql.append(" USING (cid,id) WHERE ");
        sql.append(taskTable);
        sql.append(".cid=? AND folder=? AND last_modified>=?");
        if (onlyOwn) {
            sql.append(ONLY_OWN);
        }
        if (noPrivate) {
            sql.append(NO_PRIVATE);
        }
        if (StorageType.DELETED == type) {
            sql.append(" UNION SELECT ");
            sql.append(SQL.getFields(columns, false));
            sql.append(" FROM task JOIN task_removedparticipant ON ");
            sql.append("task.cid=task_removedparticipant.cid AND ");
            sql.append("task.id=task_removedparticipant.task ");
            sql.append("WHERE task.cid=? AND folder=? AND last_modified>=?");
            if (onlyOwn) {
                sql.append(ONLY_OWN);
            }
            if (noPrivate) {
                sql.append(NO_PRIVATE);
            }
        }
        Connection con = null;
        try {
            con = DBPool.pickup(ctx);
            final PreparedStatement stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setLong(pos++, since.getTime());
            if (onlyOwn) {
                stmt.setInt(pos++, userId);
            }
            if (StorageType.DELETED == type) {
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, folderId);
                stmt.setLong(pos++, since.getTime());
                if (onlyOwn) {
                    stmt.setInt(pos++, userId);
                }
            }
            return new PrefetchIterator(new TaskIterator(ctx, userId,
                stmt.executeQuery(), folderId, columns, type));
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
    SearchIterator search(final SessionObject session,
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
        final Context ctx = session.getContext();
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
            LOG.trace(stmt);
            final ResultSet result = stmt.executeQuery();
            return new PrefetchIterator(new TaskIterator(ctx,
                session.getUserObject().getId(), result, -1, columns,
                StorageType.ACTIVE));
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
     * Inserts a task into the database.
     * @param ctx Context.
     * @param con writable connection.
     * @param task Task object to insert.
     * @param type ACTIVE or DELETED
     * @throws SQLException if an error occurs.
     * @throws TaskException if a data truncation occurs.
     */
    private void insertTask(final Context ctx, final Connection con,
        final Task task, final StorageType type) throws SQLException,
        TaskException {
        final StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO ");
        insert.append(TASK_TABLES.get(type));
        insert.append(" (");
        int values = 0;
        for (Mapper mapper : Mapping.MAPPERS) {
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
            stmt = con.prepareStatement(insert
                .toString());
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
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    Task selectTask(final Context ctx, final int taskId) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectTask(ctx, con, StorageType.ACTIVE, taskId);
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * @param ctx Context.
     * @param con readable database connection.
     * @param type ACTIVE or DELETED.
     * @param taskId unique identifier of the task to read.
     * @return the selected task or <code>null</code> if it doesn't exist.
     * @throws SQLException if an sql problem occurs.
     * @throws TaskException if no task can be found.
     */
    private Task selectTask(final Context ctx, final Connection con,
        final StorageType type, final int taskId) throws SQLException,
        TaskException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getAllFields());
        sql.append(" FROM ");
        sql.append(TASK_TABLES.get(type));
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
                for (Mapper mapper : Mapping.MAPPERS) {
                    mapper.fromDB(result, pos++, task);
                }
                task.setObjectID(taskId);
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        if (null == task) {
            throw new TaskException(Code.TASK_NOT_FOUND, taskId,
                ctx.getContextId());
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
     * @param attrs attributes of the task that should be updated.
     * @throws SQLException if a SQL problem occurs.
     * @throws TaskException if no task is updated.
     */
    private void updateTask(final Context ctx, final Connection con,
        final StorageType type, final Task task, final Date lastRead,
        final int[] attrs) throws SQLException, TaskException {
        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(TASK_TABLES.get(type));
        sql.append(" SET ");
        for (int i : attrs) {
            sql.append(Mapping.getMapping(i).getDBColumnName());
            sql.append("=?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(" WHERE cid=? AND id=? AND last_modified<=?");
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            for (int i : attrs) {
                Mapping.getMapping(i).toDB(stmt, pos++, task);
            }
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, task.getObjectID());
            stmt.setLong(pos++, lastRead.getTime());
            final int updatedRows = stmt.executeUpdate();
            if (0 == updatedRows) {
                final Task forTime = selectTask(ctx, con, type,
                    task.getObjectID());
                throw new TaskException(Code.MODIFIED,
                    forTime.getLastModified().getTime(), lastRead.getTime());
            }
        } catch (DataTruncation e) {
            throw parseTruncated(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * Parses the truncated fields out of the DataTruncation exception and
     * transforms this to a TaskException.
     * @param e DataTruncation exception.
     * @return a TaskException.
     */
    private TaskException parseTruncated(final DataTruncation e) {
        final String[] fields = DBUtils.parseTruncatedFields(e);
        final StringBuilder sFields = new StringBuilder();
        for (String field : fields) {
            sFields.append(field);
            sFields.append(", ");
        }
        sFields.setLength(sFields.length() - 1);
        final TaskException tske = new TaskException(Code.TRUNCATED, e,
            sFields.toString());
        final int[] truncated = SQL.findTruncated(fields);
        for (int i : truncated) {
            tske.addTruncatedId(i);
        }
        return tske;
    }

    /**
     * Inserts a folder in that the task will appear.
     * @param ctx Context.
     * @param con writable database connection.
     * @param type ACTIVE or DELETED.
     * @param taskId unique identifier of the task.
     * @param folders folders in that the task should appear.
     * @throws SQLException if a SQL problem occurs.
     */
    private void insertFolder(final Context ctx, final Connection con,
        final StorageType type, final int taskId, final Set<Folder> folders)
        throws SQLException {
        final PreparedStatement stmt = con.prepareStatement(INSERT_FOLDER
            .get(type));
        int pos = 1;
        stmt.setInt(pos++, ctx.getContextId());
        stmt.setInt(pos++, taskId);
        for (Folder folder : folders) {
            pos = 3;
            stmt.setInt(pos++, folder.getIdentifier());
            stmt.setInt(pos++, folder.getUser());
            stmt.addBatch();
        }
        stmt.executeBatch();
        stmt.close();
    }

    private void deleteFolder(final Context ctx, final Connection con,
        final int taskId) throws SQLException {
        Set<Folder> folders = selectFolder(ctx, con, taskId);
        insertFolder(ctx, con, StorageType.DELETED, taskId, folders);
        deleteFolder(ctx, con, taskId, folders, StorageType.ACTIVE);
    }

    private void deleteFolder(final Context ctx, final Connection con,
        final int taskId, final Set<Folder> folders, final StorageType type)
        throws SQLException {
        final int[] folderIds = new int[folders.size()];
        int pos = 0;
        for (Folder folder : folders) {
            folderIds[pos++] = folder.getIdentifier();
        }
        deleteFolder(ctx, con, taskId, type, "folder", folderIds);
    }

    /**
     * Deletes the folder mappings for the given participants.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task.
     * @param table StorageType.ACTIVE or StorageType.DELETED.
     * @param type <code>user</code> or <code>folder</code>. the given
     * identifier are user identifier or folder identifier.
     * @param ids user or folder identifier that should be deleted.
     * @throws SQLException if a SQL problem occurs.
     */
    private void deleteFolder(final Context ctx, final Connection con,
        final int taskId, final StorageType table, final String type,
        final int[] ids) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ");
        sql.append(FOLDER_TABLES.get(table));
        sql.append(" WHERE cid=? AND id=? AND ");
        sql.append(type);
        sql.append(" IN (");
        for (int i = 0; i < ids.length; i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(')');
        final PreparedStatement stmt = con.prepareStatement(sql.toString());
        int counter = 1;
        stmt.setInt(counter++, ctx.getContextId());
        stmt.setInt(counter++, taskId);
        for (int participant : ids) {
            stmt.setInt(counter++, participant);
        }
        final int deleted = stmt.executeUpdate();
        stmt.close();
        if (ids.length != deleted) {
            LOG.error(new TaskException(Code.FOLDER_DELETE_WRONG, ids.length,
                deleted));
        }
    }

    /**
     * {@inheritDoc}
     */
    Set<TaskParticipant> selectParticipants(final Context ctx,
        final int taskId, final StorageType type) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            final Set<TaskParticipant> participants =
                new HashSet<TaskParticipant>();
            participants.addAll(selectInternal(ctx, con, taskId, type));
            participants.addAll(selectExternal(ctx, con, taskId, type));
            return participants;
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    TaskInternalParticipant selectParticipant(final Context ctx,
        final int taskId, final int userId) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectParticipant(ctx, con, taskId, userId);
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Reads a participant from the database.
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task where the user is
     * participant.
     * @param userId unique identifier of the user.
     * @return the particpant object.
     * @throws SQLException if a SQL problem occurs.
     * @throws TaskException if the participant can't be loaded.
     */
    private TaskInternalParticipant selectParticipant(final Context ctx,
        final Connection con, final int taskId, final int userId)
        throws SQLException, TaskException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        TaskInternalParticipant participant = null;
        try {
            stmt = con.prepareStatement(
                SELECT_PARTS.get(StorageType.ACTIVE) + " AND user=?");
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            if (result.next()) {
                final UserParticipant userParticipant = new UserParticipant();
                pos = 1;
                userParticipant.setIdentifier(result.getInt(pos++));
                Integer groupId = result.getInt(pos++);
                if (result.wasNull()) {
                    groupId = null;
                }
                participant = new TaskInternalParticipant(userParticipant,
                    groupId);
                participant.setConfirm(result.getInt(pos++));
                participant.setConfirmMessage(result.getString(pos++));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        if (null == participant) {
            throw new TaskException(Code.PARTICIPANT_NOT_FOUND, userId,
                taskId);
        }
        return participant;
    }

    /**
     * Selects participants.
     * @param ctx Context.
     * @param con readable database connection.
     * @param taskId unique identifier of the task that participants should be
     * loaded.
     * @param type type of participants that should be loaded.
     * @return a set of participants.
     * @throws SQLException if a SQL problem occurs.
     */
    private Set<TaskInternalParticipant> selectInternal(final Context ctx,
        final Connection con, final int taskId, final StorageType type)
        throws SQLException {
        final PreparedStatement stmt = con.prepareStatement(
            SELECT_PARTS.get(type));
        stmt.setInt(1, ctx.getContextId());
        stmt.setInt(2, taskId);
        final ResultSet result = stmt.executeQuery();
        final Set<TaskInternalParticipant> participants =
            new HashSet<TaskInternalParticipant>();
        while (result.next()) {
            final UserParticipant participant = new UserParticipant();
            int pos = 1;
            participant.setIdentifier(result.getInt(pos++));
            Integer groupId = result.getInt(pos++);
            if (result.wasNull()) {
                groupId = null;
            }
            final TaskInternalParticipant taskParticipant =
                new TaskInternalParticipant(participant, groupId);
            taskParticipant.setConfirm(result.getInt(pos++));
            taskParticipant.setConfirmMessage(result.getString(pos++));
            if (StorageType.REMOVED == type) {
                final int folderId = result.getInt(pos++);
                if (0 == folderId) {
                    taskParticipant.setFolderId(UserParticipant.NO_PFID);
                } else {
                    taskParticipant.setFolderId(folderId);
                }
            }
            participants.add(taskParticipant);
        }
        result.close();
        stmt.close();
        return participants;
    }

    private Set<TaskExternalParticipant> selectExternal(final Context ctx,
        final Connection con, final int taskId, final StorageType type)
        throws SQLException {
        final Set<TaskExternalParticipant> participants =
            new HashSet<TaskExternalParticipant>();
        if (StorageType.REMOVED != type) {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement(SELECT_EXTERNAL.get(type));
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, taskId);
                result = stmt.executeQuery();
                while (result.next()) {
                    final ExternalUserParticipant external =
                        new ExternalUserParticipant();
                    int pos = 1;
                    external.setEmailAddress(result.getString(pos++));
                    external.setDisplayName(result.getString(pos++));
                    final TaskExternalParticipant participant =
                        new TaskExternalParticipant(external);
                    participants.add(participant);
                }
            } finally {
                closeSQLStuff(result, stmt);
            }
        }
        return participants;
    }

    /**
     * Deletes participants.
     * @param ctx Context.
     * @param con writable connection.
     * @param taskId unique identifier of the task.
     * @param participants participants to delete.
     * @param type type of participants to delete.
     * @param sanityCheck if <code>true</code> it will be checked if all given
     * participants have been deleted.
     * @throws SQLException if a SQL error occurs.
     */
    private void deleteParticipants(final Context ctx, final Connection con,
        final int taskId, final Set<TaskParticipant> participants,
        final StorageType type, final boolean sanityCheck)
        throws SQLException {
        if (null == participants || participants.size() == 0) {
            return;
        }
        deleteInternals(ctx, con, taskId, Tools.extractInternal(participants),
            type, sanityCheck);
        deleteExternals(ctx, con, taskId, Tools.extractExternal(participants),
            type, sanityCheck);
    }

    private void deleteInternals(final Context ctx, final Connection con,
        final int taskId, final Set<TaskInternalParticipant> participants,
        final StorageType type, final boolean sanityCheck) throws SQLException {
        final int[] identifier = new int[participants.size()];
        int pos = 0;
        for (TaskInternalParticipant participant : participants) {
            identifier[pos++] = participant.getIdentifier();
        }
        deleteInternals(ctx, con, taskId, identifier, type, sanityCheck);
    }

    private void deleteExternals(final Context ctx, final Connection con,
        final int taskId, final Set<TaskExternalParticipant> participants,
        final StorageType type, final boolean sanityCheck) throws SQLException {
        final String[] mail = new String[participants.size()];
        int pos = 0;
        for (TaskExternalParticipant participant : participants) {
            mail[pos++] = participant.getMail();
        }
        deleteExternals(ctx, con, taskId, mail, type, sanityCheck);
    }

    /**
     * Deletes participants.
     * @param ctx Context.
     * @param con writable connection.
     * @param taskId unique identifier of the task.
     * @param participants participants to delete.
     * @param type type of participants to delete.
     * @param sanityCheck if <code>true</code> it will be checked if all given
     * participants have been deleted.
     * @throws SQLException if a SQL error occurs.
     */
    private void deleteInternals(final Context ctx, final Connection con,
        final int taskId, final int[] participants, final StorageType type,
        final boolean sanityCheck) throws SQLException {
        if (participants.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append(DELETE_PARTS.get(type));
        for (int i = 0; i < participants.length; i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(')');
        PreparedStatement stmt = null;
        final int deleted;
        try {
            stmt = con.prepareStatement(sql.toString());
            int counter = 1;
            stmt.setInt(counter++, ctx.getContextId());
            stmt.setInt(counter++, taskId);
            for (int participant : participants) {
                stmt.setInt(counter++, participant);
            }
            deleted = stmt.executeUpdate();
        } finally {
            closeSQLStuff(null, stmt);
        }
        if (sanityCheck && participants.length != deleted) {
            final TaskException tske = new TaskException(Code
                .PARTICIPANT_DELETE_WRONG, participants.length, deleted);
            LOG.error(tske.getMessage(), tske);
        }
    }

    private void deleteExternals(final Context ctx, final Connection con,
        final int taskId, final String[] participants, final StorageType type,
        final boolean sanityCheck) throws SQLException {
        if (0 == participants.length || StorageType.REMOVED == type) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append(DELETE_EXTERNAL.get(type));
        for (int i = 0; i < participants.length; i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(')');
        PreparedStatement stmt = null;
        int deleted = 0;
        try {
            stmt = con.prepareStatement(sql.toString());
            int counter = 1;
            stmt.setInt(counter++, ctx.getContextId());
            stmt.setInt(counter++, taskId);
            for (String participant : participants) {
                stmt.setString(counter++, participant);
            }
            deleted = stmt.executeUpdate();
        } finally {
            closeSQLStuff(null, stmt);
        }
        if (sanityCheck && participants.length != deleted) {
            final TaskException e = new TaskException(Code
                .PARTICIPANT_DELETE_WRONG, participants.length, deleted);
            LOG.error(e.getMessage(), e);
        }
    }

    private void deleteParticipants(final Context ctx, final Connection con,
        final int taskId) throws SQLException, TaskException {
        final Set<TaskInternalParticipant> participants =
            selectInternal(ctx, con, taskId, StorageType.ACTIVE);
        insertInternals(ctx, con, taskId, participants,
            StorageType.DELETED);
        deleteInternals(ctx, con, taskId, participants,
            StorageType.ACTIVE, true);
        final Set<TaskInternalParticipant> removed =
            selectInternal(ctx, con, taskId, StorageType.REMOVED);
        insertInternals(ctx, con, taskId, removed, StorageType.DELETED);
        deleteInternals(ctx, con, taskId, removed, StorageType.REMOVED,
            true);
        final Set<TaskExternalParticipant> externals =
            selectExternal(ctx, con, taskId, StorageType.ACTIVE);
        insertExternals(ctx, con, taskId, externals, StorageType.DELETED);
        deleteExternals(ctx, con, taskId, externals, StorageType.ACTIVE, true);
    }

    private void insertParticipants(final Context ctx, final Connection con,
        final int taskId, final Set<TaskParticipant> participants,
        final StorageType type) throws SQLException, TaskException {
        final Set<TaskInternalParticipant> internals =
            Tools.extractInternal(participants);
        final Set<TaskExternalParticipant> externals =
            Tools.extractExternal(participants);
        insertInternals(ctx, con, taskId, internals, type);
        insertExternals(ctx, con, taskId, externals, type);
    }

    /**
     * Inserts an internal participant.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task the participant belongs to.
     * @param participants participants to insert.
     * @param type type of participant that should be inserted.
     * @throws SQLException if a SQL problem occurs.
     * @throws TaskException if a data truncation occurs.
     */
    private void insertInternals(final Context ctx, final Connection con,
        final int taskId, final Set<TaskInternalParticipant> participants,
        final StorageType type) throws SQLException, TaskException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_PARTS.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            for (TaskInternalParticipant participant : participants) {
                pos = 3;
                stmt.setInt(pos++, participant.getIdentifier());
                if (null == participant.getGroupId()) {
                    stmt.setNull(pos++, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(pos++, participant.getGroupId());
                }
                stmt.setInt(pos++, participant.getConfirm());
                if (null == participant.getConfirmMessage()) {
                    stmt.setNull(pos++, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos++, participant.getConfirmMessage());
                }
                if (StorageType.REMOVED == type) {
                    final int folderId = participant.getFolderId();
                    if (UserParticipant.NO_PFID == folderId) {
                        stmt.setInt(pos++, 0);
                    } else {
                        stmt.setInt(pos++, participant.getFolderId());
                    }
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (DataTruncation e) {
            final int truncated = -1; // No ID defined here
            final TaskException tske = new TaskException(Code.TRUNCATED, e,
                "description");
            tske.addTruncatedId(truncated);
            throw tske;
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * Inserts external participants.
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task the participant belongs to.
     * @param participants participants to insert.
     * @param type type of participant that should be inserted.
     * @throws TaskException if a data truncation occurs.
     * @throws SQLException if a SQL problem occurs.
     */
    private void insertExternals(final Context ctx, final Connection con,
        final int taskId, final Set<TaskExternalParticipant> participants,
        final StorageType type) throws TaskException, SQLException {
        if (0 == participants.size() || StorageType.REMOVED == type) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_EXTERNAL.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            for (TaskExternalParticipant participant : participants) {
                pos = 3;
                stmt.setString(pos++, participant.getMail());
                final String displayName = participant.getDisplayName();
                if (null == displayName) {
                    stmt.setNull(pos++, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos++, displayName);
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (DataTruncation e) {
            final int truncated = -1; // No ID defined here
            final TaskException tske = new TaskException(Code.TRUNCATED, e,
                "mail");
            tske.addTruncatedId(truncated);
            throw tske;
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    void updateParticipant(final Context ctx, final int taskId,
        final TaskInternalParticipant participant) throws TaskException {
        final String sql = "UPDATE task_participant SET accepted=?,"
            + "description=? WHERE cid=? AND task=? AND user=?";
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, participant.getConfirm());
            stmt.setString(pos++, participant.getConfirmMessage());
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setInt(pos++, participant.getIdentifier());
            stmt.executeUpdate();
        } catch (DataTruncation e) {
            final int truncated = -1; // No ID defined here
            final TaskException tske = new TaskException(Code.TRUNCATED, e,
                "description");
            tske.addTruncatedId(truncated);
            throw tske;
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deleteGroup(final Context ctx, final Connection con, final int groupId)
        throws TaskException {
        try {
            for (String table : PARTS_TABLES.values()) {
                deleteGroup(ctx, con, table, groupId);
            }
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        }
    }

    /**
     * Deletes a group from the given participant table.
     * @param ctx Context.
     * @param con writable database connection.
     * @param table participant table (task_participant,
     * task_removedparticipant, del_task_participant)
     * @param groupId unique identifier of the group that will be deleted.
     * @throws SQLException if an SQL problem occurs.
     */
    void deleteGroup(final Context ctx, final Connection con,
        final String table, final int groupId) throws SQLException {
        final String sql = "UPDATE @table@ SET group_id=? "
            + "WHERE cid=? AND group_id=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql.replace("@table@", table));
            int pos = 1;
            stmt.setNull(pos++, java.sql.Types.INTEGER);
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, groupId);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deleteUser(final Context ctx, final Connection readCon,
        final Connection writeCon, final int userId) throws TaskException {
        try {
            removeUserFromParticipants(ctx, readCon, writeCon, userId);
            assignToAdmin(ctx, readCon, writeCon, userId);
            deletePrivateTasks(ctx, readCon, writeCon, userId);
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        }
    }

    /**
     * Searches all tasks where the user is participant and deletes the user
     * from the participants. The folder mappings are also deleted.
     * @param ctx Context.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @param userId unique identifier of the to delete user.
     * @throws SQLException if a SQL error occurs.
     */
    private void removeUserFromParticipants(final Context ctx,
        final Connection readCon, final Connection writeCon, final int userId)
        throws SQLException {
        final String sql = "SELECT task FROM task_participant WHERE cid=? "
            + "AND user=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> tasks = new ArrayList<Integer>();
        try {
            stmt = readCon.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos, userId);
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (int task : tasks) {
            deleteInternals(ctx, writeCon, task, new int[] { userId },
                StorageType.ACTIVE, true);
            deleteFolder(ctx, writeCon, task, StorageType.ACTIVE, "user",
                new int[] { userId });
        }
        try {
            stmt = readCon.prepareStatement(sql.replace(PARTICIPANT_TABLE,
                "task_removedparticipant"));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            tasks.clear();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (int task : tasks) {
            deleteInternals(ctx, writeCon, task, new int[] { userId },
                StorageType.REMOVED, true);
        }
        try {
            stmt = readCon.prepareStatement(sql.replace(PARTICIPANT_TABLE,
                "del_task_participant"));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            tasks.clear();
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (int task : tasks) {
            deleteInternals(ctx, writeCon, task, new int[] { userId },
                StorageType.DELETED, true);
            deleteFolder(ctx, writeCon, task, StorageType.DELETED, "user",
                new int[] { userId });
        }
    }

    /**
     * Assign delegated tasks from a user to the mailadmin.
     * @param ctx Context.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @param userId unique identifier of the to delete user.
     * @throws SQLException if a SQL problem occurs.
     * @throws TaskException if the standard task folder of mailadmin can't be
     * determined.
     */
    private void assignToAdmin(final Context ctx, final Connection readCon,
        final Connection writeCon, final int userId) throws TaskException,
        SQLException {
        String sql = "SELECT id FROM task JOIN task_participant "
            + "ON task.cid=task_participant.cid "
            + "AND task.id=task_participant.task WHERE task.cid=? AND "
            + "(created_from=? OR changed_from=?)";
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> tasks = new ArrayList<Integer>();
        try {
            stmt = readCon.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        final int adminTaskFolder = Tools.getUserTaskStandardFolder(ctx,
            ctx.getMailadmin());
        for (int taskId : tasks) {
            final Task task = selectTask(ctx, readCon, StorageType.ACTIVE,
                taskId);
            final Date lastModified = task.getLastModified();
            if (task.getCreatedBy() == userId) {
                task.setCreatedBy(ctx.getMailadmin());
            }
            if (task.getModifiedBy() == userId) {
                task.setModifiedBy(ctx.getMailadmin());
            }
            task.setLastModified(new Date());
            updateTask(ctx, writeCon, StorageType.ACTIVE, task, lastModified,
                new int[] { Task.CREATED_BY, Task.MODIFIED_BY,
                Task.LAST_MODIFIED });
            deleteFolder(ctx, writeCon, taskId, StorageType.ACTIVE, "user",
                new int[] { userId });
            final Folder folder = new Folder(adminTaskFolder,
                ctx.getMailadmin());
            final Set<Folder> folders = new HashSet<Folder>(1, 1);
            folders.add(folder);
            insertFolder(ctx, writeCon, StorageType.ACTIVE, taskId,
                folders);
        }
        sql = "SELECT id FROM del_task JOIN del_task_participant "
            + "ON del_task.cid=del_task_participant.cid "
            + "AND del_task.id=del_task_participant.task "
            + "WHERE del_task.cid=? AND (created_from=? OR changed_from=?)";
        try {
            stmt = readCon.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, userId);
            tasks.clear();
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        for (int taskId : tasks) {
            final Task task = selectTask(ctx, readCon, StorageType.DELETED,
                taskId);
            final Date lastModified = task.getLastModified();
            if (task.getCreatedBy() == userId) {
                task.setCreatedBy(ctx.getMailadmin());
            }
            if (task.getModifiedBy() == userId) {
                task.setModifiedBy(ctx.getMailadmin());
            }
            task.setLastModified(new Date());
            updateTask(ctx, writeCon, StorageType.DELETED, task, lastModified,
                new int[] { Task.CREATED_BY, Task.MODIFIED_BY,
                Task.LAST_MODIFIED });
            deleteFolder(ctx, writeCon, taskId, StorageType.DELETED, "user",
                new int[] { userId });
            final Folder folder = new Folder(adminTaskFolder,
                ctx.getMailadmin());
            final Set<Folder> folders = new HashSet<Folder>(1, 1);
            folders.add(folder);
            insertFolder(ctx, writeCon, StorageType.DELETED, taskId,
                folders);
        }
    }

    /**
     * Deletes all private tasks of a user.
     * @param ctx Context.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @param userId unique identifier of the deleted user.
     * @throws TaskException if an error occurs.
     * @throws SQLException if an sql error occurs.
     */
    private void deletePrivateTasks(final Context ctx, final Connection readCon,
        final Connection writeCon, final int userId) throws TaskException,
        SQLException {
        String sql = "SELECT id,last_modified FROM task_folder JOIN task "
            + "USING (cid,id) LEFT JOIN task_participant "
            + "ON task.cid=task_participant.cid AND "
            + "task.id=task_participant.task WHERE task.cid=? "
            + "AND task_folder.user=? AND task_participant.user IS NULL";
        PreparedStatement stmt = readCon.prepareStatement(sql);
        stmt.setInt(1, ctx.getContextId());
        stmt.setInt(2, userId);
        ResultSet result = stmt.executeQuery();
        final List<Integer> tasks = new ArrayList<Integer>();
        final List<Long> mods = new ArrayList<Long>();
        while (result.next()) {
            tasks.add(result.getInt(1));
            mods.add(result.getLong(2));
        }
        result.close();
        stmt.close();
        for (int i = 0; i < tasks.size(); i++) {
            deleteFolder(ctx, writeCon, tasks.get(i), StorageType.ACTIVE,
                "user", new int[] { userId });
            delete(ctx, writeCon, StorageType.ACTIVE, tasks.get(i), new Date(
                mods.get(i)));
        }
        sql = "SELECT id,last_modified FROM del_task_folder JOIN del_task USING"
            + " (cid,id) LEFT JOIN del_task_participant "
            + "ON del_task.cid=del_task_participant.cid "
            + "AND del_task.id=del_task_participant.task WHERE del_task.cid=? "
            + "AND del_task_folder.user=? "
            + "AND del_task_participant.user IS NULL";
        stmt = readCon.prepareStatement(sql);
        stmt.setInt(1, ctx.getContextId());
        stmt.setInt(2, userId);
        result = stmt.executeQuery();
        tasks.clear();
        mods.clear();
        while (result.next()) {
            tasks.add(result.getInt(1));
            mods.add(result.getLong(2));
        }
        result.close();
        stmt.close();
        for (int i = 0; i < tasks.size(); i++) {
            deleteFolder(ctx, writeCon, tasks.get(i), StorageType.DELETED,
                "user", new int[] { userId });
            delete(ctx, writeCon, StorageType.DELETED, tasks.get(i), new Date(
                mods.get(i)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Set<Folder> selectFolders(final Context ctx, final int taskId)
        throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectFolder(ctx, con, taskId);
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    Folder selectFolderById(final Context ctx, final int taskId,
        final int folderId) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            return selectFolderById(ctx, con, taskId, folderId);
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    private Folder selectFolderById(final Context ctx, final Connection con,
        final int taskId, final int folderId) throws SQLException,
        TaskException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        Folder retval = null;
        try {
            stmt = con.prepareStatement(SELECT_FOLDER_BY_IDENTIFIER);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setInt(pos++, folderId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = new Folder(folderId, result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        if (null == retval) {
            throw new TaskException(Code.FOLDER_NOT_FOUND, folderId, taskId);
        }
        return retval;
    }

    private Set<Folder> selectFolder(final Context ctx, final Connection con,
        final int taskId) throws SQLException {
        final String sql = SELECT_FOLDERLINK;
        final PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, ctx.getContextId());
        stmt.setInt(2, taskId);
        final ResultSet result = stmt.executeQuery();
        final Set<Folder> retval = new HashSet<Folder>();
        while (result.next()) {
            retval.add(new Folder(result.getInt(1), result.getInt(2)));
        }
        result.close();
        stmt.close();
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsNotSelfCreatedTasks(final SessionObject session,
        final int folderId) throws TaskException {
        final String sql = "SELECT COUNT(id) FROM task JOIN task_folder "
            + "USING (cid,id) WHERE task.cid=? AND folder=? AND "
            + "created_from!=?";
        boolean retval = true;
        Connection con;
        final Context ctx = session.getContext();
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
            final int userId = session.getUserObject().getId();
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
        TASK_TABLES.put(StorageType.ACTIVE, "task");
        TASK_TABLES.put(StorageType.DELETED, "del_task");
        FOLDER_TABLES.put(StorageType.ACTIVE, "task_folder");
        FOLDER_TABLES.put(StorageType.DELETED, "del_task_folder");
        PARTS_TABLES.put(StorageType.ACTIVE, "task_participant");
        PARTS_TABLES.put(StorageType.REMOVED, "task_removedparticipant");
        PARTS_TABLES.put(StorageType.DELETED, "del_task_participant");
        LIST_MODIFIED.put(StorageType.ACTIVE, "SELECT @fields@ FROM task JOIN "
            + "task_folder USING (cid,id) "
            + "WHERE task.cid=? AND folder=? AND last_modified>=?");
        SELECT_PARTS.put(StorageType.ACTIVE,
            "SELECT user,group_id,accepted,description FROM task_participant "
            + "WHERE cid=? AND task=?");
        SELECT_PARTS.put(StorageType.REMOVED,
            "SELECT user,group_id,accepted,description,folder "
            + "FROM task_removedparticipant WHERE cid=? AND task=?");
        SELECT_PARTS.put(StorageType.DELETED,
            "SELECT user,group_id,accepted,description "
            + "FROM del_task_participant WHERE cid=? AND task=?");
        INSERT_PARTS.put(StorageType.ACTIVE,
            "INSERT INTO task_participant (cid,task,user,group_id,accepted,"
            + "description) VALUES (?,?,?,?,?,?)");
        INSERT_PARTS.put(StorageType.REMOVED,
            "INSERT INTO task_removedparticipant (cid,task,user,group_id,"
            + "accepted,description,folder) VALUES (?,?,?,?,?,?,?)");
        INSERT_PARTS.put(StorageType.DELETED,
            "INSERT INTO del_task_participant (cid,task,user,group_id,accepted,"
            + "description) VALUES (?,?,?,?,?,?)");
        DELETE_PARTS.put(StorageType.ACTIVE, "DELETE FROM "
            + "task_participant WHERE cid=? AND task=? AND user IN (");
        DELETE_PARTS.put(StorageType.REMOVED, "DELETE FROM "
            + "task_removedparticipant WHERE cid=? AND task=? AND user IN (");
        DELETE_PARTS.put(StorageType.DELETED, "DELETE FROM "
            + "del_task_participant WHERE cid=? AND task=? AND user IN (");
        SELECT_EXTERNAL.put(StorageType.ACTIVE, "SELECT mail,display_name FROM "
            + "task_eparticipant WHERE cid=? AND task=?");
        SELECT_EXTERNAL.put(StorageType.DELETED, "SELECT mail,display_name FROM"
            + " del_task_eparticipant WHERE cid=? AND task=?");
        INSERT_EXTERNAL.put(StorageType.ACTIVE, "INSERT INTO task_eparticipant"
            + " (cid,task,mail,display_name) VALUES (?,?,?,?)");
        INSERT_EXTERNAL.put(StorageType.DELETED, "INSERT INTO "
            + "del_task_eparticipant (cid,task,mail,display_name) VALUES "
            + "(?,?,?,?)");
        DELETE_EXTERNAL.put(StorageType.ACTIVE, "DELETE FROM task_eparticipant "
            + "WHERE cid=? AND task=? AND mail IN (");
        DELETE_EXTERNAL.put(StorageType.DELETED, "DELETE FROM task_eparticipant"
            + " WHERE cid=? AND task=? AND mail IN (");
        INSERT_FOLDER.put(StorageType.ACTIVE, "INSERT INTO "
            + FOLDER_TABLES.get(StorageType.ACTIVE)
            + "(cid, id, folder, user) VALUES (?,?,?,?)");
        INSERT_FOLDER.put(StorageType.DELETED, "INSERT INTO "
            + FOLDER_TABLES.get(StorageType.DELETED)
            + "(cid, id, folder, user) VALUES (?,?,?,?)");
    }
}
