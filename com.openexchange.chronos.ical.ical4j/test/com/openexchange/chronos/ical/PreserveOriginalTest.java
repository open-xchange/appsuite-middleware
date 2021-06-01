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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.Test;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.java.Streams;

/**
 * {@link PreserveOriginalTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PreserveOriginalTest extends ICalTest {

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
            "X-UNKNOWN1:abc\r\n" +
            "SUMMARY:Networld+Interop Conference\r\n" +
            "DESCRIPTION:Networld+Interop Conference\r\n" +
            "  and Exhibit\\nAtlanta World Congress Center\\n\r\n" +
            " Atlanta\\, Georgia\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:REMINDER\r\n" +
            "TRIGGER;RELATED=START:-PT15M\r\n" +
            "X-UNKNOWN1:abc\r\n" +
            "END:VALARM\r\n" +
            "BEGIN:VALARM\r\n" +
            "ACTION:DISPLAY\r\n" +
            "DESCRIPTION:REMINDER\r\n" +
            "TRIGGER;RELATED=START:-PT5M\r\n" +
            "X-UNKNOWN1:abc\r\n" +
            "END:VALARM\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;

        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.EXTRA_PROPERTIES, new String[] { "*" });
        Event event = iCalService.importICal(inputStream, iCalParameters).getEvents().get(0);

        assertEquals("uid1@example.com", event.getUid());

        ExtendedProperty xUnknownProperty = event.getExtendedProperties().get("X-UNKNOWN1");
        assertNotNull(xUnknownProperty);
        assertEquals("abc", xUnknownProperty.getValue());

        List<Alarm> alarms = event.getAlarms();
        assertEquals(2, alarms.size());
        Alarm alarm = alarms.get(0);

        xUnknownProperty = alarm.getExtendedProperties().get("X-UNKNOWN1");
        assertNotNull(xUnknownProperty);
        assertEquals("abc", xUnknownProperty.getValue());
    }

    @Test
    public void testPreserveDirParameter() throws Exception {
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:http://www.example.com/calendarapplication/\r\n" +
            "METHOD:PUBLISH\r\n" +
            "BEGIN:VEVENT\r\n" +
            "UID:461092315540@example.com\r\n" +
            "ORGANIZER;CN=\"Alice Balder, Example Inc.\":MAILTO:alice@example.com\r\n" +
            "LOCATION:Somewhere\r\n" +
            "SUMMARY:Eine Kurzinfo\r\n" +
            "DESCRIPTION:Beschreibung des Termines\r\n" +
            "CLASS:PUBLIC\r\n" +
            "DTSTART:20060910T220000Z\r\n" +
            "DTEND:20060919T215900Z\r\n" +
            "DTSTAMP:20060812T125900Z\r\n" +
            "ORGANIZER:mailto:otto@example.com\r\n" +
            "ATTENDEE;RSVP=TRUE;ROLE=REQ-PARTICIPANT;CUTYPE=INDICIDUAL;\r\n" +
            " DIR=\"ldap://host.com:66/horst\":mailto:horst@example.org\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR"
        ;

        ByteArrayInputStream inputStream = Streams.newByteArrayInputStream(iCal.getBytes("UTF-8"));
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.EXTRA_PROPERTIES, new String[] { "*" });
        Event event = iCalService.importICal(inputStream, iCalParameters).getEvents().get(0);

        assertEquals("461092315540@example.com", event.getUid());
        assertEquals("Somewhere", event.getLocation());

        ExtendedProperty attendeeProperty = event.getExtendedProperties().get("ATTENDEE");
        assertNotNull(attendeeProperty);
        assertEquals("ldap://host.com:66/horst", attendeeProperty.getParameter("DIR"));

        event.setLocation("Somewhere else");
        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.replaceAll("\\s+", "").contains("DIR=\"ldap://host.com:66/horst\""));
        assertTrue(exportedICal.contains("LOCATION:Somewhere else"));
    }

}

