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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.openexchange.chronos.Event;

/**
 * {@link ChangeExceptions}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class ChangeExceptions extends AbstractSingleTimeZoneTest {

    public ChangeExceptions(String timeZone) {
        super(timeZone);
    }

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void simple() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:45:00", tz), D("03.10.2008 19:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareChangeExceptionWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:45:00", tz), D("03.10.2008 19:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, D("05.10.2008 14:45:00", tz), D("05.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 5, count);
    }

    @Test
    public void multiple() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("01.10.2008 14:45:00", tz));
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(getInstance(master, D("01.10.2008 14:45:00", tz), D("01.10.2008 18:45:00", tz), D("01.10.2008 19:45:00", tz)));
        changeExceptions.add(getInstance(master, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:12:00", tz), D("03.10.2008 18:13:00", tz)));

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareChangeExceptionWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 18:45:00", tz), D("01.10.2008 19:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareChangeExceptionWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:12:00", tz), D("03.10.2008 18:13:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, D("05.10.2008 14:45:00", tz), D("05.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 5, count);
    }

    @Test
    public void moveBeforeFirst() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("30.09.2008 18:45:00", tz), D("30.09.2008 19:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareChangeExceptionWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("30.09.2008 18:45:00", tz), D("30.09.2008 19:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, D("05.10.2008 14:45:00", tz), D("05.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 5, count);
    }

    @Test
    public void moveOnAnother() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareChangeExceptionWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, D("05.10.2008 14:45:00", tz), D("05.10.2008 15:45:00", tz));
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
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:45:00", tz), D("03.10.2008 19:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, 3, changeExceptions);
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareChangeExceptionWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:45:00", tz), D("03.10.2008 19:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    @Test
    public void changeOutsideLimit() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("05.10.2008 18:45:00", tz), D("05.10.2008 19:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, 3, changeExceptions);
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    @Test
    public void moveOutsideRightBoundary() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("05.10.2008 14:45:00", tz), D("05.10.2008 15:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, getCal("01.10.2008 14:00:00"), getCal("04.10.2008 17:00:00"), null, changeExceptions);
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    @Test
    public void moveOutsideLeftBoundary() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(getInstance(master, D("03.10.2008 14:45:00", tz), D("30.09.2008 18:45:00", tz), D("30.09.2008 19:45:00", tz)));

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, getCal("01.10.2008 14:00:00"), getCal("04.10.2008 17:00:00"), null, changeExceptions);
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    @Test
    public void createFullTimeException() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("03.10.2008 00:00:00", utc), D("04.10.2008 00:00:00", utc));
        change.setAllDay(true);
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        boolean found1 = false, found2 = false, found3 = false, found4 = false, found5 = false;
        int count = 0;
        Date previous = null;
        while (instances.hasNext()) {
            Event instance = instances.next();
            if (instance.getStartDate().equals(D("01.10.2008 14:45:00", tz))) {
                compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                found1 = true;
            } else if (instance.getStartDate().equals(D("02.10.2008 14:45:00", tz))) {
                compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                found2 = true;
            } else if (instance.getStartDate().equals(D("03.10.2008 00:00:00", utc))) {
                compareFullTimeChangeExceptionWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("03.10.2008 00:00:00", utc), D("04.10.2008 00:00:00", utc));
                found3 = true;
            } else if (instance.getStartDate().equals(D("04.10.2008 14:45:00", tz))) {
                compareInstanceWithMaster(master, instance, D("04.10.2008 14:45:00", tz), D("04.10.2008 15:45:00", tz));
                found4 = true;
            } else if (instance.getStartDate().equals(D("05.10.2008 14:45:00", tz))) {
                compareInstanceWithMaster(master, instance, D("05.10.2008 14:45:00", tz), D("05.10.2008 15:45:00", tz));
                found5 = true;
            } else {
                fail("Bad occurrence/exception found.");
            }
            if (count++ > 0) {
                assertFalse("Bad order of occurrences.", previous.after(instance.getStartDate()));
            }
            if (count >= 5) {
                break;
            }
            previous = instance.getStartDate();
        }
        assertTrue("Missing instance.", found1);
        assertTrue("Missing instance.", found2);
        assertTrue("Missing instance.", found3);
        assertTrue("Missing instance.", found4);
        assertTrue("Missing instance.", found5);
        assertEquals("Missing instance.", 5, count);
    }

    @Test
    public void createNormalExceptionFromFullTimeSeries() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 00:00:00", utc));
        master.setEndDate(D("01.10.2008 00:00:00", utc));
        master.setTimeZone(timeZone);
        master.setAllDay(true);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 00:00:00", utc));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("03.10.2008 00:00:00", utc), D("03.10.2008 14:35:00", tz), D("03.10.2008 16:35:00", tz));
        change.removeAllDay();
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        boolean found1 = false, found2 = false, found3 = false, found4 = false, found5 = false;
        int count = 0;
        Date previous = null;
        while (instances.hasNext()) {
            Event instance = instances.next();
            if (instance.getStartDate().equals(D("01.10.2008 00:00:00", utc))) {
                compareInstanceWithMaster(master, instance, D("01.10.2008 00:00:00", utc), D("01.10.2008 00:00:00", utc));
                found1 = true;
            } else if (instance.getStartDate().equals(D("02.10.2008 00:00:00", utc))) {
                compareInstanceWithMaster(master, instance, D("02.10.2008 00:00:00", utc), D("02.10.2008 00:00:00", utc));
                found2 = true;
            } else if (instance.getStartDate().equals(D("03.10.2008 14:35:00", tz))) {
                compareChangeExceptionWithFullTimeMaster(master, instance, D("03.10.2008 00:00:00", utc), D("03.10.2008 14:35:00", tz), D("03.10.2008 16:35:00", tz));
                found3 = true;
            } else if (instance.getStartDate().equals(D("04.10.2008 00:00:00", utc))) {
                compareInstanceWithMaster(master, instance, D("04.10.2008 00:00:00", utc), D("04.10.2008 00:00:00", utc));
                found4 = true;
            } else if (instance.getStartDate().equals(D("05.10.2008 00:00:00", utc))) {
                compareInstanceWithMaster(master, instance, D("05.10.2008 00:00:00", utc), D("05.10.2008 00:00:00", utc));
                found5 = true;
            } else {
                fail("Bad occurrence/exception found.");
            }
            if (count++ > 0) {
                assertFalse("Bad order of occurrences.", previous.after(instance.getStartDate()));
            }
            if (count >= 5) {
                break;
            }
            previous = instance.getStartDate();
        }
        assertTrue("Missing instance.", found1);
        assertTrue("Missing instance.", found2);
        assertTrue("Missing instance.", found3);
        assertTrue("Missing instance.", found4);
        assertTrue("Missing instance.", found5);
        assertEquals("Missing instance.", 5, count);
    }

    @Test
    public void afterLastRegularOccurrence() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;COUNT=3");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("02.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);

        Event change = getInstance(master, D("02.10.2008 14:45:00", tz), D("23.10.2008 18:45:00", tz), D("23.10.2008 19:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        int count = 0;
        while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("03.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareChangeExceptionWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("23.10.2008 18:45:00", tz), D("23.10.2008 19:45:00", tz));
                    break;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 3, count);
    }

    @Test
    public void changeAndDeleteException() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        master.setStartDate(D("01.10.2008 14:45:00", tz));
        master.setEndDate(D("01.10.2008 15:45:00", tz));
        master.setTimeZone(timeZone);
        List<Date> changeExceptionDates = new ArrayList<Date>();
        changeExceptionDates.add(D("03.10.2008 14:45:00", tz));
        master.setChangeExceptionDates(changeExceptionDates);
        List<Date> deleteExceptionDates = new ArrayList<Date>();
        deleteExceptionDates.add(D("04.10.2008 14:45:00", tz));
        master.setDeleteExceptionDates(deleteExceptionDates);

        Event change = getInstance(master, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:45:00", tz), D("03.10.2008 19:45:00", tz));
        List<Event> changeExceptions = new ArrayList<Event>();
        changeExceptions.add(change);

        Iterator<Event> instances = service.calculateInstancesRespectExceptions(master, null, null, null, changeExceptions);
        int count = 0;
        outer: while (instances.hasNext()) {
            Event instance = instances.next();
            switch (++count) {
                case 1:
                    compareInstanceWithMaster(master, instance, D("01.10.2008 14:45:00", tz), D("01.10.2008 15:45:00", tz));
                    break;
                case 2:
                    compareInstanceWithMaster(master, instance, D("02.10.2008 14:45:00", tz), D("02.10.2008 15:45:00", tz));
                    break;
                case 3:
                    compareChangeExceptionWithMaster(master, instance, D("03.10.2008 14:45:00", tz), D("03.10.2008 18:45:00", tz), D("03.10.2008 19:45:00", tz));
                    break;
                case 4:
                    compareInstanceWithMaster(master, instance, D("05.10.2008 14:45:00", tz), D("05.10.2008 15:45:00", tz));
                    break;
                case 5:
                    compareInstanceWithMaster(master, instance, D("06.10.2008 14:45:00", tz), D("06.10.2008 15:45:00", tz));
                    break outer;
                default:
                    fail("Too many instances.");
                    break;
            }
        }
        assertEquals("Missing instance.", 5, count);
    }
}
