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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.ical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventStatus;

/**
 * {@link BasicTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BasicTest extends ICalTest {

    @Test
    public void testImportVEvent_1() throws Exception {
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
                "PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN\r\n" +
                "VERSION:2.0\r\n" +
                "BEGIN:VEVENT\r\n" +
                "DTSTAMP:19960704T120000Z\r\n" +
                "UID:uid1@example.com\r\n" +
                "ORGANIZER:mailto:jsmith@example.com\r\n" +
                "DTSTART:19960918T143000Z\r\n" +
                "DTEND:19960920T220000Z\r\n" +
                "STATUS:CONFIRMED\r\n" +
                "CATEGORIES:CONFERENCE\r\n" +
                "SUMMARY:Networld+Interop Conference\r\n" +
                "DESCRIPTION:Networld+Interop Conference\r\n" +
                "  and Exhibit\\nAtlanta World Congress Center\\n\r\n" +
                " Atlanta\\, Georgia\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";
        EventData eventData = importEvent(iCal);
        Event event = eventData.getEvent();
        //        assertEquals(D("1996-07-04 12:00:00"), event.getCreated());
        assertEquals("uid1@example.com", event.getUid());
        assertEquals("mailto:jsmith@example.com", event.getOrganizer().getUri());
        assertEquals(D("1996-09-18 14:30:00"), event.getStartDate());
        assertEquals(D("1996-09-20 22:00:00"), event.getEndDate());
        assertEquals(EventStatus.CONFIRMED, event.getStatus());
        assertEquals(Collections.singletonList("CONFERENCE"), event.getCategories());
        assertEquals("Networld+Interop Conference", event.getSummary());
        assertEquals("Networld+Interop Conference and Exhibit\r\nAtlanta World Congress Center\r\nAtlanta, Georgia", event.getDescription());
    }

    @Test
    public void testImportVEvent_2() throws Exception {
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
                "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
                "VERSION:2.0\r\n" +
                "BEGIN:VTIMEZONE\r\n" +
                "TZID:America/New_York\r\n" +
                "BEGIN:STANDARD\r\n" +
                "DTSTART:19981025T020000\r\n" +
                "TZOFFSETFROM:-0400\r\n" +
                "TZOFFSETTO:-0500\r\n" +
                "TZNAME:EST\r\n" +
                "END:STANDARD\r\n" +
                "BEGIN:DAYLIGHT\r\n" +
                "DTSTART:19990404T020000\r\n" +
                "TZOFFSETFROM:-0500\r\n" +
                "TZOFFSETTO:-0400\r\n" +
                "TZNAME:EDT\r\n" +
                "END:DAYLIGHT\r\n" +
                "END:VTIMEZONE\r\n" +
                "BEGIN:VEVENT\r\n" +
                "DTSTAMP:19980309T231000Z\r\n" +
                "UID:guid-1.example.com\r\n" +
                "ORGANIZER:mailto:mrbig@example.com\r\n" +
                "ATTENDEE;RSVP=TRUE;ROLE=REQ-PARTICIPANT;CUTYPE=GROUP:\r\n" +
                " mailto:employee-A@example.com\r\n" +
                "DESCRIPTION:Project XYZ Review Meeting\r\n" +
                "CATEGORIES:MEETING\r\n" +
                "CLASS:PUBLIC\r\n" +
                "CREATED:19980309T130000Z\r\n" +
                "SUMMARY:XYZ Project Review\r\n" +
                "DTSTART;TZID=America/New_York:19980312T083000\r\n" +
                "DTEND;TZID=America/New_York:19980312T093000\r\n" +
                "LOCATION:1CP Conference Room 4350\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";
        EventData eventData = importEvent(iCal);
        Event event = eventData.getEvent();
        //        assertEquals(D("1996-07-04 12:00:00"), event.getCreated());
        assertEquals("guid-1.example.com", event.getUid());
        assertEquals("mailto:mrbig@example.com", event.getOrganizer().getUri());
        assertTrue(null != event.getAttendees() && 1 == event.getAttendees().size());
        Attendee attendee = event.getAttendees().get(0);
        assertNotNull(attendee);
        assertEquals(CalendarUserType.GROUP, attendee.getCuType());
        assertEquals("mailto:employee-A@example.com", attendee.getUri());
        assertEquals(Boolean.TRUE, attendee.isRsvp());
        assertEquals("Project XYZ Review Meeting", event.getDescription());
        assertEquals(Collections.singletonList("MEETING"), event.getCategories());
        assertEquals(Classification.PUBLIC, event.getClassification());
        assertEquals(D("1998-03-09 13:00;00"), event.getCreated());
        assertEquals("XYZ Project Review", event.getSummary());
        assertEquals(D("1998-03-12 08:30:00", "America/New_York"), event.getStartDate());
        assertEquals(D("1998-03-12 09:30:00", "America/New_York"), event.getEndDate());
        assertEquals("America/New_York", event.getStartTimezone());
        assertEquals("America/New_York", event.getEndTimezone());
        assertEquals("1CP Conference Room 4350", event.getLocation());
    }

    @Test
    public void testImportVEvent_3() throws Exception {
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
                "METHOD:xyz\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//ABC Corporation//NONSGML My Product//EN\r\n" +
                "BEGIN:VEVENT\r\n" +
                "DTSTAMP:19970324T120000Z\r\n" +
                "SEQUENCE:0\r\n" +
                "UID:uid3@example.com\r\n" +
                "ORGANIZER:mailto:jdoe@example.com\r\n" +
                "ATTENDEE;RSVP=TRUE:mailto:jsmith@example.com\r\n" +
                "DTSTART:19970324T123000Z\r\n" +
                "DTEND:19970324T210000Z\r\n" +
                "CATEGORIES:MEETING,PROJECT\r\n" +
                "CLASS:PUBLIC\r\n" +
                "SUMMARY:Calendaring Interoperability Planning Meeting\r\n" +
                "DESCRIPTION:Discuss how we can test c&s interoperability\\n\r\n" +
                " using iCalendar and other IETF standards.\r\n" +
                "LOCATION:LDB Lobby\r\n" +
                "ATTACH;FMTTYPE=application/postscript:ftp://example.com/pub/\r\n" +
                " conf/bkgrnd.ps\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";
        CalendarImport vCalendarImport = importICal(iCal);
        assertEquals("xyz", vCalendarImport.getMethod());
        EventData eventData = importEvent(iCal);
        Event event = eventData.getEvent();
        assertEquals(Integer.valueOf(0), event.getSequence());
        assertEquals("uid3@example.com", event.getUid());
        assertEquals("mailto:jdoe@example.com", event.getOrganizer().getUri());
        assertTrue(null != event.getAttendees() && 1 == event.getAttendees().size());
        Attendee attendee = event.getAttendees().get(0);
        assertNotNull(attendee);
        assertEquals("mailto:jsmith@example.com", attendee.getUri());
        assertEquals(Boolean.TRUE, attendee.isRsvp());
        assertEquals(D("1997-03-24 12:30:00"), event.getStartDate());
        assertEquals(D("1997-03-24 21:00:00"), event.getEndDate());
        assertEquals(Arrays.asList("MEETING", "PROJECT"), event.getCategories());
        assertEquals(Classification.PUBLIC, event.getClassification());
        assertEquals("Calendaring Interoperability Planning Meeting", event.getSummary());
        assertEquals("Discuss how we can test c&s interoperability\r\nusing iCalendar and other IETF standards.", event.getDescription());
        assertEquals("LDB Lobby", event.getLocation());
        assertTrue(null != event.getAttachments() && 1 == event.getAttachments().size());
        Attachment attachment = event.getAttachments().get(0);
        assertNotNull(attachment);
        assertEquals("application/postscript", attachment.getFormatType());
        assertEquals("ftp://example.com/pub/conf/bkgrnd.ps", attachment.getUri());
    }

}
