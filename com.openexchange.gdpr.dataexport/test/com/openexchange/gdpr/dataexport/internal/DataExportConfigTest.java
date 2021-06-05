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

package com.openexchange.gdpr.dataexport.internal;

import static org.junit.Assert.assertTrue;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.gdpr.dataexport.DataExportConfig;
import com.openexchange.gdpr.dataexport.DayOfWeekTimeRanges;
import com.openexchange.gdpr.dataexport.TimeOfTheDay;
import com.openexchange.gdpr.dataexport.TimeRange;

/**
 * {@link DataExportConfigTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportConfigTest {

    /**
     * Initializes a new {@link DataExportConfigTest}.
     */
    public DataExportConfigTest() {
        super();
    }

    @Test
    public void testScheduleParser1() {
        DataExportConfig config = DataExportConfig.builder().parse("Sun-Sat 0-24").build();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            assertTrue(config.getRangesOfTheWeek().containsKey(dayOfWeek));
        }
    }

    @Test
    public void testScheduleParser2() {
        DataExportConfig config = DataExportConfig.builder().parse("Mon-Wed 0-24").build();

        assertTrue(config.getRangesOfTheWeek().containsKey(DayOfWeek.MONDAY));
        assertTrue(config.getRangesOfTheWeek().containsKey(DayOfWeek.TUESDAY));
        assertTrue(config.getRangesOfTheWeek().containsKey(DayOfWeek.WEDNESDAY));
    }

    @Test
    public void testScheduleParser3() {
        DataExportConfig config = DataExportConfig.builder().parse("Mon-Sun 0-6").build();

        Map<DayOfWeek, DayOfWeekTimeRanges> rangesOfTheWeek = config.getRangesOfTheWeek();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            assertTrue(rangesOfTheWeek.containsKey(dayOfWeek));
        }

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            DayOfWeekTimeRanges dayOfWeekTimeRanges = rangesOfTheWeek.get(dayOfWeek);

            List<TimeRange> ranges = dayOfWeekTimeRanges.getRanges();
            assertTrue(ranges.size() == 1);

            TimeRange timeRange = ranges.get(0);
            assertTrue(timeRange.getStart().equals(new TimeOfTheDay(0, 0, 0)));
            assertTrue(timeRange.getEnd().equals(new TimeOfTheDay(6, 0, 0)));
        }
    }

}
