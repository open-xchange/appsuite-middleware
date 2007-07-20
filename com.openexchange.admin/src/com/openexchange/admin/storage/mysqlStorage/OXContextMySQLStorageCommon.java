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
package com.openexchange.admin.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.groupware.update.UpdateTaskCollection;

public abstract class OXContextMySQLStorageCommon {

    public static final String LOG_ERROR_CLOSING_STATEMENT = "Error closing statement";

    private static final Log log = LogFactory.getLog(OXContextMySQLStorageCommon.class);

    private final OXUtilMySQLStorageCommon oxutilcommon;

    private static AdminCache cache = null;

    private static PropertyHandler prop = null;

    static {
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
    }

    public OXContextMySQLStorageCommon() {
        oxutilcommon = new OXUtilMySQLStorageCommon();
    }
    
    
    

    public Context getData(final Context ctx, final Connection configdb_con, final long average_size) throws SQLException, PoolException  {
        Connection oxdb_read = null;
        PreparedStatement prep = null;
        final int context_id = ctx.getIdAsInt();

        try {
            oxdb_read = cache.getREADConnectionForContext(context_id);

            Boolean enabled = Boolean.TRUE;

            prep = configdb_con.prepareStatement("SELECT context.cid,context.name,context.enabled,context.reason_id,context.filestore_id,context.filestore_name,context.filestore_login,context.filestore_passwd,context.quota_max,context_server2db_pool.server_id,context_server2db_pool.write_db_pool_id,context_server2db_pool.read_db_pool_id,context_server2db_pool.db_schema FROM context LEFT JOIN context_server2db_pool ON context.cid = context_server2db_pool.cid WHERE context.cid =? AND context_server2db_pool.server_id = (select server_id from server where name = ?)");
            prep.setInt(1, context_id);
            prep.setString(2, prop.getProp(AdminProperties.Prop.SERVER_NAME, "local"));
            ResultSet rs = prep.executeQuery();

            final Context cs = new Context();
            int reason_id = -1;
            int filestore_id = -1;
            long quota_max = -1;
            long quota_used = 0;

            String name = null;
            String filestore_name = null;
            // DATBASE HANDLE
            while (rs.next()) {
                // filestore_id | filestore_name | filestore_login |
                // filestore_passwd | quota_max
                int read_pool = -1;
                int write_pool = -1;

                Database readdb = null;
                Database writedb = null;

                name = rs.getString("name");
                enabled = rs.getBoolean("enabled");
                reason_id = rs.getInt("reason_id");
                filestore_id = rs.getInt("filestore_id");
                filestore_name = rs.getString("filestore_name");
                quota_max = rs.getLong("quota_max");
                if (quota_max != 0 && quota_max != -1) {
                    quota_max /= Math.pow(2, 20);
                }
                read_pool = rs.getInt("read_db_pool_id");
                write_pool = rs.getInt("write_db_pool_id");
                final String db_schema = rs.getString("db_schema");
                if (null != db_schema) {
                    if (-1 != read_pool) {
                        readdb = new Database(read_pool, db_schema);
                    }
                    if (-1 != write_pool) {
                        writedb = new Database(write_pool, db_schema);
                    }
                }

                cs.setReadDatabase(readdb);
                cs.setWriteDatabase(writedb);
            }

            // CONTEXT STATE INFOS #
            if (-1 != reason_id) {
                cs.setMaintenanceReason(new MaintenanceReason(reason_id));
            }
            cs.setEnabled(enabled);
            // ######################

            // GENERAL CONTEXT INFOS AND QUOTA

            rs.close();
            prep.close();

            prep = oxdb_read.prepareStatement("SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ?");
            prep.setInt(1, context_id);
            rs = prep.executeQuery();

            while (rs.next()) {
                quota_used = rs.getLong(1);
            }
            rs.close();
            prep.close();
            if (quota_used != 0 && quota_used != -1) {
                quota_used /= Math.pow(2, 20);
            }
            if (quota_used != -1) {
                // set used quota in context setup
                cs.setUsedQuota(quota_used);
            }

            // maximum quota of this context
            if (quota_max != -1) {
                // set quota max also in context setup object
                cs.setMaxQuota(quota_max);
            }
            cs.setFilestoreId(filestore_id);
            cs.setFilestore_name(filestore_name);

            cs.setAverage_size(average_size);

            // name of the context, currently same with contextid
            if (name != null) {
                cs.setName(name);
            }
            
            // add context login mappings
            prep = configdb_con.prepareStatement("SELECT login_info FROM login2context WHERE cid = ?");
            prep.setInt(1, context_id);
            rs = prep.executeQuery();
            while (rs.next()) {
                cs.addLoginMapping(rs.getString(1));                
            }
            rs.close();
            prep.close();
            
            
            // context id
            cs.setID(context_id);
            return cs;
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                log.error("SQL Error closing statement!", e);
            }
            try {
                if (oxdb_read != null) {
                    cache.pushOXDBRead(context_id, oxdb_read);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox read connection to pool!",exp);
            }
        }

    }

