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
import java.util.Calendar;
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
 * {@link Bug17535Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug17535Test extends AbstractAJAXSession {

    private Appointment appointment;
    private Appointment updateAppointment;

    /**
     * Initializes a new {@link Bug17535Test}.
     *
     * @param name
     */
    public Bug17535Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setStartDate(D("19.01.2010 00:00"));
        appointment.setEndDate(D("20.01.2010 00:00"));
        appointment.setFullTime(true);
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setDayInMonth(19);
        appointment.setMonth(Calendar.JANUARY);
        appointment.setInterval(1);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug 17599 Test");

        InsertRequest insertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(insertRequest);
        insertResponse.fillAppointment(appointment);

        updateAppointment = new Appointment();
        updateAppointment.setObjectID(appointment.getObjectID());
        updateAppointment.setStartDate(D("19.01.2010 00:00"));
        //updateAppointment.setEndDate(D("20.01.2010 00:00"));
        updateAppointment.setFullTime(true);
        updateAppointment.setRecurrenceType(Appointment.YEARLY);
        updateAppointment.setDayInMonth(19);
        updateAppointment.setMonth(Calendar.JANUARY);
        updateAppointment.setInterval(1);
        updateAppointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        updateAppointment.setIgnoreConflicts(true);
        updateAppointment.setTitle("Bug 17599 Test");
        updateAppointment.setLabel(3);
        updateAppointment.setCategories("Burtstage");
        updateAppointment.setShownAs(Appointment.FREE);
        updateAppointment.setLastModified(new Date(Long.MAX_VALUE));
    }

    @Test
    public void testBug17599() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest(updateAppointment, getClient().getValues().getTimeZone());
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        updateResponse.fillObject(appointment);

        GetRequest getRequest = new GetRequest(appointment);
        GetResponse getResponse = getClient().execute(getRequest);
        Appointment loadedAppointment = getResponse.getAppointment(getClient().getValues().getTimeZone());
        assertEquals("Wrong recurrence type", Appointment.YEARLY, loadedAppointment.getRecurrenceType());
        //assertEquals("Wrong end date", appointment.getEndDate(), loadedAppointment.getEndDate());
    }

}
