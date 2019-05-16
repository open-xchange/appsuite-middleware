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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug23181Test}
 *
 * Unable to import external appointment update with newly added participant from Thunderbird/Lightning
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug23181Test extends CalDAVTest {

    private CalendarTestManager manager2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager2 = new CalendarTestManager(getClient2());
        manager2.setFailOnError(true);
        testUser2 = testContext.acquireUser();

    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            if (null != this.manager2) {
                this.manager2.cleanUp();
                if (null != manager2.getClient()) {
                    manager2.getClient().logout();
                }
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testImportAppointment() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * Create appointment in user B's calendar on server
         */
        String userA = getClient().getValues().getDefaultAddress();
        String userB = getClient2().getValues().getDefaultAddress();
        String uid = randomUID();
        String summary = "Bug23181Test";
        String location = "tbd";
        Date start = TimeTools.D("tomorrow at 3pm");
        Date end = TimeTools.D("tomorrow at 4pm");
        Appointment appointmentB = generateAppointment(start, end, uid, summary, location);
        appointmentB.setOrganizer("extern1@example.com");
        appointmentB.addParticipant(new ExternalUserParticipant("extern2@example.com"));
        appointmentB.addParticipant(new ExternalUserParticipant("extern3@example.com"));
        appointmentB.addParticipant(new ExternalUserParticipant(userB));
        appointmentB.setIgnoreConflicts(true);
        appointmentB.setParentFolderID(manager2.getClient().getValues().getPrivateAppointmentFolder());
        appointmentB.setSequence(0);
        manager2.insert(appointmentB);
        /*
         * confirm updated appointment as user A in client
         */
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR" + "\r\n" +
            "METHOD:REQUEST" + "\r\n" +
            "PRODID:Microsoft Exchange Server 2007" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "CALSCALE:GREGORIAN" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "ORGANIZER;CN=Extern 1:MAILTO:extern1@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Extern 2:MAILTO:extern2@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Extern 3:MAILTO:extern3@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=" + userB + ":MAILTO:" + userB + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;RSVP=TRUE;CN=" + userA + ":MAILTO:" + userA + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTEND:" + formatAsUTC(end) + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "LOCATION:" + location + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART:" + formatAsUTC(start) + "\r\n" +
            "SEQUENCE:1" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment as user A on server
         */
        Appointment appointmentA = super.getAppointment(uid);
        super.rememberForCleanUp(appointmentA);
        assertNotNull("appointment not found on server", appointmentA);
        assertNotNull("appointment has no users", appointmentA.getUsers());
        UserParticipant userParticipantA = null;
        ConfirmableParticipant participantB = null;
        for (UserParticipant participant : appointmentA.getUsers()) {
            if (getClient().getValues().getUserId() == participant.getIdentifier()) {
                userParticipantA = participant;
            }
        }
        for (ConfirmableParticipant participant : appointmentA.getConfirmations()) {
            if (manager2.getClient().getValues().getDefaultAddress().equals(participant.getEmailAddress())) {
                participantB = participant;
            }
        }
        assertNotNull("added user participant not found", userParticipantA);
        assertNotNull("previous participant not found", participantB);
        assertEquals("confirmation status wrong", Appointment.ACCEPT, userParticipantA.getConfirm());
        /*
         * verify appointment as user B on server
         */
        appointmentB = manager2.get(manager2.getClient().getValues().getPrivateAppointmentFolder(), appointmentB.getObjectID());
        assertNotNull("appointment not found on server", appointmentB);
        assertNotNull("appointment has no users", appointmentB.getUsers());
        ConfirmableParticipant participantA = null;
        UserParticipant userParticipantB = null;
        for (UserParticipant participant : appointmentB.getUsers()) {
            if (manager2.getClient().getValues().getUserId() == participant.getIdentifier()) {
                userParticipantB = participant;
            }
        }
        for (ConfirmableParticipant participant : appointmentB.getConfirmations()) {
            if (getClient().getValues().getDefaultAddress().equals(participant.getEmailAddress())) {
                participantA = participant;
            }
        }
        assertNull("added user participant A was found in B's appointment", participantA);
        assertNotNull("previous participant not found", userParticipantB);
        /*
         * verify appointment as user A on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", appointmentB.getLocation(), iCalResource.getVEvent().getLocation());
        Property attendeeA = null;
        Property attendeeB = null;
        List<Property> attendees = iCalResource.getVEvent().getProperties("ATTENDEE");
        for (Property property : attendees) {
            if (property.getValue().toLowerCase().contains(super.getAJAXClient().getValues().getDefaultAddress().toLowerCase())) {
                attendeeA = property;
            } else if (property.getValue().toLowerCase().contains(manager2.getClient().getValues().getDefaultAddress().toLowerCase())) {
                attendeeB = property;
            }
        }
        assertNotNull("added user attendee not found", attendeeA);
        assertNotNull("previous attendee not found", attendeeB);
        assertEquals("partstat status wrong", "ACCEPTED", attendeeA.getAttribute("PARTSTAT"));
    }

    @Test
    public void testAlsoImportOutSequencedAppointment() throws Exception {
        /*
         * Create appointment in user B's calendar on server
         */
        String userA = testUser.getLogin();
        String userB = testUser2.getLogin();
        String uid = randomUID();
        String summary = "Bug23181Test2";
        String location = "tbd";
        Date start = TimeTools.D("tomorrow at 1pm");
        Date end = TimeTools.D("tomorrow at 2pm");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setOrganizer("extern1@example.com");
        appointment.addParticipant(new ExternalUserParticipant("extern2@example.com"));
        appointment.addParticipant(new ExternalUserParticipant("extern3@example.com"));
        appointment.addParticipant(new ExternalUserParticipant(userB));
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(manager2.getClient().getValues().getPrivateAppointmentFolder());
        appointment.setSequence(0);
        appointment = manager2.insert(appointment);
        /*
         * update the appointment once to increase the sequence number
         */
        appointment.setLocation("new location");
        manager2.update(appointment);
        /*
         * try to confirm updated appointment as user A in client
         */
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR" + "\r\n" +
            "METHOD:REQUEST" + "\r\n" +
            "PRODID:Microsoft Exchange Server 2007" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "CALSCALE:GREGORIAN" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "ORGANIZER;CN=Extern 1:MAILTO:extern1@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Extern 2:MAILTO:extern2@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Extern 3:MAILTO:extern3@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=" + userB + ":MAILTO:" + userB + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;RSVP=TRUE;CN=" + userA + ":MAILTO:" + userA + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTEND:" + formatAsUTC(end) + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "LOCATION:abcdefg" + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART:" + formatAsUTC(start) + "\r\n" +
            "SEQUENCE:0" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
    }

    @Test
    public void testAlsoImportOtherOrganizersAppointment() throws Exception {
        /*
         * Create appointment in user B's calendar on server
         */
        String userA = testUser.getLogin();
        String userB = testUser2.getLogin();
        String uid = randomUID();
        String summary = "Bug23181Test3";
        String location = "tbd";
        Date start = TimeTools.D("tomorrow at 1pm");
        Date end = TimeTools.D("tomorrow at 2pm");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setOrganizer("extern1@example.com");
        appointment.addParticipant(new ExternalUserParticipant("extern2@example.com"));
        appointment.addParticipant(new ExternalUserParticipant("extern3@example.com"));
        appointment.addParticipant(new ExternalUserParticipant(userB));
        appointment.setIgnoreConflicts(true);
        appointment.setParentFolderID(manager2.getClient().getValues().getPrivateAppointmentFolder());
        appointment.setSequence(0);
        manager2.insert(appointment);
        /*
         * try to confirm updated appointment as user A in client
         */
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR" + "\r\n" +
            "METHOD:REQUEST" + "\r\n" +
            "PRODID:Microsoft Exchange Server 2007" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "CALSCALE:GREGORIAN" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "ORGANIZER;CN=Extern 1:MAILTO:extern4@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Extern 2:MAILTO:extern2@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Extern 3:MAILTO:extern3@example.com" + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=" + userB + ":MAILTO:" + userB + "\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;RSVP=TRUE;CN=" + userA + ":MAILTO:" + userA + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTEND:" + formatAsUTC(end) + "\r\n" +
            "TRANSP:OPAQUE" + "\r\n" +
            "CLASS:PUBLIC" + "\r\n" +
            "SUMMARY:" + summary + "\r\n" +
            "LOCATION:abcdefg" + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART:" + formatAsUTC(start) + "\r\n" +
            "SEQUENCE:1" + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
    }

}
