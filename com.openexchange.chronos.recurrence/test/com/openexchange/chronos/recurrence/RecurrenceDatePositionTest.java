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
import static org.junit.Assert.assertNull;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.openexchange.chronos.Event;
import com.openexchange.time.TimeTools;

/**
 * {@link RecurrenceDatePositionTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
@RunWith(Parameterized.class)
public class RecurrenceDatePositionTest extends RecurrenceServiceTest {

    public RecurrenceDatePositionTest(String timeZone) {
        super(timeZone);
    }

    @Test
    public void dailyNoEnd() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1");
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone)));
        master.setTimeZone(timeZone);
        assertEquals("Wrong date position.", getCal("01.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 0));
        assertEquals("Wrong date position.", getCal("02.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("09.01.2009 14:45:00"), service.calculateRecurrenceDatePosition(master, 101));
    }

    @Test
    public void dailyThreeOccurrences() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;COUNT=3");
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone)));
        master.setTimeZone(timeZone);
        assertEquals("Wrong date position.", getCal("01.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("02.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("03.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 3));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 4));
    }

    @Test
    public void dailyUntil() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=DAILY;INTERVAL=1;UNTIL=" + getUntilZulu(getCal("12.10.2008 14:45:00")));
        master.setStartDate(TimeTools.D("01.10.2008 14:45:00", TimeZone.getTimeZone(timeZone)));
        master.setTimeZone(timeZone);
        assertEquals("Wrong date position.", getCal("01.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("12.10.2008 14:45:00"), service.calculateRecurrenceDatePosition(master, 12));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 13));
    }

    @Test
    public void weeklyNoEnd() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=WE;INTERVAL=1");
        master.setStartDate(TimeTools.D("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone)));
        master.setTimeZone(timeZone);
        assertEquals("Wrong date position.", getCal("01.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("08.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("15.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 3));
        assertEquals("Wrong date position.", getCal("22.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 4));
        assertEquals("Wrong date position.", getCal("29.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 5));
        assertEquals("Wrong date position.", getCal("05.11.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 6));
    }

    @Test
    public void weeklyThreeOccurrencesAndStartOffset() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=TH;INTERVAL=1;COUNT=3");
        master.setStartDate(TimeTools.D("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone)));
        master.setTimeZone(timeZone);
        assertEquals("Wrong date position.", getCal("01.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 1));
        assertEquals("Wrong date position.", getCal("02.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 2));
        assertEquals("Wrong date position.", getCal("09.10.2008 08:00:00"), service.calculateRecurrenceDatePosition(master, 3));
        assertNull("Wrong date position.", service.calculateRecurrenceDatePosition(master, 4));
    }

    @Test
    public void weeklyComplex() {
        Event master = new Event();
        master.setRecurrenceRule("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;INTERVAL=2;UNTIL=" + getUntilZulu(getCal("27.10.2008 08:00:00")));
        master.setStartDate(TimeTools.D("01.10.2008 08:00:00", TimeZone.getTimeZone(timeZone)));
        master.setTimeZone(timeZone);
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
