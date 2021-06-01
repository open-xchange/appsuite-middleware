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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;

/**
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug27476Test extends ManagedAppointmentTest {

    public Bug27476Test() {
        super();
    }

    @Test
    public void testRepeatedImportOfASeriesWithChangeException() throws Exception {
        String ical = prepareICal();
        ICalImportResponse icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical, false));
        assertFalse("Initial import failed", icalResponse.hasError());
        assertTrue("Should import master and change exception", icalResponse.getImports().length == 2);
    }

    private static String prepareICal() {
        String tmp = ICAL.replaceAll("\\$\\{UID\\}", UUID.randomUUID().toString());
        String recurrence = UUID.randomUUID().toString();
        return tmp.replaceAll("\\$\\{UID1\\}", recurrence);
    }

    private static final String ICAL = "BEGIN:VCALENDAR\n" + "PRODID:Open-Xchange\n" + "VERSION:2.0\n" + "CALSCALE:GREGORIAN\n" + "METHOD:REQUEST\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20130709T213302Z\n" + "SUMMARY:new test\n" + "DTSTART;TZID=America/New_York:20130705T160000\n" + "DTEND;TZID=America/New_York:20130705T170000\n" + "CLASS:PUBLIC\n" + "LOCATION:rwc\n" + "TRANSP:OPAQUE\n" + "UID:${UID}\n" + "CREATED:20130705T213744Z\n" + "LAST-MODIFIED:20130705T213744Z\n" + "ORGANIZER:mailto:app102@openwave.com\n" + "SEQUENCE:0\n" + "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" + "END:VEVENT\n" + "BEGIN:VTIMEZONE\n" + "TZID:America/New_York\n" + "TZURL:http://tzurl.org/zoneinfo/America/New_York\n" + "X-LIC-LOCATION:America/New_York\n" + "BEGIN:DAYLIGHT\n" + "TZOFFSETFROM:-0500\n" + "TZOFFSETTO:-0400\n" + "TZNAME:EDT\n" + "DTSTART:20070311T020000\n" + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU\n" + "END:DAYLIGHT\n" + "BEGIN:STANDARD\n" + "TZOFFSETFROM:-0400\n" + "TZOFFSETTO:-0500\n" + "TZNAME:EST\n" + "DTSTART:20071104T020000\n" + "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\n" + "END:STANDARD\n" + "BEGIN:STANDARD\n" + "TZOFFSETFROM:-045602\n" + "TZOFFSETTO:-0500\n" + "TZNAME:EST\n" + "DTSTART:18831118T120358\n" + "RDATE:\n" + "END:STANDARD\n" + "BEGIN:DAYLIGHT\n" + "TZOFFSETFROM:-0500\n" + "TZOFFSETTO:-0400\n" + "TZNAME:EDT\n" + "DTSTART:19180331T020000\n" + "END:DAYLIGHT\n" + "BEGIN:STANDARD\n" + "TZOFFSETFROM:-0400\n" + "TZOFFSETTO:-0500\n" + "TZNAME:EST\n" + "DTSTART:19181027T020000\n" + "END:STANDARD\n" + "BEGIN:STANDARD\n" + "TZOFFSETFROM:-0500\n" + "TZOFFSETTO:-0500\n" + "TZNAME:EST\n" + "DTSTART:19200101T000000\n" + "END:STANDARD\n" + "BEGIN:DAYLIGHT\n" + "TZOFFSETFROM:-0500\n" + "TZOFFSETTO:-0400\n" + "TZNAME:EWT\n" + "DTSTART:19420209T020000\n" + "RDATE:\n" + "END:DAYLIGHT\n" + "BEGIN:DAYLIGHT\n" + "TZOFFSETFROM:-0400\n" + "TZOFFSETTO:-0400\n" + "TZNAME:EPT\n" + "DTSTART:19450814T190000\n" + "RDATE:\n" + "END:DAYLIGHT\n" + "END:VTIMEZONE\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20130709T213358Z\n" + "SUMMARY:new test test\n" + "DTSTART;TZID=America/New_York:20130705T180000\n" + "DTEND;TZID=America/New_York:20130705T190000\n" + "CLASS:PUBLIC\n" + "LOCATION:rwc\n" + "TRANSP:OPAQUE\n" + "UID:${UID}\n" + "CREATED:20130705T213912Z\n" + "LAST-MODIFIED:20130705T213912Z\n" + "ORGANIZER:mailto:app102@openwave.com\n" + "SEQUENCE:0\n" + "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" + "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20130709T213358Z\n" + "SUMMARY:appt 1\n" + "DTSTART;TZID=America/New_York:20130708T090000\n" + "DTEND;TZID=America/New_York:20130708T100000\n" + "CLASS:PUBLIC\n" + "LOCATION:rwc\n" + "TRANSP:OPAQUE\n" + "UID:${UID}\n" + "CREATED:20130705T223643Z\n" + "LAST-MODIFIED:20130705T223643Z\n" + "ORGANIZER:mailto:app102@openwave.com\n" + "SEQUENCE:0\n" + "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" + "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20130709T213358Z\n" + "SUMMARY:appt 2\n" + "DTSTART;TZID=America/New_York:20130708T090000\n" + "DTEND;TZID=America/New_York:20130708T110000\n" + "CLASS:PUBLIC\n" + "LOCATION:rwc\n" + "TRANSP:OPAQUE\n" + "UID:${UID}\n" + "CREATED:20130705T223723Z\n" + "LAST-MODIFIED:20130705T223825Z\n" + "ORGANIZER:mailto:app102@openwave.com\n" + "SEQUENCE:1\n" + "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" + "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20130709T213358Z\n" + "SUMMARY:test series\n" + "DTSTART;TZID=America/New_York:20130709T190000\n" + "DTEND;TZID=America/New_York:20130709T200000\n" + "CLASS:PUBLIC\n" + "LOCATION:rwc\n" + "TRANSP:OPAQUE\n" + "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=TU\n" + "UID:${UID1}\n" + "CREATED:20130705T225751Z\n" + "LAST-MODIFIED:20130705T225854Z\n" + "ORGANIZER:mailto:app102@openwave.com\n" + "SEQUENCE:1\n" + "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" + "BEGIN:VALARM\n" + "TRIGGER:-PT15M\n" + "ACTION:DISPLAY\n" + "DESCRIPTION:Open-XChange\n" + "END:VALARM\n" + "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20130709T213358Z\n" + "SUMMARY:test series\n" + "DTSTART;TZID=America/New_York:20130709T200000\n" + "DTEND;TZID=America/New_York:20130709T230000\n" + "CLASS:PUBLIC\n" + "LOCATION:rwc\n" + "TRANSP:OPAQUE\n" + "RECURRENCE-ID:20130709T230000Z\n" + "UID:${UID1}\n" + "CREATED:20130705T225824Z\n" + "LAST-MODIFIED:20130705T225854Z\n" + "ORGANIZER:mailto:app102@openwave.com\n" + "SEQUENCE:1\n" + "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" + "BEGIN:VALARM\n" + "TRIGGER:-PT15M\n" + "ACTION:DISPLAY\n" + "DESCRIPTION:Open-XChange\n" + "END:VALARM\n" + "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTAMP:20130709T213358Z\n" + "SUMMARY:test RSVP\n" + "DESCRIPTION:test RSVP\n" + "DTSTART;TZID=America/New_York:20130705T180000\n" + "DTEND;TZID=America/New_York:20130705T190000\n" + "CLASS:PUBLIC\n" + "LOCATION:rwc\n" + "TRANSP:OPAQUE\n" + "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=FR\n" + "UID:${UID}\n" + "CREATED:20130708T185749Z\n" + "LAST-MODIFIED:20130705T214908Z\n" + "ORGANIZER:mailto:app101@openwave.com\n" + "SEQUENCE:0\n" + "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" + "END:VEVENT\n" + "END:VCALENDAR";

}
