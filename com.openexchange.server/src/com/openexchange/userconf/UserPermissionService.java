/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.userconf;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.user.User;


/**
 * {@link UserPermissionService} - The service for {@link UserPermissionBits}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc + {@link #getUserPermissionBits(Context, int[])}
 */
@SingletonService
public interface UserPermissionService {

    /**
     * Determines the instance of <code>UserPermissionBits</code> that corresponds to given user ID.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The instance of <code>UserPermissionBits</code>
     * @throws OXException If user's configuration could not be determined
     */
    UserPermissionBits getUserPermissionBits(int userId, int contextId) throws OXException;

    /**
     * Determines the instance of <code>UserPermissionBits</code> that corresponds to given user ID.
     *
     * @param userId The user identifier
     * @param ctx The context
     * @return The instance of <code>UserPermissionBits</code>
     * @throws OXException If user's configuration could not be determined
     * @see #getUserPermissionBits(int, int[], Context)
     */
    UserPermissionBits getUserPermissionBits(int userId, Context ctx) throws OXException;

    /**
     * This method reads several user module access permissions. This method is faster than reading separately the {@link UserPermissionBits}
     * for every given user.
     *
     * @param ctx The context
     * @param users The users for whom module access permission should be loaded.
     * @return An array with the module access permissions of the given users.
     * @throws OXException If users configuration could not be loaded.
     */
    UserPermissionBits[] getUserPermissionBits(Context ctx, User[] users) throws OXException;

    /**
     * Determines the instance of <code>UserPermissionBits</code> that corresponds to given user ID.
     *
     * @param connection The database connection to use. May be read only.
     * @param userId The user ID
     * @param context The context
     * @return the instance of <code>UserPermissionBits</code>
     * @throws OXException If users configuration could not be determined
     * @see #getUserPermissionBits(int, int[], Context)
     */
    UserPermissionBits getUserPermissionBits(Connection connection, int userId, Context ctx) throws OXException;

    /**
     * This method reads several user module access permissions. This method is faster than reading separately the {@link UserPermissionBits}
     * for every given user.
     *
     * @param ctx The context
     * @param userIds The identifiers of the users for whom module access permission should be loaded.
     * @return An array with the module access permissions of the given users.
     * @throws OXException If users configuration could not be loaded.
     */
    UserPermissionBits[] getUserPermissionBits(Context ctx, int[] userIds) throws OXException;

    /**
     * Clears the whole storage. All kept instances of <code>UserPermissionBits</code> are going to be removed from storage.
     * <p>
     * <b>NOTE:</b> Only the instances are going to be removed from storage; underlying database is not affected
     * @throws OXException if users configuration could not be saved.
     */
    void clearStorage() throws OXException;

    /**
     * Removes the instance of <code>UserPermissionBits</code> that corresponds to given user ID from storage.
     * <p>
     * <b>NOTE:</b> Only the instance is going to be removed from storage; underlying database is not affected
     *
     * @param userId The user identifier
     * @param ctx The context
     * @throws OXException If removal fails
     */
    void removeUserPermissionBits(int userId, Context ctx) throws OXException;

    /**
     * Saves the given permission bits.
     *
     * @param permissionBits The permission bits.
     * @throws OXException if users configuration could not be saved.
     */
    void saveUserPermissionBits(UserPermissionBits permissionBits) throws OXException;

    /**
     * Saves the given permission bits.
     *
     * @param connection The database connection to use. Must not be read only.
     *    No transaction handling is performed, you probably want to commit or rollback
     *    afterwards, depending on the success of this call.
     * @param permissionBits The permission bits.
     * @throws OXException if users configuration could not be saved.
     */
    void saveUserPermissionBits(Connection connection, UserPermissionBits permissionBits) throws OXException;

    /**
     * Deletes the permission bits of the given user.
     *
     * @param context The context
     * @param userId The user ID
     * @throws OXException if users configuration could not be deleted.
     */
    void deleteUserPermissionBits(Context context, int userId) throws OXException;

    /**
     * Deletes the permission bits of the given user.
     *
     * @param connection The database connection to use. Must not be read only.
     *    No transaction handling is performed, you probably want to commit or rollback
     *    afterwards, depending on the success of this call.
     * @param context The context
     * @param userId The user ID
     * @throws OXException if users configuration could not be deleted.
     */
    void deleteUserPermissionBits(Connection connection, Context context, int userId) throws OXException;

    /**
     * Retrieves the accessCombinationName for the given user
     *
     * @param context The context
     * @param userId The user id
     * @return The accessCombinationName
     * @throws OXException if accessCombinationName could not be retrieved
     */
    String getAccessCombinationName(Context context, int userId) throws OXException;

}
