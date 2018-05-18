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

package com.openexchange.database.internal.change.custom;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import com.google.common.collect.Lists;
import com.openexchange.database.Databases;
import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * {@link CountTablesCustomTaskChange}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CountTablesCustomTaskChange implements CustomTaskChange, CustomTaskRollback {

    /**
     * Initializes a new {@link CountTablesCustomTaskChange}.
     */
    public CountTablesCustomTaskChange() {
        super();
    }

    @Override
    public String getConfirmationMessage() {
        return "Count tables successfully created";
    }

    @Override
    public void setUp() throws SetupException {
        // Nothing
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // Ignore
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        // Ignore
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }

        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CountTablesCustomTaskChange.class);
        Connection configDbCon = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        boolean rollback = false;
        try {
            Databases.startTransaction(configDbCon);
            rollback = true;

            execute(configDbCon, logger);

            configDbCon.commit();
            rollback = false;

            logger.info("Count tables for ConfigDB successfully initialized");
        } catch (SQLException e) {
            logger.error("Failed to initialize count tables for ConfigDB", e);
            throw new CustomChangeException("SQL error", e);
        } catch (CustomChangeException e) {
            logger.error("Failed to initialize count tables for ConfigDB", e);
            throw e;
        } catch (RuntimeException e) {
            logger.error("Failed to initialize count tables for ConfigDB", e);
            throw new CustomChangeException("Runtime error", e);
        } finally {
            if (rollback) {
                Databases.rollback(configDbCon);
            }
            Databases.autocommit(configDbCon);
        }
    }

    private void execute(Connection configDbCon, org.slf4j.Logger logger) throws CustomChangeException {
        try {
            logger.info("Creating count tables for ConfigDB");
            createFilestoreCountTable(configDbCon);
            createDBPoolCountTable(configDbCon);
            createDBPoolSchemaCountTable(configDbCon);
            createSemaphoreTable(configDbCon);

            logger.info("Count tables successfully created. Initializing count tables for ConfigDB");
            checkFilestoreCountConsistency(configDbCon);
            checkDBPoolCountConsistency(configDbCon);
            checkDBPoolSchemaCountConsistency(configDbCon);
            logger.info("Count tables successfully initialized");
        } catch (SQLException e) {
            throw new CustomChangeException("SQL error", e);
        } catch (NoConnectionToDatabaseException e) {
            throw new CustomChangeException(e.getMessage(), e);
        }
    }

    private void createFilestoreCountTable(Connection configDbCon) throws SQLException {
        if (tableExists(configDbCon, "contexts_per_filestore")) {
            return;
        }

        String createStmt = "CREATE TABLE contexts_per_filestore (filestore_id INT4 UNSIGNED NOT NULL, count INT4 UNSIGNED NOT NULL, PRIMARY KEY (filestore_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

        PreparedStatement stmt = null;
        try {
            stmt = configDbCon.prepareStatement(createStmt);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void createDBPoolCountTable(Connection configDbCon) throws SQLException {
        if (false == tableExists(configDbCon, "contexts_per_dbpool")) {
            String createStmt = "CREATE TABLE contexts_per_dbpool (db_pool_id INT4 UNSIGNED NOT NULL, count INT4 UNSIGNED NOT NULL, PRIMARY KEY (db_pool_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            PreparedStatement stmt = null;
            try {
                stmt = configDbCon.prepareStatement(createStmt);
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }

        if (false == tableExists(configDbCon, "dbpool_lock")) {
            String createStmt = "CREATE TABLE dbpool_lock (db_pool_id INT4 UNSIGNED NOT NULL, PRIMARY KEY (db_pool_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            PreparedStatement stmt = null;
            try {
                stmt = configDbCon.prepareStatement(createStmt);
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

    private void createDBPoolSchemaCountTable(Connection configDbCon) throws SQLException {
        if (false == tableExists(configDbCon, "contexts_per_dbschema")) {
            String createStmt = "CREATE TABLE contexts_per_dbschema (db_pool_id INT4 UNSIGNED NOT NULL, schemaname VARCHAR(32) NOT NULL, count INT4 UNSIGNED NOT NULL, creating_date BIGINT(64) NOT NULL, PRIMARY KEY (db_pool_id, schemaname)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            PreparedStatement stmt = null;
            try {
                stmt = configDbCon.prepareStatement(createStmt);
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }

        if (false == tableExists(configDbCon, "dbschema_lock")) {
            String createStmt = "CREATE TABLE dbschema_lock (db_pool_id INT4 UNSIGNED NOT NULL, schemaname VARCHAR(32) NOT NULL, PRIMARY KEY (db_pool_id, schemaname)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            PreparedStatement stmt = null;
            try {
                stmt = configDbCon.prepareStatement(createStmt);
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

    private void createSemaphoreTable(Connection configDbCon) throws SQLException {
        if (tableExists(configDbCon, "ctx_per_schema_sem")) {
            return;
        }

        String createStmt = "CREATE TABLE `ctx_per_schema_sem` (id BIGINT UNSIGNED NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

        PreparedStatement stmt = null;
        try {
            stmt = configDbCon.prepareStatement(createStmt);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);

            stmt = configDbCon.prepareStatement("INSERT INTO ctx_per_schema_sem (id) VALUES (0)");
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void checkFilestoreCountConsistency(Connection configCon) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Drop non-existing ones held in count table
            stmt = configCon.prepareStatement("SELECT contexts_per_filestore.filestore_id FROM contexts_per_filestore LEFT JOIN filestore ON contexts_per_filestore.filestore_id=filestore.id WHERE filestore.id IS NULL");
            rs = stmt.executeQuery();
            if (rs.next()) {
                List<Integer> ids = new LinkedList<Integer>();
                do {
                    ids.add(Integer.valueOf(rs.getInt(1)));
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_filestore WHERE filestore_id=?");
                for (Integer id : ids) {
                    stmt.setInt(1, id.intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
            } else {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Check count entries for existing ones
            // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
            stmt = configCon.prepareStatement("SELECT filestore.id, COUNT(context.cid) AS num FROM filestore LEFT JOIN context ON filestore.id=context.filestore_id GROUP BY filestore.id ORDER BY num ASC");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No file store registered...
                return;
            }

            Map<Integer, Integer> counts = new LinkedHashMap<Integer, Integer>(32, 0.9F);
            do {
                int filestoreId = rs.getInt(1);
                int numContexts = rs.getInt(2);
                counts.put(Integer.valueOf(filestoreId), Integer.valueOf(numContexts));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            stmt = configCon.prepareStatement("INSERT INTO contexts_per_filestore (filestore_id, count) VALUES (?, ?) ON DUPLICATE KEY UPDATE count=?");
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                int count = entry.getValue().intValue();
                stmt.setInt(1, entry.getKey().intValue());
                stmt.setInt(2, count);
                stmt.setInt(3, count);
                stmt.addBatch();
            }
            stmt.executeBatch();
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void checkDBPoolCountConsistency(Connection configCon) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Drop non-existing ones held in count table
            stmt = configCon.prepareStatement("SELECT contexts_per_dbpool.db_pool_id FROM contexts_per_dbpool LEFT JOIN db_cluster ON contexts_per_dbpool.db_pool_id=db_cluster.write_db_pool_id WHERE db_cluster.write_db_pool_id IS NULL");
            rs = stmt.executeQuery();
            if (rs.next()) {
                List<Integer> ids = new LinkedList<Integer>();
                do {
                    ids.add(Integer.valueOf(rs.getInt(1)));
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=?");
                for (Integer id : ids) {
                    stmt.setInt(1, id.intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM dbpool_lock WHERE db_pool_id=?");
                for (Integer id : ids) {
                    stmt.setInt(1, id.intValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
            } else {
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Check count entries for existing ones
            // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
            stmt = configCon.prepareStatement("SELECT db_cluster.write_db_pool_id, COUNT(context_server2db_pool.cid) AS num FROM db_cluster LEFT JOIN context_server2db_pool ON db_cluster.write_db_pool_id = context_server2db_pool.write_db_pool_id GROUP BY db_cluster.write_db_pool_id ORDER BY num ASC");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No database registered...
                return;
            }

            Map<Integer, Integer> counts = new LinkedHashMap<Integer, Integer>(32, 0.9F);
            do {
                int filestoreId = rs.getInt(1);
                int numContexts = rs.getInt(2);
                counts.put(Integer.valueOf(filestoreId), Integer.valueOf(numContexts));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            stmt = configCon.prepareStatement("INSERT INTO contexts_per_dbpool (db_pool_id, count) VALUES (?, ?) ON DUPLICATE KEY UPDATE count=?");
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                int count = entry.getValue().intValue();
                stmt.setInt(1, entry.getKey().intValue());
                stmt.setInt(2, count);
                stmt.setInt(3, count);
                stmt.addBatch();
            }
            stmt.executeBatch();
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;

            stmt = configCon.prepareStatement("INSERT IGNORE INTO dbpool_lock (db_pool_id) VALUES (?)");
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                stmt.setInt(1, entry.getKey().intValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void checkDBPoolSchemaCountConsistency(Connection configCon) throws SQLException, NoConnectionToDatabaseException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Determine registered databases (ignore those with max_units=0)
            stmt = configCon.prepareStatement("SELECT write_db_pool_id FROM db_cluster WHERE max_units <> 0 ORDER BY write_db_pool_id ASC");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                // No database registered...
                return;
            }

            List<Integer> poolIds = new LinkedList<Integer>();
            do {
                poolIds.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Determine referenced schemas in 'context_server2db_pool' associations
            Map<Integer, DB> databases;
            Map<DB, Set<String>> db2ReferencedSchemas;
            {
                stmt = configCon.prepareStatement("SELECT DISTINCT write_db_pool_id, db_schema FROM context_server2db_pool");
                rs = stmt.executeQuery();
                Map<Integer, Set<String>> poolAndSchemas;
                if (rs.next()) {
                    poolAndSchemas = new LinkedHashMap<>();
                    do {
                        Integer poolId = Integer.valueOf(rs.getInt(1));
                        Set<String> schemas = poolAndSchemas.get(poolId);
                        if (null == schemas) {
                            schemas = new LinkedHashSet<>();
                            poolAndSchemas.put(poolId, schemas);
                        }
                        schemas.add(rs.getString(2));
                    } while (rs.next());
                } else {
                    poolAndSchemas = null;
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                databases = new HashMap<>(null == poolAndSchemas ? 16 : poolAndSchemas.size());
                db2ReferencedSchemas = new LinkedHashMap<>(null == poolAndSchemas ? 16 : poolAndSchemas.size());
                stmt = configCon.prepareStatement(Databases.getIN("SELECT db_pool_id, url, driver, login, password, name FROM db_pool WHERE db_pool_id IN (", poolIds.size()));
                int pos = 1;
                for (Integer poolId : poolIds) {
                    stmt.setInt(pos++, poolId.intValue());
                }
                rs = stmt.executeQuery();
                while (rs.next()) {
                    int poolId = rs.getInt(1);
                    DB db = new DB(poolId, rs.getString(6), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
                    Set<String> schemas = null == poolAndSchemas ? new LinkedHashSet<String>(0) : poolAndSchemas.get(Integer.valueOf(poolId));
                    if (null == schemas) {
                        schemas = new LinkedHashSet<String>(0);
                    }
                    db2ReferencedSchemas.put(db, schemas);
                    databases.put(Integer.valueOf(poolId), db);
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            // Determine really existing schemas per database
            Map<DB, Set<String>> db2ExistingSchemas = new LinkedHashMap<>(db2ReferencedSchemas.size());
            {
                for (DB db : db2ReferencedSchemas.keySet()) {
                    try {
                        List<String> schemas = listDatabases(db.name, db.url, db.driver, db.login, db.password);
                        db2ExistingSchemas.put(db, new LinkedHashSet<>(schemas));
                    } catch (NoConnectionToDatabaseException e) {
                        if (db2ReferencedSchemas.containsKey(db)) {
                            throw e;
                        }
                        // Ignore...
                    }
                }
            }

            // Determine contained schemas in 'contexts_per_dbschema' table
            Map<DB, Set<String>> db2ContainedSchemas = new LinkedHashMap<>(db2ReferencedSchemas.size());
            stmt = configCon.prepareStatement("SELECT db_pool_id, schemaname FROM contexts_per_dbschema");
            rs = stmt.executeQuery();
            Set<Integer> db2delete = null;
            Set<PoolAndSchema> schemas2delete = null;
            while (rs.next()) {
                Integer poolId = Integer.valueOf(rs.getInt(1));
                DB db = databases.get(poolId);
                if (null == db) {
                    // Such a database does not exist
                    if (null == db2delete) {
                        db2delete = new HashSet<>();
                    }
                    db2delete.add(poolId);
                } else {
                    String schema = rs.getString(2);
                    Set<String> existingSchemas = db2ExistingSchemas.get(db);
                    if (existingSchemas.remove(schema)) {
                        // Schema was contained in really existing schemas
                        Set<String> containedSchemas = db2ContainedSchemas.get(db);
                        if (null == containedSchemas) {
                            containedSchemas = new LinkedHashSet<>();
                            db2ContainedSchemas.put(db, containedSchemas);
                        }
                        containedSchemas.add(schema);
                    } else {
                        // Contained but does not exist
                        if (null == schemas2delete) {
                            schemas2delete = new HashSet<>();
                        }
                        schemas2delete.add(new PoolAndSchema(poolId.intValue(), schema));
                    }
                }
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Drop the ones referring to non-existing database hosts
            if (null != db2delete) {
                PreparedStatement deleteStmt = null;
                try {
                    deleteStmt = configCon.prepareStatement(Databases.getIN("DELETE FROM contexts_per_dbschema WHERE db_pool_id IN (", db2delete.size()));
                    int pos = 1;
                    for (Integer poolId : db2delete) {
                        deleteStmt.setInt(pos++, poolId.intValue());
                    }
                    deleteStmt.executeUpdate();
                    Databases.closeSQLStuff(deleteStmt);

                    deleteStmt = configCon.prepareStatement(Databases.getIN("DELETE FROM dbschema_lock WHERE db_pool_id IN (", db2delete.size()));
                    pos = 1;
                    for (Integer poolId : db2delete) {
                        deleteStmt.setInt(pos++, poolId.intValue());
                    }
                    deleteStmt.executeUpdate();
                } finally {
                    Databases.closeSQLStuff(deleteStmt);
                }
            }

            // Drop the ones referring to non-existing database schemas
            if (null != schemas2delete) {
                PreparedStatement deleteStmt = configCon.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=? AND schemaname=?");
                PreparedStatement deleteStmt2 = configCon.prepareStatement("DELETE FROM dbschema_lock WHERE db_pool_id=? AND schemaname=?");
                try {
                    for (PoolAndSchema poolAndSchema : schemas2delete) {
                        deleteStmt.setInt(1, poolAndSchema.dbId);
                        deleteStmt.setString(2, poolAndSchema.schema);
                        deleteStmt.addBatch();

                        deleteStmt2.setInt(1, poolAndSchema.dbId);
                        deleteStmt2.setString(2, poolAndSchema.schema);
                        deleteStmt2.addBatch();
                    }
                    deleteStmt.executeBatch();
                    deleteStmt2.executeBatch();
                } finally {
                    Databases.closeSQLStuff(deleteStmt, deleteStmt2);
                }
            }

            // Insert really existing schemas to 'contexts_per_dbschema' (if not contained)
            {
                long now = System.currentTimeMillis();
                for (Map.Entry<DB, Set<String>> entry : db2ExistingSchemas.entrySet()) {
                    DB db = entry.getKey();
                    Set<String> schemas = entry.getValue();
                    if (null != schemas && !schemas.isEmpty()) {
                        for (List<String> schemasToInsert : Lists.partition(new ArrayList<>(schemas), 25)) {
                            PreparedStatement insertStmt = null;
                            try {
                                insertStmt = configCon.prepareStatement("INSERT IGNORE INTO contexts_per_dbschema (db_pool_id, schemaname, count, creating_date) VALUES (?, ?, ?, ?)");
                                for (String schema : schemasToInsert) {
                                    insertStmt.setInt(1, db.dbId);
                                    insertStmt.setString(2, schema);
                                    insertStmt.setInt(3, 0);
                                    insertStmt.setLong(4, now);
                                    insertStmt.addBatch();

                                    Set<String> containedSchemas = db2ContainedSchemas.get(db);
                                    if (null == containedSchemas) {
                                        containedSchemas = new LinkedHashSet<>();
                                        db2ContainedSchemas.put(db, containedSchemas);
                                    }
                                    containedSchemas.add(schema);
                                }
                                insertStmt.executeBatch();
                            } finally {
                                Databases.closeSQLStuff(insertStmt);
                            }
                        }
                    }
                }
            }

            // Determine non-referenced ones (contained in 'contexts_per_dbschema' but not referenced within 'context_server2db_pool' associations)
            PreparedStatement updateStmt = null;
            try {
                for (Map.Entry<DB, Set<String>> db2ContainedSchemasEntry : db2ContainedSchemas.entrySet()) {
                    DB db = db2ContainedSchemasEntry.getKey();
                    Set<String> containedSchemas = db2ContainedSchemasEntry.getValue();
                    Set<String> referencedSchemas = db2ReferencedSchemas.get(db);

                    for (String containedSchema : containedSchemas) {
                        if (false == referencedSchemas.contains(containedSchema)) {
                            if (null == updateStmt) {
                                updateStmt = configCon.prepareStatement("UPDATE contexts_per_dbschema SET count=0 WHERE db_pool_id=? AND schemaname=?");
                            }
                            updateStmt.setInt(1, db.dbId);
                            updateStmt.setString(2, containedSchema);
                            updateStmt.addBatch();
                        }
                    }

                    // Don't know whether to check the other way around, which would mean there is an entry in 'context_server2db_pool' associations,
                    // but actually referring to a non-existing schema...
                }
                if (null != updateStmt) {
                    updateStmt.executeBatch();
                }
            } finally {
                Databases.closeSQLStuff(updateStmt);
            }

            class SchemaCount {

                final String schemaName;
                final int count;

                SchemaCount(String schemaName, int count) {
                    super();
                    this.count = count;
                    this.schemaName = schemaName;
                }
            };

            Map<Integer, List<SchemaCount>> counts = new LinkedHashMap<Integer, List<SchemaCount>>(32, 0.9F);
            for (Integer poolId : poolIds) {
                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = configCon.prepareStatement("SELECT db_schema,COUNT(db_schema) AS count FROM context_server2db_pool WHERE write_db_pool_id=? GROUP BY db_schema ORDER BY count ASC");
                stmt.setInt(1, poolId.intValue());
                rs = stmt.executeQuery();

                List<SchemaCount> schemaCounts = new LinkedList<SchemaCount>();
                while (rs.next()) {
                    schemaCounts.add(new SchemaCount(rs.getString(1), rs.getInt(2)));
                }
                counts.put(poolId, schemaCounts);
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            if (false == counts.isEmpty()) {
                // Insert with 25-sized batches
                for (List<Map.Entry<Integer, List<SchemaCount>>> entries : Lists.partition(new ArrayList<>(counts.entrySet()), 25)) {
                    stmt = configCon.prepareStatement("INSERT INTO contexts_per_dbschema (db_pool_id, schemaname, count, creating_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE count=?");
                    long now = System.currentTimeMillis();
                    for (Map.Entry<Integer, List<SchemaCount>> entry : entries) {
                        int poolId = entry.getKey().intValue();
                        List<SchemaCount> schemaCounts = entry.getValue();
                        for (SchemaCount schemaCount : schemaCounts) {
                            stmt.setInt(1, poolId);
                            stmt.setString(2, schemaCount.schemaName);
                            stmt.setInt(3, schemaCount.count);
                            stmt.setLong(4, now);
                            stmt.setInt(5, schemaCount.count);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                    Databases.closeSQLStuff(rs, stmt);
                    stmt = null;

                    stmt = configCon.prepareStatement("INSERT IGNORE INTO dbschema_lock (db_pool_id, schemaname) VALUES (?, ?)");
                    for (Map.Entry<Integer, List<SchemaCount>> entry : entries) {
                        int poolId = entry.getKey().intValue();
                        List<SchemaCount> schemaCounts = entry.getValue();
                        for (SchemaCount schemaCount : schemaCounts) {
                            stmt.setInt(1, poolId);
                            stmt.setString(2, schemaCount.schemaName);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                    Databases.closeSQLStuff(rs, stmt);
                    stmt = null;
                }
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private List<String> listDatabases(String name, String url, String driver, String login, String password) throws  SQLException, NoConnectionToDatabaseException {
        Connection con = getSimpleSQLConnectionFor(url, driver, login, password);
        try {
            return listDatabases(con, name);
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                Logger logger = org.slf4j.LoggerFactory.getLogger(CountTablesCustomTaskChange.class);
                logger.warn("Failed to close connection to database host {}", url, e);
            }
        }
    }

    private List<String> listDatabases(Connection con, String name) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SHOW DATABASES LIKE ?");
            stmt.setString(1, name + "\\_%");
            result = stmt.executeQuery();
            if (false == result.next()) {
                return Collections.emptyList();
            }

            List<String> schemas = new LinkedList<>();
            do {
                schemas.add(result.getString(1));
            } while (result.next());
            return schemas;
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private static Connection getSimpleSQLConnectionFor(String url, String driver, String login, String password) throws NoConnectionToDatabaseException {
        String passwd = "";
        if (password != null) {
            passwd = password;
        }

        try {
            Class.forName(driver);
            DriverManager.setLoginTimeout(120);

            Properties defaults = new Properties();
            defaults.put("user", login);
            defaults.put("password", passwd);
            defaults.setProperty("useSSL", "false");

            return DriverManager.getConnection(url, defaults);
        } catch (ClassNotFoundException e) {
            throw new NoConnectionToDatabaseException("Database " + extractHostName(url) + " is not accessible: No such driver class: " + driver, e);
        } catch (SQLException e) {
            throw new NoConnectionToDatabaseException("Database " + extractHostName(url) + " is not accessible: " + e.getMessage(), e);
        }
    }

    private boolean tableExists(Connection con, String table) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            return (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            Databases.closeSQLStuff(rs);
        }
    }

    // --------------------------------------------------------------------------------

    private static final class DB {

        final int dbId;
        final String name;
        final String url;
        final String driver;
        final String login;
        final String password;
        int hash = 0;

        DB(int dbId, String name, String url, String driver, String login, String password) {
            super();
            this.dbId = dbId;
            this.name = name;
            this.url = url;
            this.driver = driver;
            this.login = login;
            this.password = password;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                h = 31 * 1 + dbId;
                hash = h;
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DB)) {
                return false;
            }
            DB other = (DB) obj;
            if (dbId != other.dbId) {
                return false;
            }
            return true;
        }
    }

    private static final class PoolAndSchema {

        final int dbId;
        final String schema;

        PoolAndSchema(int dbId, String schema) {
            super();
            this.dbId = dbId;
            this.schema = schema;
        }
    }

    private static final class NoConnectionToDatabaseException extends Exception {

        private static final long serialVersionUID = 8820076916162330786L;

        NoConnectionToDatabaseException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private static String extractHostName(String jdbcUrl) {
        if (null == jdbcUrl) {
            return null;
        }

        String urlToParse = jdbcUrl;
        if (urlToParse.startsWith("jdbc:")) {
            urlToParse = urlToParse.substring(5);
        }

        try {
            return new URI(urlToParse).getHost();
        } catch (URISyntaxException e) {
            int start = urlToParse.indexOf("://");
            int end = urlToParse.indexOf('/', start + 1);
            return urlToParse.substring(start + 3, end);
        }
    }

}
