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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class TasksImpl extends Tasks {

    private static final int[] UPDATE_FIELDS = new int[] { Task.LAST_MODIFIED, Task.MODIFIED_BY };

    public TasksImpl() {
        super();
    }

    @Override
    public boolean containsNotSelfCreatedTasks(final Session session, final Connection con, final int folderId) throws OXException {
        final Context ctx = Tools.getContext(session.getContextId());
        return TaskStorage.getInstance().containsNotSelfCreatedTasks(ctx, con, session.getUserId(), folderId);
    }

    @Override
    public void deleteTasksInFolder(final Session session, final Connection con, final int folderId) throws OXException {
        final ParticipantStorage partStor = ParticipantStorage.getInstance();
        final FolderStorage foldStor = FolderStorage.getInstance();
        final Context ctx = Tools.getContext(session.getContextId());
        final int userId = session.getUserId();
        final List<Integer> deleteTask = new ArrayList<Integer>();
        final List<UpdateData> removeParticipant = new ArrayList<UpdateData>();
        {
            // Pickup all potential tasks in the folder. Sort later which tasks need to be deleted.
            final int[] taskIds = foldStor.getTasksInFolder(ctx, con, folderId, StorageType.ACTIVE);
            for (final int taskId : taskIds) {
                final UpdateData data = new UpdateData();
                data.taskId = taskId;
                removeParticipant.add(data);
            }
        }
        {
            for (final UpdateData data : removeParticipant) {
                final Set<Folder> folders = foldStor.selectFolder(ctx, con, data.taskId, StorageType.ACTIVE);
                if (folders.size() == 1) {
                    // Task is only in the folder that is deleted. Delete the task.
                    deleteTask.add(I(data.taskId));
                    continue;
                }
                // Task appears in multiple folders. Now lets have a look on the participants of the task.
                final Set<InternalParticipant> participants = ParticipantStorage.extractInternal(partStor.selectParticipants(
                    ctx,
                    con,
                    data.taskId,
                    StorageType.ACTIVE));
                final Folder folder = FolderStorage.getFolder(folders, folderId);
                final TaskParticipant participant = ParticipantStorage.getParticipant(participants, folder.getUser());
                if (null == participant) {
                    // No participant for the task folder is found. Means: delegators folder of the task is deleted.
                    deleteTask.add(I(data.taskId));
                } else {
                    // Update task and remove only participant.
                    data.task = new Task();
                    data.task.setObjectID(data.taskId);
                    data.task.setLastModified(new Date());
                    data.task.setModifiedBy(userId);
                    data.modified = UPDATE_FIELDS;
                    data.add = TaskParticipant.EMPTY;
                    data.remove = new HashSet<TaskParticipant>(1, 1);
                    data.remove.add(participant);
                    data.addFolder = Folder.EMPTY;
                    data.removeFolder = new HashSet<Folder>(1, 1);
                    data.removeFolder.add(folder);
                }
            }
        }
        {
            // Remove all tasks that have to be deleted.
            for (final int taskId : deleteTask) {
                TaskLogic.removeTask(session, ctx, con, folderId, taskId, StorageType.ACTIVE);
            }
            // Remove only participant and participants folder.
            for (final UpdateData data : removeParticipant) {
                // Skip tasks that already have been deleted.
                if (deleteTask.contains(I(data.taskId))) {
                    continue;
                }
                com.openexchange.groupware.tasks.UpdateData.updateTask(
                    ctx,
                    con,
                    data.task,
                    new Date(),
                    data.modified,
                    data.add,
                    data.remove,
                    data.addFolder,
                    data.removeFolder);
            }
        }
    }

    private static class UpdateData {
        UpdateData() {
            super();
        }
        int taskId;
        Task task;
        int[] modified;
        Set<TaskParticipant> add;
        Set<TaskParticipant> remove;
        Set<Folder> addFolder;
        Set<Folder> removeFolder;
    }

    @Override
    public boolean isFolderEmpty(final Context ctx, final int folderId) throws OXException {
        return TaskStorage.getInstance().countTasks(ctx, -1, folderId, false, false) == 0;
    }

    @Override
    public boolean isFolderEmpty(final Context ctx, final Connection con, final int folderId) throws OXException {
        return isFolderEmpty(ctx, folderId);
    }
}
