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

import com.openexchange.api2.OXException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.event.EventClient;
import com.openexchange.event.InvalidStateException;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.TaskParticipant.Type;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;

/**
 * This class contains logic methods for the tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TaskLogic {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(TaskLogic.class);

    /**
     * Prevent instantiation
     */
    private TaskLogic() {
        super();
    }

    /**
     * Checks if a new task is not missing any data, does not contains any wrong
     * data and if the user is allowed to create a task in tasks folder.
     * @param task Task to create.
     * @param userId unique identifier of the user that wants to create the
     * task.
     * @param userConfig groupware configuration of the user that wants to
     * create the task.
     * @throws TaskException if the task can't be created.
     */
    static void checkNewTask(final Task task, final int userId,
        final UserConfiguration userConfig,
        final Set<TaskParticipant> participants) throws TaskException {
        checkMissingAttributes(task, userId);
        checkDates(task);
        checkStateAndProgress(task);
        Permission.checkDelegation(userConfig, task.getParticipants());
        // TODO Check if creator is participant in private or shared folder
        // Maybe the owner of the shared folder is the delegator of the task.
        checkPrivateFlag(task.getPrivateFlag(), false, participants, null);
        checkParticipants(participants, task.getCreatedBy());
        checkRecurrence(task, null);
    }

    /**
     * Checks if the data of an to update task is correct.
     * @param task Task object with the updated attributes.
     * @param oldTask Task object that should be updated.
     * @param user user that wants to change the task.
     * @param userConfig groupware configuration of the user that wants to
     * change the task.
     * @param newParts changed participants.
     * @param oldParts participants of the original task.
     * @throws TaskException if the check fails.
     */
    static void checkUpdateTask(final Task task, final Task oldTask,
        final User user, final UserConfiguration userConfig,
        final Set<TaskParticipant> newParts,
        final Set<TaskParticipant> oldParts) throws TaskException {
        if (!task.containsLastModified()) {
            task.setLastModified(new Date());
        }
        if (!task.containsModifiedBy()) {
            task.setModifiedBy(user.getId());
        }
        checkDates(task);
        checkStateAndProgress(task);
        Permission.checkDelegation(userConfig, task.getParticipants());
        final boolean changedParts = task.containsParticipants();
        // Only creator is allowed to set private flag.
        if (task.containsPrivateFlag() && task.getPrivateFlag()
            && oldTask.getCreatedBy() != user.getId()) {
            throw new TaskException(Code.ONLY_CREATOR_PRIVATE);
        }
        final boolean privat = task.containsPrivateFlag() ? task
            .getPrivateFlag() : oldTask.getPrivateFlag();
        checkPrivateFlag(privat, changedParts, oldParts, newParts);
        // TODO Check if creator is participant in private or shared folder
        final Set<TaskParticipant> destParts = changedParts ? newParts
            : oldParts;
        checkParticipants(destParts, oldTask.getCreatedBy());
        checkRecurrence(task, oldTask);
    }

    /**
     * Checks if the new task is missing some attributes so that it can't be
     * stored to the database.
     * @param task Task to create.
     * @param userId user that wants to create the task and will be therefore
     * used as task creator if no one is defined.
     * @throws TaskException if the task can't be created.
     */
    private static void checkMissingAttributes(final Task task,
        final int userId) throws TaskException {
        if (!task.containsParentFolderID()) {
            throw new TaskException(Code.FOLDER_IS_MISSING);
        }
        final Date timestamp = new Date();
        if (!task.containsCreationDate()) {
            task.setCreationDate(timestamp);
        }
        if (!task.containsLastModified()) {
            task.setLastModified(timestamp);
        }
        if (!task.containsCreatedBy()) {
            task.setCreatedBy(userId);
        }
        if (!task.containsModifiedBy()) {
            task.setModifiedBy(userId);
        }
        if (!task.containsPrivateFlag()) {
            task.setPrivateFlag(false);
        }
        if (!task.containsRecurrenceType()) {
            task.setRecurrenceType(Task.NO_RECURRENCE);
        }
        if (!task.containsNumberOfAttachments()) {
            task.setNumberOfAttachments(0);
        }
    }

    /**
     * Checks if the start date is before the end date of a task. Some
     * additional checks are done before. <code>null</code> dates are ignored.
     * @param task Task to check the dates of.
     * @throws TaskException if the start date is before the end date.
     */
    private static void checkDates(final Task task) throws TaskException {
        if (task.containsStartDate() && task.containsEndDate()) {
            final Date start = task.getStartDate();
            final Date end = task.getEndDate();
            if (null != start && null != end && start.after(end)) {
                throw new TaskException(Code.START_NOT_BEFORE_END, start, end);
            }
        }
    }

    private static void checkStateAndProgress(final Task task)
        throws TaskException {
        if (!task.containsPercentComplete() || !task.containsStatus()) {
            return;
        }
        final int progress = task.getPercentComplete();
        if (progress < 0 || progress > Task.PERCENT_MAXVALUE) {
            throw new TaskException(Code.INVALID_PERCENTAGE, progress);
        }
        switch (task.getStatus()) {
        case Task.NOT_STARTED:
            if (0 != progress) {
                throw new TaskException(Code.PERCENTAGE_NOT_ZERO, progress);
            }
            break;
        case Task.IN_PROGRESS:
        case Task.DEFERRED:
        case Task.WAITING:
            // Nothing to check. The progress can be everything between 0 and
            // 100.
            break;
        case Task.DONE:
            // TODO Disabled because GUI has problems with this.
            if (false && Task.PERCENT_MAXVALUE != progress) {
                throw new TaskException(Code.PERCENTAGE_NOT_FULL, progress);
            }
            break;
        default:
            throw new TaskException(Code.INVALID_TASK_STATE, task.getStatus());
        }
    }

    /**
     * This method checks if the user tries to delegate a private flagged task.
     * @param privat private flag of the old or new task.
     * @param changed if updated task contains participants.
     * @param oldParts original participants of the task.
     * @param newParts changed participants of the task.
     * @throws TaskException if the check fails.
     */
    private static void checkPrivateFlag(final boolean privat,
        final boolean changed, final Set<TaskParticipant> oldParts,
        final Set<TaskParticipant> newParts) throws TaskException {
        if (!privat) {
            return;
        }
        if (changed) {
            if (newParts.size() > 0) {
                throw new TaskException(Code.NO_PRIVATE_DELEGATE);
            }
        } else {
            if (oldParts.size() > 0) {
                throw new TaskException(Code.NO_PRIVATE_DELEGATE);
            }
        }
    }
    
    /**
     * Checks that the creator can't be participant if the according global
     * option isn't set.
     * @param participants Participants of the task.
     * @param creator unique identifier of the user that created the task.
     * @throws TaskException if the check fails.
     */
    private static void checkParticipants(
        final Set<TaskParticipant> participants, final int creator)
        throws TaskException {
        if (null == participants) {
            return;
        }
        checkExternal(ParticipantStorage.extractExternal(participants));
    }

    /**
     * Checks if external participants contain consistent data. Currently
     * external participants are checked to contain an email address.
     * @param participants external participants.
     * @throws TaskException if an external participant does not contain an
     * email address.
     */
    private static void checkExternal(
        final Set<ExternalParticipant> participants) throws TaskException {
        for (ExternalParticipant participant : participants) {
            final String mail = participant.getMail();
            if (null == mail || mail.length() == 0) {
                throw new TaskException(Code.EXTERNAL_WITHOUT_MAIL);
            }
        }
    }

    /**
     * This method checks if the necessary fields for a recurring task are
     * defined.
     * @param task Recurring task.
     * @param oldTask Original recurring task on update.
     * @throws TaskException if a necessary recurrence attribute is missing.
     */
    private static void checkRecurrence(final Task task, final Task oldTask)
        throws TaskException {
        if (Task.NO_RECURRENCE != task.getRecurrenceType()) {
            if (null == oldTask) {
                if (!task.containsStartDate()) {
                    throw new TaskException(Code.MISSING_RECURRENCE_VALUE,
                        Task.START_DATE);
                }
                if (!task.containsEndDate()) {
                    throw new TaskException(Code.MISSING_RECURRENCE_VALUE,
                        Task.END_DATE);
                }
            } else {
                if (task.containsStartDate() && null == task.getStartDate()) {
                    throw new TaskException(Code.MISSING_RECURRENCE_VALUE,
                        Task.START_DATE);
                }
                if (task.containsEndDate() && null == task.getEndDate()) {
                    throw new TaskException(Code.MISSING_RECURRENCE_VALUE,
                        Task.START_DATE);
                }
            }
        }
        try {
            CalendarRecurringCollection.checkRecurring(task);
        } catch (OXException e) {
            throw new TaskException(e);
        }
    }

    /**
     * Creates task participant set from the user and group participants.
     * @param ctx Context.
     * @param participants user and group participants.
     * @return a set of task participants.
     * @throws TaskException if resolving of groups to users fails.
     */
    static Set<TaskParticipant> createParticipants(final Context ctx,
        final Participant[] participants) throws TaskException {
        final Set<TaskParticipant> retval = new HashSet<TaskParticipant>();
        if (null == participants) {
            return retval;
        }
        for (Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.USER:
                retval.add(new InternalParticipant(
                    (UserParticipant) participant, null));
                break;
            case Participant.GROUP:
                final GroupParticipant group = (GroupParticipant) participant;
                try {
                    final int[] member = GroupStorage.getInstance(ctx).getGroup(
                        group.getIdentifier()).getMember();
                    for (int userId : member) {
                        final TaskParticipant tParticipant =
                            new InternalParticipant(new UserParticipant(userId),
                            group.getIdentifier());
                        if (!retval.contains(tParticipant)) {
                            retval.add(tParticipant);
                        }
                    }
                } catch (LdapException e) {
                    throw new TaskException(e);
                }
                break;
            case Participant.EXTERNAL_USER:
                retval.add(new ExternalParticipant(
                    (ExternalUserParticipant) participant));
            default:
            }
        }
        return retval;
    }

    static UserParticipant[] createUserParticipants(
        final Set<TaskParticipant> participants) {
        final List<Participant> retval = new ArrayList<Participant>(
            participants.size());
        for (TaskParticipant participant : participants) {
            if (Type.INTERNAL == participant.getType()) {
                final InternalParticipant internal =
                    (InternalParticipant) participant;
                retval.add(internal.getUser());
            }
        }
        return retval.toArray(new UserParticipant[retval.size()]);
    }

    static Participant[] createParticipants(
        final Set<TaskParticipant> participants) {
        final List<Participant> retval = new ArrayList<Participant>();
        final Map<Integer, Participant> groups =
            new HashMap<Integer, Participant>();
        for (TaskParticipant participant : participants) {
            switch (participant.getType()) {
            case INTERNAL:
                final InternalParticipant internal =
                    (InternalParticipant) participant;
                final Integer groupId = internal.getGroupId();
                if (null == groupId) {
                    retval.add(internal.getUser());
                } else {
                    final GroupParticipant group = new GroupParticipant(groupId);
                    if (!groups.containsKey(groupId)) {
                        groups.put(groupId, group);
                    }
                }
                break;
            case EXTERNAL:
                final ExternalParticipant external =
                    (ExternalParticipant) participant;
                retval.add(external.getExternal());
                break;
            default:
                break;
            }
        }
        retval.addAll(groups.values());
        return retval.toArray(new Participant[retval.size()]);
    }

    static Set<Folder> createFolderMapping(
        final Set<InternalParticipant> participants) {
        return createFolderMapping(-1, -1, participants);
    }

    /**
     * Creates the folder mappings for a task.
     * @param folderId unique identifier of the folder that is the main folder
     * of the task.
     * @param userId unique identifier of the user who created the task. The
     * primary folder of the task will be added with this user identifier. On
     * shared folders the user identifier should be the owner of the shared
     * folder.
     * @param participants the task will also be mapped into this users private
     * folder.
     * @return the ready to insert folder mapping.
     */
    static Set<Folder> createFolderMapping(final int folderId, final int userId,
        final Set<InternalParticipant> participants) {
        final Set<Folder> retval = new HashSet<Folder>();
        if (-1 != userId) {
            retval.add(new Folder(folderId, userId));
        }
        for (InternalParticipant participant : participants) {
            if (participant.getIdentifier() == userId) {
                continue;
            }
            final Folder folder = new Folder(participant.getFolderId(),
                participant.getIdentifier());
            if (!retval.contains(folder)) {
                retval.add(folder);
            }
        }
        return retval;
    }

    static int[] findModifiedFields(final Task oldTask, final Task task) {
        final List<Integer> fields = new ArrayList<Integer>();
        for (Mapper mapper : Mapping.MAPPERS) {
            if (mapper.isSet(task)
                && (!mapper.isSet(oldTask) || !mapper.equals(task, oldTask))) {
                fields.add(mapper.getId());
            }
        }
        final int[] retval = new int[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            retval[i] = fields.get(i);
        }
        return retval;
    }

    /**
     * Calculates the next time values for a recurring task.
     * @param task the task object.
     * @return <code>true</code> if a next occurence exists.
     * @throws OXException if an error occurs.
     */
    public static boolean makeRecurrence(final Task task) throws OXException {
        if (task.containsOccurrence() && 0 == task.getOccurrence()) {
            return false;
        }
        final Date[] newTaskDates = calculateRecurring(task);
        if (0 == newTaskDates.length) {
            return false;
        }
        task.setStartDate(newTaskDates[0]);
        task.setEndDate(newTaskDates[1]);
        task.setStatus(Task.NOT_STARTED);
        task.setPercentComplete(0);
        if (task.containsOccurrence()) {
            task.setOccurrence(task.getOccurrence() - 1);
        }
        return true;
    }

    /**
     * This method does compatability changes to the task in that way that the
     * recurrence calculation for appointment also works for tasks.
     * @param task Task to modify and to calculate the recurrence for.
     * @return the new start and end date for the task.
     * @throws OXException if an error occurs.
     */
    private static Date[] calculateRecurring(final Task task)
        throws OXException {
        final Date origStart = task.getStartDate();
        task.setRecurrenceCalculator((int) ((task.getEndDate().getTime()
            - origStart.getTime()) / (CalendarRecurringCollection.MILLI_DAY)));
        boolean removeUntil = false;
        if (task.containsOccurrence() || !task.containsUntil()) {
            removeUntil = true;
            task.setUntil(new Date(Long.MAX_VALUE));
        }
        final RecurringResults rr = CalendarRecurringCollection
            .calculateRecurring(task, 0, 0, 2);
        if (removeUntil) {
            task.removeUntil();
        }
        final RecurringResult result = rr.getRecurringResult(0);
        final Date[] retval;
        if (null == result) {
            retval = new Date[0];
        } else {
            retval = new Date[] { new Date(result.getStart()), new Date(result
                .getEnd()) };
        }
        return retval;
    }

    /**
     * Extracts all participants that are added by the group.
     * @param participants internal task participants.
     * @param groupId the group identifier.
     * @return All participants that are added by the group.
     */
    static Set<InternalParticipant> extractWithGroup(
        final Set<InternalParticipant> participants, final int groupId) {
        final Set<InternalParticipant> retval =
            new HashSet<InternalParticipant>();
        for (InternalParticipant participant : participants) {
            if (null != participant.getGroupId()
                && groupId == participant.getGroupId()) {
                retval.add(participant);
            }
        }
        return retval;
    }

    /**
     * Task storage.
     */
    private static final TaskStorage storage = TaskStorage.getInstance();

    /**
     * Folder storage.
     */
    private static final FolderStorage foldStor = FolderStorage.getInstance();

    /**
     * Participant storage.
     */
    private static final ParticipantStorage partStor = ParticipantStorage
        .getInstance();

    /**
     * Stores a task with its participants and folders.
     * @param ctx Context.
     * @param task Task to store.
     * @param participants Participants of the task.
     * @param folders Folders the task should appear in.
     * @throws TaskException if an error occurs while storing the task.
     */
    static void insertTask(final Context ctx, final Task task,
        final Set<TaskParticipant> participants, final Set<Folder> folders)
        throws TaskException {
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            final int taskId = IDGenerator.getId(ctx, Types.TASK, con);
            task.setObjectID(taskId);
            storage.insertTask(ctx, con, task, StorageType.ACTIVE);
            if (participants.size() != 0) {
                partStor.insertParticipants(ctx, con, taskId, participants,
                    StorageType.ACTIVE);
            }
            foldStor.insertFolder(ctx, con, taskId, folders,
                StorageType.ACTIVE);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.INSERT_FAILED, e, e.getMessage());
        } catch (TaskException e) {
            rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                final TaskException tske = new TaskException(Code.AUTO_COMMIT,
                    e);
                LOG.error(tske.getMessage(), tske);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }
    
    /**
     * TODO Move this method to {@link UpdateData}.
     */
    static void updateTask(final Context ctx, final Task task,
        final Date lastRead, final int[] modified,
        final Set<TaskParticipant> add, final Set<TaskParticipant> remove,
        final Set<Folder> addFolder, final Set<Folder> removeFolder)
        throws TaskException {
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            final int taskId = task.getObjectID();
            storage.updateTask(ctx, con, task, lastRead, modified, StorageType
                .ACTIVE);
            if (null != add) {
                partStor.insertParticipants(ctx, con, taskId, add, StorageType
                    .ACTIVE);
                partStor.deleteParticipants(ctx, con, taskId, add, StorageType
                    .REMOVED, false);
            }
            if (null != remove) {
                partStor.insertParticipants(ctx, con, taskId, remove,
                    StorageType.REMOVED);
                partStor.deleteParticipants(ctx, con, taskId, remove,
                    StorageType.ACTIVE, true);
            }
            if (null != removeFolder) {
                foldStor.deleteFolder(ctx, con, taskId, removeFolder,
                    StorageType.ACTIVE);
            }
            if (null != addFolder) {
                foldStor.insertFolder(ctx, con, taskId, addFolder, StorageType
                    .ACTIVE);
            }
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

    /**
     * Deletes an ACTIVE task object. This stores the task as a DELETED task
     * object, deletes all reminders and sends the task delete event.
     * @param session Session.
     * @param task fully loaded task object to delete.
     * @param lastModified last modification timestamp for concurrent conflicts.
     * @throws TaskException if an exception occurs.
     */
    public static void deleteTask(final SessionObject session, final Task task,
        final Date lastModified) throws TaskException {
        final Context ctx = session.getContext();
        final int userId = session.getUserObject().getId();
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            task.setLastModified(new Date());
            task.setModifiedBy(userId);
            storage.insertTask(ctx, con, task, StorageType.DELETED);
            deleteParticipants(ctx, con, task.getObjectID());
            deleteFolder(ctx, con, task.getObjectID());
            storage.delete(ctx, con, task.getObjectID(), lastModified,
                StorageType.ACTIVE);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.DELETE_FAILED, e, e.getMessage());
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
        informDelete(session, task);
    }

    private static void deleteParticipants(final Context ctx,
        final Connection con, final int taskId) throws TaskException {
        final Set<InternalParticipant> participants =
            new HashSet<InternalParticipant>(partStor.selectInternal(ctx,
            con, taskId, StorageType.ACTIVE));
        partStor.deleteInternal(ctx, con, taskId, participants, StorageType
            .ACTIVE, true);
        final Set<InternalParticipant> removed = partStor.selectInternal(ctx,
            con, taskId, StorageType.REMOVED);
        partStor.deleteInternal(ctx, con, taskId, removed, StorageType.REMOVED,
            true);
        participants.addAll(removed);
        partStor.insertInternals(ctx, con, taskId, participants, StorageType
            .DELETED);
        final Set<ExternalParticipant> externals = partStor.selectExternal(ctx,
            con, taskId, StorageType.ACTIVE);
        partStor.insertExternals(ctx, con, taskId, externals, StorageType
            .DELETED);
        partStor.deleteExternal(ctx, con, taskId, externals, StorageType.ACTIVE,
            true);
    }

    private static void deleteFolder(final Context ctx, final Connection con,
        final int taskId) throws TaskException {
        final Set<Folder> folders = foldStor.selectFolder(ctx, con, taskId,
            StorageType.ACTIVE);
        foldStor.insertFolder(ctx, con, taskId, folders, StorageType.DELETED);
        foldStor.deleteFolder(ctx, con, taskId, folders, StorageType.ACTIVE);
    }

    /**
     * Informs other systems about a deleted task.
     * @param session Session.
     * @param task Task object.
     * @throws TaskException if an exception occurs.
     */
    static void informDelete(final SessionObject session, final Task task)
        throws TaskException {
        Reminder.deleteReminder(session.getContext(), task);
        try {
            new EventClient(session).delete(task);
        } catch (InvalidStateException e) {
            throw new TaskException(Code.EVENT, e);
        }
    }

    /**
     * Removes a task object completely.
     * @param session Session.
     * @param folderId unique identifier of the folder through that the task is
     * accessed.
     * @param taskId unique identifier of the task to remove.
     * @param type storage type of the task (only {@link StorageType#ACTIVE} or
     * {@link StorageType#DELETED}).
     * @throws TaskException if an exception occurs.
     */
    public static void removeTask(final SessionObject session,
        final Connection writeCon, final int folderId, final int taskId,
        final StorageType type) throws TaskException {
        final Context ctx = session.getContext();
        // Load the task.
        final Task task = storage.selectTask(ctx, writeCon, taskId, type);
        task.setParentFolderID(folderId);
        final Set<InternalParticipant> internal = partStor.selectInternal(ctx,
            writeCon, taskId, type);
        final Set<ExternalParticipant> external = partStor.selectExternal(ctx,
            writeCon, taskId, type);
        final Set<Folder> folders = foldStor.selectFolder(ctx, writeCon, taskId,
            type);
        final Set<TaskParticipant> parts = new HashSet<TaskParticipant>();
        parts.addAll(internal); parts.addAll(external);
        task.setParticipants(TaskLogic.createParticipants(parts));
        task.setUsers(TaskLogic.createUserParticipants(parts));
        // Now remove it.
        partStor.deleteInternal(ctx, writeCon, taskId, internal, type, true);
        if (StorageType.ACTIVE == type) {
            final Set<InternalParticipant> removed = partStor.selectInternal(
                ctx, writeCon, taskId, StorageType.REMOVED);
            partStor.deleteInternal(ctx, writeCon, taskId, removed, StorageType
                .REMOVED, true);
        }
        partStor.deleteExternal(ctx, writeCon, taskId, external, type, true);
        foldStor.deleteFolder(ctx, writeCon, taskId, folders, type);
        storage.delete(ctx, writeCon, taskId, task.getLastModified(), type);
        informDelete(session, task);
    }

    static void setConfirmation(final Context ctx, final int taskId,
        final int userId, final int confirm, final String message)
        throws TaskException {
        final InternalParticipant participant = partStor.selectInternal(ctx,
            taskId, userId, StorageType.ACTIVE);
        participant.setConfirm(confirm);
        participant.setConfirmMessage(message);
        final Task task = new Task();
        task.setObjectID(taskId);
        task.setLastModified(new Date());
        task.setModifiedBy(userId);
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            partStor.updateInternal(ctx, con, taskId, participant, StorageType
                .ACTIVE);
            TaskLogic.updateTask(ctx, task, new Date(), new int[] {
                Task.LAST_MODIFIED, Task.MODIFIED_BY }, null, null, null, null);
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                LOG.error("Problem setting auto commit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }
}
