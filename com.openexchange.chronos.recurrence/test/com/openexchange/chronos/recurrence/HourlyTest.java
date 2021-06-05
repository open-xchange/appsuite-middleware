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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.openexchange.chronos.Event;

/**
 * {@link HourlyTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class HourlyTest extends AbstractSingleTimeZoneTest {

    private static final int YEAR = 2020;

    public HourlyTest(String timeZone) {
        super(timeZone);
    }

    @Test
    public void _00() throws Exception {
        test(0);
    }

    @Test
    public void _01() throws Exception {
        test(1);
    }

    @Test
    public void _02() throws Exception {
        test(2);
    }

    @Test
    public void _03() throws Exception {
        test(3);
    }

    @Test
    public void _04() throws Exception {
        test(4);
    }

    @Test
    public void _05() throws Exception {
        test(5);
    }

    @Test
    public void _06() throws Exception {
        test(6);
    }

    @Test
    public void _07() throws Exception {
        test(7);
    }

    @Test
    public void _08() throws Exception {
        test(8);
    }

    @Test
    public void _09() throws Exception {
        test(9);
    }

    @Test
    public void _10() throws Exception {
        test(10);
    }

    @Test
    public void _11() throws Exception {
        test(11);
    }

    @Test
    public void _12() throws Exception {
        test(12);
    }

    @Test
    public void _13() throws Exception {
        test(13);
    }

    @Test
    public void _14() throws Exception {
        test(14);
    }

    @Test
    public void _15() throws Exception {
        test(15);
    }

    @Test
    public void _16() throws Exception {
        test(16);
    }

    @Test
    public void _17() throws Exception {
        test(17);
    }

    @Test
    public void _18() throws Exception {
        test(18);
    }

    @Test
    public void _19() throws Exception {
        test(19);
    }

    @Test
    public void _20() throws Exception {
        test(20);
    }

    @Test
    public void _21() throws Exception {
        test(21);
    }

    @Test
    public void _22() throws Exception {
        test(22);
    }

    @Test
    public void _23() throws Exception {
        test(23);
    }

    @Test
    public void fullTime() throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar s = GregorianCalendar.getInstance(utc);
        s.set(YEAR, Calendar.OCTOBER, 1, 0, 0, 0);
        s.set(Calendar.MILLISECOND, 0);
        Calendar e = GregorianCalendar.getInstance(utc);
        e.setTimeInMillis(s.getTimeInMillis());
        e.add(Calendar.DAY_OF_MONTH, 1);
        setStartAndEndDates(master, s.getTime(), e.getTime(), true, null);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);

        Calendar start = GregorianCalendar.getInstance(utc);
        start.set(YEAR, Calendar.OCTOBER, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = GregorianCalendar.getInstance(utc);
        end.setTimeInMillis(start.getTimeInMillis());
        end.add(Calendar.DAY_OF_MONTH, 1);

        int count = 0;
        while (instances.hasNext() && count++ <= 365) {
            Event instance = instances.next();
            compareInstanceWithMaster(master, instance, start.getTime(), end.getTime());
            start.add(Calendar.DAY_OF_MONTH, 1);
            end.add(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void test(int hourOfDay) throws Exception {
        oneHourLong(hourOfDay);
        moreThanOneDay(hourOfDay);
    }

    private void moreThanOneDay(int hourOfDay) throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        Calendar s = GregorianCalendar.getInstance(tz);
        s.set(YEAR, Calendar.OCTOBER, 1, hourOfDay, 0, 0);
        s.set(Calendar.MILLISECOND, 0);
        Calendar e = GregorianCalendar.getInstance(tz);
        e.setTimeInMillis(s.getTimeInMillis() + 3600000L * 36);
        setStartAndEndDates(master, s.getTime(), e.getTime(), false, tz);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);

        Calendar start = GregorianCalendar.getInstance(tz);
        start.set(YEAR, Calendar.OCTOBER, 1, hourOfDay, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = GregorianCalendar.getInstance(tz);
        end.setTimeInMillis(start.getTimeInMillis() + 3600000L * 36);

        int count = 0;
        while (instances.hasNext() && count++ <= 365) {
            Event instance = instances.next();
            compareInstanceWithMaster(master, instance, start.getTime(), end.getTime());
            start.add(Calendar.DAY_OF_MONTH, 1);
            end.add(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, hourOfDay);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis() + 3600000L * 36);
        }
    }

    private void oneHourLong(int hourOfDay) throws Exception {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        Calendar s = GregorianCalendar.getInstance(tz);
        s.set(YEAR, Calendar.OCTOBER, 1, hourOfDay, 0, 0);
        s.set(Calendar.MILLISECOND, 0);
        Calendar e = GregorianCalendar.getInstance(tz);
        e.setTimeInMillis(s.getTimeInMillis() + 3600000L);
        setStartAndEndDates(master, s.getTime(), e.getTime(), false, tz);

        Iterator<Event> instances = service.calculateInstances(master, null, null, null);

        Calendar start = GregorianCalendar.getInstance(tz);
        start.set(YEAR, Calendar.OCTOBER, 1, hourOfDay, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = GregorianCalendar.getInstance(tz);
        end.setTimeInMillis(start.getTimeInMillis() + 3600000L);

        int count = 0;
        while (instances.hasNext() && count++ <= 365) {
            Event instance = instances.next();
            compareInstanceWithMaster(master, instance, start.getTime(), end.getTime());
            start.add(Calendar.DAY_OF_MONTH, 1);
            end.add(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, hourOfDay);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);
            end.setTimeInMillis(start.getTimeInMillis() + 3600000L);
        }
    }

}
