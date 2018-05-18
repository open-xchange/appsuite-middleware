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

package com.openexchange.chronos.ical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedPropertyParameter;

/**
 * {@link ExtendedAttendeeParametersTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ExtendedAttendeeParametersTest extends ICalTest {

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
            "ATTENDEE;ROLE=REQ-PARTICIPANT;SCHEDULE-AGENT=NONE:mailto:cyrus@example.com\r\n" +
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
        Event event = importEvent(iCal);
        assertEquals("guid-1.example.com", event.getUid());
        assertEquals("mailto:mrbig@example.com", event.getOrganizer().getUri());
        assertTrue(null != event.getAttendees() && 1 == event.getAttendees().size());
        Attendee attendee = event.getAttendees().get(0);
        assertNotNull(attendee);
        assertEquals("mailto:cyrus@example.com", attendee.getUri());
        assertTrue(null != attendee.getExtendedParameters() && 1 == attendee.getExtendedParameters().size());
        assertEquals(new ExtendedPropertyParameter("SCHEDULE-AGENT", "NONE"), attendee.getExtendedParameters().get(0));

        String exportedICal = exportEvent(event);
        assertTrue(exportedICal.contains("SCHEDULE-AGENT=NONE"));
    }

}
