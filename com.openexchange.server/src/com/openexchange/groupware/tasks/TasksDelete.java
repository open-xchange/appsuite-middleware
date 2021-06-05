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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.session.Session;

/**
 * This class implements the delete listener for deleting tasks and participants
 * if a user or a group is deleted.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TasksDelete implements DeleteListener {

    private static final FolderStorage foldStor = FolderStorage.getInstance();

    private static final ParticipantStorage partStor = ParticipantStorage.getInstance();

    /**
     * Default constructor.
     */
    public TasksDelete() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        switch (event.getType()) {
        case DeleteEvent.TYPE_CONTEXT:
            deleteContext(event, writeCon);
            break;
        case DeleteEvent.TYPE_USER:
            deleteUser(event, writeCon);
            break;
        case DeleteEvent.TYPE_GROUP:
            deleteGroup(event, readCon, writeCon);
            break;
        case DeleteEvent.TYPE_RESOURCE:
        case DeleteEvent.TYPE_RESOURCE_GROUP:
            break;
        default:
        }
    }

    /**
     * Delete a context from tasks module.
     */
    private void deleteContext(DeleteEvent event, Connection writeCon) throws OXException {
        Context ctx = event.getContext();
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();

            stmt.addBatch("DELETE FROM del_task_participant WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM del_task_folder WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM del_task WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM task_removedparticipant WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM task_eparticipant WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM task_participant WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM task_folder WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM task WHERE cid=" + ctx.getContextId());

            stmt.executeBatch();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Delete a user from all tasks.
     * @param event Event.
     * @param con writable database connection.
     * @throws OXException if the delete gives an error.
     */
    private void deleteUser(final DeleteEvent event, final Connection con) throws OXException {
        // First remove the user from the participants of tasks. Then only
        // tasks exist that have other users as participants or no one.
        removeUserFromParticipants(event, con);
        // Check now the folder mappings. Find all folder mappings for the
        // user to delete. If tasks has participants and several folder
        // mappings(delegated in private folder) the folder mapping can be
        // deleted. A single folder mapping for a task with participants
        // must be a public folder and the
        // folder mapping must be changed to mailadmin.
        // All other single folder mappings in a public folder are also
        // changed to mailadmin. Remaining task with single folder mappings
        // must be private tasks that can be deleted.
        // Remove private task in private folders.
        Integer destUser = event.getDestinationUserID();
        if (destUser == null) {
            destUser = event.getContext().getMailadmin();
        }
        assignToUser(event, con, destUser);
        // Change createdFrom and modifiedBy attributes of left over tasks.
        changeCFMB(event, con, destUser);
    }

    /**
     * Removes the user from task participants.
     * @param event Event.
     * @param con writable database connection.
     * @throws OXException if an exception occurs.
     */
    private void removeUserFromParticipants(final DeleteEvent event, final Connection con) throws OXException {
        final Context ctx = event.getContext();
        final int userId = event.getId();
        for (final StorageType type : StorageType.values()) {
            final int[] tasks = partStor.findTasksWithParticipant(ctx, con, userId, type);
            for (final int task : tasks) {
                partStor.deleteInternal(ctx, con, task, userId , type, true);
            }
        }
    }

    /**
     * Assigns all left tasks to mailadmin.
     * @param event Event.
     * @param con writable database connection.
     * @throws OXException if a problem occurs.
     */
    private void assignToUser(final DeleteEvent event, final Connection con, final Integer destUser) throws OXException {
        final Session session = event.getSession();
        final Context ctx = event.getContext();
        final int userId = event.getId();
        for (final StorageType type : StorageType.TYPES_AD) {
            final int[][] result = foldStor.searchFolderByUser(ctx, con, userId, type);
            for (final int[] folderAndTask : result) {
                final int folderId = folderAndTask[0];
                FolderObject folder = null;
                try {
                    folder = Tools.getFolder(ctx, con, folderId);
                } catch (OXException e) {
                    // Nothing to do.
                }
                final int taskId = folderAndTask[1];
                final Set<Folder> folders = foldStor.selectFolder(ctx, con, taskId, type);
                if (folders.size() == 0) {
                    throw TaskExceptionCode.NO_PERMISSION.create(I(taskId), I(folderId));
                } else if (folders.size() > 1) {
                    // Participant with userId already removed by
                    // removeUserFromParticipants()
                    foldStor.deleteFolder(ctx, con, taskId, folderId, type);
                } else if (ctx.getMailadmin() == userId || StorageType.DELETED == type || null == folder) {
                    TaskLogic.removeTask(session, ctx, con, folderId, taskId, type, false);
                } else if (Tools.isFolderPublic(folder)) {
                    foldStor.deleteFolder(ctx, con, taskId, folderId, type);
                    final Folder aFolder = new Folder(folderId, destUser);
                    foldStor.insertFolder(ctx, con, taskId, aFolder, type);
                } else if (Tools.isFolderPrivate(folder)) {
                    TaskLogic.removeTask(session, ctx, con, folderId, taskId, type, false);
                } else {
                    throw TaskExceptionCode.UNIMPLEMENTED.create();
                }
            }
        }
    }

    /**
     * Remove deleted user from remaining tasks in the attributes createdFrom and lastModified.
     * @param event Event.
     * @param con writable database connection.
     * @throws OXException if an exception occurs.
     */
    private void changeCFMB(final DeleteEvent event, final Connection con, int destUser) throws OXException {
        if (destUser <= 0) {
            destUser = event.getContext().getMailadmin();
        }
        final int[] modified = new int[] { DataObject.CREATED_BY, DataObject.MODIFIED_BY, DataObject.LAST_MODIFIED };
        final Context ctx = event.getContext();
        final int userId = event.getId();
        final TaskStorage stor = TaskStorage.getInstance();
        for (final StorageType type : StorageType.TYPES_AD) {
            final int[] tasks = TaskSearch.getInstance().findUserTasks(ctx, con, userId, type);
            for  (final int taskId : tasks) {
                final Task update = stor.selectTask(ctx, con, taskId, type);
                final Date lastModified = update.getLastModified();
                if (update.getCreatedBy() == userId) {
                    update.setCreatedBy(destUser);
                }
                if (update.getModifiedBy() == userId) {
                    update.setModifiedBy(destUser);
                }
                update.setLastModified(new Date());
                stor.updateTask(ctx, con, update, lastModified, modified, type);
            }
        }
    }

    /**
     * Deletes a group from the participants of tasks.
     * @param event Event.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @throws OXException if the delete gives an error.
     */
    private void deleteGroup(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        final Context ctx = event.getContext();
        final int groupId = event.getId();
        final ParticipantStorage partStor = ParticipantStorage.getInstance();
        for (final StorageType type : StorageType.values()) {
            final int[] tasks = partStor.findTasksWithGroup(ctx, readCon, groupId, type);
            for (final int task : tasks) {
                Set<InternalParticipant> participants = partStor.selectInternal(ctx, readCon, task, type);
                participants = TaskLogic.extractWithGroup(participants, groupId);
                removeGroup(participants);
                partStor.updateInternal(ctx, writeCon, task, participants, type);
            }
        }
    }

    /**
     * Removes the group from the participants.
     * @param participants task internal participants.
     */
    private void removeGroup(final Set<InternalParticipant> participants) {
        for (final InternalParticipant participant : participants) {
            participant.setGroupId(null);
        }
    }
}
