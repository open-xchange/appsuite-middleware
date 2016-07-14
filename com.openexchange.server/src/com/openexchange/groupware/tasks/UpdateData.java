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

import static com.openexchange.groupware.tasks.StorageType.ACTIVE;
import static com.openexchange.groupware.tasks.StorageType.DELETED;
import static com.openexchange.groupware.tasks.StorageType.REMOVED;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.mapping.Status;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.DBPool;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * This class contains the logic for updating tasks. It calculates what is to modify.
 *
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
     * User permission bits.
     */
    private final UserPermissionBits permissionBits;

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
    private final Date lastRead;

    /**
     * Which type of task to update.
     */
    private final StorageType type;

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
     * Participants that are moved to a group.
     */
    private final Set<InternalParticipant> changedGroup = new HashSet<InternalParticipant>();

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
    private static final TaskStorage storage = TaskStorage.getInstance();

    /**
     * The participant storage.
     */
    private static final ParticipantStorage partStor = ParticipantStorage.getInstance();

    /**
     * The folder storage.
     */
    private static final FolderStorage foldStor = FolderStorage.getInstance();

    /**
     * Easier constructor for live data.
     *
     * @param ctx Context.
     * @param user User.
     * @param permissionBits User permission bits.
     * @param folder folder throught that the task is changed.
     * @param changed the changed task.
     * @param lastRead timestamp when the to update task was read last.
     */
    UpdateData(final Context ctx, final User user, final UserPermissionBits permissionBits, final FolderObject folder, final Task changed, final Date lastRead) {
        this(ctx, user, permissionBits, folder, changed, lastRead, ACTIVE);
    }

    /**
     * Default constructor.
     *
     * @param ctx Context.
     * @param user User.
     * @param permissionBits User permission bits.
     * @param folder folder throught that the task is changed.
     * @param changed the changed task.
     * @param lastRead timestamp when the to update task was read last.
     * @param type ACTIVE or DELETED.
     */
    UpdateData(final Context ctx, final User user, final UserPermissionBits permissionBits, final FolderObject folder, final Task changed, final Date lastRead, final StorageType type) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.permissionBits = permissionBits;
        this.folder = folder;
        this.changed = changed;
        this.lastRead = lastRead;
        this.type = type;
    }

    /**
     * Proxy loading method for the task.
     *
     * @return the original task.
     * @throws OXException if loading of the original tasks fails.
     */
    private Task getOrigTask() throws OXException {
        if (null == origTask) {
            origTask = storage.selectTask(ctx, getTaskId(), type);
            origTask.setParentFolderID(getFolderId());
            origTask.setUsers(TaskLogic.createUserParticipants(getOrigParticipants()));
            origTask.setParticipants(TaskLogic.createParticipants(getOrigParticipants()));
        }
        return origTask;
    }

    /**
     * @return the changed participants from the changed task.
     * @throws OXException if resolving of groups to users fails.
     */
    private Set<TaskParticipant> getChangedParticipants() throws OXException {
        if (null == changedParticipants) {
            changedParticipants = TaskLogic.createParticipants(ctx, changed.getParticipants());
        }
        return changedParticipants;
    }

    /**
     * @return the original participants from the task.
     * @throws OXException if participants can't be selected.
     */
    private Set<TaskParticipant> getOrigParticipants() throws OXException {
        if (null == origParticipants) {
            origParticipants = partStor.selectParticipants(ctx, getTaskId(), type);
            if (Tools.isFolderPrivate(folder)) {
                Tools.fillStandardFolders(ctx.getContextId(), getTaskId(), getOrigParticipants(), getOrigFolder(), true);
            }
        }
        return origParticipants;
    }

    Set<TaskParticipant> getUpdatedParticipants() throws OXException {
        if (null == updatedParticipants) {
            updatedParticipants = new HashSet<TaskParticipant>();
            if (changed.containsParticipants()) {
                updatedParticipants.addAll(getChangedParticipants());
            } else {
                updatedParticipants.addAll(getOrigParticipants());
            }
        }
        return updatedParticipants;
    }

    /**
     * @return the original folder of the task.
     * @throws OXException if folders can't be selected.
     */
    private Set<Folder> getOrigFolder() throws OXException {
        if (null == origFolders) {
            origFolders = foldStor.selectFolder(ctx, getTaskId(), type);
        }
        return origFolders;
    }

    /**
     * @return the set of folder that will be in the database after the update.
     * @throws OXException if some problem occurs.
     */
    Set<Folder> getUpdatedFolder() throws OXException {
        if (null == updatedFolders) {
            updatedFolders = new HashSet<Folder>();
            updatedFolders.addAll(getOrigFolder());
            updatedFolders.addAll(getAddedFolder());
            updatedFolders.removeAll(getRemovedFolder());
        }
        return updatedFolders;
    }

    /**
     * Checks everything and prepares the data structures for an update.
     *
     * @throws OXException if an error occurs
     */
    void prepare() throws OXException {
        if (getOrigTask().getLastModified().after(lastRead)) {
            throw TaskExceptionCode.MODIFIED.create();
        }
        checkPermission();
        // Do logic checks.
        TaskLogic.checkUpdateTask(changed, getOrigTask(), user, permissionBits, getChangedParticipants(), getOrigParticipants());
        // Now we do the damn stuff.
        prepareWithoutChecks();
    }

    /**
     * Prepares the data structures for an update.
     *
     * @throws OXException if an error occurs.
     */
    void prepareWithoutChecks() throws OXException {
        prepareFields();
        prepareParticipants();
        prepareFolder();
    }

    private boolean preparedFields;

    private void prepareFields() throws OXException {
        if (preparedFields) {
            return;
        }
        modifiedFields = TaskLogic.findModifiedFields(getOrigTask(), changed);
        preparedFields = true;
    }

    private boolean preparedFolder;

    private void prepareFolder() throws OXException {
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
                final Set<InternalParticipant> internal = ParticipantStorage.extractInternal(getUpdatedParticipants());
                Tools.fillStandardFolders(ctx, internal);
                addedFolder.addAll(TaskLogic.createFolderMapping(internal));
            }
            // Move public -> public is already fine.
        } else {
            if (FolderObject.PUBLIC == destType) {
                // Move private -> public
                removedFolder.addAll(getOrigFolder());
            } else {
                // Update folder according to added/removed participants
                final Set<InternalParticipant> addedInternal = ParticipantStorage.extractInternal(added);
                Tools.fillStandardFolders(ctx, addedInternal);
                addedFolder.addAll(TaskLogic.createFolderMapping(addedInternal));
                removedFolder.addAll(TaskLogic.createFolderMapping(ParticipantStorage.extractInternal(removed)));
            }
        }
        // Ensure only one folder mapping for one user.
        for (final Iterator<Folder> iter = addedFolder.iterator(); iter.hasNext();) {
            final Folder testFolder = iter.next();
            for (final Folder origFolder : getOrigFolder()) {
                if (testFolder.getUser() == origFolder.getUser() && !removedFolder.contains(origFolder)) {
                    iter.remove();
                }
            }
        }
        // Do not use #getUpdatedFolder() here because modifications may be done on added and removed folder.
        final Set<Folder> empty = new HashSet<Folder>();
        empty.addAll(getOrigFolder());
        empty.addAll(addedFolder);
        empty.removeAll(removedFolder);
        // Check if updated folders will be empty - last participant has been removed.
        if (empty.isEmpty()) {
            // add current user folder mapping
            addedFolder.add(FolderStorage.extractFolderOfUser(getOrigFolder(), getUserId()));
        }
        // Check if delegating user removed himself from the participant list.
        if (getUserId() == getOrigTask().getCreatedBy() && null == FolderStorage.extractFolderOfUser(empty, getUserId()) && FolderObject.PRIVATE == destType) {
            Folder delegatorFolder = FolderStorage.extractFolderOfUser(getOrigFolder(), getUserId());
            if (null == delegatorFolder) {
                delegatorFolder = new Folder(Tools.getUserTaskStandardFolder(ctx, getUserId()), getUserId());
            }
            addedFolder.add(delegatorFolder);
        }
        preparedFolder = true;
    }

    private void checkPermission() throws OXException {
        if (isMove()) {
            // task is deleted in source folder and created in destination
            // folder.
            Permission.checkDelete(ctx, user, permissionBits, folder, getOrigTask());
            Permission.checkCreate(ctx, user, permissionBits, getDestFolder());
            // move out of a shared folder is not allowed.
            if (Tools.isFolderShared(folder, user)) {
                throw TaskExceptionCode.NO_SHARED_MOVE.create(folder.getFolderName(), I(getFolderId()));
            }
            // move into a shared folder is not allowed.
            if (Tools.isFolderShared(getDestFolder(), user)) {
                throw TaskExceptionCode.NO_SHARED_MOVE.create(getDestFolder().getFolderName(), I(getDestFolderId()));
            }
            // moving private tasks to a public folder isn't allowed.
            if (getOrigTask().getPrivateFlag() && Tools.isFolderPublic(getDestFolder())) {
                throw TaskExceptionCode.NO_PRIVATE_MOVE_TO_PUBLIC.create(getDestFolder().getFolderName(), I(getDestFolderId()));
            }
        } else {
            Permission.checkWriteInFolder(ctx, user, permissionBits, folder, getOrigTask());
            // Check if task appears in folder.
            if (null == FolderStorage.getFolder(getOrigFolder(), getFolderId())) {
                throw TaskExceptionCode.NOT_IN_FOLDER.create(I(getTaskId()), folder.getFolderName(), I(getFolderId()));
            }
            if (Tools.isFolderShared(folder, user) && getOrigTask().getPrivateFlag()) {
                throw TaskExceptionCode.NO_PERMISSION.create(I(getTaskId()), folder.getFolderName(), I(getFolderId()));
            }
        }
    }

    private Set<InternalParticipant> addedGroupParticipants = null;

    private Set<InternalParticipant> getGroupParticipants() throws OXException {
        if (null == addedGroupParticipants) {
            addedGroupParticipants = TaskLogic.getGroupParticipants(ctx, changed.getParticipants());
        }
        return addedGroupParticipants;
    }

    private boolean preparedParts;

    private void prepareParticipants() throws OXException {
        if (preparedParts) {
            return;
        }
        if (changed.containsParticipants()) {
            // Find added participants
            added.addAll(getChangedParticipants());
            added.removeAll(getOrigParticipants());

            // Find all participants that contain a group id now
            changedGroup.addAll(getGroupParticipants());
            changedGroup.retainAll(getOrigParticipants());
            prepareParticipantsWithChangedGroup();

            // Replace added participants with the values from the removed
            // one to get folder and confirmation information.
            // Only internal participants can be selected here because of
            // type REMOVED.
            if (ACTIVE == type) {
                final Set<TaskParticipant> origRemovedParts = partStor.selectParticipants(ctx, getTaskId(), REMOVED);
                origRemovedParts.retainAll(added);
                added.addAll(origRemovedParts);
            }
            // Find removed participants
            removed.addAll(getOrigParticipants());
            removed.removeAll(getChangedParticipants());
        }
        preparedParts = true;
    }

    private void prepareParticipantsWithChangedGroup() throws OXException {
        final Set<TaskParticipant> toCheck = new HashSet<TaskParticipant>();
        Set<InternalParticipant> changedInternals = ParticipantStorage.extractInternal(getChangedParticipants());

        // Retain the participants with changes
        toCheck.addAll(getOrigParticipants());
        toCheck.retainAll(getChangedParticipants());

        for (final InternalParticipant ip : ParticipantStorage.extractInternal(toCheck)) {
            InternalParticipant cp = ParticipantStorage.getParticipant(changedInternals, ip.getIdentifier());
            if (cp != null) {
                cp.setConfirm(ip.getConfirm());
                cp.setConfirmMessage(ip.getConfirmMessage());
                changedGroup.add(cp);
            }
        }
    }

    private void generateUpdated() throws OXException {
        updated = new Task();
        updated.setObjectID(getTaskId());
        for (final Mapper mapper : Mapping.MAPPERS) {
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
            Tools.fillStandardFolders(ctx.getContextId(), getTaskId(), parts, getUpdatedFolder(), true);
        }
        updated.setParticipants(TaskLogic.createParticipants(parts));
        updated.setUsers(TaskLogic.createUserParticipants(parts));
    }

    /* ---------- Getter ---------- */

    /**
     * @return the added
     */
    Set<TaskParticipant> getAdded() throws OXException {
        prepareParticipants();
        return added;
    }

    /**
     * @return the participants who belong to group after the update.
     */
    Set<InternalParticipant> getChangedGroup() throws OXException {
        prepareParticipants();
        return changedGroup;
    }

    /**
     * @return the addedFolder
     * @throws OXException
     */
    Set<Folder> getAddedFolder() throws OXException {
        prepareFolder();
        return addedFolder;
    }

    /**
     * @return the modifiedFields
     * @throws OXException
     */
    int[] getModifiedFields() throws OXException {
        prepareFields();
        return modifiedFields;
    }

    /**
     * @return the removed
     */
    Set<TaskParticipant> getRemoved() throws OXException {
        prepareParticipants();
        return removed;
    }

    /**
     * @return the removedFolder
     * @throws OXException
     */
    Set<Folder> getRemovedFolder() throws OXException {
        prepareFolder();
        return removedFolder;
    }

    Task getUpdated() throws OXException {
        if (null == updated) {
            generateUpdated();
        }
        return updated;
    }

    /* ---------- Convenience methods ---------- */

    private int getFolderId() {
        return folder.getObjectID();
    }

    /**
     * @return the identifier of the updating user.
     */
    private int getUserId() {
        return user.getId();
    }

    private int getTaskId() {
        return changed.getObjectID();
    }

    boolean isMove() {
        return changed.containsParentFolderID() && changed.getParentFolderID() != getFolderId();
    }

    private FolderObject getDestFolder() throws OXException {
        if (null == destFolder) {
            destFolder = isMove() ? Tools.getFolder(ctx, changed.getParentFolderID()) : folder;
        }
        return destFolder;
    }

    private int getDestFolderId() throws OXException {
        return getDestFolder().getObjectID();
    }

    /* ---------- Now the methods for doing the update stuff ---------- */

    /**
     * This method executes the prepared update.
     *
     * @throws OXException
     */
    void doUpdate() throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            con.setAutoCommit(false);
            updateTask(
                ctx,
                con,
                changed,
                lastRead,
                getModifiedFields(),
                getAdded(),
                getRemoved(),
                getChangedGroup(),
                getAddedFolder(),
                getRemovedFolder(),
                type);
            if (ACTIVE == type && isMove()) {
                final Task dummy = Tools.createDummyTask(getTaskId(), getUserId(), getOrigTask().getUid());
                storage.insertTask(ctx, con, dummy, DELETED, true, TaskStorage.TOMBSTONE_ATTRS);
                final Folder sourceFolder = FolderStorage.getFolder(getRemovedFolder(), getFolderId());
                foldStor.insertFolder(ctx, con, getTaskId(), sourceFolder, DELETED);
                foldStor.deleteFolder(ctx, con, getTaskId(), getDestFolderId(), DELETED, false);
            }
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw TaskExceptionCode.UPDATE_FAILED.create(e, e.getMessage());
        } catch (final OXException e) {
            rollback(con);
            throw e;
        } finally {
            autocommit(con);
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    static void updateTask(final Context ctx, final Connection con, final Task task, final Date lastRead, final int[] modified, final Set<TaskParticipant> add, final Set<TaskParticipant> remove, final Set<Folder> addFolder, final Set<Folder> removeFolder) throws OXException {
        updateTask(ctx, con, task, lastRead, modified, add, remove, null, addFolder, removeFolder, ACTIVE);
    }

    /**
     * This method execute the SQL statements on the given connection defined by the given data for the update.
     *
     * @param ctx Context.
     * @param con writable database connection.
     * @param task task object with changed values.
     * @param lastRead when has this task object been read last.
     * @param modified modified task attributes.
     * @param add added participants.
     * @param remove removed participants.
     * @param addFolder added folder mappings for the participants.
     * @param removeFolder removed folder mappings for the participants.
     * @throws OXException if some SQL command fails.
     */
    static void updateTask(final Context ctx, final Connection con, final Task task, final Date lastRead, final int[] modified, final Set<TaskParticipant> add, final Set<TaskParticipant> remove, final Set<InternalParticipant> changedGroup, final Set<Folder> addFolder, final Set<Folder> removeFolder, final StorageType type) throws OXException {
        final int taskId = task.getObjectID();
        storage.updateTask(ctx, con, task, lastRead, modified, type);
        if (null != add) {
            partStor.insertParticipants(ctx, con, taskId, add, type);
            if (ACTIVE == type) {
                partStor.deleteParticipants(ctx, con, taskId, add, REMOVED, false);
            }
        }
        if (null != remove) {
            if (ACTIVE == type) {
                partStor.insertParticipants(ctx, con, taskId, remove, REMOVED);
            }
            partStor.deleteParticipants(ctx, con, taskId, remove, type, true);
        }
        if (null != changedGroup) {
            partStor.updateInternal(ctx, con, taskId, changedGroup, type);
        }
        if (null != removeFolder) {
            foldStor.deleteFolder(ctx, con, taskId, removeFolder, type);
        }
        if (null != addFolder) {
            foldStor.insertFolder(ctx, con, taskId, addFolder, type);
        }
    }

    void sentEvent(final Session session) throws OXException {
        final Task orig = getOrigTask();
        if (getUserId() != orig.getCreatedBy() && null == ParticipantStorage.getParticipant(ParticipantStorage.extractInternal(getChangedParticipants()), orig.getCreatedBy())) {
            // Delegator not participant and participant changed task. Change parent folder of original task to delegators folder identifier
            // so we are able to use that for participant notification.
            Folder delegatorFolder = FolderStorage.extractFolderOfUser(getOrigFolder(), orig.getCreatedBy());
            if (null == delegatorFolder) {
                // Delegator has been removed from participant list.
                delegatorFolder = FolderStorage.extractFolderOfUser(getOrigFolder(), getUserId());
            }
            // Something similar happens if a user changes a task in a shared folder.
            if (null == delegatorFolder && 1 == getOrigFolder().size()) {
                // If that task is only located in a single folder use that as a fallback.
                delegatorFolder = getOrigFolder().toArray(new Folder[1])[0];
            }
            if (null != delegatorFolder) {
                orig.setParentFolderID(delegatorFolder.getIdentifier());
            }
        }
        sentEvent(session, getUpdated(), orig, getDestFolder());
    }

    static void sentEvent(final Session session, final Task updated, final Task orig, final FolderObject dest) throws OXException {
        try {
            new EventClient(session).modify(orig, updated, dest);
        } catch (final OXException e) {
            throw TaskExceptionCode.EVENT.create(e);
        }
    }

    void updateReminder() throws OXException, OXException {
        if (isMove() && getUpdatedParticipants().isEmpty()) {
            Reminder.updateReminderOnMove(ctx, getUpdated().getObjectID(), getFolderId(), getDestFolderId(), getUpdated().getPrivateFlag());
        }
        updateReminder(ctx, getUpdated(), user, isMove(), getRemoved(), getDestFolder(), getUpdatedParticipants(), getUpdatedFolder());
    }

    static void updateReminder(Context ctx, Task updated, User user, boolean move, Set<TaskParticipant> removed, FolderObject destFolder, Set<TaskParticipant> participants, Set<Folder> folders) throws OXException {
        if (updated.containsAlarm()) {
            Reminder.updateAlarm(ctx, updated, user);
        }
        if (move) {
            if (Tools.isFolderPrivate(destFolder)) {
                Tools.fillStandardFolders(ctx.getContextId(), updated.getObjectID(), participants, folders, true);
            }
            Reminder.fixAlarm(ctx, updated, removed, participants, folders);
        }
    }

    void makeNextRecurrence(final Session session) throws OXException {
        if (CalendarObject.NO_RECURRENCE != updated.getRecurrenceType() && Task.DONE == updated.getStatus() && Arrays.contains(
            getModifiedFields(),
            Status.SINGLETON.getId())) {

            final TaskSearchObject search = new TaskSearchObject();
            search.setTitle(updated.getTitle());
            search.setStatus(updated.getStatus());

            try {
                Permission.checkReadInFolder(ctx, user, permissionBits, folder);
            } catch (final OXException e) {
                throw e;
            }

            final boolean own = Permission.canReadInFolder(ctx, user, permissionBits, destFolder);
            final List<Integer> emptyList = new ArrayList<Integer>();
            final List<Integer> listWithFolder = new ArrayList<Integer>();
            listWithFolder.add(I(destFolder.getObjectID()));

            TaskIterator ti;
            if (own) {
                ti = storage.search(ctx, getUserId(), search, 0, Order.ASCENDING, new int[] {
                    Task.PERCENT_COMPLETED, DataObject.CREATED_BY, CalendarObject.START_DATE, CalendarObject.TITLE }, emptyList, listWithFolder, emptyList);
            } else {
                ti = storage.search(ctx, getUserId(), search, 0, Order.ASCENDING, new int[] {
                    Task.PERCENT_COMPLETED, DataObject.CREATED_BY, CalendarObject.START_DATE, CalendarObject.TITLE }, listWithFolder, emptyList, emptyList);
            }

            try {
                final boolean next = TaskLogic.makeRecurrence(updated);
                boolean duplicateExists = false;
                while (ti.hasNext()) {
                    final Task actual = ti.next();
                    final boolean percentComplete = actual.getPercentComplete() == updated.getPercentComplete();
                    final boolean createdBy = actual.getCreatedBy() == updated.getCreatedBy();
                    final boolean title = null != actual.getTitle() && actual.getTitle().equals(updated.getTitle());
                    boolean startDate;

                    if (actual.getStartDate() != null && updated.getStartDate() != null) {
                        final long aStartDateLong = actual.getStartDate().getTime();
                        final long uStartDateLong = updated.getStartDate().getTime();
                        startDate = aStartDateLong / 1000 == uStartDateLong / 1000;
                    } else {
                        if (updated.getStartDate() == null) {
                            startDate = true;
                        } else {
                            startDate = false;
                        }
                    }
                    if (percentComplete && createdBy && startDate && title) {

                        duplicateExists = true;
                        break;
                    }
                }
                SearchIterators.close(ti);
                ti = null;

                if (!duplicateExists && next) {
                    Task recurrence = getUpdated();
                    recurrence.removeUid();
                    insertNextRecurrence(session, ctx, getUserId(), permissionBits, folder, recurrence, getUpdatedParticipants(), getUpdatedFolder());
                }
            } finally {
                SearchIterators.close(ti);
            }
        }
    }

    /**
     * Inserts a new task according to the recurrence.
     *
     * @param task recurring task.
     * @param parts participants of the updated task.
     * @param folders folders of the updated task.
     * @throws OXException if creating the new task fails.
     * @throws OXException if sending an event about new task fails.
     */
    private static void insertNextRecurrence(final Session session, final Context ctx, final int userId, final UserPermissionBits permissionBits, final FolderObject folder, final Task task, final Set<TaskParticipant> parts, final Set<Folder> folders) throws OXException, OXException {
        // TODO create insert class
        TaskLogic.checkNewTask(task, userId, permissionBits, parts);
        InsertData.insertTask(ctx, task, parts, folders);
        try {
            new EventClient(session).create(task, folder);
        } catch (final OXException e) {
            throw e;
        }
    }
}
