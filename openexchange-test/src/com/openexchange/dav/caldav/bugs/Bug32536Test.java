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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * Test for Bug 32536 that should ensure that the MacOS X reminder (caldav) does not change existing change exceptions made by changing
 * confirmation status.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 */
public class Bug32536Test extends CalDAVTest {

    private AJAXClient client1;

    private AJAXClient client2;

    private CalendarTestManager ctm;

    private Appointment appointment;

    private int nextYear;

    private int occurrence = 5;

    private String declineMessage = "Not that day";

    private String acceptMessage = "Sicha dat";

    private static final int[] COLS = new int[] {
        Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.TITLE,
        Appointment.CONFIRMATIONS, Appointment.USERS, Appointment.PARTICIPANTS, Appointment.RECURRENCE_POSITION, Appointment.START_DATE,
        Appointment.END_DATE, Appointment.LAST_MODIFIED, Appointment.LAST_MODIFIED_UTC, Appointment.CREATION_DATE };

    /**
     * Initializes a new {@link Bug32536Test}.
     * 
     * @param name
     */
    public Bug32536Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        client1 = client;
        client2 = new AJAXClient(User.User2);
        ctm = new CalendarTestManager(client1);
        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        appointment = new Appointment();
        appointment.setTitle("Test for occurrence based confirmations.");
        appointment.setStartDate(D("01.02." + nextYear + " 08:00"));
        appointment.setEndDate(D("01.02." + nextYear + " 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setAlarm(15);
        appointment.setUntil(null);
        appointment.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        UserParticipant user1 = new UserParticipant(client1.getValues().getUserId());
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        appointment.setParticipants(new Participant[] { user1, user2 });
        ctm.insert(appointment);

        ctm.setClient(client1);
        ctm.confirm(appointment, Appointment.ACCEPT, acceptMessage);
        ctm.setClient(client2);
        ctm.confirm(appointment, Appointment.ACCEPT, acceptMessage);
    }

    public void testSeriesWithOneExceptionBecauseOfConfirmationChange_calDavReminderTriggersPUT_exceptionNotChanged() throws Exception {
        ctm.setClient(client1);
        Appointment orig = ctm.get(appointment.getParentFolderID(), appointment.getObjectID());
        ctm.confirm(orig, Appointment.DECLINE, declineMessage, this.occurrence);
        // GET THE CHANGED EXCEPTION - CONFIRMATION STATUS CHANGED
        List<Appointment> changeExceptions = ctm.getChangeExceptions(client1.getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), COLS);

        String iCal = getICalFromAppointment(orig, changeExceptions.get(0));
        Map<String, String> headers = new HashMap<String, String>();

        // TEST
        super.putICal(super.getDefaultFolderID(), orig.getUid(), iCal, headers);

        // ASSERT
        List<Appointment> changeExceptionsAfterCalDavCall = ctm.getChangeExceptions(client1.getValues().getPrivateAppointmentFolder(), appointment.getObjectID(), COLS);

        Assert.assertEquals(1, changeExceptionsAfterCalDavCall.size());
        UserParticipant[] users = changeExceptionsAfterCalDavCall.get(0).getUsers();
        for (UserParticipant user : users) {
            if (user.getIdentifier() == client1.getValues().getUserId()) {
                Assert.assertEquals("Wrong confirmation status!", Appointment.DECLINE, user.getConfirm());
                Assert.assertTrue(declineMessage.equalsIgnoreCase(user.getConfirmMessage()));
            }
            if (user.getIdentifier() == client2.getValues().getUserId()) {
                Assert.assertEquals("Wrong confirmation status!", Appointment.ACCEPT, user.getConfirm());
                Assert.assertTrue(acceptMessage.equalsIgnoreCase(user.getConfirmMessage()));
            }
        }
    }

