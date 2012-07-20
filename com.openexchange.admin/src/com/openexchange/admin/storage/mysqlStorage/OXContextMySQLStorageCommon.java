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

import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.database.Assignment;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.pipesnfilters.DataSource;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersService;

public class OXContextMySQLStorageCommon {

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
    
    // TODO: The average size parameter can be removed if we have an new property handler which can
    // deal right with plugin properties
    public Context getData(final Context ctx, final Connection configdb_con, final long average_size) throws SQLException, PoolException, StorageException  {
        Connection oxdb_read = null;
        PreparedStatement prep = null;
        final int context_id = ctx.getId();

        try {
            oxdb_read = cache.getConnectionForContext(context_id);

            prep = configdb_con.prepareStatement("SELECT context.name, context.enabled, context.reason_id, context.filestore_id, context.filestore_name, context.quota_max, context_server2db_pool.write_db_pool_id, context_server2db_pool.read_db_pool_id, context_server2db_pool.db_schema, login2context.login_info FROM context LEFT JOIN ( login2context, context_server2db_pool, server ) ON ( context.cid = context_server2db_pool.cid AND context_server2db_pool.server_id = server.server_id AND context.cid = login2context.cid ) WHERE context.cid = ? AND server.name = ?");
            prep.setInt(1, context_id);
            prep.setString(2, prop.getProp(AdminProperties.Prop.SERVER_NAME, "local"));
            ResultSet rs = prep.executeQuery();

            final Context cs = new Context();

            // DATABASE HANDLE
            if (rs.next()) {
                // filestore_id | filestore_name | filestore_login |
                // filestore_passwd | quota_max
                final String name = rs.getString(1); // name
                // name of the context, currently same with contextid
                if (name != null) {
                    cs.setName(name);
                }

                cs.setEnabled(rs.getBoolean(2)); // enabled
                final int reason_id = rs.getInt(3); //reason
                // CONTEXT STATE INFOS #
                if (-1 != reason_id) {
                    cs.setMaintenanceReason(new MaintenanceReason(reason_id));
                }
                cs.setFilestoreId(rs.getInt(4)); // filestore_id
                cs.setFilestore_name(rs.getString(5)); //filestorename
                long quota_max = rs.getLong(6); //quota max
                if (quota_max != -1) {
                    quota_max /= Math.pow(2, 20);
                    // set quota max also in context setup object
                    cs.setMaxQuota(quota_max);
                }
                final int write_pool = rs.getInt(7); // write_pool_id
                final int read_pool = rs.getInt(8); //read_pool_id
                final String db_schema = rs.getString(9); // db_schema
                if (null != db_schema) {
                    cs.setReadDatabase(new Database(read_pool, db_schema));
                    cs.setWriteDatabase(new Database(write_pool, db_schema));
                }
                //DO NOT RETURN THE CONTEXT ID AS A MAPPING!!
                // THIS CAN CAUSE ERRORS IF CHANGING LOGINMAPPINGS AFTERWARDS!
                // SEE #11094 FOR DETAILS!
                final String login_mapping = rs.getString(10);   
                if(!ctx.getIdAsString().equals(login_mapping)){
                    cs.addLoginMapping(login_mapping);
                }
            }
            // All other lines contain the same content except the mapping so we concentrate on the mapping here
            while (rs.next()) {
                final String login_mapping = rs.getString(10);       
                // DO NOT RETURN THE CONTEXT ID AS A MAPPING!!
                // THIS CAN CAUSE ERRORS IF CHANGING LOGINMAPPINGS AFTERWARDS!
                // SEE #11094 FOR DETAILS!
                if(!ctx.getIdAsString().equals(login_mapping)){
                    cs.addLoginMapping(login_mapping);
                }                
            }

            // ######################

            rs.close();
            prep.close();

            prep = oxdb_read.prepareStatement("SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ?");
            prep.setInt(1, context_id);
            rs = prep.executeQuery();

            long quota_used = 0;
            while (rs.next()) {
                quota_used = rs.getLong(1);
            }
            rs.close();
            prep.close();
            quota_used /= Math.pow(2, 20);
            // set used quota in context setup
            cs.setUsedQuota(quota_used);

            cs.setAverage_size(average_size);

            // context id
            cs.setId(context_id);
            loadDynamicAttributes(oxdb_read, cs);
            return cs;
        } finally {
            closePreparedStatement(prep);
            try {
                if (oxdb_read != null) {
                    cache.pushConnectionForContext(context_id, oxdb_read);
                }
            } catch (final PoolException exp) {
                log.error("Pool Error pushing ox read connection to pool!",exp);
            }
        }
    }
    
