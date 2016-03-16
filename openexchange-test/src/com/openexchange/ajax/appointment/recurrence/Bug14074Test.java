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

package com.openexchange.ajax.appointment.recurrence;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;

/**
 * Moves a weekly series appointment starting during no daylight saving time in a week with daylight saving time and verifies it is put at
 * the correct time.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug14074Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = { Appointment.OBJECT_ID, Appointment.RECURRENCE_POSITION };

    private TimeZone tz;

    private int folderId;

    private Appointment appointment;

    public Bug14074Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AJAXClient client = getClient();
        tz = client.getValues().getTimeZone();
        folderId = client.getValues().getPrivateAppointmentFolder();
        appointment = createAppointment();
        InsertRequest request = new InsertRequest(appointment, tz);
        AppointmentInsertResponse response = client.execute(request);
        response.fillAppointment(appointment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        DeleteRequest request = new DeleteRequest(appointment);
        getClient().execute(request);
        super.tearDown();
    }

    public void testDailyFullTimeUntil() throws Throwable {
        AJAXClient client = getClient();
        // Change appointment to 1400 in daylight saving time.
        Appointment changed = changeAppointment();
        UpdateResponse response = client.execute(new UpdateRequest(changed, tz));
        appointment.setLastModified(response.getTimestamp());
        // Find appointment with recurrence position.
        Calendar calendar = TimeTools.createCalendar(tz, 2009, 6, 6, 0);
        Date start = calendar.getTime();
        calendar.add(Calendar.DATE, 7);
        Date end = calendar.getTime();
        CommonAllResponse allResponse = client.execute(new AllRequest(folderId, COLUMNS, start, end, tz, false));
        int recurrencePosition = -1;
        for (Object[] obj : allResponse) {
            if (appointment.getObjectID() == ((Integer) obj[0]).intValue()) {
                recurrencePosition = ((Integer) obj[1]).intValue();
            }
        }
        assertFalse("Changed appointment not found.", -1 == recurrencePosition);
        // Load appointment
        GetResponse getResponse = client.execute(new GetRequest(folderId, appointment.getObjectID(), recurrencePosition));
        Appointment toTest = getResponse.getAppointment(tz);
        assertEquals("Start date is not 1400.", changed.getStartDate(), toTest.getStartDate());
        assertEquals("Start date is not 1500.", changed.getEndDate(), toTest.getEndDate());
    }

    private Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setTitle("test for bug 14074");
        appointment.setParentFolderID(folderId);
        Calendar calendar = TimeTools.createCalendar(tz, 2009, 0, 23, 13);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setIgnoreConflicts(true);
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(Appointment.FRIDAY);
        appointment.setIgnoreConflicts(true);
        return appointment;
    }

    private Appointment changeAppointment() {
        Appointment changed = new Appointment();
        changed.setTitle("test for bug 14074 changed");
        changed.setParentFolderID(folderId);
        changed.setObjectID(appointment.getObjectID());
        changed.setLastModified(appointment.getLastModified());
        Calendar calendar = TimeTools.createCalendar(tz, 2009, 6, 10, 14);
        changed.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        changed.setEndDate(calendar.getTime());
        changed.setRecurrenceType(Appointment.WEEKLY);
        changed.setInterval(1);
        changed.setDays(Appointment.FRIDAY);
        changed.setIgnoreConflicts(true);
        return changed;
    }
}
