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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.time.TimeTools;

/**
 * {@link DeleteExceptionsTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class DeleteExceptionsTest extends AbstractSingleTimeZoneTest {

    public DeleteExceptionsTest(String timeZone) {
        super(timeZone);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void simple() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);
        SortedSet<RecurrenceId> deleteExceptions = new TreeSet<RecurrenceId>();
        deleteExceptions.add(new DefaultRecurrenceId(DT("03.10.2008 14:45:00", tz, false)));
        master.setDeleteExceptionDates(deleteExceptions);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, null);
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
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("05.10.2008 14:45:00", tz), TimeTools.D("05.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 4, count);
    }

    @Test
    public void multiple() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);
        SortedSet<RecurrenceId> deleteExceptions = new TreeSet<RecurrenceId>();
        deleteExceptions.add(new DefaultRecurrenceId(DT("01.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("03.10.2008 14:45:00", tz, false)));
        master.setDeleteExceptionDates(deleteExceptions);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("02.10.2008 14:45:00", tz), TimeTools.D("02.10.2008 15:45:00", tz));
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

    @Test
    public void limit() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);
        SortedSet<RecurrenceId> deleteExceptions = new TreeSet<RecurrenceId>();
        deleteExceptions.add(new DefaultRecurrenceId(DT("01.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("03.10.2008 14:45:00", tz, false)));
        master.setDeleteExceptionDates(deleteExceptions);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, I(3), null);
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("02.10.2008 14:45:00", tz), TimeTools.D("02.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("05.10.2008 14:45:00", tz), TimeTools.D("05.10.2008 15:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);

    }

    @Test
    public void leftAndRightBoundary() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);
        SortedSet<RecurrenceId> deleteExceptions = new TreeSet<RecurrenceId>();
        deleteExceptions.add(new DefaultRecurrenceId(DT("03.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("05.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("09.10.2008 14:45:00", tz, false)));
        master.setDeleteExceptionDates(deleteExceptions);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, getCal("03.10.2008 14:00:00"), getCal("09.10.2008 17:00:00"), null, null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("06.10.2008 14:45:00", tz), TimeTools.D("06.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("07.10.2008 14:45:00", tz), TimeTools.D("07.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, TimeTools.D("08.10.2008 14:45:00", tz), TimeTools.D("08.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 4, count);
    }

    @Test
    public void leftAndRightBoundaryAndLimit() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);
        SortedSet<RecurrenceId> deleteExceptions = new TreeSet<RecurrenceId>();
        deleteExceptions.add(new DefaultRecurrenceId(DT("03.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("05.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("09.10.2008 14:45:00", tz, false)));
        master.setDeleteExceptionDates(deleteExceptions);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, getCal("03.10.2008 14:00:00"), getCal("10.10.2008 17:00:00"), I(3), null);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, TimeTools.D("04.10.2008 14:45:00", tz), TimeTools.D("04.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, TimeTools.D("06.10.2008 14:45:00", tz), TimeTools.D("06.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, TimeTools.D("07.10.2008 14:45:00", tz), TimeTools.D("07.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    @Test
    public void allDeleted() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;COUNT=3");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);
        SortedSet<RecurrenceId> deleteExceptions = new TreeSet<RecurrenceId>();
        deleteExceptions.add(new DefaultRecurrenceId(DT("01.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("02.10.2008 14:45:00", tz, false)));
        deleteExceptions.add(new DefaultRecurrenceId(DT("03.10.2008 14:45:00", tz, false)));
        master.setDeleteExceptionDates(deleteExceptions);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, I(3), null);
        assertFalse("Didn't expect any occurrences at all", instances.hasNext());
        try {
            instances.next();
            fail("Didn't expect any occurrences at all");
        } catch (Exception e) {
            assertTrue("Wrong error.", NoSuchElementException.class.isInstance(e));
        }

    }

}
