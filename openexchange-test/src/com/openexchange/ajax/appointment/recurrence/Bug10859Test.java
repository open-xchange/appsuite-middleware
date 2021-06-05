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

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.Appointment;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug10859Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     * 
     * @param name test name.
     */
    public Bug10859Test() {
        super();
    }

    @Test
    public void testInvalidMonthInRecurringPattern() throws Throwable {
        final AJAXClient client = getClient();
        final int folder = client.getValues().getPrivateAppointmentFolder();
        final TimeZone tz = client.getValues().getTimeZone();
        final Appointment appointment = new Appointment();
        {
            final Calendar calendar = Calendar.getInstance(tz);
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            appointment.setTitle("Test appointment for bug 10859");
            appointment.setParentFolderID(folder);
            appointment.setStartDate(calendar.getTime());
            calendar.add(Calendar.HOUR, 1);
            appointment.setEndDate(calendar.getTime());
            appointment.setRecurrenceType(Appointment.YEARLY);
            appointment.setInterval(1);
            appointment.setMonth(-1); // this was the cause for the endless loop.
            appointment.setDayInMonth(calendar.get(Calendar.DAY_OF_MONTH));
            appointment.setOccurrence(25);
            appointment.setIgnoreConflicts(true);
        }
        {
            final InsertRequest request = new InsertRequest(appointment, tz, false);
            final CommonInsertResponse response = Executor.execute(client, request);
            assertTrue(response.hasError());
        }
    }
}
