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
package com.openexchange.admin.rmi;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * This interface defines the Open-Xchange API Version 2 for creating and manipulating OX groups within a OX context.
 *
 * @author cutmasta
 *
 */
public interface OXGroupInterface extends Remote {
    
    
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME  = "OXGroup_V2";
    
    
    /**
     * Create new group in given context.
     * @param ctx Context object.
     * @param grp Group which should be created.
     * @param auth Credentials for authenticating against server.
     * @return int containing the id of the new group.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public int create(Context ctx,Group grp,Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
    
    /**
     * Fetch a group from server.
     * @param ctx Context object
     * @param grp the group to retrieve from server.
     * @param auth Credentials for authenticating against server.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @return The Group with its data.
     */
    public Group get(Context ctx, final Group grp,Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
  
    /**
     * Change group data in given context
     * @param ctx Context object
     * @param grp Group to change.
     * @param auth Credentials for authenticating against server.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public void change(Context ctx,Group grp,Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;    
    
    
    /**
     * Delete group within given context.
     * @param ctx Context object
     * @param grps Contains all groups which should be deleted from the server.
     * @param auth Credentials for authenticating against server.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public void delete(Context ctx, Group[] grps,Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
    /**
     * Delete group within given context.
     * @param ctx Context object
     * @param grps Contains all groups which should be deleted from the server.
     * @param auth Credentials for authenticating against server.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public void delete(final Context ctx, final Group grp, final Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
    /**
     * Adds a new member to the group within given context.
     * @param ctx Context object
     * @param grp_id The ID of the group in which the new members should be added.
     * @param member_ids User IDs.
     * @param auth Credentials for authenticating against server.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public void addMember(Context ctx, final Group grp, int [] member_ids, Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
    
    /**
     * Remove member(s) from group.
     * @param ctx Context object
     * @param grp the group from which the members should be removed.
     * @param member_ids User IDs.
     * @param auth Credentials for authenticating against server.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public void removeMember(Context ctx, final Group grp,int[] member_ids, Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
   
    /**
     * Get User IDs of the members of this group.
     * @param ctx Context object
     * @param grp group from which to retrieve the members.
     * @param auth Credentials for authenticating against server.
     * @return User IDs.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public int[] getMembers(Context ctx,final Group grp,Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
    
    /**
     * List groups whithin context.
     * @param ctx Context object.
     * @param pattern Search pattern to search for e.g. "*mygroup*"
     * @param auth Credentials for authenticating against server.
     * @return Groups which matched the supplied search pattern.
     * @throws RemoteException General RMI Exception
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     */
    public Group[] list(Context ctx, String pattern,Credentials auth) 
    throws RemoteException,InvalidCredentialsException,NoSuchContextException,StorageException,InvalidDataException;
    
}
