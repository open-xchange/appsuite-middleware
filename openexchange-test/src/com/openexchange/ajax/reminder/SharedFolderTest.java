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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
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
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * We have to check two different reminder related things in shared folders. At first the folder owner must have the right to initially set
 * and to update an appointments reminder. But also the person the folder is shared to must have the right to set and change an appointments
 * reminder. In this case the reminder has to be set/changed for the folder owner. The reminders must be found through a RangeRequest and
 * the alarm field of an appointment must be set correctly in a GetResponse.
 *
 * @author <a href="steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SharedFolderTest extends AbstractAJAXSession {

    AJAXClient client;

    AJAXClient client2;

    FolderObject sharedFolder;

    Appointment firstAppointment;

    Appointment secondAppointment;

    TimeZone tz, tz2;

    public SharedFolderTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client = getClient();
        tz = client.getValues().getTimeZone();
        client2 = new AJAXClient(User.User2);
        tz2 = client2.getValues().getTimeZone();

        // Create shared folder
        sharedFolder = Create.createPrivateFolder("Bug 17327 shared folder", FolderObject.CALENDAR, client.getValues().getUserId());
        sharedFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        final InsertResponse folderInsertResponse = client.execute(new com.openexchange.ajax.folder.actions.InsertRequest(
            EnumAPI.OUTLOOK,
            sharedFolder,
            true));
        folderInsertResponse.fillObject(sharedFolder);
        FolderTools.shareFolder(
            client,
            EnumAPI.OUTLOOK,
            sharedFolder.getObjectID(),
            client2.getValues().getUserId(),
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS,
            OCLPermission.DELETE_ALL_OBJECTS);
    }

    public void testAppointmentCreatorCanChangeReminder() throws Exception {
        // Create appointment
        firstAppointment = createAppointment();
        firstAppointment.setAlarm(15);
        final AppointmentInsertResponse appointmentInsertResponse = client.execute(new InsertRequest(firstAppointment, tz, true));
        appointmentInsertResponse.fillAppointment(firstAppointment);

        // Get Appointment and Reminder to check reminder and alarm field
        {
            final GetResponse getResponse = client.execute(new GetRequest(firstAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", firstAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(firstAppointment.getEndDate());
            final RangeResponse rangeResp = client.execute(rangeReq);
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
            final UpdateResponse updateResponse = client.execute(new UpdateRequest(firstAppointment, tz));
            firstAppointment.setLastModified(updateResponse.getTimestamp());

            final GetResponse getResponse = client.execute(new GetRequest(firstAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", firstAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(firstAppointment.getEndDate());
            final RangeResponse rangeResp = client.execute(rangeReq);
            final ReminderObject reminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), firstAppointment.getObjectID());

            assertNotNull("No reminder was found.", reminder);
            final Calendar remCal = new GregorianCalendar();
            remCal.setTime(firstAppointment.getStartDate());
            remCal.add(Calendar.MINUTE, -30);
            assertEquals("Reminder date was not set correctly.", remCal.getTime(), reminder.getDate());
        }

        client.execute(new DeleteRequest(firstAppointment));
    }

    public void testSecretaryCanChangeReminder() throws Exception {
        // Create appointment
        secondAppointment = createAppointment();
        secondAppointment.setAlarm(15);
        final AppointmentInsertResponse appointmentInsertResponse = client2.execute(new InsertRequest(secondAppointment, tz2, true));
        appointmentInsertResponse.fillAppointment(secondAppointment);

        // Get Appointment and Reminder to check reminder and alarm field
        {
            final GetResponse getResponse = client.execute(new GetRequest(secondAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", secondAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(secondAppointment.getEndDate());
            final RangeResponse rangeResp = client.execute(rangeReq);
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

            final GetResponse getResponse = client.execute(new GetRequest(secondAppointment));
            final Appointment toCompare = getResponse.getAppointment(tz);
            assertEquals("Appointment did not contain correct alarm time.", secondAppointment.getAlarm(), toCompare.getAlarm());

            final RangeRequest rangeReq = new RangeRequest(secondAppointment.getEndDate());
            final RangeResponse rangeResp = client.execute(rangeReq);
            final ReminderObject reminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), secondAppointment.getObjectID());

            assertNotNull("No reminder was found.", reminder);
            final Calendar remCal = new GregorianCalendar();
            remCal.setTime(secondAppointment.getStartDate());
            remCal.add(Calendar.MINUTE, -30);
            assertEquals("Reminder date was not set correctly.", remCal.getTime(), reminder.getDate());
        }

        client2.execute(new DeleteRequest(secondAppointment));
    }

    @Override
    public void tearDown() throws Exception {
        client.execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OUTLOOK, sharedFolder));
        client2.logout();

        super.tearDown();
    }

    public Appointment createAppointment() throws Exception {
        final Calendar cal;
        final Appointment appointment = new Appointment();
        appointment.setTitle("SharedFolder Testappointment");
        appointment.setParentFolderID(sharedFolder.getObjectID());
        appointment.setIgnoreConflicts(true);
        cal = TimeTools.createCalendar(tz);
        appointment.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 2);
        appointment.setEndDate(cal.getTime());

        return appointment;
    }

}
