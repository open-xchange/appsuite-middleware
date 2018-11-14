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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.utils.CreateTableRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.Updater;
import com.openexchange.java.util.Tools;

public class OXUtilMySQLStorageCommon {

    /**
     * High speed test for whitespace! Faster than the java one (from some testing).
     *
     * @return <code>true</code> if the indicated character is whitespace; otherwise <code>false</code>
     */
    public static boolean isWhitespace(final char c) {
        switch (c) {
            case 9: // 'unicode: 0009
            case 10: // 'unicode: 000A'
            case 11: // 'unicode: 000B'
            case 12: // 'unicode: 000C'
            case 13: // 'unicode: 000D'
            case 28: // 'unicode: 001C'
            case 29: // 'unicode: 001D'
            case 30: // 'unicode: 001E'
            case 31: // 'unicode: 001F'
            case ' ': // Space
                // case Character.SPACE_SEPARATOR:
                // case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks for an empty string.
     *
     * @param string The string
     * @return <code>true</code> if input is null or empty; else <code>false</code>
     */
    public static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static Connection getSimpleSQLConnectionFor(Database db) throws StorageException {
        String passwd = "";
        if (db.getPassword() != null) {
            passwd = db.getPassword();
        }

        try {
            return cache.getSimpleSQLConnectionWithoutTimeout(db.getUrl(), db.getLogin(), passwd, db.getDriver());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString(), e);
        } catch (final ClassNotFoundException e) {
            LOG.error("Driver not found to create database ", e);
            throw new StorageException(e);
        }
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXUtilMySQLStorageCommon.class);

    private static final AdminCache cache = ClientAdminThread.cache;

    /**
     * Determines all existing schemas for specified database host.
     *
     * @param db The database providing connect information
     * @return A listing of available database schemas
     * @throws StorageException If database schemas cannot be returned
     */
    public static List<String> listDatabases(Database db) throws StorageException {
        Connection con = getSimpleSQLConnectionFor(db);
        try {
            return listDatabases(con, db.getName());
        } finally {
            cache.closeSimpleConnection(con);
        }
    }

    private static List<String> listDatabases(Connection con, String name) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SHOW DATABASES LIKE ?");
            stmt.setString(1, name + "\\_%");
            result = stmt.executeQuery();
            if (false == result.next()) {
                return Collections.emptyList();
            }

            // Schema name should match: <database-name> + '_' + <unique-number>
            int beginIndex = name.length() + 1;
            List<String> schemas = new LinkedList<>();
            do {
                String schemaName = result.getString(1);
                try {
                    int uniqueNum = Tools.getUnsignedInteger(schemaName.substring(beginIndex));
                    if (uniqueNum > 0) {
                        schemas.add(schemaName);
                    }
                } catch (RuntimeException e) {
                    // Discard...
                    LOG.debug("Failed to validate schema name: {}", schemaName, e);
                }
            } while (result.next());
            return schemas;
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * Checks if the concrete database schema exists.
     *
     * @param db The database providing connect information as schema name
     * @return <code>true</code> if such a database schema exists; otherwise <code>false</code>
     * @throws StorageException If schema existence cannot be checked
     */
    public static boolean existsDatabase(Database db) throws StorageException {
        Connection con = getSimpleSQLConnectionFor(db);
        try {
            return existsDatabase(con, db.getScheme());
        } finally {
            cache.closeSimpleConnection(con);
        }
    }

    private static boolean existsDatabase(final Connection con, final String name) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SHOW DATABASES LIKE ?");
            stmt.setString(1, name);
            result = stmt.executeQuery();
            return result.next();
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    public static void createDatabase(final Database db, Connection configdbCon) throws StorageException {
        Connection con = getSimpleSQLConnectionFor(db);
        boolean error = true;
        boolean created = false;
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            if (existsDatabase(con, db.getScheme())) {
                throw new StorageException("Database \"" + db.getScheme() + "\" already exists");
            }
            createDatabaseSchema(con, db.getScheme());
            // Only delete the schema if it has been created successfully. Otherwise it may happen that we delete a longly existing schema.
            // See bug 18788.
            created = true;

            con.setCatalog(db.getScheme());
            pumpData2DatabaseNew(con, CreateTableRegistry.getInstance().getList());
            initUpdateTaskTable(con, db.getId().intValue(), db.getScheme());

            createSchemaCountEntry(db, configdbCon);

            con.commit();
            rollback = false;
            error = false;
        } catch (final SQLException e) {
            throw new StorageException(e.toString());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            if (error && created) {
                deleteDatabaseSchema(con, db);
            }
            cache.closeSimpleConnection(con);
        }
    }

