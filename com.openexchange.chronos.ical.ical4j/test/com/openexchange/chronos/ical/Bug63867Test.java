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
import org.junit.Test;
import com.openexchange.chronos.Event;

/**
 * {@link Bug63867Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug63867Test extends ICalTest {

    @Test
    public void testImport() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" + 
            "PRODID;X-RICAL-TZSOURCE=TZINFO:-//com.denhaven2/NONSGML ri_cal gem//EN\r\n" + 
            "CALSCALE:GREGORIAN\r\n" + 
            "VERSION:2.0\r\n" + 
            "BEGIN:VTIMEZONE\r\n" + 
            "TZID;X-RICAL-TZSOURCE=TZINFO:Europe/Berlin\r\n" + 
            "BEGIN:DAYLIGHT\r\n" + 
            "DTSTART:20190331T020000\r\n" + 
            "RDATE:20190331T020000\r\n" + 
            "TZOFFSETFROM:+0100\r\n" + 
            "TZOFFSETTO:+0200\r\n" + 
            "TZNAME:CEST\r\n" + 
            "END:DAYLIGHT\r\n" + 
            "END:VTIMEZONE\r\n" + 
            "BEGIN:VEVENT\r\n" + 
            "DTEND;TZID=Europe/Berlin;VALUE=DATE-TIME:20190406T110000\r\n" + 
            "DTSTART;TZID=Europe/Berlin;VALUE=DATE-TIME:20190406T101500\r\n" + 
            "DTSTAMP;VALUE=DATE-TIME:20190225T110357Z\r\n" + 
            "UID:test\r\n" + 
            "SUMMARY:test\r\n" + 
            "END:VEVENT\r\n" + 
            "END:VCALENDAR\r\n" 
            ; // @formatter:on
        Event event = importEvent(iCal);
        assertEquals(1554538500000l, event.getStartDate().getTimestamp());
        assertEquals("Europe/Berlin", event.getStartDate().getTimeZone().getID());
    }
    
}
