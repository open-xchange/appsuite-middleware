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

/**
 * {@link Bug66412Test}
 * 
 * chronos migration "java.lang.IllegalStateException: too many empty recurrence sets"
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug66412Test {

    @Test
    public void testSeriesPatterns() {
        testSeriesPattern("t|4|i|1|b|26|c|5|s|1088233200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|19|c|1|s|1077177600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|15|c|9|s|1097823600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|31|c|6|s|1091257200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|5|c|6|s|1089010800000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|25|c|7|s|1093417200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|1|c|6|s|1088665200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|21|c|1|s|1077350400000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|5|c|5|s|1086418800000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|10|c|2|s|1078905600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|24|c|1|s|1077609600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|18|c|0|s|1074412800000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|18|c|5|s|1087542000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|16|c|1|s|1076918400000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|2|c|2|s|1078214400000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|10|c|9|s|1097391600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|12|c|4|s|1084345200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|22|c|6|s|1090479600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|30|c|9|s|1099119600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|8|c|6|s|1089270000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|21|c|5|s|1087801200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|18|c|6|s|1090134000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|3|c|2|s|1078300800000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|27|c|5|s|1088319600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|11|c|3|s|1081666800000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|8|c|5|s|1086678000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|8|c|6|s|1089270000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|31|c|0|s|1075536000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|1|c|6|s|1088665200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|14|c|1|s|1013673600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|23|c|5|s|1087974000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|23|c|6|s|1090566000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|22|c|2|s|1079942400000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|9|c|8|s|1094713200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|8|c|1|s|1076227200000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|21|c|4|s|1085122800000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|7|c|6|s|1089183600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|1|c|2|s|1078128000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|30|c|8|s|1096527600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|20|c|4|s|1085036400000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|9|c|5|s|1086764400000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|5|c|8|s|1094367600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|14|c|6|s|1089788400000|", "Europe/Berlin", false);
        testSeriesPattern("t|6|i|1|a|8|b|28|c|5|s|1151478000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|9|c|7|s|145263600000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|28|c|5|s|-79290000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|29|c|7|s|999068400000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|16|c|8|s|1095318000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|21|c|9|s|1098342000000|", "Europe/Berlin", false);
        testSeriesPattern("t|4|i|1|b|2|c|5|s|1180742400000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|27|c|4|s|44150400000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|26|c|11|s|1009324800000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|9|c|11|s|1007856000000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|12|c|11|s|-64886400000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|20|c|11|s|-127267200000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|20|c|11|s|1008806400000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|29|c|10|s|786067200000|", "Europe/Berlin", true);
        testSeriesPattern("t|6|i|1|a|1|b|2|c|4|s|1147564800000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|10|c|7|s|1218326400000|", "Europe/Berlin", true);
        testSeriesPattern("t|4|i|1|b|28|c|5|s|1151478000000|", "Europe/Berlin", false);
    }

    private void testSeriesPattern(String databasePattern, String timeZoneId, boolean allDay) {
        try {
            RecurrenceData recurrenceData = getRecurrenceData(new SeriesPattern(databasePattern), TimeZone.getTimeZone(timeZoneId), allDay);
            RecurrenceUtils.getRecurrenceIterator(recurrenceData, true);
        } catch (Exception e) {
            fail("For " + databasePattern + ": " + e.getMessage());
        }
    }

}
