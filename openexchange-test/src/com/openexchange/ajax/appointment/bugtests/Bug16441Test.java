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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug16441Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug16441Test extends AbstractAJAXSession {

    private Appointment appointment;

    public Bug16441Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        appointment = new Appointment();
        appointment.setStartDate(D("16.04.2010 07:00"));
        appointment.setEndDate(D("16.04.2010 08:00"));
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("bug 16441 test");
        appointment.setRecurrenceType(Appointment.MONTHLY);
        appointment.setInterval(1);
        appointment.setUntil(D("31.12.2010 00:00"));
        appointment.setDayInMonth(1);
        appointment.setDays(Appointment.FRIDAY);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
    }

    @Test
    public void testBug16441() throws Exception {
        AppointmentInsertResponse response = getClient().execute(new InsertRequest(appointment, getClient().getValues().getTimeZone()));
        response.fillAppointment(appointment);
        getClient().execute(new DeleteRequest(appointment.getObjectID(), appointment.getParentFolderID(), 5, appointment.getLastModified()));
    }

}
