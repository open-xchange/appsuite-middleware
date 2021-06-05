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
import org.junit.Test;
import com.openexchange.chronos.Event;

/**
 * {@link Bug17963Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug17963Test extends ICalTest {

    @Test
    public void testImport() throws Exception {
        String iCal =
            "BEGIN:VCALENDAR\n"+
            "VERSION:2.0\n"+
            "BEGIN:VEVENT\n"+
            "DTSTART;TZID=Europe/Rome:20100202T103000\n"+
            "DTEND;TZID=Europe/Rome:20100202T120000\n"+
            "RRULE:FREQ=WEEKLY;BYDAY=TU;UNTIL=20100705T215959Z\n"+
            "EXDATE:20111128\n"+
            "DTSTAMP:20110105T174810Z\n"+
            "SUMMARY:Team-Meeting\n"+
            "END:VEVENT\n" +
            "END:VCALENDAR\n"
        ;
        Event event = importEvent(iCal);
        assertEquals("Team-Meeting", event.getSummary());
        assertNotNull(event.getDeleteExceptionDates());
        assertEquals(1, event.getDeleteExceptionDates().size());
    }

}
