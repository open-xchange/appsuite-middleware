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
import static org.junit.Assert.assertEquals;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug18336Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug18336Test extends AbstractAJAXSession {

    private Appointment appointment;

    private Appointment updateAppointment;

    /**
     * Initializes a new {@link Bug18336Test}.
     *
     * @param name
     */
    public Bug18336Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setStartDate(D("25.02.2011 00:00"));
        appointment.setEndDate(D("26.02.2011 00:00"));
        appointment.setFullTime(true);
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.FRIDAY + Appointment.SATURDAY);
        appointment.setInterval(1);
        appointment.setOccurrence(2);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 18336 Test");

        InsertRequest insertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
        insertResponse.fillAppointment(appointment);

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        updateAppointment.setStartDate(new Date(1298588400000L));
        updateAppointment.setEndDate(new Date(1298674800000L));
        updateAppointment.setFullTime(false);
        updateAppointment.setRecurrenceType(Appointment.NO_RECURRENCE);
        updateAppointment.setIgnoreConflicts(true);
        updateAppointment.setLastModified(new Date(Long.MAX_VALUE));
    }

    @Test
    public void testBug18336() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(updateAppointment, getClient().getValues().getTimeZone());
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        updateResponse.fillObject(appointment);

        GetRequest getRequest = new GetRequest(appointment);
        GetResponse getResponse = getClient().execute(getRequest);
        Appointment loadedAppointment = getResponse.getAppointment(getClient().getValues().getTimeZone());

        assertEquals("Wrong start date", updateAppointment.getStartDate(), loadedAppointment.getStartDate());
        assertEquals("Wrong end date", updateAppointment.getEndDate(), loadedAppointment.getEndDate());
    }

}
