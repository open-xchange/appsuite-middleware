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
 * The interface class <code>I_OXResource</code> defines the Open-Xchange
 * API for creating and manipulating OX resources within a given OX
 * context. Example:
 *<pre>
 *      try{
 *          I_OXResource rmi_res = (I_OXResource)Naming.lookup("rmi://ox-ip.net/"+I_OXResource.RMI_NAME);
 *          Hashtable newRes = new Hashtable();
 *          newRes.put(rmi_res.RID, "myres");
 *          newRes.put(rmi_res.DISPLAYNAME, "My fine new res");
 *          rmi_res.createOXResource(6,newRes);
 *         }catch(Exception exp){
 *          exp.printStackTrace();
 *         }
 *</pre>
 *
 */
public interface I_OXResource extends Remote {
    
    
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME         = "OX_AdminDaemon_OXResource";
    
    
    
    /**
     * Key representing the resource identifier, value is a <code>String</code>
     */
    public static final String RID              = "identifier";
    
    
    
    /**
     * Key representing the resource identifier number, value must be <code>Integer</code>
     */
    public static final String RID_NUMBER       = "id";
    
    
    
    /**
     * Possible Key for the <code>Hashtable resourceData</code>, Key representing if resource is activated, value would be a <code>Boolean</code>
     */
    public static final String AVAILABLE        = "available";
    
    
    
    /**
     * Key representing a resources detailed name, value is a <code>String</code>
     */
    public static final String DISPLAYNAME      = "displayName";
    
    
    
    /**
     * Key representing a resources description, value is a <code>String</code>
     */
    public static final String DESCRIPTION      = "description";
    

    /**
     * Key representing the primary mail address , value is a <code>String</code>
     */
    public static final String PRIMARY_MAIL                = "mail";
    
    
    /**
     * String array containing fields required to create a new resource
     * @see #RID
     * @see #DISPLAYNAME
     * @see #AVAILABLE
     */
    public static final String REQUIRED_KEYS_CREATE[]   = { RID, DISPLAYNAME, AVAILABLE, PRIMARY_MAIL };
    
    
    
    /**
     * String array containing fields required to edit a resource
     * @see #RID
     * @see #RID_NUMBER
     */
    public static final String REQUIRED_KEYS_CHANGE[]   = {  };
    
    
    
    /**
     * Creates a new resource within the given context. The
     * <code>Hashtable resourceData</code> contains every element a newly
     * created resource may or must contain.
     *
     * <p><blockquote><pre>
     *      Hashtable newRes = new Hashtable();
     *      newRes.put(I_OXResource.RID, "myres");
     *      newRes.put(I_OXResource.DISPLAYNAME, "My fine new resource");
     *
     *      Vector result = rmi_res.createOXResource(context_ID, newRes);
     * </pre></blockquote></p>
     * 
     * @param context_ID numerical context identifier
     * @param resData Hash containing resource data
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceException
     */
    public Vector createOXResource( int context_ID, Hashtable resData ) throws RemoteException;
    
    
    
    /**
     * Manipulate resource data within the given context.
     * 
     * <p><blockquote><pre>
     *      Hashtable changeRes = new Hashtable();
     *      changeRes.put(I_OXResource.RID, "mynewres");
     *      changeRes.put(I_OXResource.DISPLAYNAME, "My new fine resource");
     *
     *      Vector result = rmi_res.changeOXResource(context_ID, changeRes);
     * </pre></blockquote></p>
     * 
     * @param context_ID numerical context identifier
     * @param resource_ID numerical resource identifier
     * @param resData Hash containing resource data
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceException
     */
    public Vector changeOXResource( int context_ID, int resource_ID, Hashtable resData ) throws RemoteException;
    
    
    
    /**
	 * Get Resource data from given context.
	 * 
	 * @param context_ID numerical context identifier
	 * @param resource_ID numerical resource identifier
	 * @return Vector containing return code and/or result objects
	 *         <p>
	 *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
	 *         when method succeeds and <code>"ERROR"</code> in case of a failure.
         *         </p>
         *         2nd Object is of type <code>Hashtable</code> and contains the resource data.
	 *         
	 * @throws RemoteException
	 * @see com.openexchange.admin.exceptions.OXResourceException
	 */
	public Vector getOXResourceData( int context_ID, int resource_ID ) throws RemoteException;



	/**
     * Delete resource from given context.
     * 
     * @param context_ID numerical context identifier
     * @param resource_ID numerical resource identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceException
     */
    public Vector deleteOXResource( int context_ID, int resource_ID ) throws RemoteException;
    
    
    
    /**
     * List resources matching <code>pattern</code> in given context
     * 
     * @param context_ID numerical context identifier
     * @param pattern like * or mon* (e.g. for monitor)
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Vector</code> with one <code>Hashtable</code> for each resource when method succeeds and
     *         contains an error message in case of an error.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceException
     */
    public Vector listOXResources( int context_ID, String pattern ) throws RemoteException;
       
}
