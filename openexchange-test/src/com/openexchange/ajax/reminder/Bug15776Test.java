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
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * This test checks the correctness of reminders that are set to appointment series after they are created.
 * The series starts in the past and ends in the future. Another series even ends in the past.
 * If a series has occurrences in the future a reminder should be set for the next occurring appointment.
 * If a series even ends in the past it should not be possible to set an reminder.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug15776Test extends AbstractAJAXSession {
    AJAXClient client;
    Appointment appointment;
    Appointment pastAppointment;
    TimeZone timezone;
    Calendar calendar;

    public Bug15776Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = super.getClient();
        timezone = client.getValues().getTimeZone();
        calendar = Calendar.getInstance(timezone);
        appointment = createAppointment();
        pastAppointment = createSeriesInThePast();
    }

    public void testReminder() throws Exception {
        // Set the reminder
        Appointment toUpdate = appointment.clone();
        toUpdate.setAlarm(15);
        UpdateRequest updateReq = new UpdateRequest(toUpdate, timezone, true);
        UpdateResponse updateResp = client.execute(updateReq);
        updateResp.fillObject(appointment);

        // Request all upcoming reminders until the end of the appointments current occurrence and look for the desired one
        Calendar cal = Calendar.getInstance(timezone);
        cal.setTime(appointment.getEndDate());
        cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        RangeRequest rangeReq = new RangeRequest(cal.getTime());
        RangeResponse rangeResp = client.execute(rangeReq);
        ReminderObject reminder = ReminderTools.searchByTarget(rangeResp.getReminder(timezone), appointment.getObjectID());

        assertNotNull("No reminder was found.", reminder);

        Calendar checkCal = (Calendar) calendar.clone();
        checkCal.setTime(reminder.getDate());
        assertEquals("Reminder is set to the wrong appointment.", cal.get(Calendar.YEAR), calendar.get(Calendar.YEAR));

        // Request reminder for the next occurrence
        cal.add(Calendar.YEAR, 1);
        com.openexchange.ajax.reminder.actions.DeleteRequest delReminderReq = new com.openexchange.ajax.reminder.actions.DeleteRequest(reminder, false);
        client.execute(delReminderReq);
        rangeReq = new RangeRequest(cal.getTime());
        rangeResp = client.execute(rangeReq);
        reminder = ReminderTools.searchByTarget(rangeResp.getReminder(timezone), appointment.getObjectID());

        assertNotNull("No reminder was found.", reminder);
    }

    public void testAlarmForReminderInThePast() throws Exception {
        // Set the reminder
        pastAppointment.setAlarm(15);
        UpdateRequest updateReq = new UpdateRequest(pastAppointment, timezone, false);
        UpdateResponse updateResp = client.execute(updateReq);
        updateResp.fillObject(pastAppointment);

        GetRequest getApp = new GetRequest(pastAppointment);
        GetResponse getAppResp = client.execute(getApp);
        Appointment app = getAppResp.getAppointment(timezone);

        assertFalse("Series in the past contains alarm.", app.containsAlarm());
    }

    @Override
    public void tearDown() throws Exception {
        Appointment toDelete = client.execute(new GetRequest(appointment, false)).getAppointment(timezone);
        client.execute(new DeleteRequest(toDelete, false));
        toDelete = client.execute(new GetRequest(pastAppointment, false)).getAppointment(timezone);
        client.execute(new DeleteRequest(toDelete, false));
        super.tearDown();
    }

    private Appointment createSeriesInThePast() throws Exception {
        // This yearly series starts 5 years ago.
        // The last occurrence was 1 year ago.
        Calendar cal = TimeTools.createCalendar(timezone);
        final Appointment appointment = new Appointment();

        appointment.setTitle("testBug15776SeriesInThePast");
        cal.add(Calendar.YEAR, -5);
        cal.add(Calendar.MINUTE, 10);
        appointment.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        appointment.setEndDate(cal.getTime());

        appointment.setShownAs(Appointment.ABSENT);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setNote("");
        appointment.setNotification(true);
        appointment.setPrivateFlag(false);
        appointment.setFullTime(false);
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setMonth(cal.get(Calendar.MONTH));
        appointment.setDayInMonth(cal.get(Calendar.DAY_OF_MONTH));

        appointment.setInterval(1);
        appointment.setOccurrence(4);

        final UserParticipant newParticipant = new UserParticipant(client.getValues().getUserId());
        appointment.addParticipant(newParticipant);

        final InsertRequest insReq = new InsertRequest(appointment, timezone, true);
        final AppointmentInsertResponse insResp = client.execute(insReq);
        insResp.fillAppointment(appointment);

        return appointment;
    }

    private Appointment createAppointment() throws OXException, IOException, SAXException, JSONException {
        Calendar cal = TimeTools.createCalendar(timezone);

        // This yearly series starts 5 years ago.
        // The next occurrence will be in 2 hours and will last for one hour.
        // The last occurrence will be in 15 years and 2 hours.
        cal.add(Calendar.YEAR, -5);
        cal.add(Calendar.HOUR_OF_DAY, 2);
        final Appointment appointment = new Appointment();

        appointment.setTitle("testBug15776");
        appointment.setStartDate(cal.getTime());
        appointment.setRecurrenceType(Appointment.YEARLY);
        appointment.setMonth(cal.get(Calendar.MONTH));
        appointment.setDayInMonth(cal.get(Calendar.DAY_OF_MONTH));
        cal.add(Calendar.HOUR, 1);
        appointment.setEndDate(cal.getTime());

        appointment.setShownAs(Appointment.ABSENT);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        appointment.setNote("");
        appointment.setNotification(true);
        appointment.setPrivateFlag(false);
        appointment.setFullTime(false);

        appointment.setInterval(1);
        appointment.setOccurrence(25);

        final UserParticipant newParticipant = new UserParticipant(client.getValues().getUserId());
        appointment.addParticipant(newParticipant);

        final InsertRequest insReq = new InsertRequest(appointment, timezone, true);
        final AppointmentInsertResponse insResp = client.execute(insReq);
        insResp.fillAppointment(appointment);

        return appointment;
    }



}
