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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.java.util.TimeZones;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link ConfirmationTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ConfirmationTest extends CalDAVTest {

    private CalendarTestManager manager2;

    @Before
    public void setUp() throws Exception {
        manager2 = new CalendarTestManager(new AJAXClient(User.User2));
        manager2.setFailOnError(true);
    }

    @After
    public void tearDown() throws Exception {
        if (null != this.manager2) {
            this.manager2.cleanUp();
            if (null != manager2.getClient()) {
                manager2.getClient().logout();
            }
        }
    }

    @Test
    public void testConfirmSeriesOnClient() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment as user 2 on server
         */
        String uid = randomUID();
        String summary = "serie";
        String location = "test";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        List<Participant> participants = new ArrayList<Participant>();
        participants.add(new UserParticipant(getClient().getValues().getUserId()));
        participants.add(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.setParticipants(participants);
        appointment.setParentFolderID(manager2.getPrivateFolder());
        appointment.setIgnoreConflicts(true);
        manager2.insert(appointment);
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendee);
        assertTrue("PARTSTAT wrong", null == attendee.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendee.getAttribute("PARTSTAT")));
        /*
         * accept series on client
         */
        attendee.getAttributes().put("PARTSTAT", "ACCEPTED");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertNotNull("no users found in apointment", appointment.getUsers());
        UserParticipant user = null;
        for (UserParticipant participant : appointment.getUsers()) {
            if (client.getValues().getUserId() == participant.getIdentifier()) {
                user = participant;
                break;
            }
        }
        assertNotNull("User not found", user);
        assertEquals("Confirm status wrong", Appointment.ACCEPT, user.getConfirm());
        /*
         * verify appointment on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendee);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendee.getAttribute("PARTSTAT"));
    }

    @Test
    public void testConfirmSeriesOnServer() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment as user 2 on server
         */
        String uid = randomUID();
        String summary = "serie";
        String location = "test";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        List<Participant> participants = new ArrayList<Participant>();
        participants.add(new UserParticipant(getClient().getValues().getUserId()));
        participants.add(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.setParticipants(participants);
        appointment.setParentFolderID(manager2.getPrivateFolder());
        appointment.setIgnoreConflicts(true);
        manager2.insert(appointment);
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendee);
        assertTrue("PARTSTAT wrong", null == attendee.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendee.getAttribute("PARTSTAT")));
        /*
         * accept series on server
         */
        appointment = getAppointment(uid);
        getManager().confirm(appointment, Appointment.ACCEPT, "ok");
        /*
         * verify appointment on server
         */
        appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertNotNull("no users found in apointment", appointment.getUsers());
        UserParticipant user = null;
        for (UserParticipant participant : appointment.getUsers()) {
            if (client.getValues().getUserId() == participant.getIdentifier()) {
                user = participant;
                break;
            }
        }
        assertNotNull("User not found", user);
        assertEquals("Confirm status wrong", Appointment.ACCEPT, user.getConfirm());
        /*
         * verify appointment on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendee);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendee.getAttribute("PARTSTAT"));
    }

    @Test
    public void testConfirmOccurrenceOnClient() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment as user 2 on server
         */
        String uid = randomUID();
        String summary = "serie";
        String location = "test";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setTimezone("Europe/Berlin");
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        List<Participant> participants = new ArrayList<Participant>();
        participants.add(new UserParticipant(getClient().getValues().getUserId()));
        participants.add(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.setParticipants(participants);
        appointment.setParentFolderID(manager2.getPrivateFolder());
        appointment.setIgnoreConflicts(true);
        manager2.insert(appointment);
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendee);
        assertTrue("PARTSTAT of series wrong", null == attendee.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendee.getAttribute("PARTSTAT")));
        /*
         * accept occurrence on client
         */
        Component exception = new Component("VEVENT");
        for (Property property : iCalResource.getVEvent().getProperties()) {
            exception.getProperties().add(new Property(property.toString()));
        }
        attendee = exception.getAttendee(getClient().getValues().getDefaultAddress());
        attendee.getAttributes().put("PARTSTAT", "ACCEPTED");
        Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.DAY_OF_YEAR, 5);
        Date exceptionStartDate = calendar.getTime();
        calendar.setTime(appointment.getEndDate());
        calendar.add(Calendar.DAY_OF_YEAR, 5);
        Date exceptionEndDate = calendar.getTime();
        exception.getProperties().add(new Property(
            "RECURRENCE-ID;TZID=" + appointment.getTimezone() + ":" + format(exceptionStartDate, appointment.getTimezone())));
        iCalResource.addComponent(exception);
        exception.setDTStart(exceptionStartDate);
        exception.setDTEnd(exceptionEndDate);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertNotNull("no users found in apointment", appointment.getUsers());
        UserParticipant user = null;
        for (UserParticipant participant : appointment.getUsers()) {
            if (client.getValues().getUserId() == participant.getIdentifier()) {
                user = participant;
                break;
            }
        }
        assertNotNull("User not found", user);
        assertEquals("Confirm status of series wrong", Appointment.NONE, user.getConfirm());
        assertNotNull("No change exceptions found", appointment.getChangeException());
        assertEquals("Invalid number of change exceptions found", 1, appointment.getChangeException().length);
        List<Appointment> changeExceptions = getManager().getChangeExceptions(
            appointment.getParentFolderID(), appointment.getObjectID(), Appointment.ALL_COLUMNS);
        assertNotNull("no change exceptions found", changeExceptions);
        assertEquals("Invalid number of change exceptions found", 1, changeExceptions.size());
        Appointment changeException = getManager().get(appointment.getParentFolderID(), changeExceptions.get(0).getObjectID());
        assertNotNull("change exception not found", changeException);
        assertEquals("Invalid start date of change exception", exceptionStartDate, changeException.getStartDate());
        assertNotNull("no users found in apointment", changeException.getUsers());
        user = null;
        for (UserParticipant participant : changeException.getUsers()) {
            if (client.getValues().getUserId() == participant.getIdentifier()) {
                user = participant;
                break;
            }
        }
        assertNotNull("User not found", user);
        assertEquals("Confirm status of change exception wrong", Appointment.ACCEPT, user.getConfirm());
        /*
         * verify appointment on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(eTags.keySet());
        assertEquals("no exception found in iCal", 2, iCalResource.getVEvents().size());
        Component seriesVEvent;
        Component exceptionVEvent;
        if (null == iCalResource.getVEvents().get(0).getProperty("RECURRENCE-ID")) {
            seriesVEvent = iCalResource.getVEvents().get(0);
            exceptionVEvent = iCalResource.getVEvents().get(1);
        } else {
            seriesVEvent = iCalResource.getVEvents().get(1);
            exceptionVEvent = iCalResource.getVEvents().get(0);
        }
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        attendee = seriesVEvent.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in series iCal", attendee);
        assertTrue("PARTSTAT of series wrong", null == attendee.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendee.getAttribute("PARTSTAT")));
        attendee = exceptionVEvent.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in exception iCal", attendee);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendee.getAttribute("PARTSTAT"));
    }

    @Test
    public void testConfirmOccurrenceOnServer() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment as user 2 on server
         */
        String uid = randomUID();
        String summary = "serie";
        String location = "test";
        Date start = TimeTools.D("next friday at 11:30");
        Date end = TimeTools.D("next friday at 12:45");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setTimezone("Europe/Berlin");
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        List<Participant> participants = new ArrayList<Participant>();
        participants.add(new UserParticipant(getClient().getValues().getUserId()));
        participants.add(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.setParticipants(participants);
        appointment.setParentFolderID(manager2.getPrivateFolder());
        appointment.setIgnoreConflicts(true);
        manager2.insert(appointment);
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        Property attendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in iCal", attendee);
        assertTrue("PARTSTAT of series wrong", null == attendee.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendee.getAttribute("PARTSTAT")));
        /*
         * accept occurrence on server
         */
        appointment = getAppointment(uid);
        Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.setTime(appointment.getStartDate());
        calendar.add(Calendar.DAY_OF_YEAR, 5);
        Date exceptionStartDate = calendar.getTime();
        getManager().confirm(appointment, Appointment.ACCEPT, "ok", 6);
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertNotNull("no users found in apointment", appointment.getUsers());
        UserParticipant user = null;
        for (UserParticipant participant : appointment.getUsers()) {
            if (client.getValues().getUserId() == participant.getIdentifier()) {
                user = participant;
                break;
            }
        }
        assertNotNull("User not found", user);
        assertEquals("Confirm status of series wrong", Appointment.NONE, user.getConfirm());
        assertNotNull("No change exceptions found", appointment.getChangeException());
        assertEquals("Invalid number of change exceptions found", 1, appointment.getChangeException().length);
        List<Appointment> changeExceptions = getManager().getChangeExceptions(
            appointment.getParentFolderID(), appointment.getObjectID(), Appointment.ALL_COLUMNS);
        assertNotNull("no change exceptions found", changeExceptions);
        assertEquals("Invalid number of change exceptions found", 1, changeExceptions.size());
        Appointment changeException = getManager().get(appointment.getParentFolderID(), changeExceptions.get(0).getObjectID());
        assertNotNull("change exception not found", changeException);
        assertEquals("Invalid start date of change exception", exceptionStartDate, changeException.getStartDate());
        assertNotNull("no users found in apointment", changeException.getUsers());
        user = null;
        for (UserParticipant participant : changeException.getUsers()) {
            if (client.getValues().getUserId() == participant.getIdentifier()) {
                user = participant;
                break;
            }
        }
        assertNotNull("User not found", user);
        assertEquals("Confirm status of change exception wrong", Appointment.ACCEPT, user.getConfirm());
        /*
         * verify appointment on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = super.calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        assertEquals("no exception found in iCal", 2, iCalResource.getVEvents().size());
        Component seriesVEvent;
        Component exceptionVEvent;
        if (null == iCalResource.getVEvents().get(0).getProperty("RECURRENCE-ID")) {
            seriesVEvent = iCalResource.getVEvents().get(0);
            exceptionVEvent = iCalResource.getVEvents().get(1);
        } else {
            seriesVEvent = iCalResource.getVEvents().get(1);
            exceptionVEvent = iCalResource.getVEvents().get(0);
        }
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        attendee = seriesVEvent.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in series iCal", attendee);
        assertTrue("PARTSTAT of series wrong", null == attendee.getAttribute("PARTSTAT") || "NEEDS-ACTION".equals(attendee.getAttribute("PARTSTAT")));
        attendee = exceptionVEvent.getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee not found in exception iCal", attendee);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendee.getAttribute("PARTSTAT"));
    }

}
