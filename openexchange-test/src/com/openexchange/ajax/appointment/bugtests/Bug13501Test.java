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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;

public class Bug13501Test extends AbstractAJAXSession {

    private TimeZone tz;

    private Appointment appointment, update;

    private int[] columns;

    private Date startSearch, endSearch;

    public Bug13501Test() {
        super();
    }

    @Test
    public void testBug13501() throws Exception {
        final InsertRequest request = new InsertRequest(appointment, tz);
        final CommonInsertResponse response = getClient().execute(request);
        appointment.setObjectID(response.getId());
        appointment.setLastModified(response.getTimestamp());
        update.setObjectID(response.getId());
        update.setLastModified(response.getTimestamp());
        // System.out.println(appointment.getObjectID());

        UpdateRequest updateRequest = new UpdateRequest(update, getClient().getValues().getTimeZone());
        UpdateResponse updateResponse = getClient().execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());

        AllRequest allRequest = new AllRequest(getClient().getValues().getPrivateAppointmentFolder(), columns, startSearch, endSearch, TimeZone.getTimeZone("UTC"), false);
        CommonAllResponse allResponse = getClient().execute(allRequest);
        Object[][] objects = allResponse.getArray();
        int count = 0;
        for (Object[] object : objects) {
            if (((Integer) object[0]).intValue() == appointment.getObjectID()) {
                count++;
            }
        }

        assertEquals("Wrong number of occurrences in this view.", 3, count);

        GetRequest getRequest = new GetRequest(getClient().getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        GetResponse getResponse = getClient().execute(getRequest);
        Appointment sequenceApp = getResponse.getAppointment(getClient().getValues().getTimeZone());

        assertEquals("Wrong occurrences value", 5, sequenceApp.getOccurrence());
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        tz = getClient().getValues().getTimeZone();

        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 15);
        start.set(Calendar.MONTH, Calendar.JUNE);
        start.set(Calendar.YEAR, 2009);
        start.set(Calendar.HOUR_OF_DAY, 8);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(start.getTimeInMillis() + 3600000);

        appointment = new Appointment();
        appointment.setTitle("bug 13501 test");
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(start.getTime());
        appointment.setEndDate(end.getTime());
        appointment.setRecurrenceType(CalendarObject.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(5);

        Calendar until = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        until.set(Calendar.YEAR, 2009);
        until.set(Calendar.HOUR_OF_DAY, 0);
        until.set(Calendar.MINUTE, 0);
        until.set(Calendar.SECOND, 0);
        until.set(Calendar.MILLISECOND, 0);
        until.set(Calendar.DAY_OF_MONTH, 13);
        until.set(Calendar.MONTH, Calendar.JULY);
        until.set(Calendar.YEAR, 2009);

        update = new Appointment();
        update.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        update.setRecurrenceType(CalendarObject.WEEKLY);
        update.setDays(CalendarObject.MONDAY);
        update.setOccurrence(5);
        update.setInterval(1);

        columns = new int[] { Appointment.OBJECT_ID, Appointment.RECURRENCE_COUNT, Appointment.START_DATE, Appointment.END_DATE };

        startSearch = new Date(1246233600000L); // 29.06.2009 00:00:00
        endSearch = new Date(1249257600000L); // 03.08.2009 00:00:00
    }

}
