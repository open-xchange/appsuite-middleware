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
/**
 *
 */
package com.openexchange.admin.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.sqlStorage.OXToolSQLStorage;
import com.openexchange.admin.tools.AdminCache;

/**
 * @author d7
 * @author cutmasta
 */
public class OXToolMySQLStorage extends OXToolSQLStorage {

    private final static Log log = LogFactory.getLog(OXToolMySQLStorage.class);

    
    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#checkPrimaryMail(int,
     *      java.lang.String)
     */
    public void checkPrimaryMail(final Context ctx, final String primary_mail) throws StorageException, InvalidDataException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            prep_check = con.prepareStatement("SELECT mail FROM user WHERE cid = ? AND mail = ?");
            prep_check.setInt(1,ctx.getIdAsInt());
            prep_check.setString(2, primary_mail);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                throw new InvalidDataException("Sent primary mail already exists in this context");
                // throw USER_EXCEPTIONS.create(0,primary_mail,context_id);
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }

            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
            } catch (PoolException e) {
                log.error("Error pushing ox db write connection to pool!", e);
            }

        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsContext(int)
     */
    public boolean existsContext(final Context ctx) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context WHERE cid = ?;", ctx.getIdAsInt());
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsDatabase(int)
     */
    public boolean existsDatabase(final int db_id) throws StorageException {
        return selectwithint(-1, "SELECT name FROM db_pool WHERE db_pool_id = ?", db_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsDatabase(java.lang.String)
     */
    public boolean existsDatabase(final String db_name) throws StorageException {
        return selectwithstring(-1, "SELECT db_pool_id FROM db_pool WHERE name = ?;", db_name);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      int)
     */
    public boolean existsGroup(final Context ctx, final int gid) throws StorageException {
        return selectwithint(ctx.getIdAsInt(), "SELECT id FROM groups WHERE cid = ? AND id = ?;", ctx.getIdAsInt(), gid);
    }

    public boolean existsGroup(final Context ctx, final int[] gids) throws StorageException {
        boolean retBool = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            for (final int elem : gids) {
                prep_check = con.prepareStatement("SELECT id FROM groups WHERE cid = ? AND id = ?;");
                prep_check.setInt(1, ctx.getIdAsInt());
                prep_check.setInt(2, elem);

                rs = prep_check.executeQuery();

                if (rs.next()) {
                    retBool = true;
                    prep_check.close();
                } else {
                    prep_check.close();
                    return false;
                }
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                if (con != null) {
                    cache.pushOXDBWrite(ctx.getIdAsInt(), con);
                }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }

        return retBool;

    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      java.lang.String)
     */
    public boolean existsGroup(final Context ctx, final String identifier) throws StorageException {
        boolean retBool = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());

            prep_check = con.prepareStatement("SELECT id FROM groups WHERE cid = ? AND identifier = ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, identifier);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            } else {
                retBool = false;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Error closing resultset", e);
            }
            try {
                if (prep_check != null) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
            } catch (PoolException e) {
                log.error("Error pushing ox db write connection to pool!", e);
            }

        }

        return retBool;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int[])
     */
    public boolean existsGroupMember(final Context ctx, final int group_ID, final int[] user_ids) throws StorageException {
        boolean ret = false;
        Connection con = null;
        AdminCache cache = ClientAdminThread.cache;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            StringBuffer sb = new StringBuffer();
            for (int a = 0; a < user_ids.length; a++) {
                sb.append(user_ids[a] + ",");
            }
            sb.delete(sb.length() - 1, sb.length());
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            prep = con.prepareStatement("SELECT member FROM groups_member WHERE cid = ? AND id = ? AND member IN (" + sb.toString() + ")");
            prep.setInt(1, ctx.getIdAsInt());
            prep.setInt(2, group_ID);
            rs = prep.executeQuery();
            if (rs.next()) {
                // one of the members is already in this group
                ret = true;
            }
            prep.close();
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushOXDBWrite(ctx.getIdAsInt(), con);
                }
            } catch (PoolException ecp) {
                log.error("Error pushing ox db write connection to pool!", ecp);
            }
        }
        return ret;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int)
     */
    public boolean existsGroupMember(final Context ctx, final int group_ID, final int member_ID) throws StorageException {
        return selectwithint(ctx.getIdAsInt(), "SELECT id FROM groups_member WHERE cid = ? AND id = ? AND member = ?", ctx.getIdAsInt(), group_ID, member_ID);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsReason(int)
     */
    public boolean existsReason(final int rid) throws StorageException {
        return selectwithint(-1, "SELECT id FROM reason_text WHERE id = ?;", rid);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsReason(java.lang.String)
     */
    public boolean existsReason(final String reason) throws StorageException {
        return selectwithstring(-1, "SELECT id FROM reason_text WHERE text = ?;", reason);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResource(int,
     *      java.lang.String, int)
     */
    public boolean existsResource(final Context ctx, final String identifier) throws StorageException {
        
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
           
            prep_check = con.prepareStatement("SELECT id FROM resource WHERE cid = ? AND identifier = ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, identifier);            
            rs = prep_check.executeQuery();
            if (rs.next()) {
                return true;
            }else{
                return false;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }
        
        
        //return selectwithintstringint(context_ID, "SELECT id FROM resource WHERE cid = ? AND identifier = ? OR id = ?", context_ID, identifier, resource_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResource(int,
     *      int)
     */
    public boolean existsResource(final Context ctx, final int resource_id) throws StorageException {
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
           
            prep_check = con.prepareStatement("SELECT id FROM resource WHERE cid = ? AND id = ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setInt(2, resource_id);            
            rs = prep_check.executeQuery();
            if (rs.next()) {
                return true;
            }else{
                return false;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);            
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResourceGroup(int,
     *      java.lang.String, int)
     */
    public boolean existsResourceGroup(final Context ctx, final String identifier, final int resource_group) throws StorageException {
        return selectwithintstringint(ctx.getIdAsInt(), "SELECT id FROM resource_group WHERE cid = ? AND identifier = ? OR id = ?", ctx.getIdAsInt(), identifier, resource_group);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServer(int)
     */
    public boolean existsServer(final int server_id) throws StorageException {
        return selectwithint(-1, "SELECT server_id FROM server WHERE server_id = ?", server_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServer(java.lang.String)
     */
    public boolean existsServer(final String server_name) throws StorageException {
        return selectwithstring(-1, "SELECT server_id FROM server WHERE name = ?;", server_name);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsServerID(int,
     *      java.lang.String, java.lang.String)
     */
    public boolean existsServerID(final int check_ID, final String table, final String field) throws StorageException {
        return selectwithint(-1, "SELECT server_id FROM " + table + " WHERE " + field + " = ?;", check_ID);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsStore(int)
     */
    public boolean existsStore(final int store_id) throws StorageException {
        return selectwithint(-1, "SELECT uri FROM filestore WHERE id = ?", store_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsStore(java.lang.String)
     */
    public boolean existsStore(final String url) throws StorageException {
        return selectwithstring(-1, "SELECT uri FROM filestore WHERE uri = ?", url);
    }
    
    
    /*
     * Check if any login mapping in the given context already exists in the system
     */
    public static boolean existsContextLoginMappings(Context ctx,Connection configdb_connection) throws StorageException {
        if(ctx.getLoginMappings()!=null){
            boolean retval = false;
            // check if any sent mapping entry already exists            
            PreparedStatement prep_check = null;
            ResultSet rs = null;
            try {
                                
                
                HashSet<String> logmaps = ctx.getLoginMappings();
                Iterator itr = logmaps.iterator();
                
                while(itr.hasNext()){
                    String mpi = (String)itr.next();
                    
                    prep_check = configdb_connection.prepareStatement("SELECT cid from login2context where login_info = ?");
                    prep_check.setString(1, mpi);
                    rs = prep_check.executeQuery();
                    if (rs.next()) {
                        retval = true;                        
                    }
                    rs.close();
                    prep_check.close();
                    if(retval){
                        break;
                    }
                }
                 return retval;              
           
            } catch (SQLException e) {
                log.error("SQL Error",e);
                throw new StorageException(e);
            } finally {
                if (null != rs) {
                    try {
                        if(rs!=null){
                            rs.close();
                        }
                    } catch (SQLException e) {
                        log.error("Error closing resultset", e);
                    }
                }
                try {
                    if (null != prep_check) {
                        prep_check.close();
                    }
                } catch (SQLException e) {
                    log.error("Error closing prepared statement!", e);
                }
            }
            
        }else{
            return false;
        }       
    }
    
    /*
     * Check if any login mapping in the given context already exists in the system
     */
    public boolean existsContextLoginMappings(Context ctx) throws StorageException {
        
        Connection con= null;
        
        try{
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            return existsContextLoginMappings(ctx,con); 
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        }finally{
            try {
                if(con!=null){
                    cache.pushOXDBWrite(ctx.getIdAsInt(), con);
                }
             } catch (PoolException e) {
                 log.error("Error pushing configdb write connection to pool!", e);
             }
        }
        
        
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      java.lang.String)
     */
    public boolean existsUser(final Context ctx, final String username) throws StorageException {
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
           
            prep_check = con.prepareStatement("SELECT id FROM login2user WHERE cid = ? AND uid = ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, username);            
            rs = prep_check.executeQuery();
            if (rs.next()) {
                return true;
            }else{
                return false;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int)
     */
    public boolean existsUser(final Context ctx, final int uid) throws StorageException {
        
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
           
            prep_check = con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id = ?;");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setInt(2, uid);            
            rs = prep_check.executeQuery();
            if (rs.next()) {
                return true;
            }else{
                return false;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }      
        
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int[])
     */
    public boolean existsUser(final Context ctx, final int[] user_ids) throws StorageException {
        boolean ret = false;
        Connection con = null;
        AdminCache cache = ClientAdminThread.cache;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            StringBuffer sb = new StringBuffer();
            for (int a = 0; a < user_ids.length; a++) {
                sb.append("?,");
                // sb.append(user_ids[a]+",");
            }
            sb.delete(sb.length() - 1, sb.length());
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            prep = con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id IN (" + sb.toString() + ")");
            prep.setInt(1, ctx.getIdAsInt());

            int prep_index = 2;
            for (int element : user_ids) {
                prep.setInt(prep_index, element);
            }

            rs = prep.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
            }
            rs.close();

            if (count == user_ids.length) {
                // ok, die user gibts alle
                ret = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (SQLException e) {
                log.error("Error closing statement", e);
            }

            if (null != con) {
                try {
                    cache.pushOXDBWrite(ctx.getIdAsInt(), con);
                } catch (PoolException ecp) {
                    log.error("Error pushing ox db write connection to pool!", ecp);
                }
            }
        }
        return ret;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getAdminForContext(int,
     *      java.sql.Connection)
     */
    public int getAdminForContext(final Context ctx, final Connection con) throws StorageException {
        int admin_id = 1;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // con = cache.getREADConnectionForContext(context_id);
            stmt = con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid = ?");
            stmt.setInt(1, ctx.getIdAsInt());
            rs = stmt.executeQuery();
            if (rs.next()) {
                admin_id = rs.getInt("user");
            } else {
                throw new SQLException("UNABLE TO GET MAILADMIN ID FOR CONTEXT " + ctx.getIdAsInt());
            }
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != stmt) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }

        return admin_id;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getDefaultGroupForContext(int,
     *      java.sql.Connection)
     */
public int getDefaultGroupForContext(final Context ctx, final Connection con) throws StorageException {
        int group_id = 0;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT MIN(id) FROM groups WHERE cid=?");
            stmt.setInt(1, ctx.getIdAsInt());
            rs = stmt.executeQuery();
            if (rs.next()) {
                group_id = rs.getInt("MIN(id)");
            } else {
                throw new SQLException("UNABLE TO GET DEFAULT GROUP FOR CONTEXT " + ctx.getIdAsInt());
            }
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Error closing resultset!", e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }

        return group_id;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isContextAdmin(int,
     *      int)
     */
    public boolean isContextAdmin(final Context ctx, final int user_id) throws StorageException {
        boolean isadmin = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;

        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt());
            int a = getAdminForContext(ctx, con);
            if (a == user_id) {
                isadmin = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } finally {
            try {
                if (con != null) {
                    cache.pushOXDBRead(ctx.getIdAsInt(), con);
                }
            } catch (PoolException e) {
                log.error("Error pushing oxdb read connection to pool!", e);
            }
        }
        return isadmin;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isContextEnabled(int)
     */
    public boolean isContextEnabled(final Context ctx) throws StorageException {
        boolean retBool = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement("SELECT enabled FROM context WHERE cid = ?;");
            prep_check.setInt(1, ctx.getIdAsInt());
            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = rs.getBoolean("enabled");
            } else {
                throw new SQLException("UNABLE TO QUERY CONTEXT STATUS");
            }

        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (prep_check != null) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                cache.pushConfigDBWrite(con);
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }
        }

        return retBool;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isMasterDatabase(int)
     */
    public boolean isMasterDatabase(final int database_id) throws StorageException {
        return selectwithint(-1, "SELECT cluster_id FROM db_cluster WHERE write_db_pool_id = ?", database_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#poolInUse(long)
     */
    public boolean poolInUse(final int pool_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context_server2db_pool WHERE write_db_pool_id = ? OR read_db_pool_id = ?", pool_id, pool_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#serverInUse(long)
     */
    public boolean serverInUse(final int server_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context_server2db_pool WHERE server_id = ?", server_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#storeInUse(long)
     */
    public boolean storeInUse(final int store_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context WHERE filestore_id = ?", store_id);
    }

    /**
     * This function is used for all sql queries which insert an integer
     * 
     * @param sql_select_string
     * @param context_id
     *            if -1 we use configbd connection for query, else ox db
     *            connection with given context id
     * @param ins_number
     * @return
     * @throws StorageException
     */
    private boolean selectwithint(int context_id, final String sql_select_string, final int... ins_numbers) throws StorageException {
        boolean retBool = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            if (context_id != -1) {
                con = cache.getWRITEConnectionForContext(context_id);
            } else {
                con = cache.getWRITEConnectionForCONFIGDB();
            }
            prep_check = con.prepareStatement(sql_select_string);
            int sql_counter = 1;
            for (int i = 0; i < ins_numbers.length; i++) {
                prep_check.setInt(sql_counter, ins_numbers[i]);
                sql_counter++;
            }

            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                if (context_id != -1) {
                    cache.pushOXDBWrite(context_id, con);
                } else {
                    cache.pushConfigDBWrite(con);
                }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }

        return retBool;
    }

    /**
     * This function is used for all sql queries which insert a string
     * 
     * @param sql_select_string
     * @param ins_Strings
     * @return
     * @throws StorageException
     */
    private boolean selectwithstring(int context_id, final String sql_select_string, final String... ins_strings) throws StorageException {
        boolean retBool = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            if (context_id != -1) {
                con = cache.getWRITEConnectionForContext(context_id);
            } else {
                con = cache.getWRITEConnectionForCONFIGDB();
            }
            prep_check = con.prepareStatement(sql_select_string);
            int sql_counter = 1;
            for (int i = 0; i < ins_strings.length; i++) {
                prep_check.setString(sql_counter, ins_strings[i]);
                sql_counter++;
            }

            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                if (context_id != -1) {
                    cache.pushOXDBWrite(context_id, con);
                } else {
                    cache.pushConfigDBWrite(con);
                }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }

        return retBool;
    }

    /**
     * This function is used for all sql queries which insert an integer
     * followed by a string followed by an integer as option
     * 
     * @param sql_select_string
     * @param firstnumber
     *            the first integer
     * @param string
     *            the string value
     * @param secondnumber
     *            the second integer (left out if int is -1)
     * @return
     * @throws StorageException
     */
    private boolean selectwithintstringint(int context_id, final String sql_select_string, final int firstnumber, final String string, final int secondnumber) throws StorageException {
        boolean retBool = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            if (context_id != -1) {
                con = cache.getWRITEConnectionForContext(context_id);
            } else {
                con = cache.getWRITEConnectionForCONFIGDB();
            }
            prep_check = con.prepareStatement(sql_select_string);
            prep_check.setInt(1, firstnumber);
            prep_check.setString(2, string);
            if (-1 != secondnumber) {
                prep_check.setInt(3, secondnumber);
//            } else {
//                prep_check.setInt(3, java.sql.Types.INTEGER);
            }
            // SELECT id FROM resource WHERE cid = ? AND identifier = ? OR id =
            // ?
            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            }
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (null != prep_check) {
                    prep_check.close();
                }
            } catch (SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                if (context_id != -1) {
                    cache.pushOXDBWrite(context_id, con);
                } else {
                    cache.pushConfigDBWrite(con);
                }
            } catch (PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }

        return retBool;
    }

}
