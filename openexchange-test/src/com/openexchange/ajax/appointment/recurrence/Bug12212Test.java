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
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

public class Bug12212Test extends AbstractAJAXSession {

    final String bugname = "Test for bug 12212";

    public Bug12212Test() {
        super();
    }

    public Appointment createDailyRecurringAppointment(final TimeZone timezone, final int folderId) {
        final Calendar calendar = TimeTools.createCalendar(timezone);
        final Appointment series = new Appointment();

        series.setTitle(bugname);
        series.setParentFolderID(folderId);
        series.setIgnoreConflicts(true);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        series.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        series.setEndDate(calendar.getTime());
        series.setRecurrenceType(Appointment.DAILY);
        series.setInterval(1);
        series.setOccurrence(5);
        return series;
    }

    public void shiftAppointmentDateOneHour(final Appointment appointment, final TimeZone tz) {
        final Calendar calendar = TimeTools.createCalendar(tz);
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.HOUR, 1);
        appointment.setStartDate(calendar.getTime());

        calendar.setTime(appointment.getEndDate());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
    }

    @Test
    public void testMovingExceptionTwiceShouldNeitherCrashNorDuplicate() throws OXException, IOException, JSONException {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = client.getValues().getTimeZone();

        //create appointment
        final Appointment appointmentSeries = createDailyRecurringAppointment(tz, folderId);

        {//send appointment
            final InsertRequest request = new InsertRequest(appointmentSeries, tz);
            final CommonInsertResponse response = client.execute(request);
            appointmentSeries.setObjectID(response.getId());
            appointmentSeries.setLastModified(response.getTimestamp());
        }
        //get one occurrence
        final int recurrence_position = 3;
        final Appointment occurrence;
        {
            final GetRequest request = new GetRequest(folderId, appointmentSeries.getObjectID(), recurrence_position);
            final GetResponse response = client.execute(request);
            occurrence = response.getAppointment(tz);
        }

        //make an exception out of the occurrence
        Appointment exception = new Appointment();
        exception.setObjectID(occurrence.getObjectID());
        exception.setParentFolderID(folderId);
        exception.setLastModified(occurrence.getLastModified());
        exception.setRecurrencePosition(occurrence.getRecurrencePosition());
        exception.setTitle(occurrence.getTitle() + "-changed");
        exception.setIgnoreConflicts(true);
        exception.setStartDate(occurrence.getStartDate());
        exception.setEndDate(occurrence.getEndDate());
        //move the exception one hour
        shiftAppointmentDateOneHour(exception, tz);
        //send exception
        int exceptionId;
        {
            final UpdateRequest request = new UpdateRequest(exception, tz);
            final UpdateResponse response = client.execute(request);
            exceptionId = response.getId();
            appointmentSeries.setLastModified(response.getTimestamp());
        }
        {//get exception
            final GetRequest request = new GetRequest(folderId, exceptionId);
            final GetResponse response = client.execute(request);
            exception = response.getAppointment(tz);
        }
        //move it again
        shiftAppointmentDateOneHour(exception, tz);

        {//send exception again
            exception.setIgnoreConflicts(true);
            final UpdateRequest request = new UpdateRequest(exception, tz);
            final UpdateResponse response = client.execute(request);
            exceptionId = response.getId();
            appointmentSeries.setLastModified(response.getTimestamp());
        }
        {//get exception
            final GetRequest request = new GetRequest(folderId, exceptionId);
            final GetResponse response = client.execute(request);
            exception = response.getAppointment(tz);
        }
        {//assert no duplicate exists
            final AllRequest request = new AllRequest(folderId, new int[] { Appointment.TITLE, Appointment.START_DATE, Appointment.END_DATE }, exception.getStartDate(), exception.getEndDate(), tz, false);
            final CommonAllResponse response = client.execute(request);
            final Object[][] allAppointmentsWithinTimeframe = response.getArray();
            int countOfPotentialDuplicates = 0;
            for (final Object[] arr : allAppointmentsWithinTimeframe) {
                if (null != arr[0] && ((String) arr[0]).startsWith(bugname)) {
                    countOfPotentialDuplicates++;
                }
            }
            assertEquals("Should be only one occurrence of this appointment", Integer.valueOf(1), Integer.valueOf(countOfPotentialDuplicates));
        }
    }

}
