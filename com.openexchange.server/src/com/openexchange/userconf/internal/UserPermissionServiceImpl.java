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

package com.openexchange.userconf.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link UserPermissionServiceImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserPermissionServiceImpl implements UserPermissionService {

    private AccessCombinationNameCache cache;

    /**
     * Initializes a new {@link UserPermissionServiceImpl}.
     * 
     * @throws OXException
     * @throws ClassNotFoundException
     */
    public UserPermissionServiceImpl() throws OXException {
        super();
        cache = new AccessCombinationNameCache();
        cache.initAccessCombinations();
    }

    @Override
    public UserPermissionBits getUserPermissionBits(int userId, int contextId) throws OXException {
        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, contextId);
    }

    @Override
    public UserPermissionBits getUserPermissionBits(int userId, Context ctx) throws OXException {
        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, ctx);
    }

    @Override
    public UserPermissionBits[] getUserPermissionBits(Context ctx, User[] users) throws OXException {
        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(ctx, users);
    }

    @Override
    public UserPermissionBits getUserPermissionBits(Connection connection, int userId, Context ctx) throws OXException {
        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(connection, userId, ctx);
    }

    @Override
    public void saveUserPermissionBits(UserPermissionBits permissionBits) throws OXException {
        UserPermissionBitsStorage.getInstance().saveUserPermissionBits(permissionBits.getPermissionBits(), permissionBits.getUserId(), permissionBits.getContext());
    }

    @Override
    public void saveUserPermissionBits(Connection connection, UserPermissionBits permissionBits) throws OXException {
        UserPermissionBitsStorage.getInstance().saveUserPermissionBits(connection, permissionBits.getPermissionBits(), permissionBits.getUserId(), permissionBits.getContext());
    }

    @Override
    public void deleteUserPermissionBits(Context context, int userId) throws OXException {
        try {
            RdbUserPermissionBitsStorage.deleteUserPermissionBits(userId, context);
        } catch (SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void deleteUserPermissionBits(Connection connection, Context context, int userId) throws OXException {
        try {
            RdbUserPermissionBitsStorage.deleteUserPermissionBits(userId, connection, context);
        } catch (SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public UserPermissionBits[] getUserPermissionBits(Context ctx, int[] userIds) throws OXException {
        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(ctx, userIds);
    }

    @Override
    public void clearStorage() throws OXException {
        UserPermissionBitsStorage.getInstance().clearStorage();
    }

    @Override
    public void removeUserPermissionBits(int userId, Context ctx) throws OXException {
        UserPermissionBitsStorage.getInstance().removeUserPermissionBits(userId, ctx);
    }

    @Override
    public String getAccessCombinationName(Context context, int userId) throws OXException {
        int contextId = context.getContextId();
        DatabaseService dbService = ServerServiceRegistry.getServize(DatabaseService.class);
        Connection read_ox_con = dbService.getReadOnly(context);
        try {
            UserPermissionBits user = getUserPermissionBits(userId, context);

            UserModuleAccess acc = new UserModuleAccess();

            acc.setCalendar(user.hasPermission(UserConfiguration.CALENDAR));
            acc.setContacts(user.hasPermission(UserConfiguration.CONTACTS));
            acc.setEditPublicFolders(user.hasPermission(UserConfiguration.EDIT_PUBLIC_FOLDERS));
            acc.setReadCreateSharedFolders(user.hasPermission(UserConfiguration.READ_CREATE_SHARED_FOLDERS));
            acc.setIcal(user.hasPermission(UserConfiguration.ICAL));
            acc.setInfostore(user.hasPermission(UserConfiguration.INFOSTORE));
            acc.setSyncml(user.hasPermission(UserConfiguration.MOBILITY));
            acc.setTasks(user.hasPermission(UserConfiguration.TASKS));
            acc.setVcard(user.hasPermission(UserConfiguration.VCARD));
            acc.setWebdav(user.hasPermission(UserConfiguration.WEBDAV));
            acc.setWebdavXml(user.hasPermission(UserConfiguration.WEBDAV_XML));
            acc.setWebmail(user.hasPermission(UserConfiguration.WEBMAIL));
            acc.setDelegateTask(user.hasPermission(UserConfiguration.DELEGATE_TASKS));
            acc.setEditGroup(user.hasPermission(UserConfiguration.EDIT_GROUP));
            acc.setEditResource(user.hasPermission(UserConfiguration.EDIT_RESOURCE));
            acc.setEditPassword(user.hasPermission(UserConfiguration.EDIT_PASSWORD));
            acc.setCollectEmailAddresses(user.hasPermission(UserConfiguration.COLLECT_EMAIL_ADDRESSES));
            acc.setMultipleMailAccounts(user.hasPermission(UserConfiguration.MULTIPLE_MAIL_ACCOUNTS));
            acc.setPublication(user.hasPermission(UserConfiguration.PUBLICATION));
            acc.setSubscription(user.hasPermission(UserConfiguration.SUBSCRIPTION));
            acc.setActiveSync(user.hasPermission(UserConfiguration.ACTIVE_SYNC));
            acc.setUSM(user.hasPermission(UserConfiguration.USM));
            acc.setOLOX20(user.hasPermission(UserConfiguration.OLOX20));
            acc.setDeniedPortal(user.hasPermission(UserConfiguration.DENIED_PORTAL));
            final OXFolderAdminHelper adminHelper = new OXFolderAdminHelper();
            acc.setGlobalAddressBookDisabled(adminHelper.isGlobalAddressBookDisabled(contextId, userId, read_ox_con));
            acc.setPublicFolderEditable(adminHelper.isPublicFolderEditable(contextId, userId, read_ox_con));
            return cache.getNameForAccessCombination(acc);
        } finally {
            dbService.backReadOnly(context, read_ox_con);
        }
    }

}
