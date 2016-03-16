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
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.DeleteRequest;
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

    public Bug13501Test(String name) {
        super(name);
    }

    public void testBug13501() throws Exception {
        final InsertRequest request = new InsertRequest(appointment, tz);
        final CommonInsertResponse response = client.execute(request);
        appointment.setObjectID(response.getId());
        appointment.setLastModified(response.getTimestamp());
        update.setObjectID(response.getId());
        update.setLastModified(response.getTimestamp());
        // System.out.println(appointment.getObjectID());

        UpdateRequest updateRequest = new UpdateRequest(update, client.getValues().getTimeZone());
        UpdateResponse updateResponse = client.execute(updateRequest);
        appointment.setLastModified(updateResponse.getTimestamp());

        AllRequest allRequest = new AllRequest(client.getValues().getPrivateAppointmentFolder(), columns, startSearch, endSearch, TimeZone.getTimeZone("UTC"), false);
        CommonAllResponse allResponse = client.execute(allRequest);
        Object[][] objects = allResponse.getArray();
        int count = 0;
        for (Object[] object : objects) {
            if ((Integer)object[0] == appointment.getObjectID()) {
                count++;
            }
        }

        assertEquals("Wrong number of occurrences in this view.", 3, count);

        GetRequest getRequest = new GetRequest(client.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        GetResponse getResponse = client.execute(getRequest);
        Appointment sequenceApp = getResponse.getAppointment(client.getValues().getTimeZone());

        assertEquals("Wrong occurrences value", 5, sequenceApp.getOccurrence());
    }

    @Override
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
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
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

    @Override
    public void tearDown() throws Exception {
        if (appointment.getObjectID() > 0) {
            client.execute(new DeleteRequest(
                appointment.getObjectID(),
                client.getValues().getPrivateAppointmentFolder(),
                appointment.getLastModified()));
        }
        super.tearDown();
    }
}
