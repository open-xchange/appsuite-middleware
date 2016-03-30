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

package com.openexchange.user.copy.internal.reminder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.ReminderStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.IntegerMapping;
import com.openexchange.user.copy.internal.calendar.CalendarCopyTask;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.contact.ContactCopyTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.tasks.TaskCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;

/**
 * {@link ReminderCopyTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class ReminderCopyTask implements CopyUserTaskService {

    /**
     * Initializes a new {@link ReminderCopyTask}.
     */
    public ReminderCopyTask() {
        super();
    }

    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName(),
            CalendarCopyTask.class.getName(),
            ContactCopyTask.class.getName(),
            TaskCopyTask.class.getName()
        };
    }

    @Override
    public String getObjectName() {
        return com.openexchange.groupware.reminder.ReminderObject.class.getName();
    }

    @Override
    public IntegerMapping copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);

        final Context srcCtx = copyTools.getSourceContext();
        final Context dstCtx = copyTools.getDestinationContext();

        final User srcUser = copyTools.getSourceUser();
        final User dstUser = copyTools.getDestinationUser();

        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();

        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();
        final ObjectMapping<Integer> appointmentMapping = copyTools.checkAndExtractGenericMapping(Appointment.class.getName());
        final ObjectMapping<Integer> taskMapping = copyTools.checkAndExtractGenericMapping(Task.class.getName());

        final Map<Integer, ReminderObject> reminders = loadRemindersFromDB(srcCtx, srcCon, srcUser, folderMapping);
        exchangeIds(reminders, taskMapping, appointmentMapping, folderMapping, dstUser.getId(), dstCtx, dstCon);
        writeRemindersToDatabase(dstCon, dstCtx, srcCtx, dstUser.getId(), reminders);

        final IntegerMapping mapping = new IntegerMapping();
        for (final int reminderId : reminders.keySet()) {
            final ReminderObject reminder = reminders.get(reminderId);
            mapping.addMapping(reminderId, reminder.getObjectId());
        }

        return mapping;
    }

    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {

    }

    private Map<Integer, ReminderObject> loadRemindersFromDB(final Context ctx, final Connection con, final User user, final ObjectMapping<FolderObject> folderMapping) throws OXException {
        final Map<Integer, ReminderObject> reminders = new HashMap<Integer, ReminderObject>();
        final Date end = new Date(Long.MAX_VALUE);
        final ReminderObject[] reminderArray = ReminderStorage.getInstance().selectReminder(ctx, con, user, end);
        for (int i = 0; i < reminderArray.length; i++) {
            ReminderObject reminderObject = reminderArray[i];
            if (folderMapping.getSource(reminderObject.getFolder()) != null) {
                reminders.put(reminderObject.getObjectId(), reminderObject);
            }
        }

        return reminders;
    }

    private void writeRemindersToDatabase(final Connection dstCon, final Context dstCtx, final Context srcCtx, final int dstUserId, final Map<Integer, ReminderObject> reminders) throws OXException {
        for (final int reminderId : reminders.keySet()) {
            final ReminderObject reminder = reminders.get(reminderId);
            ReminderStorage.getInstance().writeReminder(dstCon, dstCtx.getContextId(), reminder);
        }
    }

    private void exchangeIds(final Map<Integer, ReminderObject> reminders, final ObjectMapping<Integer> taskMapping, final ObjectMapping<Integer> appointmentMapping, final ObjectMapping<FolderObject> folderMapping, final int userId, final Context ctx, final Connection con) throws OXException {
        try {
            for (final int reminderId : reminders.keySet()) {
                final int newReminderId = IDGenerator.getId(ctx, com.openexchange.groupware.Types.REMINDER, con);
                final ReminderObject reminder = reminders.get(reminderId);
                reminder.setObjectId(newReminderId);
                reminder.setUser(userId);
                FolderObject dstFolder = folderMapping.getDestination(folderMapping.getSource(reminder.getFolder()));
                reminder.setFolder(dstFolder.getObjectID());

                final int targetId = reminder.getTargetId();
                int newTargetId = targetId;
                if (reminder.getModule() == com.openexchange.groupware.Types.APPOINTMENT) {
                    for (final int copy : appointmentMapping.getSourceKeys()) {
                        if (appointmentMapping.getDestination(copy) == targetId) {
                            newTargetId = copy;
                        }
                    }
                } else if (reminder.getModule() == com.openexchange.groupware.Types.TASK) {
                    for (final int copy : taskMapping.getSourceKeys()) {
                        if (taskMapping.getDestination(copy) == targetId) {
                            newTargetId = copy;
                        }
                    }
                }
                reminder.setTargetId(newTargetId);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        }
    }
}
