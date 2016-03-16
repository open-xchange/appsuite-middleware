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

package com.openexchange.ajax.reminder;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.reminder.ReminderObject;

public class Bug7590Test extends AbstractAJAXSession {

    private static final int alarmMinutes = 60;

    private AJAXClient client;

    private TimeZone tz;

    private int folderId;

    private Calendar calendar;

    private Appointment appointment;

    public Bug7590Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        tz = client.getValues().getTimeZone();
        folderId = client.getValues().getPrivateAppointmentFolder();
        calendar = TimeTools.createCalendar(tz);
        appointment = createAppointment();
    }

    @Override
    protected void tearDown() throws Exception {
        client.execute(new DeleteRequest(appointment));
        super.tearDown();
    }

    public void testBug7590() throws Exception {
        final RangeRequest request = new RangeRequest(appointment.getEndDate());
        final RangeResponse response = client.execute(request);

        ReminderObject actual = null;
        for (ReminderObject reminder : response.getReminder(tz)) {
            if (appointment.getObjectID() == reminder.getTargetId()) {
                actual = reminder;
                break;
            }
        }
        assertNotNull("No reminder found for created appointment.", actual);

        final ReminderObject expected = new ReminderObject();
        @SuppressWarnings("null")
        int reminderId = actual.getObjectId();
        expected.setObjectId(reminderId);
        expected.setFolder(folderId);
        expected.setTargetId(appointment.getObjectID());
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.MINUTE, -alarmMinutes);
        expected.setDate(calendar.getTime());
        ReminderTest.compareReminder(expected, actual);
    }

    private Appointment createAppointment() throws OXException, IOException, SAXException, JSONException {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testBug7590");

        // Start date must be in the future
        calendar.add(Calendar.HOUR, 1);
        appointmentObj.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(calendar.getTime());
        appointmentObj.setAlarm(alarmMinutes);

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(folderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(3);
        appointmentObj.setIgnoreConflicts(true);

        final AppointmentInsertResponse insertR = client.execute(new InsertRequest(appointmentObj, tz));
        insertR.fillAppointment(appointmentObj);
        return appointmentObj;
    }
}
