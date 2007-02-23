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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.FilestoreException;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.exceptions.PoolException;

import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.exceptions.OXUtilException;
import com.openexchange.groupware.IDGenerator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OXUtil_MySQL  {
    
    private AdminCache     cache   = null;
    private Log log = LogFactory.getLog(this.getClass());    
    
    private String SQL_QUERY_DELETE_DATABASE = "DELETE FROM db_pool WHERE db_pool_id = ?";
    private String SQL_QUERY_DELETE_SERVER = "DELETE FROM server WHERE server_id = ?";
    private String SQL_QUERY_DELETE_SERVER_FROM_DB_POOL = "DELETE FROM server2db_pool WHERE server_id = ?";
    private String SQL_QUERY_SEARCH_FOR_SERVER = "SELECT name,server_id FROM server WHERE name LIKE ? OR server_id = ?";
    
    private final static String SQL_QUERY_ADD_REASON = "INSERT INTO reason_text (id,text) VALUES(?,?)";
    private final static String SQL_QUERY_DELETE_REASON = "DELETE FROM reason_text WHERE id = ?";
    private final static String SQL_QUERY_GET_REASON = "SELECT text FROM reason_text WHERE id = ?";
    private final static String SQL_QUERY_GET_ALL_REASONS = "SELECT id FROM reason_text";
    
    
    // filestorage
    private final static String SQL_QUERY_REGISTER_STORAGE = "INSERT INTO filestore (id,uri,size,max_context) VALUES (?,?,?,?)";
    private final static String SQL_QUERY_UNREGISTER_STORAGE = "DELETE FROM filestore WHERE id = ?";
    
    public OXUtil_MySQL() {
        try {
            cache   = ClientAdminThread.cache;            
        } catch ( Exception e ) {
            log.error("Error init",e);
        }
    }
    
    public Vector<Object> registerFilestore(String store_URI, long store_size, int store_maxContexts) throws SQLException, PoolException, FilestoreException {
        // moved to new class
        Connection con = null;
        Vector<Object> v = new Vector<Object>();
        Long l_max = new Long( "8796093022208" );
        if ( store_size > l_max.longValue() ) {
            throw new FilestoreException( "Filestore size to large for database (max="+l_max.longValue()+")" ); 
        }
        store_size *= Math.pow(2,20);
        PreparedStatement stmt = null;
        try {
            
            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit( false );            
            int srv_id = IDGenerator.getId(con);
            con.commit();            
            stmt = con.prepareStatement(SQL_QUERY_REGISTER_STORAGE);
            stmt.setInt(1,srv_id);
            stmt.setString(2,store_URI);
            stmt.setLong(3,store_size);
            stmt.setInt(4,store_maxContexts);
            stmt.executeUpdate();
            con.commit();
            
            v.add("OK");
            v.add(new Integer(srv_id));
        } catch (SQLException ecp ) {
            log.error("Error processing registerFilestore",ecp);
            try {
                if(con!=null) {
                    con.rollback();
                }
            } catch ( Exception exp ) {
                log.error("Error processing rollback of ox connection!",exp);
            }
            throw ecp;
        } finally {
            try{
                if(stmt!=null){
                    stmt.close();
                }
            }catch(Exception e){
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(con!=null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch ( Exception exp ) {
                log.error("Error pushing configdb connection to pool!",exp);
            }
        }
        return v;
    }
    
    public Vector<String> changeFilestore( int store_id, Hashtable filestoreData ) throws SQLException, PoolException, FilestoreException {
        // MOVED TO NEW CLASS
        Vector<String> v = new Vector<String>();
        Connection configdb_write_con = null;
        
        PreparedStatement prep = null;
        try{
            configdb_write_con = cache.getWRITEConnectionForCONFIGDB();
            configdb_write_con.setAutoCommit(false);
            
            if ( filestoreData != null && filestoreData.containsKey( I_OXUtil.STORE_URL ) && filestoreData.get( I_OXUtil.STORE_URL ).toString().trim().length() > 0 ) {
                prep = configdb_write_con.prepareStatement( "UPDATE filestore SET uri = ? WHERE id = ?" );
                prep.setString( 1, filestoreData.get( I_OXUtil.STORE_URL ).toString() );
                prep.setInt( 2, store_id );
                prep.executeUpdate();
                prep.close();
            }
            
            if ( filestoreData != null && filestoreData.containsKey( I_OXUtil.STORE_SIZE ) ) {
                Long l_store = (Long)filestoreData.get( I_OXUtil.STORE_SIZE );
                long store_size = l_store.longValue();
                Long l_max = new Long( "8796093022208" );
                if ( store_size > l_max.longValue() ) {
                    throw new FilestoreException( "Filestore size to large for database (max="+l_max.longValue()+")" ); 
                }
                store_size *= Math.pow( 2, 20 );
                prep = configdb_write_con.prepareStatement( "UPDATE filestore SET size = ? WHERE id = ?" );
                prep.setLong( 1, store_size );
                prep.setInt( 2, store_id );
                prep.executeUpdate();
                prep.close();
            }
            
            if ( filestoreData != null && filestoreData.containsKey( I_OXUtil.STORE_MAX_CONTEXT ) ) {
                Integer i_store_maxContexts = (Integer)filestoreData.get( I_OXUtil.STORE_MAX_CONTEXT );
                prep = configdb_write_con.prepareStatement( "UPDATE filestore SET max_context = ? WHERE id = ?" );
                prep.setInt( 1, i_store_maxContexts.intValue() );
                prep.setInt( 2, store_id );
                prep.executeUpdate();
                prep.close();
            }
            
            configdb_write_con.commit();
            
            v.add("OK");
        }catch(SQLException exp){
            log.error("Error processing changeFilestore",exp);
            try{
                if(configdb_write_con!=null && !configdb_write_con.getAutoCommit()){
                    configdb_write_con.rollback();
                }
            }catch(Exception expd){
                log.error("Error processing rollback of configdb connection!",exp);
            }
            throw exp;
        }finally{
            try {
                if(prep!=null){
                    prep.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try{
                if ( configdb_write_con != null ) {
                    cache.pushConfigDBWrite(configdb_write_con);
                }
            }catch(Exception ecp){
                log.error("Error pushing configdb connection to pool!",ecp);
            }
        }
        return v;
    }
    
    public Vector<Object> listFilestores(String search_pattern) throws PoolException, SQLException{
        // MOVED TO NEW CLASS
        Connection con = null;
        PreparedStatement stmt = null;        
        Vector<Object> v = new Vector<Object>();
        search_pattern = search_pattern.replace('*','%');
        
        try {
            con = cache.getREADConnectionForCONFIGDB();
            
            stmt = con.prepareStatement( "SELECT id,uri,size,max_context,COUNT(cid) FROM filestore LEFT JOIN context ON filestore.id = context.filestore_id WHERE uri LIKE ? GROUP BY filestore.id");
            stmt.setString(1,search_pattern);
            ResultSet rs = stmt.executeQuery();
            Hashtable<String, Hashtable<String, Object>> data = new Hashtable<String, Hashtable<String, Object>>();
            while ( rs.next() ) {
                Hashtable<String, Object> store_data = new Hashtable<String, Object>();
                String id = rs.getString("id");
                String uri = rs.getString("uri");
                long size = rs.getLong("size");
                size /= Math.pow(2,20);
                int max_context = rs.getInt("max_context");
                int cur_context = rs.getInt("COUNT(cid)");
                store_data.put(I_OXUtil.STORE_MAX_CONTEXT,max_context);
                store_data.put(I_OXUtil.STORE_CUR_CONTEXT,cur_context);
                store_data.put(I_OXUtil.STORE_SIZE,size);
                store_data.put(I_OXUtil.STORE_URL,uri);
                data.put(id,store_data);
            }
            
            v.add("OK");
            v.add(data);
        } finally {
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(con!=null) {
                    cache.pushConfigDBRead(con);
                }
            } catch ( Exception exp ) {
                log.error("Error pushing configdb connection to pool!",exp);
            }
        }
        return v;
    }
    
    public Vector<String> unregisterFilestore(int store_id) throws SQLException, PoolException{
        // MOVE TO NEW CLASS
        Connection con = null;
        PreparedStatement stmt = null;
        Vector<String> v = new Vector<String>();
        
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit( false );
            stmt = con.prepareStatement( SQL_QUERY_UNREGISTER_STORAGE );
            stmt.setInt( 1, store_id );
            stmt.executeUpdate();
            con.commit();
            
            v.add("OK");
        } catch (SQLException ecp ) {
            log.error("Error processing unregisterFilestore",ecp);
            try {
                if(con!=null) {
                    con.rollback();
                }
            } catch ( Exception exp ) {
                log.error("Error processing rollback of configdb connection!",exp);
            }
            throw ecp;
        } finally {
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(con!=null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch ( Exception exp ) {
                log.error("Error pushing configdb connection to pool!",exp);
            }
        }
        return v;
    }
    
    public Vector<Object> addMaintenanceReason( String reason_txt ) throws SQLException, PoolException {
        // MOVED TO NEW CLASS
        Connection con = null;
        PreparedStatement stmt = null;
        Vector<Object> v = new Vector<Object>();
        
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit( false );            
            int srv_id = IDGenerator.getId(con);
            con.commit();
            stmt = con.prepareStatement( SQL_QUERY_ADD_REASON );
            stmt.setInt( 1, srv_id );
            stmt.setString( 2, reason_txt );
            stmt.executeUpdate();
            con.commit();
            
            v.add("OK");
            v.add(new Integer(srv_id));
        } catch (SQLException ecp ) {
            log.error("Error processing addMaintenanceReason",ecp);
            try {
                if(con!=null) {
                    con.rollback();
                }
            } catch ( Exception exp ) {
                log.error("Error processing rollback of configdb connection!",exp);
            }
            throw ecp;
        } finally {
            
            try {
                if(stmt!=null) {
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            try {
                if(con!=null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch ( Exception exp ) {
                log.error("Error pushing configdb connection to pool!",exp);
            }
        }
        
        return v;
    }
    
    
    public Vector<String> deleteMaintenanceReason( int reason_id ) throws SQLException, PoolException {
        // MOVED TO NEW CLASS
        Connection con = null;
        PreparedStatement stmt = null;
        Vector<String> v = new Vector<String>();
        
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            con.setAutoCommit( false );
            
            stmt = con.prepareStatement( SQL_QUERY_DELETE_REASON );
            stmt.setInt( 1, reason_id );
            stmt.executeUpdate();
            con.commit();
            
            v.add("OK");
        } catch (SQLException ecp ) {
            log.error("Error processing deleteMaintenanceReason",ecp);
            try {
                if(con!=null) {
                    con.rollback();
                }
            } catch ( Exception exp ) {
                log.error("Error processing rollback of configdb connection!",exp);
            }
            throw ecp;
        } finally {
            try {
                if(stmt!=null) {
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(con!=null) {
                    cache.pushConfigDBWrite(con);
                }
            } catch ( Exception exp ) {
                log.error("Error pushing configdb connection to pool!",exp);
            }
        }
        return v;
    }
    
    
    public Vector<String> getMaintenanceReason( int reason_id ) throws SQLException, PoolException {
        Connection con = null;
        PreparedStatement stmt = null;
        Vector<String> v = new Vector<String>();
        
        try {
            con = cache.getREADConnectionForCONFIGDB();
            con.setAutoCommit( false );
            
            stmt = con.prepareStatement( SQL_QUERY_GET_REASON );
            stmt.setInt( 1, reason_id );
            ResultSet rs = stmt.executeQuery();
            String text = "";
            while ( rs.next() ) {
                text = rs.getString( "text" );
            }
            con.commit();
            
            v.add("OK");
            v.add(text);
        } finally {
            try{
                if(stmt!=null) {
                    stmt.close();
                }
            }catch(Exception e){
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(con!=null) {
                    cache.pushConfigDBRead(con);
                }
            } catch ( Exception exp ) {
                log.error("Error pushing configdb connection to pool!",exp);
            }
        }
        
        return v;
    }
    
    
    public Vector<Object> getAllMaintenanceReasons() throws PoolException, SQLException {
        // moved to new class
        Connection con = null;
        PreparedStatement stmt = null;
        Vector<Object> v = new Vector<Object>();
        
        try {
            con = cache.getREADConnectionForCONFIGDB();
            con.setAutoCommit( false );
            
            stmt = con.prepareStatement( SQL_QUERY_GET_ALL_REASONS );
            ResultSet rs = stmt.executeQuery();
            Vector<String> ids = new Vector<String>();
            while ( rs.next() ) {
                ids.add( "" + rs.getInt( 1 ) );
            }
            
            v.add("OK");
            v.add(ids);
        } finally {
            try {
                if(stmt!=null) {
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(con!=null) {
                    cache.pushConfigDBRead(con);
                }
            } catch ( Exception exp ) {
                log.error("Error pushing configdb connection to pool!",exp);
            }
            
        }
        return v;
    }
    
    public Vector<Object> registerDatabase( Hashtable databaseData, boolean isMaster, int master_id ) throws SQLException, PoolException, OXUtilException {
        // MOVED TO NEW CLASS
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep_INSERT = null;
        try {
            retVec.add( "OK" );
            con = cache.getWRITEConnectionForCONFIGDB();
            
            String db_display = null;
            if ( databaseData.containsKey( I_OXUtil.DB_DISPLAY_NAME ) ) {
                db_display = databaseData.get( I_OXUtil.DB_DISPLAY_NAME ).toString();
            }
            
            String db_auth_id = null;
            if ( databaseData.containsKey( I_OXUtil.DB_AUTHENTICATION_ID ) ) {
                db_auth_id = databaseData.get( I_OXUtil.DB_AUTHENTICATION_ID ).toString();
            }
            
            String db_auth_pass = null;
            if ( databaseData.containsKey( I_OXUtil.DB_AUTHENTICATION_PASSWORD ) ) {
                db_auth_pass = databaseData.get( I_OXUtil.DB_AUTHENTICATION_PASSWORD ).toString();
            }
            
            
            String db_driver = null;
            if ( databaseData.containsKey( I_OXUtil.DB_DRIVER ) ) {
                db_driver = databaseData.get( I_OXUtil.DB_DRIVER ).toString();
            }
            
            int db_pool_hardl = 0;
            if ( databaseData.containsKey( I_OXUtil.DB_POOL_HARDLIMIT ) ) {
                db_pool_hardl = ( (Integer)databaseData.get( I_OXUtil.DB_POOL_HARDLIMIT ) ).intValue();
            }
            
            String db_url = null;
            if ( databaseData.containsKey( I_OXUtil.DB_URL ) ) {
                db_url = databaseData.get( I_OXUtil.DB_URL ).toString();
            }
            
            int db_pool_max = 0;
            if ( databaseData.containsKey( I_OXUtil.DB_POOL_MAX ) ) {
                db_pool_max = ( (Integer)databaseData.get( I_OXUtil.DB_POOL_MAX ) ).intValue() ;
            }
            
            int db_pool_init = 0;
            if ( databaseData.containsKey( I_OXUtil.DB_POOL_INIT ) ) {
                db_pool_init = ( (Integer)databaseData.get( I_OXUtil.DB_POOL_INIT ) ).intValue();
            }
            
            int weight = 100;
            if ( databaseData.containsKey( I_OXUtil.DB_CLUSTER_WEIGHT ) ) {
                weight = ( (Integer)databaseData.get( I_OXUtil.DB_CLUSTER_WEIGHT ) ).intValue();
            }
            
            int max_units = -1;
            if ( databaseData.containsKey( I_OXUtil.DB_MAX_UNITS ) ) {
                max_units = ( (Integer)databaseData.get( I_OXUtil.DB_MAX_UNITS ) ).intValue();
            }
            
            con.setAutoCommit( false );            
            int db_id = IDGenerator.getId(con);
            con.commit();
            con.setAutoCommit (true);
            
            prep_INSERT = con.prepareStatement( "INSERT INTO db_pool VALUES (?,?,?,?,?,?,?,?,?);" );
            prep_INSERT.setInt( 1, db_id );
            if ( db_url != null ) {
                prep_INSERT.setString( 2, db_url );
            } else {
                prep_INSERT.setNull( 2, Types.VARCHAR );
            }
            if ( db_driver != null ) {
                prep_INSERT.setString( 3, db_driver );
            } else {
                prep_INSERT.setNull( 3, Types.VARCHAR );
            }
            if ( db_auth_id != null ) {
                prep_INSERT.setString( 4, db_auth_id );
            } else {
                prep_INSERT.setNull( 4, Types.VARCHAR );
            }
            if ( db_auth_pass != null ) {
                prep_INSERT.setString( 5, db_auth_pass );
            } else {
                prep_INSERT.setNull( 5, Types.VARCHAR );
            }
            prep_INSERT.setInt( 6, db_pool_hardl );
            prep_INSERT.setInt( 7, db_pool_max );
            prep_INSERT.setInt( 8, db_pool_init );
            if ( db_display != null ) {
                prep_INSERT.setString( 9, db_display );
            } else {
                prep_INSERT.setNull( 9, Types.VARCHAR );
            }
            
            prep_INSERT.executeUpdate();
            prep_INSERT.close();            
            
            
            if( isMaster ) {
                
                con.setAutoCommit( false );            
                int c_id = IDGenerator.getId(con);
                con.commit();
                con.setAutoCommit (true);
                
                prep_INSERT = con.prepareStatement( "INSERT INTO db_cluster VALUES (?,?,?,?,?);" );
                prep_INSERT.setInt(1, c_id);
                
                // I am the master, set read_db_pool_id = 0
                prep_INSERT.setInt(2, 0);
                prep_INSERT.setInt(3, db_id);
                prep_INSERT.setInt( 4, weight );
                prep_INSERT.setInt( 5, max_units );
                prep_INSERT.executeUpdate();
                prep_INSERT.close();
                
            } else {
                prep_INSERT = con.prepareStatement("SELECT db_pool_id FROM db_pool WHERE db_pool_id = ?");
                prep_INSERT.setInt(1, master_id);
                ResultSet rs = prep_INSERT.executeQuery();
                if( ! rs.next() ){
                    throw new OXUtilException("No such master with ID=" + master_id);
                }
                rs.close();
                prep_INSERT.close();
                
                prep_INSERT = con.prepareStatement("SELECT cluster_id FROM db_cluster WHERE write_db_pool_id = ?");
                prep_INSERT.setInt(1, master_id);
                rs = prep_INSERT.executeQuery();
                if( ! rs.next() ){
                    throw new OXUtilException("No such master with ID=" + master_id + " IN db_cluster TABLE");
                }                
                int cluster_id = rs.getInt("cluster_id");
                rs.close();
                prep_INSERT.close();
                
                prep_INSERT = con.prepareStatement( "UPDATE db_cluster SET read_db_pool_id=? WHERE cluster_id=?;" );
                
                prep_INSERT.setInt(1, db_id);
                prep_INSERT.setInt(2, cluster_id);
                prep_INSERT.executeUpdate();
                prep_INSERT.close();
                
            }
            
            retVec.add( new Integer( db_id ) );
            
        } finally {
            try{
                if(prep_INSERT!=null){
                    prep_INSERT.close();
                }
            }catch(Exception ee){
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,ee);
            }
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    public Vector<String> createDatabase( Hashtable data ) throws SQLException, ClassNotFoundException, OXGenericException {
        // MOVED TO NEW CLASS
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        Statement st = null;
        try {
            
            String sql_user = (String)data.get(I_OXUtil.DB_AUTHENTICATION_ID);
            
            String sql_pass = "";
            if(data.containsKey(I_OXUtil.DB_AUTHENTICATION_PASSWORD)){
                sql_pass =  (String)data.get(I_OXUtil.DB_AUTHENTICATION_PASSWORD);
            }
            String sql_url = (String)data.get(I_OXUtil.DB_URL);
            String sql_scheme_name = (String)data.get(I_OXUtil.DB_SCHEMA);
            String sql_driver = (String)data.get(I_OXUtil.DB_DRIVER);
            con = cache.getSimpleSqlConnection(sql_url,sql_user,sql_pass,sql_driver);
            
            try {
                con.setCatalog(sql_scheme_name);
                // if exists, show error
                throw new Exception("Database \""+sql_scheme_name+"\" already exists");
            } catch(Exception ecp) {
            }
            
            if(con.getAutoCommit()) {
                con.setAutoCommit(false);
            }
            
            // initial create of the "database"
            st = con.createStatement();
            st.addBatch("CREATE DATABASE `"+sql_scheme_name+"` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci");
            st.executeBatch();
            
            pumpData2Database(cache.getOXDBInitialQueries(),"ox initial",con,sql_scheme_name);
            pumpData2Database(cache.getOXDBOptimizeQueries(),"ox optimize",con,sql_scheme_name);
            pumpData2Database(cache.getOXDBConsistencyQueries(),"ox consistency",con,sql_scheme_name);
            
            retVec.add("OK");
            
            
        } catch(SQLException cp) {
            log.error("Error processing createDatabase",cp);
            try{
                if(con!=null && !con.getAutoCommit()){
                    con.rollback();
                }
            }catch(Throwable expd){
                log.error("Error processing rollback of simple connection!",expd);
            }
            throw cp;
        } finally {
            try {
                st.close();
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            cache.closeSimpleConnection(con);
        }
        
        return retVec;
    }
    
    
    private void pumpData2Database(ArrayList db_queries,String ident,Connection con,String database) throws SQLException {
        Statement st = null;
        try {
            con.setCatalog(database);
            st = con.createStatement();
            for(int a = 0;a < db_queries.size();a++) {
                st.addBatch(""+db_queries.get(a));
            }
            st.executeBatch();
            con.commit();
        } catch(SQLException ecp) {
            log.fatal("Error occured processing queries for: \""+ident.toUpperCase()+"\" ",ecp);
            try{
                if(con!=null && !con.getAutoCommit()){
                    con.rollback();
                }
            }catch(Exception expd){
                log.error("Error processing rollback of simple connection!",expd);
            }
            throw ecp;
        }finally{
            try {
                st.close();
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
        }
        
    }
    
    public Vector<String> deleteDatabase( Hashtable data ) throws SQLException, ClassNotFoundException {
        // MOVED TO NEW CLASS
        Vector<String> retVec = new Vector<String>();
        Connection con = null;
        Statement st = null;
        try {
            
            String sql_user = (String)data.get(I_OXUtil.DB_AUTHENTICATION_ID);
            String sql_pass = (String)data.get(I_OXUtil.DB_AUTHENTICATION_PASSWORD);
            String sql_url = (String)data.get(I_OXUtil.DB_URL);
            String sql_scheme_name = (String)data.get(I_OXUtil.DB_SCHEMA);
            String sql_driver = (String)data.get(I_OXUtil.DB_DRIVER);
            con = cache.getSimpleSqlConnection(sql_url,sql_user,sql_pass,sql_driver);
            if(con.getAutoCommit()) {
                con.setAutoCommit(false);
            }
            st = con.createStatement();
            st.addBatch("DROP DATABASE if exists `"+sql_scheme_name+"`");
            st.executeBatch();
            
            con.commit();
            retVec.add("OK");
        } catch(SQLException cp) {
            log.error("Error processing deleteDatabase",cp);
            try {
                con.rollback();
            } catch(Exception ecp) {
                log.error("Error processing rollback of simple connection!",ecp);
            }
            throw cp;
        } finally {
            try {
                if(st!=null){
                    st.close();
                }
            } catch (Exception e) {
                log.error("",e);
            }
            cache.closeSimpleConnection(con);
        }
        
        return retVec;
    }
    
    
    public Vector<Object> registerServer( String serverName ) throws PoolException, SQLException {
        // moved to new class
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep_INSERT = null;
        try {
            retVec.add( "OK" );
            con = cache.getWRITEConnectionForCONFIGDB();
            
            con.setAutoCommit( false );            
            int srv_id = IDGenerator.getId(con);
            con.commit();
            con.setAutoCommit( true );  
            prep_INSERT = con.prepareStatement( "INSERT INTO server VALUES (?,?);" );
            prep_INSERT.setInt( 1, srv_id );
            if ( serverName != null ) {
                prep_INSERT.setString( 2, serverName );
            } else {
                prep_INSERT.setNull( 2, Types.VARCHAR );
            }
            prep_INSERT.executeUpdate();
            prep_INSERT.close();
            
            retVec.add( new Integer( srv_id ) );
            
        } finally {
            try {
                if(prep_INSERT!=null){
                    prep_INSERT.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    public Vector<String> unregisterDatabase(int db_id) throws PoolException, SQLException {
        // moved to new class
        Vector<String> v = new Vector<String>();
        Connection con_write = null;
        PreparedStatement stmt = null;
        try {
            con_write = cache.getWRITEConnectionForCONFIGDB();
            stmt = con_write.prepareStatement("DELETE FROM db_cluster WHERE read_db_pool_id = ? OR write_db_pool_id = ?");
            stmt.setInt(1,db_id);
            stmt.setInt(2,db_id);
            stmt.executeUpdate();
            stmt.close();
            
            stmt = con_write.prepareStatement(SQL_QUERY_DELETE_DATABASE);
            stmt.setInt(1,db_id);
            stmt.executeUpdate();
            stmt.close();
            
            v.add("OK");
            v.add("DATABASE DELETED");
        } finally {
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            if(con_write!=null) {
                try {
                    cache.pushConfigDBWrite(con_write);
                } catch(Exception exp) {
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        
        return v;
    }
    
    public Vector<String> unregisterServer(int server_id) throws PoolException, SQLException {
        // MOVED TO NEW CLASS
        Vector<String> v = new Vector<String>();
        Connection configdb_write = null;
        PreparedStatement stmt = null;
        try {
            configdb_write = cache.getWRITEConnectionForCONFIGDB();
            
            stmt = configdb_write.prepareStatement(SQL_QUERY_DELETE_SERVER_FROM_DB_POOL);
            stmt.setInt(1,server_id);
            stmt.executeUpdate();
            stmt.close();
            
            stmt = configdb_write.prepareStatement(SQL_QUERY_DELETE_SERVER);
            stmt.setInt(1,server_id);
            stmt.executeUpdate();
            stmt.close();
            
            v.add("OK");
            v.add("SERVER DELETED");
        } finally {
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            if(configdb_write!=null) {
                try {
                    cache.pushConfigDBWrite(configdb_write);
                } catch(Exception exp) {
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        
        return v;
    }
    
    public Vector<Object> searchForDatabase(String search_pattern) throws SQLException, OXUtilException, PoolException {
        // MOVED TO NEW CLASS
        Vector<Object> v = new Vector<Object>();
        Connection con_read = null;
        PreparedStatement pstmt = null;
        PreparedStatement cstmt = null;
        try {
            con_read = cache.getREADConnectionForCONFIGDB();
            search_pattern = search_pattern.replace('*','%');
            
            pstmt = con_read.prepareStatement("SELECT db_pool_id,url,driver,login,password,hardlimit,max,initial,name,weight,max_units,read_db_pool_id,write_db_pool_id FROM db_pool,db_cluster WHERE ( db_pool_id = db_cluster.write_db_pool_id OR db_pool_id = db_cluster.read_db_pool_id) AND name LIKE ?");
            pstmt.setString(1,search_pattern);
            //pstmt.setString(2,search_pattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            v.add("OK");
            Hashtable<String, Hashtable<String, Object>> ht = new Hashtable<String, Hashtable<String, Object>>();
            
            
            while(rs.next()) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                Boolean ismaster = Boolean.TRUE;
                int readid  = rs.getInt("read_db_pool_id");
                int writeid = rs.getInt("write_db_pool_id");
                int id      = rs.getInt("db_pool_id");
                int masterid= 0;
                int nrcontexts = 0;
                if( readid == id ) {
                    ismaster = Boolean.FALSE;
                    masterid = writeid;
                } else {
                    // we are master
                    cstmt = con_read.prepareStatement("SELECT COUNT(cid) FROM context_server2db_pool WHERE write_db_pool_id = ?");
                    cstmt.setInt(1, writeid);
                    ResultSet rs1 = cstmt.executeQuery();
                    if( ! rs1.next() ) {
                        throw new OXUtilException("Unable to count contexts");
                    }
                    nrcontexts = Integer.parseInt(rs1.getString("COUNT(cid)"));
                    rs1.close();
                    cstmt.close();
                }
                
                data.put(I_OXUtil.DB_DISPLAY_NAME,rs.getString("name"));
                data.put(I_OXUtil.DB_POOL_ID,rs.getInt("db_pool_id"));
                data.put(I_OXUtil.DB_AUTHENTICATION_ID,rs.getString("login"));
                data.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD,rs.getString("password"));
                data.put(I_OXUtil.DB_URL,rs.getString("url"));
                data.put(I_OXUtil.DB_DRIVER,rs.getString("driver"));
                data.put(I_OXUtil.DB_POOL_HARDLIMIT,rs.getInt("hardlimit"));
                data.put(I_OXUtil.DB_POOL_MAX,rs.getInt("max"));
                data.put(I_OXUtil.DB_POOL_INIT,rs.getInt("initial"));
                data.put(I_OXUtil.DB_CLUSTER_WEIGHT,rs.getInt("weight"));
                data.put(I_OXUtil.DB_MAX_UNITS,rs.getInt("max_units"));
                data.put(I_OXUtil.DB_POOL_IS_MASTER,ismaster.toString());
                data.put(I_OXUtil.DB_POOL_MASTER_ID,masterid);
                data.put(I_OXUtil.DB_CUR_UNITS,nrcontexts);
                
                
                ht.put(rs.getString("db_pool_id"),data);
            }
            v.add(ht);
            rs.close();
        } finally {
            
            try {
                if(cstmt!=null){
                    cstmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try{
                if(pstmt!=null){
                    pstmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            if(con_read!=null) {
                try {
                    cache.pushConfigDBRead(con_read);
                } catch(Exception exp) {
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        
        return v;
    }
    
    public Vector<Object> searchForServer(String search_pattern) throws SQLException, PoolException {
        // MOVED TO NEW CLASS
        Vector<Object> v = new Vector<Object>();
        Connection configdb_read = null;
        PreparedStatement stmt  = null;
        try {
            configdb_read = cache.getREADConnectionForCONFIGDB();
            stmt = configdb_read.prepareStatement(SQL_QUERY_SEARCH_FOR_SERVER);
            search_pattern = search_pattern.replace('*','%');
            stmt.setString(1,search_pattern);
            stmt.setString(2,search_pattern);
            
            ResultSet rs = stmt.executeQuery();
            v.add("OK");
            Hashtable<Integer, Hashtable<String, Object>> ht = new Hashtable<Integer, Hashtable<String, Object>>();
            
            while(rs.next()) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                int sid = rs.getInt("server_id");
                String name = rs.getString("name");
                
                data.put(I_OXUtil.SERVER_NAME,name);
                data.put(I_OXUtil.SERVER_ID,sid);
                ht.put(sid,data);
            }
            v.add(ht);
            rs.close();
        } finally {
            
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            if(configdb_read!=null) {
                try {
                    cache.pushConfigDBRead(configdb_read);
                } catch(Exception exp) {
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
            
        }
        return v;
    }
    
    public Vector<Object> changeDatabase(int database_id, Hashtable databaseData) throws SQLException, PoolException  {
        // MOVED TO NEW CLASS
        Vector<Object> retVec = new Vector<Object>();
        Connection con = null;
        PreparedStatement prep = null;
        
        Hashtable<String,String> MAPPINGS = new Hashtable<String, String>();
        MAPPINGS.put(I_OXUtil.DB_DISPLAY_NAME,"db_pool.name");
        MAPPINGS.put(I_OXUtil.DB_AUTHENTICATION_ID,"db_pool.login");
        MAPPINGS.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD,"db_pool.password");
        MAPPINGS.put(I_OXUtil.DB_DRIVER,"db_pool.driver");
        MAPPINGS.put(I_OXUtil.DB_POOL_HARDLIMIT,"db_pool.hardlimit");
        MAPPINGS.put(I_OXUtil.DB_POOL_INIT,"db_pool.initial");
        MAPPINGS.put(I_OXUtil.DB_POOL_MAX,"db_pool.max");
        MAPPINGS.put(I_OXUtil.DB_URL,"db_pool.url");
        MAPPINGS.put(I_OXUtil.DB_CLUSTER_WEIGHT,"db_cluster.weight");
        MAPPINGS.put(I_OXUtil.DB_MAX_UNITS,"db_cluster.max_units");
        
        try {
            retVec.add( "OK" );
            con = cache.getWRITEConnectionForCONFIGDB();
            
            String query = "UPDATE db_pool,db_cluster SET";
            Enumeration<String> e = MAPPINGS.keys();
            while( e.hasMoreElements() ) {
                String key = e.nextElement();
                String val = MAPPINGS.get(key);
                if( databaseData.containsKey( key ) ) {
                    query += " " + val + "= ?,";
                }
            }
            query = query.substring(0, query.length()-1);
            
            query += " WHERE db_pool.db_pool_id = ? AND ( db_cluster.write_db_pool_id = ? OR db_cluster.read_db_pool_id = ?)";
            prep = con.prepareStatement(query);
            e = MAPPINGS.keys();
            int count = 1;
            while( e.hasMoreElements() ) {
                String key = e.nextElement();
                if( ! databaseData.containsKey(key ) ) {
                	continue;
                }
                if( key.equals(I_OXUtil.DB_DISPLAY_NAME) ||
                        key.equals(I_OXUtil.DB_AUTHENTICATION_ID) ||
                        key.equals(I_OXUtil.DB_AUTHENTICATION_PASSWORD) ||
                        key.equals(I_OXUtil.DB_DRIVER) ||
                        key.equals(I_OXUtil.DB_URL) ) {
                    String val = (String)databaseData.get(key);
                    prep.setString(count, val);
                } else {
                    Integer val = (Integer)databaseData.get(key);
                    prep.setInt(count, val);
                }
                count++;
            }
            prep.setInt(count++, database_id);
            prep.setInt(count++, database_id);
            prep.setInt(count++, database_id);
            
            prep.executeUpdate();
            prep.close();

            
            retVec.add( new Integer( database_id ) );
        } finally {
            try{
                prep.close();
            }catch(Exception ee){
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,ee);
            }
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb connection to pool!",e);
            }
        }
        
        return retVec;
    }
    
    
    
    
    
}
