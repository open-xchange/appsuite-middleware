/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.participant.ParticipantTools;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13505Test extends AbstractAJAXSession {

    private int userId;

    private Appointment appointment, updateAppointment;

    private TimeZone tz;

    public Bug13505Test(String name) {
        super(name);
    }

    @Override
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

    public void testBug13505() throws Exception {
        SpecialUpdateRequest updateRequest = new SpecialUpdateRequest(updateAppointment, tz);
        try {
            UpdateResponse updateResponse = client.execute(updateRequest);
            appointment.setLastModified(updateResponse.getTimestamp());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        GetRequest get = new GetRequest(appointment.getParentFolderID(), appointment.getObjectID());
        Appointment loadA = getClient().execute(get).getAppointment(tz);
        assertFalse("No days values expected", loadA.containsDays());
    }

    @Override
    public void tearDown() throws Exception {
        getClient().execute(new DeleteRequest(appointment.getObjectID(), appointment.getParentFolderID(), appointment.getLastModified()));

        super.tearDown();
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
