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

import java.sql.Connection;
import java.util.Date;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.downgrade.DowngradeEvent;
import com.openexchange.groupware.downgrade.DowngradeListener;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.user.User;

/**
 * This class implements the methods to delete tasks if a user loses
 * functionalities of the tasks module.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TasksDowngrade implements DowngradeListener {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TasksDowngrade.class);

    /**
     * Default constructor.
     */
    public TasksDowngrade() {
        super();
    }

    @Override
    public void downgradePerformed(final DowngradeEvent event)
        throws OXException {
        final UserPermissionBits permissionBits = event.getNewUserConfiguration().getUserPermissionBits();
        final Session session = event.getSession();
        final Context ctx = event.getContext();
        final Connection con = event.getWriteCon();
        if (!permissionBits.hasTask()) {
            // If the user completely loses tasks the following should be deleted:
            // - All tasks in private folders if no other user sees them.
            // - The participation of the user in all tasks.
            // - All tasks in public folders that can be only edited by this user.
            try {
                removeTasks(session, ctx, permissionBits.getUserId(), con);
            } catch (OXException e) {
                throw e;
            }
        } else if (!permissionBits.canDelegateTasks()) {
            // Remove all delegations of tasks that the user created.
            try {
                removeDelegations(session, ctx, permissionBits.getUserId(), permissionBits, con);
            } catch (OXException e) {
                throw e;
            }
        }
    }

    private static final ParticipantStorage partStor = ParticipantStorage.getInstance();

    private void removeTasks(final Session session, final Context ctx,
        final int userId, final Connection con) throws OXException {
        final User user = Tools.getUser(ctx, userId);
        // Find private task folder.
        SearchIterator<FolderObject> iter = OXFolderIteratorSQL
            .getAllVisibleFoldersIteratorOfType(userId, user.getGroups(),
            new int[] { FolderObject.TASK }, FolderObject.PRIVATE,
            new int[] { FolderObject.TASK }, ctx);
        try {
            while (iter.hasNext()) {
                final FolderObject folder = iter.next();
                removeTaskInPrivateFolder(session, ctx, con, userId, folder);
            }
        } finally {
            iter.close();
        }
        // Remove all participations.
        for (final StorageType type : StorageType.TYPES_AD) {
            final int[] taskIds = partStor.findTasksWithParticipant(ctx, con,
                userId, type);
            for (final int taskId : taskIds) {
                partStor.deleteInternal(ctx, con, taskId, userId, type, true);
                final Set<Folder> folders = foldStor.selectFolder(ctx, con,
                    taskId, type);
                if (0 == folders.size()) {
                    // Inconsistent data
                    throw TaskExceptionCode.MISSING_FOLDER.create(Integer.valueOf(taskId));
                } else if (folders.size() > 1) {
                    // Simply remove folder mapping for this user.
                    final Folder folder = FolderStorage.extractFolderOfUser(
                        folders, userId);
                    if (null != folder) {
                        foldStor.deleteFolder(ctx, con, taskId,
                            folder.getIdentifier(), type);
                    }
                } else if (ctx.getMailadmin() == userId) {
                    final Folder folder = folders.iterator().next();
                    // Remove task if mailadmin even can't read them anymore.
                    TaskLogic.removeTask(session, ctx, con,
                        folder.getIdentifier(), taskId, type);
                } else {
                    Folder folder = folders.iterator().next();
                    foldStor.deleteFolder(ctx, con, taskId,
                        folder.getIdentifier(), type);
                    folder = new Folder(folder.getIdentifier(), ctx.getMailadmin());
                    foldStor.insertFolder(ctx, con, taskId, folder, type);
                }
            }
        }
        // Iterate over all public folders
        iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(userId,
            user.getGroups(), new int[] { FolderObject.TASK },
            FolderObject.PUBLIC, new int[] { FolderObject.TASK }, ctx);
        try {
            while (iter.hasNext()) {
                final FolderObject folder = iter.next();
                final OCLPermission[] ocls = folder.getPermissionsAsArray();
                boolean other = false;
                for (int i = 0; i < ocls.length && !other; i++) {
                    final OCLPermission perm = ocls[i];
                    if (perm.getEntity() != userId && perm.canWriteAllObjects()) {
                        other = true;
                    }
                }
                // If no other has write permission remove the tasks.
                if (!other) {
                    removeTaskInFolder(session, ctx, con, folder);
                }
            }
        } finally {
            iter.close();
        }
    }

    private void removeTaskInPrivateFolder(final Session session, final Context ctx, final Connection con, final int userId, final FolderObject folder) throws OXException {
        for (final StorageType type : StorageType.TYPES_AD) {
            final int[] taskIds = foldStor.getTasksInFolder(ctx, con, folder.getObjectID(), type);
            for (final int taskId : taskIds) {
                final Set<Folder> folders = foldStor.selectFolder(ctx, con, taskId, type);
                if (0 == folders.size()) {
                    // Inconsistent data
                    throw TaskExceptionCode.MISSING_FOLDER.create(Integer.valueOf(taskId));
                } else if (folders.size() > 1) {
                    // Simply remove folder mapping for this user.
                    Folder folderOfUser = FolderStorage.extractFolderOfUser(folders, userId);
                    if (folderOfUser == null) {
                        // Inconsistent data
                        throw TaskExceptionCode.PARTICIPANT_FOLDER_INCONSISTENCY.create(taskId, userId, ctx.getContextId());
                    }
                    final int folderId = folderOfUser.getIdentifier();
                    foldStor.deleteFolder(ctx, con, taskId, folderId, type);
                    // And remove participation. No check because task can be
                    // delegated.
                    partStor.deleteInternal(ctx, con, taskId, userId, type, false);
                } else {
                    TaskLogic.removeTask(session, ctx, con, folder.getObjectID(), taskId, type);
                }
            }
        }
    }

    private void removeDelegations(final Session session, final Context ctx,
        final int userId, final UserPermissionBits permissionBits,
        final Connection con) throws OXException {
        final User user = Tools.getUser(ctx, userId);
        // Find all private folder.
        SearchIterator<FolderObject> iter = OXFolderIteratorSQL
            .getAllVisibleFoldersIteratorOfType(userId, user.getGroups(),
            new int[] { FolderObject.TASK }, FolderObject.PRIVATE,
            new int[] { FolderObject.TASK }, ctx);
        try {
            while (iter.hasNext()) {
                final FolderObject folder = iter.next();
                // Remove the delegations of tasks in that folders.
                removeDelegationsInFolder(session, ctx, permissionBits, con, user,
                    folder);
            }
        } finally {
            iter.close();
        }
        // Find all public folder.
        iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(userId,
            user.getGroups(), new int[] { FolderObject.TASK },
            FolderObject.PUBLIC, new int[] { FolderObject.TASK }, ctx);
        try {
            while (iter.hasNext()) {
                final FolderObject folder = iter.next();
                final OCLPermission[] ocls = folder.getPermissionsAsArray();
                boolean other = false;
                for (int i = 0; i < ocls.length && !other; i++) {
                    final OCLPermission perm = ocls[i];
                    if (perm.getEntity() != userId && perm.canWriteAllObjects()) {
                        other = true;
                    }
                }
                if (!other) {
                    // If no other user than the current downgraded is able
                    // to edit the tasks, then remove the delegations.
                    removeDelegationsInFolder(session, ctx, permissionBits, con,
                        user, folder);
                }
            }
        } finally {
            iter.close();
        }
    }

    private static FolderStorage foldStor = FolderStorage.getInstance();

    private void removeTaskInFolder(final Session session, final Context ctx,
        final Connection con, final FolderObject folder) throws OXException {
        for (final StorageType type : StorageType.TYPES_AD) {
            final int[] taskIds = foldStor.getTasksInFolder(ctx, con, folder
                .getObjectID(), type);
            for (final int taskId : taskIds) {
                TaskLogic.removeTask(session, ctx, con, folder.getObjectID(),
                    taskId, type);
            }
        }
    }

    private void removeDelegationsInFolder(final Session session,
        final Context ctx, final UserPermissionBits permissionBits,
        final Connection con, final User user, final FolderObject folder)
        throws OXException {
        for (final StorageType type : StorageType.TYPES_AD) {
            final int[] taskIds = foldStor.getTasksInFolder(ctx, con, folder
                .getObjectID(), type);
            for (final int taskId : taskIds) {
                final Task task = new Task();
                task.setObjectID(taskId);
                task.setParentFolderID(folder.getObjectID());
                task.setParticipants(new Participant[0]);
                final UpdateData update = new UpdateData(ctx, user, permissionBits,
                    folder, task, new Date(), type);
                update.prepareWithoutChecks();
                update.doUpdate();
                if (StorageType.ACTIVE == type) {
                    update.sentEvent(session);
                    try {
                        update.updateReminder();
                    } catch (OXException e) {
                        LOG.error("Problem while updating reminder for a task.",
                            e);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return 3;
    }
}
