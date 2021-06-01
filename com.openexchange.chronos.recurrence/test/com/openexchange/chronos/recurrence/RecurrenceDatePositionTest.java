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
import static org.junit.Assert.assertNull;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.openexchange.chronos.Event;

/**
 * {@link RecurrenceDatePositionTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class RecurrenceDatePositionTest extends AbstractSingleTimeZoneTest {

    public RecurrenceDatePositionTest(String timeZone) {
        super(timeZone);
    }

    @Test
    public void dailyNoEnd() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        master.setStartDate(DT("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong date position.", getCal("01.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 0));
        assertEquals("Wrong date position.", getCal("02.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("09.01.2009 14:45:00"), service.calculateRecurrenceDatePosition(master, 101));
    }

    @Test
    public void dailyThreeOccurrences() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;COUNT=3");
        master.setStartDate(DT("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong date position.", getCal("01.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("02.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("03.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 3));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 4));
    }

    @Test
    public void dailyUntil() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;UNTIL=" + getUntilZulu(getCal("12.10.2008 14:45:00")));
        master.setStartDate(DT("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong date position.", getCal("01.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("12.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 12));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 13));
    }

    @Test
    public void weeklyNoEnd() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=WE;INTERVAL=1");
        master.setStartDate(DT("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong date position.", getCal("01.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("08.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("15.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 3));
        assertEquals("Wrong date position.", getCal("22.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 4));
        assertEquals("Wrong date position.", getCal("29.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 5));
        assertEquals("Wrong date position.", getCal("05.11.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 6));
    }

    @Test
    public void weeklyThreeOccurrencesAndStartOffset() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=TH;INTERVAL=1;COUNT=3");
        master.setStartDate(DT("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong date position.", getCal("01.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("02.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("09.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 3));
        if (COUNT_DTSTART) {
            assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 4));
        } else {
            assertEquals("Wrong date position.", getCal("16.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 4));
            assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 5));
        }
    }

    @Test
    public void weeklyComplex() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;INTERVAL=2;UNTIL=" + getUntilZulu(getCal("27.10.2008 08:00:00")));
        master.setStartDate(DT("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone), false));
        assertEquals("Wrong date position.", getCal("01.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("02.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("03.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 3));
        assertEquals("Wrong date position.", getCal("13.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 4));
        assertEquals("Wrong date position.", getCal("14.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 5));
        assertEquals("Wrong date position.", getCal("15.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 6));
        assertEquals("Wrong date position.", getCal("16.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 7));
        assertEquals("Wrong date position.", getCal("17.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 8));
        assertEquals("Wrong date position.", getCal("27.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 9));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 10));
    }

}
