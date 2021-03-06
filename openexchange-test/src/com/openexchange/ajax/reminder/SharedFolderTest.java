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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * We have to check two different reminder related things in shared folders. At first the folder owner must have the right to initially set
 * and to update an appointments reminder. But also the person the folder is shared to must have the right to set and change an appointments
 * reminder. In this case the reminder has to be set/changed for the folder owner. The reminders must be found through a RangeRequest and
 * the alarm field of an appointment must be set correctly in a GetResponse.
 *
 * @author <a href="steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SharedFolderTest extends Abstrac2UserAJAXSession {

    FolderObject sharedFolder;

    Appointment firstAppointment;

    Appointment secondAppointment;

    TimeZone tz, tz2;

    public SharedFolderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        tz = client1.getValues().getTimeZone();
        tz2 = client2.getValues().getTimeZone();

        // Create shared folder
        sharedFolder = Create.createPrivateFolder("Bug 17327 shared folder", FolderObject.CALENDAR, client1.getValues().getUserId());
        sharedFolder.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        final InsertResponse folderInsertResponse = client1.execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OUTLOOK, sharedFolder, true));
        folderInsertResponse.fillObject(sharedFolder);
        FolderTools.shareFolder(client1, EnumAPI.OUTLOOK, sharedFolder.getObjectID(), client2.getValues().getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
    }

    @Test
    public void testAppointmentCreatorCanChangeReminder() throws Exception {
        // Create appointment
        firstAppointment = createAppointment();
        firstAppointment.setAlarm(15);
        final AppointmentInsertResponse appointmentInsertResponse = client1.execute(new InsertRequest(firstAppointment, tz, true));
        appointmentInsertResponse.fillAppointment(firstAppointment);

        // Get Appointment and Reminder to check reminder and alarm field
        {
            final GetResponse getResponse = client1.execute(new GetRequest(firstAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", firstAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(firstAppointment.getEndDate());
            final RangeResponse rangeResp = client1.execute(rangeReq);
            final ReminderObject reminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), firstAppointment.getObjectID());

            assertNotNull("No reminder was found.", reminder);
            final Calendar remCal = new GregorianCalendar();
            remCal.setTime(firstAppointment.getStartDate());
            remCal.add(Calendar.MINUTE, -15);
            assertEquals("Reminder date was not set correctly.", remCal.getTime(), reminder.getDate());
        }

        // Update appointment and check again
        {
            firstAppointment.setAlarm(30);
            final UpdateResponse updateResponse = client1.execute(new UpdateRequest(firstAppointment, tz));
            firstAppointment.setLastModified(updateResponse.getTimestamp());

            final GetResponse getResponse = client1.execute(new GetRequest(firstAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", firstAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(firstAppointment.getEndDate());
            final RangeResponse rangeResp = client1.execute(rangeReq);
            final ReminderObject reminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), firstAppointment.getObjectID());

            assertNotNull("No reminder was found.", reminder);
            final Calendar remCal = new GregorianCalendar();
            remCal.setTime(firstAppointment.getStartDate());
            remCal.add(Calendar.MINUTE, -30);
            assertEquals("Reminder date was not set correctly.", remCal.getTime(), reminder.getDate());
        }

        client1.execute(new DeleteRequest(firstAppointment));
    }

    @Test
    public void testSecretaryCanChangeReminder() throws Exception {
        // Create appointment
        secondAppointment = createAppointment();
        secondAppointment.setAlarm(15);
        final AppointmentInsertResponse appointmentInsertResponse = client2.execute(new InsertRequest(secondAppointment, tz2, true));
        appointmentInsertResponse.fillAppointment(secondAppointment);

        // Get Appointment and Reminder to check reminder and alarm field
        {
            final GetResponse getResponse = client1.execute(new GetRequest(secondAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", secondAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(secondAppointment.getEndDate());
            final RangeResponse rangeResp = client1.execute(rangeReq);
            final ReminderObject reminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), secondAppointment.getObjectID());

            assertNotNull("No reminder was found.", reminder);
            final Calendar remCal = new GregorianCalendar();
            remCal.setTime(secondAppointment.getStartDate());
            remCal.add(Calendar.MINUTE, -15);
            assertEquals("Reminder date was not set correctly.", remCal.getTime(), reminder.getDate());
        }

        // Update appointment and check again
        {
            secondAppointment.setAlarm(30);
            final UpdateResponse updateResponse = client2.execute(new UpdateRequest(secondAppointment, tz2));
            secondAppointment.setLastModified(updateResponse.getTimestamp());

            final GetResponse getResponse = client1.execute(new GetRequest(secondAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", secondAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(secondAppointment.getEndDate());
            final RangeResponse rangeResp = client1.execute(rangeReq);
            final ReminderObject reminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), secondAppointment.getObjectID());

            assertNotNull("No reminder was found.", reminder);
            final Calendar remCal = new GregorianCalendar();
            remCal.setTime(secondAppointment.getStartDate());
            remCal.add(Calendar.MINUTE, -30);
            assertEquals("Reminder date was not set correctly.", remCal.getTime(), reminder.getDate());
        }

        client2.execute(new DeleteRequest(secondAppointment));
    }

    public Appointment createAppointment() {
        final Calendar cal;
        final Appointment appointment = new Appointment();
        appointment.setTitle("SharedFolder Testappointment");
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setIgnoreConflicts(true);
        cal = TimeTools.createCalendar(tz);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        appointment.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 2);
        appointment.setEndDate(cal.getTime());

        return appointment;
    }

}
