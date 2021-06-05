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
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.AbstractResourceAwareAjaxSession;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.resource.ResourceTools;
import com.openexchange.calendar.ConflictTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug15585Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15585Test extends AbstractResourceAwareAjaxSession {

    private Appointment appointment;
    private Appointment appointment2;
    private TimeZone timeZone;

    public Bug15585Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        timeZone = getClient().getValues().getTimeZone();
        appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Test for bug 15585");
        appointment.setIgnoreConflicts(true);
        final Calendar calendar = TimeTools.createCalendar(timeZone);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new ResourceParticipant(ResourceTools.getSomeResource(getClient())));
        appointment2 = appointment.clone();
        InsertRequest request = new InsertRequest(appointment, timeZone);
        AppointmentInsertResponse response = getClient().execute(request);
        response.fillAppointment(appointment);
    }

    @Test
    public void testConflictTitle() throws Throwable {
        InsertRequest request = new InsertRequest(appointment2, timeZone);
        AppointmentInsertResponse response = getClient().execute(request);
        assertTrue("Resource hard conflict expected.", response.hasConflicts());
        response.getConflicts();
        ConflictObject conflict = ConflictTools.findById(response.getConflicts(), appointment.getObjectID());
        assertEquals("Title of my appointment is not readable.", appointment.getTitle(), conflict.getTitle());
    }
}
