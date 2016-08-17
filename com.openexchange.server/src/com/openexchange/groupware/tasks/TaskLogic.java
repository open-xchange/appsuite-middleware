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
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.isTransactionRollbackException;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.annotation.Nullable;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tasks.TaskParticipant.Type;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class contains logic methods for the tasks.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TaskLogic {

    /**
     * Prevent instantiation
     */
    private TaskLogic() {
        super();
    }

    /**
     * Checks if a new task is not missing any data, does not contains any wrong data and if the user is allowed to create a task in tasks
     * folder.
     *
     * @param task Task to create.
     * @param userId unique identifier of the user that wants to create the task.
     * @param permissionBits groupware permission bits of the user that wants to create the task.
     * @throws OXException if the task can't be created.
     */
    static void checkNewTask(final Task task, final int userId, final UserPermissionBits permissionBits, final Set<TaskParticipant> participants) throws OXException {
        checkMissingAttributes(task, userId);
        checkData(task);
        checkDates(task);
        checkStateAndProgress(task);
        Permission.checkDelegation(permissionBits, task.getParticipants());
        // TODO Check if creator is participant in private or shared folder
        // Maybe the owner of the shared folder is the delegator of the task.
        checkPrivateFlag(task.getPrivateFlag(), false, participants, null);
        checkParticipants(participants);
        checkRecurrence(task, null);
        checkPriority(task);
    }

    /**
     * Checks if the data of an to update task is correct.
     *
     * @param task Task object with the updated attributes.
     * @param oldTask Task object that should be updated.
     * @param user user that wants to change the task.
     * @param permissionBits groupware permission bits of the user that wants to change the task.
     * @param newParts changed participants.
     * @param oldParts participants of the original task.
     * @throws OXException if the check fails.
     */
    static void checkUpdateTask(final Task task, final Task oldTask, final User user, final UserPermissionBits permissionBits, final Set<TaskParticipant> newParts, final Set<TaskParticipant> oldParts) throws OXException {
        if (task.containsUid()) {
            if (!oldTask.getUid().equals(task.getUid())) {
                throw TaskExceptionCode.NO_UID_CHANGE.create();
            }
            task.removeUid();
        }
        if (!task.containsLastModified()) {
            task.setLastModified(new Date());
        }
        if (!task.containsModifiedBy()) {
            task.setModifiedBy(user.getId());
        }
        checkData(task);
        checkDates(task, oldTask);
        checkStateAndProgress(task);
        Permission.checkDelegation(permissionBits, task.getParticipants());
        final boolean changedParts = task.containsParticipants();
        // Only creator is allowed to set private flag.
        if (task.containsPrivateFlag() && task.getPrivateFlag() && oldTask.getCreatedBy() != user.getId()) {
            throw TaskExceptionCode.ONLY_CREATOR_PRIVATE.create();
        }
        final boolean privat = task.containsPrivateFlag() ? task.getPrivateFlag() : oldTask.getPrivateFlag();
        checkPrivateFlag(privat, changedParts, oldParts, newParts);
        // TODO Check if creator is participant in private or shared folder
        final Set<TaskParticipant> destParts = changedParts ? newParts : oldParts;
        checkParticipants(destParts);
        checkRecurrence(task, oldTask);
        checkPriority(task);
    }

    /**
     * Checks if the new task is missing some attributes so that it can't be stored to the database.
     *
     * @param task Task to create.
     * @param userId user that wants to create the task and will be therefore used as task creator if no one is defined.
     * @throws OXException if the task can't be created.
     */
    private static void checkMissingAttributes(final Task task, final int userId) throws OXException {
        if (!task.containsParentFolderID()) {
            throw TaskExceptionCode.FOLDER_IS_MISSING.create();
        }
        if (!task.containsUid()) {
            task.setUid(UUID.randomUUID().toString());
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
            task.setRecurrenceType(CalendarObject.NO_RECURRENCE);
        }
        if (!task.containsNumberOfAttachments()) {
            task.setNumberOfAttachments(0);
        }
    }

    /**
     * Checks every string attribute of a task for invalid characters.
     *
     * @param task task to check.
     * @throws OXException if a string contains invalid characters.
     */
    private static void checkData(final Task task) throws OXException {
        for (final Mapper<String> mapper : Mapping.STRING_MAPPERS) {
            if (mapper.isSet(task) && null != mapper.get(task)) {
                final String result = Check.containsInvalidChars(mapper.get(task));
                if (null != result) {
                    throw TaskExceptionCode.INVALID_DATA.create(result);
                }
            }
        }
    }

    /**
     * Checks if the start date is before the end date of a task. Some additional checks are done before. <code>null</code> dates are
     * ignored.
     *
     * @param task Task to check the dates of.
     * @throws OXException if the start date is before the end date.
     */
    private static void checkDates(final Task task) throws OXException {
        if (task.containsStartDate() && task.containsEndDate()) {
            checkDates(task.getStartDate(), task.getEndDate());
        }
    }

    private static void checkDates(final Task task, final Task oldTask) throws OXException {
        Date start = null;
        if (task.containsStartDate()) {
            start = task.getStartDate();
        } else if (oldTask.containsStartDate()) {
            start = oldTask.getStartDate();
        }
        Date end = null;
        if (task.containsEndDate()) {
            end = task.getEndDate();
        } else if (oldTask.containsEndDate()) {
            end = oldTask.getEndDate();
        }
        checkDates(start, end);
    }

    private static void checkDates(final Date start, final Date end) throws OXException {
        if (null != start && null != end && start.after(end)) {
            throw TaskExceptionCode.START_NOT_BEFORE_END.create(start, end);
        }
    }

    private static void checkStateAndProgress(final Task task) throws OXException {
        if (!task.containsPercentComplete() || !task.containsStatus()) {
            return;
        }
        final int progress = task.getPercentComplete();
        if (progress < 0 || progress > Task.PERCENT_MAXVALUE) {
            throw TaskExceptionCode.INVALID_PERCENTAGE.create(Integer.valueOf(progress));
        }
        switch (task.getStatus()) {
        case Task.NOT_STARTED:
            if (0 != progress) {
                throw TaskExceptionCode.PERCENTAGE_NOT_ZERO.create(Integer.valueOf(progress));
            }
            break;
        case Task.IN_PROGRESS:
        case Task.DEFERRED:
        case Task.WAITING:
            // Nothing to check. The progress can be everything between 0 and
            // 100.
            break;
        case Task.DONE:
            // Status DONE should not require a 100% progress.
            break;
        default:
            throw TaskExceptionCode.INVALID_TASK_STATE.create(Integer.valueOf(task.getStatus()));
        }
    }

    /**
     * This method checks if the user tries to delegate a private flagged task.
     *
     * @param privat private flag of the old or new task.
     * @param changed if updated task contains participants.
     * @param oldParts original participants of the task.
     * @param newParts changed participants of the task.
     * @throws OXException if the check fails.
     */
    private static void checkPrivateFlag(final boolean privat, final boolean changed, final Set<TaskParticipant> oldParts, final Set<TaskParticipant> newParts) throws OXException {
        if (!privat) {
            return;
        }
        if (changed) {
            if (newParts.size() > 0) {
                throw TaskExceptionCode.NO_PRIVATE_DELEGATE.create();
            }
        } else {
            if (oldParts.size() > 0) {
                throw TaskExceptionCode.NO_PRIVATE_DELEGATE.create();
            }
        }
    }

    /**
     * Checks that the creator can't be participant if the according global option isn't set.
     *
     * @param participants Participants of the task.
     * @throws OXException if the check fails.
     */
    private static void checkParticipants(final Set<TaskParticipant> participants) throws OXException {
        if (null == participants) {
            return;
        }
        checkExternal(ParticipantStorage.extractExternal(participants));
    }

    /**
     * Checks if external participants contain consistent data. Currently external participants are checked to contain an email address.
     *
     * @param participants external participants.
     * @throws OXException if an external participant does not contain an email address.
     */
    private static void checkExternal(final Set<ExternalParticipant> participants) throws OXException {
        for (final ExternalParticipant participant : participants) {
            final String mail = participant.getMail();
            if (null == mail || mail.isEmpty()) {
                throw TaskExceptionCode.EXTERNAL_WITHOUT_MAIL.create();
            }
        }
    }

    /**
     * This method checks if the necessary fields for a recurring task are defined.
     *
     * @param task Recurring task.
     * @param oldTask Original recurring task on update. Is <code>null</code> when a task is created.
     * @throws OXException if a necessary recurrence attribute is missing.
     */
    private static void checkRecurrence(Task task, @Nullable Task oldTask) throws OXException {

        // First check if the client want to set some recurrence.
        if (task.containsRecurrenceType()) {
            if (CalendarObject.NO_RECURRENCE == task.getRecurrenceType()) {
                // No recurrence is present and set to no recurrence
                return;
            }
        } else {
            if (oldTask == null || CalendarObject.NO_RECURRENCE == oldTask.getRecurrenceType()) {
                //RecurrenceType is not present in new task and either old task is not available or it is set to no recurrence
                return;
            }
        }

        // First simple checks on start and end date.
        if (CalendarObject.NO_RECURRENCE != task.getRecurrenceType()) {
            if (null == oldTask) {
                if (!task.containsStartDate()) {
                    throw TaskExceptionCode.MISSING_RECURRENCE_VALUE.create(Integer.valueOf(CalendarObject.START_DATE));
                }
                if (!task.containsEndDate()) {
                    throw TaskExceptionCode.MISSING_RECURRENCE_VALUE.create(Integer.valueOf(CalendarObject.END_DATE));
                }
            } else {
                if (task.containsStartDate() && null == task.getStartDate()) {
                    throw TaskExceptionCode.MISSING_RECURRENCE_VALUE.create(Integer.valueOf(CalendarObject.START_DATE));
                }
                if (task.containsEndDate() && null == task.getEndDate()) {
                    throw TaskExceptionCode.MISSING_RECURRENCE_VALUE.create(Integer.valueOf(CalendarObject.END_DATE));
                }
            }
        }
        // Now copy not changed attributes from original task.
        copyRecurringValues(task, oldTask);
        // Remove values for check
        boolean daysRemoved = false;
        if (CalendarObject.NO_RECURRENCE != task.getRecurrenceType() && task.containsDays() && 0 == task.getDays()) {
            daysRemoved = true;
            task.removeDays();
        }
        // Occurrences deleted?
        boolean occurrenceRemoved = false;
        if (task.containsOccurrence() && 0 == task.getOccurrence()) {
            task.removeOccurrence();
            task.setUntil(null);
            occurrenceRemoved = true;
        }
        try {
            final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class, true);
            recColl.checkRecurring(task);
        } catch (final OXException e) {
            throw e;
        }
        if (daysRemoved) {
            task.setDays(0);
        }
        if (occurrenceRemoved) {
            task.setOccurrence(0);
        }
        // Move first due date to first occurrence like appointments do.
        moveToFirstOccurrence(task);
    }

    /**
     * Verifies that the priority of tasks is only in the allowed range.
     * @param task task that priority should be tested.
     * @throws OXException if task contains an invalid priority value.
     */
    private static void checkPriority(Task task) throws OXException {
        if (task.containsPriority() && null != task.getPriority()) {
            int priority = i(task.getPriority());
            if (priority < Task.LOW || priority > Task.HIGH) {
                throw TaskExceptionCode.INVALID_PRIORITY.create(task.getPriority());
            }
        }
    }

    /**
     * If a task is updated, it only contains the changed values. If the recurrence type is changed but not the interval it must be copied
     * from the original task to be able to perform the recurrence check.
     *
     * @param task updated task.
     * @param oldTask original task.
     */
    private static void copyRecurringValues(final Task task, final Task oldTask) {
        if (null == oldTask) {
            return;
        }
        if ((task.containsRecurrenceType() && CalendarObject.NO_RECURRENCE != task.getRecurrenceType()) || (!task.containsRecurrenceType() && oldTask.containsRecurrenceType())) {
            if (!task.containsRecurrenceType()) {
                task.setRecurrenceType(oldTask.getRecurrenceType());
            }
            if (CalendarObject.DAILY == task.getRecurrenceType() || CalendarObject.WEEKLY == task.getRecurrenceType() || CalendarObject.MONTHLY == task.getRecurrenceType()) {
                if (!task.containsInterval() && oldTask.containsInterval()) {
                    task.setInterval(oldTask.getInterval());
                }
            }
            if (CalendarObject.WEEKLY == task.getRecurrenceType()) {
                if (!task.containsDays() && oldTask.containsDays()) {
                    task.setDays(oldTask.getDays());
                }
            }
            if (CalendarObject.MONTHLY == task.getRecurrenceType() || CalendarObject.YEARLY == task.getRecurrenceType()) {
                if (!task.containsDayInMonth() && oldTask.containsDayInMonth()) {
                    task.setDayInMonth(oldTask.getDayInMonth());
                }
            }
            if (CalendarObject.YEARLY == task.getRecurrenceType()) {
                if (!task.containsMonth() && oldTask.containsMonth()) {
                    task.setMonth(oldTask.getMonth());
                }
            }
        }
    }

    private static void moveToFirstOccurrence(final Task task) throws OXException {
        // WebDAV/XML sets null values if value is missing.
        if (!task.containsStartDate() || !task.containsEndDate() || null == task.getStartDate() || null == task.getEndDate() || !task.containsRecurrenceType() || CalendarObject.NO_RECURRENCE == task.getRecurrenceType()) {
            return;
        }

        task.setRecurrenceCalculator((int) ((task.getEndDate().getTime() - task.getStartDate().getTime()) / (Constants.MILLI_DAY)));
        final CalendarCollectionService service = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        final RecurringResultsInterface results = service.calculateRecurring(task, 0, 0, 1);
        if (null == results || 0 == results.size()) {
            return;
        }
        final RecurringResultInterface result = results.getRecurringResult(0);
        if (!new Date(result.getStart()).equals(task.getStartDate())) {
            task.setStartDate(new Date(result.getStart()));
        }
        if (!new Date(result.getEnd()).equals(task.getEndDate())) {
            task.setEndDate(new Date(result.getEnd()));
        }
    }

    /**
     * @return all participants who belong to a group.
     * @throws OXException
     */
    static Set<InternalParticipant> getGroupParticipants(Context ctx, Participant[] participants) throws OXException {
        final Set<InternalParticipant> retval = new HashSet<InternalParticipant>();
        for (final Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.GROUP:
                final GroupParticipant group = (GroupParticipant) participant;
                final int[] member = GroupStorage.getInstance().getGroup(group.getIdentifier(), ctx).getMember();
                if (member.length == 0) {
                    throw TaskExceptionCode.GROUP_IS_EMPTY.create(group.getDisplayName());
                }
                for (final int userId : member) {
                    retval.add(new InternalParticipant(new UserParticipant(userId), I(group.getIdentifier())));
                }
                break;
            case Participant.USER:
            case Participant.EXTERNAL_USER:
                break;
            default:
                throw TaskExceptionCode.UNKNOWN_PARTICIPANT.create(Integer.valueOf(participant.getType()));
            }
        }
        return retval;
    }

    /**
     * Creates task participant set from the user and group participants.
     *
     * @param ctx Context.
     * @param participants user and group participants.
     * @return a set of task participants.
     * @throws OXException if resolving of groups to users fails.
     */
    static Set<TaskParticipant> createParticipants(Context ctx, Participant[] participants) throws OXException {
        final Set<TaskParticipant> retval = new HashSet<TaskParticipant>();
        if (null == participants) {
            return retval;
        }
        for (final Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.USER:
                retval.add(new InternalParticipant((UserParticipant) participant, null));
                break;
            case Participant.GROUP:
                final GroupParticipant group = (GroupParticipant) participant;
                final int[] member = GroupStorage.getInstance().getGroup(group.getIdentifier(), ctx).getMember();
                if (member.length == 0) {
                    throw TaskExceptionCode.GROUP_IS_EMPTY.create(group.getDisplayName());
                }
                for (final int userId : member) {
                    final InternalParticipant tParticipant = new InternalParticipant(new UserParticipant(userId), I(group.getIdentifier()));
                    // Prefer the single added participant before being a group member
                    if (!retval.contains(tParticipant)) {
                        retval.add(tParticipant);
                    }
                }
                break;
            case Participant.EXTERNAL_USER:
                retval.add(new ExternalParticipant((ExternalUserParticipant) participant));
                break;
            default:
                throw TaskExceptionCode.UNKNOWN_PARTICIPANT.create(Integer.valueOf(participant.getType()));
            }
        }
        return retval;
    }

    static UserParticipant[] createUserParticipants(final Set<TaskParticipant> participants) {
        final List<Participant> retval = new ArrayList<Participant>(participants.size());
        for (final TaskParticipant participant : participants) {
            if (Type.INTERNAL == participant.getType()) {
                final InternalParticipant internal = (InternalParticipant) participant;
                retval.add(internal.getUser());
            }
        }
        return retval.toArray(new UserParticipant[retval.size()]);
    }

    static Participant[] createParticipants(final Set<TaskParticipant> participants) {
        final List<Participant> retval = new ArrayList<Participant>();
        final Map<Integer, Participant> groups = new HashMap<Integer, Participant>();
        for (final TaskParticipant participant : participants) {
            switch (participant.getType()) {
            case INTERNAL:
                final InternalParticipant internal = (InternalParticipant) participant;
                final Integer groupId = internal.getGroupId();
                if (null == groupId) {
                    retval.add(internal.getUser());
                } else {
                    final GroupParticipant group = new GroupParticipant(groupId.intValue());
                    if (!groups.containsKey(groupId)) {
                        groups.put(groupId, group);
                    }
                }
                break;
            case EXTERNAL:
                final ExternalParticipant external = (ExternalParticipant) participant;
                retval.add(external.getExternal());
                break;
            default:
                break;
            }
        }
        retval.addAll(groups.values());
        return retval.toArray(new Participant[retval.size()]);
    }

    static Set<Folder> createFolderMapping(final Set<InternalParticipant> participants) {
        return createFolderMapping(-1, -1, participants);
    }

    /**
     * Creates the folder mappings for a task.
     *
     * @param folderId unique identifier of the folder that is the main folder of the task.
     * @param userId unique identifier of the user who created the task. The primary folder of the task will be added with this user
     *            identifier. On shared folders the user identifier should be the owner of the shared folder.
     * @param participants the task will also be mapped into this users private folder.
     * @return the ready to insert folder mapping.
     */
    static Set<Folder> createFolderMapping(final int folderId, final int userId, final Set<InternalParticipant> participants) {
        final Set<Folder> retval = new HashSet<Folder>();
        if (-1 != userId) {
            retval.add(new Folder(folderId, userId));
        }
        for (final InternalParticipant participant : participants) {
            if (participant.getIdentifier() == userId) {
                continue;
            }
            final Folder folder = new Folder(participant.getFolderId(), participant.getIdentifier());
            if (!retval.contains(folder)) {
                retval.add(folder);
            }
        }
        return retval;
    }

    static int[] findModifiedFields(final Task oldTask, final Task task) {
        final List<Integer> fields = new ArrayList<Integer>();
        for (final Mapper<?> mapper : Mapping.MAPPERS) {
            if (mapper.isSet(task) && (!mapper.isSet(oldTask) || !mapper.equals(task, oldTask))) {
                fields.add(Integer.valueOf(mapper.getId()));
            }
        }

        // If the end of recurrence information is changed from 'after x occurrences' to 'at *date*'
        // the field recurrence_count has explicitly to be unset.
        if (oldTask.containsOccurrence() && !task.containsOccurrence() && task.containsUntil()) {
            fields.add(I(CalendarObject.RECURRENCE_COUNT));
        }

        final int[] retval = new int[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            retval[i] = fields.get(i).intValue();
        }
        return retval;
    }

    /**
     * Calculates the next time values for a recurring task.
     *
     * @param task the task object.
     * @return <code>true</code> if a next occurence exists.
     * @throws OXException if an error occurs.
     */
    public static boolean makeRecurrence(final Task task) throws OXException {
        if (task.containsOccurrence() && 0 == task.getOccurrence()) {
            return false;
        }
        if (!task.containsStartDate() || null == task.getStartDate() || !task.containsEndDate() || null == task.getEndDate()) {
            // Workaround for tasks that could have been created due to bug 38782 having a recurrence but lack start or end date.
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
        task.removeDateCompleted();
        if (task.containsOccurrence()) {
            task.setOccurrence(task.getOccurrence() - 1);
        }
        return true;
    }

    /**
     * This method does compatability changes to the task in that way that the recurrence calculation for appointment also works for tasks.
     *
     * @param task Task to modify and to calculate the recurrence for.
     * @return the new start and end date for the task.
     * @throws OXException if an error occurs.
     */
    private static Date[] calculateRecurring(final Task task) throws OXException {
        final Date origStart = task.getStartDate();
        task.setRecurrenceCalculator((int) ((task.getEndDate().getTime() - origStart.getTime()) / (Constants.MILLI_DAY)));
        // Setting until date to Long.MAX_VALUE is not a good idea.
        // Recurring calculation sets until date itself and may add some time
        // in some conditions cause an overflow if MAX_VALUE is set and no
        // new recurrence is calculated.
        final CalendarCollectionService recColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
        final RecurringResultsInterface rr = recColl.calculateRecurring(task, 0, 0, 2);
        final RecurringResultInterface result = rr.getRecurringResult(0);
        final Date[] retval;
        if (null == result) {
            retval = new Date[0];
        } else {
            retval = new Date[] { new Date(result.getStart()), new Date(result.getEnd()) };
        }
        return retval;
    }

    /**
     * Extracts all participants that are added by the group.
     *
     * @param participants internal task participants.
     * @param groupId the group identifier.
     * @return All participants that are added by the group.
     */
    static Set<InternalParticipant> extractWithGroup(final Set<InternalParticipant> participants, final int groupId) {
        final Set<InternalParticipant> retval = new HashSet<InternalParticipant>();
        for (final InternalParticipant participant : participants) {
            if (null != participant.getGroupId() && groupId == participant.getGroupId().intValue()) {
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
    private static final ParticipantStorage partStor = ParticipantStorage.getInstance();

    /**
     * Deletes an ACTIVE task object. This stores the task as a DELETED task object, deletes all reminders and sends the task delete event.
     *
     * @param session Session.
     * @param task fully loaded task object to delete.
     * @param lastModified last modification timestamp for concurrent conflicts.
     * @throws OXException if an exception occurs.
     */
    public static void deleteTask(final Session session, final Context ctx, final int userId, final Task task, final Date lastModified) throws OXException {
        try {
            final DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                final Connection con = DBPool.pickupWriteable(ctx);
                condition.resetTransactionRollbackException();
                try {
                    DBUtils.startTransaction(con);
                    final Task t = clone(task);
                    deleteTask(ctx, con, userId, t, lastModified);
                    Reminder.deleteReminder(ctx, con, task);
                    con.commit();
                    informDelete(session, t);
                } catch (final SQLException e) {
                    rollback(con);
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw TaskExceptionCode.DELETE_FAILED.create(e, e.getMessage());
                    }
                } catch (final OXException e) {
                    rollback(con);
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw e;
                    }
                } catch (final RuntimeException e) {
                    rollback(con);
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw TaskExceptionCode.DELETE_FAILED.create(e, e.getMessage());
                    }
                } finally {
                    autocommit(con);
                    DBPool.closeWriterSilent(ctx, con);
                }
            } while (condition.checkRetry());
        } catch (final SQLException e) {
            if (isTransactionRollbackException(e)) {
                throw TaskExceptionCode.DELETE_FAILED_RETRY.create(e, e.getMessage());
            }
            throw TaskExceptionCode.DELETE_FAILED.create(e, e.getMessage());
        }
    }

    private static final int[] ALL_COLUMNS = Task.ALL_COLUMNS;

    static Task clone(final Task task) {
        final Task ret = new Task();
        for (final int field : ALL_COLUMNS) {
            if (task.contains(field)) {
                ret.set(field, task.get(field));
            }
        }
        return ret;
    }

    private static Set<Folder> deleteParticipants(final Context ctx, final Connection con, final int taskId) throws OXException {
        final Set<InternalParticipant> participants = new HashSet<InternalParticipant>(partStor.selectInternal(ctx, con, taskId, ACTIVE));
        partStor.deleteInternal(ctx, con, taskId, participants, ACTIVE, true);
        final Set<InternalParticipant> removed = partStor.selectInternal(ctx, con, taskId, REMOVED);
        partStor.deleteInternal(ctx, con, taskId, removed, REMOVED, true);
        final Set<Folder> retval = TaskLogic.createFolderMapping(removed);
        participants.addAll(removed);
        partStor.insertInternals(ctx, con, taskId, participants, DELETED);
        final Set<ExternalParticipant> externals = partStor.selectExternal(ctx, con, taskId, ACTIVE);
        partStor.insertExternals(ctx, con, taskId, externals, DELETED);
        partStor.deleteExternal(ctx, con, taskId, externals, ACTIVE, true);
        return retval;
    }

    private static void deleteFolder(final Context ctx, final Connection con, final int taskId, final Set<Folder> removed) throws OXException {
        final Set<Folder> folders = foldStor.selectFolder(ctx, con, taskId, ACTIVE);
        foldStor.deleteFolder(ctx, con, taskId, folders, ACTIVE);
        for (Folder folder : removed) {
            if (folder.getIdentifier() > 0) {
                folders.add(folder);
            }
        }
        foldStor.insertFolder(ctx, con, taskId, folders, DELETED);
    }

    /**
     * Informs other systems about a deleted task.
     *
     * @param session Session.
     * @param task Task object.
     * @throws OXException if an exception occurs.
     */
    static void informDelete(Session session, Task task) throws OXException {
        try {
            new EventClient(session).delete(task);
        } catch (final OXException e) {
            throw e;
        }
    }

    /**
     * Removes a task object completely.
     *
     * @param session Session (for event handling)
     * @param ctx Context.
     * @param con writable database connection.
     * @param folderId unique identifier of the folder through that the task is accessed.
     * @param taskId unique identifier of the task to remove.
     * @param type storage type of the task (only {@link StorageType#ACTIVE} or {@link StorageType#DELETED}).
     * @throws OXException if an exception occurs.
     */
    public static void removeTask(final Session session, final Context ctx, final Connection con, final int folderId, final int taskId, final StorageType type) throws OXException {
        // Load the task.
        final Task task = storage.selectTask(ctx, con, taskId, type);
        task.setParentFolderID(folderId);
        final Set<InternalParticipant> internal = partStor.selectInternal(ctx, con, taskId, type);
        final Set<ExternalParticipant> external = partStor.selectExternal(ctx, con, taskId, type);
        final Set<Folder> folders = foldStor.selectFolder(ctx, con, taskId, type);
        final Set<TaskParticipant> parts = new HashSet<TaskParticipant>();
        parts.addAll(internal);
        parts.addAll(external);
        task.setParticipants(TaskLogic.createParticipants(parts));
        task.setUsers(TaskLogic.createUserParticipants(parts));

        // Now remove it.
        partStor.deleteInternal(ctx, con, taskId, internal, type, true);
        if (ACTIVE == type) {
            final Set<InternalParticipant> removed = partStor.selectInternal(ctx, con, taskId, REMOVED);
            partStor.deleteInternal(ctx, con, taskId, removed, REMOVED, true);
        }
        partStor.deleteExternal(ctx, con, taskId, external, type, true);
        foldStor.deleteFolder(ctx, con, taskId, folders, type);
        storage.delete(ctx, con, taskId, task.getLastModified(), type);
        Reminder.deleteReminder(ctx, con, task);
        if (ACTIVE == type) {
            informDelete(session, task);
        }
    }

    /**
     * Deletes an ACTIVE task object. This stores the task as a DELETED task object, deletes all reminders and sends the task delete event.
     *
     * @param ctx Context
     * @param task fully loaded task object to delete.
     * @param lastModified last modification timestamp for concurrent conflicts.
     * @throws OXException if an exception occurs.
     */
    public static void deleteTask(final Context ctx, final Connection con, final int userId, final Task task, final Date lastModified) throws OXException {
        final int taskId = task.getObjectID();
        // Load the folders remembering all task source folders on move
        // operations for clients.
        final Set<Folder> movedSourceFolders = foldStor.selectFolder(ctx, con, taskId, DELETED);
        // Delete them to be able to remove the dummy task for them.
        foldStor.deleteFolder(ctx, con, taskId, movedSourceFolders, DELETED, true);
        // Delete dummy task.
        storage.delete(ctx, con, taskId, new Date(Long.MAX_VALUE), DELETED, false);
        // Move task to delete to deleted tables.
        task.setLastModified(new Date());
        task.setModifiedBy(userId);
        storage.insertTask(ctx, con, task, DELETED, TaskStorage.TOMBSTONE_ATTRS);
        final Set<Folder> removed = deleteParticipants(ctx, con, task.getObjectID());
        deleteFolder(ctx, con, task.getObjectID(), removed);
        storage.delete(ctx, con, task.getObjectID(), lastModified, ACTIVE);
        // Insert the folders remembering all task source folders on move
        // operations.
        foldStor.insertFolder(ctx, con, taskId, movedSourceFolders, DELETED);
    }

}
