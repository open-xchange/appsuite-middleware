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

package com.openexchange.groupware.reminder.internal;

import com.openexchange.api2.ReminderService;
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
    private final ReminderService reminderService;

    /**
     * Initializes a new {@link RemindAgain}.
     *
     * @param reminder The reminder
     * @param session The session
     * @param ctx The context
     */
    public RemindAgain(final ReminderObject reminder, final Session session, final Context ctx, final ReminderService reminderService) {
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
            if (reminderService.existsReminder(taskId, userId, Types.TASK)) {
                reminderService.deleteReminder(taskId, userId, Types.TASK);
            }
        } else {
            if (reminderService.existsReminder(taskId, userId, Types.TASK)) {
                reminderService.updateReminder(reminder);
            } else {
                reminderService.insertReminder(reminder);
            }
        }
        /*
         * Load updated reminder...
         */
        final ReminderObject updated = reminderService.loadReminder(reminder.getObjectId());
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