    public final void createStandardGroupForContext(final int context_id, final Connection ox_write_con, final String display_name, final int group_id, final int gid_number) throws SQLException {
        // TODO: this must be defined somewhere else
        final int NOGROUP = 65534;
        PreparedStatement group_stmt = ox_write_con.prepareStatement("INSERT INTO groups (cid, id, identifier, displayname,lastModified,gidNumber) VALUES (?,?,'users',?,?,?);");
        group_stmt.setInt(1, context_id);
        group_stmt.setInt(2, group_id);
        group_stmt.setString(3, display_name);
        group_stmt.setLong(4, System.currentTimeMillis());
        if (Integer.parseInt(prop.getGroupProp(AdminProperties.Group.GID_NUMBER_START, "-1")) > 0) {
            group_stmt.setInt(5, gid_number);
        } else {
            group_stmt.setInt(5, NOGROUP);
        }
        group_stmt.executeUpdate();
        group_stmt.close();

    }

    public final void deleteContextFromConfigDB(final Connection configdb_write_con, final int context_id) throws SQLException {
        // find out what db_schema context belongs to
        PreparedStatement stmt3 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt = null;
        try {
            boolean cs2db_broken = false;
            stmt2 = configdb_write_con.prepareStatement("SELECT db_schema,write_db_pool_id FROM context_server2db_pool WHERE cid = ?");
            stmt2.setInt(1, context_id);
            stmt2.executeQuery();
            ResultSet rs = stmt2.getResultSet();
            String db_schema = null;
            int pool_id = -1;
            if (!rs.next()) {
                // throw new OXContextException("Unable to determine db_schema
                // of context " + context_id);
                cs2db_broken = true;
                log.error("Unable to determine db_schema of context " + context_id);
            } else {
                db_schema = rs.getString("db_schema");
                pool_id = ((Integer) rs.getInt("write_db_pool_id")).intValue();
            }
            stmt2.close();
            // System.out.println("############# db_schema = " + db_schema);
            if (log.isDebugEnabled()) {
                log.debug("Deleting context_server2dbpool mapping for context " + context_id);
            }
            // delete context from context_server2db_pool
            stmt2 = configdb_write_con.prepareStatement("DELETE FROM context_server2db_pool WHERE cid = ?");
            stmt2.setInt(1, context_id);
            stmt2.executeUpdate();
            stmt2.close();
            // configdb_write_con.commit(); // temp disabled by c utmasta
    
            if (!cs2db_broken) {
                try {
                    // check if any other context uses the same db_schema
                    // if not, delete it
                    stmt2 = configdb_write_con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE db_schema = ?");
                    stmt2.setString(1, db_schema);
                    stmt2.executeQuery();
                    rs = stmt2.getResultSet();
    
                    if (!rs.next()) {
                        // get auth data from db_pool to delete schema
                        stmt3 = configdb_write_con.prepareStatement("SELECT url,driver,login,password FROM db_pool WHERE db_pool_id = ?");
                        stmt3.setInt(1, pool_id);
                        stmt3.executeQuery();
                        final ResultSet rs3 = stmt3.getResultSet();
    
                        if (!rs3.next()) {
                            throw new StorageException("Unable to determine authentication data of pool_id " + pool_id);
                        }
                        final Database db = new Database(rs3.getString("login"), rs3.getString("password"), rs3.getString("driver"), rs3.getString("url"), db_schema);
                        if (log.isDebugEnabled()) {
                            log.debug("Deleting database " + db_schema);
                        }
                        oxutilcommon.deleteDatabase(db);
    
                        stmt3.close();
                        // tell pool, that database has been removed
                        com.openexchange.database.Database.reset(context_id);
                    }
                    stmt2.close();
                } catch (final Exception e) {
                    log.error("Problem deleting database while doing rollback, cid=" + context_id + ": ", e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Deleting login2context entries for context " + context_id);
            }
            stmt = configdb_write_con.prepareStatement("DELETE FROM login2context WHERE cid = ?");
            stmt.setInt(1, context_id);
            stmt.executeUpdate();
            stmt.close();
            if (log.isDebugEnabled()) {
                log.debug("Deleting context entry for context " + context_id);
            }
            stmt = configdb_write_con.prepareStatement("DELETE FROM context WHERE cid = ?");
            stmt.setInt(1, context_id);
            stmt.executeUpdate();
            stmt.close();
    
        } finally {
            try {
                if (null != stmt3) {
                    stmt3.close();
                }
            } catch (final SQLException ecp) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ecp);
            }
            try {
                if (null != stmt2) {
                    stmt2.close();
                }
            } catch (final SQLException ecp) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ecp);
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (final SQLException ecp) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ecp);
            }
        }
    }

    public final void deleteSequenceTables(final int context_id, final Connection write_ox_con) throws SQLException {
        log.debug("Deleting sequence entries for context " + context_id);
        // delete all sequence table entries
        PreparedStatement del_stmt = null;
        PreparedStatement seq_del = null;
        try {
            del_stmt = write_ox_con.prepareStatement("show tables like ?");
            del_stmt.setString(1, "%sequence_%");
            final ResultSet rs_sequences = del_stmt.executeQuery();
    
            while (rs_sequences.next()) {
                final String del_sequence_table = rs_sequences.getString(1);
                seq_del = write_ox_con.prepareStatement("delete from " + del_sequence_table + " where cid = ?");
                seq_del.setInt(1, context_id);
                seq_del.executeUpdate();
                seq_del.close();
            }
        } finally {
            try {
                if (del_stmt != null) {
                    del_stmt.close();
                }
                if (seq_del != null) {
                    seq_del.close();
                }
            } catch (final SQLException ecp) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, ecp);
            }
        }
    }

    public void fillContextAndServer2DBPool(final Context ctx, final Connection con, final Database db) throws SQLException, StorageException {
        // dbid is the id in db_pool of database engine to use for next context
    
        // if read id -1 (not set by client ) or 0 (there is no read db for this
        // cluster) then read id must be same as write id
        // else the db pool cannot resolve the database
        if (null == db.getRead_id() || 0 == db.getRead_id()) {
            db.setRead_id(db.getId());
        }
    
        // create context entry in configdb
        // quota is in MB, but we store in Byte
        long quota_max_temp = ctx.getMaxQuota();
        if (quota_max_temp != -1) {
            quota_max_temp *= Math.pow(2, 20);
            ctx.setMaxQuota(quota_max_temp);
        }
        fillContextTable(ctx, con);
    
        // insert in the context_server2dbpool table
        fillContextServer2DBPool(ctx, db, con);
    }

    public final void handleCreateContextRollback(final Connection configdb_write_con, final Connection ox_write_con, final int context_id) {
    
        try {
            if (configdb_write_con != null && !configdb_write_con.getAutoCommit()) {
                configdb_write_con.rollback();
            }
        } catch (final SQLException expd) {
            log.error("Error processing rollback of configdb connection!", expd);
        }
        try {
            // remove all entries from configdb cause rollback might not be
            // enough
            // cause of contextserver2dbpool entries
            if (configdb_write_con != null) {
                deleteContextFromConfigDB(configdb_write_con, context_id);
            }
        } catch (final SQLException ecp) {
            log.error("SQL Error removing/rollback entries from configdb for context " + context_id, ecp);
        }
        try {
            if (ox_write_con != null && !ox_write_con.getAutoCommit()) {
                ox_write_con.rollback();
            }
        } catch (final SQLException ex) {
            log.error("SQL Error processing rollback of ox connection!", ex);
    
        }
        try {
            // delete sequences
            if (ox_write_con != null) {
                deleteSequenceTables(context_id, ox_write_con);
            }
        } catch (final SQLException ep) {
            log.error("SQL Error deleting sequence tables on rollback create context", ep);
        }
    }

    public final void handleContextDeleteRollback(final Connection write_ox_con, final Connection con_write) {
        try {
            if (con_write != null && !con_write.getAutoCommit()) {
                con_write.rollback();
                log.debug("Rollback of configdb write connection ok");
            }
        } catch (final SQLException rexp) {
            log.error("SQL Error", rexp);
        }
        try {
            if (write_ox_con != null && !write_ox_con.getAutoCommit()) {
                write_ox_con.rollback();
                log.debug("Rollback of ox db write connection ok");
            }
        } catch (final SQLException rexp) {
            log.error("Error processing rollback of ox write connection!", rexp);
        }
    }

    public final void initSequenceTables(final int context_id, final Connection con) throws SQLException, StorageException {
        PreparedStatement ps = null;
        try {
            final ArrayList<String> sequence_tables = cache.getSequenceTables();
            final Iterator<String> is = sequence_tables.iterator();
            while (is.hasNext()) {
                int startval = 0;
                final String table = is.next();
                if (table.equals("sequence_folder")) {
                    // below id 20 is reserved
                    startval = 20;
                }
                
                // check for the uid number feature
                if(table.equals("sequence_uid_number")){
                        int startnum = Integer.parseInt(prop.getUserProp(AdminProperties.User.UID_NUMBER_START,"-1"));
                        if(startnum>0){
                            // we use the uid number faeture
                            // set the start number in the sequence for uid_numbers 
                            startval = startnum;
                        }
                }
                //  check for the gid number feature
                if(table.equals("sequence_gid_number")){
                    int startnum = Integer.parseInt(prop.getGroupProp(AdminProperties.Group.GID_NUMBER_START,"-1"));
                    if(startnum>0){
                        // we use the gid number faeture
                        // set the start number in the sequence for gid_numbers 
                        startval = startnum;
                    }
                }
                
                ps = con.prepareStatement("INSERT INTO " + table + " VALUES(?,?);");
                ps.setInt(1, context_id);
                ps.setInt(2, startval);
                ps.executeUpdate();
                ps.close();
            }
        } catch (final OXGenericException oxgen) {
            log.error("Generic Error",oxgen);
            throw new StorageException("" + oxgen.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
    }

    public final void initVersionTable(final int context_id, final Connection con) throws SQLException, StorageException {
        PreparedStatement ps = null;
    
        try {
            ps = con.prepareStatement("INSERT INTO version (version,locked,gw_compatible,admin_compatible,server) VALUES(?,?,?,?,?);");
            ps.setInt(1, UpdateTaskCollection.getHighestVersion());
            ps.setInt(2, 0);
            ps.setInt(3, 1);
            ps.setInt(4, 1);
            ps.setString(5, prop.getProp(AdminProperties.Prop.SERVER_NAME, "local"));
            ps.executeUpdate();
            ps.close();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException e) {
                log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
    }

    // This method could be private but due to the fact, that it is abstract, we
    // have to set it to protected
    protected abstract int getFileStoreID(final Connection configdb_read) throws SQLException, StorageException;

    private final int getMyServerID(final Connection configdb_write_con) throws SQLException, StorageException {
        PreparedStatement sstmt = null;
        int sid = 0;
        try {

            final String servername = prop.getProp(AdminProperties.Prop.SERVER_NAME, "local");
            sstmt = configdb_write_con.prepareStatement("SELECT server_id FROM server WHERE name = ?");
            sstmt.setString(1, servername);
            final ResultSet rs2 = sstmt.executeQuery();
            if (!rs2.next()) {
                throw new StorageException("No server registered with name=" + servername);
            }
            sid = Integer.parseInt(rs2.getString("server_id"));
            rs2.close();
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            throw sql;
        } finally {
            try {
                sstmt.close();
            } catch (final SQLException e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT, e);
            }
        }
        return sid;
    }

    private final void fillContextServer2DBPool(final Context ctx, final Database db, final Connection configdb_write_con) throws SQLException, StorageException {

        PreparedStatement stmt = null;
        try {
            if (null != db.getScheme() && null != db.getRead_id() && null != db.getId()) {
                int read_id = -1;
                int write_id = -1;
                String db_schema = "openexchange";
                read_id = db.getRead_id();
                write_id = db.getId();
                db_schema = db.getScheme();

                // ok database pools exist in configdb
                final int server_id = getMyServerID(configdb_write_con);
                stmt = configdb_write_con.prepareStatement("INSERT INTO context_server2db_pool (server_id,cid,read_db_pool_id,write_db_pool_id,db_schema)" + " VALUES " + " (?,?,?,?,?)");
                stmt.setInt(1, server_id);
                stmt.setInt(2, ctx.getIdAsInt());
                stmt.setInt(3, read_id);
                stmt.setInt(4, write_id);
                stmt.setString(5, db_schema);
                stmt.executeUpdate();
                stmt.close();
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final Exception exp) {
                    log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, exp);
                }
            }
        }
    }

    private final void fillContextTable(final Context ctx, final Connection configdb_write_con) throws SQLException, StorageException {
        PreparedStatement stmt = null;
        try {

            final int store_id = getFileStoreID(configdb_write_con);
            // check if all filespool infos exist and then insert into context
            // table and login2context

            stmt = configdb_write_con.prepareStatement("INSERT INTO context (cid,name,enabled,filestore_id,filestore_name,quota_max) VALUES (?,?,?,?,?,?)");
            stmt.setInt(1, ctx.getIdAsInt());
            if (ctx.getName() != null && ctx.getName().trim().length() > 0) {
                stmt.setString(2, ctx.getName());
            } else {
                stmt.setString(2, ctx.getIdAsString());
            }
            stmt.setBoolean(3, true);
            stmt.setInt(4, store_id);
            stmt.setString(5, ctx.getIdAsString() + "_ctx_store");
            stmt.setLong(6, ctx.getMaxQuota());
            stmt.executeUpdate();
            stmt.close();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
                }

            }
        }
    }

}
