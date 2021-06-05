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

import static com.openexchange.chronos.common.CalendarUtils.asDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.java.Streams;

/**
 * {@link TestXProperties}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TestXProperties extends ICalTest {

    @Test
    public void testImportXProperties() throws Exception {
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "METHOD:REQUEST\r\n" +
            "PRODID:Microsoft Exchange Server 2007\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:W. Europe Standard Time\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:16010101T030000\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:16010101T020000\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "ORGANIZER;CN=Otto Example:mailto:otto@example.com\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=Horst Exam\r\n" +
            " ple:MAILTO:horst@example.org\r\n" +
            "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE;CN=klause@exa\r\n" +
            " mple.com:MAILTO:klaus@example.com\r\n" +
            "DESCRIPTION;LANGUAGE=de-CH:Zeit: Donnerstag\\, 8. September 2011 12:00-12:30\r\n" +
            " . Westeuropa (Normalzeit)\\nOrt: SG\\n\\n*~*~*~*~*~*~*~*~*~*\\n\\nTest-App via \r\n" +
            " OWA \\;-)\\n\\nGruss\\nOtto\\n\\n\r\n" +
            "SUMMARY;LANGUAGE=de-CH:Otto spendiert eine Flasche Wein\\, nein besser zwei.\r\n" +
            " ..\r\n" +
            "DTSTART;TZID=W. Europe Standard Time:20111208T120000\r\n" +
            "DTEND;TZID=W. Europe Standard Time:20111208T123000\r\n" +
            "UID:040000008200E00074C5B7101A82E00800000000D0AA8057A561CC01000000000000000\r\n" +
            " 010000000B4762DB1A2E3C24BA17F2D09C0F0189F\r\n" +
            "CLASS:PUBLIC\r\n" +
            "PRIORITY:5\r\n" +
            "DTSTAMP:20110823T135821Z\r\n" +
            "TRANSP:OPAQUE\r\n" +
            "STATUS:CONFIRMED\r\n" +
            "SEQUENCE:1\r\n" +
            "LOCATION;LANGUAGE=de-CH:SG\r\n" +
            "X-MICROSOFT-CDO-APPT-SEQUENCE:1\r\n" +
            "X-MICROSOFT-CDO-OWNERAPPTID:1016563675\r\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE\r\n" +
            "X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY\r\n" +
            "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE\r\n" +
            "X-MICROSOFT-CDO-IMPORTANCE:1\r\n" +
            "X-MICROSOFT-CDO-INSTTYPE:0\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:REMINDER\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;

        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.EXTRA_PROPERTIES, new String[] { "*" });
        Event event = iCalService.importICal(inputStream, iCalParameters).getEvents().get(0);

        assertEquals("040000008200E00074C5B7101A82E00800000000D0AA8057A561CC01000000000000000010000000B4762DB1A2E3C24BA17F2D09C0F0189F", event.getUid());
        assertEquals("mailto:otto@example.com", event.getOrganizer().getUri());
        assertEquals(D("2011-12-08 12:00:00", "CET"), asDate(event.getStartDate()));
        assertEquals(D("2011-12-08 12:30:00", "CET"), asDate(event.getEndDate()));
        assertEquals("W. Europe Standard Time", event.getStartDate().getTimeZone().getID());
        assertEquals("W. Europe Standard Time", event.getEndDate().getTimeZone().getID());

        assertNotNull(event.getExtendedProperties().get("X-MICROSOFT-CDO-APPT-SEQUENCE"));
        assertEquals("1", event.getExtendedProperties().get("X-MICROSOFT-CDO-APPT-SEQUENCE").getValue());
        assertNotNull(event.getExtendedProperties().get("X-MICROSOFT-CDO-OWNERAPPTID"));
        assertEquals("1016563675", event.getExtendedProperties().get("X-MICROSOFT-CDO-OWNERAPPTID").getValue());
        assertNotNull(event.getExtendedProperties().get("X-MICROSOFT-CDO-BUSYSTATUS"));
        assertEquals("TENTATIVE", event.getExtendedProperties().get("X-MICROSOFT-CDO-BUSYSTATUS").getValue());
        assertNotNull(event.getExtendedProperties().get("X-MICROSOFT-CDO-INTENDEDSTATUS"));
        assertEquals("BUSY", event.getExtendedProperties().get("X-MICROSOFT-CDO-INTENDEDSTATUS").getValue());
        assertNotNull(event.getExtendedProperties().get("X-MICROSOFT-CDO-ALLDAYEVENT"));
        assertEquals("FALSE", event.getExtendedProperties().get("X-MICROSOFT-CDO-ALLDAYEVENT").getValue());
        assertNotNull(event.getExtendedProperties().get("X-MICROSOFT-CDO-IMPORTANCE"));
        assertEquals("1", event.getExtendedProperties().get("X-MICROSOFT-CDO-IMPORTANCE").getValue());
        assertNotNull(event.getExtendedProperties().get("X-MICROSOFT-CDO-INSTTYPE"));
        assertEquals("0", event.getExtendedProperties().get("X-MICROSOFT-CDO-INSTTYPE").getValue());

        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.contains("X-MICROSOFT-CDO-APPT-SEQUENCE:1"));
        assertTrue(exportedICal.contains("X-MICROSOFT-CDO-OWNERAPPTID:1016563675"));
        assertTrue(exportedICal.contains("X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE"));
        assertTrue(exportedICal.contains("X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY"));
        assertTrue(exportedICal.contains("X-MICROSOFT-CDO-ALLDAYEVENT:FALSE"));
        assertTrue(exportedICal.contains("X-MICROSOFT-CDO-IMPORTANCE:1"));
        assertTrue(exportedICal.contains("X-MICROSOFT-CDO-INSTTYPE:0"));

    }

}
