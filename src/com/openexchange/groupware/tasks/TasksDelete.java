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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;

/**
 * This class implements the delete listener for deleting tasks and participants
 * if a user or a group is deleted.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TasksDelete implements DeleteListener {

    /**
     * StorageTypes used for tasks.
     */
    private static final StorageType[] TYPES_AD = new StorageType[] {
        StorageType.ACTIVE, StorageType.DELETED };

    /**
     * Default constructor.
     */
    public TasksDelete() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void deletePerformed(final DeleteEvent event,
        final Connection readCon, final Connection writeCon)
        throws DeleteFailedException {
        switch (event.getType()) {
        case DeleteEvent.TYPE_USER:
            deleteUser(event, readCon, writeCon);
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
     * Delete a user from all tasks.
     * @param event Event.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @throws DeleteFailedException if the delete gives an error.
     */
    private void deleteUser(final DeleteEvent event, final Connection readCon,
        final Connection writeCon) throws DeleteFailedException {
        try {
            // First remove the user from the participants of tasks. Then only
            // tasks exist that have other users as participants or no one.
            removeUserFromParticipants(event, readCon, writeCon);
            // Check now the folder mappings. Find all folder mappings for the
            // user to delete. If tasks has participants and several folder
            // mappings(delegated in private folder) the folder mapping can be
            // deleted. A single folder mapping for a task with participants
            // must be a public folder and the
            // folder mapping must be changed to mailadmin.
            // All other single folder mappings in a public folder are also
            // changed to mailadmin. Remaining task with single folder mappings
            // must be private tasks that can be deleted.
            // Delete private task in private folders.
            assignToAdmin(event, readCon, writeCon);
            // Change createdFrom and modifiedBy attributes of left over tasks.
            changeCFMB(event, readCon, writeCon);
        } catch (TaskException e) {
            throw new DeleteFailedException("Problem while deleting from "
                + "tasks.", e);
        }
    }

    /**
     * Removes the user from task participants.
     * @param event Event.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @throws TaskException if an exception occurs.
     */
    private void removeUserFromParticipants(final DeleteEvent event,
        final Connection readCon, final Connection writeCon)
        throws TaskException {
        final Context ctx = event.getContext();
        final int userId = event.getId();
        final ParticipantStorage partStor = ParticipantStorage.getInstance();
        for (StorageType type : StorageType.values()) {
            final int[] tasks = partStor.findTasksWithParticipant(ctx,
                readCon, userId, type);
            for (int task : tasks) {
                partStor.deleteInternal(ctx, writeCon, task, userId , type,
                    true);
            }
        }
    }

    /**
     * Assigns all left tasks to mailadmin.
     * @param event Event.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @throws TaskException if a problem occurs.
     */
    private void assignToAdmin(final DeleteEvent event,
        final Connection readCon, final Connection writeCon)
        throws TaskException {
        final SessionObject session = getSession(event);
        final Context ctx = event.getContext();
        final int userId = event.getId();
        final FolderStorage foldStor = FolderStorage.getInstance();
        for (StorageType type : TYPES_AD) {
            final int[][] result = foldStor.searchFolderByUser(ctx, readCon,
                userId, type);
            for (int[] folderAndTask : result) {
                final int folderId = folderAndTask[0];
                final int taskId = folderAndTask[1];
                final Set<Folder> folders = foldStor.selectFolder(ctx, readCon,
                    taskId, type);
                if (folders.size() == 0) {
                    throw new TaskException(TaskException.Code.FOLDER_NOT_FOUND,
                        folderId, taskId, userId, ctx.getContextId());
                } else if (folders.size() > 1) {
                    foldStor.deleteFolder(ctx, writeCon, taskId, folderId,
                        type);
                } else if (Tools.isFolderPublic(ctx, folderId)) {
                    Folder folder = new Folder(folderId, ctx.getMailadmin());
                    foldStor.insertFolder(ctx, writeCon, taskId, folder, type);
                    foldStor.deleteFolder(ctx, writeCon, taskId, folderId,
                        type);
                } else if (Tools.isFolderPrivate(ctx, folderId)) {
                    TaskLogic.removeTask(session, folderId, taskId, type);
                } else {
                    throw new TaskException(Code.UNIMPLEMENTED);
                }
            }
        }
    }

    /**
     * @param event Event.
     * @return the session object.
     * @throws TaskException if getting the session object fails.
     */
    private SessionObject getSession(final DeleteEvent event)
        throws TaskException {
        final SessionObject session;
        try {
            session = event.getSession();
        } catch (SQLException e) {
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        } catch (LdapException e) {
            throw new TaskException(e);
        } catch (OXException e) {
            throw new TaskException(e);
        }
        return session;
    }

    /**
     * Remove delete user from remaining tasks in the attributes createdFrom and
     * lastModified.
     * @param event Event.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @throws TaskException if an exception occurs.
     */
    private void changeCFMB(final DeleteEvent event, final Connection readCon,
        final Connection writeCon) throws TaskException {
        final int[] modified = new int[] { Task.CREATED_BY, Task.MODIFIED_BY,
            Task.LAST_MODIFIED };
        final Context ctx = event.getContext();
        final int userId = event.getId();
        final TaskStorage stor = TaskStorage.getInstance();
        for (StorageType type : TYPES_AD) {
            final int[] tasks = TaskSearch.getInstance().findDelegatedTasks(ctx,
                readCon, userId, type);
            for  (int taskId : tasks) {
                final Task update = stor.selectTask(ctx, readCon, taskId, type);
                final Date lastModified = update.getLastModified();
                if (update.getCreatedBy() == userId) {
                    update.setCreatedBy(ctx.getMailadmin());
                }
                if (update.getModifiedBy() == userId) {
                    update.setModifiedBy(ctx.getMailadmin());
                }
                update.setLastModified(new Date());
                stor.updateTask(ctx, writeCon, update, lastModified, modified,
                    type);
            }
        }
    }

    /**
     * Deletes a group from the participants of tasks.
     * @param event Event.
     * @param readCon readable database connection.
     * @param writeCon writable database connection.
     * @throws DeleteFailedException if the delete gives an error.
     */
    private void deleteGroup(final DeleteEvent event, final Connection readCon,
        final Connection writeCon) throws DeleteFailedException {
        final Context ctx = event.getContext();
        final int groupId = event.getId();
        final ParticipantStorage partStor = ParticipantStorage.getInstance();
        try {
            for (StorageType type : StorageType.values()) {
                final int[] tasks = partStor.findTasksWithGroup(ctx, readCon,
                    groupId, type);
                for (int task : tasks) {
                    Set<InternalParticipant> participants = partStor
                        .selectInternal(ctx, readCon, task, type);
                    participants = TaskLogic.extractWithGroup(participants,
                        groupId);
                    removeGroup(participants);
                    partStor.updateInternal(ctx, writeCon, task, participants,
                        type);
                }
            }
        } catch (TaskException e) {
            throw new DeleteFailedException("Problem while deleting from "
                + "tasks.", e);
        }
    }

    /**
     * Removes the group from the participants.
     * @param participants task internal participants.
     */
    private void removeGroup(final Set<InternalParticipant> participants) {
        for (InternalParticipant participant : participants) {
            participant.setGroupId(null);
        }
    }
}
