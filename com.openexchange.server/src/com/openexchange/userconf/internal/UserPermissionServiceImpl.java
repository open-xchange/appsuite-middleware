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

package com.openexchange.userconf.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.RdbUserPermissionBitsStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.user.User;
import com.openexchange.userconf.UserPermissionService;


/**
 * {@link UserPermissionServiceImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserPermissionServiceImpl implements UserPermissionService {

    private final AccessCombinationNameCache cache;

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
