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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.database.migration.internal.change.custom;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
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

        Connection configDbCon = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        boolean rollback = false;
        try {
            Databases.startTransaction(configDbCon);
            rollback = true;

            execute(configDbCon);

            configDbCon.commit();
            rollback = false;
        } catch (SQLException e) {
            throw new CustomChangeException("SQL error", e);
        } finally {
            if (rollback) {
                Databases.rollback(configDbCon);
            }
            Databases.autocommit(configDbCon);
        }
    }

    private void execute(Connection configDbCon) throws CustomChangeException {
        try {
            createFilestoreCountTable(configDbCon);
            createDBPoolCountTable(configDbCon);
            createDBPoolSchemaCountTable(configDbCon);
            createSemaphoreTable(configDbCon);

            checkFilestoreCountConsistency(configDbCon);
            checkDBPoolCountConsistency(configDbCon);
            checkDBPoolSchemaCountConsistency(configDbCon);
        } catch (SQLException e) {
            throw new CustomChangeException("SQL error", e);
        }
    }

    private void createFilestoreCountTable(Connection configDbCon) throws SQLException {
        if (tableExists(configDbCon, "contexts_per_filestore")) {
            return;
        }

        String createStmt = "CREATE TABLE contexts_per_filestore (" +
            "filestore_id INT4 UNSIGNED NOT NULL, " +
            "count INT4 UNSIGNED NOT NULL, " +
            "PRIMARY KEY (filestore_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

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
            String createStmt = "CREATE TABLE contexts_per_dbpool (" +
                "db_pool_id INT4 UNSIGNED NOT NULL, " +
                "count INT4 UNSIGNED NOT NULL, " +
                "PRIMARY KEY (db_pool_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            PreparedStatement stmt = null;
            try {
                stmt = configDbCon.prepareStatement(createStmt);
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }

        if (false == tableExists(configDbCon, "dbpool_lock")) {
            String createStmt = "CREATE TABLE dbpool_lock (" +
                "db_pool_id INT4 UNSIGNED NOT NULL, " +
                "PRIMARY KEY (db_pool_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

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
            String createStmt = "CREATE TABLE contexts_per_dbschema (" +
                "db_pool_id INT4 UNSIGNED NOT NULL, " +
                "schemaname VARCHAR(32) NOT NULL, " +
                "count INT4 UNSIGNED NOT NULL, " +
                "creating_date BIGINT(64) NOT NULL," +
                "PRIMARY KEY (db_pool_id, schemaname)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

            PreparedStatement stmt = null;
            try {
                stmt = configDbCon.prepareStatement(createStmt);
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }

        if (false == tableExists(configDbCon, "dbschema_lock")) {
            String createStmt = "CREATE TABLE dbschema_lock (" +
                "db_pool_id INT4 UNSIGNED NOT NULL, " +
                "schemaname VARCHAR(32) NOT NULL, " +
                "PRIMARY KEY (db_pool_id, schemaname)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

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

        String createStmt = "CREATE TABLE `ctx_per_schema_sem` (" +
            "id BIGINT UNSIGNED NOT NULL, " +
            "PRIMARY KEY (id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

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

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_dbpool WHERE db_pool_id=?");
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

    private void checkDBPoolSchemaCountConsistency(Connection configCon) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Drop non-existing ones held in count table
            stmt = configCon.prepareStatement("SELECT contexts_per_dbschema.db_pool_id, contexts_per_dbschema.schemaname FROM contexts_per_dbschema LEFT JOIN context_server2db_pool ON contexts_per_dbschema.db_pool_id=context_server2db_pool.write_db_pool_id AND contexts_per_dbschema.schemaname COLLATE utf8_unicode_ci = context_server2db_pool.db_schema COLLATE utf8_unicode_ci WHERE context_server2db_pool.write_db_pool_id IS NULL");
            rs = stmt.executeQuery();
            if (rs.next()) {
                class DbAndSchema {
                    final int dbId;
                    final String schema;

                    DbAndSchema(int dbId, String schema) {
                        super();
                        this.dbId = dbId;
                        this.schema = schema;
                    }
                }

                List<DbAndSchema> ids = new LinkedList<DbAndSchema>();
                do {
                    ids.add(new DbAndSchema(rs.getInt(1), rs.getString(2)));
                } while (rs.next());
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                stmt = configCon.prepareStatement("DELETE FROM contexts_per_dbschema WHERE db_pool_id=? AND schemaname=?");
                for (DbAndSchema das : ids) {
                    stmt.setInt(1, das.dbId);
                    stmt.setString(2, das.schema);
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
            stmt = configCon.prepareStatement("SELECT write_db_pool_id FROM db_cluster ORDER BY write_db_pool_id ASC");
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

            stmt = configCon.prepareStatement("INSERT INTO contexts_per_dbschema (db_pool_id, schemaname, count, creating_date) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE count=?");
            long now = System.currentTimeMillis();
            for (Map.Entry<Integer, List<SchemaCount>> entry : counts.entrySet()) {
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
            for (Map.Entry<Integer, List<SchemaCount>> entry : counts.entrySet()) {
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
        } finally {
            Databases.closeSQLStuff(rs, stmt);
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

    private Connection getConfigDbCon(DatabaseService databaseService) throws CustomChangeException {
        try {
            return databaseService.getForUpdateTask();
        } catch (OXException e) {
            throw new CustomChangeException("Pooling error", e);
        }
    }

}
