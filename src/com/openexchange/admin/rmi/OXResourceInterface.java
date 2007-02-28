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
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface defines the Open-Xchange API Version 2 for creating and
 * manipulating OX resources within an OX context.
 * 
 * @author cutmasta
 * 
 */
public interface OXResourceInterface extends Remote {

    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME = "OXResource_V2";

    /**
     * Creates a new resource within the given context.
     * @param ctx Create Resource in this Context
     * @param res Resource which should be created
     * @param auth Credentials for authenticating against server.
     * @return Contains the new resource id.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public int create(Context ctx, Resource res, Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException;

    /**
     * Change resource within the given context.
     * @param ctx Change Resource in this Context.
     * @param res Resource containing the data.
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public void change(Context ctx, Resource res, Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException;

    /**
     * Get Resource from given context.
     * @return Resource object containing data.
     * @param ctx Context object.
     * @param resource_id long containing the resource id.
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * 
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public Resource get(Context ctx, int resource_id, Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException;

    /**
     * Delete resource from given context.
     * @param ctx Context object.
     * @param resource_id ID of Resource which should be deleted.
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * 
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public void delete(Context ctx, int resource_id, Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException;

    /**
     * List resources matching <code>pattern</code> in given context.
     * @return Resource[] containing result objects.
     * @param ctx Context object.
     * @param pattern Search pattern like * or mon* (e.g. for monitor)
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public Resource[] list(Context ctx, String pattern, Credentials auth) 
    throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException;

}
