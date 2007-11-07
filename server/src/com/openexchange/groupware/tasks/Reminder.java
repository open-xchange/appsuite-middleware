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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderException;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.Collections;

/**
 * This class contains everything for handling reminder for tasks.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
final class Reminder {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Reminder.class);

    /**
     * Prevent instanciation.
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
        remind.setFolder(String.valueOf(task.getParentFolderID()));
        remind.setUser(task.getCreatedBy());
        final ReminderSQLInterface reminder = new ReminderHandler(ctx);
        reminder.insertReminder(remind);
    }

    static void fixAlarm(final Context ctx, final Task task,
        final Set<TaskParticipant> removed, final Set<Folder> folders)
        throws OXException {
        final ReminderSQLInterface reminder = new ReminderHandler(ctx);
        final int taskId = task.getObjectID();
        for (InternalParticipant participant : ParticipantStorage
            .extractInternal(removed)) {
            final int userId = participant.getIdentifier();
            if (reminder.existsReminder(taskId, userId, Types.TASK)) {
                reminder.deleteReminder(taskId, userId, Types.TASK);
            }
        }
        for (Folder folder : folders) {
            final int userId = folder.getUser();
            if (reminder.existsReminder(taskId, userId, Types.TASK)) {
                final ReminderObject remind = reminder.loadReminder(taskId,
                    folder.getUser(), Types.TASK);
                final int folderId = folder.getIdentifier();
                try {
                    if (Integer.parseInt(remind.getFolder()) != folderId) {
                        remind.setFolder(String.valueOf(folderId));
                        reminder.updateReminder(remind);
                    }
                } catch (NumberFormatException nfe) {
                    LOG.error("Parsing reminder folder identifier failed.", nfe);
                }
            }
        }
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
        final ReminderSQLInterface reminder = new ReminderHandler(ctx);
        final int taskId = task.getObjectID();
        final int userId = user.getId();
        if (null == task.getAlarm()) {
            if (reminder.existsReminder(taskId, userId, Types.TASK)) {
                reminder.deleteReminder(taskId, userId, Types.TASK);
            }
        } else {
    		final ReminderObject remind = new ReminderObject();
            remind.setDate(task.getAlarm());
            remind.setModule(Types.TASK);
            remind.setTargetId(taskId);
            remind.setFolder(String.valueOf(task.getParentFolderID()));
            remind.setUser(userId);
    		
    		if (reminder.existsReminder(taskId, userId, Types.TASK)) {
                reminder.updateReminder(remind);
    		} else {
    			reminder.insertReminder(remind);
    		}
        }
    }

    /**
     * Loads reminder for a user and several tasks.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param tasks load reminder for this tasks.
     * @throws TaskException if an error occurs.
     */
    static void loadReminder(final Context ctx, final int userId,
        final Collection<Task> tasks) throws TaskException {
        final ReminderSQLInterface remStor = new ReminderHandler(ctx);
        final Map<Integer, Task> tmp = new HashMap<Integer, Task>();
        for (Task task : tasks) {
            tmp.put(task.getObjectID(), task);
        }
        final ReminderObject[] reminders;
        try {
            reminders = remStor.loadReminder(Collections.toArray(tmp.keySet()),
                userId, Types.TASK);
        } catch (OXException e) {
            throw new TaskException(e);
        }
        for (ReminderObject reminder : reminders) {
            tmp.get(Integer.parseInt(reminder.getTargetId())).setAlarm(reminder
                .getDate());
        }
    }

    /**
     * Loads a reminder for a user and a task.
     * @param ctx Context.
     * @param userId unique identifier of the user.
     * @param task loaded task.
     * @throws TaskException if an error occurs.
     */
    static void loadReminder(final Context ctx, final int userId,
        final Task task) throws TaskException {
        final ReminderSQLInterface reminder = new ReminderHandler(ctx);
        final int taskId = task.getObjectID();
        try {
            if (reminder.existsReminder(taskId, userId, Types.TASK)) {
                final ReminderObject remind = reminder.loadReminder(taskId,
                    userId, Types.TASK);
                task.setAlarm(remind.getDate());
            }
        } catch (OXException e) {
            throw new TaskException(e);
        }
    }

    static void deleteReminder(final Context ctx, final Task task) throws TaskException {
        final ReminderSQLInterface reminder = new ReminderHandler(ctx);
        try {
            reminder.deleteReminder(task.getObjectID(), Types.TASK);
        } catch (ReminderException e) {
            if (ReminderException.Code.NOT_FOUND.getDetailNumber() != e
                .getDetailNumber()) {
                throw new TaskException(e);
            }
        } catch (OXException e) {
            throw new TaskException(e);
        }
    }

}