    private String getICalFromAppointment(Appointment orig, Appointment exception) {
        return "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "PRODID:-//Apple Inc.//Mac OS X 10.9.2//EN\n" +
            "CALSCALE:GREGORIAN\n" +
            "BEGIN:VTIMEZONE\n" +
            "TZID:Europe/Berlin\n" +
            "BEGIN:DAYLIGHT\n" +
            "TZOFFSETFROM:+0100\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" +
            "DTSTART:19810329T020000\n" +
            "TZNAME:MESZ\n" +
            "TZOFFSETTO:+0200\n" +
            "END:DAYLIGHT\n" +
            "BEGIN:STANDARD\n" +
            "TZOFFSETFROM:+0200\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" +
            "DTSTART:19961027T030000\n" +
            "TZNAME:MEZ\n" +
            "TZOFFSETTO:+0100\n" +
            "END:STANDARD\n" +
            "END:VTIMEZONE\n" +
            "BEGIN:VEVENT\n" +
            "ATTENDEE;CN=\"Martin Schneider\";CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;ROLE=\n" +
            " REQ-PARTICIPANT:mailto:martin.schneider@premium\n" +
            "ATTENDEE;CN=\"Templin, Steffen\";CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT:ma\n" +
            " ilto:steffen.templin@premium\n" +
            "DTEND;TZID=Europe/Berlin:" + formatAsUTC(orig.getEndDate()) + "\n" +
            "TRANSP:OPAQUE\n" +
            "ORGANIZER:mailto:martin.schneider@premium\n" +
            "UID:"+orig.getUid()+"\n" +
            "DTSTAMP:" + formatAsUTC(orig.getLastModified()) + "\n" +
            "SEQUENCE:0\n" +
            "CLASS:PUBLIC\n" +
            "SUMMARY:"+orig.getTitle() +"\n" +
            "LAST-MODIFIED:"+formatAsUTC(orig.getLastModified())+"\n" +
            "DTSTART;TZID=Europe/Berlin:"+formatAsUTC(orig.getStartDate())+"\n" +
            "CREATED:"+formatAsUTC(orig.getCreationDate()) + "\n" +
            "RRULE:FREQ=DAILY;INTERVAL=1\n" +
            "BEGIN:VALARM\n" +
            "X-WR-ALARMUID:035DAA1A-3B82-4580-93CB-5DC0B8BC1734\n" +
            "UID:035DAA1A-3B82-4580-93CB-5DC0B8BC1734\n" +
            "TRIGGER;VALUE=DATE-TIME:00011129T230632Z\n" +
            "ACKNOWLEDGED:"+formatAsUTC(orig.getLastModified())+"\n" +
            "X-APPLE-DEFAULT-ALARM:TRUE\n" +
            "ACTION:NONE\n" +
            "END:VALARM\n" +
            "BEGIN:VALARM\n" +
            "X-WR-ALARMUID:3086C6BA-C9D7-49FF-8E2B-487DC835F751\n" +
            "UID:3086C6BA-C9D7-49FF-8E2B-487DC835F751\n" +
            "DESCRIPTION:Open-XChange\n" +
            "ACKNOWLEDGED:"+formatAsUTC(orig.getLastModified())+"\n" +
            "TRIGGER:-PT15M\n" +
            "ACTION:DISPLAY\n" +
            "END:VALARM\n" +
            "END:VEVENT\n" +
            "BEGIN:VEVENT\n" +
            "TRANSP:OPAQUE\n" +
            "DTEND;TZID=Europe/Berlin:"+formatAsUTC(exception.getEndDate())+"\n" +
            "ORGANIZER:mailto:martin.schneider@premium\n" +
            "UID:"+orig.getUid()+"\n" +
            "DTSTAMP:" + formatAsUTC(exception.getCreationDate()) + "\n" +
            "SEQUENCE:0\n" +
            "CLASS:PUBLIC\n" +
            "RECURRENCE-ID;TZID=Europe/Berlin:" + formatAsUTC(exception.getStartDate()) + "\n" +
            "SUMMARY:"+orig.getTitle() +"\n" +
            "LAST-MODIFIED:"+formatAsUTC(orig.getLastModified())+"\n" +
            "DTSTART;TZID=Europe/Berlin:"+formatAsUTC(exception.getStartDate())+ "\n" +
            "CREATED:"+formatAsUTC(orig.getLastModified())+"\n" +
            "ATTENDEE;CN=\"Martin Schneider\";CUTYPE=INDIVIDUAL;PARTSTAT=ACCEPTED;ROLE=\n" +
            " REQ-PARTICIPANT:mailto:martin.schneider@premium\n" +
            "ATTENDEE;CN=\"Templin, Steffen\";CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT:ma\n" +
            " ilto:steffen.templin@premium\n" +
            "BEGIN:VALARM\n" +
            "X-WR-ALARMUID:9D39DADD-113A-4E0C-95DB-6534250492E4\n" +
            "UID:9D39DADD-113A-4E0C-95DB-6534250492E4\n" +
            "TRIGGER:-PT15M\n" +
            "DESCRIPTION:Open-XChange\n" +
            "ACTION:DISPLAY\n" +
            "END:VALARM\n" +
            "END:VEVENT\n" +
            "END:VCALENDAR\n" +
            "";
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

}
