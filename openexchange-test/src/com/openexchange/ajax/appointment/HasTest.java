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

package com.openexchange.ajax.appointment;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.HasRequest;
import com.openexchange.ajax.appointment.action.HasResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

public class HasTest extends AbstractAJAXSession {

    private int folderId;

    private TimeZone tz;

    public HasTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateAppointmentFolder();
        tz = getClient().getValues().getTimeZone();
    }

    @Test
    public void testHasAppointment() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 9); // Not using start of day because of daylight saving time shifting problem.
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        final int hasInterval = 7;
        final Date start = c.getTime();
        c.add(Calendar.DATE, hasInterval);
        final Date end = c.getTime();

        final Appointment appointment = new Appointment();
        appointment.setTitle("testHasAppointment");
        c.setTime(start);
        final int posInArray = 3;
        c.add(Calendar.DATE, posInArray);
        appointment.setStartDate(c.getTime());
        c.add(Calendar.HOUR, 1);
        appointment.setEndDate(c.getTime());
        appointment.setShownAs(Appointment.ABSENT);
        appointment.setParentFolderID(folderId);
        appointment.setIgnoreConflicts(true);
        final AppointmentInsertResponse insertR = getClient().execute(new InsertRequest(appointment, tz));
        insertR.fillAppointment(appointment);
        try {
            final HasResponse hasR = getClient().execute(new HasRequest(start, end, tz));
            final boolean[] hasAppointments = hasR.getValues();
            assertEquals("Length of array of action has is wrong.", hasInterval, hasAppointments.length);
            assertEquals("Inserted appointment not found in action has response.", B(hasAppointments[posInArray]), Boolean.TRUE);
        } finally {
            getClient().execute(new DeleteRequest(appointment));
        }
    }

    @Test
    public void testHasAppointmentFullTime() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        final int hasInterval = 7;
        final Date start = c.getTime();
        c.add(Calendar.DATE, hasInterval);
        final Date end = c.getTime();

        final Appointment appointment = new Appointment();
        appointment.setTitle("testHasAppointmentFullTime");
        c.setTime(start);
        final int posInArray = 3;
        c.add(Calendar.DATE, posInArray);
        appointment.setStartDate(c.getTime());
        c.add(Calendar.DATE, 1);
        appointment.setEndDate(c.getTime());
        appointment.setShownAs(Appointment.ABSENT);
        appointment.setFullTime(true);
        appointment.setParentFolderID(folderId);
        appointment.setIgnoreConflicts(true);
        final AppointmentInsertResponse insertR = getClient().execute(new InsertRequest(appointment, tz));
        insertR.fillAppointment(appointment);
        try {
            final HasResponse hasR = getClient().execute(new HasRequest(start, end, tz));
            final boolean[] hasAppointments = hasR.getValues();
            assertEquals("Length of array of action has is wrong.", hasInterval, hasAppointments.length);
            assertEquals("Inserted appointment not found in action has response.", B(hasAppointments[posInArray]), Boolean.TRUE);
        } finally {
            getClient().execute(new DeleteRequest(appointment));
        }
    }
}
