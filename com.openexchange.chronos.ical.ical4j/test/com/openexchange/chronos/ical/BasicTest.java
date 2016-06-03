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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventStatus;
import com.openexchange.java.Streams;

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
            "END:VCALENDAR\r\n"
        ;
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
        assertEquals("Networld+Interop Conference and Exhibit\nAtlanta World Congress Center\nAtlanta, Georgia", event.getDescription());
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
            "END:VCALENDAR\r\n"
        ;
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
            "END:VCALENDAR\r\n"
        ;
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
        assertEquals("Discuss how we can test c&s interoperability\nusing iCalendar and other IETF standards.", event.getDescription());
        assertEquals("LDB Lobby", event.getLocation());
        assertTrue(null != event.getAttachments() && 1 == event.getAttachments().size());
        Attachment attachment = event.getAttachments().get(0);
        assertNotNull(attachment);
        assertEquals("application/postscript", attachment.getFormatType());
        assertEquals("ftp://example.com/pub/conf/bkgrnd.ps", attachment.getUri());
    }
    
    @Test
    public void testImportVEvent_4() throws Exception {
        String iCal =
			"BEGIN:VCALENDAR\n" + 
			"PRODID:Strato Communicator 3.5\n" + 
			"VERSION:2.0\n" + 
			"CALSCALE:GREGORIAN\n" + 
			"BEGIN:VTIMEZONE\n" + 
			"TZID:Europe/Berlin\n" + 
			"X-LIC-LOCATION:Europe/Berlin\n" + 
			"BEGIN:DAYLIGHT\n" + 
			"TZOFFSETFROM:+0100\n" + 
			"TZOFFSETTO:+0200\n" + 
			"TZNAME:CEST\n" + 
			"DTSTART:19700329T020000\n" + 
			"RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" + 
			"END:DAYLIGHT\n" + 
			"BEGIN:STANDARD\n" + 
			"TZOFFSETFROM:+0200\n" + 
			"TZOFFSETTO:+0100\n" + 
			"TZNAME:CET\n" + 
			"DTSTART:19701025T030000\n" + 
			"RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" + 
			"END:STANDARD\n" + 
			"END:VTIMEZONE\n" +
			
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20111130T124433Z\n" + 
			"SUMMARY:2 Tage vorher\n" + 
			"CLASS:PRIVATE\n" + 
			"LAST-MODIFIED:20111130T124235Z\n" + 
			"DTSTART;TZID=Europe/Berlin:20300628T170000\n" + 
			"DTEND;TZID=Europe/Berlin:20300628T180000\n" + 
			"CREATED:20111130T124235Z\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-P2D\n" + 
			"DESCRIPTION:2 Tage vorher\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"END:VEVENT\n" + 
			
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20111130T124433Z\n" + 
			"SUMMARY:4 Wochen vorher\n" + 
			"CLASS:PRIVATE\n" + 
			"LAST-MODIFIED:20111130T124311Z\n" + 
			"DTEND;TZID=Europe/Berlin:20300328T230000\n" + 
			"CREATED:20111130T124311Z\n" + 
			"DTSTART;TZID=Europe/Berlin:20300328T220000\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-P4W\n" + 
			"DESCRIPTION:4 Wochen vorher\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"END:VEVENT\n" + 
			
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20111130T124433Z\n" + 
			"SUMMARY:15 Minuten vorher\n" + 
			"CLASS:PRIVATE\n" + 
			"LAST-MODIFIED:20111130T124258Z\n" + 
			"DTEND;TZID=Europe/Berlin:20300328T160000\n" + 
			"CREATED:20111130T124258Z\n" + 
			"DTSTART;TZID=Europe/Berlin:20300328T150000\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-PT15M\n" + 
			"DESCRIPTION:15 Minuten vorher\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"END:VEVENT\n" + 
			
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20111130T124433Z\n" + 
			"SUMMARY:2 Stunden vorher\n" + 
			"CLASS:PRIVATE\n" + 
			"LAST-MODIFIED:20111130T124245Z\n" + 
			"DTEND;TZID=Europe/Berlin:20300328T170000\n" + 
			"CREATED:20111130T124245Z\n" + 
			"DTSTART;TZID=Europe/Berlin:20300328T160000\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-PT2H\n" + 
			"DESCRIPTION:2 Stunden vorher\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"END:VEVENT\n" + 
			
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20111130T124433Z\n" + 
			"SUMMARY:3 Monate vorher erinnern (Erwartet 4 Wochen im OX)\n" + 
			"CLASS:PRIVATE\n" + 
			"LAST-MODIFIED:20111130T124151Z\n" + 
			"DTEND;TZID=Europe/Berlin:20300328T200000\n" + 
			"CREATED:20111130T124110Z\n" + 
			"DTSTART;TZID=Europe/Berlin:20300328T190000\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-P4W\n" + 
			"DESCRIPTION:3 Monate vorher erinnern (Erwartet 4 Wochen im OX)\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"END:VEVENT\n" + 
			
			"BEGIN:VEVENT\n" + 
			"DTSTAMP:20111130T124433Z\n" + 
			"SUMMARY:2 Wochen vorher\n" + 
			"CLASS:PRIVATE\n" + 
			"LAST-MODIFIED:20111130T124220Z\n" + 
			"DTEND;TZID=Europe/Berlin:20300328T190000\n" + 
			"CREATED:20111130T124220Z\n" + 
			"DTSTART;TZID=Europe/Berlin:20300328T180000\n" + 
			"BEGIN:VALARM\n" + 
			"ACTION:DISPLAY\n" + 
			"TRIGGER:-P2W\n" + 
			"DESCRIPTION:2 Wochen vorher\n" + 
			"END:VALARM\n" + 
			"TRANSP:OPAQUE\n" + 
			"END:VEVENT\n" + 
			"END:VCALENDAR"
		;

        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        List<EventData> events = iCalService.importICal(inputStream, null).getEvents();
        
        EventData eventData = events.get(0);
        Event event = eventData.getEvent();
        
        assertEquals("2 Tage vorher", event.getSummary());
        assertEquals(D("2030-06-28 17:00:00", "Europe/Berlin"), event.getStartDate());
        assertEquals(D("2030-06-28 18:00:00", "Europe/Berlin"), event.getEndDate());
        assertEquals("Europe/Berlin", event.getStartTimezone());
        assertEquals("Europe/Berlin", event.getEndTimezone());        
    }

    @Test
    public void testImportVEvent_5() throws Exception {
        String iCal =
    		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID:-//Ben Fortuna//iCal4j 1.0//EN\r\n" +
			"CALSCALE:GREGORIAN\r\n" +
			"METHOD:PUBLISH\r\n" +
			"BEGIN:VEVENT\r\n" +
			"DTSTAMP:20120808T211508Z\r\n" +
			"DTSTART;TZID=America/New_York:20120808T020000\r\n" +
			"DTEND;TZID=America/New_York:20120808T180000\r\n" +
			"SUMMARY:NewYork Event\r\n" +
			"LOCATION:Manhattan\r\n" +
			"DESCRIPTION:This is a test Description\r\n" +
			"TZID:America/New_York\r\n" +
			"END:VEVENT\r\n" +
			"BEGIN:VTIMEZONE\r\n" +
			"TZID:America/New_York\r\n" +
			"TZURL:http://tzurl.org/zoneinfo-outlook/America/New_York\r\n" +
			"X-LIC-LOCATION:America/New_York\r\n" +
			"BEGIN:DAYLIGHT\r\n" +
			"TZOFFSETFROM:-0500\r\n" +
			"TZOFFSETTO:-0400\r\n" +
			"TZNAME:EDT\r\n" +
			"DTSTART:19700308T020000\r\n" +
			"RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU\r\n" +
			"END:DAYLIGHT\r\n" +
			"BEGIN:STANDARD\r\n" +
			"TZOFFSETFROM:-0400\r\n" +
			"TZOFFSETTO:-0500\r\n" +
			"TZNAME:EST\r\n" +
			"DTSTART:19701101T020000\r\n" +
			"RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\r\n" +
			"END:STANDARD\r\n" +
			"END:VTIMEZONE\r\n" +
			"END:VCALENDAR\r\n"
			;
        EventData eventData = importEvent(iCal);
        Event event = eventData.getEvent();
        
        assertEquals("NewYork Event", event.getSummary());
        assertEquals(D("2012-08-08 02:00:00", "America/New_York"), event.getStartDate());
        assertEquals(D("2012-08-08 18:00:00", "America/New_York"), event.getEndDate());
        assertEquals("America/New_York", event.getStartTimezone());
        assertEquals("America/New_York", event.getEndTimezone());        
    }

}
