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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.sqlStorage.CreateTableRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.database.CreateTableService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.Updater;

public class OXUtilMySQLStorageCommon {

    private static final Log LOG = LogFactory.getLog(OXUtilMySQLStorageCommon.class);

    private static AdminCache cache = ClientAdminThread.cache;

    public void createDatabase(final Database db) throws StorageException {
        final List<String> createTableStatements;
        try {
            createTableStatements = cache.getOXDBInitialQueries();
        } catch (final OXGenericException e) {
            LOG.error("Error reading DB init Queries!", e);
            throw new StorageException(e);
        }
        final Connection con;
        String sql_pass = "";
        if (db.getPassword() != null) {
            sql_pass = db.getPassword();
        }
        try {
            con = cache.getSimpleSQLConnectionWithoutTimeout(db.getUrl(), db.getLogin(), sql_pass, db.getDriver());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString(), e);
        } catch (final ClassNotFoundException e) {
            LOG.error("Driver not found to create database ", e);
            throw new StorageException(e);
        }
        boolean created = false;
        try {
            con.setAutoCommit(false);
            if (existsDatabase(con, db.getScheme())) {
                throw new StorageException("Database \"" + db.getScheme() + "\" already exists");
            }
            createDatabase(con, db.getScheme());
            // Only delete the schema if it has been created successfully. Otherwise it may happen that we delete a longly existing schema.
            // See bug 18788.
            created = true;
            con.setCatalog(db.getScheme());
            pumpData2DatabaseOld(con, createTableStatements);
            pumpData2DatabaseNew(con, CreateTableRegistry.getInstance().getList());
            initUpdateTaskTable(con, db.getId().intValue(), db.getScheme());
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            if (created) {
                deleteDatabase(con, db);
            }
            throw new StorageException(e.toString());
        } catch (final StorageException e) {
            rollback(con);
            if (created) {
                deleteDatabase(con, db);
            }
            throw e;
        } finally {
            autocommit(con);
            cache.closeSimpleConnection(con);
        }
    }

    private boolean existsDatabase(final Connection con, final String name) throws StorageException {
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

    private void createDatabase(final Connection con, final String name) throws StorageException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate("CREATE DATABASE `" + name + "` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci");
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void pumpData2DatabaseOld(final Connection con, final List<String> db_queries) throws StorageException {
        Statement stmt = null;
        try {
            try {
                stmt = con.createStatement();
            } catch (final SQLException e) {
                throw new StorageException(e.getMessage(), e);
            }
            for (final String sqlCreate : db_queries) {
                stmt.addBatch(sqlCreate);
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            if (e.getMessage().indexOf("already exists") < 0) { // MySQL error: "PROCEDURE get_mail_service_id already exists"
                throw new StorageException(e.getMessage(), e);
            }
            closeSQLStuff(stmt);
            stmt = null;
            if (LOG.isDebugEnabled()) {
                LOG.info("Batch table creation failed.", e);
            } else {
                LOG.info("Batch table creation failed.");
            }
            /*
             * Execute them one-by-one...
             */
            try {
                for (final String sqlCreate : db_queries) {
                    stmt = con.createStatement();
                    try {
                        stmt.executeUpdate(sqlCreate);
                    } catch (final SQLException sqlException) {
                        if (LOG.isDebugEnabled()) {
                            LOG.warn("Following SQL CREATE statement failed with \"" + sqlException.getMessage() + "\": "  + sqlCreate, sqlException);
                        } else {
                            LOG.warn("Following SQL CREATE statement failed with \"" + sqlException.getMessage() + "\": "  + sqlCreate);
                        }
                    } finally {
                        closeSQLStuff(stmt);
                        stmt = null;
                    }
                }
            } catch (final SQLException abort) {
                throw new StorageException(abort.getMessage(), abort);
            }
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private void pumpData2DatabaseNew(final Connection con, final List<CreateTableService> createTables) throws StorageException {
        final Set<String> existingTables = new HashSet<String>();
        final List<CreateTableService> toCreate = new ArrayList<CreateTableService>(createTables.size());
        toCreate.addAll(createTables);
        CreateTableService next;
        try {
            while ((next = findNext(toCreate, existingTables)) != null) {
                next.perform(con);
                for (final String createdTable : next.tablesToCreate()) {
                    existingTables.add(createdTable);
                }
                toCreate.remove(next);
            }
        } catch (final OXException e) {
            throw new StorageException(e.getMessage(), e);
        }
        if (!toCreate.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
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
            sb.setLength(sb.length() - 1);
            throw new StorageException(sb.toString());
        }
    }

    private CreateTableService findNext(final List<CreateTableService> toCreate, final Set<String> existingTables) {
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

    private void initUpdateTaskTable(final Connection con, final int poolId, final String schema) throws StorageException {
        final UpdateTask[] tasks = Updater.getInstance().getAvailableUpdateTasks();
        final SchemaStore store = SchemaStore.getInstance();
        try {
            for (final UpdateTask task : tasks) {
                store.addExecutedTask(con, task.getClass().getName(), true, poolId, schema);
            }
        } catch (final OXException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    public void deleteDatabase(final Database db) throws StorageException {
        final Connection con;
        try {
            con = cache.getSimpleSQLConnectionWithoutTimeout(db.getUrl(), db.getLogin(), db.getPassword(), db.getDriver());
        } catch (final SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e.toString(), e);
        } catch (final ClassNotFoundException e) {
            LOG.error("Driver not found to create database ", e);
            throw new StorageException(e);
        }
        try {
            deleteDatabase(con, db);
        } finally {
            cache.closeSimpleConnection(con);
        }
    }

    private void deleteDatabase(final Connection con, final Database db) throws StorageException {
        Statement stmt = null;
        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.executeUpdate("DROP DATABASE IF EXISTS `" + db.getScheme() + "`");
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(stmt);
        }
    }

}
