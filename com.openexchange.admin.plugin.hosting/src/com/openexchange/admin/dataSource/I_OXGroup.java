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
 * The interface class <code>I_OXGroup</code> defines the Open-Xchange
 * API for creating and manipulating OX groups within a given OX
 * context. Example:
 *<pre>
 *      try{
 *          I_OXGroup rmi_grp = (I_OXGroup)Naming.lookup("rmi://ox-ip.net/"+I_OXGroup.RMI_NAME);
 *          Hashtable newGroup = new Hashtable();
 *          newGroup.put(I_OXGroup.GID, "mygroup");
 *          newGroup.put(I_OXGroup.DISPLAYNAME, "My fine new group");
 *          rmi_grp.createOXGroup(6,newGroup);
 *         }catch(Exception exp){
 *          exp.printStackTrace();
 *         }
 *</pre>
 *
 */
public interface I_OXGroup extends Remote {
    
    
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME         = "OX_AdminDaemon_OXGroup";
    
    
    
    /**
     * Context ID 
     */
    public static final String CID              = "cid";
    
    
    
    /**
     * Key representing the group identifier, value is a <code>String</code>
     */
    public static final String GID              = "identifier";
    
    
    
    /**
     * Key representing the group id number, value must be <code>Integer</code>
     */
    public static final String GID_NUMBER       = "id";
    
    
    
    /**
     * Key representing a groups display name, value is a <code>String</code>
     */
    public static final String DISPLAYNAME      = "displayName";
    
    
    
    /**
     * String array containing fields required to create a new group
     * @see #GID
     * @see #DISPLAYNAME
     */
    public static final String REQUIRED_KEYS_CREATE[]   = { GID, DISPLAYNAME };
    
    
    
    /**
     * String array containing fields required to edit a group
     * @see #GID
     */
    public static final String REQUIRED_KEYS_CHANGE[]   = {  };
    
    
    
    /**
     * Creates a new group within the given context. The
     * <code>Hashtable groupData</code> contains every element a newly
     * created group may or must contain.
     *
     * <p><blockquote><pre>
     *      Hashtable newGroup = new Hashtable();
     *      newGroup.put(I_OXGroup.GID, "mygroup");
     *      newGroup.put(I_OXGroup.DISPLAYNAME, "My fine new group");
     *
     *      Vector result = rmi_oxgroup.createOXGroup(context_ID, newGroup);
     * </pre></blockquote></p>
     *
     * @param context_ID numerical context identifier
     * @param groupData Hash containing group data
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Integer</code> when method succeeds and
     *         contains the group_ID to edit/delete this group.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXGroupException
     * @see #deleteOXGroup( int context_ID, int group_ID )
     */
    public Vector createOXGroup( int context_ID, Hashtable groupData ) throws RemoteException;
    
    /**
     * Get Group data from given group.
     *
     * @param context_ID numerical context identifier
     * @param group_id numerical resource identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Hashtable</code> when method succeeds and
     *         contains the data of this group.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXResourceException
     */
    public Vector getOXGroupData( int context_ID, int group_id ) throws RemoteException;
    
    /**
     * Manipulate group data within the given context.
     *
     * <p><blockquote><pre>
     *      Hashtable changeGroup = new Hashtable();
     *      changeGroup.put(I_OXGroup.DISPLAYNAME, "My new fine edited group");
     *      Vector result = rmi_oxgroup.changeOXGroup(context_ID, group_ID, changeGroup);
     * </pre></blockquote></p>
     *
     * @param context_ID numerical context identifier
     * @param group_ID numerical group identifier
     * @param groupData Hash containing group data
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXGroupException
     */
    public Vector changeOXGroup( int context_ID, int group_ID, Hashtable groupData ) throws RemoteException;
    
    
    
    /**
     * Delete group from given context.
     *
     * @param context_ID numerical context identifier
     * @param group_ID numerical group identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXGroupException
     * @see #createOXGroup( int context_ID, Hashtable groupData )
     */
    public Vector deleteOXGroup( int context_ID, int group_ID ) throws RemoteException;
    
    
    
    /**
     * Add member to group in given context.
     *
     * @param context_ID numerical context identifier
     * @param group_ID numerical group identifier
     * @param member_ids numerical member identifier array
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXGroupException
     */
    public Vector addMember( int context_ID, int group_ID, int [] member_ids ) throws RemoteException;
    
    
    
    /**
     * Remove member from group in given context
     *
     * @param context_ID numerical context identifier
     * @param group_ID numerical group identifier
     * @param member_ids numerical member identifier array
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXGroupException
     */
    public Vector removeMember( int context_ID, int group_ID, int[] member_ids ) throws RemoteException;
    
    
    
    /**
     * Get all members of group in given context
     *
     * @param context_ID numerical context identifier
     * @param group_ID numerical group identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type Vector and contains the member ids as int.
     *         <p>
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXGroupException
     */
    public Vector getMembers( int context_ID, int group_ID ) throws RemoteException;
    
    
    
    /**
     * List groups matching <code>pattern</code> in given context
     * @param context_ID numerical context identifier
     * @param pattern String containing the pattern to search for. E.g "*"
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>String "ERROR"</code> in case of a failure.
     *         <p>
     *         2nd Object is of type <code>Vector</code> when method succeeds and
     *         contains one Hashtable per Group and its Data.
     * @throws RemoteException
     * @see com.openexchange.admin.exceptions.OXGroupException
     */
    public Vector listOXGroups( int context_ID, String pattern ) throws RemoteException;
    
}
