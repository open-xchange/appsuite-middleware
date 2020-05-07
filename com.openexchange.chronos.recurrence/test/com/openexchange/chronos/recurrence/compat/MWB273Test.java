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
