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

import static com.openexchange.tools.sql.DBUtils.getIN;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
     * SQL statement for inserting external participants.
     */
    private static final Map<StorageType, String> INSERT_EXTERNAL =
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
                updateTask(ctx, con, task, lastRead, modified, StorageType
                    .ACTIVE);
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
    protected SearchIterator load(final Context ctx, final int[] taskIds,
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
    public void delete(final Context ctx, final Task task, final int userId,
        final Date lastRead) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            task.setLastModified(new Date());
            task.setModifiedBy(userId);
            insertTask(ctx, con, task, StorageType.DELETED);
            deleteParticipants(ctx, con, task.getObjectID());
            deleteFolder(ctx, con, task.getObjectID());
            delete(ctx, con, task.getObjectID(), lastRead, StorageType.ACTIVE);
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
     * @param taskId unique identifier of the task to delete.
     * @param lastRead timestamp when the task was last read.
     * @param type ACTIVE or DELETED.
     * @throws TaskException if the task has been changed in the meantime.
     */
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
                final Task forTime = selectTask(ctx, con, taskId, type);
                throw new TaskException(Code.MODIFIED,
                    forTime.getLastModified().getTime(), lastRead.getTime());
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
//            return new PrefetchIterator<Task>(new TaskIterator(ctx, userId,
//                stmt.executeQuery(), folderId, columns, StorageType.ACTIVE));
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
    SearchIterator search(final SessionObject session,
        final TaskSearchObject search, final int orderBy, final String orderDir,
        final int[] columns, final List<Integer> all, final List<Integer> own,
        final List<Integer> shared) throws TaskException {
        final int userId = session.getUserObject().getId();
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
        insert.append(SQL.TASK_TABLES.get(type));
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
                for (Mapper mapper : Mapping.MAPPERS) {
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
     * @param modified attributes of the task that should be updated.
     * @throws TaskException if no task is updated.
     */
    @Override
    void updateTask(final Context ctx, final Connection con, final Task task,
        final Date lastRead, final int[] modified, final StorageType type)
        throws TaskException {
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
                final Task forTime = selectTask(ctx, con, task.getObjectID(),
                    type);
                throw new TaskException(Code.MODIFIED,
                    forTime.getLastModified().getTime(), lastRead.getTime());
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
     * Inserts a folder in that the task will appear.
     * @param ctx Context.
     * @param con writable database connection.
     * @param type ACTIVE or DELETED.
     * @param taskId unique identifier of the task.
     * @param folders folders in that the task should appear.
     * @throws SQLException if a SQL problem occurs.
     * @deprecated Use FolderStorage.
     */
    private void insertFolder(final Context ctx, final Connection con,
        final StorageType type, final int taskId, final Set<Folder> folders)
        throws SQLException {
        final PreparedStatement stmt = con.prepareStatement(SQL.INSERT_FOLDER
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
     * @deprecated Use FolderStorage.
     */
    private void deleteFolder(final Context ctx, final Connection con,
        final int taskId, final StorageType table, final String type,
        final int[] ids) throws SQLException {
        final StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ");
        sql.append(SQL.FOLDER_TABLES.get(table));
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
     * @deprecated Use ParticipantStorage.
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
            participants.addAll(ParticipantStorage.getInstance()
                .selectInternal(ctx, con, taskId, type));
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
            stmt = con.prepareStatement(SELECT_PARTS.get(StorageType.ACTIVE)
                + " AND user=?");
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            if (result.next()) {
                participant = new TaskInternalParticipant();
                pos = 1;
                participant.setIdentifier(result.getInt(pos++));
                final int groupId = result.getInt(pos++);
                if (!result.wasNull()) {
                    participant.setGroupId(groupId);
                }
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
     * @deprecated Use ParticipantStorage.
     */
    private Set<TaskExternalParticipant> selectExternal(final Context ctx,
        final Connection con, final int taskId, final StorageType type)
        throws SQLException {
        final Set<TaskExternalParticipant> participants =
            new HashSet<TaskExternalParticipant>();
        if (StorageType.REMOVED != type) {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement(getIN(SQL.SELECT_EXTERNAL
                    .get(type), 1));
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, taskId);
                result = stmt.executeQuery();
                while (result.next()) {
                    final ExternalUserParticipant external =
                        new ExternalUserParticipant();
                    int pos = 2;
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

    /**
     * @deprecated Use ParticipantStorage.
     */
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
     * @deprecated Use ParticipantStorage.
     */
    private void deleteInternals(final Context ctx, final Connection con,
        final int taskId, final int[] participants, final StorageType type,
        final boolean sanityCheck) throws SQLException {
        if (participants.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append(SQL.DELETE_PARTS.get(type));
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

    /**
     * @deprecated Use ParticipantStorage.
     */
    private void deleteExternals(final Context ctx, final Connection con,
        final int taskId, final String[] participants, final StorageType type,
        final boolean sanityCheck) throws SQLException {
        if (0 == participants.length || StorageType.REMOVED == type) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append(SQL.DELETE_EXTERNAL.get(type));
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

    /**
     * @deprecated Use ParticipantStorage.
     */
    private void deleteParticipants(final Context ctx, final Connection con,
        final int taskId) throws SQLException, TaskException {
        final ParticipantStorage partStor = ParticipantStorage.getInstance();
        final Set<TaskInternalParticipant> participants =
            new HashSet<TaskInternalParticipant>(partStor.selectInternal(ctx,
            con, taskId, StorageType.ACTIVE));
        deleteInternals(ctx, con, taskId, participants, StorageType.ACTIVE,
            true);
        final Set<TaskInternalParticipant> removed = partStor
            .selectInternal(ctx, con, taskId, StorageType.REMOVED);
        deleteInternals(ctx, con, taskId, removed, StorageType.REMOVED, true);
        participants.addAll(removed);
        insertInternals(ctx, con, taskId, participants, StorageType.DELETED);
        final Set<TaskExternalParticipant> externals = selectExternal(ctx, con,
            taskId, StorageType.ACTIVE);
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
     * @deprecated Use FolderStore.
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

    /**
     * @deprecated Use FolderStore.
     */
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
        INSERT_EXTERNAL.put(StorageType.ACTIVE, "INSERT INTO task_eparticipant"
            + " (cid,task,mail,display_name) VALUES (?,?,?,?)");
        INSERT_EXTERNAL.put(StorageType.DELETED, "INSERT INTO "
            + "del_task_eparticipant (cid,task,mail,display_name) VALUES "
            + "(?,?,?,?)");
    }
}
