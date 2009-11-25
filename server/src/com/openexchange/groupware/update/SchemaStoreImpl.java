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

package com.openexchange.groupware.update;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.database.DBPoolingException;
import com.openexchange.database.internal.Server;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaExceptionFactoryOld;
import com.openexchange.groupware.update.internal.SchemaException;
import com.openexchange.groupware.update.internal.SchemaExceptionCodes;
import com.openexchange.groupware.update.internal.SchemaUpdateStateImpl;
import com.openexchange.tools.update.Tools;

/**
 * Implements loading and storing the schema version information.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.SCHEMA_STORE_IMPL, component = EnumComponent.UPDATE)
public class SchemaStoreImpl extends SchemaStore {

    private static final Log LOG = LogFactory.getLog(SchemaStoreImpl.class);

    private static final String TABLE_NAME = "updateTask";

    private static final String LOCKED = "LOCKED";

    /**
     * SQL command for selecting the version from the schema.
     */
    private static final String SELECT = "SELECT version,locked,gw_compatible,admin_compatible,server FROM version FOR UPDATE";

    /**
     * For creating exceptions.
     */
    private static final SchemaExceptionFactoryOld EXCEPTION = new SchemaExceptionFactoryOld(SchemaStoreImpl.class);

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
            long start = System.currentTimeMillis();
            retval = loadSchemaStatus(con);
            LOG.info("Load schema: " + (System.currentTimeMillis() - start));
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
        String sql = "CREATE TABLE " + TABLE_NAME + " (cid INT4 UNSIGNED NOT NULL,taskName VARCHAR(1024) NOT NULL,"
            + "PRIMARY KEY(cid,taskName(255))) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static final String SQL_SELECT_LOCKED_FOR_UPDATE = "SELECT locked FROM version FOR UPDATE";

    private static final String SQL_UPDATE_LOCKED = "UPDATE version SET locked = ?";

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.INTERNAL_ERROR, Category.PERMISSION,
            Category.INTERNAL_ERROR }, desc = { "", "", "", "" }, exceptionId = { 6, 7, 8, 9 }, msg = {
            "A SQL error occurred while reading schema version information: %1$s.",
            "Though expected, SQL query returned no result.",
            "Update conflict detected. Another process is currently updating schema %1$s.",
            "Table update failed. Schema %1$s could not be locked." })
    @Override
    public void lockSchema(final Schema schema, final int contextId) throws SchemaException {
        /*
         * Start of update process, so lock schema
         */
        boolean error = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection writeCon = null;
        try {
            writeCon = Database.get(contextId, true);
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            throw new SchemaException(e);
        }
        try {
            /*
             * Try to obtain exclusive lock on table 'version'
             */
            writeCon.setAutoCommit(false); // BEGIN
            stmt = writeCon.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                error = true;
                throw EXCEPTION.create(7);
            } else if (rs.getBoolean(1)) {
                /*
                 * Schema is already locked by another update process
                 */
                error = true;
                throw EXCEPTION.create(8, schema.getSchema());
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            /*
             * Lock schema
             */
            stmt = writeCon.prepareStatement(SQL_UPDATE_LOCKED);
            stmt.setBoolean(1, true);
            if (stmt.executeUpdate() == 0) {
                /*
                 * Schema could not be locked
                 */
                error = true;
                throw EXCEPTION.create(9, schema.getSchema());
            }
            /*
             * Everything went fine. Schema is marked as locked
             */
            writeCon.commit(); // COMMIT
        } catch (final SQLException e) {
            error = true;
            throw EXCEPTION.create(6, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            if (writeCon != null) {
                if (error) {
                    try {
                        writeCon.rollback();
                    } catch (final SQLException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                try {
                    if (!writeCon.getAutoCommit()) {
                        writeCon.setAutoCommit(true);
                    }
                } catch (final SQLException e) {
                    LOG.error(e.getMessage(), e);
                }
                Database.back(contextId, true, writeCon);
            }
        }
    }

    private static final String SQL_UPDATE_VERSION = "UPDATE version SET version = ?, locked = ?, gw_compatible = ?, admin_compatible = ?";

    @OXThrowsMultiple(category = { Category.CODE_ERROR, Category.INTERNAL_ERROR, Category.PERMISSION,
            Category.INTERNAL_ERROR }, desc = { "", "", "", "" }, exceptionId = { 10, 11, 12, 13 }, msg = {
            "A SQL error occurred while reading schema version information: %1$s.",
            "Though expected, SQL query returned no result.",
            "Update conflict detected. Schema %1$s is not marked as LOCKED.",
            "Table update failed. Schema %1$s could not be unlocked." })
    @Override
    public void unlockSchema(final Schema schema, final int contextId) throws SchemaException {
        /*
         * End of update process, so unlock schema
         */
        try {
            boolean error = false;
            Connection writeCon = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                /*
                 * Try to obtain exclusive lock on table 'version'
                 */
                writeCon = Database.get(contextId, true);
                writeCon.setAutoCommit(false); // BEGIN
                stmt = writeCon.prepareStatement(SQL_SELECT_LOCKED_FOR_UPDATE);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    error = true;
                    throw EXCEPTION.create(11);
                } else if (!rs.getBoolean(1)) {
                    /*
                     * Schema is NOT locked by update process
                     */
                    error = true;
                    throw EXCEPTION.create(12, schema.getSchema());
                }
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                /*
                 * Update & unlock schema
                 */
                stmt = writeCon.prepareStatement(SQL_UPDATE_VERSION);
                stmt.setInt(1, SchemaImpl.ACTUAL.getDBVersion());
                stmt.setBoolean(2, false);
                stmt.setBoolean(3, SchemaImpl.ACTUAL.isGroupwareCompatible());
                stmt.setBoolean(4, SchemaImpl.ACTUAL.isAdminCompatible());
                if (stmt.executeUpdate() == 0) {
                    /*
                     * Schema could not be unlocked
                     */
                    error = true;
                    throw EXCEPTION.create(13, schema.getSchema());
                }
                /*
                 * Everything went fine. Schema is marked as unlocked
                 */
                writeCon.commit(); // COMMIT
            } finally {
                closeSQLStuff(rs, stmt);
                if (writeCon != null) {
                    if (error) {
                        try {
                            writeCon.rollback();
                        } catch (final SQLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    if (!writeCon.getAutoCommit()) {
                        try {
                            writeCon.setAutoCommit(true);
                        } catch (final SQLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    Database.back(contextId, true, writeCon);
                }
            }
        } catch (final DBPoolingException e) {
            LOG.error(e.getMessage(), e);
            throw new SchemaException(e);
        } catch (final SQLException e) {
            throw EXCEPTION.create(10, e, e.getMessage());
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
                schema.setLocked(result.getBoolean(pos++));
                schema.setGroupwareCompatible(result.getBoolean(pos++));
                schema.setAdminCompatible(result.getBoolean(pos++));
                schema.setServer(result.getString(pos++));
                schema.setSchema(con.getCatalog());
            } else {
                throw SchemaExceptionCodes.MISSING_VERSION_ENTRY.create();
            }
            if (result.next()) {
                throw SchemaExceptionCodes.MULTIPLE_VERSION_ENTRY.create();
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
    private static final SchemaUpdateState loadSchemaStatus(Connection con) throws SchemaException, SQLException {
        final SchemaUpdateStateImpl retval = new SchemaUpdateStateImpl();
        long start = System.currentTimeMillis();
        loadUpdateTasks(con, retval);
        LOG.info("Load update tasks: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        if (Tools.tableExists(con, "version")) {
            loadOldVersionTable(con, retval);
        } else {
            retval.setDBVersion(0);
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
        LOG.info("Load old version table: " + (System.currentTimeMillis() - start));
        return retval;
    }

    private static void loadUpdateTasks(Connection con, SchemaUpdateStateImpl state) throws SchemaException {
        String sql = "SELECT taskName FROM updateTask WHERE cid=0 FOR UPDATE";
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.createStatement();
            result = stmt.executeQuery(sql);
            while (result.next()) {
                String taskName = result.getString(1);
                if (LOCKED.equals(taskName)) {
                    state.setLocked(true);
                } else {
                    state.addExecutedTask(taskName);
                }
            }
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }
}
