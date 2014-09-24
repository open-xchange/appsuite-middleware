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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
 * {@link UserPermissionService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SingletonService
public interface UserPermissionService {

    /**
     * Determines the instance of <code>UserPermissionBits</code> that corresponds to given user ID.
     *
     * @param userId The user ID
     * @param context The context
     * @return the instance of <code>UserPermissionBits</code>
     * @throws OXException If users configuration could not be determined
     * @see #getUserPermissionBits(int, int[], Context)
     */
    public UserPermissionBits getUserPermissionBits(int userId, Context context) throws OXException;

    /**
     * Gets the {@link UserPermissionBits} for a bunch of users.
     *
     * @param context The context
     * @param users User objects that module access permission should be loaded.
     * @return An array with the module access permissions of the given users.
     * @throws OXException if users configuration could not be loaded.
     */
    public UserPermissionBits[] getUserPermissionBits(Context context, User[] users) throws OXException;

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
    UserPermissionBits getUserPermissionBits(Connection connection, int userId, Context context) throws OXException;

    /**
     * Saves the given permission bits.
     *
     * @param permissionBits The permission bits.
     * @throws OXException if users configuration could not be saved.
     */
    public void saveUserPermissionBits(UserPermissionBits permissionBits) throws OXException;

    /**
     * Saves the given permission bits.
     *
     * @param connection The database connection to use. Must not be read only.
     *    No transaction handling is performed, you probably want to commit or rollback
     *    afterwards, depending on the success of this call.
     * @param permissionBits The permission bits.
     * @throws OXException if users configuration could not be saved.
     */
    public void saveUserPermissionBits(Connection connection, UserPermissionBits permissionBits) throws OXException;

    /**
     * Deletes the permission bits of the given user.
     *
     * @param context The context
     * @param userId The user ID
     * @throws OXException if users configuration could not be deleted.
     */
    public void deleteUserPermissionBits(Context context, int userId) throws OXException;

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
    public void deleteUserPermissionBits(Connection connection, Context context, int userId) throws OXException;


}
