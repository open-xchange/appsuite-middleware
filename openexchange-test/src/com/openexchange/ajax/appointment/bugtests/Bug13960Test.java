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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.AppointmentUpdatesResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.ListRequest;
import com.openexchange.ajax.appointment.action.UpdatesRequest;
import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug13960Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug13960Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { Appointment.OBJECT_ID, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION };
    private TimeZone timeZone;
    private Appointment appointment;

    public Bug13960Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        timeZone = getClient().getValues().getTimeZone();
        appointment = new Appointment();
        appointment.setTitle("Appointment for bug 13960");
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        Calendar calendar = TimeTools.createCalendar(timeZone);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        InsertRequest request = new InsertRequest(appointment, timeZone);
        AppointmentInsertResponse response = getClient().execute(request);
        response.fillAppointment(appointment);
    }

    @Test
    public void testJSONValues() throws Throwable {
        {
            GetRequest request = new GetRequest(appointment);
            GetResponse response = getClient().execute(request);
            JSONObject json = (JSONObject) response.getData();
            assertFalse(json.has(AppointmentFields.RECURRENCE_ID));
            assertFalse(json.has(AppointmentFields.RECURRENCE_POSITION));
        }
        {
            UpdatesRequest request = new UpdatesRequest(appointment.getParentFolderID(), COLUMNS, new Date(appointment.getLastModified().getTime() - 1), true);
            AppointmentUpdatesResponse response = getClient().execute(request);
            int idPos = response.getColumnPos(Appointment.OBJECT_ID);
            int row = 0;
            while (row < response.getArray().length) {
                if (response.getArray()[row][idPos].equals(I(appointment.getObjectID()))) {
                    break;
                }
                row++;
            }
            JSONArray array = ((JSONArray) response.getData()).getJSONArray(row);
            int recurrenceIdPos = response.getColumnPos(Appointment.RECURRENCE_ID);
            assertEquals(JSONObject.NULL, array.get(recurrenceIdPos));
            int recurrencePositionPos = response.getColumnPos(Appointment.RECURRENCE_POSITION);
            assertEquals(JSONObject.NULL, array.get(recurrencePositionPos));
        }
        {
            ListRequest request = new ListRequest(ListIDs.l(new int[] { appointment.getParentFolderID(), appointment.getObjectID() }), COLUMNS);
            CommonListResponse response = getClient().execute(request);
            JSONArray array = ((JSONArray) response.getData()).getJSONArray(0);
            int recurrenceIdPos = response.getColumnPos(Appointment.RECURRENCE_ID);
            assertEquals(JSONObject.NULL, array.get(recurrenceIdPos));
            int recurrencePositionPos = response.getColumnPos(Appointment.RECURRENCE_POSITION);
            assertEquals(JSONObject.NULL, array.get(recurrencePositionPos));
        }
        {
            Date start = TimeTools.getAPIDate(timeZone, appointment.getStartDate(), 0);
            Date end = TimeTools.getAPIDate(timeZone, appointment.getEndDate(), 1);
            AllRequest request = new AllRequest(appointment.getParentFolderID(), COLUMNS, start, end, timeZone);
            CommonAllResponse response = getClient().execute(request);
            int idPos = response.getColumnPos(Appointment.OBJECT_ID);
            int row = 0;
            while (row < response.getArray().length) {
                if (response.getArray()[row][idPos].equals(I(appointment.getObjectID()))) {
                    break;
                }
                row++;
            }
            JSONArray array = ((JSONArray) response.getData()).getJSONArray(row);
            int recurrenceIdPos = response.getColumnPos(Appointment.RECURRENCE_ID);
            assertEquals(JSONObject.NULL, array.get(recurrenceIdPos));
            int recurrencePositionPos = response.getColumnPos(Appointment.RECURRENCE_POSITION);
            assertEquals(JSONObject.NULL, array.get(recurrencePositionPos));
        }
    }
}
