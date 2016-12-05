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
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.dataobjects.UserProperty;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

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
     * @param ctx
     *            Context in which the new user will exist.
     * @param usrdata
     *            User containing user data.
     * @param auth
     *            Credentials for authenticating against server.
     * @param access
     *            UserModuleAccess containing module access for the user.
     * @param primaryAccountName
     *            The name of the primary mail account
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
     * Creates a new user within the given context.
     *
     * @param ctx
     *            Context in which the new user will exist.
     * @param usrdata
     *            User containing user data.
     * @param auth
     *            Credentials for authenticating against server.
     * @param access_combination_name
     *            Access combination name identifying the module rights for the new user.
     * @param primaryAccountName
     *            The name of the primary mail account
     *
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
    public User create(final Context ctx, final User usrdata, final String access_combination_name, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;


    /**
     * Creates a new user within the given context.<br>
     * Default context access rights are used!
     *
     * @param ctx
     *            Context in which the new user will exist.
     * @param usrdata
     *            User containing user data.
     * @param auth
     *            Credentials for authenticating against server.
     * @param primaryAccountName
     *            The name of the primary mail account
     *
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
    public User create(final Context ctx, final User usrdata, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;


    /**
     * Returns the module access rights of the context-admin
     *
     * @param ctx
     *            The context
     * @param auth
     *            Credentials for authenticating against server.
     *
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
    public UserModuleAccess getContextAdminUserModuleAccess(Context ctx, Credentials auth)  throws RemoteException, StorageException,InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException;

    /**
     * Returns the Context admin {@link User} object
     *
     * @param ctx The context from which to obtain the administrator
     * @param auth The credentials
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public User getContextAdmin(Context ctx, Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, InvalidDataException;

    /**
     * Gets specified user's capabilities.
     *
     * @param ctx The context
     * @param user The user
     * @param auth The credentials
     * @return The capabilities
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public Set<String> getCapabilities(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Changes specified user's capabilities.
     *
     * @param ctx The context
     * @param user The user
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param capsToDrop The capabilities to drop; e.g. clean from storage
     * @param auth The credentials
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void changeCapabilities(Context ctx, User user, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Changes the personal part of specified user's E-Mail address.
     *
     * @param ctx The context
     * @param user The user
     * @param personal The personal to set or <code>null</code> to drop the personal information (if any)
     * @param auth The credentials
     * @throws RemoteException General RMI Exception
     * @throws StorageException When an error in the subsystems occurred.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If the context does not exist in the system.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws DatabaseUpdateException
     * @throws NoSuchUserException
     */
    public void changeMailAddressPersonal(Context ctx, User user, String personal, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Moves a user's files from one storage to another.
     * <p>
     * This operation leaves quota usage unchanged and thus can be considered as the user-sensitive counterpart for <code>OXContextInterface.moveContextFilestore()</code>.
     *
     * @param ctx The context in which the user resides
     * @param user The user
     * @param dstFilestore The destination file storage
     * @param credentials The credentials
     * @return The job identifier which can be used for retrieving progress information.
     * @throws RemoteException General RMI Exception
     * @throws StorageException If an error in the subsystems occurred.
     * @throws InvalidCredentialsException If the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If no such context exists
     * @throws NoSuchFilestoreException If no file storage context exists
     * @throws InvalidDataException If passed data is invalid
     * @throws DatabaseUpdateException If update operation fails
     * @throws NoSuchUserException If no such user exists
     */
    public int moveUserFilestore(Context ctx, User user, Filestore dstFilestore, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Moves a user's files from his own storage to the storage of specified master.
     * <p>
     * This operation is quota-aware and thus transfers current quota usage to master account as well.
     *
     * @param ctx The context in which the user resides
     * @param user The user
     * @param masterUser The master user account
     * @param credentials The credentials
     * @return The job identifier which can be used for retrieving progress information.
     * @throws RemoteException General RMI Exception
     * @throws StorageException If an error in the subsystems occurred.
     * @throws InvalidCredentialsException If the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If no such context exists
     * @throws NoSuchFilestoreException If no file storage context exists
     * @throws InvalidDataException If passed data is invalid
     * @throws DatabaseUpdateException If update operation fails
     * @throws NoSuchUserException If no such user exists
     */
    public int moveFromUserFilestoreToMaster(Context ctx, User user, User masterUser, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Moves a user's files from a master account to his own storage.
     * <p>
     * This operation is quota-aware and thus transfers current quota usage from master account to user.
     *
     * @param ctx The context in which the user resides
     * @param user The user
     * @param masterUser The master user account
     * @param dstFilestore The destination file storage to move to
     * @param maxQuota TODO
     * @param credentials The credentials
     * @return The job identifier which can be used for retrieving progress information.
     * @throws RemoteException General RMI Exception
     * @throws StorageException If an error in the subsystems occurred.
     * @throws InvalidCredentialsException If the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If no such context exists
     * @throws NoSuchFilestoreException If no file storage context exists
     * @throws InvalidDataException If passed data is invalid
     * @throws DatabaseUpdateException If update operation fails
     * @throws NoSuchUserException If no such user exists
     */
    public int moveFromMasterToUserFilestore(Context ctx, User user, User masterUser, Filestore dstFilestore, long maxQuota, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Moves a user's files from a context to his own storage.
     * <p>
     * This operation is quota-aware and thus transfers current quota usage from context to user.
     *
     * @param ctx The context in which the user resides
     * @param user The user
     * @param dstFilestore The destination file storage to move to
     * @param maxQuota TODO
     * @param credentials The credentials
     * @return The job identifier which can be used for retrieving progress information.
     * @throws RemoteException General RMI Exception
     * @throws StorageException If an error in the subsystems occurred.
     * @throws InvalidCredentialsException If the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If no such context exists
     * @throws NoSuchFilestoreException If no file storage context exists
     * @throws InvalidDataException If passed data is invalid
     * @throws DatabaseUpdateException If update operation fails
     * @throws NoSuchUserException If no such user exists
     */
    public int moveFromContextToUserFilestore(Context ctx, User user, Filestore dstFilestore, long maxQuota, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Moves a user's files from his own to a context storage.
     * <p>
     * This operation is quota-aware and thus transfers current quota usage from user to context.
     *
     * @param ctx The context in which the user resides
     * @param user The user
     * @param credentials The credentials
     * @return The job identifier which can be used for retrieving progress information.
     * @throws RemoteException General RMI Exception
     * @throws StorageException If an error in the subsystems occurred.
     * @throws InvalidCredentialsException If the supplied credentials were not correct or invalid.
     * @throws NoSuchContextException If no such context exists
     * @throws NoSuchFilestoreException If no file storage context exists
     * @throws InvalidDataException If passed data is invalid
     * @throws DatabaseUpdateException If update operation fails
     * @throws NoSuchUserException If no such user exists
     */
    public int moveFromUserToContextFilestore(Context ctx, User user, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Manipulate user data within the given context.
     *
     * @param ctx
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
     * @param ctx
     *            Context in which the new user will be deleted.
     * @param users
     *            user array containing user object.
     * @param auth
     *            Credentials for authenticating against server.
     * @param destUser
     *            The user id of the the user shared data is assigned to. If set to null the context admin and the context filestore will be used instead. If set to 0 or below the data will be removed instead.
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
    public void delete(final Context ctx, final User[] users, Integer destUser, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Delete user from given context.
     *
     * @param ctx
     *            Context in which the new user will be deleted.
     * @param user
     *            user object.
     * @param destUser
     *            The user id of the the user shared data is assigned to. If set to null the context admin and the context filestore will be used instead. If set to 0 or below the data will be removed instead.
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
    public void delete(final Context ctx, final User user, final Integer destUser, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Use {@link #delete(Context, User[], Integer, Credentials)} instead.
     *
     * @param ctx
     *            Context in which the new user will be deleted.
     * @param users
     *            user array containing user object.
     * @param auth
     *            Credentials for authenticating against server.
     * @param destUser
     *            The user id of the the user shared data is assigned to.
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
    @Deprecated
    public void delete(final Context ctx, final User[] users, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Use {@link #delete(Context, User, Integer, Credentials)} instead.
     *
     * @param ctx
     *            Context in which the new user will be deleted.
     * @param user
     *            user object.
     * @param destUser
     *            The user id of the the user shared data is assigned to.
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
    @Deprecated
    public void delete(final Context ctx, final User user, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * Retrieve the ModuleAccess for an user.
     *
     * @param accessCombinationName
     *            The access combination name
     * @param auth
     *            Credentials for authenticating against server.
     * @return UserModuleAccess containing the module access rights.
     *
     * @throws RemoteException General RMI Exception
     */
    public UserModuleAccess moduleAccessForName(final String accessCombinationName) throws RemoteException;

    /**
     * Retrieve the ModuleAccess for an user.
     *
     * @param ctx
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
     * Get current access combination name of an user!
     *
     * @param ctx
     * @param user
     * @param auth
     * @return Access combination name or null if current access rights cannot be mapped to an access combination name.
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws StorageException
     * @throws InvalidDataException
     */
    public String getAccessCombinationName(final Context ctx,final User user,final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException,
	InvalidDataException, DatabaseUpdateException, NoSuchUserException;


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
     * Manipulate user module access within the given context.
     *
     * @param ctx
     *            Context object.
     * @param user_id
     *            int containing the user id.
     * @param access
     *            String containing access combination name.
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
    public void changeModuleAccess(final Context ctx, final User user, final String access_combination_name, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException;

    /**
     * This method changes module Permissions for all (!) users in all (!) contexts. This can be filtered by already existing access combinations.
     * If no filter is given, all users are changed.
     *
     * @param filter The call affects only users with exactly this access combination. This is either a String representing a defined module access combination or an Integer (masked as String) for direct definitions. null for no filter.
     * @param addAccess Access rights to be added
     * @param removeAccess Access rights to be removed
     * @param auth Credentials for authenticating against server. Must be the master Admin.
     * @throws InvalidCredentialsException When the supplied credentials were not correct or invalid.
     * @throws StorageException When an error in the subsystems occured.
     * @throws InvalidDataException If the data sent within the method contained invalid data.
     * @throws RemoteException General RMI Exception
     */
    public void changeModuleAccessGlobal(String filter, UserModuleAccess addAccess, UserModuleAccess removeAccess, Credentials auth) throws RemoteException, InvalidCredentialsException, StorageException, InvalidDataException;

    /**
     * Retrieve user objects for a range of users by username or id.
     *
     * @see User.getUsername().
     *
     * @param ctx
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
     * @param ctx
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
     * Retrieve all user objects with given filestore for a given context.
     * If filestore_id is null all user objects with a own filestore for a given context are retrieved instead.
     *
     * @param context
     *            Context object.
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] with currently ONLY id set in each User.
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
     * @throws DatabaseUpdateException
     */
    public User[] listUsersWithOwnFilestore(final Context context, final Credentials auth, final Integer filestore_id) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all users for a given context.
     * The search pattern is directly transformed into a SQL LIKE string comparison, where<br>
     * a * is transformed into a %<br>
     * a % and a _ must be escaped by a \ (e.g. if you want to search for _doe, use the pattern \_doe
     *
     * @param ctx
     *            Context object.
     * @param search_pattern
     *            A pattern to search for
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] with currently ONLY id set in each User.
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
     * Retrieve all users for a given context.
     * The search pattern is directly transformed into a SQL LIKE string comparison, where<br>
     * a * is transformed into a %<br>
     * a % and a _ must be escaped by a \ (e.g. if you want to search for _doe, use the pattern \_doe
     *
     * @param ctx
     *            Context object.
     * @param search_pattern
     *            A pattern to search for
     * @param auth
     *            Credentials for authenticating against server.
     * @param includeGuests
     *            List guest users too
     * @param excludeUsers
     *            List only guest users
     * @return User[] with currently ONLY id set in each User.
     *
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] list(final Context ctx, final String search_pattern, final Credentials auth, final boolean includeGuests, final boolean excludeUsers) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all users for a given context.
     * The search pattern is directly transformed into a SQL LIKE string comparison, where<br>
     * a * is transformed into a %<br>
     * a % and a _ must be escaped by a \ (e.g. if you want to search for _doe, use the pattern \_doe
     *
     * @param ctx
     *            Context object.
     * @param search_pattern
     *            A pattern to search for
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] with currently ONLY id set in each User.
     *
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] listCaseInsensitive(final Context ctx, final String search_pattern, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all users for a given context.
     * The search pattern is directly transformed into a SQL LIKE string comparison, where<br>
     * a * is transformed into a %<br>
     * a % and a _ must be escaped by a \ (e.g. if you want to search for _doe, use the pattern \_doe
     *
     * @param ctx
     *            Context object.
     * @param search_pattern
     *            A pattern to search for
     * @param auth
     *            Credentials for authenticating against server.
     * @param includeGuests
     *            List guest users too
     * @param excludeUsers
     *            List only guest users
     * @return User[] with currently ONLY id set in each User.
     *
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] listCaseInsensitive(final Context ctx, final String search_pattern, final Credentials auth, final boolean includeGuests, final boolean excludeUsers) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all users for a given context. The same as calling list with a search_pattern of "*"
     *
     * @param ctx
     *            Context object.
     * @param auth
     *            Credentials for authenticating against server.
     * @return User[] with currently ONLY id set in each User.
     *
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Retrieve all users for a given context. The same as calling list with a search_pattern of "*"
     *
     * @param ctx
     *            Context object.
     * @param auth
     *            Credentials for authenticating against server.
     * @param includeGuests
     *            List guest users too
     * @param excludeUsers
     *            List only guest users
     * @return User[] with currently ONLY id set in each User.
     *
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     * @throws DatabaseUpdateException
     */
    public User[] listAll(final Context ctx, final Credentials auth, boolean includeGuests, boolean excludeUsers) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException;

    /**
     * Check whether the given user exists. Either users id or name must be set.
     *
     * @param ctx
     * @param user
     * @return
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws DatabaseUpdateException
     * @throws NoSuchContextException
     */
    public boolean exists(final Context ctx, final User user, final Credentials auth) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, DatabaseUpdateException, NoSuchContextException;

    /**
     * Retrieve user configuration (within {@link UserProperty}) and its source.
     * @param ctx Context object the user is associated to
     * @param user User object to retrieve the configuration for
     * @param searchPattern string with a pattern to search for a property
     * @param credentials Credentials for authenticating against server
     * @return The found properties in a {@link List}
     * @throws InvalidDataException
     * @throws StorageException
     * @throws NoSuchUserException
     * @throws InvalidCredentialsException
     */
    public List<UserProperty> getUserConfigurationSource(final Context ctx, final User user, final String searchPattern, final Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, NoSuchUserException;

    /**
     * Gets the capabilities tree showing which capability comes from which source
     *
     * @param ctx Context object the user is associated to
     * @param user User object to retrieve the configuration for
     * @param credentials Credentials for authenticating against server
     * @return The capabilities tree
     * @throws InvalidDataException
     * @throws StorageException
     * @throws NoSuchUserException
     * @throws InvalidCredentialsException
     */
    public Map<String, Map<String, Set<String>>> getUserCapabilitiesSource(Context ctx, User user, Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, NoSuchUserException;

    /**
     * Retrieves all users with an alias within the given domain
     *
     * @param context Context object the user is associated to
     * @param aliasDomain The domain of the alias
     * @param auth Credentials for authenticating against server
     * @return The users
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws NoSuchContextException
     * @throws InvalidDataException
     */
    public User[] listByAliasDomain(Context context, String aliasDomain, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException;

}
