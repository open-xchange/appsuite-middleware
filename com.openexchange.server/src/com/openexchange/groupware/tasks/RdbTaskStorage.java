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

import static com.openexchange.groupware.tasks.StorageType.ACTIVE;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.openexchange.database.IncorrectStringSQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.TaskIterator2.StatementSetter;
import com.openexchange.java.Charsets;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class implementes the storage methods for tasks using a relational database. The used SQL is currently optimized for MySQL. Maybe
 * other databases need SQL optimized in another way.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbTaskStorage extends TaskStorage {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbTaskStorage.class);

    /**
     * This SQL statement counts the tasks in a folder. TODO Move to {@link SQL} class.
     */
    private static final String COUNT_TASKS = "SELECT COUNT(task.id) FROM task JOIN task_folder USING (cid,id) WHERE task.cid=? AND task_folder.folder=?";

    /**
     * SQL statements for selecting modified tasks.
     */
    private static final Map<StorageType, String> LIST_MODIFIED = new EnumMap<StorageType, String>(StorageType.class);

    /**
     * Additional where clause if only own objects can be seen.
     */
    private static final String ONLY_OWN = " AND created_from=?";

    /**
     * Additional where claus if no private tasks should be selected in a shared folder.
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
    protected TaskIterator load(final Context ctx, final int[] taskIds, final int[] columns) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            // TODO implement load of multiple tasks.
            return null;
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Deletes a task from the given table.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param taskId unique identifier of the task to delete.
     * @param lastRead timestamp when the task was last read.
     * @param type ACTIVE or DELETED.
     * @param sanityCheck <code>true</code> to check if task is really deleted.
     * @throws OXException if the task has been changed in the meantime or an exception occurred or there is no task to delete and
     *             sanityCheck is <code>true</code>.
     */
    @Override
    public void delete(final Context ctx, final Connection con, final int taskId, final Date lastRead, final StorageType type, final boolean sanityCheck) throws OXException {
        final String sql = "DELETE FROM @table@ WHERE cid=? AND id=? AND last_modified<=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql.replace("@table@", SQL.TASK_TABLES.get(type)));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setLong(pos++, lastRead.getTime());
            final int count = stmt.executeUpdate();
            if (sanityCheck && 1 != count) {
                throw TaskExceptionCode.MODIFIED.create();
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countTasks(final Context ctx, final int userId, final int folderId, final boolean onlyOwn, final boolean noPrivate) throws OXException {
        int number = 0;
        number = countTasks(ctx, folderId, onlyOwn, userId, noPrivate);
        return number;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskIterator list(final Context ctx, final int folderId, final int from, final int to, final int orderBy, final Order order, final int[] columns, final boolean onlyOwn, final int userId, final boolean noPrivate) throws OXException {
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
        sql.append(SQL.getOrder(orderBy, order));
        sql.append(SQL.getLimit(from, to));
        return new TaskIterator2(ctx, userId, sql.toString(), new StatementSetter() {

            @Override
            public void perform(final PreparedStatement stmt) throws SQLException {
                int pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, folderId);
                if (onlyOwn) {
                    stmt.setInt(pos++, userId);
                }
            }
        }, folderId, columns, ACTIVE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskIterator search(final Context ctx, final int userId, final TaskSearchObject search, final int orderBy, final Order order, final int[] columns, final List<Integer> all, final List<Integer> own, final List<Integer> shared) throws OXException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getFields(columns, true, true, null));
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
        sql.append(" GROUP BY task.id");
        sql.append(SQL.getOrder(orderBy, order));
        return new TaskIterator2(ctx, userId, sql.toString(), new StatementSetter() {

            @Override
            public void perform(final PreparedStatement stmt) throws SQLException {
                int pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                for (final int i : all) {
                    stmt.setInt(pos++, i);
                }
                for (final int i : own) {
                    stmt.setInt(pos++, i);
                }
                if (own.size() > 0) {
                    stmt.setInt(pos++, userId);
                }
                for (final int i : shared) {
                    stmt.setInt(pos++, i);
                }
                if (rangeCondition.length() > 0) {
                    for (final Date date : search.getRange()) {
                        stmt.setTimestamp(pos++, new Timestamp(date.getTime()));
                    }
                }
                if (patternCondition.length() > 0) {
                    final String pattern = StringCollection.prepareForSearch(search.getPattern());
                    stmt.setString(pos++, pattern);
                    stmt.setString(pos++, pattern);
                    stmt.setString(pos++, pattern);
                }
                LOG.trace(stmt.toString());
            }
        }, -1, columns, ACTIVE);
    }

    /**
     * Counts the tasks in a folder.
     *
     * @param ctx Context.
     * @param folderId unique identifier of the folder.
     * @param onlyOwn <code>true</code> if only own objects can be seen.
     * @param userId unique identifier of the user.
     * @param noPrivate <code>true</code> if the folder is a shared folder.
     * @return the number of tasks in that folder.
     * @throws OXException if an error occurs.
     */
    private int countTasks(final Context ctx, final int folderId, final boolean onlyOwn, final int userId, final boolean noPrivate) throws OXException {
        final Connection con = DBPool.pickup(ctx);
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
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
        return number;
    }

    @Override
    public void insertTask(final Context ctx, final Connection con, final Task task, final StorageType type, int[] columns) throws OXException {
        if (type == StorageType.ACTIVE) {
            handleUID(ctx, con, task);
        }
        final StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO ");
        insert.append(SQL.TASK_TABLES.get(type));
        insert.append(" (");
        List<Mapper<?>> usedMappers = new ArrayList<Mapper<?>>();
        for (final Mapper<?> mapper : Mapping.MAPPERS) {
            if (mapper.isSet(task) && (null == columns || Arrays.contains(columns, mapper.getId()))) {
                insert.append(mapper.getDBColumnName());
                insert.append(',');
                usedMappers.add(mapper);
            }
        }
        insert.append("cid,id) VALUES (");
        for (int i = 0; i < usedMappers.size(); i++) {
            insert.append("?,");
        }
        insert.append("?,?)");
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(insert.toString());
            int pos = 1;
            for (int i = 0; i < usedMappers.size(); i++) {
                usedMappers.get(i).toDB(stmt, pos++, task);
            }
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, task.getObjectID());
            stmt.execute();
        } catch (final DataTruncation e) {
            throw parseTruncated(con, e, task, type);
        } catch (IncorrectStringSQLException e) {
            throw Tools.parseIncorrectString(e);
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private void handleUID(Context ctx, Connection con, Task task) throws OXException {
        if (task.containsUid()) {
            if (checkUid(ctx, con, task.getUid())) {
                throw OXCalendarExceptionCodes.TASK_UID_ALREDY_EXISTS.create(task.getTitle(), task.getUid());
            }
        } else {
            task.setUid(UUID.randomUUID().toString());
        }
    }

    private static boolean checkUid(Context ctx, Connection con, String uid) throws OXException {
        StringBuilder select = new StringBuilder("Select id from task where uid=? and cid=?");
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(select.toString());
            stmt.setString(1, uid);
            stmt.setInt(2, ctx.getContextId());
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(e);
        } finally {
            DBUtils.closeResources(rs, stmt, null, true, ctx);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsTask(final Context ctx, final Connection con, final int taskId, final StorageType type) throws OXException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(Mapping.getMapping(DataObject.OBJECT_ID).getDBColumnName());
        sql.append(" FROM ");
        sql.append(SQL.TASK_TABLES.get(type));
        sql.append(" WHERE cid=? AND id=?");
        PreparedStatement stmt = null;
        ResultSet result = null;
        final boolean exists;
        try {
            stmt = con.prepareStatement(sql.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, taskId);
            result = stmt.executeQuery();
            if (result.next() && taskId == result.getInt(1)) {
                exists = true;
            } else {
                exists = false;
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task selectTask(final Context ctx, final Connection con, final int taskId, final StorageType type) throws OXException {
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
                for (final Mapper<?> mapper : Mapping.MAPPERS) {
                    mapper.fromDB(result, pos++, task);
                }
                task.setObjectID(taskId);
            }
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        if (null == task) {
            throw TaskExceptionCode.TASK_NOT_FOUND.create(I(taskId), I(ctx.getContextId()));
        }
        return task;
    }

    /**
     * Updates a task in the database.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param type ACTIVE or DELETED.
     * @param task Task with the updated values.
     * @param lastRead timestamp when the client last requested the object.
     * @param modified attributes of the task that should be updated.
     * @throws OXException if no task is updated.
     */
    @Override
    public void updateTask(final Context ctx, final Connection con, final Task task, final Date lastRead, final int[] modified, final StorageType type) throws OXException {
        if (modified.length == 0) {
            return;
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(SQL.TASK_TABLES.get(type));
        sql.append(" SET ");
        for (final int i : modified) {
            sql.append(Mapping.getMapping(i).getDBColumnName());
            sql.append("=?,");
        }
        sql.setLength(sql.length() - 1);
        sql.append(" WHERE cid=? AND id=? AND last_modified<=?");
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            for (final int i : modified) {
                Mapping.getMapping(i).toDB(stmt, pos++, task);
            }
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, task.getObjectID());
            stmt.setLong(pos++, lastRead.getTime());
            final int updatedRows = stmt.executeUpdate();
            if (0 == updatedRows) {
                throw TaskExceptionCode.MODIFIED.create();
            }
        } catch (final DataTruncation e) {
            throw parseTruncated(con, e, task, type);
        } catch (IncorrectStringSQLException e) {
            throw Tools.parseIncorrectString(e);
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * Parses the truncated fields out of the DataTruncation exception and transforms this to a OXException.
     *
     * @param exc DataTruncation exception.
     * @return a OXException.
     */
    private static OXException parseTruncated(final Connection con, final DataTruncation exc, final Task task, final StorageType type) {
        final String[] fields = DBUtils.parseTruncatedFields(exc);
        final StringBuilder sFields = new StringBuilder();
        final OXException.Truncated[] truncateds = new OXException.Truncated[fields.length];
        final Mapper<?>[] mappers = SQL.mapColumns(fields);
        for (int i = 0; i < fields.length; i++) {
            sFields.append(fields[i]);
            sFields.append(", ");
            final Mapper<?> mapper = mappers[i];
            final int valueLength;
            final Object tmp = mapper.get(task);
            if (tmp instanceof String) {
                valueLength = Charsets.getBytes((String) tmp, Charsets.UTF_8).length;
            } else {
                // e.g. BigDecimal (inaccurate because dot and 'e' need to be removed.
                valueLength = tmp.toString().length();
            }
            int tmp2 = -1;
            try {
                tmp2 = DBUtils.getColumnSize(con, SQL.TASK_TABLES.get(type), mapper.getDBColumnName());
            } catch (final SQLException e) {
                LOG.error("", e);
                tmp2 = -1;
            }
            final int length = -1 == tmp2 ? 0 : tmp2;
            truncateds[i] = new OXException.Truncated() {

                @Override
                public int getId() {
                    return mapper.getId();
                }

                @Override
                public int getLength() {
                    return valueLength;
                }

                @Override
                public int getMaxSize() {
                    return length;
                }
            };
        }
        sFields.setLength(sFields.length() - 2);
        final OXException tske;
        if (truncateds.length > 0) {
            final OXException.Truncated truncated = truncateds[0];
            tske =
                TaskExceptionCode.TRUNCATED.create(
                exc,
                sFields.toString(),
                Integer.valueOf(truncated.getMaxSize()),
                Integer.valueOf(truncated.getLength()));
        } else {
            tske = TaskExceptionCode.TRUNCATED.create(exc, sFields.toString(), Integer.valueOf(0), Integer.valueOf(0));
        }
        for (final OXException.Truncated truncated : truncateds) {
            tske.addProblematic(truncated);
        }
        return tske;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsNotSelfCreatedTasks(final Context ctx, final Connection con, final int userId, final int folderId) throws OXException {
        final String sql = "SELECT COUNT(id) FROM task JOIN task_folder USING (cid,id) WHERE task.cid=? AND folder=? AND created_from!=?";
        boolean retval = true;
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
                throw TaskExceptionCode.NO_COUNT_RESULT.create();
            }
            result.close();
            stmt.close();
        } catch (final SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        }
        return retval;
    }

    /** TODO move to {@link SQL} class. */
    static {
        LIST_MODIFIED.put(
            ACTIVE,
            "SELECT @fields@ FROM task JOIN task_folder USING (cid,id) WHERE task.cid=? AND folder=? AND last_modified>?");
    }

    @Override
    public TaskIterator list(final Context ctx, final int folderId, final int from, final int to, final int orderBy, final Order order, final int[] columns, final boolean onlyOwn, final int userId, final boolean noPrivate, final Connection con) throws OXException {
        if (con == null) {
            return list(ctx, folderId, from, to, orderBy, order, columns, onlyOwn, userId, noPrivate);
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getFields(columns, false));
        sql.append(" FROM task JOIN task_folder USING (cid,id) ");
        if (folderId != -1) {
            sql.append("WHERE task.cid=? AND task_folder.folder=?");
        } else {
            sql.append("WHERE task.cid=?");
        }
        if (onlyOwn) {
            sql.append(ONLY_OWN);
        }
        if (noPrivate) {
            sql.append(NO_PRIVATE);
        }
        sql.append(SQL.getOrder(orderBy, order));
        sql.append(SQL.getLimit(from, to));
        return new TaskIterator2(ctx, userId, sql.toString(), new StatementSetter() {

            @Override
            public void perform(final PreparedStatement stmt) throws SQLException {
                int pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                if (folderId != -1) {
                    stmt.setInt(pos++, folderId);
                }
                if (onlyOwn) {
                    stmt.setInt(pos++, userId);
                }
            }
        }, folderId, columns, ACTIVE, con);
    }

    @Override
    int countTasks(Context ctx) throws OXException {
        final Connection con = Database.get(ctx, false);
        final int retval;
        try {
            retval = countTasks(ctx.getContextId(), con);
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            Database.back(ctx, false, con);
        }
        return retval;
    }

    @Override
    int countTasks(int contextId, final Connection con) throws SQLException, OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final int retval;
        try {
            stmt = con.prepareStatement(SQL.COUNT_TASKS_IN_CONTEXT);
            stmt.setInt(1, contextId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = result.getInt(1);
            } else {
                throw TaskExceptionCode.NO_COUNT_RESULT.create();
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
    }
}

