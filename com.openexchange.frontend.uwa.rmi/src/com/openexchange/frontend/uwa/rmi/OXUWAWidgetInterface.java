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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.frontend.uwa.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import com.openexchange.admin.lib.rmi.dataobjects.Context;
import com.openexchange.admin.lib.rmi.dataobjects.Credentials;
import com.openexchange.admin.lib.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.lib.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.lib.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.lib.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.lib.rmi.exceptions.StorageException;


/**
 * {@link OXUWAWidgetInterface}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface OXUWAWidgetInterface extends Remote {
    public static final String RMI_NAME = "OXUWAWidget";
    
    /**
     * Creates a new context level widget.
     * 
     * @param context
     *            Context in which the widget will be shown.
     * @param data
     *            Widget data
     * @param auth
     *            Credentials for authenticating against server.
     * @throws RemoteException
     *             General RMI Exception
     * @throws StorageException
     *             When an error in the subsystems occurred.
     * @throws InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException
     *             If the context does not exist in the system.
     * @throws InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     */
    public Widget create(final Context ctx, final Widget data, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;
    
    
    /**
     * Manipulate widget data within the given context.
     * 
     * @param context
     *            Context in which the widget will be modified.
     * @param data
     *            Widget data.
     * @param auth
     *            Credentials for authenticating against server.
     * 
     * @throws RemoteException
     *             General RMI Exception
     * @throws StorageException
     *             When an error in the subsystems occurred.
     * @throws InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException
     *             If the context does not exist in the system.
     * @throws InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void change(final Context ctx, final Widget data, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Delete a widget from the given context.
     * 
     * @param context
     *            Context in which the widget will be deleted.
     * @param widgets
     *            The widgets to delete. Only the id needs to be set.
     * @param auth
     *            Credentials for authenticating against server.
     * 
     * @throws RemoteException
     *             General RMI Exception
     * @throws StorageException
     *             When an error in the subsystems occurred.
     * @throws InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException
     *             If the context does not exist in the system.
     * @throws InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void delete(final Context ctx, final Widget[] widgets, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Delete a widget from the given context.
     * 
     * @param context
     *            Context in which the widget will be deleted.
     * @param widget
     *            The widget to delete. Only the id needs to be set
     * @param auth
     *            Credentials for authenticating against server.
     * 
     * @throws RemoteException
     *             General RMI Exception
     * @throws StorageException
     *             When an error in the subsystems occurred.
     * @throws InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException
     *             If the context does not exist in the system.
     * @throws InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void delete(final Context ctx, final Widget widget, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;


    /**
     * Retrieve widgets by id.
     * 
     * @param context
     *            Context object.
     * @param widgets
     *            Widget[] with ids to load.
     * @param auth
     *            Credentials for authenticating against server.
     * @return Widget[] containing result objects.
     * 
     * @throws RemoteException
     *             General RMI Exception
     * @throws StorageException
     *             When an error in the subsystems occured.
     * @throws InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException
     *             If the context does not exist in the system.
     * @throws InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws NoSuchUserException
     * @throws DatabaseUpdateException
     */
    public Widget[] getData(final Context ctx, final Widget[] widgets, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve one Widget by id 
     * @param context
     *            Context object.
     * @param widget
     *            The widget to retrieve.
     * @param auth
     *            Credentials for authenticating against server.
     * @return The widget that was loaded.
     * 
     * @throws RemoteException
     *             General RMI Exception
     * @throws StorageException
     *             When an error in the subsystems occured.
     * @throws InvalidCredentialsException
     *             When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException
     *             If the context does not exist in the system.
     * @throws InvalidDataException
     *             If the data sent within the method contained invalid data.
     * @throws NoSuchUserException
     * @throws DatabaseUpdateException
     */
    public Widget getData(final Context ctx, final Widget widget, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all widgets in a given context.
     * 
     * @param ctx
     *            Context object.
     * @param auth
     *            Credentials for authenticating against server.
     * @return Widget[] Array of all widgets in the given context.
     * 
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public Widget[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    
}
