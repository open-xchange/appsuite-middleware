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
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.test.common.groupware.calendar.TimeTools;

public class Bug7590Test extends AbstractAJAXSession {

    private static final int alarmMinutes = 60;

    private AJAXClient client;

    private TimeZone tz;

    private int folderId;

    private Calendar calendar;

    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        folderId = client.getValues().getPrivateAppointmentFolder();
        calendar = TimeTools.createCalendar(tz);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        appointment = createAppointment();
    }

    @Test
    public void testBug7590() throws Exception {
        final RangeRequest request = new RangeRequest(appointment.getEndDate());
        final RangeResponse response = client.execute(request);

        ReminderObject actual = null;
        for (ReminderObject reminder : response.getReminder(tz)) {
            if (appointment.getObjectID() == reminder.getTargetId()) {
                actual = reminder;
                break;
            }
        }
        assertNotNull("No reminder found for created appointment.", actual);

        final ReminderObject expected = new ReminderObject();
        int reminderId = actual.getObjectId();
        expected.setObjectId(reminderId);
        expected.setFolder(folderId);
        expected.setTargetId(appointment.getObjectID());
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.MINUTE, -alarmMinutes);
        expected.setDate(calendar.getTime());
        ReminderTest.compareReminder(expected, actual);
    }

    private Appointment createAppointment() throws OXException, IOException, JSONException {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug7590");

        // Start date must be in the future
        calendar.add(Calendar.HOUR, 1);
        appointmentObj.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(calendar.getTime());
        appointmentObj.setAlarm(alarmMinutes);

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(3);
        appointmentObj.setIgnoreConflicts(true);

        final AppointmentInsertResponse insertR = client.execute(new InsertRequest(appointmentObj, tz));
        insertR.fillAppointment(appointmentObj);
        return appointmentObj;
    }
}
