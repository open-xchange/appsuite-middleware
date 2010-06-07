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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import static com.openexchange.java.Autoboxing.I;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdatesRequest;
import com.openexchange.ajax.appointment.action.UpdatesResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug13960Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug13960Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { Appointment.OBJECT_ID, Appointment.RECURRENCE_ID };
    private AJAXClient client;
    private TimeZone timeZone;
    private Appointment appointment;

    public Bug13960Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = client.getValues().getTimeZone();
        appointment = new Appointment();
        appointment.setTitle("Appointment for bug 13960");
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        Calendar calendar = TimeTools.createCalendar(timeZone);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        InsertRequest request = new InsertRequest(appointment, timeZone);
        AppointmentInsertResponse response = client.execute(request);
        response.fillAppointment(appointment);
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(appointment));
        super.tearDown();
    }

    public void testJSONValues() throws Throwable {
        UpdatesRequest request = new UpdatesRequest(appointment.getParentFolderID(), COLUMNS, new Date(appointment.getLastModified().getTime() - 1), true);
        UpdatesResponse response = client.execute(request);
        int idPos = response.getColumnPos(Appointment.OBJECT_ID);
        int recurrenceIdPos = response.getColumnPos(Appointment.RECURRENCE_ID);
        int row = 0;
        while (row < response.getArray().length) {
            if (response.getArray()[row][idPos].equals(I(appointment.getObjectID()))) {
                break;
            }
        }
        assertEquals(JSONObject.NULL, ((JSONArray) response.getData()).getJSONArray(row).get(recurrenceIdPos));
    }
}
