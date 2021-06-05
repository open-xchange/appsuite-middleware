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

package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * Checks if the data of recurring appointment exceptions is correctly stored
 * and given to the GUI.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12326Test extends AbstractAJAXSession {

    private static final int[] columns = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.RECURRENCE_TYPE, Appointment.RECURRENCE_POSITION, Appointment.RECURRENCE_ID
    };

    /**
     * Default constructor.
     * 
     * @param name test name.
     */
    public Bug12326Test() {
        super();
    }

    /**
     * Creates an appointment series and modifies one appointment of the series
     * to be an exception.
     */
    @Test
    public void testAppointmentException() throws Throwable {
        final int folderId = getClient().getValues().getPrivateAppointmentFolder();
        final TimeZone tz = getClient().getValues().getTimeZone();
        final Appointment series = new Appointment();
        final Calendar calendar = TimeTools.createCalendar(tz);
        {
            series.setTitle("Test for bug 12326");
            series.setParentFolderID(folderId);
            series.setIgnoreConflicts(true);
            // Start and end date.
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            series.setStartDate(calendar.getTime());
            calendar.add(Calendar.HOUR, 1);
            series.setEndDate(calendar.getTime());
            // Configure daily series with 5 occurences
            series.setRecurrenceType(Appointment.DAILY);
            series.setInterval(1);
            series.setOccurrence(5);
        }
        {
            final InsertRequest request = new InsertRequest(series, tz);
            final CommonInsertResponse response = getClient().execute(request);
            series.setObjectID(response.getId());
            series.setLastModified(response.getTimestamp());
        }
        try {
            final int recurrence_position = 3;
            // Load third occurence
            final Appointment occurence;
            {
                final GetRequest request = new GetRequest(folderId, series.getObjectID(), recurrence_position);
                final GetResponse response = getClient().execute(request);
                occurence = response.getAppointment(tz);
                assertEquals("Occurence must have a recurrence position.", recurrence_position, occurence.getRecurrencePosition());
            }
            // Create exception
            final int exceptionId;
            {
                final Appointment exception = new Appointment();
                exception.setObjectID(occurence.getObjectID());
                exception.setParentFolderID(folderId);
                exception.setLastModified(occurence.getLastModified());
                exception.setRecurrencePosition(occurence.getRecurrencePosition());
                exception.setTitle(occurence.getTitle() + "-changed");
                exception.setIgnoreConflicts(true);
                calendar.setTime(occurence.getEndDate());
                exception.setStartDate(calendar.getTime());
                calendar.add(Calendar.HOUR, 1);
                exception.setEndDate(calendar.getTime());
                final UpdateRequest request = new UpdateRequest(exception, tz);
                final UpdateResponse response = getClient().execute(request);
                exceptionId = response.getId();
                series.setLastModified(response.getTimestamp());
            }
            // Check exception in get response
            {
                final GetRequest request = new GetRequest(folderId, exceptionId);
                final GetResponse response = getClient().execute(request);
                final Appointment exception = response.getAppointment(tz);
                series.setLastModified(exception.getLastModified());
                // Check exception
                assertEquals("Exception is still a series.", Appointment.NO_RECURRENCE, exception.getRecurrenceType());
                assertEquals("Exception must have a recurrence position.", occurence.getRecurrencePosition(), exception.getRecurrencePosition());
                assertEquals("Exception is missing reference to series.", series.getObjectID(), exception.getRecurrenceID());
            }
            // Check exception in list response
            {
                final ListIDs ids = ListIDs.l(new int[] { folderId, exceptionId });
                final ListRequest request = new ListRequest(ids, columns);
                final CommonListResponse response = getClient().execute(request);
                final Object[] data = response.getArray()[0];
                assertEquals("Exception is still a series.", Integer.valueOf(Appointment.NO_RECURRENCE), data[2]);
                assertEquals("Exception must have a recurrence position.", Integer.valueOf(occurence.getRecurrencePosition()), data[3]);
                assertEquals("Exception is missing reference to series.", Integer.valueOf(series.getObjectID()), data[4]);
                series.setLastModified(response.getTimestamp());
            }
        } finally {
            getClient().execute(new DeleteRequest(series.getObjectID(), folderId, series.getLastModified()));
        }
    }
}
