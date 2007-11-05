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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.event.EventClient;
import com.openexchange.event.InvalidStateException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.mapping.Status;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.Arrays;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderTools;

/**
 * This class implements the methods needed by the tasks interface of the API
 * version 2.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TasksSQLInterfaceImpl implements TasksSQLInterface {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(
        TasksSQLInterfaceImpl.class);

    /**
     * Reference to the context.
     */
    private final transient SessionObject session;

    /**
     * Default constructor.
     * @param session Session.
     */
    public TasksSQLInterfaceImpl(final SessionObject session) {
        super();
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    public int getNumberOfTasks(final int folderId) throws OXException {
        final Context ctx = session.getContext();
        final User user = session.getUserObject();
        final int userId = user.getId();
        FolderObject folder;
        try {
            folder = Tools.getFolder(session.getContext(), folderId);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        boolean onlyOwn;
        try {
            onlyOwn = Permission.canReadInFolder(ctx, user, session
                .getUserConfiguration(), folder);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        final boolean noPrivate = Tools.isFolderShared(folder, user);
        final int count;
        try {
            count = TaskStorage.getInstance().countTasks(ctx, userId, folderId,
                onlyOwn, noPrivate);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    public SearchIterator getTaskList(final int folderId, final int from,
        final int until, final int orderBy, final String orderDir,
        final int[] columns) throws OXException {
        boolean onlyOwn;
        final Context ctx = session.getContext();
        final User user = session.getUserObject();
        final int userId = user.getId();
        FolderObject folder;
        try {
            folder = Tools.getFolder(ctx, folderId);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        try {
            onlyOwn = Permission.canReadInFolder(ctx, user, session
                .getUserConfiguration(), folder);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        final boolean noPrivate = Tools.isFolderShared(folder, user);
        try {
            return TaskStorage.getInstance().list(ctx, folderId, from, until,
                orderBy, orderDir, columns, onlyOwn, userId, noPrivate);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Task getTaskById(final int taskId, final int folderId)
        throws OXException {
        final Context ctx = session.getContext();
        final User user = session.getUserObject();
        final UserConfiguration userConfig = session.getUserConfiguration();
        final GetTask get = new GetTask(ctx, user, userConfig, folderId, taskId,
            StorageType.ACTIVE);
        try {
            return get.loadAndCheck();
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public SearchIterator getModifiedTasksInFolder(final int folderId,
        final int[] columns, final Date since) throws OXException {
        final Context ctx = session.getContext();
        final User user = session.getUserObject();
        final int userId = session.getUserObject().getId();
        FolderObject folder;
        try {
            folder = Tools.getFolder(ctx, folderId);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        boolean onlyOwn;
        try {
            onlyOwn = Permission.canReadInFolder(ctx, user, session
                .getUserConfiguration(), folder);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        final boolean noPrivate = Tools.isFolderShared(folder, user);
        try {
            return TaskSearch.getInstance().listModifiedTasks(ctx, folderId,
                StorageType.ACTIVE, columns, since, onlyOwn, userId, noPrivate);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public SearchIterator getDeletedTasksInFolder(final int folderId,
        final int[] columns, final Date since) throws OXException {
        final Context ctx = session.getContext();
        final User user = session.getUserObject();
        final int userId = user.getId();
        FolderObject folder;
        try {
            folder = Tools.getFolder(ctx, folderId);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        boolean onlyOwn;
        try {
            onlyOwn = Permission.canReadInFolder(ctx, user, session
                .getUserConfiguration(), folder);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        final boolean noPrivate = Tools.isFolderShared(folder, user);
        try {
            return TaskSearch.getInstance().listModifiedTasks(ctx, folderId,
                StorageType.DELETED, columns, since, onlyOwn, userId,
                noPrivate);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void insertTaskObject(final Task task) throws OXException {
        final Context ctx = session.getContext();
        final Set<TaskParticipant> parts;
        try {
            final User user = session.getUserObject();
            final int userId = user.getId();
            parts = TaskLogic.createParticipants(ctx, task.getParticipants());
            TaskLogic.checkNewTask(task, userId, session.getUserConfiguration(),
                parts);
            // Check access rights
            final int folderId = task.getParentFolderID();
            final FolderObject folder = Tools.getFolder(ctx, folderId);
            final UserConfiguration userConfig = session.getUserConfiguration();
            Permission.checkCreate(ctx, user, userConfig, folder);
            if (task.getPrivateFlag() && (Tools.isFolderPublic(folder)
                || Tools.isFolderShared(folder, user))) {
                throw new TaskException(Code.PRIVATE_FLAG, folderId);
            }
            // Create folder mappings
            Set<Folder> folders;
            if (Tools.isFolderPublic(folder)) {
                folders = TaskLogic.createFolderMapping(folderId, task
                    .getCreatedBy(), InternalParticipant.EMPTY);
            } else {
                Tools.fillStandardFolders(ctx, ParticipantStorage
                    .extractInternal(parts));
                int creator = userId;
                if (Tools.isFolderShared(folder, user)) {
                    creator = folder.getCreator();
                }
                folders = TaskLogic.createFolderMapping(folderId, creator,
                    ParticipantStorage.extractInternal(parts));
            }
            // Insert task
            TaskLogic.insertTask(ctx, task, parts, folders);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        if (task.containsAlarm()) {
            Reminder.createReminder(ctx, task);
        }
        // Prepare for event
        task.setUsers(TaskLogic.createUserParticipants(parts));
        try {
            new EventClient(session).create(task);
        } catch (InvalidStateException e) {
            throw Tools.convert(new TaskException(Code.EVENT, e));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateTaskObject(final Task task, final int folderId,
        final Date lastRead) throws OXException {
        final Context ctx = session.getContext();
        final User user = session.getUserObject();
        final UserConfiguration userConfig = session.getUserConfiguration();
        final FolderObject folder;
        try {
            folder = Tools.getFolder(ctx, folderId);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        final UpdateData update = new UpdateData(ctx, user, userConfig,
            folder, task, lastRead);
        final Task updated;
        final Set<TaskParticipant> removedParts;
        final Set<TaskParticipant> updatedParts;
        final Set<Folder> updatedFolder;
        final boolean move;
        try {
            update.prepare();
            removedParts = update.getRemoved();
            TaskLogic.updateTask(ctx, task, lastRead, update.getModifiedFields(),
                update.getAdded(), removedParts, update.getAddedFolder(),
                update.getRemovedFolder());
            updated = update.getUpdated();
            updatedParts = update.getUpdatedParticipants();
            updatedFolder = update.getUpdatedFolder();
            move = update.isMove();
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        try {
            new EventClient(session).modify(updated);
        } catch (InvalidStateException e) {
            throw Tools.convert(new TaskException(Code.EVENT, e));
        }
        if (updated.containsAlarm()) {
            Reminder.updateAlarm(ctx, updated, user);
        }
        if (move) {
            Reminder.fixAlarm(ctx, updated, removedParts, updatedFolder);
        }
        try {
            if (Task.NO_RECURRENCE != updated.getRecurrenceType() && Task.DONE
                == updated.getStatus() && Arrays.contains(update
                    .getModifiedFields(), Status.SINGLETON.getId())) {
                insertNextRecurrence(updated, updatedParts, updatedFolder);
            }
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * Inserts a new task according to the recurrence.
     * @param task recurring task.
     * @param parts participants of the updated task.
     * @param folders folders of the updated task.
     * @throws TaskException if creating the new task fails.
     * @throws OXException if sending an event about new task fails.
     */
    private void insertNextRecurrence(final Task task,
        final Set<TaskParticipant> parts, final Set<Folder> folders)
        throws TaskException,
        OXException {
        final Context ctx = session.getContext();
        final boolean next = TaskLogic.makeRecurrence(task);
        if (next) {
            TaskLogic.checkNewTask(task, session.getUserObject().getId(),
                session.getUserConfiguration(), parts);
            TaskLogic.insertTask(ctx, task, parts, folders);
            try {
                new EventClient(session).create(task);
            } catch (InvalidStateException e) {
                throw Tools.convert(new TaskException(Code.EVENT, e));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteTaskObject(final int taskId, final int folderId,
        final Date lastModified) throws OXException {
        final FolderStorage foldStor = FolderStorage.getInstance();
        final FolderObject folder;
        final Context ctx = session.getContext();
        final User user = session.getUserObject();
        final Task task;
        try {
            // Check if folder exists
            folder = Tools.getFolder(ctx, folderId);
            // Check if folder is correct.
            foldStor.selectFolderById(ctx, taskId, folderId, StorageType
                .ACTIVE);
            // Load task with participants.
            task = GetTask.load(ctx, folderId, taskId, StorageType.ACTIVE);
            // TODO Switch to only delete the participant from task
            // Check delete permission
            Permission.checkDelete(ctx, user, session.getUserConfiguration(),
                folder, task);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        try {
            TaskLogic.deleteTask(session, task, lastModified);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     * @throws DBPoolingException 
     */
    public void setUserConfirmation(final int taskId, final int userId,
        final int confirm, final String message) throws OXException {
        try {
            TaskLogic.setConfirmation(session.getContext(), taskId, userId,
                confirm, message);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public SearchIterator getObjectsById(final int[][] ids,
        final int[] columns) throws OXException {
        // TODO Improve performance
        final List<Task> tasks = new ArrayList<Task>();
        for (int[] objectAndFolderId : ids) {
            tasks.add(getTaskById(objectAndFolderId[0], objectAndFolderId[1]));
        }
        return new ArrayIterator(tasks.toArray(new Task[tasks.size()]));
    }

    /**
     * {@inheritDoc}
     */
    public SearchIterator getTasksByExtendedSearch(
        final TaskSearchObject search, final int orderBy, final String orderDir,
        final int[] columns) throws OXException {
        final User user = session.getUserObject();
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> own = new ArrayList<Integer>();
        List<Integer> shared = new ArrayList<Integer>();
        final Context ctx = session.getContext();
        final UserConfiguration config = session.getUserConfiguration();
        try {
            final int userId = user.getId();
            final int[] groups = user.getGroups();
            if (TaskSearchObject.NO_FOLDER == search.getFolder()) {
                final SearchIterator iter = OXFolderTools
                    .getAllVisibleFoldersIteratorOfModule(userId,
                        user.getGroups(), config.getAccessibleModules(),
                        FolderObject.TASK, ctx);
                while (iter.hasNext()) {
                    final FolderObject folder = (FolderObject) iter.next();
                    if (folder.isShared(userId)) {
                        shared.add(Integer.valueOf(folder.getObjectID()));
                    } else if (Permission.canOnlySeeFolder(ctx, user, config,
                        folder)) {
                        continue;
                    } else if (Permission.canReadInFolder(ctx, user, config,
                        folder)) {
                        own.add(Integer.valueOf(folder.getObjectID()));
                    } else {
                        all.add(Integer.valueOf(folder.getObjectID()));
                    }
                }
            } else {
                final FolderObject folder = Tools.getFolder(ctx,
                    search.getFolder());
                if (folder.isShared(userId)) {
                    shared.add(Integer.valueOf(folder.getObjectID()));
                } else if (Permission.canReadInFolder(ctx, user, config,
                    folder)) {
                    own.add(Integer.valueOf(folder.getObjectID()));
                } else {
                    all.add(Integer.valueOf(folder.getObjectID()));
                }
            }
            all = Collections.unmodifiableList(all);
            own = Collections.unmodifiableList(own);
            shared = Collections.unmodifiableList(shared);
        } catch (SearchIteratorException e) {
            throw new OXException(e);
        } catch (TaskException e) {
            throw Tools.convert(e);
        }
        if (LOG.isTraceEnabled()) {
	        LOG.trace("Search tasks, all: " + all + ", own: " + own + ", shared: "
	            + shared);
        }
        SearchIterator retval;
        if (all.size() + own.size() + shared.size() == 0) {
            retval = SearchIterator.EMPTY_ITERATOR;
        } else {
            try {
                retval = TaskStorage.getInstance().search(session, search,
                    orderBy, orderDir, columns, all, own, shared);
            } catch (TaskException e) {
                throw Tools.convert(e);
            }
        }
        return retval;
    }
}
