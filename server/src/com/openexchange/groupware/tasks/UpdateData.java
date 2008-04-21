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

import static com.openexchange.tools.sql.DBUtils.rollback;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.EventException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.mapping.Status;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.Arrays;

/**
 * This class contains the logic for updating tasks. It calculates what is to
 * modify.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
class UpdateData {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(UpdateData.class);

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
     * Which type of task to update.
     */
    private StorageType type;

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
        this(ctx, user, userConfig, folder, changed, lastRead, StorageType
            .ACTIVE);
    }

    /**
     * Default constructor.
     * @param ctx Context.
     * @param user User.
     * @param userConfig User configuration.
     * @param folder folder throught that the task is changed.
     * @param changed the changed task.
     * @param lastRead timestamp when the to update task was read last.
     * @param type ACTIVE or DELETED.
     */
    UpdateData(final Context ctx, final User user,
        final UserConfiguration userConfig, final FolderObject folder,
        final Task changed, final Date lastRead, final StorageType type) {
        super();
        this.ctx = ctx;
        this.user = user;
        this.userConfig = userConfig;
        this.folder = folder;
        this.changed = changed;
        this.lastRead = lastRead;
        this.type = type;
    }

    /**
     * @return the original task.
     * @throws TaskException if loading of the original tasks fails.
     */
    private Task getOrigTask() throws TaskException {
        if (null == origTask) {
            origTask = storage.selectTask(ctx, getTaskId(), type);
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
                type);
            if (Tools.isFolderPrivate(folder)) {
                Tools.fillStandardFolders(getOrigParticipants(),
                    getOrigFolder(), true);
            }
        }
        return origParticipants;
    }

    Set<TaskParticipant> getUpdatedParticipants() throws TaskException {
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
     * @throws TaskException if folders can't be selected.
     */
    private Set<Folder> getOrigFolder() throws TaskException {
        if (null == origFolders) {
            origFolders = foldStor.selectFolder(ctx, getTaskId(), type);
        }
        return origFolders;
    }

    /**
     * @return the set of folder that will be in the database after the update.
     * @throws TaskException if some problem occurs.
     */
    Set<Folder> getUpdatedFolder() throws TaskException {
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
        prepareWithoutChecks();
    }

    /**
     * Prepares the data structures for an update.
     * @throws TaskException if an error occurs.
     */
    void prepareWithoutChecks() throws TaskException {
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
        // Ensure only one folder mapping for one user.
        final Iterator<Folder> iter = addedFolder.iterator();
        while (iter.hasNext()) {
            final Folder addedFolder = iter.next();
            for (Folder origFolder : origFolders) {
                if (addedFolder.getUser() == origFolder.getUser()) {
                    iter.remove();
                }
            }
        }
        // Check if updated folders will be empty - last participant has been
        // removed.
        final Set<Folder> updated = new HashSet<Folder>();
        updated.addAll(getOrigFolder());
        updated.addAll(addedFolder);
        updated.removeAll(removedFolder);
        if (updated.isEmpty()) {
            // add current user folder mapping
            addedFolder.add(FolderStorage.extractFolderOfUser(getOrigFolder(),
                getUserId()));
        }
        preparedFolder = true;
    }

    private void checkPermission() throws TaskException {
        if (isMove()) {
            // task is deleted in source folder and created in destination
            // folder.
            Permission.checkDelete(ctx, user, userConfig, folder,
                getOrigTask());
            Permission.checkCreate(ctx, user, userConfig, getDestFolder());
            // move out of a shared folder is not allowed.
            if (Tools.isFolderShared(folder, user)) {
                throw new TaskException(Code.NO_SHARED_MOVE, folder
                    .getFolderName(), Integer.valueOf(getFolderId()));
            }
            // move into a shared folder is not allowed.
            if (Tools.isFolderShared(getDestFolder(), user)) {
                throw new TaskException(Code.NO_SHARED_MOVE, getDestFolder()
                    .getFolderName(), Integer.valueOf(getDestFolderId()));
            }
            // moving private tasks to a public folder isn't allowed.
            if (getOrigTask().getPrivateFlag()
                && Tools.isFolderPublic(getDestFolder())) {
                throw new TaskException(Code.NO_PRIVATE_MOVE_TO_PUBLIC,
                    getDestFolder().getFolderName(), Integer.valueOf(
                    getDestFolderId()));
            }
        } else {
            Permission.checkWriteInFolder(ctx, user, userConfig, folder,
                getOrigTask());
            // Check if task appears in folder.
            if (null == FolderStorage.getFolder(getOrigFolder(),
                getFolderId())) {
                throw new TaskException(Code.NO_PERMISSION, Integer.valueOf(
                    getTaskId()), folder.getFolderName(), Integer.valueOf(
                    getFolderId()));
            }
            if (Tools.isFolderShared(folder, user) && getOrigTask()
                .getPrivateFlag()) {
                throw new TaskException(Code.NO_PERMISSION, Integer.valueOf(
                    getTaskId()), folder.getFolderName(), Integer.valueOf(
                        getFolderId()));
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
            if (StorageType.ACTIVE == type) {
                final Set<TaskParticipant> origRemovedParts = partStor
                    .selectParticipants(ctx, getTaskId(), StorageType.REMOVED);
                origRemovedParts.retainAll(added);
                added.addAll(origRemovedParts);
            }
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

    /* ---------- Now the methods for doing the update stuff ---------- */

    /**
     * This method executes the prepared update.
     * @throws TaskException 
     */
    void doUpdate() throws TaskException {
        updateTask(ctx, changed, lastRead, getModifiedFields(), getAdded(),
            getRemoved(), getAddedFolder(), getRemovedFolder(), type);
    }

    static void updateTask(final Context ctx, final Task task,
        final Date lastRead, final int[] modified,
        final Set<TaskParticipant> add, final Set<TaskParticipant> remove,
        final Set<Folder> addFolder, final Set<Folder> removeFolder)
        throws TaskException {
        updateTask(ctx, task, lastRead, modified, add, remove, addFolder,
            removeFolder, StorageType.ACTIVE);
    }

    /**
     * This method execute the SQL statements on writable connection defined by
     * the given data for the update. The database connection is put into
     * transaction mode.
     * @param ctx Context.
     * @param task task object with changed values.
     * @param lastRead when has this task object been read last.
     * @param modified modified task attributes.
     * @param add added participants.
     * @param remove removed participants.
     * @param addFolder added folder mappings for the participants.
     * @param removeFolder removed folder mappings for the participants.
     * @throws TaskException if some SQL command fails.
     */
    static void updateTask(final Context ctx, final Task task,
        final Date lastRead, final int[] modified,
        final Set<TaskParticipant> add, final Set<TaskParticipant> remove,
        final Set<Folder> addFolder, final Set<Folder> removeFolder,
        final StorageType type) throws TaskException {
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            updateTask(ctx, con, task, lastRead, modified, add, remove,
                addFolder, removeFolder, type);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.UPDATE_FAILED, e, e.getMessage());
        } catch (TaskException e) {
            rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error("Problem setting auto commit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    static void updateTask(final Context ctx, final Connection con,
        final Task task, final Date lastRead, final int[] modified,
        final Set<TaskParticipant> add, final Set<TaskParticipant> remove,
        final Set<Folder> addFolder, final Set<Folder> removeFolder)
        throws TaskException {
        updateTask(ctx, con, task, lastRead, modified, add, remove, addFolder,
            removeFolder, StorageType.ACTIVE);
    }

    /**
     * This method execute the SQL statements on the given connection defined by
     * the given data for the update.
     * @param ctx Context.
     * @param con writable database connection.
     * @param task task object with changed values.
     * @param lastRead when has this task object been read last.
     * @param modified modified task attributes.
     * @param add added participants.
     * @param remove removed participants.
     * @param addFolder added folder mappings for the participants.
     * @param removeFolder removed folder mappings for the participants.
     * @throws TaskException if some SQL command fails.
     */
    static void updateTask(final Context ctx, final Connection con,
        final Task task, final Date lastRead, final int[] modified,
        final Set<TaskParticipant> add, final Set<TaskParticipant> remove,
        final Set<Folder> addFolder, final Set<Folder> removeFolder,
        final StorageType type) throws TaskException {
        final int taskId = task.getObjectID();
        storage.updateTask(ctx, con, task, lastRead, modified, type);
        if (null != add) {
            partStor.insertParticipants(ctx, con, taskId, add, type);
            if (StorageType.ACTIVE == type) {
                partStor.deleteParticipants(ctx, con, taskId, add, StorageType
                    .REMOVED, false);
            }
        }
        if (null != remove) {
            if (StorageType.ACTIVE == type) {
                partStor.insertParticipants(ctx, con, taskId, remove,
                    StorageType.REMOVED);
            }
            partStor.deleteParticipants(ctx, con, taskId, remove, type, true);
        }
        if (null != removeFolder) {
            foldStor.deleteFolder(ctx, con, taskId, removeFolder, type);
        }
        if (null != addFolder) {
            foldStor.insertFolder(ctx, con, taskId, addFolder, type);
        }
    }

    void sentEvent(final Session session) throws OXException, TaskException {
        sentEvent(session, getUpdated());
    }

    static void sentEvent(final Session session, final Task updated)
        throws TaskException, OXException {
        try {
            new EventClient(session).modify(updated);
        } catch (EventException e) {
            throw new TaskException(Code.EVENT, e);
        } catch (ContextException e) {
            throw new TaskException(Code.EVENT, e);
        }
    }

    void updateReminder() throws OXException, TaskException {
        updateReminder(ctx, getUpdated(), user, isMove(), getRemoved(),
            getUpdatedFolder());
    }

    static void updateReminder(final Context ctx, final Task updated,
        final User user, final boolean move, final Set<TaskParticipant> removed,
        final Set<Folder> folders) throws OXException {
        if (updated.containsAlarm()) {
            Reminder.updateAlarm(ctx, updated, user);
        }
        if (move) {
            Reminder.fixAlarm(ctx, updated, removed, folders);
        }
    }

    void makeNextRecurrence(final Session session) throws TaskException,
        OXException {
        if (Task.NO_RECURRENCE != updated.getRecurrenceType() && Task.DONE
            == updated.getStatus() && Arrays.contains(getModifiedFields(),
                Status.SINGLETON.getId())) {
            insertNextRecurrence(session, ctx, getUserId(), userConfig,
                getUpdated(), getUpdatedParticipants(), getUpdatedFolder());
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
    private static void insertNextRecurrence(final Session session,
        final Context ctx, final int userId, final UserConfiguration userConfig,
        final Task task, final Set<TaskParticipant> parts,
        final Set<Folder> folders) throws TaskException, OXException {
        final boolean next = TaskLogic.makeRecurrence(task);
        if (next) {
            // TODO create insert class
            TaskLogic.checkNewTask(task, userId, userConfig, parts);
            TaskLogic.insertTask(ctx, task, parts, folders);
            try {
                new EventClient(session).create(task);
            } catch (EventException e) {
                throw Tools.convert(new TaskException(Code.EVENT, e));
            } catch (ContextException e) {
                throw Tools.convert(new TaskException(Code.EVENT, e));
            }
        }
    }
}
