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

package com.openexchange.chronos.recurrence.compat;

import static com.openexchange.chronos.compat.Appointment2Event.getRecurrenceData;
import static org.junit.Assert.fail;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.recurrence.service.RecurrenceUtils;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.java.util.TimeZones;

/**
 * {@link MWB273Test}
 *
 * Unexpected error [Error performing calendar migration in context xxx] caused by NullPointerException
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class MWB273Test {

    @Test
    public void testSeriesPatterns() {
        testSeriesPattern("t|4|i|1|b|8|c|11|s|1544227200000|", null, true);
        testSeriesPattern("t|5|i|3|a|2|s|1547476200000|", "UTC", false);
        testSeriesPattern("t|2|i|1|a|4|o|11|s|1570525200000|e|1576540800000|", "CST6CDT", false);
        testSeriesPattern("t|6|i|1|a|2|b|5|c|4|s|1590364800000", null, true);
        testSeriesPattern("t|4|i|1|b|18|c|7|s|1534550400000", null, true);
        testSeriesPattern("t|4|i|1|b|27|c|7|s|1535328000000", null, true);
        testSeriesPattern("t|4|i|1|b|21|c|11|s|1545350400000", null, true);
        testSeriesPattern("t|4|i|1|b|23|c|0|s|1548201600000", null, true);
        testSeriesPattern("t|4|i|1|b|2|c|11|s|1543708800000", null, true);
        testSeriesPattern("t|4|i|1|b|24|c|5|s|1529798400000", null, true);
        testSeriesPattern("t|4|i|1|b|6|c|3|s|1522972800000", null, true);
        testSeriesPattern("t|2|i|6|a|2|s|1546880400000", "UTC", false);
        testSeriesPattern("t|4|i|1|b|6|c|11|s|1575590400000", null, true);
        testSeriesPattern("t|4|i|1|b|20|c|2|s|1584662400000", null, true);
        testSeriesPattern("t|4|i|1|b|10|c|0|s|1578614400000", null, true);
        testSeriesPattern("t|4|i|1|b|25|c|11|s|1577232000000", null, true);
        testSeriesPattern("t|4|i|1|b|24|c|11|s|1577145600000", null, true);
        testSeriesPattern("t|4|i|1|b|31|c|11|s|1577750400000", null, true);
        testSeriesPattern("t|5|i|3|a|32|b|3|o|6|s|1579284000000", "CST6CDT", false);
        testSeriesPattern("t|5|i|3|a|32|b|3|o|6|s|1584723600000", "CST6CDT", false);
        testSeriesPattern("t|5|i|3|a|32|b|2|o|6|s|1578668400000", "CST6CDT", false);
        testSeriesPattern("t|5|i|3|a|32|b|2|o|6|s|1584108000000", "CST6CDT", false);
        testSeriesPattern("t|5|i|3|a|32|b|2|o|5|s|1588946400000", "CST6CDT", false);
        testSeriesPattern("t|6|i|1|a|2|b|3|c|1|s|1581897600000", null, true);
        testSeriesPattern("t|6|i|1|a|2|b|3|c|0|s|1579478400000", null, true);
        testSeriesPattern("t|4|i|1|b|1|c|0|s|1577836800000", null, true);
        testSeriesPattern("t|4|i|1|b|14|c|1|s|1581638400000", null, true);
        testSeriesPattern("t|4|i|1|b|4|c|6|s|1593820800000", null, true);
        testSeriesPattern("t|4|i|1|b|3|c|6|s|1593734400000", null, true);
        testSeriesPattern("t|6|i|1|a|2|b|1|c|8|s|1599436800000", null, true);
        testSeriesPattern("t|6|i|1|a|2|b|2|c|9|s|1602460800000", null, true);
        testSeriesPattern("t|6|i|1|a|16|b|4|c|10|s|1606348800000", null, true);
        testSeriesPattern("t|2|i|1|a|4|s|1575385200000", "America/Chicago", false);
        testSeriesPattern("t|4|i|1|b|2|c|11|s|1575244800000", null, true);
        testSeriesPattern("t|4|i|1|b|8|c|1|s|1581120000000", null, true);
    }

    private void testSeriesPattern(String databasePattern, String timeZoneId, boolean allDay) {
        try {
            TimeZone timeZone = null == timeZoneId ? TimeZones.UTC : TimeZone.getTimeZone(timeZoneId);
            RecurrenceData recurrenceData = getRecurrenceData(new SeriesPattern(databasePattern), timeZone, allDay);
            RecurrenceUtils.getRecurrenceIterator(recurrenceData, true);
        } catch (Exception e) {
            fail("For " + databasePattern + ": " + e.getMessage());
        }
    }

}
