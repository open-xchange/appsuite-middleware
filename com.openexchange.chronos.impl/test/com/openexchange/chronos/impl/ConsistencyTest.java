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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.time.TimeTools;

/**
 * {@link ConsistencyTest}
 * 
 * Tests different consistency operations that get applied when storing event data.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class ConsistencyTest {

    @Test
    public void testNormalizeRecurrenceIDs_1() {
        TimeZone tzBerlin = TimeZone.getTimeZone("Europe/Berlin");
        DateTime startDate = new DateTime(tzBerlin, TimeTools.D("2019-12-03 16:40:00", tzBerlin).getTime());
        DateTime recurrenceId = new DateTime(tzBerlin, TimeTools.D("2019-12-03 16:40:00", tzBerlin).getTime());
        testNormalization(startDate, recurrenceId);
    }

    @Test
    public void testNormalizeRecurrenceIDs_2() {
        TimeZone tzBerlin = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone tzMoscow = TimeZone.getTimeZone("Europe/Moscow");
        DateTime startDate = new DateTime(tzBerlin, TimeTools.D("2019-12-03 16:40:00", tzBerlin).getTime());
        DateTime recurrenceId = new DateTime(tzMoscow, TimeTools.D("2019-12-03 18:40:00", tzMoscow).getTime());
        testNormalization(startDate, recurrenceId);
    }

    @Test
    public void testNormalizeRecurrenceIDs_3() {
        DateTime startDate = new DateTime(2019, 11, 3);
        DateTime recurrenceId = new DateTime(2019, 11, 3);
        testNormalization(startDate, recurrenceId);
    }

    @Test
    public void testNormalizeRecurrenceIDs_4() {
        TimeZone tzBerlin = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone tzUTC = TimeZone.getTimeZone("UTC");
        DateTime startDate = new DateTime(tzBerlin, TimeTools.D("2019-12-03 16:40:00", tzBerlin).getTime());
        DateTime recurrenceId = new DateTime(TimeTools.D("2019-12-03 18:40:00", tzUTC).getTime());
        testNormalization(startDate, recurrenceId);
    }

    @Test
    public void testNormalizeRecurrenceIDs_5() {
        TimeZone tzUTC = TimeZone.getTimeZone("UTC");
        TimeZone tzBerlin = TimeZone.getTimeZone("Europe/Berlin");
        DateTime startDate = new DateTime(TimeTools.D("2019-12-03 18:40:00", tzUTC).getTime());
        DateTime recurrenceId = new DateTime(tzBerlin, TimeTools.D("2019-12-03 16:40:00", tzBerlin).getTime());
        testNormalization(startDate, recurrenceId);
    }

    @Test
    public void testNormalizeRecurrenceIDs_6() {
        TimeZone tzBerlin = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone tzNewYork = TimeZone.getTimeZone("America/New_York");
        DateTime startDate = new DateTime(tzBerlin, TimeTools.D("2019-11-01 16:40:00", tzBerlin).getTime());
        DateTime recurrenceId = new DateTime(tzNewYork, TimeTools.D("2019-11-01 11:40:00", tzNewYork).getTime());
        testNormalization(startDate, recurrenceId);
    }

    private void testNormalization(DateTime referenceDate, DateTime recurrenceIdDate) {
        DefaultRecurrenceId recurrenceId = new DefaultRecurrenceId(recurrenceIdDate);
        Event event = new Event();
        event.setRecurrenceId(recurrenceId);
        Consistency.normalizeRecurrenceIDs(referenceDate, event);
        DateTime normalizedRecurrenceIdDate = event.getRecurrenceId().getValue();
        assertTrue("All-Day not equal", referenceDate.isAllDay() == normalizedRecurrenceIdDate.isAllDay());
        assertTrue("Recurrence not equal", recurrenceId.matches(event.getRecurrenceId()));
        assertEquals("Timestamp shifted", recurrenceIdDate.getTimestamp(), normalizedRecurrenceIdDate.getTimestamp());
        assertEquals("Timezones not equal", referenceDate.getTimeZone(), normalizedRecurrenceIdDate.getTimeZone());
    }

}
