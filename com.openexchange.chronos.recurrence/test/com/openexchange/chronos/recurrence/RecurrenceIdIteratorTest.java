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

import static com.openexchange.time.TimeTools.D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.recurrence.service.RecurrenceIdIterator;

/**
 * {@link RecurrenceIdIteratorTest} - Test for bug 66282
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(Parameterized.class)
public class RecurrenceIdIteratorTest extends RecurrenceServiceTest {

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<Object[]> retval = new ArrayList<Object[]>();
        for (String tzId : TimeZone.getAvailableIDs()) {
            retval.add(new Object[] { tzId });
        }
        return retval;
    }

    /**
     * Initializes a new {@link RecurrenceIdIteratorTest}.
     */
    public RecurrenceIdIteratorTest(String timeZone) {
        super(timeZone);
    }

    @Test
    public void testSingleChangeException() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;COUNT=10");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        SortedSet<RecurrenceId> changeExceptions = new TreeSet<RecurrenceId>();
        changeExceptions.add(new DefaultRecurrenceId(DT(D("03.10.2008 14:45:00", tz), master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        master.setChangeExceptionDates(changeExceptions);

        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(master.getRecurrenceRule(), master.getStartDate(), convert(changeExceptions));
        
        RecurrenceIdIterator iterator = new RecurrenceIdIterator(new TestRecurrenceConfig(), recurrenceData, false, null, null, null, null);
        checkPosition(iterator, changeExceptions, Collections.singletonList(Integer.valueOf(3)));
    }

    @Test
    public void testMultipleChangeExceptions() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;COUNT=10");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        setStartAndEndDates(master, "01.10.2008 14:45:00", "01.10.2008 15:45:00", false, tz);

        SortedSet<RecurrenceId> changeExceptions = new TreeSet<RecurrenceId>();
        List<Integer> changePositions = new ArrayList<Integer>(5);
        changeExceptions.add(new DefaultRecurrenceId(DT(D("03.10.2008 14:45:00", tz), master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        changePositions.add(Integer.valueOf(3));
        changeExceptions.add(new DefaultRecurrenceId(DT(D("04.10.2008 14:45:00", tz), master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        changePositions.add(Integer.valueOf(4));
        changeExceptions.add(new DefaultRecurrenceId(DT(D("05.10.2008 14:45:00", tz), master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        changePositions.add(Integer.valueOf(5));
        changeExceptions.add(new DefaultRecurrenceId(DT(D("08.10.2008 14:45:00", tz), master.getStartDate().getTimeZone(), master.getStartDate().isAllDay())));
        changePositions.add(Integer.valueOf(8));
        master.setChangeExceptionDates(changeExceptions);

        DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(master.getRecurrenceRule(), master.getStartDate(), convert(changeExceptions));
        RecurrenceIdIterator iterator = new RecurrenceIdIterator(new TestRecurrenceConfig(), recurrenceData, false, null, null, null, null);

        checkPosition(iterator, changeExceptions, changePositions);
    }

    private void checkPosition(RecurrenceIdIterator iterator, SortedSet<RecurrenceId> exceptions, List<Integer> exceptionPositions) {
        int i = 0;
        while (iterator.hasNext() && ++i <= 10) {
            if (exceptionPositions.contains(Integer.valueOf(i))) {
                // Skip exceptions, do not move iterator pointer
                continue;
            }
            RecurrenceId id = iterator.next();
            if (1 == i) {
                Assert.assertTrue(iterator.isFirstOccurrence());
            }
            Assert.assertTrue("Position is wrong!", i == iterator.getPosition());
            Assert.assertFalse("Exception was not skipped", exceptions.contains(id));
            if (10 == i) {
                Assert.assertTrue(iterator.isLastOccurrence());
            }
        }
    }

    private long[] convert(SortedSet<RecurrenceId> recurrenceIds) {
        long[] retval = new long[recurrenceIds.size()];
        int i = 0;
        for (RecurrenceId id : recurrenceIds) {
            retval[i++] = id.getValue().getTimestamp();
        }
        return retval;
    }

}
