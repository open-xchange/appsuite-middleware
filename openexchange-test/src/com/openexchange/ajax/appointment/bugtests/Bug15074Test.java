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

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug15074Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug15074Test extends AbstractAJAXSession {

    private Appointment appointment;

    /**
     * Initializes a new {@link Bug15074Test}.
     *
     * @param name
     */
    public Bug15074Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 15074 Test");
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setStartDate(D("07.12.2007 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setEndDate(D("08.12.2007 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setFullTime(true);
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setInterval(1);
        appointment.setDayInMonth(7);
        appointment.setMonth(Calendar.DECEMBER);
        appointment.setIgnoreConflicts(true);

        InsertRequest appointmentInsertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse appointmentInsertResponse = getClient().execute(appointmentInsertRequest);
        appointmentInsertResponse.fillAppointment(appointment);
    }

    @Test
    public void testBug() throws Exception {

        int[] columns = new int[] { Appointment.OBJECT_ID };

        AllRequest allRequest = new AllRequest(getClient().getValues().getPrivateAppointmentFolder(), columns, D("01.12.2009 00:00", TimeZone.getTimeZone("UTC")), D("01.01.2010 00:00", TimeZone.getTimeZone("UTC")), TimeZone.getTimeZone("UTC"), false);

        CommonAllResponse allResponse = getClient().execute(allRequest);
        Object[][] objects = allResponse.getArray();
        boolean found = false;
        for (Object[] object : objects) {
            if (((Integer) object[0]).intValue() == appointment.getObjectID()) {
                found = true;
                break;
            }
        }

        assertTrue("Unable to find appointment in this month.", found);

    }

}
