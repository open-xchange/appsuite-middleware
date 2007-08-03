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
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.sqlStorage.OXToolSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.update.Updater;
import com.openexchange.groupware.update.exception.UpdateException;

/**
 * @author d7
 * @author cutmasta
 */
public class OXToolMySQLStorage extends OXToolSQLStorage implements OXMySQLDefaultValues {

    private final static Log log = LogFactory.getLog(OXToolMySQLStorage.class);

    @Override
    public boolean domainInUse(final Context ctx, final String domain) throws StorageException {        
        Connection con = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            Resource[] res =  getDomainUsedbyResource(ctx, domain, con);
            Group[] grp =  getDomainUsedbyGroup(ctx, domain, con);
            User[] usr =  getDomainUsedbyUser(ctx, domain, con);
            return (res!=null || grp!=null || usr!=null);             
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        }finally{
            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }
        }
    }

    @Override
    public Group[] domainInUseByGroup(Context ctx, String domain) throws StorageException {
        // currently mailaddresse not used  in core for groups
        return null;
    }

    @Override
    public Resource[] domainInUseByResource(Context ctx, String domain) throws StorageException {
        Connection con = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            return getDomainUsedbyResource(ctx, domain, con);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        }finally{
            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }
        }
    }

    @Override
    public User[] domainInUseByUser(Context ctx, String domain) throws StorageException {
        Connection con = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            return getDomainUsedbyUser(ctx, domain, con);
        } catch (SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } catch (PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        }finally{
            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsContext(int)
     */
    public boolean existsContext(final Context ctx) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context WHERE cid = ?;", ctx.getIdAsInt());
    }

    /*
     * Check if any login mapping in the given context already exists in the system
     */
    public boolean existsContextLoginMappings(final Context ctx) throws StorageException {
        
        Connection con= null;
        
        try{
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            return existsContextLoginMappings(ctx,con); 
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        }finally{
            returnConnection(ctx.getIdAsInt(), con);
        }
        
        
    }

    /*
     * Check if any login mapping in the given context already exists in the system
     */
    public boolean existsContextLoginMappings(final Context ctx,final Connection configdb_connection) throws StorageException {
        if(ctx.getLoginMappings()!=null){
            boolean retval = false;
            // check if any sent mapping entry already exists            
            PreparedStatement prep_check = null;
            ResultSet rs = null;
            try {
                                
                
                final HashSet<String> logmaps = ctx.getLoginMappings();
                final Iterator<String> itr = logmaps.iterator();
                
                while(itr.hasNext()){
                    final String mpi = (String)itr.next();
                    
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
           
            } catch (final SQLException e) {
                log.error("SQL Error",e);
                throw new StorageException(e);
            } finally {
                if (null != rs) {
                    try {
                        if(rs!=null){
                            rs.close();
                        }
                    } catch (final SQLException e) {
                        log.error("Error closing resultset", e);
                    }
                }
                closePreparedStatement(prep_check);
            }
            
        }else{
            return false;
        }       
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

    @Override
    public boolean existsDisplayName(final Context ctx, final User usr) throws StorageException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt());
            ps = con.prepareStatement("SELECT field01 FROM prg_contacts WHERE cid = ? AND field01 = ?;");
            ps.setInt(1, ctx.getIdAsInt());
            ps.setString(2, usr.getDisplay_name());
            
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(ps);
            returnConnection(ctx.getIdAsInt(), con);
        }
    }

    @Override
    public boolean existsGroup(final Context ctx, final Group[] grps) throws StorageException {
        boolean retBool = false;
        final AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            for (final Group grp : grps) {
                prep_check = con.prepareStatement("SELECT id FROM groups WHERE cid = ? AND id = ?;");
                prep_check.setInt(1, ctx.getIdAsInt());
                prep_check.setInt(2, grp.getId());

                rs = prep_check.executeQuery();

                if (rs.next()) {
                    retBool = true;
                    prep_check.close();
                } else {
                    prep_check.close();
                    return false;
                }
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
            returnConnection(ctx.getIdAsInt(), con);
        }

        return retBool;
    }

    @Override
    public boolean existsGroup(final Context ctx, final Group grp) throws StorageException {
        return existsGroup(ctx, new Group[]{grp});
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      int)
     */
    public boolean existsGroup(final Context ctx, final int gid) throws StorageException {
        return selectwithint(ctx.getIdAsInt(), "SELECT id FROM groups WHERE cid = ? AND id = ?;", ctx.getIdAsInt(), gid);
    }

    /**
     * @deprecated Use method with User[] instead
     */
    @Deprecated
    public boolean existsGroup(final Context ctx, final int[] gids) throws StorageException {
        boolean retBool = false;
        final AdminCache cache = ClientAdminThread.cache;
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
            returnConnection(ctx.getIdAsInt(), con);
        }

        return retBool;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroup(int,
     *      java.lang.String)
     */
    public boolean existsGroup(final Context ctx, final String identifier) throws StorageException {
        boolean retBool = false;
        final AdminCache cache = ClientAdminThread.cache;
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);
            returnConnection(ctx.getIdAsInt(), con);
        }

        return retBool;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int)
     */
    public boolean existsGroupMember(final Context ctx, final int group_ID, final int member_ID) throws StorageException {
        return selectwithint(ctx.getIdAsInt(), "SELECT id FROM groups_member WHERE cid = ? AND id = ? AND member = ?", ctx.getIdAsInt(), group_ID, member_ID);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsGroupMember(int,
     *      int, int[])
     */
    public boolean existsGroupMember(final Context ctx, final int group_ID, final int[] user_ids) throws StorageException {
        boolean ret = false;
        Connection con = null;
        final AdminCache cache = ClientAdminThread.cache;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            final StringBuffer sb = new StringBuffer();
            for (int element : user_ids) {
                sb.append(element + ",");
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }
            try {
                if (con != null) {
                    cache.pushOXDBWrite(ctx.getIdAsInt(), con);
                }
            } catch (final PoolException ecp) {
                log.error("Error pushing ox db write connection to pool!", ecp);
            }
        }
        return ret;
    }

    @Override
    public boolean existsGroupMember(Context ctx, int group_ID, User[] users) throws StorageException {
        int []ids = new int[users.length];
        for(int i=0; i<ids.length; i++) {
            ids[i] = users[i].getId();
        }
        return existsGroupMember(ctx, group_ID, ids);
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
     *      int)
     */
    public boolean existsResource(final Context ctx, final int resource_id) throws StorageException {
        final AdminCache cache = ClientAdminThread.cache;
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);            
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (final PoolException e) {
                log.error("Error pushing ox write connection to pool!", e);
            }

        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsResource(int,
     *      java.lang.String, int)
     */
    public boolean existsResource(final Context ctx, final String identifier) throws StorageException {
        
        final AdminCache cache = ClientAdminThread.cache;
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (final PoolException e) {
                log.error("Error pushing ox write connection to pool!", e);
            }

        }
        
        
        //return selectwithintstringint(context_ID, "SELECT id FROM resource WHERE cid = ? AND identifier = ? OR id = ?", context_ID, identifier, resource_id);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.storage.sqlStorage.OXToolSQLStorage#existsResourceAddress(com.openexchange.admin.rmi.dataobjects.Context, java.lang.String)
     */
    @Override
    public boolean existsResourceAddress(Context ctx, String address) throws StorageException {
        
        final AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
           
            prep_check = con.prepareStatement("SELECT mail FROM resource WHERE cid = ? AND mail = ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, address);            
            rs = prep_check.executeQuery();
            if (rs.next()) {
                return true;
            }else{
                return false;
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (final PoolException e) {
                log.error("Error pushing ox write connection to pool!", e);
            }

        }
    }

    public boolean existsResourceAddress(Context ctx, String address,Integer resource_id) throws StorageException {
        
        final AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
           
            prep_check = con.prepareStatement("SELECT id from resource where cid = ? and mail = ? AND id != ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, address);      
            prep_check.setInt(3, resource_id);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                return true;
            }else{
                return false;
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (final PoolException e) {
                log.error("Error pushing ox write connection to pool!", e);
            }

        }
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

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int)
     */
    public boolean existsUser(final Context ctx, final int uid) throws StorageException {
        
        final AdminCache cache = ClientAdminThread.cache;
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (final PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }      
        
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      int[])
     */
    @SuppressWarnings("unused")
    public boolean existsUser(final Context ctx, final int[] user_ids) throws StorageException {
        boolean ret = false;
        Connection con = null;
        final AdminCache cache = ClientAdminThread.cache;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            final StringBuffer sb = new StringBuffer();
            for (final int element : user_ids) {
                sb.append("?,");
                // sb.append(user_ids[a]+",");
            }
            sb.delete(sb.length() - 1, sb.length());
            con = cache.getWRITEConnectionForContext(ctx.getIdAsInt());
            prep = con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id IN (" + sb.toString() + ")");
            prep.setInt(1, ctx.getIdAsInt());

            int prep_index = 2;
            for (final int element : user_ids) {
                prep.setInt(prep_index, element);
                prep_index++;
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            try {
                if (prep != null) {
                    prep.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing statement", e);
            }

            if (null != con) {
                try {
                    cache.pushOXDBWrite(ctx.getIdAsInt(), con);
                } catch (final PoolException ecp) {
                    log.error("Error pushing ox db write connection to pool!", ecp);
                }
            }
        }
        return ret;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#existsUser(int,
     *      java.lang.String)
     */
    public boolean existsUser(final Context ctx, final String username) throws StorageException {
        final AdminCache cache = ClientAdminThread.cache;
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
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
               if(con!=null){
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
               }
            } catch (final PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }
    }
    
    @Override
    public boolean existsUser(final Context ctx, final User[] users) throws StorageException {
        int []ids = new int[users.length];
        for(int i=0; i<ids.length; i++) {
            ids[i] = users[i].getId();
        }
        // FIXME: Should be rewritten to optimize performance
        return existsUser(ctx, ids);
    }

    @Override
    public boolean existsUser(final Context ctx, final User user) throws StorageException {
        // FIXME: Should be rewritten to optimize performance
        return existsUser(ctx, new User[]{user});
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
                throw new SQLException("Unable to determine admin for context " + ctx.getIdAsInt());
            }
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            closePreparedStatement(stmt);
        }

        return admin_id;
    }

    @Override
    public int getContextIDByContextname(String ctxname) throws StorageException {
        return getByNameForConfigDB(ctxname, "context", "SELECT cid FROM context WHERE name=?");
    }

    @Override
    public int getDatabaseIDByDatabasename(final String dbname) throws StorageException {
        return getByNameForConfigDB(dbname, "database", "SELECT db_pool_id FROM db_pool WHERE name=?");
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
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            try {
                rs.close();
            } catch (final SQLException e) {
                log.error("Error closing resultset!", e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }

        return group_id;
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#getDefaultGroupForContext(int,
     *      java.sql.Connection)
     */
public int getDefaultGroupForContextWithOutConnection(final Context ctx) throws StorageException {
        int group_id = 0;
        Connection con = null;
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt());
            stmt = con.prepareStatement("SELECT MIN(id) FROM groups WHERE cid=?");
            stmt.setInt(1, ctx.getIdAsInt());
            rs = stmt.executeQuery();
            if (rs.next()) {
                group_id = rs.getInt("MIN(id)");
            } else {
                throw new SQLException("UNABLE TO GET DEFAULT GROUP FOR CONTEXT " + ctx.getIdAsInt());
            }
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing resultset!", e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
            try {
                if (con != null) {
                    cache.pushOXDBRead(ctx.getIdAsInt(), con);
                }
            } catch (final PoolException e) {
                log.error("Error pushing oxdb read connection to pool!", e);
            }
        }

        return group_id;
    }

    public int getGidNumberOfGroup(final Context ctx,final int group_id, final Connection con) throws StorageException {
        int gid_number = -1;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT gidNumber FROM groups WHERE cid=? AND id=?");
            stmt.setInt(1, ctx.getIdAsInt().intValue());
            stmt.setInt(2, group_id);
            rs = stmt.executeQuery();
            if(rs.next()){
                gid_number = rs.getInt("gidNumber");
            }
            rs.close();
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            try {
                if(rs!=null){
                    rs.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing resultset!", e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }

        return gid_number;
    }

    @Override
    public int getGroupIDByGroupname(Context ctx, String groupname) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            prep_check = con.prepareStatement("SELECT id from groups where cid = ? and identifier = ?");
            prep_check.setInt(1,ctx.getIdAsInt().intValue());
            prep_check.setString(2, groupname);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                // grab id and return 
                return rs.getInt("id");
            }else{
                throw new StorageException("No such group "+groupname+" in context "+ctx.getIdAsInt().intValue()+"");
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(prep_check);

            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }

        }
    }

    @Override
    public String getGroupnameByGroupID(Context ctx, int group_id) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            prep_check = con.prepareStatement("SELECT identifier from groups where cid = ? and id = ?");
            prep_check.setInt(1,ctx.getIdAsInt().intValue());
            prep_check.setInt(2, group_id);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                // grab username and return 
                return rs.getString("identifier");
            }else{
                throw new StorageException("No such group "+group_id+" in context "+ctx.getIdAsInt().intValue()+"");
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(prep_check);

            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }

        }
    }

    @Override
    public int getResourceIDByResourcename(Context ctx, String resourcename) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            prep_check = con.prepareStatement("SELECT id from resource where cid = ? and identifier = ?");
            prep_check.setInt(1,ctx.getIdAsInt().intValue());
            prep_check.setString(2, resourcename);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                // grab username and return 
                return rs.getInt("id");
            }else{
                throw new StorageException("No such resource "+resourcename+" in context "+ctx.getIdAsInt().intValue()+"");
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(prep_check);

            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }

        }
    }

    @Override
    public String getResourcenameByResourceID(Context ctx, int resource_id) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            prep_check = con.prepareStatement("SELECT identifier from resource where cid = ? and id = ?");
            prep_check.setInt(1,ctx.getIdAsInt().intValue());
            prep_check.setInt(2, resource_id);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                // grab username and return 
                return rs.getString("identifier");
            }else{
                throw new StorageException("No such resource "+resource_id+" in context "+ctx.getIdAsInt().intValue()+"");
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(prep_check);

            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }

        }
    }

    @Override
    public int getUserIDByUsername(final Context ctx, final String username) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            prep_check = con.prepareStatement("SELECT id from login2user where cid = ? and uid = ?");
            prep_check.setInt(1,ctx.getIdAsInt().intValue());
            prep_check.setString(2, username);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                // grab user id and return 
                return rs.getInt("id");
            }else{
                throw new StorageException("No such user "+username+" in context "+ctx.getIdAsInt().intValue()+"");
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(prep_check);

            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }

        }
    }

    @Override
    public String getUsernameByUserID(final Context ctx, final int user_id) throws StorageException {
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt().intValue());
            prep_check = con.prepareStatement("SELECT uid from login2user where cid = ? and id = ?");
            prep_check.setInt(1,ctx.getIdAsInt().intValue());
            prep_check.setInt(2, user_id);
            rs = prep_check.executeQuery();
            if (rs.next()) {
                // grab username and return 
                return rs.getString("uid");
            }else{
                throw new StorageException("No such user "+user_id+" in context "+ctx.getIdAsInt().intValue()+"");
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(prep_check);

            try {
                cache.pushOXDBRead(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }

        }
    }

