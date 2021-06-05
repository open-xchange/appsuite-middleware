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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
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

    public Bug14111Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        calendar = GregorianCalendar.getInstance(tz);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        appointment = createAppointment();
        exception = createException();
    }

    @Test
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

    private Appointment createAppointment() throws OXException, IOException, JSONException {
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
