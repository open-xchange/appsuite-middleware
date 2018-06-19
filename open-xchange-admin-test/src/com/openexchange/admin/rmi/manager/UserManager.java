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
 *    trademarks of the OX Software GmbH user of companies.
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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.rmi.manager;

import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;

/**
 * {@link UserManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserManager extends AbstractManager {

    private static UserManager INSTANCE;

    /**
     * Gets the instance of the {@link UserManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static UserManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new UserManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link UserManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private UserManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates the specified {@link User} in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param user The {@link User}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * 
     * @return The newly created {@link User}
     * @throws Exception if an error is occurred
     */
    public User createUser(Context context, User user, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.create(context, user, contextAdminCredentials);
    }

    /**
     * Creates the specified {@link User} in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param user The {@link User}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * 
     * @return The newly created {@link User}
     * @throws Exception if an error is occurred
     */
    public User createUser(Context context, User user, UserModuleAccess userModuleAccess, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.create(context, user, userModuleAccess, contextAdminCredentials);
    }

    /**
     * Creates the specified {@link User} in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param user The {@link User}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * 
     * @return The newly created {@link User}
     * @throws Exception if an error is occurred
     */
    public User createUser(Context context, User user, String combination, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.create(context, user, combination, contextAdminCredentials);
    }

    /**
     * Retrieves all data of the specified {@link User} in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param User The {@link User}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * 
     * @return The {@link User} with all its data loaded
     * @throws Exception if an error is occurred
     */
    public User getData(Context context, User user, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.getData(context, user, contextAdminCredentials);
    }

    /**
     * Retrieves an array with all found {@link User} in the specified {@link Context}
     * that match the specified search pattern.
     * 
     * @param context The {@link Context}
     * @param searchPattern The search pattern
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return An array with all found {@link User}s
     * @throws Exception if an error is occurred
     */
    public User[] listUsers(Context context, String searchPattern, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.list(context, searchPattern, contextAdminCredentials);
    }

    /**
     * Returns an array with all {@link User}s in the specified {@link Context}
     * 
     * @param context The context
     * @param contextAdminCredentials The context admin credentials
     * @return An array with all {@link User}s
     * @throws Exception if an error is occurred
     */
    public User[] listAllUsers(Context context, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.listAll(context, contextAdminCredentials);
    }

    /**
     * Changes the specified {@link User} in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param User The {@link User} to change
     * @param contextAdminCredentials The context admin {@link Credentials}
     * 
     * @throws Exception if an error is occurred
     */
    public void changeUser(Context context, User user, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        userInterface.change(context, user, contextAdminCredentials);
    }

    /**
     * Checks whether the specified {@link User} exists in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param user The {@link User}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return <code>true</code> if the user exists; <code>false</code> otherwise
     * 
     * @throws Exception if an error is occurred
     */
    public boolean existsUser(Context context, User user, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.exists(context, user, contextAdminCredentials);
    }

    /**
     * Moves a user's files from a context to his own storage.
     * 
     * This operation is quota-aware and thus transfers current quota usage from context to user.
     * 
     * @param context The context
     * @param user The user
     * @param filestore The {@link Filestore}
     * @param maxQuota The max quota
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void moveFromContextToUserFilestore(Context context, User user, Filestore filestore, long maxQuota, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        userInterface.moveFromContextToUserFilestore(context, user, filestore, maxQuota, contextAdminCredentials);
    }

    /**
     * Retrieve all user objects with given filestore for a given context.
     * If <code>filestoreId</code> is <code>null</code> all user objects with
     * a dedicated filestore for a given context are retrieved instead.
     * 
     * @param context The context
     * @param filestoreId The {@link Filestore} identifier
     * @param contextAdminCredentials The context admin credentials
     * @return An array with {@link User}s
     * @throws Exception if an error is occurred
     */
    public User[] listUsersWithOwnFilestore(Context context, Integer filestoreId, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        return userInterface.listUsersWithOwnFilestore(context, contextAdminCredentials, filestoreId);
    }

    /**
     * Retrieves the {@link UserModuleAccess} for the specified {@link User}
     * 
     * @param context The {@link Context}
     * @param user The {@link User}
     * @param contextAdminCredentials The context admin credentials
     * @return The {@link UserModuleAccess}
     * @throws Exception if an error is occurred
     */
    public UserModuleAccess getModuleAccess(Context context, User user, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInteface = getUserInterface();
        return userInteface.getModuleAccess(context, user, contextAdminCredentials);
    }

    /**
     * Changes the {@link UserModuleAccess} for the specified {@link User} in the specified
     * {@link Context}
     * 
     * @param context The {@link Context}
     * @param user The {@link User}
     * @param access The {@link UserModuleAccess}
     * @param contextAdminCredentials The context admin credentials
     * @throws Exception if an error is occurred
     */
    public void changeModuleAccess(Context context, User user, UserModuleAccess access, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInteface = getUserInterface();
        userInteface.changeModuleAccess(context, user, access, contextAdminCredentials);
    }

    /**
     * Deletes the specified {@link User} from the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param user The {@link User} to delete
     * @param contextAdminCredentials The context's admin {@link Credentials}
     * 
     * @throws Exception if an error is occurred
     */
    public void deleteUser(Context context, User user, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        userInterface.delete(context, user, null, contextAdminCredentials);
    }

    /**
     * Deletes the specified {@link User} from the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param user The {@link User} to delete
     * @param contextAdminCredentials The context's admin {@link Credentials}
     * 
     * @throws Exception if an error is occurred
     */
    public void deleteUser(Context context, User[] user, Credentials contextAdminCredentials) throws Exception {
        OXUserInterface userInterface = getUserInterface();
        userInterface.delete(context, user, null, contextAdminCredentials);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    boolean clean(Object object) {
        // Nothing to do, the user will be implicitly deleted when the context is deleted.
        return true;
    }

    /**
     * Retrieves the remote {@link OXUserInterface}
     * 
     * @return the remote {@link OXUserInterface}
     * @throws Exception if the remote interface cannot be retrieved
     */
    private OXUserInterface getUserInterface() throws Exception {
        return getRemoteInterface(OXUserInterface.RMI_NAME, OXUserInterface.class);
    }
}
