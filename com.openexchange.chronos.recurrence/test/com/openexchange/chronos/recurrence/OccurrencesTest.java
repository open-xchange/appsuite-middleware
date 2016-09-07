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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.recurrence;

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
    public void noLimits() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

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
    public void limit() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, null, null, 3);
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
    public void leftBoundaryBeforeStart() {
        leftBoundary("02.10.2008 14:00:00", false);
    }

    @Test
    public void leftBoundaryAtStart() {
        leftBoundary("02.10.2008 14:45:00", false);
    }

    @Test
    public void leftBoundaryInside() {
        leftBoundary("02.10.2008 15:00:00", false);
    }

    @Test
    public void leftBoundaryAtEnd() {
        leftBoundary("02.10.2008 15:45:00", true);
    }

    @Test
    public void leftBoundaryBeforeStartAndLimit() {
        leftBoundaryWithLimit("02.10.2008 14:00:00", false);
    }

    @Test
    public void leftBoundaryAtStartAndLimit() {
        leftBoundaryWithLimit("02.10.2008 14:45:00", false);
    }

    @Test
    public void leftBoundaryInsideAndLimit() {
        leftBoundaryWithLimit("02.10.2008 15:00:00", false);
    }

    @Test
    public void leftBoundaryAtEndAndLimit() {
        leftBoundaryWithLimit("02.10.2008 15:45:00", true);
    }

    @Test
    public void rightBoundaryAtStart() {
        rightBoundary("05.10.2008 14:45:00", true);
    }

    @Test
    public void rightBoundaryInside() {
        rightBoundary("05.10.2008 15:00:00", false);
    }

    @Test
    public void rightBoundaryAtEnd() {
        rightBoundary("05.10.2008 15:45:00", false);
    }

    @Test
    public void rightBoundaryAfterEnd() {
        rightBoundary("05.10.2008 16:45:00", false);
    }

    @Test
    public void rightBoundaryAtStartAndLimit() {
        rightBoundaryWithLimit("05.10.2008 14:45:00");
    }

    @Test
    public void rightBoundaryInsideAndLimit() {
        rightBoundaryWithLimit("05.10.2008 15:00:00");
    }

    @Test
    public void rightBoundaryAtEndAndLimit() {
        rightBoundaryWithLimit("05.10.2008 15:45:00");
    }

    @Test
    public void rightBoundaryAfterEndAndLimit() {
        rightBoundaryWithLimit("05.10.2008 16:45:00");
    }

    @Test
    public void leftAndRightBoundary() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

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
    public void leftAndRightBoundaryAndLimit() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, getCal("03.10.2008 14:00:00"), getCal("07.10.2008 17:00:00"), 3);
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

    private void leftBoundary(String leftBoundary, boolean atEnd) {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

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

    private void leftBoundaryWithLimit(String leftBoundary, boolean atEnd) {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, getCal(leftBoundary), null, 1);
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

    private void rightBoundary(String rightBoundary, boolean atStart) {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

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

    private void rightBoundaryWithLimit(String rightBoundary) {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", tz));
        master.setEndDate(TimeTools.D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);

        Iterator<Event> instances = service.calculateInstances(master, null, getCal(rightBoundary), 1);
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
