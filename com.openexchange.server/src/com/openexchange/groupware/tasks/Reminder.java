/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.tasks;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.Collections;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.User;

/**
 * This class contains everything for handling reminder for tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class Reminder {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Reminder.class);

    /**
     * Prevent instantiation.
     */
    private Reminder() {
        super();
    }

    /**
     * Creates a reminder.
     * @param ctx Context.
     * @param task reminder will be created for this task.
     * @throws OXException if inserting the reminder fails.
     */
    static void createReminder(final Context ctx, final Task task)
        throws OXException {
        final ReminderObject remind = new ReminderObject();
        remind.setDate(task.getAlarm());
        remind.setModule(Types.TASK);
        remind.setTargetId(task.getObjectID());
        remind.setFolder(task.getParentFolderID());
        remind.setUser(task.getCreatedBy());
        final ReminderSQLInterface reminder = ReminderHandler.getInstance();
        reminder.insertReminder(remind, ctx);
    }

    static void fixAlarm(Context ctx, Task task, Set<TaskParticipant> removed, Set<TaskParticipant> participants, Set<Folder> folders) throws OXException {
        final ReminderSQLInterface reminder = ReminderHandler.getInstance();
        final int taskId = task.getObjectID();
        for (InternalParticipant participant : ParticipantStorage.extractInternal(removed)) {
            final int userId = participant.getIdentifier();
            if (reminder.existsReminder(taskId, userId, Types.TASK, ctx)) {
                reminder.deleteReminder(taskId, userId, Types.TASK, ctx);
            }
        }
        for (InternalParticipant participant : ParticipantStorage.extractInternal(participants)) {
            final int userId = participant.getIdentifier();
            if (reminder.existsReminder(taskId, userId, Types.TASK, ctx)) {
                final ReminderObject remind = reminder.loadReminder(taskId, userId, Types.TASK, ctx);
                final int folderId = getFolderId(participant, folders);
                try {
                    if (remind.getFolder() != folderId) {
                        remind.setFolder(folderId);
                        reminder.updateReminder(remind, ctx);
                    }
                } catch (NumberFormatException nfe) {
                    LOG.error("Parsing reminder folder identifier failed.", nfe);
                }
            }
        }
    }

    private static int getFolderId(InternalParticipant participant, Set<Folder> folders) {
        if (-1 == participant.getFolderId()) {
            // task in public folder
            if (1 != folders.size()) {
                return -1;
            }
            return folders.iterator().next().getIdentifier();
        }
        return participant.getFolderId();
    }

    /**
     * Updates the alarm for a task.
     * @param ctx Context.
     * @param task Task.
     * @param user User.
     * @throws OXException if the update of the alarm makes problems.
     */
    static void updateAlarm(final Context ctx,
        final Task task, final User user)
        throws OXException {
        final ReminderSQLInterface reminder = ReminderHandler.getInstance();
        final int taskId = task.getObjectID();
        final int userId = user.getId();
        if (null == task.getAlarm()) {
            if (reminder.existsReminder(taskId, userId, Types.TASK, ctx)) {
                reminder.deleteReminder(taskId, userId, Types.TASK, ctx);
            }
        } else {
            final ReminderObject remind = new ReminderObject();
            remind.setDate(task.getAlarm());
            remind.setModule(Types.TASK);
            remind.setTargetId(taskId);
            remind.setFolder(task.getParentFolderID());
            remind.setUser(userId);
            if (reminder.existsReminder(taskId, userId, Types.TASK, ctx)) {
                reminder.updateReminder(remind, ctx);
            } else {
                reminder.insertReminder(remind, ctx);
            }
        }
    }

    /**
     * Loads reminder for a user and several tasks.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param tasks load reminder for this tasks.
     * @throws OXException if an error occurs.
     */
    static void loadReminder(final Context ctx, final int userId,
        final Collection<Task> tasks) throws OXException {
        final ReminderSQLInterface remStor = ReminderHandler.getInstance();
        final Map<Integer, Task> tmp = new HashMap<Integer, Task>();
        for (final Task task : tasks) {
            tmp.put(Integer.valueOf(task.getObjectID()), task);
        }
        final ReminderObject[] reminders;
        try {
            reminders = remStor.loadReminder(Collections.toArray(tmp.keySet()),
                userId, Types.TASK, ctx);
        } catch (OXException e) {
            throw e;
        }
        for (final ReminderObject reminder : reminders) {
            tmp.get(Integer.valueOf(reminder.getTargetId())).setAlarm(reminder
                .getDate());
        }
    }

    static void loadReminder(final Context ctx, final int userId,
        final Collection<Task> tasks, final Connection con) throws OXException {
            final ReminderSQLInterface remStor = ReminderHandler.getInstance();
            final Map<Integer, Task> tmp = new HashMap<Integer, Task>();
            for (final Task task : tasks) {
                tmp.put(Integer.valueOf(task.getObjectID()), task);
            }
            final ReminderObject[] reminders;
            reminders = remStor.loadReminders(Collections.toArray(tmp.keySet()), userId, Types.TASK, con, ctx);
            for (final ReminderObject reminder : reminders) {
                tmp.get(Integer.valueOf(reminder.getTargetId())).setAlarm(reminder
                    .getDate());
            }
    }

    /**
     * Loads a reminder for a user and a task.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param task loaded task.
     * @throws OXException if an error occurs.
     */
    static void loadReminder(final Context ctx, final int userId,
        final Task task) throws OXException {
        final ReminderSQLInterface reminder = ReminderHandler.getInstance();
        final int taskId = task.getObjectID();
        try {
            if (reminder.existsReminder(taskId, userId, Types.TASK, ctx)) {
                final ReminderObject remind = reminder.loadReminder(taskId,
                    userId, Types.TASK, ctx);
                task.setAlarm(remind.getDate());
            }
        } catch (OXException e) {
            throw e;
        }
    }

    static void deleteReminder(Context ctx, Connection con, Task task) throws OXException {
        final ReminderSQLInterface reminder = ReminderHandler.getInstance();
        try {
            reminder.deleteReminder(task.getObjectID(), Types.TASK, con, ctx);
        } catch (OXException e) {
            if (!ReminderExceptionCode.NOT_FOUND.equals(e)) {
                throw e;
            }
        }
    }

    static void updateReminderOnMove(Context ctx, int taskId, int sourceFolderId, int destFolderId, boolean privateFlag) throws OXException {
        FolderObject folder = Tools.getFolder(ctx, destFolderId);
        ReminderSQLInterface service = ReminderHandler.getInstance();
        SearchIterator<ReminderObject> iter = service.listReminder(Types.TASK, taskId, ctx);
        try {
            while (iter.hasNext()) {
                ReminderObject reminder = iter.next();
                if (reminder.getFolder() == sourceFolderId) {
                    int userId = reminder.getUser();
                    User user = Tools.getUser(ctx, userId);
                    UserPermissionBits userPerms = Tools.getUserPermissionBits(ctx, userId);
                    try {
                        boolean testFlag = com.openexchange.groupware.tasks.Permission.canReadInFolder(ctx, user, userPerms, folder);
                        if (testFlag && privateFlag) {
                            // Go to catch clause.
                            throw new OXException();
                        }
                        reminder.setFolder(destFolderId);
                        service.updateReminder(reminder, ctx);
                    } catch (OXException e) {
                        // Reading is not allowed. Delete the reminder
                        service.deleteReminder(reminder, ctx);
                    }
                }
            }
        } finally {
            SearchIterators.close(iter);
        }
    }
}
