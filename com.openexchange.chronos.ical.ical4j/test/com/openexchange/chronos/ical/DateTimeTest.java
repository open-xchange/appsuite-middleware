/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.ical;

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.SortedSet;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceId;

/**
 * {@link DateTimeTest}
 * 
 * Tests parsing and writing of date-time- and lists of date-time properties in different notations.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class DateTimeTest extends ICalTest {
    
    @Test
    public void testRecurrenceIdWithTimezone() throws Exception {
        String iCal = // @formatter:off
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
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;TZID=America/New_York:20190612T083000\r\n" +
            "DTEND;TZID=America/New_York:20190612T093000\r\n" +
            "RECURRENCE-ID;TZID=America/New_York:20190612T083000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        assertNotNull(event.getRecurrenceId());
        assertEquals(DateTime.parse("America/New_York", "20190612T083000"), event.getRecurrenceId().getValue());
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.contains("RECURRENCE-ID;TZID=America/New_York:20190612T083000"));
    }    

    @Test
    public void testRecurrenceIdFloating() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART:20190612T083000\r\n" +
            "DTEND:20190612T093000\r\n" +
            "RECURRENCE-ID:20190612T083000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        assertNotNull(event.getRecurrenceId());
        assertEquals(DateTime.parse("20190612T083000"), event.getRecurrenceId().getValue());
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.contains("RECURRENCE-ID:20190612T083000"));
    }

    @Test
    public void testRecurrenceIdAllDay() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;VALUE=DATE:20190612\r\n" +
            "RECURRENCE-ID;VALUE=DATE:20190612\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        assertNotNull(event.getRecurrenceId());
        assertEquals(DateTime.parse("20190612"), event.getRecurrenceId().getValue());
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.contains("RECURRENCE-ID;VALUE=DATE:20190612"));
    }

    @Test
    public void testRecurrenceIdUTC() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART:20190612T083000Z\r\n" +
            "DTEND:20190612T093000Z\r\n" +
            "RECURRENCE-ID:20190612T083000Z\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        assertNotNull(event.getRecurrenceId());
        assertEquals(DateTime.parse("20190612T083000Z"), event.getRecurrenceId().getValue());
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.contains("RECURRENCE-ID:20190612T083000Z"));
    }

    @Test
    public void testExDateWithTimezone() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:America/New_York\r\n" +
            "TZURL:http://tzurl.org/zoneinfo-outlook-global/America/New_York\r\n" +
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
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;TZID=America/New_York:20190612T083000\r\n" +
            "DTEND;TZID=America/New_York:20190612T093000\r\n" +
            "RRULE:FREQ=DAILY;COUNT=10\r\n" +
            "EXDATE;TZID=America/New_York:20190613T083000,20190615T083000,20190618T083000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        SortedSet<RecurrenceId> deleteExceptionDates = event.getDeleteExceptionDates();
        assertNotNull(deleteExceptionDates);
        assertEquals(3, deleteExceptionDates.size());
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("America/New_York", "20190613T083000"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("America/New_York", "20190615T083000"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("America/New_York", "20190618T083000"))));
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.replaceAll("\\s+", "").contains("EXDATE;TZID=America/New_York:20190613T083000,20190615T083000,20190618T083000"));
    }

    @Test
    public void testExDateFloating() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART:20190612T083000\r\n" +
            "DTEND:20190612T093000\r\n" +
            "RRULE:FREQ=DAILY;COUNT=10\r\n" +
            "EXDATE:20190613T083000,20190615T083000,20190618T083000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        SortedSet<RecurrenceId> deleteExceptionDates = event.getDeleteExceptionDates();
        assertNotNull(deleteExceptionDates);
        assertEquals(3, deleteExceptionDates.size());
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190613T083000"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190615T083000"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190618T083000"))));
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.replaceAll("\\s+", "").contains("EXDATE:20190613T083000,20190615T083000,20190618T083000"));
    }

    @Test
    public void testExDateAllDay() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART;VALUE=DATE:20190612\r\n" +
            "DTEND;VALUE=DATE:20190612\r\n" +
            "RRULE:FREQ=DAILY;COUNT=10\r\n" +
            "EXDATE;VALUE=DATE:20190613,20190615,20190618\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        SortedSet<RecurrenceId> deleteExceptionDates = event.getDeleteExceptionDates();
        assertNotNull(deleteExceptionDates);
        assertEquals(3, deleteExceptionDates.size());
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190613"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190615"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190618"))));
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.replaceAll("\\s+", "").contains("EXDATE;VALUE=DATE:20190613,20190615,20190618"));
    }

    @Test
    public void testExDateUTC() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART:20190612T083000Z\r\n" +
            "DTEND:20190612T093000Z\r\n" +
            "RRULE:FREQ=DAILY;COUNT=10\r\n" +
            "EXDATE:20190613T083000Z,20190615T083000Z,20190618T083000Z\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        SortedSet<RecurrenceId> deleteExceptionDates = event.getDeleteExceptionDates();
        assertNotNull(deleteExceptionDates);
        assertEquals(3, deleteExceptionDates.size());
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190613T083000Z"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190615T083000Z"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190618T083000Z"))));
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.replaceAll("\\s+", "").contains("EXDATE:20190613T083000Z,20190615T083000Z,20190618T083000Z"));
    }

    @Test
    public void testMultipleExDatesUTC() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//RDU Software//NONSGML HandCal//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VEVENT\r\n" +
            "DTSTAMP:19980309T231000Z\r\n" +
            "UID:guid-1.example.com\r\n" +
            "CLASS:PUBLIC\r\n" +
            "SUMMARY:test\r\n" +
            "DTSTART:20190612T083000Z\r\n" +
            "DTEND:20190612T093000Z\r\n" +
            "RRULE:FREQ=DAILY;COUNT=10\r\n" +
            "EXDATE:20190613T083000Z\r\n" +
            "EXDATE:20190615T083000Z\r\n" +
            "EXDATE:20190618T083000Z\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        Event event = importEvent(iCal);
        SortedSet<RecurrenceId> deleteExceptionDates = event.getDeleteExceptionDates();
        assertNotNull(deleteExceptionDates);
        assertEquals(3, deleteExceptionDates.size());
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190613T083000Z"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190615T083000Z"))));
        assertTrue(contains(deleteExceptionDates, new DefaultRecurrenceId(DateTime.parse("20190618T083000Z"))));
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.replaceAll("\\s+", "").contains("EXDATE:20190613T083000Z,20190615T083000Z,20190618T083000Z"));
    }

}
