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

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * This class implements the method that are necessary for linking tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Task2Links {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Task2Links.class);

    /**
     * Prevent instantiation
     */
    private Task2Links() {
        super();
    }

    /**
     * Checks if a task referenced by a link may be read.
     * @param session Session.
     * @param taskId Unique identifier of the task.
     * @param folderId Unique identifier of the folder through that the task is
     * referenced.
     * @return <code>true</code> if the task may be read, <code>false</code>
     * otherwise.
     */
    public static boolean checkMayReadTask(final Session session, final Context ctx, final UserPermissionBits permissionBits, final int taskId) {
        final User user;
        final Task task;
        final Set<Folder> folders;
        try {
            user = Tools.getUser(ctx, session.getUserId());
            final TaskStorage storage = TaskStorage.getInstance();
            task = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
            folders = FolderStorage.getInstance().selectFolder(ctx, taskId, StorageType.ACTIVE);
        } catch (OXException e) {
            LOG.error("", e);
            return false;
        }
        for (final Folder folder : folders) {
            if (mayRead(ctx, user, permissionBits, task, folder)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkMayReadTask(final Session session, final Context ctx, final UserPermissionBits permissionBits, final int taskId, final int folderId) {
        final User user;
        final Task task;
        final Folder folder;
        try {
            user = Tools.getUser(ctx, session.getUserId());
            final TaskStorage storage = TaskStorage.getInstance();
            task = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
            folder = FolderStorage.getInstance().selectFolderById(ctx, taskId, folderId, StorageType.ACTIVE);
        } catch (OXException e) {
            LOG.error("", e);
            return false;
        }
        return null == folder ? false : mayRead(ctx, user, permissionBits, task, folder);
    }

    private static boolean mayRead(final Context ctx, final User user, final UserPermissionBits permissionBits, final Task task, final Folder folder) {
        final FolderObject folder2;
        try {
            folder2 = Tools.getFolder(ctx, folder.getIdentifier());
        } catch (OXException e) {
            LOG.error("", e);
            return false;
        }
        try {
            Permission.isFolderVisible(ctx, user, permissionBits, folder2);
            Permission.canReadInFolder(ctx, user, permissionBits, folder2, task);
            return true;
        } catch (OXException e) {
            return false;
        }
    }
}
