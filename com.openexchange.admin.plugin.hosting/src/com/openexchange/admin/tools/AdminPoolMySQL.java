///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the Open-Xchange, Inc. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2004-2006 Open-Xchange, Inc.
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//package com.openexchange.admin.tools;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
//import com.openexchange.admin.exceptions.PoolException;
//import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterfaceExtension;
//
//import java.util.Hashtable;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//public class AdminPoolMySQL implements OXAdminPoolInterfaceExtension {
//    
//    private Log log = LogFactory.getLog(this.getClass());
//    private MysqlConnectionPoolDataSource dsConfWrite;
//    private MysqlConnectionPoolDataSource dsConfRead;
//    private Hashtable<Integer,MysqlConnectionPoolDataSource> readCtxPool = new Hashtable<Integer, MysqlConnectionPoolDataSource>();
//    private Hashtable<Integer,MysqlConnectionPoolDataSource> writeCtxPool = new Hashtable<Integer, MysqlConnectionPoolDataSource>();
//    
//    public AdminPoolMySQL(PropertyHandler prop) {
//        super();
//        try {
//            dsConfWrite = new MysqlConnectionPoolDataSource();
//            String url = prop.getSqlProp("CONFIGDB_WRITE_URL", "jdbc:mysql://127.0.0.1:3306/configdb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true" );
//            String user = prop.getSqlProp("CONFIGDB_WRITE_USERNAME", "openexchange" );
//            String pass = prop.getSqlProp("CONFIGDB_WRITE_PASSWORD", "secret" );
//            dsConfWrite.setURL(url);
//            dsConfWrite.setUser(user);
//            dsConfWrite.setPassword(pass);
//            
//            dsConfRead = new MysqlConnectionPoolDataSource();
//            url = prop.getSqlProp("CONFIGDB_READ_URL", "jdbc:mysql://127.0.0.1:3306/configdb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true" );
//            user = prop.getSqlProp("CONFIGDB_READ_USERNAME", "openexchange" );
//            pass = prop.getSqlProp("CONFIGDB_READ_PASSWORD", "secret" );
//            dsConfRead.setURL(url);
//            dsConfRead.setUser(user);
//            dsConfRead.setPassword(pass);
//            
//        } catch( Exception e ) {
//            log.error("Error init",e);
//        }
//    }
//    
//    public Connection getREADConnectionForCONFIGDB() throws PoolException {
//        Connection con = null;
//        try{
//            con = dsConfRead.getPooledConnection().getConnection();
//        }catch(Exception exp){
//            log.error("Error get config db read connection from pool!",exp);
//            throw new PoolException(""+exp.getMessage());
//        }
//        return con;
//    }
//    
//    public Connection getREADConnectionForContext(int context_id) throws PoolException {
//        log.debug("### pickupOXDBRead");
//        Connection con = null;
//        try{
//        if( readCtxPool.containsKey(context_id) ) {
//            log.debug("### using existing connection pool for context " + context_id);
//            MysqlConnectionPoolDataSource ds = readCtxPool.get(context_id);
//            log.debug("### databaseName=" + ds.getDatabaseName());
//            con = ds.getPooledConnection().getConnection();
//        } else {
//            log.debug("### creating new connection pool for context " + context_id);
//            Connection confcon = getREADConnectionForCONFIGDB();
//            PreparedStatement ps = confcon.prepareStatement("SELECT db_schema,read_db_pool_id FROM context_server2db_pool WHERE cid = ?");
//            ps.setInt(1, context_id);
//            ResultSet st =ps.executeQuery();
//            String schema = null;
//            int poolid = 0;
//            if( st.next() ) {
//                schema = st.getString("db_schema");
//                poolid = st.getInt("read_db_pool_id");
//            } else {
//                throw new PoolException("UNABLE TO QUERY READ_DB_POOL_ID");
//            }
//            ps.close();
//            st.close();
//            log.debug("### using schema=" + schema + ", poolid=" + poolid);
//            ps = confcon.prepareStatement("SELECT url,login,password FROM db_pool WHERE db_pool_id = ?");
//            ps.setInt(1, poolid);
//            st = ps.executeQuery();
//            String url = null;
//            String login = null;
//            String password = null;
//            if( st.next() ) {
//                url = st.getString("url");
//                login = st.getString("login");
//                password = st.getString("password");
//            } else {
//                throw new PoolException("UNABLE TO GET PARAMETERS FROM DB_POOL ID=" + poolid);
//            }
//            url += schema;
//            log.debug("### using url=" + url + ", login=" + login + ", pass=" + password);
//            MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
//            ds.setUser(login);
//            ds.setPassword(password);
//            ds.setDatabaseName(schema);
//            ds.setURL(url);
//            readCtxPool.put(context_id, ds);
//            con = ds.getPooledConnection().getConnection();
//        }
//        }catch(Exception exp){
//            log.error("Error get ox db read connection from pool!",exp);
//            throw new PoolException(""+exp.getMessage());
//        }
//        return con;
//    }
//    
//    public Connection getWRITEConnectionForCONFIGDB() throws PoolException {
//        Connection con = null;
//        try{
//        con = dsConfWrite.getPooledConnection().getConnection();
//        }catch(Exception exp){
//            throw new PoolException(""+exp.getMessage());
//        }
//        return con;
//    }
//    
//    public Connection getWRITEConnectionForContext(int context_id) throws PoolException {
//        log.debug("### pickupOXDBWrite");
//        Connection con = null;
//        try{
//            if( writeCtxPool.containsKey(context_id) ) {
//                log.debug("### using existing connection pool for context " + context_id);
//                MysqlConnectionPoolDataSource ds = writeCtxPool.get(context_id);
//                log.debug("### databaseName=" + ds.getDatabaseName());
//                con = ds.getPooledConnection().getConnection();
//            } else {
//                log.debug("### creating new connection pool for context " + context_id);
//                Connection confcon = getREADConnectionForCONFIGDB();
//                PreparedStatement ps = confcon.prepareStatement("SELECT db_schema,write_db_pool_id FROM context_server2db_pool WHERE cid = ?");
//                ps.setInt(1, context_id);
//                ResultSet st =ps.executeQuery();
//                String schema = null;
//                int poolid = 0;
//                if( st.next() ) {
//                    schema = st.getString("db_schema");
//                    poolid = st.getInt("write_db_pool_id");
//                } else {
//                    throw new PoolException("UNABLE TO QUERY READ_DB_POOL_ID");
//                }
//                log.debug("### using schema=" + schema + ", poolid=" + poolid);
//                ps.close();
//                st.close();
//                ps = confcon.prepareStatement("SELECT url,login,password FROM db_pool WHERE db_pool_id = ?");
//                ps.setInt(1, poolid);
//                st = ps.executeQuery();
//                String url = null;
//                String login = null;
//                String password = null;
//                if( st.next() ) {
//                    url = st.getString("url");
//                    login = st.getString("login");
//                    password = st.getString("password");
//                } else {
//                    throw new PoolException("UNABLE TO GET PARAMETERS FROM DB_POOL ID=" + poolid);
//                }
//                url += schema;
//                log.debug("### using url=" + url + ", login=" + login + ", pass=" + password);
//                MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
//                ds.setUser(login);
//                ds.setPassword(password);
//                ds.setDatabaseName(schema);
//                ds.setURL(url);
//                //ds.setServerName("localhost");
//                writeCtxPool.put(context_id, ds);
//                con = ds.getPooledConnection().getConnection();
//            }
//        }catch(Exception exp){
//            log.error("Error get ox db write connection from pool!",exp);
//            throw new PoolException(""+exp.getMessage());
//        }
//        return con;
//    }
//    
//    public boolean pushConfigDBRead(Connection con) throws PoolException {
//        try{
//            if(con != null && !con.getAutoCommit() && !con.isClosed() ){
//                con.setAutoCommit(true);
//            }
//            if( con != null ){
//                con.close();
//            }
//        }catch(Exception exp){
//            log.error("Error pushing config db read connection to pool!",exp);
//            throw new PoolException(""+exp.getMessage());
//        }
//        return true;
//    }
//    
//    public boolean pushConfigDBWrite(Connection con) throws PoolException {
//        try{
//            if(con != null && !con.getAutoCommit() && !con.isClosed() ){
//                con.setAutoCommit(true);
//            }
//            if( con != null ){
//                con.close();
//            }
//        }catch(Exception exp){
//            log.error("Error pushing config db write connection to pool!",exp);
//            throw new PoolException(""+exp.getMessage());
//        }
//        return true;
//    }
//    
//    public boolean pushOXDBRead(int context_id, Connection con) throws PoolException {
//        try{
//            if(con != null && !con.getAutoCommit() && !con.isClosed() ){
//                con.setAutoCommit(true);
//            }
//            if( con != null ){
//                con.close();
//            }
//        }catch(Exception exp){
//            log.error("Error pushing ox db read connection to pool!",exp);
//            throw new PoolException(""+exp.getMessage());
//        }
//        return true;
//    }
//    
//    public boolean pushOXDBWrite(int context_id, Connection con) throws PoolException {
//        try{
//            if(con != null && !con.getAutoCommit() && !con.isClosed() ){
//                con.setAutoCommit(true);
//            }
//            if( con != null ){
//                con.close();
//            }
//        }catch(Exception exp){
//            log.error("Error pushing ox db write connection to pool!",exp);
//            throw new PoolException(""+exp.getMessage());
//        }
//        return true;
//    }
//
//    public int getDBPoolIdForContextId(int context_id) throws PoolException {
//        return -1;
//    }
//
//    public Connection getWRITEConnectionForPoolId(int db_pool_id,String db_schema) throws PoolException {
//        return null;
//    }
//
//    public void pushWRITEConnectionForPoolId(int db_pool_id,Connection con) throws PoolException {
//        
//    }
//
//    public void resetPoolMappingForContext(int context_id) throws PoolException {
//    }
//    
//    public String getSchemeForContextId(int context_id) throws PoolException{
//        return null;
//    }
//    
//}
