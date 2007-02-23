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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;




/**
 * The interface class <code>I_OXResourceGroup</code> defines the Open-Xchange
 * API for creating and manipulating OX resource groups within a given OX
 * context. Example:
 *<pre>
 *      try{
 *          I_OXResourceGroup rmi_resgrp = (I_OXResourceGroup)Naming.lookup("rmi://ox-ip.net/"+I_OXResourceGroup.RMI_NAME);
 *          Hashtable newResGroup = new Hashtable();
 *          newResGroup.put(rmi_resgrp.UID, "myresgroup");
 *          newResGroup.put(rmi_resgrp.DISPLAYNAME, "My fine new resgroup");
 *          rmi_resgrp.createOXResourceGroup(6,newResGroup);
 *         }catch(Exception exp){
 *          exp.printStackTrace();
 *         }
 *</pre>
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> , 
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> , 
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public interface I_OXResourceGroup extends Remote {
    
    
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME             = "OX_AdminDaemon_OXResourceGroup";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceGroupData</code>, value would be an <code>Integer</code>
     */
    public static final String CID                  = "cid";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceGroupData</code>, Key representing the resource group identifier, value is a <code>String</code>
     */
    public static final String UID                  = "identifier";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceGroupData</code>, value would be an <code>Vector</code> with values <code>Integer</code>
     * @see I_OXResource#RID_NUMBER
     */
    public static final String MEMBERS              = "member";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceGroupData</code>, Key representing the resource group identifier number, value would be an <code>Integer</code>
     */
    public static final String RESGROUPID_NUMBER    = "id";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceGroupData</code>, Key representing if resource group is activated, value would be a <code>Boolean</code>
     */
    public static final String AVAILABLE            = "available";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceGroupData</code>, Key representing a resource groups detailed name, value is a <code>String</code>
     */
    public static final String DISPLAYNAME          = "displayName";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceGroupData</code>, Key representing a resource groups extendet informations, value is a <code>String</code>
     */
    public static final String DESCRIPTION          = "description";
    
    
    
    /**
     * String array containing fields required to create a new resource group
     * @see #UID
     * @see #DISPLAYNAME
     * @see #AVAILABLE
     */
    public static final String REQUIRED_KEYS_CREATE[]   = { UID, DISPLAYNAME, AVAILABLE };
    
    
    
    /**
     * String array containing fields required to edit a resource group
     * @see #UID
     * @see #RESGROUPID_NUMBER
     */
    public static final String REQUIRED_KEYS_CHANGE[]   = { UID, RESGROUPID_NUMBER };
    
    
    
    /**
     * Creates a new resource group within the given context. The
     * <code>Hashtable resourceGroupData</code> contains every element a newly
     * created resource group may or must contain.
     *
     * <p><blockquote><pre>
     *      Hashtable newResGroup = new Hashtable();
     *      newResGroup.put(rmi_resgrp.UID, "myresgroup");
     *      newResGroup.put(rmi_resgrp.DISPLAYNAME, "My fine new resource group");
     *
     *      Vector result = rmi_resgrp.createOXResourceGroup(context_ID, newResGroup);
     * </pre></blockquote></p>
     * 
     * @param context_ID numerical context identifier
     * @param resourceGroupData Hash containing resource group data
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceGroupException
     */
    public Vector createOXResourceGroup( int context_ID, Hashtable resourceGroupData ) throws RemoteException;
    
    
    
    /**
     * List resource groups matching <code>pattern</code> in given context
     * 
     * @param context_ID numerical context identifier
     * @param pattern like * or hard* (e.g. for hardware)
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Vector</code> with one <code>Hashtable</code> for each resource group when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceGroupException
     */
    public Vector listOXResourceGroups( int context_ID, String pattern ) throws RemoteException;
    
    
    
    /**
     * Manipulate resource group data within the given context.
     *
     * <p><blockquote><pre>
     *      Hashtable changeResGroup = new Hashtable();
     *      changeResGroup.put(rmi_resgrp.UID, "mynewresgroup");
     *      changeResGroup.put(rmi_resgrp.DISPLAYNAME, "My new fine resource group");
     *
     *      Vector result = rmi_resgrp.changeOXResourceGroup(context_ID, changeResGroup);
     * </pre></blockquote></p>
     * 
     * @param context_ID numerical context identifier
     * @param resourceGroup_ID numerical resource group identifier
     * @param resourceGroupData
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceGroupException
     */
    public Vector changeOXResourceGroup( int context_ID, int resourceGroup_ID, Hashtable resourceGroupData ) throws RemoteException;
    
    
    
    /**
     * Add resource to resource group in given context.
     * 
     * @param context_ID numerical context identifier
     * @param resourceGroup_ID numerical resource group identifier
     * @param resource_ID numerical resource identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceGroupException
     */
    public Vector addResource( int context_ID, int resourceGroup_ID, int resource_ID ) throws RemoteException;
    
    
    
    /**
     * Get all resources of resource group in given context
     * 
     * @param context_ID numerical context identifier
     * @param resourceGroup_ID numerical resource group identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Vector</code> with <code>Integer</code> values when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceGroupException
     */
    public Vector getResources( int context_ID, int resourceGroup_ID ) throws RemoteException;
    
    
    
    /**
     * Drop resource from resource group in given context
     * 
     * @param context_ID numerical context identifier
     * @param resourceGroup_ID numerical resource group identifier
     * @param resource_ID numerical resource identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceGroupException
     */
    public Vector dropResource( int context_ID, int resourceGroup_ID, int resource_ID ) throws RemoteException;
    
    
    
    /**
     * Delete resource group from given context.
     * 
     * @param context_ID Delete group from given context.
     * @param resourceGroup_ID numerical resource group identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceGroupException
     */
    public Vector deleteOXResourceGroup( int context_ID, int resourceGroup_ID ) throws RemoteException;

}
