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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13505Test extends AbstractAJAXSession {

    private int userId;

    private Appointment appointment, updateAppointment;

    private TimeZone tz;

    public Bug13505Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userId = getClient().getValues().getUserId();
        tz = getClient().getValues().getTimeZone();

        appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Bug 13505 Test");
        appointment.setStartDate(TimeTools.createCalendar(tz, 2009, Calendar.JULY, 6, 12).getTime());
        appointment.setEndDate(TimeTools.createCalendar(tz, 2009, Calendar.JULY, 6, 13).getTime());
        appointment.setParticipants(ParticipantTools.createParticipants(userId));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.MONDAY);
        appointment.setInterval(1);
        appointment.setOccurrence(3);
        appointment.setIgnoreConflicts(true);
        InsertRequest request = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse insertResponse = getClient().execute(request);
        insertResponse.fillObject(appointment);

        updateAppointment = new Appointment();
        insertResponse.fillObject(updateAppointment);
        updateAppointment.setParentFolderID(appointment.getParentFolderID());
        updateAppointment.setRecurrenceType(Appointment.MONTHLY);
        //updateAppointment.setDays(0);
        updateAppointment.setInterval(1);
        updateAppointment.setDayInMonth(1);
        updateAppointment.setOccurrence(3);
        updateAppointment.setIgnoreConflicts(true);
    }

    @Test
    public void testBug13505() throws Exception {
        SpecialUpdateRequest updateRequest = new SpecialUpdateRequest(updateAppointment, tz);
        try {
            UpdateResponse updateResponse = getClient().execute(updateRequest);
            appointment.setLastModified(updateResponse.getTimestamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        GetRequest get = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID());
        Appointment loadA = getClient().execute(get).getAppointment(tz);
        assertFalse("No days values expected", loadA.containsDays());
    }

    protected class SpecialUpdateRequest extends UpdateRequest {

        public SpecialUpdateRequest(Appointment appointmentObj, TimeZone timeZone) {
            super(appointmentObj, timeZone);
        }

        public SpecialUpdateRequest(Appointment appointment, TimeZone timezone, boolean failOnError) {
            super(appointment, timezone, failOnError);
        }

        public SpecialUpdateRequest(int originFolder, Appointment appointment, TimeZone timezone, boolean failOnError) {
            super(originFolder, appointment, timezone, failOnError);
        }

        @Override
        public JSONObject getBody() throws JSONException {
            JSONObject retval = super.getBody();
            if (retval.has("days")) {
                retval.remove("days");
                retval.put("days", JSONObject.NULL);
            }
            return retval;
        }

    }
}
