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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.TaskException.Detail;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderTools;

/**
 * This class contains the logic for updating tasks. It calculates what is to
 * modify.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
class UpdateLogic {

    /**
     * Session.
     */
    private final SessionObject session;

    /**
     * Context.
     */
    private final Context ctx;

    /**
     * The user object.
     */
    private final User user;

    /**
     * Unique identifier of the user.
     */
    private final int userId;

    /**
     * The changed task.
     */
    private final Task changed;

    /**
     * Unique identifier of the task.
     */
    private final int taskId;

    /**
     * Identifier of the folder through that the task is changed.
     */
    private final int folderId;

    /**
     * timestamp when the to update task was read last.
     */
    private Date lastRead;

    /**
     * The task storage.
     */
    private final TaskStorage storage;

    /**
     * Default constructor.
     * @param session Session.
     * @param changed the changed task.
     * @param folderId unique identifier of the folder throught that the task
     * is changed.
     * @param lastRead timestamp when the to update task was read last.
     */
    UpdateLogic(final SessionObject session, final Task changed,
        final int folderId, final Date lastRead) {
        super();
        this.session = session;
        ctx = session.getContext();
        user = session.getUserObject();
        userId = user.getId();
        this.changed = changed;
        taskId = changed.getObjectID();
        this.folderId = folderId;
        this.lastRead = lastRead;
        storage = TaskStorage.getInstance();
    }

    /**
     * The original task.
     */
    private Task origTask;

    /**
     * @return the original task.
     * @throws TaskException if loading of the original tasks fails.
     */
    private Task getOrigTask() throws TaskException {
        if (null == origTask) {
            origTask = storage.selectTask(ctx, taskId, StorageType.ACTIVE);
            origTask.setParentFolderID(folderId);
        }
        return origTask;
    }

    /**
     * Changed participants from the changed task.
     */
    private Set<TaskParticipant> changedParticipants;

    /**
     * @return the changed participants from the changed task.
     * @throws TaskException if resolving of groups to users fails.
     */
    private Set<TaskParticipant> getChangedParticipants() throws TaskException {
        if (null == changedParticipants) {
            changedParticipants = TaskLogic.createParticipants(ctx,
                changed.getParticipants());
        }
        return changedParticipants;
    }

    /**
     * Original participants from the task.
     */
    private Set<TaskParticipant> origParticipants;

    /**
     * @return the original participants from the task.
     * @throws TaskException if participants can't be selected.
     */
    private Set<TaskParticipant> getOrigParticipants() throws TaskException {
        if (null == origParticipants) {
            origParticipants = storage.selectParticipants(ctx, taskId,
                StorageType.ACTIVE);
        }
        return origParticipants;
    }

    /**
     * Original folders from task.
     */
    private Set<Folder> origFolder;

    /**
     * @return the original folder of the task.
     * @throws TaskException if folders can't be selected.
     */
    private Set<Folder> getOrigFolder() throws TaskException {
        if (null == origFolder) {
            origFolder = storage.selectFolders(ctx, taskId);
        }
        return origFolder;
    }

    /**
     * Prepares the data structures for an update.
     * @throws TaskException if an error occurs
     */
    void prepare() throws TaskException {
        if (getOrigTask().getLastModified().after(lastRead)) {
            throw new TaskException(Code.MODIFIED,
                getOrigTask().getLastModified().getTime(), lastRead.getTime());
        }
        final boolean move = changed.containsParentFolderID()
            && changed.getParentFolderID() != folderId;
        final int destFolderId = move ? changed.getParentFolderID() : folderId;
        final FolderObject folder = Tools.getFolder(ctx, folderId);
        if (move) {
            // Move
            final FolderObject destFolder = Tools.getFolder(ctx, destFolderId);
            // task is deleted in source folder and created in destination
            // folder.
            Access.checkDelete(ctx, userId, user.getGroups(), session
                .getUserConfiguration(), folder, getOrigTask());
            // move out of or into a shared folder is not allowed.
            if (Tools.isFolderShared(folder, userId)
                || Tools.isFolderShared(destFolder, userId)) {
                throw new TaskException(Code.NO_SHARED_MOVE,
                    folder.getFolderName(), folderId);
            }
            // moving private tasks to a public folder isn't allowed.
            if (getOrigTask().getPrivateFlag()
                && Tools.isFolderPublic(destFolder)) {
                throw new TaskException(Code.NO_PRIVATE_MOVE_TO_PUBLIC,
                    destFolder.getFolderName(), destFolderId);
            }
        } else {
            TaskLogic.checkWriteInFolder(ctx, userId, user.getGroups(),
                session.getUserConfiguration(),
                folderId, getOrigTask().getCreatedBy());
            // Check if task appears in folder.
            storage.selectFolderById(ctx, taskId, folderId);
            if (Tools.isFolderShared(folder, userId)
                && getOrigTask().getPrivateFlag()) {
                throw new TaskException(Code.NO_PRIVATE_PERMISSION,
                    folder.getFolderName(), folderId);
            }
        }
        // Do logic checks.
        TaskLogic.checkUpdateTask(changed, getOrigTask(), userId,
            session.getUserConfiguration(), getChangedParticipants());
        if (changed.containsPrivateFlag() && changed.getPrivateFlag()) {
            if ((changed.containsParticipants()
                && getChangedParticipants().size() > 0)
                || getOrigParticipants().size() > 0) {
                throw new TaskException(Code.NO_PRIVATE_DELEGATE);
            }
            if (getOrigTask().getCreatedBy() != userId) {
                throw new TaskException(Code.ONLY_CREATOR_PRIVATE);
            }
        }
        if (changed.containsParticipants()) {
            // TODO Use FolderObject
            if (!Tools.isFolderPublic(folder)) {
                Tools.fillStandardFolders(getOrigParticipants(),
                    getOrigFolder(), true);
            }
            // Find added participants
            added.addAll(getChangedParticipants());
            added.removeAll(getOrigParticipants());
            // Replace added participants with the values from the removed
            // one to get folder and confirmation information.
            final Set<TaskParticipant> origRemovedParticipants = storage
                .selectParticipants(ctx, taskId, StorageType.REMOVED);
            origRemovedParticipants.retainAll(added);
            added.addAll(origRemovedParticipants);
            // Find removed participants
            removed.addAll(getOrigParticipants());
            removed.removeAll(getChangedParticipants());
        }
        int sourceType;
        int destType;
        try {
            sourceType = OXFolderTools.getFolderType(folderId, userId, ctx);
            destType = OXFolderTools.getFolderType(destFolderId, userId, ctx);
        } catch (OXException e) {
            throw new TaskException(e);
        }
        if (move) {
            removedFolder.add(new Folder(folderId, userId));
            addedFolder.add(new Folder(destFolderId, userId));
        }
        if (FolderObject.PUBLIC == sourceType) {
            if (FolderObject.PUBLIC != destType) {
                Tools.fillStandardFolders(ctx, getOrigParticipants());
                addedFolder.addAll(TaskLogic.createFolderMapping(
                    Tools.extractInternal(getOrigParticipants())));
                Tools.fillStandardFolders(ctx, added);
                addedFolder.addAll(TaskLogic.createFolderMapping(Tools
                    .extractInternal(added)));
                removedFolder.addAll(TaskLogic.createFolderMapping(Tools
                    .extractInternal(removed)));
            }
        } else {
            if (FolderObject.PUBLIC == destType) {
                removedFolder.addAll(getOrigFolder());
            } else {
                Tools.fillStandardFolders(ctx, added);
                addedFolder.addAll(TaskLogic.createFolderMapping(Tools
                    .extractInternal(added)));
                removedFolder.addAll(TaskLogic.createFolderMapping(Tools
                    .extractInternal(removed)));
            }
        }
        modifiedFields = TaskLogic.findModifiedFields(getOrigTask(), changed);
    }

    /**
     * Added participants.
     */
    private final Set<TaskParticipant> added = new HashSet<TaskParticipant>();

    /**
     * Removed participants.
     */
    private final Set<TaskParticipant> removed = new HashSet<TaskParticipant>();

    /**
     * Folders in that the task must appear additionally.
     */
    private final Set<Folder> addedFolder = new HashSet<Folder>();

    /**
     * Folders in that the task mustn't appear anymore.
     */
    private final Set<Folder> removedFolder = new HashSet<Folder>();

    /**
     * Unique identifier of the changed attributes of the task.
     */
    private int[] modifiedFields;

    /**
     * @return the added
     */
    Set<TaskParticipant> getAdded() {
        return added;
    }

    /**
     * @return the addedFolder
     */
    Set<Folder> getAddedFolder() {
        return addedFolder;
    }

    /**
     * @return the modifiedFields
     */
    int[] getModifiedFields() {
        return modifiedFields;
    }

    /**
     * @return the removed
     */
    Set<TaskParticipant> getRemoved() {
        return removed;
    }

    /**
     * @return the removedFolder
     */
    Set<Folder> getRemovedFolder() {
        return removedFolder;
    }
}
