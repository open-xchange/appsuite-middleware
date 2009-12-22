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

package com.openexchange.groupware.update.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.internal.Server;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.SchemaException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.UpdateTaskCollection;
import com.openexchange.tools.update.Tools;

/**
 * Implements loading and storing the schema version information.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaStoreImpl extends SchemaStore {

    private static final String TABLE_NAME = "updateTask";

    private static final String LOCKED = "LOCKED";

    public SchemaStoreImpl() {
        super();
    }

    @Override
    public SchemaUpdateState getSchema(int poolId, String schemaName) throws SchemaException {
        Connection con;
        try {
            con = Database.get(poolId, schemaName);
        } catch (DBPoolingException e) {
            throw SchemaExceptionCodes.DATABASE_DOWN.create(e);
        }
        final SchemaUpdateState retval;
        try {
            con.setAutoCommit(false);
            checkForTable(con);
            retval = loadSchemaStatus(con);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (SchemaException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            Database.back(poolId, con);
        }
        return retval;
    }

    /**
     * @param con connection to master in transaction mode.
     * @return <code>true</code> if the table has been created.
     */
    private static void checkForTable(Connection con) throws SQLException {
        if (!Tools.tableExists(con, TABLE_NAME)) {
            createTable(con);
        }
    }

    private static void createTable(Connection con) throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(CreateUpdateTaskTable.CREATES[0]);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void lockSchema(Schema schema, int contextId) throws SchemaException {
        final Connection con;
        try {
            con = Database.get(contextId, true);
        } catch (DBPoolingException e) {
            throw new SchemaException(e);
        }
        try {
            con.setAutoCommit(false); // BEGIN
            // Insert LOCKED
            insertLock(con, schema);
            // Setting old version table to locked
            if (Tools.tableExists(con, "version")) {
                lockOldVersionTable(con, schema);
            }
            // Everything went fine. Schema is marked as locked
            con.commit();
        } catch (SchemaException e) {
            rollback(con);
            throw e;
        } catch (SQLException e) {
            rollback(con);
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private static final int MYSQL_DEADLOCK = 1213;

    private static final int MYSQL_DUPLICATE = 1062;

    private static void insertLock(Connection con, Schema schema) throws SchemaException {
        // Check for existing lock exclusively
        String[] tasks = readUpdateTasks(con);
        for (String task : tasks) {
            if (LOCKED.equals(task)) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(schema.getSchema());
            }
        }
        // Insert lock
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO updateTask (cid,taskName,successful,lastModified) VALUES (0,?,true,?)");
            stmt.setString(1, LOCKED);
            stmt.setLong(2, System.currentTimeMillis());
            if (stmt.executeUpdate() == 0) {
                throw SchemaExceptionCodes.LOCK_FAILED.create(schema.getSchema());
            }
        } catch (SQLException e) {
            if (MYSQL_DEADLOCK == e.getErrorCode() || MYSQL_DUPLICATE == e.getErrorCode()) {
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(e, schema.getSchema());
            }
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static void lockOldVersionTable(Connection con, Schema schema) throws SchemaException {
        // Check for existing lock
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            // Try to obtain exclusive lock on table 'version'
            stmt = con.prepareStatement("SELECT locked FROM version FOR UPDATE");
            result = stmt.executeQuery();
            if (!result.next()) {
                throw SchemaExceptionCodes.MISSING_VERSION_ENTRY.create(schema.getSchema());
            } else if (result.getBoolean(1)) {
                // Schema is already locked by another update process
                throw SchemaExceptionCodes.ALREADY_LOCKED.create(schema.getSchema());
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        // Lock schema
        try {
            stmt = con.prepareStatement("UPDATE version SET locked=?");
            stmt.setBoolean(1, true);
            if (stmt.executeUpdate() == 0) {
                // Schema could not be locked
                throw SchemaExceptionCodes.LOCK_FAILED.create(schema.getSchema());
            }
            // Everything went fine. Schema is marked as locked
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    @Override
    public void unlockSchema(final Schema schema, final int contextId) throws SchemaException {
        final Connection con;
        try {
            con = Database.get(contextId, true);
        } catch (DBPoolingException e) {
            throw new SchemaException(e);
        }
        try {
            // End of update process, so unlock schema
            con.setAutoCommit(false);
            // Delete LOCKED
            deleteLock(con, schema);
            // Unlock old version table
            if (Tools.tableExists(con, "version")) {
                unlockOldVersionTable(con, schema);
            }
            // Everything went fine. Schema is marked as unlocked
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (SchemaException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    private static void deleteLock(Connection con, Schema schema) throws SchemaException {
        // Check for existing lock exclusively
        String[] tasks = readUpdateTasks(con);
        boolean found = false;
        for (String task : tasks) {
            if (LOCKED.equals(task)) {
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
            stmt.setString(1, LOCKED);
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

    private static void unlockOldVersionTable(Connection con, Schema schema) throws SchemaException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            // Try to obtain exclusive lock on table 'version'
            stmt = con.prepareStatement("SELECT locked FROM version FOR UPDATE");
            result = stmt.executeQuery();
            if (!result.next()) {
                throw SchemaExceptionCodes.MISSING_VERSION_ENTRY.create(schema.getSchema());
            } else if (!result.getBoolean(1)) {
                // Schema is NOT locked by update process
                throw SchemaExceptionCodes.UPDATE_CONFLICT.create(schema.getSchema());
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        try {
            // Update & unlock schema
            stmt = con.prepareStatement("UPDATE version SET version=?,locked=?");
            stmt.setInt(1, UpdateTaskCollection.getInstance().getHighestVersion());
            stmt.setBoolean(2, false);
            if (stmt.executeUpdate() == 0) {
                // Schema could not be unlocked
                throw SchemaExceptionCodes.UNLOCK_FAILED.create(schema.getSchema());
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * Loads the old schema version information from the database.
     * @param con connection to the master in transaction state.
     * @param schema schema object to put the information to.
     * @throws SchemaException if loading fails.
     */
    private static void loadOldVersionTable(Connection con, SchemaImpl schema) throws SchemaException {
        String sql = "SELECT version,locked,gw_compatible,admin_compatible,server FROM version FOR UPDATE";
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.createStatement();
            result = stmt.executeQuery(sql);
            if (result.next()) {
                int pos = 1;
                schema.setDBVersion(result.getInt(pos++));
                // Use locked information from updateTask before using locked information from version table
                if (!schema.isLocked()) {
                    schema.setLocked(result.getBoolean(pos++));
                }
                schema.setGroupwareCompatible(result.getBoolean(pos++));
                schema.setAdminCompatible(result.getBoolean(pos++));
                schema.setServer(result.getString(pos++));
                schema.setSchema(con.getCatalog());
            } else {
                throw SchemaExceptionCodes.MISSING_VERSION_ENTRY.create(schema.getSchema());
            }
            if (result.next()) {
                throw SchemaExceptionCodes.MULTIPLE_VERSION_ENTRY.create(schema.getSchema());
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    /**
     * @param con connection to the master in transaction mode.
     */
    private static SchemaUpdateState loadSchemaStatus(Connection con) throws SchemaException, SQLException {
        final SchemaUpdateStateImpl retval = new SchemaUpdateStateImpl();
        loadUpdateTasks(con, retval);
        if (Tools.tableExists(con, "version")) {
            loadOldVersionTable(con, retval);
        } else {
            retval.setDBVersion(UpdateTaskCollection.getInstance().getHighestVersion());
            retval.setLocked(false);
            retval.setGroupwareCompatible(true);
            retval.setAdminCompatible(true);
            try {
                retval.setServer(Server.getServerName());
            } catch (DBPoolingException e) {
                throw new SchemaException(e);
            }
            retval.setSchema(con.getCatalog());
        }
        return retval;
    }

    private static void loadUpdateTasks(Connection con, SchemaUpdateStateImpl state) throws SchemaException {
        for (String taskName : readUpdateTasks(con)) {
            if (LOCKED.equals(taskName)) {
                state.setLocked(true);
            } else {
                state.addExecutedTask(taskName);
            }
        }
    }

    private static String[] readUpdateTasks(Connection con) throws SchemaException {
        String sql = "SELECT taskName FROM updateTask WHERE cid=0 FOR UPDATE";
        Statement stmt = null;
        ResultSet result = null;
        List<String> retval = new ArrayList<String>();
        try {
            stmt = con.createStatement();
            result = stmt.executeQuery(sql);
            while (result.next()) {
                retval.add(result.getString(1));
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval.toArray(new String[retval.size()]);
    }

    @Override
    public void addExecutedTask(int contextId, String taskName, boolean success) throws SchemaException {
        final Connection con;
        try {
            con = Database.get(contextId, true);
        } catch (DBPoolingException e) {
            throw new SchemaException(e);
        }
        try {
            con.setAutoCommit(false);
            addExecutedTask(con, taskName, success);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (SchemaException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void addExecutedTask(Connection con, String taskName, boolean success) throws SchemaException {
        boolean update = false;
        for (String executed : readUpdateTasks(con)) {
            if (taskName.equals(executed)) {
                update = true;
                break;
            }
        }
        String insertSQL = "INSERT INTO updateTask (cid,successful,lastModified,taskName) VALUES (0,?,?,?)";
        String updateSQL = "UPDATE updateTask SET successful=?, lastModified=? WHERE cid=0 AND taskName=?";
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(update ? updateSQL : insertSQL);
            int pos = 1;
            stmt.setBoolean(pos++, success);
            stmt.setLong(pos++, System.currentTimeMillis());
            stmt.setString(pos++, taskName);
            int rows = stmt.executeUpdate();
            if (1 != rows) {
                throw SchemaExceptionCodes.WRONG_ROW_COUNT.create(I(1), I(rows));
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
