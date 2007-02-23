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
package com.openexchange.admin.dataSource.impl;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.dataSource.I_OXUtil;
import com.openexchange.admin.dataSource.OXUtil_MySQL;
import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.exceptions.FilestoreException;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.exceptions.OXUtilException;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.exceptions.UtilException;
import com.openexchange.admin.exceptions.UtilExceptionFactory;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;

import java.sql.SQLException;

@OXExceptionSource(
classId=Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXUTIL,
        component=Component.ADMIN_UTIL
        )
        public class OXUtil implements I_OXUtil {
    
    static UtilExceptionFactory UTIL_EXCEPTIONS = new UtilExceptionFactory(OXUtil.class);
    private static final long serialVersionUID = -8706544966976397808L;
    
    private Log log = LogFactory.getLog(this.getClass());
    
    
    public OXUtil() throws RemoteException {
        super();
        try {
            log.info( "Class loaded: " + this.getClass().getName() );
        } catch ( Exception e ) {
            log.error("Error init OXUtil",e);
        }
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ","Invalid data sent by client","store already exists","Filestore size to large for database (max=8796093022208)"},
            exceptionId={0,1,2,54},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXUtilException.STORE_EXISTS+" %s","Filestore error %s"}
    )
    public Vector registerFilestore( String store_URI, long store_size, int store_maxContexts )
    throws RemoteException{
        Vector<Object> retValue = new Vector<Object>();
        log.debug(store_URI+" - "+store_size);
        try {
            if( AdminDaemonTools.existsStore( store_URI ) ) {
                throw UTIL_EXCEPTIONS.create(2,store_URI);
            }
            
            if ( !AdminDaemonTools.checkValidStoreURI( store_URI ) ) {
                throw UTIL_EXCEPTIONS.create(1,"Invalid store: \""+store_URI+"\"");
            }
            
            try{
                java.io.File f = new java.io.File(new java.net.URI(store_URI));
                if (!f.exists()) {
                    throw UTIL_EXCEPTIONS.create(1,"No such directory: \""+store_URI+"\"");
                }
                if (!f.isDirectory()) {
                    throw UTIL_EXCEPTIONS.create(1,"No directory: \""+store_URI+"\"");
                }
            }catch(URISyntaxException urex){
                throw UTIL_EXCEPTIONS.create(1,urex.getMessage());
            }
            
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.registerFilestore(store_URI,store_size,store_maxContexts);
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(0).getMessage());
        }catch(UtilException groupexp){
            log.debug(OXContext.LOG_CLIENT_ERROR, groupexp);
            retValue.clear();
            retValue.add( OXContext.RESPONSE_ERROR );
            retValue.add( groupexp.getMessage());
        }catch(FilestoreException fst){
            log.error(OXContext.LOG_ERROR,fst);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(54,fst.getMessage()).getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ","Invalid data sent by client","store does not exist","Filestore size to large for database (max=8796093022208)"},
            exceptionId={3,4,5,55},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXUtilException.NO_SUCH_STORE+" %s","Filestore error %s"}
    )
    public Vector<String> changeFilestore( int store_id, Hashtable filestoreData )
    throws RemoteException{
        Vector<String> retValue = new Vector<String>();
        log.debug( filestoreData );
        try {
            if ( filestoreData != null && filestoreData.containsKey( I_OXUtil.STORE_URL ) && !AdminDaemonTools.checkValidStoreURI( filestoreData.get( I_OXUtil.STORE_URL ).toString() ) ) {
                throw UTIL_EXCEPTIONS.create( 4, "INVALID STORE \""+filestoreData.get( I_OXUtil.STORE_URL ).toString()+"\"" );
            }
            
            if ( !AdminDaemonTools.existsStore( store_id ) ) {
                throw UTIL_EXCEPTIONS.create( 5, store_id );
            }
            
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.changeFilestore( store_id, filestoreData );
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(3).getMessage());
        }catch(UtilException groupexp){
            log.debug(OXContext.LOG_CLIENT_ERROR, groupexp );
            retValue.clear();
            retValue.add( OXContext.RESPONSE_ERROR );
            retValue.add( groupexp.getMessage());
        }catch(FilestoreException fst){
            log.error(OXContext.LOG_ERROR,fst);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(55,fst.getMessage()).getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={" ","Invalid data sent by client"},
            exceptionId={6,7},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s"}
    )
    public Vector<Object> listFilestores(String search_pattern)
    throws RemoteException{
        Vector<Object> retValue = new Vector<Object>();
        log.debug(search_pattern);
        try {
            if( !(search_pattern!=null) && !(search_pattern.trim().length()>0)){
                throw UTIL_EXCEPTIONS.create(7,"Invalid pattern \""+search_pattern+"\"" );
            }
            
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.listFilestores(search_pattern);
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(6).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        } catch(UtilException exc ) {
            log.debug(OXContext.LOG_CLIENT_ERROR,exc);
            retValue.clear();
            retValue.add( OXContext.RESPONSE_ERROR );
            retValue.add( exc.toString() );
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT, Category.USER_INPUT},
            desc={" ","no such store","store in use"},
            exceptionId={8,9,51},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_STORE+" %s",OXUtilException.STORE_IN_USE+" %s"}
    )
    public Vector unregisterFilestore(int store_id)
    throws RemoteException{
        Vector<String> retValue = new Vector<String>();
        log.debug(store_id);
        try {
            if(!AdminDaemonTools.existsStore(store_id)) {
                throw UTIL_EXCEPTIONS.create(9,store_id);
            }
            if( AdminDaemonTools.storeInUse(store_id) ) {
                throw UTIL_EXCEPTIONS.create(51,store_id);
            }
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.unregisterFilestore(store_id);
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(8).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(UtilException groupexp){
            log.debug(OXContext.LOG_CLIENT_ERROR,groupexp);
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(groupexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ","reason already exists","invalid data"},
            exceptionId={10,11,12},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.REASON_EXISTS+" %s",OXContext.MSG_INVALID_DATA_SENT+"-%s"}
    )
    public Vector addMaintenanceReason(String reason_txt) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(reason_txt);
        try {
            if( reason_txt == null || reason_txt.trim().length() == 0 ) {
                throw UTIL_EXCEPTIONS.create(12,OXGenericException.NULL_EMPTY);
            }
            
            if( AdminDaemonTools.existsReason(reason_txt) ) {
                throw UTIL_EXCEPTIONS.create(11,reason_txt);
            }
            
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.addMaintenanceReason(reason_txt);
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(10).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(UtilException groupexp){
            log.debug(OXContext.LOG_CLIENT_ERROR, groupexp );
            retValue.clear();
            retValue.add( OXContext.RESPONSE_ERROR );
            retValue.add( groupexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={" ","reason does not exist"},
            exceptionId={13,14},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_REASON+" %s"}
    )
    public Vector deleteMaintenanceReason(int reason_id) throws RemoteException {
        Vector<String> retValue = new Vector<String>();
        log.debug(reason_id);
        try {
            if( ! AdminDaemonTools.existsReason(reason_id) ) {
                throw UTIL_EXCEPTIONS.create(13,reason_id);
            }
            
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.deleteMaintenanceReason(reason_id);
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(13).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(UtilException groupexp){
            log.debug(OXContext.LOG_CLIENT_ERROR, groupexp );
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(groupexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
   
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={" ","reason does not exist"},
            exceptionId={15,16},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_REASON+" %s"}
    )
    public Vector getMaintenanceReason(int reason_id) throws RemoteException {
        Vector<String> retValue = new Vector<String>();
        log.debug(reason_id);
        try {
            if( ! AdminDaemonTools.existsReason(reason_id) ) {
                throw UTIL_EXCEPTIONS.create(16,reason_id);
            }
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.getMaintenanceReason(reason_id);
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(15).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(UtilException groupexp){
            log.debug(OXContext.LOG_CLIENT_ERROR, groupexp );
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(groupexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR},
            desc={" "},
            exceptionId={17},
            msg={OXContext.MSG_SQL_OPERATION_ERROR}
    )
    public Vector getAllMaintenanceReasons() throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug("");
        try {
            OXUtil_MySQL oxutil = new OXUtil_MySQL();
            retValue = oxutil.getAllMaintenanceReasons();
        } catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(17).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
            exceptionId={18,19,20},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR+"-%s"}
    )
    public Vector createDatabase( Hashtable databaseData ) throws RemoteException {
        Vector<String> retValue = new Vector<String>();
        log.debug(databaseData);
        try {
            try{
                AdminDaemonTools.checkNeeded( databaseData, I_OXUtil.REQUIRED_KEYS_CREATE_DATABASE );
            }catch(OXGenericException oxgen){
                throw UTIL_EXCEPTIONS.create(19,oxgen.getMessage());
            }
            try{
                AdminDaemonTools.checkEmpty( databaseData, I_OXUtil.REQUIRED_KEYS_CREATE_DATABASE );
            }catch(OXGenericException oxgen){
                throw UTIL_EXCEPTIONS.create(19,oxgen.getMessage());
            }
            
            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.createDatabase( databaseData );
        }catch(OXGenericException oxgen){
            log.error(OXContext.MSG_INTERNAL_ERROR, oxgen );
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(20,oxgen.getMessage()).getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(18).getMessage());
        }catch(ClassNotFoundException cnf){
            log.error("Error loading SQL Driver");
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(20,cnf.getMessage()).getMessage());
        }catch(UtilException ut){
            log.debug(OXContext.LOG_CLIENT_ERROR, ut );
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(ut.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
            exceptionId={21,22,23},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR}
    )
    public Vector deleteDatabase( Hashtable databaseData ) throws RemoteException {
        Vector<String> retValue = new Vector<String>();
        log.debug(databaseData);
        try {
            
            try{
                AdminDaemonTools.checkNeeded( databaseData, I_OXUtil.REQUIRED_KEYS_DELETE_DATABASE );
            }catch(OXGenericException oxgen){
                throw UTIL_EXCEPTIONS.create(22,oxgen.getMessage());
            }
            
            try{
                AdminDaemonTools.checkEmpty( databaseData, I_OXUtil.REQUIRED_KEYS_DELETE_DATABASE );
            }catch(OXGenericException oxgen){
                throw UTIL_EXCEPTIONS.create(22,oxgen.getMessage());
            }
            
            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.deleteDatabase( databaseData );
        }catch(ClassNotFoundException cnf){
            log.error("Error loading SQL Driver");
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(23).getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(21).getMessage());
        } catch (UtilException ecp ) {
            log.debug(OXContext.LOG_CLIENT_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"db already exists"},
            exceptionId={24,25,26,27,28},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.DATABASE_EXISTS+" %s","DB_CLUSTER_WEIGHT not within range (0-100): %s"}
    )
    public Vector registerDatabase( Hashtable databaseData, boolean isMaster, int master_id ) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(databaseData+" - "+isMaster+" - "+master_id);
        try {
            
            try{
                AdminDaemonTools.checkNeeded( databaseData, I_OXUtil.REQUIRED_KEYS_ADD_DATABASE );
            }catch(OXGenericException oxgen){
                throw UTIL_EXCEPTIONS.create(25,oxgen.getMessage());
            }
            
            try{
                AdminDaemonTools.checkEmpty( databaseData, I_OXUtil.REQUIRED_KEYS_ADD_DATABASE );
            }catch(OXGenericException oxgen){
                throw UTIL_EXCEPTIONS.create(25,oxgen.getMessage());
            }
            
            
            if ( databaseData.containsKey( I_OXUtil.DB_DISPLAY_NAME ) ) {
                String db_display = databaseData.get( I_OXUtil.DB_DISPLAY_NAME ).toString();
                if ( AdminDaemonTools.existsDatabase( db_display ) ) {
                    throw UTIL_EXCEPTIONS.create(50,db_display);
                }
            }
            
            int weight = 0;
            if ( databaseData.containsKey( I_OXUtil.DB_CLUSTER_WEIGHT ) ) {
                weight = ( (Integer)databaseData.get( I_OXUtil.DB_CLUSTER_WEIGHT ) ).intValue();
            }
            if( weight < 0 || weight > 100 ) {
                throw UTIL_EXCEPTIONS.create(28,weight);
            }
            
            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.registerDatabase( databaseData, isMaster, master_id );
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(24).getMessage());
        }catch(OXUtilException oxu){
            log.error(OXContext.RESPONSE_ERROR,oxu);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(25,oxu.getMessage()).getMessage());
        }catch(UtilException groupexp){
            log.debug(OXContext.LOG_CLIENT_ERROR, groupexp );
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(groupexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"server exists"},
            exceptionId={29,30,31,32},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.SERVER_EXISTS+" %s"}
    )
    public Vector registerServer( String serverName ) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(serverName);
        try {
            if( serverName == null || serverName.trim().length() == 0 ) {
                throw UTIL_EXCEPTIONS.create(30,OXGenericException.NULL_EMPTY);
            }
            
            if ( AdminDaemonTools.existsServer( serverName ) ) {
                throw UTIL_EXCEPTIONS.create(32,serverName);
            }
            
            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.registerServer( serverName );
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(29).getMessage());
        }catch(UtilException oxu){
            log.debug(OXContext.LOG_CLIENT_ERROR,oxu);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+oxu.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"no such db","pool in use"},
            exceptionId={33,34,35,36,52},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.NO_SUCH_DATABASE+" %s",OXUtilException.POOL_IN_USE+" %s"}
    )
    public Vector unregisterDatabase(int db_id) {
        Vector<String> retValue = new Vector<String>();
        log.debug(db_id);
        try{
            
            if(!AdminDaemonTools.existsDatabase(db_id)){
                throw UTIL_EXCEPTIONS.create(36,db_id);
            }
            if( AdminDaemonTools.poolInUse(db_id) ) {
                throw UTIL_EXCEPTIONS.create(52,db_id);
            }

            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.unregisterDatabase(db_id);
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(33).getMessage());
        }catch(UtilException oxu){
            log.debug(OXContext.LOG_CLIENT_ERROR,oxu);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+oxu.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"no such server","server in use"},
            exceptionId={37,38,39,40,53},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.NO_SUCH_SERVER+" %s",OXUtilException.SERVER_IN_USE+" %s"}
    )
    public Vector unregisterServer(int server_id) {
        Vector<String> retValue = new Vector<String>();
        log.debug(server_id);
        try{
            
            if(!AdminDaemonTools.existsServer(server_id)){
                throw UTIL_EXCEPTIONS.create(40,server_id);
            }
            if( AdminDaemonTools.serverInUse(server_id) ) {
                throw UTIL_EXCEPTIONS.create(53,server_id);
            }

            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.unregisterServer(server_id);
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(37).getMessage());
        }catch(UtilException oxu){
            log.debug(OXContext.LOG_CLIENT_ERROR,oxu);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+oxu.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
            exceptionId={41,42,43},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR+"-%s"}
    )
    public Vector searchForDatabase(String search_pattern) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(search_pattern);
        try{
            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.searchForDatabase(search_pattern);
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(41).getMessage());
        }catch(OXUtilException oxu){
            log.debug(OXContext.LOG_CLIENT_ERROR,oxu);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(43,oxu.getMessage()).getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
            exceptionId={44,45,46},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR+"-%s"}
    )
    public Vector searchForServer(String search_pattern) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(search_pattern);
        try{
            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.searchForServer(search_pattern);
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(44).getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ",OXContext.MSG_INVALID_DATA_SENT,"no such db","db already exists"},
            exceptionId={47,48,49,50},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXUtilException.NO_SUCH_DATABASE+" %s",OXUtilException.DATABASE_EXISTS+" %s"}
    )

    public Vector changeDatabase(int database_id, Hashtable databaseData) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        try {
            
            if ( ! AdminDaemonTools.existsDatabase( database_id ) ) {
                throw UTIL_EXCEPTIONS.create(49,database_id);
            }
            
            if ( databaseData.containsKey( I_OXUtil.DB_DISPLAY_NAME ) ) {
            	String db_display = databaseData.get( I_OXUtil.DB_DISPLAY_NAME ).toString();
            	if ( AdminDaemonTools.existsDatabase( db_display ) ) {
            		throw UTIL_EXCEPTIONS.create(50,db_display);
            	}
            }
            
            int weight = 0;
            if ( databaseData.containsKey( I_OXUtil.DB_CLUSTER_WEIGHT ) ) {
                weight = ( (Integer)databaseData.get( I_OXUtil.DB_CLUSTER_WEIGHT ) ).intValue();
            }
            if( weight < 0 || weight > 100 ) {
                throw UTIL_EXCEPTIONS.create(48,"DB_CLUSTER_WEIGHT not within range (0-100): " + weight);                
            }
            
            OXUtil_MySQL oxcox = new OXUtil_MySQL();
            retValue = oxcox.changeDatabase( database_id, databaseData );
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+UTIL_EXCEPTIONS.create(47).getMessage());        
        }catch(UtilException exp){
            log.debug(OXContext.LOG_CLIENT_ERROR, exp );
            retValue.clear();
            retValue.add( OXContext.RESPONSE_ERROR );
            retValue.add( exp.getMessage());            
        } 
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
}