    /**
     * Parses a dynamic attribute from the user_attribute table
     * Returns a String[] with retval[0] being the namespace and retval[1] being the name
     * @throws StorageException 
     */
    public static String[] parseDynamicAttribute(final String name) throws StorageException {
        final int pos = name.indexOf('/');
        if(pos == -1) {
            throw new StorageException("Could not parse dynamic attribute name: "+name);
        }
        final String[] parsed = new String[2];
        parsed[0] = name.substring(0, pos);
        parsed[1] = name.substring(pos+1);
        return parsed;
    }

    public static boolean isDynamicAttribute(final String name) {
        return name.indexOf('/') >= 0;
    }

    
    private void loadDynamicAttributes(final Connection oxCon, final Context ctx) throws SQLException, PoolException, StorageException {
        ResultSet rs = null;
        PreparedStatement stmtuserattributes = null;
        final int contextId = ctx.getId();
        try {
            stmtuserattributes = oxCon.prepareStatement("SELECT name, value FROM contextAttribute WHERE cid = ?");
            stmtuserattributes.setInt(1, contextId);
            rs = stmtuserattributes.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                final String value = rs.getString("value");
                if (isDynamicAttribute(name)) {
                    final String[] namespaced = parseDynamicAttribute(name);
                    ctx.setUserAttribute(namespaced[0], namespaced[1], value);
                }
            }
        } finally {
            if(rs != null) {
                rs.close();
            }
            if(stmtuserattributes != null) {
                stmtuserattributes.close();
            }
        }

    }


    public Context[] loadContexts(final Collection<Integer> cids, final long averageSize, final List<Filter<Context, Context>> filters, final boolean failOnMissing) throws StorageException {
        PipesAndFiltersService pnfService;
        try {
            pnfService = AdminServiceRegistry.getInstance().getService(PipesAndFiltersService.class, true);
        } catch (final OXException e) {
            throw new StorageException(e.getMessage(), e);
        }
        DataSource<Context> output = pnfService.create(cids).addFilter(new ContextLoader(cache, failOnMissing));
        if( null != filters && !filters.isEmpty()) {
            for(final Filter<Context, Context> f : filters) {
                output = output.addFilter(f);
            }
        }
        output = output.addFilter(new LoginInfoLoader(cache)).addFilter(new FilestoreUsageLoader(cache, averageSize)).addFilter(new DynamicAttributesLoader(cache));
        final SortedMap<Integer, Context> retval = new TreeMap<Integer, Context>();
        try {
            while (output.hasData()) {
                final List<Context> tmp = new ArrayList<Context>();
                output.getData(tmp);
                for (final Context context : tmp) {
                    retval.put(context.getId(), context);
                }
            }
        } catch (final PipesAndFiltersException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof StorageException) {
                throw (StorageException) cause;
            }
            throw new StorageException(cause.getMessage(), cause);
        }
        return retval.values().toArray(new Context[retval.size()]);
    }

    public final void createStandardGroupForContext(final int context_id, final Connection ox_write_con, final String display_name, final int group_id, final int gid_number) throws SQLException {
        // TODO: this must be defined somewhere else
        final int NOGROUP = 65534;
        final PreparedStatement group_stmt = ox_write_con.prepareStatement("INSERT INTO groups (cid, id, identifier, displayname,lastModified,gidNumber) VALUES (?,?,'users',?,?,?);");
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

    public final void deleteContextFromConfigDB(final Connection configCon, final int contextId) throws SQLException {
        // find out what db_schema context belongs to
        PreparedStatement stmt3 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt = null;
        try {
            boolean cs2dbBroken = false;
            stmt2 = configCon.prepareStatement("SELECT db_schema,write_db_pool_id FROM context_server2db_pool WHERE cid=?");
            stmt2.setInt(1, contextId);
            stmt2.executeQuery();
            ResultSet rs = stmt2.getResultSet();
            String dbSchema = null;
            int poolId = -1;
            if (!rs.next()) {
                // throw new OXContextException("Unable to determine db_schema of context " + context_id);
                cs2dbBroken = true;
                log.error("Unable to determine db_schema of context " + contextId);
            } else {
                dbSchema = rs.getString(1);
                poolId = rs.getInt(2);
            }
            stmt2.close();
            if (log.isDebugEnabled()) {
                log.debug("Deleting context_server2dbpool mapping for context " + contextId);
            }
            // delete context from context_server2db_pool
            stmt2 = configCon.prepareStatement("DELETE FROM context_server2db_pool WHERE cid=?");
            stmt2.setInt(1, contextId);
            stmt2.executeUpdate();
            stmt2.close();
            // tell pool, that database has been removed
            try {
                com.openexchange.databaseold.Database.reset(contextId);
            } catch (final OXException e) {
                log.error(e.getMessage(), e);
            }

            if (!cs2dbBroken) {
                try {
                    // check if any other context uses the same db_schema
                    // if not, delete it
                    stmt2 = configCon.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE db_schema=?");
                    stmt2.setString(1, dbSchema);
                    stmt2.executeQuery();
                    rs = stmt2.getResultSet();
    
                    if (!rs.next()) {
                        // get auth data from db_pool to delete schema
                        stmt3 = configCon.prepareStatement("SELECT url,driver,login,password FROM db_pool WHERE db_pool_id=?");
                        stmt3.setInt(1, poolId);
                        stmt3.executeQuery();
                        final ResultSet rs3 = stmt3.getResultSet();
    
                        if (!rs3.next()) {
                            throw new StorageException("Unable to determine authentication data of pool_id " + poolId);
                        }
                        final Database db = new Database(rs3.getString("login"), rs3.getString("password"), rs3.getString("driver"), rs3.getString("url"), dbSchema);
                        if (log.isDebugEnabled()) {
                            log.debug("Deleting database " + dbSchema);
                        }
                        oxutilcommon.deleteDatabase(db);
                        stmt3.close();
                    }
                    stmt2.close();
                } catch (final Exception e) {
                    log.error("Problem deleting database while doing rollback, cid=" + contextId + ": ", e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Deleting login2context entries for context " + contextId);
            }
            stmt = configCon.prepareStatement("DELETE FROM login2context WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            stmt.close();
            if (log.isDebugEnabled()) {
                log.debug("Deleting context entry for context " + contextId);
            }
            stmt = configCon.prepareStatement("DELETE FROM context WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            stmt.close();
        } finally {
            closePreparedStatement(stmt);
            closePreparedStatement(stmt2);
            closePreparedStatement(stmt3);
        }
    }

    private String[] determineSequenceTables(final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<String> tmp = new ArrayList<String>();
        try {
            stmt = con.prepareStatement("SHOW TABLES LIKE ?");
            stmt.setString(1, "%sequence\\_%");
            result = stmt.executeQuery();
            while (result.next()) {
                tmp.add(result.getString(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        return tmp.toArray(new String[tmp.size()]);
    }

    public void fillContextAndServer2DBPool(final Context ctx, final Connection con, final Database db) throws StorageException {
        // dbid is the id in db_pool of database engine to use for next context
    
        // if read id -1 (not set by client ) or 0 (there is no read db for this
        // cluster) then read id must be same as write id
        // else the db pool cannot resolve the database
        if (null == db.getRead_id() || 0 == db.getRead_id().intValue()) {
            db.setRead_id(db.getId());
        }
    
        // create context entry in configdb
        // quota is in MB, but we store in Byte
        long quota_max_temp = ctx.getMaxQuota().longValue();
        if (quota_max_temp != -1) {
            quota_max_temp *= Math.pow(2, 20);
            ctx.setMaxQuota(L(quota_max_temp));
        }
        fillContextTable(ctx, con);

        try {
            final int serverId = ClientAdminThread.cache.getServerId();
            ClientAdminThread.cache.getPool().writeAssignment(con, new Assignment() {
                @Override
                public int getContextId() {
                    return i(ctx.getId());
                }
                @Override
                public int getServerId() {
                    return serverId;
                }
                @Override
                public int getReadPoolId() {
                    return i(db.getRead_id());
                }
                @Override
                public int getWritePoolId() {
                    return i(db.getId());
                }
                @Override
                public String getSchema() {
                    return db.getScheme();
                }
            });
        } catch (PoolException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    public final void handleCreateContextRollback(final Connection configCon, final Connection oxCon, final int contextId) {
        // Creating the whole context is now done in a transaction. Rolling back this transaction should be sufficient to remove the context
        // if creation fails.
        rollback(oxCon);
        // remove all entries from configuration database because everything to configuration database has been commited.
        try {
            if (configCon != null) {
                deleteContextFromConfigDB(configCon, contextId);
                configCon.commit();
            }
        } catch (final SQLException e) {
            log.error("SQL Error removing/rollback entries from configdb for context " + contextId, e);
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

    public final void initSequenceTables(final int contextId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            for (final String tableName : determineSequenceTables(con)) {
                final int startValue = modifyStartValue(tableName);
                stmt = con.prepareStatement("INSERT INTO `" + tableName + "` VALUES (?,?)");
                stmt.setInt(1, contextId);
                stmt.setInt(2, startValue);
                stmt.execute();
                stmt.close();
            }
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public final void initReplicationMonitor(final Connection con, final int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO replicationMonitor (cid,transaction) VALUES (?,0)");
            stmt.setInt(1, contextId);
            stmt.execute();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public final void initFilestoreUsage(final Connection con, final int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO filestore_usage (cid,used) VALUES (?,0)");
            stmt.setInt(1, contextId);
            stmt.execute();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private int modifyStartValue(final String tableName) {
        int retval = 0;
        if ("sequence_folder".equals(tableName)) {
            // below id 20 is reserved
            retval = 20;
        }
        // check for the uid number feature
        if ("sequence_uid_number".equals(tableName)) {
            final int startnum = Integer.parseInt(prop.getUserProp(AdminProperties.User.UID_NUMBER_START, "-1"));
            if (startnum > 0) {
                // we use the uid number feature
                // set the start number in the sequence for uid_numbers 
                retval = startnum;
            }
        }
        // check for the gid number feature
        if ("sequence_gid_number".equals(tableName)){
            final int startnum = Integer.parseInt(prop.getGroupProp(AdminProperties.Group.GID_NUMBER_START, "-1"));
            if (startnum > 0) {
                // we use the gid number feature
                // set the start number in the sequence for gid_numbers 
                retval = startnum;
            }
        }
        return retval;
    }

    private final void fillContextTable(final Context ctx, final Connection configdbCon) throws StorageException {
        PreparedStatement stmt = null;
        try {
            stmt = configdbCon.prepareStatement("INSERT INTO context (cid,name,enabled,filestore_id,filestore_name,quota_max) VALUES (?,?,?,?,?,?)");
            stmt.setInt(1, ctx.getId().intValue());
            if (ctx.getName() != null && ctx.getName().trim().length() > 0) {
                stmt.setString(2, ctx.getName());
            } else {
                stmt.setString(2, ctx.getIdAsString());
            }
            stmt.setBoolean(3, true);
            stmt.setInt(4, ctx.getFilestoreId().intValue());
            stmt.setString(5, ctx.getFilestore_name());
            stmt.setLong(6, ctx.getMaxQuota().longValue());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closePreparedStatement(stmt);
        }
    }

    public void fillLogin2ContextTable(final Context ctx, final Connection configdb_write_con) throws SQLException, StorageException {
        final HashSet<String> loginMappings = ctx.getLoginMappings();
        final Integer ctxid = ctx.getId();
        PreparedStatement stmt = null;
        PreparedStatement checkAvailable = null;
        ResultSet found = null;
        try {
            checkAvailable = configdb_write_con.prepareStatement("SELECT 1 FROM login2context WHERE login_info = ?");
            stmt = configdb_write_con.prepareStatement("INSERT INTO login2context (cid,login_info) VALUES (?,?)");
            for (final String mapping : loginMappings) {
                checkAvailable.setString(1, mapping);
                found = checkAvailable.executeQuery();
                final boolean mappingTaken = found.next();
                found.close();

                if(mappingTaken) {
                    throw new StorageException("Cannot map '"+mapping+"' to the newly created context. This mapping is already in use.");
                }

                stmt.setInt(1, ctxid);
                stmt.setString(2, mapping);
                stmt.executeUpdate();
            }
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            throw sql;
        } finally {
            closeResultSet(found);
            closePreparedStatement(checkAvailable);
            closePreparedStatement(stmt);

        }
    }

    private void closeResultSet(final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final SQLException e) {
            log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
        }
    }
    
    private void closePreparedStatement(final PreparedStatement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (final SQLException e) {
            log.error(OXContextMySQLStorageCommon.LOG_ERROR_CLOSING_STATEMENT, e);
        }
    }


}
