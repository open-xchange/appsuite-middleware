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

package com.openexchange.ajax.reminder;

import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.reminder.actions.DeleteRequest;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link DeleteMultipleReminderTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DeleteMultipleReminderTest extends ReminderTest {

    private AJAXClient client;
    private Appointment appointment;
    private Task task;
    private ReminderObject[] reminders;
    private TimeZone timeZone;

    /**
     * Initializes a new {@link DeleteMultipleReminderTest}.
     *
     * @param name
     */
    public DeleteMultipleReminderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = client.getValues().getTimeZone();

        task = new Task();
        task.setTitle("Test Reminder");
        task.setStartDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60));
        task.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));
        task.setAlarm(new Date(System.currentTimeMillis() + 1000 * 60 * 30));
        task.setParentFolderID(client.getValues().getPrivateTaskFolder());
        InsertRequest taskInsert = new InsertRequest(task, timeZone);
        InsertResponse taskResponse = client.execute(taskInsert);
        taskResponse.fillTask(task);

        appointment = new Appointment();
        appointment.setTitle("Test Reminder");
        appointment.setStartDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60));
        appointment.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2));
        appointment.setAlarm(15);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        com.openexchange.ajax.appointment.action.InsertRequest appointmentInsert = new com.openexchange.ajax.appointment.action.InsertRequest(appointment, timeZone);
        AppointmentInsertResponse appointmentResponse = client.execute(appointmentInsert);
        appointmentResponse.fillAppointment(appointment);

        RangeRequest rngReq = new RangeRequest(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 4));
        RangeResponse rngRes = client.execute(rngReq);
        reminders = rngRes.getReminder(timeZone);
    }

    @Test
    public void testDeleteMultipleReminders() throws Exception {
        DeleteRequest delReq = new DeleteRequest(reminders, true);
        client.execute(delReq);
    }

}
