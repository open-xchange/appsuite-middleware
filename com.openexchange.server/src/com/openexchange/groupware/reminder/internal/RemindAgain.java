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

package com.openexchange.groupware.reminder.internal;

import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.session.Session;

/**
 * {@link RemindAgain} - Sets a new alarm for specified reminder object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RemindAgain {

    private final Session session;
    private final Context ctx;
    private final ReminderObject reminder;
    private final ReminderSQLInterface reminderService;

    /**
     * Initializes a new {@link RemindAgain}.
     *
     * @param reminder The reminder
     * @param session The session
     * @param ctx The context
     */
    public RemindAgain(final ReminderObject reminder, final Session session, final Context ctx, final ReminderSQLInterface reminderService) {
        super();
        this.session = session;
        this.ctx = ctx;
        this.reminder = reminder;
        this.reminderService = reminderService;
    }

    /**
     * Performs the update of reminder's alarm date.
     *
     * @throws OXException If update fails
     */
    public void remindAgain() throws OXException {
        final int module = reminder.getModule();
        if (Types.APPOINTMENT == module) {
            throw ReminderExceptionCode.UNEXPECTED_ERROR.create("Operation not supported for module calendar.");
        } else if (Types.TASK == module) {
            remindAgainTask();
        } else {
            throw ReminderExceptionCode.UNEXPECTED_ERROR.create("Unknown module: " + module);
        }
    }

    private void remindAgainTask() throws OXException {
        /*
         * Task module does not hold any reminder data by itself. Just change reminder data in database.
         */
        final int taskId = reminder.getTargetId();
        final int userId = reminder.getUser();
        if (null == reminder.getDate()) {
            if (reminderService.existsReminder(taskId, userId, Types.TASK, ctx)) {
                reminderService.deleteReminder(taskId, userId, Types.TASK, ctx);
            }
        } else {
            if (reminderService.existsReminder(taskId, userId, Types.TASK, ctx)) {
                reminderService.updateReminder(reminder, ctx);
            } else {
                reminderService.insertReminder(reminder, ctx);
            }
        }
        /*
         * Load updated reminder...
         */
        final ReminderObject updated = reminderService.loadReminder(reminder.getObjectId(), ctx);
        /*
         * ... and apply new values to passed reminder object
         */
        reminder.setDate(updated.getDate());
        reminder.setDescription(updated.getDescription());
        reminder.setFolder(updated.getFolder());
        reminder.setLastModified(updated.getLastModified());
        reminder.setModule(updated.getModule());
        reminder.setObjectId(updated.getObjectId());
        reminder.setRecurrenceAppointment(updated.isRecurrenceAppointment());
        reminder.setRecurrencePosition(updated.getRecurrencePosition());
        reminder.setTargetId(updated.getTargetId());
        reminder.setUser(updated.getUser());
    }

}
