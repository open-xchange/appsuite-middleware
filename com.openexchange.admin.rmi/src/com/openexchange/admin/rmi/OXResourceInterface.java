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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This interface defines the Open-Xchange API Version 2 for creating and
 * manipulating OX resources within an OX context.<br><br>
 *
 * <b>Example:</b>
 * <pre>
 * final OXResourceInterface iface = (OXResourceInterface)Naming.lookup("rmi:///oxhost/"+OXResourceInterface.RMI_NAME);
 *
 * final Context ctx = new Context(1);
 *
 * final Credentials auth = new Credentials();
 * auth.setLogin("myuser");
 * auth.setPassword("secret");
 *
 * final Resource res = new Resource();
 * res.setName("resource");
 * res.setDisplayName("my resource display name");
 *
 * Resource res_created = iface.create(ctx,res,auth);
 *
 * </pre>
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXResourceInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXResource_V2";

    /**
     * Creates a new resource within the given context.
     *
     * @param ctx Create Resource in this Context
     * @param res Resource which should be created
     * @param auth Credentials for authenticating against server.
     * @return Resource containing the resource id.
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws DatabaseUpdateException
     */
    public Resource create(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException;

    /**
     * Change resource within the given context.
     *
     * @param ctx Change Resource in this Context.
     * @param res Resource containing the data.
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws DatabaseUpdateException
     * @throws NoSuchResourceException
     */
    public void change(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException;

    /**
     * Delete resource from given context.
     *
     * @param ctx Context object.
     * @param res Resource which should be deleted.
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     *
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws DatabaseUpdateException
     * @throws NoSuchResourceException
     */
    public void delete(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException;

    /**
     * Get Resource from given context.
     *
     * @return Resource object containing data.
     * @param ctx Context object.
     * @param res Resource containing the resource id.
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     *
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws DatabaseUpdateException
     * @throws NoSuchResourceException
     */
    public Resource getData(final Context ctx, final Resource res, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchResourceException;

    /**
     * Get specified resources from server. Can be used to get resource data including extensions.
     * Resources will be identified by id or by name.
     * @param ctx
     * @param resources
     * @param auth
     * @return Resources including extension data if requested!
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws NoSuchResourceException
     * @throws DatabaseUpdateException
     */
    public Resource[] getData(final Context ctx, final Resource[] resources, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchResourceException, DatabaseUpdateException;

    /**
     * List resources matching <code>pattern</code> in given context.
     *
     * @return Resource[] containing result objects.
     * @param ctx Context object.
     * @param pattern Search pattern like * or mon* (e.g. for monitor)
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws DatabaseUpdateException
     */
    public Resource[] list(final Context ctx, final String pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException;

    /**
     * List all resources in given context.
     *
     * @return Resource[] containing result objects.
     * @param ctx Context object.
     * @param auth Credentials for authenticating against server.
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     * @throws DatabaseUpdateException
     */
    public Resource[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException;;
}
