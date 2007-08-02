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

import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskException.Code;

/**
 * This class contains the logic for updating tasks. It calculates what is to
 * modify.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
class UpdateData {

    /**
     * Context.
     */
    private final Context ctx;

    /**
     * The user object.
     */
    private final User user;

    /**
     * User configuration.
     */
    private final UserConfiguration userConfig;
 
    /**
     * Folder for permission checks.
     */
    private final FolderObject folder;

    /**
     * Destination folder for move.
     */
    private FolderObject destFolder;

    /**
     * timestamp when the to update task was read last.
     */
    private Date lastRead;

    /**
     * The changed task.
     */
    private final Task changed;

    /**
     * The original task.
     */
    private Task origTask;

    /**
     * The updated task.
     */
    private Task updated;

    /**
     * Unique identifier of the changed attributes of the task.
     */
    private int[] modifiedFields;

    /**
     * Changed participants from the changed task.
     */
    private Set<TaskParticipant> changedParticipants;

    /**
     * Original participants from the task.
     */
    private Set<TaskParticipant> origParticipants;

    /**
     * Participants when the update is done.
     */
    private Set<TaskParticipant> updatedParticipants;

    /**
     * Original folders from task.
     */
    private Set<Folder> origFolders;

    /**
     * Folder mappings when the update is done.
     */
    private Set<Folder> updatedFolders;

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
     * The task storage.
     */
    private final TaskStorage storage = TaskStorage.getInstance();

    /**
     * The participant storage.
     */
    private final ParticipantStorage partStor = ParticipantStorage.getInstance();

    /**
     * The folder storage.
     */
    private final FolderStorage foldStor = FolderStorage.getInstance();

    /**
     * Default constructor.
     * @param ctx Context.
     * @param user User.
     * @param userConfig User configuration.
     * @param folder folder throught that the task is changed.
     * @param changed the changed task.
     * @param lastRead timestamp when the to update task was read last.
     */
    UpdateData(final Context ctx, final User user,
        final UserConfiguration userConfig, final FolderObject folder,
        final Task changed, final Date lastRead) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.userConfig = userConfig;
        this.folder = folder;
        this.changed = changed;
        this.lastRead = lastRead;
    }

    /**
     * @return the original task.
     * @throws TaskException if loading of the original tasks fails.
     */
    private Task getOrigTask() throws TaskException {
        if (null == origTask) {
            origTask = storage.selectTask(ctx, getTaskId(), StorageType.ACTIVE);
            origTask.setParentFolderID(getFolderId());
        }
        return origTask;
    }

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
     * @return the original participants from the task.
     * @throws TaskException if participants can't be selected.
     */
    private Set<TaskParticipant> getOrigParticipants() throws TaskException {
        if (null == origParticipants) {
            origParticipants = partStor.selectParticipants(ctx, getTaskId(),
                StorageType.ACTIVE);
            if (Tools.isFolderPrivate(folder)) {
                Tools.fillStandardFolders(getOrigParticipants(),
                    getOrigFolder(), true);
            }
        }
        return origParticipants;
    }

    Set<TaskParticipant> getUpdatedParticipants() throws TaskException {
        if (null == updatedParticipants) {
            if (changed.containsParticipants()) {
                updatedParticipants = getChangedParticipants();
            } else {
                updatedParticipants = getOrigParticipants();
            }
        }
        return updatedParticipants;
    }

    /**
     * @return the original folder of the task.
     * @throws TaskException if folders can't be selected.
     */
    private Set<Folder> getOrigFolder() throws TaskException {
        if (null == origFolders) {
            origFolders = foldStor.selectFolder(ctx, getTaskId(), StorageType
                .ACTIVE);
        }
        return origFolders;
    }

    Set<Folder> getUpdatedFolder() throws TaskException {
        if (null == updatedFolders) {
            updatedFolders = getOrigFolder();
            updatedFolders.addAll(getAddedFolder());
            updatedFolders.removeAll(getRemovedFolder());
        }
        return updatedFolders;
    }

    /**
     * Prepares the data structures for an update.
     * @throws TaskException if an error occurs
     */
    void prepare() throws TaskException {
        if (getOrigTask().getLastModified().after(lastRead)) {
            throw new TaskException(Code.MODIFIED);
        }
        checkPermission();
        // Do logic checks.
        TaskLogic.checkUpdateTask(changed, getOrigTask(), user, userConfig,
            getChangedParticipants(), getOrigParticipants());
        // Now we do the damn stuff.
        prepareFields();
        prepareParticipants();
        prepareFolder();
    }

    private boolean preparedFields;

    private void prepareFields() throws TaskException {
        if (preparedFields) {
            return;
        }
        modifiedFields = TaskLogic.findModifiedFields(getOrigTask(), changed);
        preparedFields = true;
    }

    private boolean preparedFolder;

    private void prepareFolder() throws TaskException {
        if (preparedFolder) {
            return;
        }
        prepareParticipants();
        // Shared folders are not allowed.
        final int sourceType = folder.getType();
        final int destType = getDestFolder().getType();
        if (isMove()) {
            removedFolder.add(new Folder(getFolderId(), getUserId()));
            addedFolder.add(new Folder(getDestFolderId(), getUserId()));
        }
        if (FolderObject.PUBLIC == sourceType) {
            if (FolderObject.PRIVATE == destType) {
                // Move public -> private
                final Set<InternalParticipant> internal = ParticipantStorage
                    .extractInternal(getUpdatedParticipants());
                Tools.fillStandardFolders(ctx, internal);
                addedFolder.addAll(TaskLogic.createFolderMapping(internal));
            }
        } else {
            if (FolderObject.PUBLIC == destType) {
                // Move private -> public
                removedFolder.addAll(getOrigFolder());
            } else {
                // Update folder according to added/removed participants
                final Set<InternalParticipant> addedInternal =
                    ParticipantStorage.extractInternal(added);
                Tools.fillStandardFolders(ctx, addedInternal);
                addedFolder.addAll(TaskLogic.createFolderMapping(
                    addedInternal));
                removedFolder.addAll(TaskLogic.createFolderMapping(
                    ParticipantStorage.extractInternal(removed)));
            }
        }
        preparedFolder = true;
    }

    private void checkPermission() throws TaskException {
        if (isMove()) {
            // task is deleted in source folder and created in destination
            // folder.
            Permission.checkDelete(ctx, user, userConfig, folder,
                getOrigTask());
            // move out of a shared folder is not allowed.
            if (Tools.isFolderShared(folder, user)) {
                throw new TaskException(Code.NO_SHARED_MOVE,
                    folder.getFolderName(), getFolderId());
            }
            // move into a shared folder is not allowed.
            if (Tools.isFolderShared(getDestFolder(), user)) {
                throw new TaskException(Code.NO_SHARED_MOVE,
                    getDestFolder().getFolderName(), getDestFolderId());
            }
            // moving private tasks to a public folder isn't allowed.
            if (getOrigTask().getPrivateFlag()
                && Tools.isFolderPublic(getDestFolder())) {
                throw new TaskException(Code.NO_PRIVATE_MOVE_TO_PUBLIC,
                    getDestFolder().getFolderName(), getDestFolderId());
            }
        } else {
            Permission.checkWriteInFolder(ctx, user, userConfig, folder,
                getOrigTask());
            // Check if task appears in folder.
            if (null == FolderStorage.getFolder(getOrigFolder(),
                getFolderId())) {
                throw new TaskException(Code.FOLDER_NOT_FOUND, getFolderId(),
                    getTaskId(), getUserId(), ctx.getContextId());
            }
            if (Tools.isFolderShared(folder, user)
                && getOrigTask().getPrivateFlag()) {
                throw new TaskException(Code.NO_PRIVATE_PERMISSION,
                    folder.getFolderName(), getFolderId());
            }
        }
    }

    private boolean preparedParts;

    private void prepareParticipants() throws TaskException {
        if (preparedParts) {
            return;
        }
        if (changed.containsParticipants()) {
            // Find added participants
            added.addAll(getChangedParticipants());
            added.removeAll(getOrigParticipants());
            // Replace added participants with the values from the removed
            // one to get folder and confirmation information.
            // Only internal participants can be selected here because of
            // type REMOVED.
            final Set<TaskParticipant> origRemovedParts = partStor
                .selectParticipants(ctx, getTaskId(), StorageType.REMOVED);
            origRemovedParts.retainAll(added);
            added.addAll(origRemovedParts);
            // Find removed participants
            removed.addAll(getOrigParticipants());
            removed.removeAll(getChangedParticipants());
        }
        preparedParts = true;
    }

    private void generateUpdated() throws TaskException {
        updated = new Task();
        updated.setObjectID(getTaskId());
        for (Mapper mapper : Mapping.MAPPERS) {
            if (mapper.isSet(changed)) {
                if (null != mapper.get(changed)) {
                    mapper.set(updated, mapper.get(changed));
                }
            } else if (mapper.isSet(getOrigTask())) {
                mapper.set(updated, mapper.get(getOrigTask()));
            }
        }
        if (changed.containsNotification()) {
            updated.setNotification(changed.getNotification());
        }
        if (changed.containsAlarm()) {
            updated.setAlarm(changed.getAlarm());
        }
        updated.setParentFolderID(getDestFolderId());
        final Set<TaskParticipant> parts = getUpdatedParticipants();
        if (Tools.isFolderPrivate(getDestFolder())) {
            Tools.fillStandardFolders(parts, getUpdatedFolder(), true);
        }
        updated.setParticipants(TaskLogic.createParticipants(parts));
        updated.setUsers(TaskLogic.createUserParticipants(parts));
    }

    /* ---------- Getter ---------- */
    
    /**
     * @return the added
     */
    Set<TaskParticipant> getAdded() throws TaskException {
        prepareParticipants();
        return added;
    }

    /**
     * @return the addedFolder
     * @throws TaskException 
     */
    Set<Folder> getAddedFolder() throws TaskException {
        prepareFolder();
        return addedFolder;
    }

    /**
     * @return the modifiedFields
     * @throws TaskException 
     */
    int[] getModifiedFields() throws TaskException {
        prepareFields();
        return modifiedFields;
    }

    /**
     * @return the removed
     */
    Set<TaskParticipant> getRemoved() throws TaskException {
        prepareParticipants();
        return removed;
    }

    /**
     * @return the removedFolder
     * @throws TaskException 
     */
    Set<Folder> getRemovedFolder() throws TaskException {
        prepareFolder();
        return removedFolder;
    }

    Task getUpdated() throws TaskException {
        if (null == updated) {
            generateUpdated();
        }
        return updated;
    }
    
    /* ---------- Convenience methods ---------- */

    private int getFolderId() {
        return folder.getObjectID();
    }

    private int getUserId() {
        return user.getId();
    }

    private int getTaskId() {
        return changed.getObjectID();
    }

    boolean isMove() {
        return changed.containsParentFolderID()
            && changed.getParentFolderID() != getFolderId();
    }

    private FolderObject getDestFolder() throws TaskException {
        if (null == destFolder) {
            destFolder = isMove() ? Tools.getFolder(ctx, changed
                .getParentFolderID()) : folder;
        }
        return destFolder;
    }

    private int getDestFolderId() throws TaskException {
        return getDestFolder().getObjectID();
    }
}
