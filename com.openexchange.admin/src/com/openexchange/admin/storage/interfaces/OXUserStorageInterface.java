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
package com.openexchange.admin.storage.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Set;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

/**
 * This interface provides an abstraction to the storage of the user information
 *
 * @author d7
 * @author cutmasta
 *
 */
public abstract class OXUserStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXUserStorageInterface.class);

    private static volatile OXUserStorageInterface instance;

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXUserStorageInterface getInstance() throws StorageException {
        OXUserStorageInterface i = instance;
        if (null == i) {
            synchronized (OXUserStorageInterface.class) {
                i = instance;
                if (null == i) {
                    Class<? extends OXUserStorageInterface> implementingClass;
                    AdminCache cache = ClientAdminThread.cache;
                    PropertyHandler prop = cache.getProperties();
                    final String className = prop.getProp(PropertyHandler.USER_STORAGE, null);
                    if (null != className) {
                        try {
                            implementingClass = Class.forName(className).asSubclass(OXUserStorageInterface.class);
                        } catch (final ClassNotFoundException e) {
                            log.error("", e);
                            throw new StorageException(e);
                        }
                    } else {
                        final StorageException storageException = new StorageException("Property for user_storage not defined");
                        log.error("", storageException);
                        throw storageException;
                    }

                    Constructor<? extends OXUserStorageInterface> cons;
                    try {
                        cons = implementingClass.getConstructor(new Class[] {});
                        i = cons.newInstance(new Object[] {});
                        instance = i;
                    } catch (final SecurityException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final NoSuchMethodException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalArgumentException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InstantiationException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalAccessException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InvocationTargetException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    }
                }
            }
        }
        return i;
    }

    /**
     * Checks if specified context exists.
     *
     * @param ctx The context at least providing context identifier
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws StorageException If an error occurred
     */
    public abstract boolean doesContextExist (final Context ctx)  throws StorageException;

    /**
     * Retrieve the ModuleAccess for an user.
     *
     * @param context Context
     * @param user_id long containing the user id.
     * @return UserModuleAccess containing the module access rights.
     * @throws StorageException
     *
     */
    public abstract UserModuleAccess getModuleAccess (final Context ctx,final int user_id)  throws StorageException;

    /**
     * Manipulate user module access within the given context.
     * @param ctx Context object.
     * @param userId int[] containing the user id.
     * @param moduleAccess UserModuleAccess containing module access.
     *
     * @throws StorageException
     */
    public abstract void changeModuleAccess(Context ctx, int userId, UserModuleAccess moduleAccess) throws StorageException;

    /**
     * Manipulate users module access within the given context.
     *
     * @param ctx Context object.
     * @param user_ids int[] containing the user ids.
     * @param moduleAccess UserModuleAccess containing module access.
     *
     * @throws StorageException
     */
    public abstract void changeModuleAccess(final Context ctx,final int[] user_ids,final UserModuleAccess moduleAccess) throws StorageException;


    /**
     * Retrieve user objects for a range of users identified by User.getUsername().
     *
     * @param context Context object.
     * @param users User[] with users to get data for. Attention: These objects will be cloned by a shallow copy, so
     * non native attributes will point to the same reference after this method
     * @return User[] containing result objects.
     * @throws RemoteException
     *
     */
    public abstract User[] getData(final Context ctx, User[] users) throws StorageException;

    /**
     * Changes specified context's capabilities.
     *
     * @param ctx The context
     * @param user The user
     * @param capsToAdd The capabilities to add
     * @param capsToRemove The capabilities to remove
     * @param capsToDrop The capabilities to drop; e.g. clean from storage
     * @param auth The credentials
     * @throws StorageException When an error in the subsystems occurred.
     */
    public abstract void changeCapabilities(Context ctx, User user, Set<String> capsToAdd, Set<String> capsToRemove, Set<String> capsToDrop, Credentials auth) throws StorageException;

    /**
     * Changes the personal part of specified user's E-Mail address.
     *
     * @param ctx The context
     * @param user The user
     * @param personal The personal to set or <code>null</code> to drop the personal information (if any)
     * @throws StorageException When an error in the subsystems occurred.
     */
    public abstract void changeMailAddressPersonal(Context ctx, User user, String personal) throws StorageException;

    /**
     * Gets the current capabilities for denoted user.
     *
     * @param ctx The context
     * @param user The user
     * @return The current capabilities
     * @throws StorageException If retrieving capabilities fails
     */
    public abstract Set<String> getCapabilities(Context ctx, User user) throws StorageException;

    /**
     * Manipulate user data within the given context.
     *
     * @param context Context in which the new user will be modified.
     * @param usrdata User containing user data.
     * @throws StorageException
     */
    public abstract void change(Context ctx, User usrdata) throws StorageException;

    /**
     * Enables denoted user.
     *
     * @param userId The user identifier
     * @param context The context
     * @throws StorageException If operation fails
     */
    public abstract void enableUser(int userId, Context ctx) throws StorageException;

    /**
     * Disables denoted user.
     *
     * @param userId The user identifier
     * @param context The context
     * @throws StorageException If operation fails
     */
    public abstract void disableUser(int userId, Context ctx) throws StorageException;

    /**
     * Switches user's <code>mailenabled</code> flag.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param value The value
     * @param con The connection to use
     * @throws StorageException If operation fails
     */
    public abstract void setUserEnabled(int userId, int contextId, boolean value, Connection con) throws StorageException;

    /**
     * Changes last modified data in database
     */
    public abstract void changeLastModified( final int user_id, final Context ctx, final Connection write_ox_con ) throws StorageException;

    /**
     * Create new user in given connection with given contact and user id
     * If the uid number feature is active then also supply a correct uid_number(IDGenerator with Type UID_NUMBER).Else set this to -1
     */
    public abstract int create(final Context ctx, final User usrdata, final UserModuleAccess moduleAccess, final Connection write_ox_con, final int internal_user_id, final int contact_id,final int uid_number, final String primaryAccountName) throws StorageException;

    /**
     * Create new user in context ctx
     */
    public abstract int create(final Context ctx,final User usrdata, final UserModuleAccess moduleAccess, final String primaryAccountName) throws StorageException;


    /**
     * Retrieve all user ids for a given context.
     *
     * @param ctx numerical context identifier
     * @return int[] containing user ids.
     * @throws StorageException
     *
     */
    public abstract int[] getAll(final Context ctx) throws StorageException;

    /**
     * Retrieve all user objects for a given context. Which match the given search_pattern
     *
     * @param ctx numerical context identifier
     * @return User[] containing user ids.
     * @throws StorageException
     *
     */
    public abstract User[] list(final Context ctx, final String search_pattern) throws StorageException;

    /**
     * Retrieve all user objects for a given context. Which match the given search_pattern
     *
     * @param ctx numerical context identifier
     * @param includeGuests List guest users too
     * @param excludeUsers List only guest users
     * @return User[] containing user ids.
     * @throws StorageException
     *
     */
    public abstract User[] list(final Context ctx, final String search_pattern, final boolean includeGuests, final boolean excludeUsers) throws StorageException;

    /**
     * Retrieve all user objects for a given context, that match (case insensitive) the given search_pattern
     *
     * @param ctx The context
     * @return User[] containing user identifier
     * @throws StorageException
     */
    public abstract User[] listCaseInsensitive(final Context ctx, final String search_pattern) throws StorageException;

    /**
     * Retrieve all user objects for a given context, that match (case insensitive) the given search_pattern
     *
     * @param ctx The context
     * @param includeGuests List guest users too
     * @param excludeUsers List only guest users
     * @return User[] containing user identifier
     * @throws StorageException
     */
    public abstract User[] listCaseInsensitive(final Context ctx, final String search_pattern, final boolean includeGuests, final boolean excludeUsers) throws StorageException;

    /**
     * Retrieve all user objects with given filestore for a given context.
     * If filestore_id is null all user objects with a own filestore for a given context are retrieved instead.
     *
     * @param context The context
     * @param filestore_id The id of the filestore or null
     * @return User[] containing user identifier
     * @throws StorageException
     */
    public abstract User[] listUsersWithOwnFilestore(final Context context, final Integer filestore_id) throws StorageException;

    /**
     * Retrieve all users with given alias domain
     *
     * @param context The context
     * @param aliasDomain The alias domain
     * @return The users
     * @throws StorageException
     */
    public abstract User[] listUsersByAliasDomain(final Context context, String aliasDomain) throws StorageException;

    /**
     * Delete an user or multiple from given context in given connection
     */
    public abstract void delete(final Context ctx, final User[] user_ids, Integer destUID, final Connection write_ox_con) throws StorageException;

    /**
     * Delete users in given context
     */
    public abstract void delete(final Context ctx, final User[] users, Integer destUser) throws StorageException;

    /**
     * Delete one user in given context
     */
    public abstract void delete(final Context ctx, final User user, Integer destUser) throws StorageException;

    /**
     * Fetch all data from current user  and add it to "del_user"
     */
    public abstract void createRecoveryData ( final Context ctx,final int user_id, final Connection write_ox_con ) throws StorageException;

    /**
     *  Delete from "del_user" for given context and user
     */
    public abstract void deleteRecoveryData ( final Context ctx,final int user_id, final Connection con ) throws StorageException;


    /**
     * Delete from "del_user" for given context
     */
    public abstract void deleteAllRecoveryData (final Context ctx, final Connection con ) throws StorageException;


}
