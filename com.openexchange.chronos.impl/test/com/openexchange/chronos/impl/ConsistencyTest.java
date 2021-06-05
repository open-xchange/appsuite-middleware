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
