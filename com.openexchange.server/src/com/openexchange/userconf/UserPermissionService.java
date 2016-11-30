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

package com.openexchange.userconf;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.osgi.annotation.SingletonService;


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
