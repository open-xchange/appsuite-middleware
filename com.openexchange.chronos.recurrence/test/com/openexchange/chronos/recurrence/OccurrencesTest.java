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

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Iterator;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.openexchange.chronos.Event;
import com.openexchange.time.TimeTools;

/**
 * {@link OccurrencesTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class OccurrencesTest extends AbstractSingleTimeZoneTest {

    public OccurrencesTest(String timeZone) {
        super(timeZone);
    }

    @Test
    public void noLimits() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("01.10.2008 14:45:00", tz), TimeTools.D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("02.10.2008 14:45:00", tz), TimeTools.D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("03.10.2008 14:45:00", tz), TimeTools.D("03.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, TimeTools.D("05.10.2008 14:45:00", tz), TimeTools.D("05.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 5, count);
    }

    @Test
    public void limit() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, null, null, I(3));
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("01.10.2008 14:45:00", tz), TimeTools.D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("02.10.2008 14:45:00", tz), TimeTools.D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("03.10.2008 14:45:00", tz), TimeTools.D("03.10.2008 15:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    @Test
    public void leftBoundaryBeforeStart() throws Exception {
        leftBoundary("02.10.2008 14:00:00", false);
    }

    @Test
    public void leftBoundaryAtStart() throws Exception {
        leftBoundary("02.10.2008 14:45:00", false);
    }

    @Test
    public void leftBoundaryInside() throws Exception {
        leftBoundary("02.10.2008 15:00:00", false);
    }

    @Test
    public void leftBoundaryAtEnd() throws Exception {
        leftBoundary("02.10.2008 15:45:00", true);
    }

    @Test
    public void leftBoundaryBeforeStartAndLimit() throws Exception {
        leftBoundaryWithLimit("02.10.2008 14:00:00", false);
    }

    @Test
    public void leftBoundaryAtStartAndLimit() throws Exception {
        leftBoundaryWithLimit("02.10.2008 14:45:00", false);
    }

    @Test
    public void leftBoundaryInsideAndLimit() throws Exception {
        leftBoundaryWithLimit("02.10.2008 15:00:00", false);
    }

    @Test
    public void leftBoundaryAtEndAndLimit() throws Exception {
        leftBoundaryWithLimit("02.10.2008 15:45:00", true);
    }

    @Test
    public void rightBoundaryAtStart() throws Exception {
        rightBoundary("05.10.2008 14:45:00", true);
    }

    @Test
    public void rightBoundaryInside() throws Exception {
        rightBoundary("05.10.2008 15:00:00", false);
    }

    @Test
    public void rightBoundaryAtEnd() throws Exception {
        rightBoundary("05.10.2008 15:45:00", false);
    }

    @Test
    public void rightBoundaryAfterEnd() throws Exception {
        rightBoundary("05.10.2008 16:45:00", false);
    }

    @Test
    public void rightBoundaryAtStartAndLimit() throws Exception {
        rightBoundaryWithLimit("05.10.2008 14:45:00");
    }

    @Test
    public void rightBoundaryInsideAndLimit() throws Exception {
        rightBoundaryWithLimit("05.10.2008 15:00:00");
    }

    @Test
    public void rightBoundaryAtEndAndLimit() throws Exception {
        rightBoundaryWithLimit("05.10.2008 15:45:00");
    }

    @Test
    public void rightBoundaryAfterEndAndLimit() throws Exception {
        rightBoundaryWithLimit("05.10.2008 16:45:00");
    }

    @Test
    public void leftAndRightBoundary() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, getCal("03.10.2008 14:00:00"), getCal("07.10.2008 17:00:00"), null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("03.10.2008 14:45:00", tz), TimeTools.D("03.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("05.10.2008 14:45:00", tz), TimeTools.D("05.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("06.10.2008 14:45:00", tz), TimeTools.D("06.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, TimeTools.D("07.10.2008 14:45:00", tz), TimeTools.D("07.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 5, count);
    }

    @Test
    public void leftAndRightBoundaryAndLimit() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, getCal("03.10.2008 14:00:00"), getCal("07.10.2008 17:00:00"), I(3));
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("03.10.2008 14:45:00", tz), TimeTools.D("03.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("05.10.2008 14:45:00", tz), TimeTools.D("05.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    private void leftBoundary(String leftBoundary, boolean atEnd) throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, getCal(leftBoundary), null, null);
        int count = atEnd ? 1 : 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("02.10.2008 14:45:00", tz), TimeTools.D("02.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("03.10.2008 14:45:00", tz), TimeTools.D("03.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("05.10.2008 14:45:00", tz), TimeTools.D("05.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, TimeTools.D("06.10.2008 14:45:00", tz), TimeTools.D("06.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 5, count);
    }

    private void leftBoundaryWithLimit(String leftBoundary, boolean atEnd) throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, getCal(leftBoundary), null, I(1));
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    if (atEnd) {
                        compareInstanceWithMaster(master, instance, TimeTools.D("03.10.2008 14:45:00", tz), TimeTools.D("03.10.2008 15:45:00", tz));
                    } else {
                        compareInstanceWithMaster(master, instance, TimeTools.D("02.10.2008 14:45:00", tz), TimeTools.D("02.10.2008 15:45:00", tz));
                    }
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 1, count);
    }

    private void rightBoundary(String rightBoundary, boolean atStart) throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, null, getCal(rightBoundary), null);
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("01.10.2008 14:45:00", tz), TimeTools.D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("02.10.2008 14:45:00", tz), TimeTools.D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("03.10.2008 14:45:00", tz), TimeTools.D("03.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 5:
                    if (atStart) {
                        fail("Too many instances.");
                    } else {
                        compareInstanceWithMaster(master, instance, TimeTools.D("05.10.2008 14:45:00", tz), TimeTools.D("05.10.2008 15:45:00", tz));
                    }
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", atStart ? 4 : 5, count);
    }

    private void rightBoundaryWithLimit(String rightBoundary) throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        Iterator<Event> instances = service.calculateInstances(master, null, getCal(rightBoundary), I(1));
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("01.10.2008 14:45:00", tz), TimeTools.D("01.10.2008 15:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 1, count);
    }

}
