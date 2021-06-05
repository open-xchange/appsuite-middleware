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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.User;

/**
 * Contains convenience methods for checking access rights.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Permission {

    /**
     * Prevent instantiation
     */
    private Permission() {
        super();
    }

    /**
     * Checks if a user
     * @param ctx
     * @param user
     * @param userPerms
     * @param folder
     * @param task
     * @throws OXException
     */
    public static void checkDelete(final Context ctx, final User user, final UserPermissionBits userPerms, final FolderObject folder, final Task task) throws OXException {
        checkForTaskFolder(folder);
        final OCLPermission permission = getPermission(ctx, user, userPerms, folder);
        if (!permission.canDeleteAllObjects() && !permission.canDeleteOwnObjects()) {
            throw TaskExceptionCode.NO_DELETE_PERMISSION.create();
        }
        final boolean onlyOwn = !permission.canDeleteAllObjects() && permission.canDeleteOwnObjects();
        if (onlyOwn && task.getCreatedBy() != user.getId()) {
            throw TaskExceptionCode.NO_DELETE_PERMISSION.create();
        }
        final boolean noPrivate = Tools.isFolderShared(folder, user);
        if (noPrivate && task.getPrivateFlag()) {
            throw TaskExceptionCode.NO_DELETE_PERMISSION.create();
        }
    }

    /**
     * Checks if the user is allowed to delegate tasks.
     * @param userPerms groupware configuration of the user.
     * @param participants Participants of a task.
     * @throws OXException if delegation is not allowed.
     */
    static void checkDelegation(final UserPermissionBits userPerms, final Participant[] participants) throws OXException {
        if (!userPerms.canDelegateTasks() && null != participants && participants.length > 0) {
            throw TaskExceptionCode.NO_DELEGATE_PERMISSION.create();
        }
    }

    /**
     * Checks if a user is allowed to create a task in a folder.
     * @param ctx Context.
     * @param user User.
     * @param userPerms Module configuration of the user.
     * @param folder Folder in that a task should be created.
     * @throws OXException if the user is not allowed to create the task.
     */
    static void checkCreate(final Context ctx, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        if (!userPerms.hasTask()) {
            throw TaskExceptionCode.NO_TASKS.create(Integer.valueOf(user.getId()));
        }
        checkForTaskFolder(folder);
        final OCLPermission permission = getPermission(ctx, user, userPerms, folder);
        if (!permission.isFolderVisible()) {
            throw TaskExceptionCode.NOT_VISIBLE.create(I(folder.getObjectID()));
        }
        if (!permission.canCreateObjects()) {
            throw TaskExceptionCode.NO_CREATE_PERMISSION.create(folder.getFolderName(), I(folder.getObjectID()));
        }
    }

    /**
     * Checks if the user is only allowed to see the folder.
     * @param ctx Context.
     * @param user User.
     * @param userPerms Groupware configuration of the user.
     * @param folder folder object that should be tested for only see
     * permission.
     * @return <code>true</code> if the folder can only be seen.
     * @throws OXException if the folder is not a task folder or getting the
     * user specific permissions fails.
     */
    static boolean canOnlySeeFolder(final Context ctx, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        checkForTaskFolder(folder);
        final OCLPermission permission = getPermission(ctx, user, userPerms, folder);
        return permission.isFolderVisible() && !permission.canReadAllObjects() && !permission.canReadOwnObjects();
    }

    static boolean isFolderVisible(final Context ctx, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        checkForTaskFolder(folder);
        final OCLPermission permission = getPermission(ctx, user, userPerms, folder);
        return permission.isFolderVisible();
    }

    /**
     * Checks if the user is allowed to read tasks in a folder. Beware that the
     * private flag of tasks must be checked.
     * @param ctx Context.
     * @param user User.
     * @param userPerms Groupware configuration of the user.
     * @param folder folder object that should be tested for read access.
     * @return <code>false</code> if all objects can be read and <code>true</code> if only own objects can be read.
     * @throws OXException if the reading is not okay.
     */
    static boolean canReadInFolder(final Context ctx, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            return canReadInFolder(ctx, con, user, userPerms, folder);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Checks if the user is allowed to read tasks in a folder. Beware that the
     * private flag of tasks must be checked.
     * @param ctx Context.
     * @param con read only database connection.
     * @param user User.
     * @param userPerms Groupware configuration of the user.
     * @param folder folder object that should be tested for read access.
     * @return <code>false</code> if all objects can be read and <code>true</code> if only own objects can be read.
     * @throws OXException if the reading is not okay.
     */
    static boolean canReadInFolder(final Context ctx, final Connection con, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        checkForTaskFolder(folder);
        final OCLPermission permission = getPermission(ctx, con, user, userPerms, folder);
        if (!permission.canReadAllObjects() && !permission.canReadOwnObjects()) {
            throw TaskExceptionCode.FOLDER_NOT_FOUND.create(I(folder.getObjectID()));
        }
        return !permission.canReadAllObjects() && permission.canReadOwnObjects();
    }

    /**
     * Checks if the user is allowed to read the task.
     * @param ctx Context.
     * @param user User.
     * @param userPerms Groupware configuration of the user.
     * @param folder folder object that should be tested for read access.
     * @param task Task to read.
     * @throws OXException if the reading is not okay.
     */
    static void canReadInFolder(final Context ctx, final User user, final UserPermissionBits userPerms, final FolderObject folder, final Task task) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            canReadInFolder(ctx, con, user, userPerms, folder, task);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Checks if the user is allowed to read the task.
     * @param ctx Context.
     * @param con read only database connection.
     * @param user User.
     * @param userPerms Groupware configuration of the user.
     * @param folder folder object that should be tested for read access.
     * @param task Task to read.
     * @throws OXException if the reading is not okay.
     */
    static void canReadInFolder(final Context ctx, final Connection con, final User user, final UserPermissionBits userPerms, final FolderObject folder, final Task task) throws OXException {
        final boolean onlyOwn = canReadInFolder(ctx, con, user, userPerms, folder);
        if (onlyOwn && (user.getId() != task.getCreatedBy())) {
            throw TaskExceptionCode.NO_READ_PERMISSION.create(I(folder.getObjectID()));
        }
    }

    static void checkReadInFolder(final Context ctx, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            checkReadInFolder(ctx, con, user, userPerms, folder);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    static void checkReadInFolder(final Context ctx, final Connection con, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        checkForTaskFolder(folder);
        final OCLPermission permission = getPermission(ctx, con, user, userPerms, folder);
        if (!permission.canReadAllObjects() && !permission.canReadOwnObjects()) {
            throw TaskExceptionCode.NO_READ_PERMISSION.create(I(folder.getObjectID()));
        }
    }

    /**
     * Checks if a user is allowed to update a task.
     * @param ctx Context.
     * @param user User.
     * @param userPermissionBits Groupware configuration of the user.
     * @param folder folder object that should be tested for write access.
     * @param task Task to update.
     * @throws OXException if the task can't be updated.
     */
    static void checkWriteInFolder(final Context ctx, final User user, final UserPermissionBits userPermissionBits, final FolderObject folder, final Task task) throws OXException {
        checkForTaskFolder(folder);
        final OCLPermission permission = getPermission(ctx, user, userPermissionBits, folder);
        if (!permission.canWriteAllObjects() && !(permission.canWriteOwnObjects() && (user.getId() == task.getCreatedBy()))) {
            throw TaskExceptionCode.NO_WRITE_PERMISSION.create(I(folder.getObjectID()));
        }
    }

    static OCLPermission getPermission(final Context ctx, final User user, final UserPermissionBits userPermissionBits, final FolderObject folder) throws OXException {
        return new OXFolderAccess(ctx).getFolderPermission(folder.getObjectID(), user.getId(), userPermissionBits);
    }

    static OCLPermission getPermission(final Context ctx, final Connection con, final User user, final UserPermissionBits userPerms, final FolderObject folder) throws OXException {
        return new OXFolderAccess(con, ctx).getFolderPermission(folder.getObjectID(), user.getId(), userPerms);
    }

    static void checkForTaskFolder(final FolderObject folder) throws OXException {
        if (!Tools.isFolderTask(folder)) {
            throw TaskExceptionCode.FOLDER_NOT_FOUND.create(I(folder.getObjectID()));
        }
    }
}