    private static void createDatabaseSchema(final Connection con, final String name) throws StorageException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate("CREATE DATABASE `" + name + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void createSchemaCountEntry(Database db) throws SQLException, StorageException {
        Connection configdbCon = null;
        boolean rollback = false;
        try {
            configdbCon = cache.getWriteConnectionForConfigDB();
            configdbCon.setAutoCommit(false);
            rollback = true;

            createSchemaCountEntry(db, configdbCon);

            configdbCon.commit();
            rollback = false;
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e.toString(), e);
        } finally {
            if (rollback) {
                rollback(configdbCon);
            }
            autocommit(configdbCon);
            try {
                cache.pushWriteConnectionForConfigDB(configdbCon);
            } catch (PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    private static void createSchemaCountEntry(Database db, Connection configdbCon) throws SQLException, StorageException {
        if (null == configdbCon) {
            createSchemaCountEntry(db);
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = configdbCon.prepareStatement("INSERT INTO contexts_per_dbschema (db_pool_id, schemaname, count, creating_date) VALUES (?, ?, 0, ?)");
            stmt.setInt(1, db.getId().intValue());
            stmt.setString(2, db.getScheme());
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = configdbCon.prepareStatement("INSERT INTO dbschema_lock (db_pool_id, schemaname) VALUES (?, ?)");
            stmt.setInt(1, db.getId().intValue());
            stmt.setString(2, db.getScheme());
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static void pumpData2DatabaseNew(final Connection con, final List<CreateTableService> createTables) throws StorageException {
        final Set<String> existingTables = new HashSet<String>();
        final List<CreateTableService> toCreate = new ArrayList<CreateTableService>(createTables.size());
        toCreate.addAll(createTables);
        for (CreateTableService next; (next = findNext(toCreate, existingTables)) != null;) {
            try {
                next.perform(con);
                for (final String createdTable : next.tablesToCreate()) {
                    existingTables.add(createdTable);
                }
                toCreate.remove(next);
            } catch (final OXException e) {
                throw new StorageException("Failed to create tables " + Arrays.toString(next.tablesToCreate()) + ": " + e.getMessage(), e);
            }
        }
        if (!toCreate.isEmpty()) {
            final StringBuilder sb = new StringBuilder(2048);
            sb.append("Unable to determine next CreateTableService to execute.\n");
            sb.append("Existing tables: ");
            for (final String existingTable : existingTables) {
                sb.append(existingTable);
                sb.append(',');
            }
            sb.setCharAt(sb.length() - 1, '\n');
            for (final CreateTableService service : toCreate) {
                sb.append(service.getClass().getName());
                sb.append(": ");
                for (final String tableToCreate : service.requiredTables()) {
                    sb.append(tableToCreate);
                    sb.append(',');
                }
                sb.setCharAt(sb.length() - 1, '\n');
            }
            sb.deleteCharAt(sb.length() - 1);
            throw new StorageException(sb.toString());
        }
    }

    private static CreateTableService findNext(final List<CreateTableService> toCreate, final Set<String> existingTables) {
        for (final CreateTableService service : toCreate) {
            final List<String> requiredTables = new ArrayList<String>();
            for (final String requiredTable : service.requiredTables()) {
                requiredTables.add(requiredTable);
            }
            if (existingTables.containsAll(requiredTables)) {
                return service;
            }
        }
        return null;
    }

    private static void initUpdateTaskTable(final Connection con, final int poolId, final String schema) throws StorageException {
        final UpdateTaskV2[] tasks = Updater.getInstance().getAvailableUpdateTasks();
        final SchemaStore store = SchemaStore.getInstance();
        try {
            List<String> taskNames = new ArrayList<>(tasks.length);
            for (UpdateTaskV2 task : tasks) {
                taskNames.add(task.getClass().getName());
            }

            store.addExecutedTasks(con, taskNames, true, poolId, schema);
        } catch (final OXException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    /**
     * Deletes specified database (schema) and associated schema-count entry.
     *
     * @param db The database instance providing at least identifier and schema name
     * @param configdbCon The connection to the ConfigD (optional)
     * @throws StorageException If deleting database (schema) fails
     */
    public static void deleteDatabase(final Database db, final Connection configdbCon) throws StorageException {
        Connection con;
        try {
            con = cache.getSimpleSQLConnectionWithoutTimeout(db.getUrl(), db.getLogin(), db.getPassword(), db.getDriver());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString(), e);
        } catch (final ClassNotFoundException e) {
            LOG.error("Driver not found to create database ", e);
            throw new StorageException(e);
        }

        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;

            deleteDatabase(con, db, configdbCon);

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw new StorageException(e.toString(), e);
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            cache.closeSimpleConnection(con);
        }
    }

    /**
     * Deletes specified database (schema) and associated schema-count entry.
     * <p>
     * <b>Note</b>: Specified <code>con</code> argument is required to be not <code>null</code>!
     *
     * @param con The connection to the database, on which the schema resides that is supposed to be deleted
     * @param db The database instance providing at least identifier and schema name
     * @param configdbCon The connection to the ConfigD (optional)
     * @throws StorageException If deleting database (schema) fails
     */
    private static void deleteDatabase(Connection con, Database db, Connection configdbCon) throws StorageException {
        if (null == db.getScheme()) {
            throw new StorageException("Schema not set in specified Database instance");
        }
        deleteDatabaseSchema(con, db);
        deleteSchemaCountEntry(db, configdbCon);
    }

    private static void deleteDatabaseSchema(Connection con, Database db) throws StorageException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate("DROP DATABASE IF EXISTS `" + db.getScheme() + "`");
        } catch (final SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void deleteSchemaCountEntry(Database db) throws StorageException {
        Connection configdbCon = null;
        boolean rollback = false;
        try {
            configdbCon = cache.getWriteConnectionForConfigDB();
            configdbCon.setAutoCommit(false);
            rollback = true;

            deleteSchemaCountEntry(db, configdbCon);

            configdbCon.commit();
            rollback = false;
        } catch (PoolException e) {
            LOG.error("Pool Error", e);
            throw new StorageException(e.toString(), e);
        } catch (SQLException e) {
            throw new StorageException(e.toString(), e);
        } finally {
            if (rollback) {
                rollback(configdbCon);
            }
            autocommit(configdbCon);
            try {
                cache.pushWriteConnectionForConfigDB(configdbCon);
            } catch (PoolException e) {
                LOG.error("Error pushing configdb connection to pool!", e);
            }
        }
    }

    private static void deleteSchemaCountEntry(Database db, Connection configdbCon) throws StorageException {
        if (null == configdbCon) {
            deleteSchemaCountEntry(db);
            return;
        }

        PreparedStatement pstmt = null;
        try {
            pstmt = configdbCon.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=? AND schemaname=?");
            pstmt.setInt(1, db.getId().intValue());
            pstmt.setString(2, db.getScheme());
            pstmt.executeUpdate();
            closeSQLStuff(pstmt);
            pstmt = null;

            pstmt = configdbCon.prepareStatement("DELETE FROM dbschema_lock WHERE db_pool_id=? AND schemaname=?");
            pstmt.setInt(1, db.getId().intValue());
            pstmt.setString(2, db.getScheme());
            pstmt.executeUpdate();
            closeSQLStuff(pstmt);
            pstmt = null;
        } catch (SQLException e) {
            throw new StorageException(e.toString(), e);
        } finally {
            closeSQLStuff(pstmt);
        }
    }

}
