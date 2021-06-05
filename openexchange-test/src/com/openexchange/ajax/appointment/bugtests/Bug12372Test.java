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

import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12372Test extends AbstractAJAXSession {

    /**
     * @param name test name.
     */
    public Bug12372Test() {
        super();
    }

    @Test
    public void testDeleteOfStrangeApp() throws Throwable {
        final TimeZone tz = getClient().getValues().getTimeZone();
        final Appointment appointment = new Appointment();
        appointment.setTitle("bug 12372 test");
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        final Calendar calendar = TimeTools.createCalendar(tz);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        appointment.setStartDate(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        appointment.setEndDate(calendar.getTime());
        // Recurrence start should be yesterday.
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        appointment.setRecurringStart(calendar.getTimeInMillis());
        appointment.setOccurrence(7);
        appointment.setRecurrenceID(1868);
        final InsertRequest request = new InsertRequest(appointment, tz);
        final CommonInsertResponse response = getClient().execute(request);
        response.fillObject(appointment);
        getClient().execute(new DeleteRequest(appointment));
    }
}
