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
package com.openexchange.admin.rmi.impl;

import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;

@OXExceptionSource (
classId=Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXUTIL,
        component=Component.ADMIN_UTIL
        )
public class OXUtil extends BasicAuthenticator implements OXUtilInterface {
    
    //static UtilExceptionFactory UTIL_EXCEPTIONS = new UtilExceptionFactory (OXUtil.class);    
    private final static Log log = LogFactory.getLog (OXUtil.class);
    
    
    public OXUtil () throws RemoteException {
        super ();       
    }
    
    
//    @OXThrowsMultiple(
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
//            desc={" ","Invalid data sent by client","store already exists","Filestore size to large for database (max=8796093022208)"},
//            exceptionId={0,1,2,54},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXUtilException.STORE_EXISTS+" %s","Filestore error %s"}
//    )
    public int registerFilestore (Filestore fstore,Credentials auth) 
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException{
        doAuthentication(auth);
        
        log.debug (fstore.getUrl () + " - " + fstore.getSize ());
        
        if ( !AdminDaemonTools.checkValidStoreURI ( fstore.getUrl () ) ) {
            throw new InvalidDataException ("Invalid url sent");
            //UTIL_EXCEPTIONS.create(1,"Invalid store: \""+ fstore.getUrl() +"\"");
        }
        
        if(fstore.getSize()==-1){
            throw new InvalidDataException("Invalid store size -1");
        }
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if(tools.existsStore (fstore.getUrl ())){
            throw new InvalidDataException ("Store already exists");
            //UTIL_EXCEPTIONS.create(2,fstore.getUrl());
        }
        
        
        try{
            java.io.File f = new java.io.File (new java.net.URI (fstore.getUrl ()));
            if (!f.exists ()) {
                throw new InvalidDataException ("No such directory: \""+ fstore.getUrl () +"\"");
                //throw UTIL_EXCEPTIONS.create (1,"No such directory: \""+ fstore.getUrl () +"\"");
            }
            if (!f.isDirectory ()) {
                throw new InvalidDataException("No directory: \""+ fstore.getUrl () +"\"");
                //throw UTIL_EXCEPTIONS.create (1,"No directory: \""+ fstore.getUrl () +"\"");
            }
        }catch(URISyntaxException urex){
            throw new InvalidDataException("Invalid filstore url");
            //throw UTIL_EXCEPTIONS.create(1,urex.getMessage());
        }catch(IllegalArgumentException urex){
            throw new InvalidDataException("Invalid filstore url");
            //throw UTIL_EXCEPTIONS.create(1,urex.getMessage());
        }
        
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        int response = oxutil.registerFilestore (fstore);
        log.debug ("RESPONSE "+response);
        return response;
        
        
        
    }
    
//    @OXThrowsMultiple(
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
//            desc={" ","Invalid data sent by client","store does not exist","Filestore size to large for database (max=8796093022208)"},
//            exceptionId={3,4,5,55},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXUtilException.NO_SUCH_STORE+" %s","Filestore error %s"}
//    )
    public void changeFilestore (Filestore fstore, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug ( fstore.getUrl ()+" "+fstore.getMaxContexts ()+" "+fstore.getSize ()+" "+fstore.getId () );
        
        if (!AdminDaemonTools.checkValidStoreURI (fstore.getUrl ())) {
            throw new InvalidDataException ("Invalid store url "+fstore.getUrl ());
            //throw new StorageException(UTIL_EXCEPTIONS.create ( 4, "INVALID STORE \""+ fstore.getUrl () +"\"" ));
            
        }
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if(!tools.existsStore (fstore.getId ())){
            throw new InvalidDataException ("No such store "+fstore.getUrl ());
            //throw UTIL_EXCEPTIONS.create ( 5, fstore.getId () );
        }
        
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        oxutil.changeFilestore ( fstore );
    }
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
//            desc={" ","Invalid data sent by client"},
//            exceptionId={6,7},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s"}
//    )
    public Filestore[] listFilestores (String search_pattern, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException{
        
        doAuthentication(auth);
        
        log.debug (search_pattern);
        
        if(search_pattern==null || search_pattern.trim ().length ()==0){
            throw new InvalidDataException ("Invalid search pattern");
            //throw UTIL_EXCEPTIONS.create (7,"Invalid pattern \""+search_pattern+"\"" );
        }
        
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return oxutil.listFilestores (search_pattern);
    }
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT, Category.USER_INPUT},
//            desc={" ","no such store","store in use"},
//            exceptionId={8,9,51},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_STORE+" %s",OXUtilException.STORE_IN_USE+" %s"}
//    )
    public void unregisterFilestore (int store_id,Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException{
        
        doAuthentication(auth);
        
        log.debug (store_id);
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if(!tools.existsStore (store_id)){
            throw new InvalidDataException ("No such store");
            //throw UTIL_EXCEPTIONS.create (9,store_id);
        }
        
        if( tools.storeInUse (store_id) ) {
            throw new InvalidDataException ("Store "+store_id+" in use");
            //throw UTIL_EXCEPTIONS.create (51,store_id);
        }
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        oxutil.unregisterFilestore (store_id);
        
    }
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
//            desc={" ","reason already exists","invalid data"},
//            exceptionId={10,11,12},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.REASON_EXISTS+" %s",OXContext.MSG_INVALID_DATA_SENT+"-%s"}
//    )
    public int addMaintenanceReason (MaintenanceReason reason, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (reason);
        
        if( reason.getText () == null || reason.getText ().trim ().length () == 0 ) {
            throw new InvalidDataException ("Invalid reason text!");
            //throw UTIL_EXCEPTIONS.create (12,OXGenericException.NULL_EMPTY);
        }
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        if( tools.existsReason (reason.getText ()) ) {
            throw new InvalidDataException ("Reason already exists!");
            //throw UTIL_EXCEPTIONS.create (11,reason.getText ());
        }
        
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return oxutil.addMaintenanceReason (reason);
        
//        log.debug(OXContext.LOG_RESPONSE+retValue);
    }
    
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
//            desc={" ","reason does not exist"},
//            exceptionId={13,14},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_REASON+" %s"}
//    )
    public void deleteMaintenanceReason (MaintenanceReason reason, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (reason);
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        if(!tools.existsReason (reason.getId ()) ) {
            throw new InvalidDataException ("No such reason");
//                throw UTIL_EXCEPTIONS.create (13,reason.getId ());
        }
        
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        int[] tmp = {reason.getId()};
        oxutil.deleteMaintenanceReason (tmp);
        
    }
    
    
//    @OXThrowsMultiple(
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
//            desc={" ","reason does not exist"},
//            exceptionId={15,16},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_REASON+" %s"}
//    )
    public MaintenanceReason[] getMaintenanceReasons (int reason_ids[], Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (reason_ids);
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        for(int i = 0; i < reason_ids.length;i ++){
            if(!tools.existsReason (reason_ids[i])) {
                throw new InvalidDataException ("Reason with id "+reason_ids[i]+" does not exists");
                //    throw UTIL_EXCEPTIONS.create(16,reason_ids[i]);
            }
        }
        
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return oxutil.getMaintenanceReasons (reason_ids);
        
    }
    
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR},
//            desc={" "},
//            exceptionId={17},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR}
//    )
    public MaintenanceReason[] getAllMaintenanceReasons (Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException{
        
        doAuthentication(auth);
        
        OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
        return oxutil.getAllMaintenanceReasons ();
        
    }
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
//            exceptionId={18,19,20},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR+"-%s"}
//    )
    public void createDatabase (Database db, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (db.toString());
        
        if (!db.attributesforcreateset ()) {
             throw new InvalidDataException ("Mandatory fields not set!");            
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.createDatabase (db);
        
    }
    
//    @OXThrowsMultiple(
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
//            exceptionId={21,22,23},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR}
//    )
    public void deleteDatabase (Database db, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (db.toString());
        
        
        if(!db.attributesfordeleteset ()){
            throw new InvalidDataException ("Mandatory fields not set!");
            //throw UTIL_EXCEPTIONS.create(22,oxgen.getMessage());
        }else{
            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            oxcox.deleteDatabase ( db );
        }
    }
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT,Category.USER_INPUT},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"db already exists"},
//            exceptionId={24,25,26,27,28},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.DATABASE_EXISTS+" %s","DB_CLUSTER_WEIGHT not within range (0-100): %s"}
//    )
    public int registerDatabase (Database db, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (db.toString());
        
        
        if(!db.attributesforregisterset ()){
            throw new InvalidDataException ("Mandatory fields not set!");
            //throw UTIL_EXCEPTIONS.create(25,oxgen.getMessage());
        }
        
        
        if(db.getClusterWeight ()== null){
            db.setClusterWeight (100);
        }
        
        if( db.getClusterWeight () < 0 || db.getClusterWeight () > 100 ) {
            throw new InvalidDataException ("Clusterweight not within range (0-100)");
            //throw UTIL_EXCEPTIONS.create(28,weight);
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        return oxcox.registerDatabase ( db );
        
    }
    
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"server exists"},
//            exceptionId={29,30,31,32},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.SERVER_EXISTS+" %s"}
//    )
    public int registerServer (Server srv, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (srv.getName ());
        
        if( srv.getName () == null || srv.getName ().trim ().length () == 0 ) {
            throw new InvalidDataException ("Invalid server name");
            //throw UTIL_EXCEPTIONS.create(30,OXGenericException.NULL_EMPTY);
        }
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        if ( tools.existsServer (srv.getName ())) {
            throw new InvalidDataException ("Server already exists!");
            //throw UTIL_EXCEPTIONS.create(32, srv.getName());
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        return oxcox.registerServer (srv.getName ());
        
    }
    
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT,Category.USER_INPUT},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"no such db","pool in use"},
//            exceptionId={33,34,35,36,52},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.NO_SUCH_DATABASE+" %s",OXUtilException.POOL_IN_USE+" %s"}
//    )
    public void unregisterDatabase (int database_id, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException{
        
        doAuthentication(auth);
        
        log.debug (database_id);
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        if(!tools.existsDatabase (database_id)){
            throw new InvalidDataException ("No such database "+database_id);
            //throw UTIL_EXCEPTIONS.create (36, database_id);
        }
        if(tools.poolInUse (database_id) ) {
            throw new StorageException ("Pool is in use "+database_id);
            //throw UTIL_EXCEPTIONS.create (52, database_id);
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.unregisterDatabase (database_id);
        
    }
    
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR,Category.USER_INPUT,Category.USER_INPUT},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR,"no such server","server in use"},
//            exceptionId={37,38,39,40,53},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR,OXUtilException.NO_SUCH_SERVER+" %s",OXUtilException.SERVER_IN_USE+" %s"}
//    )
    public void unregisterServer (int server_id, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (server_id);
        
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        if(!tools.existsServer (server_id)){
            throw new InvalidDataException ("No such server "+server_id);
            //throw UTIL_EXCEPTIONS.create (40, server_id);
        }
        if( tools.serverInUse (server_id) ) {
            throw new StorageException ("Server "+server_id+" is in use");
            //throw UTIL_EXCEPTIONS.create (53, server_id);
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.unregisterServer (server_id);
        
    }
    
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
//            exceptionId={41,42,43},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR+"-%s"}
//    )
    public Database[] searchForDatabase (String search_pattern, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException{
        
        doAuthentication(auth);
        
        log.debug (search_pattern);
        
        if(search_pattern==null || search_pattern.length()==0){
            throw new InvalidDataException("Invalid search pattern");
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        return oxcox.searchForDatabase (search_pattern);
        
    }
    
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.INTERNAL_ERROR},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,OXContext.MSG_INTERNAL_ERROR},
//            exceptionId={44,45,46},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXContext.MSG_INTERNAL_ERROR+"-%s"}
//    )
    public Server[] searchForServer (String search_pattern,Credentials auth) 
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug (search_pattern);
        
        if(search_pattern==null || search_pattern.length()==0){
            throw new InvalidDataException("Invalid search pattern");
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        return oxcox.searchForServer (search_pattern);
        
    }
    
//    @OXThrowsMultiple (
//    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
//            desc={" ",OXContext.MSG_INVALID_DATA_SENT,"no such db","db already exists"},
//            exceptionId={47,48,49,50},
//            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContext.MSG_INVALID_DATA_SENT+"-%s",OXUtilException.NO_SUCH_DATABASE+" %s",OXUtilException.DATABASE_EXISTS+" %s"}
//    )
    
    public void changeDatabase (Database db, Credentials auth)
    throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
         log.debug (db.toString());
        
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        
        
        if ( ! tools.existsDatabase ( db.getId () ) ) {
            throw new InvalidDataException ("No such database with id "+db.getId ());
            //throw UTIL_EXCEPTIONS.create (49, db.getId ());
        }
        
        if ( null != db.getDisplayname () ) {
            if ( tools.existsDatabase ( db.getDisplayname () ) ) {
                throw new InvalidDataException ("Database with name "+db.getDisplayname ()+" already exists");
                //throw UTIL_EXCEPTIONS.create (50, db.getDisplayname () );
            }
        }
        
        if(db.getClusterWeight ()==null){
            db.setClusterWeight (0);
        }
        
        if( db.getClusterWeight () < 0 || db.getClusterWeight () > 100 ) {
            throw new InvalidDataException ("Clusterweight not within range (0-100)");
            //throw UTIL_EXCEPTIONS.create(28,weight);
        }
        
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.changeDatabase ( db );
        
    }
    
    public void deleteMaintenanceReason(int[] reason_ids, Credentials auth) 
        throws RemoteException,StorageException,InvalidCredentialsException,InvalidDataException {
        
        doAuthentication(auth);
        
        log.debug(Arrays.toString(reason_ids));
        
        OXToolStorageInterface tools = OXToolStorageInterface.getInstance();
        for(int i = 0; i < reason_ids.length;i ++){
            if(!tools.existsReason (reason_ids[i])) {
                throw new InvalidDataException ("Reason with id "+reason_ids[i]+" does not exists");
                //    throw UTIL_EXCEPTIONS.create(16,reason_ids[i]);
            }
        }
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        oxcox.deleteMaintenanceReason(reason_ids);
        
    }
    
    public int[] getAllMaintenanceReasonIds (Credentials auth) 
        throws RemoteException,StorageException,InvalidCredentialsException{
        
        doAuthentication(auth);
        
        OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
        MaintenanceReason[] allobjs = oxcox.getAllMaintenanceReasons ();
        int[] retval = new int[allobjs.length];
        for(int a = 0;a<allobjs.length;a++){
            retval[a] = allobjs[a].getId();
        }
        return retval;
    }
}
