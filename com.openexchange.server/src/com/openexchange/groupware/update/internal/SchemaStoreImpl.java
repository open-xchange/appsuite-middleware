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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import edu.emory.mathcs.backport.java.util.Collections;

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
    private static final String LOCKED = "LOCKED";
    private static final String BACKGROUND = "BACKGROUND";

    final Lock cacheLock = new ReentrantLock();

    Cache cache;

    public SchemaStoreImpl() {
        super();
    }

    @Override
    protected SchemaUpdateState getSchema(final int poolId, final String schemaName, final Connection con) throws OXException {
        return SerializedCachingLoader.fetch(cache, CACHE_REGION, null, cacheLock, new StorageLoader<SchemaUpdateState>() {
            @Override
            public Serializable getKey() {
                return cache.newCacheKey(poolId, schemaName);
            }
            @Override
            public SchemaUpdateState load() throws OXException {
                return loadSchema(con);
            }
        });
    }

    static SchemaUpdateState loadSchema(Connection con) throws OXException {
        final SchemaUpdateState retval;
        try {
            con.setAutoCommit(false);
            checkForTable(con);
            retval = loadSchemaStatus(con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
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
    public void lockSchema(final Schema schema, final int contextId, final boolean background) throws OXException {
        final int poolId = Database.resolvePool(contextId, true);
        CacheKey key = null;
        if (null != cache) {
            key = cache.newCacheKey(poolId, schema.getSchema());
            try {
                cache.remove(key);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        lockSchemaDB(schema, contextId, background);
        if (null != cache && null != key) {
            try {
                cache.remove(key);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    private static void lockSchemaDB(final Schema schema, final int contextId, final boolean background) throws OXException {
        final Connection con = Database.get(contextId, true);
        try {
            con.setAutoCommit(false); // BEGIN
            // Insert lock
            insertLock(con, schema, background ? BACKGROUND : LOCKED);
            // Everything went fine. Schema is marked as locked
            con.commit();
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } catch (final SQLException e) {
            rollback(con);
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private static final int MYSQL_DEADLOCK = 1213;

    private static final int MYSQL_DUPLICATE = 1062;

    private static void insertLock(final Connection con, final Schema schema, final String idiom) throws SQLException, OXException {
        if (hasUUID(con)) {
            insertLockUUID(con, schema, idiom);
        } else {
            insertLockNoUUID(con, schema, idiom);
        }
    }

    private static void insertLockNoUUID(Connection con, Schema schema, String idiom) throws OXException {
        // Check for existing lock exclusively
        final ExecutedTask[] tasks = readUpdateTasks(con);
        for (final ExecutedTask task : tasks) {
            if (idiom.equals(task.getTaskName())) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(schema.getSchema());
            }
        }
        // Insert lock
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO updateTask (cid,taskName,successful,lastModified) VALUES (0,?,true,?)");
            stmt.setString(1, idiom);
            stmt.setLong(2, System.currentTimeMillis());
            if (stmt.executeUpdate() == 0) {
                throw SchemaExceptionCodes.LOCK_FAILED.create(schema.getSchema());
            }
        } catch (final SQLException e) {
            if (MYSQL_DEADLOCK == e.getErrorCode() || MYSQL_DUPLICATE == e.getErrorCode()) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(e, schema.getSchema());
            }
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void insertLockUUID(Connection con, Schema schema, String idiom) throws OXException {
        // Check for existing lock exclusively
        final ExecutedTask[] tasks = readUpdateTasks(con);
        for (final ExecutedTask task : tasks) {
            if (idiom.equals(task.getTaskName())) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(schema.getSchema());
            }
        }
        // Insert lock
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO updateTask (cid,taskName,successful,lastModified,uuid) VALUES (0,?,true,?,?)");
            stmt.setString(1, idiom);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setBytes(3, generateUUID());
            if (stmt.executeUpdate() == 0) {
                throw SchemaExceptionCodes.LOCK_FAILED.create(schema.getSchema());
            }
        } catch (final SQLException e) {
            if (MYSQL_DEADLOCK == e.getErrorCode() || MYSQL_DUPLICATE == e.getErrorCode()) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(e, schema.getSchema());
            }
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void unlockSchema(final Schema schema, final int contextId, final boolean background) throws OXException {
        final int poolId = Database.resolvePool(contextId, true);
        CacheKey key = null;
        if (null != cache) {
            key = cache.newCacheKey(poolId, schema.getSchema());
            try {
                cache.remove(key);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        unlockSchemaDB(schema, contextId, background);
        if (null != cache && null != key) {
            try {
                cache.remove(key);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    private static void unlockSchemaDB(final Schema schema, final int contextId, final boolean background) throws OXException {
        final Connection con = Database.get(contextId, true);
        try {
            // End of update process, so unlock schema
            con.setAutoCommit(false);
            // Delete lock
            deleteLock(con, schema, background ? BACKGROUND : LOCKED);
            // Everything went fine. Schema is marked as unlocked
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private static void deleteLock(final Connection con, final Schema schema, final String idiom) throws OXException {
        // Check for existing lock exclusively
        final ExecutedTask[] tasks = readUpdateTasks(con);
        boolean found = false;
        for (final ExecutedTask task : tasks) {
            if (idiom.equals(task.getTaskName())) {
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
            stmt.setString(1, idiom);
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
    private static SchemaUpdateState loadSchemaStatus(final Connection con) throws OXException, SQLException {
        final SchemaUpdateStateImpl retval = new SchemaUpdateStateImpl();
        retval.setBlockingUpdatesRunning(false);
        retval.setBackgroundUpdatesRunning(false);
        loadUpdateTasks(con, retval);
        retval.setGroupwareCompatible(true);
        retval.setAdminCompatible(true);
        retval.setServer(Database.getServerName());
        retval.setSchema(con.getCatalog());
        return retval;
    }

    private static void loadUpdateTasks(final Connection con, final SchemaUpdateStateImpl state) throws OXException {
        for (final ExecutedTask task : readUpdateTasks(con)) {
            if (LOCKED.equals(task.getTaskName())) {
                state.setBlockingUpdatesRunning(true);
            } else if (BACKGROUND.equals(task.getTaskName())) {
                state.setBackgroundUpdatesRunning(true);
            } else {
                state.addExecutedTask(task.getTaskName());
            }
        }
    }

    private static ExecutedTask[] readUpdateTasks(final Connection con) throws OXException {
        final String sql = "SELECT taskName,successful,lastModified FROM updateTask WHERE cid=0 FOR UPDATE";
        Statement stmt = null;
        ResultSet result = null;
        final List<ExecutedTask> retval = new ArrayList<ExecutedTask>();
        try {
            stmt = con.createStatement();
            result = stmt.executeQuery(sql);
            while (result.next()) {
                final ExecutedTask task = new ExecutedTaskImpl(result.getString(1), result.getBoolean(2), new Date(result.getLong(3)));
                retval.add(task);
            }
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        Collections.sort(retval, new Comparator<ExecutedTask>() {

            @Override
            public int compare(final ExecutedTask o1, final ExecutedTask o2) {
                final Date lastModified1 = o1.getLastModified();
                final Date lastModified2 = o2.getLastModified();
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
    public void addExecutedTask(final Connection con, final String taskName, final boolean success, final int poolId, final String schema) throws OXException {
        addExecutedTask(con, taskName, success);
        if (null != cache) {
            final CacheKey key = cache.newCacheKey(poolId, schema);
            try {
                cache.remove(key);
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    private static void addExecutedTask(final Connection con, final String taskName, final boolean success) throws OXException {
        try {
            if (hasUUID(con)) {
                addExecutedTaskUUID(con, taskName, success);
            } else {
                addExecutedTaskNoUUID(con, taskName, success);
            }
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
    }

    private static void addExecutedTaskNoUUID(Connection con, String taskName, boolean success) throws OXException {
        boolean update = false;
        for (final ExecutedTask executed : readUpdateTasks(con)) {
            if (taskName.equals(executed.getTaskName())) {
                update = true;
                break;
            }
        }
        final String insertSQL = "INSERT INTO updateTask (cid,successful,lastModified,taskName) VALUES (0,?,?,?)";
        final String updateSQL = "UPDATE updateTask SET successful=?, lastModified=? WHERE cid=0 AND taskName=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(update ? updateSQL : insertSQL);
            int pos = 1;
            stmt.setBoolean(pos++, success);
            stmt.setLong(pos++, System.currentTimeMillis());
            stmt.setString(pos++, taskName);
            final int rows = stmt.executeUpdate();
            if (1 != rows) {
                throw SchemaExceptionCodes.WRONG_ROW_COUNT.create(I(1), I(rows));
            }
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void addExecutedTaskUUID(Connection con, String taskName, boolean success) throws OXException {
        boolean update = false;
        for (final ExecutedTask executed : readUpdateTasks(con)) {
            if (taskName.equals(executed.getTaskName())) {
                update = true;
                break;
            }
        }
        final String insertSQL = "INSERT INTO updateTask (cid,successful,lastModified,taskName,uuid) VALUES (0,?,?,?,?)";
        final String updateSQL = "UPDATE updateTask SET successful=?, lastModified=? WHERE cid=0 AND taskName=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(update ? updateSQL : insertSQL);
            int pos = 1;
            stmt.setBoolean(pos++, success);
            stmt.setLong(pos++, System.currentTimeMillis());
            stmt.setString(pos++, taskName);
            if (!update) {
                stmt.setBytes(pos++, generateUUID());
            }
            final int rows = stmt.executeUpdate();
            if (1 != rows) {
                throw SchemaExceptionCodes.WRONG_ROW_COUNT.create(I(1), I(rows));
            }
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public ExecutedTask[] getExecutedTasks(final int poolId, final String schemaName) throws OXException {
        final Connection con = Database.get(poolId, schemaName);
        final ExecutedTask[] retval;
        try {
            con.setAutoCommit(false);
            retval = readUpdateTasks(con);
            con.commit();
        } catch (final SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(poolId, con);
        }
        return retval;
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
        if (null != cache) {
            try {
                cache.clear();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            cache = null;
        }
    }

    private static boolean hasUUID(Connection con) throws SQLException {
        return Tools.columnExists(con, TABLE_NAME, "uuid");
    }

    private static byte[] generateUUID() {
        UUID uuid = UUID.randomUUID();
        return UUIDs.toByteArray(uuid);
    }
}
