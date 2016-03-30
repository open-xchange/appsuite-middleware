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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDInt;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug14357Test extends AbstractAJAXSession {

    private Appointment appointment;

    public Bug14357Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setTitle("Bug14357Test");
        appointment.setStartDate(D("01.01.2010 13:00", getClient().getValues().getTimeZone()));
        appointment.setEndDate(D("01.01.2010 14:00", getClient().getValues().getTimeZone()));
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setDays(Appointment.WEEKDAY);
        appointment.setDayInMonth(2);
        appointment.setMonth(Calendar.FEBRUARY);
        appointment.setInterval(1);
        appointment.setIgnoreConflicts(true);

        InsertRequest appointmentInsertRequest = new InsertRequest(appointment, getClient().getValues().getTimeZone());
        AppointmentInsertResponse appointmentInsertResponse = getClient().execute(appointmentInsertRequest);
        appointmentInsertResponse.fillAppointment(appointment);
    }

    public void testBug14357() throws Exception {
        checkYear(1, 2010, 2);
        checkYear(2, 2011, 2);
        checkYear(3, 2012, 2);
        checkYear(4, 2013, 4);
        checkYear(5, 2014, 4);
        checkYear(6, 2015, 3);
        checkYear(7, 2016, 2);
        checkYear(8, 2017, 2);
        checkYear(9, 2018, 2);
        checkYear(10, 2019, 4);
    }

    @Override
    public void tearDown() throws Exception {
        DeleteRequest appointmentDeleteRequest = new DeleteRequest(appointment);
        getClient().execute(appointmentDeleteRequest);

        super.tearDown();
    }

    private void checkYear(int position, int year, int expectedDay) throws Exception {
        Date expectedStartOfappointment = D(expectedDay + ".02." + year + " 13:00");

        ListIDs list = new ListIDs();
        list.add(new ListIDInt(getClient().getValues().getPrivateAppointmentFolder(), getClient().getValues().getUserId()));
        MyListRequest listRequest = new MyListRequest(
            list,
            new int[] { Appointment.OBJECT_ID, Appointment.START_DATE },
            true,
            getClient().getValues().getPrivateAppointmentFolder(),
            position,
            appointment.getObjectID(),
            getClient().getValues().getUserId());

        CommonListResponse listResponse = getClient().execute(listRequest);

        JSONArray foundAppointment = null;
        JSONArray json = (JSONArray) listResponse.getData();
        for (int i = 0; i < json.length(); i++) {
            JSONArray appointmentJson = json.getJSONArray(i);
            if (appointmentJson.getInt(0) == appointment.getObjectID()) {
                foundAppointment = appointmentJson;
            }
        }

        assertNotNull("Did not find appointment.", foundAppointment);
        long startLong = foundAppointment.getLong(1);
        assertEquals("Wrong start date of occurrence.", expectedStartOfappointment.getTime(), startLong);
    }

    private class MyListRequest extends ListRequest {

        private final int pos;

        private final int recId;

        private final int createdBy;

        private final int folder;

        public MyListRequest(ListIDs identifier, int[] columns, boolean failOnError, int folder, int pos, int recId, int createdBy) {
            super(identifier, columns, failOnError);
            this.folder = folder;
            this.pos = pos;
            this.recId = recId;
            this.createdBy = createdBy;
        }

        @Override
        public Object getBody() throws JSONException {
            JSONArray array = new JSONArray();
            JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_INFOLDER, folder);
            json.put(DataFields.ID, recId);
            json.put("recurrence_position", pos);
            json.put("recurrence_id", recId);
            json.put("createdBy", createdBy);
            array.put(json);
            return array;
        }

    }

}