//    /**
//     * This function is used for all sql queries which insert an integer
//     * followed by a string followed by an integer as option
//     * 
//     * @param sql_select_string
//     * @param firstnumber
//     *            the first integer
//     * @param string
//     *            the string value
//     * @param secondnumber
//     *            the second integer (left out if int is -1)
//     * @return
//     * @throws StorageException
//     */
//    private boolean selectwithintstringint(final int context_id, final String sql_select_string, final int firstnumber, final String string, final int secondnumber) throws StorageException {
//        boolean retBool = false;
//        final AdminCache cache = ClientAdminThread.cache;
//        Connection con = null;
//        PreparedStatement prep_check = null;
//        ResultSet rs = null;
//        try {
//            if (context_id != -1) {
//                con = cache.getWRITEConnectionForContext(context_id);
//            } else {
//                con = cache.getWRITEConnectionForCONFIGDB();
//            }
//            prep_check = con.prepareStatement(sql_select_string);
//            prep_check.setInt(1, firstnumber);
//            prep_check.setString(2, string);
//            if (-1 != secondnumber) {
//                prep_check.setInt(3, secondnumber);
////            } else {
////                prep_check.setInt(3, java.sql.Types.INTEGER);
//            }
//            // SELECT id FROM resource WHERE cid = ? AND identifier = ? OR id =
//            // ?
//            rs = prep_check.executeQuery();
//            if (rs.next()) {
//                retBool = true;
//            }
//        } catch (final PoolException e) {
//            log.error("Pool Error",e);
//            throw new StorageException(e);
//        } catch (final SQLException e) {
//            log.error("SQL Error",e);
//            throw new StorageException(e);
//        } finally {
//            if (null != rs) {
//                try {
//                    rs.close();
//                } catch (final SQLException e) {
//                    log.error("Error closing resultset", e);
//                }
//            }
//            try {
//                if (null != prep_check) {
//                    prep_check.close();
//                }
//            } catch (final SQLException e) {
//                log.error("Error closing prepared statement!", e);
//            }
//
//            try {
//                if (context_id != -1) {
//                    cache.pushOXDBWrite(context_id, con);
//                } else {
//                    cache.pushConfigDBWrite(con);
//                }
//            } catch (final PoolException e) {
//                log.error("Error pushing configdb write connection to pool!", e);
//            }
//
//        }
//
//        return retBool;
//    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#isContextAdmin(int,
     *      int)
     */
    public boolean isContextAdmin(final Context ctx, final int user_id) throws StorageException {
        boolean isadmin = false;
        final AdminCache cache = ClientAdminThread.cache;
        Connection con = null;

        try {
            con = cache.getREADConnectionForContext(ctx.getIdAsInt());
            final int a = getAdminForContext(ctx, con);
            if (a == user_id) {
                isadmin = true;
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } finally {
            try {
                if (con != null) {
                    cache.pushOXDBRead(ctx.getIdAsInt(), con);
                }
            } catch (final PoolException e) {
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
        final AdminCache cache = ClientAdminThread.cache;
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

        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final Exception e) {
                    log.error("Error closing resultset", e);
                }
            }
            try {
                if (prep_check != null) {
                    prep_check.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }

            try {
                cache.pushConfigDBWrite(con);
            } catch (final PoolException e) {
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

    @Override
    public boolean isUserSettingMailBitSet(final Context ctx, final User user, final int bit, final Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT bits FROM user_setting_mail WHERE cid = ? AND user = ?");
            stmt.setInt(1, ctx.getIdAsInt());
            stmt.setInt(2, user.getId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                final int bits = rs.getInt("bits");
                if( (bits & bit) == bit ) {
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new SQLException("Unable to get features from bitfield for User: " + user.getId() + ", Context: " + ctx.getIdAsInt());
            }
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            closePreparedStatement(stmt);
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#poolInUse(long)
     */
    public boolean poolInUse(final int pool_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context_server2db_pool WHERE write_db_pool_id = ? OR read_db_pool_id = ?", pool_id, pool_id);
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#primaryMailExists(int,
     *      java.lang.String)
     */
    public void primaryMailExists(final Context ctx, final String primary_mail) throws StorageException, InvalidDataException {
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
                throw new InvalidDataException("Primary mail address already exists in this context");
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(prep_check);

            try {
                cache.pushOXDBWrite(ctx.getIdAsInt(), con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db write connection to pool!", e);
            }

        }
    }

    @Override
    public boolean schemaBeingLockedOrNeedsUpdate(final Context ctx) throws StorageException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String schema = null;
        int writePoolId = -1;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            ps = con.prepareStatement("SELECT db_schema,write_db_pool_id FROM context_server2db_pool WHERE cid = ?");
            ps.setInt(1,ctx.getIdAsInt().intValue());
            rs = ps.executeQuery();
            if( ! rs.next() ) {
                throw new SQLException("Unable to determine Database update status");
            }
            schema = rs.getString("db_schema");
            writePoolId = rs.getInt("write_db_pool_id");

            return schemaBeingLockedOrNeedsUpdate(writePoolId, schema);
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);

            closePreparedStatement(ps);

            try {
                cache.pushConfigDBWrite(con);
            } catch (final PoolException e) {
                log.error("Error pushing ox db read connection to pool!", e);
            }

        }
    }

    @Override
    public boolean schemaBeingLockedOrNeedsUpdate(final int writePoolId, final String schema) throws StorageException {
        Updater updater;
        try {
            updater = Updater.getInstance();
            return updater.isLocked(schema, writePoolId) || updater.toUpdate(schema, writePoolId);
        } catch (final UpdateException e) {
            if (e.getDetailNumber() == 102) {
                // no entry found in table, nobody has ever logged into this context
                // this situation is mostly caused when we move contexts between dbms
                log.debug("NO row was found in version table!\nThis is mostly caused when a context is moved between dbms!\nIf this is here not the case, report the error to the admin!",e);
                return false;
            }
            log.error("UpdateCheck Error",e);
            throw new StorageException(e);
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#serverInUse(long)
     */
    public boolean serverInUse(final int server_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context_server2db_pool WHERE server_id = ?", server_id);
    }

    @Override
    public void setUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT bits FROM user_setting_mail WHERE cid = ? AND user = ?");
            stmt.setInt(1, ctx.getIdAsInt());
            stmt.setInt(2, user.getId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                int bits = rs.getInt("bits");
                rs.close();
                stmt.close();
                bits |= bit;
                stmt = con.prepareStatement("UPDATE user_setting_mail SET bits = ? WHERE cid = ? AND user = ?");
                stmt.setInt(1, bits);
                stmt.setInt(2, ctx.getIdAsInt());
                stmt.setInt(3, user.getId());
                stmt.executeUpdate();
            } else {
                throw new SQLException("Unable to set features from bitfield for User: " + user.getId() + ", Context: " + ctx.getIdAsInt());
            }
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            closePreparedStatement(stmt);
        }
    }

    /**
     * @see com.openexchange.admin.storage.interfaces.OXToolStorageInterface#storeInUse(long)
     */
    public boolean storeInUse(final int store_id) throws StorageException {
        return selectwithint(-1, "SELECT cid FROM context WHERE filestore_id = ?", store_id);
    }

    @Override
    public void unsetUserSettingMailBit(final Context ctx, final User user, final int bit, final Connection con) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT bits FROM user_setting_mail WHERE cid = ? AND user = ?");
            stmt.setInt(1, ctx.getIdAsInt());
            stmt.setInt(2, user.getId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                int bits = rs.getInt("bits");
                rs.close();
                stmt.close();
                bits &= ~bit;
                stmt = con.prepareStatement("UPDATE user_setting_mail SET bits = ? WHERE cid = ? AND user = ?");
                stmt.setInt(1, bits);
                stmt.setInt(2, ctx.getIdAsInt());
                stmt.setInt(3, user.getId());
                stmt.executeUpdate();
            } else {
                throw new SQLException("Unable to set features from bitfield for User: " + user.getId() + ", Context: " + ctx.getIdAsInt());
            }
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final SQLException e) {
                    log.error("Error closing resultset", e);
                }
            }
            closePreparedStatement(stmt);
        }
    }

    private void closePreparedStatement(PreparedStatement ps) {
        try {
            if (null != ps) {
                ps.close();
            }
        } catch (final SQLException e) {
            log.error("Error closing prepared statement!", e);
        }
    }

    private void closeRecordSet(ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (final SQLException e) {
                log.error("Error closing resultset", e);
            }
        }
    }

    private int getByNameForConfigDB(final String name, final String objectname, final String SQLQuery) throws StorageException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = cache.getREADConnectionForCONFIGDB();
            stmt = con.prepareStatement(SQLQuery);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("No such " + objectname + " " + name);
            }
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e);
        } finally {
            try {
                rs.close();
            } catch (final SQLException e) {
                log.error("Error closing resultset!", e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (final SQLException e) {
                log.error("Error closing prepared statement!", e);
            }
        }
    }

    private Group[] getDomainUsedbyGroup(Context ctx,String domain,Connection oxcon) throws SQLException{
        // groups are currently not used with mail addresses in the core
        return null;
//        ArrayList<Group> data = new ArrayList<Group>();
//        
//        if(data.size()==0){
//            return null;
//        }else{
//            return data.toArray(new Group[data.size()]);
//        }        
    }

    
    private Resource[] getDomainUsedbyResource(Context ctx,String domain,Connection oxcon) throws SQLException{
        ArrayList<Resource> data = new ArrayList<Resource>();
        
        
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        
        try{
           
            // fetch
            prep_check = oxcon.prepareStatement("SELECT id FROM resource where cid = ? and mail like ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, "%@"+domain);
            rs = prep_check.executeQuery();
            while(rs.next()){                
                data.add(new Resource(rs.getInt("id")));
            }
            rs.close();
            prep_check.close();       
        }finally{
            closeRecordSet(rs);

            closePreparedStatement(prep_check);
        }
        
        if(data.size()==0){
            return null;
        }else{
            return data.toArray(new Resource[data.size()]);
        }        
    }
    
    private User[] getDomainUsedbyUser(Context ctx,String domain,Connection oxcon) throws SQLException{
        ArrayList<User> data = new ArrayList<User>();
        
        
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        
        try{
            HashSet<Integer> usr_ids = new HashSet<Integer>();
            // fetch from alias table
            prep_check = oxcon.prepareStatement("SELECT id FROM user_attribute WHERE cid = ? AND name = 'alias' AND VALUE like ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, "%@"+domain);
            rs = prep_check.executeQuery();
            while(rs.next()){
                usr_ids.add(rs.getInt("id"));
            }
            rs.close();
            prep_check.close();
            // fetch from user table
            prep_check = oxcon.prepareStatement("SELECT id FROM user WHERE cid = ? AND mail like ?");
            prep_check.setInt(1, ctx.getIdAsInt());
            prep_check.setString(2, "%@"+domain);
            rs = prep_check.executeQuery();
            while(rs.next()){
                usr_ids.add(rs.getInt("id"));
            }
            rs.close();
            prep_check.close();
            
            // if we had time we could resolv the complete user object in db but at the moment we only need the ids of the user            
           Iterator<Integer> ids_itr =  usr_ids.iterator();
           while (ids_itr.hasNext()) {
               Integer id  = (Integer) ids_itr.next();
               data.add(new User(id.intValue()));
           }            
        }finally{
            closeRecordSet(rs);

            closePreparedStatement(prep_check);
        }
        
        if(data.size()==0){
            return null;
        }else{
            return data.toArray(new User[data.size()]);
        }        
    }
    
    private void returnConnection(final int ctxid, Connection con) {
        try {
            if (con != null) {
                cache.pushOXDBWrite(ctxid, con);
            }
        } catch (final PoolException e) {
            log.error("Error pushing configdb write connection to pool!", e);
        }
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
    private boolean selectwithint(final int context_id, final String sql_select_string, final int... ins_numbers) throws StorageException {
        boolean retBool = false;
        final AdminCache cache = ClientAdminThread.cache;
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
            for (int element : ins_numbers) {
                prep_check.setInt(sql_counter, element);
                sql_counter++;
            }

            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
                if (context_id != -1) {
                    cache.pushOXDBWrite(context_id, con);
                } else {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException e) {
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
    private boolean selectwithstring(final int context_id, final String sql_select_string, final String... ins_strings) throws StorageException {
        boolean retBool = false;
        final AdminCache cache = ClientAdminThread.cache;
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
            for (String element : ins_strings) {
                prep_check.setString(sql_counter, element);
                sql_counter++;
            }

            rs = prep_check.executeQuery();
            if (rs.next()) {
                retBool = true;
            }
        } catch (final PoolException e) {
            log.error("Pool Error",e);
            throw new StorageException(e);
        } catch (final SQLException e) {
            log.error("SQL Error",e);
            throw new StorageException(e);
        } finally {
            closeRecordSet(rs);
            closePreparedStatement(prep_check);

            try {
                if (context_id != -1) {
                    cache.pushOXDBWrite(context_id, con);
                } else {
                    cache.pushConfigDBWrite(con);
                }
            } catch (final PoolException e) {
                log.error("Error pushing configdb write connection to pool!", e);
            }

        }

        return retBool;
    }
}
