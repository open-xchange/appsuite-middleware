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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.Property;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.ExecutedTask;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.UpdateProperty;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.caching.SerializedCachingLoader;
import com.openexchange.tools.caching.StorageLoader;
import com.openexchange.tools.update.Tools;

/**
 * Implements loading and storing the schema version information.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaStoreImpl extends SchemaStore implements Reloadable {

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
        private final AtomicReference<Long> idleTimeMillis;

        private Idiom(String name, byte[] uuid) {
            this.name = name;
            this.uuid = uuid;
            this.idleTimeMillis = new AtomicReference<>(null);
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

        /**
         * Gets the number of milliseconds of idleness after which an update process is considered as stalled.
         *
         * @return The idle duration in milliseconds
         */
        long getIdleMillis() {
            Long idleMillis = idleTimeMillis.get();
            if (idleMillis == null) {
                synchronized (this) {
                    idleMillis = idleTimeMillis.get();
                    if (idleMillis == null) {
                        Property property = Idiom.LOCKED == this ? UpdateProperty.BLOCKED_IDLE_MILLIS : UpdateProperty.BACKGROUND_IDLE_MILLIS;
                        long defaultMillis = property.getDefaultValue(Long.class).longValue();

                        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                        if (configService == null) {
                            return defaultMillis;
                        }

                        idleMillis = Long.valueOf(configService.getIntProperty(property.getFQPropertyName(), (int) defaultMillis));
                        idleTimeMillis.set(idleMillis);
                    }
                }
            }
            return idleMillis.longValue();
        }

        /**
         * Clears possibly cached idle time milliseconds.
         */
        void clearIdleMillis() {
            idleTimeMillis.set(null);
        }
    }

    private static final int MYSQL_DEADLOCK = 1213;
    private static final int MYSQL_DUPLICATE = 1062;

    private static final SchemaStoreImpl INSTANCE = new SchemaStoreImpl();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static SchemaStoreImpl getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Lock cacheLock;
    private volatile Cache cache;

    /**
     * Initializes a new {@link SchemaStoreImpl}.
     */
    private SchemaStoreImpl() {
        super();
        cacheLock = new ReentrantLock();
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(
            UpdateProperty.BLOCKED_IDLE_MILLIS.getFQPropertyName(),
            UpdateProperty.BACKGROUND_IDLE_MILLIS.getFQPropertyName());
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        for (Idiom idiom : Idiom.values()) {
            idiom.clearIdleMillis();
        }
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
        } catch (SQLException e) {
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
        int rollback = 0;
        Connection con = Database.get(schema.getPoolId(), schema.getSchema());
        try {
            con.setAutoCommit(false); // BEGIN
            rollback = 1;
            // Insert lock
            insertLock(con, schema, background ? Idiom.BACKGROUND : Idiom.LOCKED);
            // Everything went fine. Schema is marked as locked
            con.commit(); // COMMIT
            rollback = 2;
            // Invalidate cache
            invalidateCache(schema);
        } catch (SQLException e) {
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
        Date update = null;
        {
            ExecutedTask[] executedTasks = readUpdateTasks(Optional.of(idiom.getName()), con);
            for (int i = 0; update == null && i < executedTasks.length; i++) {
                ExecutedTask task = executedTasks[i];
                if (idiom.getName().equals(task.getTaskName())) {
                    // Check if threshold is not exceeded
                    if (isNotTimedOut(task, idiom)) {
                        throw SchemaExceptionCodes.ALREADY_LOCKED.create(schema.getSchema());
                    }
                    update = task.getLastModified();
                }
            }
        }

        // Insert or update lock
        PreparedStatement stmt = null;
        try {
            if (update == null) {
                stmt = con.prepareStatement("INSERT INTO updateTask (cid, taskName, successful, lastModified, uuid) VALUES (0,?,true,?,?)");
                stmt.setString(1, idiom.getName());
                stmt.setLong(2, System.currentTimeMillis());
                stmt.setBytes(3, idiom.getUuid());
                try {
                    if (stmt.executeUpdate() <= 0) {
                        throw SchemaExceptionCodes.LOCK_FAILED.create(schema.getSchema());
                    }
                } catch (SQLException e) {
                    if (MYSQL_DEADLOCK == e.getErrorCode() || MYSQL_DUPLICATE == e.getErrorCode() || Databases.isPrimaryKeyConflictInMySQL(e)) {
                        throw SchemaExceptionCodes.ALREADY_LOCKED.create(e, schema.getSchema());
                    }
                    throw e;
                }
            } else {
                stmt = con.prepareStatement("UPDATE updateTask SET lastModified=? WHERE cid=0 AND uuid=? AND taskName=? AND lastModified=?");
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setBytes(2, idiom.getUuid());
                stmt.setString(3, idiom.getName());
                stmt.setLong(4, update.getTime());
                if (stmt.executeUpdate() <= 0) {
                    throw SchemaExceptionCodes.ALREADY_LOCKED.create(schema.getSchema());
                }
            }
            // If thread comes that far, lock has been acquired
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static boolean isNotTimedOut(ExecutedTask task, Idiom idiom) {
        return isTimedOut(task, idiom) == false;
    }

    private static boolean isTimedOut(ExecutedTask task, Idiom idiom) {
        // Check if threshold is not exceeded
        long idleMillis = idiom.getIdleMillis();
        return idleMillis > 0 && (System.currentTimeMillis() - task.getLastModified().getTime() > idleMillis);
    }

    @Override
    public long getIdleMillis(boolean background) {
        return (background ? Idiom.BACKGROUND : Idiom.LOCKED).getIdleMillis();
    }

    @Override
    public boolean tryRefreshSchemaLock(Schema schema, boolean background) throws OXException {
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
            stmt = con.prepareStatement("UPDATE updateTask SET lastModified = ? WHERE cid=0 AND uuid=?");
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setBytes(2, idiom.getUuid());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void unlockSchema(final Schema schema, final boolean background) throws OXException {
        int poolId = schema.getPoolId();
        int rollback = 0;
        Connection con = Database.get(poolId, schema.getSchema());
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
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (OXException e) {
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
        final ExecutedTask[] tasks = readUpdateTasks(Optional.of(idiom.getName()), con);
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
            stmt = con.prepareStatement("DELETE FROM updateTask WHERE cid=0 AND uuid=?");
            stmt.setBytes(1, idiom.getUuid());
            if (stmt.executeUpdate() == 0) {
                throw SchemaExceptionCodes.UNLOCK_FAILED.create(schema.getSchema());
            }
        } catch (SQLException e) {
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
    private static SchemaUpdateState loadSchemaStatus(int poolId, String schemaName, Connection con) throws OXException {
        final SchemaUpdateStateImpl retval = new SchemaUpdateStateImpl();
        retval.setBlockingUpdatesRunning(false);
        retval.setBackgroundUpdatesRunning(false);
        loadUpdateTasks(con, retval);
        retval.setServer(Database.getServerName());
        retval.setSchema(schemaName);
        retval.setPoolId(poolId);
        return retval;
    }

    private static Map<String, BiConsumer<ExecutedTask, SchemaUpdateStateImpl>> STATE_UPDATERS = ImmutableMap.<String, BiConsumer<ExecutedTask, SchemaUpdateStateImpl>> builder()
        .put(Idiom.LOCKED.getName(), (task, state) -> {
            if (isNotTimedOut(task, Idiom.LOCKED)) {
                state.setBlockingUpdatesRunning(true);
                state.setBlockingUpdatesRunningSince(task.getLastModified());
            }
        })
        .put(Idiom.BACKGROUND.getName(), (task, state) -> {
            if (isNotTimedOut(task, Idiom.BACKGROUND)) {
                state.setBackgroundUpdatesRunning(true);
                state.setBackgroundUpdatesRunningSince(task.getLastModified());
            }
        })
        .build();

    private static void loadUpdateTasks(final Connection con, final SchemaUpdateStateImpl state) throws OXException {
        for (ExecutedTask task : readUpdateTasks(Optional.empty(), con)) {
            BiConsumer<ExecutedTask, SchemaUpdateStateImpl> c = STATE_UPDATERS.get(task.getTaskName());
            if (c == null) {
                state.addExecutedTask(task.getTaskName(), task.isSuccessful());
            } else {
                c.accept(task, state);
            }
        }
    }

    private static ExecutedTask[] readUpdateTasks(Optional<String> optionalTaskName, Connection con) throws OXException {
        if (optionalTaskName.isPresent()) {
            String taskName = optionalTaskName.get();
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement("SELECT taskName,successful,lastModified,uuid FROM updateTask WHERE cid=0 AND taskName=? FOR UPDATE");
                stmt.setString(1, taskName);
                result = stmt.executeQuery();
                return result.next() ? new ExecutedTask[] { createExecutedTaskFrom(result) } : new ExecutedTask[0];
            } catch (SQLException e) {
                throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            } finally {
                closeSQLStuff(result, stmt);
            }
        }

        // No task name filter. Read them all.
        List<ExecutedTask> retval;
        {
            Statement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.createStatement();
                result = stmt.executeQuery("SELECT taskName,successful,lastModified,uuid FROM updateTask WHERE cid=0 FOR UPDATE");
                if (result.next() == false) {
                    return new ExecutedTask[0];
                }

                retval = new ArrayList<ExecutedTask>(512);
                do {
                    retval.add(createExecutedTaskFrom(result));
                } while (result.next());
            } catch (SQLException e) {
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

    private static ExecutedTask createExecutedTaskFrom(ResultSet result) throws SQLException {
        return new ExecutedTaskImpl(result.getString(1), result.getBoolean(2), new Date(result.getLong(3)), UUIDs.toUUID(result.getBytes(4)));
    }

    @Override
    public void addExecutedTask(Connection con, String taskName, boolean success, int poolId, String schema) throws OXException {
        doAddExecutedTasks(con, Collections.singletonList(taskName), success, poolId, schema);
    }

    @Override
    public void addExecutedTasks(Connection con, Collection<String> taskNames, boolean success, int poolId, String schema) throws OXException {
        if (null == taskNames || taskNames.isEmpty()) {
            return;
        }

        doAddExecutedTasks(con, taskNames, success, poolId, schema);
    }

    private void doAddExecutedTasks(Connection con, Collection<String> taskNames, boolean success, int poolId, String schema) throws OXException {
        doAddExecutedTasks(con, taskNames, success);
        invalidateCache(poolId, schema);
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
            ExecutedTask[] tasks = readUpdateTasks(Optional.empty(), con);
            con.commit();
            return tasks;
        } catch (SQLException e) {
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
        } catch (OXException e) {
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
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

    @Override
    public void invalidateCache(Schema schema) {
        invalidateCache(schema.getPoolId(), schema.getSchema());
    }

    private void invalidateCache(int poolId, String schema) {
        Cache cache = this.cache;
        if (null != cache) {
            CacheKey key = cache.newCacheKey(poolId, schema);
            try {
                cache.remove(key);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

}
