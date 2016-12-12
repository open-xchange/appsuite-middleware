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
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.UserParticipant;
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
    public void testImportAppointment() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * Create appointment in user B's calendar on server
         */
        String userA = client.execute(new GetRequest(Tree.DefaultAddress)).getString();
        String userB = manager2.getClient().execute(new GetRequest(Tree.DefaultAddress)).getString();
        String uid = randomUID();
        String summary = "Bug23181Test";
        String location = "tbd";
        Date start = TimeTools.D("tomorrow at 3pm");
        Date end = TimeTools.D("tomorrow at 4pm");
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
         * confirm updated appointment as user A in client
         */
        String iCal =
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
        ;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment as user A on server
         */
        appointment = super.getAppointment(uid);
        super.rememberForCleanUp(appointment);
        assertNotNull("appointment not found on server", appointment);
        assertNotNull("appointment has no users", appointment.getUsers());
        UserParticipant partipantA = null;
        UserParticipant partipantB = null;
        for (UserParticipant user : appointment.getUsers()) {
            if (getAJAXClient().getValues().getUserId() == user.getIdentifier()) {
                partipantA = user;
            } else if (manager2.getClient().getValues().getUserId() == user.getIdentifier()) {
                partipantB = user;
            }
        }
        assertNotNull("added user participant not found", partipantA);
        assertNotNull("previous participant not found", partipantB);
        assertEquals("confirmation status wrong", Appointment.ACCEPT, partipantA.getConfirm());
        /*
         * verify appointment as user B on server
         */
        appointment = manager2.get(manager2.getClient().getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertNotNull("appointment not found on server", appointment);
        assertNotNull("appointment has no users", appointment.getUsers());
        partipantA = null;
        partipantB = null;
        for (UserParticipant user : appointment.getUsers()) {
            if (getAJAXClient().getValues().getUserId() == user.getIdentifier()) {
                partipantA = user;
            } else if (manager2.getClient().getValues().getUserId() == user.getIdentifier()) {
                partipantB = user;
            }
        }
        assertNotNull("added user participant not found", partipantA);
        assertNotNull("previous participant not found", partipantB);
        assertEquals("confirmation status wrong", Appointment.ACCEPT, partipantA.getConfirm());
        /*
         * verify appointment as user A on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", appointment.getLocation(), iCalResource.getVEvent().getLocation());
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
    public void testDontImportOutSequencedAppointment() throws Exception {
        /*
         * Create appointment in user B's calendar on server
         */
        String userA = super.getLogin(User.User1);
        String userB = super.getLogin(User.User2);
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
        String iCal =
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
        ;
        assertEquals("response code wrong", StatusCodes.SC_CONFLICT, super.putICal(uid, iCal));
    }

    @Test
    public void testDontImportOtherOrganizersAppointment() throws Exception {
        /*
         * Create appointment in user B's calendar on server
         */
        String userA = super.getLogin(User.User1);
        String userB = super.getLogin(User.User2);
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
        String iCal =
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
        ;
        assertEquals("response code wrong", StatusCodes.SC_CONFLICT, super.putICal(uid, iCal));
    }

}
