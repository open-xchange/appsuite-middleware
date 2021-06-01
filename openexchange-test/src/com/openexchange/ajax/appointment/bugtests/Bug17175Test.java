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
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug17175Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug17175Test extends AbstractAJAXSession {

    private Appointment appointment;
    private Appointment updateAppointment;

    /**
     * Initializes a new {@link Bug17175Test}.
     *
     * @param name
     */
    public Bug17175Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setTitle("Bug 17175 Test");
        appointment.setStartDate(D("07.10.2010 09:00"));
        appointment.setEndDate(D("07.10.2010 10:00"));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.THURSDAY);
        appointment.setInterval(1);
        appointment.setOccurrence(2);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        InsertRequest request = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(request);
        insertResponse.fillObject(appointment);

        updateAppointment = new Appointment();
        insertResponse.fillObject(updateAppointment);
        updateAppointment.setParentFolderID(appointment.getParentFolderID());
        updateAppointment.setRecurrenceType(Appointment.WEEKLY);
        updateAppointment.setDays(Appointment.THURSDAY);
        updateAppointment.setInterval(1);
        updateAppointment.setOccurrence(0);
        updateAppointment.setIgnoreConflicts(true);
    }

    @Test
    public void testBug17175() throws Exception {
        UpdateResponse updateResponse = getClient().execute(new UpdateRequest(updateAppointment, getClient().getValues().getTimeZone()));
        appointment.setLastModified(updateResponse.getTimestamp());

        int[] columns = new int[] { Appointment.OBJECT_ID, Appointment.START_DATE, Appointment.END_DATE };
        AllRequest allRequest = new AllRequest(getClient().getValues().getPrivateAppointmentFolder(), columns, D("01.11.2010 00:00"), D("01.12.2010 00:00"), getClient().getValues().getTimeZone(), false);
        CommonAllResponse allResponse = getClient().execute(allRequest);
        JSONArray json = (JSONArray) allResponse.getData();
        int count = 0;
        for (int i = 0; i < json.length(); i++) {
            if (json.getJSONArray(i).getInt(0) == appointment.getObjectID()) {
                count++;
            }
        }

        assertEquals("Wrong amount of occurrences", 4, count);
    }

}
