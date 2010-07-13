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
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.tools.servlet.AjaxException;

/**
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
        
//        ,"recurrence_position":0,
//        "categories":"","sequence":0,
//        ,"notification":true,
//        "location":"""uid":"cafe3d1c-ab01-4e2f-aaf2-4a7fb2d877c9",
//        "note":null,"modified_by":84,"recurrence_id":56274,"recurrence_start":"578620800000",
//        "number_of_attachments":0,
//       "timezone":"Europe/Berlin","users":[{"confirmation":1,"id":84}],"color_label":0,
//        "created_by":84

        // Set the reminder
        Appointment toUpdate = appointment.clone();
        toUpdate.setAlarm(15);
        UpdateRequest updateReq = new UpdateRequest(toUpdate, timezone, true);
        UpdateResponse updateResp = client.execute(updateReq);
        updateResp.fillObject(appointment);
        
        // Request the reminder
        Calendar cal = (Calendar) calendar.clone();
        int diff = 24 - cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.HOUR_OF_DAY, diff);
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
        
        // Request Alarm for 
        
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
        super.tearDown();
    }
    
    private Appointment createSeriesInThePast() throws Exception {
        Calendar cal = TimeTools.createCalendar(timezone);
        final Appointment appointmentObj = new Appointment();

        appointmentObj.setTitle("testBug15776SeriesInThePast");
        cal.add(Calendar.YEAR, -5);
        cal.add(Calendar.MINUTE, 10);
        appointmentObj.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(cal.getTime());

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setNote("");
        appointmentObj.setNotification(true);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(false);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setMonth(cal.get(Calendar.MONTH));
        appointmentObj.setDayInMonth(cal.get(Calendar.DAY_OF_MONTH));
        
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(4);

        final UserParticipant newParticipant = new UserParticipant(client.getValues().getUserId());
        appointmentObj.addParticipant(newParticipant);

        final InsertRequest insReq = new InsertRequest(appointmentObj, timezone, true);
        final AppointmentInsertResponse insResp = client.execute(insReq);
        insResp.fillAppointment(appointmentObj);

        return appointmentObj;
    }
    
    private Appointment createAppointment() throws AjaxException, IOException, SAXException, JSONException {
        Calendar cal = TimeTools.createCalendar(timezone);
        cal.add(Calendar.YEAR, -5);
        cal.add(Calendar.HOUR_OF_DAY, 2);
        final Appointment appointmentObj = new Appointment();

        appointmentObj.setTitle("testBug15776");
        appointmentObj.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(cal.getTime());

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setNote("");
        appointmentObj.setNotification(true);
        appointmentObj.setPrivateFlag(false);
        appointmentObj.setFullTime(false);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setMonth(cal.get(Calendar.MONTH));
        appointmentObj.setDayInMonth(cal.get(Calendar.DAY_OF_MONTH));
        
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(25);

        final UserParticipant newParticipant = new UserParticipant(client.getValues().getUserId());
        appointmentObj.addParticipant(newParticipant);

        final InsertRequest insReq = new InsertRequest(appointmentObj, timezone, true);
        final AppointmentInsertResponse insResp = client.execute(insReq);
        insResp.fillAppointment(appointmentObj);

        return appointmentObj;
    }
    
    
    
}
