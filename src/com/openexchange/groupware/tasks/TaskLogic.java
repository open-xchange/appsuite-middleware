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

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;

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
import com.openexchange.groupware.Types;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.TaskParticipant.Type;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderTools;

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
     * Prevent instanciation.
     */
    private TaskLogic() {
        super();
    }

    /**
     * Checks if the folder is a tasks folder and if the user is allowed to
     * create object in that folder.
     * @param session Session.
     * @param folderId unique identifier of the folder.
     * @throws TaskException if an error occurs or the user is not allowed to
     * create object in that folder.
     */
    static void checkCreateInFolder(final SessionObject session,
        final int folderId) throws TaskException {
        final UserConfiguration config = session.getUserConfiguration();
        final User user = session.getUserObject();
        if (!config.hasTask()) {
            throw new TaskException(Code.NO_TASKS, user.getId());
        }
        final Context ctx = session.getContext();
        if (!Tools.isFolderTask(ctx, folderId)) {
            throw new TaskException(Code.NOT_TASK_FOLDER, "", folderId);
        }
        final OCLPermission permission;
        try {
            permission = OXFolderTools.getEffectiveFolderOCL(folderId,
                user.getId(), user.getGroups(), ctx, config);
        } catch (OXException e) {
            throw new TaskException(e);
        }
        if (!permission.canCreateObjects()) {
            throw new TaskException(Code.NO_CREATE_PERMISSION, "", folderId);
        }
    }

    /**
     * Checks if the folder is a tasks folder and if the user is allowed to read
     * objects in that folder.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param groups Groups the user belongs to.
     * @param userConfig Groupware configuration of the user.
     * @param folderId unique identifier of the folder.
     * @return <code>true</code> if only private objects can be seen.
     * @throws TaskException if the reading is not okay.
     */
    static boolean canReadInFolder(final Context ctx, final int userId,
        final int[] groups, final UserConfiguration userConfig,
        final int folderId) throws TaskException {
        final FolderObject folder = Tools.getFolder(ctx, folderId);
        return canReadInFolder(ctx, userId, groups, userConfig, folder);
    }

    /**
     * Checks if the folder is a tasks folder and if the user is allowed to read
     * objects in that folder.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param groups Groups the user belongs to.
     * @param userConfig groupware configuration of the user.
     * @param folder folder object that should be tested for read access.
     * @return <code>true</code> if only private objects can be seen.
     * @throws TaskException if the reading is not okay.
     */
    static boolean canReadInFolder(final Context ctx, final int userId,
        final int[] groups, final UserConfiguration userConfig,
        final FolderObject folder) throws TaskException {
        if (!Tools.isFolderTask(ctx, folder.getObjectID())) {
            throw new TaskException(Code.NOT_TASK_FOLDER,
                folder.getFolderName(), folder.getObjectID());
        }
        final OCLPermission permission;
        try {
            permission = OXFolderTools.getEffectiveFolderOCL(
                folder.getObjectID(), userId, groups, ctx, userConfig);
        } catch (OXException e) {
            throw new TaskException(e);
        }
        if (!permission.canReadAllObjects() && !permission
            .canReadOwnObjects()) {
            throw new TaskException(Code.NO_READ_PERMISSION,
                folder.getFolderName(), folder.getObjectID());
        }
        final boolean onlyOwn = !permission.canReadAllObjects() && permission
            .canReadOwnObjects();
        return onlyOwn;
    }

    /**
     * Checks if the user is allowed to read the task.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param groups Groups the user belongs to.
     * @param userConfig groupware access rights of the user.
     * @param folderId unique identifier of the folder.
     * @param taskCreator unique identifier of the user who created the task.
     * @throws TaskException if the reading is not okay.
     */
    static void canReadInFolder(final Context ctx, final int userId,
        final int[] groups, final UserConfiguration userConfig,
        final int folderId, final int taskCreator) throws TaskException {
        final FolderObject folder = Tools.getFolder(ctx, folderId);
        canReadInFolder(ctx, userId, groups, userConfig, folder, taskCreator);
    }

    /**
     * Checks if the user is allowed to read the task.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param groups Groups the user belongs to.
     * @param userConfig groupware access rights of the user.
     * @param folder folder object that should be tested for read access.
     * @param taskCreator unique identifier of the user who created the task.
     * @throws TaskException if the reading is not okay.
     */
    static void canReadInFolder(final Context ctx, final int userId,
        final int[] groups, final UserConfiguration userConfig,
        final FolderObject folder, final int taskCreator)
        throws TaskException {
        final boolean onlyOwn = canReadInFolder(ctx, userId, groups,
            userConfig, folder);
        if (onlyOwn && (userId != taskCreator)) {
            throw new TaskException(Code.NO_READ_PERMISSION,
                folder.getFolderName(), folder.getObjectID());
        }
    }

    /**
     * Checks if a user is allowed to update a task.
     * @param ctx Context.
     * @param user User.
     * @param userConfiguration Groupware configuration of the user.
     * @param task Task to update.
     * @throws TaskException if the task can't be updated.
     */
    static void checkWriteInFolder(final Context ctx, final User user,
        final UserConfiguration userConfiguration, final Task task)
        throws TaskException {
        final OCLPermission permission;
        final int folderId = task.getParentFolderID();
        try {
            permission = OXFolderTools.getEffectiveFolderOCL(folderId,
                user.getId(), user.getGroups(), ctx, userConfiguration);
        } catch (OXException e) {
            throw new TaskException(e);
        }
        if (!permission.canWriteAllObjects()
            && !(permission.canWriteOwnObjects()
                && (user.getId() == task.getCreatedBy()))) {
            final FolderObject folder = Tools.getFolder(ctx, folderId);
            throw new TaskException(Code.NO_WRITE_PERMISSION,
                folder.getFolderName(), folderId);
        }
    }

    static void checkWriteInFolder(final Context ctx, final int userId,
        final int[] groups, final UserConfiguration userConfig,
        final int folderId, final int taskCreator)
        throws TaskException {
        final OCLPermission permission;
        try {
            permission = OXFolderTools.getEffectiveFolderOCL(folderId, userId,
                groups, ctx, userConfig);
        } catch (OXException e) {
            throw new TaskException(e);
        }
        if (!permission.canWriteAllObjects()
            && !(permission.canWriteOwnObjects() && (userId == taskCreator))) {
            final FolderObject folder = Tools.getFolder(ctx, folderId);
            throw new TaskException(Code.NO_WRITE_PERMISSION,
                folder.getFolderName(), folderId);
        }
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
        checkDelegation(userConfig, task.getParticipants());
        // TODO Check if creator is participant in private or shared folder
        // Maybe the owner of the shared folder is the delegator of the task.
        checkParticipants(participants, task.getPrivateFlag(),
            task.getCreatedBy());
        checkRecurrence(task, null);
    }

    /**
     * Checks if the data of an to update task is correct.
     * @param task Task object with the updated attributes.
     * @param oldTask Task object that should be updated.
     * @param userId unique identifier of the user that wants to change the
     * task.
     * @param userConfig groupware configuration of the user that wants to
     * change the task.
     * @param participants 
     * @throws TaskException if the check fails.
     */
    static void checkUpdateTask(final Task task, final Task oldTask,
        final int userId, final UserConfiguration userConfig,
        final Set<TaskParticipant> participants) throws TaskException {
        if (!task.containsLastModified()) {
            task.setLastModified(new Date());
        }
        if (!task.containsModifiedBy()) {
            task.setModifiedBy(userId);
        }
        checkDates(task);
        checkStateAndProgress(task);
        checkDelegation(userConfig, task.getParticipants());
        // TODO Check if creator is participant in private or shared folder
        final boolean privateFlag = task.containsPrivateFlag() ? task
            .getPrivateFlag() : oldTask.getPrivateFlag();
        checkParticipants(participants, privateFlag, oldTask.getCreatedBy());
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
     * Checks if the user is allowed to delegate tasks.
     * @param userConfig groupware configuration of the user.
     * @param participants Participants of a task.
     * @throws TaskException if delegation is not allowed.
     */
    private static void checkDelegation(final UserConfiguration userConfig,
        final Participant[] participants) throws TaskException {
        if (!userConfig.canDelegateTasks()
            && null != participants && participants.length > 0) {
            throw new TaskException(Code.NO_DELEGATE_PERMISSION);
        }
    }

    /**
     * Checks that the creator can't be participant if the according global
     * option isn't set. This method also checks if the user tries to delegate
     * a private flagged task.
     * @param participants Participants of the task.
     * @param privateFlag private flag of the task.
     * @param creator unique identifier of the user that created the task.
     * @throws TaskException if the check fails.
     */
    private static void checkParticipants(
        final Set<TaskParticipant> participants, final boolean privateFlag,
        final int creator) throws TaskException {
        if (null != participants) {
            if (participants.size() > 0 && privateFlag) {
                throw new TaskException(Code.NO_PRIVATE_DELEGATE);
            }
            if (Configuration.isNoCreatorToParticipants()) {
                for (TaskParticipant participant : participants) {
                    switch (participant.getType()) {
                    case INTERNAL:
                        final TaskInternalParticipant internal =
                            (TaskInternalParticipant) participant;
                        if (internal.getIdentifier() == creator) {
                            throw new TaskException(Code
                                .NO_CREATOR_PARTICIPANT);
                        }
                        break;
                    case EXTERNAL:
                        final TaskExternalParticipant external =
                            (TaskExternalParticipant) participant;
                        final String mail = external.getMail();
                        if (mail == null || mail.length() == 0) {
                            throw new TaskException(Code.EXTERNAL_WITHOUT_MAIL);
                        }
                    }
                }
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
                retval.add(new TaskInternalParticipant(
                    (UserParticipant) participant, null));
                break;
            case Participant.GROUP:
                final GroupParticipant group = (GroupParticipant) participant;
                try {
                    final int[] member = GroupStorage.getInstance(ctx).getGroup(
                        group.getIdentifier()).getMember();
                    for (int userId : member) {
                        final UserParticipant user = new UserParticipant();
                        user.setIdentifier(userId);
                        final TaskParticipant tParticipant =
                            new TaskInternalParticipant(user,
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
                retval.add(new TaskExternalParticipant(
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
                final TaskInternalParticipant internal =
                    (TaskInternalParticipant) participant;
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
                final TaskInternalParticipant internal =
                    (TaskInternalParticipant) participant;
                final Integer groupId = internal.getGroupId();
                if (null == groupId) {
                    retval.add(internal.getUser());
                } else {
                    final GroupParticipant group = new GroupParticipant();
                    group.setIdentifier(groupId);
                    if (!groups.containsKey(groupId)) {
                        groups.put(groupId, group);
                    }
                }
                break;
            case EXTERNAL:
                final TaskExternalParticipant external =
                    (TaskExternalParticipant) participant;
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
        final Set<TaskInternalParticipant> participants) {
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
        final Set<TaskInternalParticipant> participants) {
        final Set<Folder> retval = new HashSet<Folder>();
        if (-1 != userId) {
            retval.add(new Folder(folderId, userId));
        }
        for (TaskInternalParticipant participant : participants) {
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

    static Folder getFolder(final Set<Folder> folders, final int folderId) {
        Folder retval = null;
        for (Folder folder : folders) {
            if (folder.getIdentifier() == folderId) {
                retval = folder;
                break;
            }
        }
        return retval;
    }

    /**
     * @param folders Set of task folder mappings.
     * @param userId unique identifier of a user.
     * @return the folder mapping for the user.
     */
    static Folder getFolderOfUser(final Set<Folder> folders, final int userId) {
        Folder retval = null;
        for (Folder folder : folders) {
            if (folder.getUser() == userId) {
                retval = folder;
                break;
            }
        }
        return retval;
    }

    static TaskInternalParticipant getParticipant(
        final Set<TaskInternalParticipant> participants, final int userId) {
        TaskInternalParticipant retval = null;
        for (TaskInternalParticipant participant : participants) {
            if (participant.getIdentifier() == userId) {
                retval = participant;
                break;
            }
        }
        return retval;
    }

    /**
     * Extracts all participants that are added by the group.
     * @param participants internal task participants.
     * @param groupId the group identifier.
     * @return All participants that are added by the group.
     */
    static Set<TaskInternalParticipant> extractWithGroup(
        final Set<TaskInternalParticipant> participants, final int groupId) {
        final Set<TaskInternalParticipant> retval =
            new HashSet<TaskInternalParticipant>();
        for (TaskInternalParticipant participant : participants) {
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
     * Does all actions to load a complete task object.
     * @param ctx Context.
     * @param folderId unique identifier of the folder through that the task is
     * accessed.
     * @param taskId unique identifier of the task to load.
     * @param type storage type of the task to load.
     * @return the task object.
     * @throws TaskException if an exception occurs.
     */
    public static Task loadTask(final Context ctx, final int folderId,
        final int taskId, final StorageType type) throws TaskException {
        final Task retval = storage.selectTask(ctx, taskId, type);
        retval.setParentFolderID(folderId);
        final Set<TaskParticipant> parts = loadParticipantsWithFolder(ctx,
            folderId, taskId, type);
        retval.setParticipants(TaskLogic.createParticipants(parts));
        retval.setUsers(TaskLogic.createUserParticipants(parts));
        return retval;
    }

    /**
     * Loads participants of a task. If the task is in a public folder no folder
     * mapping for the participants will be loaded.
     * @param ctx Context.
     * @param folderId Unique identifier of the folder through that the task is
     * accessed.
     * @param taskId Unique identifier of the task.
     * @param type storage type of the participants (only ACTIVE or DELETED).
     * @return the loaded participants.
     * @throws TaskException if an exception occurs.
     */
    static Set<TaskParticipant> loadParticipantsWithFolder(final Context ctx,
        final int folderId, final int taskId, final StorageType type)
        throws TaskException {
        final Set<TaskParticipant> parts = storage.selectParticipants(ctx,
            taskId, type);
        // TODO Use FolderObject
        if (!Tools.isFolderPublic(ctx, folderId)) {
            final Set<Folder> folders = foldStor.selectFolder(ctx, taskId,
                type);
            Tools.fillStandardFolders(parts, folders, true);
        }
        return parts;
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
        storage.delete(ctx, task, userId, lastModified);
        informDelete(session, task);
    }

    /**
     * Informs other systems about a deleted task.
     * @param session Session.
     * @param task Task object.
     * @throws TaskException if an exception occurs.
     */
    static void informDelete(final SessionObject session, final Task task)
        throws TaskException {
        final ReminderSQLInterface reminder = new ReminderHandler(session
            .getContext());
        try {
            reminder.deleteReminder(task.getObjectID(), Types.TASK);
        } catch (OXObjectNotFoundException e) {
            // If the task does not have a reminder this exception is
            // thrown. Which is quite okay because not every task must have
            // a reminder.
        	if (LOG.isTraceEnabled()) { // Added to remove PMD warning about an empty catch clause
        		LOG.trace(e.getMessage(), e);
        	}
        } catch (OXException e) {
            throw new TaskException(e);
        }
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
        final int folderId, final int taskId, final StorageType type)
        throws TaskException {
        final Context ctx = session.getContext();
        // Load the task.
        final Task task = storage.selectTask(ctx, taskId, type);
        task.setParentFolderID(folderId);
        final Set<TaskInternalParticipant> internal = partStor.selectInternal(
            ctx, taskId, type);
        final Set<TaskExternalParticipant> external = partStor.selectExternal(
            ctx, taskId, type);
        final Set<Folder> folders = foldStor.selectFolder(ctx, taskId, type);
        final Set<TaskParticipant> parts = new HashSet<TaskParticipant>();
        parts.addAll(internal); parts.addAll(external);
        task.setParticipants(TaskLogic.createParticipants(parts));
        task.setUsers(TaskLogic.createUserParticipants(parts));
        // Now remove it.
        Connection con;
        try {
            con = DBPool.pickupWriteable(ctx);
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
        try {
            con.setAutoCommit(false);
            partStor.deleteInternal(ctx, con, taskId, internal, type, true);
            if (StorageType.ACTIVE == type) {
                final Set<TaskInternalParticipant> removed = partStor
                    .selectInternal(ctx, con, taskId, StorageType.REMOVED);
                partStor.deleteInternal(ctx, con, taskId, removed,
                    StorageType.REMOVED, true);
            }
            partStor.deleteExternal(ctx, con, taskId, external, type, true);
            foldStor.deleteFolder(ctx, con, taskId, folders, type);
            storage.delete(ctx, con, taskId, task.getLastModified(), type);
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } catch (TaskException e) {
            rollback(con);
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                final TaskException exc = new TaskException(Code.SQL_ERROR, e,
                    e.getMessage());
                LOG.error(exc.getMessage(), e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
        informDelete(session, task);
    }
}
