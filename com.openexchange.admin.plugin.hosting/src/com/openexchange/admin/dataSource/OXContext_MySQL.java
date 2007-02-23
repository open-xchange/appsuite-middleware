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

import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.dataSource.impl.OXUser;
import com.openexchange.admin.dataSource.impl.OXUtil;
import com.openexchange.admin.exceptions.DatabaseContextMappingException;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.exceptions.QuotaException;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.exceptions.UserException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.admin.tools.database.TableColumnObject;
import com.openexchange.admin.tools.database.TableObject;
import com.openexchange.admin.tools.database.TableRowObject;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Enumeration;

import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.database.DataFetcher;
import com.openexchange.admin.tools.database.DataFetcherMysql;
import com.openexchange.server.DBPoolingException;
import com.openexchange.tools.oxfolder.OXFolderAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OXContext_MySQL {
    
    private AdminCacheExtended     cache   = null;
    private static Log log = LogFactory.getLog(OXContext_MySQL.class);
    private PropertyHandler prop    = null;
    
    private static final int UNIT_CONTEXT = 1;
    private static final int UNIT_USER    = 2;
    public static final String LOG_ERROR_CLOSING_STATEMENT = "Error closing statement";
    private int CONTEXTS_PER_SCHEMA = 1;
    private int USE_UNIT = UNIT_CONTEXT;
    
    
    public Vector<Object> changeDatabaseContext(int context_id,Hashtable new_databasehandle) throws RemoteException{
        throw new RemoteException("Not implemented");
    }
    
    public OXContext_MySQL() {
        try {
            cache = ClientAdminThreadExtended.cache;
            prop    = cache.getProperties();
            
            this.CONTEXTS_PER_SCHEMA = Integer.parseInt(prop.getProp("CONTEXTS_PER_SCHEMA","1"));
            if( CONTEXTS_PER_SCHEMA <=0 ){
                throw new OXContextException("CONTEXTS_PER_SCHEMA MUST BE > 0");
            }
            
            String unit = prop.getProp("CREATE_CONTEXT_USE_UNIT","context");
            if( unit.trim().toLowerCase().equals("context") ) {
                this.USE_UNIT = UNIT_CONTEXT;
            } else if( unit.trim().toLowerCase().equals("user") ) {
                this.USE_UNIT = UNIT_USER;
            } else {
                this.USE_UNIT = UNIT_CONTEXT;
                log.warn("unknown unit " + unit + ", using context");
            }
        } catch ( Exception e ) {
            log.error("Error init",e);
        }
    }
    
// FIXME
//    private void printTables(Vector tablelist) {
//        final StringBuilder sb = new StringBuilder();
//        for(int a = 0;a<tablelist.size();a++){
//            TableObject to = (TableObject)tablelist.get(a);
//            sb.append("Table: ");
//            sb.append(to.getName());
//            sb.append(", references: ");
//            Iterator iter = to.getCrossReferenceTables();
//            while (iter.hasNext()) {
//                sb.append(iter.next());
//                sb.append(',');
//            }
//            sb.deleteCharAt(sb.length() - 1);
//            sb.append(", referenced by: ");
//            iter = to.getReferencedByTables();
//            while (iter.hasNext()) {
//                sb.append(iter.next());
//                sb.append(',');
//            }
//            sb.deleteCharAt(sb.length() - 1);
//            sb.append('\n');
//        }
//        log.error(sb.toString());
//    }
    
    private Hashtable<String, String> getDatabaseHandleById(final int database_id, final Connection configdb_write) throws SQLException{
        Hashtable<String, String> ht = new Hashtable<String, String>();
        PreparedStatement pstm = null;
        try{
            pstm = configdb_write.prepareStatement("SELECT url,driver,login,password,name FROM db_pool WHERE db_pool_id = ?");
            pstm.setInt(1,database_id);
            ResultSet rs = pstm.executeQuery();
            while(rs.next()){
                ht.put(I_OXUtil.DB_AUTHENTICATION_ID, rs.getString("login"));
                ht.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD, rs.getString("password"));
                ht.put(I_OXUtil.DB_DRIVER, rs.getString("driver"));
                ht.put(I_OXUtil.DB_URL, rs.getString("url"));
                ht.put("SELECTED_SERVER", rs.getString("name"));
            }
            rs.close();
        }finally{
            try {
                if(pstm!=null){
                    pstm.close();
                }
            } catch (SQLException e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
        return ht;
    }
    
    private void updateContextServer2DbPool(final Hashtable db_handle, final Connection configdb_write_con, final int target_database_id, final int context_id) throws SQLException{
        PreparedStatement pstm = null;
        try{
            pstm = configdb_write_con.prepareStatement(
                    "UPDATE " +
                    "context_server2db_pool " +
                    "SET " +
                    "read_db_pool_id = ?," +
                    "write_db_pool_id = ?," +
                    "db_schema = ? " +
                    "WHERE " +
                    "cid = ?");
            pstm.setInt(1,target_database_id);
            pstm.setInt(2,target_database_id);
            pstm.setString(3,(String)db_handle.get(I_OXUtil.DB_SCHEMA));
            pstm.setInt(4,context_id);
            pstm.executeUpdate();
        }finally{
            try {
                if(pstm!=null){
                    pstm.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
    }
    
    private void createDatabaseAndMappingForContext(final Hashtable<String, String> db_handle, final Connection configdb_write_con, final int target_database_id, final int context_id)
    throws RemoteException,SQLException, PoolException{
        
        OXUtil oxu = new OXUtil();
        if( CONTEXTS_PER_SCHEMA == 1 ) {
            
            String schema_name;
            synchronized (ClientAdminThreadExtended.create_mutex) {
                configdb_write_con.setAutoCommit(false);
                int srv_id = IDGenerator.getId (configdb_write_con);
                configdb_write_con.commit();
                schema_name = (String)db_handle.get("SELECTED_SERVER") + "_" + srv_id;
            }
            db_handle.put(I_OXUtil.DB_SCHEMA, schema_name);
            Vector ret = oxu.createDatabase(db_handle);
            if( ! ret.get(0).equals("OK")){
                throw new SQLException(""+ret.get(1));
            }
            // update contextserver2dbpool table with new infos
            updateContextServer2DbPool(db_handle,configdb_write_con,target_database_id,context_id);
        } else {
            // check if there's a db schema which is not yet full
            synchronized (ClientAdminThreadExtended.create_mutex) {
                String schema_name = getNextUnfilledSchemaFromDB(target_database_id,configdb_write_con);
                // there's none? create one
                if(schema_name == null){
                    configdb_write_con.setAutoCommit(false);
                    int srv_id = IDGenerator.getId (configdb_write_con);
                    configdb_write_con.commit();
                    schema_name = (String)db_handle.get("SELECTED_SERVER") + "_" + srv_id;
                    db_handle.put(I_OXUtil.DB_SCHEMA, schema_name);
                    Vector ret = oxu.createDatabase(db_handle);
                    if(!ret.get(0).equals("OK")){
                        throw new SQLException(""+ret.get(1));
                    }
                    // update contextserver2dbpool table with new infos
                    updateContextServer2DbPool(db_handle,configdb_write_con,target_database_id,context_id);
                } else {
                    db_handle.put(I_OXUtil.DB_SCHEMA,schema_name);
                    // update contextserver2dbpool table with new infos
                    updateContextServer2DbPool(db_handle,configdb_write_con,target_database_id,context_id);
                }
            }
        }
        
    }
    
    private TableObject backupContextServer2DBPoolEntry(final int context_id, final Connection configdb_write_con) throws SQLException {
        TableObject ret = new TableObject();
        PreparedStatement pstm = null;
        
        try{
            ret.setName("context_server2db_pool");
            pstm = configdb_write_con.prepareStatement("SELECT server_id,cid,read_db_pool_id,write_db_pool_id,db_schema FROM context_server2db_pool where cid = ?");
            pstm.setInt(1,context_id);
            ResultSet rs = pstm.executeQuery();
            while(rs.next()){
                TableRowObject tro = new TableRowObject();
                
                TableColumnObject tco = new TableColumnObject();
                Object srv_id = rs.getObject("server_id");
                tco.setData(srv_id);
                tco.setName("server_id");
                tco.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco);
                
                TableColumnObject tco2 = new TableColumnObject();
                Object cid = rs.getObject("cid");
                tco2.setData(cid);
                tco2.setName("cid");
                tco2.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco2);
                
                TableColumnObject tco3 = new TableColumnObject();
                Object obj = rs.getObject("read_db_pool_id");
                tco3.setData(obj);
                tco3.setName("read_db_pool_id");
                tco3.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco3);
                
                TableColumnObject tco4 = new TableColumnObject();
                Object obj2 = rs.getObject("write_db_pool_id");
                tco4.setData(obj2);
                tco4.setName("write_db_pool_id");
                tco4.setType(java.sql.Types.INTEGER);
                tro.setColumn(tco4);
                
                TableColumnObject tco5 = new TableColumnObject();
                Object obj3 = rs.getObject("db_schema");
                tco5.setData(obj3);
                tco5.setName("db_schema");
                tco5.setType(java.sql.Types.VARCHAR);
                tro.setColumn(tco5);
                
                ret.setDataRow(tro);
            }
            rs.close();
        }finally{
            try {
                if(pstm!=null){
                    pstm.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
        return ret;
    }
    
    private void fillTargetDatabase(final DataFetcher dbfetch, final Vector<TableObject> sorted_tables, final Connection target_ox_db_con)
    throws PoolException, SQLException {
        
        
        // do the inserts for all tables!
        for(int a = 0;a<sorted_tables.size();a++){
            TableObject to =  sorted_tables.get(a);
            to = dbfetch.getDataForTable(to);
            if(to.getDataRowCount()>0){
                // ok data in table found, copy to db
                
                for(int i = 0;i<to.getDataRowCount();i++){
                    
                    StringBuilder prep_sql = new StringBuilder();
                    StringBuilder sb_values = new StringBuilder();
                    
                    prep_sql.append("INSERT INTO "+to.getName()+" ");
                    prep_sql.append("(");
                    sb_values.append("(");
                    
                    TableRowObject tro = to.getDataRow(i);
                    Enumeration enumi = tro.getColumnNames();
                    
                    // Save the order of the columns in this list, that all values are correct mapped to their fields
                    // for later use in prepared_statement
                    List<String> columns_list = new ArrayList<String>();
                    
                    while(enumi.hasMoreElements()){
                        String column = (String)enumi.nextElement();
                        columns_list.add(column);
                        prep_sql.append(""+column+",");
                        sb_values.append("?,");
                    }
                    
                    // set up the sql query for the prep statement
                    prep_sql.deleteCharAt(prep_sql.length()-1);
                    sb_values.deleteCharAt(sb_values.length()-1);
                    prep_sql.append(") ");
                    sb_values.append(") ");
                    prep_sql.append(" VALUES ");
                    prep_sql.append(sb_values.toString());
                    
                    
                    // now create the statements for each row
                    PreparedStatement prep_ins = null;
                    try{
                        prep_ins = target_ox_db_con.prepareStatement(prep_sql.toString());
                        enumi = tro.getColumnNames();
                        int ins_pos = 1;
                        for(int c = 0;c<columns_list.size();c++){
                            TableColumnObject tco = tro.getColumn(columns_list.get(c));
                            prep_ins.setObject(ins_pos,tco.getData(),tco.getType());
                            ins_pos++;
                        }
                        prep_ins.executeUpdate();
                        prep_ins.close();
                    }finally{
                        try {
                            if(prep_ins!=null){
                                prep_ins.close();
                            }
                        } catch (Exception e) {
                            log.error(LOG_ERROR_CLOSING_STATEMENT,e);
                        }
                    }
                    // }// end of test table
                }// end of datarow loop
                
            }// end of if table has data
            to = null;
        }// end of table loop
        
        
        
        
        
    }
    
    public Vector<String> moveDatabaseContext(final int context_id, final int target_database_id, final int reason_id)
    throws SQLException, PoolException, TargetDatabaseException, DatabaseContextMappingException{
        
        long start = 0;
        long end = 0;
        if(log.isDebugEnabled ()){
            start = System.currentTimeMillis ();
        }
        log.debug("Move of data for context "+context_id+" is now starting to target database "+target_database_id+"!");
        
        Vector<String> retval = new Vector<String>();
        
        Connection ox_db_write_con = null;
        Connection configdb_write_con = null;
        
        PreparedStatement stm = null;
        
        Connection target_ox_db_con = null;
        
        TableObject contextserver2dbpool_backup = null;
        int source_database_id = -1;
        try {
            
            configdb_write_con = cache.getWRITEConnectionForCONFIGDB();
            //ox_db_write_con = cache.getWRITEConnectionForContext(context_id);
            source_database_id = cache.getDBPoolIdForContextId(context_id);
            String scheme = cache.getSchemeForContextId(context_id);
            
            ox_db_write_con = cache.getWRITEConnectionForPoolId(source_database_id,scheme);
            
            /* 1.
             * Lock the context if not already locked. if already locked, throw exception
             * cause the context could be already in progress for moving.
             */
            log.debug("Context "+context_id+" will now be disabled for moving!");
            disableContext(context_id,reason_id);
            log.debug("Context "+context_id+" is now disabled!");
            
            DataFetcher dbfetch = new DataFetcherMysql();
            dbfetch.setDbConnection(ox_db_write_con,ox_db_write_con.getCatalog());
            dbfetch.setMatchingColumn("cid");
            dbfetch.setColumnMatchObject(context_id,Types.INTEGER);
            
            /* 2.
             * Fetch tables with cid column which could perhaps store data relevant for us
             */
            log.debug("Fetching table structure from database scheme!");
            dbfetch.fetchTableObjects();
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Table structure fetched!");
            
            // this must sort the tables by references (foreign keys)
            log.debug("Try to find foreign key dependencies between tables and sort table!");
            Vector<TableObject> sorted_tables = dbfetch.sortTableObjects();
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Dependencies found and tables sorted!");
            
            // fetch data for db handle to create database
            log.debug("Get database handle information for target database system!");
            Hashtable<String, String> db_handle = getDatabaseHandleById(target_database_id,configdb_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Database handle information found!");
            
            // backup old mapping in contextserver2dbpool for recovery if something breaks
            log.debug("Backing up current configdb entries for context "+context_id);
            contextserver2dbpool_backup = backupContextServer2DBPoolEntry(context_id,configdb_write_con);
            // ####### ##### geht hier was kaputt -> enableContext(); ########
            log.debug("Backup complete!");
            
            // create database or use existing database AND update the mapping in contextserver2dbpool
            try{
                log.debug("Creating new scheme or using existing scheme on target database system!");
                createDatabaseAndMappingForContext(db_handle,configdb_write_con,target_database_id,context_id);
                cache.resetPoolMappingForContext(context_id);
                log.debug("Scheme found and mapping in configdb changed to new target database system!");
            }catch(RemoteException rem){
                throw new DatabaseContextMappingException(""+rem.getMessage());
            }catch(SQLException sqle){
                throw new DatabaseContextMappingException(""+sqle.getMessage());
            }catch(PoolException poolex){
                throw new DatabaseContextMappingException(""+poolex.getMessage());
            }
            
            
            
            // now insert all data to target db
            log.debug("Now filling target database system "+target_database_id+" with data of context "+context_id+"!");
            try{
                target_ox_db_con = cache.getWRITEConnectionForContext(context_id);
                target_ox_db_con.setAutoCommit(false);
                fillTargetDatabase(dbfetch,sorted_tables,target_ox_db_con);
                // commit ALL tables with all data of every row
                target_ox_db_con.commit();
            }catch(SQLException sql){
                throw new TargetDatabaseException(""+sql.getMessage());
            }catch(PoolException pexp){
                throw new TargetDatabaseException(""+pexp.getMessage());
            }
            
            log.debug("Filling completed for target database system "+target_database_id+" with data of context "+context_id+"!");
            
            // now delete from old database schema all the data
            // For delete from database we loop recursive
            ox_db_write_con.setAutoCommit(false);
            log.debug("Now deleting data for context "+context_id+" from old scheme!");
            for(int a = sorted_tables.size()-1;a>=0;a--){
                TableObject to =  sorted_tables.get(a);
                stm = ox_db_write_con.prepareStatement("DELETE FROM "+to.getName()+" WHERE cid = ?");
                stm.setInt(1,context_id);
                log.debug("Deleting data from table \""+to.getName()+"\" for context "+context_id);
                stm.executeUpdate();
                stm.close();
            }            
            log.debug("Data delete for context "+context_id+" completed!");
            
            
            // check if scheme is empty after deleting context data on source db
            // if yes, drop whole database
            deleteSchemeFromDatabaseIfEmpty(ox_db_write_con,configdb_write_con,source_database_id,scheme);
            ox_db_write_con.commit();
            
            // all this was ok , then enable context back again
            log.debug("Enabling context "+context_id+" back again!");
            enableContext(context_id);
            
        }catch(DatabaseContextMappingException dcme){
            log.error("Exception caught while updating mapping in configdb",dcme);
            // revoke contextserver2dbpool()
            try{
                log.error("Now revoking entries in configdb (cs2dbpool) for context "+context_id);
                revokeConfigdbMapping(contextserver2dbpool_backup,configdb_write_con,context_id);
                cache.resetPoolMappingForContext(context_id);
            }catch(Exception ecp){
                log.fatal("!!!!!!WARNING!!!!! Could not revoke configdb entries for "+context_id+"!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!",ecp);
            }
            
            // enableContext() back
            enableContextBackAfterError(context_id);
            throw dcme;
        }catch(TargetDatabaseException tde){
            log.error("Exception caught while moving data for context "+context_id+" to target database "+target_database_id,tde);
            log.error("Target database rollback starts for context "+context_id);
            // rollback insert on target db
            if(target_ox_db_con!=null){
                try{
                    target_ox_db_con.rollback();
                    log.error("Target database rollback finished for context "+context_id);
                }catch(Exception ecp){
                    log.error("Error rollback on target database",ecp);
                }
            }
            
            // revoke contextserver2dbpool()
            try{
                log.error("Now revoking entries in configdb (cs2dbpool) for context "+context_id);
                revokeConfigdbMapping(contextserver2dbpool_backup,configdb_write_con,context_id);
                cache.resetPoolMappingForContext(context_id);
            }catch(Exception ecp){
                log.fatal("!!!!!!WARNING!!!!! Could not revoke configdb entries for "+context_id+"!!!!!!WARNING!!! INFORM ADMINISTRATOR!!!!!!",ecp);
            }
            
            // enableContext() back
            enableContextBackAfterError(context_id);
            
            throw tde;
        }catch(SQLException sql){
            // enableContext back
            log.error("SQL Exception caught while moving data " +
                    "for context "+context_id+" to target database "+target_database_id,sql);
            enableContext(context_id);
            
            
            // rollback
            if(ox_db_write_con!=null){
                try{
                    ox_db_write_con.rollback();
                }catch(Exception ecp){
                    log.error("Error rollback connection",ecp);
                }
            }
            
            // rollback
            if(configdb_write_con!=null){
                try{
                    configdb_write_con.rollback();
                }catch(Exception ecp){
                    log.error("Error rollback connection",ecp);
                }
            }
            
            throw sql;
        }catch(PoolException pexp){
            log.error("Pool exception caught!",pexp);
            
            // rollback
            if(ox_db_write_con!=null){
                try{
                    ox_db_write_con.rollback();
                }catch(Exception ecp){
                    log.error("Error rollback connection",ecp);
                }
            }
            
            // rollback
            if(configdb_write_con!=null && !configdb_write_con.getAutoCommit()){
                try{
                    configdb_write_con.rollback();
                }catch(Exception ecp){
                    log.error("Error rollback connection",ecp);
                }
            }
            
            enableContextBackAfterError(context_id);
            throw pexp;
        }finally{
            if(ox_db_write_con!=null){
                try{
                    cache.pushWRITEConnectionForPoolId(source_database_id,ox_db_write_con);
                }catch(Exception ex){
                    log.error("Error pushing connection",ex);
                }
            }
            if(configdb_write_con!=null){
                try{
                    cache.pushConfigDBWrite(configdb_write_con);
                }catch(Exception ex){
                    log.error("Error pushing connection",ex);
                }
            }
            if(stm!=null){
                try{
                    stm.close();
                }catch(Exception ex){
                    log.error(LOG_ERROR_CLOSING_STATEMENT,ex);
                }
            }
            if(target_ox_db_con!=null){
                try{
                    cache.pushWRITEConnectionForPoolId(target_database_id,target_ox_db_con);
                }catch(Exception ex){
                    log.error("Error pushing connection",ex);
                }
            }
        }
        
        if(log.isDebugEnabled ()){
            end = System.currentTimeMillis ();
            double time_ = end-start;
            time_ = time_/1000;
            log.debug("Data moving for context "+context_id+" to target database system "+target_database_id+" completed in "+time_+" seconds!");            
        }
        
        return retval;
        
    }
    
    private void deleteSchemeFromDatabaseIfEmpty(final Connection ox_db_write_con, final Connection configdb_con, final int source_database_id, String scheme) throws SQLException {
        PreparedStatement stmt = null;
        PreparedStatement dropstmt = null;
        try {
            // check if any context is in scheme X on database Y
            stmt = configdb_con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE db_schema = ? AND write_db_pool_id = ?");
            stmt.setString(1,scheme);
            stmt.setInt(2,source_database_id);
            ResultSet rs = stmt.executeQuery();
            if(!rs.next()){
                // no contexts found on this scheme and db, DROP scheme from db
                log.debug("NO remaining contexts found in scheme "+scheme+" on pool with id "+source_database_id+"!");
                log.debug("NOW dropping scheme "+scheme+" on pool with id "+source_database_id+"!");
                dropstmt = ox_db_write_con.prepareStatement("DROP DATABASE if exists `"+scheme+"`");                
                dropstmt.executeUpdate();
                log.debug("Scheme "+scheme+" on pool with id "+source_database_id+" dropped successfully!");
            }
            rs.close();
        } finally {
            try{
                if(stmt!=null){
                    stmt.close();
                }
            }catch(Exception ex){
                log.error(LOG_ERROR_CLOSING_STATEMENT,ex);
            }
            try{
                if(dropstmt!=null){
                    dropstmt.close();
                }
            }catch(Exception ex){
                log.error(LOG_ERROR_CLOSING_STATEMENT,ex);
            }
        }
        
    }
    
    private void enableContextBackAfterError(final int context_id) throws PoolException, SQLException{
        log.error("Try enabling context "+context_id+" back again!");
        enableContext(context_id);
        log.error("Context "+context_id+" enabled back again!");
    }
    
    private void revokeConfigdbMapping(final TableObject contextserver2dbpool_backup, final Connection configdb_write_con, final int context_id) throws SQLException {
        for(int a = 0;a<contextserver2dbpool_backup.getDataRowCount();a++){
            TableRowObject tro = contextserver2dbpool_backup.getDataRow(a);
            
            StringBuilder prep_sql = new StringBuilder();
            
            prep_sql.append("UPDATE "+contextserver2dbpool_backup.getName()+" SET ");
            
            Enumeration enumi = tro.getColumnNames();
            
            // Save the order of the columns in this list, that all values are correct mapped to their fields
            // for later use in prepared_statement
            List<String> columns_list = new ArrayList<String>();
            
            while(enumi.hasMoreElements()){
                String column = (String)enumi.nextElement();
                columns_list.add(column);
                prep_sql.append(""+column+"=?,");
            }
            
            // set up the sql query for the prep statement
            prep_sql.deleteCharAt(prep_sql.length()-1);
            
            prep_sql.append(" WHERE cid = ?");
            
            // now create the statements for each row
            PreparedStatement prep_ins = null;
            try{
                prep_ins = configdb_write_con.prepareStatement(prep_sql.toString());
                enumi = tro.getColumnNames();
                int ins_pos = 1;
                for(int c = 0;c<columns_list.size();c++){
                    TableColumnObject tco = tro.getColumn(columns_list.get(c));
                    prep_ins.setObject(ins_pos,tco.getData(),tco.getType());
                    ins_pos++;
                }
                prep_ins.setInt(ins_pos++,context_id);
                prep_ins.executeUpdate();
                prep_ins.close();
            }finally{
                try {
                    if(prep_ins!=null){
                        prep_ins.close();
                    }
                } catch (Exception e) {
                    log.error(LOG_ERROR_CLOSING_STATEMENT,e);
                }
            }
            // end of test table
            
        }
    }
    
    public Vector<String> changeStorageData(final int context_id,Hashtable new_filestore_handle) throws PoolException, SQLException, QuotaException {
        Vector<String> retval = new Vector<String>();
        
        Connection configdb_write_con = null;
        PreparedStatement prep = null;
        try{
            
            configdb_write_con = cache.getWRITEConnectionForCONFIGDB();
            configdb_write_con.setAutoCommit(false);
            
            if(new_filestore_handle.containsKey(I_OXContext.CONTEXT_FILESTORE_ID)){
                prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_id = ? WHERE cid = ?");
                prep.setString(1,""+new_filestore_handle.get(I_OXContext.CONTEXT_FILESTORE_ID));
                prep.setInt(2,context_id);
                prep.executeUpdate();
                prep.close();
            }
            
            if(new_filestore_handle.containsKey(I_OXContext.CONTEXT_FILESTORE_USERNAME)){
                prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_login = ? WHERE cid = ?");
                prep.setString(1,""+new_filestore_handle.get(I_OXContext.CONTEXT_FILESTORE_USERNAME));
                prep.setInt(2,context_id);
                prep.executeUpdate();
                prep.close();
            }
            
            if(new_filestore_handle.containsKey(I_OXContext.CONTEXT_FILESTORE_PASSWORD)){
                prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_passwd = ? WHERE cid = ?");
                prep.setString(1,""+new_filestore_handle.get(I_OXContext.CONTEXT_FILESTORE_PASSWORD));
                prep.setInt(2,context_id);
                prep.executeUpdate();
                prep.close();
            }
            
            if(new_filestore_handle.containsKey(I_OXContext.CONTEXT_FILESTORE_NAME)){
                prep = configdb_write_con.prepareStatement("UPDATE context SET filestore_name = ? WHERE cid = ?");
                prep.setString(1,""+new_filestore_handle.get(I_OXContext.CONTEXT_FILESTORE_NAME));
                prep.setInt(2,context_id);
                prep.executeUpdate();
                prep.close();
            }
            
            if(new_filestore_handle.containsKey(I_OXContext.CONTEXT_FILESTORE_QUOTA_MAX)){
                try {
                    long dong = Long.parseLong(""+new_filestore_handle.get(I_OXContext.CONTEXT_FILESTORE_QUOTA_MAX));// send as mb
                    // convert  to byte for db
                    dong *= Math.pow(2,20);
                    prep = configdb_write_con.prepareStatement("UPDATE context SET quota_max = ? WHERE cid = ?");
                    prep.setLong(1,dong);
                    prep.setInt(2,context_id);
                    prep.executeUpdate();
                    prep.close();
                } catch (java.lang.NumberFormatException e) {
                    throw new QuotaException("Invalid quota");
                }
            }
            
            configdb_write_con.commit();
            
            retval.add("OK");
            
        }catch(SQLException exp){
            try{
                if(configdb_write_con!=null && !configdb_write_con.getAutoCommit()){
                    configdb_write_con.rollback();
                }
            }catch(Exception expd){
                log.error("Error processing rollback of connection!",expd);
            }
            throw exp;
        }finally{
            try{
                if(prep!=null){
                    prep.close();
                }
            }catch(Exception exp){
                log.error(LOG_ERROR_CLOSING_STATEMENT);
            }
            try{
                if ( configdb_write_con != null ) {
                    cache.pushConfigDBWrite(configdb_write_con);
                }
            }catch(Exception ecp){
                log.error("Error pushing configdb connection to pool!",ecp);
            }
        }
        
        return retval;
    }
    
    private Hashtable getMyContextSetupContainer(final int context_id) throws PoolException, SQLException {
        Hashtable<String, Object> ret = new Hashtable<String, Object>();
        
        Connection config_db_read = null;
        Connection ox_db_read = null;
        PreparedStatement prep = null;
        try{
            config_db_read = cache.getREADConnectionForCONFIGDB();
            ox_db_read = cache.getREADConnectionForContext(context_id);
            
            String reason_id =null;
            String name =null;
            int filestore_id = -1;
            String filestore_name =null;
            String filestore_user = null;
            String filestore_passwd = null;
            long quota_max = -1;
            long quota_used = -1;
            Boolean enabled = Boolean.TRUE;
            
            prep = config_db_read.prepareStatement("SELECT context.cid,context.name,context.enabled,context.reason_id," +
                    "context.filestore_id,context.filestore_name,context.filestore_login,context.filestore_passwd,context.quota_max," +
                    "context_server2db_pool.server_id,context_server2db_pool.write_db_pool_id,context_server2db_pool.read_db_pool_id," +
                    "context_server2db_pool.db_schema " +
                    "FROM context LEFT JOIN context_server2db_pool ON context.cid = context_server2db_pool.cid " +
                    "WHERE context.cid =? " +
                    "AND context_server2db_pool.server_id = (select server_id from server where name = ?)");
            prep.setInt(1,context_id);
            prep.setString(2,prop.getProp(AdminProperties.Prop.SERVER_NAME,"local"));
            ResultSet rs = prep.executeQuery();
            
            
            // DATBASE HANDLE
            while(rs.next()){
                //filestore_id | filestore_name | filestore_login | filestore_passwd | quota_max
                Hashtable<String, String> data = new Hashtable<String, String>();
                name = rs.getString("name");
                enabled = new Boolean(rs.getBoolean("enabled"));
                reason_id = rs.getString("reason_id");
                filestore_id = rs.getInt("filestore_id");
                filestore_name = rs.getString("filestore_name");
                filestore_user = rs.getString("filestore_login");
                filestore_passwd =  rs.getString("filestore_passwd");
                quota_max = rs.getLong("quota_max");
                if(quota_max!=0 && quota_max!=-1){
                    quota_max /= Math.pow(2,20);
                }
                //System.out.println("**********************" + rs.getMetaData());
                String read_pool =  rs.getString("read_db_pool_id");
                String write_pool =  rs.getString("write_db_pool_id");
                //String server_id =  rs.getString("server_id");
                String db_schema =  rs.getString("db_schema");
                if(read_pool!=null){
                    data.put(I_OXContext.CONTEXT_READ_POOL_ID,read_pool);
                }
                if(write_pool!=null){
                    data.put(I_OXContext.CONTEXT_WRITE_POOL_ID,write_pool);
                }
                if(db_schema!=null){
                    data.put(I_OXContext.CONTEXT_DB_SCHEMA_NAME,db_schema);
                }
                
                ret.put(I_OXContext.CONTEXT_DATABASE_HANDLE,data);
                
            }
//            if(server_id!=null){
//                    ret.put(I_OXUtil.SERVER_ID,server_id);
//            }
            
            
            // CONTEXT STATE INFOS #
            if(reason_id!=null){
                ret.put(I_OXContext.CONTEXT_LOCKED_TXT_ID,reason_id);
            }
            ret.put(I_OXContext.CONTEXT_LOCKED,!enabled);
            //######################
            
            
            
            // FILESTORE HANDLE INFOS ##
            //Hashtable filestore_data = new Hashtable();
            if(filestore_id!=-1){
                ret.put(I_OXContext.CONTEXT_FILESTORE_ID,filestore_id);
            }
            if(filestore_user!=null){
                ret.put(I_OXContext.CONTEXT_FILESTORE_USERNAME,filestore_user);
            }
            if(filestore_name!=null){
                ret.put(I_OXContext.CONTEXT_FILESTORE_NAME,filestore_name);
            }
            if(filestore_passwd!=null){
                ret.put(I_OXContext.CONTEXT_FILESTORE_PASSWORD,filestore_passwd);
            }
            //ret.put(I_OXContext.CONTEXT_FILESTORE_HANDLE,filestore_data);
            // ##########################
            
            
            // GENERAL CONTEXT INFOS AND QUOTA
            
            rs.close();
            prep.close();
            
            prep = ox_db_read.prepareStatement("SELECT filestore_usage.used FROM filestore_usage WHERE filestore_usage.cid = ?");
            prep.setInt(1,context_id);
            rs = prep.executeQuery();
            
            while(rs.next()){
                quota_used = rs.getLong(1);
            }
            rs.close();
            prep.close();
            if(quota_used!=0 && quota_used!=-1){
                quota_used /= Math.pow(2,20);
            }
            if(quota_used!=-1){
                ret.put(I_OXContext.CONTEXT_FILESTORE_QUOTA_USED,quota_used);
            }
            
            // maximum quota of this context
            if(quota_max!=-1){
                ret.put(I_OXContext.CONTEXT_FILESTORE_QUOTA_MAX,quota_max);
            }
            
            long average_size = Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE","100"));
            ret.put(I_OXContext.CONTEXT_AVERAGE_QUOTA,average_size);
            
            // name of the context, currently same with contextid
            if(name!=null){
                ret.put(I_OXContext.CONTEXT_NAME,name);
            }
            
            // context id
            ret.put(I_OXContext.CONTEXT_ID,context_id);
            
        }finally{
            try{
                if(prep!=null){
                    prep.close();
                }
            }catch(Exception ecp){
                log.error(LOG_ERROR_CLOSING_STATEMENT);
            }
            
            try{
                if(config_db_read!=null){
                    cache.pushConfigDBRead(config_db_read);
                }
            }catch(Exception exp){
                log.error("Error pushing configdb connection to pool!",exp);
            }
            try{
                if(ox_db_read!=null){
                    cache.pushOXDBRead(context_id,ox_db_read);
                    //ox_db_read.close();
                }
            }catch(Exception exp){
                log.error("Error pushing ox connection to pool!",exp);
            }
        }
        
        
        return ret;
    }
    
    public Vector<Object> getContextSetup(final int context_id) throws PoolException, SQLException  {
        Vector<Object> v = new Vector<Object>();
        
        // returns webdav infos, database infos(mapping), context status (disabled,enabled,text)
        
        v.add("OK");
        v.add(getMyContextSetupContainer(context_id));
        
        
        return v;
    }
    
    private String getNextUnfilledSchemaFromDB(final int pool_id, final Connection con) throws SQLException {
        PreparedStatement pstm = null;
        try {
            pstm = con.prepareStatement("SELECT db_schema,COUNT(db_schema) FROM context_server2db_pool WHERE write_db_pool_id = ? GROUP BY db_schema");
            pstm.setInt(1, pool_id);
            ResultSet rs = pstm.executeQuery();
            String ret = null;
            
            while( rs.next() ) {
                String schema = rs.getString("db_schema");
                int count     = rs.getInt("COUNT(db_schema)");
                if( count < CONTEXTS_PER_SCHEMA ) {
                    log.debug("count =" + count + " of schema " + schema + ", using it for next context");
                    ret = schema;
                    break;
                }
            }
            
            
            return ret;
        } catch (SQLException e) {
            throw e;
        }finally{
            try {
                if(pstm!=null){
                    pstm.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT);
            }
        }
        
    }
    
    private Integer countUnits(final Hashtable server, final Connection con) throws SQLException, OXContextException {
        PreparedStatement pis = null;
        PreparedStatement ppool = null;
        try{
            Integer count = 0;
            
            int pool_id   = ((Integer)server.get("ID")).intValue();
            String url    = ((String)server.get("URL"));
            String user   = ((String)server.get("LOGIN"));
            String passwd = ((String)server.get("PASSWORD"));
            String driver = ((String)server.get("DRIVER"));
            
            if( USE_UNIT == UNIT_CONTEXT ) {
                pis = con.prepareStatement("SELECT COUNT(server_id) FROM context_server2db_pool WHERE write_db_pool_id=?");
                pis.setInt(1, pool_id);
                ResultSet rsi = pis.executeQuery();
                
                if( ! rsi.next() ){
                    throw new OXContextException("Unable to count contextsof db_pool_id=" + pool_id);
                }
                count = rsi.getInt("COUNT(server_id)");
                rsi.close();
                pis.close();
            } else if( USE_UNIT == UNIT_USER ) {
                ppool = con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE write_db_pool_id=?");
                ppool.setInt(1, pool_id);
                ResultSet rpool = ppool.executeQuery();
                while( rpool.next() ) {
                    String schema = rpool.getString("db_schema");
                    ResultSet rsi = null;
                    try {
                        Connection rcon = cache.getSimpleSqlConnection(url + schema, user, passwd, driver);
                        pis = rcon.prepareStatement("SELECT COUNT(id) FROM user");
                        
                        rsi = pis.executeQuery();
                        if( ! rsi.next() ){
                            throw new OXContextException("Unable to count users of db_pool_id=" + pool_id);
                        }
                        count += rsi.getInt("COUNT(id)");
                        rcon.close();
                        pis.close();
                        rsi = null;
                        rcon = null;
                    } catch( ClassNotFoundException e ) {
                        log.fatal("Error counting users of db pool",e);
                        throw new OXContextException(e.toString());
                    }finally{
                        rsi.close();
                    }
                }
                rpool.close();
                log.debug("***** found " + count + " users on " + pool_id);
            } else {
                throw new OXContextException("UNKNOWN UNIT TO COUNT: " + USE_UNIT);
            }
            
            return count;
        }finally{
            try {
                if(pis!=null){
                    pis.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(ppool!=null){
                    ppool.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
        
        
    }
    
    private Hashtable<String, Object> getNextDBHandleByWeight(final Connection con) throws SQLException, OXContextException {
        PreparedStatement pstm = null;
        try{
            Hashtable<String, Hashtable> servers = new Hashtable<String, Hashtable>();
            
            pstm = con.prepareStatement("SELECT db_pool_id,url,driver,login,password,name,weight,max_units FROM db_pool, db_cluster WHERE db_cluster.write_db_pool_id = db_pool_id");
            ResultSet rs = pstm.executeQuery();
            
            int totalDatabases = 0;
            
            while(rs.next()){
                Hashtable<String, Object> sdata = new Hashtable<String, Object>();
                Integer id = rs.getInt("db_pool_id");
                
                sdata.put("URL"      , rs.getString("url"));
                sdata.put("ID"       , id);
                sdata.put("DRIVER"   , rs.getString("driver"));
                sdata.put("LOGIN"    , rs.getString("login"));
                sdata.put("PASSWORD" , rs.getString("password"));
                sdata.put("WEIGHT"   , rs.getInt("weight"));
                sdata.put("UNIT_MAX" , rs.getInt("max_units"));
                Integer   db_count   = countUnits(sdata, con);
                sdata.put("COUNT"    , db_count);
                
                totalDatabases += db_count.intValue();
                servers.put(rs.getString("name"), sdata);
                
                log.debug("SERVERDATA(" + rs.getString("name") + ")= " + sdata);
            }
            rs.close();
            pstm.close();
            String selected_server = null;
            double maxdist = 0;
            
            Enumeration e = servers.keys();
            while( e.hasMoreElements() ) {
                String name = (String)e.nextElement();
                Hashtable sdata = servers.get(name);
                int unit_max  = ((Integer)sdata.get("UNIT_MAX")).intValue();
                int weight    = ((Integer)sdata.get("WEIGHT")).intValue();
                int db_count  = ((Integer)sdata.get("COUNT")).intValue();
                
                // 0 means "locked" := no more entries must be added
                // -1 is unlimited
                if( unit_max == -1 || ( unit_max != 0 && db_count < unit_max ) ) {
                    double currweight = (double)totalDatabases / 100 * db_count;
                    double x = currweight / weight;
                    currweight -= (int)x*weight;
                    double currdist = weight - currweight;
                    log.debug(name + ":\tX="+ x + "\tcurrweight=" + currweight + "\tcurrdist=" + currdist + "\tmaxdist=" + maxdist);
                    if( currdist > maxdist ) {
                        selected_server = name;
                        maxdist = weight - currweight;
                    }
                    
                }
            }
            
            if( selected_server == null ){
                throw new OXContextException("Unable to find a suitable server");
            }
            Hashtable sel = servers.get(selected_server);
            
            Hashtable<String, Object> ht = new Hashtable<String, Object>();
            ht.put(I_OXUtil.DB_AUTHENTICATION_ID      , sel.get("LOGIN"));
            ht.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD, sel.get("PASSWORD"));
            ht.put(I_OXUtil.DB_DRIVER                 , sel.get("DRIVER"));
            ht.put(I_OXUtil.DB_URL                    , sel.get("URL"));
            ht.put("SELECTED_SERVER"                  , selected_server);
            
            // ID is the ID of the pool where db will be created
            ht.put("ID"                               , sel.get("ID"));
            
            pstm = con.prepareStatement("SELECT read_db_pool_id FROM db_cluster WHERE write_db_pool_id = ?");
            pstm.setInt(1, (Integer)sel.get("ID"));
            rs = pstm.executeQuery();
            if( ! rs.next() ) {
                throw new OXContextException("Unable to read table db_cluster");
            }
            int slave_id = rs.getInt("read_db_pool_id");
            rs.close();
            pstm.close();
            if( slave_id > 0 ) {
                ht.put("READ_ID", slave_id);
            }
            
            return ht;
        }finally{
            try {
                if(pstm!=null){
                    pstm.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
        
        
        
    }
    
    private void fillContextAndServer2DBPool(final int context_id, final long quota_max, final Connection con, final Hashtable ht) throws SQLException, OXContextException {
        // dbid is the id in db_pool of database engine to use for next context
        Integer dbid = (Integer)ht.get("ID");
        Integer readid = dbid;
        
        if( ht.containsKey("READ_ID") ) {
            readid = (Integer)ht.get("READ_ID");
        }
        
        Hashtable<String, Object> database_handle = new Hashtable<String, Object>();
        database_handle.put(I_OXContext.CONTEXT_READ_POOL_ID, readid);
        database_handle.put(I_OXContext.CONTEXT_WRITE_POOL_ID, dbid);
        database_handle.put(I_OXContext.CONTEXT_DB_SCHEMA_NAME, ht.get(I_OXUtil.DB_SCHEMA));
        
        // create context entry in configdb
        // quota is in MB, but we store in Byte
        long quota_max_temp = quota_max;
        if( quota_max != -1 ){
            quota_max_temp *= Math.pow(2, 20);
        }
        fillContextTable(context_id, quota_max_temp, con);
        
        // insert in the context_server2dbpool table
        fillContextServer2DBPool(context_id,database_handle,con);
    }
    
    
    public Vector<String> createContext(final int context_id, final long quota_max, final Hashtable user_container) throws SQLException, PoolException, OXContextException, OXException, RemoteException, DBPoolingException, NoSuchAlgorithmException {
        Vector<String> v = new Vector<String>();
        Connection configdb_write_con = null;
        Connection ox_write_con = null;
        
        try {
            
            if( user_container!=null ){
                OXUtil oxu = new OXUtil();
                // Get config_db/ox_db connection from pool
                configdb_write_con = cache.getWRITEConnectionForCONFIGDB();
                
                Hashtable<String, Object> ht = getNextDBHandleByWeight(configdb_write_con);
                
                // dbid is the id in db_pool of database engine to use for next context
                Integer dbid = (Integer)ht.get("ID");
                
                
                if( CONTEXTS_PER_SCHEMA == 1 ) {
                    //synchronized (ClientAdminThread.create_mutex) {
                    // FIXME: generate unique schema name
                    String schema_name;
                    synchronized (ClientAdminThreadExtended.create_mutex) {
                        configdb_write_con.setAutoCommit(false);
                        int srv_id = IDGenerator.getId (configdb_write_con);
                        configdb_write_con.commit();
                        schema_name = (String)ht.get("SELECTED_SERVER") + "_" + srv_id;
                    }
                    ht.put(I_OXUtil.DB_SCHEMA, schema_name);
                    Vector ret = oxu.createDatabase(ht);
                    if( ! ret.get(0).equals("OK")){
                        throw new OXContextException(""+ret.get(1));
                    }
                    
                    fillContextAndServer2DBPool(context_id, quota_max, configdb_write_con, ht);
                    //}
                } else {
                    // check if there's a db schema which is not yet full
                    synchronized (ClientAdminThreadExtended.create_mutex) {
                        String schema_name = getNextUnfilledSchemaFromDB(dbid.intValue(),configdb_write_con);
                        // there's none? create one
                        if( schema_name == null ) {
                            configdb_write_con.setAutoCommit(false);
                            int srv_id = IDGenerator.getId (configdb_write_con);
                            configdb_write_con.commit();
                            schema_name = (String)ht.get("SELECTED_SERVER") + "_" + srv_id;
                            ht.put(I_OXUtil.DB_SCHEMA, schema_name);
                            Vector ret = oxu.createDatabase(ht);
                            if( ! ret.get(0).equals("OK")){
                                throw new OXContextException(""+ret.get(1));
                            }
                            fillContextAndServer2DBPool(context_id, quota_max, configdb_write_con, ht);
                        } else {
                            ht.put(I_OXUtil.DB_SCHEMA                 , schema_name);
                            fillContextAndServer2DBPool(context_id, quota_max, configdb_write_con, ht);
                        }
                    }
                }
                configdb_write_con.setAutoCommit(false);
                // create login2context mapping in configdb
                fillLogin2ContextTable(context_id, configdb_write_con);
                configdb_write_con.commit();
                
                
                ox_write_con = cache.getWRITEConnectionForContext(context_id);
                ox_write_con.setAutoCommit(false);
                
                initSequenceTables(context_id, ox_write_con); // perhaps the seqs must be deleted on exception
                ox_write_con.commit();
                
                // must be fetched before any other actions, else all statements are commited on this con
                int group_id = IDGenerator.getId(context_id,com.openexchange.groupware.Types.PRINCIPAL,ox_write_con);
                ox_write_con.commit();
                
                int internal_user_id_for_admin = IDGenerator.getId(context_id,com.openexchange.groupware.Types.PRINCIPAL,ox_write_con);
                ox_write_con.commit();
                
                int contact_id_for_admin = IDGenerator.getId(context_id,com.openexchange.groupware.Types.CONTACT,ox_write_con);
                ox_write_con.commit();
                
                
                
                // create group users for context
                // get display name for context default group resolved via admins language
                String lang = OXUser.getLanguage(user_container);
                
                String def_group_disp_name = prop.getGroupProp("DEFAULT_CONTEXT_GROUP_"+lang.toUpperCase(),"Users");
                createStandardGroupForContext(context_id, ox_write_con,def_group_disp_name,group_id);
                
                createAdminForContext(context_id, user_container,ox_write_con,internal_user_id_for_admin,contact_id_for_admin);
                // create system folder for context
                // get lang and displayname of admin
                String display_name = OXUser.getDisplayName(user_container);
                
                createSystemFoldersForContext(context_id, ox_write_con,lang,display_name);
                
                ox_write_con.commit();
                
                // context created
                v.add("OK");
                log.info("Context "+context_id+" created!");
            }
            
        }catch(SQLException ecop){
            log.error("Error processing createContext!Rollback starts!",ecop);
            handleCreateContextRollback(configdb_write_con,ox_write_con,context_id);
            throw ecop;
        }catch(PoolException ecop2){
            log.error("Error processing createContext!Rollback starts!",ecop2);
            handleCreateContextRollback(configdb_write_con,ox_write_con,context_id);
            throw ecop2;
        }catch(OXContextException ecop3){
            log.error("Error processing createContext!Rollback starts!",ecop3);
            handleCreateContextRollback(configdb_write_con,ox_write_con,context_id);
            throw ecop3;
        }catch(OXException ecop4){
            log.error("Error processing createContext!Rollback starts!",ecop4);
            handleCreateContextRollback(configdb_write_con,ox_write_con,context_id);
            throw ecop4;
        }catch(RemoteException ecop5){
            log.error("Error processing createContext!Rollback starts!",ecop5);
            handleCreateContextRollback(configdb_write_con,ox_write_con,context_id);
            throw ecop5;
        }catch(DBPoolingException ecop6){
            log.error("Error processing createContext!Rollback starts!",ecop6);
            handleCreateContextRollback(configdb_write_con,ox_write_con,context_id);
            throw ecop6;
        }catch(NoSuchAlgorithmException ecop7){
            log.error("Error processing createContext!Rollback starts!",ecop7);
            handleCreateContextRollback(configdb_write_con,ox_write_con,context_id);
            throw ecop7;
        }finally{
            
            try{
                cache.pushConfigDBWrite(configdb_write_con);
            }catch(Exception ecp){
                log.error("Error pushing configdb connection to pool!",ecp);
            }
            
            try{
                cache.pushOXDBWrite(context_id,ox_write_con);
            }catch(Exception ecp){
                log.error("Error pushing ox write connection to pool!",ecp);
            }
            
        }
        
        return v;
    }
    
    private void handleCreateContextRollback(final Connection configdb_write_con, final Connection ox_write_con, final int context_id){
        
        try{
            if(configdb_write_con!=null && !configdb_write_con.getAutoCommit()){
                configdb_write_con.rollback();
            }
        }catch(Exception expd){
            log.error("Error processing rollback of configdb connection!",expd);
        }
        try{
            // remove all entries from configdb cause rollback might not be enough
            //  cause of contextserver2dbpool entries
            if(configdb_write_con!=null){
                deleteContextFromConfigDB(configdb_write_con,context_id);
            }
        }catch(Exception ecp){
            log.error("Error removing/rollback entries from configdb for context "+context_id,ecp);
        }
        try {
            if(ox_write_con!=null && !ox_write_con.getAutoCommit()){
                ox_write_con.rollback();
            }
        } catch (Exception ex) {
            log.error("Error processing rollback of ox connection!",ex);
            
        }
        try{
            // delete sequences
            if(ox_write_con!=null){
                deleteSequenceTables(context_id,ox_write_con);
            }
        }catch(Exception ep){
            log.error("Error deleting sequence tables on rollback create context",ep);
        }
    }
    
    private void fillContextServer2DBPool(final int context_id, final Hashtable database_handle, final Connection configdb_write_con) throws SQLException, OXContextException {
        
        PreparedStatement stmt = null;
        try{
            if(database_handle.containsKey(I_OXContext.CONTEXT_DB_SCHEMA_NAME) &&
                    database_handle.containsKey(I_OXContext.CONTEXT_READ_POOL_ID) &&
                    database_handle.containsKey(I_OXContext.CONTEXT_WRITE_POOL_ID)){
                
                int read_id = -1;
                int write_id = -1;
                String db_schema = "openexchange";
                read_id   = ((Integer)database_handle.get(I_OXContext.CONTEXT_READ_POOL_ID)).intValue();
                write_id  = ((Integer)database_handle.get(I_OXContext.CONTEXT_WRITE_POOL_ID)).intValue();
                db_schema = (String)database_handle.get(I_OXContext.CONTEXT_DB_SCHEMA_NAME);
                
                // ok database pools exist in configdb
                int server_id = getMyServerID(configdb_write_con);
                stmt = configdb_write_con.prepareStatement("INSERT INTO context_server2db_pool (server_id,cid,read_db_pool_id,write_db_pool_id,db_schema)" +
                        " VALUES " +
                        " (?,?,?,?,?)");
                stmt.setInt(1,server_id);
                stmt.setInt(2,context_id);
                stmt.setInt(3,read_id);
                stmt.setInt(4,write_id);
                stmt.setString(5,db_schema);
                stmt.executeUpdate();
                stmt.close();
            }
        }finally{
            if(stmt!=null){
                try{
                    stmt.close();
                }catch(Exception exp){
                    log.error(LOG_ERROR_CLOSING_STATEMENT,exp);
                }
            }
        }
    }
    
    
    private int getMyServerID(final Connection configdb_write_con) throws SQLException,OXContextException{
        
        PreparedStatement sstmt = null;
        int sid = 0;
        try{
            
            String servername = prop.getProp(AdminProperties.Prop.SERVER_NAME,"local");
            sstmt = configdb_write_con.prepareStatement("SELECT server_id FROM server WHERE name = ?");
            sstmt.setString(1, servername);
            ResultSet rs2 = sstmt.executeQuery();
            if( !rs2.next() ){
                throw new OXContextException("No server registered with name=" + servername);
            }
            sid = Integer.parseInt(rs2.getString("server_id"));
            rs2.close();
        }catch(SQLException sql){
            throw sql;
        }finally{
            try {
                sstmt.close();
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
        return sid;
    }
    
    private void fillContextTable(final int context_id, final long quota_max, final Connection configdb_write_con) throws SQLException, OXContextException {
        PreparedStatement stmt = null;
        try{
            int store_id = getNextFileStoreID(configdb_write_con);
            // check if all filespool infos exist and then insert into context table and login2context
            
            stmt = configdb_write_con.prepareStatement("INSERT INTO context (cid,name,enabled,filestore_id,filestore_name,quota_max) VALUES (?,?,?,?,?,?)");
            stmt.setInt(1,context_id);
            stmt.setString(2,""+context_id);
            stmt.setBoolean(3,true);
            stmt.setInt(4,store_id);
            stmt.setString(5,""+context_id+"_ctx_store");
            stmt.setLong(6,quota_max);
            stmt.executeUpdate();
            stmt.close();
        }finally{
            if(stmt!=null){
                try {
                    stmt.close();
                } catch (Exception e) {
                    log.error(LOG_ERROR_CLOSING_STATEMENT,e);
                }
                
            }
        }
        
    }
    
    private void fillLogin2ContextTable(final int context_id, final Connection configdb_write_con) throws SQLException {
        PreparedStatement stmt = null;
        try{
            stmt = configdb_write_con.prepareStatement("INSERT INTO login2context (cid,login_info) VALUES (?,?)");
            
            stmt.setInt(1,context_id);
            stmt.setString(2,""+context_id);
            stmt.executeUpdate();
            
        }catch(SQLException sql){
            throw sql;
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
    }
    
    private void createStandardGroupForContext(final int context_id, final Connection ox_write_con, final String display_name, final int group_id) throws SQLException {
        
        //ox_write_con.setAutoCommit(true);
        
        PreparedStatement group_stmt = ox_write_con.prepareStatement("INSERT INTO groups (cid, id, identifier, displayname,lastModified) VALUES (?,?,'users',?,?);");
        group_stmt.setInt(1,context_id);
        group_stmt.setInt(2,group_id);
        group_stmt.setString(3,display_name);
        group_stmt.setLong(4, System.currentTimeMillis());
        group_stmt.executeUpdate();
        group_stmt.close();
    }
    
    private void createAdminForContext(final int context_id, final Hashtable user_container, final Connection ox_write_con, final int internal_user_id, final int contact_id) throws PoolException, SQLException, UserException, DBPoolingException, OXException, NoSuchAlgorithmException {
        // here implemente the user creation for the context
        OXUser_MySQL oxs = new OXUser_MySQL();
        Hashtable<String, Boolean> newAccess = new Hashtable<String, Boolean>();
        
        newAccess.put( I_OXUser.ACCESS_CALENDAR, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_CONTACTS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_DELEGATE_TASKS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_FORUM, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_ICAL, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_INFOSSTORE, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_PROJECTS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_RSS_BOOKMARKS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_RSS_PORTAL, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_SYNCML, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_TASKS, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_VCARD, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_WEBDAV,Boolean.TRUE);
        newAccess.put( I_OXUser.ACCESS_WEBDAV_XML, Boolean.TRUE );
        newAccess.put( I_OXUser.ACCESS_WEBMAIL, Boolean.TRUE );
        
        AdminDaemonTools.checkPrimaryMail(context_id,user_container.get( I_OXUser.PRIMARY_MAIL).toString());
        //oxs.createUser( context_id, user_container, newAccess);
        oxs.createUser(context_id,user_container,newAccess,ox_write_con,internal_user_id,contact_id);
    }
    
    private void createSystemFoldersForContext(final int context_id, final Connection ox_write_con, final String admin_language, final String admin_displayname) throws OXException {
        OXFolderAction oxa = new OXFolderAction();
        oxa.addContextSystemFolders(context_id,admin_displayname,admin_language,ox_write_con);
    }
    
    public Vector<String> deleteContext(final int context_id) throws SQLException, PoolException, RemoteException, DBPoolingException, OXContextException, ContextException, DeleteFailedException, LdapException {
        Vector<String> v = new Vector<String>();
        Connection write_ox_con = null;
        Connection con_write = null;
        PreparedStatement del_stmt = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt = null;
        try{
            
            con_write = cache.getWRITEConnectionForCONFIGDB();
            con_write.setAutoCommit(false);
            // delete from groups_member ehre context id like unsere
            //delete from groups_member where id = (select id from groups where cid = 96);
            
            write_ox_con = cache.getWRITEConnectionForContext(context_id);
            try{
                // delete all users within this context except admin, admin must be the last user
                int admin_id = AdminDaemonTools.getAdminForContext(context_id, write_ox_con);
                
                write_ox_con.setAutoCommit(false);
                
                del_stmt = write_ox_con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id != ?");
                del_stmt.setInt(1,context_id);
                del_stmt.setLong(2,admin_id);
                ResultSet r22 = del_stmt.executeQuery();
                
                OXUser_MySQL oxu = new OXUser_MySQL();
                while(r22.next()){
                    int del_id = r22.getInt("id");
                    log.debug("Deleting user with id "+del_id+" from context "+context_id);
                    oxu.deleteUser(context_id,del_id,write_ox_con);
                }
                
                r22.close();
                del_stmt.close();
                
                log.debug("Deleting admin (Id:"+admin_id+") for context "+context_id);
                oxu.deleteUser(context_id,new Integer(""+admin_id),write_ox_con);
                
                // delete all folder stuff via groupware api
                OXFolderAction aa = new OXFolderAction();
                log.debug("Deleting context folders via OX API ");
                aa.deleteAllContextFolders(context_id,write_ox_con,write_ox_con);
                
                log.debug("Deleting admin mapping for context "+context_id);
                del_stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_admin WHERE cid = ?");
                del_stmt.setInt(1,context_id);
                del_stmt.executeUpdate();
                del_stmt.close();
                
                log.debug("Deleting group members for context "+context_id);
                // delete from members
                del_stmt = write_ox_con.prepareStatement("DELETE FROM groups_member WHERE id = (SELECT id FROM groups WHERE cid = ?)");
                del_stmt.setInt(1,context_id);
                del_stmt.executeUpdate();
                del_stmt.close();
                
                log.debug("Deleting groups for context "+context_id);
                // delete from groups
                del_stmt = write_ox_con.prepareStatement("DELETE FROM groups WHERE cid = ?");
                del_stmt.setInt(1,context_id);
                del_stmt.executeUpdate();
                del_stmt.close();
                
                log.debug("Deleting resource members for context "+context_id);
                // delete from resource_group_member
                del_stmt = write_ox_con.prepareStatement("DELETE FROM resource_group_member WHERE cid = ?");
                del_stmt.setInt(1,context_id);
                del_stmt.executeUpdate();
                del_stmt.close();
                
                log.debug("Deleting resource groups for context "+context_id);
                // delete from resource_group
                del_stmt = write_ox_con.prepareStatement("DELETE FROM resource_group WHERE cid = ?");
                del_stmt.setInt(1,context_id);
                del_stmt.executeUpdate();
                del_stmt.close();
                
                log.debug("Deleting resources for context "+context_id);
                // delete from resource
                del_stmt = write_ox_con.prepareStatement("DELETE FROM resource WHERE cid = ?");
                del_stmt.setInt(1,context_id);
                del_stmt.executeUpdate();
                del_stmt.close();
                
                // delete sequences
                deleteSequenceTables(context_id,write_ox_con);
                
                // call ALL delete methods to delete from del_* tables
                OXGroup_MySQL.deleteAllRecoveryData(context_id,write_ox_con);
                OXUser_MySQL.deleteAllRecoveryData(context_id,write_ox_con);
                OXResource_MySQL.deleteAllRecoveryData(context_id,write_ox_con);
                
                write_ox_con.commit();
            }catch(SQLException exp){
                log.error("Error processing deleteContext!Rollback starts!",exp);
                handleContextDeleteRollback(write_ox_con,con_write);
                throw exp;
            }catch(PoolException pexp){
                log.error("Error processing deleteContext!Rollback starts!",pexp);
                handleContextDeleteRollback(write_ox_con,con_write);
                throw pexp;
            }catch(DBPoolingException pexp2){
                log.error("Error processing deleteContext!Rollback starts!",pexp2);
                handleContextDeleteRollback(write_ox_con,con_write);
                throw pexp2;
            }catch(ContextException pexp3){
                log.error("Error processing deleteContext!Rollback starts!",pexp3);
                handleContextDeleteRollback(write_ox_con,con_write);
                throw pexp3;
            }catch(DeleteFailedException pexp4){
                log.error("Error processing deleteContext!Rollback starts!",pexp4);
                handleContextDeleteRollback(write_ox_con,con_write);
                throw pexp4;
            }catch(LdapException pexp5){
                log.error("Error processing deleteContext!Rollback starts!",pexp5);
                handleContextDeleteRollback(write_ox_con,con_write);
                throw pexp5;
            }finally{
                try{
                    if(write_ox_con!=null){
                        cache.pushOXDBWrite(context_id,write_ox_con);
                    }
                }catch(Exception exp){
                    log.error("Error pushing ox write connection to pool!",exp);
                }
            }
            
            // execute deletecontextfromconfigdb
            deleteContextFromConfigDB(con_write,context_id);
            
            con_write.commit();
            
            v.add("OK");
            v.add("CONTEXT DELETED");
        }catch(SQLException exp){
            log.error("Error processing deleteContext!Rollback starts!",exp);
            handleContextDeleteRollback(write_ox_con,con_write);
            throw exp;
        }catch(PoolException pexp){
            log.error("Error processing deleteContext!Rollback starts!",pexp);
            handleContextDeleteRollback(write_ox_con,con_write);
            throw pexp;
        }catch(DBPoolingException pexp2){
            log.error("Error processing deleteContext!Rollback starts!",pexp2);
            handleContextDeleteRollback(write_ox_con,con_write);
            throw pexp2;
        }catch(ContextException pexp3){
            log.error("Error processing deleteContext!Rollback starts!",pexp3);
            handleContextDeleteRollback(write_ox_con,con_write);
            throw pexp3;
        }catch(DeleteFailedException pexp4){
            log.error("Error processing deleteContext!Rollback starts!",pexp4);
            handleContextDeleteRollback(write_ox_con,con_write);
            throw pexp4;
        }catch(LdapException pexp5){
            log.error("Error processing deleteContext!Rollback starts!",pexp5);
            handleContextDeleteRollback(write_ox_con,con_write);
            throw pexp5;
        }finally{
            try{
                if( stmt != null ) {
                    stmt.close();
                }
            }catch(Exception e){
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            try{
                if( stmt2 != null ) {
                    stmt2.close();
                }
            }catch(Exception e){
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            try {
                if( del_stmt != null ) {
                    del_stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            try{
                if(con_write!=null){
                    cache.pushConfigDBWrite(con_write);
                }
            }catch(Exception exp){
                log.error("Error pushing configdb connection to pool!",exp);
            }
            
            
        }
        
        return v;
    }
    
    private void handleContextDeleteRollback(final Connection write_ox_con, final Connection con_write) {
        try{
            if(con_write!=null && !con_write.getAutoCommit()){
                con_write.rollback();
                log.debug("Rollback of configdb write connection ok");
            }
        }catch(Exception rexp){
            log.error("Error processing rollback of configdb connection!",rexp);
        }
        try{
            if(write_ox_con!=null && !write_ox_con.getAutoCommit()){
                write_ox_con.rollback();
                log.debug("Rollback of ox db write connection ok");
            }
        }catch(Exception rexp){
            log.error("Error processing rollback of ox write connection!",rexp);
        }
    }
    
    private static void deleteContextFromConfigDB(final Connection configdb_write_con, final int context_id) throws SQLException, OXContextException, RemoteException, DBPoolingException{
        // find out what db_schema context belongs to
        PreparedStatement stmt3 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt = null;
        try{
            boolean cs2db_broken = false;
            stmt2 = configdb_write_con.prepareStatement("SELECT db_schema,write_db_pool_id FROM context_server2db_pool WHERE cid = ?");
            stmt2.setInt(1,context_id);
            stmt2.executeQuery();
            ResultSet rs = stmt2.getResultSet();
            String db_schema = null;
            int pool_id = -1;
            if( ! rs.next() ){
                //throw new OXContextException("Unable to determine db_schema of context " + context_id);
                cs2db_broken = true;
                log.error("Unable to determine db_schema of context " + context_id);
            } else {
                db_schema = rs.getString("db_schema");
                pool_id = ((Integer)rs.getInt("write_db_pool_id")).intValue();
            }
            stmt2.close();
            //System.out.println("############# db_schema = " + db_schema);
            
            
            log.debug("Deleting context_server2dbpool mapping for context "+context_id);
            // delete context from context_server2db_pool
            stmt2 = configdb_write_con.prepareStatement("DELETE FROM context_server2db_pool WHERE cid = ?");
            stmt2.setInt(1,context_id);
            stmt2.executeUpdate();
            stmt2.close();
            //configdb_write_con.commit(); // temp disabled by c utmasta
            
            
            if( ! cs2db_broken ) {
                try {
                    // check if any other context uses the same db_schema
                    // if not, delete it
                    stmt2 = configdb_write_con.prepareStatement("SELECT db_schema FROM context_server2db_pool WHERE db_schema = ?");
                    stmt2.setString(1, db_schema);
                    stmt2.executeQuery();
                    rs = stmt2.getResultSet();
                    
                    if( ! rs.next() ) {
                        // get auth data from db_pool to delete schema
                        stmt3 = configdb_write_con.prepareStatement("SELECT url,driver,login,password FROM db_pool WHERE db_pool_id = ?");
                        stmt3.setInt(1,pool_id);
                        stmt3.executeQuery();
                        ResultSet rs3 = stmt3.getResultSet();
                        
                        if( ! rs3.next() ){
                            throw new OXContextException("Unable to determine authentication data of pool_id " + pool_id);
                        }
                        Hashtable<String, String> db = new Hashtable<String, String>();
                        db.put(I_OXUtil.DB_AUTHENTICATION_ID       , rs3.getString("login"));
                        db.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD , rs3.getString("password"));
                        db.put(I_OXUtil.DB_DRIVER                  , rs3.getString("driver"));
                        db.put(I_OXUtil.DB_URL                     , rs3.getString("url"));
                        db.put(I_OXUtil.DB_SCHEMA                  , db_schema);
                        
                        log.debug("Deleting database " + db_schema);
                        
                        OXUtil oxus = new OXUtil();
                        Vector ret = oxus.deleteDatabase(db);
                        if( ! ret.get(0).equals("OK") ){
                            throw new OXContextException((String)ret.get(1));
                        }
                        
                        stmt3.close();
                        // tell pool, that database has been removed
                        com.openexchange.database.Database.reset(context_id);
                    }
                    stmt2.close();
                } catch (Exception e) {
                    log.error("Problem deleting database while doing rollback, cid=" + context_id + ": " , e);
                }
            }
            
            log.debug("Deleting login2context entries for context "+context_id);
            stmt = configdb_write_con.prepareStatement("DELETE FROM login2context WHERE cid = ?");
            stmt.setInt(1,context_id);
            stmt.executeUpdate();
            stmt.close();
            
            log.debug("Deleting context entry for context "+context_id);
            stmt = configdb_write_con.prepareStatement("DELETE FROM context WHERE cid = ?");
            stmt.setInt(1,context_id);
            stmt.executeUpdate();
            stmt.close();
            
        }finally{
            try{
                if( stmt3 != null ) {
                    stmt3.close();
                }
            }catch(Exception ecp){
                log.error(LOG_ERROR_CLOSING_STATEMENT,ecp);
            }
            try{
                if( stmt2 != null ) {
                    stmt2.close();
                }
            }catch(Exception ecp){
                log.error(LOG_ERROR_CLOSING_STATEMENT,ecp);
            }
            try{
                if( stmt != null ) {
                    stmt.close();
                }
            }catch(Exception ecp){
                log.error(LOG_ERROR_CLOSING_STATEMENT,ecp);
            }
        }
    }
    
    public Vector<Object> searchContext(final String search_pattern) throws PoolException, SQLException {
        Vector<Object> v = new Vector<Object>();
        Connection configdb_read = null;
        PreparedStatement stmt = null;
        try{
            configdb_read = cache.getREADConnectionForCONFIGDB();
            String search_patterntmp = search_pattern.replace('*','%');
            stmt = configdb_read.prepareStatement("SELECT " +
                    "name,cid,enabled,reason_id,filestore_id,filestore_name,filestore_login,filestore_passwd,quota_max " +
                    "FROM context " +
                    "WHERE name " +
                    "LIKE ? " +
                    "OR cid " +
                    "LIKE ?");
            stmt.setString(1,search_patterntmp);
            stmt.setString(2,search_patterntmp);
            
            ResultSet rs = stmt.executeQuery();
            
            
            Hashtable<String, Hashtable> ht = new Hashtable<String, Hashtable>();
            
            while(rs.next()){
                //filestore_id | filestore_name | filestore_login | filestore_passwd | quota_max
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                String cid = rs.getString("cid");
                String name = rs.getString("name");
                int reason_id = rs.getInt("reason_id");
                int store_id = rs.getInt("filestore_id");
                String store_name = rs.getString("filestore_name");
                String store_login = rs.getString("filestore_login");
                String store_passwd = rs.getString("filestore_passwd");
                boolean enabled = rs.getBoolean("enabled");
                data.put(I_OXContext.CONTEXT_NAME,name);
                data.put(I_OXContext.CONTEXT_ID,cid);
                
                data.put(I_OXContext.CONTEXT_LOCKED,new Boolean(!enabled));
                data.put(I_OXContext.CONTEXT_LOCKED_TXT_ID,reason_id);
                data.put(I_OXContext.CONTEXT_FILESTORE_ID,store_id);
                if(store_login!=null){
                    data.put(I_OXContext.CONTEXT_FILESTORE_USERNAME,store_login);
                }
                if(store_passwd!=null){
                    data.put(I_OXContext.CONTEXT_FILESTORE_PASSWORD,store_passwd);
                }
                if(store_name!=null){
                    data.put(I_OXContext.CONTEXT_FILESTORE_NAME,store_name);
                }
                ht.put(cid,data);
            }
            rs.close();
            stmt.close();
            
            v.add("OK");
            v.add(ht);
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try{
                cache.pushConfigDBRead(configdb_read);
            }catch(Exception exp){
                log.error("Error pushing configdb connection to pool!",exp);
            }
            
        }
        
        return v;
    }
    
    public Vector<Object> disableContext(final int context_id, final int reason_id) throws PoolException, SQLException {
        return myEnableDisableContext(context_id,false,reason_id);
    }
    
    public Vector<Object> enableContext(final int context_id) throws PoolException, SQLException  {
        return myEnableDisableContext(context_id,true,-1);
    }
    
    private Vector<Object> myEnableDisableContext(final int context_id, final boolean enabled, final int reason_id) throws PoolException, SQLException{
        Vector<Object> v = new Vector<Object>();
        Connection con_write = null;
        PreparedStatement stmt = null;
        try{
            con_write = cache.getWRITEConnectionForCONFIGDB();
            con_write.setAutoCommit(false);
            stmt = con_write.prepareStatement("UPDATE context SET enabled = ?, reason_id = ? WHERE cid = ?");
            
            stmt.setBoolean(1,enabled);
            if(enabled){
                stmt.setNull(2,java.sql.Types.INTEGER);
            }else{
                if(reason_id!=-1){
                    try{
                        stmt.setLong(2,reason_id);
                    }catch(Exception exp){
                        log.error("Invalid reason ID!",exp);
                    }
                }else{
                    stmt.setNull(2,java.sql.Types.INTEGER);
                }
            }
            stmt.setInt(3,context_id);
            stmt.executeUpdate();
            stmt.close();
            con_write.commit();
            v.add("OK");
            if(enabled){
                v.add("CONTEXT UNLOCKED");
            }else{
                v.add("CONTEXT LOCKED");
            }
        }catch(SQLException sql){
            try{
                con_write.rollback();
            }catch(Exception ec){
                log.error("Error rollback configdb connection",ec);
            }
            throw sql;
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            if(con_write!=null){
                try{
                    cache.pushConfigDBWrite(con_write);
                }catch(Exception exp){
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        
        return v;
    }
    
    public Vector<String> disableAllContexts(final int reason_id) throws PoolException, SQLException  {
        return myLockUnlockAllContexts(false, reason_id);
    }
    
    public Vector<String> enableAllContexts() throws PoolException, SQLException  {
        return myLockUnlockAllContexts(true, 1);
    }
    
    private Vector<String> myLockUnlockAllContexts(final boolean lock_all, final int reason_id) throws PoolException, SQLException{
        Vector<String> v = new Vector<String>();
        Connection con_write = null;
        PreparedStatement stmt= null;
        try{
            con_write = cache.getWRITEConnectionForCONFIGDB();
            con_write.setAutoCommit(false);
            if( reason_id != -1 ) {
                stmt = con_write.prepareStatement("UPDATE context SET enabled = ?, reason_id = ?");
                stmt.setBoolean(1,lock_all);
                stmt.setInt(2,reason_id);
            } else {
                stmt = con_write.prepareStatement("UPDATE context SET enabled = ?");
                stmt.setBoolean(1,lock_all);
            }
            stmt.executeUpdate();
            stmt.close();
            con_write.commit();
            
            v.add("OK");
            if(lock_all){
                v.add("ALL CONTEXTS UNLOCKED");
            }else{
                v.add("ALL CONTEXTS LOCKED");
            }
        }catch(SQLException sql){
            try{
                con_write.rollback();
            }catch(Exception ec){
                log.error("Error rollback configdb connection",ec);
            }
            throw sql;
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            if(con_write!=null){
                try{
                    cache.pushConfigDBWrite(con_write);
                }catch(Exception exp){
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        
        return v;
    }
    
    public Vector<Object> searchContextByDatabase(final String db_host_url) throws PoolException, SQLException{
        Connection con = null;
        Vector<Object> v = new Vector<Object>();
        // maybe we should make the search pattern configurable
        //db_host_url = db_host_url.replace('*','%');
        PreparedStatement stmt = null;
        try {
            con = cache.getREADConnectionForCONFIGDB();
            stmt = con.prepareStatement( "SELECT context_server2db_pool.cid FROM context_server2db_pool,db_pool WHERE db_pool.url LIKE ? AND db_pool.db_pool_id = context_server2db_pool.write_db_pool_id" );
            stmt.setString(1,db_host_url);
            ResultSet rs = stmt.executeQuery();
            Vector<Integer> ids = new Vector<Integer>();
            while(rs.next()){
                ids.add(rs.getInt("cid"));
            }
            rs.close();
            stmt.close();
            v.add("OK");
            v.add(ids);
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            if(con!=null) {
                try {
                    cache.pushConfigDBRead(con);
                } catch(Exception exp) {
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        return v;
    }
    
    public Vector<Object> searchContextByFilestore(final String filestore_url) throws PoolException, SQLException {
        Connection con = null;
        Vector<Object> v = new Vector<Object>();
        // maybe we should make the search pattern configurable
        //filestore_url = filestore_url.replace('*','%');
        PreparedStatement stmt = null;
        try {
            con = cache.getREADConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT context.cid FROM context,filestore WHERE filestore.uri LIKE ? AND filestore.id = context.filestore_id");
            stmt.setString(1,filestore_url);
            ResultSet rs = stmt.executeQuery();
            Vector<Integer> ids = new Vector<Integer>();
            while(rs.next()){
                ids.add(rs.getInt("cid"));
            }
            rs.close();
            stmt.close();
            v.add("OK");
            v.add(ids);
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            if(con!=null) {
                try {
                    cache.pushConfigDBRead(con);
                } catch(Exception exp) {
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        return v;
    }
    
    public Vector<String> changeQuota(final int context_id, final long quota_max) throws PoolException, SQLException {
        Vector<String> v = new Vector<String>();
        
        long quota_max_temp = quota_max;
        if( quota_max != -1 ){
            quota_max_temp *= Math.pow(2, 20);
        }
        Connection con_write = null;
        PreparedStatement stmt =null;
        try{
            con_write = cache.getWRITEConnectionForCONFIGDB();
            con_write.setAutoCommit(false);
            stmt = con_write.prepareStatement("UPDATE context SET quota_max=? WHERE cid=?");
            stmt.setLong(1, quota_max_temp);
            stmt.setInt(2, context_id);
            stmt.executeUpdate();
            stmt.close();
            con_write.commit();
            v.add("OK");
        }catch(SQLException sql){
            try{
                con_write.rollback();
            }catch(Exception ec){
                log.error("Error rollback configdb connection",ec);
            }
            throw sql;
        }finally{
            
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            if(con_write!=null){
                try{
                    cache.pushConfigDBWrite(con_write);
                }catch(Exception exp){
                    log.error("Error pushing configdb connection to pool!",exp);
                }
            }
        }
        
        return v;
    }
    
    private int getNextFileStoreID(final Connection configdb_read) throws SQLException, OXContextException {
        int return_store_id = 0;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            long average_size = Long.parseLong(prop.getProp("AVERAGE_CONTEXT_SIZE","100"));
            average_size *= Math.pow(2,20);// to byte
            stmt = configdb_read.prepareStatement("SELECT id,size,max_context FROM filestore");
            
            rs = stmt.executeQuery();
            while(rs.next()){
                
                int store_max_contexts = rs.getInt("max_context");
                // don't add contexts if 0
                if( store_max_contexts == 0 ) {
                    continue;
                }
                
                int store_id = rs.getInt("id");
                long store_size = rs.getLong("size"); // must be as byte in the db
                
                PreparedStatement pis = configdb_read.prepareStatement("SELECT COUNT(cid) FROM context WHERE filestore_id = ?");
                pis.setInt(1, store_id);
                ResultSet rsi = pis.executeQuery();
                if( ! rsi.next() ){
                    throw new OXContextException("Unable to determine usage of filestore=" + store_id);
                }
                Integer store_count = rsi.getInt("COUNT(cid)");
                
                rsi.close();
                pis.close();
                // don't add if limit reached
                if( store_count >= store_max_contexts ) {
                    continue;
                }
                
                long used_mb = store_count*average_size ; // theoretisch benutzter speicher on store
                long with_this_context = used_mb+average_size; // theoretisch benutzter speicher on store inkl. dem neuen
                if(with_this_context<=store_size){
                    return_store_id = store_id;
                    break;
                }
            }
            // all stores are set to 0 in max_context(means they should NOT be touched or increased)
            if( return_store_id==0 ){
                throw new OXContextException("No usable or free enough filestore found");
            }
        }catch(java.lang.NumberFormatException juppes){
            throw new OXContextException("Invalid average context size");
        }finally{
            try{
                rs.close();
            }catch (SQLException exp){
                log.error("Error closing Resultset",exp);
            }
            try {
                stmt.close();
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
        
        
        return return_store_id;
    }
    
    private void deleteSequenceTables(final int context_id, final Connection write_ox_con) throws SQLException{
        log.debug("Deleting sequence entries for context "+context_id);
        // delete all sequence table entries
        PreparedStatement del_stmt = null;
        PreparedStatement seq_del = null;
        try{
            del_stmt = write_ox_con.prepareStatement("show tables like ?");
            del_stmt.setString(1,"%sequence_%");
            ResultSet rs_sequences = del_stmt.executeQuery();
            
            while(rs_sequences.next()){
                String del_sequence_table = rs_sequences.getString(1);
                seq_del = write_ox_con.prepareStatement("delete from "+del_sequence_table+" where cid = ?");
                seq_del.setInt(1,context_id);
                seq_del.executeUpdate();
                seq_del.close();
            }
        }finally{
            try{
                if(del_stmt!=null){
                    del_stmt.close();
                }
                if(seq_del!=null){
                    seq_del.close();
                }
            }catch(Exception ecp){
                log.error(LOG_ERROR_CLOSING_STATEMENT,ecp);
            }
        }
    }
    
    private void initSequenceTables(final int context_id, final Connection con) throws SQLException, OXContextException  {
        PreparedStatement ps = null;
        try{
            AdminCache  cache   = ClientAdminThreadExtended.cache;
            ArrayList<String> sequence_tables = cache.getSequenceTables();
            Iterator<String> is = sequence_tables.iterator();
            while( is.hasNext() ) {
                int startval = 0;
                String table = is.next();
                if( table.equals("sequence_folder") ) {
                    // below id 20 is reserved
                    startval = 20;
                }
                ps = con.prepareStatement("INSERT INTO " + table + " VALUES(?,?);");
                ps.setInt(1, context_id);
                ps.setInt(2, startval);
                ps.executeUpdate();
                ps.close();
            }
        }catch(OXGenericException oxgen){
            throw new OXContextException(""+oxgen.getMessage());
        }finally{
            try {
                if(ps!=null){
                    ps.close();
                }
            } catch (Exception e) {
                log.error(LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
    }
    
    
    
    
    
    
    
    
}
