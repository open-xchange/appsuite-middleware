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

package com.openexchange.groupware.update.internal;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.ExecutedTask;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.caching.SerializedCachingLoader;
import com.openexchange.tools.caching.StorageLoader;
import com.openexchange.tools.update.Tools;

/**
 * Implements loading and storing the schema version information.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaStoreImpl extends SchemaStore {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaStoreImpl.class);
    private static final String CACHE_REGION = "OXDBPoolCache";
    private static final String TABLE_NAME = "updateTask";

    /** The idioms for either blocking or background updates */
    private static enum Idiom {

        /**
         * The idiom for a blocking update
         */
        LOCKED("LOCKED", UUIDs.toByteArray(UUIDs.fromUnformattedString("8d8b93e559794baca39daef8f87087a1"))),
        /**
         * The idiom for a background update
         */
        BACKGROUND("BACKGROUND", UUIDs.toByteArray(UUIDs.fromUnformattedString("5b7a2847985a49aa874c17df79af48b3")));

        private final String name;
        private final byte[] uuid;

        private Idiom(String name, byte[] uuid) {
            this.name = name;
            this.uuid = uuid;
        }

        /**
         * Gets the UUID
         *
         * @return The UUID
         */
        byte[] getUuid() {
            return uuid;
        }

        /**
         * Gets the name
         *
         * @return The name
         */
        String getName() {
            return name;
        }
    }

    private static final int MYSQL_DEADLOCK = 1213;
    private static final int MYSQL_DUPLICATE = 1062;

    // -------------------------------------------------------------------------------------------------------------------------------------------------

    private final Lock cacheLock;
    private volatile Cache cache;

    /**
     * Initializes a new {@link SchemaStoreImpl}.
     */
    public SchemaStoreImpl() {
        super();
        cacheLock = new ReentrantLock();
    }

    @Override
    protected SchemaUpdateState getSchema(final int poolId, final String schemaName, final Connection con) throws OXException {
        final Cache cache = this.cache;
        return SerializedCachingLoader.fetch(cache, CACHE_REGION, null, cacheLock, new StorageLoader<SchemaUpdateState>() {

            @Override
            public Serializable getKey() {
                return cache.newCacheKey(poolId, schemaName);
            }

            @Override
            public SchemaUpdateState load() throws OXException {
                return loadSchema(poolId, schemaName, con);
            }
        });
    }

    static SchemaUpdateState loadSchema(int poolId, String schemaName, Connection con) throws OXException {
        final SchemaUpdateState retval;
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            checkForTable(con);
            retval = loadSchemaStatus(poolId, schemaName, con);

            con.commit();
            rollback = 2;
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
        return retval;
    }

    /**
     * @param con connection to master in transaction mode.
     * @return <code>true</code> if the table has been created.
     */
    private static void checkForTable(final Connection con) throws SQLException {
        if (!Tools.tableExists(con, TABLE_NAME)) {
            createTable(con);
        }
    }

    private static void createTable(final Connection con) throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(CreateUpdateTaskTable.CREATES_PRIMARY_KEY[0]);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void lockSchema(Schema schema, boolean background) throws OXException {
        Connection con = Database.get(schema.getPoolId(), schema.getSchema());
        int rollback = 0;
        try {
            con.setAutoCommit(false); // BEGIN
            rollback = 1;
            // Insert lock
            insertLock(con, schema, background ? Idiom.BACKGROUND : Idiom.LOCKED);
            // Everything went fine. Schema is marked as locked
            con.commit();
            rollback = 2;
            // Invalidate cache
            invalidateCache(schema);
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
                autocommit(con);
            }
            Database.back(schema.getPoolId(), con);
        }
    }

    private static void insertLock(Connection con, Schema schema, Idiom idiom) throws OXException {
        // Check for existing lock exclusively
        final ExecutedTask[] tasks = readUpdateTasks(con);
        for (final ExecutedTask task : tasks) {
            if (idiom.getName().equals(task.getTaskName())) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(schema.getSchema());
            }
        }
        // Insert lock
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO updateTask (cid,taskName,successful,lastModified,uuid) VALUES (0,?,true,?,?)");
            stmt.setString(1, idiom.getName());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setBytes(3, idiom.getUuid());
            if (stmt.executeUpdate() == 0) {
                throw SchemaExceptionCodes.LOCK_FAILED.create(schema.getSchema());
            }
        } catch (final SQLException e) {
            if (MYSQL_DEADLOCK == e.getErrorCode() || MYSQL_DUPLICATE == e.getErrorCode() || Databases.isPrimaryKeyConflictInMySQL(e)) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(e, schema.getSchema());
            }
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean tryRefreshSchemaLock(Schema schema, boolean background) throws OXException {
        return tryRefreshLock(schema, background);
    }

    private boolean tryRefreshLock(Schema schema, boolean background) throws OXException {
        int poolId = schema.getPoolId();
        Connection con = Database.get(poolId, schema.getSchema());
        try {
            // Refresh lock
            boolean refreshed = tryRefreshLock(con, background ? Idiom.BACKGROUND : Idiom.LOCKED);
            if (refreshed) {
                invalidateCache(schema);
            }
            return refreshed;
        } finally {
            Database.back(poolId, con);
        }
    }

    private static boolean tryRefreshLock(Connection con, Idiom idiom) throws OXException {
        // Refresh lock
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE updateTask SET lastModified = ? WHERE cid=0 AND taskName=?");
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, idiom.getName());
            return stmt.executeUpdate() > 0;
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void unlockSchema(final Schema schema, final boolean background) throws OXException {
        int poolId = schema.getPoolId();
        Connection con = Database.get(poolId, schema.getSchema());
        int rollback = 0;
        try {
            // End of update process, so unlock schema
            con.setAutoCommit(false);
            rollback = 1;
            // Delete lock
            deleteLock(con, schema, background ? Idiom.BACKGROUND : Idiom.LOCKED);
            // Everything went fine. Schema is marked as unlocked
            con.commit();
            rollback = 2;
            // Invalidate
            invalidateCache(schema);
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final OXException e) {
            throw e;
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
            Database.back(poolId, con);
        }
    }

    private static void deleteLock(final Connection con, final Schema schema, final Idiom idiom) throws OXException {
        // Check for existing lock exclusively
        final ExecutedTask[] tasks = readUpdateTasks(con);
        boolean found = false;
        for (final ExecutedTask task : tasks) {
            if (idiom.getName().equals(task.getTaskName())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw SchemaExceptionCodes.UPDATE_CONFLICT.create(schema.getSchema());
        }
        // Delete lock
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM updateTask WHERE cid=0 AND taskName=?");
            stmt.setString(1, idiom.getName());
            if (stmt.executeUpdate() == 0) {
                throw SchemaExceptionCodes.UNLOCK_FAILED.create(schema.getSchema());
            }
        } catch (final SQLException e) {
            if (MYSQL_DEADLOCK == e.getErrorCode() || MYSQL_DUPLICATE == e.getErrorCode()) {
                throw SchemaExceptionCodes.UNLOCK_FAILED.create(e, schema.getSchema());
            }
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * @param con connection to the master in transaction mode.
     */
    private static SchemaUpdateState loadSchemaStatus(int poolId, String schemaName, Connection con) throws OXException, SQLException {
        final SchemaUpdateStateImpl retval = new SchemaUpdateStateImpl();
        retval.setBlockingUpdatesRunning(false);
        retval.setBackgroundUpdatesRunning(false);
        loadUpdateTasks(con, retval);
        retval.setServer(Database.getServerName());
        retval.setSchema(schemaName);
        retval.setPoolId(poolId);
        return retval;
    }

    private static void loadUpdateTasks(final Connection con, final SchemaUpdateStateImpl state) throws OXException {
        String lockedName = Idiom.LOCKED.getName();
        String backgroundName = Idiom.BACKGROUND.getName();
        for (final ExecutedTask task : readUpdateTasks(con)) {
            if (lockedName.equals(task.getTaskName())) {
                state.setBlockingUpdatesRunning(true);
                state.setBlockingUpdatesRunningSince(task.getLastModified());
            } else if (backgroundName.equals(task.getTaskName())) {
                state.setBackgroundUpdatesRunning(true);
                state.setBackgroundUpdatesRunningSince(task.getLastModified());
            } else {
                state.addExecutedTask(task.getTaskName(), task.isSuccessful());
            }
        }
    }

    private static ExecutedTask[] readUpdateTasks(final Connection con) throws OXException {
        List<ExecutedTask> retval;
        {
            Statement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.createStatement();
                result = stmt.executeQuery("SELECT taskName,successful,lastModified,uuid FROM updateTask WHERE cid=0 FOR UPDATE");
                if (false == result.next()) {
                    return new ExecutedTask[0];
                }

                retval = new ArrayList<ExecutedTask>(512);
                do {
                    ExecutedTask task = new ExecutedTaskImpl(result.getString(1), result.getBoolean(2), new Date(result.getLong(3)), UUIDs.toUUID(result.getBytes(4)));
                    retval.add(task);
                } while (result.next());
            } catch (final SQLException e) {
                throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
        }

        Collections.sort(retval, new Comparator<ExecutedTask>() {

            @Override
            public int compare(ExecutedTask o1, ExecutedTask o2) {
                Date lastModified1 = o1.getLastModified();
                Date lastModified2 = o2.getLastModified();
                if (null == lastModified1) {
                    return null == lastModified2 ? 0 : -1;
                } else if (null == lastModified2) {
                    return 1;
                }
                return lastModified1.compareTo(lastModified2);
            }
        });
        return retval.toArray(new ExecutedTask[retval.size()]);
    }

    @Override
    public void addExecutedTask(Connection con, String taskName, boolean success, int poolId, String schema) throws OXException {
        doAddExecutedTasks(con, Collections.singletonList(taskName), success, poolId, schema);
        invalidateCache(poolId, schema);
    }

    @Override
    public void addExecutedTasks(Connection con, Collection<String> taskNames, boolean success, int poolId, String schema) throws OXException {
        if (null == taskNames || taskNames.isEmpty()) {
            return;
        }

        doAddExecutedTasks(con, taskNames, success, poolId, schema);
        invalidateCache(poolId, schema);
    }

    private static void doAddExecutedTasks(Connection con, Collection<String> taskNames, boolean success, int poolId, String schema) throws OXException {
        doAddExecutedTasks(con, taskNames, success);
    }

    private static void doAddExecutedTasks(Connection con, Collection<String> taskNames, boolean success) throws OXException {
        PreparedStatement insertStatement = null;
        PreparedStatement updateStatement = null;
        try {
            long now = System.currentTimeMillis();
            for (String taskName : taskNames) {
                if (taskExists(con, taskName)) {
                    if (null == updateStatement) {
                        updateStatement = con.prepareStatement("UPDATE updateTask SET successful=?,lastModified=? WHERE cid=0 AND taskName=?");
                    }
                    updateTask(updateStatement, taskName, success, now);
                } else {
                    if (null == insertStatement) {
                        insertStatement = con.prepareStatement("INSERT INTO updateTask (cid,successful,lastModified,taskName,uuid) VALUES (0,?,?,?,?)");
                    }
                    insertTask(insertStatement, taskName, success, now);
                }
            }
            if (null != insertStatement) {
                insertStatement.executeBatch();
            }
            if (null != updateStatement) {
                updateStatement.executeBatch();
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(insertStatement, updateStatement);
        }
    }

    /**
     * Inserts an update task
     *
     * @param insertStatement The {@link PreparedStatement} for the insert
     * @param taskName the task's name
     * @param success whether the task was successful
     * @param now The current time stamp
     * @throws SQLException if an SQL error is occurred
     */
    private static void insertTask(PreparedStatement insertStatement, String taskName, boolean success, long now) throws SQLException {
        int parameterIndex = 1;
        insertStatement.setBoolean(parameterIndex++, success);
        insertStatement.setLong(parameterIndex++, now);
        insertStatement.setString(parameterIndex++, taskName);
        insertStatement.setBytes(parameterIndex++, UUIDs.toByteArray(UUID.randomUUID()));
        insertStatement.addBatch();
    }

    /**
     * Updates the status and time stamp of the task with the specified name
     *
     * @param updateStatement The {@link PreparedStatement} for the update
     * @param taskName The task's name
     * @param success Whether the task was successful
     * @param now the current time stamp
     * @throws SQLException if an SQL error is occurred
     */
    private static void updateTask(PreparedStatement updateStatement, String taskName, boolean success, long now) throws SQLException {
        int parameterIndex = 1;
        updateStatement.setBoolean(parameterIndex++, success);
        updateStatement.setLong(parameterIndex++, now);
        updateStatement.setString(parameterIndex, taskName);
        updateStatement.addBatch();
    }

    /**
     * Checks whether a task already exists in the table
     *
     * @param con The {@link Connection}
     * @param taskName The task's name
     * @return <code>true</code> if another task already exists in the table; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    private static boolean taskExists(Connection con, String taskName) throws SQLException {
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = con.prepareStatement("SELECT 1 FROM updateTask WHERE cid=0 AND taskName=? FOR UPDATE");
            statement.setString(1, taskName);
            result = statement.executeQuery();
            return result.next();
        } finally {
            closeSQLStuff(statement, result);
        }
    }

    @Override
    public ExecutedTask[] getExecutedTasks(final int poolId, final String schemaName) throws OXException {
        final Connection con = Database.get(poolId, schemaName);
        try {
            con.setAutoCommit(false);
            ExecutedTask[] tasks = readUpdateTasks(con);
            con.commit();
            return tasks;
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(poolId, con);
        }
    }

    @Override
    public void setCacheService(final CacheService cacheService) {
        try {
            cache = cacheService.getCache(CACHE_REGION);
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    @Override
    public void removeCacheService() {
        Cache cache = this.cache;
        if (null != cache) {
            this.cache = null;
            try {
                cache.clear();
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    private void invalidateCache(Schema schema) {
        invalidateCache(schema.getPoolId(), schema.getSchema());
    }

    private void invalidateCache(int poolId, String schema) {
        Cache cache = this.cache;
        if (null != cache) {
            CacheKey key = cache.newCacheKey(poolId, schema);
            try {
                cache.remove(key);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

}
