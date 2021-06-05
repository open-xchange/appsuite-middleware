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

package com.openexchange.ajax.appointment.recurrence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Moves a weekly series appointment starting during no daylight saving time in a week with daylight saving time and verifies it is put at
 * the correct time.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug14074Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { Appointment.OBJECT_ID, Appointment.RECURRENCE_POSITION };

    private TimeZone tz;

    private int folderId;

    private Appointment appointment;

    public Bug14074Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        AJAXClient client = getClient();
        tz = client.getValues().getTimeZone();
        folderId = client.getValues().getPrivateAppointmentFolder();
        appointment = createAppointment();
        InsertRequest request = new InsertRequest(appointment, tz);
        AppointmentInsertResponse response = client.execute(request);
        response.fillAppointment(appointment);
    }

    @Test
    public void testDailyFullTimeUntil() throws Throwable {
        AJAXClient client = getClient();
        // Change appointment to 1400 in daylight saving time.
        Appointment changed = changeAppointment();
        UpdateResponse response = client.execute(new UpdateRequest(changed, tz));
        appointment.setLastModified(response.getTimestamp());
        // Find appointment with recurrence position.
        Calendar calendar = TimeTools.createCalendar(tz, 2009, 6, 6, 0);
        Date start = calendar.getTime();
        calendar.add(Calendar.DATE, 7);
        Date end = calendar.getTime();
        CommonAllResponse allResponse = client.execute(new AllRequest(folderId, COLUMNS, start, end, tz, false));
        int recurrencePosition = -1;
        for (Object[] obj : allResponse) {
            if (appointment.getObjectID() == ((Integer) obj[0]).intValue()) {
                recurrencePosition = ((Integer) obj[1]).intValue();
            }
        }
        assertFalse("Changed appointment not found.", -1 == recurrencePosition);
        // Load appointment
        GetResponse getResponse = client.execute(new GetRequest(folderId, appointment.getObjectID(), recurrencePosition));
        Appointment toTest = getResponse.getAppointment(tz);
        assertEquals("Start date is not 1400.", changed.getStartDate(), toTest.getStartDate());
        assertEquals("Start date is not 1500.", changed.getEndDate(), toTest.getEndDate());
    }

    private Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setTitle("test for bug 14074");
        appointment.setParentFolderID(folderId);
        Calendar calendar = TimeTools.createCalendar(tz, 2009, 0, 23, 13);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(Appointment.FRIDAY);
        appointment.setIgnoreConflicts(true);
        return appointment;
    }

    private Appointment changeAppointment() {
        Appointment changed = new Appointment();
        changed.setTitle("test for bug 14074 changed");
        changed.setParentFolderID(folderId);
        changed.setObjectID(appointment.getObjectID());
        changed.setLastModified(appointment.getLastModified());
        Calendar calendar = TimeTools.createCalendar(tz, 2009, 6, 10, 14);
        changed.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        changed.setEndDate(calendar.getTime());
        changed.setRecurrenceType(Appointment.WEEKLY);
        changed.setInterval(1);
        changed.setDays(Appointment.FRIDAY);
        changed.setIgnoreConflicts(true);
        return changed;
    }
}
