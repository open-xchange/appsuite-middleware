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

package com.openexchange.chronos.recurrence;

import static org.junit.Assert.assertEquals;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.openexchange.chronos.Event;

/**
 * {@link RecurrencePositionTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class RecurrencePositionTest extends AbstractSingleTimeZoneTest {

    public RecurrencePositionTest(String timeZone) {
        super(timeZone);
    }

    @Test
    public void dailyNoEnd() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        master.setStartDate(DT("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong position", 1, service.calculateRecurrencePosition(master, getCal("01.10.2008 14:45:00")));
        assertEquals("Wrong position", 0, service.calculateRecurrencePosition(master, getCal("01.10.2008 14:46:00")));
        assertEquals("Wrong position", 2, service.calculateRecurrencePosition(master, getCal("02.10.2008 14:45:00")));
        assertEquals("Wrong position", 101, service.calculateRecurrencePosition(master, getCal("09.01.2009 14:45:00")));
    }

    @Test
    public void dailyThreeOccurrences() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;COUNT=3");
        master.setStartDate(DT("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong position", 1, service.calculateRecurrencePosition(master, getCal("01.10.2008 14:45:00")));
        assertEquals("Wrong position", 3, service.calculateRecurrencePosition(master, getCal("03.10.2008 14:45:00")));
        assertEquals("Wrong position", 0, service.calculateRecurrencePosition(master, getCal("04.10.2008 14:45:00")));
    }

    @Test
    public void dailyUntil() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;UNTIL=" + getUntilZulu(getCal("12.10.2008 14:45:00")));
        master.setStartDate(DT("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong position", 1, service.calculateRecurrencePosition(master, getCal("01.10.2008 14:45:00")));
        assertEquals("Wrong position", 12, service.calculateRecurrencePosition(master, getCal("12.10.2008 14:45:00")));
        assertEquals("Wrong position", 0, service.calculateRecurrencePosition(master, getCal("13.10.2008 14:45:00")));
    }

    @Test
    public void weeklyNoEnd() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=WE;INTERVAL=1");
        master.setStartDate(DT("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong position", 1, service.calculateRecurrencePosition(master, getCal("01.10.2008 08:00:00")));
        assertEquals("Wrong position", 2, service.calculateRecurrencePosition(master, getCal("08.10.2008 08:00:00")));
        assertEquals("Wrong position", 3, service.calculateRecurrencePosition(master, getCal("15.10.2008 08:00:00")));
        assertEquals("Wrong position", 4, service.calculateRecurrencePosition(master, getCal("22.10.2008 08:00:00")));
        assertEquals("Wrong position", 5, service.calculateRecurrencePosition(master, getCal("29.10.2008 08:00:00")));
        assertEquals("Wrong position", 6, service.calculateRecurrencePosition(master, getCal("05.11.2008 08:00:00")));
    }

    @Test
    public void weeklyThreeOccurrencesAndStartOffset() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=TH;INTERVAL=1;COUNT=3");
        master.setStartDate(DT("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong position", 1, service.calculateRecurrencePosition(master, getCal("01.10.2008 08:00:00")));
        assertEquals("Wrong position", 2, service.calculateRecurrencePosition(master, getCal("02.10.2008 08:00:00")));
        assertEquals("Wrong position", 3, service.calculateRecurrencePosition(master, getCal("09.10.2008 08:00:00")));
        if (COUNT_DTSTART) {
            assertEquals("Wrong position", 0, service.calculateRecurrencePosition(master, getCal("16.10.2008 08:00:00")));
        } else {
            assertEquals("Wrong position", 4, service.calculateRecurrencePosition(master, getCal("16.10.2008 08:00:00")));
            assertEquals("Wrong position", 0, service.calculateRecurrencePosition(master, getCal("23.10.2008 08:00:00")));
        }
    }

    @Test
    public void weeklyComplex() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;INTERVAL=2;UNTIL=" + getUntilZulu(getCal("27.10.2008 08:00:00")));
        master.setStartDate(DT("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong position", 1, service.calculateRecurrencePosition(master, getCal("01.10.2008 08:00:00")));
        assertEquals("Wrong position", 2, service.calculateRecurrencePosition(master, getCal("02.10.2008 08:00:00")));
        assertEquals("Wrong position", 3, service.calculateRecurrencePosition(master, getCal("03.10.2008 08:00:00")));
        assertEquals("Wrong position", 4, service.calculateRecurrencePosition(master, getCal("13.10.2008 08:00:00")));
        assertEquals("Wrong position", 5, service.calculateRecurrencePosition(master, getCal("14.10.2008 08:00:00")));
        assertEquals("Wrong position", 6, service.calculateRecurrencePosition(master, getCal("15.10.2008 08:00:00")));
        assertEquals("Wrong position", 7, service.calculateRecurrencePosition(master, getCal("16.10.2008 08:00:00")));
        assertEquals("Wrong position", 8, service.calculateRecurrencePosition(master, getCal("17.10.2008 08:00:00")));
        assertEquals("Wrong position", 9, service.calculateRecurrencePosition(master, getCal("27.10.2008 08:00:00")));
        assertEquals("Wrong position", 0, service.calculateRecurrencePosition(master, getCal("28.10.2008 08:00:00")));
    }

}
