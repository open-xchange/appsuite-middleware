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

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.reminder.actions.DeleteRequest;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug14111Test extends AbstractAJAXSession {

    private AJAXClient client;

    private TimeZone tz;

    private Calendar calendar;

    private Appointment appointment;

    private Appointment exception;

    public Bug14111Test(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        calendar = GregorianCalendar.getInstance(tz);
        appointment = createAppointment();
        exception = createException();
    }

    public void testReminders() throws Exception {
        // Get one reminder after another and delete it to calculate the next one
        final Calendar cal = (Calendar) calendar.clone();
        cal.setTime(appointment.getEndDate());
        RangeRequest rangeReq;
        RangeResponse rangeResp;
        ReminderObject reminder;
        ReminderObject[] reminders;
        for (int i = 0; i < 10; i++) {
            // This is the exception. It should have it's own reminder but the series reminder must not point on the same day.
            if (i == 0) {
                rangeReq = new RangeRequest(cal.getTime());
                rangeResp = client.execute(rangeReq);
                reminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), appointment.getObjectID());
                final ReminderObject exceptionReminder = ReminderTools.searchByTarget(rangeResp.getReminder(tz), exception.getObjectID());
                assertNull("A reminder of the series was created on the exception appointment.", reminder);
                assertNotNull("No reminder was created for the exception.", exceptionReminder);
                client.execute(new DeleteRequest(exceptionReminder, false));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            } else {
                // All other reminders have to appear
                rangeReq = new RangeRequest(cal.getTime());
                rangeResp = client.execute(rangeReq);
                reminders = rangeResp.getReminder(tz);
                reminder = ReminderTools.searchByTarget(reminders, appointment.getObjectID());
                cal.add(Calendar.DAY_OF_MONTH, 1);
                if (reminder == null) {
                    fail("No reminder found.");
                } else {
                    client.execute(new DeleteRequest(reminder, false));
                }
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        // Delete the exception.
        final com.openexchange.ajax.appointment.action.DeleteRequest delReq1 = new com.openexchange.ajax.appointment.action.DeleteRequest(
            exception,
            false);
        client.execute(delReq1);

        // Delete the series.
        final GetRequest getApp = new GetRequest(appointment, false);
        final GetResponse getAppResp = client.execute(getApp);
        final Appointment toDelete = getAppResp.getAppointment(tz);
        final com.openexchange.ajax.appointment.action.DeleteRequest delReq2 = new com.openexchange.ajax.appointment.action.DeleteRequest(
            toDelete,
            false);
        client.execute(delReq2);
        super.tearDown();
    }

    private Appointment createAppointment() throws OXException, IOException, SAXException, JSONException {
        final Calendar cal = (Calendar) calendar.clone();
        final Appointment appointmentObj = new Appointment();

        appointmentObj.setTitle("testBug14111");
        cal.add(Calendar.MINUTE, 10);
        appointmentObj.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(cal.getTime());
        appointmentObj.setAlarm(15);

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setNote("");
        appointmentObj.setNotification(true);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(false);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(10);

        final UserParticipant newParticipant = new UserParticipant(client.getValues().getUserId());
        appointmentObj.addParticipant(newParticipant);

        final InsertRequest insReq = new InsertRequest(appointmentObj, tz, false);
        final AppointmentInsertResponse insResp = client.execute(insReq);
        insResp.fillAppointment(appointmentObj);

        return appointmentObj;
    }

    private Appointment createException() throws Exception {
        final int recurrencePosition = 1;
        final Calendar cal = (Calendar) calendar.clone();
        cal.setTime(appointment.getStartDate());
        cal.add(Calendar.DAY_OF_MONTH, recurrencePosition - 1);
        final Appointment exception = new Appointment();
        exception.setTitle(appointment.getTitle() + " exception");
        exception.setIgnoreConflicts(true);
        exception.setParentFolderID(appointment.getParentFolderID());
        exception.setObjectID(appointment.getObjectID());
        exception.setRecurrencePosition(recurrencePosition);
        exception.setRecurrenceType(Appointment.NO_RECURRENCE);
        exception.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        exception.setEndDate(cal.getTime());
        exception.setLastModified(appointment.getLastModified());
        exception.setAlarm(15);

        final UpdateRequest insReq = new UpdateRequest(exception, tz, false);
        final UpdateResponse insResp = client.execute(insReq);
        exception.setObjectID(insResp.getId());
        exception.setLastModified(insResp.getTimestamp());

        return exception;
    }

}
