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
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.recurrence.service.RecurrenceIterator;

/**
 * {@link RecurrenceIteratorTest} - Test for bug 66282
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(Parameterized.class)
public class RecurrenceIteratorTest extends RecurrenceServiceTest {

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        List<Object[]> retval = new ArrayList<Object[]>();
        for (String tzId : TimeZone.getAvailableIDs()) {
            retval.add(new Object[] { tzId });
        }
        return retval;
    }

    /**
     * Initializes a new {@link RecurrenceIteratorTest}.
     */
    public RecurrenceIteratorTest(String timeZone) {
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

        RecurrenceIterator iterator = new RecurrenceIterator(new TestRecurrenceConfig(), master, false, null, null, Integer.valueOf(11), false);
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

        RecurrenceIterator iterator = new RecurrenceIterator(new TestRecurrenceConfig(), master, false, null, null, Integer.valueOf(11), false);

        checkPosition(iterator, changeExceptions, changePositions);
    }

    private void checkPosition(RecurrenceIterator iterator, SortedSet<RecurrenceId> exceptions, List<Integer> exceptionPositions) {
        int i = 0;
        while (iterator.hasNext() && ++i <= 10) {
            if (exceptionPositions.contains(Integer.valueOf(i))) {
                // Skip exceptions, do not move iterator pointer
                continue;
            }
            RecurrenceId id = iterator.next().getRecurrenceId();
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
}
