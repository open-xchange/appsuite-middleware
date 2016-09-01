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
public class HourlyTest extends RecurrenceServiceTest {

    private static final int YEAR = 2016;

    public HourlyTest(String timeZone) {
        super(timeZone);
    }

    @Test
    public void _00() {
        test(0);
    }

    @Test
    public void _01() {
        test(1);
    }

    @Test
    public void _02() {
        test(2);
    }

    @Test
    public void _03() {
        test(3);
    }

    @Test
    public void _04() {
        test(4);
    }

    @Test
    public void _05() {
        test(5);
    }

    @Test
    public void _06() {
        test(6);
    }

    @Test
    public void _07() {
        test(7);
    }

    @Test
    public void _08() {
        test(8);
    }

    @Test
    public void _09() {
        test(9);
    }

    @Test
    public void _10() {
        test(10);
    }

    @Test
    public void _11() {
        test(11);
    }

    @Test
    public void _12() {
        test(12);
    }

    @Test
    public void _13() {
        test(13);
    }

    @Test
    public void _14() {
        test(14);
    }

    @Test
    public void _15() {
        test(15);
    }

    @Test
    public void _16() {
        test(16);
    }

    @Test
    public void _17() {
        test(17);
    }

    @Test
    public void _18() {
        test(18);
    }

    @Test
    public void _19() {
        test(19);
    }

    @Test
    public void _20() {
        test(20);
    }

    @Test
    public void _21() {
        test(21);
    }

    @Test
    public void _22() {
        test(22);
    }

    @Test
    public void _23() {
        test(23);
    }

    @Test
    public void fullTime() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar s = GregorianCalendar.getInstance(utc);
        s.set(YEAR, Calendar.OCTOBER, 1, 0, 0, 0);
        s.set(Calendar.MILLISECOND, 0);
        Calendar e = GregorianCalendar.getInstance(utc);
        e.setTimeInMillis(s.getTimeInMillis());
        e.add(Calendar.DAY_OF_MONTH, 1);
        master.setStartDate(s.getTime());
        master.setEndDate(e.getTime());
        master.setAllDay(true);
        master.setTimeZone(timeZone);

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

    public void moreThanOneDay(int hourOfDay) {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        Calendar s = GregorianCalendar.getInstance(tz);
        s.set(YEAR, Calendar.OCTOBER, 1, hourOfDay, 0, 0);
        s.set(Calendar.MILLISECOND, 0);
        Calendar e = GregorianCalendar.getInstance(tz);
        e.setTimeInMillis(s.getTimeInMillis() + 3600000L * 36);
        master.setStartDate(s.getTime());
        master.setEndDate(e.getTime());
        master.setTimeZone(timeZone);

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

    private void test(int hourOfDay) {
        moreThanOneDay(hourOfDay);

        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        Calendar s = GregorianCalendar.getInstance(tz);
        s.set(YEAR, Calendar.OCTOBER, 1, hourOfDay, 0, 0);
        s.set(Calendar.MILLISECOND, 0);
        Calendar e = GregorianCalendar.getInstance(tz);
        e.setTimeInMillis(s.getTimeInMillis() + 3600000L);
        master.setStartDate(s.getTime());
        master.setEndDate(e.getTime());
        master.setTimeZone(timeZone);

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
