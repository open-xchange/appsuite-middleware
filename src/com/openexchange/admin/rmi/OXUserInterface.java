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
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This class defines the Open-Xchange API Version 2 for creating and
 * manipulating OX Users within an OX context.<br><br>
 * 
 * <b>Example:</b>
 * <pre>
 * final OXUserInterface iface = (OXUserInterface)Naming.lookup("rmi:///oxhost/"+OXUserInterface.RMI_NAME);
 * 
 * final Context ctx = new Context(1);
 * 
 * User usr = new User();
 * usr.setDisplay_name("display name");
 * usr.setName("name");
 * usr.setPassword("secret");  
 * usr.setMailenabled(true);       
 * usr.setPrimaryEmail("primaryemail@example.org");
 * usr.setEmail1("primaryemail@example.org");
 * usr.setGiven_name("Givenname");
 * usr.setSur_name("Lastname");
 * 
 * final UserModuleAccess access = new UserModuleAccess();  
 * access.access.enableAll();  // give access to all modules.
 * 
 * final Credentials auth = new Credentials();
 * auth.setLogin("admin");
 * auth.setPassword("secret");
 * 
 * User created = iface.create(ctx,usr,access,auth);
 *  
 * </pre>
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * 
 */
public interface OXUserInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXUser_V2";

    /**
     * Creates a new user within the given context.
     * 
     * @param context
     *            Context in which the new user will exist.
     * @param usrdata
     *            User containing user data.
     * @param auth
     *            Credentials for authenticating against server.
     * @param access
     *            UserModuleAccess containing module access for the user.
     * @return int containing the id of the new user.
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
     */
    public User create(final Context ctx, final User usrdata, final UserModuleAccess access, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Manipulate user data within the given context.
     * 
     * @param context
     *            Context in which the new user will be modified.
     * @param usrdata
     *            User containing user data.
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
    public void change(final Context ctx, final User usrdata, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Delete user from given context.
     * 
     * @param context
     *            Context in which the new user will be deleted.
     * @param users
     *            user array containing user object.
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
    public void delete(final Context ctx, final User[] users, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Delete user from given context.
     * 
     * @param context
     *            Context in which the new user will be deleted.
     * @param user
     *            user object.
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
    public void delete(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Retrieve the ModuleAccess for an user.
     * 
     * @param context
     *            Context
     * @param user_id
     *            int containing the user id.
     * @param auth
     *            Credentials for authenticating against server.
     * @return UserModuleAccess containing the module access rights.
     * 
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException 
     * @throws NoSuchUserException
     * @deprecated Will be removed with next service pack 
     */
    public UserModuleAccess getModuleAccess(final Context ctx, final int user_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Retrieve the ModuleAccess for an user.
     * 
     * @param context
     *            Context
     * @param user_id
     *            int containing the user id.
     * @param auth
     *            Credentials for authenticating against server.
     * @return UserModuleAccess containing the module access rights.
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
    public UserModuleAccess getModuleAccess(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Manipulate user module access within the given context.
     * 
     * @param ctx
     *            Context object.
     * @param user_id
     *            int containing the user id.
     * @param moduleAccess
     *            UserModuleAccess containing module access.
     * @param auth
     *            Credentials for authenticating against server.
     *            
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException 
     * @throws NoSuchUserException
     * @deprecated Will be removed with next service pack
     */
    public void changeModuleAccess(final Context ctx, final int user_id, final UserModuleAccess moduleAccess, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Manipulate user module access within the given context.
     * 
     * @param ctx
     *            Context object.
     * @param user_id
     *            int containing the user id.
     * @param moduleAccess
     *            UserModuleAccess containing module access.
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
    public void changeModuleAccess(final Context ctx, final User user, final UserModuleAccess moduleAccess, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Retrieve user objects for a range of users by id
     * 
     * @param ctx
     *            numerical context identifier
     * @param user_id
     *            int of the user id
     * @param auth
     *            Credentials for authenticating against server.
     * @return User containing result object.
     * 
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws NoSuchUserException 
     * @throws DatabaseUpdateException 
     * @deprecated Will be removed with next service pack. Use {@link #getData(Context,User,Credential)} instead
     */
    @Deprecated
    public User getData(final Context ctx, final int user_id, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException;

    /**
     * Retrieve user objects for a range of users by id
     * 
     * @deprecated  Use {@link #getData(Context,User[],Credential)} instead
     * @param ctx
     *            numerical context identifier
     * @param user_id
     *            int[] array containing user id(s)
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] containing result objects.
     * 
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws NoSuchUserException
     * @throws DatabaseUpdateException 
     * @deprecated Will be removed with next service pack
     */
    @Deprecated
    public User[] getData(Context ctx, int[] user_ids, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException;

    /**
     * Retrieve user objects for a range of users by username or id.
     * 
     * @see User.getUsername().
     * 
     * @param context
     *            Context object.
     * @param users
     *            User[] with users to get data for.
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] containing result objects.
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
    public User[] getData(final Context ctx, final User[] users, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException;

    /**
     * Retrieve user objects for a range of users by username or id.
     * 
     * @see User.getUsername().
     * 
     * @param context
     *            Context object.
     * @param user
     *            user object with user to get data for.
     * @param auth
     *            Credentials for authenticating against server.
     * @return User containing result object.
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
    public User getData(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException;

    /**
     * Checks if given User is Administrator for the given Context.
     * 
     * @param ctx
     *          Context object.
     * @param user
     *          User object.
     * @param auth
     *          Credentials for authenticating against server.
     * @return
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws NoSuchUserException
     * @throws DatabaseUpdateException
     * @deprecated Will be removed with next service pack
     */
    public boolean isContextAdmin(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException;

    /**
     * Retrieve all users for a given context.
     * 
     * @param ctx
     *            Context object.
     * @param search_pattern
     *            A pattern to search for
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] with currently ONYL id set in each User.
     * 
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] list(final Context ctx, final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all user ids for a given context.
     * 
     * @param ctx
     *            numerical context identifier
     * @param auth
     *            Credentials for authenticating against server.
     * @return int[] containing user ids.
     * 
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     * @deprecated Will be removed with next service pack 
     */
    public int[] getAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all users for a given context. The same as calling list with a search_pattern of "*"
     * 
     * @param ctx
     *            Context object.
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] with currently ONYL id set in each User.
     * 
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    
}
