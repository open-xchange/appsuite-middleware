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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.CalendarUtils.getDuration;
import static com.openexchange.chronos.common.CalendarUtils.shiftRecurrenceId;
import static org.dmfs.rfc5545.DateTime.parse;
import static org.junit.Assert.assertEquals;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;

/**
 * {@link UtilsTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UtilsTest {

    @Test
    public void testShiftRecurrenceIdDate() {
        assertEquals(new DefaultRecurrenceId("20180103"), shiftRecurrenceId(new DefaultRecurrenceId("20180103"), parse("20180101"), parse("20180101")));
        assertEquals(new DefaultRecurrenceId("20180203"), shiftRecurrenceId(new DefaultRecurrenceId("20180103"), parse("20180101"), parse("20180201")));
        assertEquals(new DefaultRecurrenceId("20180103"), shiftRecurrenceId(new DefaultRecurrenceId("20180203"), parse("20180201"), parse("20180101")));
        assertEquals(new DefaultRecurrenceId("20180603"), shiftRecurrenceId(new DefaultRecurrenceId("20180103"), parse("20180101"), parse("20180601")));
        assertEquals(new DefaultRecurrenceId("20180103"), shiftRecurrenceId(new DefaultRecurrenceId("20180603"), parse("20180601"), parse("20180101")));
    }

    @Test
    public void testShiftRecurrenceIdDateTimeWithSameTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals(new DefaultRecurrenceId("20180103T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180103T130000Z"), parse(timeZone, "20180101T140000"), parse(timeZone, "20180101T140000")));
        assertEquals(new DefaultRecurrenceId("20180203T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180103T130000Z"), parse(timeZone, "20180101T140000"), parse(timeZone, "20180201T140000")));
        assertEquals(new DefaultRecurrenceId("20180103T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180203T130000Z"), parse(timeZone, "20180201T140000"), parse(timeZone, "20180101T140000")));
        assertEquals(new DefaultRecurrenceId("20180603T120000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180103T130000Z"), parse(timeZone, "20180101T140000"), parse(timeZone, "20180601T140000")));
        assertEquals(new DefaultRecurrenceId("20180103T130000Z"), shiftRecurrenceId(new DefaultRecurrenceId("20180603T120000Z"), parse(timeZone, "20180601T140000"), parse(timeZone, "20180101T140000")));
    }

    @Test
    public void testGetDurationDate() {
        assertEquals("P0D", getDuration(DateTime.parse("20171205"), DateTime.parse("20171205")).toString());
        assertEquals("P1D", getDuration(DateTime.parse("20171205"), DateTime.parse("20171206")).toString());
        assertEquals("-P1D", getDuration(DateTime.parse("20171206"), DateTime.parse("20171205")).toString());
        assertEquals("P365D", getDuration(DateTime.parse("20161206"), DateTime.parse("20171206")).toString());
        assertEquals("-P365D", getDuration(DateTime.parse("20171206"), DateTime.parse("20161206")).toString());
        assertEquals("P731D", getDuration(DateTime.parse("20151206"), DateTime.parse("20171206")).toString());
        assertEquals("-P731D", getDuration(DateTime.parse("20171206"), DateTime.parse("20151206")).toString());
    }

    @Test
    public void testGetDurationDateTimeWithSameTimeZone() {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("P0D", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171205T160100")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse(timeZone, "20171205T160100"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171205T193000")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T193000"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT8H", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171206T000000")).toString());
        assertEquals("-PT8H", getDuration(DateTime.parse(timeZone, "20171206T000000"), DateTime.parse(timeZone, "20171205T160000")).toString());
        assertEquals("PT11H", getDuration(DateTime.parse(timeZone, "20171205T160000"), DateTime.parse(timeZone, "20171206T030000")).toString());
        assertEquals("-PT11H", getDuration(DateTime.parse(timeZone, "20171206T030000"), DateTime.parse(timeZone, "20171205T160000")).toString());
    }

    @Test
    public void testGetDurationDateTimeWithMixedTimeZone() {
        TimeZone timeZone1 = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone timeZone2 = TimeZone.getTimeZone("America/New_York");
        assertEquals("P0D", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T100100")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse(timeZone1, "20171205T160100"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T133000")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse(timeZone1, "20171205T193000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT8H", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T180000")).toString());
        assertEquals("-PT8H", getDuration(DateTime.parse(timeZone1, "20171206T000000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
        assertEquals("PT11H", getDuration(DateTime.parse(timeZone1, "20171205T160000"), DateTime.parse(timeZone2, "20171205T210000")).toString());
        assertEquals("-PT11H", getDuration(DateTime.parse(timeZone1, "20171206T030000"), DateTime.parse(timeZone2, "20171205T100000")).toString());
    }

    @Test
    public void testGetDurationDateAndDateTime() {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("P0D", getDuration(DateTime.parse("20171205"), DateTime.parse(timeZone, "20171205T000000")).toString());
        assertEquals("P1D", getDuration(DateTime.parse("20171205"), DateTime.parse(timeZone, "20171206T000000")).toString());
        assertEquals("-P1D", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171205T000000")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171206T000100")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171205T235900")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse("20171205"), DateTime.parse(timeZone, "20171205T033000")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse("20171206"), DateTime.parse(timeZone, "20171205T203000")).toString());
    }

    @Test
    public void testGetDurationDateTimeAndDate() {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
        assertEquals("P0D", getDuration(DateTime.parse(timeZone, "20171205T000000"), DateTime.parse("20171205")).toString());
        assertEquals("P1D", getDuration(DateTime.parse(timeZone, "20171205T000000"), DateTime.parse("20171206")).toString());
        assertEquals("-P1D", getDuration(DateTime.parse(timeZone, "20171206T000000"), DateTime.parse("20171205")).toString());
        assertEquals("PT1M", getDuration(DateTime.parse(timeZone, "20171205T235900"), DateTime.parse("20171206")).toString());
        assertEquals("-PT1M", getDuration(DateTime.parse(timeZone, "20171206T000100"), DateTime.parse("20171206")).toString());
        assertEquals("PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T203000"), DateTime.parse("20171206")).toString());
        assertEquals("-PT3H30M", getDuration(DateTime.parse(timeZone, "20171205T033000"), DateTime.parse("20171205")).toString());
    }

}
