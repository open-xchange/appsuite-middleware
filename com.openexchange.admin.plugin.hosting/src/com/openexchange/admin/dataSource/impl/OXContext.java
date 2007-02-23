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

import com.openexchange.admin.exceptions.ContextException;
import com.openexchange.admin.exceptions.QuotaException;
import com.openexchange.admin.tools.monitoring.MonitoringInfos;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.OXThrowsMultiple;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.dataSource.I_OXUtil;
import com.openexchange.admin.dataSource.OXContext_MySQL;
import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.exceptions.ContextExceptionFactory;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.OXUtilException;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.exceptions.UserException;
import com.openexchange.admin.jobs.AdminJob;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.admin.tools.DatabaseDataMover;
import com.openexchange.admin.tools.FilestoreDataMover;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;

import java.sql.SQLException;

@OXExceptionSource(
classId=Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXCONTEXT,
        component=Component.ADMIN_CONTEXT
        )
        public class OXContext implements I_OXContext {
    
    static ContextExceptionFactory CONTEXT_EXCEPTIONS = new ContextExceptionFactory(OXContext.class);
    
    private Log log = LogFactory.getLog(this.getClass());
    
    
    // error messages and so on
    public static final String MSG_SQL_QUERY_FAILED = " ";
    public static final String MSG_NO_SUCH_USER_IN_CONTEXT = "No such user %s in context %s";
    public static final String MSG_SQL_OPERATION_ERROR = "SQL operation error";
    public static final String MSG_INTERNAL_ERROR = "Internal error";
    public static final String MSG_INVALID_DATA_SENT = "Invalid data sent";
    public static final String RESPONSE_ERROR = "ERROR";
    public static final String LOG_ERROR = "Error";
    public static final String LOG_RESPONSE = "Response - ";
    public static final String LOG_CLIENT_ERROR = "Client error";
    public static final String LOG_PROBLEM_WITH_DB_POOL = "Problem with database connection pool";
    
    public OXContext() throws RemoteException {
        super();
        log.info( "Class loaded: " + this.getClass().getName() );
    }
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={MSG_SQL_QUERY_FAILED,"Invalid data sent by client"},
            exceptionId={0,1},
            msg={MSG_SQL_OPERATION_ERROR,"Invalid data sent-%s"}
    )
    public Vector searchContextByDatabase(String db_host_url) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(""+db_host_url);
        if(db_host_url!=null){
            try{
                OXContext_MySQL oxcox = new OXContext_MySQL();
                retValue = oxcox.searchContextByDatabase(db_host_url);
            }catch(PoolException ecp){
                log.error(LOG_ERROR,ecp);
                retValue.add(RESPONSE_ERROR);
                retValue.add(""+ecp.getMessage());
            }catch(SQLException sql){
                log.error(MSG_SQL_OPERATION_ERROR,sql);
                retValue.add(RESPONSE_ERROR);
                retValue.add(""+CONTEXT_EXCEPTIONS.create(0).getMessage());
            }
        }else{
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(1,db_host_url).getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={MSG_SQL_QUERY_FAILED,"Invalid data sent by client"},
            exceptionId={2,3},
            msg={MSG_SQL_OPERATION_ERROR,"Invalid data sent-%s"}
    )
    public Vector searchContextByFilestore(String filestore_url){
        Vector<Object> retValue = new Vector<Object>();
        log.debug(""+filestore_url);
        if(filestore_url!=null){
            try{
                OXContext_MySQL oxcox = new OXContext_MySQL();
                retValue = oxcox.searchContextByFilestore(filestore_url);
            }catch(PoolException ecp){
                log.error(LOG_ERROR,ecp);
                retValue.add(RESPONSE_ERROR);
                retValue.add(""+ecp.getMessage());
            }catch(SQLException sql){
                log.error(MSG_SQL_OPERATION_ERROR,sql);
                retValue.add(RESPONSE_ERROR);
                retValue.add(""+CONTEXT_EXCEPTIONS.create(2).getMessage());
            }
        }else{
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(3,filestore_url).getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={MSG_SQL_QUERY_FAILED," "},
            exceptionId={4,5},
            msg={MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s"}
    )
    public Vector changeDatabaseContext(int context_id,Hashtable newdatabasehandle){
        Vector<Object> retValue = new Vector<Object>();
        log.debug(""+context_id+" - "+newdatabasehandle);
        try{
            if( !AdminDaemonTools.existsContext(context_id) ) {
                throw CONTEXT_EXCEPTIONS.create(5,context_id);
            }
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.changeDatabaseContext(context_id,newdatabasehandle);
        }catch(PoolException ecp){
            log.error(LOG_ERROR,ecp);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(4).getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }catch(RemoteException remi){
            log.error(LOG_ERROR,remi);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+remi.getMessage());
        }
        
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={MSG_SQL_QUERY_FAILED," ","invalid quota size"},
            exceptionId={6,7,8},
            msg={MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s","Invalid quota size"}
    )
    public Vector changeStorageData(int context_id,Hashtable new_filestore_handle){
        Vector<String> retValue = new Vector<String>();
        log.debug(""+context_id+" - "+new_filestore_handle);
        try{
            if( !AdminDaemonTools.existsContext(context_id) ) {
                throw CONTEXT_EXCEPTIONS.create(7,context_id);
            }
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.changeStorageData(context_id,new_filestore_handle);
        }catch(PoolException ecp){
            log.error(LOG_ERROR,ecp);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(6).getMessage());
        }catch(QuotaException genxo){
            log.debug(LOG_CLIENT_ERROR,genxo);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(8).getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    

    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR},
          desc={" "},
          exceptionId={48},
          msg={"Unable to disable Context %s"}
    	    )
    private void reEnableContext(final int context_id, final OXContext oxcox) throws ContextException {
    	Vector retValue = null;
    	retValue = oxcox.enableContext(context_id); 
        if( ! retValue.get(0).equals("OK") ||
        		retValue.size()<2) {
            throw CONTEXT_EXCEPTIONS.create(43,context_id);
        }
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT,Category.PROGRAMMING_ERROR,Category.PROGRAMMING_ERROR,Category.PROGRAMMING_ERROR,Category.PROGRAMMING_ERROR,Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={MSG_SQL_QUERY_FAILED," ","not implemented"," "," "," "," "," "," "," "," "," "},
            exceptionId={9,10,11,40,41,42,43,44,45,46,47,49},
            msg={MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s","Not implemented",OXUtilException.NO_SUCH_STORE+" %s",OXUtilException.NO_SUCH_REASON+" %s",OXContextException.CONTEXT_DISABLED+" %s","Unable to disable Context %s","Unable to get Context data %s","Unable to get filestore directory %s","Unable to list filestores","Unable to move filestore","Src and dst store id is the same: %s"}
    )
    public Vector moveContextFilestore( int context_id, int dstStore_id, int reason_id ) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug( "" + context_id + " - " + dstStore_id );
        try {
            if ( !AdminDaemonTools.existsContext( context_id ) ) {
                throw CONTEXT_EXCEPTIONS.create( 10, context_id );
            }
            if ( !AdminDaemonTools.existsStore(dstStore_id)) {
            	throw CONTEXT_EXCEPTIONS.create(40, dstStore_id);
            }
            if(!AdminDaemonTools.existsReason(reason_id)){
                throw CONTEXT_EXCEPTIONS.create(41,reason_id);
            }
            if(!AdminDaemonTools.isContextEnabled(context_id)){
                throw CONTEXT_EXCEPTIONS.create(42,context_id);
            }

            OXContext oxcox = new OXContext();

            // disable context
            retValue = oxcox.disableContext(context_id, reason_id); 
            if( ! retValue.get(0).equals("OK") ||
            		retValue.size()<2) {
                throw CONTEXT_EXCEPTIONS.create(43,context_id);
            }

            // find old context store data 
            retValue = oxcox.getContextSetup(context_id);
            if( ! retValue.get(0).equals("OK") ||
            		retValue.size()<2) {
            	reEnableContext(context_id, oxcox);
            	throw CONTEXT_EXCEPTIONS.create(44,context_id);
            }
            Hashtable csetup = (Hashtable)retValue.get(1);
            int srcStore_id = Integer.parseInt( csetup.get( I_OXContext.CONTEXT_FILESTORE_ID ).toString() );

            if( srcStore_id == dstStore_id ) {
            	reEnableContext(context_id, oxcox);
            	retValue.clear();
            	throw CONTEXT_EXCEPTIONS.create(49,dstStore_id);
            }
            	
            String ctxdir = (String)csetup.get(I_OXContext.CONTEXT_FILESTORE_NAME); 
            if( ctxdir == null ) {
            	reEnableContext(context_id, oxcox);
            	retValue.clear();
                throw CONTEXT_EXCEPTIONS.create(45,context_id);
            }

            // get src and dst path from filestores 
            OXUtil oxu = new OXUtil();
            retValue = oxu.listFilestores("*");
            if( ! retValue.get(0).equals("OK") ||
            		retValue.size()<2) {
            	reEnableContext(context_id, oxcox);
                throw CONTEXT_EXCEPTIONS.create(46);
            }
            
            String src = null;
            String dst = null;

            Hashtable fstores = (Hashtable)retValue.get(1);
            Enumeration<String> fss = fstores.keys();
            while( fss.hasMoreElements() ) {
            	String id = fss.nextElement();
            	Hashtable store = (Hashtable)fstores.get(id);
                String s_url = store.get( I_OXUtil.STORE_URL ).toString();
                
            	if ( id.equals( ""+srcStore_id ) ) {
            		URI uri = new URI( s_url );
                    src = uri.getPath();
                    if ( !src.endsWith( "/" ) ) {
                        src += "/";
                    }
            		src += ctxdir;
                     if ( src.endsWith( "/" ) ) {
                         src = src.substring( 0, src.length() - 1 );
                     }
                    
            	} else if ( id.equals( ""+dstStore_id ) ) {
            		URI uri = new URI( s_url );
            		dst = uri.getPath() ;
                    if ( !dst.endsWith( "/" ) ) {
                        dst += "/";
                    }
                    dst += ctxdir;
                    if ( dst.endsWith( "/" ) ) {
                        dst = dst.substring( 0, dst.length() - 1 );
                    }
                    
            	}
            }

            if( src == null || dst == null ) {
            	if( src == null ) {
            		log.error("src is null");
            	}
            	if( dst == null ) {
            		log.error("dst is null");
            	}
            	reEnableContext(context_id, oxcox);
            	retValue.clear();
            	throw CONTEXT_EXCEPTIONS.create(47);
            }
            
            
            FilestoreDataMover rdm = new FilestoreDataMover(src, dst, context_id, dstStore_id);
            // add to job queue
            retValue.clear();
            retValue.add("OK");
            ClientAdminThread.ajx.addJob(rdm, context_id, dstStore_id, reason_id, AdminJob.Mode.MOVE_FILESTORE);

        } catch ( PoolException ecp ) {
            log.error( LOG_ERROR, ecp );
            retValue.add( RESPONSE_ERROR );
            retValue.add( "" + ecp.getMessage() );
        } catch ( SQLException sql ) {
            log.error( MSG_SQL_OPERATION_ERROR, sql );
            retValue.add( RESPONSE_ERROR );
            retValue.add( "" + CONTEXT_EXCEPTIONS.create(9).getMessage() );
        } catch ( ContextException ctxe ) {
            log.debug( LOG_CLIENT_ERROR, ctxe );
            retValue.add( RESPONSE_ERROR );
            retValue.add( "" + ctxe.getMessage() );
        } catch ( RemoteException e ) {
            log.debug( LOG_ERROR, e );
            retValue.add( RESPONSE_ERROR );
            retValue.add( "" + e.getMessage() );
        } catch (URISyntaxException e) {
            log.debug( LOG_ERROR, e );
            retValue.add( RESPONSE_ERROR );
            retValue.add( "" + e.getMessage() );
		}
        log.debug( LOG_RESPONSE + retValue );
        return retValue;
    }
    

    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={MSG_SQL_QUERY_FAILED," ","not implemented","","",""},
            exceptionId={12,13,14,37,38,39},
            msg={MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s","Not implemented",OXUtilException.NO_SUCH_REASON+" %s","Context %s is already disabled.Move already in progress?","Database with id %s is NOT a master!"}
    )             
    public Vector moveContextDatabase(final int context_id,final int database_id,final int reason_id) {
        Vector<String> retValue = new Vector<String>();
        log.debug(""+context_id+" - "+database_id+" - "+reason_id);
        try{
            
            if(!AdminDaemonTools.existsReason(reason_id)){
                throw CONTEXT_EXCEPTIONS.create(37,reason_id);
            }
            
            if(!AdminDaemonTools.existsContext(context_id)){
                throw CONTEXT_EXCEPTIONS.create(13,context_id);
            }
            
            if(!AdminDaemonTools.isContextEnabled(context_id)){
                throw CONTEXT_EXCEPTIONS.create(38,context_id);
            }
            
            if(!AdminDaemonTools.isMasterDatabase(database_id)){
                throw CONTEXT_EXCEPTIONS.create(39,database_id);
            }

            DatabaseDataMover ddm = new DatabaseDataMover(context_id, database_id, reason_id);
            
            // add to job queue
            retValue.clear();
            retValue.add("OK");
            ClientAdminThread.ajx.addJob(ddm, context_id, database_id, reason_id, AdminJob.Mode.MOVE_DATABASE);

        }catch(PoolException ecp){
            log.error(LOG_ERROR,ecp);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(12).getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());                
        }
        
        
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR},
            desc={MSG_SQL_QUERY_FAILED},
            exceptionId={15},
            msg={MSG_SQL_OPERATION_ERROR}
    )
    public Vector enableAllContexts(){
        Vector<String> retValue = new Vector<String>();
        log.debug("");
        try{
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.enableAllContexts();
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(15).getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={MSG_SQL_QUERY_FAILED,"Invalid data"},
            exceptionId={16,17},
            msg={MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_REASON+" %s"}
    )
    public Vector disableAllContexts(int reason_id){
        Vector<String> retValue = new Vector<String>();
        log.debug(""+reason_id);
        try{
            OXContext_MySQL oxcox = new OXContext_MySQL();
            if(!AdminDaemonTools.existsReason(reason_id)) {
                throw CONTEXT_EXCEPTIONS.create(17);
            }
            retValue = oxcox.disableAllContexts(reason_id);
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(16).getMessage());
        }catch(ContextException ctxe){
        log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.PROGRAMMING_ERROR,Category.PROGRAMMING_ERROR},
            desc={MSG_SQL_QUERY_FAILED,"context already exists",MSG_INTERNAL_ERROR,"Error add Context System Folders"},
            exceptionId={18,19,20,36},
            msg={MSG_SQL_OPERATION_ERROR,OXContextException.CONTEXT_EXISTS+" %s",MSG_INTERNAL_ERROR+"-%s","Context system folders create error"}
    )
    public Vector createContext(int context_id,long quota_max,Hashtable user_container) {
        Vector<String> retValue = new Vector<String>();
        log.debug(""+context_id+" - "+quota_max+" - "+user_container);
        try{
            if( AdminDaemonTools.existsContext(context_id) ) {
                throw CONTEXT_EXCEPTIONS.create(19,context_id);
            }
            AdminCache cache = ClientAdminThread.cache ;
            OXUser.checkCreateUserData(context_id,user_container,cache.getProperties());
            
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.createContext(context_id,quota_max,user_container);
            MonitoringInfos.incrementNumberOfCreateContextCalled();
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(18).getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }catch(NoSuchAlgorithmException ctxe){
            log.debug(LOG_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(20,ctxe.getMessage()).getMessage());
        }catch(UserException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());        
        }catch(DBPoolingException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());        
        }catch(OXContextException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(20,popx.getMessage()).getMessage());
        }catch(OXException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(36).getMessage());
        }catch(RemoteException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(20,popx.getMessage()).getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.USER_INPUT,Category.PROGRAMMING_ERROR,Category.SETUP_ERROR},
            desc={" ",MSG_SQL_QUERY_FAILED,MSG_INTERNAL_ERROR},
            exceptionId={21,22,23},
            msg={OXContextException.NO_SUCH_CONTEXT+" %s",MSG_SQL_OPERATION_ERROR,MSG_INTERNAL_ERROR+"-%s"}
    )
    public Vector deleteContext(int context_id) {
        Vector<String> retValue = new Vector<String>();
        log.debug(""+context_id);
        try{
            if( !AdminDaemonTools.existsContext(context_id) ) {
                throw CONTEXT_EXCEPTIONS.create(21,context_id);
            }
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.deleteContext(context_id);
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(22).getMessage());
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());
        }catch(DBPoolingException pexp){
            log.error("Problem with database connection pool",pexp);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+pexp.getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }catch(OXContextException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(23,popx.getMessage()).getMessage());
        }catch(RemoteException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(23,popx.getMessage()).getMessage());
        }catch(com.openexchange.groupware.contexts.ContextException pexp3){
            log.error("Context error in OX delete API ",pexp3);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+pexp3.getMessage());
        }catch(DeleteFailedException pexp4){
            log.error("Delete error in OX delete API ",pexp4);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+pexp4.getMessage());
        }catch(LdapException pexp5){
            log.error("Delete error in OX delete API ",pexp5);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+pexp5.getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.USER_INPUT,Category.PROGRAMMING_ERROR},
            desc={"invalid data",MSG_SQL_QUERY_FAILED},
            exceptionId={24,25},
            msg={"Invalid data sent-%s",MSG_SQL_OPERATION_ERROR}
    )
    public Vector searchContext(String search_pattern) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(""+search_pattern);
        if(search_pattern!=null){
            try{
                OXContext_MySQL oxcox = new OXContext_MySQL();
                retValue = oxcox.searchContext(search_pattern);
            }catch(SQLException sql){
                log.error(MSG_SQL_OPERATION_ERROR,sql);
                retValue.add(RESPONSE_ERROR);
                retValue.add(""+CONTEXT_EXCEPTIONS.create(25).getMessage());
            }catch(PoolException popx){
                log.error(LOG_ERROR,popx);
                retValue.add(RESPONSE_ERROR);
                retValue.add(""+popx.getMessage());
            }
        }else{
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(24,search_pattern).getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    
    @OXThrowsMultiple(
    category={Category.USER_INPUT,Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ",MSG_SQL_QUERY_FAILED,"Invalid data","context is disabled"},
            exceptionId={26,27,28,29},
            msg={OXContextException.NO_SUCH_CONTEXT+" %s",MSG_SQL_OPERATION_ERROR,OXUtilException.NO_SUCH_REASON+" %s","Context %s is already disabled"}
    )
    public Vector<Object> disableContext(int context_id, int reason_id)  {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(""+context_id+" - "+reason_id);
        try{
            if( !AdminDaemonTools.existsContext(context_id) ) {
                throw CONTEXT_EXCEPTIONS.create(26,context_id);
            }
            if( !AdminDaemonTools.existsReason(reason_id) ) {
                throw CONTEXT_EXCEPTIONS.create(28,reason_id);
            }
            if( !AdminDaemonTools.isContextEnabled(context_id)) {
                throw CONTEXT_EXCEPTIONS.create(29,context_id);
            }
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.disableContext(context_id,reason_id);
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(27).getMessage());
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }
        
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.USER_INPUT,Category.PROGRAMMING_ERROR},
            desc={" ",MSG_SQL_QUERY_FAILED},
            exceptionId={30,31},
            msg={OXContextException.NO_SUCH_CONTEXT+" %s",MSG_SQL_OPERATION_ERROR}
    )
    public Vector<Object> enableContext(int context_id) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(""+context_id);
        try{
            if( !AdminDaemonTools.existsContext(context_id) ) {
                throw CONTEXT_EXCEPTIONS.create(30,context_id);
            }
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.enableContext(context_id);
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(31).getMessage());
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }
        
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.USER_INPUT,Category.PROGRAMMING_ERROR},
            desc={" ",MSG_SQL_QUERY_FAILED},
            exceptionId={32,33},
            msg={OXContextException.NO_SUCH_CONTEXT+" %s",MSG_SQL_OPERATION_ERROR}
    )
    public Vector<Object> getContextSetup(int context_id) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(""+context_id);
        try{
            if(!AdminDaemonTools.existsContext(context_id)){
                throw CONTEXT_EXCEPTIONS.create(32,context_id);
            }
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.getContextSetup(context_id);
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(33).getMessage());
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.USER_INPUT,Category.PROGRAMMING_ERROR},
            desc={" ",MSG_SQL_QUERY_FAILED},
            exceptionId={34,35},
            msg={OXContextException.NO_SUCH_CONTEXT+" %s",MSG_SQL_OPERATION_ERROR}
    )
    public Vector changeQuota(int context_id, long quota_max) throws RemoteException {
        Vector<String> retValue = new Vector<String>();
        log.debug(""+context_id+" - "+quota_max);
        try{
            if( !AdminDaemonTools.existsContext(context_id) ) {
                throw CONTEXT_EXCEPTIONS.create(34,context_id);
            }
            OXContext_MySQL oxcox = new OXContext_MySQL();
            retValue = oxcox.changeQuota(context_id, quota_max);
        }catch(SQLException sql){
            log.error(MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+CONTEXT_EXCEPTIONS.create(35).getMessage());
        }catch(PoolException popx){
            log.error(LOG_ERROR,popx);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+popx.getMessage());
        }catch(ContextException ctxe){
            log.debug(LOG_CLIENT_ERROR,ctxe);
            retValue.add(RESPONSE_ERROR);
            retValue.add(""+ctxe.getMessage());
        }
        log.debug(LOG_RESPONSE+retValue);
        return retValue;
    }

   
}
