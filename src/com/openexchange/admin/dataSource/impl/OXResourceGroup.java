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

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.dataSource.I_OXResourceGroup;
import com.openexchange.admin.exceptions.OXGenericException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Vector;

//import com.openexchange.admin.dataSource.OXResourceGroup_MySQL;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.admin.tools.PropertyHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


//public class OXResourceGroup extends UnicastRemoteObject implements I_OXResourceGroup {
//    
//    private static final long serialVersionUID = 3314510845014196235L;
//    
//    private AdminCache      cache   = null;
//    private static Log log = LogFactory.getLog(OXResourceGroup.class);
//    private PropertyHandler prop    = null;
//    
//    
//    
//    public OXResourceGroup() throws RemoteException {
//        super();
//        try {
//            cache = ClientAdminThread.cache;
//            prop = cache.getProperties();
//            
//            log.info( "class loaded: " + this.getClass().getName() );
//        } catch ( Exception e ) {
//            log.error("Error "+e);
//        }
//    }
//    
//    
//    
//    public Vector createOXResourceGroup( int context_ID, Hashtable resourceGroupData ) {
//        Vector<String> retValue = new Vector<String>();
//        log.info( "createOXResourceGroup" );
//        try {
//            AdminDaemonTools.checkNeeded( resourceGroupData, I_OXResourceGroup.REQUIRED_KEYS_CREATE );
//            
//            if ( prop.getResourceGroupProp( AdminProperties.ResourceGroup.AUTO_LOWERCASE, true ) ) {
//                String uid = resourceGroupData.get( I_OXResourceGroup.UID ).toString().toLowerCase();
//                resourceGroupData.put( I_OXResourceGroup.UID, uid );
//            }
//            if ( prop.getResourceGroupProp( AdminProperties.ResourceGroup.CHECK_NOT_ALLOWED_CHARS, true ) ) {
//                //AdminDaemonTools.validateResourceGroupName( resourceGroupData.get( I_OXResourceGroup.UID ).toString() );
//            }
//            
//            I_OXResourceGroup oxResGroup = new OXResourceGroup_MySQL();
//            retValue = oxResGroup.createOXResourceGroup( context_ID, resourceGroupData );
//        } catch( Exception exc ) {
//            retValue.clear();
//            retValue.add( OXGenericException.GENERAL_ERROR );
//            retValue.add( exc.toString() );
//            log.error( OXGenericException.GENERAL_ERROR, exc );
//        }
//        return retValue;
//    }
//    
//    
//    
//    public Vector listOXResourceGroups( int context_ID, String pattern ) {
//        Vector<String> retValue = new Vector<String>();
//        log.info( "listOXResourceGroups" );
//        try {
//            I_OXResourceGroup oxResGroup = new OXResourceGroup_MySQL();
//            retValue = oxResGroup.listOXResourceGroups( context_ID, pattern );
//        } catch( Exception exc ) {
//            retValue.clear();
//            retValue.add( OXGenericException.GENERAL_ERROR );
//            retValue.add( exc.toString() );
//            log.error( OXGenericException.GENERAL_ERROR, exc );
//        }
//        return retValue;
//    }
//    
//    
//    
//    public Vector changeOXResourceGroup( int context_ID, int resourceGroup_ID, Hashtable resourceGroupData ) {
//        Vector<String> retValue = new Vector<String>();
//        log.info( "changeOXResourceGroup" );
//        try {
//            if ( prop.getResourceProp( AdminProperties.ResourceGroup.AUTO_LOWERCASE, true ) ) {
//                String uid = resourceGroupData.get( I_OXResourceGroup.UID ).toString().toLowerCase();
//                resourceGroupData.put( I_OXResourceGroup.UID, uid );
//            }
//            
//            if ( prop.getResourceProp( AdminProperties.ResourceGroup.CHECK_NOT_ALLOWED_CHARS, true ) ) {
//                AdminDaemonTools.validateGroupName( resourceGroupData.get( I_OXResourceGroup.UID ).toString() );
//            }
//            
//            I_OXResourceGroup oxResGroup = new OXResourceGroup_MySQL();
//            retValue = oxResGroup.changeOXResourceGroup( context_ID, resourceGroup_ID, resourceGroupData );
//        } catch( Exception exc ) {
//            retValue.clear();
//            retValue.add( OXGenericException.GENERAL_ERROR );
//            retValue.add( exc.toString() );
//            log.error( OXGenericException.GENERAL_ERROR, exc );
//        }
//        return retValue;
//    }
//    
//    
//    
//    public Vector addResource( int context_ID, int resourceGroup_ID, int resource_ID ) {
//        Vector<String> retValue = new Vector<String>();
//        log.info( "addResource" );
//        try {
//            I_OXResourceGroup oxResGroup = new OXResourceGroup_MySQL();
//            retValue = oxResGroup.addResource( context_ID, resourceGroup_ID, resource_ID );
//        } catch( Exception exc ) {
//            retValue.clear();
//            retValue.add( OXGenericException.GENERAL_ERROR );
//            retValue.add( exc.toString() );
//            log.error( OXGenericException.GENERAL_ERROR, exc );
//        }
//        return retValue;
//    }
//    
//    
//    
//    public Vector dropResource( int context_ID, int resourceGroup_ID, int resource_ID ) {
//        Vector<String> retValue = new Vector<String>();
//        log.info( "dropResource" );
//        try {
//            I_OXResourceGroup oxResGroup = new OXResourceGroup_MySQL();
//            retValue = oxResGroup.dropResource( context_ID, resourceGroup_ID, resource_ID );
//        } catch( Exception exc ) {
//            retValue.clear();
//            retValue.add( OXGenericException.GENERAL_ERROR );
//            retValue.add( exc.toString() );
//            log.error( OXGenericException.GENERAL_ERROR, exc );
//        }
//        return retValue;
//    }
//    
//    
//    
//    public Vector deleteOXResourceGroup( int context_ID, int resourceGroup_ID ) {
//        Vector<String> retValue = new Vector<String>();
//        log.info( "deleteOXResourceGroup" );
//        try {
//            I_OXResourceGroup oxResGroup = new OXResourceGroup_MySQL();
//            retValue = oxResGroup.deleteOXResourceGroup( context_ID, resourceGroup_ID );
//        } catch( Exception exc ) {
//            retValue.clear();
//            retValue.add( OXGenericException.GENERAL_ERROR );
//            retValue.add( exc.toString() );
//            log.error( OXGenericException.GENERAL_ERROR, exc );
//        }
//        return retValue;
//    }
//    
//    
//    
//    public Vector getResources( int context_ID, int resourceGroup_ID ) throws RemoteException {
//        Vector<String> retValue = new Vector<String>();
//        log.info( "getResources" );
//        try {
//            I_OXResourceGroup oxResGroup = new OXResourceGroup_MySQL();
//            retValue = oxResGroup.getResources( context_ID, resourceGroup_ID );
//        } catch( Exception exc ) {
//            retValue.clear();
//            retValue.add( OXGenericException.GENERAL_ERROR );
//            retValue.add( exc.toString() );
//            log.error( OXGenericException.GENERAL_ERROR, exc );
//        }
//        return retValue;
//    }
//}
