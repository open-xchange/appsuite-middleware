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
/*
 * $Id$
 */
package com.openexchange.admin.dataSource.impl;

import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

// import com.openexchange.admin.adminConsole.OXResourceGroup;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.dataSource.I_OXResource;
import com.openexchange.admin.dataSource.OXResource_MySQL;
import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.OXResourceException;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.exceptions.ResourceException;
import com.openexchange.admin.exceptions.ResourceExceptionFactory;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.monitoring.MonitoringInfos;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@OXExceptionSource(
classId=Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXRESOURCE,
        component=Component.ADMIN_RESOURCE
        )
        public class OXResource implements I_OXResource {
    
    static ResourceExceptionFactory RESOURCE_EXCEPTIONS = new ResourceExceptionFactory(OXResource.class);
    private static final long serialVersionUID = -7012370962672596682L;
    
    private AdminCache      cache   = null;
    private PropertyHandler prop    = null;
    private Log log = LogFactory.getLog(this.getClass());
    
    public OXResource() throws RemoteException {
        super();
        try {
            cache = ClientAdminThread.cache;
            prop = cache.getProperties();
            log.info( "Class loaded: " + this.getClass().getName() );
        } catch ( Exception e ) {
            log.error("Error init OXResource",e);
        }
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ","Invalid data sent by client","Resource already exists",""},
            exceptionId={0,1,2,3},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,"Invalid data sent-%s",OXResourceException.RESOURCE_EXISTS+" %s",OXContextException.NO_SUCH_CONTEXT+" %s"}
    )
    public Vector createOXResource( int context_id, Hashtable resData ) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id+" - "+resData);
        try {
            
            if(!AdminDaemonTools.existsContext(context_id) ) {
                throw RESOURCE_EXCEPTIONS.create(3,context_id);
            }
            
            if (AdminDaemonTools.existsResource( context_id, ""+resData.get(I_OXResource.RID),-1 ) ) {
                throw RESOURCE_EXCEPTIONS.create(2,""+resData.get(I_OXResource.RID));
            }
            
            try{
                AdminDaemonTools.checkNeeded( resData, I_OXResource.REQUIRED_KEYS_CREATE );
            }catch(OXGenericException oxgen){
                throw RESOURCE_EXCEPTIONS.create(1,oxgen.getMessage());
            }
            
            if ( prop.getResourceProp( AdminProperties.Resource.AUTO_LOWERCASE, true ) ) {
                String uid = resData.get( I_OXResource.RID ).toString().toLowerCase();
                resData.put( I_OXResource.RID, uid );
            }
            
            if ( prop.getResourceProp( AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true ) ) {
                try{
                    AdminDaemonTools.validateResourceName( resData.get( I_OXResource.RID ).toString() );
                }catch(OXResourceException oxres){
                    throw RESOURCE_EXCEPTIONS.create(1,oxres.getMessage());
                }
            }
            
            OXResource_MySQL oxRes = new OXResource_MySQL();
            retValue = oxRes.createOXResource( context_id, resData );
            MonitoringInfos.incrementNumberOfCreateResourceCalled();
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+RESOURCE_EXCEPTIONS.create(0).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(ResourceException exc){
            log.debug(OXContext.LOG_CLIENT_ERROR, exc );
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(exc.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={" "," "},
            exceptionId={4,5},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s"}
    )
    public Vector listOXResources( int context_id, String pattern ) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_id+" - "+pattern);
        try {
            
            if(!AdminDaemonTools.existsContext(context_id) ) {
                throw RESOURCE_EXCEPTIONS.create(5,context_id);
            }
            
            OXResource_MySQL oxRes = new OXResource_MySQL();
            retValue = oxRes.listOXResources( context_id, pattern );
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+RESOURCE_EXCEPTIONS.create(4).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(ResourceException resx){
            log.debug(OXContext.LOG_CLIENT_ERROR,resx);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(resx.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," ","no such resource","invalid data sent"},
            exceptionId={6,7,8,9},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXResourceException.NO_SUCH_RESOURCE+" %s","Invalid data sent-%s"}
    )
    public Vector changeOXResource( int context_id, int resource_ID, Hashtable resData ) {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_id+" - "+resource_ID+" - "+resData);
        try {
            
            if(!AdminDaemonTools.existsContext(context_id) ) {
                throw RESOURCE_EXCEPTIONS.create(7,context_id);
            }
            
            if (!AdminDaemonTools.existsResource( context_id, resource_ID ) ) {
                throw RESOURCE_EXCEPTIONS.create(8,resource_ID);
            }
            
            if ( resData.containsKey(I_OXResource.RID) &&
                    prop.getResourceProp( AdminProperties.Resource.AUTO_LOWERCASE, true ) ) {
                String rid = resData.get( I_OXResource.RID ).toString().toLowerCase();
                resData.put( I_OXResource.RID, rid );
            }
            
            if ( resData.containsKey(I_OXResource.RID) &&
                    prop.getResourceProp( AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true ) ) {
                try{
                    AdminDaemonTools.validateResourceName( resData.get( I_OXResource.RID ).toString() );
                }catch(OXResourceException xres){
                    throw RESOURCE_EXCEPTIONS.create(9,xres.getMessage());
                }
            }
            
            OXResource_MySQL oxRes = new OXResource_MySQL();
            retValue = oxRes.changeOXResource( context_id, resource_ID, resData );
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+RESOURCE_EXCEPTIONS.create(6).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(ResourceException rexexp){
            log.debug(OXContext.LOG_CLIENT_ERROR,rexexp);
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(rexexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," ","no such resource"},
            exceptionId={10,11,12},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXResourceException.NO_SUCH_RESOURCE+" %s"}
    )
    public Vector deleteOXResource( int context_id, int resource_ID ) {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_id+" - "+resource_ID);
        try {
            
            if(!AdminDaemonTools.existsContext(context_id) ) {
                throw RESOURCE_EXCEPTIONS.create(11,context_id);
            }
            
            if (!AdminDaemonTools.existsResource( context_id, resource_ID ) ) {
                throw RESOURCE_EXCEPTIONS.create(12,resource_ID);
            }
            
            OXResource_MySQL oxRes = new OXResource_MySQL();
            retValue = oxRes.deleteOXResource( context_id, resource_ID );
        }catch(DBPoolingException dbp){
            log.error(OXContext.LOG_ERROR,dbp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+dbp.getMessage());
        }catch(LdapException ldp){
            log.error(OXContext.MSG_INTERNAL_ERROR,ldp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ldp.getMessage());
        }catch(DeleteFailedException dexp){
            log.error(OXContext.MSG_INTERNAL_ERROR,dexp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+dexp.getMessage());
        }catch(ContextException ctx){
            log.error(OXContext.MSG_INTERNAL_ERROR,ctx);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ctx.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+RESOURCE_EXCEPTIONS.create(10).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        } catch( ResourceException exc ) {
            log.debug(OXContext.LOG_CLIENT_ERROR,exc);
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(exc.getMessage());
        }
        
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," ","no such resource"},
            exceptionId={13,14,15},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXResourceException.NO_SUCH_RESOURCE+" %s"}
    )
    public Vector getOXResourceData(int context_ID, int resource_ID) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_ID+" - "+resource_ID);
        try {
            
            if(!AdminDaemonTools.existsContext(context_ID) ) {
                throw RESOURCE_EXCEPTIONS.create(14,context_ID);
            }
            
            if (!AdminDaemonTools.existsResource( context_ID, resource_ID )) {
                throw RESOURCE_EXCEPTIONS.create(15,resource_ID);
            }
            
            OXResource_MySQL oxRes = new OXResource_MySQL();
            retValue = oxRes.getOXResourceData( context_ID, resource_ID );
            
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+RESOURCE_EXCEPTIONS.create(13).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(ResourceException rexexp){
            log.debug(OXContext.LOG_CLIENT_ERROR,rexexp);
            retValue.clear();
            retValue.add( OXContext.RESPONSE_ERROR );
            retValue.add(rexexp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
}
