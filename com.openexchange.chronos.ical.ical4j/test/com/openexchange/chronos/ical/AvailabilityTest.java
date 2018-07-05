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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
import static org.junit.Assert.assertNull;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;

/**
 * {@link AvailabilityTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailabilityTest extends ICalTest {

    /**
     * Test the availability of a user, always available Monday through Friday,
     * 9:00 am to 5:00 pm in the America/Montreal time zone.
     */
    @Test
    public void testImportSingleVAvailability() throws Exception {
        //@formatter:off
        String iCal = "BEGIN:VCALENDAR\n" +
            "BEGIN:VAVAILABILITY\n" + 
            "ORGANIZER:mailto:bernard@example.com\n" + 
            "UID:0428C7D2-688E-4D2E-AC52-CD112E2469DF\n" + 
            "DTSTAMP:20111005T133225Z\n" + 
            "BEGIN:AVAILABLE\n" + 
            "UID:34EDA59B-6BB1-4E94-A66C-64999089C0AF\n" + 
            "SUMMARY:Monday to Friday from 9:00 to 17:00\n" + 
            "DTSTART;TZID=America/Montreal:20111002T090000\n" + 
            "DTEND;TZID=America/Montreal:20111002T170000\n" + 
            "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR\n" + 
            "END:AVAILABLE\n" + 
            "END:VAVAILABILITY\n" + 
            "END:VCALENDAR\n";
        //@formatter:on

        ImportedCalendar importICal = importICal(iCal);
        assertNotNull("No availability components found", importICal.getAvailability());

        Availability availability = importICal.getAvailability();
        assertEquals("The organizer uri does not match", "mailto:bernard@example.com", availability.getOrganizer().getUri());
        assertEquals("The uid does not match", "0428C7D2-688E-4D2E-AC52-CD112E2469DF", availability.getUid());

        List<Available> freeSlots = availability.getAvailable();
        assertNotNull("No 'available' sub-components found", freeSlots);
        assertEquals("Expected 1 'available' sub-component", 1, freeSlots.size());

        Available freeSlot = freeSlots.get(0);
        assertEquals("The summary does not match", "Monday to Friday from 9:00 to 17:00", freeSlot.getSummary());
        assertEquals("The uid does not match", "34EDA59B-6BB1-4E94-A66C-64999089C0AF", freeSlot.getUid());
        assertEquals("The recurrence rule does not match", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", freeSlot.getRecurrenceRule());
        assertEquals("The start timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), freeSlot.getStartTime().getTimeZone());
        assertEquals("The end timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), freeSlot.getEndTime().getTimeZone());
    }

    /**
     * Test the availability of a user available Monday through Thursday,
     * 9:00 am to 5:00 pm, at the main office, and Friday, 9:00 am to 12:00 pm,
     * in the branch office in the America/Montreal time zone between
     * October 2nd and December 2nd 2011
     */
    @Test
    public void testImportMultipleAvailableBlocks() throws Exception {
        //@formatter:off
        String iCal ="BEGIN:VCALENDAR\n" + 
            "BEGIN:VAVAILABILITY\n" + 
            "ORGANIZER:mailto:bernard@example.com\n" + 
            "UID:84D0F948-7FC6-4C1D-BBF3-BA9827B424B5\n" + 
            "DTSTAMP:20111005T133225Z\n" + 
            "DTSTART;TZID=America/Montreal:20111002T000000\n" + 
            "DTEND;TZID=America/Montreal:20111202T000000\n" + 
            "BEGIN:AVAILABLE\n" + 
            "UID:7B33093A-7F98-4EED-B381-A5652530F04D\n" + 
            "SUMMARY:Monday to Thursday from 9:00 to 17:00\n" + 
            "DTSTART;TZID=America/Montreal:20111002T090000\n" + 
            "DTEND;TZID=America/Montreal:20111002T170000\n" + 
            "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH\n" + 
            "LOCATION:Main Office\n" + 
            "END:AVAILABLE\n" + 
            "BEGIN:AVAILABLE\n" + 
            "UID:DF39DC9E-D8C3-492F-9101-0434E8FC1896\n" + 
            "SUMMARY:Friday from 9:00 to 12:00\n" + 
            "DTSTART;TZID=America/Montreal:20111006T090000\n" + 
            "DTEND;TZID=America/Montreal:20111006T120000\n" + 
            "RRULE:FREQ=WEEKLY\n" + 
            "LOCATION:Branch Office\n" + 
            "END:AVAILABLE\n" + 
            "END:VAVAILABILITY\n" + 
            "END:VCALENDAR\n";
        //@formatter:on

        ImportedCalendar importICal = importICal(iCal);
        assertNotNull("No availability components found", importICal.getAvailability());

        Availability availability = importICal.getAvailability();
        assertEquals("The organizer uri does not match", "mailto:bernard@example.com", availability.getOrganizer().getUri());
        assertEquals("The uid does not match", "84D0F948-7FC6-4C1D-BBF3-BA9827B424B5", availability.getUid());

        List<Available> freeSlots = availability.getAvailable();
        assertNotNull("No 'available' sub-components found", freeSlots);
        assertEquals("Expected 2 'available' sub-components", 2, freeSlots.size());

        Available freeSlot = freeSlots.get(0);
        assertEquals("The summary does not match", "Monday to Thursday from 9:00 to 17:00", freeSlot.getSummary());
        assertEquals("The uid does not match", "7B33093A-7F98-4EED-B381-A5652530F04D", freeSlot.getUid());
        assertEquals("The recurrence rule does not match", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH", freeSlot.getRecurrenceRule());
        assertEquals("The location does not match", "Main Office", freeSlot.getLocation());

        freeSlot = freeSlots.get(1);
        assertEquals("The summary does not match", "Friday from 9:00 to 12:00", freeSlot.getSummary());
        assertEquals("The uid does not match", "DF39DC9E-D8C3-492F-9101-0434E8FC1896", freeSlot.getUid());
        assertEquals("The recurrence rule does not match", "FREQ=WEEKLY", freeSlot.getRecurrenceRule());
        assertEquals("The location does not match", "Branch Office", freeSlot.getLocation());
    }

    /**
     * <p>Tests the availability of a user with multiple VAvailability blocks.</p>
     * 
     * <p>
     * The base availability is from Monday through Friday, 8:00 am to 6:00 pm each day with a "VAVAILABILITY"
     * with default "PRIORITY" (there is no "DTEND" property so that this availability is unbounded). For the
     * week the calendar user is working in Denver (October 23rd through October 30th), the availability is
     * represented with a "VAVAILABILITY" component with priority 1, which overrides the base availability.
     * There is also a two hour meeting starting at 12:00 pm (in the America/Denver time zone).
     * </p>
     */
    @Ignore
    public void testImportMultipleAvailabilityBlocks() throws Exception {
        //@formatter:off
        String iCal = "BEGIN:VCALENDAR\n" +
            "BEGIN:VAVAILABILITY\n" + 
            "ORGANIZER:mailto:bernard@example.com\n" + 
            "UID:BE082249-7BDD-4FE0-BDBA-DE6598C32FC9\n" + 
            "DTSTAMP:20111005T133225Z\n" + 
            "DTSTART;TZID=America/Montreal:20111002T000000\n" + 
            "DTEND;TZID=America/Montreal:20111023T030000\n" + 
            "BEGIN:AVAILABLE\n" + 
            "UID:54602321-CEDB-4620-9099-757583263981\n" + 
            "SUMMARY:Monday to Friday from 9:00 to 17:00\n" + 
            "DTSTART;TZID=America/Montreal:20111002T090000\n" + 
            "DTEND;TZID=America/Montreal:20111002T170000\n" + 
            "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR\n" + 
            "LOCATION:Montreal\n" + 
            "END:AVAILABLE\n" + 
            "END:VAVAILABILITY\n" + 
            "BEGIN:VAVAILABILITY\n" + 
            "ORGANIZER:mailto:bernard@example.com\n" + 
            "UID:A1FF55E3-555C-433A-8548-BF4864B5621E\n" + 
            "DTSTAMP:20111005T133225Z\n" + 
            "DTSTART;TZID=America/Denver:20111023T000000\n" + 
            "DTEND;TZID=America/Denver:20111030T000000\n" + 
            "BEGIN:AVAILABLE\n" + 
            "UID:57DD4AAF-3835-46B5-8A39-B3B253157F01\n" + 
            "SUMMARY:Monday to Friday from 9:00 to 17:00\n" + 
            "DTSTART;TZID=America/Denver:20111023T090000\n" + 
            "DTEND;TZID=America/Denver:20111023T170000\n" + 
            "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR\n" + 
            "LOCATION:Denver\n" + 
            "END:AVAILABLE\n" + 
            "END:VAVAILABILITY\n" + 
            "BEGIN:VAVAILABILITY\n" + 
            "ORGANIZER:mailto:bernard@example.com\n" + 
            "UID:1852F9E1-E0AA-4572-B4C4-ED1680A4DA40\n" + 
            "DTSTAMP:20111005T133225Z\n" + 
            "DTSTART;TZID=America/Montreal:20111030T030000\n" + 
            "BEGIN:AVAILABLE\n" + 
            "UID:D27C421F-16C2-4ECB-8352-C45CA352C72A\n" + 
            "SUMMARY:Monday to Friday from 9:00 to 17:00\n" + 
            "DTSTART;TZID=America/Montreal:20111030T090000\n" + 
            "DTEND;TZID=America/Montreal:20111030T170000\n" + 
            "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR\n" + 
            "LOCATION:Montreal\n" + 
            "END:AVAILABLE\n" + 
            "END:VAVAILABILITY\n" +
            "END:VCALENDAR\n"; 
        //@formatter:on

        ImportedCalendar importICal = importICal(iCal);
        assertNotNull("No availability components found", importICal.getAvailability());

        // Availability 1
        Availability availability = importICal.getAvailability();
        assertEquals("The organizer uri does not match", "mailto:bernard@example.com", availability.getOrganizer().getUri());
        assertEquals("The uid does not match", "BE082249-7BDD-4FE0-BDBA-DE6598C32FC9", availability.getUid());
        assertEquals("The start timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), availability.getStartTime().getTimeZone());
        assertEquals("The end timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), availability.getEndTime().getTimeZone());

        List<Available> freeSlots = availability.getAvailable();
        assertNotNull("No 'available' sub-components found", freeSlots);
        assertEquals("Expected 1 'available' sub-component", 1, freeSlots.size());

        Available freeSlot = freeSlots.get(0);
        assertEquals("The summary does not match", "Monday to Friday from 9:00 to 17:00", freeSlot.getSummary());
        assertEquals("The uid does not match", "54602321-CEDB-4620-9099-757583263981", freeSlot.getUid());
        assertEquals("The recurrence rule does not match", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", freeSlot.getRecurrenceRule());
        assertEquals("The location does not match", "Montreal", freeSlot.getLocation());
        assertEquals("The start timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), freeSlot.getStartTime().getTimeZone());
        assertEquals("The end timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), freeSlot.getEndTime().getTimeZone());

        // Availability 2
        //availability = importICal.getAvailability().get(1);
        assertEquals("The organizer uri does not match", "mailto:bernard@example.com", availability.getOrganizer().getUri());
        assertEquals("The uid does not match", "A1FF55E3-555C-433A-8548-BF4864B5621E", availability.getUid());
        assertEquals("The start timezone does not match", java.util.TimeZone.getTimeZone("America/Denver"), availability.getStartTime().getTimeZone());
        assertEquals("The end timezone does not match", java.util.TimeZone.getTimeZone("America/Denver"), availability.getEndTime().getTimeZone());

        freeSlots = availability.getAvailable();
        assertNotNull("No 'available' sub-components found", freeSlots);
        assertEquals("Expected 1 'available' sub-component", 1, freeSlots.size());

        freeSlot = freeSlots.get(0);
        assertEquals("The summary does not match", "Monday to Friday from 9:00 to 17:00", freeSlot.getSummary());
        assertEquals("The uid does not match", "57DD4AAF-3835-46B5-8A39-B3B253157F01", freeSlot.getUid());
        assertEquals("The recurrence rule does not match", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", freeSlot.getRecurrenceRule());
        assertEquals("The location does not match", "Denver", freeSlot.getLocation());
        assertEquals("The start timezone does not match", java.util.TimeZone.getTimeZone("America/Denver"), freeSlot.getStartTime().getTimeZone());
        assertEquals("The end timezone does not match", java.util.TimeZone.getTimeZone("America/Denver"), freeSlot.getEndTime().getTimeZone());

        // Availability 3
        //availability = importICal.getAvailability().get(2);
        assertEquals("The organizer uri does not match", "mailto:bernard@example.com", availability.getOrganizer().getUri());
        assertEquals("The uid does not match", "1852F9E1-E0AA-4572-B4C4-ED1680A4DA40", availability.getUid());
        assertEquals("The start timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), availability.getStartTime().getTimeZone());
        assertNull("The end timezone does not match", availability.getEndTime());

        freeSlots = availability.getAvailable();
        assertNotNull("No 'available' sub-components found", freeSlots);
        assertEquals("Expected 1 'available' sub-component", 1, freeSlots.size());

        freeSlot = freeSlots.get(0);
        assertEquals("The summary does not match", "Monday to Friday from 9:00 to 17:00", freeSlot.getSummary());
        assertEquals("The uid does not match", "D27C421F-16C2-4ECB-8352-C45CA352C72A", freeSlot.getUid());
        assertEquals("The recurrence rule does not match", "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", freeSlot.getRecurrenceRule());
        assertEquals("The location does not match", "Montreal", freeSlot.getLocation());
        assertEquals("The start timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), freeSlot.getStartTime().getTimeZone());
        assertEquals("The end timezone does not match", java.util.TimeZone.getTimeZone("America/Montreal"), freeSlot.getEndTime().getTimeZone());
    }
}
