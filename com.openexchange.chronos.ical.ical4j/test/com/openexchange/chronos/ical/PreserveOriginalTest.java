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
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.openexchange.chronos.Event;
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
                "END:VCALENDAR\r\n";
        EventData eventData = importEvent(iCal);

        Event event = eventData.getEvent();
        assertEquals("uid1@example.com", event.getUid());

        String originalICal = Streams.stream2string(eventData.getComponent().getStream(), "UTF-8");
        assertTrue(originalICal.contains("X-UNKNOWN1:abc"));

        List<AlarmData> alarms = eventData.getAlarms();
        assertEquals(2, alarms.size());
        AlarmData alarmData = alarms.get(0);
        originalICal = Streams.stream2string(alarmData.getComponent().getStream(), "UTF-8");
        assertTrue(originalICal.contains("X-UNKNOWN1:abc"));
    }

}
