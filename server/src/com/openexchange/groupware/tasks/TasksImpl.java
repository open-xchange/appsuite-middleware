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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class TasksImpl extends Tasks {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TasksImpl.class);

    /**
     * Fields to update of a participant is removed from a task.
     */
    private static final int[] UPDATE_FIELDS = new int[] { Task.LAST_MODIFIED,
                            Task.MODIFIED_BY };

    /**
     * Default constructor.
     */
    public TasksImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsNotSelfCreatedTasks(final SessionObject session,
        final int folderId) throws OXException {
        try {
            return TaskStorage.getInstance().containsNotSelfCreatedTasks(
                session, folderId);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsNotSelfCreatedTasks(final SessionObject session,
        final Connection con, final int folderId) throws OXException {
        return containsNotSelfCreatedTasks(session, folderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTasksInFolder(final SessionObject session,
        final int folderId) throws OXException {
        final TaskStorage storage = TaskStorage.getInstance();
        final ParticipantStorage partStor = ParticipantStorage.getInstance();
        final FolderStorage foldStor = FolderStorage.getInstance();
        final Context ctx = session.getContext();
        final int userId = session.getUserId();
        final List<Integer> deleteTask = new ArrayList<Integer>();
        final List<UpdateData> removeParticipant = new ArrayList<UpdateData>();
        TaskIterator iter = null;
        try {
            iter = storage.list(ctx, folderId, 0, -1, 0,
                null, new int[] { Task.OBJECT_ID }, false, userId, false);
            while (iter.hasNext()) {
                final Task task = iter.next();
                final UpdateData data = new UpdateData();
                data.taskId = task.getObjectID();
                data.lastRead = task.getLastModified();
                removeParticipant.add(data);
            }
        } catch (TaskException e) {
            throw Tools.convert(e);
        } catch (SearchIteratorException e) {
            throw new OXException(e);
        } finally {
            if (null != iter) {
                try {
                    iter.close();
                } catch (SearchIteratorException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        try {
            for (UpdateData data : removeParticipant) {
                final Set<Folder> folders = foldStor.selectFolder(ctx, data
                    .taskId, StorageType.ACTIVE);
                if (folders.size() == 1) {
                    // Task is only in the folder that is deleted.
                    deleteTask.add(data.taskId);
                    continue;
                }
                final Set<InternalParticipant> participants = ParticipantStorage
                    .extractInternal(partStor.selectParticipants(ctx,
                        data.taskId, StorageType.ACTIVE));
                final Folder folder = FolderStorage.getFolder(folders, folderId);
                final TaskParticipant participant = ParticipantStorage
                    .getParticipant(participants, folder.getUser());
                if (null == participant) {
                    // Delegators folder of the task is deleted.
                    deleteTask.add(data.taskId);
                } else {
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
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        removeParticipant.removeAll(deleteTask);
        try {
            for (int taskId : deleteTask) {
                final Task task = GetTask.load(ctx, folderId, taskId,
                    StorageType.ACTIVE);
                TaskLogic.deleteTask(session, task, task.getLastModified());
            }
            for (UpdateData data : removeParticipant) {
                if (deleteTask.contains(data.taskId)) {
                    continue;
                }
                TaskLogic.updateTask(ctx, data.task, data.lastRead,
                    data.modified, data.add, data.remove, data.addFolder,
                    data.removeFolder);
            }
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    private static class UpdateData {
        private int taskId;
        private Task task;
        private Date lastRead;
        private int[] modified;
        private Set<TaskParticipant> add;
        private Set<TaskParticipant> remove;
        private Set<Folder> addFolder;
        private Set<Folder> removeFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFolderEmpty(final Context ctx, final int folderId)
        throws OXException {
        final TaskStorage storage = TaskStorage.getInstance();
        try {
            return storage.countTasks(ctx, -1, folderId, false, false) == 0;
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFolderEmpty(final Context ctx, final Connection con,
        final int folderId) throws OXException {
        return isFolderEmpty(ctx, folderId);
    }

}
