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
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;
import java.util.Vector;

import com.openexchange.admin.exceptions.OXResourceException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.delete.DeleteEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OXResource_MySQL  {
    
    private AdminCache      cache   = null;
    private static Log log = LogFactory.getLog(OXResource_MySQL.class);
    
    
    public OXResource_MySQL() {
        try {
            cache   = ClientAdminThread.cache;
        } catch ( Exception e ) {
            log.error("Error init",e);
        }
    }
    
    
    
    public Vector<Object> createOXResource( int context_ID, Hashtable resData ) throws SQLException, PoolException {
        Vector<Object> retVec = new Vector<Object>();
        
        Connection con = null;
        PreparedStatement prep_insert = null;
        
        try {
            
            
            con = cache.getWRITEConnectionForContext( context_ID );
            con.setAutoCommit(false);
            
            String identifier = null;
            if ( resData.containsKey( I_OXResource.RID ) ) {
                identifier = resData.get( I_OXResource.RID ).toString();
            }
            
            String displayName = null;
            if ( resData.containsKey( I_OXResource.DISPLAYNAME ) ) {
                displayName = resData.get( I_OXResource.DISPLAYNAME ).toString();
            }
            
            int available = 1;
            if ( resData.containsKey( I_OXResource.AVAILABLE ) ) {
                Boolean b = (Boolean)resData.get( I_OXResource.AVAILABLE );
                if ( b.booleanValue() ) {
                    available = 1;
                } else {
                    available = 0;
                }
            }
            
            String description = null;
            if ( resData.containsKey( I_OXResource.DESCRIPTION ) ) {
                description = resData.get( I_OXResource.DESCRIPTION ).toString();
            }
            
            String mail = null;
            if ( resData.containsKey( I_OXResource.PRIMARY_MAIL ) ) {
                mail = resData.get( I_OXResource.PRIMARY_MAIL ).toString();
            }
            
            
            
            int resID = IDGenerator.getId(context_ID,com.openexchange.groupware.Types.PRINCIPAL,con);
            con.commit();
            
            
            prep_insert = con.prepareStatement( "INSERT INTO resource (cid,id,identifier,displayName,available,description,lastModified,mail)VALUES (?,?,?,?,?,?,?,?);" );
            prep_insert.setInt( 1, context_ID );
            prep_insert.setInt( 2, resID);
            if ( identifier != null ) {
                prep_insert.setString( 3, identifier );
            } else {
                prep_insert.setNull( 3, Types.VARCHAR );
            }
            if ( displayName != null ) {
                prep_insert.setString( 4, displayName );
            } else {
                prep_insert.setNull( 4, Types.VARCHAR );
            }
            prep_insert.setInt( 5, available );
            if ( description != null ) {
                prep_insert.setString( 6, description );
            } else {
                prep_insert.setNull( 6, Types.VARCHAR );
            }
            prep_insert.setLong( 7, System.currentTimeMillis() );
            if ( mail != null ) {
                prep_insert.setString( 8, mail );
            } else {
                prep_insert.setNull( 8, Types.VARCHAR );
            }
            
            prep_insert.executeUpdate();
            
            con.commit();
            retVec.add( "OK" );
            retVec.add(resID);
        }catch(SQLException e ){
            log.error("Error processing createOXResource",e);
            try {
                con.rollback();
            } catch ( Exception exp ) {
                log.error("Error processing rollback of ox connection!",exp);
            }
            throw e;
        } finally {
            try{
                if(prep_insert!=null){
                    prep_insert.close();
                }
            }catch(Exception e){
                log.error("Error closing statemtent!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    
    
    public Vector<Object> listOXResources( int context_id, String pattern ) throws PoolException, SQLException {
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement prep_list = null;
        pattern = pattern.replace('*','%');
        try {
            
            con = cache.getREADConnectionForContext( context_id );
            
            prep_list = con.prepareStatement("SELECT resource.mail,resource.cid,resource.id,resource.identifier,resource.displayName,resource.available,resource.description FROM resource WHERE resource.cid = ? AND (resource.identifier like ? OR resource.displayName = ?)");
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
                String mail = rs.getString("mail");
                String disp = rs.getString("displayName");
                Boolean aval = rs.getBoolean("available");
                String desc = rs.getString("description");
                data.put(I_OXContext.CONTEXT_ID,cid);
                data.put(I_OXResource.RID_NUMBER,id);
                if(mail!=null){
                    data.put(I_OXResource.PRIMARY_MAIL,mail);
                }
                if(disp!=null){
                    data.put(I_OXResource.DISPLAYNAME,disp);
                }
                
                data.put(I_OXResource.RID,ident);
                if(desc!=null){
                    data.put(I_OXResource.DESCRIPTION,desc);
                }
                if(aval!=null){
                    data.put(I_OXResource.AVAILABLE,aval);
                }
                daten.add(data);
            }
            retVec.add( "OK" );
            retVec.add( daten );
        } finally {
            
            try {
                if(rs!=null){
                    rs.close();
                }
                
            } catch (Exception ex) {
                log.error("Error closing ResultSet",ex);
            }
            
            try {
                if(prep_list!=null){
                    prep_list.close();
                }
            } catch (SQLException ex) {
                log.error("Error closing PreparedStatement",ex);
            }
            try {
                cache.pushOXDBRead(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox read connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    
    
    public Vector<String> changeOXResource( int context_id, int resource_id, Hashtable data ) throws SQLException, PoolException {
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        PreparedStatement editres = null;
        
        try {
            
            con = cache.getWRITEConnectionForContext( context_id );
            con.setAutoCommit(false);
            
            int edited_the_resource = 0;
            
            // update status of resource availability
            if(data.containsKey(I_OXResource.AVAILABLE)){
                editres = con.prepareStatement("UPDATE resource SET available = ? WHERE cid = ? AND id = ?");
                try{
                    Boolean b = (Boolean)data.get(I_OXResource.AVAILABLE);
                    if(b.booleanValue()){
                        editres.setInt(1,1);
                    }else{
                        editres.setInt(1,0);
                    }
                    editres.setInt(2,context_id);
                    editres.setInt(3,resource_id);
                    editres.executeUpdate();
                    editres.close();
                    edited_the_resource++;
                }catch(Exception exp){
                    log.debug("Error in data (available)",exp);
                }
                
            }
            
            // update description of resource
            if(data.containsKey(I_OXResource.DESCRIPTION)){
                editres = con.prepareStatement("UPDATE resource SET description = ? WHERE cid = ? AND id = ?");
                editres.setString(1,""+data.get(I_OXResource.DESCRIPTION));
                editres.setInt(2,context_id);
                editres.setInt(3,resource_id);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }
            
            // update mail of resource
            if(data.containsKey(I_OXResource.PRIMARY_MAIL)){
                editres = con.prepareStatement("UPDATE resource SET mail = ? WHERE cid = ? AND id = ?");
                editres.setString(1,""+data.get(I_OXResource.PRIMARY_MAIL));
                editres.setInt(2,context_id);
                editres.setInt(3,resource_id);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }
            
            // update displayName of resource
            if(data.containsKey(I_OXResource.DISPLAYNAME)){
                editres = con.prepareStatement("UPDATE resource SET displayName = ? WHERE cid = ? AND id = ?");
                editres.setString(1,""+data.get(I_OXResource.DISPLAYNAME));
                editres.setInt(2,context_id);
                editres.setInt(3,resource_id);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }
            
            // update displayName of resource
            if(data.containsKey(I_OXResource.RID)){
                editres = con.prepareStatement("UPDATE resource SET identifier = ? WHERE cid = ? AND id = ?");
                editres.setString(1,""+data.get(I_OXResource.RID));
                editres.setInt(2,context_id);
                editres.setInt(3,resource_id);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }
            
            if(edited_the_resource>0){
                // update modifed
                changeLastModifiedOnResource(resource_id,context_id,con);
            }
            con.commit();
            retVec.add( "OK" );
        } catch (SQLException e ) {
            log.error("Error processing changeOXResource",e);
            try{
                con.rollback();
            }catch(Exception e2){
                log.error("Error rollback",e2);
            }
            throw e;
        } finally {
            try {
                if(editres!=null){
                    editres.close();
                }
            } catch (SQLException ex) {
                log.error("Error closing PreparedStatement",ex);
            }
            try {
                cache.pushOXDBWrite(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool!",e);
            }
            
        }
        
        return retVec;
    }
    
    public static void changeLastModifiedOnResource(int resource_id,int context_id,Connection write_ox_con) throws SQLException {
        PreparedStatement prep_edit_user = null;
        try{
            prep_edit_user = write_ox_con.prepareStatement("UPDATE resource SET lastModified=? WHERE cid=? AND id=?");
            prep_edit_user.setLong( 1, System.currentTimeMillis() );
            prep_edit_user.setInt(2,context_id);
            prep_edit_user.setInt(3,resource_id);
            prep_edit_user.executeUpdate();
            prep_edit_user.close();
        }finally{
            try{
                prep_edit_user.close();
            }catch(Exception e){
                log.error("Error closing statement!",e);
            }
        }
    }
    
    
    public Vector<String> deleteOXResource( int context_id, int resource_ID )
    throws PoolException, ContextException, DeleteFailedException, LdapException, SQLException, DBPoolingException {
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        PreparedStatement prep_del = null;
        try {
            
            con = cache.getWRITEConnectionForContext( context_id );
            con.setAutoCommit(false);
            
            DeleteEvent delev = new DeleteEvent(this,resource_ID,DeleteEvent.TYPE_RESOURCE,context_id);
            AdminCache.delreg.fireDeleteEvent(delev,con,con);
            
            createRecoveryData(resource_ID,context_id,con);
            
            prep_del = con.prepareStatement( "DELETE FROM resource WHERE cid=? AND id=?;" );
            prep_del.setInt( 1, context_id);
            prep_del.setInt( 2,  resource_ID  );
            prep_del.executeUpdate();            
            
            con.commit();
            retVec.add( "OK" );
        } catch (SQLException e ) {
            log.error("Error processing deleteOXResource",e);
            try{
                con.rollback();
            }catch(Exception e2){
                log.error("Error rollback ox db write connection",e2);
            }
            throw e;
        } finally {
            try {
                if(prep_del!=null){
                    prep_del.close();
                }
            } catch (SQLException ex) {
                log.error("Error closing  PreparedStatement",ex);
            }
            try {
                
                cache.pushOXDBWrite(context_id,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox write connection to pool!",e);
            }
            
        }
        
        return retVec;
    }
    
    
    
    public Vector<Object> getOXResourceData( int context_id, int resource_ID ) throws SQLException, PoolException {
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep_list = null;
        
        try {
            
            
            con = cache.getREADConnectionForContext( context_id );
            
            prep_list = con.prepareStatement( "SELECT cid,id,identifier,displayName,available,description,mail FROM resource WHERE resource.cid = ? AND resource.id = ?" );
            prep_list.setInt( 1, context_id );
            prep_list.setInt( 2, resource_ID );
            ResultSet rs = prep_list.executeQuery();
            Hashtable<String, Object> data = new Hashtable<String, Object>();
            if(rs.next()){
                int cid = rs.getInt("cid");
                int id = rs.getInt("id");
                String ident = rs.getString("identifier");
                String mail = rs.getString("mail");
                String disp = rs.getString("displayName");
                Boolean aval = rs.getBoolean("available");
                String desc = rs.getString("description");
                data.put(I_OXContext.CONTEXT_ID,cid);
                data.put(I_OXResource.RID_NUMBER,id);
                if(mail!=null){
                    data.put(I_OXResource.PRIMARY_MAIL,mail);
                }
                if(disp!=null){
                    data.put(I_OXResource.DISPLAYNAME,disp);
                }
                
                data.put(I_OXResource.RID,ident);
                if(desc!=null){
                    data.put(I_OXResource.DESCRIPTION,desc);
                }
                if(aval!=null){
                    data.put(I_OXResource.AVAILABLE,aval);
                }
                retVec.add("OK");
                retVec.add(data);
            }else{
                retVec.add("ERROR");
                retVec.add(OXResourceException.NO_SUCH_RESOURCE);
            }
        } finally {
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
    
    public static void createRecoveryData(int resource_id, int context_id, Connection con) throws SQLException {
        // insert into del_resource table
        PreparedStatement del_st = null;
        ResultSet rs = null;
        try{
            
            del_st = con.prepareStatement(
                    "SELECT " +
                    "identifier,displayName,mail,description,available " +
                    "FROM resource " +
                    "WHERE " +
                    "id = ? " +
                    "AND " +
                    "cid = ?");
            del_st.setInt(1,resource_id);
            del_st.setInt(2,context_id);
            rs = del_st.executeQuery();
            String ident = null;
            String disp = null;
            String mail = null;
            String desc = null;
            int available = -1;
            if(rs.next()){
                ident = rs.getString("identifier");
                disp = rs.getString("displayName");
                mail = rs.getString("mail");
                desc = rs.getString("description");
                available = rs.getInt("available");
            }
            del_st.close();
            rs.close();
            
            
            del_st = con.prepareStatement("" +
                    "INSERT " +
                    "into del_resource " +
                    "(id,cid,lastModified,identifier,mail,description,displayName,available) " +
                    "VALUES " +
                    "(?,?,?,?,?,?,?,?)");
            del_st.setInt(1,resource_id);
            del_st.setInt(2,context_id);
            del_st.setLong(3,System.currentTimeMillis());
            del_st.setString(4,ident);
            del_st.setString(5,mail);
            del_st.setString(6,desc);
            del_st.setString(7,disp);
            del_st.setInt(8,available);
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
    
    public static void deleteRecoveryData(int resource_id, int context_id, Connection con) throws SQLException {
        // delete from del_resource table
        PreparedStatement del_st = null;
        try{
            del_st = con.prepareStatement("" +
                    "DELETE " +
                    "from del_resource " +
                    "WHERE " +
                    "id = ? " +
                    "AND " +
                    "cid = ?");
            del_st.setInt(1,resource_id);
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
        // delete from del_resource table
        PreparedStatement del_st = null;
        try{
            del_st = con.prepareStatement("" +
                    "DELETE " +
                    "from del_resource " +
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
