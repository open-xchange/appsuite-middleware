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
package com.openexchange.admin.dataSource;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.delete.DeleteEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OXGroup_MySQL {
    
    private AdminCache      cache   = null;
    private static Log log = LogFactory.getLog(OXGroup_MySQL.class);
    
    public OXGroup_MySQL() {
        cache = ClientAdminThread.cache;            
    }
    
    
    
    public Vector<Object> createOXGroup( int context_ID, Hashtable groupData ) throws SQLException, PoolException {
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep_insert = null;
        
        try {
            retVec.add( "OK" );
            con = cache.getWRITEConnectionForContext( context_ID );
            con.setAutoCommit(false);
            String identifier = "";
            if ( groupData.containsKey( I_OXGroup.GID ) ) {
                identifier = groupData.get( I_OXGroup.GID ).toString();
            }
            
            String displayName = "";
            if ( groupData.containsKey( I_OXGroup.DISPLAYNAME ) ) {
                displayName = groupData.get( I_OXGroup.DISPLAYNAME ).toString();
            }
            int groupID = IDGenerator.getId(context_ID,com.openexchange.groupware.Types.PRINCIPAL,con);
            con.commit();
            
            prep_insert = con.prepareStatement( "INSERT INTO groups (cid,id,identifier,displayName,lastModified) VALUES (?,?,?,?,?);" );
            prep_insert.setInt( 1, context_ID );
            prep_insert.setInt( 2, groupID );
            prep_insert.setString( 3, identifier );
            prep_insert.setString( 4, displayName );
            prep_insert.setLong( 5, System.currentTimeMillis() );
            prep_insert.executeUpdate();
            con.commit();
            retVec.add(new Integer(groupID));
            log.info("Group "+groupID+" created!");
        }catch(SQLException sql){
            try{
                con.rollback();
            }catch(Exception ec){
                log.error("Error rollback configdb connection",ec);
            }
            throw sql;
        } finally {
            try {
                if(prep_insert!=null){
                    prep_insert.close();
                }
            } catch ( Exception e ) {
                prep_insert = null;
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    
    
    public Vector<Object> listOXGroups( int context_id, String pattern ) throws PoolException, SQLException {
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep_list = null;
        ResultSet rs = null;
        
        try {
            
            if(pattern!=null){
                pattern = pattern.replace('*','%');
            }
            
            con = cache.getREADConnectionForContext( context_id );
            
            prep_list = con.prepareStatement( "SELECT cid,id,identifier,displayName FROM groups WHERE groups.cid = ? AND (identifier like ? OR displayName like ?)" );
            prep_list.setInt(1,context_id);
            prep_list.setString(2,pattern);
            prep_list.setString(3,pattern);
            rs = prep_list.executeQuery();
            Vector<Hashtable<String, Object>> daten = new Vector<Hashtable<String, Object>>();
            while(rs.next()){
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                int cid = rs.getInt("cid");
                int id = rs.getInt("id");
                String ident = rs.getString("identifier");
                String disp = rs.getString("displayName");
                data.put(I_OXGroup.CID,cid);
                data.put(I_OXGroup.GID_NUMBER,id);
                if(disp!=null){
                    data.put(I_OXGroup.DISPLAYNAME,disp);
                }
                if(ident!=null){
                    data.put(I_OXGroup.GID,ident);
                }
                daten.add(data);
            }
            retVec.add("OK");
            retVec.add(daten);
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                log.error("Error closing Resultset!",ex);
            }
            try {
                if(prep_list!=null){
                    prep_list.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBRead(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox read connection to pool!",e);
            }
            
        }
        
        return retVec;
    }
    
    public Vector<Object> getOXGroupData( int context_ID, int group_id ) throws PoolException, SQLException {
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep_list = null;
        
        try {
            
            con = cache.getREADConnectionForContext( context_ID );
            
            prep_list = con.prepareStatement( "SELECT cid,id,identifier,displayName FROM groups WHERE groups.cid = ? AND groups.id = ?" );
            prep_list.setInt( 1, context_ID );
            prep_list.setInt( 2, group_id );
            ResultSet rs = prep_list.executeQuery();
            Hashtable<String, Object> data = new Hashtable<String, Object>();
            while(rs.next()){
                int cid = rs.getInt("cid");
                int id = rs.getInt("id");
                String ident = rs.getString("identifier");
                String disp = rs.getString("displayName");
                data.put(I_OXGroup.CID,cid);
                data.put(I_OXGroup.GID_NUMBER,id);
                data.put(I_OXGroup.DISPLAYNAME,disp);
                data.put(I_OXGroup.GID,ident);
            }
            retVec.add("OK");
            retVec.add(data);
        } finally {
            try {
                if(prep_list!=null){
                    prep_list.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                if(con!=null){
                    cache.pushOXDBRead(context_ID,con);
                }
            } catch ( Exception e ) {
                log.error("Error pushing ox read connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    
    
    
    public Vector<String> changeOXGroup( int context_id, int group_id, Hashtable groupData ) throws PoolException, SQLException {
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        PreparedStatement prep_edit_group = null;
        
        try {
            
            con = cache.getWRITEConnectionForContext( context_id );
            con.setAutoCommit(false);
            
            String identifier = "";
            if ( groupData.containsKey( I_OXGroup.GID ) ) {
                identifier = groupData.get( I_OXGroup.GID ).toString();
                prep_edit_group = con.prepareStatement("UPDATE groups SET identifier=? WHERE cid=? AND id = ?");
                prep_edit_group.setString(1,identifier);
                prep_edit_group.setInt( 2, context_id );
                prep_edit_group.setInt( 3, group_id );
                prep_edit_group.executeUpdate();
                prep_edit_group.close();
            }
            
            String displayName = "";
            if ( groupData.containsKey( I_OXGroup.DISPLAYNAME ) ) {
                displayName = groupData.get( I_OXGroup.DISPLAYNAME ).toString();
                prep_edit_group = con.prepareStatement("UPDATE groups SET displayName=? WHERE cid=? AND id = ?");
                prep_edit_group.setString(1,displayName);
                prep_edit_group.setInt( 2, context_id );
                prep_edit_group.setInt( 3, group_id );
                prep_edit_group.executeUpdate();
                prep_edit_group.close();
            }
            
            // set last modified
            changeLastModifiedOnGroup( context_id, group_id, con );
            
            con.commit();
            
            retVec.add("OK");
            
        } catch ( SQLException e ) {
            log.error("Error processing changeOXGroup",e);
            try{
                if(con!=null){
                    con.rollback();
                }
            }catch(Exception ecp){
                log.error("Error processing rollback of connection!",ecp);
            }
            throw e;
        } finally {
            try {
                if(prep_edit_group!=null){
                    prep_edit_group.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                if(con!=null){
                    cache.pushOXDBWrite(context_id,con);
                }
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    
    
    private static void changeLastModifiedOnGroup( int context_id, int group_id, Connection write_ox_con ) throws SQLException{
        
        PreparedStatement prep_edit_group = null;
        try {
            prep_edit_group = write_ox_con.prepareStatement( "UPDATE groups SET lastModified=? WHERE cid=? AND id=?;" );
            prep_edit_group.setLong( 1, System.currentTimeMillis() );
            prep_edit_group.setInt( 2, context_id );
            prep_edit_group.setInt( 3, group_id );
            prep_edit_group.executeUpdate();
            prep_edit_group.close();
        }finally{
            try {
                if(prep_edit_group!=null){
                    prep_edit_group.close();
                }
            } catch (Exception ee) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,ee);
            }
        }
        
    }
    
    
    
    public Vector<String> addMember( int context_id, int group_id, int[] member_id ) throws PoolException, SQLException {
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        PreparedStatement prep_add_member = null;
        try {
            
            con = cache.getWRITEConnectionForContext( context_id );
            con.setAutoCommit(false);
            
            for(int a = 0;a < member_id.length;a++){
                prep_add_member = con.prepareStatement( "INSERT INTO groups_member VALUES (?,?,?);" );
                prep_add_member.setInt( 1, context_id );
                prep_add_member.setInt( 2, group_id  );
                prep_add_member.setInt( 3, member_id[a]  );
                prep_add_member.executeUpdate();
                prep_add_member.close();
            }
            
            // set last modified on group
            changeLastModifiedOnGroup( context_id, group_id, con );
            // update last modified on user
            for(int b = 0;b < member_id.length;b++){
                OXUser_MySQL.changeLastModifiedOnUser( member_id[b], context_id, con );
            }
            con.commit();
            
            retVec.add("OK");
        }catch(SQLException sql){
            try{
                con.rollback();
            }catch(Exception ecp){
                log.error("Error rollback addmember operation",ecp);
            }
            throw sql;
        } finally {
            try {
                prep_add_member.close();
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                cache.pushOXDBWrite(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool! ",e);
            }
        }
        
        return retVec;
    }
    
    
    
    public Vector<String> removeMember( int context_id, int group_id, int[] member_id ) throws PoolException, SQLException {
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        PreparedStatement prep_del_member = null;
        try {
            
            con = cache.getWRITEConnectionForContext( context_id );
            con.setAutoCommit(false);
            
            for(int a = 0;a < member_id.length;a++){
                prep_del_member = con.prepareStatement( "DELETE FROM groups_member WHERE cid=? AND id=? AND member=?;" );
                prep_del_member.setInt( 1, context_id );
                prep_del_member.setInt( 2, group_id );
                prep_del_member.setInt( 3,  member_id[a] );
                prep_del_member.executeUpdate();
                prep_del_member.close();
            }
            
            // set last modified
            changeLastModifiedOnGroup( context_id, group_id,con );
            for(int b = 0;b < member_id.length;b++){
                if ( AdminDaemonTools.existsUser( context_id, member_id[b] ) ) {
                    // update last modified on user
                    OXUser_MySQL.changeLastModifiedOnUser( member_id[b], context_id, con );
                }
            }
            con.commit();
            
            retVec.add("OK");
            
        }catch(SQLException sql){
            try{
                con.rollback();
            }catch(Exception ecp){
                log.error("Error rollback addmember operation",ecp);
            }
            throw sql;
        } finally {
            try {
                if(prep_del_member!=null){
                    prep_del_member.close();
                }
            } catch (Exception ee) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,ee);
            }
            try {
                cache.pushOXDBWrite(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool! ",e);
            }
        }
        
        return retVec;
    }
    
    
    
    public Vector<String> deleteOXGroup( int context_id, int group_id )
    throws PoolException, DeleteFailedException, SQLException, LdapException, DBPoolingException, ContextException {
        
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        PreparedStatement prep_del_members = null;
        PreparedStatement prep_del_group = null;
        
        try {
            
            con = cache.getWRITEConnectionForContext( context_id );
            con.setAutoCommit(false);
            DeleteEvent delev = new DeleteEvent(this,group_id,DeleteEvent.TYPE_GROUP,context_id);
            AdminCache.delreg.fireDeleteEvent(delev,con,con);
            
            prep_del_members = con.prepareStatement( "DELETE FROM groups_member WHERE cid=? AND id=?" );
            prep_del_members.setInt(1,context_id);
            prep_del_members.setInt(2,group_id);
            prep_del_members.executeUpdate();
            prep_del_members.close();
            
            createRecoveryData(group_id,context_id,con);
            
            prep_del_group = con.prepareStatement( "DELETE FROM groups WHERE cid=? AND id=?" );
            prep_del_group.setInt(1,context_id);
            prep_del_group.setInt(2,group_id);
            prep_del_group.executeUpdate();
            prep_del_group.close();           
            
            con.commit();
            retVec.add( "OK" );
        }catch(SQLException sql){
            try{
                con.rollback();
            }catch(Exception ecp){
                log.error("Error rollback ox db write connection ",ecp);
            }
            throw sql;
        } finally {
            try {
                if(prep_del_members!=null){
                    prep_del_members.close();
                }
            } catch ( Exception e ) {
                prep_del_members = null;
            }
            
            try {
                if(prep_del_group!=null){
                    prep_del_group.close();
                }
            } catch ( Exception e ) {
                prep_del_group = null;
            }
            
            try {
                cache.pushOXDBWrite(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool! ",e);
            }
        }
        
        return retVec;
    }
    
    
    
    public Vector<Object> getMembers( int context_id, int group_id ) throws PoolException, SQLException {
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep_list = null;
        
        try {
            
            con = cache.getREADConnectionForContext( context_id );
            
            prep_list = con.prepareStatement( "SELECT member FROM groups_member WHERE groups_member.cid = ? AND groups_member.id = ?;" );
            prep_list.setInt( 1, context_id );
            prep_list.setInt( 2, group_id );
            ResultSet rs = prep_list.executeQuery();
            Vector<Integer> ids = new Vector<Integer>();
            while(rs.next()){
                ids.add(rs.getInt("member"));
            }
            retVec.add("OK");
            retVec.add(ids);
        } finally {
            try {
                if(prep_list!=null){
                    prep_list.close();
                }
            } catch ( Exception e ) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            try {
                cache.pushOXDBRead(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox read connection to pool! ",e);
            }
        }
        
        return retVec;
    }
    
    private void createRecoveryData(int group_id, int context_id, Connection write_ox_con) throws SQLException {             
        PreparedStatement del_st = null;
        ResultSet rs = null;
        try{           
            
            del_st = write_ox_con.prepareStatement(
                    "SELECT " +
                    "identifier,displayName " +
                    "FROM groups " +
                    "WHERE " +
                    "id = ? " +
                    "AND " +
                    "cid = ?");
            del_st.setInt(1,group_id);
            del_st.setInt(2,context_id);
            rs = del_st.executeQuery();
            String ident = null;
            String disp = null; 
            
            if(rs.next()){
                ident = rs.getString("identifier");
                disp = rs.getString("displayName");                
                
            }
            del_st.close();
            rs.close();
            
            del_st = write_ox_con.prepareStatement("" +
                    "INSERT " +
                    "into del_groups " +
                    "(id,cid,lastModified,identifier,displayName) " +
                    "VALUES " +
                    "(?,?,?,?,?)");
            del_st.setInt(1,group_id);
            del_st.setInt(2,context_id);
            del_st.setLong(3,System.currentTimeMillis());
            del_st.setString(4,ident);
            del_st.setString(5,disp);            
            del_st.executeUpdate();
            del_st.close();
            
        }finally{
            try {
                if(del_st!=null){
                    del_st.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
        }
    }
    
    public static void deleteRecoveryData(int group_id, int context_id, Connection con) throws SQLException {
        // delete from del_groups table
        PreparedStatement del_st = null;
        try{
            del_st = con.prepareStatement("" +
                    "DELETE " +
                    "from del_groups " +
                    "WHERE " +
                    "id = ? " +
                    "AND " +
                    "cid = ?");
            del_st.setInt(1,group_id);
            del_st.setInt(2,context_id);            
            del_st.executeUpdate();
        }finally{
            try {
                if(del_st!=null){
                    del_st.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
        }
    }
    
    public static void deleteAllRecoveryData(int context_id, Connection con) throws SQLException {
        // delete from del_groups table
        PreparedStatement del_st = null;
        try{
            del_st = con.prepareStatement("" +
                    "DELETE " +
                    "from del_groups " +
                    "WHERE " +
                    "cid = ?");            
            del_st.setInt(1,context_id);            
            del_st.executeUpdate();
        }finally{
            try {
                if(del_st!=null){
                    del_st.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
        }
    }
}
