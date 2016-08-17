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

package com.openexchange.ajax.reminder;

import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
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
     * @param name
     */
    public DeleteMultipleReminderTest(String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User1);
        timeZone = client.getValues().getTimeZone();

        task = new Task();
        task.setTitle("Test Reminder");
        task.setStartDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60));
        task.setEndDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *2));
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
    
    @Override
    public void tearDown() throws Exception {
        appointment.setLastModified(new Date(Long.MAX_VALUE));
        com.openexchange.ajax.appointment.action.DeleteRequest aDelReq = new com.openexchange.ajax.appointment.action.DeleteRequest(appointment);
        client.execute(aDelReq);

        com.openexchange.ajax.task.actions.DeleteRequest tDelReq = new com.openexchange.ajax.task.actions.DeleteRequest(task);
//        client.execute(tDelReq);

        super.tearDown();
    }

}
