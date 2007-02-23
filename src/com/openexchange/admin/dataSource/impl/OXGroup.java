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

import com.openexchange.admin.exceptions.GroupException;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.dataSource.I_OXGroup;
import com.openexchange.admin.dataSource.OXGroup_MySQL;
import com.openexchange.admin.exceptions.Classes;
import com.openexchange.admin.exceptions.GroupExceptionFactory;
import com.openexchange.admin.exceptions.OXContextException;
import com.openexchange.admin.exceptions.OXGroupException;
import com.openexchange.admin.exceptions.OXUserException;
import com.openexchange.admin.exceptions.PoolException;
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
classId=Classes.COM_OPENEXCHANGE_ADMIN_DATASOURCE_OXGROUP,
        component=Component.ADMIN_GROUP
        )
        public class OXGroup implements I_OXGroup {
    
    private static final long serialVersionUID = -8949889293005549513L;
    static GroupExceptionFactory GROUP_EXCEPTIONS = new GroupExceptionFactory(OXGroup.class);
    private AdminCache      cache   = null;
    private Log log = LogFactory.getLog(this.getClass());
    private PropertyHandler prop    = null;
    
    
    public OXGroup() throws RemoteException {
        super();
        try {
            cache = ClientAdminThread.cache;
            prop = cache.getProperties();
            log.info( "Class loaded: " + this.getClass().getName() );
        } catch ( Exception e ) {
            log.error("Error init OXGroup",e);
        }
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ","Invalid data sent by client"," ","group already exists"},
            exceptionId={0,1,2,3},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,"Invalid data sent-%s",OXContextException.NO_SUCH_CONTEXT+" %s",OXGroupException.GROUP_EXISTS+""}
    )
    public Vector createOXGroup( int context_ID, Hashtable groupData ) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_ID+" - "+groupData);
        try {
            
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(2,context_ID);
            }
            try{
                AdminDaemonTools.checkNeeded( groupData, I_OXGroup.REQUIRED_KEYS_CREATE);
            }catch(OXGenericException ex){
                throw GROUP_EXCEPTIONS.create(1,ex.getMessage());
            }
            if ( prop.getGroupProp( AdminProperties.Group.AUTO_LOWERCASE, true ) ) {
                String gid = groupData.get( I_OXGroup.GID ).toString().toLowerCase();
                groupData.put( I_OXGroup.GID, gid );
            }
            
            if ( prop.getGroupProp( AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true ) ) {
                try{
                    AdminDaemonTools.validateGroupName( groupData.get( I_OXGroup.GID ).toString() );
                }catch(OXGroupException ox){
                    throw GROUP_EXCEPTIONS.create(1,ox.getMessage());
                }
            }
            
            if ( AdminDaemonTools.existsGroup( context_ID, groupData.get( I_OXGroup.GID ).toString() ) ) {
                throw GROUP_EXCEPTIONS.create(3);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.createOXGroup( context_ID, groupData );
            MonitoringInfos.incrementNumberOfCreateGroupCalled();
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(0).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(GroupException grp){
            log.debug(OXContext.LOG_CLIENT_ERROR,grp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+grp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT},
            desc={" "," "},
            exceptionId={4,6},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s"}
    )
    public Vector listOXGroups( int context_ID, String pattern ) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_ID+" - "+pattern);
        try {
            
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(6,context_ID);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.listOXGroups( context_ID, pattern );
            
        }catch(GroupException grp){
            log.debug(OXContext.LOG_CLIENT_ERROR,grp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+grp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(4).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," "," "},
            exceptionId={7,8,9},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXGroupException.NO_SUCH_GROUP+" %s"}
    )
    public Vector getOXGroupData( int context_ID, int group_id ) {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_ID+" - "+group_id);
        try {
            
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(8,context_ID);
            }
            
            if ( !AdminDaemonTools.existsGroup( context_ID, group_id ) ) {
                throw GROUP_EXCEPTIONS.create(9,group_id);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.getOXGroupData(context_ID,group_id);
            
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(7).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        } catch( GroupException exc ) {
            log.debug(OXContext.LOG_CLIENT_ERROR,exc);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(exc.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," "," ","invalid data sent"},
            exceptionId={10,11,12,13},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXGroupException.NO_SUCH_GROUP+" %s","Invalid data sent-%s"}
    )
    public Vector changeOXGroup( int context_ID, int group_ID, Hashtable groupData ) {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_ID+" - "+group_ID+" - "+groupData);
        try {
            
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(11,context_ID);
            }
            
            try{
                AdminDaemonTools.checkNeeded( groupData, I_OXGroup.REQUIRED_KEYS_CHANGE );
            }catch(OXGenericException oxgen){
                throw GROUP_EXCEPTIONS.create(13,oxgen.getMessage());
            }
            
            if ( groupData.containsKey(I_OXGroup.GID) &&
                    prop.getGroupProp( AdminProperties.Group.AUTO_LOWERCASE, true ) ) {
                String gid = groupData.get( I_OXGroup.GID ).toString().toLowerCase();
                groupData.put( I_OXGroup.GID, gid );
            }
            
            if ( groupData.containsKey(I_OXGroup.GID) &&
                    prop.getGroupProp( AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, true ) ) {
                try{
                    AdminDaemonTools.validateGroupName( groupData.get( I_OXGroup.GID ).toString() );
                }catch(OXGroupException ox){
                    throw GROUP_EXCEPTIONS.create(13,ox.getMessage());
                }
            }
            
            if ( !AdminDaemonTools.existsGroup( context_ID, group_ID ) ) {
                throw GROUP_EXCEPTIONS.create(12,group_ID);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.changeOXGroup( context_ID, group_ID, groupData );
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(10).getMessage());
        } catch(GroupException exc ) {
            log.debug(OXContext.LOG_CLIENT_ERROR,exc);
            retValue.clear();
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(exc.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," "," ","no such user","member already exists in group"},
            exceptionId={14,15,16,17,18},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXGroupException.NO_SUCH_GROUP+" %s",OXUserException.NO_SUCH_USER,OXGroupException.HAVE_THIS_MEMBER}
    )
    public Vector addMember( int context_ID, int group_ID, int [] member_ids ) {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_ID+" - "+group_ID+" - "+Arrays.toString(member_ids));
        try {
            
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(15,context_ID);
            }
            
            if ( !AdminDaemonTools.existsGroup( context_ID, group_ID ) ) {
                throw GROUP_EXCEPTIONS.create(16,group_ID);
            }
            
            if ( !AdminDaemonTools.existsUser(context_ID,member_ids) ) {
                throw GROUP_EXCEPTIONS.create(17);
            }
            
            if ( AdminDaemonTools.existsGroupMember( context_ID, group_ID, member_ids ) ) {
                throw GROUP_EXCEPTIONS.create(18);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.addMember( context_ID, group_ID, member_ids );
            
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(14).getMessage());
        }catch(GroupException grp){
            log.debug(OXContext.LOG_CLIENT_ERROR,grp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(grp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," "," ","no such user"},
            exceptionId={19,20,21,22},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXGroupException.NO_SUCH_GROUP+" %s",OXUserException.NO_SUCH_USER}
    )
    public Vector removeMember( int context_ID, int group_ID, int [] member_ids ) {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_ID+" - "+group_ID+" - "+Arrays.toString(member_ids));
        
        try {
            
            if ( !AdminDaemonTools.existsUser(context_ID,member_ids) ) {
                throw GROUP_EXCEPTIONS.create(22);
            }
            
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(20,context_ID);
            }
            
            if ( !AdminDaemonTools.existsGroup( context_ID, group_ID ) ) {
                throw GROUP_EXCEPTIONS.create(21,group_ID);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.removeMember( context_ID, group_ID, member_ids );
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(19).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(GroupException grp){
            log.debug(OXContext.LOG_CLIENT_ERROR,grp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(grp.getMessage());
        }
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT,Category.USER_INPUT},
            desc={" "," "," ","group cannot be deleted"},
            exceptionId={23,24,25,26},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXGroupException.NO_SUCH_GROUP+" %s","Group with id %s cannot be deleted"}
    )
    public Vector deleteOXGroup( int context_ID, int group_ID ) {
        Vector<String> retValue = new Vector<String>();
        log.debug(context_ID+" - "+group_ID);
        try {
            
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(24,context_ID);
            }
            
            // should we allow of deleting the users group?
            if(group_ID==1){
                throw GROUP_EXCEPTIONS.create(26,group_ID);
            }
            
            if ( !AdminDaemonTools.existsGroup( context_ID, group_ID ) ) {
                throw GROUP_EXCEPTIONS.create(25,group_ID);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.deleteOXGroup( context_ID, group_ID );
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(23).getMessage());
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(GroupException grp){
            log.debug(OXContext.LOG_CLIENT_ERROR,grp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(grp.getMessage());
        }catch(DeleteFailedException defxp){
            log.error(OXContext.LOG_ERROR,defxp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+defxp.getMessage());
        }catch(LdapException defxp){
            log.error(OXContext.LOG_ERROR,defxp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+defxp.getMessage());
        }catch(DBPoolingException depx){
            log.error(OXContext.LOG_ERROR,depx);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+depx.getMessage());
        }catch(ContextException depx){
            log.error(OXContext.LOG_ERROR,depx);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+depx.getMessage());
        }
        
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
    
    @OXThrowsMultiple(
    category={Category.PROGRAMMING_ERROR,Category.USER_INPUT,Category.USER_INPUT},
            desc={" ","" ," "},
            exceptionId={27,28,29},
            msg={OXContext.MSG_SQL_OPERATION_ERROR,OXContextException.NO_SUCH_CONTEXT+" %s",OXGroupException.NO_SUCH_GROUP+" %s"}
    )
    public Vector getMembers( int context_ID, int group_ID ) throws RemoteException {
        Vector<Object> retValue = new Vector<Object>();
        log.debug(context_ID+" - "+group_ID);
        
        try{
            if( !AdminDaemonTools.existsContext(context_ID) ) {
                throw GROUP_EXCEPTIONS.create(28,context_ID);
            }
            
            if ( !AdminDaemonTools.existsGroup( context_ID, group_ID ) ) {
                throw GROUP_EXCEPTIONS.create(29,group_ID);
            }
            
            OXGroup_MySQL oxGroup = new OXGroup_MySQL();
            retValue = oxGroup.getMembers( context_ID, group_ID );
        }catch(PoolException ecp){
            log.error(OXContext.LOG_ERROR,ecp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+ecp.getMessage());
        }catch(SQLException sql){
            log.error(OXContext.MSG_SQL_OPERATION_ERROR,sql);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(""+GROUP_EXCEPTIONS.create(27).getMessage());
        }catch(GroupException grp){
            log.debug(OXContext.LOG_CLIENT_ERROR,grp);
            retValue.add(OXContext.RESPONSE_ERROR);
            retValue.add(grp.getMessage());
        }
        
        log.debug(OXContext.LOG_RESPONSE+retValue);
        return retValue;
    }
    
}
