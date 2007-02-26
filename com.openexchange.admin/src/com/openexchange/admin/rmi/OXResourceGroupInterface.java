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
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.ResourceGroup;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This defines the Open-Xchange API Version 2 for creating and manipulating OX
 * resource groups within a OXcontext.
 * 
 * @author cutmasta
 * 
 */
public interface OXResourceGroupInterface extends Remote {

    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME = "OXResourceGroup_V2";

    /**
     * Creates a new resource group within the given context.
     * @return New resource group id.
     * @param ctx Context object.
     * @param resgroup ResourceGroup containing the data.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.StorageException When an error in the subsystems occured.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public int create(Context ctx, ResourceGroup resgroup, Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException;

    /**
     * List resource groups matching <code>pattern</code> in given context.
     * @return ResourceGroup[] containing result objects.
     * @param ctx Context object..
     * @param pattern Search pattern like * or hard* (e.g. for hardware)
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public ResourceGroup[] list(Context ctx, String pattern, Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException;

    /**
     * Change resource group within context.
     * @param ctx Context object..
     * @param resgroup ResourceGroup with data.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.StorageException When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public void change(Context ctx, ResourceGroup resgroup, Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException;

    /**
     * Add member to resource group in context.
     * @param ctx Context object.
     * @param resource_group_id Resource group ID to which the new members should be added.
     * @param res Resources which should be added to the resource group.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public void addMember(Context ctx, int resource_group_id, Resource[] res, Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException;

    /**
     * Get all members of resource group in given context
     * @return Resource[] containing result objects.
     * @param ctx Context object.
     * @param resource_group_id ID of the resource group.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException When an error in the subsystems occured.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public Resource[] getMembers(Context ctx, int resource_group_id, Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException;

    /**
     * Remove member(s) from resource group in given context
     * @param ctx Context object.
     * @param resource_group_id Resource group ID.
     * @param members Members to remove from resource group.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.StorageException When an error in the subsystems occured.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public void removeMember(Context ctx, int resource_group_id, Resource[] members, Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException;

    /**
     * Delete resource group from given context.
     * @param ctx Context object.
     * @param resource_group_id Resource group ID.
     * @param auth Credentials for authenticating against server.
     * @throws com.openexchange.admin.rmi.exceptions.InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws com.openexchange.admin.rmi.exceptions.NoSuchContextException If the context does not exist in the system.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.StorageException When an error in the subsystems occured.
     * 
     * @throws com.openexchange.admin.rmi.exceptions.InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public void delete(Context ctx, int[] resource_group_id, Credentials auth) 
    throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException,InvalidDataException;

}
